package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import net.kyori.adventure.text.Component
import java.util.*

@SurfNettyPacket(DefaultIds.SERVERBOUND_SEND_PLAYER_LIST_HEADER_AND_FOOTER, PacketFlow.SERVERBOUND)
class ServerboundSendPlayerListHeaderAndFooter : NettyPacket {
    companion object {
        val STREAM_CODEC = packetCodec(
            ServerboundSendPlayerListHeaderAndFooter::write,
            ::ServerboundSendPlayerListHeaderAndFooter
        )
    }

    val uuid: UUID
    val header: Component
    val footer: Component

    constructor(uuid: UUID, header: Component, footer: Component) {
        this.uuid = uuid
        this.header = header
        this.footer = footer
    }

    private constructor(buf: SurfByteBuf) {
        this.uuid = buf.readUuid()
        this.header = buf.readComponent()
        this.footer = buf.readComponent()
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeUuid(uuid)
        buf.writeComponent(header)
        buf.writeComponent(footer)
    }
}