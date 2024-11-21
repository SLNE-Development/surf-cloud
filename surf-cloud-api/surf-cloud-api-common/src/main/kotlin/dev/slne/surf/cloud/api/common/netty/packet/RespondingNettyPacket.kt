package dev.slne.surf.cloud.api.common.netty.packet

import dev.slne.surf.cloud.api.common.netty.network.Connection
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.readUuid
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.writeUuid
import io.netty.buffer.ByteBuf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull
import org.jetbrains.annotations.ApiStatus.Internal
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

abstract class RespondingNettyPacket<P : ResponseNettyPacket> : NettyPacket() {
    private var uniqueSessionId: UUID? = null

    @Deprecated("internal use only")
    @Internal
    val response = CompletableDeferred<P>()

    lateinit var responseConnection: Connection

    suspend fun fireAndAwait(timeout: Duration = 15.seconds): P? = withTimeoutOrNull(timeout) {
        TODO("Fire packet")
        response.await()
    }

    fun respond(packet: P) {
        packet.responseTo = uniqueSessionId ?: error("Responding packet has no session id. Are you sure it was sent?")
        responseConnection.send(packet)
    }

    @Deprecated("internal use only")
    @Internal
    fun extraEncode(buf: ByteBuf) {
        buf.writeUuid(getUniqueSessionIdOrCreate())
    }

    @Deprecated("internal use only")
    @Internal
    fun extraDecode(buf: ByteBuf) {
        uniqueSessionId = buf.readUuid()
    }

    fun getUniqueSessionIdOrCreate(): UUID {
        if (uniqueSessionId == null) {
            uniqueSessionId = UUID.randomUUID()
        }
        return uniqueSessionId!!
    }
}