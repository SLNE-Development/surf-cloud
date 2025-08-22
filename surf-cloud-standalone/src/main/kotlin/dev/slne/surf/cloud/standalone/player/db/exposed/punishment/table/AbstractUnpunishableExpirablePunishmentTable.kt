package dev.slne.surf.cloud.standalone.player.db.exposed.punishment.table

import dev.slne.surf.cloud.api.server.exposed.columns.zonedDateTime
import dev.slne.surf.cloud.standalone.player.db.exposed.CloudPlayerTable
import org.jetbrains.exposed.sql.ReferenceOption

abstract class AbstractUnpunishableExpirablePunishmentTable(name: String) :
    AbstractPunishmentTable(name) {
    val unpunished = bool("unpunished").default(false)
    val unpunishedDate = zonedDateTime("unpunished_date").nullable().default(null)
    val unpunisherPlayerId = reference(
        "unpunisher_player_id",
        CloudPlayerTable,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    ).nullable().default(null)

    val expirationDate = zonedDateTime("expiration_date").nullable().default(null)
    val permanent = bool("permanent").default(false)

    init {
        index("idx_permanent_exp", false, permanent, expirationDate)
    }
}