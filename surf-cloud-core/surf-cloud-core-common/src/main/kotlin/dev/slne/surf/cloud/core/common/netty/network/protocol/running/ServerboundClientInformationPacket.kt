package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.streamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.readEnum
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

    val serverName: String
    val information: ClientInformation

    constructor(serverName: String, information: ClientInformation) {
        this.serverName = serverName
        this.information = information
    }

    private constructor(buffer: SurfByteBuf) {
        serverName = buffer.readUtf()
        information = buffer.readJsonWithCodec(ClientInformation.CODEC)
    }

    private fun write(buffer: SurfByteBuf) {
        buffer.writeUtf(serverName)
        buffer.writeJsonWithCodec(ClientInformation.CODEC, information)
    }
}

data class ClientInformation(
    val maxPlayerCount: Int,
    val allowlist: Boolean,
    val state: ServerState
) {
    companion object {
        val CODEC = createRecordCodec<ClientInformation> {
            group(
                int("maxPlayerCount") { maxPlayerCount },
                bool("allowlist") { allowlist },
                enum("state") { state }
            ).apply(this, ::ClientInformation)
        }

        val STREAM_CODEC = streamCodec<SurfByteBuf, ClientInformation>({ buf, info ->
            buf.writeVarInt(info.maxPlayerCount)
            buf.writeBoolean(info.allowlist)
            buf.writeEnum(info.state)
        }, { buf ->
            ClientInformation(
                maxPlayerCount = buf.readVarInt(),
                allowlist = buf.readBoolean(),
                state = buf.readEnum()
            )
        })

        val NOT_AVAILABLE = ClientInformation(
            maxPlayerCount = -1,
            allowlist = true,
            state = ServerState.OFFLINE
        )
    }
}
