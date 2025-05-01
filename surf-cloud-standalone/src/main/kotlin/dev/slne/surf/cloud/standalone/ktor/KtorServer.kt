package dev.slne.surf.cloud.standalone.ktor

import dev.slne.surf.cloud.api.common.util.TimeLogger
import dev.slne.surf.cloud.api.server.plugin.KtorPlugin
import dev.slne.surf.cloud.api.server.plugin.PluginManager
import dev.slne.surf.cloud.core.common.coroutines.KtorScope
import dev.slne.surf.cloud.core.common.spring.CloudLifecycleAware
import dev.slne.surf.cloud.standalone.config.standaloneConfig
import dev.slne.surf.cloud.standalone.ktor.routes.punish.punishRoutes
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.html.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.resources.Resources
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.coroutines.launch
import kotlinx.html.*
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.seconds

@Component
@Order(CloudLifecycleAware.KTOR_SERVER_PRIORITY)
class KtorServer : CloudLifecycleAware {
    private lateinit var server: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>

    override suspend fun onEnable(timeLogger: TimeLogger) {
        timeLogger.measureStep("Starting Ktor server") {
            start()
        }
    }

    override suspend fun onDisable(timeLogger: TimeLogger) {
        timeLogger.measureStep("Stopping Ktor server") {
            server.stop()
        }
    }

    fun start() = KtorScope.launch {
        val (port, host) = standaloneConfig.ktor
        val plugins = PluginManager.instance.getPlugins().filterIsInstance<KtorPlugin>()

        server = embeddedServer(Netty, port = port, host = host) {
            install(StatusPages) { configure() }
            install(WebSockets) {
                pingPeriod = 15.seconds
            }
            install(Resources)

            for (plugin in plugins) {
                plugin.apply { configure() }
            }

            routing {
                for (plugin in plugins) {
                    plugin.apply { installRoutes() }
                }

                punishRoutes()
            }
        }
        server.start(wait = true)
    }

    private fun StatusPagesConfig.configure() {
        status(HttpStatusCode.NotFound) { call, _ ->
            call.respondErrorPage(
                HttpStatusCode.NotFound,
                "Oops! The page you requested was not found."
            )
        }

        exception<IllegalStateException> { call, cause ->
            call.respondErrorPage(
                HttpStatusCode.InternalServerError,
                "Application is in an illegal state: ${cause.message}"
            )
        }

        exception<IllegalArgumentException> { call, cause ->
            call.respondErrorPage(
                HttpStatusCode.BadRequest,
                "Bad Request: ${cause.message ?: "Unknown error"}"
            )
        }

        exception<NotFoundException> { call, cause ->
            call.respondErrorPage(
                HttpStatusCode.NotFound,
                "Not Found: ${cause.message ?: "The requested resource was not found"}"
            )
        }

        exception<BadRequestException> { call, cause ->
            call.respondErrorPage(HttpStatusCode.BadRequest, "Bad Request: ${cause.message}")
        }

        exception<MissingRequestParameterException> { call, cause ->
            call.respondErrorPage(
                HttpStatusCode.BadRequest,
                "Missing Parameter: '${cause.parameterName}' is required."
            )
        }

        exception<ParameterConversionException> { call, cause ->
            call.respondErrorPage(
                HttpStatusCode.BadRequest,
                "Invalid Parameter: '${cause.parameterName}' must be a valid ${cause.type}."
            )
        }

        exception<UnsupportedMediaTypeException> { call, cause ->
            call.respondErrorPage(
                HttpStatusCode.UnsupportedMediaType,
                "Unsupported Media Type: ${cause.message}"
            )
        }

        exception<PayloadTooLargeException> { call, cause ->
            call.respondErrorPage(
                HttpStatusCode.PayloadTooLarge,
                "Payload Too Large: Request exceeds the size limit."
            )
        }

        exception<ContentTransformationException> { call, cause ->
            call.respondErrorPage(
                HttpStatusCode.UnsupportedMediaType,
                "Content Transformation Error: ${cause.message}"
            )
        }

        exception<Throwable> { call, cause ->
            call.respondErrorPage(
                HttpStatusCode.InternalServerError,
                "Unexpected error: ${cause.message}"
            )
        }
    }

    private suspend fun ApplicationCall.respondErrorPage(status: HttpStatusCode, message: String) {
        respondHtml(status) {
            head {
                title { +"Error ${status.value}" }
                style {
                    //language=CSS
                    unsafe {
                        +"body { font-family: Arial, sans-serif; text-align: center; padding: 50px; background-color: #f4f4f4; }"
                        +"h1 { color: #d9534f; }"
                        +"p { color: #333; font-size: 18px; }"
                        +".container { max-width: 600px; margin: auto; background: white; padding: 20px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }"
                        +".btn { display: inline-block; margin-top: 20px; padding: 10px 20px; background: #0275d8; color: white; text-decoration: none; border-radius: 5px; }"
                    }
                }
            }
            body {
                div("container") {
                    h1 { +"Error ${status.value}" }
                    p { +message }
                    a(classes = "btn", href = "https://server.castcrafter.de") { +"Go Home" }
                }
            }
        }
    }
}