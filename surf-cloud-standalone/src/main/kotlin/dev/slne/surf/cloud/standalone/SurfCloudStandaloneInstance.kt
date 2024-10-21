package dev.slne.surf.cloud.standalone

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.SurfCloudInstance
import dev.slne.surf.cloud.core.SurfCloudCoreInstance
import dev.slne.surf.cloud.standalone.plugin.StandalonePluginManager
import dev.slne.surf.cloud.standalone.redis.RedisEvent
import org.springframework.boot.SpringApplication
import org.springframework.data.redis.core.ReactiveRedisTemplate
import reactor.util.Loggers
import java.nio.file.Path

@AutoService(SurfCloudInstance::class)
class SurfCloudStandaloneInstance : SurfCloudCoreInstance() {
    private val redisEventLog = Loggers.getLogger("RedisEvent")

    override val dataFolder: Path = Path.of("")
    override val classLoader: ClassLoader = javaClass.classLoader
    override val springProfile = "independent"

    fun callRedisEvent(event: RedisEvent) {
        val template = dataContext.getBean(
            "reactiveRedisTemplate",
            ReactiveRedisTemplate::class.java
        ) as ReactiveRedisTemplate<String, Any>
        for (channel in event.channels) {
            template.convertAndSend(channel, event).log(redisEventLog).subscribe()
        }
    }

    override fun onLoad() {
        super.onLoad()
        SpringApplication.getShutdownHandlers().add(StandalonePluginManager)
    }

    override fun onEnable() {
        super.onEnable()
        println("ready")
    }

    companion object {
        @JvmStatic
        fun get() = SurfCloudInstance.get() as SurfCloudStandaloneInstance
    }
}

val independentCloudInstance
    get() = SurfCloudStandaloneInstance.get()
