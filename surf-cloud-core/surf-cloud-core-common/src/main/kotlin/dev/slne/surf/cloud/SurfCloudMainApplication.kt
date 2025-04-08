package dev.slne.surf.cloud

import dev.slne.surf.cloud.api.common.SurfCloudApplication
import dev.slne.surf.cloud.api.common.exceptions.FatalSurfError
import dev.slne.surf.surfapi.core.api.util.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import org.apache.commons.lang3.ArrayUtils
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.AutoConfigurationPackage
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.core.NestedRuntimeException
import org.springframework.scheduling.annotation.AsyncConfigurer
import java.lang.reflect.Method

@SurfCloudApplication
@AutoConfigurationPackage
class SurfCloudMainApplication : AsyncConfigurer, ApplicationContextAware {
    private val log = logger()
    private lateinit var applicationContext: ApplicationContext

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    override fun getAsyncExecutor() = Dispatchers.IO.asExecutor()

    override fun getAsyncUncaughtExceptionHandler() =
        AsyncUncaughtExceptionHandler { ex, method, params ->
            if (ex is FatalSurfError) {
                handleFatalSurfError(ex, method, params)
            } else if (ex is NestedRuntimeException && ex.rootCause is FatalSurfError) {
                handleFatalSurfError(ex.rootCause as FatalSurfError, method, params)
            } else {
                log.atSevere()
                    .withCause(ex)
                    .log(
                        """
                            Exception message - %s
                            Method name - %s
                            ParameterValues - %s
                            """.trimIndent(),
                        ex.message,
                        method.name,
                        ArrayUtils.toString(params)
                    )
            }
        }


    private fun handleFatalSurfError(
        fatalSurfError: FatalSurfError,
        method: Method,
        params: Array<Any>
    ) {
        log.atSevere()
            .log(
                "Fatal error occurred in method %s with parameters %s",
                method.name,
                ArrayUtils.toString(params)
            )

        log.atSevere()
            .log(fatalSurfError.buildMessage())

        SpringApplication.exit(applicationContext, fatalSurfError)
    }
}