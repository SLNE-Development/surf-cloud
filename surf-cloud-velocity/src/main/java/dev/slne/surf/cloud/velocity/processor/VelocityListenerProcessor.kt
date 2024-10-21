package dev.slne.surf.cloud.velocity.processor

import com.velocitypowered.api.event.Subscribe
import dev.slne.surf.cloud.api.util.isCandidateFor
import dev.slne.surf.cloud.api.util.ultimateTargetClass
import dev.slne.surf.cloud.velocity.VelocityMain
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

        if (targetClass.isCandidateFor<Subscribe>()) {
            registerEventHandlers(beanName, bean)
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
}
