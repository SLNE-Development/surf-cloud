package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.codec.composite
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import java.net.InetSocketAddress

@SurfNettyPacket(
    DefaultIds.CLIENTBOUND_REGISTER_SERVER_PACKET,
    PacketFlow.CLIENTBOUND,
    handlerMode = PacketHandlerMode.NETTY
)
class ClientboundRegisterServerPacket(
    val proxy: Boolean,
    val lobby: Boolean,
    val group: String,
    val serverName: String,
    val playAddress: InetSocketAddress
) : NettyPacket(), InternalNettyPacket<RunningClientPacketListener> {

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOLEAN_CODEC,
            ClientboundRegisterServerPacket::proxy,
            ByteBufCodecs.BOOLEAN_CODEC,
            ClientboundRegisterServerPacket::lobby,
            ByteBufCodecs.STRING_CODEC,
            ClientboundRegisterServerPacket::group,
            ByteBufCodecs.STRING_CODEC,
            ClientboundRegisterServerPacket::serverName,
            ByteBufCodecs.INET_SOCKET_ADDRESS_CODEC,
            ClientboundRegisterServerPacket::playAddress,
            ::ClientboundRegisterServerPacket
        )
    }

    override fun handle(listener: RunningClientPacketListener) {
        listener.handleRegisterServerPacket(this)
    }
}