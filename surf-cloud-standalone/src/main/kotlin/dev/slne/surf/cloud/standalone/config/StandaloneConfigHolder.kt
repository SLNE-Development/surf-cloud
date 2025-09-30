package dev.slne.surf.cloud.standalone.config

import dev.slne.surf.cloud.core.common.CloudCoreInstance
import dev.slne.surf.cloud.core.common.config.AbstractSurfCloudConfigHolder
import dev.slne.surf.cloud.core.common.config.ConfigReloadAware
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.getBean
import org.springframework.stereotype.Component

@Component
class StandaloneConfigHolder(reloadAware: ObjectProvider<ConfigReloadAware>) :
    AbstractSurfCloudConfigHolder<StandaloneConfig>(reloadAware, StandaloneConfig::class.java) {

    companion object {
        val holder
            get() = CloudCoreInstance.internalContext?.getBean<StandaloneConfigHolder>()
                ?: error("StandaloneConfigHolder is not initialized yet")

        fun isFlywayEnabled(): Boolean = createConfig(StandaloneConfig::class.java)
            .connectionConfig
            .databaseConfig
            .flyway.enabled
    }
}