package com.shivam.urlshortenerservice.services;

import com.shivam.urlshortenerservice.exceptions.ExpiredShortCodeException;
import com.shivam.urlshortenerservice.exceptions.InvalidDateFormatException;
import com.shivam.urlshortenerservice.exceptions.ShortCodeAlreadyExistException;
import com.shivam.urlshortenerservice.exceptions.ShortCodeNotFoundException;
import com.shivam.urlshortenerservice.models.ShortUrl;
import com.shivam.urlshortenerservice.repositories.ShortUrlRepository;
import com.shivam.urlshortenerservice.utils.ShortCodeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class ShortUrlService implements IShortUrlService {

    private final ShortUrlRepository shortUrlRepository;
    private final StringRedisTemplate redisTemplate;

    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final Logger LOGGER = LoggerFactory.getLogger(ShortUrlService.class);

    public ShortUrlService(ShortUrlRepository shortUrlRepository, StringRedisTemplate redisTemplate) {
        this.shortUrlRepository = shortUrlRepository;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public ShortUrl createShortUrl(String originalUrl, String alias, String expirationDate) {

        String shortCode = alias != null && !alias.isBlank() ? alias : generateUniqueCode();
        Date expiresAt = getExpirationDate(expirationDate);

        if (shortUrlRepository.existsByShortCode(shortCode)) {
            throw new ShortCodeAlreadyExistException("Provided custom alias already exists.");
        }

        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setOriginalUrl(originalUrl);
        shortUrl.setShortCode(shortCode);
        shortUrl.setExpiresAt(expiresAt);

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

    private String generateUniqueCode() {
        String code;
        do {
            code = ShortCodeUtil.generateRandomCode(6);
        } while (shortUrlRepository.existsByShortCode(code));
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
}
