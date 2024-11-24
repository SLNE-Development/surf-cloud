package dev.slne.surf.cloud.standalone.server

import dev.slne.surf.cloud.api.common.netty.network.Connection
import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.server.server.ServerCloudServer
import dev.slne.surf.cloud.core.common.server.CloudServerImpl
import dev.slne.surf.cloud.standalone.player.StandaloneCloudPlayerImpl

class StandaloneServerImpl(
    uid: Long,
    group: String,
    name: String,
    proxy: Boolean,
    override val connection: Connection
) : CloudServerImpl(uid, group, name, proxy), ServerCloudServer {
    fun addPlayer(player: CloudPlayer) {
        users.add(player)
    }

    fun removePlayer(player: CloudPlayer) {
        users.remove(player)
    }
}