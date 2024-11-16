package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.api.common.util.codec.ExtraCodecs
import net.kyori.adventure.title.Title
import java.util.*

@SurfNettyPacket(DefaultIds.SERVERBOUND_SEND_TITLE_PACKET, PacketFlow.SERVERBOUND)
class ServerboundShowTitlePacket : NettyPacket {

    companion object {
        val STREAM_CODEC = packetCodec(ServerboundShowTitlePacket::write, ::ServerboundShowTitlePacket)
    }

    val uuid: UUID
    val title: Title

    constructor(uuid: UUID, title: Title) {
        this.uuid = uuid
        this.title = title
    }

    private constructor(buf: SurfByteBuf) {
        uuid = buf.readUuid()
        title = ExtraCodecs.STREAM_TITLE_CODEC.decode(buf)
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeUuid(uuid)
        ExtraCodecs.STREAM_TITLE_CODEC.encode(buf, title)
    }
}