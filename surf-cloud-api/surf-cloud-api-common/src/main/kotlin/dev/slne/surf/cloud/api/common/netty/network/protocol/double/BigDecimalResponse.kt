package dev.slne.surf.cloud.api.common.netty.network.protocol.double

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseType
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseTypeFactory
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import java.math.BigDecimal

typealias BigDecimalResponsePacket = RespondingNettyPacket<BigDecimalResponse>

@SurfNettyPacket("big_decimal_response", PacketFlow.BIDIRECTIONAL)
class BigDecimalResponse(override val value: BigDecimal) : ResponseNettyPacket(),
    CommonResponseType<BigDecimal> {
    companion object : CommonResponseTypeFactory<BigDecimalResponse, BigDecimal> {
        val STREAM_CODEC = packetCodec(BigDecimalResponse::write, ::BigDecimalResponse)

        override fun create(value: BigDecimal): BigDecimalResponse {
            return BigDecimalResponse(value)
        }
    }

    private constructor(buf: SurfByteBuf) : this(BigDecimal(buf.readUtf()))

    private fun write(buf: SurfByteBuf) {
        buf.writeUtf(value.toString())
    }

    operator fun component1() = value
}