package dev.slne.surf.cloud.core.common.data

import dev.slne.surf.cloud.core.common.coreCloudInstance
import net.querz.nbt.io.NBTUtil
import net.querz.nbt.tag.CompoundTag
import net.querz.nbt.tag.Tag
import java.io.File


internal object PersistentDataImpl {
    private val file: File by lazy {
        coreCloudInstance.dataFolder.resolve("data.dat").toFile().apply {
            if (!exists()) {
                parentFile?.mkdirs()
                createNewFile()
                NBTUtil.write(CompoundTag(), this, true)
            }
        }
    }

    private val tag: CompoundTag by lazy { NBTUtil.read(file, true).tag as CompoundTag }
    private fun saveTag() = NBTUtil.write(tag, file, true)

    fun <T : Tag<D>, D> data(
        key: String,
        type: Class<T>,
        toValue: (T) -> D,
        toTag: (D) -> T,
        defaultValue: D?
    ): PersistentData<D> {
        return DataImpl(tag, key, toValue, toTag, type, defaultValue)
    }


    @JvmRecord
    private data class DataImpl<T : Tag<D>, D>(
        val tag: CompoundTag,
        val key: String,
        val toValue: (T) -> D,
        val toTag: (D) -> T,
        val type: Class<T>,
        val defaultValue: D?
    ) : PersistentData<D> {
        override fun value(): D? = tag.get(key, type)?.let { toValue(it) } ?: defaultValue
        override fun setValue(value: D?) {
            if (value == null) {
                tag.remove(key)
            } else {
                tag.put(key, toTag(value))
            }
            saveTag()
        }

       override operator fun contains(key: String): Boolean = tag.containsKey(key)
    }
}
