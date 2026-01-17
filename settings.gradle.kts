pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

val githubUser = providers.gradleProperty("gpr.user").orNull
    ?: System.getenv("GITHUB_ACTOR")
val githubToken = providers.gradleProperty("gpr.key").orNull
    ?: System.getenv("GITHUB_TOKEN")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/spotify/android-sdk")
            credentials {
                username = githubUser
                password = githubToken
            }
            content {
                includeGroup("com.spotify.android")
            }
        }
    }
}

rootProject.name = "RAMBOOST"
include(":app")
