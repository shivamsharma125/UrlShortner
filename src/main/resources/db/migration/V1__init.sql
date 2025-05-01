CREATE TABLE short_urls
(
    id               BIGINT AUTO_INCREMENT NOT NULL,
    created_at       datetime NULL,
    last_modified_at datetime NULL,
    state            SMALLINT NULL,
    original_url     VARCHAR(2048) NOT NULL,
    short_code       VARCHAR(20)   NOT NULL,
    expires_at       datetime NULL,
    CONSTRAINT pk_short_urls PRIMARY KEY (id)
);

ALTER TABLE short_urls
    ADD CONSTRAINT uc_short_urls_shortcode UNIQUE (short_code);

CREATE INDEX idx_f9063c421889c8f4a20b5a6e8 ON short_urls (short_code);