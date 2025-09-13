package dev.slne.surf.cloud.standalone.ktor.routes.punish.create

import dev.slne.surf.cloud.core.common.player.PunishmentManager
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
        bean<PunishmentManager>().broadcastBan(banMeta.toApiObject())
        call.respond(HttpStatusCode.OK, "Ban punishment broadcasted successfully.")
    }



    post<CreateKickPunishmentRoute> { kickMeta ->
        bean<PunishmentManager>().broadcastKick(kickMeta.toApiObject())
        call.respond(HttpStatusCode.OK, "Kick punishment broadcasted successfully.")
    }

    post<CreateMutePunishmentRoute> { muteMeta ->
        bean<PunishmentManager>().broadcastMute(muteMeta.toApiObject())
        call.respond(HttpStatusCode.OK, "Mute punishment broadcasted successfully.")
    }

    post<CreateWarnPunishmentRoute> { warnMeta ->
        bean<PunishmentManager>().broadcastWarn(warnMeta.toApiObject())
        call.respond(HttpStatusCode.OK, "Warn punishment broadcasted successfully.")
    }
}