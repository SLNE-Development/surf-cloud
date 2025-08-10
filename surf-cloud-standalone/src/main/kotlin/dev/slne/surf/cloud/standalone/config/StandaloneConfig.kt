package dev.slne.surf.cloud.standalone.config

import dev.slne.surf.cloud.core.common.config.AbstractSurfCloudConfig
import dev.slne.surf.cloud.core.common.config.AbstractSurfCloudConfigHolder
import dev.slne.surf.cloud.core.common.config.ConfigReloadAware
import dev.slne.surf.cloud.standalone.config.ktor.KtorConfig
import dev.slne.surf.cloud.standalone.config.logging.ServerLoggingConfig
import dev.slne.surf.cloud.standalone.config.proxy.ProxyConfig
import dev.slne.surf.cloud.standalone.config.queue.QueueConfig
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment
import org.spongepowered.configurate.objectmapping.meta.Setting
import org.springframework.beans.factory.ObjectProvider
import org.springframework.stereotype.Component

@ConfigSerializable
class StandaloneConfig(
    @Comment("Configuration for the Ktor server.")
    @Setting("ktor")
    val ktor: KtorConfig = KtorConfig(),

    @Comment("Configuration for the logging.")
    @Setting("logging")
    val serverLogging: ServerLoggingConfig = ServerLoggingConfig(),

    @Comment("Configuration for the queue system.")
    @Setting("queue")
    val queue: QueueConfig = QueueConfig(),

    @Setting("proxy")
    val proxy: ProxyConfig = ProxyConfig(),

    @Setting("whitelist")
    val whitelist: WhitelistConfig = WhitelistConfig(),
) : AbstractSurfCloudConfig()

@Component
class StandaloneConfigHolder(reloadAware: ObjectProvider<ConfigReloadAware>) :
    AbstractSurfCloudConfigHolder<StandaloneConfig>(reloadAware, StandaloneConfig::class.java)