package com.shivam.urlshortenerservice.repositories;

import com.shivam.urlshortenerservice.models.ClickEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {
    List<ClickEvent> findByShortUrl_ShortCode(String shortCode);

    @Query("SELECT COUNT(e) FROM ClickEvent e WHERE e.shortUrl.shortCode = :shortCode")
    long countByShortCode(String shortCode);
}

