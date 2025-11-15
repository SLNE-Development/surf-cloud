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
    DefaultIds.CLIENTBOUND_SEND_RESOURCE_PACKS_PACKET,
    PacketFlow.CLIENTBOUND,
    handlerMode = PacketHandlerMode.DEFAULT
)
class ClientboundSendResourcePacksPacket(
    val uuid: UUID,
    val request: ResourcePackRequest
) : NettyPacket(), InternalNettyPacket<RunningClientPacketListener> {

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.UUID_CODEC,
            ClientboundSendResourcePacksPacket::uuid,
            ByteBufCodecs.RESOURCE_PACK_REQUEST_CODEC,
            ClientboundSendResourcePacksPacket::request,
            ::ClientboundSendResourcePacksPacket
        )
    }

    override fun handle(listener: RunningClientPacketListener) {
        listener.handleSendResourcePacks(this)
    }
}