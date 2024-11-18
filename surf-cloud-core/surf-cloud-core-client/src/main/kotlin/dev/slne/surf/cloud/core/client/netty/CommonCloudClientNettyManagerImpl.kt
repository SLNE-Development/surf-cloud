package dev.slne.surf.cloud.core.client.netty

import dev.slne.surf.cloud.api.client.netty.CloudClientNettyManager
import dev.slne.surf.cloud.core.common.util.checkInstantiationByServiceLoader

abstract class CommonCloudClientNettyManagerImpl : CloudClientNettyManager {
    init {
        checkInstantiationByServiceLoader()
    }
}