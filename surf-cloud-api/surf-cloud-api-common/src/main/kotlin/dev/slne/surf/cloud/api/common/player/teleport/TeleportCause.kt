package dev.slne.surf.cloud.api.common.player.teleport

import dev.slne.surf.cloud.api.common.util.ByIdMap
import dev.slne.surf.cloud.api.common.util.IdRepresentable

enum class TeleportCause(override val id: Int) : IdRepresentable {
    /**
     * Indicates the teleporation was caused by a player throwing an Ender
     * Pearl
     */
    ENDER_PEARL(1),

    /**
     * Indicates the teleportation was caused by a player executing a
     * command
     */
    COMMAND(2),

    /**
     * Indicates the teleportation was caused by a plugin
     */
    PLUGIN(3),

    /**
     * Indicates the teleportation was caused by a player entering a
     * Nether portal
     */
    NETHER_PORTAL(4),

    /**
     * Indicates the teleportation was caused by a player entering an End
     * portal
     */
    END_PORTAL(5),

    /**
     * Indicates the teleportation was caused by a player teleporting to a
     * Entity/Player via the spectator menu
     */
    SPECTATE(6),

    /**
     * Indicates the teleportation was caused by a player entering an End
     * gateway
     */
    END_GATEWAY(7),

    /**
     * Indicates the teleportation was caused by a player exiting a vehicle
     */
    DISMOUNT(8),

    /**
     * Indicates the teleportation was caused by a player exiting a bed
     */
    EXIT_BED(9),

    CONSUMABLE_EFFECT(10),

    /**
     * Indicates the teleportation was caused by an event not covered by
     * this enum
     */
    UNKNOWN(11);

    companion object {
        val BY_ID = IdRepresentable.enumIdMap<TeleportCause>(ByIdMap.OutOfBoundsStrategy.LAST)
        val STREAM_CODEC = IdRepresentable.codec(BY_ID)
    }
}