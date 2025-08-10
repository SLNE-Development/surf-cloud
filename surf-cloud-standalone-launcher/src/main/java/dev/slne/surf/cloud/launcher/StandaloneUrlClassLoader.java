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
      return Class.forName(name, false, Main.class.getClassLoader());
    } else if (LAUNCHER_AGENT.equals(name)) {
      return Class.forName(name, false, Main.class.getClassLoader());
    }

    return super.findClass(name);
  }

  //  public void addTransformer(ClassFileTransformer transformer) {
//    this.transformers.add(transformer);
//  }
//
//  public ClassLoader getThrowawayClassLoader() {
//    return new StandaloneUrlClassLoader(getURLs(), getParent());
//  }

//  @Override
//  public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
//    if (isExcludedFromTransformation(name)) {
//      return super.loadClass(name, resolve);
//    }
//
//    Class<?> c = findLoadedClass(name);
//    if (c == null) {
//      try {
//        c = super.loadClass(name, false);
//      } catch (ClassNotFoundException ex) {
//        c = findClass(name);
//      }
//    }
//
//    if (resolve) {
//      resolveClass(c);
//    }
//
//    return c;
//  }

//  @Override
//  protected Class<?> findClass(String name) throws ClassNotFoundException {
//    String resourceName = name.replace('.', '/').concat(".class");
//
//    URL resource = getResource(resourceName);
//    if (resource == null) {
//      throw new ClassNotFoundException("Resource not found: " + resourceName);
//    }
//
//    byte[] classBytes;
//    try (InputStream is = resource.openStream()) {
//      classBytes = is.readAllBytes();
//    } catch (IOException ex) {
//      throw new ClassNotFoundException("Could not read class:" + name, ex);
//    }
//
//    byte[] transformedBytes = transformClassBytes(name, classBytes);
//

  /// /    CodeSource codeSource = new CodeSource(resource, (java.security.cert.Certificate[])
  /// null); /    ProtectionDomain pd = new ProtectionDomain(codeSource, null);
//
//    return defineClass(name, transformedBytes, 0, transformedBytes.length);
//  }

//  private byte[] transformClassBytes(String className, byte[] originalBytes)
//      throws ClassNotFoundException {
//    byte[] current = originalBytes;
//    for (ClassFileTransformer transformer : transformers) {
//      try {
//        byte[] transformed = transformer.transform(
//            this,
//            className,
//            null,
//            null,
//            current
//        );
//        if (transformed != null) {
//          current = transformed;
//        }
//      } catch (IllegalClassFormatException e) {
//        throw new ClassNotFoundException(
//            "Error while transforming class " + className, e
//        );
//      }
//    }
//    return current;
//  }
  private boolean isExcludedFromTransformation(String className) {
    return className.startsWith("java.")
        || className.startsWith("javax.")
        || className.startsWith("sun.")
        || className.startsWith("jdk.")
        || className.startsWith("org.objectweb.asm.")
        || className.startsWith("org.springframework.asm.");
  }
}
