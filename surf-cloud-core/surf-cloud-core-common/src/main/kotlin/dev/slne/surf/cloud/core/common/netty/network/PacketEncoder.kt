package dev.slne.surf.cloud.core.common.netty.network

import dev.slne.surf.cloud.api.common.exceptions.SkipPacketException
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.writeNullable
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.writeVarLong
import dev.slne.surf.cloud.api.common.util.logger
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import org.checkerframework.checker.units.qual.m
import java.io.Serial


// packet size is encoded into 3-byte varint
const val MAX_FINAL_PACKET_SIZE = (1 shl 21) - 1

// Max size for the encoder (before compression)
const val MAX_PACKET_SIZE = 0x800000

class PacketEncoder<T : PacketListener>(private val protocolInfo: ProtocolInfo<T>) :
    MessageToByteEncoder<NettyPacket>() {
    private val log = logger()

    @Suppress("DEPRECATION")
    override fun encode(ctx: ChannelHandlerContext, msg: NettyPacket, out: ByteBuf) {
        try {
            protocolInfo.codec.encode(out, msg)

            if (msg is RespondingNettyPacket<*>) {
                msg.extraEncode(out)
            }

            if (msg is ResponseNettyPacket) {
                msg.extraEncode(out)
            }

        } catch (e: Throwable) {
            log.atSevere()
                .withCause(e)
                .log("Error sending packet ${msg.javaClass.name} (skippable? ${msg.skippable})")

            if (msg.skippable) {
                throw SkipPacketException(e)
            }

            throw e
        } finally {
            val packetLength = out.readableBytes()
            if (packetLength > MAX_PACKET_SIZE) {
                throw PacketTooLargeException(msg, packetLength)
            }

            ProtocolSwapHandler.handleOutboundTerminalPacket(ctx, msg)
        }
    }
}

class PacketTooLargeException(val packet: NettyPacket, packetLength: Int) :
    RuntimeException("PacketTooLarge - ${packet.javaClass.simpleName} - is $packetLength. Max is $MAX_PACKET_SIZE") {
    companion object {
        @Serial
        @JvmStatic
        private val serialVersionUID: Long = 2374175573787490036L
    }
}