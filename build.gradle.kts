plugins {
    `maven-publish`
}

allprojects {
    plugins.apply("maven-publish")
    group = "net.slimediamond"
    version = "2.0r-SNAPSHOT"
    publishing {
        repositories {
            maven {
                name = "GitLab"
                url = uri("https://gitlab.com/api/v4/projects/" + System.getenv("CI_PROJECT_ID") + "/packages/maven")
                credentials(HttpHeaderCredentials::class) {
                    name = "Job-Token"
                    value = System.getenv("CI_JOB_TOKEN") // deploy packages should only be created by a CI job
                }
                authentication {
                    create("header", HttpHeaderAuthentication::class)
                }
            }
        }
    }
}