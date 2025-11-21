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
import dev.slne.surf.cloud.core.common.sync.SyncMapImpl
import dev.slne.surf.surfapi.core.api.util.logger
import dev.slne.surf.surfapi.core.api.util.mutableObjectListOf

@SurfNettyPacket(
    "cloud:batch_sync_map",
    PacketFlow.BIDIRECTIONAL,
    ConnectionProtocol.SYNCHRONIZING,
    handlerMode = PacketHandlerMode.DEFAULT
)
class ClientboundBatchSyncMapPacket : NettyPacket, InternalNettyPacket<ClientSynchronizingPacketListener> {
    companion object {
        private val log = logger()
        val STREAM_CODEC =
            packetCodec(ClientboundBatchSyncMapPacket::write, ::ClientboundBatchSyncMapPacket)
    }

    val syncMaps: List<Pair<String, Map<Any?, Any?>>>

    constructor(syncMaps: Map<String, SyncMapImpl<*, *>>) {
        this.syncMaps = syncMaps.map { (key, value) -> key to value.toMap() }
    }

    private constructor(buf: SurfByteBuf) {
        val unknownSyncMaps = mutableObjectListOf<String>()

        syncMaps = buf.readList { buf ->
            val syncId = buf.readUtf()
            val syncSize = buf.readInt()

            val syncMap = CommonSyncRegistryImpl.instance.getMap<Any?, Any?>(syncId)
            if (syncMap == null) {
                buf.skipBytes(syncSize)
                unknownSyncMaps.add(syncId)
                null
            } else {
                syncId to syncMap.codec.decode(buf)
            }
        }.filterNotNull()

        if (unknownSyncMaps.isNotEmpty()) {
            log.atWarning()
                .log("Unknown sync maps: [${unknownSyncMaps.joinToString(", ")}]")
        }
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeCollection(syncMaps) { buf, (syncId, map) ->
            buf.writeUtf(syncId)

            // Reserve 4 bytes for length
            val lengthIndex = buf.writerIndex()
            buf.writeInt(0)

            val startIndex = buf.writerIndex()
            val syncMap = CommonSyncRegistryImpl.instance.getMap<Any?, Any?>(syncId)
                ?: encodeError("SyncMap '$syncId' is not registered in SyncRegistry")
            syncMap.codec.encode(buf, map)
            val endIndex = buf.writerIndex()

            // Write the actual length of the encoded value
            buf.setInt(lengthIndex, endIndex - startIndex)
        }
    }

    override fun handle(listener: ClientSynchronizingPacketListener) {
        listener.handleBatchSyncMap(this)
    }
}
