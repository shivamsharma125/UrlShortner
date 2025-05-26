package com.shivam.urlshortenerservice.services;

import com.shivam.urlshortenerservice.exceptions.ExpiredShortCodeException;
import com.shivam.urlshortenerservice.exceptions.ShortCodeNotFoundException;
import com.shivam.urlshortenerservice.models.ShortUrl;
import com.shivam.urlshortenerservice.models.State;
import com.shivam.urlshortenerservice.repositories.ShortUrlRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class RedirectionService implements IRedirectionService {
    private final StringRedisTemplate redisTemplate;
    private final ShortUrlRepository shortUrlRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(RedirectionService.class);

    public RedirectionService(StringRedisTemplate redisTemplate, ShortUrlRepository shortUrlRepository) {
        this.redisTemplate = redisTemplate;
        this.shortUrlRepository = shortUrlRepository;
    }

    @Override
    public String getOriginalUrl(String shortCode) {
        // Check shortCode in Redis first
        String cachedUrl = redisTemplate.opsForValue().get(shortCode);
        if (cachedUrl != null) {
            LOGGER.debug("Returned long url from cache : {}", cachedUrl);
            return cachedUrl;
        }

        ShortUrl shortUrl = shortUrlRepository.findByShortCodeAndState(shortCode, State.ACTIVE)
                .orElseThrow(() -> new ShortCodeNotFoundException("Short URL does not exist or deleted"));

        if (shortUrl.getExpiresAt() != null && shortUrl.getExpiresAt().before(new Date())) {
            throw new ExpiredShortCodeException("Short URL has expired");
        }

        // Save in Redis
        long ttl = (shortUrl.getExpiresAt().getTime() - System.currentTimeMillis()) / 1000;
        redisTemplate.opsForValue().set(shortCode, shortUrl.getOriginalUrl(), ttl, TimeUnit.SECONDS);

        LOGGER.debug("Returned long url from DB : {}", shortUrl.getShortCode());
        return shortUrl.getOriginalUrl();
    }
}
