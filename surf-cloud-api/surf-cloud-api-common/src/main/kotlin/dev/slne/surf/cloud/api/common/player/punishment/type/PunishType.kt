package dev.slne.surf.cloud.api.common.player.punishment.type

import dev.slne.surf.cloud.api.common.player.punishment.type.PunishSpec.BanSpec
import dev.slne.surf.cloud.api.common.player.punishment.type.PunishSpec.BasicSpec
import dev.slne.surf.cloud.api.common.player.punishment.type.ban.PunishmentBan
import dev.slne.surf.cloud.api.common.player.punishment.type.kick.PunishmentKick
import dev.slne.surf.cloud.api.common.player.punishment.type.mute.PunishmentMute
import dev.slne.surf.cloud.api.common.player.punishment.type.warn.PunishmentWarn
import dev.slne.surf.cloud.api.common.util.annotation.InternalApi
import java.net.InetSocketAddress
import java.time.ZonedDateTime

sealed interface PunishType<P : Punishment, Spec : PunishSpec<P, Spec, Builder>, Builder : NoteBuilder> {

    data object WARN :
        PunishType<PunishmentWarn, BasicSpec<PunishmentWarn, NoteBuilder>, NoteBuilder> {
        override fun invoke(block: NoteBuilder.() -> Unit) =
            BasicSpec(this, NoteBuilder().apply(block).notes)

        override fun withNote(note: String) = BasicSpec(this, listOf(note))
        override fun emptySpec(): BasicSpec<PunishmentWarn, NoteBuilder> =
            BasicSpec(this, emptyList())
    }

    data object KICK :
        PunishType<PunishmentKick, BasicSpec<PunishmentKick, NoteBuilder>, NoteBuilder> {
        override fun invoke(block: NoteBuilder.() -> Unit) =
            BasicSpec(this, NoteBuilder().apply(block).notes)

        override fun withNote(note: String) = BasicSpec(this, listOf(note))
        override fun emptySpec(): BasicSpec<PunishmentKick, NoteBuilder> =
            BasicSpec(this, emptyList())
    }

    @PunishDsl
    operator fun invoke(
        block: Builder.() -> Unit
    ): Spec

    fun withNote(note: String): Spec

    @InternalApi
    fun emptySpec(): Spec

    sealed interface BAN : PunishType<PunishmentBan, BanSpec, BanBuilder> {
        val permanent: Boolean
        val expirationDate: ZonedDateTime?
        val security: Boolean
        val raw: Boolean

        @PunishDsl
        override fun invoke(
            block: BanBuilder.() -> Unit
        ): BanSpec = BanBuilder().apply(block).let {
            BanSpec(this, it.notes, it.ips)
        }

        override fun withNote(note: String): BanSpec =
            BanSpec(this, listOf(note))

        fun withIpAddress(ip: String): BanSpec =
            BanSpec(this, emptyList(), listOf(ip))

        override fun emptySpec(): BanSpec = BanSpec(this, emptyList(), emptyList())

        data object Permanent : BAN {
            override val permanent = true
            override val expirationDate = null
            override val security = false
            override val raw = false
        }

        data object Security : BAN {
            override val permanent = false
            override val expirationDate = null
            override val security = true
            override val raw = false
        }

        @Deprecated("If you are accessing this, you are doing something wrong. Use a proper ban type.")
        data object Raw : BAN {
            override val permanent = false
            override val expirationDate = null
            override val security = false
            override val raw = true
        }

        data class Expirable(val until: ZonedDateTime) : BAN {
            override val permanent = false
            override val expirationDate = until
            override val security = false
            override val raw = false
        }
    }

    sealed interface MUTE :
        PunishType<PunishmentMute, BasicSpec<PunishmentMute, NoteBuilder>, NoteBuilder> {
        val permanent: Boolean
        val expirationDate: ZonedDateTime?

        override fun invoke(block: NoteBuilder.() -> Unit) =
            BasicSpec(this, NoteBuilder().apply(block).notes)

        override fun withNote(note: String) = BasicSpec(this, listOf(note))

        override fun emptySpec(): BasicSpec<PunishmentMute, NoteBuilder> = BasicSpec(this, emptyList())

        data object Permanent : MUTE {
            override val permanent = true
            override val expirationDate = null
        }

        data class Expirable(val until: ZonedDateTime) : MUTE {
            override val permanent = false
            override val expirationDate = until
        }
    }
}

sealed interface PunishSpec<P : Punishment, SELF : PunishSpec<P, SELF, Builder>, Builder : NoteBuilder> {
    val type: PunishType<P, SELF, Builder>
    val notes: List<String>

    data class BasicSpec<T : Punishment, Builder : NoteBuilder>(
        override val type: PunishType<T, BasicSpec<T, Builder>, Builder>,
        override val notes: List<String> = emptyList()
    ) : PunishSpec<T, BasicSpec<T, Builder>, Builder> {
        fun withNote(note: String) = copy(notes = notes + note)
    }

    data class BanSpec(
        override val type: PunishType.BAN,
        override val notes: List<String> = emptyList(),
        val ipAddresses: List<String> = emptyList()
    ) : PunishSpec<PunishmentBan, BanSpec, BanBuilder> {
        fun withNote(note: String) = copy(notes = notes + note)
        fun withIpAddress(ip: String) = copy(ipAddresses = ipAddresses + ip)
        fun withIpAddress(ip: InetSocketAddress) = withIpAddress(ip.hostName)
    }
}

@DslMarker
annotation class PunishDsl

@PunishDsl
open class NoteBuilder internal constructor() {
    internal val notes = mutableListOf<String>()
    fun note(text: String) {
        notes += text
    }
}

@PunishDsl
class BanBuilder : NoteBuilder() {
    internal val ips = mutableListOf<String>()
    fun ipAddress(addr: String) {
        ips += addr
    }
}

