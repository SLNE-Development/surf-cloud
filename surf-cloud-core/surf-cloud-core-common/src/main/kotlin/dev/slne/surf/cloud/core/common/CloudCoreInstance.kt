package dev.slne.surf.cloud.core.common

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.SurfCloudMainApplication
import dev.slne.surf.cloud.api.common.CloudInstance
import dev.slne.surf.cloud.api.common.exceptions.ExitCodes
import dev.slne.surf.cloud.api.common.exceptions.FatalSurfError
import dev.slne.surf.cloud.api.common.util.TimeLogger
import dev.slne.surf.cloud.api.common.util.classloader.JoinClassLoader
import dev.slne.surf.cloud.api.common.util.forEachOrdered
import dev.slne.surf.cloud.core.common.event.CloudEventListenerBeanPostProcessor
import dev.slne.surf.cloud.core.common.netty.network.EncryptionManager
import dev.slne.surf.cloud.core.common.player.punishment.CloudPlayerPunishmentManagerBridgeImpl
import dev.slne.surf.cloud.core.common.plugin.task.CloudBeforeStartTaskHandler
import dev.slne.surf.cloud.core.common.processors.NettyPacketProcessor
import dev.slne.surf.cloud.core.common.player.task.PrePlayerJoinTaskAutoRegistrationHandler
import dev.slne.surf.cloud.core.common.spring.CloudChildSpringApplicationConfiguration
import dev.slne.surf.cloud.core.common.spring.CloudLifecycleAware
import dev.slne.surf.cloud.core.common.spring.SurfSpringBanner
import dev.slne.surf.cloud.core.common.spring.event.RootSpringContextInitialized
import dev.slne.surf.cloud.core.common.util.getCallerClass
import dev.slne.surf.cloud.core.common.util.tempChangeSystemClassLoader
import dev.slne.surf.surfapi.core.api.util.checkInstantiationByServiceLoader
import dev.slne.surf.surfapi.core.api.util.logger
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.getBeanProvider
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.RootBeanDefinition
import org.springframework.boot.Banner
import org.springframework.boot.WebApplicationType
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.NestedRuntimeException
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.DefaultResourceLoader
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.*
import kotlin.time.Duration.Companion.minutes

@AutoService(CloudInstance::class)
class CloudCoreInstance : CloudInstance {
    private val log = logger()

    val dataContext get() = internalContext ?: error("Data context is not initialized yet.")

    lateinit var dataFolder: Path
    private lateinit var lifecycles: ObjectProvider<CloudLifecycleAware>

    init {
        checkInstantiationByServiceLoader()
    }

    val springProfile by lazy {
        ClassPathResource("spring.profile", javaClass.classLoader).getContentAsString(
            StandardCharsets.UTF_8
        )
    }

    suspend fun bootstrap(data: BootstrapData) {
        val timeLogger = TimeLogger("SurfCloud Bootstrap")

        timeLogger.measureStep("Setup uncaught exception handler") {
            setupDefaultUncaughtExceptionHandler()
        }

        timeLogger.measureStep("Initialize bootstrap data") {
            initBootstrapData(data)
        }

        timeLogger.measureStep("Setup encryption manager") {
            EncryptionManager.instance.init()
        }

        withTimeout(1.minutes) {
            timeLogger.measureStep("Start Spring application") {
                startSpringApplication()
            }
        }

        lifecycles.forEachOrdered { it.onBootstrap(data, timeLogger) }
        timeLogger.printSummary()
    }

