import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.model.PluginDependency

plugins {
    `java-library`
    id("org.spongepowered.gradle.plugin") version "2.2.0"
}

group = "net.slimediamond"
version = "1.1-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.enginehub.org/repo/")
}

dependencies {
    compileOnly("com.sk89q.worldedit:worldedit-core:7.3.9")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.0")

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
}

sponge {
    apiVersion("14.0.0-SNAPSHOT")
    license("All-Rights-Reserved")
    loader {
        name(PluginLoaders.JAVA_PLAIN)
        version("1.0")
    }
    plugin("espial") {
        displayName("espial")
        entrypoint("net.slimediamond.espial.Espial")
        description("Plugin for logging and viewing block actions")
        links {
            // homepageLink("https://spongepowered.org")
            // sourceLink("https://spongepowered.org/source")
            // issuesLink("https://spongepowered.org/issues")
        }
        dependency("spongeapi") {
            loadOrder(PluginDependency.LoadOrder.AFTER)
            optional(false)
        }
        dependency("worldedit") {
            loadOrder(PluginDependency.LoadOrder.AFTER)
            version("7.1.0")
            optional(true)
        }
    }
}

val javaTarget = 21 // Sponge targets a minimum of Java 17
java {
    sourceCompatibility = JavaVersion.toVersion(javaTarget)
    targetCompatibility = JavaVersion.toVersion(javaTarget)
    if (JavaVersion.current() < JavaVersion.toVersion(javaTarget)) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(javaTarget))
    }
}

tasks.withType(JavaCompile::class).configureEach {
    options.apply {
        encoding = "utf-8" // Consistent source file encoding
        if (JavaVersion.current().isJava10Compatible) {
            release.set(javaTarget)
        }
    }
}

// Make sure all tasks which produce archives (jar, sources jar, javadoc jar, etc) produce more consistent output
tasks.withType(AbstractArchiveTask::class).configureEach {
    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false
}
