package dev.slne.surf.cloud.core.common.server

import dev.slne.surf.cloud.api.common.netty.network.codec.streamCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.api.common.server.UserListImpl
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientInformation

open class CloudServerImpl(
    override val uid: Long,
    override val group: String,
    override val name: String,
    override val proxy: Boolean,
    override val users: UserListImpl = UserListImpl(),

    @Volatile
    var information: ClientInformation = ClientInformation.NOT_AVAILABLE
) : CloudServer {
    companion object {
        val STREAM_CODEC = streamCodec<SurfByteBuf, CloudServerImpl>({ buf, server ->
            buf.writeVarLong(server.uid)
            buf.writeUtf(server.group)
            buf.writeUtf(server.name)
            buf.writeBoolean(server.proxy)
            UserListImpl.STREAM_CODEC.encode(buf, server.users)
            ClientInformation.STREAM_CODEC.encode(buf, server.information)
        }, { buf ->
            CloudServerImpl(
                buf.readVarLong(),
                buf.readUtf(),
                buf.readUtf(),
                buf.readBoolean(),
                UserListImpl.STREAM_CODEC.decode(buf),
                ClientInformation.STREAM_CODEC.decode(buf)
            )
        })
    }

    override suspend fun sendAll(server: CloudServer) {
        TODO("Not yet implemented")
    }

    override suspend fun sendAll(
        server: CloudServer,
        filter: (CloudPlayer) -> Boolean
    ) {
        TODO("Not yet implemented")
    }

    override fun shutdown() {
        TODO("Not yet implemented")
    }

    override val maxPlayerCount get() = information.maxPlayerCount
    override val currentPlayerCount get() = users.size
    override val whitelist get() = information.whitelist
    override val state get() = information.state

    override fun audiences() = users
    override fun toString(): String {
        return "CloudServerImpl(group='$group', uid=$uid, name='$name', proxy=$proxy, users=$users, information=$information, maxPlayerCount=$maxPlayerCount, currentPlayerCount=$currentPlayerCount, whitelist=$whitelist, state=$state)"
    }


}
