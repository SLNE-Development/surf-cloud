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
        tag = buf.readCompoundTag().fast(synchronize = true)
    }

    fun fromTagCompound(tag: CompoundBinaryTag) {
        this.tag = tag.fast(synchronize = true)
    }

    fun applyOps(root: FastCompoundBinaryTag, patch: PdcPatch) {
        for (op in patch.ops) {
            when (op) {
                is PdcOp.Remove -> removeAtPath(root, op.path)
                is PdcOp.Put -> putAtPath(root, op.path, op.value)
            }
        }
    }

    private fun ensureCompoundAtPath(
        root: FastCompoundBinaryTag,
        path: List<String>
    ): FastCompoundBinaryTag {
        var cur: FastCompoundBinaryTag = root
        for (p in path) {
            val next = (cur.get(p) as? CompoundBinaryTag)?.fast()
            if (next == null) {
                val created = CompoundBinaryTag.empty().fast()
                cur.put(p, created.fast())
                cur = created
            } else {
                cur = next
            }
        }
        return cur
    }

    private fun putAtPath(root: FastCompoundBinaryTag, path: List<String>, value: BinaryTag) {
        if (path.isEmpty()) {
            root.clear()
            root.put((value as CompoundBinaryTag))
            return
        }
        val parent = ensureCompoundAtPath(root, path.dropLast(1))
        parent.put(path.last(), value)
    }

    private fun removeAtPath(root: FastCompoundBinaryTag, path: List<String>) {
        if (path.isEmpty()) {
            root.clear()
            return
        }
        val parent = ensureCompoundAtPath(root, path.dropLast(1))
        parent.remove(path.last())
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