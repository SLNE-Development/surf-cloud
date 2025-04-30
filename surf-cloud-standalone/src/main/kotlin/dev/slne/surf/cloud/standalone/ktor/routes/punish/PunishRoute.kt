package dev.slne.surf.cloud.standalone.ktor.routes.punish

import dev.slne.surf.cloud.standalone.ktor.routes.punish.create.createPunishRoutes
import io.ktor.server.routing.*

fun Routing.punishRoutes() = route("/punish") {
    createPunishRoutes()
}