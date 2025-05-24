package com.shivam.urlshortenerservice.services;

import com.shivam.urlshortenerservice.dtos.ClickStatsResponse;
import com.shivam.urlshortenerservice.exceptions.InvalidDateFormatException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
    public long getClickCount(String shortCode) {
        if (!shortUrlRepository.existsByShortCode(shortCode))
            throw new ShortCodeNotFoundException("Short URL not found");

        return clickEventRepository.countByShortCode(shortCode);
    }

    @Override
    public Page<ClickEvent> getFilteredClickEvents(String shortCode, String startDate, String endDate, String browser, String os,
                                                   String deviceType, int page, int size, String sort, String sortDirection) {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date start = null, end = null;
        try {
            start = (startDate != null) ? formatter.parse(startDate) : new Date(0); // from epoch
            end = (endDate != null) ? formatter.parse(endDate) : new Date(); // now
        } catch (ParseException e) {
            throw new InvalidDateFormatException("Invalid date format. Use yyyy-MM-dd HH:mm:ss");
        }

        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));

        return clickEventRepository.findFilteredClickEvents(
                shortCode, start, end,
                browser != null ? browser : "",
                os != null ? os : "",
                deviceType != null ? deviceType : "",
                pageable
        );
    }

    @Override
    public List<ClickStatsResponse> getDailyClickStats(String shortCode) {
        List<Object[]> dailyClickStats = clickEventRepository.getClickCountsPerDay(shortCode);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        return dailyClickStats.stream()
                .map(dailyClickStat -> new ClickStatsResponse(
                        shortCode,
                        formatter.format(dailyClickStat[0]),
                        (Long) dailyClickStat[1]
                ))
                .collect(Collectors.toList());
    }

    public List<ClickStatsResponse> getStatsInDateRange(String shortCode, String start, String end) {
        Date startDate = null, endDate = null;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        try {
            startDate = formatter.parse(start);
            endDate = formatter.parse(end);
        } catch (ParseException e) {
            throw new InvalidDateFormatException("Invalid date format. Use yyyy-MM-dd");
        }

        List<Object[]> clickStats = clickEventRepository
                .getClickCountsPerDayBetweenDates(shortCode, startDate, endDate);

        return clickStats.stream()
                .map(clickStat -> new ClickStatsResponse(
                        shortCode,
                        formatter.format(clickStat[0]),
                        (Long) clickStat[1])
                )
                .collect(Collectors.toList());
    }

}

