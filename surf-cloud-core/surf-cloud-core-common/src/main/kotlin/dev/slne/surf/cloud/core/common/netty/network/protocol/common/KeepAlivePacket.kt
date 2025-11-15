package dev.slne.surf.cloud.core.common.netty.network.protocol.common

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.network.protocol.long.LongResponsePacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket

@SurfNettyPacket(
    "cloud:bidirectional:keep_alive",
    PacketFlow.BIDIRECTIONAL,
    ConnectionProtocol.RUNNING,
    ConnectionProtocol.PRE_RUNNING,
    ConnectionProtocol.SYNCHRONIZING,
    handlerMode = PacketHandlerMode.NETTY,
)
class KeepAlivePacket(val keepAliveId: Long) : LongResponsePacket(),
    InternalNettyPacket<CommonPacketListener> {
    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG_CODEC,
            KeepAlivePacket::keepAliveId,
            ::KeepAlivePacket
        )
    }

    override fun handle(listener: CommonPacketListener) {
        listener.handleKeepAlive(this)
    }
}