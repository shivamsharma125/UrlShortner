package com.shivam.urlshortenerservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShortUrlRequest {
    private String originalUrl;
    private String customAlias; // optional
    // format: yyyy-MM-dd HH:mm:ss
    private String expiresAt; // optional
}
