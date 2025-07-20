package dev.slne.surf.cloud.core.common.server

import dev.slne.surf.cloud.api.common.server.ProxyCloudServer
import dev.slne.surf.cloud.api.common.server.UserListImpl
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientInformation
import java.net.InetSocketAddress

abstract class AbstractProxyCloudServer(
    uid: Long,
    group: String,
    name: String,
    playAddress: InetSocketAddress,
    users: UserListImpl = UserListImpl(),
    information: ClientInformation = ClientInformation.NOT_AVAILABLE
) : CommonCloudServerImpl(uid, group, name, users, playAddress, information), ProxyCloudServer