package dev.slne.surf.cloud.core.common.server

import dev.slne.surf.cloud.api.common.server.ProxyCloudServer

class ProxyCloudServerImpl(uid: Long, group: String, name: String) :
    CommonCloudServerImpl(uid, group, name), ProxyCloudServer