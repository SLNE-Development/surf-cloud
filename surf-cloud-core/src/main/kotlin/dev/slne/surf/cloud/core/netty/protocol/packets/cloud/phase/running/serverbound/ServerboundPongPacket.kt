package dev.slne.surf.cloud.core.netty.protocol.packets.cloud.phase.running.serverbound

import dev.slne.surf.cloud.api.meta.DefaultIds
import dev.slne.surf.cloud.api.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.netty.protocol.buffer.SurfByteBuf
import kotlin.properties.Delegates.notNull

/**
 * Response to the clientbound packet
 * ([dev.slne.surf.cloud.core.netty.protocol.packets.cloud.phase.running.clientbound.ClientboundPingPacket])
 * with the same id.
 */
@SurfNettyPacket(DefaultIds.SERVERBOUND_PONG_PACKET)
class ServerboundPongPacket : NettyPacket<ServerboundPongPacket> {
    var pingId by notNull<Long>()
        private set

    internal constructor()

    constructor(pingId: Long) {
        this.pingId = pingId
    }

    override fun encode(buffer: SurfByteBuf) {
        buffer.writeLong(pingId)
    }

    override fun decode(buffer: SurfByteBuf): ServerboundPongPacket? {
        pingId = buffer.readLong()
        return this
    }
}