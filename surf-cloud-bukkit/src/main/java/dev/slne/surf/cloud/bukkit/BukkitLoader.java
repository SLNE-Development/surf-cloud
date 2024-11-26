package dev.slne.surf.cloud.bukkit;

import com.google.gson.Gson;
import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

@SuppressWarnings({"UnstableApiUsage", "unused"})
public final class BukkitLoader implements PluginLoader {

  @Override
  public void classloader(PluginClasspathBuilder classpathBuilder) {
    final var resolver = new MavenLibraryResolver();
    final var pluginLibraries = load();

    pluginLibraries.asDependencies().forEach(resolver::addDependency);
    pluginLibraries.asRepositories().forEach(resolver::addRepository);

    classpathBuilder.addLibrary(resolver);
  }

  private PluginLibraries load() {
    try (final var in = getClass().getResourceAsStream("/paper-libraries.json")) {
      if (in != null) {
        return new Gson().fromJson(new InputStreamReader(in, StandardCharsets.UTF_8),
            PluginLibraries.class);
      } else {
        return new PluginLibraries(Map.of(), List.of());
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private record PluginLibraries(
      @Nullable Map<String, String> repositories,
      @Nullable List<String> dependencies
  ) {

    Stream<Dependency> asDependencies() {
      return dependencies != null ? dependencies.stream()
          .map(dep -> new Dependency(new DefaultArtifact(dep), null))
          : Stream.empty();
    }

    Stream<RemoteRepository> asRepositories() {
      return repositories != null ? repositories.entrySet().stream()
          .map(entry -> new RemoteRepository.Builder(entry.getKey(), "default",
              entry.getValue()).build())
          : Stream.empty();
    }
  }
}