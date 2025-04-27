import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.model.PluginDependency

plugins {
    `java-library`
    `maven-publish`
    id("signing")
    id("org.spongepowered.gradle.plugin") version "2.3.0"
    id("com.gradleup.shadow") version "9.0.0-beta8"
}

group = "net.slimediamond"

repositories {
    mavenCentral()
    maven("https://maven.enginehub.org/repo/")
}

val spongeDefault: String by project
val pluginName: String by project
val pluginId: String by project
val pluginDescription: String by project

// build in the same location
subprojects {
    tasks.withType<Jar>().configureEach {
        destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs"))
    }
}

dependencies {
    compileOnly("com.sk89q.worldedit:worldedit-sponge:7.3.12-SNAPSHOT")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.0")

    compileOnly("org.spongepowered:spongeapi:${spongeDefault}")

    api(project(":api"))
}

allprojects {
    apply(plugin = "java")
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
}

tasks.shadowJar {
    archiveClassifier.set("")
    mergeServiceFiles()
    relocate("com.fasterxml.jackson", "net.slimediamond.jackson")
}

artifacts {
    archives(tasks.shadowJar)
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.jar {
    dependsOn(tasks.shadowJar)
    enabled = false
}

artifacts {
    archives(tasks.shadowJar)
}

sponge {
    apiVersion(spongeDefault)
    license("MIT")
    loader {
        name(PluginLoaders.JAVA_PLAIN)
        version("1.0")
    }
    plugin(pluginId) {
        displayName(pluginName)
        entrypoint("net.slimediamond.espial.Espial")
        description(pluginDescription)
        dependency("spongeapi") {
            loadOrder(PluginDependency.LoadOrder.AFTER)
            optional(false)
        }
        dependency("worldedit") {
            loadOrder(PluginDependency.LoadOrder.AFTER)
            version("7.1.0")
            optional(true)
        }
        contributor("SlimeDiamond") {
            description("Lead Developer")
        }
    }
}
