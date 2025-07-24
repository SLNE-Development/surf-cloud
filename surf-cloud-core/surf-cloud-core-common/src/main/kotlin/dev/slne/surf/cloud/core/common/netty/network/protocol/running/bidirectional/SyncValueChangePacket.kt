package dev.slne.surf.cloud.core.common.netty.network.protocol.running.bidirectional

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.core.common.sync.BasicSyncValue
import dev.slne.surf.cloud.core.common.sync.CommonSyncRegistryImpl

@SurfNettyPacket("cloud:sync_value_change", PacketFlow.BIDIRECTIONAL)
class SyncValueChangePacket : NettyPacket {

    companion object {
        val STREAM_CODEC = packetCodec(SyncValueChangePacket::write, ::SyncValueChangePacket)
    }

    val origin: Long?
    val syncId: String
    val value: Any?
    val registered: Boolean

    constructor(origin: Long?, value: BasicSyncValue<*>) {
        this.origin = origin
        this.syncId = value.id
        this.value = value.get()
        this.registered = true
    }

    constructor(buf: SurfByteBuf) {
        this.origin = buf.readNullableLong()
        this.syncId = buf.readUtf()
        val codec = CommonSyncRegistryImpl.instance.getSyncValueCodec(syncId)

        if (codec == null) {
            this.value = null
            this.registered = false
            buf.skipBytes(buf.readableBytes())
        } else {
            this.registered = true
            this.value = codec.decode(buf)
        }
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeNullable(origin)
        buf.writeUtf(syncId)
        val codec = CommonSyncRegistryImpl.instance.getSyncValueCodec(syncId)
            ?: error("'$syncId' is not registered in SyncRegistry")

        codec.encode(buf, value)
    }

    override fun toString(): String {
        return "SyncValueChangePacket(" +
                "origin=$origin, " +
                "syncId='$syncId', " +
                "value=$value" +
                ")" +
                " ${super.toString()}"
    }

}