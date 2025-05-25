package com.shivam.urlshortenerservice.services;

import com.shivam.urlshortenerservice.models.ShortUrl;

public interface IShortUrlService {
    ShortUrl createShortUrl(String originalUrl, String alias, String expirationDate, String userEmail);
    ShortUrl getOriginalUrl(String shortUrl);
}

