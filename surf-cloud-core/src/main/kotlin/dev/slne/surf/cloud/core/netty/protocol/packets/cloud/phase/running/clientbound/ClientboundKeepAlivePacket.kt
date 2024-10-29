package dev.slne.surf.cloud.core.netty.protocol.packets.cloud.phase.running.clientbound

import dev.slne.surf.cloud.api.meta.DefaultIds
import dev.slne.surf.cloud.api.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.netty.protocol.buffer.SurfByteBuf
import kotlin.properties.Delegates.notNull

/**
 * The server will frequently send out a keep-alive, each containing a random ID. The client must
 * respond with the same payload (see
 * [dev.slne.surf.cloud.core.netty.protocol.packets.cloud.phase.running.serverbound.ServerboundKeepAlivePacket]).
 * If the client does not respond to a
 * Keep Alive packet within `15 seconds` after it was sent, the server kicks the client. Vice versa,
 * if the server does not send any keep-alives for `20 seconds`, the client will disconnect and yields
 * a "Timed out" exception.
 */
@SurfNettyPacket(DefaultIds.CLIENTBOUND_KEEP_ALIVE_PACKET)
class ClientboundKeepAlivePacket : NettyPacket<ClientboundKeepAlivePacket> {
    var keepAliveId by notNull<Long>()
        private set

    internal constructor()

    constructor(keepAliveId: Long) {
        this.keepAliveId = keepAliveId
    }

    override fun encode(buffer: SurfByteBuf) {
        buffer.writeLong(keepAliveId)
    }

    override fun decode(buffer: SurfByteBuf): ClientboundKeepAlivePacket {
        keepAliveId = buffer.readLong()
        return this
    }
}