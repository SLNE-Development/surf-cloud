package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.network.protocol.boolean.BooleanResponsePacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import java.util.*

@SurfNettyPacket(
    "cloud:bidirectional:request_player_permission",
    PacketFlow.BIDIRECTIONAL,
    handlerMode = PacketHandlerMode.DEFAULT
)
class RequestPlayerPermissionPacket(val uuid: UUID, val permission: String) :
    BooleanResponsePacket(), InternalNettyPacket<RunningServerPacketListener> {

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.UUID_CODEC,
            RequestPlayerPermissionPacket::uuid,
            ByteBufCodecs.STRING_CODEC,
            RequestPlayerPermissionPacket::permission,
            ::RequestPlayerPermissionPacket
        )
    }

    override fun handle(listener: RunningServerPacketListener) {
        listener.handleRequestPlayerPermission(this)
    }
}