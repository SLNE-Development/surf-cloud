package dev.slne.surf.cloud.api.server

import dev.slne.surf.cloud.api.server.state.ServerState
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.Range
import java.net.InetSocketAddress
import javax.annotation.ParametersAreNonnullByDefault

@ApiStatus.NonExtendable
@ParametersAreNonnullByDefault
interface CloudServer {
    var groupId: String?
    var port: @Range(from = 0, to = 65535) Int
    var host: String?

    fun address() = "$host:$port"
    fun inetSocketAddress() = InetSocketAddress(host, port)


    fun inetSocketAddress(inetSocketAddress: InetSocketAddress) {
        host = inetSocketAddress.hostString
        port = inetSocketAddress.port
    }

    val renderedName: String
        get() = category + "-" + serverGuid.toULong().toString(16)

    //  UserList userList();
    var category: String?
    var serverGuid: Long
    var currentPlayerCount: Int
    var maxPlayerCount: Int
    var state: ServerState
}
