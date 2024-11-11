package dev.slne.surf.cloud.core.common.netty.network

import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.BundlerInfo
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageEncoder

class PacketBundleUnpacker(private val bundlerInfo: BundlerInfo): MessageToMessageEncoder<NettyPacket>() {
    override fun encode(ctx: ChannelHandlerContext, packet: NettyPacket, out: MutableList<Any>) {
        bundlerInfo.unbundlePacket(packet, out::add)
        if (packet.terminal) {
            ctx.pipeline().remove(ctx.name())
        }
    }
}