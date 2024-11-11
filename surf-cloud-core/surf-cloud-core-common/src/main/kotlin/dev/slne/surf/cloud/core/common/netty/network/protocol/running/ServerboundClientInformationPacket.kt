package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.api.common.server.state.ServerState
import dev.slne.surf.cloud.api.common.util.codec.bool
import dev.slne.surf.cloud.api.common.util.codec.createRecordCodec
import dev.slne.surf.cloud.api.common.util.codec.enum
import dev.slne.surf.cloud.api.common.util.codec.int


@SurfNettyPacket(
    DefaultIds.SERVERBOUND_CLIENT_INFORMATION_PACKET,
    PacketFlow.SERVERBOUND
)
class ServerboundClientInformationPacket : NettyPacket {

    companion object {
        val STREAM_CODEC = packetCodec(
            ServerboundClientInformationPacket::write,
            ::ServerboundClientInformationPacket
        )
    }

    val information: ClientInformation

    constructor(information: ClientInformation) {
        this.information = information
    }

    private constructor(buffer: SurfByteBuf) {
        information = buffer.readJsonWithCodec(ClientInformation.CODEC)
    }

    private fun write(buffer: SurfByteBuf) {
        buffer.writeJsonWithCodec(ClientInformation.CODEC, information)
    }
}

data class ClientInformation(
    val maxPlayerCount: Int,
    val currentPlayerCount: Int,
    val whitelist: Boolean,
    val state: ServerState
) {
    companion object {
        val CODEC = createRecordCodec<ClientInformation> {
            group(
                int("maxPlayerCount") { maxPlayerCount },
                int("currentPlayerCount") { currentPlayerCount },
                bool("whitelist") { whitelist },
                enum("state") { state }
            ).apply(this, ::ClientInformation)
        }

        val NOT_AVAILABLE = ClientInformation(
            maxPlayerCount = -1,
            currentPlayerCount = -1,
            whitelist = true,
            state = ServerState.OFFLINE
        )
    }
}
