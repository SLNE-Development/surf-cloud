package dev.slne.surf.cloud.standalone.spring.config.logback

import ch.qos.logback.classic.Level
import dev.slne.surf.surfapi.core.api.util.Util
import org.springframework.boot.ansi.AnsiColor
import org.springframework.boot.ansi.AnsiElement
import org.springframework.boot.logging.logback.ColorConverter
import java.lang.reflect.Field
import java.util.*

internal object ColorConverterModifier {
    private fun modify(modifiableLevels: MutableMap<Int, AnsiElement>) {
        modifiableLevels[Level.INFO_INTEGER] = AnsiColor.DEFAULT
    }

    fun changeInfoColorToWhite() {
        val levelsField = ColorConverter::class.java.getDeclaredField("LEVELS")
        levelsField.isAccessible = true

        val modifiableLevels = extractLevels(levelsField)
        modify(modifiableLevels)

        Util.setStaticFinalField(levelsField, Collections.unmodifiableMap(modifiableLevels))
    }

    private fun extractLevels(field: Field) = HashMap(field.get(null) as Map<Int, AnsiElement>)
}
