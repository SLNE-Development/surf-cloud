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
    DefaultIds.CLIENTBOUND_PLAYER_LIST_HEADER_AND_FOOTER,
    PacketFlow.CLIENTBOUND,
    handlerMode = PacketHandlerMode.NETTY
)
class ClientboundSendPlayerListHeaderAndFooterPacket(
    val uuid: UUID,
    val header: Component,
    val footer: Component
) : NettyPacket(), InternalNettyPacket<RunningClientPacketListener> {
    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.UUID_CODEC,
            ClientboundSendPlayerListHeaderAndFooterPacket::uuid,
            ByteBufCodecs.COMPONENT_CODEC,
            ClientboundSendPlayerListHeaderAndFooterPacket::header,
            ByteBufCodecs.COMPONENT_CODEC,
            ClientboundSendPlayerListHeaderAndFooterPacket::footer,
            ::ClientboundSendPlayerListHeaderAndFooterPacket
        )
    }

    override fun handle(listener: RunningClientPacketListener) {
        listener.handleSendPlayerListHeaderAndFooter(this)
    }
}