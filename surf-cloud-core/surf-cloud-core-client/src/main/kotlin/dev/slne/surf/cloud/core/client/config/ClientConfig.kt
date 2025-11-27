package dev.slne.surf.cloud.core.client.config

import dev.slne.surf.cloud.core.client.config.reconnect.ReconnectConfig
import dev.slne.surf.cloud.core.common.config.AbstractSurfCloudConfig
import dev.slne.surf.cloud.core.common.config.AbstractSurfCloudConfigHolder
import dev.slne.surf.cloud.core.common.config.ConfigReloadAware
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment
import org.spongepowered.configurate.objectmapping.meta.Setting
import org.springframework.beans.factory.ObjectProvider
import org.springframework.stereotype.Component

@ConfigSerializable
class ClientConfig(
    @Comment("Whether this server is considered as a lobby server.")
    @Setting("isLobby")
    val isLobby: Boolean = false,

    @Comment("Reconnect configuration.")
    @Setting("reconnect")
    val reconnectConfig: ReconnectConfig = ReconnectConfig()
) : AbstractSurfCloudConfig()

@Component
class ClientConfigHolder(reloadAware: ObjectProvider<ConfigReloadAware>) :
    AbstractSurfCloudConfigHolder<ClientConfig>(reloadAware, ClientConfig::class.java)