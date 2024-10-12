package dev.slne.surf.cloud.api.netty

import dev.slne.surf.cloud.api.netty.annotation.PlatformRequirement
import dev.slne.surf.cloud.api.netty.connection.NettyConnection
import dev.slne.surf.cloud.api.netty.source.ProxiedNettySource
import org.jetbrains.annotations.ApiStatus

@PlatformRequirement(PlatformRequirement.Platform.COMMON)
@ApiStatus.NonExtendable
interface NettyBase<Source : ProxiedNettySource<Source>> {
    val port: Int
    val host: String
    val name: String
    val connection: NettyConnection<Source>

    val isServer: Boolean
        get() = this is NettyServer

    val isClient: Boolean
        get() = this is NettyClient
}











