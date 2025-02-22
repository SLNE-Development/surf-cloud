package dev.slne.surf.cloud.api.common.util

/**
 * Returns the single element of the collection if it contains exactly one element,
 * or `null` if it is empty. Throws an exception if the collection contains more than one element.
 *
 * @return The single element or `null` if the collection is empty.
 * @throws IllegalStateException if the collection contains more than one element.
 */
fun <T> Collection<T>.singleOrNullOrThrow(): T? {
    return when (size) {
        0 -> null
        1 -> elementAt(0)
        else -> error("List has more than one element")
    }
}

/**
 * Returns the single element of the iterable if it contains exactly one element,
 * or `null` if it is empty. Throws an exception if the iterable contains more than one element.
 *
 * If the iterable is a collection, it delegates to [Collection.singleOrNullOrThrow].
 *
 * @return The single element or `null` if the iterable is empty.
 * @throws IllegalStateException if the iterable contains more than one element.
 */
fun <T> Iterable<T>.singleOrNullOrThrow(): T? {
    return when (this) {
        is Collection -> singleOrNullOrThrow()
        else -> {
            val iterator = iterator()
            if (!iterator.hasNext()) return null
            val single = iterator.next()
            if (iterator.hasNext()) error("Iterable has more than one element")
            single
        }
    }
}

/**
 * Returns the single element matching the given [predicate] if exactly one element matches,
 * or `null` if no elements match. Throws an exception if more than one element matches.
 *
 * @param predicate A function that returns `true` for elements to be considered.
 * @return The single matching element or `null` if no elements match.
 * @throws IllegalStateException if more than one element matches the predicate.
 */
inline fun <T> Iterable<T>.singleOrNullOrThrow(predicate: (T) -> Boolean): T? {
    var single: T? = null
    var found = false
    for (element in this) {
        if (predicate(element)) {
            if (found) error("Iterable has more than one matching element")
            single = element
            found = true
        }
    }
    return single
}