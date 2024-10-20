package dev.slne.surf.cloud.core.spring.config;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import dev.slne.surf.cloud.core.SurfCloudCoreInstance;
import dev.slne.surf.cloud.core.config.SurfCloudConfig;
import dev.slne.surf.surfapi.core.api.SurfCoreApi;
import java.util.TimeZone;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class SurfCloudCommonConfig {

  @Bean
  public SurfCloudConfig surfDataConfig() {
    return SurfCoreApi.getCore().createModernYamlConfig(
        SurfCloudConfig.class,
        SurfCloudCoreInstance.get().dataFolder,
        "config.yml"
    );
  }


  @Bean
  public JsonMapper objectMapper(ObjectProvider<com.fasterxml.jackson.databind.Module> modules) {
    return JsonMapper.builder()
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false)
        .configure(SerializationFeature.WRITE_DATES_WITH_ZONE_ID, true)
        .configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true)
        .configure(Feature.USE_FAST_DOUBLE_PARSER, true)
        .configure(Feature.USE_FAST_BIG_NUMBER_PARSER, true)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .addModules(modules.orderedStream().toArray(Module[]::new))
        .defaultTimeZone(TimeZone.getDefault())
        .build();
  }
}
