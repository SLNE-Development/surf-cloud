package dev.slne.surf.cloud.core.processors

import dev.slne.surf.cloud.api.lifecycle.SurfLifecycle
import dev.slne.surf.cloud.api.util.add
import dev.slne.surf.cloud.api.util.mutableObject2MultiObjectsMapOf
import org.springframework.beans.BeansException
import org.springframework.beans.factory.config.BeanPostProcessor
import kotlin.reflect.KClass

abstract class AbstractLifecycleProcessor : BeanPostProcessor {
    private val lifecycleMap = mutableObject2MultiObjectsMapOf<KClass<*>, SurfLifecycle>()

    @Throws(BeansException::class)
    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
        if (bean !is SurfLifecycle) return bean

        val providingClass = getProvidingClass(bean)
        lifecycleMap.add(providingClass, bean)

        return bean
    }

    protected abstract fun getProvidingClass(lifecycle: SurfLifecycle): KClass<*>

    fun getLifecycles(providingClass: KClass<*>) = lifecycleMap[providingClass]
}
