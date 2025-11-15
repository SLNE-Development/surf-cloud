package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import net.kyori.adventure.resource.ResourcePackRequest
import java.util.*

@SurfNettyPacket(
    DefaultIds.SERVERBOUND_SEND_RESOURCE_PACKS_PACKET,
    PacketFlow.SERVERBOUND,
    handlerMode = PacketHandlerMode.NETTY
)
class ServerboundSendResourcePacksPacket(
    val uuid: UUID,
    val request: ResourcePackRequest
) : NettyPacket(), InternalNettyPacket<RunningServerPacketListener> {

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.UUID_CODEC,
            ServerboundSendResourcePacksPacket::uuid,
            ByteBufCodecs.RESOURCE_PACK_REQUEST_CODEC,
            ServerboundSendResourcePacksPacket::request,
            ::ServerboundSendResourcePacksPacket
        )
    }

    override fun handle(listener: RunningServerPacketListener) {
        listener.handleSendResourcePacks(this)
    }
}