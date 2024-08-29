package dev.slne.surf.data.core.spring;

import java.io.PrintStream;
import org.springframework.boot.Banner;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.core.env.Environment;

public class SurfSpringBanner implements Banner {

  @Override
  public void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) {
    out.println(AnsiOutput.toString(AnsiColor.GREEN, "Starting plugin with Spring Boot..."));
  }
}
