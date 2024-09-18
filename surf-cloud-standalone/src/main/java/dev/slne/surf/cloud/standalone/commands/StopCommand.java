package dev.slne.surf.cloud.standalone.commands;

import org.springframework.shell.command.CommandContext;
import org.springframework.shell.command.annotation.Command;

public class StopCommand {

  @Command(
      command = "stop",
      alias = {"exit", "quit"},
      description = "Stops the server"
  )
  public void stop(CommandContext context) {
    context.getTerminal().writer().println("Stopping server...");
    System.exit(0);
  }
}
