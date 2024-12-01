package dev.slne.surf.cloud.core.client.server

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.common.server.CloudServerManager
import dev.slne.surf.cloud.api.common.server.CommonCloudServer
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientInformation
import dev.slne.surf.cloud.core.common.server.CommonCloudServerImpl
import dev.slne.surf.cloud.core.common.server.CommonCloudServerManagerImpl

@AutoService(CloudServerManager::class)
class ClientCloudServerManagerImpl : CommonCloudServerManagerImpl<CommonCloudServer>() {
    fun updateServerInformationNow(uid: Long, information: ClientInformation) {
        (servers[uid] as? CommonCloudServerImpl)?.information = information
    }
}

val serverManagerImpl get() = CloudServerManager.instance as ClientCloudServerManagerImpl