//package dev.slne.surf.cloud.standalone.commands
//
//import dev.slne.surf.cloud.api.exceptions.ExitCodes
//import org.springframework.shell.command.CommandContext
//import org.springframework.shell.command.annotation.Command
//import kotlin.system.exitProcess
//
//
//@Command(group = "lifecycle")
//class LifecycleCommand {
//
//    @Command(command = ["stop"], description = "Stops the server")
//    fun stop(context: CommandContext) {
//        context.terminal.writer().println("Stopping server...")
//        exitProcess(ExitCodes.NORMAL)
//    }
//}
