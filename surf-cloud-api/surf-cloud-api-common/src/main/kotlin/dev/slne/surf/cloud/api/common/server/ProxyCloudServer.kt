package dev.slne.surf.cloud.api.common.server

import org.jetbrains.annotations.ApiStatus

/**
 * Represents a proxy server within the cloud infrastructure.
 *
 * This interface is specifically designed for proxy servers like Velocity,
 * providing metadata and management capabilities distinct from those of a regular server.
 * Proxy servers act as intermediaries for player connections, routing traffic
 * between clients and backend servers.
 *
 * @see CommonCloudServer
 */
@ApiStatus.NonExtendable
interface ProxyCloudServer: CommonCloudServer