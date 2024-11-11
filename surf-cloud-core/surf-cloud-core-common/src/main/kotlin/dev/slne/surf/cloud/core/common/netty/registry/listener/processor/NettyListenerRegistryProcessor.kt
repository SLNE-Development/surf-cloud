package dev.slne.surf.cloud.core.common.netty.registry.listener.processor

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacketHandler
import dev.slne.surf.cloud.api.common.netty.exception.SurfNettyListenerRegistrationException
import dev.slne.surf.cloud.core.common.netty.registry.listener.NettyListenerRegistry
import org.springframework.aop.framework.AopInfrastructureBean
import org.springframework.aop.framework.AopProxyUtils
import org.springframework.beans.factory.BeanCreationException
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.core.MethodIntrospector
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.stereotype.Component
import java.lang.reflect.Method

@Component
class NettyListenerRegistryProcessor : BeanPostProcessor {
    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
        if (bean is AopInfrastructureBean) return bean
        val targetClass = AopProxyUtils.ultimateTargetClass(bean)

        if (!AnnotationUtils.isCandidateClass(
                targetClass,
                SurfNettyPacketHandler::class.java
            )
        ) return bean


        val nettyHandlers = MethodIntrospector.selectMethods(
            targetClass
        ) { method ->
            AnnotatedElementUtils.isAnnotated(
                method,
                SurfNettyPacketHandler::class.java
            )
        }

        if (nettyHandlers.isNotEmpty()) {
            registerNettyHandlers(beanName, bean, nettyHandlers)
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
