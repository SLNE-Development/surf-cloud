package dev.slne.surf.cloud.core.common.plugin.task

import dev.slne.surf.cloud.api.common.plugin.spring.task.CloudInitialSynchronizeTask
import dev.slne.surf.cloud.api.common.util.mutableObjectSetOf
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.SmartLifecycle
import org.springframework.stereotype.Component

@Component
class CloudBeforeStartTaskHandler: BeanPostProcessor, SmartLifecycle {
    private val watched = mutableObjectSetOf<CloudInitialSynchronizeTask>()
    private var running = false

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        if (bean is CloudInitialSynchronizeTask) {
            watched.add(bean)
            if (running) {
                CloudSynchronizeTaskManager.registerTask(bean)
            }
        }

        return bean
    }

    override fun start() {
        CloudSynchronizeTaskManager.registerTasks(watched)
        running = true
    }

    override fun stop() {
        running = false
        watched.forEach { CloudSynchronizeTaskManager.unregisterTask(it) }
    }

    override fun isRunning(): Boolean {
        return running
    }
}