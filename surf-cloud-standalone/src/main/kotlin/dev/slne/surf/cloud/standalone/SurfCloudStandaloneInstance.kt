package dev.slne.surf.cloud.standalone

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.common.SurfCloudInstance
import dev.slne.surf.cloud.core.common.SurfCloudCoreInstance
import dev.slne.surf.cloud.core.common.util.checkInstantiationByServiceLoader
import dev.slne.surf.cloud.core.common.util.random
import dev.slne.surf.cloud.standalone.netty.server.StandaloneNettyManager
import dev.slne.surf.cloud.standalone.plugin.StandalonePluginManager
import dev.slne.surf.cloud.standalone.redis.RedisEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.springframework.boot.SpringApplication
import org.springframework.data.redis.core.ReactiveRedisTemplate
import reactor.util.Loggers
import java.nio.file.Path
import kotlin.concurrent.thread
import kotlin.time.Duration.Companion.seconds

@AutoService(SurfCloudInstance::class)
class SurfCloudStandaloneInstance : SurfCloudCoreInstance(StandaloneNettyManager) {
    private val redisEventLog = Loggers.getLogger("RedisEvent")
    override val springProfile = "independent"

    init {
        checkInstantiationByServiceLoader()
    }

    fun callRedisEvent(event: RedisEvent) {
        val template = dataContext.getBean(
            "reactiveRedisTemplate",
            ReactiveRedisTemplate::class.java
        ) as ReactiveRedisTemplate<String, Any>
        for (channel in event.channels) {
            template.convertAndSend(channel, event).log(redisEventLog).subscribe()
        }
    }

    override suspend fun onLoad() {
        SpringApplication.getShutdownHandlers().add(StandalonePluginManager)
        thread(name = "KeepAlive") { while (true) runBlocking { delay(5.seconds) } }
        super.onLoad()
        random
    }

    override suspend fun onEnable() {
        super.onEnable()

        afterStart()
        println("ready")
    }

    companion object {
        @JvmStatic
        fun get() = SurfCloudInstance.get() as SurfCloudStandaloneInstance
    }
}

val independentCloudInstance
    get() = SurfCloudStandaloneInstance.get()