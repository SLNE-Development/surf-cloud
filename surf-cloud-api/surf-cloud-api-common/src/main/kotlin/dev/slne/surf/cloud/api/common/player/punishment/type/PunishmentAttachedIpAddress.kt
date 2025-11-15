package dev.slne.surf.cloud.api.common.player.punishment.type

import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.util.annotation.InternalApi
import dev.slne.surf.cloud.api.common.util.attemptParsingIpString
import io.netty.buffer.ByteBuf
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

    companion object {
        val STREAM_CODEC: StreamCodec<ByteBuf, PunishmentAttachedIpAddress> = StreamCodec.composite(
            ByteBufCodecs.STRING_CODEC,
            PunishmentAttachedIpAddress::rawIp,
            ::PunishmentAttachedIpAddressImpl
        )
    }
}