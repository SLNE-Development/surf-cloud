package dev.slne.surf.cloud.standalone.player.db.exposed.punishment.entity

import dev.slne.surf.cloud.api.server.exposed.table.AuditableLongEntityClass
import dev.slne.surf.cloud.core.common.player.punishment.type.PunishmentBanImpl
import dev.slne.surf.cloud.standalone.player.db.exposed.punishment.table.BanPunishmentIpAddressTable
import dev.slne.surf.cloud.standalone.player.db.exposed.punishment.table.BanPunishmentNoteTable
import dev.slne.surf.cloud.standalone.player.db.exposed.punishment.table.BanPunishmentTable
import org.jetbrains.exposed.dao.id.EntityID

class BanPunishmentEntity(id: EntityID<Long>) :
    AbstractUnpunishableExpirablePunishmentEntity<BanPunishmentEntity, BanPunishmentEntity.Companion>(
        id,
        BanPunishmentTable,
        BanPunishmentEntity
    ) {
    companion object : AuditableLongEntityClass<BanPunishmentEntity>(BanPunishmentTable)

    var securityBan by BanPunishmentTable.securityBan
    var raw by BanPunishmentTable.raw

    val notes by BanPunishmentNoteEntity referrersOn BanPunishmentNoteTable
    val ipAddresses by BanPunishmentIpAddressEntity referrersOn BanPunishmentIpAddressTable

    fun toApiObject(): PunishmentBanImpl = PunishmentBanImpl(
        id = id.value,
        punishmentId = punishmentId,
        punishedUuid = punishedPlayer.uuid,
        issuerUuid = issuerPlayer?.uuid,
        reason = reason,
        permanent = permanent,
        expirationDate = expirationDate,
        punishmentDate = createdAt,
        unpunished = unpunished,
        unpunishedDate = unpunishedDate,
        unpunisherUuid = unpunisherPlayer?.uuid,
        securityBan = securityBan,
        raw = raw,
        parent = parentPunishment?.toApiObject()
    )
}