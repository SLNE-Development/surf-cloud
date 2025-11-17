package dev.slne.surf.cloud.core.client.server

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.client.netty.packet.fireAndAwaitOrThrow
import dev.slne.surf.cloud.api.client.server.CloudClientServerManager
import dev.slne.surf.cloud.api.common.config.properties.CloudProperties
import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.ConnectionResultEnum
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.api.common.server.CloudServerManager
import dev.slne.surf.cloud.api.common.server.CommonCloudServer
import dev.slne.surf.cloud.api.common.server.ProxyCloudServer
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientInformation
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundPullPlayersToGroupPacket
import dev.slne.surf.cloud.core.common.server.CommonCloudServerImpl
import dev.slne.surf.cloud.core.common.server.CommonCloudServerManagerImpl
import dev.slne.surf.surfapi.core.api.util.mutableObjectListOf
import dev.slne.surf.surfapi.core.api.util.mutableObjectSetOf
import it.unimi.dsi.fastutil.objects.ObjectList
import org.jetbrains.annotations.Unmodifiable
import kotlin.time.Duration

@AutoService(CloudServerManager::class)
class ClientCloudServerManagerImpl : CommonCloudServerManagerImpl<CommonCloudServer>(),
    CloudClientServerManager {

    fun updateServerInformationNow(serverName: String, information: ClientInformation) {
        (serverCache.getIfPresent(serverName) as? CommonCloudServerImpl)?.information = information
    }

    override fun current(): CommonCloudServer {
        return retrieveServerByName(CloudProperties.SERVER_NAME)
            ?: throw AssertionError("Current server not found")
    }

    override fun currentServer(): CloudServer {
        return current() as? CloudServer
            ?: error("Current server is not a CloudServer. Ensure you are calling this method on the correct platform.")
    }

    override fun currentProxy(): ProxyCloudServer {
        return current() as? ProxyCloudServer
            ?: error("Current server is not a ProxyCloudServer. Ensure you are calling this method on the correct platform.")
    }

    override suspend fun pullPlayersToGroup(
        group: String,
        players: Iterable<CloudPlayer>
    ): @Unmodifiable ObjectList<Pair<CloudPlayer, ConnectionResultEnum>> {
        val uuids = players.mapTo(mutableObjectSetOf()) { it.uuid }
        val resultOrException = ServerboundPullPlayersToGroupPacket(group, uuids)
            .fireAndAwaitOrThrow(Duration.INFINITE) // we will always get a response or something went terribly wrong
            .result

        return resultOrException.map({ result ->
            result.mapNotNullTo(mutableObjectListOf()) { (uuid, result) ->
                CloudPlayer[uuid]?.let { it to result }
            }
        }, { errorMessage ->
            throw IllegalStateException("Failed to pull players to group '$group': $errorMessage")
        })
    }
}

val serverManagerImpl get() = CloudServerManager.instance as ClientCloudServerManagerImpl