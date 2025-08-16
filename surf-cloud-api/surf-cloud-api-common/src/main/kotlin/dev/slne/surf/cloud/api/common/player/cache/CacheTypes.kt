package dev.slne.surf.cloud.api.common.player.cache

import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectList
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import org.gradle.internal.impldep.it.unimi.dsi.fastutil.objects.Object2ObjectMap
import java.util.concurrent.ConcurrentMap

interface DeltaBacked {
    var suppressOutbound: Boolean
}

interface CacheList<E : Any> : ObjectList<E>, DeltaBacked {
    fun snapshot(): ObjectArrayList<E>
}

interface CacheSet<E : Any> : MutableSet<E>, DeltaBacked {
    fun snapshot(): ObjectOpenHashSet<E>
}

interface CacheMap<K : Any, V : Any> : ConcurrentMap<K, V>, DeltaBacked {
    fun snapshot(): Object2ObjectMap<K, V>

    // TODO: 13.08.2025 18:38 - support these methods in a better way
//    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
//        get() = throw UnsupportedOperationException("Use put/remove/clear on CacheMap")
//    override val keys: MutableSet<K>
//        get() = throw UnsupportedOperationException("Use put/remove/clear on CacheMap")
//    override val values: MutableCollection<V>
//        get() = throw UnsupportedOperationException("Use put/remove/clear on CacheMap")
}