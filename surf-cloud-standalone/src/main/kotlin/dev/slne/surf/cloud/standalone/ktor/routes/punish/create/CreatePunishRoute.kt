package dev.slne.surf.cloud.standalone.ktor.routes.punish.create

import dev.slne.surf.cloud.core.common.player.PunishmentManager
import dev.slne.surf.cloud.core.common.util.bean
import dev.slne.surf.cloud.standalone.ktor.routes.punish.create.ban.CreateBanPunishmentRoute
import dev.slne.surf.cloud.standalone.ktor.routes.punish.create.kick.CreateKickPunishmentRoute
import dev.slne.surf.cloud.standalone.ktor.routes.punish.create.mute.CreateMutePunishmentRoute
import dev.slne.surf.cloud.standalone.ktor.routes.punish.create.warn.CreateWarnPunishmentRoute
import dev.slne.surf.surfapi.core.api.util.logger
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.createPunishRoutes() = route("/create") {
    post<CreateBanPunishmentRoute> { banMeta ->
        bean<PunishmentManager>().broadcastBan(banMeta.toApiObject())
        call.respond(HttpStatusCode.OK, "Ban punishment broadcasted successfully.")
    }

    get("/kick") {
        logger().atInfo().log("Received GET request for kick punishment creation.")
        call.respond(
            HttpStatusCode.MethodNotAllowed,
            "GET method is not allowed for kick punishment creation. Use POST instead."
        )
    }

    post("/kick") {
        val kickMeta = call.receive<CreateKickPunishmentRoute>()

        logger().atInfo().log("Broadcasting kick punishment: $kickMeta")
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