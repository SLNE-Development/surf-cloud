package dev.slne.surf.cloud.api.common.netty.packet

import dev.slne.surf.cloud.api.common.netty.protocol.buffer.readUuid
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.writeUuid
import io.netty.buffer.ByteBuf
import org.jetbrains.annotations.ApiStatus.Internal
import java.util.*

abstract class ResponseNettyPacket : NettyPacket() {
    var responseTo: UUID? = null
        internal set

    @Deprecated("internal use only")
    @Internal
    fun extraEncode(buf: ByteBuf) {
        buf.writeUuid(
            responseTo
                ?: error("ResponseTo is null. Are you sure you are responding with RespondingNettyPacket#respond()?")
        )
    }

    @Deprecated("internal use only")
    @Internal
    fun extraDecode(buf: ByteBuf) {
        responseTo = buf.readUuid()
    }
}