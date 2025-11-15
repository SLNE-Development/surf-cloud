package dev.slne.surf.cloud.core.common.netty.network.protocol.synchronizing

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.encodeError
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import dev.slne.surf.cloud.core.common.sync.BasicSyncValue
import dev.slne.surf.cloud.core.common.sync.CommonSyncRegistryImpl
import dev.slne.surf.surfapi.core.api.util.logger
import dev.slne.surf.surfapi.core.api.util.mutableObjectListOf

@SurfNettyPacket(
    "cloud:batch_sync_value",
    PacketFlow.BIDIRECTIONAL,
    ConnectionProtocol.SYNCHRONIZING,
    handlerMode = PacketHandlerMode.DEFAULT
)
class ClientboundBatchSyncValuePacket : NettyPacket,
    InternalNettyPacket<ClientSynchronizingPacketListener> {
    companion object {
        private val log = logger()
        val STREAM_CODEC =
            packetCodec(ClientboundBatchSyncValuePacket::write, ::ClientboundBatchSyncValuePacket)
    }

    val syncValues: List<Pair<String, Any?>>

    constructor(syncValues: Map<String, BasicSyncValue<*>>) {
        this.syncValues = syncValues.map { (key, value) -> key to value.get() }
    }

    private constructor(buf: SurfByteBuf) {
        val unknownSyncValues = mutableObjectListOf<String>()

        syncValues = buf.readList { buf ->
            val syncId = buf.readUtf()
            val syncSize = buf.readInt()

            val codec = CommonSyncRegistryImpl.instance.getSyncValueCodec(syncId)
            if (codec == null) {
                buf.skipBytes(syncSize)
                unknownSyncValues.add(syncId)
                null
            } else {
                syncId to codec.decode(buf)
            }
        }.filterNotNull()

        if (unknownSyncValues.isNotEmpty()) {
            log.atWarning()
                .log("Unknown sync values: [${unknownSyncValues.joinToString(", ")}]")
        }
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeCollection(syncValues) { buf, (syncId, value) ->
            buf.writeUtf(syncId)

            // Reserve 4 bytes for length
            val lengthIndex = buf.writerIndex()
            buf.writeInt(0)

            val startIndex = buf.writerIndex()
            val codec = CommonSyncRegistryImpl.instance.getSyncValueCodec(syncId)
                ?: encodeError("'$syncId' is not registered in SyncRegistry")
            codec.encode(buf, value)
            val endIndex = buf.writerIndex()

            // Write the actual length of the encoded value
            buf.setInt(lengthIndex, endIndex - startIndex)
        }
    }

    override fun handle(listener: ClientSynchronizingPacketListener) {
        listener.handleBatchSyncValue(this)
    }
}