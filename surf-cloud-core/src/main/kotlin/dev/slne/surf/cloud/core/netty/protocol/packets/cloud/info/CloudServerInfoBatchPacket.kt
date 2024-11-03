package dev.slne.surf.cloud.core.netty.protocol.packets.cloud.info


//@SurfNettyPacket(id = DefaultIds.CLOUD_SERVER_INFO_BATCH_PACKET)
//class CloudServerInfoBatchPacket : NettyPacket<CloudServerInfoBatchPacket> {
//    lateinit var servers: List<CloudServer>
//        private set
//    lateinit var action: CloudServerInfoAction
//        private set
//
//    internal constructor()
//
//    constructor(action: CloudServerInfoAction, servers: Iterable<CloudServer>) {
//        this.action = action
//        this.servers = servers.toList()
//    }
//
//    @AutoDslConstructor
//    constructor(action: CloudServerInfoAction, vararg servers: CloudServer) {
//        this.action = action
//        this.servers = servers.toList()
//    }
//
//    override fun encode(buffer: SurfByteBuf) {
//        buffer.writeEnum(action)
//        buffer.writeCollectionWithCodec(CloudServerImpl.CODEC, servers)
//    }
//
//    override fun decode(buffer: SurfByteBuf): CloudServerInfoBatchPacket? {
//        action = buffer.readEnum(CloudServerInfoAction::class.java)
//        servers = buffer.readListWithCodec(CloudServerImpl.CODEC)
//        return this
//    }
//}
//
//@AutoDsl
//class Location {
//    val lat: Double
//    val lng: Double
//
//    constructor() {
//        lat = 0.0
//        lng = 0.0
//    }
//
//    // with multiple constructors you can specify which one to use.
//    @AutoDslConstructor
//    constructor(lat: Double, lng: Double) {
//        this.lat = lat
//        this.lng = lng
//    }
//}
