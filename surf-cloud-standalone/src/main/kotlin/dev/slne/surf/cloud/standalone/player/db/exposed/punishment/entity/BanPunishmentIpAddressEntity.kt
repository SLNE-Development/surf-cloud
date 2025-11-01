package dev.slne.surf.cloud.standalone.player.db.exposed.punishment.entity

import dev.slne.surf.cloud.api.common.player.punishment.type.PunishmentAttachedIpAddress.PunishmentAttachedIpAddressImpl
import dev.slne.surf.cloud.api.server.exposed.table.AuditableLongEntity
import dev.slne.surf.cloud.api.server.exposed.table.AuditableLongEntityClass
import dev.slne.surf.cloud.standalone.player.db.exposed.punishment.table.BanPunishmentIpAddressTable
import org.jetbrains.exposed.dao.id.EntityID

class BanPunishmentIpAddressEntity(id: EntityID<Long>) :
    AuditableLongEntity(id, BanPunishmentIpAddressTable) {
    companion object :
        AuditableLongEntityClass<BanPunishmentIpAddressEntity>(BanPunishmentIpAddressTable)

    var ipAddress by BanPunishmentIpAddressTable.ipAddress
    var punishment by BanPunishmentEntity referencedOn BanPunishmentIpAddressTable.punishment

    fun toApiObject() = PunishmentAttachedIpAddressImpl(ipAddress)
}