package dev.slne.surf.cloud.api.common.player.punishment.type

import dev.slne.surf.cloud.api.common.util.annotation.InternalApi
import dev.slne.surf.cloud.api.common.util.attemptParsingIpString
import kotlinx.serialization.Serializable
import java.net.InetAddress

@Serializable
sealed interface PunishmentAttachedIpAddress {
    val rawIp: String

    val ip: InetAddress
        get() = attemptParsingIpString(rawIp) ?: error("$rawIp is not a valid IP address")

    @Serializable
    @InternalApi
    data class PunishmentAttachedIpAddressImpl(override val rawIp: String) :
        PunishmentAttachedIpAddress
}