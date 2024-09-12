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
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class BukkitLoader implements PluginLoader {

  @Override
  public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
    final MavenLibraryResolver resolver = new MavenLibraryResolver();
    final PluginLibraries pluginLibraries = load();

    pluginLibraries.asDependencies().forEach(resolver::addDependency);
    pluginLibraries.asRepositories().forEach(resolver::addRepository);

    classpathBuilder.addLibrary(resolver);
  }

  private PluginLibraries load() {
    try (var in = getClass().getResourceAsStream("/paper-libraries.json")) {
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

  private record PluginLibraries(Map<String, String> repositories, List<String> dependencies) {

    public Stream<Dependency> asDependencies() {
      return dependencies != null ? dependencies.stream()
          .map(d -> new Dependency(new DefaultArtifact(d), null))
          : Stream.empty();
    }

    public Stream<RemoteRepository> asRepositories() {
      return repositories != null ? repositories.entrySet().stream()
          .map(e -> new RemoteRepository.Builder(e.getKey(), "default", e.getValue()).build())
          : Stream.empty();
    }
  }
}
