package dev.slne.surf.cloud.api.common

import dev.slne.surf.cloud.api.common.util.requiredService
import org.jetbrains.annotations.ApiStatus
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ApplicationListener
import org.springframework.context.ConfigurableApplicationContext
import java.util.UUID
import kotlin.reflect.KClass

/**
 * Represents the main interface for Surf Cloud instance,
 * providing methods to start Spring applications
 * and access internal components.
 *
 * This interface is non-extendable.
 */
@ApiStatus.NonExtendable
interface SurfCloudInstance {

    /**
     * Starts a Spring application with the specified parameters.
     *
     * @param applicationClass The main application class.
     * @param classLoader The class loader to use. Defaults to the class loader of [applicationClass].
     * @param parentClassLoader Additional class loaders to consider.
     * @param customizer A block to customize the [SpringApplicationBuilder].
     * @return The resulting [ConfigurableApplicationContext].
     */
    fun startSpringApplication(
        applicationClass: Class<*>,
        classLoader: ClassLoader = applicationClass.classLoader,
        vararg parentClassLoader: ClassLoader,
        customizer: SpringApplicationBuilder.() -> Unit = {}
    ): ConfigurableApplicationContext

    /**
     * Internal listener for Netty packet processing events.
     */
    @get:ApiStatus.Internal
    @Deprecated("Not needed anymore?", level = DeprecationLevel.ERROR)
    val nettyPacketProcessorListener: ApplicationListener<*>

    companion object {
        val instance = requiredService<SurfCloudInstance>()
    }
}

/**
 * Provides a convenient property to access the singleton [SurfCloudInstance].
 */
val cloudInstance: SurfCloudInstance
    get() = SurfCloudInstance.instance

/**
 * Extension function to start a Spring application using [KClass].
 *
 * @param applicationClass The main application class as a [KClass].
 * @param classLoader The class loader to use. Defaults to the class loader of [applicationClass].
 * @param parentClassLoader Additional class loaders to consider.
 * @param customizer A block to customize the [SpringApplicationBuilder].
 * @return The resulting [ConfigurableApplicationContext].
 */
fun SurfCloudInstance.startSpringApplication(
    applicationClass: KClass<*>,
    classLoader: ClassLoader = applicationClass.java.classLoader,
    vararg parentClassLoader: ClassLoader,
    customizer: SpringApplicationBuilder.() -> Unit = {}
) = startSpringApplication(applicationClass.java, classLoader, *parentClassLoader, customizer = customizer)