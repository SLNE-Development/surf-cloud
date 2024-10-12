package dev.slne.surf.cloud.api.netty.protocol.buffer.ecoder

import io.netty.buffer.ByteBuf

@FunctionalInterface
fun interface Encoder<B : ByteBuf> {
    fun encode(buffer: B)
}
