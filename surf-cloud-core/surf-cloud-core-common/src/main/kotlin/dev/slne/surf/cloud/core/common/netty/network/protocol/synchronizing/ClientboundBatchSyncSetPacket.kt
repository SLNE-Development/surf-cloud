package dev.slne.surf.cloud.core.common.netty.network.protocol.synchronizing

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.surfapi.core.api.util.mutableObjectListOf
import dev.slne.surf.cloud.core.common.sync.CommonSyncRegistryImpl
import dev.slne.surf.cloud.core.common.sync.SyncSetImpl
import dev.slne.surf.surfapi.core.api.util.logger

@SurfNettyPacket(
    "cloud:batch_sync_set",
    PacketFlow.BIDIRECTIONAL,
    ConnectionProtocol.SYNCHRONIZING
)
class ClientboundBatchSyncSetPacket : NettyPacket {
    companion object {
        private val log = logger()
        val STREAM_CODEC =
            packetCodec(ClientboundBatchSyncSetPacket::write, ::ClientboundBatchSyncSetPacket)
    }

    val syncSets: List<Pair<String, Set<Any?>>>

    constructor(syncValues: Map<String, SyncSetImpl<*>>) {
        this.syncSets = syncValues.map { (key, value) -> key to value.toSet() }
    }

    private constructor(buf: SurfByteBuf) {
        val unknownSyncValues = mutableObjectListOf<String>()

        syncSets = buf.readList { buf ->
            val syncId = buf.readUtf()
            val syncSize = buf.readInt()

            val syncSet = CommonSyncRegistryImpl.instance.getSet<Any?>(syncId)
            if (syncSet == null) {
                buf.skipBytes(syncSize)
                unknownSyncValues.add(syncId)
                null
            } else {
                syncId to syncSet.codec.decode(buf)
            }
        }.filterNotNull()

        if (unknownSyncValues.isNotEmpty()) {
            log.atWarning()
                .log("Unknown sync sets: [${unknownSyncValues.joinToString(", ")}]")
        }
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeCollection(syncSets) { buf, (syncId, set) ->
            buf.writeUtf(syncId)

            // Reserve 4 bytes for length
            val lengthIndex = buf.writerIndex()
            buf.writeInt(0)

            val startIndex = buf.writerIndex()
            val syncSet = CommonSyncRegistryImpl.instance.getSet<Any?>(syncId)
                ?: error("SyncSet '$syncId' is not registered in SyncRegistry")
            syncSet.codec.encode(buf, set)
            val endIndex = buf.writerIndex()

            // Write the actual length of the encoded value
            buf.setInt(lengthIndex, endIndex - startIndex)
        }
    }
}