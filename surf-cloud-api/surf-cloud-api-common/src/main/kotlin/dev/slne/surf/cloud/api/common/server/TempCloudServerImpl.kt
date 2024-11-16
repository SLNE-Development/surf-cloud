package dev.slne.surf.cloud.api.common.server

import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.server.state.ServerState
import net.kyori.adventure.audience.Audience

class TempCloudServerImpl: CloudServer {
    override val uid: Long
        get() = TODO("Not yet implemented")
    override val group: String
        get() = TODO("Not yet implemented")
    override val name: String
        get() = TODO("Not yet implemented")
    override val maxPlayerCount: Int
        get() = TODO("Not yet implemented")
    override val currentPlayerCount: Int
        get() = TODO("Not yet implemented")
    override val whitelist: Boolean
        get() = TODO("Not yet implemented")
    override val state: ServerState
        get() = TODO("Not yet implemented")
    val _users = UserListImpl()
    override val users: UserList
        get() = _users

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
    }

    override fun audiences(): MutableIterable<Audience> {
        return users.snapshot()
    }
}