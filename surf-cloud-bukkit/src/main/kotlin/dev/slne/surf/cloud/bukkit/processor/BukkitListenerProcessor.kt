package dev.slne.surf.cloud.bukkit.processor

import dev.slne.surf.cloud.api.util.isAnnotated
import dev.slne.surf.cloud.api.util.isCandidateFor
import dev.slne.surf.cloud.api.util.selectFunctions
import dev.slne.surf.cloud.api.util.ultimateTargetClass
import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.EventException
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.EventExecutor
import org.bukkit.plugin.java.JavaPlugin
import org.springframework.aop.framework.AopInfrastructureBean
import org.springframework.beans.factory.BeanCreationException
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.stereotype.Component
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

@Component
class BukkitListenerProcessor : BeanPostProcessor {
    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
        if (bean is AopInfrastructureBean) return bean

        val targetClass = bean.ultimateTargetClass()
        if (!targetClass.isCandidateFor<EventHandler>()) return bean

        val eventHandlers = targetClass.selectFunctions { it.isAnnotated<EventHandler>() }

        if (eventHandlers.isNotEmpty()) {
            registerEventHandlers(beanName, bean, eventHandlers)
        }

        return bean
    }

    private fun registerEventHandlers(
        beanName: String,
        bean: Any,
        eventHandlerMethods: Set<Method>
    ) {
        for (handlerMethod in eventHandlerMethods) {
            val params = handlerMethod.parameterTypes

            if (params.size != 1) {
                throw BeanCreationException(
                    beanName,
                    "Event handler method must have exactly one parameter"
                )
            }

            val eventParam = params[0]
            if (!eventParam.isAssignableFrom(Event::class.java)) {
                throw BeanCreationException(
                    beanName,
                    "Event handler method parameter must be a subclass of Event"
                )
            }

            val eventClass = eventParam.asSubclass(Event::class.java)
            val eventHandler = checkNotNull(
                AnnotationUtils.getAnnotation(
                    handlerMethod,
                    EventHandler::class.java
                )
            ) { "This method should only be called for event handlers" }
            val eventExecutor = EventExecutor { _, event ->
                try {
                    if (eventParam.isInstance(event)) {
                        handlerMethod.invoke(bean, event)
                    }
                } catch (e: InvocationTargetException) {
                    throw EventException(e, "Error invoking event handler")
                } catch (e: IllegalAccessException) {
                    throw EventException(e, "Error invoking event handler")
                }
            }
            registerEventHandler(bean, eventClass, eventHandler, eventExecutor)
        }
    }

    private fun registerEventHandler(
        bean: Any,
        event: Class<out Event>,
        eventHandler: EventHandler,
        eventExecutor: EventExecutor
    ) {
        val priority = eventHandler.priority
        val ignoreCancelled = eventHandler.ignoreCancelled

        Bukkit.getPluginManager().registerEvent(
            event,
            object : Listener {
            },
            priority,
            eventExecutor,
            getPluginFromBean(bean),
            ignoreCancelled
        )
    }

    private fun getPluginFromBean(bean: Any): JavaPlugin {
        return JavaPlugin.getProvidingPlugin(bean.javaClass)
    }
}
