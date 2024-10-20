package dev.slne.surf.cloud.api.util

import it.unimi.dsi.fastutil.objects.*
import org.jetbrains.annotations.Unmodifiable
import org.jetbrains.annotations.UnmodifiableView

// region ObjectSet
fun <T> mutableObjectSetOf(vararg elements: T) = ObjectOpenHashSet(elements)
fun <T> mutableObjectSetOf() = ObjectOpenHashSet<T>()
fun <T> objectSetOf(vararg elements: T) = mutableObjectSetOf(elements).freeze()
fun <T> objectSetOf() = emptyObjectSet<T>()
fun <T> emptyObjectSet(): @Unmodifiable ObjectSet<T> = ObjectSets.emptySet()
fun <T> ObjectSet<T>.synchronize(): ObjectSet<T> = ObjectSets.synchronize(this)
fun <T> ObjectSet<T>.freeze(): @UnmodifiableView ObjectSet<T> = ObjectSets.unmodifiable(this)
fun <T> Sequence<T>.toMutableObjectSet() = ObjectOpenHashSet(toList())
fun <T> Sequence<T>.toObjectSet() = toMutableObjectSet().freeze()
// endregion

// region ObjectMap
// region Object2ObjectMap
fun <K, V> mutableObject2ObjectMapOf(vararg pairs: Pair<K, V>) = Object2ObjectOpenHashMap<K, V>(pairs.size).apply { putAll(pairs) }
fun <K, V> mutableObject2ObjectMapOf() = Object2ObjectOpenHashMap<K, V>()
fun <K, V> object2ObjectMapOf(vararg pairs: Pair<K, V>) = mutableObject2ObjectMapOf(*pairs).freeze()
fun <K, V> object2ObjectMapOf() = emptyObject2ObjectMap<K, V>()
fun <K, V> emptyObject2ObjectMap(): @Unmodifiable Object2ObjectMap<K, V> = Object2ObjectMaps.emptyMap()
fun <K, V> Object2ObjectOpenHashMap<K, V>.synchronize(): Object2ObjectMap<K, V> = Object2ObjectMaps.synchronize(this)
fun <K, V> Object2ObjectOpenHashMap<K, V>.freeze(): @UnmodifiableView Object2ObjectMap<K, V> = Object2ObjectMaps.unmodifiable(this)

fun <K, V> mutableObject2MultiObjectsMapOf(vararg pairs: Pair<K, ObjectSet<V>>) = Object2ObjectOpenHashMap<K, ObjectSet<V>>(pairs.size).apply { putAll(pairs) }
fun <K, V> mutableObject2MultiObjectsMapOf() = Object2ObjectOpenHashMap<K, ObjectSet<V>>()
fun <K, V> object2MultiObjectsMapOf(vararg pairs: Pair<K, ObjectSet<V>>) = mutableObject2MultiObjectsMapOf(*pairs).freeze()
fun <K, V> object2MultiObjectsMapOf() = emptyObject2MultiObjectsMap<K, V>()
fun <K, V> emptyObject2MultiObjectsMap(): @Unmodifiable Object2MultiObjectsMap<K, V> = Object2ObjectMaps.emptyMap()
fun <K, V> Object2ObjectOpenHashMap<K, ObjectSet<V>>.synchronize(): Object2MultiObjectsMap<K, V> = Object2ObjectMaps.synchronize(this)
typealias Object2MultiObjectsMap<K, V> = Object2ObjectMap<K, ObjectSet<V>>
fun <K, V> Object2MultiObjectsMap<K, V>.add(key: K, value: V) {
    val set = get(key) ?: mutableObjectSetOf<V>().also { put(key, it) }
    set.add(value)
}
// endregion
// endregion