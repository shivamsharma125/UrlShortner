package com.shivam.urlshortenerservice.models;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "click_events")
public class ClickEvent extends BaseModel {
    private String ipAddress;
    private String browser;
    private String operatingSystem;
    private String deviceType;
    private String referrer;
    private Date clickedAt;
    @ManyToOne
    @JoinColumn(name = "short_url_id")
    private ShortUrl shortUrl;
}
