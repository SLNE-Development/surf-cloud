package dev.slne.surf.cloud.api.common.util

import net.querz.nbt.tag.CompoundTag
import net.querz.nbt.tag.ListTag

fun ListTag<*>.getCompound(index: Int): CompoundTag {
    if (index >= 0 && index < size()) {
        val tag = get(index)
        if (tag.id == CompoundTag.ID) {
            return tag as CompoundTag
        }
    }

    return CompoundTag()
}