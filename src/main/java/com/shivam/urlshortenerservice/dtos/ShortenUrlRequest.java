package com.shivam.urlshortenerservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShortenUrlRequest {
    private String originalUrl;
    private String customAlias; // optional
    // Date Format : "YYYY-MM-DD 00:00:00"
    private String expirationDate; // optional
}
