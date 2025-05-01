package dev.slne.surf.cloud.api.common.player.punishment.type

import java.time.ZonedDateTime

interface ExpirablePunishment: Punishment, Comparable<ExpirablePunishment> {
    val permanent: Boolean
    val expirationDate: ZonedDateTime?

    val active: Boolean
        get() {
            val unpunished = this is UnpunishablePunishment && unpunished
            if (unpunished) return false
            if (permanent) return true

            val expirationDate = expirationDate ?: return false
            val now = ZonedDateTime.now()

            return now.isBefore(expirationDate)
        }

    override fun compareTo(other: ExpirablePunishment): Int {
        if (this == other) return 0
        if (this.permanent && other.permanent) return 0
        if (this.permanent) return -1
        if (other.permanent) return 1

        if (this.expirationDate == null && other.expirationDate == null) return 0
        val thisExpirationDate = this.expirationDate ?: return -1
        val otherExpirationDate = other.expirationDate ?: return 1

        return thisExpirationDate.compareTo(otherExpirationDate)
    }
}