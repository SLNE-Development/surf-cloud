package dev.slne.surf.cloud.core.client.player

import dev.slne.surf.cloud.api.client.netty.packet.fireAndAwaitOrThrow
import dev.slne.surf.cloud.api.common.player.task.PrePlayerJoinTask
import dev.slne.surf.cloud.core.common.messages.MessageManager
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundRequestPlayerPersistentDataContainer
import dev.slne.surf.cloud.core.common.player.CloudPlayerManagerImpl
import dev.slne.surf.cloud.core.common.player.CommonOfflineCloudPlayerImpl
import dev.slne.surf.cloud.core.common.player.playerManagerImpl
import dev.slne.surf.surfapi.core.api.nbt.fast
import dev.slne.surf.surfapi.core.api.util.logger
import net.kyori.adventure.audience.Audience
import java.util.*
import java.util.concurrent.TimeUnit

abstract class CommonClientCloudPlayerManagerImpl<Platform : Audience, P : ClientCloudPlayerImpl<Platform>> :
    CloudPlayerManagerImpl<P>() {
    private val log = logger()

    override suspend fun updateProxyServer(
        player: P,
        serverName: String
    ) {
        player.proxyServerName = serverName
    }

    override suspend fun updateServer(
        player: P,
        serverName: String
    ) {
        player.serverName = serverName
    }

    override suspend fun removeProxyServer(
        player: P,
        serverName: String
    ) {
        player.proxyServerName = null
    }

    override suspend fun removeServer(
        player: P,
        serverName: String
    ) {
        player.serverName = null
    }

    override fun getProxyServerName(player: P): String? {
        return player.proxyServerName
    }

    override fun getServerName(player: P): String? {
        return player.serverName
    }

    override fun getOfflinePlayer(
        uuid: UUID,
        createIfNotExists: Boolean
    ): CommonOfflineCloudPlayerImpl {
        return OfflineCloudPlayerImpl(uuid, createIfNotExists)
    }

    override suspend fun onNetworkConnect(uuid: UUID, player: P) {
        try {
            val ppdcData = ServerboundRequestPlayerPersistentDataContainer(uuid)
                .fireAndAwaitOrThrow()
                .nbt

            player.ppdcData = ppdcData.fast()
        } catch (e: Exception) {
            log.atWarning()
                .withCause(e)
                .atMostEvery(5, TimeUnit.SECONDS)
                .log("Could not fetch persistent player data container for player $uuid, denying join")

            throw PreJoinDenied(PrePlayerJoinTask.Result.DENIED(MessageManager.couldNotFetchPlayerData))
        }

        super.onNetworkConnect(uuid, player)
    }

    abstract fun getAudience(uuid: UUID): Audience?
}

val commonPlayerManagerImpl get() = playerManagerImpl as CommonClientCloudPlayerManagerImpl<*, *>