package dev.slne.surf.cloud.velocity.processor

import com.velocitypowered.api.event.Subscribe
import dev.slne.surf.cloud.api.common.util.containsMethodWithAnnotation
import dev.slne.surf.cloud.api.common.util.isCandidateFor
import dev.slne.surf.surfapi.core.api.util.mutableObjectSetOf
import dev.slne.surf.cloud.api.common.util.ultimateTargetClass
import dev.slne.surf.cloud.velocity.VelocityMain
import dev.slne.surf.cloud.velocity.plugin
import dev.slne.surf.surfapi.core.api.util.logger
import org.springframework.aop.framework.AopInfrastructureBean
import org.springframework.beans.BeansException
import org.springframework.beans.factory.BeanCreationException
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component

@Component
class VelocityListenerProcessor : BeanPostProcessor {

    @Throws(BeansException::class)
    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
        if (bean is AopInfrastructureBean) return bean

        val targetClass = bean.ultimateTargetClass()

        if (targetClass.isCandidateFor<Subscribe>() && targetClass.containsMethodWithAnnotation<Subscribe>()) {
//            registerEventHandlers(beanName, bean)
            candidates.add(bean)
        }

        return bean
    }

    private fun registerEventHandlers(beanName: String, bean: Any) {
        try {
            VelocityMain.instance.eventManager.register(VelocityMain.instance, bean)
        } catch (e: Throwable) {
            throw BeanCreationException(beanName, "Failed to register event handler methods", e)
        }
    }

    companion object {
        private val log = logger()
        private val candidates = mutableObjectSetOf<Any>()

        internal fun registerListeners() {
            for (listener in candidates) {
                try {
                    plugin.eventManager.register(plugin, listener)
                } catch (e: Throwable) {
                    log.atWarning()
                        .withCause(e)
                        .log("Failed to register listener $listener continuing with next listener")
                }
            }
        }
    }
}
