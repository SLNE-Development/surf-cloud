package dev.slne.surf.cloud.core.netty.protocol.codec

import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.netty.protocol.buffer.wrap
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder


class NettyPacketEncoder : MessageToByteEncoder<NettyPacket<*>>() {
    override fun encode(ctx: ChannelHandlerContext, msg: NettyPacket<*>, out: ByteBuf) {
        val surfByteBuf = out.wrap()
        surfByteBuf.writeInt(msg.id)
        msg.encode(surfByteBuf)
    }
}

