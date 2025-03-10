plugins {
    `java-library`
    `maven-publish`
    id("signing")
    id("com.gradleup.shadow") version "9.0.0-beta8"
}

group = "net.slimediamond"

repositories {
    mavenCentral()
    maven("https://maven.enginehub.org/repo/")
}

val spongeDefault: String by project

// build in the same location
subprojects {
    tasks.withType<Jar>().configureEach {
        destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs"))
    }
}

dependencies {
    compileOnly("com.sk89q.worldedit:worldedit-core:7.3.10")
    //compileOnly("com.sk89q.worldedit:worldedit-sponge:7.3.10")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.0")

    compileOnly("org.spongepowered:spongeapi:${spongeDefault}")

    api(project(":api"))

    // HACKHACK: Include a WorldEdit jar yourself
    // https://ore.spongepowered.org/EngineHub/WorldEdit/versions
    // EngineHub repo is broken - raised this to them. They might fix.
    compileOnly(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
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

    dependsOn(":sponge:api12:shadowJar")
    dependsOn(":sponge:api14:shadowJar")
}

artifacts {
    archives(tasks.shadowJar)
}
