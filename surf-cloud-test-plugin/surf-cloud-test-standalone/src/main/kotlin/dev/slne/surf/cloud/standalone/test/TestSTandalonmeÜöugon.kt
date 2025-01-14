package dev.slne.surf.cloud.standalone.test

import dev.slne.surf.cloud.api.server.server.plugin.StandalonePlugin
import org.apache.commons.io.FileSystemUtils

class TestSTandalonmeÜöugon : StandalonePlugin() {
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
    }

    override suspend fun disable() {
        repeat(20) {
            println("# Disabling $it")
        }
    }
}