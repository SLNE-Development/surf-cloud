package dev.slne.surf.cloud.api.common.player.ppdc

/**
 * Represents a data type capable of storing a list of elements in a persistent container.
 *
 * @param P The primitive type of the list elements.
 * @param C The complex type of the list elements.
 */
interface ListPersistentPlayerDataType<P : Any, C>: PersistentPlayerDataType<List<P>, List<C>> {

    /**
     * The data type of the elements in the list.
     */
    val elementType: PersistentPlayerDataType<P, C>
}