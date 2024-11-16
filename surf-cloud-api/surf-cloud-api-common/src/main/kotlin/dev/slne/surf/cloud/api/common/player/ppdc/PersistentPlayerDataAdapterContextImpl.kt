package dev.slne.surf.cloud.api.common.player.ppdc

internal object PersistentPlayerDataAdapterContextImpl: PersistentPlayerDataAdapterContext {
    override fun newPersistentDataContainer(): PersistentPlayerDataContainer {
        return PersistentPlayerDataContainerImpl()
    }
}