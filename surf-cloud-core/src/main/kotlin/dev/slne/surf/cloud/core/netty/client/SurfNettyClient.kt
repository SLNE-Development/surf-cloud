package dev.slne.surf.cloud.core.netty.client

import dev.slne.surf.cloud.api.netty.NettyClient
import dev.slne.surf.cloud.api.netty.source.NettyServerSource
import dev.slne.surf.cloud.core.netty.AbstractNettyBase
import dev.slne.surf.cloud.core.netty.client.connection.NettyClientConnectionImpl
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("client")
class SurfNettyClient :
    AbstractNettyBase<SurfNettyClient, NettyClientConnectionImpl, NettyServerSource>("client"),
    NettyClient {
    override fun createConnectionInstance() = NettyClientConnectionImpl(this)
}
