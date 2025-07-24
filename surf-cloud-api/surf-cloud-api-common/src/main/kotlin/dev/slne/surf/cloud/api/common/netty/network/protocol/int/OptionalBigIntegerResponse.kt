package dev.slne.surf.cloud.api.common.netty.network.protocol.int

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseType
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseTypeFactory
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import java.math.BigInteger

typealias OptionalBigIntegerResponsePacket = RespondingNettyPacket<OptionalBigIntegerResponse>

@SurfNettyPacket("optional_big_integer_response", PacketFlow.BIDIRECTIONAL)
class OptionalBigIntegerResponse(override val value: BigInteger?) : ResponseNettyPacket(),
    CommonResponseType<BigInteger?> {
    companion object : CommonResponseTypeFactory<OptionalBigIntegerResponse, BigInteger?> {
        val STREAM_CODEC =
            packetCodec(OptionalBigIntegerResponse::write, ::OptionalBigIntegerResponse)

        override fun create(value: BigInteger?): OptionalBigIntegerResponse {
            return OptionalBigIntegerResponse(value)
        }
    }

    private constructor(buf: SurfByteBuf) : this(buf.readNullableString()?.let { BigInteger(it) })

    private fun write(buf: SurfByteBuf) {
        buf.writeNullable(value?.toString())
    }

    operator fun component1() = value
}