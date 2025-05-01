package com.shivam.urlshortenerservice.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "short_urls", indexes = @Index(columnList = "shortCode"))
public class ShortUrl extends BaseModel {
    @Column(nullable = false, length = 2048)
    private String originalUrl;
    @Column(nullable = false, unique = true, length = 20)
    private String shortCode;
    @Column
    private Date expiresAt;
}


