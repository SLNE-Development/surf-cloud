package dev.slne.surf.cloud.core.netty.protocol.channel

import io.netty.channel.socket.SocketChannel
import org.jetbrains.annotations.ApiStatus.OverrideOnly

fun interface ChannelInitializerModifier {

    @OverrideOnly
    @Throws(Exception::class)
    fun modify(channel: SocketChannel)
}
