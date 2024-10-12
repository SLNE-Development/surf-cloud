package dev.slne.surf.cloud.api.config.properties

import org.jetbrains.annotations.ApiStatus

@ApiStatus.NonExtendable
interface SystemProperty<T> {
    fun value(): T?
}

fun <T> systemProperty(
    key: String,
    parser: (String) -> T,
    defaultValue: T?
): SystemProperty<T> = SystemPropertyImpl(
    arrayOf("surf", "cloud", key).joinToString("."),
    parser,
    defaultValue
)


fun <T> systemProperty(
    prefix: String,
    key: String,
    parser: (String) -> T,
    defaultValue: T?
): SystemProperty<T> = SystemPropertyImpl("$prefix.$key", parser, defaultValue)

