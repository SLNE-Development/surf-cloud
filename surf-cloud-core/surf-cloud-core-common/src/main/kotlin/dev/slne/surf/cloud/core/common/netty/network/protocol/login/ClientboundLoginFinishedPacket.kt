package dev.slne.surf.cloud.core.common.netty.network.protocol.login

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.codec.streamCodecUnitSimple
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf

/**
 * This packet is sent by the server to the client to indicate that the login process was successful on the server.
 */
@SurfNettyPacket(
    DefaultIds.CLIENTBOUND_LOGIN_FINISHED_PACKET,
    PacketFlow.CLIENTBOUND,
    ConnectionProtocol.LOGIN
)
class ClientboundLoginFinishedPacket : NettyPacket {
    companion object {
        val STREAM_CODEC = packetCodec(ClientboundLoginFinishedPacket::write, ::ClientboundLoginFinishedPacket)
    }

    override val terminal = true

    val dummy: Int

    constructor() {
        println("ClientboundLoginFinishedPacket constructor")
        dummy = 0
    }

    private constructor(buf: SurfByteBuf) {
        println("ClientboundLoginFinishedPacket constructor buf")
        dummy = buf.readInt()
    }

    private fun write(buf: SurfByteBuf) {
        println("ClientboundLoginFinishedPacket write")
        buf.writeInt(dummy)
    }
}