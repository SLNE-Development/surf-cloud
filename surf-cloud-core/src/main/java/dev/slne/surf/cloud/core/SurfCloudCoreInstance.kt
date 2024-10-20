package dev.slne.surf.cloud.core

import com.google.common.base.Preconditions
import dev.slne.surf.cloud.SurfCloudMainApplication
import dev.slne.surf.cloud.api.SurfCloudInstance
import dev.slne.surf.cloud.api.cloudInstance
import dev.slne.surf.cloud.api.exceptions.FatalSurfError
import dev.slne.surf.cloud.api.exceptions.FatalSurfError.ExitCodes
import dev.slne.surf.cloud.api.util.JoinClassLoader
import dev.slne.surf.cloud.core.spring.SurfSpringBanner
import dev.slne.surf.cloud.core.util.Util
import lombok.Getter
import lombok.extern.flogger.Flogger
import org.apache.commons.lang3.ArrayUtils
import org.springframework.boot.Banner
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.NestedRuntimeException
import org.springframework.core.io.DefaultResourceLoader
import java.nio.file.Path
import java.util.function.Supplier
import javax.annotation.OverridingMethodsMustInvokeSuper
import javax.annotation.ParametersAreNonnullByDefault
import kotlin.concurrent.Volatile

@Getter
@ParametersAreNonnullByDefault
@Flogger
abstract class SurfCloudCoreInstance : SurfCloudInstance {
    @Volatile
    lateinit var dataContext: ConfigurableApplicationContext
        private set

    init {
        val caller = Util.getCallerClass()
        if (!caller.name.startsWith("java.util.ServiceLoader")) {
            throw IllegalAccessException("Cannot instantiate instance directly")
        }
    }

    @OverridingMethodsMustInvokeSuper
    fun onLoad() {
        Thread.setDefaultUncaughtExceptionHandler { t: Thread, e: Throwable ->
            SurfCloudCoreInstance.log.atSevere()
                .withCause(e)
                .log(
                    """
              An uncaught exception occurred in thread %s
              Exception type: %s
              Exception message: %s
              
              """.trimIndent(),
                    t.name, e.javaClass.name, e.message
                )
        }

        try {
            dataContext = startSpringApplication(SurfCloudMainApplication::class.java)
        } catch (e: Throwable) {
            if (e is FatalSurfError) {
                // Re-throw FatalSurfError directly
                throw e
            } else if (e is NestedRuntimeException
                && e.getRootCause() is FatalSurfError
            ) {
                // Re-throw FatalSurfError if it is wrapped inside NestedRuntimeException
                throw fatal
            } else {
                // Build and throw a new FatalSurfError for any other unexpected errors
                throw FatalSurfError.builder()
                    .simpleErrorMessage("An unexpected error occurred during the onLoad process.")
                    .detailedErrorMessage(
                        "An error occurred while starting the Spring application during the onLoad phase."
                    )
                    .cause(e)
                    .additionalInformation("Error occurred in: " + javaClass.name)
                    .additionalInformation(
                        "Root cause: " + (if (e.cause != null) e.cause!!.message else "Unknown")
                    )
                    .additionalInformation("Exception type: " + e.javaClass.name)
                    .possibleSolution("Check the logs for more detailed error information.")
                    .possibleSolution("Ensure that the application configurations are correct.")
                    .possibleSolution(
                        "Make sure that all dependencies are correctly initialized before loading."
                    )
                    .exitCode(ExitCodes.UNKNOWN_ERROR)
                    .build()
            }
        }
    }

    @OverridingMethodsMustInvokeSuper
    open fun onEnable() {
    }

    @OverridingMethodsMustInvokeSuper
    fun onDisable() {
        if (dataContext != null && dataContext.isActive()) {
            dataContext.close()
        }
    }

    override fun startSpringApplication(
        applicationClass: Class<*>?,
        classLoader: ClassLoader?,
        vararg parentClassLoader: ClassLoader
    ): ConfigurableApplicationContext {
        Preconditions.checkNotNull(applicationClass, "applicationClass")
        Preconditions.checkNotNull(classLoader, "classLoader")
        Preconditions.checkNotNull<Array<ClassLoader>>(parentClassLoader, "parentClassLoader")

        val joinClassLoader: JoinClassLoader = JoinClassLoader(
            classLoader,
            ArrayUtils.addFirst<ClassLoader?>(parentClassLoader, classLoader)
        )

        val run: Supplier<ConfigurableApplicationContext> =
            Supplier<ConfigurableApplicationContext> {
                val builder: SpringApplicationBuilder = SpringApplicationBuilder(applicationClass)
                    .resourceLoader(DefaultResourceLoader(joinClassLoader))
                    .bannerMode(Banner.Mode.CONSOLE)
                    .banner(SurfSpringBanner())
                    .profiles(springProfile)
                    .listeners()
                if (dataContext != null) {
                    builder.parent(dataContext)
                }
                builder.run()
            }

        return Util.tempChangeSystemClassLoader(joinClassLoader, run)
    }

    abstract val dataFolder: Path

    protected open val springProfile: String?
        get() = "client"

    companion object {
        @JvmStatic
        fun get(): SurfCloudCoreInstance {
            return SurfCloudInstance.get()
        }
    }
}

val coreCloudInstance: SurfCloudCoreInstance
    get() = cloudInstance as SurfCloudCoreInstance
