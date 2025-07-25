package dev.slne.surf.cloud.standalone

import MigrationUtils
import dev.slne.surf.cloud.api.common.config.properties.requiredSystemProperty
import dev.slne.surf.cloud.standalone.player.db.exposed.punishment.table.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ExperimentalDatabaseMigrationApi
import org.jetbrains.exposed.sql.transactions.transaction

val database = Database.connect(
    url = requiredSystemProperty("migration", "dbUrl") { it }.value(),
    user = requiredSystemProperty("migration", "dbUser") { it }.value(),
    password = requiredSystemProperty("migration", "dbPassword") { it }.value()
)

@OptIn(ExperimentalDatabaseMigrationApi::class)
fun main() {
    transaction(database) {
        MigrationUtils.generateMigrationScript(
            BanPunishmentTable,
            KickPunishmentTable,
            MutePunishmentTable,
            WarnPunishmentTable,
            BanPunishmentIpAddressTable,
            BanPunishmentNoteTable,
            KickPunishmentNoteTable,
            MutePunishmentNoteTable,
            WarnPunishmentNoteTable,
            scriptDirectory = "src/main/resources/db/migration",
            scriptName = "V5__replace_string_uuid_with_native_uuid",
        )
    }
}