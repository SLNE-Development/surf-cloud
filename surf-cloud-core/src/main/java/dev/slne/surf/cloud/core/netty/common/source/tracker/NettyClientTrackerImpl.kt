package dev.slne.surf.cloud.core.netty.common.source.tracker;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.flogger.StackSize;
import dev.slne.surf.cloud.api.netty.source.ProxiedNettySource;
import dev.slne.surf.cloud.api.netty.source.tracker.NettyClientTracker;
import dev.slne.surf.cloud.core.netty.AbstractNettyBase;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.ParametersAreNonnullByDefault;
import lombok.RequiredArgsConstructor;
import lombok.extern.flogger.Flogger;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;

@Flogger
@RequiredArgsConstructor
@ParametersAreNonnullByDefault
public class NettyClientTrackerImpl<Client extends ProxiedNettySource<Client>> implements
    NettyClientTracker<Client> {

  private final ObjectSet<Client> clients = ObjectSets.synchronize(new ObjectOpenHashSet<>());
  private final AbstractNettyBase<?, ?, Client> nettyBase;

  @Override
  @Unmodifiable
  public ObjectSet<Client> findByGroupId(String groupId) {
    checkNotNull(groupId, "groupId");

    return ObjectSets.unmodifiable(clients.stream()
        .filter(client -> client.cloudServer().isPresent())
        .filter(client -> Objects.equals(client.cloudServer().orElseThrow().groupId(), groupId))
        .collect(ObjectOpenHashSet::new, ObjectSet::add, ObjectSet::addAll));
  }

  @Override
  public Optional<Client> findByServerGuid(long serverGuid) {
    return clients.stream()
        .filter(client -> client.cloudServer().isPresent())
        .filter(client -> client.cloudServer().orElseThrow().serverGuid() == serverGuid)
        .findFirst();
  }

  public Client addClient(Client client) {
    checkNotNull(client, "client");

    if (!clients.add(client)) {
      log.atWarning()
          .withStackTrace(StackSize.MEDIUM)
          .log("Client %s already exists in the list", client);
    }

    return client;
  }

  @Override
  @UnmodifiableView
  public ObjectSet<Client> clients() {
    return ObjectSets.unmodifiable(clients);
  }

  public void removeClient(Client client) {
    checkNotNull(client, "client");

    clients.remove(client);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NettyClientTrackerImpl<?> that)) {
      return false;
    }

    return clients.equals(that.clients) && Objects.equals(nettyBase, that.nettyBase);
  }

  @Override
  public int hashCode() {
    int result = clients.hashCode();
    result = 31 * result + Objects.hashCode(nettyBase);
    return result;
  }
}
