package dev.slne.surf.cloud.api.common.util.codec

import com.mojang.serialization.DataResult
import java.util.stream.IntStream

fun fixedSize(stream: IntStream, length: Int): DataResult<IntArray> {
    val ints = stream.limit((length + 1).toLong()).toArray()
    if (ints.size != length) {
        val error = { "Input is not a list of $length ints" }
        return if (ints.size >= length) DataResult.error(
            error,
            ints.copyOf(length)
        ) else DataResult.error(error)
    } else {
        return DataResult.success(ints)
    }
}