package dev.slne.surf.cloud.api.common.player.ppdc

/**
 * The list persistent data represents a data type that is capable of storing a
 * list of other data types in a [PersistentPlayerDataContainer].
 *
 * @param <P> the primitive type of the list element.
 * @param <C> the complex type of the list elements.
 */
interface ListPersistentPlayerDataType<P : Any, C>: PersistentPlayerDataType<List<P>, C> {

    /**
     * Provides the persistent data type of the elements found in the list.
     */
    val elementType: PersistentPlayerDataType<P, C>
}