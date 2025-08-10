package dev.slne.surf.cloud.standalone.config.ktor

import dev.slne.surf.surfapi.core.api.util.random
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import java.util.*

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