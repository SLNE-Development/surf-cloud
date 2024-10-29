package dev.slne.surf.cloud.core.netty.temp

import dev.slne.surf.cloud.api.netty.connection.NettyConnection
import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.netty.source.NettySource
import dev.slne.surf.cloud.api.netty.source.ProxiedNettySource
import dev.slne.surf.cloud.core.netty.AbstractNettyBase
import dev.slne.surf.cloud.core.netty.common.source.AbstractNettySource
import org.jetbrains.annotations.ApiStatus

abstract class AbstractNettyConnection<SELF : AbstractNettyConnection<SELF, Client, B>, Client : ProxiedNettySource<Client>, B : AbstractNettyBase<B, SELF, Client>>(
    override val base: B
) : NettyConnection<Client> { // Sorry Simon for the number of generics

    /**
     * Broadcast the packet on servers <br></br> On clients only to server
     *
     * @param packet the packet to broadcast
     */
    abstract fun broadcast(packet: NettyPacket<*>) // TODO: 12.10.2024 15:41 - when on client send a wrapped packet to server wich then broadcasts it to all clients
    abstract suspend fun close()

    @ApiStatus.OverrideOnly
    protected abstract fun sendPacket0(
        source: AbstractNettySource<Client>,
        packet: NettyPacket<*>
    )

    @Suppress("UNCHECKED_CAST")
    fun sendPacket(source: NettySource<*>, packet: NettyPacket<*>) {
        require(source is AbstractNettySource<*>) { "source must be an instance of AbstractNettySource" }

        base.checkPacket(packet)
        sendPacket0(source as AbstractNettySource<Client>, packet)
    }
}