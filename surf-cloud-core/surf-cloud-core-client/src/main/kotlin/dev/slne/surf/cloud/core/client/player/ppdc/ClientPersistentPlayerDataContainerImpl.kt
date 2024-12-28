package dev.slne.surf.cloud.core.client.player.ppdc

import dev.slne.surf.cloud.core.common.player.ppdc.PersistentPlayerDataContainerViewImpl
import net.querz.nbt.tag.CompoundTag
import net.querz.nbt.tag.Tag

class ClientPersistentPlayerDataContainerImpl(
    var tag: CompoundTag
) : PersistentPlayerDataContainerViewImpl() {
    override fun toTagCompound() = tag
    override fun getTag(key: String): Tag<*>? = tag[key]
}