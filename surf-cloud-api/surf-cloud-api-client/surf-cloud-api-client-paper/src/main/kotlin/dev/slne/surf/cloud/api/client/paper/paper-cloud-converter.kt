package dev.slne.surf.cloud.api.client.paper

import dev.slne.surf.cloud.api.common.player.teleport.TeleportCause
import dev.slne.surf.cloud.api.common.player.teleport.TeleportLocation
import io.papermc.paper.entity.TeleportFlag
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.event.player.PlayerTeleportEvent
import dev.slne.surf.cloud.api.common.player.teleport.TeleportFlag as CloudTeleportFlag

fun Location.toCloudTpLocation() = TeleportLocation(world.uid, x, y, z, yaw, pitch)
fun TeleportLocation.toLocation() = Location(Bukkit.getWorld(world), x, y, z, yaw, pitch)
fun PlayerTeleportEvent.TeleportCause.toCloudTpCause(): TeleportCause = when (this) {
    PlayerTeleportEvent.TeleportCause.PLUGIN -> TeleportCause.PLUGIN
    PlayerTeleportEvent.TeleportCause.COMMAND -> TeleportCause.COMMAND
    PlayerTeleportEvent.TeleportCause.UNKNOWN -> TeleportCause.UNKNOWN
    PlayerTeleportEvent.TeleportCause.DISMOUNT -> TeleportCause.DISMOUNT
    PlayerTeleportEvent.TeleportCause.SPECTATE -> TeleportCause.SPECTATE
    PlayerTeleportEvent.TeleportCause.EXIT_BED -> TeleportCause.EXIT_BED
    PlayerTeleportEvent.TeleportCause.END_PORTAL -> TeleportCause.END_PORTAL
    PlayerTeleportEvent.TeleportCause.ENDER_PEARL -> TeleportCause.ENDER_PEARL
    PlayerTeleportEvent.TeleportCause.END_GATEWAY -> TeleportCause.END_GATEWAY
    PlayerTeleportEvent.TeleportCause.NETHER_PORTAL -> TeleportCause.NETHER_PORTAL
    PlayerTeleportEvent.TeleportCause.CONSUMABLE_EFFECT -> TeleportCause.CONSUMABLE_EFFECT
}

fun TeleportCause.toBukkitTpCause(): PlayerTeleportEvent.TeleportCause = when (this) {
    TeleportCause.PLUGIN -> PlayerTeleportEvent.TeleportCause.PLUGIN
    TeleportCause.COMMAND -> PlayerTeleportEvent.TeleportCause.COMMAND
    TeleportCause.UNKNOWN -> PlayerTeleportEvent.TeleportCause.UNKNOWN
    TeleportCause.DISMOUNT -> PlayerTeleportEvent.TeleportCause.DISMOUNT
    TeleportCause.SPECTATE -> PlayerTeleportEvent.TeleportCause.SPECTATE
    TeleportCause.EXIT_BED -> PlayerTeleportEvent.TeleportCause.EXIT_BED
    TeleportCause.END_PORTAL -> PlayerTeleportEvent.TeleportCause.END_PORTAL
    TeleportCause.ENDER_PEARL -> PlayerTeleportEvent.TeleportCause.ENDER_PEARL
    TeleportCause.END_GATEWAY -> PlayerTeleportEvent.TeleportCause.END_GATEWAY
    TeleportCause.NETHER_PORTAL -> PlayerTeleportEvent.TeleportCause.NETHER_PORTAL
    TeleportCause.CONSUMABLE_EFFECT -> PlayerTeleportEvent.TeleportCause.CONSUMABLE_EFFECT
}

fun TeleportFlag.toCloudTpFlag(): CloudTeleportFlag = when (this) {
    TeleportFlag.Relative.VELOCITY_X -> CloudTeleportFlag.VELOCITY_X
    TeleportFlag.Relative.VELOCITY_Y -> CloudTeleportFlag.VELOCITY_Y
    TeleportFlag.Relative.VELOCITY_Z -> CloudTeleportFlag.VELOCITY_Z
    TeleportFlag.Relative.VELOCITY_ROTATION -> CloudTeleportFlag.VELOCITY_ROTATION
    TeleportFlag.EntityState.RETAIN_VEHICLE -> CloudTeleportFlag.RETAIN_VEHICLE
    TeleportFlag.EntityState.RETAIN_PASSENGERS -> CloudTeleportFlag.RETAIN_PASSENGERS
    TeleportFlag.EntityState.RETAIN_OPEN_INVENTORY -> CloudTeleportFlag.RETAIN_OPEN_INVENTORY
}

fun CloudTeleportFlag.toBukkitTpFlag(): TeleportFlag = when (this) {
    CloudTeleportFlag.VELOCITY_X -> TeleportFlag.Relative.VELOCITY_X
    CloudTeleportFlag.VELOCITY_Y -> TeleportFlag.Relative.VELOCITY_Y
    CloudTeleportFlag.VELOCITY_Z -> TeleportFlag.Relative.VELOCITY_Z
    CloudTeleportFlag.VELOCITY_ROTATION -> TeleportFlag.Relative.VELOCITY_ROTATION
    CloudTeleportFlag.RETAIN_VEHICLE -> TeleportFlag.EntityState.RETAIN_VEHICLE
    CloudTeleportFlag.RETAIN_PASSENGERS -> TeleportFlag.EntityState.RETAIN_PASSENGERS
    CloudTeleportFlag.RETAIN_OPEN_INVENTORY -> TeleportFlag.EntityState.RETAIN_OPEN_INVENTORY
}
