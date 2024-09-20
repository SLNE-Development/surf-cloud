package dev.slne.surf.cloud.launcher;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class Main {

  public static final Path PLUGIN_DIRECTORY = Path.of("plugins");

  public static void main(String[] args) {
    if (Path.of("").toAbsolutePath().toString().contains("!")) {
      System.err.println(
          "surf-cloud-standalone may not run in a directory containing '!'. Please rename the affected folder.");
      System.exit(1);
    }

    final URL[] classpathUrls = setupClasspath();
    final ClassLoader parentClassLoader = Main.class.getClassLoader().getParent();
    System.out.println("Parent class loader: " + parentClassLoader);
    System.out.println("Classpath URLs: " + Arrays.toString(classpathUrls));
    final StandaloneUrlClassLoader classLoader = new StandaloneUrlClassLoader(classpathUrls,
        parentClassLoader);

    final String mainClassName = findMainClass();
    System.out.println("Starting " + mainClassName);

    final StandaloneThread runThread = new StandaloneThread(classLoader, mainClassName, args);
    runThread.setContextClassLoader(classLoader);
    runThread.start();
  }

  private static final class StandaloneThread extends Thread {

    private final ClassLoader classLoader;
    private final String mainClassName;
    private final String[] args;

    private StandaloneThread(ClassLoader classLoader, String mainClassName, String[] args) {
      super("StandaloneMain");
      this.classLoader = classLoader;
      this.mainClassName = mainClassName;
      this.args = args;
    }

    @Override
    public void run() {
      try {
        final Class<?> mainClass = Class.forName(mainClassName, true, classLoader);
        final MethodHandle mainMethod = MethodHandles.lookup()
            .findStatic(mainClass, "main", MethodType.methodType(void.class, String[].class))
            .asFixedArity();
        mainMethod.invoke((Object) args);
      } catch (final Throwable e) {
        throw LauncherUtils.fail("Failed to start main class", e);
      }
    }
  }

  private static URL[] setupClasspath() {
    final URL[][] urls = {
        {getStandaloneUrl()},
        getPluginUrls()
    };

    return Stream.of(urls)
        .filter(Objects::nonNull)
        .flatMap(Stream::of)
        .filter(Objects::nonNull)
        .toArray(URL[]::new);
  }

  private static URL getStandaloneUrl() {
    // get resource 'surf-cloud-standalone.jara'
    URL resource = Main.class.getResource("/surf-cloud-standalone.jara");

    if (resource == null) {
      throw LauncherUtils.fail("Failed to find standalone jar", null);
    }

    // rename 'surf-cloud-standalone.jara' to 'surf-cloud-standalone.jar'
    String path = resource.toString().replace("jara", "jar");
    try {
      return new URI(path).toURL();
    } catch (MalformedURLException | URISyntaxException e) {
      throw LauncherUtils.fail("Failed to convert standalone jar URL", e);
    }
  }

  @SuppressWarnings("CallToPrintStackTrace")
  @Contract(pure = true)
  private static URL @NotNull [] getPluginUrls() {
    final File pluginDir = PLUGIN_DIRECTORY.toFile();
    if (!pluginDir.exists()) {
      if (!pluginDir.mkdirs()) {
        System.err.println("Failed to create plugin directory");
      }
      return new URL[0];
    }

    final File[] files = pluginDir.listFiles((dir, name) -> dir.isFile() && name.endsWith(".jar"));

    if (files == null) {
      return new URL[0];
    }

    return Stream.of(files)
        .map(file -> {
          try {
            return file.toURI().toURL();
          } catch (MalformedURLException e) {
            System.err.println("Failed to convert file to URL");
            e.printStackTrace();
            return null;
          }
        })
        .filter(Objects::nonNull)
        .toArray(URL[]::new);
  }

  @SneakyThrows
  private static String findMainClass() {
//    try (final InputStream manifestStream = Launcher.class.getResource("/META-INF/MANIFEST.MF")
//        .openStream()) {
//      final Manifest manifest = new Manifest(manifestStream);
//      System.out.println("manifest: " + manifest);
//
//      System.out.println("main attributes: " + manifest.getMainAttributes().entrySet());
//      String mainClass = manifest.getMainAttributes().getValue("Real-Main-Class");
//      System.out.println("main class: " + mainClass);
//
//      if (mainClass == null) {
//        throw new IllegalStateException("Main class not found in manifest");
//      }
//
//      return mainClass;
//    }

    // TODO: 19.09.2024 18:47 - fix
    return "dev.slne.surf.cloud.standalone.Bootstrap";
  }
}