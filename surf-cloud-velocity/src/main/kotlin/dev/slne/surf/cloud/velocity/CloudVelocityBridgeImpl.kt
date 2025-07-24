package dev.slne.surf.cloud.velocity

import com.google.auto.service.AutoService
import com.velocitypowered.api.proxy.Player
import dev.slne.surf.cloud.api.client.velocity.InternalCloudVelocityBridge
import dev.slne.surf.cloud.core.common.coroutines.CommonScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@AutoService(InternalCloudVelocityBridge::class)
class CloudVelocityBridgeImpl : InternalCloudVelocityBridge {
    override fun getPlayer(name: String): Player? {
        return plugin.server.getPlayer(name).orElse(null)
    }

    override fun launch(block: suspend CoroutineScope.() -> Unit): Job {
        return CommonScope.launch(block = block)
    }
}