package dev.slne.surf.cloud.core.common.processors

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.findPacketCodec
import dev.slne.surf.cloud.api.common.netty.packet.getPacketMetaOrNull
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.core.common.netty.network.ProtocolInfo
import dev.slne.surf.cloud.core.common.netty.network.protocol.ProtocolInfoBuilder
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.RunningProtocols
import dev.slne.surf.cloud.core.common.netty.network.protocol.synchronizing.SynchronizingProtocols
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

            val protocols = packetMeta.protocols
            if (!protocols.contains(ConnectionProtocol.RUNNING) && !protocols.contains(
                    ConnectionProtocol.SYNCHRONIZING
                )
            ) continue
            val codec = packet.findPacketCodec<SurfByteBuf, NettyPacket>()

            if (codec == null) {
                log.atWarning()
                    .log("Packet $packet does not have a findable codec")
                continue
            }


            for (protocol in protocols) {
                when (protocol) {
                    ConnectionProtocol.RUNNING -> {
                        registerPacket(
                            packetMeta.flow,
                            RunningProtocols.CLIENTBOUND_TEMPLATE,
                            RunningProtocols.SERVERBOUND_TEMPLATE,
                            packet,
                            codec
                        )
                    }

                    ConnectionProtocol.SYNCHRONIZING -> {
                        registerPacket(
                            packetMeta.flow,
                            SynchronizingProtocols.CLIENTBOUND_TEMPLATE,
                            SynchronizingProtocols.SERVERBOUND_TEMPLATE,
                            packet,
                            codec
                        )
                    }

                    else -> {}
                }
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

    private fun registerPacket(
        flow: PacketFlow,
        clientboundTemplate: ProtocolInfo.Unbound.Mutable<*, SurfByteBuf>,
        serverboundTemplate: ProtocolInfo.Unbound.Mutable<*, SurfByteBuf>,
        packet: KClass<out NettyPacket>,
        codec: StreamCodec<in SurfByteBuf, out NettyPacket>,
    ) {
        when (flow) {
            PacketFlow.CLIENTBOUND -> clientboundTemplate.addPacket(
                packet.java as Class<NettyPacket>,
                codec
            )

            PacketFlow.SERVERBOUND -> serverboundTemplate.addPacket(
                packet.java as Class<NettyPacket>,
                codec
            )

            PacketFlow.BIDIRECTIONAL -> {
                clientboundTemplate.addPacket(
                    packet.java as Class<NettyPacket>,
                    codec
                )
                serverboundTemplate.addPacket(
                    packet.java as Class<NettyPacket>,
                    codec
                )
            }
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
}