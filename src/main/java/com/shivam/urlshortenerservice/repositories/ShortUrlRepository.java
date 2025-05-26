package com.shivam.urlshortenerservice.repositories;

import com.shivam.urlshortenerservice.models.ShortUrl;
import com.shivam.urlshortenerservice.models.State;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {
    Optional<ShortUrl> findByShortCodeAndState(String shortCode, State state);

    boolean existsByShortCodeAndState(String shortCode, State state);

    Page<ShortUrl> findAllByCreatedBy_EmailAndState(String email, State state, Pageable pageable);
}
