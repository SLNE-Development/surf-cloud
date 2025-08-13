CREATE TABLE IF NOT EXISTS cloud_players
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at      TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL,
    updated_at      TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL,
    uuid            UUID                                      NOT NULL,
    last_server     CHAR(255)                                 NULL,
    last_seen       TIMESTAMP(6)                              NULL,
    last_ip_address VARCHAR(45)                               NULL
);
ALTER TABLE cloud_players
    ADD CONSTRAINT cloud_players_uuid_unique UNIQUE (uuid);
CREATE TABLE IF NOT EXISTS whitelists
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at      TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL,
    updated_at      TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL,
    blocked         BOOLEAN      DEFAULT FALSE                NOT NULL,
    `group`         VARCHAR(255)                              NULL,
    `server_name`   VARCHAR(255)                              NULL,
    cloud_player_id BIGINT                                    NOT NULL,
    CONSTRAINT fk_whitelists_cloud_player_id__id FOREIGN KEY (cloud_player_id) REFERENCES cloud_players (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT ck_group_xor_server CHECK (((`group` IS NOT NULL) AND (`server_name` IS NULL)) OR
                                          ((`group` IS NULL) AND (`server_name` IS NOT NULL)))
);
ALTER TABLE whitelists
    ADD CONSTRAINT whitelists_cloud_player_id_group_unique UNIQUE (cloud_player_id, `group`);
ALTER TABLE whitelists
    ADD CONSTRAINT whitelists_cloud_player_id_server_name_unique UNIQUE (cloud_player_id, `server_name`);
ALTER TABLE punish_bans
    ADD parent_punishment_id BIGINT DEFAULT NULL NULL;
ALTER TABLE punish_bans
    ADD punished_player_id BIGINT NOT NULL;
ALTER TABLE punish_bans
    ADD issuer_player_id BIGINT DEFAULT NULL NULL;
ALTER TABLE punish_bans
    ADD unpunisher_player_id BIGINT DEFAULT NULL NULL;
CREATE INDEX punish_bans_punished_player_id ON punish_bans (punished_player_id);
CREATE INDEX idx_punished_uuid_unpunished ON punish_bans (punished_player_id, unpunished);
ALTER TABLE punish_kicks
    ADD parent_punishment_id BIGINT DEFAULT NULL NULL;
ALTER TABLE punish_kicks
    ADD punished_player_id BIGINT NOT NULL;
ALTER TABLE punish_kicks
    ADD issuer_player_id BIGINT DEFAULT NULL NULL;
CREATE INDEX punish_kicks_punished_player_id ON punish_kicks (punished_player_id);
ALTER TABLE punish_mutes
    ADD parent_punishment_id BIGINT DEFAULT NULL NULL;
ALTER TABLE punish_mutes
    ADD punished_player_id BIGINT NOT NULL;
ALTER TABLE punish_mutes
    ADD issuer_player_id BIGINT DEFAULT NULL NULL;
ALTER TABLE punish_mutes
    ADD unpunisher_player_id BIGINT DEFAULT NULL NULL;
CREATE INDEX punish_mutes_punished_player_id ON punish_mutes (punished_player_id);
CREATE INDEX idx_punished_uuid_unpunished ON punish_mutes (punished_player_id, unpunished);
ALTER TABLE punish_warnings
    ADD parent_punishment_id BIGINT DEFAULT NULL NULL;
ALTER TABLE punish_warnings
    ADD punished_player_id BIGINT NOT NULL;
ALTER TABLE punish_warnings
    ADD issuer_player_id BIGINT DEFAULT NULL NULL;
CREATE INDEX punish_warnings_punished_player_id ON punish_warnings (punished_player_id);
ALTER TABLE punish_bans
    ADD CONSTRAINT fk_punish_bans_parent_punishment_id__id FOREIGN KEY (parent_punishment_id) REFERENCES punish_bans (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE punish_bans
    ADD CONSTRAINT fk_punish_bans_punished_player_id__id FOREIGN KEY (punished_player_id) REFERENCES cloud_players (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE punish_bans
    ADD CONSTRAINT fk_punish_bans_issuer_player_id__id FOREIGN KEY (issuer_player_id) REFERENCES cloud_players (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE punish_bans
    ADD CONSTRAINT fk_punish_bans_unpunisher_player_id__id FOREIGN KEY (unpunisher_player_id) REFERENCES cloud_players (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE punish_kicks
    ADD CONSTRAINT fk_punish_kicks_parent_punishment_id__id FOREIGN KEY (parent_punishment_id) REFERENCES punish_kicks (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE punish_kicks
    ADD CONSTRAINT fk_punish_kicks_punished_player_id__id FOREIGN KEY (punished_player_id) REFERENCES cloud_players (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE punish_kicks
    ADD CONSTRAINT fk_punish_kicks_issuer_player_id__id FOREIGN KEY (issuer_player_id) REFERENCES cloud_players (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE punish_mutes
    ADD CONSTRAINT fk_punish_mutes_parent_punishment_id__id FOREIGN KEY (parent_punishment_id) REFERENCES punish_mutes (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE punish_mutes
    ADD CONSTRAINT fk_punish_mutes_punished_player_id__id FOREIGN KEY (punished_player_id) REFERENCES cloud_players (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE punish_mutes
    ADD CONSTRAINT fk_punish_mutes_issuer_player_id__id FOREIGN KEY (issuer_player_id) REFERENCES cloud_players (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE punish_mutes
    ADD CONSTRAINT fk_punish_mutes_unpunisher_player_id__id FOREIGN KEY (unpunisher_player_id) REFERENCES cloud_players (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE punish_warnings
    ADD CONSTRAINT fk_punish_warnings_parent_punishment_id__id FOREIGN KEY (parent_punishment_id) REFERENCES punish_warnings (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE punish_warnings
    ADD CONSTRAINT fk_punish_warnings_punished_player_id__id FOREIGN KEY (punished_player_id) REFERENCES cloud_players (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE punish_warnings
    ADD CONSTRAINT fk_punish_warnings_issuer_player_id__id FOREIGN KEY (issuer_player_id) REFERENCES cloud_players (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE punish_bans
    DROP COLUMN punished_uuid;
ALTER TABLE punish_bans
    DROP COLUMN issuer_uuid;
ALTER TABLE punish_bans
    DROP COLUMN unpunisher_uuid;
ALTER TABLE punish_bans
    DROP COLUMN parent_id;
ALTER TABLE punish_kicks
    DROP COLUMN punished_uuid;
ALTER TABLE punish_kicks
    DROP COLUMN issuer_uuid;
ALTER TABLE punish_mutes
    DROP COLUMN punished_uuid;
ALTER TABLE punish_mutes
    DROP COLUMN issuer_uuid;
ALTER TABLE punish_mutes
    DROP COLUMN unpunisher_uuid;
ALTER TABLE punish_warnings
    DROP COLUMN punished_uuid;
ALTER TABLE punish_warnings
    DROP COLUMN issuer_uuid;
ALTER TABLE punish_mutes
    DROP INDEX idx_punished_uuid_unpunished;
ALTER TABLE punish_mutes
    DROP INDEX idx_punished_uuid;
ALTER TABLE punish_bans
    DROP INDEX FK_punish_bans_punish_bans;
ALTER TABLE punish_bans
    DROP INDEX idx_punished_uuid_unpunished;
ALTER TABLE punish_bans
    DROP INDEX FK_punish_bans_punish_bans;
