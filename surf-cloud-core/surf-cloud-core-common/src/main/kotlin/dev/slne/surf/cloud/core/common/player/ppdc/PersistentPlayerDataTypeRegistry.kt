package dev.slne.surf.cloud.core.common.player.ppdc

import com.google.common.primitives.Primitives
import dev.slne.surf.cloud.api.common.player.ppdc.ListPersistentPlayerDataType
import dev.slne.surf.cloud.api.common.player.ppdc.PersistentPlayerDataContainer
import dev.slne.surf.cloud.api.common.player.ppdc.PersistentPlayerDataType
import dev.slne.surf.surfapi.core.api.util.mutableObjectListOf
import net.kyori.adventure.nbt.*
import java.util.concurrent.ConcurrentHashMap

private typealias AdapterCreator = (Class<*>) -> PersistentPlayerDataTypeRegistry.TagAdapter<*, *>

@Suppress("UNCHECKED_CAST")
object PersistentPlayerDataTypeRegistry {
    private val adapters = ConcurrentHashMap<Class<*>, TagAdapter<*, *>>()

    private val createAdapter: AdapterCreator = { createAdapter(it) }

    private fun <T : Any> createAdapter(type: Class<T>): TagAdapter<*, *> {
        var type = type
        if (!Primitives.isWrapperType(type)) {
            type = Primitives.wrap(type) //Make sure we will always "switch" over the wrapper types
        }

        // region Primitives
        return when (type) {
            Byte::class.java -> createAdapter(
                Byte::class.java,
                ByteBinaryTag::class.java,
                BinaryTagTypes.BYTE,
                ByteBinaryTag::byteBinaryTag
            ) { it.value() }

            Short::class.java -> createAdapter(
                Short::class.java,
                ShortBinaryTag::class.java,
                BinaryTagTypes.SHORT,
                ShortBinaryTag::shortBinaryTag
            ) { it.value() }

            Int::class.java -> createAdapter(
                Int::class.java,
                IntBinaryTag::class.java,
                BinaryTagTypes.INT,
                IntBinaryTag::intBinaryTag
            ) { it.value() }

            Long::class.java -> createAdapter(
                Long::class.java,
                LongBinaryTag::class.java,
                BinaryTagTypes.LONG,
                LongBinaryTag::longBinaryTag
            ) { it.value() }

            Float::class.java -> createAdapter(
                Float::class.java,
                FloatBinaryTag::class.java,
                BinaryTagTypes.FLOAT,
                FloatBinaryTag::floatBinaryTag
            ) { it.value() }

            Double::class.java -> createAdapter(
                Double::class.java,
                DoubleBinaryTag::class.java,
                BinaryTagTypes.DOUBLE,
                DoubleBinaryTag::doubleBinaryTag
            ) { it.value() }

            Boolean::class.java -> createAdapter(
                Boolean::class.java,
                ByteBinaryTag::class.java,
                BinaryTagTypes.BYTE,
                { if (it) ByteBinaryTag.ONE else ByteBinaryTag.ZERO }
            ) { it.value() != 0.toByte() }

            Char::class.java -> createAdapter(
                Char::class.java,
                IntBinaryTag::class.java,
                BinaryTagTypes.INT,
                { IntBinaryTag.intBinaryTag(it.code) }
            ) { it.value().toChar() }

            String::class.java -> createAdapter(
                String::class.java,
                StringBinaryTag::class.java,
                BinaryTagTypes.STRING,
                StringBinaryTag::stringBinaryTag
            ) { it.value() }

            // endregion
            // region Primitive non-list arrays
            ByteArray::class.java -> createAdapter(
                ByteArray::class.java,
                ByteArrayBinaryTag::class.java,
                BinaryTagTypes.BYTE_ARRAY,
                ByteArrayBinaryTag::byteArrayBinaryTag,
                ByteArrayBinaryTag::value
            )


            IntArray::class.java -> createAdapter(
                IntArray::class.java,
                IntArrayBinaryTag::class.java,
                BinaryTagTypes.INT_ARRAY,
                IntArrayBinaryTag::intArrayBinaryTag,
                IntArrayBinaryTag::value
            )

            LongArray::class.java -> createAdapter(
                LongArray::class.java,
                LongArrayBinaryTag::class.java,
                BinaryTagTypes.LONG_ARRAY,
                LongArrayBinaryTag::longArrayBinaryTag,
                LongArrayBinaryTag::value
            )

            // endregion
            Array<PersistentPlayerDataContainer>::class.java -> createAdapter(
                Array<PersistentPlayerDataContainer>::class.java,
                ListBinaryTag::class.java,
                BinaryTagTypes.LIST,
                { containerArray ->
                    val builder = ListBinaryTag.builder(BinaryTagTypes.COMPOUND)
                    for (pdc in containerArray) {
                        require(pdc is PersistentPlayerDataContainerImpl) { "The PDC must be an instance of PersistentPlayerDataContainerImpl" }
                        builder.add(pdc.toTagCompound())
                    }
                    builder.build()
                }, { tag ->
                    tag.mapIndexed { index, _ ->
                        val container = PersistentPlayerDataContainerImpl()
                        val compound = tag.getCompound(index)
                        compound.forEach { (key, value) -> container.put(key, value) }
                        container
                    }.toTypedArray()
                }
            )


            PersistentPlayerDataContainer::class.java -> createAdapter(
                PersistentPlayerDataContainerImpl::class.java,
                CompoundBinaryTag::class.java,
                BinaryTagTypes.COMPOUND,
                PersistentPlayerDataContainerImpl::toTagCompound
            ) {
                PersistentPlayerDataContainerImpl().apply {
                    it.forEach { (key, value) ->
                        put(key, value)
                    }
                }
            }

            List::class.java -> createAdapter(
                List::class.java,
                ListBinaryTag::class.java,
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

            else -> error("Could not find a valid TagAdapter implementation for the requested type ${type.simpleName}")
        }
    }

    private fun <T : Any, Z : BinaryTag> createAdapter(
        primitiveType: Class<T>,
        tagType: Class<Z>,
        nbtBaseType: BinaryTagType<Z>,
        builder: (T) -> Z,
        extractor: (Z) -> T
    ): TagAdapter<T, Z> = createAdapter(
        primitiveType,
        tagType,
        nbtBaseType,
        { _, value -> builder(value) },
        { _, value -> extractor(value) },
        { _, tag -> nbtBaseType.test(tag.type()) }
    )

    private fun <T : Any, Z : BinaryTag> createAdapter(
        primitiveType: Class<T>,
        tagType: Class<Z>,
        nbtBaseType: BinaryTagType<Z>,
        builder: (PersistentPlayerDataType<T, *>, T) -> Z,
        extractor: (PersistentPlayerDataType<T, *>, Z) -> T,
        matcher: (PersistentPlayerDataType<T, *>, BinaryTag) -> Boolean
    ): TagAdapter<T, Z> =
        TagAdapter(primitiveType, tagType, nbtBaseType, builder, extractor, matcher)


    fun <P : Any, C> isInstanceOf(type: PersistentPlayerDataType<P, C>, tag: BinaryTag): Boolean {
        return getOrCreateAdapter<P, BinaryTag>(type).isInstance(type, tag)
    }

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

    private fun <P : Any> constructList(
        type: PersistentPlayerDataType<List<P>, *>,
        list: List<P>
    ): ListBinaryTag {
        check(type is ListPersistentPlayerDataType<*, *>) { "The passed list cannot be written to the PDC with a ${type::class.simpleName} (expected a list data type)" }
        val listType = type as ListPersistentPlayerDataType<P, *>
        val elementType = listType.elementType
        val elementAdapter = getOrCreateAdapter<P, BinaryTag>(elementType)

        val builder = ListBinaryTag.builder(elementAdapter.nbtBaseType, list.size)
        for (element in list) {
            builder.add(wrap(elementType, element))
        }

        return builder.build()
    }

    private fun <P : Any> extractList(
        type: PersistentPlayerDataType<P, *>,
        listTag: ListBinaryTag
    ): List<P> {
        check(type is ListPersistentPlayerDataType<*, *>) { "The found list tag cannot be read with a ${type::class.simpleName} (expected a list data type)" }
        val listType = type as ListPersistentPlayerDataType<P, *>
        val elementType = listType.elementType
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
        val primitiveType: Class<P>,
        val tagType: Class<T>,
        val nbtBaseType: BinaryTagType<T>,
        val builder: (PersistentPlayerDataType<P, *>, P) -> T,
        val extractor: (PersistentPlayerDataType<P, *>, T) -> P,
        val matcher: (PersistentPlayerDataType<P, *>, BinaryTag) -> Boolean
    ) {
        fun extract(dataType: PersistentPlayerDataType<P, *>, base: BinaryTag): P {
            require(nbtBaseType.test(base.type())) { "The provided NBTBase was of the type ${base.type()}. Expected type $nbtBaseType" }
            return extractor(dataType, tagType.cast(base))
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