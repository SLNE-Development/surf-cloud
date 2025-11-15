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
    DefaultIds.CLIENTBOUND_SEND_MESSAGE_PACKET,
    PacketFlow.CLIENTBOUND,
    handlerMode = PacketHandlerMode.NETTY
)
class ClientboundSendMessagePacket(
    val uuid: UUID,
    val message: Component,
    val permission: String? = null
) : NettyPacket(), InternalNettyPacket<RunningClientPacketListener> {
    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.UUID_CODEC,
            ClientboundSendMessagePacket::uuid,
            ByteBufCodecs.COMPONENT_CODEC,
            ClientboundSendMessagePacket::message,
            ByteBufCodecs.STRING_CODEC.apply(ByteBufCodecs::nullable),
            ClientboundSendMessagePacket::permission,
            ::ClientboundSendMessagePacket
        )
    }

    override fun handle(listener: RunningClientPacketListener) {
        listener.handleSendMessage(this)
    }
}