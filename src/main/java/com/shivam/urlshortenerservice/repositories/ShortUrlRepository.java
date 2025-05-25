package com.shivam.urlshortenerservice.repositories;

import com.shivam.urlshortenerservice.models.ShortUrl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {
    Optional<ShortUrl> findByShortCode(String shortCode);
    boolean existsByShortCode(String shortCode);
    Page<ShortUrl> findByCreatedBy_Email(String email, Pageable pageable);
    void deleteByShortCode(String shortCode);
}
