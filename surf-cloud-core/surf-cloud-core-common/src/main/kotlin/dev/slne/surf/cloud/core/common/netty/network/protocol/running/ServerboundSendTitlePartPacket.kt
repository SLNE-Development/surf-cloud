package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.decodeError
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.encodeError
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import io.netty.buffer.ByteBuf
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.TitlePart
import java.util.*

@SurfNettyPacket(
    DefaultIds.SERVERBOUND_SEND_TITLE_PART_PACKET,
    PacketFlow.SERVERBOUND,
    handlerMode = PacketHandlerMode.NETTY
)
class ServerboundSendTitlePartPacket(
    val uuid: UUID,
    val titlePart: TitlePart<*>,
    val value: Any
) : NettyPacket(), InternalNettyPacket<RunningServerPacketListener> {

    companion object {
        val STREAM_CODEC = packetCodec(ServerboundSendTitlePartPacket::write, ::read)

        private fun read(buf: ByteBuf): ServerboundSendTitlePartPacket {
            val uuid = ByteBufCodecs.UUID_CODEC.decode(buf)
            val titlePart = ByteBufCodecs.TITLE_PART_CODEC.decode(buf)
            val value = when (titlePart) {
                TitlePart.TITLE, TitlePart.SUBTITLE -> ByteBufCodecs.COMPONENT_CODEC.decode(buf)
                TitlePart.TIMES -> ByteBufCodecs.TITLE_TIMES_CODEC.decode(buf)
                else -> decodeError("Unknown title part: $titlePart")
            }

            return ServerboundSendTitlePartPacket(uuid, titlePart, value)
        }

    }

    private fun write(buf: ByteBuf) {
        ByteBufCodecs.UUID_CODEC.encode(buf, uuid)
        ByteBufCodecs.TITLE_PART_CODEC.encode(buf, titlePart)
        when (titlePart) {
            TitlePart.TITLE, TitlePart.SUBTITLE -> ByteBufCodecs.COMPONENT_CODEC.encode(
                buf,
                value as Component
            )

            TitlePart.TIMES -> ByteBufCodecs.TITLE_TIMES_CODEC.encode(buf, value as Title.Times)
            else -> encodeError("Unknown title part: $titlePart")
        }
    }

    override fun handle(listener: RunningServerPacketListener) {
        listener.handleSendTitlePart(this)
    }
}