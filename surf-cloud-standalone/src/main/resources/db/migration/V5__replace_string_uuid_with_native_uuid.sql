-- V5__replace_string_uuid_with_native_uuid.sql
-- Migration to replace CHAR(36) UUID columns with native UUID types
-- Assumes data is UUID-valid and castable

-- Convert `cloud_player`.`uuid`
ALTER TABLE cloud_player
    MODIFY COLUMN uuid UUID NOT NULL;

-- Convert `punish_bans` UUID columns
ALTER TABLE punish_bans
    MODIFY COLUMN punished_uuid UUID NOT NULL,
    MODIFY COLUMN issuer_uuid UUID NULL,
    MODIFY COLUMN unpunisher_uuid UUID NULL;

-- Convert `punish_kicks` UUID columns
ALTER TABLE punish_kicks
    MODIFY COLUMN punished_uuid UUID NOT NULL,
    MODIFY COLUMN issuer_uuid UUID NULL;

-- Convert `punish_mutes` UUID columns
ALTER TABLE punish_mutes
    MODIFY COLUMN punished_uuid UUID NOT NULL,
    MODIFY COLUMN issuer_uuid UUID NULL,
    MODIFY COLUMN unpunisher_uuid UUID NULL;

-- Convert `punish_notes_ban`.`note_id`
ALTER TABLE punish_notes_ban
    MODIFY COLUMN note_id UUID NOT NULL;

-- Convert `punish_notes_kick`.`note_id`
ALTER TABLE punish_notes_kick
    MODIFY COLUMN note_id UUID NOT NULL;

-- Convert `punish_notes_mute`.`note_id`
ALTER TABLE punish_notes_mute
    MODIFY COLUMN note_id UUID NOT NULL;

-- Convert `punish_notes_warn`.`note_id`
ALTER TABLE punish_notes_warn
    MODIFY COLUMN note_id UUID NOT NULL;

-- Convert `punish_warnings` UUID columns
ALTER TABLE punish_warnings
    MODIFY COLUMN punished_uuid UUID NOT NULL,
    MODIFY COLUMN issuer_uuid UUID NULL;
