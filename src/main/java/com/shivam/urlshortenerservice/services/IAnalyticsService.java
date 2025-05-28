package com.shivam.urlshortenerservice.services;

import com.shivam.urlshortenerservice.dtos.ClickStatsResponse;
import com.shivam.urlshortenerservice.dtos.TopClickedResponse;
import com.shivam.urlshortenerservice.models.ClickEvent;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IAnalyticsService {
    void logClick(String shortCode, String ipAddress, String userAgent, String referrer);
    long getClickCount(String shortCode, String userEmail);
    Page<ClickEvent> getFilteredClickEvents(String shortCode, String startDate, String endDate, String browser, String os,
                                            String deviceType, int page, int size, String sort, String sortDirection, String userEmail);
    List<ClickStatsResponse> getDailyClickStats(String shortCode, String userEmail);
    List<ClickStatsResponse> getStatsInDateRange(String shortCode, String start, String end, String userEmail);
    List<TopClickedResponse> getTopClickedUrls(int count);
}

