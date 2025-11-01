package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.network.protocol.component.OptionalComponentResponse
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import java.util.*

@SurfNettyPacket(DefaultIds.REQUEST_OFFLINE_DISPLAY_NAME_PACKET, PacketFlow.BIDIRECTIONAL)
class RequestOfflineDisplayNamePacket(val uuid: UUID) :
    RespondingNettyPacket<OptionalComponentResponse>() {
    private constructor(buf: SurfByteBuf) : this(buf.readUuid())

    companion object {
        val STREAM_CODEC =
            packetCodec(RequestOfflineDisplayNamePacket::write, ::RequestOfflineDisplayNamePacket)
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeUuid(uuid)
    }
}