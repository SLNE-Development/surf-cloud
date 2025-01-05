package dev.slne.surf.cloud.api.common.netty.network.protocol

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import it.unimi.dsi.fastutil.objects.ObjectCollection
import it.unimi.dsi.fastutil.objects.ObjectList
import kotlin.collections.toTypedArray

@SurfNettyPacket("string_array_response", PacketFlow.BIDIRECTIONAL)
class StringArrayResponse(val value: Array<String>) : ResponseNettyPacket() {
    companion object {
        val STREAM_CODEC = packetCodec(StringArrayResponse::write, ::StringArrayResponse)
    }

    private constructor(buf: SurfByteBuf) : this(buf.readStringArray())

    private fun write(buf: SurfByteBuf) {
        buf.writeStringArray(value)
    }

    operator fun component1() = value
}

fun RespondingNettyPacket<StringArrayResponse>.respond(value: Array<String>) =
    respond(StringArrayResponse(value))

fun RespondingNettyPacket<StringArrayResponse>.respond(value: Collection<String>) =
    respond(value.toTypedArray())