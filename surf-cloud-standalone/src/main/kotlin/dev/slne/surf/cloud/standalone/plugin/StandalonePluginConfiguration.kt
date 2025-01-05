package dev.slne.surf.cloud.standalone.plugin

import org.pf4j.spring.SpringPluginManager
import org.springframework.context.annotation.Bean

//@Configuration
class StandalonePluginConfiguration {

    @Bean
    fun pluginManager(): SpringPluginManager {
        for (i in 0..10) {
            println("#".repeat(20))
        }
        System.err.println("pluginManager")
        for (i in 0..10) {
            println("#".repeat(20))
        }
        return SpringPluginManager()
//        return object : SpringPluginManager() {
//            override fun createExtensionFactory(): ExtensionFactory {
//                return SingletonSpringExtensionFactory(this)
//            }
//        }
    }

}