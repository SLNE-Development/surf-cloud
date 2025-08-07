package dev.slne.surf.cloud.core.common.player.punishment

import dev.slne.surf.cloud.api.common.player.punishment.CloudPlayerPunishmentManager
import dev.slne.surf.cloud.api.common.player.punishment.type.NoteBuilder
import dev.slne.surf.cloud.api.common.player.punishment.type.PunishSpec
import dev.slne.surf.cloud.api.common.player.punishment.type.PunishType
import dev.slne.surf.cloud.api.common.player.punishment.type.Punishment
import dev.slne.surf.cloud.api.common.player.punishment.type.ban.PunishmentBan
import dev.slne.surf.cloud.api.common.player.punishment.type.kick.PunishmentKick
import dev.slne.surf.cloud.api.common.player.punishment.type.mute.PunishmentMute
import dev.slne.surf.cloud.api.common.player.punishment.type.warn.PunishmentWarn
import dev.slne.surf.cloud.api.common.util.emptyObjectList
import dev.slne.surf.cloud.api.common.util.toObjectList
import dev.slne.surf.cloud.core.common.player.PunishmentManager
import dev.slne.surf.cloud.core.common.player.punishment.type.PunishmentMuteImpl
import dev.slne.surf.cloud.core.common.util.bean
import dev.slne.surf.surfapi.core.api.util.logger
import it.unimi.dsi.fastutil.objects.ObjectList
import org.jetbrains.annotations.Unmodifiable
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

class CloudPlayerPunishmentManagerImpl(private val playerUuid: UUID) :
    CloudPlayerPunishmentManager {
    companion object {
        private val log = logger()
    }

    private var cachedMutes = CopyOnWriteArrayList<PunishmentMuteImpl>()

    @Volatile
    private var cachedMutesFetched = false

    override val isMuted: Boolean
        get() = cachedMutes.any { it.active }

    override suspend fun fetchBans(
        onlyActive: Boolean,
        sort: Boolean
    ): @Unmodifiable ObjectList<out PunishmentBan> {
        val fetched = bean<PunishmentManager>().fetchBans(playerUuid, onlyActive)
        return (if (sort) fetched.sorted() else fetched).toObjectList()
    }

    override suspend fun fetchMutes(
        onlyActive: Boolean,
        sort: Boolean
    ): @Unmodifiable ObjectList<out PunishmentMute> {
        val fetched = bean<PunishmentManager>().fetchMutes(playerUuid, onlyActive)
        return (if (sort) fetched.sorted() else fetched).toObjectList()
    }

    override fun cachedMutes(
        onlyActive: Boolean,
        sort: Boolean
    ): @Unmodifiable ObjectList<out PunishmentMute> {
        if (!cachedMutesFetched) {
            log.atWarning().log("Cached mutes not fetched yet, returning empty list")
            return emptyObjectList()
        }

        val mutes = if (onlyActive) cachedMutes.filter { it.active } else cachedMutes
        return (if (sort) mutes.sorted() else mutes).toObjectList()
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun cacheMutes() {
        cachedMutes = CopyOnWriteArrayList(fetchMutes() as ObjectList<PunishmentMuteImpl>)
        cachedMutesFetched = true
    }

    fun cacheMute(mute: PunishmentMuteImpl) {
        if (cachedMutesFetched) {
            cachedMutes.add(mute)
        } else {
            log.atWarning()
                .log("Cached mutes not fetched yet, mute %s will not be cached", mute)
        }
    }

    fun updateCachedMute(mute: PunishmentMuteImpl) {
        if (cachedMutesFetched) {
            cachedMutes.removeIf { it.punishmentId == mute.punishmentId }
            cachedMutes.add(mute)
        } else {
            log.atWarning()
                .log("Cached mutes not fetched yet, mute %s will not be updated", mute)
        }
    }

    override fun longestActiveMute(): PunishmentMute? {
        if (!cachedMutesFetched) {
            log.atWarning()
                .log("Cached mutes not fetched yet, longest active mute will not be returned")
            return null
        }

        return cachedMutes.maxOrNull()
    }

    override suspend fun fetchWarnings(): @Unmodifiable ObjectList<out PunishmentWarn> {
        return bean<PunishmentManager>().fetchWarnings(playerUuid).toObjectList()
    }

    override suspend fun fetchKicks(): @Unmodifiable ObjectList<out PunishmentKick> {
        return bean<PunishmentManager>().fetchKicks(playerUuid).toObjectList()
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun <P : Punishment, Spec : PunishSpec<P, Spec, Builder>, Builder : NoteBuilder> punish(
        spec: Spec,
        reason: String?,
        issuerUuid: UUID?
    ): P {
        val manager = bean<PunishmentManager>()
        val punishment: P = when (val type = spec.type) {
            is PunishType.BAN -> manager.createBan(
                punishedUuid = playerUuid,
                issuerUuid = issuerUuid,
                reason = reason,
                permanent = type.permanent,
                expirationDate = type.expirationDate,
                securityBan = type.security,
                raw = type.raw,
                initialNotes = spec.notes,
                initialIpAddresses = (spec as PunishSpec.BanSpec).ipAddresses
            )

            is PunishType.KICK -> manager.createKick(
                punishedUuid = playerUuid,
                issuerUuid = issuerUuid,
                reason = reason,
                initialNotes = spec.notes,
            )

            is PunishType.MUTE -> manager.createMute(
                punishedUuid = playerUuid,
                issuerUuid = issuerUuid,
                reason = reason,
                initialNotes = spec.notes,
                permanent = type.permanent,
                expirationDate = type.expirationDate,
            )

            is PunishType.WARN -> manager.createWarn(
                punishedUuid = playerUuid,
                issuerUuid = issuerUuid,
                reason = reason,
                initialNotes = spec.notes,
            )
        } as P

        return punishment
    }
}