package dev.slne.surf.cloud.core.common.netty.network.protocol.handshake

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf

const val PROTOCOL_VERSION = 2

/**
 * First packet sent by the client to the cloud server. It is used to establish a connection to the cloud server.
 */
@SurfNettyPacket(
    DefaultIds.SERVERBOUND_HANDSHAKE_PACKET,
    PacketFlow.SERVERBOUND,
    ConnectionProtocol.HANDSHAKING
)
class ServerboundHandshakePacket : NettyPacket {
    companion object {
        @JvmStatic
        val STREAM_CODEC =
            packetCodec(ServerboundHandshakePacket::write, ::ServerboundHandshakePacket)
    }

    val protocolVersion: Int
    val hostName: String
    val port: Int
    val intention: ClientIntent

    override val terminal = true

    constructor(hostName: String, port: Int, intention: ClientIntent) {
        this.protocolVersion = PROTOCOL_VERSION // Currently not used
        this.hostName = hostName
        this.port = port
        this.intention = intention
    }

    constructor(buf: SurfByteBuf) {
        protocolVersion = PROTOCOL_VERSION // Currently not used
        hostName = buf.readUtf()
        port = buf.readVarInt()
        intention = buf.readEnum(ClientIntent::class)
    }

    fun write(buffer: SurfByteBuf) {
        buffer.writeUtf(hostName)
        buffer.writeVarInt(port)
        buffer.writeEnum(intention)
    }
}