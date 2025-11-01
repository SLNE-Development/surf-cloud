package dev.slne.surf.cloudtest

import dev.slne.surf.cloud.api.common.CloudInstance
import dev.slne.surf.cloud.api.common.SurfCloudApplication
import dev.slne.surf.cloud.api.common.startSpringApplication
import org.springframework.context.ConfigurableApplicationContext

@SurfCloudApplication
class TestSpringApplication {
    companion object {
        lateinit var context: ConfigurableApplicationContext
        fun run() {
            context = CloudInstance.startSpringApplication(TestSpringApplication::class)
        }

        fun stop() {
            context.stop()
        }
    }
}