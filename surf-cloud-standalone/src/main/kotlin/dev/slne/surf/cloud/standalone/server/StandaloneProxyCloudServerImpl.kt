package dev.slne.surf.cloud.standalone.server

import dev.slne.surf.cloud.api.common.netty.network.Connection
import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.server.server.ServerProxyCloudServer
import dev.slne.surf.cloud.core.common.server.ProxyCloudServerImpl
import dev.slne.surf.cloud.standalone.player.StandaloneCloudPlayerImpl

class StandaloneProxyCloudServerImpl(
    uid: Long,
    group: String,
    name: String,
    override val connection: Connection
) : ProxyCloudServerImpl(uid, group, name), ServerProxyCloudServer {
    fun addPlayer(player: StandaloneCloudPlayerImpl) {
        users.add(player)
    }

    fun removePlayer(player: StandaloneCloudPlayerImpl) {
        users.remove(player)
    }
}