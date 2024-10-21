package dev.slne.surf.cloud.core.netty.common.source

import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.netty.source.NettySource
import dev.slne.surf.cloud.api.netty.source.ProxiedNettySource
import dev.slne.surf.cloud.core.netty.AbstractNettyBase


abstract class AbstractNettySource<Client : ProxiedNettySource<Client>>(override val base: AbstractNettyBase<*, *, Client>) :
    NettySource<Client> {
    override fun sendPacket(packet: NettyPacket<*>) {
        base.connection.sendPacket(this, packet)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbstractNettySource<*>) return false

        if (base != other.base) return false

        return true
    }

    override fun hashCode(): Int {
        return base.hashCode()
    }

    override fun toString(): String {
        return "AbstractNettySource(base=$base)"
    }
}
