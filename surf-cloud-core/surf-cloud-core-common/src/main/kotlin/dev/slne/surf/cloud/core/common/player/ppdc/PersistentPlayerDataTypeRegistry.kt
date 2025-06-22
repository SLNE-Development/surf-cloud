package dev.slne.surf.cloud.core.common.player.ppdc

import com.google.common.primitives.Primitives
import dev.slne.surf.cloud.api.common.player.ppdc.ListPersistentPlayerDataType
import dev.slne.surf.cloud.api.common.player.ppdc.PersistentPlayerDataContainer
import dev.slne.surf.cloud.api.common.player.ppdc.PersistentPlayerDataType
import dev.slne.surf.cloud.api.common.util.mutableObject2ObjectMapOf
import dev.slne.surf.cloud.api.common.util.mutableObjectListOf
import dev.slne.surf.cloud.api.common.util.synchronize
import net.kyori.adventure.nbt.*
import kotlin.reflect.KClass
import kotlin.reflect.cast

private typealias AdapterCreator = (KClass<*>) -> PersistentPlayerDataTypeRegistry.TagAdapter<*, *>

object PersistentPlayerDataTypeRegistry {
    private val adapters = mutableObject2ObjectMapOf<KClass<*>, TagAdapter<*, *>>().synchronize()

    private val createAdapter: AdapterCreator = { createAdapter(it) }

