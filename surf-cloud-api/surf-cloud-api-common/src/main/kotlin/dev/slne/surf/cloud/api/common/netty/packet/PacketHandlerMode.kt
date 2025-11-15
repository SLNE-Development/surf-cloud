package dev.slne.surf.cloud.api.common.netty.packet

/**
 * Defines how a packet handler method should be executed when handling incoming Netty packets.
 *
 * This enum controls the coroutine context or thread in which the handler runs.
 * Depending on the selected mode, packet handling can occur directly on the Netty event loop,
 * or be dispatched to coroutine scopes for background or IO-intensive work.
 */
enum class PacketHandlerMode {
    /**
     * Executes directly on the Netty event loop thread.
     *
     * - No coroutine dispatching is used.
     * - The handler method **must not** be `suspend`.
     * - Intended for lightweight, non-blocking operations that must run in Netty’s thread context.
     */
    NETTY,

    /**
     * Executes within the PacketHandlerScope using the **Default** coroutine dispatcher.
     *
     * - Suitable for general-purpose or CPU-bound work.
     * - The handler method **may** be `suspend`.
     * - Keeps Netty threads unblocked by offloading work to the default coroutine dispatcher.
     */
    DEFAULT,

    /**
     * Executes within the PacketHandlerIoScope using the **IO** coroutine dispatcher.
     *
     * - Recommended for IO-bound operations such as database queries or network requests.
     * - The handler method **may** be `suspend`.
     */
    IO,

    /**
     * Used only in method-level annotations.
     *
     * - Inherits the default handler mode from the associated [dev.slne.surf.cloud.api.common.meta.SurfNettyPacket.handlerMode].
     * - This allows consistent behavior between packet definitions and their handlers.
     */
    INHERIT
}