package dev.slne.surf.cloud.standalone.player.db.exposed.punishment.table

import dev.slne.surf.cloud.api.server.exposed.columns.nativeUuid
import dev.slne.surf.cloud.api.server.exposed.table.AuditableLongIdTable

abstract class AbstractPunishmentTable(name: String) : AuditableLongIdTable(name) {
    val punishmentId = char("punishment_id", 8).uniqueIndex()
    val parentPunishment = reference(
        "parent_punishment_id",
        this,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    ).nullable()
    val punishedPlayer = reference(
        "cloud_player_id",
        CloudPlayerTable,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
    val issuerPlayer = reference(
        "issuer_id",
        CloudPlayerTable,
        onDelete = ReferenceOption.SET_NULL,
        onUpdate = ReferenceOption.SET_NULL
    ).nullable()
    val reason = largeText("reason").nullable()
}