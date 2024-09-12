package dev.slne.surf.data.bukkit.aspects;

import dev.slne.surf.data.api.lifecycle.SurfLifecycle;
import dev.slne.surf.data.bukkit.processor.BukkitLifecycleProcessor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class SurfBukkitLifecycleAspect {

  private final BukkitLifecycleProcessor bukkitLifecycleProcessor;

  public SurfBukkitLifecycleAspect(BukkitLifecycleProcessor bukkitLifecycleProcessor) {
    this.bukkitLifecycleProcessor = bukkitLifecycleProcessor;
  }

  @After("execution(* org.bukkit.plugin.java.JavaPlugin.onLoad(..))")
  public void afterOnLoad(JoinPoint joinPoint) {

    final Class<?> pluginClass = joinPoint.getTarget().getClass();
    bukkitLifecycleProcessor.getLifecycles(pluginClass).forEach(SurfLifecycle::onLoad);
  }

  @After("execution(* org.bukkit.plugin.java.JavaPlugin.onEnable(..))")
  public void afterOnEnable(JoinPoint joinPoint) {

    final Class<?> pluginClass = joinPoint.getTarget().getClass();
    bukkitLifecycleProcessor.getLifecycles(pluginClass).forEach(SurfLifecycle::onEnable);
  }

  @After("execution(* org.bukkit.plugin.java.JavaPlugin.onDisable(..))")
  public void afterOnDisable(JoinPoint joinPoint) {

    final Class<?> pluginClass = joinPoint.getTarget().getClass();
    bukkitLifecycleProcessor.getLifecycles(pluginClass).forEach(SurfLifecycle::onDisable);
  }
}
