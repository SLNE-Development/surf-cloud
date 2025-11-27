package dev.slne.surf.cloud.core.common.netty.network.protocol.initialize

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.core.common.netty.network.AlwaysImmediate

@SurfNettyPacket(
    DefaultIds.CLIENTBOUND_INITIALIZE_ID_RESPONSE,
    PacketFlow.CLIENTBOUND,
    ConnectionProtocol.INITIALIZE
)
@AlwaysImmediate
class ClientboundInitializeIdResponsePacket(id: Long) : NettyPacket() {

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG_CODEC,
            ClientboundInitializeIdResponsePacket::generatedId,
            ::ClientboundInitializeIdResponsePacket
        )
    }

    val generatedId: Long = id
}