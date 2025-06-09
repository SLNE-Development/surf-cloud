package dev.slne.surf.cloud.standalone

import java.lang.instrument.Instrumentation
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType

object InstrumentationProvider {
    private val classLoader: ClassLoader = javaClass.classLoader.parent
    private val getInstrumentationMethodHandle: MethodHandle

    init {
        val lookup = MethodHandles.lookup()
        val launcherAgentClass =
            Class.forName("dev.slne.surf.cloud.launcher.LauncherAgent", true, classLoader)
        getInstrumentationMethodHandle = lookup.findStatic(
            launcherAgentClass, "getInstrumentation", MethodType.methodType(
                Instrumentation::class.java
            )
        )
    }

    fun getInstrumentation(): Instrumentation {
        return getInstrumentationMethodHandle.invokeExact() as Instrumentation
    }
}