    private fun <T : Any> createAdapter(type: KClass<T>): TagAdapter<*, *> {
        var type = type
        if (!Primitives.isWrapperType(type.java)) {
            type =
                Primitives.wrap(type.java).kotlin //Make sure we will always "switch" over the wrapper types
        }

        // region Primitives
        when (type) {
            Byte::class -> {
                return createAdapter(
                    Byte::class,
                    BinaryTagTypes.BYTE,
                    ByteBinaryTag::byteBinaryTag
                ) { it.value() }
            }

            Short::class -> {
                return createAdapter(
                    Short::class,
                    BinaryTagTypes.SHORT,
                    ShortBinaryTag::shortBinaryTag
                ) { it.value() }
            }

            Int::class -> {
                return createAdapter(
                    Int::class,
                    BinaryTagTypes.INT,
                    IntBinaryTag::intBinaryTag
                ) { it.value() }
            }

            Long::class -> {
                return createAdapter(
                    Long::class,
                    BinaryTagTypes.LONG,
                    LongBinaryTag::longBinaryTag
                ) { it.value() }
            }

            Float::class -> {
                return createAdapter(
                    Float::class,
                    BinaryTagTypes.FLOAT,
                    FloatBinaryTag::floatBinaryTag
                ) { it.value() }
            }

            Double::class -> {
                return createAdapter(
                    Double::class,
                    BinaryTagTypes.DOUBLE,
                    DoubleBinaryTag::doubleBinaryTag
                ) { it.value() }
            }

            Boolean::class -> {
                return createAdapter(
                    Boolean::class,
                    BinaryTagTypes.BYTE,
                    { if (it) ByteBinaryTag.ONE else ByteBinaryTag.ZERO }
                ) { it.value() != 0.toByte() }
            }

            Char::class -> {
                return createAdapter(
                    Char::class,
                    BinaryTagTypes.INT,
                    { IntBinaryTag.intBinaryTag(it.code) }
                ) { it.value().toChar() }
            }

            String::class -> {
                return createAdapter(
                    String::class,
                    BinaryTagTypes.STRING,
                    StringBinaryTag::stringBinaryTag
                ) { it.value() }
            }
            // endregion
            // region Primitive non-list arrays
            ByteArray::class -> {
                return createAdapter(
                    ByteArray::class,
                    BinaryTagTypes.BYTE_ARRAY,
                    { ByteArrayBinaryTag.byteArrayBinaryTag(*it.copyOf()) },
                    { it.value().copyOf() }
                )
            }

            IntArray::class -> {
                return createAdapter(
                    IntArray::class,
                    BinaryTagTypes.INT_ARRAY,
                    { IntArrayBinaryTag.intArrayBinaryTag(*it.copyOf()) },
                    { it.value().copyOf() }
                )
            }

            LongArray::class -> {
                return createAdapter(
                    LongArray::class,
                    BinaryTagTypes.LONG_ARRAY,
                    { LongArrayBinaryTag.longArrayBinaryTag(*it.copyOf()) },
                    { it.value().copyOf() }
                )
            }

            BooleanArray::class -> {
                return createAdapter(
                    BooleanArray::class,
                    BinaryTagTypes.BYTE_ARRAY,
                    { bytes ->
                        ByteArrayBinaryTag.byteArrayBinaryTag(*bytes.map { if (it) 1.toByte() else 0.toByte() }
                            .toByteArray())
                    },
                    { tag -> tag.value().map { it == 1.toByte() }.toBooleanArray() }
                )
            }

            CharArray::class -> {
                return createAdapter(
                    CharArray::class,
                    BinaryTagTypes.INT_ARRAY,
                    { ints ->
                        IntArrayBinaryTag.intArrayBinaryTag(*ints.map { it.code }.toIntArray())
                    },
                    { tag -> tag.value().map { it.toChar() }.toCharArray() }
                )
            }
            // endregion
            Array<PersistentPlayerDataContainer>::class -> {
                return createAdapter(
                    Array<PersistentPlayerDataContainer>::class,
                    BinaryTagTypes.LIST,
                    { pdcs ->
                        val builder = ListBinaryTag.builder(BinaryTagTypes.COMPOUND)
                        for (pdc in pdcs) {
                            require(pdc is PersistentPlayerDataContainerImpl) { "The PDC must be an instance of PersistentPlayerDataContainerImpl" }
                            builder.add(pdc.toTagCompound())
                        }
                        builder.build()
                    }, {
                        it.mapIndexed { index, _ ->
                            val container = PersistentPlayerDataContainerImpl()
                            val compound = it.getCompound(index)
                            compound.forEach { (key, value) -> container.put(key, value) }
                            container
                        }.toTypedArray()
                    }
                )
            }

            PersistentPlayerDataContainer::class -> {
                return createAdapter(
                    PersistentPlayerDataContainerImpl::class,
                    BinaryTagTypes.COMPOUND,
                    { it.toTagCompound() },
                    {
                        PersistentPlayerDataContainerImpl().apply {
                            it.forEach { (key, value) ->
                                put(key, value)
                            }
                        }
                    }
                )
            }

            List::class -> {
                @Suppress("UNCHECKED_CAST")
                return createAdapter(
                    List::class,
                    BinaryTagTypes.LIST,
                    { type, value ->
                        constructList(
                            type as PersistentPlayerDataType<List<T>, *>,
                            value as List<T>
                        )
                    },
                    this::extractList,
                    this::matchesListTag
                )
            }
        }

        error("Could not find a valid TagAdapter implementation for the requested type ${type.simpleName}")
    }

    private fun <T : Any, Z : BinaryTag> createAdapter(
        primitiveType: KClass<T>,
        nbtBaseType: BinaryTagType<Z>,
        builder: (T) -> Z,
        extractor: (Z) -> T
    ): TagAdapter<T, Z> {
        return createAdapter(
            primitiveType,
            nbtBaseType,
            { _, value -> builder(value) },
            { _, value -> extractor(value) },
            { _, tag -> nbtBaseType.test(tag.type()) }
        )
    }

    private fun <T : Any, Z : BinaryTag> createAdapter(
        primitiveType: KClass<T>,
        nbtBaseType: BinaryTagType<Z>,
        builder: (PersistentPlayerDataType<T, *>, T) -> Z,
        extractor: (PersistentPlayerDataType<T, *>, Z) -> T,
        matcher: (PersistentPlayerDataType<T, *>, BinaryTag) -> Boolean
    ): TagAdapter<T, Z> {
        return TagAdapter(primitiveType, nbtBaseType, builder, extractor, matcher)
    }

