CREATE TABLE users
(
    id       BIGINT AUTO_INCREMENT NOT NULL,
    username VARCHAR(20)           NOT NULL,
    password VARCHAR(100)          NOT NULL,
    email    VARCHAR(100)          NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);

ALTER TABLE users
    ADD CONSTRAINT uc_users_username UNIQUE (username);

CREATE TABLE weather_data
(
    id                     BIGINT AUTO_INCREMENT NOT NULL,
    location_code          INT                   NULL,
    temp_ta                DOUBLE                NULL,
    wind_dir_wd            DOUBLE                NULL,
    wind_speed_ws          DOUBLE                NULL,
    humidity_hm            DOUBLE                NULL,
    precipitation_rn       DOUBLE                NULL,
    is_strong_wind_warning TINYINT               NULL,
    is_dry_warning         TINYINT               NULL,
    created_at             datetime              NULL,
    CONSTRAINT pk_weatherdata PRIMARY KEY (id)
);

CREATE INDEX idx_weather_created_at ON weather_data (created_at);

CREATE INDEX idx_weather_location_code ON weather_data (location_code);

CREATE TABLE device
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    name       VARCHAR(255)          NULL,
    location   VARCHAR(100)          NULL,
    ip_address VARCHAR(15)           NULL,
    mac_id     VARCHAR(20)           NOT NULL,
    status     VARCHAR(20)           NOT NULL,
    created_at datetime              NULL,
    updated_at datetime              NULL,
    CONSTRAINT pk_device PRIMARY KEY (id)
);

ALTER TABLE device
    ADD CONSTRAINT uc_device_macid UNIQUE (mac_id);