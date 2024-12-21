package dev.slne.surf.cloud.core.common

import dev.slne.surf.cloud.SurfCloudMainApplication
import dev.slne.surf.cloud.api.common.SurfCloudInstance
import dev.slne.surf.cloud.api.common.cloudInstance
import dev.slne.surf.cloud.api.common.exceptions.ExitCodes
import dev.slne.surf.cloud.api.common.exceptions.FatalSurfError
import dev.slne.surf.cloud.api.common.startSpringApplication
import dev.slne.surf.cloud.api.common.util.JoinClassLoader
import dev.slne.surf.cloud.api.common.util.logger
import dev.slne.surf.cloud.core.common.netty.NettyManager
import dev.slne.surf.cloud.core.common.processors.NettyPacketProcessor
import dev.slne.surf.cloud.core.common.spring.SurfSpringBanner
import dev.slne.surf.cloud.core.common.spring.event.RootSpringContextInitialized
import dev.slne.surf.cloud.core.common.util.checkInstantiationByServiceLoader
import dev.slne.surf.cloud.core.common.util.tempChangeSystemClassLoader
import org.jetbrains.annotations.MustBeInvokedByOverriders
import org.springframework.boot.Banner
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.NestedRuntimeException
import org.springframework.core.io.DefaultResourceLoader
import java.nio.file.Path
import javax.annotation.OverridingMethodsMustInvokeSuper

abstract class SurfCloudCoreInstance(private val nettyManager: NettyManager) : SurfCloudInstance {
    protected val log = logger()

    val dataContext get() = internalContext ?: error("Data context is not initialized yet.")

    lateinit var dataFolder: Path
    protected open val springProfile = "client"

    @MustBeInvokedByOverriders
    open suspend fun bootstrap(data: BootstrapData) {
        log.atInfo().log("Bootstrapping SurfCloudCoreInstance...")

        setupDefaultUncaughtExceptionHandler()
        initBootstrapData(data)
        startSpringApplication()
        nettyManager.bootstrap()

        log.atInfo().log("SurfCloudCoreInstance bootstrapped.")
    }

    private fun setupDefaultUncaughtExceptionHandler() {
        log.atInfo().log("Setting up default uncaught exception handler...")
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
    }

    private fun initBootstrapData(data: BootstrapData) {
        dataFolder = data.dataFolder
    }

    private fun startSpringApplication() {
        log.atInfo().log("Starting Spring application...")
        try {
            internalContext = startSpringApplication(SurfCloudMainApplication::class)
        } catch (e: Throwable) {
            if (e is FatalSurfError || e is OutOfMemoryError) {
                // Re-throw FatalSurfError and OutOfMemoryError immediately
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
        internalContext?.publishEvent(RootSpringContextInitialized(this))
    }

    @MustBeInvokedByOverriders
    open suspend fun onLoad() {
        log.atInfo().log("Loading SurfCloudCoreInstance...")

        nettyManager.onLoad()

        log.atInfo().log("SurfCloudCoreInstance loaded.")
    }

    @OverridingMethodsMustInvokeSuper
    open suspend fun onEnable() {
        log.atInfo().log("Enabling SurfCloudCoreInstance...")

        nettyManager.blockPlayerConnections()
        nettyManager.onEnable()

        log.atInfo().log("SurfCloudCoreInstance enabled.")
    }

    @OverridingMethodsMustInvokeSuper
    open suspend fun onDisable() {
        log.atInfo().log("Disabling SurfCloudCoreInstance...")

        nettyManager.stop()
        if (internalContext?.isActive == true) internalContext?.close()

        log.atInfo().log("SurfCloudCoreInstance disabled.")
    }

    @MustBeInvokedByOverriders
    open suspend fun afterStart() {
        log.atInfo().log("Running afterStart...")

        nettyManager.afterStart()
        nettyManager.unblockPlayerConnections()

        log.atInfo().log("afterStart completed.")
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
                .initializers(NettyPacketProcessor)

            if (internalContext != null) {
                builder.parent(internalContext)
            }

            log.atInfo().log("Starting Spring application...")
            builder.run()
        }
    }

    companion object {
        @Volatile
        var internalContext: ConfigurableApplicationContext? = null
    }

    data class BootstrapData(val dataFolder: Path)
}

val coreCloudInstance: SurfCloudCoreInstance
    get() = cloudInstance as SurfCloudCoreInstance

inline fun FatalSurfError.handle(additionalHandling: (FatalSurfError) -> Unit) {
    val log = logger()
    log.atSevere().log("A fatal error occurred: ")
    log.atSevere()
        .withCause(cause)
        .log(buildMessage())
    additionalHandling(this)
}

inline fun Throwable.handleEventuallyFatalError(additionalHandling: (FatalSurfError) -> Unit) {
    if (this is OutOfMemoryError) {
        throw this
    }

    if (this is FatalSurfError) {
        handle(additionalHandling)
    } else if (this is NestedRuntimeException && this.rootCause is FatalSurfError) {
        (this.rootCause as FatalSurfError).handle(additionalHandling)
    } else {
        logger().atSevere().withCause(this).log("An unexpected error occurred")
    }
}