package dev.slne.surf.cloud.api.netty.connection

import dev.slne.surf.cloud.api.netty.annotation.PlatformRequirement
import dev.slne.surf.cloud.api.netty.source.NettyClientSource
import io.netty.channel.ServerChannel

@PlatformRequirement(PlatformRequirement.Platform.SERVER)
interface NettyServerConnection : NettyConnection<NettyClientSource> {
    override val channel: ServerChannel
}
