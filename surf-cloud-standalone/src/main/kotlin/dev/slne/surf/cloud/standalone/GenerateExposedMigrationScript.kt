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
        scriptName = "V7__add_whitelist_table",
    )
}