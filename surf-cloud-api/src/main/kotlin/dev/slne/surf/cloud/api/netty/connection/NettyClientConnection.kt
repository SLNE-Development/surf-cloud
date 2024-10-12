package dev.slne.surf.cloud.api.netty.connection

import dev.slne.surf.cloud.api.netty.annotation.PlatformRequirement
import dev.slne.surf.cloud.api.netty.source.NettyServerSource
import org.jetbrains.annotations.ApiStatus

@PlatformRequirement(PlatformRequirement.Platform.CLIENT)
@ApiStatus.NonExtendable
interface NettyClientConnection : NettyConnection<NettyServerSource> {
    val source: NettyServerSource
}
