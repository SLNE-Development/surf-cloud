package dev.slne.surf.cloud.core.netty.common.source

import dev.slne.surf.cloud.api.netty.source.NettyServerSource
import dev.slne.surf.cloud.core.netty.AbstractNettyBase

class NettyServerSourceImpl(nettyBase: AbstractNettyBase<*, *, NettyServerSource>) :
    AbstractProxiedNettySource<NettyServerSource>(nettyBase), NettyServerSource
