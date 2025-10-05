package dev.slne.surf.cloud.standalone.spring.config.redis

import com.mojang.serialization.Codec
import dev.slne.surf.cloud.api.server.redis.KotlinJsonSerializationCodec
import dev.slne.surf.cloud.api.server.redis.RedisCodec
import dev.slne.surf.cloud.api.server.redis.RedisEvent
import dev.slne.surf.cloud.api.server.redis.RedisEventMeta
import dev.slne.surf.surfapi.core.api.util.logger
import dev.slne.surf.surfapi.core.api.util.mutableObject2ObjectMapOf
import jakarta.annotation.PostConstruct
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializerOrNull
import org.springframework.boot.autoconfigure.AutoConfigurationPackages
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.core.type.filter.AnnotationTypeFilter
import org.springframework.stereotype.Component
import org.springframework.util.ClassUtils
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.isAccessible

private const val DEFAULT_CODEC_NAME = "CODEC"

@Component
class RedisEventDiscoverer : ApplicationContextAware {
    private lateinit var applicationContext: ApplicationContext

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    @PostConstruct
    fun registerAll() {
        val events = discover()
        RedisEventRegistry.registerAll(events)
    }

    fun discover(): Map<Class<out RedisEvent>, RedisEventMetadata<*>> {
        val basePackages = resolveBasePackages().let { pkgs ->
            pkgs.filterNot { parent -> pkgs.any { it != parent && it.startsWith("$parent.") } }
        }

        log.atInfo().log("Eligible packages for RedisEvents: $basePackages")

        val scanner = ClassPathScanningCandidateComponentProvider(false).apply {
            addIncludeFilter(AnnotationTypeFilter(RedisEventMeta::class.java))
        }

        val result = mutableObject2ObjectMapOf<Class<out RedisEvent>, RedisEventMetadata<*>>()

        for (pkg in basePackages) {
            for (beanDefinition in scanner.findCandidateComponents(pkg)) {
                val className = beanDefinition.beanClassName ?: continue
                val clazz = ClassUtils.forName(className, null)

                if (!RedisEvent::class.java.isAssignableFrom(clazz)) {
                    log.atWarning()
                        .log("Class $className is annotated with @RedisEventMeta but does not extend RedisEvent")
                    continue
                }

                val metaAnnotation =
                    AnnotationUtils.findAnnotation(clazz, RedisEventMeta::class.java) ?: continue

                @Suppress("UNCHECKED_CAST")
                val codec = findCodec(clazz as Class<out RedisEvent>)

                if (codec == null) {
                    log.atWarning()
                        .log("No codec found for RedisEvent class $className")
                    continue
                }

                val metadata = RedisEventMetadata(
                    id = metaAnnotation.id,
                    codec = codec
                )

                val previous = result.putIfAbsent(clazz, metadata)
                require(previous == null) { "Duplicate RedisEvent id ${metaAnnotation.id} for classes ${previous!!.javaClass.name} and $className" }
            }
        }

        return result
    }

    @OptIn(InternalSerializationApi::class)
    private fun findCodec(eventClass: Class<out RedisEvent>): Codec<out RedisEvent>? {
        val kClass = eventClass.kotlin
        val properties =
            kClass.declaredMemberProperties + (kClass.companionObject?.declaredMemberProperties
                ?: emptyList())

        val codecProperty = properties.find {
            it.findAnnotation<RedisCodec>() != null ||
                    (it.name == DEFAULT_CODEC_NAME && it.returnType.classifier == Codec::class)
        }?.apply { isAccessible = true }

        val codec = codecProperty?.let { prop ->
            when {
                kClass.objectInstance != null -> prop.call(kClass.objectInstance)
                kClass.companionObjectInstance != null -> prop.call(kClass.companionObjectInstance)
                else -> prop.call(null)
            } as? Codec<out RedisEvent>
        }

        if (codec != null) {
            return codec
        }

        val serializer = kClass.serializerOrNull()
        if (serializer != null) {
            return KotlinJsonSerializationCodec(serializer, json)
        }

        return null
    }

    private fun resolveBasePackages(): List<String> {
        return try {
            AutoConfigurationPackages.get(applicationContext)
        } catch (_: IllegalStateException) {
            listOf(applicationContext.javaClass.`package`?.name ?: "")
        }
    }

    companion object {
        private val log = logger()
        private val json = Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
            isLenient = true
            useAlternativeNames = false
            decodeEnumsCaseInsensitive = true
            // TODO: 04.10.2025 16:58 - serializer module from surf-api
        }
    }
}