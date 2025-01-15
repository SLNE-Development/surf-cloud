package dev.slne.surf.cloud.api.common.player.ppdc

import kotlin.reflect.KClass

/**
 * Represents a data type that can be serialized and deserialized in a persistent player data container.
 *
 * @param P The primitive type used for storage.
 * @param C The complex type represented by the primitive.
 */
interface PersistentPlayerDataType<P : Any, C> {
    /**
     * The primitive type used for serialization.
     */
    val primitiveType: KClass<P>

    /**
     * Converts a primitive value to its complex representation.
     *
     * @param primitive The primitive value to convert.
     * @param context The serialization context.
     * @return The complex representation of the primitive value.
     */
    fun fromPrimitive(primitive: P, context: PersistentPlayerDataAdapterContext): C

    /**
     * Converts a complex value to its primitive representation.
     *
     * @param complex The complex value to convert.
     * @param context The serialization context.
     * @return The primitive representation of the complex value.
     */
    fun toPrimitive(complex: C, context: PersistentPlayerDataAdapterContext): P
}