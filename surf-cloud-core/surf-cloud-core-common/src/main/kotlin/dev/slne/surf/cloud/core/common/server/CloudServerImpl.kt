package dev.slne.surf.cloud.core.common.server

import dev.slne.surf.cloud.api.common.server.CloudServer

class CloudServerImpl(uid: Long, group: String, name: String) :
    CommonCloudServerImpl(uid, group, name), CloudServer {
    override val whitelist get() = information.whitelist
}