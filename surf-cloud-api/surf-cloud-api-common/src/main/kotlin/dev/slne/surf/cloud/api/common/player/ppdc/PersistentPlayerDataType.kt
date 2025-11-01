package dev.slne.surf.cloud.api.common.player.ppdc

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
    val primitiveType: Class<P>

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

    companion object {
        val BYTE = PrimitivePersistentPlayerDataType<Byte>()
        val SHORT = PrimitivePersistentPlayerDataType<Short>()
        val INT = PrimitivePersistentPlayerDataType<Int>()
        val LONG = PrimitivePersistentPlayerDataType<Long>()
        val FLOAT = PrimitivePersistentPlayerDataType<Float>()
        val DOUBLE = PrimitivePersistentPlayerDataType<Double>()
        val BOOLEAN = BooleanPersistentPlayerDataType.create()
        val STRING = PrimitivePersistentPlayerDataType<String>()
        val BYTE_ARRAY = PrimitivePersistentPlayerDataType<ByteArray>()
        val INT_ARRAY = PrimitivePersistentPlayerDataType<IntArray>()
        val LONG_ARRAY = PrimitivePersistentPlayerDataType<LongArray>()
        val TAG_CONTAINER = PrimitivePersistentPlayerDataType<PersistentPlayerDataContainer>()
        val LIST by lazy { ListPersistentPlayerDataTypeProvider() }


        internal class PrimitivePersistentPlayerDataType<P : Any>(override val primitiveType: Class<P>) :
            PersistentPlayerDataType<P, P> {
            override fun fromPrimitive(
                primitive: P,
                context: PersistentPlayerDataAdapterContext
            ): P = primitive

            override fun toPrimitive(
                complex: P,
                context: PersistentPlayerDataAdapterContext
            ): P = complex

            companion object {
                internal inline operator fun <reified P : Any> invoke(): PersistentPlayerDataType<P, P> {
                    return PrimitivePersistentPlayerDataType(P::class.java)
                }
            }
        }

        internal class BooleanPersistentPlayerDataType :
            PersistentPlayerDataType<Byte, Boolean> {
            override val primitiveType: Class<Byte> = Byte::class.java

            override fun fromPrimitive(
                primitive: Byte,
                context: PersistentPlayerDataAdapterContext
            ): Boolean = primitive != 0.toByte()

            override fun toPrimitive(
                complex: Boolean,
                context: PersistentPlayerDataAdapterContext
            ): Byte = if (complex) 1 else 0

            companion object {
                fun create(): PersistentPlayerDataType<Byte, Boolean> =
                    BooleanPersistentPlayerDataType()
            }
        }
    }
}