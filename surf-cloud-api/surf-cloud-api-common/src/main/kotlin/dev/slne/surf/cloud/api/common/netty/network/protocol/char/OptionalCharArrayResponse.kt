package dev.slne.surf.cloud.api.common.netty.network.protocol.char

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseType
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseTypeFactory
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.network.protocol.respond
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import it.unimi.dsi.fastutil.chars.CharCollection

typealias OptionalCharArrayResponsePacket = RespondingNettyPacket<OptionalCharArrayResponse>

/**
 * A Netty packet that represents a response containing a character array.
 *
 * @property value The character array being transmitted in the response.
 */
@SurfNettyPacket("optional_char_array_response", PacketFlow.BIDIRECTIONAL)
class OptionalCharArrayResponse(override val value: CharArray?) : ResponseNettyPacket(),
    CommonResponseType<CharArray?> {
    companion object : CommonResponseTypeFactory<OptionalCharArrayResponse, CharArray?> {
        val STREAM_CODEC = packetCodec(OptionalCharArrayResponse::write, ::OptionalCharArrayResponse)
        override fun create(value: CharArray?): OptionalCharArrayResponse {
            return OptionalCharArrayResponse(value)
        }
    }

    private constructor(buf: SurfByteBuf) : this(buf.readNullable { it.readCharArray() })

    private fun write(buf: SurfByteBuf) {
        buf.writeNullable(value) { buf, value -> buf.writeCharArray(value) }
    }

    operator fun component1() = value
}

fun OptionalCharArrayResponsePacket.respond(value: CharCollection?) =
    respond(value?.toCharArray())