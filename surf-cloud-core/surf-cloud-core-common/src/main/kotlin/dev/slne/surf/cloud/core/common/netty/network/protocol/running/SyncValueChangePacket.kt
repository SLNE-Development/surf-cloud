package dev.slne.surf.cloud.core.common.netty.network.protocol.running

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

    val originServerName: String?
    val syncId: String
    val value: Any?
    val registered: Boolean

    constructor(originServerName: String?, value: BasicSyncValue<*>) {
        this.originServerName = originServerName
        this.syncId = value.id
        this.value = value.get()
        this.registered = true
    }

    constructor(buf: SurfByteBuf) {
        this.originServerName = buf.readNullableString()
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
        buf.writeNullable(originServerName)
        buf.writeUtf(syncId)
        val codec = CommonSyncRegistryImpl.instance.getSyncValueCodec(syncId)
            ?: error("'$syncId' is not registered in SyncRegistry")

        codec.encode(buf, value)
    }

    override fun toString(): String {
        return "SyncValueChangePacket(" +
                "origin=$originServerName, " +
                "syncId='$syncId', " +
                "value=$value" +
                ")" +
                " ${super.toString()}"
    }

}