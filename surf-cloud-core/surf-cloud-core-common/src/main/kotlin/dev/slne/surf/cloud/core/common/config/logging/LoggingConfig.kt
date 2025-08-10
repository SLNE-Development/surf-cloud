package dev.slne.surf.cloud.core.common.config.logging

import io.netty.handler.logging.LogLevel
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment
import org.spongepowered.configurate.objectmapping.meta.Setting

@ConfigSerializable
data class LoggingConfig(
    @Comment("Whether to log IPs from clients")
    @Setting("log-ips")
    val logIps: Boolean = true,

    @Comment("The logging level for netty")
    @Setting("netty-log-level")
    val nettyLogLevelInternal: LogLevelWrapper = LogLevelWrapper.DEBUG,
) {
    enum class LogLevelWrapper {
        TRACE, DEBUG, INFO, WARN, ERROR
    }

    val nettyLogLevel: LogLevel
        get() = when (nettyLogLevelInternal) {
            LogLevelWrapper.TRACE -> LogLevel.TRACE
            LogLevelWrapper.DEBUG -> LogLevel.DEBUG
            LogLevelWrapper.INFO -> LogLevel.INFO
            LogLevelWrapper.WARN -> LogLevel.WARN
            LogLevelWrapper.ERROR -> LogLevel.ERROR
        }
}