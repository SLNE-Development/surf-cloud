package dev.slne.surf.cloud.api.server.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.ZonedDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class SurfAuditableEntity extends SurfEntity {

  @CreatedDate
  @Column(name = "created_date")
  private ZonedDateTime createdDate;

  @LastModifiedDate
  @Column(name = "last_modified_date")
  private ZonedDateTime lastModifiedDate;

  public ZonedDateTime getLastModifiedDate() {
    return lastModifiedDate;
  }

  public void setLastModifiedDate(ZonedDateTime lastModifiedDate) {
    this.lastModifiedDate = lastModifiedDate;
  }

  public ZonedDateTime getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(ZonedDateTime createdDate) {
    this.createdDate = createdDate;
  }
}
