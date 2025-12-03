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
import dev.slne.surf.cloud.core.common.sync.CommonSyncRegistryImpl
import dev.slne.surf.cloud.core.common.sync.SyncSetImpl
import dev.slne.surf.surfapi.core.api.util.logger
import dev.slne.surf.surfapi.core.api.util.mutableObjectListOf

@SurfNettyPacket(
    "cloud:batch_sync_set",
    PacketFlow.BIDIRECTIONAL,
    ConnectionProtocol.SYNCHRONIZING,
    handlerMode = PacketHandlerMode.DEFAULT
)
class ClientboundBatchSyncSetPacket : NettyPacket, InternalNettyPacket<ClientSynchronizingPacketListener> {
    companion object {
        private val log = logger()
        val STREAM_CODEC =
            packetCodec(ClientboundBatchSyncSetPacket::write, ::ClientboundBatchSyncSetPacket)
    }

    val syncSets: List<Triple<String, Set<Any?>, Long>>

    constructor(syncValues: Map<String, SyncSetImpl<*>>) {
        this.syncSets = syncValues.map { (key, value) -> Triple(key, value.toSet(), value.currentChangeId) }
    }

    private constructor(buf: SurfByteBuf) {
        val unknownSyncValues = mutableObjectListOf<String>()

        syncSets = buf.readList { buf ->
            val syncId = buf.readUtf()
            val syncSize = buf.readInt()

            val syncSet = CommonSyncRegistryImpl.instance.getSet<Any?>(syncId)
            if (syncSet == null) {
                buf.skipBytes(syncSize)
                buf.readLong() // Skip the change ID as well
                unknownSyncValues.add(syncId)
                null
            } else {
                val set = syncSet.codec.decode(buf)
                val changeId = buf.readLong()
                Triple(syncId, set, changeId)
            }
        }.filterNotNull()

        if (unknownSyncValues.isNotEmpty()) {
            log.atWarning()
                .log("Unknown sync sets: [${unknownSyncValues.joinToString(", ")}]")
        }
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeCollection(syncSets) { buf, (syncId, set, changeId) ->
            buf.writeUtf(syncId)

            // Reserve 4 bytes for length
            val lengthIndex = buf.writerIndex()
            buf.writeInt(0)

            val startIndex = buf.writerIndex()
            val syncSet = CommonSyncRegistryImpl.instance.getSet<Any?>(syncId)
                ?: encodeError("SyncSet '$syncId' is not registered in SyncRegistry")
            syncSet.codec.encode(buf, set)
            val endIndex = buf.writerIndex()

            // Write the actual length of the encoded value
            buf.setInt(lengthIndex, endIndex - startIndex)

            // Write the change ID after the set data
            buf.writeLong(changeId)
        }
    }

    override fun handle(listener: ClientSynchronizingPacketListener) {
        listener.handleBatchSyncSet(this)
    }
}