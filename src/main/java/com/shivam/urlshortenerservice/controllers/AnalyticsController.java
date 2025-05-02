package com.shivam.urlshortenerservice.controllers;

import com.shivam.urlshortenerservice.dtos.AnalyticsResponse;
import com.shivam.urlshortenerservice.dtos.ClickEventDto;
import com.shivam.urlshortenerservice.models.ClickEvent;
import com.shivam.urlshortenerservice.services.IClickEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

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
    public ResponseEntity<List<ClickEventDto>> getClickEvents(@PathVariable String shortCode) {
        List<ClickEvent> events = clickEventService.getClickEvents(shortCode);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        List<ClickEventDto> response = events.stream().
                map(event -> new ClickEventDto(
                        event.getIpAddress(),
                        event.getBrowser(),
                        event.getOperatingSystem(),
                        event.getDeviceType(),
                        event.getReferrer(),
                        formatter.format(event.getClickedAt())
                ))
                .collect(Collectors.toList());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

