package dev.slne.surf.cloud.api.meta

import org.springframework.core.annotation.AliasFor
import org.springframework.stereotype.Component

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Component
annotation class Registry(@get:AliasFor(annotation = Component::class) val value: String = "")
