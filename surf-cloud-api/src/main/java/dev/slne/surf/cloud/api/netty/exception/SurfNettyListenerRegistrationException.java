package dev.slne.surf.cloud.api.netty.exception;

import java.io.Serial;
import lombok.experimental.StandardException;

@StandardException
public class SurfNettyListenerRegistrationException extends SurfNettyListenerException {

  @Serial
  private static final long serialVersionUID = 4077119187296696024L;
}
