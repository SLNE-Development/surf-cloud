package dev.slne.surf.cloud.launcher;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RemoteRepository.Builder;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.supplier.RepositorySystemSupplier;
import org.eclipse.aether.transfer.AbstractTransferListener;
import org.eclipse.aether.transfer.TransferCancelledException;
import org.eclipse.aether.transfer.TransferEvent;

public final class LibraryLoader {

  private final RepositorySystem repository = new RepositorySystemSupplier().getRepositorySystem();
  private final DefaultRepositorySystemSession session;

  {
    final DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
    session.setSystemProperties(System.getProperties());
    session.setChecksumPolicy(RepositoryPolicy.CHECKSUM_POLICY_FAIL);
    session.setLocalRepositoryManager(
        repository.newLocalRepositoryManager(session, new LocalRepository("libraries")));
    session.setTransferListener(new AbstractTransferListener() {
      @Override
      public void transferInitiated(TransferEvent event) throws TransferCancelledException {
        System.out.println(
            "Downloading " + event.getResource().getRepositoryUrl() + event.getResource()
                .getResourceName());
      }
    });
    session.setReadOnly();

    this.session = session;
  }

  public List<Path> loadLibraries(ZipFile zipFile, ZipEntry reposFile, ZipEntry dependenciesFile)
      throws DependencyResolutionException {
    final List<String> reposRaw = extractLines(zipFile, reposFile);
    final List<String> dependenciesRaw = extractLines(zipFile, dependenciesFile);

    final List<RemoteRepository> repos = reposRaw.stream()
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .map(s -> new Builder(null, "default", s).build())
        .toList();

    final List<Dependency> dependencies = dependenciesRaw.stream()
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .map(s -> s.split(":", 3))
        .filter(parts -> parts.length == 3)
        .map(parts -> new Dependency(
            new DefaultArtifact("%s:%s:%s".formatted(parts[0], parts[1], parts[2])), null))
        .toList();

    final List<RemoteRepository> resolutionRepos = repository.newResolutionRepositories(session,
        repos);

    final DependencyResult result = repository.resolveDependencies(session,
        new DependencyRequest(new CollectRequest((Dependency) null, dependencies, resolutionRepos),
            null));

    return result.getArtifactResults().stream()
        .map(artifact -> artifact.getArtifact().getPath())
        .toList();
  }

  private List<String> extractLines(ZipFile zipFile, ZipEntry file) {
    final List<String> lines = new ArrayList<>();

    if (file == null || file.isDirectory()) {
      throw new IllegalArgumentException("Invalid ZipEntry: " + file);
    }

    try (final InputStream is = zipFile.getInputStream(file);
        final BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

      String line;
      while ((line = reader.readLine()) != null) {
        lines.add(line);
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to read ZipEntry: " + file, e);
    }

    return lines;
  }
}
