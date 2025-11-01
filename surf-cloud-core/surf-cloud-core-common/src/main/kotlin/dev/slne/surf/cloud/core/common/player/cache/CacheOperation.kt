package dev.slne.surf.cloud.core.common.player.cache

sealed interface CacheOperation {
    // Value
    data class ValueSet<T : Any>(val value: T?) : CacheOperation

    sealed interface ListOperation : CacheOperation {
        data class Append<E : Any>(val elements: List<E>) : ListOperation
        data class Insert<E : Any>(val index: Int, val elements: List<E>) : ListOperation
        data class Set<E : Any>(val index: Int, val element: E) : ListOperation
        data class RemoveAt(val index: Int, val count: Int) : ListOperation
        data object Clear : ListOperation
    }

    sealed interface SetOperation : CacheOperation {
        data class Add<E : Any>(val elements: Set<E>) : SetOperation
        data class Remove<E : Any>(val elements: Set<E>) : SetOperation
        data object Clear : SetOperation
    }

    sealed interface MapOperation : CacheOperation {
        data class Put<K : Any, V : Any>(val entries: Map<K, V>) : MapOperation
        data class Remove<K : Any>(val keys: Set<K>) : MapOperation
        data object Clear : MapOperation

    }

    // Structured
    data class StructuredDelta<D : Any>(val delta: D) : CacheOperation
}