package dev.slne.surf.cloud.core.spring.event

import org.springframework.context.ApplicationEvent
import java.io.Serial

class RootSpringContextInitialized(source: Any): ApplicationEvent(source) {
    companion object {
        @Serial
        private val serialVersionUID: Long = 6293513894838425358L
    }
}