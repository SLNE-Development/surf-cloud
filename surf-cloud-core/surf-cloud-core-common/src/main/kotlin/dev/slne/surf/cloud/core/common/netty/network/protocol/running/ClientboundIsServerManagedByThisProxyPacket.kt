package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import java.net.InetSocketAddress

@SurfNettyPacket(
    DefaultIds.SERVERBOUND_IS_SERVER_MANAGED_BY_THIS_PROXY_REQUEST,
    PacketFlow.CLIENTBOUND
)
class ClientboundIsServerManagedByThisProxyPacket :
    RespondingNettyPacket<ServerboundIsServerManagedByThisProxyResponse> {

    companion object {
        val STREAM_CODEC = packetCodec(
            ClientboundIsServerManagedByThisProxyPacket::write,
            ::ClientboundIsServerManagedByThisProxyPacket
        )
    }

    val clientAddress: InetSocketAddress

    constructor(clientAddress: InetSocketAddress) {
        this.clientAddress = clientAddress
    }

    private constructor(buf: SurfByteBuf) {
        this.clientAddress = buf.readInetSocketAddress()
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeInetSocketAddress(clientAddress)
    }
}