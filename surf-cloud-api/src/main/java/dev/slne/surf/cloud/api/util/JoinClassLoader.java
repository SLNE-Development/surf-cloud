package dev.slne.surf.cloud.api.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JoinClassLoader extends ClassLoader {

  private ClassLoader[] delegateClassLoaders;

  /**
   * Instantiates a new Join class loader.
   *
   * @param parent               the parent
   * @param delegateClassLoaders the delegate class loaders
   */
  public JoinClassLoader(ClassLoader parent, ClassLoader... delegateClassLoaders) {
    super(parent);

    this.delegateClassLoaders = delegateClassLoaders;
  }

  /**
   * Instantiates a new Join class loader.
   *
   * @param parent               the parent
   * @param delegateClassLoaders the delegate class loaders
   */
  public JoinClassLoader(ClassLoader parent, Collection<ClassLoader> delegateClassLoaders) {
    this(parent, delegateClassLoaders.toArray(ClassLoader[]::new));
  }

  @Override
  protected Class<?> findClass(@NotNull String name) throws ClassNotFoundException {
    final Class<?> parentClass = getFromParent(parent -> parent.loadClass(name));

    if (parentClass != null) {
      return parentClass;
    }

    final String path = name.replace('.', '/') + ".class";
    final URL url = findResource(path);
    final ByteBuffer byteCode;

    if (url == null) {
      throw new ClassNotFoundException(name);
    }

    try {
      byteCode = loadResource(url);
    } catch (IOException exception) {
      throw new ClassNotFoundException(name, exception);
    }

    return defineClass(name, byteCode, null);
  }

  @Override
  protected URL findResource(String name) {
    final URL parentResource = getFromParent(parent -> parent.getResource(name));

    if (parentResource != null) {
      return parentResource;
    }

    for (final ClassLoader delegateClassLoader : delegateClassLoaders) {
      final URL resource = delegateClassLoader.getResource(name);

      if (resource != null) {
        return resource;
      }
    }

    return null;
  }

  @Override
  protected Enumeration<URL> findResources(String name) throws IOException {
    final Vector<URL> vector = new Vector<>();
    final Enumeration<URL> parentResources = getFromParent(parent -> parent.getResources(name));

    if (parentResources != null) {
      parentResources.asIterator().forEachRemaining(vector::add);
    }

    for (final ClassLoader delegateClassLoader : delegateClassLoaders) {
      delegateClassLoader.getResources(name).asIterator().forEachRemaining(vector::add);
    }

    return vector.elements();
  }

  /**
   * Load resource byte buffer.
   *
   * @param url the url
   * @return the byte buffer
   * @throws IOException the io exception
   */
  private @NotNull ByteBuffer loadResource(URL url) throws IOException {
    try (final InputStream stream = url.openStream()) {
      int initialBufferCapacity = Math.min(0x40000, stream.available() + 1);

      if (initialBufferCapacity <= 2) {
        initialBufferCapacity = 0x10000;
      } else {
        initialBufferCapacity = Math.max(initialBufferCapacity, 0x200);
      }

      ByteBuffer buffer = ByteBuffer.allocate(initialBufferCapacity);

      while (true) {
        if (!buffer.hasRemaining()) {
          final ByteBuffer newBuf = ByteBuffer.allocate(buffer.capacity() * 2);

          buffer.flip();
          newBuf.put(buffer);
          buffer = newBuf;
        }

        int length = stream.read(buffer.array(), buffer.position(), buffer.remaining());

        if (length <= 0) {
          break;
        }

        buffer.position(buffer.position() + length);
      }

      buffer.flip();

      return buffer;
    }
  }

  public final void addDelegateClassLoader(ClassLoader classLoader) {
    final ClassLoader[] newDelegateClassLoaders = new ClassLoader[delegateClassLoaders.length + 1];
    System.arraycopy(delegateClassLoaders, 0, newDelegateClassLoaders, 0,
        delegateClassLoaders.length);
    newDelegateClassLoaders[delegateClassLoaders.length] = classLoader;
    delegateClassLoaders = newDelegateClassLoaders;
  }

  private <T> @Nullable T getFromParent(Mapper<T> mapper) {
    final ClassLoader parent = getParent();
    try {
      return parent == null ? null : mapper.map(parent);
    } catch (Exception e) {
      return null;
    }
  }

  private interface Mapper<T> {
    T map(ClassLoader classLoader) throws Exception;
  }
}
