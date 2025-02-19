package dev.slne.surf.cloud.api.common.server

import org.jetbrains.annotations.ApiStatus

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
}