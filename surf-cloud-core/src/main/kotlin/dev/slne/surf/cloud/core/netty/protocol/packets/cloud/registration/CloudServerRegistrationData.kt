package dev.slne.surf.cloud.core.netty.protocol.packets.cloud.registration

import dev.slne.surf.cloud.api.netty.protocol.buffer.codec.Codec

data class CloudServerRegistrationData(
    val serverId: Long,
    val category: String?,
    val port: Int,
    val host: String?
) {
    companion object {
        @JvmStatic
        val CODEC = Codec.codec({ buf, data ->
            buf.writeLong(data.serverId)
            buf.writeNullable(data.category)
            buf.writeInt(data.port)
            buf.writeNullable(data.host)
        }, { buf ->
            CloudServerRegistrationData(
                buf.readLong(),
                buf.readNullableString(),
                buf.readInt(),
                buf.readNullableString()
            )
        })
    }
}
