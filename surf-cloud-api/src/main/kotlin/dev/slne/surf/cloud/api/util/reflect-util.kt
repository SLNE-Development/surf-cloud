package dev.slne.surf.cloud.api.util

import java.lang.reflect.Modifier
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaField

fun KProperty1<*, *>.isStaticFinal() = javaField?.let {
    Modifier.isStatic(it.modifiers) && Modifier.isFinal(it.modifiers)
} ?: false
