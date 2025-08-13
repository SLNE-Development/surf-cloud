package dev.slne.surf.cloud.standalone

import dev.slne.surf.cloud.api.server.exposed.migration.generateSimpleExposedMigration
import dev.slne.surf.cloud.standalone.player.db.exposed.punishment.table.*
import dev.slne.surf.cloud.standalone.player.db.exposed.whitelist.WhitelistTable

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
        WhitelistTable,
        scriptName = "V8__rename_tables_and_use_cloud_player_references",
    )
}