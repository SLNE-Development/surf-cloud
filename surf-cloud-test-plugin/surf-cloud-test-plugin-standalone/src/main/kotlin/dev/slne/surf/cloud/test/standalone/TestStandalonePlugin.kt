package dev.slne.surf.cloud.test.standalone

import dev.slne.surf.cloud.standalone.plugin.StandalonePlugin
import dev.slne.surf.cloud.standalone.plugin.StandalonePluginConfiguration
import org.pf4j.PluginWrapper
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.AnnotationConfigApplicationContext

class TestStandalonePlugin(wrapper: PluginWrapper) : StandalonePlugin(wrapper) {

    override fun start() {
        for (i in 0..10) {
            println("#".repeat(20))
        }
        println("TestStandalonePlugin started")
        for (i in 0..10) {
            println("#".repeat(20))
        }
    }

    override fun stop() {
        for (i in 0..10) {
            println("#".repeat(20))
        }
        println("TestStandalonePlugin stopped")
        for (i in 0..10) {
            println("#".repeat(20))
        }
        super.stop()
    }

    override fun createApplicationContext(): ApplicationContext {
        return AnnotationConfigApplicationContext().apply {
            classLoader = getWrapper().pluginClassLoader
            register(StandalonePluginConfiguration::class.java)
            refresh()
        }
    }
}