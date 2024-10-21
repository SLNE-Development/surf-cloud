package dev.slne.surf.cloud.launcher;

final class LauncherUtils {

  @SuppressWarnings("CallToPrintStackTrace")
  public static RuntimeException fail(final String message, final Throwable err) {
    System.err.println(message);
    if (err != null) {
      err.printStackTrace();
    }
    System.exit(1);
    throw new InternalError();
  }
}
