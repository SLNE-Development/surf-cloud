package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import dev.slne.surf.cloud.core.common.player.PunishmentCacheImpl
import kotlinx.serialization.Serializable
import java.util.*

@SurfNettyPacket(
    "cloud:serverbound:get_current_login_validation_punishment_cache",
    PacketFlow.SERVERBOUND,
    handlerMode = PacketHandlerMode.NETTY
)
class ServerboundGetCurrentLoginValidationPunishmentCachePacket(val uuid: UUID) :
    RespondingNettyPacket<ClientboundGetCurrentLoginValidationPunishmentCacheResponsePacket>(),
    InternalNettyPacket<RunningServerPacketListener> {

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.UUID_CODEC,
            ServerboundGetCurrentLoginValidationPunishmentCachePacket::uuid,
            ::ServerboundGetCurrentLoginValidationPunishmentCachePacket
        )
    }

    override fun handle(listener: RunningServerPacketListener) {
        listener.handleGetCurrentLoginValidationPunishmentCache(this)
    }
}

@Serializable
@SurfNettyPacket(
    "cloud:clientbound:get_current_login_validation_punishment_cache_response",
    PacketFlow.CLIENTBOUND
)
class ClientboundGetCurrentLoginValidationPunishmentCacheResponsePacket(val cache: PunishmentCacheImpl?) :
    ResponseNettyPacket()