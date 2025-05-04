package com.shivam.urlshortenerservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClickStatsPerDayResponse {
    private String shortCode;
    private String date;
    private long count;
}

