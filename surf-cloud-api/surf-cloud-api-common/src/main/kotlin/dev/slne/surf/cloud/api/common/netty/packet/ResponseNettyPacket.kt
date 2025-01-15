package dev.slne.surf.cloud.api.common.netty.packet

import dev.slne.surf.cloud.api.common.netty.protocol.buffer.readUuid
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.writeUuid
import dev.slne.surf.cloud.api.common.util.InternalApi
import io.netty.buffer.ByteBuf
import org.jetbrains.annotations.ApiStatus.Internal
import java.util.*

/**
 * Represents a Netty packet that serves as a response to a [RespondingNettyPacket].
 */
abstract class ResponseNettyPacket : NettyPacket() {

    /**
     * The unique ID of the packet to which this response corresponds.
     */
    @InternalApi
    var responseTo: UUID? = null
        internal set

    /**
     * Encodes the unique ID into the buffer. Internal use only.
     *
     * @param buf The buffer to write to.
     */
    @InternalApi
    fun extraEncode(buf: ByteBuf) {
        buf.writeUuid(
            responseTo
                ?: error("ResponseTo is null. Are you sure you are responding with RespondingNettyPacket#respond()?")
        )
    }

    /**
     * Decodes the unique ID from the buffer. Internal use only.
     *
     * @param buf The buffer to read from.
     */
    @InternalApi
    fun extraDecode(buf: ByteBuf) {
        responseTo = buf.readUuid()
    }
}