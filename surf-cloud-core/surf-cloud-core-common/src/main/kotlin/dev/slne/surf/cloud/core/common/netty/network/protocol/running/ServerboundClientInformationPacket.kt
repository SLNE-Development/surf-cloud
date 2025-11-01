package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.codec.composite
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.server.state.ServerState


@SurfNettyPacket(
    DefaultIds.SERVERBOUND_CLIENT_INFORMATION_PACKET,
    PacketFlow.SERVERBOUND
)
class ServerboundClientInformationPacket(
    val serverName: String,
    val information: ClientInformation
) : NettyPacket() {
    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_CODEC,
            ServerboundClientInformationPacket::serverName,
            ClientInformation.STREAM_CODEC,
            ServerboundClientInformationPacket::information,
            ::ServerboundClientInformationPacket
        )
    }
}

data class ClientInformation(
    val maxPlayerCount: Int,
    val allowlist: Boolean,
    val state: ServerState
) {
    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT_CODEC,
            ClientInformation::maxPlayerCount,
            ByteBufCodecs.BOOLEAN_CODEC,
            ClientInformation::allowlist,
            ServerState.STREAM_CODEC,
            ClientInformation::state,
            ::ClientInformation
        )

        val NOT_AVAILABLE = ClientInformation(
            maxPlayerCount = -1,
            allowlist = true,
            state = ServerState.OFFLINE
        )
    }
}
