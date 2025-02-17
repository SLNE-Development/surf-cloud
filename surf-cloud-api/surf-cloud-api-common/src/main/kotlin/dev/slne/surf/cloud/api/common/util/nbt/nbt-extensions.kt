package dev.slne.surf.cloud.api.common.util.nbt

import net.querz.nbt.io.NBTDeserializer
import net.querz.nbt.io.NBTSerializer
import net.querz.nbt.io.NamedTag
import net.querz.nbt.tag.CompoundTag
import net.querz.nbt.tag.ListTag
import net.querz.nbt.tag.Tag
import java.nio.file.Path
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

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

fun Tag<*>.writeToPath(path: Path, compressed: Boolean = true) {
    val tag = NamedTag(null, this)
    tag.writeToPath(path, compressed)
}

fun NamedTag.writeToPath(path: Path, compressed: Boolean = true) {
    path.outputStream().use { stream -> NBTSerializer(compressed).toStream(this, stream) }
}

fun Path.readTag(compressed: Boolean = true): NamedTag =
    inputStream().use { stream -> NBTDeserializer(compressed).fromStream(stream) }

fun Path.readCompoundTag(compressed: Boolean = true): CompoundTag =
    readTag(compressed).tag as? CompoundTag ?: error("Expected a compound tag")
