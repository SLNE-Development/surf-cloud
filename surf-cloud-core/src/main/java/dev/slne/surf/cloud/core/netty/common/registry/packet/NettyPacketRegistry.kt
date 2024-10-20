package dev.slne.surf.cloud.core.netty.common.registry.packet

import dev.slne.surf.cloud.api.netty.exception.SurfNettyRegisterPacketException
import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import kotlin.reflect.KClass

object NettyPacketRegistry {
    private val packets = Int2ObjectOpenHashMap<RegisteredPacket>()
    private val packet2Class = Object2IntOpenHashMap<KClass<out NettyPacket<*>>>()

    init {
        packet2Class.defaultReturnValue(-1)
    }

    @Throws(SurfNettyRegisterPacketException::class)
    fun registerPacket(packetId: Int, packet: NettyPacket<*>) {
        registerPacket(packetId, packet::class)
    }

    @Throws(SurfNettyRegisterPacketException::class)
    fun registerPacket(packetId: Int, packetClass: KClass<out NettyPacket<*>>) {
        if (isRegistered(packetId)) {
            throw SurfNettyRegisterPacketException("Packet with id $packetId is already registered")
        }

        packets.put(packetId, RegisteredPacket(packetClass))
        packet2Class.put(packetClass, packetId)
    }

    fun getPacketId(packetClass: KClass<out NettyPacket<*>>): Int =
        packet2Class.getInt(packetClass)

    fun <T : NettyPacket<T>> createPacket(packetId: Int): T? {
        val packet = packets[packetId] ?: return null

        return packet.createPacket() as? T
    }

    fun isRegistered(packetId: Int) = packetId in packets
    fun isRegistered(packet: NettyPacket<*>?): Boolean {
        if (packet == null) {
            return false
        }

        return packet::class in packet2Class
    }
}
