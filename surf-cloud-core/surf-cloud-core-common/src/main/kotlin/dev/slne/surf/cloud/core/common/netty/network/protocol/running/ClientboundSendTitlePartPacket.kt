package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.api.common.util.codec.ExtraCodecs
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.TitlePart
import java.util.UUID

@SurfNettyPacket(DefaultIds.CLIENTBOUND_TITLE_PART_PACKET, PacketFlow.CLIENTBOUND)
class ClientboundSendTitlePartPacket: NettyPacket {

    companion object {
        val STREAM_CODEC = packetCodec(ClientboundSendTitlePartPacket::write, ::ClientboundSendTitlePartPacket)
    }

    val uuid: UUID
    val titlePart: TitlePart<*>
    val value: Any

    constructor(uuid: UUID, titlePart: TitlePart<*>, value: Any) {
        this.uuid = uuid
        this.titlePart = titlePart
        this.value = value
    }

    private constructor(buf: SurfByteBuf) {
        this.uuid = buf.readUuid()
        this.titlePart = ExtraCodecs.STREAM_TITLE_PART_CODEC.decode(buf)
        this.value = when (titlePart) {
            TitlePart.TITLE, TitlePart.SUBTITLE -> buf.readComponent()
            TitlePart.TIMES -> ExtraCodecs.TITLE_TIMES_STREAM_CODEC.decode(buf)
            else -> error("Unknown title part: $titlePart")
        }
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeUuid(uuid)
        ExtraCodecs.STREAM_TITLE_PART_CODEC.encode(buf, titlePart)
        when (titlePart) {
            TitlePart.TITLE, TitlePart.SUBTITLE -> buf.writeComponent(value as Component)
            TitlePart.TIMES -> ExtraCodecs.TITLE_TIMES_STREAM_CODEC.encode(buf, value as Title.Times)
            else -> error("Unknown title part: $titlePart")
        }
    }
}