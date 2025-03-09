import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.model.PluginDependency

plugins {
    id("java")
    id("org.spongepowered.gradle.plugin") version "2.3.0"
    id("com.gradleup.shadow") version "9.0.0-beta8"
}

group = "net.slimediamond"

repositories {
    mavenCentral()
}

val spongeDefault: String by project
val pluginVersion: String by project
val pluginName: String by project
val pluginId: String by project
val pluginDescription: String by project

dependencies {
    implementation(project(":"))
//    implementation("org.spongepowered:spongeapi:${spongeDefault}")
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    archiveClassifier.set("sponge-12")
    mergeServiceFiles()
    archiveBaseName.set("${pluginId}-${pluginVersion}")
    relocate("com.fasterxml.jackson", "net.slimediamond.jackson")
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

// TODO: Make some common logic for this
sponge {
    apiVersion("12.0.0")
    license("MIT")
    loader {
        name(PluginLoaders.JAVA_PLAIN)
        version("1.0")
    }
    plugin(pluginId) {
        displayName(pluginName)
        version(pluginVersion)
        entrypoint("net.slimediamond.espial.EspialSpongeAPI12")
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