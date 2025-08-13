package dev.slne.surf.cloud.standalone.player.db.exposed.punishment.table

import dev.slne.surf.cloud.api.server.exposed.table.AuditableLongIdTable
import dev.slne.surf.cloud.standalone.player.db.exposed.CloudPlayerTable
import org.jetbrains.exposed.sql.ReferenceOption

abstract class AbstractPunishmentTable(name: String) : AuditableLongIdTable(name) {
    val punishmentId = char("punishment_id", 8).uniqueIndex()
    val parentPunishmentId = reference(
        "parent_punishment_id",
        this,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    ).nullable().default(null)

    val punishedPlayerId = reference(
        "punished_player_id",
        CloudPlayerTable,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    ).index()

    val issuerPlayerId = reference(
        "issuer_player_id",
        CloudPlayerTable,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    ).nullable().default(null)

    val reason = largeText("reason").nullable()
}