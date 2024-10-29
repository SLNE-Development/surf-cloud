package dev.slne.surf.cloud.core.netty.protocol.packets.cloud.phase.running.clientbound

import dev.slne.surf.cloud.api.meta.DefaultIds
import dev.slne.surf.cloud.api.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.netty.protocol.buffer.SurfByteBuf
import kotlin.properties.Delegates.notNull

/**
 * When sent to the client, client responds with a
 * [dev.slne.surf.cloud.core.netty.protocol.packets.cloud.phase.running.serverbound.ServerboundPongPacket]
 * packet with the same id.
 */
@SurfNettyPacket(DefaultIds.CLIENTBOUND_PING_PACKET)
class ClientboundPingPacket: NettyPacket<ClientboundPingPacket> {
    var pingId by notNull<Long>()
        private set

    internal constructor()

    constructor(pingId: Long) {
        this.pingId = pingId
    }

    override fun encode(buffer: SurfByteBuf) {
        buffer.writeLong(pingId)
    }

    override fun decode(buffer: SurfByteBuf): ClientboundPingPacket? {
        pingId = buffer.readLong()
        return this
    }
}