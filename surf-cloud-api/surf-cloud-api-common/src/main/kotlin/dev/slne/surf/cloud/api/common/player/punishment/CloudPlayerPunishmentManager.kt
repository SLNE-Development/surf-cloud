package dev.slne.surf.cloud.api.common.player.punishment

import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.cloud.api.common.player.punishment.type.*
import dev.slne.surf.cloud.api.common.player.punishment.type.ban.PunishmentBan
import dev.slne.surf.cloud.api.common.player.punishment.type.kick.PunishmentKick
import dev.slne.surf.cloud.api.common.player.punishment.type.mute.PunishmentMute
import dev.slne.surf.cloud.api.common.player.punishment.type.warn.PunishmentWarn
import it.unimi.dsi.fastutil.objects.ObjectList
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.ApiStatus.NonExtendable
import org.jetbrains.annotations.Unmodifiable
import java.net.InetAddress
import java.util.*

@NonExtendable
interface CloudPlayerPunishmentManager {

    companion object {
        const val ANTICHEAT_BAN_REASON = "Unerlaubte Clientmodifikation"
        const val SECURITY_BAN_REASON = "Security Ban"

        suspend fun fetchIpBans(
            ip: InetAddress,
            onlyActive: Boolean = true,
            sort: Boolean = false
        ) = fetchIpBans(ip.hostAddress, onlyActive, sort)

        suspend fun fetchIpBans(
            ip: String,
            onlyActive: Boolean = true,
            sort: Boolean = false
        ): @Unmodifiable ObjectList<out PunishmentBan> =
            CloudPlayerPunishmentManagerBridge.instance.fetchIpBans(ip, onlyActive, sort)

    }

    val isMuted: Boolean

    /**
     * Retrieves a list of (active) bans applied to the player, optionally sorted.
     * The returned list is immutable.
     *
     * @param onlyActive whether the method should only return active bans
     * @param sort whether to sort the bans by expiration date
     */
    suspend fun fetchBans(
        onlyActive: Boolean = true,
        sort: Boolean = false
    ): @Unmodifiable ObjectList<out PunishmentBan>

    suspend fun fetchMutes(
        onlyActive: Boolean = true,
        sort: Boolean = false
    ): @Unmodifiable ObjectList<out PunishmentMute>

    fun cachedMutes(
        onlyActive: Boolean = true,
        sort: Boolean = false
    ): @Unmodifiable ObjectList<out PunishmentMute>

    fun longestActiveMute(): PunishmentMute?

    suspend fun fetchWarnings(): @Unmodifiable ObjectList<out PunishmentWarn>
    suspend fun fetchKicks(): @Unmodifiable ObjectList<out PunishmentKick>

    suspend fun <P : Punishment, Spec : PunishSpec<P, Spec, Builder>, Builder : NoteBuilder<P>> punish(
        type: PunishType<P, Spec, Builder>,
        reason: String?,
        issuerUuid: UUID? = null
    ): P = punish(type.emptySpec(), reason, issuerUuid)

    suspend fun <P : Punishment, Spec : PunishSpec<P, Spec, Builder>, Builder : NoteBuilder<P>> punish(
        spec: Spec,
        reason: String?,
        issuerUuid: UUID? = null
    ): P

    suspend fun securityBan(banBuilder: BanBuilder.() -> Unit = {}) =
        punish(PunishType.BAN.Security(banBuilder), SECURITY_BAN_REASON)

    suspend fun anticheatBan(banBuilder: BanBuilder.() -> Unit = {}) =
        punish(PunishType.BAN.Permanent(banBuilder), ANTICHEAT_BAN_REASON)
}

interface PunishmentLoginValidation {

    @ApiStatus.OverrideOnly
    suspend fun performLoginCheck(
        player: OfflineCloudPlayer,
        punishmentCache: PunishmentCache
    ): Result

    @NonExtendable
    interface PunishmentCache {
        val mutes: List<PunishmentMute>
        val bans: List<PunishmentBan>
        val kicks: List<PunishmentKick>
        val warnings: List<PunishmentWarn>
    }

    @Serializable
    sealed interface Result {

        @Serializable
        data object ALLOWED : Result

        @Serializable
        data class DENIED(val reason: Component) : Result

        @Serializable
        data object ERROR : Result
    }
}