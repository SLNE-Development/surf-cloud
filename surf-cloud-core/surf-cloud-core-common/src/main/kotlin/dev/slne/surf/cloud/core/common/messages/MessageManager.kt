package dev.slne.surf.cloud.core.common.messages

import dev.slne.surf.cloud.api.common.player.CloudPlayerManager
import dev.slne.surf.cloud.api.common.player.punishment.type.ExpirablePunishment
import dev.slne.surf.cloud.api.common.player.punishment.type.Punishment
import dev.slne.surf.cloud.api.common.player.punishment.type.ban.PunishmentBan
import dev.slne.surf.cloud.api.common.player.punishment.type.kick.PunishmentKick
import dev.slne.surf.cloud.api.common.player.punishment.type.mute.PunishmentMute
import dev.slne.surf.cloud.api.common.player.punishment.type.warn.PunishmentWarn
import dev.slne.surf.cloud.api.common.player.toOfflineCloudPlayer
import dev.slne.surf.cloud.api.common.server.CloudServerManager
import dev.slne.surf.cloud.core.common.permission.CommonCloudPermissions
import dev.slne.surf.surfapi.core.api.messages.CommonComponents
import dev.slne.surf.surfapi.core.api.messages.adventure.appendNewline
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.surfapi.core.api.messages.adventure.text
import dev.slne.surf.surfapi.core.api.messages.builder.SurfComponentBuilder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import org.apache.commons.text.WordUtils
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

object MessageManager { // TODO: Add more messages
    private val BERLIN_ZONE_ID = ZoneId.of("Europe/Berlin")

    val serverStarting = buildText {
        error("Der Server wird gerade gestartet. Bitte warte einen Moment.")
    }

    val unknownErrorDuringLogin = buildText {
        CommonComponents.renderDisconnectMessage(
            this,
            "UNBEKANNTER VERBINDUNGSFEHLER",
            {
                error("Beim Verbindungsaufbau ist")
                appendNewline()
                error("ein unbekannter Fehler aufgetreten.")
            },
            true
        )
    }

    val loginTimedOut = buildText {
        CommonComponents.renderDisconnectMessage(
            this,
            "VERBINDUNG ZUR SERVER-INSTANZ GETRENNT",
            {
                error("Die Verbindung zur Server-Instanz")
                appendNewline()
                error("wurde unerwartet getrennt.")
            },
            true
        )
    }

    val noServersAvailableToJoin = buildText {
        CommonComponents.renderDisconnectMessage(
            this,
            "KEINE SERVER ZUM BETRETEN VERFÜGBAR",
            {
                error("Es sind aktuell keine Server verfügbar,")
                appendNewline()
                error("denen du beitreten kannst.")
            },
            true
        )
    }

    fun formatZonedDateTime(time: ZonedDateTime?) = buildText {
        if (time == null) {
            variableValue("N/A")
            return@buildText
        }

        val berlinTime = time.withZoneSameInstant(BERLIN_ZONE_ID)
        fun padded(i: Int) = variableValue("%02d".format(i))

        padded(berlinTime.dayOfMonth)
        spacer(".")
        padded(berlinTime.monthValue)
        spacer(".")
        variableValue(berlinTime.year)
        appendSpace()
        padded(berlinTime.hour)
        spacer(":")
        padded(berlinTime.minute)
        variableValue(" Uhr")
    }

    object Queue {
        fun getMaxRetriesReached(serverName: String, maxRetries: Int) = buildText {
            appendPrefix()
            append {
                error("Du konntest nicht mit dem Server")
                variableValue(serverName)
                error("verbunden werden, ")
            }
            appendNewPrefixedLine()
            error("da das Maximum an Versuchen ($maxRetries) erreicht wurde.")
        }

        fun getQueueSwap(oldQueue: String, newQueue: String) = buildText {
            appendPrefix()
            info("Du warst in der Warteschlange für ")
            variableValue(oldQueue)
            info(" und bist jetzt in der Warteschlange für ")
            variableValue(newQueue)
        }

        fun getQueued(serverName: String) = buildText {
            appendPrefix()
            appendNewPrefixedLine {
                info("Du befindest dich nun in der Warteschlange für ")
                variableValue(serverName)
            }
            appendNewPrefixedLine()
            appendNewPrefixedLine { info("Bitte warte einen Moment.") }
        }
    }

    object Punish {
        abstract class PunishmentComponentBuilder<P : Punishment>(protected val punishment: P) {
            companion object {
                const val NO_REASON_PROVIDED = "Kein Grund angegeben"
                private const val SHORT_WORDS_PER_LINE_LENGTH = 75
                private const val LONG_WORDS_PER_LINE_LENGTH = 100
            }

            protected fun getPunishmentAnnouncement(
                unban: Boolean,
                banAnnounceText: SurfComponentBuilder.() -> Unit
            ) = buildText {
                darkSpacer(">> ")
                append(if (unban) punishment.type.unpunishDisplay else punishment.type.punishDisplay)
                darkSpacer(" | ")
                banAnnounceText()
            }

