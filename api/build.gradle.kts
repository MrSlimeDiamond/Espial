plugins {
    id("java")
    `maven-publish`
    signing
}

group = "net.slimediamond"

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/maven")
}

val spongeDefault: String by project

dependencies {
    compileOnly("org.spongepowered:spongeapi:${spongeDefault}")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.0")
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    archiveBaseName.set("espial-api")
}

java {
//    withJavadocJar()
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

signing {
    useGpgCmd()
    sign(publishing.publications["maven"])
}
