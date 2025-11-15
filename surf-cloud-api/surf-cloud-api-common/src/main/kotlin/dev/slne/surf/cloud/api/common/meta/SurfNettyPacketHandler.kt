package dev.slne.surf.cloud.api.common.meta

import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import org.springframework.aot.hint.annotation.Reflective

/**
 * Marks a method as a Netty packet handler within a component.
 *
 * Methods annotated with this annotation are automatically registered as listeners
 * for incoming packets. The target packet type is inferred from the method’s
 * parameter type rather than being manually specified.
 *
 * The [mode] parameter controls how the handler method is executed —
 * either directly on the Netty event loop or in a coroutine dispatcher scope.
 *
 * Example:
 * ```kotlin
 * @SurfNettyPacketHandler(mode = PacketHandlerMode.IO)
 * suspend fun onPlayerUpdate(packet: PlayerUpdatePacket) {
 *     // handle packet asynchronously (IO-bound)
 * }
 * ```
 *
 * @property id
 * **Deprecated:** No longer used.
 * The packet type is now inferred from the method’s parameter type.
 *
 * @property mode
 * Defines how the handler is executed when processing the packet.
 * Defaults to [PacketHandlerMode.INHERIT], meaning it inherits the handler mode
 * from the corresponding [SurfNettyPacket.handlerMode].
 */
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(
    AnnotationRetention.RUNTIME
)
@Reflective
annotation class SurfNettyPacketHandler(
    @Deprecated(
        "Not used. The packet is determined based on the method parameter type.",
        level = DeprecationLevel.ERROR
    )
    val id: String = "",
    val mode: PacketHandlerMode = PacketHandlerMode.INHERIT
)
