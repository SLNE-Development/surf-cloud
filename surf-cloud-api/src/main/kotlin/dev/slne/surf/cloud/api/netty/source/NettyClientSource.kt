package dev.slne.surf.cloud.api.netty.source

import dev.slne.surf.cloud.api.netty.annotation.PlatformRequirement
import io.netty.channel.Channel
import org.jetbrains.annotations.ApiStatus

@ApiStatus.NonExtendable
@PlatformRequirement(PlatformRequirement.Platform.SERVER)
interface NettyClientSource : ProxiedNettySource<NettyClientSource> {
    val channel: Channel
}
