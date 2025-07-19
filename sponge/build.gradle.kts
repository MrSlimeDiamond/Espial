import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.model.PluginDependency

plugins {
    id("java")
    id("org.spongepowered.gradle.plugin") version "2.3.0"
    id("com.gradleup.shadow") version "9.0.0-beta8"
}

val javaTarget = 21

java {
    sourceCompatibility = JavaVersion.toVersion(javaTarget)
    targetCompatibility = JavaVersion.toVersion(javaTarget)
    if (JavaVersion.current() < JavaVersion.toVersion(javaTarget)) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(javaTarget))
    }
}

repositories {
    mavenCentral()
    maven("https://maven.enginehub.org/repo/")
}

val spongeDefault: String by project
val pluginName: String by project
val pluginId: String by project
val pluginDescription: String by project

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("com.zaxxer:HikariCP:6.3.0") {
        exclude("org.slf4j")
    }
    compileOnly("org.spongepowered:spongeapi:$spongeDefault")
    compileOnly("org.jetbrains:annotations:26.0.2")
    compileOnly("com.sk89q.worldedit:worldedit-sponge:7.3.12-SNAPSHOT")
    api(project(":api"))
    api(project(":common"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    archiveClassifier.set("")
    archiveBaseName.set("espial")
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
        entrypoint("net.slimediamond.espial.sponge.Espial")
        description(pluginDescription)
        links {
            homepage("https://github.com/MrSlimeDiamond/Espial")
            source("https://github.com/MrSlimeDiamond/Espial")
            issues("https://github.com/MrSlimeDiamond/Espial/issues")
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
        contributor("SlimeDiamond") {
            description("Lead Developer")
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["shadow"])

            artifactId = "espial-sponge"
        }
    }
}
