package dev.slne.surf.cloud.api.common.player.task

import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component

interface PrePlayerJoinTask { // TODO: 19.04.2025 12:47 - implement
    suspend fun preJoin(player: OfflineCloudPlayer): Result

    @Serializable
    sealed interface Result {

        @Serializable
        data object ALLOWED: Result

        @Serializable
        data class DENIED(val reason: @Contextual Component): Result

        @Serializable
        data object ERROR: Result
    }

    companion object {
        const val PUNISHMENT_MANAGER = 500
        const val VELOCITY_PLAYER_JOIN_VALIDATION = 600
        const val MUTE_PUNISHMENT_LISTENER = 750
        const val ATTACH_IP_ADDRESS_HANDLER = 900
        const val PUNISHMENT_LOGIN_VALIDATION_HANDLER = 1000
    }
}