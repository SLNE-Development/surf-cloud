package dev.slne.surf.cloud.api.common.player.ppdc

/**
 * This interface represents the context in which the [PersistentPlayerDataType] can
 * serialize and deserialize the passed values.
 */
interface PersistentPlayerDataAdapterContext {
    /**
     * Creates a new and empty meta-container instance.
     *
     * @return the fresh container instance
     */
    fun newPersistentDataContainer(): PersistentPlayerDataContainer
}