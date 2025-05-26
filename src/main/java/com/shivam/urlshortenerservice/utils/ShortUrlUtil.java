package com.shivam.urlshortenerservice.utils;

import com.shivam.urlshortenerservice.dtos.ShortUrlResponse;
import com.shivam.urlshortenerservice.models.ShortUrl;

import java.security.SecureRandom;

public class ShortUrlUtil {
    private static final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final SecureRandom random = new SecureRandom();
    private static final String baseUrl = "http://localhost:8080/";

    public static String generateRandomCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(BASE62.charAt(random.nextInt(BASE62.length())));
        }
        return sb.toString();
    }

    public static ShortUrlResponse from(ShortUrl shortUrl){
        ShortUrlResponse response = new ShortUrlResponse();
        response.setShortUrl(baseUrl + shortUrl.getShortCode());
        response.setOriginalUrl(shortUrl.getOriginalUrl());
        response.setCreatedAt(DateUtils.formatDate(shortUrl.getCreatedAt()));
        response.setExpiredAt(DateUtils.formatDate(shortUrl.getExpiresAt()));
        return response;
    }
}
