package dev.slne.surf.cloud.standalone.config

import dev.slne.surf.cloud.core.common.coreCloudInstance
import dev.slne.surf.surfapi.core.api.config.createSpongeYmlConfig
import dev.slne.surf.surfapi.core.api.config.surfConfigApi
import dev.slne.surf.surfapi.core.api.util.random
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment
import org.spongepowered.configurate.objectmapping.meta.Setting
import java.util.Base64


val standaloneConfig: StandaloneConfig by lazy {
    surfConfigApi.createSpongeYmlConfig(coreCloudInstance.dataFolder, "standalone-config.yml")
}

@ConfigSerializable
data class StandaloneConfig(

    @Comment("Configuration for the Ktor server.")
    @Setting("ktor")
    val ktor: KtorConfig = KtorConfig(),

    @Comment("Whether only one proxy is used by the network. If enabled the server are automatically registered to the proxy.")
    @Setting("use-single-proxy-setup2")
    val useSingleProxySetup: Boolean = false,

    @Comment("Configuration for the logging.")
    @Setting("logging")
    val logging: LoggingConfig = LoggingConfig(),

    @Comment("common configuration for punishments")
    @Setting("punish")
    val punish: PunishmentConfig = PunishmentConfig(),
)

@ConfigSerializable
data class KtorConfig(
    val port: Int = 8080,
    val host: String = "0.0.0.0",
    val bearerToken: String = generateBearerToken()
) {
    companion object {
        private fun generateBearerToken(length: Int = 32): String {
            require(length >= 16) { "Token should be at least 16 characters long" }

            val randomBytes = ByteArray(length)
            random.nextBytes(randomBytes)

            val token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes)
            return "Bearer $token"
        }
    }
}

@ConfigSerializable
data class LoggingConfig(
    val logPlayerConnections: Boolean = true,
)

@ConfigSerializable
data class PunishmentConfig(
    val webhookUrls: List<String> = listOf()
)