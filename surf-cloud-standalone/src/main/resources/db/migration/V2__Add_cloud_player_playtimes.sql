CREATE TABLE cloud_player_playtimes
(
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    server_name      VARCHAR(255) NOT NULL,
    category         VARCHAR(255) NOT NULL,
    duration_seconds BIGINT       NOT NULL DEFAULT 0,
    cloud_player_id  BIGINT       NOT NULL,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT FK_CLOUD_PLAYER_PLAYTIMES_ON_CLOUD_PLAYER FOREIGN KEY (cloud_player_id)
        REFERENCES cloud_player (id) ON DELETE CASCADE
);
