package dev.slne.surf.cloud.api.common.player.cache.types

import org.gradle.internal.impldep.it.unimi.dsi.fastutil.objects.Object2ObjectMap
import java.util.concurrent.ConcurrentMap

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