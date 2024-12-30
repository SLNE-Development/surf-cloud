package dev.slne.surf.cloud.standalone.netty.server

import dev.slne.surf.cloud.api.common.util.mutableObjectListOf
import dev.slne.surf.cloud.api.common.util.synchronize
import dev.slne.surf.cloud.standalone.server.StandaloneCloudServerImpl
import dev.slne.surf.cloud.standalone.server.StandaloneProxyCloudServerImpl

object ProxyServerAutoregistration {
    private val pendingClients = mutableObjectListOf<StandaloneCloudServerImpl>().synchronize()
    private var proxy: StandaloneProxyCloudServerImpl? = null

    val hasProxy get() = proxy != null

    fun registerClient(client: StandaloneCloudServerImpl) {
        if (proxy == null) {
            pendingClients.add(client)
            return
        }

        proxy!!.registerClients(client)
    }

    fun setProxy(proxy: StandaloneProxyCloudServerImpl) {
        this.proxy = proxy
        proxy.registerClients(*pendingClients.toTypedArray())
        pendingClients.clear()
    }

    fun clearProxy() {
        proxy = null
    }
}