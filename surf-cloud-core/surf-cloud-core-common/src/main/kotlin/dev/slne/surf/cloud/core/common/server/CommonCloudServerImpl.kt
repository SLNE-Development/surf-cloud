package dev.slne.surf.cloud.core.common.server

import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.server.CommonCloudServer
import dev.slne.surf.cloud.api.common.server.UserListImpl
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientInformation

abstract class CommonCloudServerImpl(
    override val uid: Long,
    override val group: String,
    override val name: String,
    override val users: UserListImpl,

    @Volatile
    var information: ClientInformation
) : CommonCloudServer {
    override suspend fun sendAll(server: CommonCloudServer) {
        TODO("Not yet implemented")
    }

    override suspend fun sendAll(
        server: CommonCloudServer,
        filter: (CloudPlayer) -> Boolean
    ) {
        TODO("Not yet implemented")
    }

    override fun shutdown() {
        TODO("Not yet implemented")
    }

    override val maxPlayerCount get() = information.maxPlayerCount
    override val currentPlayerCount get() = users.size
    override val state get() = information.state

    override fun audiences() = users
    override fun toString(): String {
        return "CloudServerImpl(group='$group', uid=$uid, name='$name, users=$users, information=$information, maxPlayerCount=$maxPlayerCount, currentPlayerCount=$currentPlayerCount, state=$state)"
    }
}
