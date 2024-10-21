package dev.slne.surf.cloud.core.netty.protocol.packets.server

import dev.slne.surf.cloud.api.meta.DefaultIds
import dev.slne.surf.cloud.api.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.core.util.random

@SurfNettyPacket(id = DefaultIds.KEEP_ALIVE_PACKET)
class KeepAlivePacket : NettyPacket<KeepAlivePacket>() {
    var keepAliveId: Long
        private set

    init {
        keepAliveId = random.nextLong()
    }

    override fun decode(buffer: SurfByteBuf): KeepAlivePacket {
        return this
    }

    override fun encode(buffer: SurfByteBuf) {
        buffer.writeLong(keepAliveId)
    }
}
