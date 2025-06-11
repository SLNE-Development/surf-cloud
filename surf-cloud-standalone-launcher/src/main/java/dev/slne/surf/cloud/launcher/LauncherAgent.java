package dev.slne.surf.cloud.launcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import org.springframework.instrument.InstrumentationSavingAgent;

public class LauncherAgent {
  private static Instrumentation instrumentation;

  public static void agentmain(String agentArgs, Instrumentation inst) {
    premain(agentArgs, inst);
  }

  public static void premain(String agentArgs, Instrumentation inst) {
    System.out.println("Launcher-Agent started. Loading actual agent...");
    InstrumentationSavingAgent.premain(agentArgs, inst);
    instrumentation = inst;
//    launchSparkHook(agentArgs, inst);
  }

  private static void launchSparkHook(String agentArgs, Instrumentation inst) {
    System.out.println("SparkHook started. Loading agent...");
    try {
      final String regex = ".*/spark-.*-standalone-agent\\.jara$";
      final String resourcePath = findResourceByRegex(regex);

      if (resourcePath == null) {
        throw new FileNotFoundException("Spark agent resource not found: " + regex);
      }

      final File agentJar = extractAgentJar(resourcePath);

      try (final URLClassLoader cl = new URLClassLoader(new URL[]{agentJar.toURI().toURL()}, LauncherAgent.class.getClassLoader())) {
        final ClassLoader currentContextClassloader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(cl);

        final Class<?> agentClass = cl.loadClass(
            "me.lucko.spark.standalone.StandaloneSparkAgent");
        final Method premain = agentClass.getMethod("premain", String.class,
            Instrumentation.class);
        premain.invoke(null, agentArgs, inst);

        Thread.currentThread().setContextClassLoader(currentContextClassloader);
      }
    } catch (Exception e) {
      throw new Error(e);
    }
  }

  private static String findResourceByRegex(String regex) throws IOException {
    final String jarPath = LauncherAgent.class
        .getProtectionDomain()
        .getCodeSource()
        .getLocation()
        .getPath();

    final Pattern pattern = Pattern.compile(regex);

    try (final JarFile jarFile = new JarFile(jarPath)) {
      final Enumeration<JarEntry> entries = jarFile.entries();
      while (entries.hasMoreElements()) {
        final JarEntry entry = entries.nextElement();
        final String name = "/" + entry.getName();
        if (pattern.matcher(name).matches()) {
          return name;
        }
      }
    }
    return null;
  }

  private static File extractAgentJar(String resourcePath) throws IOException {
    try (final InputStream in = LauncherAgent.class.getResourceAsStream(resourcePath)) {
      if (in == null) {
        throw new FileNotFoundException("Resource not found: " + resourcePath);
      }

      final File tempFile = File.createTempFile("spark-agent-", ".jar");
      tempFile.deleteOnExit();

      try (final OutputStream out = new FileOutputStream(tempFile)) {
        byte[] buffer = new byte[4096];
        int len;
        while ((len = in.read(buffer)) > 0) {
          out.write(buffer, 0, len);
        }
      }
      return tempFile;
    }
  }

  public static Instrumentation getInstrumentation() {
    return instrumentation;
  }
}
