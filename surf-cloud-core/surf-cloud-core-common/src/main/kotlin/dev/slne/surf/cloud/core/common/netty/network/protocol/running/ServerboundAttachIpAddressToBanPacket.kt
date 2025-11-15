package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.network.protocol.boolean.BooleanResponsePacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket

@SurfNettyPacket(
    "cloud:serverbound:attach_ip_address_to_ban",
    PacketFlow.SERVERBOUND,
    handlerMode = PacketHandlerMode.NETTY
)
data class ServerboundAttachIpAddressToBanPacket(val banId: Long, val rawIp: String) :
    BooleanResponsePacket(), InternalNettyPacket<RunningServerPacketListener> {

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.LONG_CODEC,
            ServerboundAttachIpAddressToBanPacket::banId,
            ByteBufCodecs.STRING_CODEC,
            ServerboundAttachIpAddressToBanPacket::rawIp,
            ::ServerboundAttachIpAddressToBanPacket
        )
    }

    override fun handle(listener: RunningServerPacketListener) {
        listener.handleAttachIpAddressToBan(this)
    }
}