package dev.slne.surf.cloud.standalone.player.db.exposed.punishment.table

import dev.slne.surf.cloud.api.server.exposed.columns.charUuid
import dev.slne.surf.cloud.api.server.exposed.columns.zonedDateTime

abstract class AbstractUnpunishableExpirablePunishmentTable(name: String) : AbstractPunishmentTable(name) {
    val unpunished = bool("unpunished").default(false)
    val unpunishedDate = zonedDateTime("unpunished_date").nullable().default(null)
    val unpunisherUuid = charUuid("unpunisher_uuid").nullable().default(null)
    val expirationDate = zonedDateTime("expiration_date").nullable().default(null)
    val permanent = bool("permanent").default(false)
}