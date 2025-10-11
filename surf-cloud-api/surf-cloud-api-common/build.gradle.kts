import dev.slne.surf.surfapi.gradle.util.slneReleases
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.nio.file.Files
import java.nio.file.StandardOpenOption

plugins {
    `exclude-kotlin`
    id("dev.slne.surf.surfapi.gradle.core")
    idea
}

dependencies {
    api(libs.bundles.spring.api.common)
    api(libs.bundles.jackson.api.common)
    api(libs.bundles.spring.aop)

    api(libs.aide.reflection)
    api(libs.bundles.netty.all)
    api(libs.kotlin.byte.buf.serializer) {
        exclude(group = "io.netty")
    }

    api(libs.datafixerupper) {
        isTransitive = false
    }
    api(libs.byte.buddy)
}

val writeCloudVersion by tasks.registering {
    group = "build"
    description = "Writes the cloud version to the classpath resource"

    val outputDir = layout.projectDirectory.dir("src/main/resources")
    val outputFile = outputDir.file("cloud.version")

    doLast {
        outputDir.asFile.mkdirs()
        outputFile.asFile.writeText(project.version as String)
    }
}

tasks {
    processResources {
        dependsOn(writeCloudVersion)
    }
}

kotlin {
    compilerOptions {
        optIn.add("dev.slne.surf.cloud.api.common.util.annotation.InternalApi")
    }
}

publishing {
    repositories {
        slneReleases()
    }
}



// ---------------- Generate StreamCodec Composites ----------------


val streamCodecPackage = "dev.slne.surf.cloud.api.common.netty.network.codec"
val compositeStartFrom = 2
val compositeMaxArity = 20

val generatedDir = layout.buildDirectory.dir("generated/sources/streamcodec/kotlin")

val genComposites = tasks.register("generateStreamCodecComposites") {
    group = "generate"

    outputs.dir(generatedDir)
    inputs.property("startFrom", compositeStartFrom)
    inputs.property("maxArity", compositeMaxArity)

    doLast {
        require(compositeMaxArity >= compositeStartFrom) { "maxArity ($compositeMaxArity) must be >= startFrom ($compositeStartFrom)" }
        val file = generatedDir.get().file("StreamCodecComposites.kt").asFile
        file.parentFile.mkdirs()
        val content = buildString {
            appendLine("// GENERATED FILE — do not edit manually")
            appendLine("@file:Suppress(\"RedundantVisibilityModifier\", \"unused\")")
            appendLine("package $streamCodecPackage")
            appendLine()
            appendLine("import java.util.function.Function")
            appendLine()
            appendLine("@Suppress(\"UNCHECKED_CAST\", \"ClassName\", \"NOTHING_TO_INLINE\")")
            appendLine("private object _CompositeGenSupport {")
            appendLine("    @JvmStatic inline fun <B, T> dec(c: StreamCodec<in B, T>, buf: B): T = (c as StreamCodec<B, T>).decode(buf)")
            appendLine("    @JvmStatic inline fun <B, T> enc(c: StreamCodec<in B, T>, buf: B, v: T) = (c as StreamCodec<B, T>).encode(buf, v)")
            appendLine("}")
            appendLine()


            fun genArity(n: Int) {
                val typeParams = (1..n).joinToString(", ") { "T$it" }
                val params = buildString {
                    for (i in 1..n) {
                        appendLine("        codec$i: StreamCodec<in B, T$i>,")
                        appendLine("        from$i: Function<C, T$i>,")
                    }
                    append("        to: (")
                    append((1..n).joinToString(", ") { "T$it" })
                    appendLine(") -> C")
                }
                val decodeVars = (1..n).joinToString("\n") { i ->
                    "                val o$i: T$i = _CompositeGenSupport.dec(codec$i, buf)"
                }
                val encodeLines = (1..n).joinToString("\n") { i ->
                    "                _CompositeGenSupport.enc(codec$i, buf, from$i.apply(value))"
                }
                val toArgs = (1..n).joinToString(", ") { "o$it" }

                appendLine("public fun <B, C, $typeParams> StreamCodec.Companion.composite(")
                appendLine(params)
                appendLine("): StreamCodec<B, C> {")
                appendLine("    return object : StreamCodec<B, C> {")
                appendLine("        override fun decode(buf: B): C {")
                appendLine(decodeVars)
                appendLine("            return to($toArgs)")
                appendLine("        }")
                appendLine()
                appendLine("        override fun encode(buf: B, value: C) {")
                appendLine(encodeLines)
                appendLine("        }")
                appendLine("    }")
                appendLine("}")
                appendLine()
            }

            for (n in compositeStartFrom..compositeMaxArity) genArity(n)

        }
        Files.write(
            file.toPath(),
            content.toByteArray(),
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE
        )

        println("Generated ${file.relativeTo(project.projectDir)} with composite ${compositeStartFrom}..$compositeMaxArity")
    }
}

val streamCodecGenerated = files(generatedDir).builtBy(genComposites)

kotlin {
    sourceSets.main {
        kotlin.srcDir(streamCodecGenerated)
    }
}

tasks {
    withType<KotlinCompile>().configureEach {
        dependsOn(genComposites)
    }
}

tasks.named("build") {
    dependsOn(genComposites)
}

idea {
    module {
        generatedSourceDirs.add(generatedDir.get().asFile)
    }
}