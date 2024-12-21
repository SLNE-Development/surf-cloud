package dev.slne.surf.cloud.standalone.config

import dev.slne.surf.cloud.core.common.coreCloudInstance
import dev.slne.surf.surfapi.core.api.SurfCoreApi
import dev.slne.surf.surfapi.core.api.config.createSpongeYmlConfig
import dev.slne.surf.surfapi.core.api.config.surfConfigApi
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment
import org.spongepowered.configurate.objectmapping.meta.Setting


val standaloneConfig: StandaloneConfig by lazy {
    surfConfigApi.createSpongeYmlConfig(coreCloudInstance.dataFolder, "standalone-config.yml")
}

@ConfigSerializable
data class StandaloneConfig(

    @Comment("Whether only one proxy is used by the network. If enabled the server are automatically registered to the proxy.")
    @Setting("use-single-proxy-setup")
    val useSingleProxySetup: Boolean = false
)