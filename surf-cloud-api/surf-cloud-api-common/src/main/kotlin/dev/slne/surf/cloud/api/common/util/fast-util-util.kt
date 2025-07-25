package dev.slne.surf.cloud.api.common.util

import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import it.unimi.dsi.fastutil.ints.IntSet
import it.unimi.dsi.fastutil.ints.IntSets
import it.unimi.dsi.fastutil.longs.*
import it.unimi.dsi.fastutil.objects.*
import org.jetbrains.annotations.Unmodifiable
import org.jetbrains.annotations.UnmodifiableView

// region ObjectSet
fun <T> mutableObjectSetOf(vararg elements: T) = ObjectOpenHashSet(elements)
fun <T> mutableObjectSetOf() = ObjectOpenHashSet<T>()
fun <T> mutableObjectSetOf(capacity: Int) = ObjectOpenHashSet<T>(capacity)
fun <T> mutableObjectSetOf(iterable: Iterable<T>) = when (iterable) {
    is Collection -> ObjectOpenHashSet(iterable)
    else -> ObjectOpenHashSet<T>(iterable.iterator())
}

fun <T> objectSetOf(vararg elements: T): ObjectSet<out T> = when (elements.size) {
    0 -> emptyObjectSet<T>()
    1 -> ObjectSets.singleton(elements[0])
    else -> mutableObjectSetOf<T>(*elements).freeze()
}

fun <T> objectSetOf(collection: Iterable<T>) = mutableObjectSetOf<T>(collection).freeze()
fun <T> objectSetOf() = emptyObjectSet<T>()
fun <T> emptyObjectSet(): @Unmodifiable ObjectSet<T> = ObjectSets.emptySet()
fun <T> ObjectSet<T>.synchronize(): ObjectSet<T> = ObjectSets.synchronize(this)
fun <T> ObjectSet<T>.freeze(): @UnmodifiableView ObjectSet<T> =
    this as? ObjectSets.UnmodifiableSet ?: ObjectSets.unmodifiable(this)

fun <T> Sequence<T>.toMutableObjectSet() = ObjectOpenHashSet(iterator())
fun <T> Sequence<T>.toObjectSet() = toMutableObjectSet().freeze()
fun <T> Array<out T>.toObjectSet() = objectSetOf(*this)
fun <T> Iterable<T>.toObjectSet() = (this as? ObjectSet<T>)?.freeze() ?: objectSetOf(this)

// endregion
// region ObjectList
fun <T> mutableObjectListOf(vararg elements: T) = ObjectArrayList<T>().apply { addAll(elements) }
fun <T> mutableObjectListOf() = ObjectArrayList<T>()
fun <T> mutableObjectListOf(capacity: Int) = ObjectArrayList<T>(capacity)
fun <T> objectListOf(vararg elements: T) = mutableObjectListOf(*elements).freeze()
fun <T> objectListOf() = emptyObjectList<T>()
fun <T> emptyObjectList(): @Unmodifiable ObjectList<T> = ObjectLists.emptyList()
fun <T> ObjectArrayList<T>.synchronize(): ObjectList<T> = ObjectLists.synchronize(this)
fun <T> ObjectArrayList<T>.freeze(): @UnmodifiableView ObjectList<T> =
    ObjectLists.unmodifiable(this)

fun <T> Sequence<T>.toMutableObjectList() = ObjectArrayList<T>().apply { addAll(toList()) }
fun <T> Sequence<T>.toObjectList() = toMutableObjectList().freeze()
fun <T> Collection<T>.toObjectList() = this as? ObjectList<T> ?: ObjectArrayList<T>(this).freeze()
fun <T> Iterable<T>.toObjectList() =
    this as? ObjectList<T> ?: ObjectArrayList<T>(iterator()).freeze()
// endregion

// region ObjectMap
// region Object2ObjectMap
fun <K, V> mutableObject2ObjectMapOf(vararg pairs: Pair<K, V>) =
    Object2ObjectOpenHashMap<K, V>(pairs.size).apply { putAll(pairs) }

