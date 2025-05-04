package com.shivam.urlshortenerservice.controllers;

import com.shivam.urlshortenerservice.dtos.AnalyticsResponse;
import com.shivam.urlshortenerservice.dtos.ClickEventDto;
import com.shivam.urlshortenerservice.dtos.ClickStatsDto;
import com.shivam.urlshortenerservice.models.ClickEvent;
import com.shivam.urlshortenerservice.services.IClickEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.List;

@RestController
@RequestMapping("/shortener/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final IClickEventService clickEventService;

    @GetMapping("/{shortCode}/click-count")
    public ResponseEntity<AnalyticsResponse> getAnalytics(@PathVariable String shortCode) {
        long clickCount = clickEventService.getClickCount(shortCode);

        AnalyticsResponse response = new AnalyticsResponse();
        response.setShortCode(shortCode);
        response.setTotalClicks(clickCount);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{shortCode}/click-events")
    public ResponseEntity<Page<ClickEventDto>> getFilteredClickEvents(
            @PathVariable String shortCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "clickedAt") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String browser,
            @RequestParam(required = false) String os,
            @RequestParam(required = false) String deviceType
    ) {
        Page<ClickEvent> clickEvents = clickEventService
                .getFilteredClickEvents(shortCode, startDate, endDate, browser, os, deviceType, page, size, sort, direction);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Page<ClickEventDto> responsePage = clickEvents.map(event ->
                new ClickEventDto(
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
    public ResponseEntity<List<ClickStatsDto>> getDailyClickStats(@PathVariable String shortCode) {
        return new ResponseEntity<>(clickEventService.getDailyClickStats(shortCode), HttpStatus.OK);
    }

    @GetMapping("/{shortCode}/click-events/range")
    public ResponseEntity<List<ClickStatsDto>> getClickStatsInRange(
            @PathVariable String shortCode,
            @RequestParam String start, // Format : yyyy-MM-dd
            @RequestParam String end // Format : yyyy-MM-dd
    ) {
        List<ClickStatsDto> clickStats = clickEventService.getStatsInDateRange(shortCode, start, end);
        return new ResponseEntity<>(clickStats, HttpStatus.OK);
    }

}

