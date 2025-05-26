package com.shivam.urlshortenerservice.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "short_urls", indexes = @Index(columnList = "shortCode"))
public class ShortUrl extends BaseModel {
    @Column(nullable = false, length = 2048)
    private String originalUrl;
    @Column(nullable = false, unique = true, length = 20)
    private String shortCode;
    @Column(nullable = false)
    private Date expiresAt;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User createdBy; // [M:1]
}


