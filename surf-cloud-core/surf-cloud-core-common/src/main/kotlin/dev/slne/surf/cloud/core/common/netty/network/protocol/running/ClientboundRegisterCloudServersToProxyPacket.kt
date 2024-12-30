package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import java.net.InetSocketAddress

typealias RegistrationInfo = Pair<String, InetSocketAddress>

@SurfNettyPacket(DefaultIds.CLIENTBOUND_REGISTER_CLOUD_SERVERS_TO_PROXY, PacketFlow.CLIENTBOUND)
class ClientboundRegisterCloudServersToProxyPacket : NettyPacket {

    companion object {
        val STREAM_CODEC = packetCodec(
            ClientboundRegisterCloudServersToProxyPacket::write,
            ::ClientboundRegisterCloudServersToProxyPacket
        )
    }

    val servers: Array<RegistrationInfo>

    constructor(servers: Array<RegistrationInfo>) {
        this.servers = servers
    }

    private constructor(buf: SurfByteBuf) {
        servers = buf.readArray { buf.readUtf() to buf.readInetSocketAddress() }
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeArray(servers) { buffer, (name, address) ->
            buffer.writeUtf(name)
            buffer.writeInetSocketAddress(address)
        }
    }
}