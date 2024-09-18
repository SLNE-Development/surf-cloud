package dev.slne.surf.cloud.standalone.launcher;

import lombok.experimental.UtilityClass;

@UtilityClass
class LauncherUtils {

  @SuppressWarnings("CallToPrintStackTrace")
  public RuntimeException fail(final String message, final Throwable err) {
    System.err.println(message);
    if (err != null) {
      err.printStackTrace();
    }
    System.exit(1);
    throw new InternalError();
  }
}
