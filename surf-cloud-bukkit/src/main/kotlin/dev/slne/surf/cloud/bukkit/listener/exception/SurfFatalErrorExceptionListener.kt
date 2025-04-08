package dev.slne.surf.cloud.bukkit.listener.exception

import com.destroystokyo.paper.event.server.ServerExceptionEvent
import dev.slne.surf.cloud.api.common.exceptions.FatalSurfError
import dev.slne.surf.cloud.core.common.handleEventuallyFatalError
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

object SurfFatalErrorExceptionListener : Listener {
    private val handler = { _: FatalSurfError -> Bukkit.shutdown() }

    @EventHandler
    fun onServerException(event: ServerExceptionEvent) {
        val e = event.exception
        if (e.handle()) return
        if (e.cause?.handle() == true) return
    }

    private fun Throwable.handle() = handleEventuallyFatalError(handler, false, false)

}