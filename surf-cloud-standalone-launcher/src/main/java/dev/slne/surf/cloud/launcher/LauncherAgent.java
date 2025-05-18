package dev.slne.surf.cloud.launcher;

import java.lang.instrument.Instrumentation;

public class LauncherAgent {

  public static void agentmain(String agentArgs, Instrumentation inst) {
    premain(agentArgs, inst);
  }

  public static void premain(String agentArgs, Instrumentation inst) {
    System.out.println("Launcher-Agent started. Loading actual agent...");
//    InstrumentationSavingAgent.premain(agentArgs, inst);
  }
}
