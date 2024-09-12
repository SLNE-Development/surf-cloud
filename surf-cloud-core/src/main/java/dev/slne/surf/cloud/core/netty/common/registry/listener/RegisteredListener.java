package dev.slne.surf.cloud.core.netty.common.registry.listener;

import dev.slne.surf.cloud.api.netty.exception.SurfNettyListenerRegistrationException;
import dev.slne.surf.cloud.api.netty.packet.NettyPacket;
import java.lang.reflect.Method;
import tech.hiddenproject.aide.reflection.LambdaWrapperHolder;
import tech.hiddenproject.aide.reflection.annotation.Invoker;

public class RegisteredListener {

  static {
    LambdaWrapperHolder.DEFAULT.add(RegisteredListenerInvoker.class);
  }

  private final Object bean;
  private final RegisteredListenerInvoker invoker;

  public RegisteredListener(Object bean, Method listenerMethod)
      throws SurfNettyListenerRegistrationException {
    this.bean = bean;
    this.invoker = LambdaWrapperHolder.DEFAULT.wrap(listenerMethod, RegisteredListenerInvoker.class)
        .getWrapper();
  }

  public void handle(NettyPacket<?> packet) {
    invoker.handle(bean, packet);
  }

  @FunctionalInterface
  private interface RegisteredListenerInvoker {

    @Invoker
    void handle(Object caller, NettyPacket<?> packet);
  }
}
