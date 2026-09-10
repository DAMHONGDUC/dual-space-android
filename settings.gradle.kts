pluginManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

rootProject.name = "GameSpace"
include(":app")
include(":the-universe-core")
include(":the-universe-reflection")
include(":the-universe-compiler")

project(":the-universe-core").projectDir = file("the-universe-core")
project(":the-universe-reflection").projectDir = file("the-universe-reflection")
project(":the-universe-compiler").projectDir = file("the-universe-compiler")
