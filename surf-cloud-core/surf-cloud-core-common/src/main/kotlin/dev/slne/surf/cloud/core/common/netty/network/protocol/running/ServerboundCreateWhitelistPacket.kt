package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import dev.slne.surf.cloud.core.common.player.whitelist.WhitelistEntryImpl

@SurfNettyPacket(
    "cloud:serverbound:create_whitelist",
    PacketFlow.SERVERBOUND,
    handlerMode = PacketHandlerMode.IO
)
class ServerboundCreateWhitelistPacket(val entry: WhitelistEntryImpl) :
    RespondingNettyPacket<WhitelistResponsePacket>(),
    InternalNettyPacket<RunningServerPacketListener> {

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            WhitelistEntryImpl.STREAM_CODEC,
            ServerboundCreateWhitelistPacket::entry,
            ::ServerboundCreateWhitelistPacket
        )
    }

    override fun handle(listener: RunningServerPacketListener) {
        listener.handleCreateWhitelist(this)
    }
}