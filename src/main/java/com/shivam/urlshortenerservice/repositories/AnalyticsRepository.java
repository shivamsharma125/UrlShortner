package com.shivam.urlshortenerservice.repositories;

import com.shivam.urlshortenerservice.models.ClickEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface AnalyticsRepository extends JpaRepository<ClickEvent, Long> {
    @Query("SELECT COUNT(e) FROM ClickEvent e WHERE e.shortUrl.shortCode = :shortCode")
    long countByShortCode(String shortCode);

    @Query("SELECT c FROM ClickEvent c " +
            "WHERE c.shortUrl.shortCode = :shortCode " +
            "AND c.clickedAt BETWEEN :startDate AND :endDate " +
            "AND LOWER(c.browser) LIKE LOWER(CONCAT('%', :browser, '%')) " +
            "AND LOWER(c.operatingSystem) LIKE LOWER(CONCAT('%', :os, '%')) " +
            "AND LOWER(c.deviceType) LIKE LOWER(CONCAT('%', :deviceType, '%'))")
    Page<ClickEvent> findFilteredClickEvents(String shortCode, Date startDate, Date endDate, String browser,
                                             String os, String deviceType, Pageable pageable);

    @Query("SELECT FUNCTION('DATE', c.clickedAt), COUNT(c) " +
            "FROM ClickEvent c WHERE c.shortUrl.shortCode = :shortCode " +
            "GROUP BY FUNCTION('DATE', c.clickedAt) ORDER BY FUNCTION('DATE', c.clickedAt) DESC")
    List<Object[]> getClickCountsPerDay(String shortCode);

    @Query("SELECT FUNCTION('DATE', c.clickedAt), COUNT(c) " +
            "FROM ClickEvent c " +
            "WHERE c.shortUrl.shortCode = :shortCode " +
            "AND c.clickedAt BETWEEN :startDate AND :endDate " +
            "GROUP BY FUNCTION('DATE', c.clickedAt) " +
            "ORDER BY FUNCTION('DATE', c.clickedAt) ASC")
    List<Object[]> getClickCountsPerDayBetweenDates(String shortCode, Date startDate, Date endDate);

    @Query("SELECT c.shortUrl.shortCode, COUNT(c) " +
            "FROM ClickEvent c " +
            "GROUP BY c.shortUrl.shortCode " +
            "ORDER BY COUNT(c) DESC " +
            "LIMIT :count")
    List<Object[]> findTopClickedUrls(int count);
}

