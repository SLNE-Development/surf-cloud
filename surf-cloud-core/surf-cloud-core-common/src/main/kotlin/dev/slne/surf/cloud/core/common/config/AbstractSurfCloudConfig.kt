@file:Internal

package dev.slne.surf.cloud.core.common.config

import dev.slne.surf.cloud.api.common.util.forEachAnnotationOrdered
import dev.slne.surf.cloud.core.common.config.connection.ConnectionConfig
import dev.slne.surf.cloud.core.common.config.logging.LoggingConfig
import dev.slne.surf.cloud.core.common.coreCloudInstance
import dev.slne.surf.surfapi.core.api.config.manager.SpongeConfigManager
import dev.slne.surf.surfapi.core.api.config.surfConfigApi
import org.jetbrains.annotations.ApiStatus.Internal
import org.spongepowered.configurate.objectmapping.meta.Comment
import org.spongepowered.configurate.objectmapping.meta.Setting
import org.springframework.beans.factory.ObjectProvider

abstract class AbstractSurfCloudConfig(
    @Comment("Config for various connections")
    @Setting("connection")
    val connectionConfig: ConnectionConfig = ConnectionConfig(),

    @Comment("Config for logging")
    @Setting("logging")
    val logging: LoggingConfig = LoggingConfig()
)

abstract class AbstractSurfCloudConfigHolder<C : AbstractSurfCloudConfig>(
    private val reloadAware: ObjectProvider<ConfigReloadAware>,
    configClass: Class<C>
) {
    private val manager: SpongeConfigManager<C>

    init {
        surfConfigApi.createSpongeYmlConfig(
            configClass,
            coreCloudInstance.dataFolder,
            "config.yml"
        )

        manager = surfConfigApi.getSpongeConfigManagerForConfig(configClass)
    }

    val config: C
        get() = manager.config


    fun reloadFromFile() {
        reloadAware.forEachAnnotationOrdered { it.beforeReload() }
        manager.reloadFromFile()
        reloadAware.forEachAnnotationOrdered { it.afterReload() }
    }

    fun saveToFile() {
        reloadAware.forEachAnnotationOrdered { it.beforeSave() }
        manager.save()
    }
}