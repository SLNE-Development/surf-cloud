package dev.slne.surf.cloud.core.netty.protocol.packets.cloud.info

import com.faendir.kotlin.autodsl.AutoDsl
import com.faendir.kotlin.autodsl.AutoDslConstructor
import dev.slne.surf.cloud.api.meta.DefaultIds
import dev.slne.surf.cloud.api.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.api.server.CloudServer
import dev.slne.surf.cloud.core.server.CloudServerImpl

@DslMarker
annotation class CloudServerInfoBatchPacketDsl

@SurfNettyPacket(id = DefaultIds.CLOUD_SERVER_INFO_BATCH_PACKET)
@AutoDsl(CloudServerInfoBatchPacketDsl::class)
class CloudServerInfoBatchPacket : NettyPacket<CloudServerInfoBatchPacket> {
    lateinit var servers: List<CloudServer>
        private set
    lateinit var action: CloudServerInfoAction
        private set

    internal constructor()

    constructor(action: CloudServerInfoAction, servers: Iterable<CloudServer>) {
        this.action = action
        this.servers = servers.toList()
    }

    @AutoDslConstructor
    constructor(action: CloudServerInfoAction, vararg servers: CloudServer) {
        this.action = action
        this.servers = servers.toList()
    }

    override fun encode(buffer: SurfByteBuf) {
        buffer.writeEnum(action)
        buffer.writeCollectionWithCodec(CloudServerImpl.CODEC, servers)
    }

    override fun decode(buffer: SurfByteBuf): CloudServerInfoBatchPacket? {
        action = buffer.readEnum(CloudServerInfoAction::class.java)
        servers = buffer.readListWithCodec(CloudServerImpl.CODEC)
        return this
    }
}

@AutoDsl
class Location {
    val lat: Double
    val lng: Double

    constructor() {
        lat = 0.0
        lng = 0.0
    }

    // with multiple constructors you can specify which one to use.
    @AutoDslConstructor
    constructor(lat: Double, lng: Double) {
        this.lat = lat
        this.lng = lng
    }
}
