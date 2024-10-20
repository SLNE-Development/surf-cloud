package dev.slne.surf.cloud.core

import dev.slne.surf.cloud.SurfCloudMainApplication
import dev.slne.surf.cloud.api.SurfCloudInstance
import dev.slne.surf.cloud.api.cloudInstance
import dev.slne.surf.cloud.api.exceptions.ExitCodes
import dev.slne.surf.cloud.api.exceptions.FatalSurfError
import dev.slne.surf.cloud.api.util.JoinClassLoader
import dev.slne.surf.cloud.api.util.logger
import dev.slne.surf.cloud.core.spring.SurfSpringBanner
import dev.slne.surf.cloud.core.util.getCallerClass
import dev.slne.surf.cloud.core.util.tempChangeSystemClassLoader
import org.springframework.boot.Banner
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.NestedRuntimeException
import org.springframework.core.io.DefaultResourceLoader
import java.nio.file.Path
import javax.annotation.OverridingMethodsMustInvokeSuper
import kotlin.concurrent.Volatile

abstract class SurfCloudCoreInstance : SurfCloudInstance {
    protected val log = logger()

    @Volatile
    private var _dataContext: ConfigurableApplicationContext? = null
    val dataContext: ConfigurableApplicationContext
        get() = _dataContext ?: throw IllegalStateException("Data context is not initialized yet.")

    abstract val dataFolder: Path
    protected open val springProfile = "client"

    init {
        check(getCallerClass()?.name?.startsWith("java.util.ServiceLoader") == false) { "Cannot instantiate instance directly" }
    }

    @OverridingMethodsMustInvokeSuper
    open fun onLoad() {
        Thread.setDefaultUncaughtExceptionHandler { thread, e ->
            log.atSevere()
                .withCause(e)
                .log(
                    """
                    An uncaught exception occurred in thread %s
                    Exception type: %s
                    Exception message: %s
                    """.trimIndent(),
                    thread.name, e.javaClass.name, e.message
                )
        }

        try {
            _dataContext = startSpringApplication(SurfCloudMainApplication::class.java)
        } catch (e: Throwable) {
            if (e is FatalSurfError) {
                // Re-throw FatalSurfError immediately
                throw e
            } else if (e is NestedRuntimeException && e.rootCause is FatalSurfError) {
                // Re-throw FatalSurfError if it is wrapped inside NestedRuntimeException
                throw e.rootCause as FatalSurfError
            } else {
                // Build and throw a new FatalSurfError for any other unexpected errors
                throw FatalSurfError {
                    simpleErrorMessage("An unexpected error occurred during the onLoad process.")
                    detailedErrorMessage("An error occurred while starting the Spring application during the onLoad phase.")
                    cause(e)
                    additionalInformation("Error occurred in: " + javaClass.name)
                    additionalInformation("Root cause: ${e.cause?.message ?: "Unknown"}")
                    additionalInformation("Exception type: " + e.javaClass.name)
                    possibleSolution("Check the logs for more detailed error information.")
                    possibleSolution("Ensure that the application configurations are correct.")
                    possibleSolution(
                        "Make sure that all dependencies are correctly initialized before loading."
                    )
                    exitCode(ExitCodes.UNKNOWN_ERROR)
                }
            }
        }
    }

    @OverridingMethodsMustInvokeSuper
    open fun onEnable() {
    }

    @OverridingMethodsMustInvokeSuper
    open fun onDisable() {
        if (_dataContext?.isActive == true) _dataContext?.close()
    }

    override fun startSpringApplication(
        applicationClass: Class<*>,
        classLoader: ClassLoader,
        vararg parentClassLoader: ClassLoader
    ): ConfigurableApplicationContext {
        val joinClassLoader = JoinClassLoader(classLoader, parentClassLoader)
        return tempChangeSystemClassLoader(joinClassLoader) {
            val builder = SpringApplicationBuilder(applicationClass)
                .resourceLoader(DefaultResourceLoader(joinClassLoader))
                .bannerMode(Banner.Mode.CONSOLE)
                .banner(SurfSpringBanner())
                .profiles(springProfile)
                .listeners()

            if (_dataContext != null) {
                builder.parent(_dataContext)
            }

            builder.run()
        }
    }

    companion object {
        @JvmStatic
        fun get() = SurfCloudInstance.get() as SurfCloudCoreInstance
    }
}

val coreCloudInstance: SurfCloudCoreInstance
    get() = cloudInstance as SurfCloudCoreInstance
