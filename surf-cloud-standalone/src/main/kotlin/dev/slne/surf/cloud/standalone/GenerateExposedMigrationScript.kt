package dev.slne.surf.cloud.standalone

import dev.slne.surf.cloud.api.server.exposed.migration.generateSimpleExposedMigration
import dev.slne.surf.cloud.standalone.player.db.exposed.punishment.table.*

fun main() {
    generateSimpleExposedMigration(
        BanPunishmentTable,
        KickPunishmentTable,
        MutePunishmentTable,
        WarnPunishmentTable,
        BanPunishmentIpAddressTable,
        BanPunishmentNoteTable,
        KickPunishmentNoteTable,
        MutePunishmentNoteTable,
        WarnPunishmentNoteTable,
        scriptName = "V5__replace_string_uuid_with_native_uuid"
    )
}