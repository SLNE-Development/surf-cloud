package dev.slne.surf.cloud.api.netty.connection

import dev.slne.surf.cloud.api.netty.NettyBase
import dev.slne.surf.cloud.api.netty.annotation.PlatformRequirement
import dev.slne.surf.cloud.api.netty.source.ProxiedNettySource
import dev.slne.surf.cloud.api.netty.source.tracker.NettyClientTracker
import dev.slne.surf.cloud.api.netty.util.ConnectionStateAccessor
import io.netty.channel.Channel
import org.jetbrains.annotations.ApiStatus

@PlatformRequirement(PlatformRequirement.Platform.COMMON)
@ApiStatus.NonExtendable
interface NettyConnection<Client : ProxiedNettySource<Client>> : ConnectionStateAccessor {
    val base: NettyBase<Client>
    val channel: Channel
    val clientTracker: NettyClientTracker<Client>

    fun source(channel: Channel): Client?
}
