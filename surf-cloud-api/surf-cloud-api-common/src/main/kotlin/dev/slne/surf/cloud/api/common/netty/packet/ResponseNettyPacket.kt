package dev.slne.surf.cloud.api.common.netty.packet

import dev.slne.surf.cloud.api.common.netty.protocol.buffer.readUuid
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.writeUuid
import dev.slne.surf.cloud.api.common.util.annotation.InternalApi
import io.netty.buffer.ByteBuf
import java.util.*

/**
 * Represents a Netty packet that serves as a response to a [RespondingNettyPacket].
 *
 * Each response carries a [responseTo] field containing the unique session ID
 * of the corresponding request. This ID is used on the sending side
 * (see RespondingPacketSendHandler) to look up and complete the correct
 * deferred response.
 *
 * Note:
 * - If a response arrives for an unknown or already completed session ID,
 *   it is typically logged as a warning and ignored.
 * - This can legitimately happen if the connection is closed and reopened,
 *   or if the requester has already timed out or canceled the request.
 */
abstract class ResponseNettyPacket : NettyPacket() {

    /**
     * The unique ID of the packet to which this response corresponds.
     *
     * This is written by [RespondingNettyPacket.respond] and read by
     * RespondingPacketSendHandler to match responses with their pending
     * requests.
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