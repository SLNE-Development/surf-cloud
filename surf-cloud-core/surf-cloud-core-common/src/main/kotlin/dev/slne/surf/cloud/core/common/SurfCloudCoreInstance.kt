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
import dev.slne.surf.cloud.core.common.util.checkCallerClass
import dev.slne.surf.cloud.core.common.util.checkInstantiationByServiceLoader
import dev.slne.surf.cloud.core.common.util.getCallerClass
import dev.slne.surf.cloud.core.common.util.tempChangeSystemClassLoader
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.runBlocking
import org.jetbrains.annotations.MustBeInvokedByOverriders
import org.reactivestreams.Publisher
import org.springframework.boot.Banner
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.NestedRuntimeException
import org.springframework.core.io.DefaultResourceLoader
import reactor.core.publisher.Mono
import java.nio.file.Path
import java.time.Duration
import javax.annotation.OverridingMethodsMustInvokeSuper

abstract class SurfCloudCoreInstance(private val nettyManager: NettyManager) : SurfCloudInstance {
    protected val log = logger()

    val dataContext: ConfigurableApplicationContext
        get() = internalContext
            ?: throw IllegalStateException("Data context is not initialized yet.")

    abstract val dataFolder: Path
    protected open val springProfile = "client"

    init {
        checkInstantiationByServiceLoader()
    }

    @MustBeInvokedByOverriders
    open fun onLoad() {
        log.atInfo().log("Loading SurfCloudCoreInstance...")
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

        log.atInfo().log("Starting Spring application...")
        try {
            internalContext = startSpringApplication(SurfCloudMainApplication::class)
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

        log.atInfo().log("SurfCloudCoreInstance loaded.")
        internalContext?.publishEvent(RootSpringContextInitialized(this))
    }

    @OverridingMethodsMustInvokeSuper
    open fun onEnable() {
        nettyManager.blockPlayerConnections()
    }

    @OverridingMethodsMustInvokeSuper
    open fun onDisable() {
        nettyManager.stop()
        if (internalContext?.isActive == true) internalContext?.close()
    }

    open fun afterStart() = runBlocking {
        nettyManager.afterStart()
        nettyManager.unblockPlayerConnections()
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

        @JvmStatic
        fun get() = SurfCloudInstance.get() as SurfCloudCoreInstance
    }

}

val coreCloudInstance: SurfCloudCoreInstance
    get() = cloudInstance as SurfCloudCoreInstance
