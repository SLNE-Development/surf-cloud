package dev.slne.surf.cloud.core.netty.common.registry.packet;

import dev.slne.surf.cloud.api.meta.Registry;
import dev.slne.surf.cloud.api.netty.exception.SurfNettyRegisterPacketException;
import dev.slne.surf.cloud.api.netty.packet.NettyPacket;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

@Registry
public class SurfPacketRegistry {

  private final Int2ObjectMap<RegisteredPacket> packets = new Int2ObjectOpenHashMap<>();
  private final Object2IntMap<Class<? extends NettyPacket<?>>> packet2Class = new Object2IntOpenHashMap<>();

  {
    packet2Class.defaultReturnValue(-1);
  }

  @SuppressWarnings("unchecked")
  public void registerPacket(int packetId, NettyPacket<?> packet) throws SurfNettyRegisterPacketException {
    registerPacket(packetId, (Class<? extends NettyPacket<?>>) packet.getClass());
  }

  public void registerPacket(int packetId, Class<? extends NettyPacket<?>> packetClass) throws SurfNettyRegisterPacketException {
    if (isRegistered(packetId)) {
      throw new SurfNettyRegisterPacketException("Packet with id " + packetId + " is already registered");
    }

    packets.put(packetId, new RegisteredPacket(packetClass));
    packet2Class.put(packetClass, packetId);
  }

  public int getPacketId(Class<? extends NettyPacket<?>> packetClass) {
    return packet2Class.getInt(packetClass);
  }

  @SuppressWarnings("unchecked")
  public <T extends NettyPacket<T>> T createPacket(int packetId) {
    return (T) packets.get(packetId).createPacket();
  }

  public boolean isRegistered(int packetId) {
    return packets.containsKey(packetId);
  }

  public boolean isRegistered(NettyPacket<?> packet) {
    return packet2Class.containsKey(packet.getClass());
  }
}
