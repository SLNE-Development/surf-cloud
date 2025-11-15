package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.network.protocol.boolean.BooleanResponsePacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import dev.slne.surf.cloud.core.common.player.whitelist.MutableWhitelistEntryImpl

@SurfNettyPacket(
    "cloud:serverbound:update_whitelist",
    PacketFlow.SERVERBOUND,
    handlerMode = PacketHandlerMode.IO
)
class ServerboundUpdateWhitelistPacket(val updated: MutableWhitelistEntryImpl) :
    BooleanResponsePacket(), InternalNettyPacket<RunningServerPacketListener> {

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            MutableWhitelistEntryImpl.STREAM_CODEC,
            ServerboundUpdateWhitelistPacket::updated,
            ::ServerboundUpdateWhitelistPacket
        )
    }

    override fun handle(listener: RunningServerPacketListener) {
        listener.handleUpdateWhitelist(this)
    }
}