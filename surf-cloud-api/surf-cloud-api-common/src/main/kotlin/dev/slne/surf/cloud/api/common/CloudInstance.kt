package dev.slne.surf.cloud.api.common

import dev.slne.surf.cloud.api.common.util.annotation.InternalApi
import dev.slne.surf.surfapi.core.api.util.requiredService
import org.jetbrains.annotations.ApiStatus
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ConfigurableApplicationContext
import kotlin.reflect.KClass

/**
 * Represents the main interface for Surf Cloud instance,
 * providing methods to start Spring applications
 * and access internal components.
 *
 * This interface is non-extendable.
 */
@ApiStatus.NonExtendable
interface CloudInstance {

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

    companion object : CloudInstance by INSTANCE {
        @InternalApi
        val instance = INSTANCE
    }
}

private val INSTANCE = requiredService<CloudInstance>()

/**
 * Extension function to start a Spring application using [KClass].
 *
 * @param applicationClass The main application class as a [KClass].
 * @param classLoader The class loader to use. Defaults to the class loader of [applicationClass].
 * @param parentClassLoader Additional class loaders to consider.
 * @param customizer A block to customize the [SpringApplicationBuilder].
 * @return The resulting [ConfigurableApplicationContext].
 */
fun CloudInstance.startSpringApplication(
    applicationClass: KClass<*>,
    classLoader: ClassLoader = applicationClass.java.classLoader,
    vararg parentClassLoader: ClassLoader,
    customizer: SpringApplicationBuilder.() -> Unit = {}
) = startSpringApplication(
    applicationClass.java,
    classLoader,
    *parentClassLoader,
    customizer = customizer
)