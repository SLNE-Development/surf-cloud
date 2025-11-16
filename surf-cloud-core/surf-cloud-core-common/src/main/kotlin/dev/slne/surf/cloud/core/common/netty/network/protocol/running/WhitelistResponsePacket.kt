package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.core.common.player.whitelist.WhitelistEntryImpl

@SurfNettyPacket("cloud:bidirectional:whitelist_response", PacketFlow.BIDIRECTIONAL)
class WhitelistResponsePacket(val whitelist: WhitelistEntryImpl?) : ResponseNettyPacket() {
    companion object {
        val STREAM_CODEC = WhitelistEntryImpl.STREAM_CODEC
            .apply(ByteBufCodecs::nullable)
            .map(::WhitelistResponsePacket, WhitelistResponsePacket::whitelist)
    }
}