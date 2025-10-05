package dev.slne.surf.cloud.api.server.redis

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class RedisEventMeta(val id: String)
