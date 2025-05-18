package dev.slne.surf.cloud.core.common.util

import dev.slne.surf.surfapi.core.api.messages.adventure.getPointer
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.permission.PermissionChecker

fun Audience.hasPermissionPlattform(permission: String) =
    getPointer(PermissionChecker.POINTER)?.test(permission)
        ?: error("Audience does not have a PermissionChecker pointer")