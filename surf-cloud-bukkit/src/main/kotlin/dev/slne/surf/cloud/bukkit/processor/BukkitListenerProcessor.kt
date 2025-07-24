package dev.slne.surf.cloud.bukkit.processor

import dev.slne.surf.cloud.api.common.util.*
import dev.slne.surf.cloud.core.common.spring.CloudLifecycleAware
import dev.slne.surf.surfapi.core.api.util.mutableObjectListOf
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
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

@Component
@Order(CloudLifecycleAware.MISC_PRIORITY)
class BukkitListenerProcessor : BeanPostProcessor, CloudLifecycleAware {
    private val listeners = mutableObjectListOf<ListenerMetaData>()

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
            if (!Event::class.java.isAssignableFrom(eventParam)) {
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

            listeners.add(
                ListenerMetaData(
                    bean,
                    eventClass,
                    eventHandler,
                    eventExecutor
                )
            )
        }
    }

    override suspend fun onEnable(timeLogger: TimeLogger) {
        timeLogger.measureStep("Registering Bukkit listeners from Spring beans") {
            registerListeners()
        }
    }

    fun registerListeners() {
        for (listener in listeners) {
            registerEventHandler(
                listener.bean,
                listener.event,
                listener.eventHandler,
                listener.eventExecutor
            )
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

    data class ListenerMetaData(
        val bean: Any,
        val event: Class<out Event>,
        val eventHandler: EventHandler,
        val eventExecutor: EventExecutor
    )
}
