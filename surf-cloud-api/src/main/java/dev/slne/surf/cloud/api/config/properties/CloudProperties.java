package dev.slne.surf.cloud.api.config.properties;

import static dev.slne.surf.cloud.api.config.properties.SystemProperty.property;

import java.util.function.Function;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CloudProperties {

  // @formatter:off
  public final SystemProperty<String> SERVER_CATEGORY = property("serverCategory", Function.identity(), "default");
  // @formatter:on
}
