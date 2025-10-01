package dev.slne.surf.cloud.api.common.player.cache

interface CacheStructured<T : Any, D : Any> : DirtyMarkable {
    var value: T
    val type: StructuredType<T, D>

    fun emit(delta: D)
}