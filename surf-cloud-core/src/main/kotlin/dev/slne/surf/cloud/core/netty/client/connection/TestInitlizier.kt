package dev.slne.surf.cloud.core.netty.client.connection

import dev.slne.surf.cloud.api.util.logger
import dev.slne.surf.cloud.core.netty.protocol.channel.ChannelInitializerModifier
import io.netty.channel.socket.SocketChannel
import io.netty.handler.timeout.ReadTimeoutHandler

//@Component
class TestInitlizier : ChannelInitializerModifier {
    private val log = logger()

    override fun modify(ch: SocketChannel) {
        log.atInfo().log("Modifying channel")
        ch.pipeline().addLast(ReadTimeoutHandler(5))
    }
}
