package dev.slne.surf.cloud.standalone.player.cache

import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.core.common.player.cache.CacheWire
import org.springframework.stereotype.Component
import java.util.*

@Component
class ServerCacheWire() : CacheWire {
    override fun requestFullSync(playerId: UUID) {
    }

    override fun sendToServer(packet: NettyPacket) {
    }
}