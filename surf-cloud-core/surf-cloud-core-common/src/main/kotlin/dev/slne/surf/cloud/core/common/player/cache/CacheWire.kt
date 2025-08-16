package dev.slne.surf.cloud.core.common.player.cache

import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import java.util.*

interface CacheWire {
    fun requestFullSync(playerId: UUID)
    fun sendToServer(packet: NettyPacket)
}