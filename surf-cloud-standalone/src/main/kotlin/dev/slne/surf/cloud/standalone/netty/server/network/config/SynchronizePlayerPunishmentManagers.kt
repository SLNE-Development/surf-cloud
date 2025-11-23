package dev.slne.surf.cloud.standalone.netty.server.network.config

import dev.slne.surf.cloud.api.common.netty.NettyClient
import dev.slne.surf.cloud.api.common.plugin.spring.task.CloudInitialSynchronizeTask
import dev.slne.surf.cloud.core.common.netty.network.protocol.synchronizing.ClientboundSynchronizePlayerMutes
import dev.slne.surf.cloud.standalone.player.StandaloneCloudPlayerImpl
import dev.slne.surf.cloud.standalone.player.standalonePlayerManagerImpl

object SynchronizePlayerPunishmentManagers : CloudInitialSynchronizeTask {
    override suspend fun execute(client: NettyClient) {
        val players = standalonePlayerManagerImpl.getRawOnlinePlayers()
        synchronizeCachedMutes(client, players)
    }

    private fun synchronizeCachedMutes(
        client: NettyClient,
        players: List<StandaloneCloudPlayerImpl>
    ) {
        for (player in players) {
            val mutes = player.punishmentManager.rawCachedMutes()
            if (mutes.isEmpty()) continue
            client.fireAndForget(ClientboundSynchronizePlayerMutes(player.uuid, mutes))
        }
    }
}