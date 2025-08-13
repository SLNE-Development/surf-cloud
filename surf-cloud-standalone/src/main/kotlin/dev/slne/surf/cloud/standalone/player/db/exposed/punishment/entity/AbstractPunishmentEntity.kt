package dev.slne.surf.cloud.standalone.player.db.exposed.punishment.entity

import dev.slne.surf.cloud.api.server.exposed.table.AuditableLongEntity
import dev.slne.surf.cloud.standalone.player.db.exposed.CloudPlayerEntity
import dev.slne.surf.cloud.standalone.player.db.exposed.punishment.table.AbstractPunishmentTable
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

abstract class AbstractPunishmentEntity<E : AbstractPunishmentEntity<E, C>, C : LongEntityClass<E>>(
    id: EntityID<Long>,
    table: AbstractPunishmentTable,
    entityClass: C
) : AuditableLongEntity(id, table) {
    var punishmentId by table.punishmentId
    var parentPunishment by entityClass optionalReferencedOn table.parentPunishmentId
    var punishedPlayer by CloudPlayerEntity referencedOn table.punishedPlayerId
    var issuerPlayer by CloudPlayerEntity optionalReferencedOn table.issuerPlayerId
    var reason by table.reason
}