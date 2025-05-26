package com.shivam.urlshortenerservice.controllers;

import com.shivam.urlshortenerservice.services.ClickEventService;
import com.shivam.urlshortenerservice.services.IRedirectionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedirectionController {
    private final IRedirectionService redirectionService;
    private final ClickEventService clickEventService;

    public RedirectionController(IRedirectionService redirectionService, ClickEventService clickEventService) {
        this.redirectionService = redirectionService;
        this.clickEventService = clickEventService;
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<String> redirectToOriginalUrl(@PathVariable String shortCode,
                                                        HttpServletRequest request) {
        String originalUrl = redirectionService.getOriginalUrl(shortCode);

        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        String referrer = request.getHeader("Referer");

        clickEventService.logClick(shortCode,ipAddress,userAgent,referrer);

        MultiValueMap<String,String> headers = new LinkedMultiValueMap<>();
        headers.add("Location", originalUrl);

        return new ResponseEntity<>(headers, HttpStatus.NO_CONTENT);
    }
}
