package dev.slne.surf.cloud.core.client.config

import dev.slne.surf.cloud.core.common.coreCloudInstance
import dev.slne.surf.surfapi.core.api.config.createSpongeYmlConfig
import dev.slne.surf.surfapi.core.api.config.surfConfigApi
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment
import org.spongepowered.configurate.objectmapping.meta.Setting

val clientConfig: ClientConfig by lazy {
    surfConfigApi.createSpongeYmlConfig<ClientConfig>(coreCloudInstance.dataFolder, "client-config.yml")
}

@ConfigSerializable
data class ClientConfig(

    @Comment("Whether this server is considered as a lobby server.")
    @Setting("isLobby")
    val isLobby: Boolean = false,
)