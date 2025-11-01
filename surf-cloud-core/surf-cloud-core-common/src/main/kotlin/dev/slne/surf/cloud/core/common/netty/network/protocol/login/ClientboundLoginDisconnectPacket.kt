package dev.slne.surf.cloud.core.common.netty.network.protocol.login

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.core.common.netty.network.DisconnectReason
import dev.slne.surf.cloud.core.common.netty.network.DisconnectionDetails

@SurfNettyPacket(
    DefaultIds.CLIENTBOUND_LOGIN_DISCONNECT_PACKET,
    PacketFlow.CLIENTBOUND,
    ConnectionProtocol.LOGIN
)
class ClientboundLoginDisconnectPacket : NettyPacket {
    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            DisconnectionDetails.STREAM_CODEC,
            ClientboundLoginDisconnectPacket::details,
            ::ClientboundLoginDisconnectPacket
        )
    }

    val details: DisconnectionDetails

    constructor(details: DisconnectionDetails) {
        this.details = details
    }

    constructor(reason: DisconnectReason) {
        this.details = DisconnectionDetails(reason)
    }
}