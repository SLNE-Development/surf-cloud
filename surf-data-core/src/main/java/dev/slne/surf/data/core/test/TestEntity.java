package dev.slne.surf.data.core.test;

import jakarta.persistence.Entity;
import java.util.UUID;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity(name = "test_entity")
public class TestEntity extends AbstractPersistable<UUID> {

  private String name;

  public TestEntity() {
  }

  public TestEntity(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
