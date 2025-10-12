package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.streamCodecComposite
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.core.common.player.ppdc.network.PdcPatch
import java.util.*

@SurfNettyPacket("cloud:clientbound:player_pdc/update", PacketFlow.CLIENTBOUND)
class ClientboundUpdatePlayerPersistentDataContainerPacket(
    val uuid: UUID,
    val patch: PdcPatch
) : NettyPacket() {
    companion object {
        val STREAM_CODEC = streamCodecComposite(
            ByteBufCodecs.UUID_CODEC,
            ClientboundUpdatePlayerPersistentDataContainerPacket::uuid,
            PdcPatch.STREAM_CODEC,
            ClientboundUpdatePlayerPersistentDataContainerPacket::patch,
            ::ClientboundUpdatePlayerPersistentDataContainerPacket
        )
    }
}