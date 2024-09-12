package dev.slne.surf.cloud.api.filter;

import dev.slne.surf.cloud.api.meta.SurfJpaRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public class JpaRepositoryFilter extends org.springframework.core.type.filter.AnnotationTypeFilter {

//  public JpaRepositoryFilter() {
//    super(SurfJpaRepository.class);
//  }

  private static final String JPA_REPOSITORY = JpaRepository.class.getName();

  public JpaRepositoryFilter() {
    super(SurfJpaRepository.class);
  }

//  @Override
//  public boolean match(MetadataReader metadata, MetadataReaderFactory metadataReaderFactory) {
//    boolean matches = Arrays.asList(metadata.getClassMetadata().getInterfaceNames()).contains(SurfJpaRepository.class.getName());
//    System.err.println("Checking: " + metadata.getClassMetadata().getClassName() + " -> " + matches);
//    return matches;
//  }


  @Override
  protected Boolean hasAnnotation(String typeName) {
    Boolean b = super.hasAnnotation(typeName);
    System.err.println("Checking Jpa: " + typeName + " -> " + b);
    return b;
  }
}
