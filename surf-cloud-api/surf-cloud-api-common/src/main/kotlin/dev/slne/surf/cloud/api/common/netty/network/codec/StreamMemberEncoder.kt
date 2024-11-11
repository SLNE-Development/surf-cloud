package dev.slne.surf.cloud.api.common.netty.network.codec

@FunctionalInterface
fun interface StreamMemberEncoder<O, T> {
    fun encode(value: T, buf: O)
}