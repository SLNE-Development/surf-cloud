package dev.slne.surf.cloud.standalone.ktor.routes.punish.update

import dev.slne.surf.cloud.core.common.player.PunishmentManager
import dev.slne.surf.cloud.core.common.player.punishment.type.PunishmentBanImpl
import dev.slne.surf.cloud.core.common.player.punishment.type.PunishmentKickImpl
import dev.slne.surf.cloud.core.common.player.punishment.type.PunishmentMuteImpl
import dev.slne.surf.cloud.core.common.player.punishment.type.PunishmentWarnImpl
import dev.slne.surf.cloud.core.common.util.bean
import dev.slne.surf.cloud.standalone.ktor.routes.punish.update.ban.UpdateBanPunishmentRoute
import dev.slne.surf.cloud.standalone.ktor.routes.punish.update.kick.UpdateKickPunishmentRoute
import dev.slne.surf.cloud.standalone.ktor.routes.punish.update.mute.UpdateMutePunishmentRoute
import dev.slne.surf.cloud.standalone.ktor.routes.punish.update.warn.UpdateWarnPunishmentRoute
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Routing.updatePunishRoute() = route("/update") {
    post<UpdateBanPunishmentRoute> { banMeta ->
        bean<PunishmentManager>().broadcastBanUpdate(
            PunishmentBanImpl(
                id = banMeta.id,
                punishmentId = banMeta.punishmentId,
                punishedUuid = banMeta.punishedUuid,
                issuerUuid = banMeta.issuerUuid,
                reason = banMeta.reason,
                permanent = banMeta.permanent,
                securityBan = banMeta.securityBan,
                raw = banMeta.raw,
                expirationDate = banMeta.expirationDate,
                punishmentDate = banMeta.punishmentDate,
                unpunished = banMeta.unpunished,
                unpunishedDate = banMeta.unpunishedDate,
                unpunisherUuid = banMeta.unpunisherUuid,
            )
        )

        call.respond(HttpStatusCode.OK, "Ban punishment updated successfully.")
    }

    post<UpdateMutePunishmentRoute> { muteMeta ->
        bean<PunishmentManager>().broadcastMuteUpdate(
            PunishmentMuteImpl(
                id = muteMeta.id,
                punishmentId = muteMeta.punishmentId,
                punishedUuid = muteMeta.punishedUuid,
                issuerUuid = muteMeta.issuerUuid,
                reason = muteMeta.reason,
                permanent = muteMeta.permanent,
                expirationDate = muteMeta.expirationDate,
                punishmentDate = muteMeta.punishmentDate,
                unpunished = muteMeta.unpunished,
                unpunishedDate = muteMeta.unpunishedDate,
                unpunisherUuid = muteMeta.unpunisherUuid,
            )
        )

        call.respond(HttpStatusCode.OK, "Mute punishment updated successfully.")
    }

    post<UpdateKickPunishmentRoute> { kickMeta ->
        bean<PunishmentManager>().broadcastKickUpdate(
            PunishmentKickImpl(
                id = kickMeta.id,
                punishmentId = kickMeta.punishmentId,
                punishedUuid = kickMeta.punishedUuid,
                issuerUuid = kickMeta.issuerUuid,
                reason = kickMeta.reason,
                punishmentDate = kickMeta.punishmentDate,
            )
        )

        call.respond(HttpStatusCode.OK, "Kick punishment updated successfully.")
    }

    post<UpdateWarnPunishmentRoute> { warnMeta ->
        bean<PunishmentManager>().broadcastWarnUpdate(
            PunishmentWarnImpl(
                id = warnMeta.id,
                punishmentId = warnMeta.punishmentId,
                punishedUuid = warnMeta.punishedUuid,
                issuerUuid = warnMeta.issuerUuid,
                reason = warnMeta.reason,
                punishmentDate = warnMeta.punishmentDate,
            )
        )

        call.respond(HttpStatusCode.OK, "Warn punishment updated successfully.")
    }
}