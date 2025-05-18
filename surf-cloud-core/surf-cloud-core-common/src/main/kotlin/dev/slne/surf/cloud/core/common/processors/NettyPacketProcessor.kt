package dev.slne.surf.cloud.core.common.processors

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.findPacketCodec
import dev.slne.surf.cloud.api.common.netty.packet.getPacketMetaOrNull
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.core.common.netty.network.protocol.ProtocolInfoBuilder
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.RunningProtocols
import dev.slne.surf.surfapi.core.api.util.logger
import org.springframework.boot.autoconfigure.AutoConfigurationPackages
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ApplicationListener
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.context.annotation.ScannedGenericBeanDefinition
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.core.type.filter.AnnotationTypeFilter
import kotlin.reflect.KClass

private val internalPackage = ProtocolInfoBuilder::class.java.packageName

class NettyPacketProcessor : ApplicationContextInitializer<ConfigurableApplicationContext>,
    ApplicationListener<ContextRefreshedEvent> {
    private val log = logger()

    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        applicationContext.addApplicationListener(this)
    }

    override fun onApplicationEvent(event: ContextRefreshedEvent) {
//                val basePackages = findBasePackages(event.applicationContext as ConfigurableApplicationContext)
//                println("Base packages: ${basePackages.toList()}")

        val context = event.applicationContext
        val autoConfigurationPackages = AutoConfigurationPackages.get(context)
        log.atInfo()
            .log("Eligible packets for NettyPackets: $autoConfigurationPackages")

        val scanner = ClassPathScanningCandidateComponentProvider(false)
        scanner.resourceLoader = context
        scanner.addIncludeFilter(AnnotationTypeFilter(SurfNettyPacket::class.java))

        val packets = autoConfigurationPackages
            .asSequence()
            .map { scanner.findCandidateComponents(it) }
            .flatten()
            .mapNotNull { it as? ScannedGenericBeanDefinition }
            .mapNotNull { it.resolveBeanClass(context.classLoader) }
            .mapNotNull { (it as? Class<out NettyPacket>)?.kotlin }
            .filter { it.qualifiedName?.startsWith(internalPackage) == false }

        for (packet in packets) {
            val packetMeta = packet.getPacketMetaOrNull()
            if (packetMeta == null) {
                log.atInfo()
                    .log("Packet $packet does not have a @SurfNettyPacket annotation")
                continue
            }

            if (!packetMeta.protocols.contains(ConnectionProtocol.RUNNING)) continue
            val codec = packet.findPacketCodec<SurfByteBuf, NettyPacket>()

            if (codec == null) {
                log.atWarning()
                    .log("Packet $packet does not have a findable codec")
                continue
            }

            when (packetMeta.flow) {
                PacketFlow.CLIENTBOUND -> RunningProtocols.CLIENTBOUND_TEMPLATE.addPacket(
                    packet.java as Class<NettyPacket>,
                    codec as StreamCodec<in SurfByteBuf, NettyPacket>
                )

                PacketFlow.SERVERBOUND -> RunningProtocols.SERVERBOUND_TEMPLATE.addPacket(
                    packet.java as Class<NettyPacket>,
                    codec as StreamCodec<in SurfByteBuf, NettyPacket>
                )

                PacketFlow.BIDIRECTIONAL -> {
                    RunningProtocols.CLIENTBOUND_TEMPLATE.addPacket(
                        packet.java as Class<NettyPacket>,
                        codec as StreamCodec<in SurfByteBuf, NettyPacket>
                    )
                    RunningProtocols.SERVERBOUND_TEMPLATE.addPacket(
                        packet.java as Class<NettyPacket>,
                        codec
                    )
                }
            }

            logRegistration(packet, packetMeta)
        }
    }

    private fun logRegistration(packet: KClass<out NettyPacket>, packetMeta: SurfNettyPacket) {
        log.atFine()
            .log(buildString {
                append("Registered packet: ")
                append(packet.simpleName)
                append(" (id: ")
                append(packetMeta.id)
                append(") ")
                append(packetMeta.flow.displayName())
            })
    }

//    private fun findBasePackages(context: ConfigurableApplicationContext): Sequence<String> {
////        val candidates1 = context.beanDefinitionNames
////            .mapNotNull { context.getType(it)?.`package`?.name }
////            .distinct()
//
//        val candidates2 =
//            findMainClass(context)?.getAnnotation(SurfCloudApplication::class.java)?.basePackages
//                ?: emptyArray()
//
//        return (candidates2).asSequence().distinct().filter { it.contains("org.springframework") }
//    }
//
//    private fun findMainClass(context: ConfigurableApplicationContext): Class<*>? {
//        return context.getBeansWithAnnotation(SurfCloudApplication::class.java).values
//            .map { it::class.java }
//            .firstOrNull()
//    }
}