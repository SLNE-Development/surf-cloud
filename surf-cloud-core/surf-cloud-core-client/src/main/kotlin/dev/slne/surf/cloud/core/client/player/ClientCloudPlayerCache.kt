package dev.slne.surf.cloud.core.client.player

import dev.slne.surf.cloud.api.client.netty.packet.fireAndForget
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.player.cache.key.CacheKey
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.ServerboundBundlePacket
import dev.slne.surf.cloud.core.common.player.cache.AbstractCloudPlayerCache
import dev.slne.surf.cloud.core.common.player.cache.ChangeCounter
import java.util.*

class ClientCloudPlayerCache(uuid: UUID, changeCounter: ChangeCounter) :
    AbstractCloudPlayerCache(uuid, changeCounter) {

    override fun wireSend(packet: NettyPacket) {
        packet.fireAndForget()
    }

    override fun createBundle(packets: List<NettyPacket>) = ServerboundBundlePacket(packets)
    override fun <T : Any> get(key: CacheKey<T>): T? {
        TODO("Not yet implemented")
    }

    override fun <T : Any> set(
        key: CacheKey.Value<T>,
        value: T
    ) {
        TODO("Not yet implemented")
    }

    override fun remove(key: CacheKey<*>) {
        TODO("Not yet implemented")
    }
}