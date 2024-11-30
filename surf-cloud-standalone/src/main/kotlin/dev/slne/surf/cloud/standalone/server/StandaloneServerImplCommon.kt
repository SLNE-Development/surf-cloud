package dev.slne.surf.cloud.standalone.server

import dev.slne.surf.cloud.api.common.netty.network.Connection
import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.ConnectionResult
import dev.slne.surf.cloud.api.server.server.ServerCommonCloudServer
import dev.slne.surf.cloud.core.common.server.CommonCloudServerImpl
import dev.slne.surf.cloud.standalone.player.StandaloneCloudPlayerImpl
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class StandaloneServerImplCommon(
    uid: Long,
    group: String,
    name: String,
    proxy: Boolean,
    override val connection: Connection
) : CommonCloudServerImpl(uid, group, name, proxy), ServerCommonCloudServer {
    private val connectionThrottlerer = Mutex()

    fun addPlayer(player: CloudPlayer) {
        users.add(player)
    }

    fun removePlayer(player: CloudPlayer) {
        users.remove(player)
    }

    suspend fun connectPlayer(player: StandaloneCloudPlayerImpl): ConnectionResult {
        connectionThrottlerer.withLock {
            if (player.server == this || player.proxyServer == this) {
                return ConnectionResult.ALREADY_CONNECTED
            }

            if (emptySlots <= 0) { // TODO: 26.11.2024 20:36 - add bypass perm
                return ConnectionResult.SERVER_FULL
            }


        }
    }
}