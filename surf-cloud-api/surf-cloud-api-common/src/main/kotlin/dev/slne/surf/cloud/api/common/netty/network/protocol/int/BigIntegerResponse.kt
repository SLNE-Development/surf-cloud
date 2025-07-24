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

typealias BigIntegerResponsePacket = RespondingNettyPacket<BigIntegerResponse>

@SurfNettyPacket("big_integer_response", PacketFlow.BIDIRECTIONAL)
class BigIntegerResponse(override val value: BigInteger) : ResponseNettyPacket(),
    CommonResponseType<BigInteger> {
    companion object : CommonResponseTypeFactory<BigIntegerResponse, BigInteger> {
        val STREAM_CODEC = packetCodec(BigIntegerResponse::write, ::BigIntegerResponse)

        override fun create(value: BigInteger): BigIntegerResponse {
            return BigIntegerResponse(value)
        }
    }

    private constructor(buf: SurfByteBuf) : this(BigInteger(buf.readUtf()))

    private fun write(buf: SurfByteBuf) {
        buf.writeUtf(value.toString())
    }

    operator fun component1() = value
}