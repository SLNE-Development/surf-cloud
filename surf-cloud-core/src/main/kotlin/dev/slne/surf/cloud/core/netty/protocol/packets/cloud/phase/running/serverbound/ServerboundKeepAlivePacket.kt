package dev.slne.surf.cloud.core.netty.protocol.packets.cloud.phase.running.serverbound

import dev.slne.surf.cloud.api.meta.DefaultIds
import dev.slne.surf.cloud.api.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.netty.protocol.buffer.SurfByteBuf
import kotlin.properties.Delegates.notNull

/**
 * The server will frequently send out a keep-alive (see
 * [dev.slne.surf.cloud.core.netty.protocol.packets.cloud.phase.running.clientbound.ClientboundKeepAlivePacket]),
 * each containing a random ID. The client must respond with the same packet.
 */
@SurfNettyPacket(DefaultIds.SERVERBOUND_KEEP_ALIVE_PACKET)
class ServerboundKeepAlivePacket: NettyPacket<ServerboundKeepAlivePacket> {
    var keepAliveId by notNull<Long>()
        private set

    internal constructor()

    constructor(keepAliveId: Long) {
        this.keepAliveId = keepAliveId
    }

    override fun encode(buffer: SurfByteBuf) {
        buffer.writeLong(keepAliveId)
    }

    override fun decode(buffer: SurfByteBuf): ServerboundKeepAlivePacket {
        keepAliveId = buffer.readLong()
        return this
    }
}