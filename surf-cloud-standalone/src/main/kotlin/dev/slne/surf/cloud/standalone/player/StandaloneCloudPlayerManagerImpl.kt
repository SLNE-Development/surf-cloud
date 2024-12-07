package dev.slne.surf.cloud.standalone.player

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.common.player.CloudPlayerManager
import dev.slne.surf.cloud.api.common.util.logger
import dev.slne.surf.cloud.core.common.player.CloudPlayerManagerImpl
import dev.slne.surf.cloud.core.common.util.checkInstantiationByServiceLoader
import dev.slne.surf.cloud.standalone.server.StandaloneCloudServerImpl
import dev.slne.surf.cloud.standalone.server.StandaloneProxyCloudServerImpl
import dev.slne.surf.cloud.standalone.server.asStandaloneServer
import dev.slne.surf.cloud.standalone.server.serverManagerImpl
import java.util.*

@AutoService(CloudPlayerManager::class)
class StandaloneCloudPlayerManagerImpl : CloudPlayerManagerImpl<StandaloneCloudPlayerImpl>() {
    private val log = logger()

    init {
        checkInstantiationByServiceLoader()
    }

    override suspend fun createPlayer(
        uuid: UUID,
        serverUid: Long,
        proxy: Boolean
    ): StandaloneCloudPlayerImpl {
        return StandaloneCloudPlayerImpl(uuid).also {
            val server = serverUid.toServer()
            if (server != null) {
                if (proxy) {
                    check(server is StandaloneProxyCloudServerImpl) { "Server with id $serverUid is not a proxy server but specified as proxy" }
                    it.proxyServer = server
                } else {
                    check(server is StandaloneCloudServerImpl) { "Server with id $serverUid is not a standalone server but specified as standalone" }
                    it.server = server
                }
            } else {
                log.atWarning()
                    .log("Could not find server with id $serverUid for player $uuid")
            }
        }
    }

    override suspend fun updateProxyServer(player: StandaloneCloudPlayerImpl, serverUid: Long) {
        val server = serverUid.toServer()
        check(server == null || server is StandaloneProxyCloudServerImpl) { "Server with id $serverUid is not a proxy server but specified as proxy" }

        if (server != null) {
            player.proxyServer = server
        } else {
            logServerNotFound(serverUid, player)
        }
    }

    override suspend fun updateServer(player: StandaloneCloudPlayerImpl, serverUid: Long) {
        val server = serverUid.toServer()
        check(server == null || server is StandaloneCloudServerImpl) { "Server with id $serverUid is not a standalone server but specified as standalone" }

        if (server != null) {
            player.server = server
        } else {
            logServerNotFound(serverUid, player)
        }
    }

    override suspend fun removeProxyServer(player: StandaloneCloudPlayerImpl, serverUid: Long) {
        val server = serverUid.toServer()
        check(server == null || server is StandaloneProxyCloudServerImpl) { "Server with id $serverUid is not a proxy server but specified as proxy" }

        if (server != null && player.proxyServer == server) {
            player.proxyServer = null
        } else {
            logServerNotFound(serverUid, player)
        }
    }

    override suspend fun removeServer(player: StandaloneCloudPlayerImpl, serverUid: Long) {
        val server = serverUid.toServer()
        check(server == null || server is StandaloneCloudServerImpl) { "Server with id $serverUid is not a standalone server but specified as standalone" }

        if (server != null && player.server == server) {
            player.server = null
        } else {
            logServerNotFound(serverUid, player)
        }
    }

    override fun getProxyServerUid(player: StandaloneCloudPlayerImpl) = player.proxyServer?.uid
    override fun getServerUid(player: StandaloneCloudPlayerImpl) = player.server?.uid

    override suspend fun onConnect(
        uuid: UUID,
        player: StandaloneCloudPlayerImpl
    ) {
    }

    override suspend fun onNetworkDisconnect(
        uuid: UUID,
        player: StandaloneCloudPlayerImpl,
        oldProxy: Long?,
        oldServer: Long?
    ) {
        oldServer?.toServer()?.asStandaloneServer()?.queue?.handlePlayerLeave(player)
    }

    private suspend fun Long.toServer() = serverManagerImpl.retrieveServerById(this)

    private fun logServerNotFound(uid: Long, player: StandaloneCloudPlayerImpl) {
        log.atWarning()
            .log("Could not find server with id $uid for player ${player.uuid}")
    }
}