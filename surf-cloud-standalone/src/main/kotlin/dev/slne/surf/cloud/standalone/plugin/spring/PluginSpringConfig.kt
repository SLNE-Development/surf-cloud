package dev.slne.surf.cloud.standalone.plugin.spring

import dev.slne.surf.cloud.api.server.plugin.PluginConfig
import dev.slne.surf.cloud.api.server.plugin.configuration.PluginMeta
import dev.slne.surf.cloud.api.server.plugin.provider.classloader.SpringPluginClassloader
import dev.slne.surf.cloud.core.common.spring.CloudChildSpringApplicationConfiguration
import dev.slne.surf.cloud.standalone.commands.ConsoleCommandProcessor
import dev.slne.surf.cloud.standalone.plugin.entrypoint.classloader.SpringPluginClassloaderImpl
import dev.slne.surf.cloud.standalone.plugin.spring.config.DatabaseConfigConfiguration
import dev.slne.surf.cloud.standalone.plugin.spring.config.PluginDatasourceConfiguration
import dev.slne.surf.cloud.standalone.plugin.spring.config.PluginFlywayConfigurationCustomizer
import dev.slne.surf.cloud.standalone.plugin.spring.config.PluginLoadTimeWeavingConfiguration
import org.jetbrains.exposed.spring.autoconfigure.ExposedAutoConfiguration
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.RootBeanDefinition
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.stereotype.Component

@Component
class PluginSpringConfig(private val consoleCommandProcessor: ConsoleCommandProcessor) :
    CloudChildSpringApplicationConfiguration {

    override fun configureChildApplication(
        builder: SpringApplicationBuilder,
        classLoader: ClassLoader,
        vararg parentClassLoader: ClassLoader
    ) {
        require(classLoader is SpringPluginClassloaderImpl)
        builder.initializers({ context ->
            require(context is BeanDefinitionRegistry)
            context.registerBeanDefinition(
                "pluginMeta",
                RootBeanDefinition(PluginMeta::class.java) { classLoader.meta }
            )
            context.registerBeanDefinition(
                "pluginClassloader",
                RootBeanDefinition(SpringPluginClassloader::class.java) { classLoader }
            )
            context.registerBeanDefinition(
                "pluginConsoleCommandProcessor",
                RootBeanDefinition(ConsoleCommandProcessor::class.java) { consoleCommandProcessor }
            )

            classLoader.context = context
        })

        builder.sources(
            DatabaseConfigConfiguration::class.java,
            PluginLoadTimeWeavingConfiguration::class.java,
            PluginDatasourceConfiguration::class.java,
            PluginFlywayConfigurationCustomizer::class.java,
            ExposedAutoConfiguration::class.java,
            PluginConfig::class.java
        )
    }
}