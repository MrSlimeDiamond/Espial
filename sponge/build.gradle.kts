import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.model.PluginDependency

plugins {
    java
    id("org.spongepowered.gradle.plugin") version "2.3.0"
}

repositories {
    mavenCentral()
}

// only compile submodules
tasks.jar {
    enabled = false
}

val pluginVersion: String by project

sponge {
    license("MIT")
    loader {
        name(PluginLoaders.JAVA_PLAIN)
        version("1.0")
    }
    plugin("espial") {
        displayName("Espial")
        entrypoint("net.slimediamond.espial.EspialSponge")
        description("A plugin for looking up blocks and fixing grief")
        version(pluginVersion)
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
