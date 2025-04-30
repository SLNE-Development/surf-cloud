package dev.slne.surf.cloud.api.common.player.punishment.type.ban

import dev.slne.surf.cloud.api.common.player.punishment.type.ExpirablePunishment
import dev.slne.surf.cloud.api.common.player.punishment.type.PunishmentAttachedIpAddress
import dev.slne.surf.cloud.api.common.player.punishment.type.PunishmentType
import dev.slne.surf.cloud.api.common.player.punishment.type.UnpunishablePunishment
import java.net.InetAddress

interface PunishmentBan : UnpunishablePunishment, ExpirablePunishment {
    override val type get() = PunishmentType.BAN
    val securityBan: Boolean
    val raw: Boolean

    suspend fun attachIpAddress(ip: InetAddress) = attachIpAddress(ip.hostAddress)
    suspend fun attachIpAddress(rawIp: String): Boolean

    suspend fun isIpAttached(ip: InetAddress) = isIpAttached(ip.hostAddress)
    suspend fun isIpAttached(rawIp: String) = fetchIpAddresses().any { it.rawIp == rawIp }


    suspend fun fetchIpAddresses(): Set<PunishmentAttachedIpAddress>
}