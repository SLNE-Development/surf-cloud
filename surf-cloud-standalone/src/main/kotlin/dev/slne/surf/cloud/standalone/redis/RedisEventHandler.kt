package dev.slne.surf.cloud.standalone.redis

import org.springframework.core.annotation.AliasFor

@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
annotation class RedisEventHandler(
    @get:AliasFor("channels") vararg val value: String = [],
    @get:AliasFor("value") val channels: Array<String> = []
)
