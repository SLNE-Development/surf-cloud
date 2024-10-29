package dev.slne.surf.cloud.core.netty.client.source

import dev.slne.surf.cloud.api.netty.source.NettyClientSource
import dev.slne.surf.cloud.core.netty.AbstractNettyBase
import dev.slne.surf.cloud.core.netty.common.source.AbstractProxiedNettySource
import dev.slne.surf.cloud.core.netty.protocol.packet.handler.ServerRunningPacketListener
import io.netty.channel.Channel


class NettyClientSourceImpl(
    base: AbstractNettyBase<*, *, NettyClientSource>,
    override val channel: Channel
) : AbstractProxiedNettySource<NettyClientSource>(base), NettyClientSource {
    private var _connection: ServerRunningPacketListener? = null
    val connection get() = _connection ?: error("Connection not set")

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NettyClientSourceImpl) return false
        if (!super.equals(other)) return false

        if (channel != other.channel) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + channel.hashCode()
        return result
    }

    override fun toString(): String {
        return "NettyClientSourceImpl(channel=$channel) ${super.toString()}"
    }
}
