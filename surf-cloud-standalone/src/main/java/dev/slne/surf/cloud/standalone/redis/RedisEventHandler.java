package dev.slne.surf.cloud.standalone.redis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisEventHandler {

  @AliasFor("channels")
  String[] value() default {};

  @AliasFor("value")
  String[] channels() default {};
}
