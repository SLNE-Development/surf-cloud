package dev.slne.surf.cloud.launcher;

import java.net.URL;
import java.net.URLClassLoader;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public class StandaloneUrlClassLoader extends URLClassLoader {

  public StandaloneUrlClassLoader(URL[] urls, ClassLoader parent) {
    super(urls, parent);
  }

  @Deprecated
  @Override
  public void addURL(URL url) {
    super.addURL(url);
  }
}
