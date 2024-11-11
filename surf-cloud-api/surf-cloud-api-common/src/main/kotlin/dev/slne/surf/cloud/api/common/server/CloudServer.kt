package dev.slne.surf.cloud.api.common.server

import dev.slne.surf.cloud.api.common.server.state.ServerState
import net.kyori.adventure.audience.ForwardingAudience
import org.jetbrains.annotations.ApiStatus
import javax.annotation.ParametersAreNonnullByDefault

@ApiStatus.NonExtendable
@ParametersAreNonnullByDefault
interface CloudServer: ForwardingAudience {
    //  UserList userList();
    val uid: Long

    val maxPlayerCount: Int
    val currentPlayerCount: Int
    val whitelist: Boolean
    val state: ServerState

    fun shutdown()
}
