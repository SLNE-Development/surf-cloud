package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import java.net.InetSocketAddress

@SurfNettyPacket(DefaultIds.CLIENTBOUND_REGISTER_SERVER_PACKET, PacketFlow.CLIENTBOUND)
class ClientboundRegisterServerPacket : NettyPacket {

    companion object {
        val STREAM_CODEC =
            packetCodec(ClientboundRegisterServerPacket::write, ::ClientboundRegisterServerPacket)
    }

    val proxy: Boolean
    val lobby: Boolean
    val group: String
    val serverName: String
    val playAddress: InetSocketAddress

    constructor(
        proxy: Boolean,
        lobby: Boolean,
        group: String,
        name: String,
        address: InetSocketAddress,
    ) {
        this.proxy = proxy
        this.lobby = lobby
        this.group = group
        this.serverName = name
        this.playAddress = address
    }

    private constructor(buf: SurfByteBuf) {
        proxy = buf.readBoolean()
        lobby = buf.readBoolean()
        group = buf.readUtf()
        serverName = buf.readUtf()
        playAddress = buf.readInetSocketAddress()
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeBoolean(proxy)
        buf.writeBoolean(lobby)
        buf.writeUtf(group)
        buf.writeUtf(serverName)
        buf.writeInetSocketAddress(playAddress)
    }
}