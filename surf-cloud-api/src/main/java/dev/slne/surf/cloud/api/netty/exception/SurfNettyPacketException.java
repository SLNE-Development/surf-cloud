package dev.slne.surf.cloud.api.netty.exception;

import java.io.Serial;
import lombok.experimental.StandardException;

@StandardException
public abstract class SurfNettyPacketException extends SurfNettyException{

  @Serial
  private static final long serialVersionUID = -7658527490298368796L;
}
