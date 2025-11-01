package dev.slne.surf.cloud.api.server.plugin

import io.ktor.server.application.*
import io.ktor.server.routing.*

interface KtorPlugin {
    fun Routing.installRoutes()
    fun Application.configure() = Unit
}