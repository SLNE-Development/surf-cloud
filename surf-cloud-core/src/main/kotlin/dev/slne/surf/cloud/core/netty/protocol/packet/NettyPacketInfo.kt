package dev.slne.surf.cloud.core.netty.protocol.packet

import dev.slne.surf.cloud.api.netty.source.NettyClientSource
import dev.slne.surf.cloud.api.netty.source.NettyServerSource
import dev.slne.surf.cloud.api.netty.source.NettySource
import dev.slne.surf.cloud.api.netty.source.ProxiedNettySource
import org.jetbrains.annotations.Contract

data class NettyPacketInfo(
    val connection: Any, // TODO
    val proxiedSource: ProxiedNettySource<*>?
) {
    constructor(source: NettySource<*>) : this(source, null)

    @Contract(pure = true)
    fun asClientSource() = source as NettyClientSource

    @Contract(pure = true)
    fun asServerSource() = source as NettyServerSource

}
