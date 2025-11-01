package dev.slne.surf.cloud.standalone.spark

import dev.slne.surf.cloud.api.common.util.TimeLogger
import dev.slne.surf.cloud.api.server.plugin.PluginManager
import dev.slne.surf.cloud.core.common.CloudCoreInstance
import dev.slne.surf.cloud.core.common.coroutines.BaseScope
import dev.slne.surf.cloud.core.common.spring.CloudLifecycleAware
import dev.slne.surf.cloud.standalone.InstrumentationProvider
import dev.slne.surf.cloud.standalone.spark.impl.CloudPlatformInfoImpl
import dev.slne.surf.cloud.standalone.spark.provider.*
import dev.slne.surf.surfapi.core.api.messages.Colors
import dev.slne.surf.surfapi.core.api.util.mutableObjectSetOf
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import me.lucko.spark.common.SparkPlatform
import me.lucko.spark.common.SparkPlugin
import me.lucko.spark.common.command.CommandResponseHandler
import me.lucko.spark.common.command.sender.CommandSender
import me.lucko.spark.common.monitor.ping.PlayerPingProvider
import me.lucko.spark.common.platform.MetadataProvider
import me.lucko.spark.common.platform.PlatformInfo
import me.lucko.spark.common.platform.serverconfig.ServerConfigProvider
import me.lucko.spark.common.sampler.source.ClassSourceLookup
import me.lucko.spark.common.sampler.source.SourceMetadata
import me.lucko.spark.common.tick.TickHook
import me.lucko.spark.common.tick.TickReporter
import me.lucko.spark.common.util.SparkThreadFactory
import me.lucko.spark.common.util.classfinder.ClassFinder
import me.lucko.spark.common.util.classfinder.FallbackClassFinder
import me.lucko.spark.common.util.classfinder.InstrumentationClassFinder
import me.lucko.spark.lib.adventure.text.format.TextColor
import me.lucko.spark.standalone.StandaloneCommandSender
import org.springframework.stereotype.Component
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.instrument.Instrumentation
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.logging.Level
import java.util.stream.Stream
import kotlin.io.path.Path

@Component
class SparkHook : CloudLifecycleAware {

    lateinit var spark: CloudSparkPlugin

    override suspend fun onBootstrap(
        data: CloudCoreInstance.BootstrapData,
        timeLogger: TimeLogger
    ) {
        timeLogger.measureStep("Hooking Spark") {
            start()
            spark.execute(arrayOf("profiler", "start"), StandaloneCommandSender.SYSTEM_OUT).await()
        }
    }

    override suspend fun onDisable(timeLogger: TimeLogger) {
        timeLogger.measureStep("Disabling Spark") {
            spark.disable()
        }
    }

    fun start() {
        spark = CloudSparkPlugin(
            InstrumentationProvider.getInstrumentation()
        )
    }

    class CloudSparkPlugin(private val instrumentation: Instrumentation) :
        SparkPlugin {

        val platform = SparkPlatform(this)
        private val sender = mutableObjectSetOf<CommandSender>()

        override fun getVersion(): String {
            return "1.10.138"
        }

        override fun getPluginDirectory(): Path {
            return Path("spark")
        }

        override fun getCommandName(): String {
            return "spark"
        }

        override fun getCommandSenders(): Stream<out CommandSender?> {
            return sender.stream()
        }

        override fun executeAsync(p0: Runnable?) {
            SparkScope.launch {
                p0?.run()
            }
        }

        override fun getPlatformInfo(): PlatformInfo {
            return CloudPlatformInfoImpl
        }

        override fun log(p0: Level, p1: String) {
            log(p0, p1, null)
        }

        override fun log(level: Level, msg: String, throwable: Throwable?) {
            val responseHandler = createResponseHandler(StandaloneCommandSender.SYSTEM_OUT)

            if (level.intValue() < Level.WARNING.intValue()) {
                responseHandler.replyPrefixed(me.lucko.spark.lib.adventure.text.Component.text(msg))
            } else {
                responseHandler.replyPrefixed(
                    me.lucko.spark.lib.adventure.text.Component.text(
                        msg,
                        TextColor.color(Colors.ERROR.value())
                    )
                )
                if (throwable != null) {
                    val stringWriter = StringWriter()
                    throwable.printStackTrace(PrintWriter(stringWriter))
                    responseHandler.replyPrefixed(
                        me.lucko.spark.lib.adventure.text.Component.text(
                            stringWriter.toString(),
                            TextColor.color(Colors.WARNING.value())
                        )
                    )
                }
            }
        }

        override fun createClassFinder(): ClassFinder? {
            return ClassFinder.combining(
                *arrayOf(
                    InstrumentationClassFinder(instrumentation),
                    FallbackClassFinder.INSTANCE
                )
            )
        }

        fun createResponseHandler(sender: StandaloneCommandSender) =
            CommandResponseHandler(platform, sender)


        fun execute(
            args: Array<String>,
            sender: StandaloneCommandSender
        ): CompletableFuture<Void> {
            return this.platform.executeCommand(sender, args)
        }

        fun suggest(
            args: Array<String>,
            sender: StandaloneCommandSender
        ): MutableList<String> {
            return this.platform.tabCompleteCommand(sender, args)
        }

        fun disable() {
            this.platform.disable()
        }

        override fun createPlayerPingProvider(): PlayerPingProvider {
            return CloudServerPingProvider
        }

        override fun createServerConfigProvider(): ServerConfigProvider {
            return CloudServerConfigProvider
        }

        override fun createTickHook(): TickHook {
            return CloudTickHook
        }

        override fun createTickReporter(): TickReporter {
            return CloudTickReporter
        }

        override fun createClassSourceLookup(): ClassSourceLookup {
            return CloudClassSourceLookup
        }

        override fun getKnownSources(): Collection<SourceMetadata> {
            return PluginManager.instance.getPlugins().map { plugin ->
                val meta = plugin.meta
                SourceMetadata(
                    meta.name,
                    meta.version,
                    meta.authors.joinToString(", "),
                    meta.description,
                )
            }
        }

        override fun createExtraMetadataProvider(): MetadataProvider {
            return CloudMetadataProvider
        }
    }
}

object SparkScope : BaseScope(
    dispatcher = Executors.newFixedThreadPool(4, SparkThreadFactory()).asCoroutineDispatcher(),
    name = "spark"
)