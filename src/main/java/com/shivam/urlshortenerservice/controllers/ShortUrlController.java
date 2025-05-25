package com.shivam.urlshortenerservice.controllers;

import com.shivam.urlshortenerservice.dtos.ShortenUrlRequest;
import com.shivam.urlshortenerservice.dtos.ShortenUrlResponse;
import com.shivam.urlshortenerservice.models.ShortUrl;
import com.shivam.urlshortenerservice.services.IClickEventService;
import com.shivam.urlshortenerservice.services.IShortUrlService;
import com.shivam.urlshortenerservice.utils.DateUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
public class ShortUrlController {

    private final IShortUrlService shortUrlService;
    private final IClickEventService clickEventService;
    @Value("${app.base-url}")
    private String baseUrl;

    public ShortUrlController(IShortUrlService shortUrlService, IClickEventService clickEventService) {
        this.shortUrlService = shortUrlService;
        this.clickEventService = clickEventService;
    }

    @PostMapping("/shortener/shorten")
    public ResponseEntity<ShortenUrlResponse> shortenUrl(@RequestBody ShortenUrlRequest request,
                                                         @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername();

        ShortUrl shortUrl = shortUrlService.createShortUrl(
                request.getOriginalUrl(),
                request.getCustomAlias(),
                request.getExpiresAt(),
                email
        );



        return new ResponseEntity<>(from(shortUrl), HttpStatus.CREATED);
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

    @GetMapping("shortener/shorturls")
    public ResponseEntity<List<ShortenUrlResponse>> getUserShortUrls(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        String email = authentication.getName();

        Page<ShortUrl> shortUrlPage = shortUrlService.getShortUrlsForUser(email,page,size);

        List<ShortenUrlResponse> response = shortUrlPage.stream()
                .map(this::from)
                .toList();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/shortener/{shortCode}")
    public ResponseEntity<Void> deleteShortUrl(@PathVariable String shortCode,
                                               Authentication authentication) {
        String email = authentication.getName();
        shortUrlService.deleteShortUrl(shortCode, email);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private ShortenUrlResponse from(ShortUrl shortUrl){
        ShortenUrlResponse response = new ShortenUrlResponse();
        response.setShortUrl(baseUrl + shortUrl.getShortCode());
        response.setOriginalUrl(shortUrl.getOriginalUrl());
        response.setCreatedAt(DateUtils.formatDate(shortUrl.getCreatedAt()));
        response.setExpiredAt(DateUtils.formatDate(shortUrl.getExpiresAt()));
        return response;
    }
}

