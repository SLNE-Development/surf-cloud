package dev.slne.surf.cloud.standalone.config

import dev.slne.surf.cloud.core.common.coreCloudInstance
import dev.slne.surf.surfapi.core.api.config.createSpongeYmlConfig
import dev.slne.surf.surfapi.core.api.config.surfConfigApi
import dev.slne.surf.surfapi.core.api.util.random
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment
import org.spongepowered.configurate.objectmapping.meta.Setting
import java.util.*


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

    @Comment("Configuration for the queue system.")
    @Setting("queue")
    val queue: QueueConfig = QueueConfig(),

    @Setting("proxy")
    val proxy: ProxyConfig = ProxyConfig(),

    @Setting("whitelist")
    val whitelist: WhitelistConfig = WhitelistConfig(),
)

@ConfigSerializable
data class KtorConfig(
    val port: Int = 8080,
    val host: String = "0.0.0.0",
    val bearerToken: String = generateBearerToken()
) {
    companion object {
        private fun generateBearerToken(length: Int = 32): String {
            // The default token length of 32 provides a good balance between security and usability.
            // A minimum length of 16 is enforced to ensure sufficient entropy for security purposes.
            require(length >= 16) { "Token should be at least 16 characters long" }

            val randomBytes = ByteArray(length)
            random.nextBytes(randomBytes)

            // Base64 encoding is used to make the token URL-safe and compact while preserving randomness.
            val token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes)
            return token
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

@ConfigSerializable
data class QueueConfig(
    val maxConnectionAttempts: Int = 3,
    val multiQueue: Boolean = true,
    val allowJoiningSuspendedQueue: Boolean = false,
    val removePlayerOnServerSwitch: Boolean = true,

    /** How many minutes a queue is held after the last access. */
    val cacheRetainMinutes: Int = 30,

    val suspendedQueueCharacter: Char = '⏸',
)

@ConfigSerializable
data class ProxyConfig(
    @Setting("secret")
    val secretConfig: SecretConfig = SecretConfig())
{
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

@ConfigSerializable
data class WhitelistConfig(
    val enforcedGroups: MutableSet<String> = mutableSetOf(),
    val enforcedServers: MutableSet<String> = mutableSetOf(),
)