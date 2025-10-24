package dev.slne.surf.cloudtest.standalone.test

import dev.slne.surf.cloud.api.server.plugin.KtorPlugin
import dev.slne.surf.cloud.api.server.plugin.StandalonePlugin
import dev.slne.surf.cloud.api.server.plugin.utils.bean
import dev.slne.surf.cloudtest.standalone.test.redis.TestRedisEvent
import dev.slne.surf.cloudtest.standalone.test.sync.SyncValueTest
import io.ktor.server.routing.*

class TestStandalonePlugin : StandalonePlugin(), KtorPlugin {
    override suspend fun load() {
    }

    override suspend fun enable() {
        TestRedisEvent.createSample().publish()
        bean<SyncValueTest>().test()
    }

    override suspend fun disable() {

    }

    override fun Routing.installRoutes() {
    }
}

val plugin get() = StandalonePlugin.getPlugin<TestStandalonePlugin>()