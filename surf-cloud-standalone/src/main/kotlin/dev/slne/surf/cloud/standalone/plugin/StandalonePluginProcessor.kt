package dev.slne.surf.cloud.standalone.plugin

import dev.slne.surf.cloud.api.util.findAnnotation
import dev.slne.surf.cloud.api.util.logger
import dev.slne.surf.cloud.api.util.ultimateTargetClass
import org.springframework.aop.framework.AopInfrastructureBean
import org.springframework.beans.factory.BeanCreationException
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component

@Component
class StandalonePluginProcessor : BeanPostProcessor {
    private val log = logger()
    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        if (bean is AopInfrastructureBean) return bean

        val targetClass = bean.ultimateTargetClass()
        targetClass.findAnnotation<StandalonePluginMeta>() ?: return bean

        if (bean !is StandalonePlugin) {
            log.atSevere()
                .withCause(
                    BeanCreationException(
                        beanName,
                        "Bean annotated with @StandalonePluginMeta must extend StandalonePlugin"
                    )
                )
                .log("Bean annotated with @StandalonePluginMeta must extend StandalonePlugin")

            return null
        }

        try {
            bean.start()
        } catch (e: Exception) {
            log.atSevere()
                .withCause(e)
                .log("Failed to start plugin '%s'", bean.id)
            return null
        }


        StandalonePluginManager.addPlugin(bean)
        return bean
    }
}
