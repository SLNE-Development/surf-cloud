package dev.slne.surf.cloud.core.common.server

import dev.slne.surf.cloud.api.common.netty.network.codec.streamCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.ConnectionResultEnum
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.api.common.server.UserListImpl
import dev.slne.surf.cloud.api.common.util.emptyObjectList
import dev.slne.surf.cloud.api.common.util.mapAsync
import dev.slne.surf.cloud.api.common.util.objectListOf
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientInformation
import it.unimi.dsi.fastutil.objects.ObjectList
import kotlinx.coroutines.awaitAll
import org.jetbrains.annotations.Unmodifiable
import kotlin.to

open class CloudServerImpl(
    uid: Long,
    group: String,
    name: String,
    users: UserListImpl = UserListImpl(),
    information: ClientInformation = ClientInformation.NOT_AVAILABLE
) : CommonCloudServerImpl(uid, group, name, users, information), CloudServer {
    companion object {
        val STREAM_CODEC = streamCodec<SurfByteBuf, CloudServerImpl>({ buf, server ->
            buf.writeVarLong(server.uid)
            buf.writeUtf(server.group)
            buf.writeUtf(server.name)
            UserListImpl.STREAM_CODEC.encode(buf, server.users)
            ClientInformation.STREAM_CODEC.encode(buf, server.information)
        }, { buf ->
            CloudServerImpl(
                buf.readVarLong(),
                buf.readUtf(),
                buf.readUtf(),
                UserListImpl.STREAM_CODEC.decode(buf),
                ClientInformation.STREAM_CODEC.decode(buf)
            )
        })
    }

    override val allowlist get() = information.allowlist

    override suspend fun pullPlayers(players: Collection<CloudPlayer>): ObjectList<Pair<CloudPlayer, ConnectionResultEnum>> {
        if (players.isEmpty()) return emptyObjectList()
        if (players.size == 1) {
            val player = players.first()
            val (result, _) = player.connectToServer(this)
            return objectListOf(player to result)
        }

        val results = players.mapAsync { it to it.connectToServer(this) }.awaitAll()
            .map { (player, rawResult) ->
                val (result, _) = rawResult
                player to result
            }

        return objectListOf(*results.toTypedArray())
    }
}