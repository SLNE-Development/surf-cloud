package dev.slne.surf.cloud.standalone.player.db.exposed.whitelist

import dev.slne.surf.cloud.api.server.exposed.table.AuditableLongEntity
import dev.slne.surf.cloud.api.server.exposed.table.AuditableLongEntityClass
import dev.slne.surf.cloud.standalone.player.db.exposed.CloudPlayerEntity
import org.jetbrains.exposed.dao.id.EntityID

class WhitelistEntity(id: EntityID<Long>) : AuditableLongEntity(id, WhitelistTable) {
    companion object : AuditableLongEntityClass<WhitelistEntity>(WhitelistTable)

    var uuid by WhitelistTable.uuid
    var blocked by WhitelistTable.blocked
    var group by WhitelistTable.group
    var serverName by WhitelistTable.serverName
    var cloudPlayer by CloudPlayerEntity referencedOn WhitelistTable.cloudPlayerId
}