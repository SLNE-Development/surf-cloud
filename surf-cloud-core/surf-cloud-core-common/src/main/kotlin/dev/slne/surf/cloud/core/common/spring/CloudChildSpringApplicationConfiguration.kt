package dev.slne.surf.cloud.core.common.spring

import org.springframework.boot.builder.SpringApplicationBuilder

interface CloudChildSpringApplicationConfiguration {

    val excludedAutoConfiguration: List<Class<*>>
        get() = listOf()

    fun configureChildApplication(
        builder: SpringApplicationBuilder,
        classLoader: ClassLoader,
        vararg parentClassLoader: ClassLoader
    )
}