package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import java.net.InetSocketAddress

@SurfNettyPacket(
    DefaultIds.SERVERBOUND_IS_SERVER_MANAGED_BY_THIS_PROXY_REQUEST,
    PacketFlow.CLIENTBOUND,
    handlerMode = PacketHandlerMode.NETTY
)
class ClientboundIsServerManagedByThisProxyPacket(val clientAddress: InetSocketAddress) :
    RespondingNettyPacket<ServerboundIsServerManagedByThisProxyResponse>(),
    InternalNettyPacket<RunningClientPacketListener> {

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INET_SOCKET_ADDRESS_CODEC,
            ClientboundIsServerManagedByThisProxyPacket::clientAddress,
            ::ClientboundIsServerManagedByThisProxyPacket
        )
    }

    override fun handle(listener: RunningClientPacketListener) {
        listener.handleIsServerManagedByThisProxy(this)
    }
}