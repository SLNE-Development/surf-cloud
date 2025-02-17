package dev.slne.surf.cloud.launcher;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

  public static @Nullable String readResourceText(final @NotNull String path) throws IOException {
    final String p = path.startsWith("/") ? path : "/" + path;
    final InputStream stream = LauncherUtils.class.getResourceAsStream(p);
    if (stream == null) {
      return null;
    }

    final StringWriter writer = new StringWriter();
    try (stream) {
      final Reader reader = new InputStreamReader(stream);
      reader.transferTo(writer);
    }

    return writer.toString();
  }
}
