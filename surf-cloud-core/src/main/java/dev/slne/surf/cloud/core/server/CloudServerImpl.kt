package dev.slne.surf.cloud.core.server

import dev.slne.surf.cloud.api.netty.protocol.buffer.codec.Codec
import dev.slne.surf.cloud.api.netty.protocol.buffer.readVarInt
import dev.slne.surf.cloud.api.netty.protocol.buffer.writeVarInt
import dev.slne.surf.cloud.api.server.CloudServer
import dev.slne.surf.cloud.api.server.state.ServerState
import org.jetbrains.annotations.Range

class CloudServerImpl private constructor(
    override var groupId: String? = null,
    override var port: @Range(from = 0, to = 65535) Int = 0,
    override var host: String? = null,
    override var category: String? = null,
    override var serverGuid: Long = -1,
    override var currentPlayerCount: Int = 0,
    override var maxPlayerCount: Int = 0,
    override var state: ServerState = ServerState.OFFLINE,
) : CloudServer {

    companion object {
        val CODEC = Codec.codec<CloudServer>({ buf, value ->
            buf.writeLong(value.serverGuid)
            buf.writeNullable(value.host) // TODO: 15.09.2024 10:29 - why nullable?
            buf.writeInt(value.port)
            buf.writeNullable(value.category)
            buf.writeVarInt(value.currentPlayerCount)
            buf.writeVarInt(value.maxPlayerCount)
            buf.writeEnum(value.state)
            buf.writeNullable(value.groupId)
        }, { buf ->
            CloudServerImpl(
                serverGuid = buf.readLong(),
                host = buf.readNullableString(),
                port = buf.readInt(),
                category = buf.readNullableString(),
                currentPlayerCount = buf.readVarInt(),
                maxPlayerCount = buf.readVarInt(),
                state = buf.readEnum(ServerState::class),
                groupId = buf.readNullableString()
            )
        })
    }
}
