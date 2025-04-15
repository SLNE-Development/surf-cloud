package dev.slne.surf.cloud.standalone

import dev.slne.surf.cloud.core.common.CloudCoreInstance.BootstrapData
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

            standaloneCloudInstance.bootstrap(
                BootstrapData(
                    dataFolder = Path("")
                )
            )
            standaloneCloudInstance.onLoad()
            standaloneCloudInstance.onEnable()
        } catch (e: Throwable) {
            e.handleEventuallyFatalError {
                val context = standaloneCloudInstance.dataContext
                if (context.isActive) {
                    SpringApplication.exit(context, it)
                } else {
                    exitProcess(it.exitCode)
                }
            }
        }
    }
}