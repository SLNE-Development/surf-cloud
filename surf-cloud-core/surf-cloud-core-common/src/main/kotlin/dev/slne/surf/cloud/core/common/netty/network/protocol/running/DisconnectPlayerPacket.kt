package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.CommonRunningPacketListener
import net.kyori.adventure.text.Component
import java.util.*

@SurfNettyPacket(
    DefaultIds.BIDIRECTIONAL_DISCONNECT_PLAYER,
    PacketFlow.BIDIRECTIONAL,
    handlerMode = PacketHandlerMode.NETTY
)
class DisconnectPlayerPacket(val uuid: UUID, val reason: Component) : NettyPacket(),
    InternalNettyPacket<CommonRunningPacketListener> {

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.UUID_CODEC,
            DisconnectPlayerPacket::uuid,
            ByteBufCodecs.COMPONENT_CODEC,
            DisconnectPlayerPacket::reason,
            ::DisconnectPlayerPacket
        )
    }

    override fun handle(listener: CommonRunningPacketListener) {
        listener.handleDisconnectPlayer(this)
    }
}