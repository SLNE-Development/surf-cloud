package dev.slne.surf.cloud.api.common.netty.packet

import dev.slne.surf.cloud.api.common.netty.network.Connection
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.readUuid
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.writeUuid
import io.netty.buffer.ByteBuf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import org.jetbrains.annotations.ApiStatus.Internal
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

val DEFAULT_TIMEOUT = 15.seconds
val DEFAULT_URGENT_TIMEOUT = 5.seconds

abstract class RespondingNettyPacket<P : ResponseNettyPacket> : NettyPacket() {
    private var uniqueSessionId: UUID? = null

    @Deprecated("internal use only")
    @Internal
    val response = CompletableDeferred<P>()

    lateinit var responseConnection: Connection

    @Suppress("DEPRECATION")
    suspend fun fireAndAwait(connection: Connection, timeout: Duration = DEFAULT_TIMEOUT): P? =
        withTimeoutOrNull(timeout) {
            connection.send(this@RespondingNettyPacket)
            response.await()
        }

    suspend fun fireAndAwaitUrgent(connection: Connection): P? =
        fireAndAwait(connection, DEFAULT_URGENT_TIMEOUT)

    @Suppress("DEPRECATION")
    suspend fun fireAndAwaitOrThrow(
        connection: Connection,
        timeout: Duration = DEFAULT_TIMEOUT
    ): P = withTimeout(timeout) {
        connection.send(this@RespondingNettyPacket)
        response.await()
    }

    suspend fun fireAndAwaitOrThrowUrgent(connection: Connection): P =
        fireAndAwaitOrThrow(connection, DEFAULT_URGENT_TIMEOUT)

    fun respond(packet: P) {
        packet.responseTo = uniqueSessionId
            ?: error("Responding packet has no session id. Are you sure it was sent?")
        responseConnection.send(packet)
    }

    @Deprecated("internal use only")
    @Internal
    @Suppress("DEPRECATION")
    fun extraEncode(buf: ByteBuf) {
        buf.writeUuid(getUniqueSessionIdOrCreate())
    }

    @Deprecated("internal use only")
    @Internal
    fun extraDecode(buf: ByteBuf) {
        uniqueSessionId = buf.readUuid()
    }

    @Deprecated("internal use only")
    @Internal
    fun getUniqueSessionIdOrCreate(): UUID {
        if (uniqueSessionId == null) {
            uniqueSessionId = UUID.randomUUID()
        }
        return uniqueSessionId!!
    }
}