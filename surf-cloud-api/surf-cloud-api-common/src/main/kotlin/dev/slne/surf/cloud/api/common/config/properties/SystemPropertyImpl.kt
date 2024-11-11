package dev.slne.surf.cloud.api.common.config.properties

class SystemPropertyImpl<T>(
    private val key: String,
    private val parser: (String) -> T,
    private val defaultValue: T? = null,
    private var value: T? = null,
    private var initialized: Boolean = false,
) : SystemProperty<T> {

    override fun value(): T? {
        if (!initialized) {
            val property = System.getProperty(key)
            this.value = if (property != null) parser(property) else defaultValue
            this.initialized = true
        }

        return this.value
    }
}

class RequiredSystemPropertyImpl<T>(
    private val key: String,
    private val parser: (String) -> T,
    private var value: T? = null,
    private var initialized: Boolean = false,
) : RequiredSystemProperty<T> {

    override fun value(): T {
        if (!initialized) {
            val property = System.getProperty(key)
            this.value = if (property != null) parser(property) else error("Required system property $key not set")
            this.initialized = true
        }

        return this.value ?: error("Required system property $key not set")
    }
}