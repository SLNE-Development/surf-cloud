package dev.slne.surf.cloud.api.netty.exception;

import java.io.Serial;
import lombok.experimental.StandardException;

@StandardException
public class SurfNettyPacketNotRegisteredException extends SurfNettyPacketException{

  @Serial
  private static final long serialVersionUID = -7120879291609543337L;
}
