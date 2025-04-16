package dev.slne.surf.cloud.standalone

import dev.slne.surf.cloud.core.common.CloudCoreInstance
import dev.slne.surf.cloud.core.common.CloudCoreInstance.BootstrapData
import dev.slne.surf.cloud.core.common.coreCloudInstance
import dev.slne.surf.cloud.core.common.handleEventuallyFatalError
import dev.slne.surf.surfapi.core.api.util.logger
import dev.slne.surf.surfapi.standalone.SurfApiStandaloneBootstrap
import kotlinx.coroutines.runBlocking
import org.springframework.boot.SpringApplication
import kotlin.io.path.Path
import kotlin.system.exitProcess

object Bootstrap {
    val log = logger()

    @JvmStatic
    fun main(args: Array<String>): Unit = runBlocking {
        try {
            log.atInfo()
                .log("Classloader: " + Bootstrap::class.java.classLoader)

            SurfApiStandaloneBootstrap.bootstrap()
            SurfApiStandaloneBootstrap.enable()

            coreCloudInstance.bootstrap(
                BootstrapData(
                    dataFolder = Path("")
                )
            )
            coreCloudInstance.onLoad()
            coreCloudInstance.onEnable()
            coreCloudInstance.afterStart()
            log.atInfo()
                .log("Standalone instance is ready!")
        } catch (e: Throwable) {
            e.handleEventuallyFatalError {
                shutdown(it)
            }
        }
    }

    suspend fun shutdown(exitCode: Int): Nothing {
        coreCloudInstance.onDisable()

        val context = CloudCoreInstance.internalContext
        val exitCode =
            if (context != null) SpringApplication.exit(context, { exitCode }) else exitCode
        exitProcess(exitCode)
    }
}