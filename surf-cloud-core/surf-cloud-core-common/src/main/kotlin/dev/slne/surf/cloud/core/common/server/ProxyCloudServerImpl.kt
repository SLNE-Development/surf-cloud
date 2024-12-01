package dev.slne.surf.cloud.core.common.server

import dev.slne.surf.cloud.api.common.netty.network.codec.streamCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.api.common.server.ProxyCloudServer
import dev.slne.surf.cloud.api.common.server.UserListImpl
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientInformation

open class ProxyCloudServerImpl(
    uid: Long,
    group: String,
    name: String,
    users: UserListImpl = UserListImpl(),
    information: ClientInformation = ClientInformation.NOT_AVAILABLE
) : CommonCloudServerImpl(uid, group, name, users, information), ProxyCloudServer {
    companion object {
        val STREAM_CODEC = streamCodec<SurfByteBuf, ProxyCloudServerImpl>({ buf, server ->
            buf.writeVarLong(server.uid)
            buf.writeUtf(server.group)
            buf.writeUtf(server.name)
            UserListImpl.STREAM_CODEC.encode(buf, server.users)
            ClientInformation.STREAM_CODEC.encode(buf, server.information)
        }, { buf ->
            ProxyCloudServerImpl(
                buf.readVarLong(),
                buf.readUtf(),
                buf.readUtf(),
                UserListImpl.STREAM_CODEC.decode(buf),
                ClientInformation.STREAM_CODEC.decode(buf)
            )
        })
    }
}