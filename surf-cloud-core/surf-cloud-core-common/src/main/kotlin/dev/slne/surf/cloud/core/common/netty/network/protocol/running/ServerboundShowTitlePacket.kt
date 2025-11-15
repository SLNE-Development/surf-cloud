package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import net.kyori.adventure.title.Title
import java.util.*

@SurfNettyPacket(
    DefaultIds.SERVERBOUND_SEND_TITLE_PACKET,
    PacketFlow.SERVERBOUND,
    handlerMode = PacketHandlerMode.NETTY
)
class ServerboundShowTitlePacket(val uuid: UUID, val title: Title) : NettyPacket(),
    InternalNettyPacket<RunningServerPacketListener> {

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.UUID_CODEC,
            ServerboundShowTitlePacket::uuid,
            ByteBufCodecs.TITLE_CODEC,
            ServerboundShowTitlePacket::title,
            ::ServerboundShowTitlePacket
        )
    }

    override fun handle(listener: RunningServerPacketListener) {
        listener.handleShowTitle(this)
    }
}