package dev.slne.surf.cloud.standalone.player.db.exposed

import dev.slne.surf.cloud.api.server.exposed.table.AuditableLongEntity
import dev.slne.surf.cloud.api.server.exposed.table.AuditableLongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class CloudPlayerEntity(id: EntityID<Long>) : AuditableLongEntity(id, CloudPlayerTable) {
    companion object : AuditableLongEntityClass<CloudPlayerEntity>(CloudPlayerTable)

    var uuid by CloudPlayerTable.uuid
    var lastServer by CloudPlayerTable.lastServer
    var lastSeen by CloudPlayerTable.lastSeen
    var lastIpAddress by CloudPlayerTable.lastIpAddress
    val nameHistories by CloudPlayerNameHistoryEntity referrersOn CloudPlayerNameHistoryTable.player
}

class CloudPlayerNameHistoryEntity(id: EntityID<Long>) : AuditableLongEntity(
    id,
    CloudPlayerNameHistoryTable
) {
    companion object :
        AuditableLongEntityClass<CloudPlayerNameHistoryEntity>(CloudPlayerNameHistoryTable)

    var name by CloudPlayerNameHistoryTable.name
    var player by CloudPlayerEntity referencedOn CloudPlayerNameHistoryTable.player
}