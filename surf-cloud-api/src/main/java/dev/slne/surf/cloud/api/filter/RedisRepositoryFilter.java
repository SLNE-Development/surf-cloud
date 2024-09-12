package dev.slne.surf.cloud.api.filter;

import dev.slne.surf.cloud.api.meta.SurfRedisRepository;
import org.springframework.core.type.filter.AnnotationTypeFilter;

public class RedisRepositoryFilter extends AnnotationTypeFilter {

  public RedisRepositoryFilter() {
    super(SurfRedisRepository.class);
  }

  @Override
  protected Boolean hasAnnotation(String typeName) {
    Boolean b = super.hasAnnotation(typeName);
    System.err.println("Checking Redis: " + typeName + " -> " + b);
    return b;
  }
}
