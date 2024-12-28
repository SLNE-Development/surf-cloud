package dev.slne.surf.cloud.api.common.util.nbt

import net.querz.nbt.tag.CompoundTag
import net.querz.nbt.tag.ListTag
import net.querz.nbt.tag.Tag

/**
 * Extension function to put a tag into a compound tag.
 *
 * @param key the key to put the tag under
 * @param value the tag to put
 * @return the previous tag under the key, if any
 * @see CompoundTag.put
 */
operator fun CompoundTag.set(key: String, value: Tag<*>): Tag<*>? = put(key, value)

operator fun CompoundTag.set(key: String, value: Boolean): Tag<*>? = putBoolean(key, value)
operator fun CompoundTag.set(key: String, value: Byte): Tag<*>? = putByte(key, value)
operator fun CompoundTag.set(key: String, value: Short): Tag<*>? = putShort(key, value)
operator fun CompoundTag.set(key: String, value: Int): Tag<*>? = putInt(key, value)
operator fun CompoundTag.set(key: String, value: Long): Tag<*>? = putLong(key, value)
operator fun CompoundTag.set(key: String, value: Float): Tag<*>? = putFloat(key, value)
operator fun CompoundTag.set(key: String, value: Double): Tag<*>? = putDouble(key, value)
operator fun CompoundTag.set(key: String, value: String): Tag<*>? = putString(key, value)
operator fun CompoundTag.set(key: String, value: ByteArray): Tag<*>? = putByteArray(key, value)
operator fun CompoundTag.set(key: String, value: IntArray): Tag<*>? = putIntArray(key, value)
operator fun CompoundTag.set(key: String, value: LongArray): Tag<*>? = putLongArray(key, value)


fun ListTag<*>.getCompound(index: Int): CompoundTag {
    if (index >= 0 && index < size()) {
        val tag = get(index)
        if (tag.id == CompoundTag.ID) {
            return tag as CompoundTag
        }
    }

    return CompoundTag()
}