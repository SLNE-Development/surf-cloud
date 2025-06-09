package dev.slne.surf.cloud.core.common.data

import dev.slne.surf.cloud.api.common.util.nbt.readCompoundTag
import dev.slne.surf.cloud.api.common.util.nbt.writeToPath
import dev.slne.surf.cloud.core.common.coreCloudInstance
import net.querz.nbt.tag.CompoundTag
import net.querz.nbt.tag.Tag
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
                CompoundTag().writeToPath(this)
            }
        }
    }

    val tag by lazy { file.readCompoundTag() }
    private fun saveTag() = tag.writeToPath(file)

    fun <T : Tag<D>, D> data(
        key: String,
        type: Class<T>,
        toValue: (T) -> D,
        toTag: (D) -> T,
        defaultValue: D?
    ): PersistentData<D> {
        return DataImpl(tag, key, toValue, toTag, type, defaultValue)
    }

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
