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

typealias OptionalBigDecimalResponsePacket = RespondingNettyPacket<OptionalBigDecimalResponse>

@SurfNettyPacket("optional_big_decimal_response", PacketFlow.BIDIRECTIONAL)
class OptionalBigDecimalResponse(override val value: BigDecimal?) : ResponseNettyPacket(),
    CommonResponseType<BigDecimal?> {
    companion object : CommonResponseTypeFactory<OptionalBigDecimalResponse, BigDecimal?> {
        val STREAM_CODEC =
            packetCodec(OptionalBigDecimalResponse::write, ::OptionalBigDecimalResponse)

        override fun create(value: BigDecimal?): OptionalBigDecimalResponse {
            return OptionalBigDecimalResponse(value)
        }
    }

    private constructor(buf: SurfByteBuf) : this(buf.readNullableString()?.let { BigDecimal(it) })

    private fun write(buf: SurfByteBuf) {
        buf.writeNullable(value?.toString())
    }

    operator fun component1() = value
}