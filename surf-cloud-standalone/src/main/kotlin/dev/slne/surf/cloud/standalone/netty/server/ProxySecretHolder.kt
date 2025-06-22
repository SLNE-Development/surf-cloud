package dev.slne.surf.cloud.standalone.netty.server

import dev.slne.surf.cloud.api.server.netty.packet.broadcast
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.ClientboundSetVelocitySecretPacket
import dev.slne.surf.cloud.standalone.config.ProxyConfig.SecretConfig.SecretType
import dev.slne.surf.cloud.standalone.config.standaloneConfig
import dev.slne.surf.surfapi.core.api.util.random

object ProxySecretHolder {

    private var dynamicSecret = randomSecret()

    private fun randomSecret() = ByteArray(128).apply { random.nextBytes(this) }

    fun currentSecret(): ByteArray {
        val secretConfig = standaloneConfig.proxy.secretConfig
        return when (secretConfig.type) {
            SecretType.MANUAL -> secretConfig.manualSecret.toByteArray()
            SecretType.DYNAMIC -> dynamicSecret
        }
    }

    fun reloadSecret() {
        dynamicSecret = randomSecret()
        ClientboundSetVelocitySecretPacket(currentSecret()).broadcast()
    }
}