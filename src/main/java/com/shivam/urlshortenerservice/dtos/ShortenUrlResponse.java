package com.shivam.urlshortenerservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShortenUrlResponse {
    private String shortUrl;
    private String originalUrl;
    private String createdAt;
    private String expiredAt;
}
