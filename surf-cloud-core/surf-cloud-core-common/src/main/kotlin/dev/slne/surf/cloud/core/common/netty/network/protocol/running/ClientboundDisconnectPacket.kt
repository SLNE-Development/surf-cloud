package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.core.common.netty.network.DisconnectReason
import dev.slne.surf.cloud.core.common.netty.network.DisconnectionDetails
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.ClientCommonPacketListener

@SurfNettyPacket(
    DefaultIds.CLIENTBOUND_DISCONNECT_PACKET,
    PacketFlow.CLIENTBOUND,
    handlerMode = PacketHandlerMode.NETTY
)
class ClientboundDisconnectPacket(val details: DisconnectionDetails) : NettyPacket(),
    InternalNettyPacket<ClientCommonPacketListener> {

    constructor(reason: DisconnectReason) : this(DisconnectionDetails(reason))

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            DisconnectionDetails.STREAM_CODEC,
            ClientboundDisconnectPacket::details,
            ::ClientboundDisconnectPacket
        )
    }

    override fun handle(listener: ClientCommonPacketListener) {
        listener.handleDisconnect(this)
    }
}