    private fun setupDefaultUncaughtExceptionHandler() {
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
        try {
            internalContext = startMainSpringApplication().also { context ->
                lifecycles = context.getBeanProvider<CloudLifecycleAware>()
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

    suspend fun onLoad() {
        val timeLogger = TimeLogger("SurfCloud load")
        lifecycles.forEachOrdered { it.onLoad(timeLogger) }
        timeLogger.printSummary()
    }

    suspend fun onEnable() {
        val timeLogger = TimeLogger("SurfCloud enable")
        lifecycles.forEachOrdered { it.onEnable(timeLogger) }
        timeLogger.printSummary()
    }

    suspend fun afterStart() {
        val timeLogger = TimeLogger("SurfCloud afterStart")
        lifecycles.forEachOrdered { it.afterStart(timeLogger) }
        timeLogger.printSummary()
    }

    suspend fun onDisable() {
        val timeLogger = TimeLogger("SurfCloud disable")
        lifecycles.forEachOrdered { it.onDisable(timeLogger) }
        timeLogger.printSummary()
    }

    private fun startMainSpringApplication() = tempChangeSystemClassLoader(javaClass.classLoader) {
        SpringApplicationBuilder(SurfCloudMainApplication::class.java)
            .profiles(springProfile)
            .initializers(NettyPacketProcessor())
            .web(WebApplicationType.NONE)
            .run()
    }

    override fun startSpringApplication(
        applicationClass: Class<*>,
        classLoader: ClassLoader,
        vararg parentClassLoader: ClassLoader,
        customizer: SpringApplicationBuilder.() -> Unit
    ): ConfigurableApplicationContext {
        val joinClassLoader = JoinClassLoader(classLoader, parentClassLoader)
        return tempChangeSystemClassLoader(joinClassLoader) {
            val parentContext = internalContext ?: error("Parent context is not initialized yet.")
            val resourceLoader = DefaultResourceLoader(joinClassLoader)
            val childConfigurations =
                parentContext.getBeansOfType(CloudChildSpringApplicationConfiguration::class.java)

            val properties = Properties().apply {
                setProperty("spring.application.name", applicationClass.simpleName)
                setProperty("logging.include-application-name", "false")
                setProperty("logging.level.root", "info")

                setProperty("spring.jpa.show-sql", "false")
                setProperty(
                    "spring.jpa.properties.jakarta.persistence.sharedCache.mode",
                    "ENABLE_SELECTIVE"
                )
                setProperty("spring.jpa.properties.hibernate.generate_statistics", "true")
                setProperty("spring.jpa.properties.hibernate.cache.use_second_level_cache", "true")
                setProperty("spring.jpa.properties.hibernate.cache.use_query_cache", "true")
                setProperty("spring.jpa.properties.hibernate.cache.region.factory_class", "jcache")
                setProperty(
                    "spring.jpa.properties.hibernate.javax.cache.provider",
                    "org.ehcache.jsr107.EhcacheCachingProvider"
                )
                setProperty(
                    "spring.jpa.properties.hibernate.javax.cache.missing_cache_strategy",
                    "create"
                )

                setProperty(
                    "spring.autoconfigure.exclude",
                    childConfigurations.values.flatMap { it.excludedAutoConfiguration }
                        .toMutableList()
                        .distinct()
                        .joinToString(",") { it.canonicalName }
                )
            }

            val builder = SpringApplicationBuilder(applicationClass)
                .resourceLoader(resourceLoader)
                .bannerMode(Banner.Mode.CONSOLE)
                .banner(SurfSpringBanner())
                .properties(properties)
                .profiles(springProfile)
                .profiles("plugin")
                .web(WebApplicationType.NONE)
                .initializers({ ctx ->
                    require(ctx is BeanDefinitionRegistry)

                    ctx.registerBeanDefinition(
                        "cloudEventBpp",
                        RootBeanDefinition(CloudEventListenerBeanPostProcessor::class.java)
                    )
                    ctx.registerBeanDefinition(
                        "loginValidationAutoRegistrationHandler",
                        RootBeanDefinition(CloudPlayerPunishmentManagerBridgeImpl.LoginValidationAutoRegistrationHandler::class.java)
                    )
                    ctx.registerBeanDefinition(
                        "beforeStartTaskHandler",
                        RootBeanDefinition(CloudBeforeStartTaskHandler::class.java)
                    )
                    ctx.registerBeanDefinition(
                        "prePlayerJoinTaskAutoRegistrationHandler",
                        RootBeanDefinition(PrePlayerJoinTaskAutoRegistrationHandler::class.java)
                    )
                })


//            builder.parent(parentContext)

            childConfigurations
                .forEach { (_, config) ->
                    config.configureChildApplication(
                        builder,
                        classLoader,
                        *parentClassLoader
                    )
                }

            customizer(builder)

            log.atInfo().log("Starting Spring application...")
            builder.run()
        }
    }

    override fun <B> getBean(beanClass: Class<B>): B {
        return dataContext.getBean(beanClass)
    }

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

fun FatalSurfError.handle(): Int {
    val log = logger()
    log.atSevere().log("A fatal error occurred: ")
    log.atSevere()
        .withCause(cause)
        .log(buildMessage())
    return exitCode
}

suspend inline fun Throwable.handleEventuallyFatalError(
    log: Boolean = true,
    handleTimeout: Boolean = true,
    shutdown: (exitCode: Int) -> Unit
) {
    if (this is OutOfMemoryError) {
        throw this
    }

    val exitCode = if (this is FatalSurfError) {
        handle()
    } else if (this is NestedRuntimeException && this.rootCause is FatalSurfError) {
        (this.rootCause as FatalSurfError).handle()
    } else if (this is TimeoutCancellationException && handleTimeout) {
        val fatalError = FatalSurfError {
            simpleErrorMessage("An operation timed out")
            detailedErrorMessage("An operation timed out")
            cause(this@handleEventuallyFatalError)
            additionalInformation("Error occurred in: " + getCallerClass()?.simpleName)
            exitCode(ExitCodes.TIMEOUT)
        }
        fatalError.handle()
    } else {
        if (log) {
            logger().atSevere().withCause(this).log("An unexpected error occurred")
        }
        ExitCodes.UNKNOWN_ERROR
    }

    logger().atWarning()
        .log("Waiting for 1 minute before exiting with code $exitCode")
    delay(1.minutes)
    shutdown(exitCode)
}