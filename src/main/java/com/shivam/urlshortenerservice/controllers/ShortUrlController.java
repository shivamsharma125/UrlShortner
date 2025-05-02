package com.shivam.urlshortenerservice.controllers;

import com.shivam.urlshortenerservice.dtos.ShortenUrlRequest;
import com.shivam.urlshortenerservice.dtos.ShortenUrlResponse;
import com.shivam.urlshortenerservice.models.ShortUrl;
import com.shivam.urlshortenerservice.services.IShortUrlService;
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
    @Value("${app.base-url}")
    private String baseUrl;

    @PostMapping("/shorten")
    public ResponseEntity<ShortenUrlResponse> shortenUrl(@RequestBody ShortenUrlRequest request) {
        ShortenUrlResponse response = new ShortenUrlResponse();

        ShortUrl shortUrl = shortUrlService.createShortUrl(
                request.getOriginalUrl(),
                request.getCustomAlias(),
                request.getExpirationDate()
        );

        response.setShortUrl(baseUrl + shortUrl.getShortCode());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<String> redirectToOriginalUrl(@PathVariable String shortCode) {
        ShortUrl shortUrl = shortUrlService.getOriginalUrl(shortCode);

        MultiValueMap<String,String> headers = new LinkedMultiValueMap<>();
        headers.add("Location", shortUrl.getOriginalUrl());

        return new ResponseEntity<>("Redirecting to: " + shortUrl.getOriginalUrl(), headers, HttpStatus.FOUND);
    }
}

