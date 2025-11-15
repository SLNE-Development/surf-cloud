package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import java.util.*

@SurfNettyPacket(
    DefaultIds.SERVERBOUND_REMOVE_RESOURCE_PACKS_PACKET,
    PacketFlow.SERVERBOUND,
    handlerMode = PacketHandlerMode.NETTY
)
class ServerboundRemoveResourcePacksPacket(
    val uuid: UUID,
    val packIds: MutableList<UUID>,
) : NettyPacket(),
    InternalNettyPacket<RunningServerPacketListener> {

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.UUID_CODEC,
            ServerboundRemoveResourcePacksPacket::uuid,
            ByteBufCodecs.UUID_CODEC.apply(ByteBufCodecs.list()),
            ServerboundRemoveResourcePacksPacket::packIds,
            ::ServerboundRemoveResourcePacksPacket
        )
    }

    override fun handle(listener: RunningServerPacketListener) {
        listener.handleRemoveResourcePacks(this)
    }
}