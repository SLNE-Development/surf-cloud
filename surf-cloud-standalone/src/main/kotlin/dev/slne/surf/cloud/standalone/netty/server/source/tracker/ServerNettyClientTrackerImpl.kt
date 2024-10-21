package dev.slne.surf.cloud.standalone.netty.server.source.tracker

import dev.slne.surf.cloud.api.netty.source.NettyClientSource
import dev.slne.surf.cloud.api.netty.source.tracker.ServerNettyClientTracker
import dev.slne.surf.cloud.core.netty.AbstractNettyBase
import dev.slne.surf.cloud.core.netty.common.source.tracker.NettyClientTrackerImpl
import io.netty.channel.Channel

class ServerNettyClientTrackerImpl(nettyBase: AbstractNettyBase<*, *, NettyClientSource>) :
    NettyClientTrackerImpl<NettyClientSource>(nettyBase), ServerNettyClientTracker {
    override fun client(channel: Channel) = _clients.find { source -> source.channel === channel }
}
