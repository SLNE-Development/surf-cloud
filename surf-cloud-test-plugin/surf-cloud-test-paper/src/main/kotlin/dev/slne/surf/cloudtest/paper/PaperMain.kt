package dev.slne.surf.cloudtest.paper

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.cloudtest.TestSpringApplication
import dev.slne.surf.cloudtest.paper.test.command.testPpdcCommand
import org.bukkit.plugin.java.JavaPlugin
import org.springframework.beans.factory.getBean

class PaperMain : SuspendingJavaPlugin() {
    override suspend fun onLoadAsync() {
        testPpdcCommand()
    }
}

val plugin get() = JavaPlugin.getPlugin(PaperMain::class.java)
inline fun <reified T : Any> bean() = TestSpringApplication.context.getBean<T>()