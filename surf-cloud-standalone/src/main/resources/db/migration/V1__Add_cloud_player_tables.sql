CREATE TABLE cloud_player
(
    id              BIGINT AUTO_INCREMENT               NOT NULL,
    uuid            CHAR(36)                            NOT NULL,
    last_server     VARCHAR(255)                        NULL,
    last_seen       TIMESTAMP                           NULL,
    last_ip_address VARCHAR(45)                         NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE cloud_player_name_history
(
    id              BIGINT AUTO_INCREMENT               NOT NULL,
    name            VARCHAR(16)                         NOT NULL,
    cloud_player_id BIGINT                              NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

ALTER TABLE cloud_player
    ADD CONSTRAINT uc_cloud_player_uuid UNIQUE (uuid);

ALTER TABLE cloud_player_name_history
    ADD CONSTRAINT FK_CLOUD_PLAYER_NAME_HISTORY_ON_CLOUD_PLAYER FOREIGN KEY (cloud_player_id) REFERENCES cloud_player (id) ON DELETE CASCADE;

CREATE INDEX FK_CLOUD_PLAYER_NAME_HISTORY_ON_CLOUD_PLAYER ON cloud_player_name_history (cloud_player_id);