package dev.slne.surf.cloud.api.common.config.properties

import org.jetbrains.annotations.ApiStatus

@ApiStatus.NonExtendable
interface SystemProperty<T> {
    fun value(): T?
}
interface RequiredSystemProperty<T> : SystemProperty<T> {
    override fun value(): T
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

fun <T> requiredSystemProperty(
    key: String,
    parser: (String) -> T
): RequiredSystemProperty<T> = RequiredSystemPropertyImpl(
    arrayOf("surf", "cloud", key).joinToString("."),
    parser
)

fun <T> requiredSystemProperty(
    prefix: String,
    key: String,
    parser: (String) -> T
): RequiredSystemProperty<T> = RequiredSystemPropertyImpl("$prefix.$key", parser)

