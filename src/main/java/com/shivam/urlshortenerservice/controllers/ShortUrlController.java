package com.shivam.urlshortenerservice.controllers;

import com.shivam.urlshortenerservice.dtos.ShortUrlRequest;
import com.shivam.urlshortenerservice.dtos.ShortUrlResponse;
import com.shivam.urlshortenerservice.models.ShortUrl;
import com.shivam.urlshortenerservice.services.IShortUrlService;
import com.shivam.urlshortenerservice.utils.ShortUrlUtil;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import static com.shivam.urlshortenerservice.utils.ShortUrlUtil.from;

@RestController
@RequestMapping("/shorten")
public class ShortUrlController {

    private final IShortUrlService shortUrlService;

    public ShortUrlController(IShortUrlService shortUrlService) {
        this.shortUrlService = shortUrlService;
    }

    @PostMapping
    public ResponseEntity<ShortUrlResponse> shortenUrl(@RequestBody ShortUrlRequest request,
                                                       Authentication authentication) {

        String email = authentication.getName();

        ShortUrl shortUrl = shortUrlService.createShortUrl(request.getOriginalUrl(),
                request.getCustomAlias(), request.getExpiresAt(), email);

        return new ResponseEntity<>(from(shortUrl), HttpStatus.CREATED);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<ShortUrlResponse> getShortUrl(@PathVariable String shortCode,
                                                        Authentication authentication){
        String email = authentication.getName();
        ShortUrl shortUrl = shortUrlService.getShortUrl(shortCode,email);
        return ResponseEntity.ok(from(shortUrl));
    }

    @DeleteMapping("/{shortCode}")
    public ResponseEntity<Void> deleteShortUrl(@PathVariable String shortCode,
                                               Authentication authentication) {
        String email = authentication.getName();
        shortUrlService.deleteShortUrl(shortCode, email);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/my")
    public ResponseEntity<Page<ShortUrlResponse>> getUserShortUrls(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        String email = authentication.getName();

        Page<ShortUrl> shortUrlPage = shortUrlService.getShortUrlsForUser(email,page,size);

        Page<ShortUrlResponse> shortUrlResponsePage = shortUrlPage.map(ShortUrlUtil::from);

        return ResponseEntity.ok(shortUrlResponsePage);
    }

    @GetMapping("/admin/urls")
    public ResponseEntity<Page<ShortUrlResponse>> getAllUrls(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ShortUrl> shortUrlPage = shortUrlService.getAllShortUrls(page, size);
        Page<ShortUrlResponse> shortUrlResponsePage = shortUrlPage.map(ShortUrlUtil::from);
        return ResponseEntity.ok(shortUrlResponsePage);
    }

    @DeleteMapping("/admin/{shortCode}")
    public ResponseEntity<Void> deleteAsAdmin(@PathVariable String shortCode,
                                              Authentication authentication) {
        String adminEmail = authentication.getName();
        shortUrlService.deleteShortUrlAsAdmin(shortCode, adminEmail);
        return ResponseEntity.noContent().build();
    }
}

