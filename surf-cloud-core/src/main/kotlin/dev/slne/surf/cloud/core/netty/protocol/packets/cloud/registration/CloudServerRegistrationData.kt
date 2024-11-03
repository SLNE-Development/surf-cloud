package dev.slne.surf.cloud.core.netty.protocol.packets.cloud.registration

//data class CloudServerRegistrationData(
//    val serverId: Long,
//    val category: String?,
//    val port: Int,
//    val host: String?
//) {
//    companion object {
//        @JvmStatic
//        val CODEC = Codec.codec({ buf, data ->
//            buf.writeLong(data.serverId)
//            buf.writeNullable(data.category)
//            buf.writeInt(data.port)
//            buf.writeNullable(data.host)
//        }, { buf ->
//            CloudServerRegistrationData(
//                buf.readLong(),
//                buf.readNullableString(),
//                buf.readInt(),
//                buf.readNullableString()
//            )
//        })
//    }
//}