    fun <P : Any, C> isInstanceOf(type: PersistentPlayerDataType<P, C>, tag: BinaryTag): Boolean {
        return getOrCreateAdapter<P, BinaryTag>(type).isInstance(type, tag)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any, Z : BinaryTag> getOrCreateAdapter(type: PersistentPlayerDataType<T, *>): TagAdapter<T, Z> {
        return adapters.computeIfAbsent(type.primitiveType, createAdapter) as TagAdapter<T, Z>
    }

    fun <T : Any> wrap(type: PersistentPlayerDataType<T, *>, value: T): BinaryTag {
        return getOrCreateAdapter<T, BinaryTag>(type).build(type, value)
    }

    fun <T : Any, Z : BinaryTag> extract(type: PersistentPlayerDataType<T, *>, tag: BinaryTag): T {
        val primitiveType = type.primitiveType
        val adapter = getOrCreateAdapter<T, Z>(type)
        require(
            adapter.isInstance(
                type,
                tag
            )
        ) { "The found tag instance (${tag::class.simpleName}) cannot store ${primitiveType.simpleName}" }

        val foundValue = adapter.extract(type, tag)
        require(primitiveType.isInstance(foundValue)) { "The found object is of the type ${foundValue::class.simpleName}. Expected type ${primitiveType.simpleName}" }
        return primitiveType.cast(foundValue)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <P : Any, T : List<P>> constructList(
        type: PersistentPlayerDataType<T, *>,
        list: List<P>
    ): ListBinaryTag {
        check(type is ListPersistentPlayerDataType<*, *>) { "The passed list cannot be written to the PDC with a ${type::class.simpleName} (expected a list data type)" }
        val type = type as ListPersistentPlayerDataType<P, *>
        val elementType = type.elementType

        getOrCreateAdapter<P, BinaryTag>(elementType)
        val values = list.map { wrap(elementType, it) }

        return ListBinaryTag.heterogeneousListBinaryTag()
            .add(values)
            .build()
    }

    @Suppress("UNCHECKED_CAST")
    private fun <P : Any> extractList(
        type: PersistentPlayerDataType<P, *>,
        listTag: ListBinaryTag
    ): List<P> {
        check(type is ListPersistentPlayerDataType<*, *>) { "The found list tag cannot be read with a ${type::class.simpleName} (expected a list data type)" }
        val type = type as ListPersistentPlayerDataType<P, *>
        val elementType = type.elementType
        val output = mutableObjectListOf<P>(listTag.size())

        for (tag in listTag) {
            output.add(extract<P, BinaryTag>(elementType, tag))
        }

        return output
    }


    private fun matchesListTag(
        type: PersistentPlayerDataType<List<*>, *>,
        tag: BinaryTag
    ): Boolean {
        if (type !is ListPersistentPlayerDataType<*, *>) {
            return false
        }

        if (tag !is ListBinaryTag) {
            return false
        }

        val elementType = tag.elementType()
        val elementAdapter: TagAdapter<out Any, BinaryTag> = getOrCreateAdapter(type.elementType)

        return elementAdapter.nbtBaseType.test(elementType) || elementType == BinaryTagTypes.END
    }

    internal data class TagAdapter<P : Any, T : BinaryTag>(
        val primitiveType: KClass<P>,
        val nbtBaseType: BinaryTagType<T>,
        val builder: (PersistentPlayerDataType<P, *>, P) -> T,
        val extractor: (PersistentPlayerDataType<P, *>, T) -> P,
        val matcher: (PersistentPlayerDataType<P, *>, BinaryTag) -> Boolean
    ) {
        @Suppress("UNCHECKED_CAST")
        fun extract(dataType: PersistentPlayerDataType<P, *>, base: BinaryTag): P {
            require(nbtBaseType.test(base.type())) { "The provided NBTBase was of the type ${base.type()}. Expected type $nbtBaseType" }
            return extractor(dataType, base as T)
        }

        fun build(dataType: PersistentPlayerDataType<P, *>, value: Any): T {
            require(primitiveType.isInstance(value)) { "The provided value was of the type ${value::class.simpleName}. Expected type ${primitiveType.simpleName}" }
            return builder(dataType, primitiveType.cast(value))
        }

        fun isInstance(
            persistentDataType: PersistentPlayerDataType<P, *>,
            base: BinaryTag
        ): Boolean = matcher(persistentDataType, base)

    }
}