package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import net.kyori.adventure.text.Component
import java.util.*

@SurfNettyPacket(
    DefaultIds.CLIENTBOUND_ACTION_BAR_PACKET,
    PacketFlow.CLIENTBOUND,
    handlerMode = PacketHandlerMode.NETTY
)
class ClientboundSendActionBarPacket(val uuid: UUID, val message: Component) : NettyPacket(),
    InternalNettyPacket<RunningClientPacketListener> {
    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.UUID_CODEC,
            ClientboundSendActionBarPacket::uuid,
            ByteBufCodecs.COMPONENT_CODEC,
            ClientboundSendActionBarPacket::message,
            ::ClientboundSendActionBarPacket
        )
    }

    override fun handle(listener: RunningClientPacketListener) {
        listener.handleSendActionBar(this)
    }
}