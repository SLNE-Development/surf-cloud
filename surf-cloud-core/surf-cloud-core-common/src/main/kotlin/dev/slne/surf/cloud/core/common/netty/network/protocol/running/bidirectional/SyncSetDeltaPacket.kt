package dev.slne.surf.cloud.core.common.netty.network.protocol.running.bidirectional

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.core.common.sync.CommonSyncRegistryImpl
import dev.slne.surf.cloud.core.common.sync.SyncSetImpl

@SurfNettyPacket(
    "cloud:sync_set_delta",
    PacketFlow.BIDIRECTIONAL,
)
class SyncSetDeltaPacket : NettyPacket {
    companion object {
        val STREAM_CODEC = packetCodec(SyncSetDeltaPacket::write, ::SyncSetDeltaPacket)
    }

    val setId: String
    val added: Boolean
    val changeId: Long
    val element: Any?
    val registered: Boolean

    constructor(
        syncSet: SyncSetImpl<*>,
        added: Boolean,
        changeId: Long,
        element: Any?
    ) {
        this.setId = syncSet.id
        this.added = added
        this.changeId = changeId
        this.element = element
        this.registered = true
    }

    constructor(buf: SurfByteBuf) {
        this.setId = buf.readUtf()
        this.added = buf.readBoolean()
        this.changeId = buf.readLong()
        val codec = CommonSyncRegistryImpl.instance.getSet<Any>(setId)?.valueCodec

        if (codec == null) {
            this.registered = false
            this.element = null
            buf.skipBytes(buf.readableBytes())
        } else {
            this.registered = true
            this.element = codec.decode(buf)
        }
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeUtf(setId)
        buf.writeBoolean(added)
        buf.writeLong(changeId)
        val codec = CommonSyncRegistryImpl.instance.getSet<Any?>(setId)
            ?: error("SyncSet '$setId' is not registered")
        codec.valueCodec.encode(buf, element)
    }
}