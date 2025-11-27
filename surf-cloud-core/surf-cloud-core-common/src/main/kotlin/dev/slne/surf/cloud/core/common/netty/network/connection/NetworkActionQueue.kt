package dev.slne.surf.cloud.core.common.netty.network.connection

import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.core.common.coroutines.ConnectionManagementScope
import dev.slne.surf.cloud.core.common.netty.network.AlwaysImmediate
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

internal class NetworkActionQueue(private val connection: ConnectionImpl) {
    private val pendingActions = ConcurrentLinkedQueue<WrappedConsumer>()
    private val drainingActions = AtomicBoolean(false)

    fun internalSend(
        packet: NettyPacket,
        flush: Boolean = true,
        deferred: CompletableDeferred<Boolean>? = null
    ) {
        val connectedNow = connection.connected
        if (!connectedNow && !connection.preparing) {
            deferred?.complete(false)
            return
        }

        if (connectedNow
            && (AlwaysImmediate.canSendImmediate(packet.javaClass)
                    || (packet.isReady() && pendingActions.isEmpty() && packet.extraPackets.isNullOrEmpty()))
        ) {
            connection.sendPacketNow(packet, flush, deferred)
        } else {
            // Write the packets to the queue, then flush
            val extraPackets = buildExtraPackets(packet)

            if (extraPackets.isNullOrEmpty()) {
                pendingActions.add(PacketSendAction(packet, flush, deferred))
            } else {
                pendingActions.addAll(buildList {
                    // Delay the future listener until the end of the extra packets
                    add(PacketSendAction(packet, false, null))
                    extraPackets.forEachIndexed { index, extraPacket ->
                        add(
                            PacketSendAction(
                                extraPacket,
                                index == extraPackets.size - 1,
                                if (index == extraPackets.size - 1) deferred else null
                            )
                        )
                    }
                })
            }

            flushQueue()
        }
    }

    fun runOnceConnected(block: ConnectionImpl.() -> Unit) {
        if (connection.connected) {
            flushQueue()
            block(connection)
        } else {
            pendingActions.add(WrappedConsumer(block))
        }
    }

    suspend fun runOnceConnectedSuspend(block: suspend ConnectionImpl.() -> Unit) {
        if (connection.connected) {
            flushQueue()
            block(connection)
        } else {
            val deferred = CompletableDeferred<Unit>()
            pendingActions.add(WrappedConsumer { connection ->
                ConnectionManagementScope.launch {
                    deferred.complete(block(connection))
                }
            })
            deferred.await()
        }
    }

    fun flushChannel() = runOnceConnected { connection.flushChannelNow() }

    fun flushQueue(): Boolean {
        if (!connection.connected) return true
        val channel = connection._channel ?: return false
        val eventLoop = channel.eventLoop()
        if (eventLoop.inEventLoop()) {
            drainQueueOnEventLoop()
        } else {
            eventLoop.execute {
                drainQueueOnEventLoop()
            }
        }

        return true
    }

    private fun drainQueueOnEventLoop() {
        // disable parallel drains
        if (!drainingActions.compareAndSet(false, true)) return

        try {
            while (true) {
                val head = pendingActions.peek() ?: break

                // Already consumed? Then throw out and next round
                if (head.isConsumed()) {
                    pendingActions.poll()
                    continue
                }

                // Head blocked? Then do not remove and stop processing
                if (head is PacketSendAction && !head.packet.isReady()) {
                    break
                }

                // Now it can be processed
                val queued = pendingActions.poll() ?: continue
                if (queued.tryMarkConsumed()) {
                    queued.consumer(connection)
                }
            }
        } finally {
            drainingActions.set(false)
            // If new elements came in during the drain, trigger again
            if (!pendingActions.isEmpty()) {
                // We are already in the EventLoop thread - Prevent reentrancy:
                if (drainingActions.compareAndSet(false, true)) {
                    try {
                        // short second pass
                        while (true) {
                            val head = pendingActions.peek() ?: break
                            if (head is PacketSendAction && !head.packet.isReady()) break
                            val queued = pendingActions.poll() ?: continue
                            if (queued.tryMarkConsumed()) queued.consumer(connection)
                        }
                    } finally {
                        drainingActions.set(false)
                    }
                } else {
                    // Another thread is already draining - it will pick it up
                }
            }
        }
    }

    fun clear() {
        pendingActions.clear()
    }

    companion object {
        private fun buildExtraPackets(packet: NettyPacket): List<NettyPacket>? {
            val extra = packet.extraPackets ?: return null
            if (extra.isEmpty()) return null

            return buildList(extra.size + 1) { buildExtraPackets0(extra) }
        }

        private fun MutableList<NettyPacket>.buildExtraPackets0(extraPackets: List<NettyPacket>) {
            for (extra in extraPackets) {
                add(extra)
                val extraExtra = extra.extraPackets

                if (!extraExtra.isNullOrEmpty()) buildExtraPackets0(extraExtra)
            }
        }
    }

    private open class WrappedConsumer(val consumer: (ConnectionImpl) -> Unit) {
        private val consumed = AtomicBoolean(false)

        fun tryMarkConsumed() = consumed.compareAndSet(false, true)
        fun isConsumed() = consumed.get()
    }

    private class PacketSendAction(
        val packet: NettyPacket,
        flush: Boolean,
        deferred: CompletableDeferred<Boolean>? = null
    ) : WrappedConsumer({ it.sendPacketNow(packet, flush, deferred) })
}