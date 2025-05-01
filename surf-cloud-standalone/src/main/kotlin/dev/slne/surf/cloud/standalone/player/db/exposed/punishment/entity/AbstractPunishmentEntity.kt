package dev.slne.surf.cloud.standalone.player.db.exposed.punishment.entity

import dev.slne.surf.cloud.api.server.exposed.table.AuditableLongEntity
import dev.slne.surf.cloud.standalone.player.db.exposed.punishment.table.AbstractPunishmentTable
import org.jetbrains.exposed.dao.id.EntityID

abstract class AbstractPunishmentEntity(id: EntityID<Long>, table: AbstractPunishmentTable) :
    AuditableLongEntity(id, table) {
    var punishmentId by table.punishmentId
    var punishedUuid by table.punishedUuid
    var issuerUuid by table.issuerUuid
    var reason by table.reason
}