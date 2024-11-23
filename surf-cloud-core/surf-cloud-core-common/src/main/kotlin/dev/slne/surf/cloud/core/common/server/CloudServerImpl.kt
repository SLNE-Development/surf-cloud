package dev.slne.surf.cloud.core.common.server

import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.api.common.server.UserListImpl
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientInformation

open class CloudServerImpl(
    override val uid: Long,
    override val group: String,
    override val name: String,
    override val proxy: Boolean
) : CloudServer {
    var information = ClientInformation.NOT_AVAILABLE
    override val users = UserListImpl()
    override suspend fun sendAll(server: CloudServer) {
        TODO("Not yet implemented")
    }

    override suspend fun sendAll(
        server: CloudServer,
        filter: (CloudPlayer) -> Boolean
    ) {
        TODO("Not yet implemented")
    }

    override fun shutdown() {
        TODO("Not yet implemented")
    }

    override val maxPlayerCount get() = information.maxPlayerCount
    override val currentPlayerCount get() = information.currentPlayerCount
    override val whitelist get() = information.whitelist
    override val state get() = information.state

    override fun audiences() = users
}
