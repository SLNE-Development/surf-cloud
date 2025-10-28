package dev.slne.surf.cloud.core.common.player.ppdc

import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.api.common.player.ppdc.PersistentPlayerDataContainer
import dev.slne.surf.cloud.api.common.player.ppdc.PersistentPlayerDataType
import dev.slne.surf.cloud.core.common.player.ppdc.network.PdcOp
import dev.slne.surf.cloud.core.common.player.ppdc.network.PdcPatch
import dev.slne.surf.surfapi.core.api.nbt.FastCompoundBinaryTag
import dev.slne.surf.surfapi.core.api.nbt.fast
import net.kyori.adventure.key.Key
import net.kyori.adventure.nbt.*
import java.util.*

open class PersistentPlayerDataContainerImpl(
    tag: FastCompoundBinaryTag = CompoundBinaryTag.empty().fast()
) : PersistentPlayerDataContainerViewImpl(), PersistentPlayerDataContainer {
    @Volatile
    var tag: FastCompoundBinaryTag = tag
        private set

    override fun getTag(key: String) = tag.get(key)

    private inline fun <reified T : BinaryTag> getTag(key: Key): T? {
        return tag.get(key.asString()) as? T
    }

    override fun <P : Any, C> set(
        key: Key,
        type: PersistentPlayerDataType<P, C>,
        value: C
    ) {
        tag.put(
            key.asString(),
            PersistentPlayerDataTypeRegistry.wrap(
                type,
                type.toPrimitive(value, PersistentPlayerDataAdapterContextImpl)
            )
        )
    }

    fun put(key: String, value: BinaryTag) {
        tag.put(key, value)
    }

    override fun setBoolean(key: Key, value: Boolean) {
        tag.putBoolean(key.asString(), value)
    }

    override fun setByte(key: Key, value: Byte) {
        tag.putByte(key.asString(), value)
    }

    override fun setShort(key: Key, value: Short) {
        tag.putShort(key.asString(), value)
    }

    override fun setInt(key: Key, value: Int) {
        tag.putInt(key.asString(), value)
    }

    override fun setLong(key: Key, value: Long) {
        tag.putLong(key.asString(), value)
    }

    override fun setFloat(key: Key, value: Float) {
        tag.putFloat(key.asString(), value)
    }

    override fun setDouble(key: Key, value: Double) {
        tag.putDouble(key.asString(), value)
    }

    override fun setString(key: Key, value: String) {
        tag.putString(key.asString(), value)
    }

    override fun setByteArray(key: Key, value: ByteArray) {
        tag.putByteArray(key.asString(), value)
    }

    override fun setIntArray(key: Key, value: IntArray) {
        tag.putIntArray(key.asString(), value)
    }

    override fun setLongArray(key: Key, value: LongArray) {
        tag.putLongArray(key.asString(), value)
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

    override fun remove(key: Key) {
        tag.remove(key.asString())
    }

    override val empty: Boolean
        get() = tag.size() == 0

    override fun toTagCompound(): CompoundBinaryTag = tag.fast()

    override fun readFromBuf(buf: SurfByteBuf) {
        tag = buf.readCompoundTag().fast()
    }

    override fun snapshot(): PersistentPlayerDataContainerImpl {
        return PersistentPlayerDataContainerImpl(tag.fast())
    }

    fun fromTagCompound(tag: CompoundBinaryTag) {
        this.tag = tag.fast()
    }

    fun applyOps(root: FastCompoundBinaryTag, patch: PdcPatch) {
        for (op in patch.ops) {
            when (op) {
                is PdcOp.Remove -> removeAtPath(root, op.path)
                is PdcOp.Put -> putAtPath(root, op.path, op.value)
            }
        }
    }

    private fun putAtPath(root: FastCompoundBinaryTag, path: List<String>, value: BinaryTag) {
        if (path.isEmpty()) {
            require(value is CompoundBinaryTag) { "root replace expects CompoundBinaryTag, but was ${value::class.simpleName}" }
            root.clear()
            root.put(value)
        } else {
            val parent = traverseCompoundPath(root, path.dropLast(1), createIfMissing = true)!!
            val key = path.last()

            if (value is CompoundBinaryTag && value.isEmpty) {
                parent.remove(key)
                pruneEmptyAncestors(root, path.dropLast(1))
            } else {
                parent.put(key, value)
            }
        }
    }

    private fun removeAtPath(root: FastCompoundBinaryTag, path: List<String>) {
        if (path.isEmpty()) {
            root.clear()
        } else {
            val parent =
                traverseCompoundPath(root, path.dropLast(1), createIfMissing = false) ?: return
            parent.remove(path.last())
            pruneEmptyAncestors(root, path.dropLast(1))
        }
    }

    private fun traverseCompoundPath(
        root: FastCompoundBinaryTag,
        path: List<String>,
        createIfMissing: Boolean = true
    ): FastCompoundBinaryTag? {
        var current = root
        for (segment in path) {
            val existing = current.getCompound(segment, null)
            val child = when {
                existing != null -> existing as? FastCompoundBinaryTag ?: existing.fast()
                createIfMissing -> CompoundBinaryTag.empty().fast()
                else -> return null
            }

            current.put(segment, child)
            current = child
        }
        return current
    }

    private fun pruneEmptyAncestors(
        root: FastCompoundBinaryTag,
        pathToDeepestParent: List<String>
    ) {
        if (pathToDeepestParent.isEmpty()) return

        val stack = Stack<Pair<FastCompoundBinaryTag, String>>()
        var current: FastCompoundBinaryTag = root

        for (segment in pathToDeepestParent) {
            val childTag = current.getCompound(segment, null) ?: return
            val childFast = childTag as? FastCompoundBinaryTag ?: childTag.fast()

            current.put(segment, childFast)
            stack.push(current to segment)
            current = childFast
        }

        while (stack.isNotEmpty()) {
            val (parent, key) = stack.pop()
            val child = parent.getCompound(key, null) ?: continue
            if (child.size() == 0) {
                parent.remove(key)
            } else {
                break
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PersistentPlayerDataContainerImpl) return false

        if (tag != other.tag) return false

        return true
    }

    override fun hashCode(): Int {
        return tag.hashCode()
    }

    override fun toString(): String {
        return "PersistentPlayerDataContainerImpl(tag=$tag)"
    }
}