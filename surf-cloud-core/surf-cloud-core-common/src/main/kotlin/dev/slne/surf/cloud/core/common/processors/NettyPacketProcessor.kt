package dev.slne.surf.cloud.core.common.processors

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.findPacketCodec
import dev.slne.surf.cloud.api.common.netty.packet.getPacketMetaOrNull
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.api.common.util.logger
import dev.slne.surf.cloud.core.common.netty.network.protocol.ProtocolInfoBuilder
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.RunningProtocols
import org.springframework.boot.autoconfigure.AutoConfigurationPackages
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ApplicationListener
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.context.annotation.ScannedGenericBeanDefinition
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.core.type.filter.AnnotationTypeFilter

private val internalPackage = ProtocolInfoBuilder::class.java.packageName

object NettyPacketProcessor : ApplicationContextInitializer<ConfigurableApplicationContext> {
    private val log = logger()

    override fun initialize(cxt: ConfigurableApplicationContext) {
        @Suppress("ObjectLiteralToLambda")
        cxt.addApplicationListener(object : ApplicationListener<ContextRefreshedEvent> {
            @Suppress("UNCHECKED_CAST")
            override fun onApplicationEvent(event: ContextRefreshedEvent) {
//                val basePackages = findBasePackages(event.applicationContext as ConfigurableApplicationContext)
//                println("Base packages: ${basePackages.toList()}")

                val context = event.applicationContext
                val autoConfigurationPackages =
                    AutoConfigurationPackages.get(context)
                println("Eligible packets for NettyPackets: $autoConfigurationPackages")

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

                    if (packetMeta.protocol != ConnectionProtocol.RUNNING) continue
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
                                codec as StreamCodec<in SurfByteBuf, NettyPacket>
                            )
                        }
                    }

                    println(
                        "Registered packet: ${packet.simpleName} (id: 0x${
                            packetMeta.id.toString(
                                16
                            )
                        }) ${if (packetMeta.flow == PacketFlow.SERVERBOUND) "C->S" else "S->C"}"
                    )
                }
            }
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