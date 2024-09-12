package dev.slne.surf.data.api.jpa;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import java.io.Serializable;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.data.util.ProxyUtils;
import org.springframework.lang.Nullable;

@MappedSuperclass
public abstract class SurfEntity implements Persistable<Long> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private @Nullable Long id;

  @Override
  @Nullable
  public Long getId() {
    return id;
  }

  @Override
  @Transient
  public boolean isNew() {
    return null == id;
  }

  @Override
  public String toString() {
    return String.format("Entity of type %s with id: %s", this.getClass().getName(), getId());
  }

  @Override
  public boolean equals(Object obj) {

    if (null == obj) {
      return false;
    }

    if (this == obj) {
      return true;
    }

    if (!getClass().equals(ProxyUtils.getUserClass(obj))) {
      return false;
    }

    AbstractPersistable<?> that = (AbstractPersistable<?>) obj;

    return null != this.getId() && this.getId().equals(that.getId());
  }

  @Override
  public int hashCode() {

    int hashCode = 17;

    hashCode += null == getId() ? 0 : getId().hashCode() * 31;

    return hashCode;
  }
}
