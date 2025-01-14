package dev.slne.surf.cloud.standalone.plugin.provider.configuration

import dev.slne.surf.cloud.api.server.server.plugin.configuration.PluginMeta
import dev.slne.surf.cloud.standalone.plugin.provider.configuration.serializer.ComponentSerializer
import dev.slne.surf.cloud.standalone.plugin.provider.configuration.serializer.EnumValueSerializer
import dev.slne.surf.cloud.standalone.plugin.provider.configuration.serializer.constraints.PluginMetaConstraints
import dev.slne.surf.cloud.standalone.plugin.provider.configuration.type.DependencyConfiguration
import dev.slne.surf.cloud.standalone.plugin.provider.configuration.type.PluginDependencyLifeCycle
import org.spongepowered.configurate.kotlin.dataClassFieldDiscoverer
import org.spongepowered.configurate.loader.HeaderMode
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.ObjectMapper
import org.spongepowered.configurate.objectmapping.meta.Required
import org.spongepowered.configurate.yaml.NodeStyle
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.BufferedReader
import java.util.*

@ConfigSerializable
data class StandalonePluginMeta(

    @PluginMetaConstraints.PluginName
    @Required
    override val name: String,

    @Required
    override val main: String,

    val bootstrapper: String? = null,

    val loader: String? = null,

    @Required
    override val version: String,

    override val description: String? = null,

    override val authors: Set<String> = emptySet(),
    override val contributors: Set<String> = emptySet(),

    val dependencies: Map<PluginDependencyLifeCycle, Map<String, DependencyConfiguration>> = EnumMap(
        PluginDependencyLifeCycle::class.java
    )
) : PluginMeta {
    override val pluginDependencies: Set<String>
        get() = dependencies.getOrDefault(PluginDependencyLifeCycle.SERVER, emptyMap())
            .filterValues { it.required && it.joinClasspath }
            .keys

    override val pluginSoftDependencies: Set<String>
        get() = dependencies.getOrDefault(PluginDependencyLifeCycle.SERVER, emptyMap())
            .filterValues { !it.required && it.joinClasspath }
            .keys

    override val loadBefore: Set<String>
        get() = dependencies.getOrDefault(PluginDependencyLifeCycle.SERVER, emptyMap())
            // This plugin will load BEFORE all dependencies (so dependencies will load AFTER plugin)
            .filterValues { it.load == DependencyConfiguration.LoadOrder.AFTER }
            .keys

    val loadAfter: Set<String>
        get() = dependencies.getOrDefault(PluginDependencyLifeCycle.SERVER, emptyMap())
            // This plugin will load AFTER all dependencies (so dependencies will load BEFORE plugin)
            .filterValues { it.load == DependencyConfiguration.LoadOrder.BEFORE }
            .keys

    val bootstrapDependencies: Map<String, DependencyConfiguration>
        get() = dependencies.getOrDefault(PluginDependencyLifeCycle.BOOTSTRAP, emptyMap())

    val serverDependencies: Map<String, DependencyConfiguration>
        get() = dependencies.getOrDefault(PluginDependencyLifeCycle.SERVER, emptyMap())

    companion object {
        fun create(reader: BufferedReader): StandalonePluginMeta {
            val loader = YamlConfigurationLoader.builder()
                .indent(2)
                .nodeStyle(NodeStyle.BLOCK)
                .headerMode(HeaderMode.NONE)
                .source { reader }
                .defaultOptions {
                    it.serializers {
                        it.register(EnumValueSerializer())
                            .register(ComponentSerializer())
                            .registerAnnotatedObjects(
                                ObjectMapper.factoryBuilder()
                                    .addConstraint(
                                        PluginMetaConstraints.PluginName::class.java,
                                        String::class.java,
                                        PluginMetaConstraints.PluginName.Factory()
                                    )
                                    .addDiscoverer(dataClassFieldDiscoverer())
                                    .build()
                            )
                    }
                }.build()

            val node = loader.load()
            val meta = node.require(StandalonePluginMeta::class.java)

            return meta
        }
    }
}