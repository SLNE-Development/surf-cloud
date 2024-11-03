package dev.slne.surf.cloud.core.netty.protocol.packets.cloud.registration

//@SurfNettyPacket(id = DefaultIds.CLOUD_REGISTER_SERVER_PACKET)
//class CloudRegisterServerPacket : NettyPacket<CloudRegisterServerPacket> {
//    lateinit var type: Type
//        private set
//    lateinit var data: CloudServerRegistrationData
//        private set
//
//    internal constructor()
//
//    constructor(type: Type, data: CloudServerRegistrationData) {
//        this.type = type
//        this.data = data
//    }
//
//    override fun encode(buffer: SurfByteBuf) {
//        buffer.writeEnum(type)
//        buffer.writeWithCodec(CloudServerRegistrationData.CODEC, data)
//    }
//
//    override fun decode(buffer: SurfByteBuf): CloudRegisterServerPacket? {
//        type = buffer.readEnum(Type::class)
//        data = buffer.readWithCodec(CloudServerRegistrationData.CODEC)
//        return this
//    }
//
//    enum class Type {
//        /**
//         * Requests preload
//         */
//        FETCH_PRELOAD,
//
//        /**
//         * Contains all information
//         */
//        PRELOAD
//    }
//}
