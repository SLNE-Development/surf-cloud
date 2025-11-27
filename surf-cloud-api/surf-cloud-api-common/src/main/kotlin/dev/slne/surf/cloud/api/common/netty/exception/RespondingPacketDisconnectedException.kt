package dev.slne.surf.cloud.api.common.netty.exception

/**
 * Exception thrown when a [dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket] can no longer receive a response
 * because the underlying connection has been closed (e.g., network disconnect,
 * client/server shutdown).
 *
 * This is different from a regular timeout:
 * - A timeout means the packet was sent successfully, but no response arrived
 *   within the expected time window.
 * - This exception means the request/response cycle is impossible to complete
 *   because the channel was closed and all pending responding packets were
 *   canceled proactively.
 *
 * Callers of fireAndAwait/fireAndAwaitOrThrow should handle this case explicitly
 * if they want to retry the operation after a reconnect, instead of treating it
 * as a simple timeout.
 */
class RespondingPacketDisconnectedException(message: String) : SurfNettyPacketException(message)