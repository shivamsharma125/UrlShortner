package com.shivam.urlshortenerservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClickEventDto {
    private String ipAddress;
    private String browser;
    private String operatingSystem;
    private String deviceType;
    private String referrer;
    private String clickedAt;
}

