package dev.slne.surf.cloud.api.common.player.cache

interface DirtyMarkable {
    fun markDirty()
    fun consumeDirty(): Boolean

}