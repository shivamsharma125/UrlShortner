package com.shivam.urlshortenerservice.services;

import com.shivam.urlshortenerservice.exceptions.ForbiddenOperationException;
import com.shivam.urlshortenerservice.exceptions.InvalidDateFormatException;
import com.shivam.urlshortenerservice.exceptions.ShortCodeAlreadyExistException;
import com.shivam.urlshortenerservice.exceptions.ShortCodeNotFoundException;
import com.shivam.urlshortenerservice.models.ShortUrl;
import com.shivam.urlshortenerservice.models.State;
import com.shivam.urlshortenerservice.models.User;
import com.shivam.urlshortenerservice.repositories.ShortUrlRepository;
import com.shivam.urlshortenerservice.repositories.UserRepository;
import com.shivam.urlshortenerservice.utils.ShortUrlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

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

        if (shortUrlRepository.existsByShortCodeAndState(shortCode,State.ACTIVE)) {
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
    public ShortUrl getShortUrl(String shortCode, String userEmail) {
        ShortUrl shortUrl = checkForShortCodeOwnership(shortCode,userEmail);

        // Cache in Redis
        long ttl = (shortUrl.getExpiresAt().getTime() - System.currentTimeMillis()) / 1000;
        redisTemplate.opsForValue().set(shortCode, shortUrl.getOriginalUrl(), ttl, TimeUnit.SECONDS);

        return shortUrl;
    }

    @Override
    public Page<ShortUrl> getShortUrlsForUser(String email, int page, int size) {
        Pageable pageable = PageRequest.of(page,size, Sort.by("createdAt").descending());
        return shortUrlRepository.findAllByCreatedBy_EmailAndState(email,State.ACTIVE,pageable);
    }

    @Override
    public void deleteShortUrl(String shortCode, String userEmail) {
        ShortUrl shortUrl = checkForShortCodeOwnership(shortCode,userEmail);

        shortUrl.setState(State.DELETED);

        // Delete from cache
        redisTemplate.delete(shortCode);

        shortUrlRepository.save(shortUrl);
    }

    @Override
    public Page<ShortUrl> getAllShortUrls(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page,size);
        return shortUrlRepository.findAllByState(State.ACTIVE,pageRequest);
    }

    @Override
    public void deleteShortUrlAsAdmin(String shortCode, String adminEmail) {
        ShortUrl shortUrl = shortUrlRepository.findByShortCodeAndState(shortCode,State.ACTIVE)
                .orElseThrow(() -> new ShortCodeNotFoundException("Short URL does not exist or deleted"));

        shortUrl.setState(State.DELETED);

        // Delete from cache
        redisTemplate.delete(shortCode);

        shortUrlRepository.save(shortUrl);
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = ShortUrlUtil.generateRandomCode(6);
        } while (shortUrlRepository.existsByShortCodeAndState(code,State.ACTIVE));
        return code;
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

    private ShortUrl checkForShortCodeOwnership(String shortCode, String userEmail){
        ShortUrl shortUrl = shortUrlRepository.findByShortCodeAndState(shortCode,State.ACTIVE)
                .orElseThrow(() -> new ShortCodeNotFoundException("Short URL does not exist or deleted"));

        if(!shortUrl.getCreatedBy().getEmail().equals(userEmail)){
            throw new ForbiddenOperationException("user is not the owner of this url");
        }

        return shortUrl;
    }
}
