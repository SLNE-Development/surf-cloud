package dev.slne.surf.cloud.api.common.player.teleport

import dev.slne.surf.cloud.api.common.util.ByIdMap.OutOfBoundsStrategy
import dev.slne.surf.cloud.api.common.util.IdRepresentable

enum class TeleportFlag(override val id: Int) : IdRepresentable {

    /**
     * Indicates that a player should not have their current open inventory closed when teleporting.
     */
    @Deprecated("Deprecated in Paper 1.21.10", level = DeprecationLevel.ERROR)
    RETAIN_OPEN_INVENTORY(1),

    /**
     * If all passengers should not be required to be removed prior to teleportation.
     */
    @Deprecated("Deprecated in Paper 1.21.10", level = DeprecationLevel.ERROR)
    RETAIN_PASSENGERS(2),

    /**
     * If the entity should not be dismounted if they are riding another entity.
     */
    @Deprecated("Deprecated in Paper 1.21.10", level = DeprecationLevel.ERROR)
    RETAIN_VEHICLE(3),

    /**
     * Configures the player to not lose velocity in their current rotation during the teleport.
     */
    VELOCITY_ROTATION(4),

    /**
     * Configures the player to not lose velocity in their x-axis during the teleport.
     */
    VELOCITY_X(5),

    /**
     * Configures the player to not lose velocity in their y-axis during the teleport.
     */
    VELOCITY_Y(6),

    /**
     * Configures the player to not lose velocity in their z-axis during the teleport.
     */
    VELOCITY_Z(7);

    companion object {
        val BY_ID = IdRepresentable.enumIdMap<TeleportFlag>(OutOfBoundsStrategy.DECODE_ERROR)
        val STREAM_CODEC = IdRepresentable.codec(BY_ID)
    }

}