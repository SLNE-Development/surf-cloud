package dev.slne.surf.cloud.api.common.util

import java.time.LocalDateTime
import java.time.ZoneOffset

fun currentUtc(): LocalDateTime = LocalDateTime.now(ZoneOffset.UTC)