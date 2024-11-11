package dev.slne.surf.cloud.api.common.util

import net.kyori.adventure.util.Services

inline fun <reified T> requiredService(): T = Services.serviceWithFallback(T::class.java)
    .orElseThrow { Error("Service ${T::class.java.name} not available") }