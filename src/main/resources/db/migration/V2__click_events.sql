CREATE TABLE click_events
(
    id               BIGINT AUTO_INCREMENT NOT NULL,
    created_at       datetime NULL,
    last_modified_at datetime NULL,
    state            SMALLINT NULL,
    ip_address       VARCHAR(255) NULL,
    browser          VARCHAR(255) NULL,
    operating_system VARCHAR(255) NULL,
    device_type      VARCHAR(255) NULL,
    referrer         VARCHAR(255) NULL,
    clicked_at       datetime NULL,
    short_url_id     BIGINT NULL,
    CONSTRAINT pk_clickevent PRIMARY KEY (id)
);

ALTER TABLE click_events
    ADD CONSTRAINT FK_CLICKEVENT_ON_SHORT_URL FOREIGN KEY (short_url_id) REFERENCES short_urls (id);