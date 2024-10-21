package dev.slne.surf.cloud.core.netty.protocol.packets.server

import dev.slne.surf.cloud.api.meta.DefaultIds
import dev.slne.surf.cloud.api.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.netty.protocol.buffer.SurfByteBuf

@SurfNettyPacket(id = DefaultIds.CLIENT_QUIT)
class ClientQuitPacket : NettyPacket<ClientQuitPacket> {
    internal constructor()

    override fun decode(buffer: SurfByteBuf): ClientQuitPacket? {
        TODO("Not yet implemented")
    }

    override fun encode(buffer: SurfByteBuf) {
        TODO("Not yet implemented")
    }
}
