package dev.slne.surf.cloud.api.common.player.cache.types

import it.unimi.dsi.fastutil.objects.ObjectArrayList

interface CacheList<E : Any> : MutableList<E>, DeltaBacked {

    fun snapshot(): ObjectArrayList<E>
}