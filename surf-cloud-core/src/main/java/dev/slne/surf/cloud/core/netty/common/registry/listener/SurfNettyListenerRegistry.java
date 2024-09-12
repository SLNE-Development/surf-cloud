package dev.slne.surf.cloud.core.netty.common.registry.listener;

import dev.slne.surf.cloud.api.netty.exception.SurfNettyListenerRegistrationException;
import dev.slne.surf.cloud.api.netty.packet.NettyPacket;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.lang.reflect.Method;
import org.springframework.stereotype.Component;

@Component
public class SurfNettyListenerRegistry {

  private final Object2ObjectMap<Class<? extends NettyPacket<?>>, ObjectSet<RegisteredListener>> listeners;

  public SurfNettyListenerRegistry() {
    this.listeners = new Object2ObjectOpenHashMap<>();
  }

  @SuppressWarnings("unchecked")
  public void registerListener(Method listenerMethod, Object bean) {
    final Class<?>[] params = listenerMethod.getParameterTypes();

    if (params.length != 1 || !NettyPacket.class.isAssignableFrom(params[0])) {
      throw new SurfNettyListenerRegistrationException(
          "Listener method must have exactly one parameter of type NettyPacket");
    }

    final Class<? extends NettyPacket<?>> packetClass = (Class<? extends NettyPacket<?>>) params[0];
    listeners.computeIfAbsent(packetClass, (key) -> new ObjectOpenHashSet<>(1))
        .add(new RegisteredListener(bean, listenerMethod));
  }

  public boolean hasListeners(Class<? extends NettyPacket<?>> packetClass) {
    return listeners.containsKey(packetClass);
  }

  public void callListeners(NettyPacket<?> packet) {
    final ObjectSet<RegisteredListener> listeners = this.listeners.get(packet.getClass());

    if (listeners != null) {
      listeners.forEach((listener) -> listener.handle(packet));
    }
  }
}
