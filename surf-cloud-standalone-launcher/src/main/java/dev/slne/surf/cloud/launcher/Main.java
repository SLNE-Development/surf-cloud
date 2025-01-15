package dev.slne.surf.cloud.launcher;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import org.eclipse.aether.resolution.DependencyResolutionException;

public class Main {
  public static void main(String[] args) {
    if (Path.of("").toAbsolutePath().toString().contains("!")) {
      System.err.println(
          "surf-cloud-standalone may not run in a directory containing '!'. Please rename the affected folder.");
      System.exit(1);
    }

    final URL[] classpathUrls = setupClasspath();
    final ClassLoader parentClassLoader = Main.class.getClassLoader();
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

      setDaemon(false);
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
    final URL standaloneUrl = getStandaloneUrl();
    final JarFile standaloneJar;

    try {
      standaloneJar = new JarFile(new File(standaloneUrl.getFile()));
    } catch (IOException e) {
      throw new RuntimeException("Failed to load standalone jar", e);
    }

    final URL[][] urls = {
        {standaloneUrl},
        getLibraries(standaloneJar)
    };

    return Stream.of(urls)
        .filter(Objects::nonNull)
        .flatMap(Stream::of)
        .filter(Objects::nonNull)
        .toArray(URL[]::new);
  }

  private static URL getStandaloneUrl() {
    final URL resource = Main.class.getResource("/surf-cloud-standalone.jara");

    if (resource == null) {
      throw LauncherUtils.fail("Failed to find standalone jar", null);
    }

    try {
      final File tempJar = File.createTempFile("surf-cloud-standalone", ".jar");
      try (final InputStream inputStream = resource.openStream()) {
        Files.copy(inputStream, tempJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
      }

      return tempJar.toURI().toURL();
    } catch (final IOException e) {
      throw LauncherUtils.fail("Failed to extract standalone jar", e);
    }
  }

  private static URL[] getLibraries(JarFile file) {
    final ZipEntry reposEntry = file.getEntry("repos");
    final ZipEntry dependenciesEntry = file.getEntry("dependencies");
    final LibraryLoader loader = new LibraryLoader();

    try {
      return loader.loadLibraries(file, reposEntry, dependenciesEntry).stream()
          .map(Path::toUri)
          .map(uri -> {
            try {
              return uri.toURL();
            } catch (MalformedURLException e) {
              throw new RuntimeException("Failed to convert path to URL", e);
            }
          })
          .toArray(URL[]::new);
    } catch (DependencyResolutionException e) {
      throw new RuntimeException("Failed to load libraries", e);
    }
  }

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