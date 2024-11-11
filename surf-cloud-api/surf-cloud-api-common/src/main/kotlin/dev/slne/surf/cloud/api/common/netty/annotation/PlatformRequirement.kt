package dev.slne.surf.cloud.api.common.netty.annotation

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class PlatformRequirement(val value: Platform) {
    enum class Platform {
        COMMON,
        CLIENT,
        SERVER
    }
}
