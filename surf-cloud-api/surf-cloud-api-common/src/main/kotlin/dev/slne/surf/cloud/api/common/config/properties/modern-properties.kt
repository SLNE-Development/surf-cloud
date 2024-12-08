package dev.slne.surf.cloud.api.common.config.properties

/**
 * Gets the system property indicated by the specified [property name][propertyName],
 * or returns [defaultValue] if there is no property with that key.
 *
 * **Note: this function should be used in JVM tests only, other platforms use the default value.**
 */
fun systemProperty(
    propertyName: String,
    defaultValue: Boolean
): Boolean = systemProperty(propertyName)?.toBoolean() ?: defaultValue

/**
 * Gets the system property indicated by the specified [property name][propertyName],
 * or returns [defaultValue] if there is no property with that key. It also checks that the result
 * is between [minValue] and [maxValue] (inclusively), throws [IllegalStateException] if it is not.
 */
fun systemProperty(
    propertyName: String,
    defaultValue: Int,
    minValue: Int = 1,
    maxValue: Int = Int.MAX_VALUE
): Int = systemProperty(
    propertyName,
    defaultValue.toLong(),
    minValue.toLong(),
    maxValue.toLong()
).toInt()

fun systemPropertyRequired(
    propertyName: String,
    minValue: Int = 1,
    maxValue: Int = Int.MAX_VALUE
): Int = systemPropertyRequired(
    propertyName,
    minValue.toLong(),
    maxValue.toLong()
).toInt()

/**
 * Gets the system property indicated by the specified [property name][propertyName],
 * or returns [defaultValue] if there is no property with that key. It also checks that the result
 * is between [minValue] and [maxValue] (inclusively), throws [IllegalStateException] if it is not.
 */
fun systemProperty(
    propertyName: String,
    defaultValue: Long,
    minValue: Long = 1,
    maxValue: Long = Long.MAX_VALUE
): Long {
    val value = systemProperty(propertyName) ?: return defaultValue
    return parseLong(value, minValue, maxValue)
}

fun systemPropertyRequired(
    propertyName: String,
    minValue: Long = 1,
    maxValue: Long = Long.MAX_VALUE
): Long {
    val value = systemProperty(propertyName) ?: error("System property '$propertyName' is required")
    return parseLong(value, minValue, maxValue)
}

private fun parseLong(value: String, minValue: Long, maxValue: Long): Long {
    val parsed = value.toLongOrNull()
        ?: error("System property has unrecognized value '$value'")
    if (parsed !in minValue..maxValue) {
        error("System property should be in range $minValue..$maxValue, but is '$parsed'")
    }
    return parsed
}

/**
 * Gets the system property indicated by the specified [property name][propertyName],
 * or returns [defaultValue] if there is no property with that key.
 */
fun systemProperty(
    propertyName: String,
    defaultValue: String
): String = systemProperty(propertyName) ?: defaultValue

fun systemPropertyRequired(propertyName: String): String =
    systemProperty(propertyName) ?: error("System property '$propertyName' is required")


/**
 * Gets the system property indicated by the specified [property name][propertyName],
 * or returns `null` if there is no property with that key.
 */
fun systemProperty(propertyName: String): String? = System.getProperty(propertyName)