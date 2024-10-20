package dev.slne.surf.cloud.api.netty.packet

import dev.slne.surf.cloud.api.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.api.netty.protocol.buffer.decoder.Decoder
import dev.slne.surf.cloud.api.netty.protocol.buffer.ecoder.Encoder
import java.util.concurrent.ThreadLocalRandom
import kotlin.reflect.full.findAnnotation


abstract class NettyPacket<SELF : NettyPacket<SELF>> : Encoder<SurfByteBuf>,
    Decoder<SurfByteBuf, SELF> {

    internal var sessionId = ThreadLocalRandom.current().nextLong()
        private set

    val id: Int

    init {
        val meta = this::class.findAnnotation<SurfNettyPacket>()
            ?: error("NettyPacket class must be annotated with SurfNettyPacket")
        this.id = meta.id
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NettyPacket<*>) return false

        if (sessionId != other.sessionId) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sessionId.hashCode()
        result = 31 * result + id
        return result
    }

    override fun toString(): String {
        return "NettyPacket(id=$id, sessionId=$sessionId)"
    }
    // TODO: 16.09.2024 14:49 - send method
}
