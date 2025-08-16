package dev.slne.surf.cloud.standalone.player.cache

import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import java.util.*

interface PlayerSessionRouter {
    fun broadcast(playerUuid: UUID, packet: NettyPacket, except: Long? = null)
    fun sourceTokenFor(packet: NettyPacket): Any?
}