package com.shivam.urlshortenerservice.services;

import com.shivam.urlshortenerservice.models.ClickEvent;

import java.util.List;

public interface IClickEventService {
    void logClick(String shortCode, String ipAddress, String userAgent, String referrer);
    List<ClickEvent> getClickEvents(String shortCode);
    long getClickCount(String shortCode);
}

