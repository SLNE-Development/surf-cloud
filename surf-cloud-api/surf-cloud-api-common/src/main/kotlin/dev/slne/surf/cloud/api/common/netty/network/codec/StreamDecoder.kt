package dev.slne.surf.cloud.api.common.netty.network.codec

@FunctionalInterface
fun interface StreamDecoder<I, T> {
    fun decode(buf: I): T
}