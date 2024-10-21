package dev.slne.surf.cloud.core.netty.protocol.packet.handler

import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.util.logger
import dev.slne.surf.cloud.core.netty.AbstractNettyBase
import dev.slne.surf.cloud.core.netty.protocol.packets.ProxiedNettyPacket
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise

class NettyPacketHandler(private val base: AbstractNettyBase<*, *, *>) : ChannelDuplexHandler() {
    private val log = logger()

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg !is NettyPacket<*>) {
            super.channelRead(ctx, msg)
            return
        }

        val (proxiedSource, packet) = when (msg) {
            is ProxiedNettyPacket -> msg.source to msg.packet
            else -> null to msg
        }

        val source = base.connection.source(ctx.channel())
        checkNotNull(source) { "No source found for channel" }

        base.onPacketReceived(packet, source, proxiedSource)

        // if !cancelled // TODO: Implement this?
        super.channelRead(ctx, msg)
    }

    @Throws(Exception::class)
    override fun write(
        ctx: ChannelHandlerContext,
        msg: Any,
        promise: ChannelPromise
    ) { // TODO: 18.09.2024 18:05 - currently not needed. Ever needed?
//    if (!(msg instanceof final NettyPacket<?> packet)) {
//      super.write(ctx, msg, promise);
//      return;
//    }
//
//    final AbstractProxiedNettySource proxiedSource;
//    final NettyPacket<?> finalPacket;
//    if (packet instanceof final ProxiedNettyPacket proxiedNettyPacket) {
//      finalPacket = proxiedNettyPacket.getPacket();
//      proxiedSource = proxiedNettyPacket.getSource();
//    } else {
//      finalPacket = packet;
//      proxiedSource = null;
//    }
//
//    final AbstractNettySource source = base.container().source(ctx.channel()).orElseThrow();
//    base.onPacketSent(finalPacket, source, proxiedSource);
//
//    // if !cancelled // TODO: Implement this?
        super.write(ctx, msg, promise)
    }

    override fun handlerAdded(ctx: ChannelHandlerContext) {
        log.atInfo()
            .log("Joined. (%s)", ctx.channel().remoteAddress())
    }

    override fun handlerRemoved(ctx: ChannelHandlerContext) {
        log.atInfo()
            .log("Quited. (%s)", ctx.channel().remoteAddress())
    }
}
