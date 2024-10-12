package dev.slne.surf.cloud.api.netty.source.tracker

import dev.slne.surf.cloud.api.netty.annotation.PlatformRequirement
import dev.slne.surf.cloud.api.netty.source.NettyClientSource
import io.netty.channel.Channel
import org.jetbrains.annotations.ApiStatus

@ApiStatus.NonExtendable
@PlatformRequirement(PlatformRequirement.Platform.SERVER)
interface ServerNettyClientTracker : NettyClientTracker<NettyClientSource> {
    fun client(channel: Channel): NettyClientSource?
}
