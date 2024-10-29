package dev.slne.surf.cloud.core.netty.common.source

import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.netty.source.NettySource
import dev.slne.surf.cloud.api.netty.source.ProxiedNettySource
import dev.slne.surf.cloud.core.netty.network.Connection


abstract class AbstractNettySource<Client : ProxiedNettySource<Client>>(val connection: Connection) :
    NettySource<Client> {
    override fun sendPacket(packet: NettyPacket<*>) {
        connection.send(packet)
    }
}