fun <K, V> mutableObject2ObjectMapOf() = Object2ObjectOpenHashMap<K, V>()
fun <K, V> mutableObject2ObjectMapOf(capacity: Int) = Object2ObjectOpenHashMap<K, V>(capacity)
fun <K, V> object2ObjectMapOf(vararg pairs: Pair<K, V>) = mutableObject2ObjectMapOf(*pairs).freeze()
fun <K, V> object2ObjectMapOf() = emptyObject2ObjectMap<K, V>()
fun <K, V> emptyObject2ObjectMap(): @Unmodifiable Object2ObjectMap<K, V> =
    Object2ObjectMaps.emptyMap()

fun <K, V> Object2ObjectOpenHashMap<K, V>.synchronize(): Object2ObjectMap<K, V> =
    Object2ObjectMaps.synchronize(this)

fun <K, V> Object2ObjectMap<K, V>.freeze(): @UnmodifiableView Object2ObjectMap<K, V> =
    Object2ObjectMaps.unmodifiable(this)

fun <K, V> mutableObject2MultiObjectsMapOf(vararg pairs: Pair<K, ObjectSet<V>>) =
    Object2ObjectOpenHashMap<K, ObjectSet<V>>(pairs.size).apply { putAll(pairs) }

fun <K, V> mutableObject2MultiObjectsMapOf() = Object2ObjectOpenHashMap<K, ObjectSet<V>>()
fun <K, V> object2MultiObjectsMapOf(vararg pairs: Pair<K, ObjectSet<V>>) =
    mutableObject2MultiObjectsMapOf(*pairs).freeze()

fun <K, V> object2MultiObjectsMapOf() = emptyObject2MultiObjectsMap<K, V>()
fun <K, V> emptyObject2MultiObjectsMap(): @Unmodifiable Object2MultiObjectsMap<K, V> =
    Object2ObjectMaps.emptyMap()
typealias Object2MultiObjectsMap<K, V> = Object2ObjectMap<K, ObjectSet<V>>

fun <K, V> Object2MultiObjectsMap<K, V>.add(key: K, value: V) {
    val set = get(key) ?: mutableObjectSetOf<V>().also { put(key, it) }
    set.add(value)
}

// endregion
// endregion
// region LongMap
// region Long2LongMap
fun mutableLong2LongMapOf(vararg pairs: Pair<Long, Long>) =
    Long2LongOpenHashMap(pairs.size).apply { putAll(pairs) }

fun mutableLong2LongMapOf() = Long2LongOpenHashMap()
fun long2LongMapOf(vararg pairs: Pair<Long, Long>) = mutableLong2LongMapOf(*pairs).freeze()
fun long2LongMapOf() = emptyLong2LongMap()
fun emptyLong2LongMap(): @Unmodifiable Long2LongMap = Long2LongMaps.EMPTY_MAP
fun Long2LongOpenHashMap.synchronize(): Long2LongMap = Long2LongMaps.synchronize(this)
fun Long2LongOpenHashMap.freeze(): @UnmodifiableView Long2LongMap = Long2LongMaps.unmodifiable(this)

// endregion
// region Long2ObjectMap
fun <V> mutableLong2ObjectMapOf(vararg pairs: Pair<Long, V>) =
    Long2ObjectOpenHashMap<V>(pairs.size).apply { putAll(pairs) }

fun <V> mutableLong2ObjectMapOf() = Long2ObjectOpenHashMap<V>()
fun <V> long2ObjectMapOf(vararg pairs: Pair<Long, V>) = mutableLong2ObjectMapOf(*pairs).freeze()
fun <V> long2ObjectMapOf() = emptyLong2ObjectMap<V>()
fun <V> emptyLong2ObjectMap(): @Unmodifiable Long2ObjectMap<V> = Long2ObjectMaps.emptyMap()
fun <V> Long2ObjectOpenHashMap<V>.synchronize(): Long2ObjectMap<V> =
    Long2ObjectMaps.synchronize(this)

fun <V> Long2ObjectOpenHashMap<V>.freeze(): @UnmodifiableView Long2ObjectMap<V> =
    Long2ObjectMaps.unmodifiable(this)

