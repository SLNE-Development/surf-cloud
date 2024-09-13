package dev.slne.surf.cloud.core.netty.common;

import com.google.common.flogger.StackSize;
import dev.slne.surf.cloud.core.netty.NettyBase;
import dev.slne.surf.cloud.core.netty.ProxiedNettySource;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.flogger.Flogger;
import org.jetbrains.annotations.UnmodifiableView;

@Flogger
@RequiredArgsConstructor
public final class SourceList<Source extends ProxiedNettySource> {

  private final ObjectSet<Source> clients = ObjectSets.synchronize(new ObjectOpenHashSet<>());
  private final NettyBase nettyBase;

  public ObjectSet<Source> findByGroupId(String groupId) {
    return clients.stream()
        .filter(source -> Objects.equals(source.getCloudServer().groupId(), groupId))
        .collect(ObjectOpenHashSet::new, ObjectSet::add, ObjectSet::addAll);
  }

  public Optional<Source> findByServerGuid(long serverGuid) {
    return clients.stream()
        .filter(source -> source.getCloudServer().serverGuid() == serverGuid)
        .findFirst();
  }

  public Source addClient(Source source) {
    if (!clients.add(source)) {
      log.atWarning()
          .withStackTrace(StackSize.MEDIUM)
          .log("Client %s already exists in the list", source);
    }

    return source;
  }

  @UnmodifiableView
  public ObjectSet<Source> getClients() {
    return ObjectSets.unmodifiable(clients);
  }

  public void removeClient(Source source) {
    clients.remove(source);
  }
}
