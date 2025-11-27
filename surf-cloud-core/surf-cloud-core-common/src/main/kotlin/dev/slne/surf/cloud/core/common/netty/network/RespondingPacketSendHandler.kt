package dev.slne.surf.cloud.core.common.netty.network

import dev.slne.surf.cloud.api.common.netty.exception.RespondingPacketDisconnectedException
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.util.netty.UnifiedReadOnlyChannelHandler
import dev.slne.surf.cloud.core.common.netty.network.connection.ConnectionImpl
import dev.slne.surf.surfapi.core.api.util.logger
import dev.slne.surf.surfapi.core.api.util.mutableObject2ObjectMapOf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import kotlinx.coroutines.CompletableDeferred
import java.util.*

/**
 * Netty handler responsible for managing request/response style packets:
 *
 * #### For [RespondingNettyPacket]s:
 *   - Registers a [CompletableDeferred] for each outgoing request keyed by
 *     its unique session ID.
 *   - Injects the originating [ConnectionImpl] into the packet so that the
 *     request handler can respond via [RespondingNettyPacket.respond].
 *
 * #### For [ResponseNettyPacket]s:
 *   - Uses [ResponseNettyPacket.responseTo] to look up the corresponding
 *     deferred and complete it with the received response.
 *
 * - On channel inactivity:
 *   - Cancels **all** pending responding packets with
 *     [RespondingPacketDisconnectedException], so callers do not have to
 *     wait for timeouts when the connection is clearly gone.
 *
 * ##### This separation ensures:
 * - no stale responses after reconnect,
 * - clear signal to callers when a disconnect occurs vs. a simple timeout,
 * - and no memory leaks from uncompleted deferreds.
 */
class RespondingPacketSendHandler : UnifiedReadOnlyChannelHandler<NettyPacket>() {
    private val log = logger()
    private val respondingPackets =
        mutableObject2ObjectMapOf<UUID, CompletableDeferred<ResponseNettyPacket>>()

    @Suppress("DEPRECATION")
    override fun handleRead(
        ctx: ChannelHandlerContext,
        msg: NettyPacket
    ) {
        if (msg is RespondingNettyPacket<*> || msg is ResponseNettyPacket) {
            if (msg.handled) {
                error("Packet $msg was already handled")
            }
            msg.handled()
        }

        if (msg is RespondingNettyPacket<*>) {
            msg.initResponseConnection(
                ctx.channel().attr(ConnectionImpl.CHANNEL_ATTRIBUTE_KEY).get()
            )
        }

        if (msg is ResponseNettyPacket) {
            val responseTo = msg.responseTo
            if (responseTo != null) {
                val deferred = respondingPackets.remove(responseTo)
                deferred?.complete(msg)

                if (deferred == null) {
                    log.atWarning().log("Received response for unknown packet $responseTo")
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun handleWrite(
        ctx: ChannelHandlerContext,
        msg: NettyPacket,
        promise: ChannelPromise
    ) {
        if (msg is RespondingNettyPacket<*> || msg is ResponseNettyPacket) {
            if (msg.handled) {
                error("Packet $msg was already handled")
            }
            msg.handled()
        }

        if (msg is RespondingNettyPacket<*>) {
            respondingPackets[msg.getUniqueSessionIdOrCreate()] =
                msg.response as CompletableDeferred<ResponseNettyPacket>
        }
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        super.channelInactive(ctx)

        if (!respondingPackets.isEmpty()) {
            val exception =
                RespondingPacketDisconnectedException("Channel ${ctx.channel()} became inactive while waiting for responses")

            val size = respondingPackets.size
            val iterator = respondingPackets.values.iterator()
            while (iterator.hasNext()) {
                iterator.next().completeExceptionally(exception)
                iterator.remove()
            }

            log.atWarning()
                .withCause(exception)
                .log(
                    "Channel %s closed with %d pending responding packets, all completed exceptionally.",
                    ctx.channel(),
                    size
                )
        }
    }
}