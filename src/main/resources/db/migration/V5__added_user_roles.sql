CREATE TABLE roles
(
    id               BIGINT AUTO_INCREMENT NOT NULL,
    created_at       datetime              NULL,
    last_modified_at datetime              NULL,
    state            SMALLINT              NULL,
    name             VARCHAR(255)          NULL,
    CONSTRAINT pk_roles PRIMARY KEY (id)
);

CREATE TABLE users_roles
(
    role_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    CONSTRAINT pk_users_roles PRIMARY KEY (role_id, user_id)
);

ALTER TABLE users_roles
    ADD CONSTRAINT fk_userole_on_role FOREIGN KEY (role_id) REFERENCES roles (id);

ALTER TABLE users_roles
    ADD CONSTRAINT fk_userole_on_user FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE short_urls
    MODIFY expires_at datetime NOT NULL;