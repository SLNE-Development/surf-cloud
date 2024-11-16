package dev.slne.surf.cloud.api.common.server

import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.server.state.ServerState
import net.kyori.adventure.audience.ForwardingAudience
import org.jetbrains.annotations.ApiStatus
import javax.annotation.ParametersAreNonnullByDefault

@ApiStatus.NonExtendable
@ParametersAreNonnullByDefault
interface CloudServer: ForwardingAudience {
    val uid: Long
    val group: String
    val name: String

    val maxPlayerCount: Int
    val currentPlayerCount: Int
    val whitelist: Boolean
    val state: ServerState

    val users: UserList

    suspend fun sendAll(server: CloudServer)
    suspend fun sendAll(server: CloudServer, filter: (CloudPlayer) -> Boolean)

    fun shutdown()
}
