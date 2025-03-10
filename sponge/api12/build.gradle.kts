plugins {
    id("java")
    id("org.spongepowered.gradle.plugin")
    id("com.gradleup.shadow") version "9.0.0-beta8"
}

group = "net.slimediamond"

repositories {
    mavenCentral()
}

val spongeDefault: String by project
val pluginVersion: String by project

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
    archiveBaseName.set("espial-${pluginVersion}")
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

sponge {
    apiVersion("12.0.0")
}
