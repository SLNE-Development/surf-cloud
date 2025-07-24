package dev.slne.surf.cloud.core.common.player.task

import dev.slne.surf.cloud.api.common.player.task.PrePlayerJoinTask
import dev.slne.surf.surfapi.core.api.util.mutableObjectSetOf
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.SmartLifecycle
import org.springframework.stereotype.Component

@Component
class PrePlayerJoinTaskAutoRegistrationHandler : BeanPostProcessor, SmartLifecycle {
    private val watched = mutableObjectSetOf<PrePlayerJoinTask>()
    private var running = false

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        if (bean is PrePlayerJoinTask) {
            if (watched.add(bean) && running) {
                PrePlayerJoinTaskManager.registerTask(bean)
            }
        }
        return bean
    }

    override fun start() {
        PrePlayerJoinTaskManager.registerTasks(watched)
        running = true
    }

    override fun stop() {
        watched.forEach { PrePlayerJoinTaskManager.unregisterTask(it) }
        watched.clear()
        running = false
    }

    override fun isRunning() = running
}