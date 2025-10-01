package dev.slne.surf.cloud.api.common.player.cache

typealias CacheValueListener<T> = (oldValue: T?, newValue: T?) -> Unit
typealias CacheSetListener<T> = (added: Boolean, element: T) -> Unit
typealias CacheListListener<T> = (added: Boolean, index: Int, element: T) -> Unit
typealias CacheMapListener<K, V> = (removed: Boolean, key: K, value: V?) -> Unit