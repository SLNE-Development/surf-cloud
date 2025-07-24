package dev.slne.surf.cloud.core.common.netty.network.protocol.running.serverbound

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.api.common.util.codec.ExtraCodecs
import net.kyori.adventure.inventory.Book
import java.util.UUID

@SurfNettyPacket(DefaultIds.SERVERBOUND_OPEN_BOOK_PACKET, PacketFlow.SERVERBOUND)
class ServerboundOpenBookPacket : NettyPacket {

    companion object {
        val STREAM_CODEC =
            packetCodec(ServerboundOpenBookPacket::write, ::ServerboundOpenBookPacket)
    }

    val uuid: UUID
    val book: Book

    constructor(uuid: UUID, book: Book) {
        this.uuid = uuid
        this.book = book
    }

    private constructor(buf: SurfByteBuf) {
        this.uuid = buf.readUuid()
        this.book = ExtraCodecs.STREAM_BOOK_CODEC.decode(buf)
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeUuid(uuid)
        ExtraCodecs.STREAM_BOOK_CODEC.encode(buf, book)
    }
}