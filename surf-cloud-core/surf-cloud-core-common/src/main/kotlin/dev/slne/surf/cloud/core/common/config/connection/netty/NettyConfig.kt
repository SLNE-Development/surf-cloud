package dev.slne.surf.cloud.core.common.config.connection.netty

import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment
import org.spongepowered.configurate.objectmapping.meta.Setting

@ConfigSerializable
data class NettyConfig(
    @Comment("Port for netty connection")
    @Setting("port")
    val port: Int = 5555,

    @Comment("Host for netty connection")
    @Setting("host")
    val host: String = "127.0.0.1",

    @Comment("Reconnect delay for netty connection in seconds")
    @Setting("reconnect-delay")
    val reconnectDelay: Int = 3,

    @Comment("Whether to use epoll for netty connection")
    @Setting("use-epoll")
    val useEpoll: Boolean = true
)