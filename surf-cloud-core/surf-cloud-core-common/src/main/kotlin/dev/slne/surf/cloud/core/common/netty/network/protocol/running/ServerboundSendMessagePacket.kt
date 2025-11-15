package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.codec.composite
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import net.kyori.adventure.text.Component
import java.util.*

@SurfNettyPacket(
    DefaultIds.SERVERBOUND_SEND_MESSAGE_PACKET,
    PacketFlow.SERVERBOUND,
    handlerMode = PacketHandlerMode.NETTY
)
class ServerboundSendMessagePacket(
    val uuid: UUID,
    val message: Component,
    val permission: String? = null
) : NettyPacket(), InternalNettyPacket<RunningServerPacketListener> {
    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.UUID_CODEC,
            ServerboundSendMessagePacket::uuid,
            ByteBufCodecs.COMPONENT_CODEC,
            ServerboundSendMessagePacket::message,
            ByteBufCodecs.STRING_CODEC.apply(ByteBufCodecs::nullable),
            ServerboundSendMessagePacket::permission,
            ::ServerboundSendMessagePacket
        )
    }

    override fun handle(listener: RunningServerPacketListener) {
        listener.handleSendMessage(this)
    }
}