package dev.slne.surf.cloud.core.common.messages

import dev.slne.surf.surfapi.core.api.messages.Colors
import net.kyori.adventure.text.Component

object MessageManager { // TODO: Add more messages
    val serverStarting = Component.text().run {
        append(Component.text("Der Server startet noch. Bitte warte einen Moment.", Colors.ERROR))
        build()
    }
}