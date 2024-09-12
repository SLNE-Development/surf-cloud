package dev.slne.surf.cloud.api.netty.exception;

import java.io.Serial;
import lombok.experimental.StandardException;

@StandardException
public abstract class SurfNettyListenerException extends SurfNettyException{

  @Serial
  private static final long serialVersionUID = 7748641971923464222L;
}
