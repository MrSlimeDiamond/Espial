import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.model.PluginDependency

plugins {
    `java-library`
    `maven-publish`
    id("signing")
    id("org.spongepowered.gradle.plugin") version "2.2.0"
    id("com.gradleup.shadow") version "9.0.0-beta8"
}

group = "net.slimediamond"
version = "1.2.2-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.enginehub.org/repo/")
}

dependencies {
    compileOnly("com.sk89q.worldedit:worldedit-core:7.3.10")
    //compileOnly("com.sk89q.worldedit:worldedit-sponge:7.3.10")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.0")

    // HACKHACK: Include a WorldEdit jar yourself
    // https://ore.spongepowered.org/EngineHub/WorldEdit/versions
    // EngineHub repo is broken - raised this to them. They might fix.
    compileOnly(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
}

sponge {
    apiVersion("12.1.0-SNAPSHOT")
    license("MIT")
    loader {
        name(PluginLoaders.JAVA_PLAIN)
        version("1.0")
    }
    plugin("espial") {
        displayName("Espial")
        entrypoint("net.slimediamond.espial.Espial")
        description("A plugin for looking up blocks and fixing grief")
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

tasks.shadowJar {
    archiveClassifier.set("")
    mergeServiceFiles()
    relocate("com.fasterxml.jackson", "net.slimediamond.jackson")
}

val apiJar by tasks.registering(Jar::class) {
    archiveClassifier.set("api")
    from(sourceSets.main.get().output)
    include("net/slimediamond/espial/api/**")
}

tasks.build {
    dependsOn(tasks.shadowJar)
    dependsOn("apiJar")
}

artifacts {
    archives(tasks.shadowJar)
}

java {
  withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "espial"

            // Attach the API JAR
            artifact(tasks["apiJar"]) {
                classifier = ""
            }

            pom {
                name.set("Espial")
                description.set("A plugin for looking up blocks and fixing grief")
                url.set("https://github.com/MrSlimeDiamond/Espial")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                developers {
                    developer {
                        id.set("SlimeDiamond")
                        name.set("Findlay Richardson")
                        email.set("findlayrichardson3@gmail.com")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/MrSlimeDiamond/Espial.git")
                    developerConnection.set("scm:git:ssh://github.com/MrSlimeDiamond/Espial.git")
                    url.set("https://github.com/MrSlimeDiamond/Espial")
                }
            }
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/MrSlimeDiamond/Espial")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }
        maven {
            url = uri("${System.getProperty("user.home")}/.m2/repository")
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications["maven"])
}
