package dev.slne.surf.cloud.api.common.player.cache.types

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet

interface CacheSet<E : Any> : MutableSet<E>, DeltaBacked {
    fun snapshot(): ObjectOpenHashSet<E>
}