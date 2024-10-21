package dev.slne.surf.cloud.standalone.netty.server

import dev.slne.surf.cloud.api.netty.NettyServer
import dev.slne.surf.cloud.api.netty.source.NettyClientSource
import dev.slne.surf.cloud.core.netty.AbstractNettyBase
import dev.slne.surf.cloud.standalone.netty.server.connection.NettyServerConnectionImpl
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("independent")
class SurfNettyServer :
    AbstractNettyBase<SurfNettyServer, NettyServerConnectionImpl, NettyClientSource>("independent"),
    NettyServer {

    override fun createConnectionInstance() = NettyServerConnectionImpl(this)
}
