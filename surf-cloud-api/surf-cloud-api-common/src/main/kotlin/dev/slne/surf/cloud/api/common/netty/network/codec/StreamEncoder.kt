package dev.slne.surf.cloud.api.common.netty.network.codec

@FunctionalInterface
fun interface StreamEncoder<O, T> {
    fun encode(buf: O, value: T)
}