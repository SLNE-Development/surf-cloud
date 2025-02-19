package dev.slne.surf.cloud.core.common

import dev.slne.surf.cloud.SurfCloudMainApplication
import dev.slne.surf.cloud.api.common.CloudInstance
import dev.slne.surf.cloud.api.common.exceptions.ExitCodes
import dev.slne.surf.cloud.api.common.exceptions.FatalSurfError
import dev.slne.surf.cloud.api.common.startSpringApplication
import dev.slne.surf.cloud.api.common.util.classloader.JoinClassLoader
import dev.slne.surf.cloud.core.common.netty.NettyManager
import dev.slne.surf.cloud.core.common.player.playerManagerImpl
import dev.slne.surf.cloud.core.common.processors.NettyPacketProcessor
import dev.slne.surf.cloud.core.common.server.CommonCloudServerImpl
import dev.slne.surf.cloud.core.common.spring.SurfSpringBanner
import dev.slne.surf.cloud.core.common.spring.event.RootSpringContextInitialized
import dev.slne.surf.cloud.core.common.util.getCallerClass
import dev.slne.surf.cloud.core.common.util.tempChangeSystemClassLoader
import dev.slne.surf.surfapi.core.api.util.logger
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.jetbrains.annotations.MustBeInvokedByOverriders
import org.springframework.boot.Banner
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.NestedRuntimeException
import org.springframework.core.io.DefaultResourceLoader
import java.nio.file.Path
import javax.annotation.OverridingMethodsMustInvokeSuper
import kotlin.time.Duration.Companion.minutes

abstract class CloudCoreInstance(protected val nettyManager: NettyManager) : CloudInstance {
    protected val log = logger()

    val dataContext get() = internalContext ?: error("Data context is not initialized yet.")

    lateinit var dataFolder: Path
    protected open val springProfile = "client"

    @MustBeInvokedByOverriders
    open suspend fun bootstrap(data: BootstrapData) {
        log.atInfo().log("Bootstrapping SurfCloudCoreInstance...")
        setupDefaultUncaughtExceptionHandler()
        initBootstrapData(data)

        preBootstrap()
        withTimeout(1.minutes) {
            startSpringApplication()
            nettyManager.bootstrap()

            log.atInfo().log("SurfCloudCoreInstance bootstrapped.")
        }
    }

    open suspend fun preBootstrap() {

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
            internalContext = startSpringApplication(SurfCloudMainApplication::class) {
                listeners(NettyPacketProcessor)
            }
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

        playerManagerImpl.terminate()
        nettyManager.stop()
//        if (internalContext?.isActive == true) internalContext?.close()

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
        vararg parentClassLoader: ClassLoader,
        customizer: SpringApplicationBuilder.() -> Unit
    ): ConfigurableApplicationContext {
        val joinClassLoader = JoinClassLoader(classLoader, parentClassLoader)
        return tempChangeSystemClassLoader(joinClassLoader) {
            val builder = SpringApplicationBuilder(applicationClass)
                .resourceLoader(DefaultResourceLoader(joinClassLoader))
                .bannerMode(Banner.Mode.CONSOLE)
                .banner(SurfSpringBanner())
                .profiles(springProfile)

            if (internalContext != null) {
                builder.parent(internalContext)
            }

            customizer(builder)

            log.atInfo().log("Starting Spring application...")
            builder.run()
        }
    }

    abstract fun shutdownServer(server: CommonCloudServerImpl)


    fun isRunning(): Boolean {
        return internalContext?.isActive == true
    }

    companion object {
        @Volatile
        var internalContext: ConfigurableApplicationContext? = null
    }

    data class BootstrapData(val dataFolder: Path)
}

val coreCloudInstance: CloudCoreInstance
    get() = CloudInstance.instance as CloudCoreInstance

inline fun FatalSurfError.handle(additionalHandling: (FatalSurfError) -> Unit) {
    val log = logger()
    log.atSevere().log("A fatal error occurred: ")
    log.atSevere()
        .withCause(cause)
        .log(buildMessage())
    additionalHandling(this)
}

inline fun Throwable.handleEventuallyFatalError(
    additionalHandling: (FatalSurfError) -> Unit,
    log: Boolean = true,
    handleTimeout: Boolean = true
): Boolean {
    if (this is OutOfMemoryError) {
        throw this
    }

    if (this is FatalSurfError) {
        handle(additionalHandling)
        return true
    } else if (this is NestedRuntimeException && this.rootCause is FatalSurfError) {
        (this.rootCause as FatalSurfError).handle(additionalHandling)
        return true
    } else if (this is TimeoutCancellationException && handleTimeout) {
        val fatalError = FatalSurfError {
            simpleErrorMessage("An operation timed out")
            detailedErrorMessage("An operation timed out")
            cause(this@handleEventuallyFatalError)
            additionalInformation("Error occurred in: " + getCallerClass()?.simpleName)
            exitCode(ExitCodes.TIMEOUT)
        }
        fatalError.handle(additionalHandling)
        return true
    } else {
        if (log) {
            logger().atSevere().withCause(this).log("An unexpected error occurred")
        }
    }

    return false
}