            protected fun SurfComponentBuilder.appendPunishmentFooterComponent() {
                append {
                    variableKey("Punishment ID: ")
                    variableValue(punishment.punishmentId)
                }

                if (punishment is PunishmentBan) {
                    appendNewline(3)

                    if (!punishment.securityBan) {
                        spacer("Du denkst, dies ist eine Fehlentscheidung?")
                    }
                    appendNewline {
                        spacer("Kontaktiere bitte unseren Support auf Discord.")
                    }
                    appendNewline(2)
                    appendDiscordLink()
                }
                appendNewline()
            }

            protected fun SurfComponentBuilder.appendPunishmentReason(short: Boolean = true) {
                val reason = punishment.reason ?: return appendReasonItem(NO_REASON_PROVIDED)
                WordUtils.wrap(
                    reason,
                    if (short) SHORT_WORDS_PER_LINE_LENGTH else LONG_WORDS_PER_LINE_LENGTH,
                    null,
                    true
                ).split(System.lineSeparator()).forEach { appendReasonItem(it) }
            }

            private fun SurfComponentBuilder.appendReasonItem(reasonItem: String) {
                variableValue(reasonItem)
                appendNewline()
            }

            protected fun SurfComponentBuilder.appendPunishmentExpirationDate() = when {
                punishment !is ExpirablePunishment -> variableValue("Unbekannt")
                punishment.permanent -> variableValue("PERMANENT")
                else -> append(formatZonedDateTime(punishment.expirationDate))
            }

            protected suspend fun broadcastPunishAnnouncement(
                unban: Boolean,
                punishAnnounceText: SurfComponentBuilder.(displayName: Component) -> Unit
            ) {
                val displayName = getPunishedDisplayName()
                val announcement =
                    getPunishmentAnnouncement(unban) { punishAnnounceText(displayName) }

                broadcastMessage(announcement, punishment.punishedUuid)
            }

            protected suspend fun getPunishedDisplayName() =
                punishment.punishedPlayer().displayName() ?: text("#Unbekannt")

            private suspend fun getDisplayName(uuid: UUID) =
                uuid.toOfflineCloudPlayer().displayName() ?: text("#Unbekannt")

            private fun broadcastMessage(message: ComponentLike, vararg excludedUuids: UUID) {
                val message = message.asComponent()
                val excludedUuids = excludedUuids.toSet()

                CloudPlayerManager.getOnlinePlayers().forEach { player ->
                    if (excludedUuids.contains(player.uuid)) return@forEach
                    player.sendMessage(message)
                }
            }
        }

        object Mute {
            val voiceMutedActionbar = buildText { error("Du bist vom Voice-Chat ausgeschlossen!") }

            operator fun invoke(punishment: PunishmentMute) = MuteComponentBuilder(punishment)

            class MuteComponentBuilder internal constructor(punishment: PunishmentMute) :
                PunishmentComponentBuilder<PunishmentMute>(punishment) {
                suspend fun announceMute() = apply {
                    broadcastPunishAnnouncement(unban = false) { displayName ->
                        append(displayName)
                        info(" wurde stummgeschaltet.")
                    }
                }

                suspend fun announceUnmute() = apply {
                    broadcastPunishAnnouncement(unban = true) { displayName ->
                        info("Die Stummschaltung von ")
                        append(displayName)
                        info(" wurde aufgehoben.")
                    }
                }

                fun muteComponent(justMuted: Boolean = false) = buildText {
                    appendPrefix()
                    appendNewPrefixedLine {
                        error("Du ")
                        if (justMuted) error("wurdest") else error("bist")
                        error(" vom Chat ausgeschlossen!")
                    }
                    appendNewPrefixedLine()
                    appendNewPrefixedLine()
                    appendNewPrefixedLine { variableKey("Grund: ") }
                    appendNewPrefixedLine { appendPunishmentReason(short = false) }
                    appendNewPrefixedLine()
                    appendNewPrefixedLine { variableKey("Ausgeschlossen bis: ") }
                    appendNewPrefixedLine { appendPunishmentExpirationDate() }
                    appendNewPrefixedLine()
                }

                fun sendMuteComponentToPunishedPlayer(justMuted: Boolean = false) = apply {
                    punishment.punishedPlayer().player?.sendMessage(muteComponent(justMuted))
                }

                fun sendUnmuteComponentToPunishedPlayer() = apply {
                    punishment.punishedPlayer().player?.sendText {
                        appendPrefix()
                        appendNewPrefixedLine { success("Dein Ausschluss vom Chat wurde aufgehoben!") }
                        appendNewPrefixedLine()
                    }
                }
            }
        }

        object Ban {
            operator fun invoke(punishment: PunishmentBan) = BanComponentBuilder(punishment)

