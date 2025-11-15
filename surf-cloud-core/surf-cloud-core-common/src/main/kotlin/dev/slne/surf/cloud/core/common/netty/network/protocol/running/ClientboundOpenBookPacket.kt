package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import net.kyori.adventure.inventory.Book
import java.util.*

@SurfNettyPacket(
    DefaultIds.CLIENTBOUND_OPEN_BOOK_PACKET,
    PacketFlow.CLIENTBOUND,
    handlerMode = PacketHandlerMode.DEFAULT
)
class ClientboundOpenBookPacket(val uuid: UUID, val book: Book) : NettyPacket(),
    InternalNettyPacket<RunningClientPacketListener> {

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.UUID_CODEC,
            ClientboundOpenBookPacket::uuid,
            ByteBufCodecs.BOOK_CODEC,
            ClientboundOpenBookPacket::book,
            ::ClientboundOpenBookPacket
        )
    }

    override fun handle(listener: RunningClientPacketListener) {
        listener.handleOpenBook(this)
    }
}