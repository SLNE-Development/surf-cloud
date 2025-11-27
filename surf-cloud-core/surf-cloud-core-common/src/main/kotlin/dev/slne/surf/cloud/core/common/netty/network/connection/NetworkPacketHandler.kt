package dev.slne.surf.cloud.core.common.netty.network.connection

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacketInfo
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.core.common.coroutines.PacketHandlerIoScope
import dev.slne.surf.cloud.core.common.coroutines.PacketHandlerScope
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import dev.slne.surf.cloud.core.common.netty.network.PacketListener
import dev.slne.surf.cloud.core.common.netty.registry.listener.NettyListenerRegistry
import dev.slne.surf.surfapi.core.api.util.logger
import io.netty.channel.ChannelHandlerContext
import kotlinx.coroutines.launch

internal class NetworkPacketHandler(private val connection: ConnectionImpl) {

    fun handlePacket(msg: NettyPacket) {
        handlePacket(connection.channel.pipeline().context(connection.handler), msg)
    }

    fun handlePacket(ctx: ChannelHandlerContext, msg: NettyPacket) {
        val packetMode = resolvePacketMode(msg)
        when (packetMode) {
            PacketHandlerMode.NETTY -> {
                try {
                    if (!handleInternalPacket(connection.packetListener, msg)) {
                        dispatchPluginListeners(ctx, msg)
                    }
                } catch (e: Throwable) {
                    handlePacketHandleException(ctx, msg, e)
                }
            }

            PacketHandlerMode.DEFAULT, PacketHandlerMode.INHERIT -> {
                PacketHandlerScope.launch {
                    try {
                        if (!handleInternalPacket(connection.packetListener, msg)) {
                            dispatchPluginListeners(ctx, msg)
                        }
                    } catch (e: Throwable) {
                        handlePacketHandleException(ctx, msg, e)
                    }
                }
            }

            PacketHandlerMode.IO -> {
                PacketHandlerIoScope.launch {
                    try {
                        if (!handleInternalPacket(connection.packetListener, msg)) {
                            dispatchPluginListeners(ctx, msg)
                        }
                    } catch (e: Throwable) {
                        handlePacketHandleException(ctx, msg, e)
                    }
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun handleInternalPacket(listener: PacketListener, msg: NettyPacket): Boolean {
        if (msg !is InternalNettyPacket<*>) return false
        msg as InternalNettyPacket<PacketListener>
        msg.handle(listener)

        return true
    }

    private fun dispatchPluginListeners(ctx: ChannelHandlerContext, msg: NettyPacket) {
        val protocol = connection.inboundProtocolInfo.id
        if (protocol != ConnectionProtocol.RUNNING && protocol != ConnectionProtocol.SYNCHRONIZING) {
            error("Unexpected packet: " + msg::class.simpleName + " in protocol " + protocol)
        }

        val info = NettyPacketInfo(connection, protocol)

        NettyListenerRegistry.dispatch(
            ctx.channel(),
            msg,
            info
        ) { e, listener ->
            log.atWarning()
                .withCause(e)
                .log(
                    "Failed to call listener %s for packet %s",
                    listener.owner::class.simpleName,
                    msg::class.simpleName
                )
        }
    }

    private fun handlePacketHandleException(
        ctx: ChannelHandlerContext,
        msg: NettyPacket,
        e: Throwable
    ) {
        if (e is Error) return connection.handler.exceptionCaught(ctx, e)

        log.atWarning()
            .withCause(e)
            .log("Exception while handling packet %s", msg::class.simpleName)
    }

    companion object {
        private val log = logger()

        private object PacketModeCache : ClassValue<PacketHandlerMode>() {
            override fun computeValue(type: Class<*>): PacketHandlerMode {
                val annotation = type.getAnnotation(SurfNettyPacket::class.java)
                return annotation?.handlerMode ?: PacketHandlerMode.DEFAULT
            }
        }

        private fun resolvePacketMode(packet: NettyPacket): PacketHandlerMode {
            return PacketModeCache.get(packet.javaClass)
        }
    }
}