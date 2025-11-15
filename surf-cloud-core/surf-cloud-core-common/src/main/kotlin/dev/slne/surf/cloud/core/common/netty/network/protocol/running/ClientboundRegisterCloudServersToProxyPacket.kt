package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import java.net.InetSocketAddress

typealias RegistrationInfo = Pair<String, InetSocketAddress>

@SurfNettyPacket(
    DefaultIds.CLIENTBOUND_REGISTER_CLOUD_SERVERS_TO_PROXY,
    PacketFlow.CLIENTBOUND,
    handlerMode = PacketHandlerMode.NETTY
)
class ClientboundRegisterCloudServersToProxyPacket(val servers: Array<RegistrationInfo>) :
    NettyPacket(),
    InternalNettyPacket<RunningClientPacketListener> {

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.pair(ByteBufCodecs.STRING_CODEC, ByteBufCodecs.INET_SOCKET_ADDRESS_CODEC)
                .apply(ByteBufCodecs.array()),
            ClientboundRegisterCloudServersToProxyPacket::servers,
            ::ClientboundRegisterCloudServersToProxyPacket
        )
    }

    override fun handle(listener: RunningClientPacketListener) {
        listener.handleRegisterCloudServersToProxy(this)
    }
}