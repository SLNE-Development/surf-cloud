package dev.slne.surf.cloud.core.common.plugin.task

import dev.slne.surf.cloud.api.common.netty.NettyClient
import dev.slne.surf.cloud.api.common.plugin.spring.task.CloudInitialSynchronizeTask
import dev.slne.surf.cloud.api.common.util.mutableObjectListOf
import dev.slne.surf.cloud.api.common.util.synchronize
import dev.slne.surf.cloud.core.common.coroutines.BeforeStartTaskScope
import dev.slne.surf.surfapi.core.api.util.freeze
import dev.slne.surf.surfapi.core.api.util.logger
import kotlinx.coroutines.launch
import org.springframework.core.OrderComparator
import kotlin.system.measureTimeMillis

object CloudSynchronizeTaskManager {
    private val log = logger()
    private val _tasks = mutableObjectListOf<CloudInitialSynchronizeTask>().synchronize()
    val tasks = _tasks.freeze()

    fun registerTask(task: CloudInitialSynchronizeTask) {
        if (_tasks.contains(task)) return
        _tasks.add(task)
        OrderComparator.sort(_tasks)
    }

    fun registerTasks(tasks: Collection<CloudInitialSynchronizeTask>) {
        for (task in tasks) {
            if (!this._tasks.contains(task)) {
                this._tasks.add(task)
            }
        }
        OrderComparator.sort(this._tasks)
    }

    fun unregisterTask(task: CloudInitialSynchronizeTask) {
        _tasks.remove(task)
    }

    suspend fun executeTasks(client: NettyClient) {
        for ((position, task) in _tasks.withIndex()) {
            log.atInfo()
                .log("Executing initial synchronize task: ${task.name} (${position + 1}/${_tasks.size})")

            val duration = measureTimeMillis {
                BeforeStartTaskScope.launch(BeforeStartTaskScope.TaskName(task.name, position)) {
                    task.execute(client)
                }.join()
            }

            log.atInfo()
                .log("Task ${task.name} executed in ${duration}ms")
        }
    }
}