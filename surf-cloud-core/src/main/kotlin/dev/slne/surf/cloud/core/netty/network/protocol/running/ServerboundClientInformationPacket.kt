package dev.slne.surf.cloud.core.netty.network.protocol.running

import dev.slne.surf.cloud.api.meta.DefaultIds
import dev.slne.surf.cloud.api.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.netty.packet.packetCodec
import dev.slne.surf.cloud.api.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.api.util.codec.bool
import dev.slne.surf.cloud.api.util.codec.createRecordCodec
import dev.slne.surf.cloud.api.util.codec.int


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
) {
    companion object {
        val CODEC = createRecordCodec<ClientInformation> {
            group(
                int("maxPlayerCount") { maxPlayerCount },
                int("currentPlayerCount") { currentPlayerCount },
                bool("whitelist") { whitelist }
            ).apply(this, ::ClientInformation)
        }
    }
}
