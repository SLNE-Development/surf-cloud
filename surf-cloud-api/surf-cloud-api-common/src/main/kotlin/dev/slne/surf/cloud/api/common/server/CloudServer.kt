package dev.slne.surf.cloud.api.common.server

import dev.slne.surf.cloud.api.common.server.state.ServerState
import org.jetbrains.annotations.ApiStatus
import javax.annotation.ParametersAreNonnullByDefault

@ApiStatus.NonExtendable
@ParametersAreNonnullByDefault
interface CloudServer {
    //  UserList userList();

    val maxPlayerCount: Int
    val currentPlayerCount: Int
    val whitelist: Boolean
    val state: ServerState

    fun shutdown()
}
