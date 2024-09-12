package dev.slne.surf.cloud.api.netty.exception;

import java.io.Serial;
import lombok.experimental.StandardException;

@StandardException
public abstract class SurfNettyException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 63872266580221436L;
}
