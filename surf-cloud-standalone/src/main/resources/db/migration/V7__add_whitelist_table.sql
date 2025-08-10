CREATE TABLE IF NOT EXISTS whitelist
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at      TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL,
    updated_at      TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL,
    uuid            UUID                                      NOT NULL,
    blocked         BOOLEAN      DEFAULT FALSE                NOT NULL,
    `group`         VARCHAR(255)                              NULL,
    `server_name`   VARCHAR(255)                              NULL,
    cloud_player_id BIGINT                                    NOT NULL,
    CONSTRAINT fk_whitelist_cloud_player_id__id FOREIGN KEY (cloud_player_id) REFERENCES cloud_player (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT ck_group_xor_server CHECK (((`group` IS NOT NULL) AND (`server_name` IS NULL)) OR
                                          ((`group` IS NULL) AND (`server_name` IS NOT NULL)))
);
CREATE INDEX whitelist_uuid ON whitelist (uuid);
ALTER TABLE whitelist
    ADD CONSTRAINT whitelist_uuid_group_unique UNIQUE (uuid, `group`);
ALTER TABLE whitelist
    ADD CONSTRAINT whitelist_uuid_server_name_unique UNIQUE (uuid, `server_name`);
