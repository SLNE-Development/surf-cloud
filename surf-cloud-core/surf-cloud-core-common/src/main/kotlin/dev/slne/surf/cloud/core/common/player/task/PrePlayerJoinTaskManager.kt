package dev.slne.surf.cloud.core.common.player.task

import dev.slne.surf.cloud.api.common.player.task.PrePlayerJoinTask
import dev.slne.surf.cloud.api.common.util.mutableObjectListOf
import dev.slne.surf.cloud.core.common.player.CommonOfflineCloudPlayerImpl
import dev.slne.surf.surfapi.core.api.util.logger
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.SmartInitializingSingleton
import org.springframework.core.OrderComparator
import org.springframework.stereotype.Component
import dev.slne.surf.cloud.core.common.coroutines.PrePlayerJoinTaskScope as TaskScope

object PrePlayerJoinTaskManager {
    private val log = logger()
    private val tasks = mutableObjectListOf<PrePlayerJoinTask>()
    private var singletonsInstantiated = false

    fun registerTask(task: PrePlayerJoinTask) {
        if (!tasks.contains(task)) {
            tasks.add(task)
            maybeSort()
        }
    }

    fun registerTasks(tasks: Collection<PrePlayerJoinTask>) {
        for (task in tasks) {
            if (!this.tasks.contains(task)) {
                this.tasks.add(task)
            }
        }
        maybeSort()
    }

    fun unregisterTask(task: PrePlayerJoinTask) {
        tasks.remove(task)
    }

    private fun maybeSort() {
        if (singletonsInstantiated) {
            OrderComparator.sort(tasks)
        }
    }

    suspend fun runTasks(player: CommonOfflineCloudPlayerImpl) = withContext(TaskScope.context) {
        for (task in tasks) {
            val result = runCatching { task.preJoin(player) }
                .getOrElse { e ->
                    log.atSevere()
                        .withCause(e)
                        .log("Failed to run pre player join task: ${task::class.simpleName} for player: ${player.uuid}")
                    PrePlayerJoinTask.Result.ERROR
                }

            if (result !is PrePlayerJoinTask.Result.ALLOWED) {
                return@withContext result
            }
        }
        PrePlayerJoinTask.Result.ALLOWED
    }

    @Component
    class Lifecycle : SmartInitializingSingleton {
        override fun afterSingletonsInstantiated() {
            singletonsInstantiated = true
            maybeSort()
        }
    }
}
