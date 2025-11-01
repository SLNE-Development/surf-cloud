package dev.slne.surf.cloud.api.common.util

import dev.slne.surf.cloud.api.common.util.ByIdMap.OutOfBoundsStrategy.*
import io.netty.handler.codec.DecoderException
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import org.spongepowered.math.GenericMath

object ByIdMap {
    private fun <T> createMap(keyExtractor: (T) -> Int, values: Array<T>): (Int) -> T? {
        require(values.isNotEmpty()) { "Empty value list" }
        val map = Int2ObjectOpenHashMap<T>()

        for (element in values) {
            val id = keyExtractor(element)
            val previous = map.put(id, element)
            require(previous == null) { "Duplicate entry on id $id: current=$element, previous=$previous" }
        }

        return { map.get(it) }
    }

    fun <T> sparse(
        keyExtractor: (T) -> Int,
        values: Array<T>,
        fallback: T
    ): (Int) -> T {
        val intFunction = createMap(keyExtractor, values)
        return { key -> intFunction(key) ?: fallback }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> createSortedArray(
        keyExtractor: (T) -> Int,
        values: Array<T>
    ): Array<T> {
        val size = values.size
        require(size > 0) { "Empty value list" }

        val objects =
            java.lang.reflect.Array.newInstance(values.javaClass.componentType, size) as Array<T?>

        for (element in values) {
            val id = keyExtractor(element)
            require(id in 0 until size) { "Values are not continous, found index $id for value $element" }

            val previous = objects[id]
            require(previous == null) { "Duplicate entry on id $id: current=$element, previous=$previous" }

            objects[id] = element
        }

        for (index in 0..<size) {
            requireNotNull(objects[index]) { "Missing value at index: $index" }
        }

        return objects.requireNoNulls()
    }

    fun <T : Any> continuous(
        keyExtractor: (T) -> Int,
        values: Array<T>,
        outOfBoundsStrategy: OutOfBoundsStrategy
    ): (id: Int) -> T {
        val objects = createSortedArray(keyExtractor, values)
        val size = objects.size

        return when (outOfBoundsStrategy) {
            ZERO -> {
                val first = objects[0]
                { key -> if (key >= 0 && key < size) objects[key] else first }
            }

            LAST -> {
                val last = objects[size - 1]
                { key -> if (key >= 0 && key < size) objects[key] else last }
            }

            WRAP -> { key ->
                objects[Math.floorMod(
                    key,
                    size
                )]
            }

            CLAMP -> { key ->
                objects[GenericMath.clamp(
                    key,
                    0,
                    size - 1
                )]
            }

            DECODE_ERROR -> { key ->
                if (key >= 0 && key < size) objects[key] else throw DecoderException("Invalid key index: $key")
            }

            is ERROR -> { key ->
                if (key in 0..<size) objects[key] else throw outOfBoundsStrategy.errorFactory(key)
            }
        }
    }

    @Suppress("ClassName")
    sealed class OutOfBoundsStrategy {
        data object ZERO : OutOfBoundsStrategy()
        data object LAST : OutOfBoundsStrategy()
        data object WRAP : OutOfBoundsStrategy()
        data object CLAMP : OutOfBoundsStrategy()
        data object DECODE_ERROR : OutOfBoundsStrategy()
        data class ERROR(val errorFactory: (Int) -> Throwable) : OutOfBoundsStrategy()
    }
}
