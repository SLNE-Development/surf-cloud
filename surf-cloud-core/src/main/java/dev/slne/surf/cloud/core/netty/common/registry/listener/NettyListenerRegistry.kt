package dev.slne.surf.cloud.core.netty.common.registry.listener;

import dev.slne.surf.cloud.api.netty.exception.SurfNettyListenerRegistrationException;
import dev.slne.surf.cloud.api.netty.packet.NettyPacket;
import dev.slne.surf.cloud.core.netty.protocol.packet.NettyPacketInfo;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.springframework.stereotype.Component;

@Component
public class NettyListenerRegistry {

  private final Object2ObjectMap<Class<? extends NettyPacket<?>>, ObjectSet<RegisteredListener>> listeners;

  public NettyListenerRegistry() {
    this.listeners = new Object2ObjectOpenHashMap<>();
  }

  @SuppressWarnings("unchecked")
  public void registerListener(Method listenerMethod, Object bean) {
    final Class<?>[] params = listenerMethod.getParameterTypes();

    if (params.length == 0 || params.length > 2) {
      throw new SurfNettyListenerRegistrationException(
          "Listener method must have one or two parameters of type NettyPacket and optional NettyPacketInfo");
    }

    Class<? extends NettyPacket<?>> packetClass = null;
    int packetClassIndex = -1;
    int packetInfoIndex = -1;

    for (int i = 0; i < params.length; i++) {
      final Class<?> param = params[i];
      if (NettyPacket.class.isAssignableFrom(param)) {
        if (packetClass != null) {
          throw new SurfNettyListenerRegistrationException("Listener method must have only one parameter of type NettyPacket");
        }

        packetClass = (Class<? extends NettyPacket<?>>) param;
        packetClassIndex = i;

      } else if (NettyPacketInfo.class.isAssignableFrom(param)) {
        if (packetInfoIndex != -1) {
          throw new SurfNettyListenerRegistrationException("Listener method must have only one parameter of type NettyPacketInfo");
        }

        packetInfoIndex = i;
      } else {
        throw new SurfNettyListenerRegistrationException("Listener method must have one or two parameters of type NettyPacket and optional NettyPacketInfo");
      }
    }

    if (packetClass == null) {
      throw new SurfNettyListenerRegistrationException("Listener method must have one parameter of type NettyPacket");
    }

    if (!Modifier.isPublic(listenerMethod.getModifiers())) {
      throw new SurfNettyListenerRegistrationException("Listener method must be public");
    }

    listeners.computeIfAbsent(packetClass, (key) -> new ObjectOpenHashSet<>(1))
        .add(new RegisteredListener(bean, listenerMethod, packetClassIndex, packetInfoIndex));
  }

  public boolean hasListeners(Class<? extends NettyPacket<?>> packetClass) {
    return listeners.containsKey(packetClass);
  }

  public void callListeners(NettyPacket<?> packet, NettyPacketInfo info) {
    final ObjectSet<RegisteredListener> listeners = this.listeners.get(packet.getClass());

    if (listeners != null) {
      listeners.forEach((listener) -> listener.handle(packet, info));
    }
  }
}
