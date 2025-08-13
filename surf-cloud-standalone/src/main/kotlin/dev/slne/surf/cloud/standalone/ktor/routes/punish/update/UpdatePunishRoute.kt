package dev.slne.surf.cloud.standalone.ktor.routes.punish.update

import dev.slne.surf.cloud.core.common.player.PunishmentManager
import dev.slne.surf.cloud.core.common.util.bean
import dev.slne.surf.cloud.standalone.ktor.routes.punish.update.ban.UpdateBanPunishmentRoute
import dev.slne.surf.cloud.standalone.ktor.routes.punish.update.kick.UpdateKickPunishmentRoute
import dev.slne.surf.cloud.standalone.ktor.routes.punish.update.mute.UpdateMutePunishmentRoute
import dev.slne.surf.cloud.standalone.ktor.routes.punish.update.warn.UpdateWarnPunishmentRoute
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.updatePunishRoute() = route("/update") {
    post<UpdateBanPunishmentRoute> { banMeta ->
        bean<PunishmentManager>().broadcastBanUpdate(banMeta.toApiObject())
        call.respond(HttpStatusCode.OK, "Ban punishment updated successfully.")
    }

    post<UpdateMutePunishmentRoute> { muteMeta ->
        bean<PunishmentManager>().broadcastMuteUpdate(muteMeta.toApiObject())
        call.respond(HttpStatusCode.OK, "Mute punishment updated successfully.")
    }

    post<UpdateKickPunishmentRoute> { kickMeta ->
        bean<PunishmentManager>().broadcastKickUpdate(kickMeta.toApiObject())
        call.respond(HttpStatusCode.OK, "Kick punishment updated successfully.")
    }

    post<UpdateWarnPunishmentRoute> { warnMeta ->
        bean<PunishmentManager>().broadcastWarnUpdate(warnMeta.toApiObject())
        call.respond(HttpStatusCode.OK, "Warn punishment updated successfully.")
    }
}