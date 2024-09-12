package dev.slne.surf.data.core.netty.common;

import dev.slne.surf.data.api.netty.exception.SurfNettyRegisterPacketException;
import dev.slne.surf.data.api.netty.packet.NettyPacket;
import java.lang.reflect.Constructor;
import lombok.Getter;
import tech.hiddenproject.aide.reflection.LambdaWrapper;
import tech.hiddenproject.aide.reflection.LambdaWrapperHolder;
import tech.hiddenproject.aide.reflection.MethodHolder;

public final class RegisteredPacket {

  @Getter
  private final Class<? extends NettyPacket<?>> packetClass;
  private final MethodHolder<LambdaWrapper, Void, ? extends NettyPacket<?>> fastConstructor;

  public RegisteredPacket(Class<? extends NettyPacket<?>> packetClass)
      throws SurfNettyRegisterPacketException {
    this.packetClass = packetClass;

    try {
      final Constructor<? extends NettyPacket<?>> constructor = packetClass.getConstructor();
      this.fastConstructor = LambdaWrapperHolder.DEFAULT.wrapSafe(constructor);
    } catch (NoSuchMethodException e) {
      throw new SurfNettyRegisterPacketException(
          "Packet class must have a public no-args constructor", e);
    }
  }

  public NettyPacket<?> createPacket() {
    return fastConstructor.invokeStatic();
  }
}
