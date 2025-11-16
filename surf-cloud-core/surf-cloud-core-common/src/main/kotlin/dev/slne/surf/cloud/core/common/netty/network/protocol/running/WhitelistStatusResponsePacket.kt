package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.player.whitelist.WhitelistStatus

@SurfNettyPacket("cloud:bidirectional:whitelist_status_response", PacketFlow.BIDIRECTIONAL)
class WhitelistStatusResponsePacket(val status: WhitelistStatus) : ResponseNettyPacket() {
    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            WhitelistStatus.STREAM_CODEC,
            WhitelistStatusResponsePacket::status,
            ::WhitelistStatusResponsePacket
        )
    }
}