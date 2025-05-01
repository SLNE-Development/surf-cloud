package dev.slne.surf.cloud.standalone.ktor.routes.punish

import dev.slne.surf.cloud.standalone.ktor.routes.punish.create.createPunishRoutes
import dev.slne.surf.cloud.standalone.ktor.routes.punish.update.updatePunishRoute
import io.ktor.server.routing.*

fun Routing.punishRoutes() = route("/punish") {
    createPunishRoutes()
    updatePunishRoute()
}