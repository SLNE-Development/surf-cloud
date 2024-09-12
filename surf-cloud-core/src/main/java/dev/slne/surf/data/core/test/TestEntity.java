package dev.slne.surf.data.core.test;

import dev.slne.surf.data.api.jpa.SurfEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Entity(name = "test_entity")
@Table(name = "test_entities")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TestEntity extends SurfEntity {

  @Column(name = "name", nullable = false, length = 16)
  private String name;

  @Column(name = "uuid", nullable = false, unique = true)
  private UUID uuid;

  @Setter
  @Column(name = "age", nullable = false)
  private int age;

  @Setter
  @Column(name = "active", nullable = false)
  private boolean active;

  @Setter
  @Column(name = "balance", nullable = false)
  private double balance;

  @Column(name = "dumb", nullable = false)
  @Setter
  private boolean dumb;

  @Setter
  @Column(name = "dummy")
  private String dummy;

  public TestEntity(boolean active, int age, double balance, UUID uuid, String name, boolean dumb) {
    this.active = active;
    this.age = age;
    this.balance = balance;
    this.uuid = uuid;
    this.name = name;
    this.dumb = dumb;
  }
}
