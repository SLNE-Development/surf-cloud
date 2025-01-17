package dev.slne.surf.cloud.standalone.test

import dev.slne.surf.cloud.api.server.plugin.StandalonePlugin
import kotlinx.coroutines.delay
import org.apache.commons.io.FileSystemUtils
import kotlin.time.Duration.Companion.seconds

class TestStandalonePlugin : StandalonePlugin() {
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
            throw RuntimeException("Test exception")
        }
    }

    override suspend fun disable() {
        repeat(20) {
            println("# Disabling $it")
        }
    }
}

val plugin get() = StandalonePlugin.getPlugin<TestStandalonePlugin>()