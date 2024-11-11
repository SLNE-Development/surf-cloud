package dev.slne.surf.cloud.core.common.spring

import dev.slne.surf.cloud.api.common.util.buildAnsiString
import org.springframework.boot.Banner
import org.springframework.boot.ansi.AnsiColor
import org.springframework.core.env.Environment
import java.io.PrintStream

class SurfSpringBanner : Banner {
    override fun printBanner(environment: Environment, sourceClass: Class<*>?, out: PrintStream) {
        out.println(
            buildAnsiString(
                AnsiColor.GREEN,
                "Starting plugin with Spring Boot..."
            )
        )
    }
}
