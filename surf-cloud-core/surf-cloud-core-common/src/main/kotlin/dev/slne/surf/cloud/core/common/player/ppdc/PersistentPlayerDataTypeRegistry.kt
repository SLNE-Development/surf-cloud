package dev.slne.surf.cloud.core.common.player.ppdc

import com.google.common.primitives.Primitives
import dev.slne.surf.cloud.api.common.player.ppdc.ListPersistentPlayerDataType
import dev.slne.surf.cloud.api.common.player.ppdc.PersistentPlayerDataContainer
import dev.slne.surf.cloud.api.common.player.ppdc.PersistentPlayerDataType
import dev.slne.surf.cloud.api.common.util.mutableObject2ObjectMapOf
import dev.slne.surf.cloud.api.common.util.mutableObjectListOf
import dev.slne.surf.cloud.api.common.util.nbt.getCompound
import dev.slne.surf.cloud.api.common.util.synchronize
import net.querz.nbt.tag.*
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
        if (Byte::class == type) {
            return createAdapter(
                Byte::class,
                ByteTag::class,
                ByteTag.ID,
                ::ByteTag
            ) { it.asByte() }
        } else if (Short::class == type) {
            return createAdapter(
                Short::class,
                ShortTag::class,
                ShortTag.ID,
                ::ShortTag
            ) { it.asShort() }
        } else if (Int::class == type) {
            return createAdapter(
                Int::class,
                IntTag::class,
                IntTag.ID,
                ::IntTag
            ) { it.asInt() }
        } else if (Long::class == type) {
            return createAdapter(
                Long::class,
                LongTag::class,
                LongTag.ID,
                ::LongTag
            ) { it.asLong() }
        } else if (Float::class == type) {
            return createAdapter(
                Float::class,
                FloatTag::class,
                FloatTag.ID,
                ::FloatTag
            ) { it.asFloat() }
        } else if (Double::class == type) {
            return createAdapter(
                Double::class,
                DoubleTag::class,
                DoubleTag.ID,
                ::DoubleTag
            ) { it.asDouble() }
        } else if (Boolean::class == type) {
            return createAdapter(
                Boolean::class,
                ByteTag::class,
                ByteTag.ID,
                { if (it) ByteTag(1) else ByteTag(0) }
            ) { it.asByte() == 1.toByte() }
        } else if (Char::class == type) {
            return createAdapter(
                Char::class,
                IntTag::class,
                IntTag.ID,
                { IntTag(it.code) }
            ) { it.asInt().toChar() }
        } else if (String::class == type) {
            return createAdapter(
                String::class,
                StringTag::class,
                StringTag.ID,
                ::StringTag
            ) { it.value }
        }
        // endregion
        // region Primitive non-list arrays
        else if (ByteArray::class == type) {
            return createAdapter(
                ByteArray::class,
                ByteArrayTag::class,
                ByteArrayTag.ID,
                { ByteArrayTag(it.copyOf()) },
                { it.value.copyOf() }
            )
        } else if (IntArray::class == type) {
            return createAdapter(
                IntArray::class,
                IntArrayTag::class,
                IntArrayTag.ID,
                { IntArrayTag(it.copyOf()) },
                { it.value.copyOf() }
            )
        } else if (LongArray::class == type) {
            return createAdapter(
                LongArray::class,
                LongArrayTag::class,
                LongArrayTag.ID,
                { LongArrayTag(it.copyOf()) },
                { it.value.copyOf() }
            )
        } else if (BooleanArray::class == type) {
            return createAdapter(
                BooleanArray::class,
                ByteArrayTag::class,
                ByteArrayTag.ID,
                { ByteArrayTag(it.map { if (it) 1.toByte() else 0.toByte() }.toByteArray()) },
                { it.value.map { it == 1.toByte() }.toBooleanArray() }
            )
        } else if (CharArray::class == type) {
            return createAdapter(
                CharArray::class,
                IntArrayTag::class,
                IntArrayTag.ID,
                { IntArrayTag(it.map { it.code }.toIntArray()) },
                { it.value.map { it.toChar() }.toCharArray() }
            )
        }
        // endregion

        else if (Array<PersistentPlayerDataContainer>::class == type) {
            return createAdapter(
                Array<PersistentPlayerDataContainer>::class,
                ListTag::class,
                ListTag.ID,
                {
                    ListTag(Tag::class.java).apply {
                        it.forEach { add((it as PersistentPlayerDataContainerImpl).toTagCompound()) }
                    }
                }, {
                    it.mapIndexed { index, _ ->
                        val container = PersistentPlayerDataContainerImpl()
                        val compound = it.getCompound(index)
                        compound.forEach { key, value -> container.put(key, value) }

                        container
                    }.toTypedArray()
                }
            )
        } else if (PersistentPlayerDataContainer::class == type) {
            return createAdapter(
                PersistentPlayerDataContainerImpl::class,
                CompoundTag::class,
                CompoundTag.ID,
                { it.toTagCompound() },
                {
                    PersistentPlayerDataContainerImpl().apply {
                        it.forEach { key, value ->
                            put(key, value)
                        }
                    }
                }
            )
        } else if (List::class == type) {
            @Suppress("UNCHECKED_CAST")
            return createAdapter(
                List::class,
                ListTag::class,
                ListTag.ID,
                { type, value -> constructList(type as PersistentPlayerDataType<List<T>, *>, value as List<T>) },
                this::extractList,
                this::matchesListTag
            )
        }

        error("Could not find a valid TagAdapter implementation for the requested type ${type.simpleName}")
    }

    private fun <T : Any, Z : Tag<*>> createAdapter(
        primitiveType: KClass<T>,
        nbtBaseType: KClass<Z>,
        serializedTypeByte: Byte,
        builder: (T) -> Z,
        extractor: (Z) -> T
    ): TagAdapter<T, Z> {
        return createAdapter(
            primitiveType,
            nbtBaseType,
            serializedTypeByte,
            { _, value -> builder(value) },
            { _, value -> extractor(value) },
            { _, tag -> nbtBaseType.isInstance(tag) }
        )
    }

    private fun <T : Any, Z : Tag<*>> createAdapter(
        primitiveType: KClass<T>,
        nbtBaseType: KClass<Z>,
        serializedTypeByte: Byte,
        builder: (PersistentPlayerDataType<T, *>, T) -> Z,
        extractor: (PersistentPlayerDataType<T, *>, Z) -> T,
        matcher: (PersistentPlayerDataType<T, *>, Tag<*>) -> Boolean
    ): TagAdapter<T, Z> {
        return TagAdapter(primitiveType, nbtBaseType, builder, extractor, matcher)
    }

    fun <P : Any, C> isInstanceOf(type: PersistentPlayerDataType<P, C>, tag: Tag<*>): Boolean {
        return getOrCreateAdapter<P, Tag<*>>(type).isInstance(type, tag)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any, Z : Tag<*>> getOrCreateAdapter(type: PersistentPlayerDataType<T, *>): TagAdapter<T, Z> {
        return adapters.computeIfAbsent(type.primitiveType, createAdapter) as TagAdapter<T, Z>
    }

    fun <T : Any> wrap(type: PersistentPlayerDataType<T, *>, value: T): Tag<*> {
        return getOrCreateAdapter<T, Tag<*>>(type).build(type, value)
    }

    fun<T : Any, Z: Tag<*>> extract(type: PersistentPlayerDataType<T, *>, tag: Tag<*>): T {
        val primitiveType = type.primitiveType
        val adapter = getOrCreateAdapter<T, Z>(type)
        require(adapter.isInstance(type, tag)) { "The found tag instance (${tag::class.simpleName}) cannot store ${primitiveType.simpleName}" }

        val foundValue = adapter.extract(type, tag)
        require(primitiveType.isInstance(foundValue)) { "The found object is of the type ${foundValue::class.simpleName}. Expected type ${primitiveType.simpleName}" }
        return primitiveType.cast(foundValue)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <P : Any, T : List<P>> constructList(
        type: PersistentPlayerDataType<T, *>,
        list: List<P>
    ): ListTag<*> {
        check(type is ListPersistentPlayerDataType<*, *>) { "The passed list cannot be written to the PDC with a ${type::class.simpleName} (expected a list data type)" }
        val type = type as ListPersistentPlayerDataType<P, *>
        val elementType = type.elementType

        getOrCreateAdapter<P, Tag<*>>(elementType)
        val values = list.map { wrap(elementType, it) }

        return ListTag(Tag::class.java).apply { addAll(values) }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <P : Any> extractList(type: PersistentPlayerDataType<P, *>, listTag: ListTag<*>): List<P> {
        check(type is ListPersistentPlayerDataType<*, *>) { "The found list tag cannot be read with a ${type::class.simpleName} (expected a list data type)" }
        val type = type as ListPersistentPlayerDataType<P, *>
        val elementType = type.elementType
        val output = mutableObjectListOf<P>(listTag.size())

        for (tag in listTag) {
            output.add(extract<P, Tag<*>>(elementType, tag))
        }

        return output
    }


    private fun matchesListTag(type: PersistentPlayerDataType<List<*>, *>, tag: Tag<*>): Boolean {
        if (type !is ListPersistentPlayerDataType<*, *>) {
            return false
        }

        if (tag !is ListTag<*>) {
            return false
        }

        val elementType = tag.typeClass
        val elementAdapter: TagAdapter<out Any, Tag<*>> = getOrCreateAdapter(type.elementType)

        return elementAdapter.nbtBaseType == elementType.kotlin || elementType == EndTag::class.java
    }

    internal data class TagAdapter<P : Any, T : Tag<*>>(
        val primitiveType: KClass<P>,
        val nbtBaseType: KClass<T>,
        val builder: (PersistentPlayerDataType<P, *>, P) -> T,
        val extractor: (PersistentPlayerDataType<P, *>, T) -> P,
        val matcher: (PersistentPlayerDataType<P, *>, Tag<*>) -> Boolean
    ) {
        fun extract(dataType: PersistentPlayerDataType<P, *>, base: Tag<*>): P {
            require(nbtBaseType.isInstance(base)) { "The provided NBTBase was of the type ${base::class.simpleName}. Expected type ${nbtBaseType.simpleName}" }
            return extractor(dataType, nbtBaseType.cast(base))
        }

        fun build(dataType: PersistentPlayerDataType<P, *>, value: Any): T {
            require(primitiveType.isInstance(value)) { "The provided value was of the type ${value::class.simpleName}. Expected type ${primitiveType.simpleName}" }
            return builder(dataType, primitiveType.cast(value))
        }

        fun isInstance(
            persistentDataType: PersistentPlayerDataType<P, *>,
            base: Tag<*>
        ): Boolean = matcher(persistentDataType, base)

    }
}