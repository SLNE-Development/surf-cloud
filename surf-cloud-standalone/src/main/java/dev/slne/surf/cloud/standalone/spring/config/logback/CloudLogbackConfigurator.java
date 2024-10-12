package dev.slne.surf.cloud.standalone.spring.config.logback;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CloudLogbackConfigurator {

  @SuppressWarnings("CallToPrintStackTrace")
  public void configure() {
    try {
      ColorConverterModifier.changeInfoColorToWhite();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
