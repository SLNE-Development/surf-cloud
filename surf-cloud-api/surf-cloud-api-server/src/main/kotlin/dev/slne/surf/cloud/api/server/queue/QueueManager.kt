package dev.slne.surf.cloud.api.server.queue

import dev.slne.surf.cloud.api.common.CloudInstance
import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.ConnectionResultEnum
import dev.slne.surf.cloud.api.common.server.CloudServer
import org.jetbrains.annotations.ApiStatus
import java.util.*

@ApiStatus.NonExtendable
interface QueueManager {

    suspend fun getQueueTimoutTime(player: CloudPlayer): Long
    suspend fun getQueuePriority(player: CloudPlayer): Int

    fun getServerQueue(server: CloudServer)
    fun getGroupQueue(group: String)

    suspend fun dequeueEverywhere(uuid: UUID, result: ConnectionResultEnum)

    companion object : QueueManager by CloudInstance.getBean(QueueManager::class.java)
}