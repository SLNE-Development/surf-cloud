package dev.slne.surf.cloud.standalone.config.proxy

import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting

@ConfigSerializable
data class ProxyConfig(
    @Setting("secret")
    val secretConfig: SecretConfig = SecretConfig()
) {
    @ConfigSerializable
    data class SecretConfig(
        val type: SecretType = SecretType.DYNAMIC,
        val manualSecret: String = "",
    ) {
        enum class SecretType {
            MANUAL,
            DYNAMIC
        }
    }
}