package dev.slne.surf.cloud.api.common.player.teleport

enum class TeleportFlag {

    /**
     * Indicates that a player should not have their current open inventory closed when teleporting.
     */
    RETAIN_OPEN_INVENTORY,

    /**
     * If all passengers should not be required to be removed prior to teleportation.
     */
    RETAIN_PASSENGERS,

    /**
     * If the entity should not be dismounted if they are riding another entity.
     */
    RETAIN_VEHICLE,

    /**
     * Configures the player to not lose velocity in their current rotation during the teleport.
     */
    VELOCITY_ROTATION,

    /**
     * Configures the player to not lose velocity in their x axis during the teleport.
     */
    VELOCITY_X,

    /**
     * Configures the player to not lose velocity in their y axis during the teleport.
     */
    VELOCITY_Y,

    /**
     * Configures the player to not lose velocity in their z axis during the teleport.
     */
    VELOCITY_Z

}