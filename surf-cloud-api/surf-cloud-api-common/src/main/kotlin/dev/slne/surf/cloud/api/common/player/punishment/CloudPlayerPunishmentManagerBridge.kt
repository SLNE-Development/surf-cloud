package dev.slne.surf.cloud.api.common.player.punishment

import dev.slne.surf.cloud.api.common.player.punishment.type.ban.PunishmentBan
import dev.slne.surf.cloud.api.common.util.annotation.InternalApi
import dev.slne.surf.surfapi.core.api.util.requiredService
import it.unimi.dsi.fastutil.objects.ObjectList
import org.jetbrains.annotations.Unmodifiable

@InternalApi
interface CloudPlayerPunishmentManagerBridge {

    fun registerLoginValidation(check: PunishmentLoginValidation)
    fun unregisterLoginValidation(check: PunishmentLoginValidation)

    suspend fun fetchIpBans(
        ip: String,
        onlyActive: Boolean,
        sort: Boolean
    ): @Unmodifiable ObjectList<out PunishmentBan>

    companion object {
        @InternalApi
        val instance = requiredService<CloudPlayerPunishmentManagerBridge>()
    }
}