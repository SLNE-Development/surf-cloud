package dev.slne.surf.cloud.core.netty.common.registry.listener;

import dev.slne.surf.cloud.api.netty.exception.SurfNettyListenerRegistrationException;
import dev.slne.surf.cloud.api.netty.packet.NettyPacket;
import dev.slne.surf.cloud.core.netty.protocol.packet.NettyPacketInfo;
import java.lang.reflect.Method;
import tech.hiddenproject.aide.reflection.LambdaWrapperHolder;
import tech.hiddenproject.aide.reflection.annotation.Invoker;

final class RegisteredListener {

  static {
    LambdaWrapperHolder.DEFAULT.add(RegisteredListenerInvoker1.class);
    LambdaWrapperHolder.DEFAULT.add(RegisteredListenerInvoker2.class);
    LambdaWrapperHolder.DEFAULT.add(RegisteredListenerInvoker2Rev.class);
  }

  private final Object bean;
  private final Object invoker;
  private final InvokerType invokerType;

  RegisteredListener(
      Object bean,
      Method listenerMethod,
      int packetClassIndex,
      int packetInfoIndex
  ) throws SurfNettyListenerRegistrationException {
    this.bean = bean;

    final Class<?>[] params = listenerMethod.getParameterTypes();

    if (params.length == 1) { // Only NettyPacket
      this.invoker = LambdaWrapperHolder.DEFAULT.wrap(listenerMethod,
          RegisteredListenerInvoker1.class).getWrapper();
      this.invokerType = InvokerType.ONE_PARAM;

    } else if (params.length == 2) { // NettyPacket and NettyPacketInfo

      if (packetClassIndex == 0
          && packetInfoIndex == 1) { // Normal order (NettyPacket, NettyPacketInfo)
        invoker = LambdaWrapperHolder.DEFAULT.wrap(listenerMethod, RegisteredListenerInvoker2.class)
            .getWrapper();
        invokerType = InvokerType.TWO_PARAMS;

      } else if (packetInfoIndex == 0
          && packetClassIndex == 1) { // Reversed order (NettyPacketInfo, NettyPacket)
        invoker = LambdaWrapperHolder.DEFAULT.wrap(listenerMethod,
            RegisteredListenerInvoker2Rev.class).getWrapper();
        invokerType = InvokerType.TWO_PARAMS_REVERSED;
      } else {
        throw new SurfNettyListenerRegistrationException("Invalid parameter order");
      }
    } else {
      throw new SurfNettyListenerRegistrationException("Invalid number of parameters");
    }
  }

  public void handle(NettyPacket<?> packet, NettyPacketInfo info) {
    switch (invokerType) {
      case ONE_PARAM -> ((RegisteredListenerInvoker1) invoker).handle(bean, packet);
      case TWO_PARAMS -> ((RegisteredListenerInvoker2) invoker).handle(bean, packet, info);
      case TWO_PARAMS_REVERSED ->
          ((RegisteredListenerInvoker2Rev) invoker).handle(bean, info, packet);
      default -> throw new IllegalStateException("Unknown invoker type");
    }
  }

  private enum InvokerType {
    ONE_PARAM,
    TWO_PARAMS,
    TWO_PARAMS_REVERSED
  }

  @FunctionalInterface
  public interface RegisteredListenerInvoker1 {

    @Invoker
    void handle(Object caller, NettyPacket<?> packet);
  }

  @FunctionalInterface
  public interface RegisteredListenerInvoker2 {

    @Invoker
    void handle(Object caller, NettyPacket<?> packet, NettyPacketInfo info);
  }

  @FunctionalInterface
  public interface RegisteredListenerInvoker2Rev {

    @Invoker
    void handle(Object caller, NettyPacketInfo info, NettyPacket<?> packet);
  }
}
