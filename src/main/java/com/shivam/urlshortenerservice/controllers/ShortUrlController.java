package com.shivam.urlshortenerservice.controllers;

import com.shivam.urlshortenerservice.dtos.ShortenUrlRequest;
import com.shivam.urlshortenerservice.dtos.ShortenUrlResponse;
import com.shivam.urlshortenerservice.models.ShortUrl;
import com.shivam.urlshortenerservice.services.IClickEventService;
import com.shivam.urlshortenerservice.services.IShortUrlService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ShortUrlController {

    private final IShortUrlService shortUrlService;
    private final IClickEventService clickEventService;
    @Value("${app.base-url}")
    private String baseUrl;

    @PostMapping("/shortener/shorten")
    public ResponseEntity<ShortenUrlResponse> shortenUrl(@RequestBody ShortenUrlRequest request) {
        ShortenUrlResponse response = new ShortenUrlResponse();

        ShortUrl shortUrl = shortUrlService.createShortUrl(
                request.getOriginalUrl(),
                request.getCustomAlias(),
                request.getExpiresAt()
        );

        response.setShortUrl(baseUrl + shortUrl.getShortCode());
        response.setOriginalUrl(shortUrl.getOriginalUrl());
        response.setCreatedAt(shortUrl.getCreatedAt());
        response.setExpiredAt(shortUrl.getExpiresAt());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<String> redirectToOriginalUrl(@PathVariable String shortCode,
                                                        HttpServletRequest request) {
        ShortUrl shortUrl = shortUrlService.getOriginalUrl(shortCode);

        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        String referrer = request.getHeader("Referer");

        clickEventService.logClick(shortCode,ipAddress,userAgent,referrer);

        MultiValueMap<String,String> headers = new LinkedMultiValueMap<>();
        headers.add("Location", shortUrl.getOriginalUrl());

        return new ResponseEntity<>("Redirecting to: " + shortUrl.getOriginalUrl(), headers, HttpStatus.FOUND);
    }
}

