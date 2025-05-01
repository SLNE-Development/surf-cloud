package dev.slne.surf.cloud.standalone.player.db.exposed.punishment.entity

import dev.slne.surf.cloud.standalone.player.db.exposed.punishment.table.AbstractUnpunishableExpirablePunishmentTable
import org.jetbrains.exposed.dao.id.EntityID

abstract class AbstractUnpunishableExpirablePunishmentEntity(
    id: EntityID<Long>,
    table: AbstractUnpunishableExpirablePunishmentTable
) : AbstractPunishmentEntity(
    id, table
) {
    var unpunished by table.unpunished
    var unpunishedDate by table.unpunishedDate
    var unpunisherUuid by table.unpunisherUuid
    var expirationDate by table.expirationDate
    var permanent by table.permanent
}