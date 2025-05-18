package dev.slne.surf.cloud.standalone.server.queue.entry

import dev.slne.surf.cloud.api.common.player.ConnectionResultEnum
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import java.util.*

data class PlayerQueueHandle(val uuid: UUID) {
    private val deferred = CompletableDeferred<ConnectionResultEnum>()
    val result: Deferred<ConnectionResultEnum> get() = deferred

    fun complete(result: ConnectionResultEnum) = deferred.complete(result)
    fun cancel() = complete(ConnectionResultEnum.CONNECTION_CANCELLED)
}