ALTER TABLE punish_ban_ip_addresses
    DROP FOREIGN KEY fk_punish_ban_ip_addresses_punishment__id;
ALTER TABLE punish_ban_ip_addresses
    ADD CONSTRAINT fk_punish_ban_ip_addresses_punishment__id FOREIGN KEY (punishment) REFERENCES punish_bans (id) ON DELETE CASCADE ON UPDATE RESTRICT;

ALTER TABLE punish_notes_ban
    DROP FOREIGN KEY fk_punish_notes_ban_punishment_id__id;

ALTER TABLE punish_notes_ban
    ADD CONSTRAINT fk_punish_notes_ban_punishment_id__id FOREIGN KEY (punishment_id) REFERENCES punish_bans (id) ON DELETE CASCADE ON UPDATE RESTRICT;

ALTER TABLE punish_notes_kick
    DROP FOREIGN KEY fk_punish_notes_kick_punishment_id__id;

ALTER TABLE punish_notes_kick
    ADD CONSTRAINT fk_punish_notes_kick_punishment_id__id FOREIGN KEY (punishment_id) REFERENCES punish_kicks (id) ON DELETE CASCADE ON UPDATE RESTRICT;

ALTER TABLE punish_notes_mute
    DROP FOREIGN KEY fk_punish_notes_mute_punishment_id__id;

ALTER TABLE punish_notes_mute
    ADD CONSTRAINT fk_punish_notes_mute_punishment_id__id FOREIGN KEY (punishment_id) REFERENCES punish_mutes (id) ON DELETE CASCADE ON UPDATE RESTRICT;

ALTER TABLE punish_notes_warn
    DROP FOREIGN KEY fk_punish_notes_warn_punishment_id__id;

ALTER TABLE punish_notes_warn
    ADD CONSTRAINT fk_punish_notes_warn_punishment_id__id FOREIGN KEY (punishment_id) REFERENCES punish_warnings (id) ON DELETE CASCADE ON UPDATE RESTRICT;

CREATE INDEX idx_punished_uuid ON punish_warnings (punished_uuid);
CREATE INDEX idx_punished_uuid_unpunished ON punish_bans (punished_uuid, unpunished);
CREATE INDEX idx_permanent_exp ON punish_bans (permanent, expiration_date);
CREATE INDEX idx_permanent_exp ON punish_mutes (permanent, expiration_date);
CREATE INDEX idx_punished_uuid_unpunished ON punish_mutes (punished_uuid, unpunished);
CREATE INDEX idx_punished_uuid ON punish_mutes (punished_uuid);
CREATE INDEX idx_punished_uuid ON punish_kicks (punished_uuid);
CREATE INDEX idx_punished_uuid ON punish_bans (punished_uuid);
CREATE INDEX idx_ip_address ON punish_ban_ip_addresses (ip_address);
