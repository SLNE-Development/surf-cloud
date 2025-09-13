package dev.slne.surf.cloud.core.common.player.cache

import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.player.cache.CloudPlayerCache
import dev.slne.surf.cloud.core.common.netty.network.protocol.BundlePacket
import java.util.*

abstract class AbstractCloudPlayerCache(
    override val uuid: UUID,
    private val changeCounter: ChangeCounter
) : CloudPlayerCache {
    protected fun send(packet: NettyPacket) {
        wireSend(packet)
    }

    protected abstract fun wireSend(packet: NettyPacket)
    abstract fun createBundle(packets: List<NettyPacket>): BundlePacket
}