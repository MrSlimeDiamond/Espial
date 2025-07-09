plugins {
    id("java")
}

group = "net.slimediamond.espial"
version = "2.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    compileOnly("org.jetbrains:annotations:26.0.2")
    compileOnly("net.kyori:adventure-api:4.22.0")
    compileOnly(project(":api"))
}

tasks.test {
    useJUnitPlatform()
}