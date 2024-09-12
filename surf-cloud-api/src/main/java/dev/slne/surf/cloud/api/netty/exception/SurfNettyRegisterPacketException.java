package dev.slne.surf.cloud.api.netty.exception;

import java.io.Serial;

public class SurfNettyRegisterPacketException extends SurfNettyPacketException {

  @Serial
  private static final long serialVersionUID = -5234200008867680129L;

  public SurfNettyRegisterPacketException(String message) {
    super(message);
  }

  public SurfNettyRegisterPacketException(String message, Throwable cause) {
    super(message, cause);
  }

}