// endregion
// region LongSet
fun mutableLongSetOf(vararg elements: Long) = LongOpenHashSet(elements)
fun mutableLongSetOf() = LongOpenHashSet()
fun longSetOf(vararg elements: Long) = mutableLongSetOf(*elements).freeze()
fun longSetOf() = emptyLongSet()
fun emptyLongSet(): @Unmodifiable LongSet = LongSets.EMPTY_SET
fun LongSet.synchronize(): LongSet = LongSets.synchronize(this)
fun LongSet.freeze(): @UnmodifiableView LongSet = LongSets.unmodifiable(this)
fun Sequence<Long>.toMutableLongSet() = LongOpenHashSet(toList())
fun Sequence<Long>.toLongSet() = toMutableLongSet().freeze()
// endregion

// region IntSet
fun mutableIntSetOf(vararg elements: Int) = IntOpenHashSet(elements)
fun mutableIntSetOf() = IntOpenHashSet()
fun intSetOf(vararg elements: Int) = mutableIntSetOf(*elements).freeze()
fun intSetOf() = emptyIntSet()
fun emptyIntSet(): @Unmodifiable IntSet = IntSets.EMPTY_SET
fun IntSet.synchronize(): IntSet = IntSets.synchronize(this)
fun IntSet.freeze(): @UnmodifiableView IntSet = IntSets.unmodifiable(this)
fun Sequence<Int>.toMutableIntSet() = IntOpenHashSet(toList())
fun Sequence<Int>.toIntSet() = toMutableIntSet().freeze()
// endregion
// region IntMap
// region Int2ObjectMap
fun <V> mutableInt2ObjectMapOf(vararg pairs: Pair<Int, V>) =
    Int2ObjectOpenHashMap<V>(pairs.size).apply { putAll(pairs) }

fun <V> mutableInt2ObjectMapOf() = Int2ObjectOpenHashMap<V>()
fun <V> int2ObjectMapOf(vararg pairs: Pair<Int, V>) = mutableInt2ObjectMapOf(*pairs).freeze()
fun <V> int2ObjectMapOf() = emptyInt2ObjectMap<V>()
fun <V> emptyInt2ObjectMap(): @Unmodifiable Int2ObjectMap<V> = Int2ObjectMaps.emptyMap()
fun <V> Int2ObjectOpenHashMap<V>.synchronize(): Int2ObjectMap<V> = Int2ObjectMaps.synchronize(this)
fun <V> Int2ObjectOpenHashMap<V>.freeze(): @UnmodifiableView Int2ObjectMap<V> =
    Int2ObjectMaps.unmodifiable(this)

// endregion
// region Object2IntMap
fun <K> mutableObject2IntMapOf(vararg pairs: Pair<K, Int>) =
    Object2IntOpenHashMap<K>(pairs.size).apply { putAll(pairs) }

fun <K> mutableObject2IntMapOf() = Object2IntOpenHashMap<K>()
fun <K> object2IntMapOf(vararg pairs: Pair<K, Int>) = mutableObject2IntMapOf(*pairs).freeze()
fun <K> object2IntMapOf() = emptyObject2IntMap<K>()
fun <K> emptyObject2IntMap(): @Unmodifiable Object2IntMap<K> = Object2IntMaps.emptyMap()
fun <K> Object2IntOpenHashMap<K>.synchronize(): Object2IntMap<K> = Object2IntMaps.synchronize(this)
fun <K> Object2IntOpenHashMap<K>.freeze(): @UnmodifiableView Object2IntMap<K> =
    Object2IntMaps.unmodifiable(this)
// endregion
// region Object2BooleanMap
fun <K> mutableObject2BooleanMapOf(vararg pairs: Pair<K, Boolean>) =
    Object2BooleanOpenHashMap<K>(pairs.size).apply { putAll(pairs) }

fun <K> mutableObject2BooleanMapOf() = Object2BooleanOpenHashMap<K>()
fun <K> object2BooleanMapOf(vararg pairs: Pair<K, Boolean>) = mutableObject2BooleanMapOf(*pairs).freeze()
fun <K> object2BooleanMapOf() = emptyObject2BooleanMap<K>()
fun <K> emptyObject2BooleanMap(): @Unmodifiable Object2BooleanMap<K> = Object2BooleanMaps.emptyMap()
fun <K> Object2BooleanOpenHashMap<K>.synchronize(): Object2BooleanMap<K> =
    Object2BooleanMaps.synchronize(this)

fun <K> Object2BooleanOpenHashMap<K>.freeze(): @UnmodifiableView Object2BooleanMap<K> =
    Object2BooleanMaps.unmodifiable(this)
// endregion