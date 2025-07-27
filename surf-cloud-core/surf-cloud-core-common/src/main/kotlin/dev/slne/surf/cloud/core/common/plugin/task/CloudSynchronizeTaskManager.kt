package dev.slne.surf.cloud.core.common.plugin.task

import dev.slne.surf.cloud.api.common.netty.NettyClient
import dev.slne.surf.cloud.api.common.plugin.spring.task.CloudInitialSynchronizeTask
import dev.slne.surf.cloud.core.common.coroutines.BeforeStartTaskScope
import dev.slne.surf.surfapi.core.api.util.logger
import kotlinx.coroutines.launch
import org.springframework.core.annotation.AnnotationAwareOrderComparator
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.time.measureTime

object CloudSynchronizeTaskManager {
    private val log = logger()
    private val tasks = CopyOnWriteArrayList<CloudInitialSynchronizeTask>()

    fun registerTask(task: CloudInitialSynchronizeTask) {
        if (tasks.addIfAbsent(task)) {
            AnnotationAwareOrderComparator.sort(tasks)
        }
    }

    fun registerTasks(tasks: Collection<CloudInitialSynchronizeTask>) {
        if (this.tasks.addAllAbsent(tasks) > 0) {
            AnnotationAwareOrderComparator.sort(this.tasks)
        }
    }

    fun unregisterTask(task: CloudInitialSynchronizeTask) {
        tasks.remove(task)
    }

    suspend fun executeTasks(client: NettyClient) {
        for ((position, task) in tasks.withIndex()) {
            log.atInfo()
                .log("Executing initial synchronize task: ${task.name} (${position + 1}/${tasks.size})")

            val duration = measureTime {
                BeforeStartTaskScope.launch(BeforeStartTaskScope.TaskName(task.name, position)) {
                    task.execute(client)
                }.join()
            }

            log.atInfo()
                .log("Task ${task.name} executed in $duration")
        }
    }
}