package dev.slne.surf.cloud.core.client.server

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.client.netty.packet.fireAndAwaitOrThrow
import dev.slne.surf.cloud.api.client.server.CloudClientServerManager
import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.CloudPlayerManager
import dev.slne.surf.cloud.api.common.player.ConnectionResultEnum
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.api.common.server.CloudServerManager
import dev.slne.surf.cloud.api.common.server.CommonCloudServer
import dev.slne.surf.cloud.core.common.data.CloudPersistentData
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientInformation
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundPullPlayersToGroupPacket
import dev.slne.surf.cloud.core.common.server.CommonCloudServerImpl
import dev.slne.surf.cloud.core.common.server.CommonCloudServerManagerImpl
import dev.slne.surf.surfapi.core.api.util.mutableObjectListOf
import it.unimi.dsi.fastutil.objects.ObjectList
import org.jetbrains.annotations.Unmodifiable
import kotlin.time.Duration

@AutoService(CloudServerManager::class)
class ClientCloudServerManagerImpl : CommonCloudServerManagerImpl<CommonCloudServer>(),
    CloudClientServerManager {

    fun updateServerInformationNow(uid: Long, information: ClientInformation) {
        (servers[uid] as? CommonCloudServerImpl)?.information = information
    }

    override fun currentServer(): CloudServer {
        return getServerByIdUnsafe(CloudPersistentData.SERVER_ID) as? CloudServer
            ?: throw AssertionError("Current server not found")
    }

    override suspend fun pullPlayersToGroup(
        group: String,
        players: Collection<CloudPlayer>
    ): @Unmodifiable ObjectList<Pair<CloudPlayer, ConnectionResultEnum>> {
        return ServerboundPullPlayersToGroupPacket(
            group,
            players.map { it.uuid }
        ).fireAndAwaitOrThrow(Duration.INFINITE)
            .results
            .mapNotNullTo(mutableObjectListOf()) { (uuid, result) ->
                CloudPlayerManager.getPlayer(uuid)?.let { it to result }
            }
    }
}

val serverManagerImpl get() = CloudServerManager.instance as ClientCloudServerManagerImpl