package dev.slne.surf.cloud.bukkit.aspects

import dev.slne.surf.cloud.bukkit.processor.BukkitLifecycleProcessor
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.After
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component

@Aspect
@Component
class SurfBukkitLifecycleAspect(private val bukkitLifecycleProcessor: BukkitLifecycleProcessor) {

    @After("execution(* org.bukkit.plugin.java.JavaPlugin.onLoad(..))")
    fun afterOnLoad(joinPoint: JoinPoint) {
        val pluginClass = joinPoint.target::class
        bukkitLifecycleProcessor.getLifecycles(pluginClass)?.forEach { it.onLoad() }
    }

    @After("execution(* org.bukkit.plugin.java.JavaPlugin.onEnable(..))")
    fun afterOnEnable(joinPoint: JoinPoint) {
        val pluginClass = joinPoint.target::class
        bukkitLifecycleProcessor.getLifecycles(pluginClass)?.forEach { it.onEnable() }
    }

    @After("execution(* org.bukkit.plugin.java.JavaPlugin.onDisable(..))")
    fun afterOnDisable(joinPoint: JoinPoint) {
        val pluginClass = joinPoint.target::class
        bukkitLifecycleProcessor.getLifecycles(pluginClass)?.forEach { it.onDisable() }
    }
}
