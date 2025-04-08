package dev.slne.surf.cloud.api.common.player.teleport

enum class TeleportCause {
    /**
     * Indicates the teleporation was caused by a player throwing an Ender
     * Pearl
     */
    ENDER_PEARL,

    /**
     * Indicates the teleportation was caused by a player executing a
     * command
     */
    COMMAND,

    /**
     * Indicates the teleportation was caused by a plugin
     */
    PLUGIN,

    /**
     * Indicates the teleportation was caused by a player entering a
     * Nether portal
     */
    NETHER_PORTAL,

    /**
     * Indicates the teleportation was caused by a player entering an End
     * portal
     */
    END_PORTAL,

    /**
     * Indicates the teleportation was caused by a player teleporting to a
     * Entity/Player via the spectator menu
     */
    SPECTATE,

    /**
     * Indicates the teleportation was caused by a player entering an End
     * gateway
     */
    END_GATEWAY,

    /**
     * Indicates the teleportation was caused by a player consuming chorus
     * fruit
     */
    CHORUS_FRUIT,

    /**
     * Indicates the teleportation was caused by a player exiting a vehicle
     */
    DISMOUNT,

    /**
     * Indicates the teleportation was caused by a player exiting a bed
     */
    EXIT_BED,

    /**
     * Indicates the teleportation was caused by an event not covered by
     * this enum
     */
    UNKNOWN
}