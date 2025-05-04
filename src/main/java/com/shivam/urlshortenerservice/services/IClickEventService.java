package com.shivam.urlshortenerservice.services;

import com.shivam.urlshortenerservice.dtos.ClickStatsDto;
import com.shivam.urlshortenerservice.models.ClickEvent;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IClickEventService {
    void logClick(String shortCode, String ipAddress, String userAgent, String referrer);
    long getClickCount(String shortCode);
    Page<ClickEvent> getFilteredClickEvents(String shortCode, String startDate, String endDate, String browser, String os,
                                            String deviceType, int page, int size, String sort, String sortDirection);
    List<ClickStatsDto> getDailyClickStats(String shortCode);
    List<ClickStatsDto> getStatsInDateRange(String shortCode, String start, String end);
}

