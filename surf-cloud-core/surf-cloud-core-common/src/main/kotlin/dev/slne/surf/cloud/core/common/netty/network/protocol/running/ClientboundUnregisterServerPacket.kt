package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket

@SurfNettyPacket(
    DefaultIds.CLIENTBOUND_UNREGISTER_SERVER_PACKET,
    PacketFlow.CLIENTBOUND,
    handlerMode = PacketHandlerMode.NETTY
)
class ClientboundUnregisterServerPacket(val serverName: String) : NettyPacket(),
    InternalNettyPacket<RunningClientPacketListener> {

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_CODEC,
            ClientboundUnregisterServerPacket::serverName,
            ::ClientboundUnregisterServerPacket
        )
    }

    override fun handle(listener: RunningClientPacketListener) {
        listener.handleUnregisterServerPacket(this)
    }
}