package dev.slne.surf.cloud.api.common.player.ppdc

/**
 * Represents the context for serializing and deserializing data types.
 */
interface PersistentPlayerDataAdapterContext {
    /**
     * Creates a new, empty instance of a persistent data container.
     *
     * @return A new [PersistentPlayerDataContainer].
     */
    fun newPersistentDataContainer(): PersistentPlayerDataContainer
}