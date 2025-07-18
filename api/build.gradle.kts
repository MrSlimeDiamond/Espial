plugins {
    id("java")
    `maven-publish`
}

group = "net.slimediamond"
version = "2.0r-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/maven")
}

val spongeDefault: String by project

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    compileOnly("org.spongepowered:spongeapi:$spongeDefault")
    compileOnly("org.jetbrains:annotations:26.0.2")
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "espial"

            // Attach the API JAR
            artifact(tasks["jar"]) {
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