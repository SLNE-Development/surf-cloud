package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import dev.slne.surf.cloud.core.common.player.ppdc.network.PdcPatch
import java.util.*

@SurfNettyPacket(
    "cloud:bidirectional:player_pdc/update",
    PacketFlow.BIDIRECTIONAL,
    handlerMode = PacketHandlerMode.NETTY
)
class UpdatePlayerPersistentDataContainerPacket(
    val uuid: UUID,
    val patch: PdcPatch
) : NettyPacket(), InternalNettyPacket<RunningServerPacketListener> {
    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.UUID_CODEC,
            UpdatePlayerPersistentDataContainerPacket::uuid,
            PdcPatch.STREAM_CODEC,
            UpdatePlayerPersistentDataContainerPacket::patch,
            ::UpdatePlayerPersistentDataContainerPacket
        )
    }

    override fun handle(listener: RunningServerPacketListener) {
        listener.handleUpdatePlayerPersistentDataContainer(this)
    }
}