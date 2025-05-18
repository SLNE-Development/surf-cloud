package dev.slne.surf.cloud.standalone.server.queue

import dev.slne.surf.cloud.api.common.event.CloudEventHandler
import dev.slne.surf.cloud.api.common.event.player.connection.CloudPlayerConnectToNetworkEvent
import dev.slne.surf.cloud.api.common.event.player.connection.CloudPlayerDisconnectFromNetworkEvent
import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.ConnectionResultEnum
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.api.server.queue.QueueManager
import dev.slne.surf.cloud.core.common.coroutines.QueueConnectionScope
import dev.slne.surf.cloud.core.common.messages.MessageManager
import dev.slne.surf.cloud.core.common.permission.CommonCloudPermissions
import dev.slne.surf.cloud.standalone.config.standaloneConfig
import dev.slne.surf.cloud.standalone.player.StandaloneCloudPlayerImpl
import dev.slne.surf.cloud.standalone.server.StandaloneCloudServerImpl
import dev.slne.surf.cloud.standalone.server.queue.repo.QueueRepository
import dev.slne.surf.cloud.standalone.server.queue.timeout.QueueTimeoutService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import org.springframework.stereotype.Component
import java.util.*

private const val QUEUE_TIMEOUT_TIME_KEY = "queue_timeout_time_seconds"
private const val QUEUE_PRIORITY_KEY = "queue_priority"

@Component
class QueueManagerImpl(
    private val queues: QueueRepository,
    private val timeouts: QueueTimeoutService
) : QueueManager {
    @CloudEventHandler
    suspend fun onPlayerDisconnect(event: CloudPlayerDisconnectFromNetworkEvent) {
        val player = event.player
        val uuid = player.uuid
        val timeoutTime = getQueueTimoutTime(player)

        if (timeoutTime == -1L) {
            queues.dequeueEverywhere(uuid, ConnectionResultEnum.DISCONNECTED)
        } else {
            timeouts.register(uuid, timeoutTime)
        }
    }

    @CloudEventHandler
    suspend fun onPlayerConnect(event: CloudPlayerConnectToNetworkEvent) {
        timeouts.cancel(event.player.uuid)
    }

    suspend fun queueForServer(
        player: StandaloneCloudPlayerImpl,
        target: StandaloneCloudServerImpl,
        sendQueuedMessage: Boolean
    ): Deferred<ConnectionResultEnum> {
        val hasQueueBypass = player.hasPermission(CommonCloudPermissions.QUEUE_BYPASS)

        if (hasQueueBypass) {
            return QueueConnectionScope.async { player.connectToServer(target) }
        }

        val queue = queues.getServer(target.uid)
        if (queue.isQueued(player.uuid)) {
            return CompletableDeferred(ConnectionResultEnum.ALREADY_QUEUED(target.name))
        }

        if (!standaloneConfig.queue.multiQueue) {
            queues.all().find { it.isQueued(player.uuid) }?.let { other ->
                other.dequeue(
                    player.uuid,
                    ConnectionResultEnum.QUEUE_SWAPED(other.getQueueName(), target.name)
                )
//                player.sendMessage(MessageManager.Queue.getQueueSwap(other.getQueueName(), target.name))
            }
        }

        if (queue.suspended && !standaloneConfig.queue.allowJoiningSuspendedQueue) {
            return CompletableDeferred(ConnectionResultEnum.QUEUE_SUSPENDED(target.name))
        }

        val playerPriority = QueueConnectionScope.async { getQueuePriority(player) }
        val bypassFull =
            QueueConnectionScope.async { player.hasPermission(CommonCloudPermissions.QUEUE_BYPASS_FULL) }

        val queueResult = queue.queue(
            player.uuid,
            playerPriority.await(),
            bypassFull.await(),
            bypassQueue = false
        )

        if (sendQueuedMessage) {
            player.sendMessage(MessageManager.Queue.getQueued(target.name))
        }

        return queueResult
    }

    suspend fun queueForGroup(
        player: StandaloneCloudPlayerImpl,
        target: String,
        sendQueuedMessage: Boolean
    ): Deferred<ConnectionResultEnum> {
        val hasQueueBypass = player.hasPermission(CommonCloudPermissions.QUEUE_BYPASS)

        if (hasQueueBypass) {
            return QueueConnectionScope.async { player.connectToServer(target) }
        }

        val queue = queues.getGroup(target)
        if (queue.isQueued(player.uuid)) {
            return CompletableDeferred(ConnectionResultEnum.ALREADY_QUEUED(target))
        }

        if (!standaloneConfig.queue.multiQueue) {
            queues.all().find { it.isQueued(player.uuid) }?.let { other ->
                other.dequeue(
                    player.uuid,
                    ConnectionResultEnum.QUEUE_SWAPED(other.getQueueName(), target)
                )
//                player.sendMessage(MessageManager.Queue.getQueueSwap(other.getQueueName(), target.name))
            }
        }

        if (queue.suspended && !standaloneConfig.queue.allowJoiningSuspendedQueue) {
            return CompletableDeferred(ConnectionResultEnum.QUEUE_SUSPENDED(target))
        }

        val playerPriority = QueueConnectionScope.async { getQueuePriority(player) }
        val bypassFull =
            QueueConnectionScope.async { player.hasPermission(CommonCloudPermissions.QUEUE_BYPASS_FULL) }

        val queueResult = queue.queue(
            player.uuid,
            playerPriority.await(),
            bypassFull.await(),
            bypassQueue = false
        )

        if (sendQueuedMessage) {
            player.sendMessage(MessageManager.Queue.getQueued(target))
        }

        return queueResult
    }

    override suspend fun getQueueTimoutTime(player: CloudPlayer) =
        player.getLuckpermsMetaData(QUEUE_TIMEOUT_TIME_KEY)?.toLongOrNull() ?: -1

    override suspend fun getQueuePriority(player: CloudPlayer) =
        player.getLuckpermsMetaData(QUEUE_PRIORITY_KEY)?.toIntOrNull() ?: 0

    override fun getServerQueue(server: CloudServer) {
        queues.getServer(server.uid)
    }

    override fun getGroupQueue(group: String) {
        queues.getGroup(group)
    }

    override suspend fun dequeueEverywhere(
        uuid: UUID,
        result: ConnectionResultEnum
    ) {
        queues.dequeueEverywhere(uuid, result)
    }
}