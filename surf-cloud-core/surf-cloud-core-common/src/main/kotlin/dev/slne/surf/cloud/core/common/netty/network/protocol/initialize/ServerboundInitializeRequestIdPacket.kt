package dev.slne.surf.cloud.core.common.netty.network.protocol.initialize

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.codec.streamCodecUnitSimple
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket

@SurfNettyPacket(
    DefaultIds.SERVERBOUND_INITIALIZE_REQUEST_ID_PACKET,
    PacketFlow.SERVERBOUND,
    ConnectionProtocol.INITIALIZE,
    handlerMode = PacketHandlerMode.NETTY,
)
object ServerboundInitializeRequestIdPacket : NettyPacket(), InternalNettyPacket<ServerInitializePacketListener> {
    val STREAM_CODEC = streamCodecUnitSimple(ServerboundInitializeRequestIdPacket)
    override fun handle(listener: ServerInitializePacketListener) {
        listener.handleIdRequest(this)
    }
}