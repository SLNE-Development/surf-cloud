package dev.slne.surf.cloud.core.client.netty.state

import com.google.common.util.concurrent.Striped
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.surfapi.core.api.util.logger
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue
import kotlinx.coroutines.CompletableDeferred
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.RejectedExecutionException

class ReconnectPacketQueue {

    private val queues =
        ConcurrentHashMap<ConnectionProtocol, ObjectArrayFIFOQueue<Entry>>(2)
    private val stripedLock = Striped.lock(ConnectionProtocol.entries.size)

    fun enqueue(
        packet: NettyPacket,
        protocol: ConnectionProtocol,
        deferred: CompletableDeferred<Boolean>? = null
    ) {
        val lock = stripedLock.get(protocol)
        lock.lock()
        try {
            val queue = queues.computeIfAbsent(protocol) { ObjectArrayFIFOQueue() }

            val sizeBefore = queue.size()
            if (sizeBefore >= MAX_QUEUE_SIZE) {
                // Remove the oldest packet to make space
                val oldEntry = queue.dequeue()
                oldEntry.deferred?.completeExceptionally(RejectedExecutionException("Packet dropped: reconnect queue overflow for protocol=$protocol"))
                log.atWarning()
                    .log(
                        "ReconnectPacketQueue overflow for protocol=%s (sizeBefore=%d, max=%d). Dropping oldest packet (hasDeferred=%s).",
                        protocol,
                        sizeBefore,
                        MAX_QUEUE_SIZE,
                        oldEntry.deferred != null
                    )
            }

            val entry = Entry(packet, deferred)
            queue.enqueue(entry)

        } finally {
            lock.unlock()
        }
    }

    fun drainForProtocol(
        protocol: ConnectionProtocol,
        consumer: (NettyPacket, deferred: CompletableDeferred<Boolean>?) -> Unit
    ) {
        val lock = stripedLock.get(protocol)
        lock.lock()
        try {
            val queue = queues[protocol] ?: return
            while (!queue.isEmpty) {
                val entry = queue.dequeue()
                consumer(entry.packet, entry.deferred)
            }
        } finally {
            lock.unlock()
        }
    }

    fun clear() {
        queues.clear()
    }

    fun isEmpty(protocol: ConnectionProtocol) = queues[protocol]?.isEmpty ?: true

    @JvmRecord
    private data class Entry(val packet: NettyPacket, val deferred: CompletableDeferred<Boolean>?)

    companion object {
        private val log = logger()
        const val MAX_QUEUE_SIZE = 10_000
    }
}