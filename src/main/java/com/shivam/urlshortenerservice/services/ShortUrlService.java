package com.shivam.urlshortenerservice.services;

import com.shivam.urlshortenerservice.exceptions.*;
import com.shivam.urlshortenerservice.models.ShortUrl;
import com.shivam.urlshortenerservice.models.User;
import com.shivam.urlshortenerservice.repositories.ShortUrlRepository;
import com.shivam.urlshortenerservice.repositories.UserRepository;
import com.shivam.urlshortenerservice.utils.ShortCodeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class ShortUrlService implements IShortUrlService {

    private final ShortUrlRepository shortUrlRepository;
    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;

    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final Logger LOGGER = LoggerFactory.getLogger(ShortUrlService.class);

    public ShortUrlService(ShortUrlRepository shortUrlRepository, StringRedisTemplate redisTemplate,
                           UserRepository userRepository) {
        this.shortUrlRepository = shortUrlRepository;
        this.redisTemplate = redisTemplate;
        this.userRepository = userRepository;
    }

    @Override
    public ShortUrl createShortUrl(String originalUrl, String alias, String expirationDate, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("user not found"));

        String shortCode = alias != null && !alias.isBlank() ? alias : generateUniqueCode();
        Date expiresAt = getExpirationDate(expirationDate);

        if (shortUrlRepository.existsByShortCode(shortCode)) {
            throw new ShortCodeAlreadyExistException("Provided custom alias already exists.");
        }

        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setOriginalUrl(originalUrl);
        shortUrl.setShortCode(shortCode);
        shortUrl.setExpiresAt(expiresAt);
        shortUrl.setCreatedBy(user);

        shortUrl = shortUrlRepository.save(shortUrl);

        // Cache in Redis
        long ttl = (expiresAt.getTime() - System.currentTimeMillis()) / 1000;
        redisTemplate.opsForValue().set(shortCode, originalUrl, ttl, TimeUnit.SECONDS);

        return shortUrl;
    }

    @Override
    public ShortUrl getOriginalUrl(String shortCode) {
        // Check shortCode in Redis first
        String cachedUrl = redisTemplate.opsForValue().get(shortCode);
        if (cachedUrl != null) {
            LOGGER.debug("Returned long url from cache : {}", cachedUrl);
            ShortUrl shortUrl = new ShortUrl();
            shortUrl.setOriginalUrl(cachedUrl);
            shortUrl.setShortCode(shortCode);

            return shortUrl;
        }

        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortCodeNotFoundException("Short URL not found"));

        if (shortUrl.getExpiresAt() != null && shortUrl.getExpiresAt().before(new Date())) {
            throw new ExpiredShortCodeException("Short URL has expired");
        }

        // Save in Redis
        long ttl = (shortUrl.getExpiresAt().getTime() - System.currentTimeMillis()) / 1000;
        redisTemplate.opsForValue().set(shortCode, shortUrl.getOriginalUrl(), ttl, TimeUnit.SECONDS);

        LOGGER.debug("Returned long url from DB : {}", shortUrl.getShortCode());
        return shortUrl;
    }

    @Override
    public Page<ShortUrl> getShortUrlsForUser(String email, int page, int size) {
        Pageable pageable = PageRequest.of(page,size, Sort.by("createdAt").descending());
        return shortUrlRepository.findByCreatedBy_Email(email,pageable);
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = ShortCodeUtil.generateRandomCode(6);
        } while (shortUrlRepository.existsByShortCode(code));
        return code;
    }

    @Transactional
    @Override
    public void deleteShortUrl(String shortCode, String userEmail) {
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortCodeNotFoundException("Short URL not found"));

        if(!shortUrl.getCreatedBy().getEmail().equals(userEmail)){
            throw new ForbiddenOperationException("user is not allowed to delete this url");
        }

        shortUrlRepository.deleteByShortCode(shortCode);
    }

    private Date getExpirationDate(String expirationDate) {
        Date expiresAt;

        if (expirationDate != null && !expirationDate.isBlank()) {
            try {
                expiresAt = formatter.parse(expirationDate);
            } catch (ParseException e) {
                throw new InvalidDateFormatException("Invalid expiration date format. Use 'yyyy-MM-dd HH:mm:ss'.");
            }
        } else {
            expiresAt = new Date(System.currentTimeMillis() + (15L * 24 * 60 * 60 * 1000)); // default 15 days
        }

        return expiresAt;
    }
}
