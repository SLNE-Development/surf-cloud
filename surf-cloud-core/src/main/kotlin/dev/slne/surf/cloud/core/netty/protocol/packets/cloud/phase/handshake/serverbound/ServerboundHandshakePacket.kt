package dev.slne.surf.cloud.core.netty.protocol.packets.cloud.phase.handshake.serverbound

import dev.slne.surf.cloud.api.meta.DefaultIds
import dev.slne.surf.cloud.api.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.netty.packet.packetCodec
import dev.slne.surf.cloud.api.netty.protocol.buffer.SurfByteBuf
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds

const val PROTOCOL_VERSION = -1 // TODO: implement maybe?

/**
 * First packet sent by the client to the cloud server. It is used to establish a connection to the cloud server.
 */
@SurfNettyPacket(DefaultIds.SERVERBOUND_HANDSHAKE_PACKET, PacketFlow.SERVERBOUND, ConnectionProtocol.HANDSHAKING)
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

    constructor(hostName: String, port: Int, intention: ClientIntent) {
        this.protocolVersion = PROTOCOL_VERSION // Currently not used
        this.hostName = hostName
        this.port = port
        this.intention = intention
    }

    private constructor(buf: SurfByteBuf) {
        protocolVersion = PROTOCOL_VERSION // Currently not used
        hostName = buf.readUtf()
        port = buf.readUnsignedShort()
        intention = buf.readEnum(ClientIntent::class)
    }

    private fun write(buffer: SurfByteBuf) {
        buffer.writeUtf(hostName)
        buffer.writeUnsigned(port)
        buffer.writeEnum(intention)
    }

    enum class ClientIntent {
        STATUS, // TODO: implement
        INITIALIZE,
        LOGIN,
    }
}