package dev.slne.surf.cloud.core.netty.network

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import java.io.IOException

class PacketDecoder<T : PacketListener>(private val protocolInfo: ProtocolInfo<T>) :
    ByteToMessageDecoder() {
    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        val readableBytes = buf.readableBytes()
        if (readableBytes == 0) return

        val packet = protocolInfo.codec.decode(buf)
        if (buf.readableBytes() > 0) {
            val packetName = packet.javaClass.simpleName
            throw IOException("Packet ${protocolInfo.id.name}/$packetName was larger than I expected, found ${buf.readableBytes()} bytes extra whilst reading packet $packetName")
        }

        out.add(packet)
        ProtocolSwapHandler.handleInboundTerminalPacket(ctx, packet)
    }
}