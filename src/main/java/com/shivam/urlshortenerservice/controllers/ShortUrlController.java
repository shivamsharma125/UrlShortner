package com.shivam.urlshortenerservice.controllers;

import com.shivam.urlshortenerservice.dtos.ShortUrlRequest;
import com.shivam.urlshortenerservice.dtos.ShortUrlResponse;
import com.shivam.urlshortenerservice.exceptions.InvalidRequestException;
import com.shivam.urlshortenerservice.models.ShortUrl;
import com.shivam.urlshortenerservice.services.IAnalyticsService;
import com.shivam.urlshortenerservice.services.IShortUrlService;
import com.shivam.urlshortenerservice.utils.ShortUrlUtil;
import com.shivam.urlshortenerservice.utils.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import static com.shivam.urlshortenerservice.utils.ShortUrlUtil.from;

@RestController
public class ShortUrlController {

    private final IShortUrlService shortUrlService;
    private final IAnalyticsService analyticsService;

    public ShortUrlController(IShortUrlService shortUrlService, IAnalyticsService analyticsService) {
        this.shortUrlService = shortUrlService;
        this.analyticsService = analyticsService;
    }

    @PostMapping("/shorten")
    public ResponseEntity<ShortUrlResponse> shortenUrl(@RequestBody ShortUrlRequest request,
                                                       Authentication authentication) {
        if (StringUtils.isEmpty(request.getOriginalUrl())) throw new InvalidRequestException("Invalid original url");

        String email = authentication.getName();

        ShortUrl shortUrl = shortUrlService.createShortUrl(request.getOriginalUrl(),
                request.getCustomAlias(), request.getExpiresAt(), email);

        return new ResponseEntity<>(from(shortUrl), HttpStatus.CREATED);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<String> redirectToOriginalUrl(@PathVariable String shortCode,
                                                        HttpServletRequest request) {
        if (StringUtils.isEmpty(shortCode)) throw new InvalidRequestException("Invalid short url");

        String originalUrl = shortUrlService.getOriginalUrl(shortCode);

        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        String referrer = request.getHeader("Referer");

        analyticsService.logClick(shortCode,ipAddress,userAgent,referrer);

        MultiValueMap<String,String> headers = new LinkedMultiValueMap<>();
        headers.add("Location", originalUrl);

        return new ResponseEntity<>("Redirecting to: " + originalUrl, headers, HttpStatus.FOUND);
    }

    @GetMapping("/shorten/{shortCode}")
    public ResponseEntity<ShortUrlResponse> getShortUrl(@PathVariable String shortCode,
                                                        Authentication authentication){
        if (StringUtils.isEmpty(shortCode)) throw new InvalidRequestException("Invalid short code");

        String email = authentication.getName();
        ShortUrl shortUrl = shortUrlService.getShortUrl(shortCode,email);
        return ResponseEntity.ok(from(shortUrl));
    }

    @DeleteMapping("/shorten/{shortCode}")
    public ResponseEntity<Void> deleteShortUrl(@PathVariable String shortCode,
                                               Authentication authentication) {
        if (StringUtils.isEmpty(shortCode)) throw new InvalidRequestException("Invalid short code");

        String email = authentication.getName();
        shortUrlService.deleteShortUrl(shortCode, email);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/shorten/my")
    public ResponseEntity<Page<ShortUrlResponse>> getUserShortUrls(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        String email = authentication.getName();

        Page<ShortUrl> shortUrlPage = shortUrlService.getShortUrlsForUser(email,page,size);

        Page<ShortUrlResponse> shortUrlResponsePage = shortUrlPage.map(ShortUrlUtil::from);

        return ResponseEntity.ok(shortUrlResponsePage);
    }

    @GetMapping("/shorten/admin/urls")
    public ResponseEntity<Page<ShortUrlResponse>> getAllUrls(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ShortUrl> shortUrlPage = shortUrlService.getAllShortUrls(page, size);
        Page<ShortUrlResponse> shortUrlResponsePage = shortUrlPage.map(ShortUrlUtil::from);
        return ResponseEntity.ok(shortUrlResponsePage);
    }

    @DeleteMapping("/shorten/admin/{shortCode}")
    public ResponseEntity<Void> deleteAsAdmin(@PathVariable String shortCode,
                                              Authentication authentication) {
        if (StringUtils.isEmpty(shortCode)) throw new InvalidRequestException("Invalid short code");

        String adminEmail = authentication.getName();
        shortUrlService.deleteShortUrlAsAdmin(shortCode, adminEmail);
        return ResponseEntity.noContent().build();
    }
}

