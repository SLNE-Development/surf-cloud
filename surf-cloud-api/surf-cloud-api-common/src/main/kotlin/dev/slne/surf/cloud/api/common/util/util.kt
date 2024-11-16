package dev.slne.surf.cloud.api.common.util

import java.util.*
import java.util.function.ToIntFunction

const val LINEAR_LOOKUP_THRESHOLD = 8

fun <T> createIndexLookup(values: List<T>): ToIntFunction<T> {
    val size = values.size
    return if (size < LINEAR_LOOKUP_THRESHOLD) {
        ToIntFunction { values.indexOf(it) }
    } else {
        mutableObject2IntMapOf<T>().apply {
            for (index in 0 until size) {
                put(values[index], index)
            }
            defaultReturnValue(-1)
        }
    }
}

fun IntArray.toUuid(): UUID {
    return UUID(
        this[0].toLong() shl 32 or (this[1].toLong() and 0xFFFFFFFF),
        this[2].toLong() shl 32 or (this[3].toLong() and 0xFFFFFFFF)
    )
}

fun UUID.toIntArray() = leastMostToIntArray(mostSignificantBits, leastSignificantBits)


private fun leastMostToIntArray(uuidMost: Long, uuidLeast: Long): IntArray {
    return intArrayOf(
        (uuidMost shr 32).toInt(),
        uuidMost.toInt(),
        (uuidLeast shr 32).toInt(),
        uuidLeast.toInt()
    )
}