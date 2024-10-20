package dev.slne.surf.cloud.core.netty.protocol.packets.cloud

import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.netty.protocol.buffer.SurfByteBuf

class CloudServerInfoNettyPacket : NettyPacket<CloudServerInfoNettyPacket>() {
    override fun decode(buffer: SurfByteBuf): CloudServerInfoNettyPacket? {
        TODO("Not yet implemented")
    }

    override fun encode(buffer: SurfByteBuf) {
        TODO("Not yet implemented")
    }
}
