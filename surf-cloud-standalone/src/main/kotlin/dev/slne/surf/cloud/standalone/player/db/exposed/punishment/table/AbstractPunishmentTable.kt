package dev.slne.surf.cloud.standalone.player.db.exposed.punishment.table

import dev.slne.surf.cloud.api.server.exposed.columns.charUuid
import dev.slne.surf.cloud.api.server.exposed.table.AuditableLongIdTable

abstract class AbstractPunishmentTable(name: String): AuditableLongIdTable(name) {
    val punishmentId = char("punishment_id", 8).uniqueIndex()
    val punishedUuid = charUuid("punished_uuid")
    val issuerUuid = charUuid("issuer_uuid").nullable()
    val reason = largeText("reason").nullable()

    init {
        index("idx_punished_uuid", false, punishedUuid)
    }
}