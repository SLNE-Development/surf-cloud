package dev.slne.surf.cloudtest.standalone.test.packet

import dev.slne.surf.cloud.api.common.meta.PacketCodec
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf

@SurfNettyPacket("test:test", PacketFlow.BIDIRECTIONAL)
class TestPacket(val test: String) : NettyPacket() {

    companion object {
        @PacketCodec
        val codec = packetCodec(TestPacket::write, ::TestPacket)
    }

    private constructor(buf: SurfByteBuf) : this(buf.readUtf())

    private fun write(buf: SurfByteBuf) {
        buf.writeUtf(test)
    }
}