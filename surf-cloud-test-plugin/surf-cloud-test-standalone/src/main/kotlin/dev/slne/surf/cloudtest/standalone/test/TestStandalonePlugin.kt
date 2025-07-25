package dev.slne.surf.cloudtest.standalone.test

import dev.slne.surf.cloud.api.server.plugin.KtorPlugin
import dev.slne.surf.cloud.api.server.plugin.StandalonePlugin
import dev.slne.surf.cloud.api.server.plugin.utils.bean
import dev.slne.surf.cloudtest.standalone.test.sync.SyncValueTest
import io.ktor.server.routing.*
import kotlinx.coroutines.delay
import org.apache.commons.io.FileSystemUtils
import kotlin.time.Duration.Companion.seconds

class TestStandalonePlugin : StandalonePlugin(), KtorPlugin {
    override suspend fun load() {
        repeat(20) {
            println("# Loading4554 $it")
        }
    }

    override suspend fun enable() {
        repeat(20) {
            println("# Enabling $it")
        }

        val freeSpaceKb = FileSystemUtils.freeSpaceKb("/")
        println("Free space: $freeSpaceKb KB")

        launch {
            logger.info("Hello from coroutine")
            delay(5.seconds)
            logger.info("Hello from coroutine after delay")
            delay(5.seconds)
        }

        bean<SyncValueTest>().test()
    }

    override suspend fun disable() {
        repeat(20) {
            println("# Disabling $it")
        }
    }

    override fun Routing.installRoutes() {
    }
}

val plugin get() = StandalonePlugin.getPlugin<TestStandalonePlugin>()