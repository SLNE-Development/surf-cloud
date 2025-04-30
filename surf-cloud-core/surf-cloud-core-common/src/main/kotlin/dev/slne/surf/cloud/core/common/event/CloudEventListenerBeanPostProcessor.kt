package dev.slne.surf.cloud.core.common.event

import dev.slne.surf.cloud.api.common.event.CloudEventHandler
import dev.slne.surf.cloud.api.common.util.isAnnotated
import dev.slne.surf.cloud.api.common.util.isCandidateFor
import dev.slne.surf.cloud.api.common.util.mutableObject2ObjectMapOf
import dev.slne.surf.cloud.api.common.util.selectFunctions
import dev.slne.surf.cloud.api.common.util.ultimateTargetClass
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.SmartLifecycle
import org.springframework.stereotype.Component
import java.lang.reflect.Method

@Component
class CloudEventListenerBeanPostProcessor : BeanPostProcessor, SmartLifecycle {

    private val watched = mutableObject2ObjectMapOf<Any, MutableSet<Method>>()
    private var running = false

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        val targetClass = bean.ultimateTargetClass()
        if (targetClass.isCandidateFor<CloudEventHandler>()) {
            val listenerMethods =
                targetClass.selectFunctions { it.isAnnotated<CloudEventHandler>() }

            if (listenerMethods.isNotEmpty()) {
                watched.computeIfAbsent(bean) { mutableSetOf() }.addAll(listenerMethods)
                if (running) {
                    cloudEventBusImpl.register(bean, listenerMethods)
                }
            }
        }

        return bean
    }

    override fun start() {
        watched.forEach { (bean, methods) ->
            cloudEventBusImpl.register(bean, methods)
        }
        running = true
    }

    override fun stop() {
        running = false
        watched.forEach { (bean, _) ->
            cloudEventBusImpl.unregister(bean)
        }
    }

    override fun isRunning(): Boolean {
        return running
    }
}