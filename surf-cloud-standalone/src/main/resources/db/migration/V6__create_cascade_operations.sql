ALTER TABLE punish_ban_ip_addresses
    DROP FOREIGN KEY fk_punish_ban_ip_addresses_punishment__id;
ALTER TABLE punish_ban_ip_addresses
    ADD CONSTRAINT fk_punish_ban_ip_addresses_punishment__id FOREIGN KEY (punishment) REFERENCES punish_bans (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE punish_notes_ban
    DROP FOREIGN KEY fk_punish_notes_ban_punishment_id__id;
ALTER TABLE punish_notes_ban
    ADD CONSTRAINT fk_punish_notes_ban_punishment_id__id FOREIGN KEY (punishment_id) REFERENCES punish_bans (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE punish_notes_kick
    DROP FOREIGN KEY fk_punish_notes_kick_punishment_id__id;
ALTER TABLE punish_notes_kick
    ADD CONSTRAINT fk_punish_notes_kick_punishment_id__id FOREIGN KEY (punishment_id) REFERENCES punish_kicks (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE punish_notes_mute
    DROP FOREIGN KEY fk_punish_notes_mute_punishment_id__id;
ALTER TABLE punish_notes_mute
    ADD CONSTRAINT fk_punish_notes_mute_punishment_id__id FOREIGN KEY (punishment_id) REFERENCES punish_mutes (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE punish_notes_warn
    DROP FOREIGN KEY fk_punish_notes_warn_punishment_id__id;
ALTER TABLE punish_notes_warn
    ADD CONSTRAINT fk_punish_notes_warn_punishment_id__id FOREIGN KEY (punishment_id) REFERENCES punish_warnings (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE cloud_player_playtimes
    DROP FOREIGN KEY FK_CLOUD_PLAYER_PLAYTIMES_ON_CLOUD_PLAYER;
AlTER TABLE cloud_player_playtimes
    ADD CONSTRAINT FK_CLOUD_PLAYER_PLAYTIMES_ON_CLOUD_PLAYER FOREIGN KEY (cloud_player_id) REFERENCES cloud_player (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE cloud_player_name_history
    DROP FOREIGN KEY FK_CLOUD_PLAYER_NAME_HISTORY_ON_CLOUD_PLAYER;
ALTER TABLE cloud_player_name_history
    ADD CONSTRAINT FK_CLOUD_PLAYER_NAME_HISTORY_ON_CLOUD_PLAYER FOREIGN KEY (cloud_player_id) REFERENCES cloud_player (id) ON DELETE CASCADE ON UPDATE CASCADE