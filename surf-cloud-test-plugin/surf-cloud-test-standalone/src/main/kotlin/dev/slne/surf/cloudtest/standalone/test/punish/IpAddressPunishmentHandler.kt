package dev.slne.surf.cloudtest.standalone.test.punish

import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.cloud.api.common.player.punishment.CloudPlayerPunishmentManager
import dev.slne.surf.cloud.api.common.player.punishment.PunishmentLoginValidation
import dev.slne.surf.cloud.api.common.player.punishment.PunishmentLoginValidation.Result
import org.springframework.stereotype.Component


@Component
class IpAddressPunishmentHandler : PunishmentLoginValidation {
    init {
        println("IP Address Punishment Handler initialized")
    }

    override suspend fun performLoginCheck(
        player: OfflineCloudPlayer,
        punishmentCache: PunishmentLoginValidation.PunishmentCache
    ): Result {
        val playerIp = player.latestIpAddress() ?: return Result.ERROR
        val ipBans = CloudPlayerPunishmentManager.fetchIpBans(playerIp, onlyActive = false)

        println("IP Bans: $ipBans")
        println("Player IP: $playerIp")

        if (ipBans.isEmpty()) {
            return Result.ALLOWED
        }

        // Split up the ip bans into active and inactive
        val (activeBans, inactiveBans) = ipBans.partition { it.active }

        // Loop over the inactive ip bans -> unbanned
        // If there is a security ban that is unbanned and the uuid matches the current uuid
        // the user is allowed to join
        val unbannedSecurityBan = inactiveBans.filter { it.securityBan }
            .any { it.punishedUuid == player.uuid }

        if (!unbannedSecurityBan) {
            // If there is no unbanned security ban and there are no active ip bans the user
            // is allowed to join
            if (activeBans.isEmpty()) {
                return Result.ALLOWED
            }

            // If there are active ip bans the user is not allowed to join
            // And a new security is issued
            val ban = player.punishmentManager.securityBan()
            return Result.DENIED(ban.punishmentPlayerComponent())
        }

        return Result.ALLOWED
    }
}