package dev.slne.surf.cloud.standalone.netty.server

import dev.slne.surf.cloud.api.server.netty.packet.broadcast
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.ClientboundSetVelocitySecretPacket
import dev.slne.surf.cloud.core.common.util.bean
import dev.slne.surf.cloud.standalone.config.StandaloneConfigHolder
import dev.slne.surf.cloud.standalone.config.proxy.ProxyConfig.SecretConfig.SecretType
import org.apache.commons.lang3.RandomStringUtils

object ProxySecretHolder {

    private var dynamicSecret = randomSecret()

    fun currentSecret(): ByteArray {
        val secretConfig = bean<StandaloneConfigHolder>().config.proxy.secretConfig
        return when (secretConfig.type) {
            SecretType.MANUAL -> secretConfig.manualSecret.toByteArray()
            SecretType.DYNAMIC -> dynamicSecret
        }
    }

    fun reloadSecret() {
        dynamicSecret = randomSecret()
        ClientboundSetVelocitySecretPacket(currentSecret()).broadcast()
    }

    private fun randomSecret() = RandomStringUtils.secureStrong().next(128).toByteArray()
}