package dev.slne.surf.cloud.standalone.commands
//
//import dev.slne.surf.cloud.api.util.buildAnsiString
//import dev.slne.surf.cloud.core.util.bean
//import dev.slne.surf.cloud.standalone.independentCloudInstance
//import dev.slne.surf.cloud.standalone.netty.server.NettyServerImpl
//import org.springframework.boot.ansi.AnsiColor
//import org.springframework.shell.command.CommandContext
//import org.springframework.shell.command.annotation.Command
//
//@Command(command = ["debug"], description = "Debug command")
//class DebugCommand {
//
//    @Command(command = ["listClients"], description = "List connected clients")
//    fun listConnectedClients(context: CommandContext) {
//        with(context.terminal.writer()) {
//            val server = bean<NettyServerImpl>()
//            val clients = server.connection.clientTracker.clients
//
//            println(buildAnsiString(AnsiColor.GREEN, "Connected clients:"))
//            clients.forEach { client ->
//                println("-  ${client.serverGuid} (${client.channel.remoteAddress()})")
//            }
//        }
//    }
//}