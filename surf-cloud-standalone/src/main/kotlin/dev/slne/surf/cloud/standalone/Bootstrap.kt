package dev.slne.surf.cloud.standalone

import dev.slne.surf.cloud.api.common.exceptions.FatalSurfError
import dev.slne.surf.cloud.standalone.spring.config.logback.CloudLogbackConfigurator
import dev.slne.surf.surfapi.standalone.SurfApiStandaloneBootstrap
import org.springframework.boot.SpringApplication
import org.springframework.core.NestedRuntimeException
import kotlin.concurrent.thread
import kotlin.system.exitProcess

object Bootstrap {
    @JvmStatic
    fun main(args: Array<String>) {
        try {
            System.err.println("Classloader: " + Bootstrap::class.java.classLoader)

            SurfApiStandaloneBootstrap.bootstrap()
            CloudLogbackConfigurator.configure()

            independentCloudInstance.onLoad()
            independentCloudInstance.onEnable()

            Runtime.getRuntime().addShutdownHook(thread(start = false) {
                independentCloudInstance.onDisable()
                SurfApiStandaloneBootstrap.shutdown()
            })
        } catch (e: NestedRuntimeException) {
            System.err.println("Root cause " + e.rootCause)
            if (e.rootCause is FatalSurfError) {
                handleFatalError(e.rootCause as FatalSurfError)
            } else {
                throw e
            }
        } catch (error: FatalSurfError) {
            handleFatalError(error)
        }
    }

    private fun handleFatalError(error: FatalSurfError) {
        System.err.println(error.buildMessage())
        error.cause?.printStackTrace()

        val context = independentCloudInstance.dataContext
        if (context.isActive) {
            SpringApplication.exit(context, error)
        } else {
            exitProcess(error.exitCode)
        }
    }
}