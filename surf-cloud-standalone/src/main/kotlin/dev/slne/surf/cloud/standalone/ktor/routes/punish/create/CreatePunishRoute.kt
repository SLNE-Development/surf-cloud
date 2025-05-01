package dev.slne.surf.cloud.standalone.ktor.routes.punish.create

import dev.slne.surf.cloud.core.common.player.PunishmentManager
import dev.slne.surf.cloud.core.common.player.punishment.type.PunishmentBanImpl
import dev.slne.surf.cloud.core.common.player.punishment.type.PunishmentKickImpl
import dev.slne.surf.cloud.core.common.player.punishment.type.PunishmentMuteImpl
import dev.slne.surf.cloud.core.common.player.punishment.type.PunishmentWarnImpl
import dev.slne.surf.cloud.core.common.util.bean
import dev.slne.surf.cloud.standalone.ktor.routes.punish.create.ban.CreateBanPunishmentRoute
import dev.slne.surf.cloud.standalone.ktor.routes.punish.create.kick.CreateKickPunishmentRoute
import dev.slne.surf.cloud.standalone.ktor.routes.punish.create.mute.CreateMutePunishmentRoute
import dev.slne.surf.cloud.standalone.ktor.routes.punish.create.warn.CreateWarnPunishmentRoute
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.createPunishRoutes() = route("/create") {
    post<CreateBanPunishmentRoute> { banMeta ->
        bean<PunishmentManager>().broadcastBan(
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

        call.respond(HttpStatusCode.OK, "Ban punishment broadcasted successfully.")
    }

    post<CreateKickPunishmentRoute> { kickMeta ->
        bean<PunishmentManager>().broadcastKick(
            PunishmentKickImpl(
                id = kickMeta.id,
                punishmentId = kickMeta.punishmentId,
                punishedUuid = kickMeta.punishedUuid,
                issuerUuid = kickMeta.issuerUuid,
                reason = kickMeta.reason,
                punishmentDate = kickMeta.punishmentDate,
            )
        )

        call.respond(HttpStatusCode.OK, "Kick punishment broadcasted successfully.")
    }

    post<CreateMutePunishmentRoute> { muteMeta ->
        bean<PunishmentManager>().broadcastMute(
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
                unpunisherUuid = muteMeta.unpunisherUuid
            )
        )

        call.respond(HttpStatusCode.OK, "Mute punishment broadcasted successfully.")
    }

    post<CreateWarnPunishmentRoute> { warnMeta ->
        bean<PunishmentManager>().broadcastWarn(
            PunishmentWarnImpl(
                id = warnMeta.id,
                punishmentId = warnMeta.punishmentId,
                punishedUuid = warnMeta.punishedUuid,
                issuerUuid = warnMeta.issuerUuid,
                reason = warnMeta.reason,
                punishmentDate = warnMeta.punishmentDate,
            )
        )

        call.respond(HttpStatusCode.OK, "Warn punishment broadcasted successfully.")
    }
}