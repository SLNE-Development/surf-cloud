package dev.slne.surf.cloud.api.client.velocity

import com.velocitypowered.api.proxy.Player
import dev.slne.surf.cloud.api.common.util.annotation.InternalApi
import dev.slne.surf.surfapi.core.api.util.requiredService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

@InternalApi
interface InternalCloudVelocityBridge {
    fun getPlayer(name: String): Player?

    fun launch(block: suspend CoroutineScope.() -> Unit): Job

    companion object {
        val instance = requiredService<InternalCloudVelocityBridge>()
    }
}