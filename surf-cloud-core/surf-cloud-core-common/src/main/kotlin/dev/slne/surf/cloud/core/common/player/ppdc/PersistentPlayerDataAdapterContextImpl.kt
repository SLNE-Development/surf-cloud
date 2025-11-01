package dev.slne.surf.cloud.core.common.player.ppdc

import dev.slne.surf.cloud.api.common.player.ppdc.PersistentPlayerDataAdapterContext
import dev.slne.surf.cloud.api.common.player.ppdc.PersistentPlayerDataContainer

object PersistentPlayerDataAdapterContextImpl : PersistentPlayerDataAdapterContext {
    override fun newPersistentDataContainer(): PersistentPlayerDataContainer {
        return PersistentPlayerDataContainerImpl()
    }
}