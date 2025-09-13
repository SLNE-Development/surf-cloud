package dev.slne.surf.cloud.standalone.config

import dev.slne.surf.cloud.core.common.config.AbstractSurfCloudConfigHolder
import dev.slne.surf.cloud.core.common.config.ConfigReloadAware
import org.springframework.beans.factory.ObjectProvider
import org.springframework.stereotype.Component

@Component
class StandaloneConfigHolder(reloadAware: ObjectProvider<ConfigReloadAware>) :
    AbstractSurfCloudConfigHolder<StandaloneConfig>(reloadAware, StandaloneConfig::class.java)