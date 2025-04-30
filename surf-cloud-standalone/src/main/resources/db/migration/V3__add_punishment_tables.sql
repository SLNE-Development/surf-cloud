CREATE TABLE IF NOT EXISTS punish_bans
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at      TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL,
    updated_at      TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL,
    punishment_id   CHAR(8)                                   NOT NULL,
    punished_uuid   CHAR(36)                                  NOT NULL,
    issuer_uuid     CHAR(36)                                  NULL,
    reason          LONGTEXT                                  NULL,
    unpunished      BOOLEAN      DEFAULT FALSE                NOT NULL,
    unpunished_date TIMESTAMP(6) DEFAULT NULL                 NULL,
    unpunisher_uuid CHAR(36)     DEFAULT NULL                 NULL,
    expiration_date TIMESTAMP(6) DEFAULT NULL                 NULL,
    permanent       BOOLEAN      DEFAULT FALSE                NOT NULL,
    security_ban    BOOLEAN      DEFAULT FALSE                NOT NULL,
    raw             BOOLEAN      DEFAULT FALSE                NOT NULL
);

ALTER TABLE punish_bans
    ADD CONSTRAINT punish_bans_punishment_id_unique UNIQUE (punishment_id);



CREATE TABLE IF NOT EXISTS punish_kicks
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at    TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL,
    updated_at    TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL,
    punishment_id CHAR(8)                                   NOT NULL,
    punished_uuid CHAR(36)                                  NOT NULL,
    issuer_uuid   CHAR(36)                                  NULL,
    reason        LONGTEXT                                  NULL
);

ALTER TABLE punish_kicks
    ADD CONSTRAINT punish_kicks_punishment_id_unique UNIQUE (punishment_id);



CREATE TABLE IF NOT EXISTS punish_mutes
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at      TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL,
    updated_at      TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL,
    punishment_id   CHAR(8)                                   NOT NULL,
    punished_uuid   CHAR(36)                                  NOT NULL,
    issuer_uuid     CHAR(36)                                  NULL,
    reason          LONGTEXT                                  NULL,
    unpunished      BOOLEAN      DEFAULT FALSE                NOT NULL,
    unpunished_date TIMESTAMP(6) DEFAULT NULL                 NULL,
    unpunisher_uuid CHAR(36)     DEFAULT NULL                 NULL,
    expiration_date TIMESTAMP(6) DEFAULT NULL                 NULL,
    permanent       BOOLEAN      DEFAULT FALSE                NOT NULL
);

ALTER TABLE punish_mutes
    ADD CONSTRAINT punish_mutes_punishment_id_unique UNIQUE (punishment_id);



CREATE TABLE IF NOT EXISTS punish_warnings
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at    TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL,
    updated_at    TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL,
    punishment_id CHAR(8)                                   NOT NULL,
    punished_uuid CHAR(36)                                  NOT NULL,
    issuer_uuid   CHAR(36)                                  NULL,
    reason        LONGTEXT                                  NULL
);

ALTER TABLE punish_warnings
    ADD CONSTRAINT punish_warnings_punishment_id_unique UNIQUE (punishment_id);



CREATE TABLE IF NOT EXISTS punish_ban_ip_addresses
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL,
    ip_address CHAR(45)                                  NOT NULL,
    punishment BIGINT                                    NOT NULL,
    CONSTRAINT fk_punish_ban_ip_addresses_punishment__id FOREIGN KEY (punishment) REFERENCES punish_bans (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);



CREATE TABLE IF NOT EXISTS punish_notes_ban
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at    TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL,
    updated_at    TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL,
    note_id       CHAR(36)                                  NOT NULL,
    punishment_id BIGINT                                    NOT NULL,
    note          LONGTEXT                                  NOT NULL,
    creator_id    BIGINT       DEFAULT NULL                 NULL,
    generated     BOOLEAN      DEFAULT FALSE                NOT NULL,
    CONSTRAINT fk_punish_notes_ban_punishment_id__id FOREIGN KEY (punishment_id) REFERENCES punish_bans (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);

ALTER TABLE punish_notes_ban
    ADD CONSTRAINT punish_notes_ban_note_id_unique UNIQUE (note_id);



CREATE TABLE IF NOT EXISTS punish_notes_kick
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at    TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL,
    updated_at    TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL,
    note_id       CHAR(36)                                  NOT NULL,
    punishment_id BIGINT                                    NOT NULL,
    note          LONGTEXT                                  NOT NULL,
    creator_id    BIGINT       DEFAULT NULL                 NULL,
    generated     BOOLEAN      DEFAULT FALSE                NOT NULL,
    CONSTRAINT fk_punish_notes_kick_punishment_id__id FOREIGN KEY (punishment_id) REFERENCES punish_kicks (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);

ALTER TABLE punish_notes_kick
    ADD CONSTRAINT punish_notes_kick_note_id_unique UNIQUE (note_id);



CREATE TABLE IF NOT EXISTS punish_notes_mute
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at    TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL,
    updated_at    TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL,
    note_id       CHAR(36)                                  NOT NULL,
    punishment_id BIGINT                                    NOT NULL,
    note          LONGTEXT                                  NOT NULL,
    creator_id    BIGINT       DEFAULT NULL                 NULL,
    generated     BOOLEAN      DEFAULT FALSE                NOT NULL,
    CONSTRAINT fk_punish_notes_mute_punishment_id__id FOREIGN KEY (punishment_id) REFERENCES punish_mutes (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);
ALTER TABLE punish_notes_mute
    ADD CONSTRAINT punish_notes_mute_note_id_unique UNIQUE (note_id);



CREATE TABLE IF NOT EXISTS punish_notes_warn
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at    TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL,
    updated_at    TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL,
    note_id       CHAR(36)                                  NOT NULL,
    punishment_id BIGINT                                    NOT NULL,
    note          LONGTEXT                                  NOT NULL,
    creator_id    BIGINT       DEFAULT NULL                 NULL,
    generated     BOOLEAN      DEFAULT FALSE                NOT NULL,
    CONSTRAINT fk_punish_notes_warn_punishment_id__id FOREIGN KEY (punishment_id) REFERENCES punish_warnings (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);

ALTER TABLE punish_notes_warn
    ADD CONSTRAINT punish_notes_warn_note_id_unique UNIQUE (note_id);
