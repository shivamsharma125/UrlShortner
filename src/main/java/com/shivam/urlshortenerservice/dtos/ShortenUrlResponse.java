package com.shivam.urlshortenerservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShortenUrlResponse {
    private String shortUrl;
    private String originalUrl;
    private Date createdAt;
    private Date expiredAt;
}
