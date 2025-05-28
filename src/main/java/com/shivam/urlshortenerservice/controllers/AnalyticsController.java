package com.shivam.urlshortenerservice.controllers;

import com.shivam.urlshortenerservice.dtos.AnalyticsResponse;
import com.shivam.urlshortenerservice.dtos.ClickEventResponse;
import com.shivam.urlshortenerservice.dtos.ClickStatsResponse;
import com.shivam.urlshortenerservice.dtos.TopClickedResponse;
import com.shivam.urlshortenerservice.models.ClickEvent;
import com.shivam.urlshortenerservice.services.IAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.List;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final IAnalyticsService clickEventService;

    @GetMapping("/{shortCode}/click-count")
    public ResponseEntity<AnalyticsResponse> getAnalytics(@PathVariable String shortCode,
                                                          Authentication authentication) {
        String email = authentication.getName();
        long clickCount = clickEventService.getClickCount(shortCode,email);

        AnalyticsResponse response = new AnalyticsResponse();
        response.setShortCode(shortCode);
        response.setTotalClicks(clickCount);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{shortCode}/click-events")
    public ResponseEntity<Page<ClickEventResponse>> getFilteredClickEvents(
            @PathVariable String shortCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "clickedAt") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String browser,
            @RequestParam(required = false) String os,
            @RequestParam(required = false) String deviceType,
            Authentication authentication
    ) {
        String email = authentication.getName();
        Page<ClickEvent> clickEvents = clickEventService
                .getFilteredClickEvents(shortCode, startDate, endDate, browser, os, deviceType,
                        page, size, sort, direction,email);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Page<ClickEventResponse> responsePage = clickEvents.map(event ->
                new ClickEventResponse(
                        event.getIpAddress(),
                        event.getBrowser(),
                        event.getOperatingSystem(),
                        event.getDeviceType(),
                        event.getReferrer(),
                        formatter.format(event.getClickedAt())
                ));

        return new ResponseEntity<>(responsePage, HttpStatus.OK);
    }

    @GetMapping("/{shortCode}/click-events/daily")
    public ResponseEntity<List<ClickStatsResponse>> getDailyClickStats(@PathVariable String shortCode,
                                                                       Authentication authentication) {
        String email = authentication.getName();
        List<ClickStatsResponse> responses = clickEventService.getDailyClickStats(shortCode,email);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @GetMapping("/{shortCode}/click-events/range")
    public ResponseEntity<List<ClickStatsResponse>> getClickStatsInRange(
            @PathVariable String shortCode,
            @RequestParam String start, // Format : yyyy-MM-dd
            @RequestParam String end, // Format : yyyy-MM-dd
            Authentication authentication
    ) {
        String email = authentication.getName();
        List<ClickStatsResponse> clickStats = clickEventService
                .getStatsInDateRange(shortCode, start, end, email);
        return new ResponseEntity<>(clickStats, HttpStatus.OK);
    }

    @GetMapping("/admin/top-clicked")
    public ResponseEntity<List<TopClickedResponse>> getTopClickedUrls(
            @RequestParam(defaultValue = "5") int count) {
        return ResponseEntity.ok(clickEventService.getTopClickedUrls(count));
    }
}

