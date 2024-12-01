package dev.slne.surf.cloud.standalone.server

import dev.slne.surf.cloud.api.common.netty.network.Connection
import dev.slne.surf.cloud.api.server.server.ServerCloudServer
import dev.slne.surf.cloud.core.common.server.CloudServerImpl
import dev.slne.surf.cloud.standalone.player.StandaloneCloudPlayerImpl

class StandaloneCloudServerImpl(
    uid: Long,
    group: String,
    name: String,
    override val connection: Connection
) : CloudServerImpl(uid, group, name), ServerCloudServer {
    fun addPlayer(player: StandaloneCloudPlayerImpl) {
        users.add(player)
    }
    fun removePlayer(player: StandaloneCloudPlayerImpl) {
        users.remove(player)
    }

}