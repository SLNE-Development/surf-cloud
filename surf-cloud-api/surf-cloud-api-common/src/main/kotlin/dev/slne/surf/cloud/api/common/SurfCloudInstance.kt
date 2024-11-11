package dev.slne.surf.cloud.api.common

import net.kyori.adventure.util.Services
import org.jetbrains.annotations.ApiStatus
import org.springframework.context.ConfigurableApplicationContext
import kotlin.reflect.KClass

@ApiStatus.NonExtendable
interface SurfCloudInstance {
    fun startSpringApplication(applicationClass: Class<*>) =
        startSpringApplication(applicationClass, applicationClass.classLoader)

    fun startSpringApplication(
        applicationClass: Class<*>,
        classLoader: ClassLoader,
        vararg parentClassLoader: ClassLoader
    ): ConfigurableApplicationContext

    @get:ApiStatus.Internal
    val classLoader: ClassLoader

    companion object {
        private val INSTANCE = Services.service(SurfCloudInstance::class.java)
            .orElseThrow { Error("SurfCloudInstance not available") }

        @JvmStatic
        fun get(): SurfCloudInstance = INSTANCE
    }
}

val cloudInstance: SurfCloudInstance
    get() = SurfCloudInstance.get()

fun SurfCloudInstance.startSpringApplication(
    applicationClass: KClass<*>,
    classLoader: ClassLoader = applicationClass.java.classLoader
) = startSpringApplication(applicationClass.java, classLoader)