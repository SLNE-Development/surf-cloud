package dev.slne.surf.cloud.launcher;

import java.net.URL;
import java.net.URLClassLoader;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public class StandaloneUrlClassLoader extends URLClassLoader {

  private static final String INSTRUMENTATION_SAVING_AGENT = "org.springframework.instrument.InstrumentationSavingAgent";
  private static final String LAUNCHER_AGENT = "dev.slne.surf.cloud.launcher.LauncherAgent";

  public StandaloneUrlClassLoader(URL[] urls, ClassLoader parent) {
    super(urls, parent);
  }

  @Deprecated
  @Override
  public void addURL(URL url) {
    super.addURL(url);
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    // This is a workaround for the Spring InstrumentationSavingAgent
    if (INSTRUMENTATION_SAVING_AGENT.equals(name)) {
      for (int i = 0; i < 10; i++) {
        System.out.println("Loading InstrumentationSavingAgent");
      }

      return Class.forName(name, false, Main.class.getClassLoader());
    } else if (LAUNCHER_AGENT.equals(name)) {
      return Class.forName(name, false, Main.class.getClassLoader());
    }

    return super.findClass(name);
  }

  private boolean isExcludedFromTransformation(String className) {
    return className.startsWith("java.")
        || className.startsWith("javax.")
        || className.startsWith("sun.")
        || className.startsWith("jdk.")
        || className.startsWith("org.objectweb.asm.")
        || className.startsWith("org.springframework.asm.");
  }
}
