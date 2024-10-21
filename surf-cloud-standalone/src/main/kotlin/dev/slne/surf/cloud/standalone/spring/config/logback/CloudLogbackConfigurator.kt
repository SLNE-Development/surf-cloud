package dev.slne.surf.cloud.standalone.spring.config.logback

object CloudLogbackConfigurator {
    fun configure() {
        try {
            ColorConverterModifier.changeInfoColorToWhite()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
