package com.shivam.urlshortenerservice.services;

import com.shivam.urlshortenerservice.exceptions.ShortCodeNotFoundException;
import com.shivam.urlshortenerservice.models.ClickEvent;
import com.shivam.urlshortenerservice.models.ShortUrl;
import com.shivam.urlshortenerservice.repositories.ClickEventRepository;
import com.shivam.urlshortenerservice.repositories.ShortUrlRepository;
import lombok.RequiredArgsConstructor;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClickEventService implements IClickEventService {

    private final Logger LOGGER = LoggerFactory.getLogger(ClickEventService.class);

    private final ClickEventRepository clickEventRepository;
    private final ShortUrlRepository shortUrlRepository;
    private final UserAgentAnalyzer userAgentAnalyzer;

    @Override
    public void logClick(String shortCode, String ipAddress, String userAgentString, String referrer) {
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortCodeNotFoundException("Short URL not found"));

        UserAgent userAgent = userAgentAnalyzer.parse(userAgentString);
        referrer = (referrer == null || referrer.isBlank()) ? "Direct" : referrer;

        ClickEvent clickEvent = ClickEvent.builder()
                .clickedAt(new Date())
                .ipAddress(ipAddress)
                .browser(userAgent.getValue(UserAgent.AGENT_NAME))
                .operatingSystem(userAgent.getValue(UserAgent.OPERATING_SYSTEM_NAME))
                .deviceType(userAgent.getValue(UserAgent.DEVICE_CLASS))
                .referrer(referrer)
                .shortUrl(shortUrl)
                .build();

        clickEventRepository.save(clickEvent);
    }

    @Override
    public List<ClickEvent> getClickEvents(String shortCode) {
        if (!shortUrlRepository.existsByShortCode(shortCode))
                throw new ShortCodeNotFoundException("Short URL not found");

        return clickEventRepository.findByShortUrl_ShortCode(shortCode);
    }

    @Override
    public long getClickCount(String shortCode) {
        if (!shortUrlRepository.existsByShortCode(shortCode))
            throw new ShortCodeNotFoundException("Short URL not found");

        return clickEventRepository.countByShortCode(shortCode);
    }
}

