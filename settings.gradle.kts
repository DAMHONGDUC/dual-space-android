pluginManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

rootProject.name = "Parallel_app"
include(":app")
include(":test-companion")
include(":the-universe-core")
include(":the-universe-reflection")
include(":the-universe-compiler")

project(":the-universe-core").projectDir = file("packages/the_universe/core")
project(":the-universe-reflection").projectDir = file("packages/the_universe/reflection")
project(":the-universe-compiler").projectDir = file("packages/the_universe/compiler")
project(":test-companion").projectDir = file("tools/android_test_companion")
