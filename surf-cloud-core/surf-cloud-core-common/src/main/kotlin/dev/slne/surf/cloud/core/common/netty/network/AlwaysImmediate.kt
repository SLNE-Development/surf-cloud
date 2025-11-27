package dev.slne.surf.cloud.core.common.netty.network

/**
 * Always sends the packet immediately
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class AlwaysImmediate() {
    companion object {
        private val cache = object : ClassValue<Boolean>() {
            override fun computeValue(type: Class<*>): Boolean {
                return type.getAnnotation(AlwaysImmediate::class.java) != null
            }
        }

        fun canSendImmediate(type: Class<*>): Boolean = cache.get(type)
    }
}
