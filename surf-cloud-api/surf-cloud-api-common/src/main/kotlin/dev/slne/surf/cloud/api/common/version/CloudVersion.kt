package dev.slne.surf.cloud.api.common.version

import org.springframework.core.io.ClassPathResource

private val rawVersion by lazy { ClassPathResource("cloud.version").getContentAsString(Charsets.UTF_8) }

object CloudVersion {
    val fullVersion: String by lazy {
        rawVersion
    }

    val minecraftVersion: String by lazy {
        rawVersion.split("-").getOrNull(0) ?: "unknown"
    }

    val snapshot: Boolean by lazy {
        rawVersion.endsWith("-SNAPSHOT")
    }

    val version: String by lazy {
        rawVersion.split("-").getOrNull(1) ?: "unknown"
    }

    val major: Int by lazy {
        version.split(".").getOrNull(0)?.toIntOrNull() ?: 0
    }

    val minor: Int by lazy {
        version.split(".").getOrNull(1)?.toIntOrNull() ?: 0
    }

    val patch: Int by lazy {
        version.split(".").getOrNull(2)?.toIntOrNull() ?: 0
    }

    fun isAtLeast(major: Int, minor: Int = 0, patch: Int = 0): Boolean {
        return this.major > major || (this.major == major && this.minor > minor) || (this.major == major && this.minor == minor && this.patch >= patch)
    }

    fun isAtMost(major: Int, minor: Int = 0, patch: Int = 0): Boolean {
        return this.major < major || (this.major == major && this.minor < minor) || (this.major == major && this.minor == minor && this.patch <= patch)
    }

    fun isEqualTo(major: Int, minor: Int = 0, patch: Int = 0): Boolean {
        return this.major == major && this.minor == minor && this.patch == patch
    }

    fun isNewerThan(major: Int, minor: Int = 0, patch: Int = 0): Boolean {
        return this.major < major || (this.major == major && this.minor < minor) || (this.major == major && this.minor == minor && this.patch < patch)
    }

    fun isOlderThan(major: Int, minor: Int = 0, patch: Int = 0): Boolean {
        return this.major > major || (this.major == major && this.minor > minor) || (this.major == major && this.minor == minor && this.patch > patch)
    }
}