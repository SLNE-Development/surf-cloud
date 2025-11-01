package dev.slne.surf.cloud.core.common.player.ppdc

import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.api.common.player.ppdc.PersistentPlayerDataAdapterContext
import dev.slne.surf.cloud.api.common.player.ppdc.PersistentPlayerDataContainerView
import dev.slne.surf.cloud.api.common.player.ppdc.PersistentPlayerDataType
import dev.slne.surf.cloud.api.common.util.toObjectSet
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectSet
import net.kyori.adventure.key.Key
import net.kyori.adventure.nbt.*
import org.jetbrains.annotations.Unmodifiable
import java.util.*

abstract class PersistentPlayerDataContainerViewImpl : PersistentPlayerDataContainerView {

    abstract fun toTagCompound(): CompoundBinaryTag
    abstract fun getTag(key: String): BinaryTag?

    override fun <P : Any, C> has(
        key: Key,
        type: PersistentPlayerDataType<P, C>
    ): Boolean {
        val value = getTag(key.asString()) ?: return false
        return PersistentPlayerDataTypeRegistry.isInstanceOf(type, value)
    }

    override fun has(key: Key): Boolean {
        return getTag(key.asString()) != null
    }

    override fun <P : Any, C> get(
        key: Key,
        type: PersistentPlayerDataType<P, C>
    ): C? {
        val value = getTag(key.asString()) ?: return null

        return type.fromPrimitive(
            PersistentPlayerDataTypeRegistry.extract<P, BinaryTag>(type, value),
            PersistentPlayerDataAdapterContextImpl
        )
    }

    private inline fun <reified T : BinaryTag> getTag(key: Key): T? {
        return getTag(key.asString()) as? T
    }

    override fun getBoolean(key: Key): Boolean? {
        val tag = getTag<ByteBinaryTag>(key) ?: return null
        return tag.value() != 0.toByte()
    }

    override fun getNumber(key: Key): Number? {
        return getTag<NumberBinaryTag>(key)?.numberValue()
    }

    override fun getByte(key: Key): Byte? {
        return getTag<ByteBinaryTag>(key)?.value()
    }

    override fun getShort(key: Key): Short? {
        return getTag<ShortBinaryTag>(key)?.value()
    }

    override fun getInt(key: Key): Int? {
        return getTag<IntBinaryTag>(key)?.value()
    }

    override fun getLong(key: Key): Long? {
        return getTag<LongBinaryTag>(key)?.value()
    }

    override fun getFloat(key: Key): Float? {
        return getTag<FloatBinaryTag>(key)?.value()
    }

    override fun getDouble(key: Key): Double? {
        return getTag<DoubleBinaryTag>(key)?.value()
    }

    override fun getString(key: Key): String? {
        return getTag<StringBinaryTag>(key)?.value()
    }

    override fun getByteArray(key: Key): ByteArray? {
        return getTag<ByteArrayBinaryTag>(key)?.value()
    }

    override fun getIntArray(key: Key): IntArray? {
        return getTag<IntArrayBinaryTag>(key)?.value()
    }

    override fun getLongArray(key: Key): LongArray? {
        return getTag<LongArrayBinaryTag>(key)?.value()
    }

    override val keys: @Unmodifiable ObjectSet<Key>
        get() = toTagCompound().keySet().asSequence()
            .map { it.split(":", limit = 2) }
            .filter { it.size == 2 }
            .map { Key.key(it[0], it[1]) }
            .toObjectSet()


    override val empty: Boolean
        get() = toTagCompound().size() == 0

    override val adapterContext: PersistentPlayerDataAdapterContext
        get() = PersistentPlayerDataAdapterContextImpl

    override fun writeToBuf(buf: SurfByteBuf) {
        val root = toTagCompound()
        buf.writeCompoundTag(root)
    }

    override fun snapshot(): PersistentPlayerDataContainerViewImpl {
        val tag = deepCopy(toTagCompound())

        val tagCopy = CompoundBinaryTag.builder()
            .put(tag)
            .build()

        return object : PersistentPlayerDataContainerViewImpl() {
            override fun toTagCompound() = tagCopy
            override fun getTag(key: String) = tagCopy.get(key)
        }
    }


    /**
     * Creates a deep copy of the provided `CompoundBinaryTag` without using recursion.
     *
     * This function iterates through the tree structure of the `CompoundBinaryTag` in a non-recursive manner
     * to build a complete copy. It avoids stack overflow issues that can occur with deeply nested structures
     * when using a recursive approach.
     *
     * @param root The root `CompoundBinaryTag` to be deep copied.
     * @return A deep copy of the specified `CompoundBinaryTag`.
     */
    private fun deepCopy(root: CompoundBinaryTag): CompoundBinaryTag {
        data class Frame(
            val entries: List<Pair<String, BinaryTag>>,
            var idx: Int,
            val builder: CompoundBinaryTag.Builder,
            val parent: Frame?,
            val parentKey: String?
        )

        fun entriesOf(tag: CompoundBinaryTag): List<Pair<String, BinaryTag>> {
            val list = ObjectArrayList<Pair<String, BinaryTag>>(tag.size())
            tag.forEach { (k, v) -> list.add(k to v) }
            return list
        }

        val stack = ArrayDeque<Frame>()
        stack.addLast(
            Frame(
                entries = entriesOf(root),
                idx = 0,
                builder = CompoundBinaryTag.builder(),
                parent = null,
                parentKey = null
            )
        )

        var result: CompoundBinaryTag? = null

        while (stack.isNotEmpty()) {
            val top = stack.removeLast()

            while (top.idx < top.entries.size) {
                val (key, value) = top.entries[top.idx++]

                if (value is CompoundBinaryTag) {
                    stack.addLast(top)
                    stack.addLast(
                        Frame(
                            entries = entriesOf(value),
                            idx = 0,
                            builder = CompoundBinaryTag.builder(),
                            parent = top,
                            parentKey = key
                        )
                    )

                    break
                } else {
                    top.builder.put(key, value)
                }
            }


            if (top.idx >= top.entries.size) {
                val built = top.builder.build()
                if (top.parent == null) {
                    result = built
                } else {
                    val parent = top.parent
                    parent.builder.put(requireNotNull(top.parentKey), built)
                }
            }
        }

        return requireNotNull(result)
    }
}