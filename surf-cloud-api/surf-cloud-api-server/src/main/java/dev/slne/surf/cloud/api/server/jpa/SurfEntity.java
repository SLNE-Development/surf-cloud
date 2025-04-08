package dev.slne.surf.cloud.api.server.jpa;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import java.util.Objects;
import org.springframework.data.domain.Persistable;
import org.springframework.data.util.ProxyUtils;
import org.springframework.lang.Nullable;

/**
 * Base class for JPA entities in the Surf Cloud application. Implements the {@link Persistable}
 * interface to provide consistent behavior for entity lifecycle operations.
 */
@MappedSuperclass
public abstract class SurfEntity implements Persistable<Long> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Nullable
  private Long id;

  @Nullable
  @Override
  public Long getId() {
    return id;
  }

  public void setId(@Nullable Long id) {
    this.id = id;
  }

  @Transient
  @Override
  public boolean isNew() {
    return null == getId();
  }

  @Override
  public String toString() {
    return String.format("Entity of type %s with id: %s", this.getClass().getName(), getId());
  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || ProxyUtils.getUserClass(this) != ProxyUtils.getUserClass(o)) {
      return false;
    }
    SurfEntity that = (SurfEntity) o;
    return getId() != null && Objects.equals(getId(), that.getId());
  }

  @Override
  public final int hashCode() {
    return ProxyUtils.getUserClass(this).hashCode();
  }
}
