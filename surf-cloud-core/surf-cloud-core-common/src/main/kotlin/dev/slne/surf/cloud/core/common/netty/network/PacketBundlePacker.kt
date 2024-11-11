package dev.slne.surf.cloud.core.common.netty.network

import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.checkDecoded
import dev.slne.surf.cloud.core.common.netty.network.protocol.BundlerInfo
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder

class PacketBundlePacker(private val bundlerInfo: BundlerInfo) :
    MessageToMessageDecoder<NettyPacket>() {

    private var currentBundler: BundlerInfo.Bundler? = null


    override fun decode(ctx: ChannelHandlerContext, packet: NettyPacket, out: MutableList<Any>) {
        val currentBuilder = currentBundler

        if (currentBuilder != null) {
            verifyNonTerminalPacket(packet)
            val bundledPacket = currentBuilder.addPacket(packet)

            if (bundledPacket != null) {
                this.currentBundler = null
                out.add(bundledPacket)
            }

        } else {
            val newBundler = bundlerInfo.startPacketBundling(packet)

            if (newBundler != null) {
                verifyNonTerminalPacket(packet)
                this.currentBundler = newBundler
            } else {
                out.add(packet)

                if (packet.terminal) {
                    ctx.pipeline().remove(ctx.name())
                }
            }
        }
    }

    private fun verifyNonTerminalPacket(packet: NettyPacket) {
        checkDecoded(!packet.terminal) { "Terminal message received in bundle" }
    }
}