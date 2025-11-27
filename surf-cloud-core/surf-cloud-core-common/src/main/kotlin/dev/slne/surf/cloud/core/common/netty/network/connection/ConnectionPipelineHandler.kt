package dev.slne.surf.cloud.core.common.netty.network.connection

import dev.slne.surf.cloud.api.common.exceptions.SkipPacketException
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.core.common.netty.network.DisconnectReason
import dev.slne.surf.cloud.core.common.netty.network.DisconnectionDetails
import dev.slne.surf.cloud.core.common.netty.network.PacketTooLargeException
import dev.slne.surf.cloud.core.common.netty.network.ProtocolSwapHandler
import dev.slne.surf.cloud.core.common.netty.network.protocol.login.ClientboundLoginDisconnectPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientboundDisconnectPacket
import dev.slne.surf.surfapi.core.api.util.logger
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.DecoderException
import io.netty.handler.codec.EncoderException
import io.netty.handler.ssl.NotSslRecordException
import io.netty.handler.timeout.TimeoutException

class ConnectionPipelineHandler(private val connection: ConnectionImpl) :
    SimpleChannelInboundHandler<NettyPacket>() {

    private var handlingFault = false

    override fun channelActive(ctx: ChannelHandlerContext) {
        super.channelActive(ctx)
        val channel = ctx.channel()

        connection._channel = channel
        connection._address = channel.remoteAddress()
        connection.preparing = false

        connection.delayedDisconnect?.let { connection.disconnect(it) }
        connection.setupEncryption()
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        connection.disconnect(DisconnectReason.END_OF_STREAM)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, e: Throwable?) {
        if (e is DecoderException && e.cause is NotSslRecordException) {
            ctx.close()
            return
        }

        log.atInfo().withCause(e).log("Exception caught") // TODO: remove this debug line
        var throwable = e

        if (throwable is EncoderException) {
            val cause = throwable.cause

            if (cause is PacketTooLargeException) {
                val packet = cause.packet
                if (packet.packetTooLarge(connection)) {
                    ProtocolSwapHandler.handleOutboundTerminalPacket(ctx, packet)
                    return
                } else if (packet.skippable) {
                    log.atFine().withCause(cause).log("Skipping packet due to errors")
                    ProtocolSwapHandler.handleOutboundTerminalPacket(ctx, packet)
                    return
                } else {
                    throwable = cause
                }
            }
        }

        if (throwable is SkipPacketException) {
            log.atFine().withCause(throwable.cause).log("Skipping packet due to errors")
        } else {
            val previousHandlingFault = !handlingFault
            handlingFault = true

            if (connection.channel.isOpen) {
                if (throwable is TimeoutException) {
                    log.atFine().withCause(throwable).log("Timeout")
                    connection.disconnect(DisconnectReason.TIMEOUT)
                } else {
                    val reason = "Internal Exception: ${throwable?.message}"
                    val disconnectionDetails =
                        connection._packetListener?.createDisconnectionInfo(
                            DisconnectReason.INTERNAL_EXCEPTION,
                            reason
                        ) ?: DisconnectionDetails(DisconnectReason.INTERNAL_EXCEPTION, reason)

                    if (previousHandlingFault) {
                        log.atFine().withCause(throwable).log("Failed to sent packet")

                        val doesDisconnectExist =
                            connection._packetListener?.protocol != ConnectionProtocol.INITIALIZE && connection._packetListener?.protocol != ConnectionProtocol.HANDSHAKING

                        if (connection.sending == PacketFlow.CLIENTBOUND && doesDisconnectExist) {
                            val packet =
                                if (connection.sendLoginDisconnect) ClientboundLoginDisconnectPacket(
                                    disconnectionDetails
                                ) else ClientboundDisconnectPacket(disconnectionDetails)

                            connection.sendPacketNow(packet) { future ->
                                connection.disconnect(disconnectionDetails)
                                if (!future.isSuccess) {
                                    future.channel().pipeline()
                                        .fireExceptionCaught(future.cause())
                                }
                            }
                        } else {
                            connection.disconnect(disconnectionDetails)
                        }

                        connection.setReadOnly()
                    } else {
                        log.atFine().withCause(throwable).log("Double fault")
                        connection.disconnect(disconnectionDetails)
                    }
                }
            }
        }
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: NettyPacket) {
        if (connection._channel?.isOpen == false) return

        val packetListener = connection._packetListener
        check(packetListener != null) { "Received a packet before the packet listener was initialized" }

        if (connection.stopReadingPackets) return
        if (!packetListener.shouldHandleMessage(msg)) return

        connection.stats.onPacketReceived()
        if (msg is ResponseNettyPacket) return // handled by RespondingPacketSendHandler
        connection.networkPacketHandler.handlePacket(ctx, msg)
    }

    companion object {
        private val log = logger()
    }
}