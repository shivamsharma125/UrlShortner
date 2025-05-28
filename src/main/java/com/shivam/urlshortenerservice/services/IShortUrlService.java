package com.shivam.urlshortenerservice.services;

import com.shivam.urlshortenerservice.models.ShortUrl;
import org.springframework.data.domain.Page;

public interface IShortUrlService {
    ShortUrl createShortUrl(String originalUrl, String alias, String expirationDate, String userEmail);
    ShortUrl getShortUrl(String shortCode, String userEmail);
    Page<ShortUrl> getShortUrlsForUser(String email, int page, int size);
    void deleteShortUrl(String shortCode, String userEmail);
    Page<ShortUrl> getAllShortUrls(int page, int size);
    void deleteShortUrlAsAdmin(String shortCode, String adminEmail);
}

