package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.ClientCommonPacketListener

/**
 * When sent to the client, client responds with a
 * [ServerboundPongPacket]
 * packet with the same id.
 */
@SurfNettyPacket(
    DefaultIds.CLIENTBOUND_PING_PACKET,
    PacketFlow.CLIENTBOUND,
    handlerMode = PacketHandlerMode.NETTY
)
class ClientboundPingPacket(val pingId: Long) : NettyPacket(),
    InternalNettyPacket<ClientCommonPacketListener> {

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG_CODEC,
            ClientboundPingPacket::pingId,
            ::ClientboundPingPacket
        )
    }

    override fun handle(listener: ClientCommonPacketListener) {
        listener.handlePing(this)
    }
}