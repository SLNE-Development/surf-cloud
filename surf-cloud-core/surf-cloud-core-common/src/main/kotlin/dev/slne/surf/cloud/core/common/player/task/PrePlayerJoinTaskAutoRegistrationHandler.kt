package dev.slne.surf.cloud.core.common.player.task

import dev.slne.surf.cloud.api.common.player.task.PrePlayerJoinTask
import dev.slne.surf.cloud.api.common.util.mutableObjectSetOf
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.SmartLifecycle
import org.springframework.stereotype.Component

@Component
class PrePlayerJoinTaskAutoRegistrationHandler(
    private val taskManager: PrePlayerJoinTaskManager
) : BeanPostProcessor, SmartLifecycle {
    private val watched = mutableObjectSetOf<PrePlayerJoinTask>()
    private var running = false

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        if (bean is PrePlayerJoinTask) {
            watched.add(bean)
            if (running) {
                taskManager.registerTask(bean)
            }
        }
        return bean
    }

    override fun start() {
        taskManager.registerTasks(watched)
        running = true
    }

    override fun stop() {
        running = false
        watched.forEach { taskManager.unregisterTask(it) }
    }

    override fun isRunning(): Boolean = running
}
