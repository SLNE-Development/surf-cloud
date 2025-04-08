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

typealias CharArrayResponsePacket = RespondingNettyPacket<CharArrayResponse>

/**
 * A Netty packet that represents a response containing a character array.
 *
 * @property value The character array being transmitted in the response.
 */
@SurfNettyPacket("char_array_response", PacketFlow.BIDIRECTIONAL)
class CharArrayResponse(override val value: CharArray) : ResponseNettyPacket(),
    CommonResponseType<CharArray> {
    companion object : CommonResponseTypeFactory<CharArrayResponse, CharArray> {
        val STREAM_CODEC = packetCodec(CharArrayResponse::write, ::CharArrayResponse)
        override fun create(value: CharArray): CharArrayResponse {
            return CharArrayResponse(value)
        }
    }

    private constructor(buf: SurfByteBuf) : this(buf.readCharArray())

    private fun write(buf: SurfByteBuf) {
        buf.writeCharArray(value)
    }

    operator fun component1() = value
}

fun CharArrayResponsePacket.respond(value: CharCollection) =
    respond(value.toCharArray())