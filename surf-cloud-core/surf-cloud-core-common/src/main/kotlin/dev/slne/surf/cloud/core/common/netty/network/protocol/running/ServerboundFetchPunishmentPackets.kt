package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import dev.slne.surf.cloud.core.common.player.punishment.type.AbstractPunishment
import kotlinx.serialization.Serializable
import java.util.*

@SurfNettyPacket(
    "cloud:serverbound:fetch_punishments_mutes",
    PacketFlow.SERVERBOUND,
    handlerMode = PacketHandlerMode.NETTY
)
data class ServerboundFetchMutesPacket(
    val punishedUuid: UUID,
    val onlyActive: Boolean
) : RespondingNettyPacket<ClientboundFetchedPunishmentsResponsePacket>(),
    InternalNettyPacket<RunningServerPacketListener> {

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.UUID_CODEC,
            ServerboundFetchMutesPacket::punishedUuid,
            ByteBufCodecs.BOOLEAN_CODEC,
            ServerboundFetchMutesPacket::onlyActive,
            ::ServerboundFetchMutesPacket
        )
    }

    override fun handle(listener: RunningServerPacketListener) {
        listener.handleFetchMutes(this)
    }
}

@SurfNettyPacket(
    "cloud:serverbound:fetch_punishments_bans",
    PacketFlow.SERVERBOUND,
    handlerMode = PacketHandlerMode.NETTY
)
data class ServerboundFetchBansPacket(
    val punishedUuid: UUID,
    val onlyActive: Boolean,
) : RespondingNettyPacket<ClientboundFetchedPunishmentsResponsePacket>(),
    InternalNettyPacket<RunningServerPacketListener> {
    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.UUID_CODEC,
            ServerboundFetchBansPacket::punishedUuid,
            ByteBufCodecs.BOOLEAN_CODEC,
            ServerboundFetchBansPacket::onlyActive,
            ::ServerboundFetchBansPacket
        )
    }

    override fun handle(listener: RunningServerPacketListener) {
        listener.handleFetchBans(this)
    }

}

@SurfNettyPacket(
    "cloud:serverbound:fetch_ip_bans",
    PacketFlow.SERVERBOUND,
    handlerMode = PacketHandlerMode.NETTY
)
data class ServerboundFetchIpBansPacket(
    val onlyActive: Boolean,
    val ip: String,
) : RespondingNettyPacket<ClientboundFetchedPunishmentsResponsePacket>(),
    InternalNettyPacket<RunningServerPacketListener> {

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOLEAN_CODEC,
            ServerboundFetchIpBansPacket::onlyActive,
            ByteBufCodecs.STRING_CODEC,
            ServerboundFetchIpBansPacket::ip,
            ::ServerboundFetchIpBansPacket
        )
    }

    override fun handle(listener: RunningServerPacketListener) {
        listener.handleFetchIpBans(this)
    }
}

@SurfNettyPacket(
    "cloud:serverbound:fetch_punishments_kicks",
    PacketFlow.SERVERBOUND,
    handlerMode = PacketHandlerMode.NETTY
)
data class ServerboundFetchKicksPacket(val punishedUuid: UUID) :
    RespondingNettyPacket<ClientboundFetchedPunishmentsResponsePacket>(),
    InternalNettyPacket<RunningServerPacketListener> {

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.UUID_CODEC,
            ServerboundFetchKicksPacket::punishedUuid,
            ::ServerboundFetchKicksPacket
        )
    }

    override fun handle(listener: RunningServerPacketListener) {
        listener.handleFetchKicks(this)
    }
}

@SurfNettyPacket(
    "cloud:serverbound:fetch_punishments_warns",
    PacketFlow.SERVERBOUND,
    handlerMode = PacketHandlerMode.NETTY
)
data class ServerboundFetchWarnsPacket(val punishedUuid: UUID) :
    RespondingNettyPacket<ClientboundFetchedPunishmentsResponsePacket>(),
    InternalNettyPacket<RunningServerPacketListener> {

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.UUID_CODEC,
            ServerboundFetchWarnsPacket::punishedUuid,
            ::ServerboundFetchWarnsPacket
        )
    }

    override fun handle(listener: RunningServerPacketListener) {
        listener.handleFetchWarns(this)
    }
}


@Serializable
@SurfNettyPacket("cloud:clientbound:fetch_punishments_response", PacketFlow.CLIENTBOUND)
class ClientboundFetchedPunishmentsResponsePacket(val punishments: List<AbstractPunishment>) :
    ResponseNettyPacket()