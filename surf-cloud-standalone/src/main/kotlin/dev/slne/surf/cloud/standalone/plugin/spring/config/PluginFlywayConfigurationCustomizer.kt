package dev.slne.surf.cloud.standalone.plugin.spring.config

import dev.slne.surf.cloud.api.server.plugin.provider.classloader.SpringPluginClassloader
import dev.slne.surf.surfapi.core.api.reflection.Field
import dev.slne.surf.surfapi.core.api.reflection.SurfProxy
import dev.slne.surf.surfapi.core.api.reflection.createProxy
import dev.slne.surf.surfapi.core.api.reflection.surfReflection
import org.flywaydb.core.api.configuration.ClassicConfiguration
import org.flywaydb.core.api.configuration.FluentConfiguration
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.net.URLClassLoader

@Configuration(proxyBeanMethods = false)
@Profile("plugin")
@Suppress("SpringJavaInjectionPointsAutowiringInspection")
class PluginFlywayConfigurationCustomizer(private val pluginClassloader: SpringPluginClassloader) :
    FlywayConfigurationCustomizer {

    override fun customize(configuration: FluentConfiguration) {
        configuration.table(pluginClassloader.meta.flywayTableName)

        val config = fluentConfigurationProxy.configuration(configuration)
        val source = pluginClassloader.source
        config.classLoader =
            URLClassLoader("${source.fileName}-flyway", arrayOf(source.toUri().toURL()), null)
    }

    companion object {

        val fluentConfigurationProxy = surfReflection.createProxy<FluentConfigurationProxy>()

        @SurfProxy(FluentConfiguration::class)
        interface FluentConfigurationProxy {
            @Field(name = "config", type = Field.Type.GETTER)
            fun configuration(instance: FluentConfiguration): ClassicConfiguration
        }
    }
}