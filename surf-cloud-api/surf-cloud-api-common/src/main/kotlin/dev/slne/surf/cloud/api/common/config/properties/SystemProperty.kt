package dev.slne.surf.cloud.api.common.config.properties

import org.jetbrains.annotations.ApiStatus

/**
 * Represents a generic system property.
 *
 * @param T The type of the property value.
 */
@ApiStatus.NonExtendable
interface SystemProperty<T> {

    /**
     * Retrieves the value of the system property.
     *
     * @return The property value, or `null` if not set.
     */
    fun value(): T?
}


/**
 * Represents a required system property.
 *
 * @param T The type of the property value.
 */
@ApiStatus.NonExtendable
interface RequiredSystemProperty<T> : SystemProperty<T> {

    /**
     * Retrieves the value of the required system property.
     *
     * @return The property value.
     * @throws IllegalStateException If the property is not set.
     */
    override fun value(): T
}

/**
 * Creates a new [SystemProperty] with the given key, parser, and optional default value.
 *
 * @param key The key of the system property.
 * @param parser A function to parse the property value.
 * @param defaultValue The default value, or `null` if not set.
 * @return A new [SystemProperty] instance.
 */
fun <T> systemProperty(
    key: String,
    parser: (String) -> T,
    defaultValue: T?
): SystemProperty<T> = SystemPropertyImpl(
    arrayOf("surf", "cloud", key).joinToString("."),
    parser,
    defaultValue
)

/**
 * Creates a new [SystemProperty] with the specified prefix and key.
 *
 * @param prefix The prefix for the property key.
 * @param key The key of the system property.
 * @param parser A function to parse the property value.
 * @param defaultValue The default value, or `null` if not set.
 * @return A new [SystemProperty] instance.
 */
fun <T> systemProperty(
    prefix: String,
    key: String,
    parser: (String) -> T,
    defaultValue: T?
): SystemProperty<T> = SystemPropertyImpl("$prefix.$key", parser, defaultValue)

/**
 * Creates a new [RequiredSystemProperty] with the given key and parser.
 *
 * @param key The key of the system property.
 * @param parser A function to parse the property value.
 * @return A new [RequiredSystemProperty] instance.
 */
fun <T> requiredSystemProperty(
    key: String,
    parser: (String) -> T
): RequiredSystemProperty<T> = RequiredSystemPropertyImpl(
    arrayOf("surf", "cloud", key).joinToString("."),
    parser
)

/**
 * Creates a new [RequiredSystemProperty] with the specified prefix and key.
 *
 * @param prefix The prefix for the property key.
 * @param key The key of the system property.
 * @param parser A function to parse the property value.
 * @return A new [RequiredSystemProperty] instance.
 */
fun <T> requiredSystemProperty(
    prefix: String,
    key: String,
    parser: (String) -> T
): RequiredSystemProperty<T> = RequiredSystemPropertyImpl("$prefix.$key", parser)

