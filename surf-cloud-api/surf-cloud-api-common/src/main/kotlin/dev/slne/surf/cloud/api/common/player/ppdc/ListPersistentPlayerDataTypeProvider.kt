package dev.slne.surf.cloud.api.common.player.ppdc

import com.google.common.collect.Lists

class ListPersistentPlayerDataTypeProvider {
    companion object {
        private val BYTE = ListPersistentPlayerDataTypeImpl.create(PersistentPlayerDataType.BYTE)
        private val SHORT = ListPersistentPlayerDataTypeImpl.create(PersistentPlayerDataType.SHORT)
        private val INT = ListPersistentPlayerDataTypeImpl.create(PersistentPlayerDataType.INT)
        private val LONG = ListPersistentPlayerDataTypeImpl.create(PersistentPlayerDataType.LONG)
        private val FLOAT = ListPersistentPlayerDataTypeImpl.create(PersistentPlayerDataType.FLOAT)
        private val DOUBLE =
            ListPersistentPlayerDataTypeImpl.create(PersistentPlayerDataType.DOUBLE)
        private val BOOLEAN =
            ListPersistentPlayerDataTypeImpl.create(PersistentPlayerDataType.BOOLEAN)
        private val STRING =
            ListPersistentPlayerDataTypeImpl.create(PersistentPlayerDataType.STRING)
        private val BYTE_ARRAY =
            ListPersistentPlayerDataTypeImpl.create(PersistentPlayerDataType.BYTE_ARRAY)
        private val INT_ARRAY =
            ListPersistentPlayerDataTypeImpl.create(PersistentPlayerDataType.INT_ARRAY)
        private val LONG_ARRAY =
            ListPersistentPlayerDataTypeImpl.create(PersistentPlayerDataType.LONG_ARRAY)
        private val DATA_CONTAINER =
            ListPersistentPlayerDataTypeImpl.create(PersistentPlayerDataType.TAG_CONTAINER)
    }


    fun bytes() = BYTE
    fun shorts() = SHORT
    fun ints() = INT
    fun longs() = LONG
    fun floats() = FLOAT
    fun doubles() = DOUBLE
    fun booleans() = BOOLEAN
    fun strings() = STRING
    fun byteArrays() = BYTE_ARRAY
    fun intArrays() = INT_ARRAY
    fun longArrays() = LONG_ARRAY
    fun dataContainers() = DATA_CONTAINER
    fun <P : Any, C> listTypeFrom(elementType: PersistentPlayerDataType<P, C>) =
        ListPersistentPlayerDataTypeImpl.create(elementType)

    private class ListPersistentPlayerDataTypeImpl<P : Any, C>(
        override val elementType: PersistentPlayerDataType<P, C>
    ) : ListPersistentPlayerDataType<P, C> {
        @Suppress("UNCHECKED_CAST")
        override val primitiveType = List::class.java as Class<List<P>>

        override fun fromPrimitive(
            primitive: List<P>,
            context: PersistentPlayerDataAdapterContext
        ): List<C> {
            return Lists.transform(primitive) { elementType.fromPrimitive(it, context) }
        }

        override fun toPrimitive(
            complex: List<C>,
            context: PersistentPlayerDataAdapterContext
        ): List<P> {
            return Lists.transform(complex) { elementType.toPrimitive(it, context) }
        }

        companion object {
            fun <P : Any, C> create(elementType: PersistentPlayerDataType<P, C>): ListPersistentPlayerDataType<P, C> {
                return ListPersistentPlayerDataTypeImpl(elementType)
            }
        }
    }
}