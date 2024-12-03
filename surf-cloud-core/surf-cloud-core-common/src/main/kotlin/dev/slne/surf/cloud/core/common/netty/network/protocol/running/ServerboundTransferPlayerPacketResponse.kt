package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.readEnum
import net.kyori.adventure.text.Component

@SurfNettyPacket(
    DefaultIds.SERVERBOUND_TRANSFER_PLAYER_PACKET_RESPONSE_PACKET,
    PacketFlow.SERVERBOUND
)
class ServerboundTransferPlayerPacketResponse : ResponseNettyPacket {

    companion object {
        val STREAM_CODEC = packetCodec(
            ServerboundTransferPlayerPacketResponse::write,
            ::ServerboundTransferPlayerPacketResponse
        )
    }

    val status: Status
    val reasonComponent: Component?

    constructor(status: Status, reasonComponent: Component?) {
        this.status = status
        this.reasonComponent = reasonComponent
    }

    private constructor(buf: SurfByteBuf) {
        status = buf.readEnum()
        reasonComponent = buf.readNullable { it.readComponent() }
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeEnum(status)
        buf.writeNullable(reasonComponent, SurfByteBuf::writeComponent)
    }

    enum class Status {
        /**
         * The player was successfully connected to the server.
         */
        SUCCESS,

        /**
         * The player is already connected to this server.
         */
        ALREADY_CONNECTED,

        /**
         * The connection is already in progress.
         */
        CONNECTION_IN_PROGRESS,

        /**
         * A plugin has cancelled this connection.
         */
        CONNECTION_CANCELLED,

        /**
         * The server disconnected the user. A reason may be provided in the [Result] object.
         */
        SERVER_DISCONNECTED
    }
}