package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.encodeError
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.CommonSynchronizingRunningPacketListener
import dev.slne.surf.cloud.core.common.sync.CommonSyncRegistryImpl
import dev.slne.surf.cloud.core.common.sync.SyncMapImpl

@SurfNettyPacket(
    "cloud:sync_map_delta",
    PacketFlow.BIDIRECTIONAL,
    handlerMode = PacketHandlerMode.DEFAULT
)
class SyncMapDeltaPacket : NettyPacket,
    InternalNettyPacket<CommonSynchronizingRunningPacketListener> {
    companion object {
        val STREAM_CODEC = packetCodec(SyncMapDeltaPacket::write, ::SyncMapDeltaPacket)
    }

    val mapId: String
    val key: Any?
    val oldValue: Any?
    val newValue: Any?
    val changeId: Long
    val registered: Boolean

    constructor(
        syncMap: SyncMapImpl<*, *>,
        key: Any?,
        oldValue: Any?,
        newValue: Any?,
        changeId: Long
    ) {
        this.mapId = syncMap.id
        this.key = key
        this.oldValue = oldValue
        this.newValue = newValue
        this.changeId = changeId
        this.registered = true
    }

    constructor(buf: SurfByteBuf) {
        this.mapId = buf.readUtf()
        this.changeId = buf.readLong()
        val map = CommonSyncRegistryImpl.instance.getMap<Any?, Any?>(mapId)

        if (map == null) {
            this.registered = false
            this.key = null
            this.oldValue = null
            this.newValue = null
            buf.skipBytes(buf.readableBytes())
        } else {
            this.registered = true
            this.key = map.keyCodec.decode(buf)
            val hasOldValue = buf.readBoolean()
            this.oldValue = if (hasOldValue) map.valueCodec.decode(buf) else null
            val hasNewValue = buf.readBoolean()
            this.newValue = if (hasNewValue) map.valueCodec.decode(buf) else null
        }
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeUtf(mapId)
        buf.writeLong(changeId)
        val map = CommonSyncRegistryImpl.instance.getMap<Any?, Any?>(mapId)
            ?: encodeError("SyncMap '$mapId' is not registered")
        
        map.keyCodec.encode(buf, key)
        
        if (oldValue != null) {
            buf.writeBoolean(true)
            map.valueCodec.encode(buf, oldValue)
        } else {
            buf.writeBoolean(false)
        }
        
        if (newValue != null) {
            buf.writeBoolean(true)
            map.valueCodec.encode(buf, newValue)
        } else {
            buf.writeBoolean(false)
        }
    }

    override fun handle(listener: CommonSynchronizingRunningPacketListener) {
        listener.handleSyncMapDelta(this)
    }
}
