package dev.slne.surf.cloud.core.netty.protocol.codec

import com.google.common.flogger.StackSize
import dev.slne.surf.cloud.api.netty.protocol.buffer.wrap
import dev.slne.surf.cloud.api.util.logger
import dev.slne.surf.cloud.core.netty.common.registry.packet.NettyPacketRegistry
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder
import java.util.concurrent.TimeUnit

class NettyPacketDecoder : MessageToMessageDecoder<ByteBuf>() {
    private val log = logger()

    override fun decode(ctx: ChannelHandlerContext, msg: ByteBuf, out: MutableList<Any>) {
        if (!msg.isReadable(4)) return

        val packetId = msg.readInt()
        val packet = NettyPacketRegistry.createPacket(packetId)

        if (packet == null) {
            log.atWarning()
                .withStackTrace(StackSize.MEDIUM)
                .atMostEvery(5, TimeUnit.SECONDS)
                .log("Received unknown packet with id %d", packetId)
            return
        }

        val surfByteBuf = msg.wrap()
        val decoded = packet.decode(surfByteBuf)

        if (decoded != null) {
            out.add(decoded)
        }
    }
}
