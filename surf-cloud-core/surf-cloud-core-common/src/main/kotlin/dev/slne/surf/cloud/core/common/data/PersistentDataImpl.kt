package dev.slne.surf.cloud.core.common.data

import dev.slne.surf.cloud.core.common.coreCloudInstance
import dev.slne.surf.surfapi.core.api.nbt.FastCompoundBinaryTag
import dev.slne.surf.surfapi.core.api.nbt.fast
import net.kyori.adventure.nbt.BinaryTag
import net.kyori.adventure.nbt.BinaryTagIO
import net.kyori.adventure.nbt.BinaryTagIO.Compression
import net.kyori.adventure.nbt.BinaryTagType
import net.kyori.adventure.nbt.CompoundBinaryTag
import kotlin.io.path.createFile
import kotlin.io.path.createParentDirectories
import kotlin.io.path.div
import kotlin.io.path.notExists


object PersistentDataImpl {
    private val file by lazy {
        (coreCloudInstance.dataFolder / "storage" / "data.dat").apply {
            if (notExists()) {
                createParentDirectories()
                createFile()
                BinaryTagIO.writer().write(CompoundBinaryTag.empty(), this, Compression.GZIP)
            }
        }
    }

    val tag by lazy { BinaryTagIO.unlimitedReader().read(file, Compression.GZIP).fast() }
    private fun saveTag() = BinaryTagIO.writer().write(tag, file, Compression.GZIP)

    fun <T : BinaryTag, D> data(
        key: String,
        type: BinaryTagType<T>,
        toValue: (T) -> D,
        toTag: (D) -> T,
        defaultValue: D?
    ): PersistentData<D> {
        return DataImpl(tag, key, toValue, toTag, type, defaultValue)
    }

    private data class DataImpl<T : BinaryTag, D>(
        val tag: FastCompoundBinaryTag,
        val key: String,
        val toValue: (T) -> D,
        val toTag: (D) -> T,
        val type: BinaryTagType<T>,
        val defaultValue: D?
    ) : PersistentData<D> {
        init {
            tag.get("")
        }

        override fun value(): D? {
            val tag = tag.get(key) ?: return defaultValue
            if (type.test(tag.type())) {
                return toValue(tag as T)
            }
            throw IllegalStateException("Tag at key '$key' is not of type ${type}, but ${tag.type()}")
        }

        override fun setValue(value: D?) {
            if (value == null) {
                tag.remove(key)
            } else {
                tag.put(key, toTag(value))
            }
            saveTag()
        }

        override operator fun contains(key: String): Boolean = tag.get(key) != null
    }
}
