package dev.slne.surf.cloud.api.common.server

import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.ConnectionResultEnum
import it.unimi.dsi.fastutil.objects.ObjectList
import net.kyori.adventure.text.Component
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.Unmodifiable

/**
 * Represents a backend server within the cloud infrastructure.
 *
 * This interface is specifically designed for actual game servers like Paper,
 * providing metadata and management capabilities distinct from proxy servers.
 * Backend servers host the game world and handle player interactions.
 *
 * @see CommonCloudServer
 */
@ApiStatus.NonExtendable
interface CloudServer : CommonCloudServer {
    /**
     * Indicates whether the server has the allowlist enabled.
     *
     * When enabled, only players on the allowlist (whitelist) can join the server.
     */
    val allowlist: Boolean

    suspend fun pullPlayers(players: Collection<CloudPlayer>): @Unmodifiable ObjectList<Pair<CloudPlayer, ConnectionResultEnum>>
}