            class BanComponentBuilder internal constructor(punishment: PunishmentBan) :
                PunishmentComponentBuilder<PunishmentBan>(punishment) {

                companion object {
                    private const val SECURITY_DISCONNECT_REASON = "DEIN ACCOUNT WURDE DEAKTIVIERT"
                    private const val NORMAL_DISCONNECT_REASON =
                        "DU WURDEST VOM SERVER AUSGESCHLOSSEN"
                }

                suspend fun announceBan() = apply {
                    broadcastPunishAnnouncement(false) { displayName ->
                        append(displayName)
                        info(" wurde vom Netzwerk ausgeschlossen.")
                    }
                }

                suspend fun announceUnban() = apply {
                    broadcastPunishAnnouncement(true) { displayName ->
                        info("Der Ausschluss von ")
                        append(displayName)
                        info(" wurde aufgehoben.")
                    }
                }

                suspend fun announceJoinDuringBan() = apply {
                    val displayName = getPunishedDisplayName()

                    CloudServerManager.broadcast(
                        buildText {
                            appendPrefix()
                            appendNewPrefixedLine {
                                append(displayName)
                                info(" versucht, dem Netzwerk beizutreten, ist jedoch ausgeschlossen!")
                            }
                            appendNewPrefixedLine()
                            appendNewPrefixedLine {
                                variableKey("Grund: ")
                                variableValue(punishment.reason ?: NO_REASON_PROVIDED)
                            }
                            appendNewPrefixedLine {
                                variableKey("Ausgeschlossen am: ")
                                append(formatZonedDateTime(punishment.punishmentDate))
                            }
                            appendNewPrefixedLine {
                                variableKey("Ausgeschlossen bis: ")
                                appendPunishmentExpirationDate()
                            }
                            appendNewPrefixedLine()
                        },
                        permission = CommonCloudPermissions.ANNOUNCE_BANNED_TRIED_TO_JOIN,
                        playSound = false
                    )
                }

                fun banDisconnectComponent() = buildText {
                    val securityBan = punishment.securityBan
                    appendDisconnectMessage(
                        if (securityBan) SECURITY_DISCONNECT_REASON else NORMAL_DISCONNECT_REASON, // TODO: specify color
                        {
                            if (securityBan) {
                                variableValue("Das System hat Unregelmäßigkeiten festgestellt.")
                                appendNewline()
                                variableValue("Dein Account wurde aus Sicherheitsgründen deaktiviert!")
                            } else {
                                append {
                                    variableKey("Grund: ")
                                    if (punishment.raw) {
                                        appendNewline(2)
                                        variableValue("Dein Ausschluss wird zurzeit bearbeitet...")
                                        appendNewline()
                                        variableValue("Bitte habe einen Moment Geduld.")
                                        appendNewline()
                                    } else {
                                        appendPunishmentReason()
                                    }
                                }
                                appendNewline()
                                appendNewline {
                                    variableKey("Ausgeschlossen am: ")
                                    append(formatZonedDateTime(punishment.punishmentDate))
                                }
                                appendNewline {
                                    variableKey("Ausgeschlossen bis: ")
                                    appendPunishmentExpirationDate()
                                }
                            }
                        }, {
                            appendPunishmentFooterComponent()
                        }
                    )
                }
            }
        }

        object Kick {
            operator fun invoke(punishment: PunishmentKick) = KickComponentBuilder(punishment)
            class KickComponentBuilder internal constructor(punishment: PunishmentKick) :
                PunishmentComponentBuilder<PunishmentKick>(punishment) {

                suspend fun announceKick() = apply {
                    broadcastPunishAnnouncement(unban = false) { displayName ->
                        append(displayName)
                        info(" wurde vom Server geworfen.")
                    }
                }

                fun kickDisconnectComponent() = buildText {
                    appendKickDisconnectMessage(
                        {
                            variableKey("Grund: ")
                            appendPunishmentReason()
                        }, {
                            appendPunishmentFooterComponent()
                        }
                    )
                }
            }
        }

        object Warn {
            operator fun invoke(punishment: PunishmentWarn) = WarnComponentBuilder(punishment)
            class WarnComponentBuilder internal constructor(punishment: PunishmentWarn) :
                PunishmentComponentBuilder<PunishmentWarn>(punishment) {
                suspend fun announceWarn() = apply {
                    broadcastPunishAnnouncement(unban = false) { displayName ->
                        append(displayName)
                        info(" wurde verwarnt.")
                    }
                }

                fun warnComponent() = buildText {
                    appendPrefix()
                    appendNewPrefixedLine { error("Du wurdest verwarnt.") }
                    appendNewPrefixedLine()
                    appendNewPrefixedLine()
                    appendNewPrefixedLine { variableKey("Grund: ") }
                    appendNewPrefixedLine { appendPunishmentReason(short = false) }
                    appendNewPrefixedLine()
                }

                fun warnPunishedPlayer() = apply {
                    punishment.punishedPlayer().player?.sendMessage(warnComponent())
                }
            }
        }
    }
}