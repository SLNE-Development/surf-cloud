package dev.slne.surf.cloud.core.common.netty.registry.listener.processor

import dev.slne.surf.cloud.api.common.netty.exception.SurfNettyListenerRegistrationException
import dev.slne.surf.cloud.api.common.util.isAnnotated
import dev.slne.surf.cloud.api.common.util.isCandidateFor
import dev.slne.surf.cloud.api.common.util.selectFunctions
import dev.slne.surf.cloud.api.common.util.ultimateTargetClass
import dev.slne.surf.cloud.core.common.netty.registry.listener.NettyListenerRegistry
import org.springframework.aop.framework.AopInfrastructureBean
import org.springframework.beans.factory.BeanCreationException
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component
import java.lang.reflect.Method
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacketHandler as Handler

@Component
class NettyListenerRegistryProcessor : BeanPostProcessor {
    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
        if (bean !is AopInfrastructureBean) {
            val targetClass = bean.ultimateTargetClass()
            if (targetClass.isCandidateFor<Handler>()) {
                val nettyHandlers = targetClass.selectFunctions { it.isAnnotated<Handler>() }
                if (nettyHandlers.isNotEmpty()) {
                    registerNettyHandlers(beanName, bean, nettyHandlers)
                }
            }
        }

        return bean
    }

    private fun registerNettyHandlers(beanName: String, bean: Any, nettyHandlers: Set<Method>) {
        try {
            for (handler in nettyHandlers) {
                NettyListenerRegistry.registerListener(handler, bean)
            }
        } catch (e: SurfNettyListenerRegistrationException) {
            throw BeanCreationException(beanName, e.message ?: "<null>", e)
        }
    }
}
