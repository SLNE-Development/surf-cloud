package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.readEnum
import dev.slne.surf.cloud.api.common.player.ConnectionResult
import dev.slne.surf.cloud.api.common.player.ConnectionResultEnum

@SurfNettyPacket(DefaultIds.CLIENTBOUND_CONNECT_PLAYER_TO_SERVER_RESPONSE, PacketFlow.CLIENTBOUND)
class ClientboundConnectPlayerToServerResponse : ResponseNettyPacket {
    companion object {
        val STREAM_CODEC = packetCodec(
            ClientboundConnectPlayerToServerResponse::write,
            ::ClientboundConnectPlayerToServerResponse
        )
    }

    val result: ConnectionResult

    constructor(result: ConnectionResult) {
        this.result = result
    }

    private constructor(buf: SurfByteBuf) {
        result = buf.readEnum<ConnectionResultEnum>() to buf.readNullable { it.readComponent() }
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeEnum(result.first)
        buf.writeNullable(result.second, SurfByteBuf::writeComponent)
    }
}