import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// Local secrets live in env/ (git-ignored): env.<flavor>.properties, key.properties, release.jks, debug.keystore.
val envDirectory: File = rootProject.file("env")

fun envProperties(fileName: String): Properties = Properties().apply {
    envDirectory.resolve(fileName).takeIf { file -> file.isFile }?.inputStream()?.use { stream -> load(stream) }
}

// Each flavor reads its own env file: env/env.dev.properties or env/env.prod.properties.
fun flavorEnvironment(flavor: String): Properties = envProperties("env.$flavor.properties")

val versionProperties: Properties = Properties().apply {
    rootProject.file("env/version.properties").inputStream().use { stream -> load(stream) }
}

fun versionValue(name: String): String =
    versionProperties.getProperty(name)?.trim()?.takeIf { value -> value.isNotEmpty() }
        ?: throw GradleException("$name is missing in env/version.properties")

val appVersionCode: Int = versionValue("versionCode").toIntOrNull()
    ?: throw GradleException("versionCode in env/version.properties must be a whole number")
val appVersionName: String = versionValue("versionName")

// Blank values count as unset so an env file with empty keys still builds with the fallback.
fun configuredValue(environment: Properties, name: String, fallback: String): String =
    providers.gradleProperty(name).orNull?.takeIf { value -> value.isNotBlank() }
        ?: environment.getProperty(name)?.trim()?.takeIf { value -> value.isNotEmpty() }
        ?: fallback

// Google's published sample publisher; its IDs never serve revenue.
val admobTestPublisherPrefix = "ca-app-pub-3940256099942544"

class AdmobIds(val appId: String, val bannerAdUnitId: String)

// Unset IDs fall back to Google's test IDs; prod release validation rejects them.
fun admobIds(flavor: String): AdmobIds {
    val environment = flavorEnvironment(flavor)
    return AdmobIds(
        appId = configuredValue(environment, "ADMOB_APP_ID", "$admobTestPublisherPrefix~3347511713"),
        bannerAdUnitId = configuredValue(environment, "ADMOB_BANNER_AD_UNIT_ID", "$admobTestPublisherPrefix/6300978111"),
    )
}

val prodAdmobIds = admobIds("prod")
val devAdmobIds = admobIds("dev")

val supportedAppEnvironments = setOf("dev", "prod")

// ENV in the flavor's env file decides whether the app shows the dev version tag; unset means the flavor name.
fun appEnvironment(flavor: String): String {
    val environment = flavorEnvironment(flavor).getProperty("ENV")?.trim()?.lowercase()?.takeIf { value -> value.isNotEmpty() }
        ?: return flavor
    if (environment !in supportedAppEnvironments) {
        throw GradleException("ENV in env/env.$flavor.properties must be dev or prod")
    }
    return environment
}

val prodAppEnvironment = appEnvironment("prod")
val devAppEnvironment = appEnvironment("dev")

// Reports only key names so secret values never reach build logs.
val releaseConfigurationProblems: List<String> = buildList {
    if (prodAdmobIds.bannerAdUnitId.startsWith(admobTestPublisherPrefix)) add("ADMOB_BANNER_AD_UNIT_ID in env/env.prod.properties is missing or a Google test ID")
    if (prodAdmobIds.appId.startsWith(admobTestPublisherPrefix)) add("ADMOB_APP_ID in env/env.prod.properties is missing or a Google test ID")
    if (prodAppEnvironment != "prod") add("ENV in env/env.prod.properties must be prod for a Play release")
}

val validateProdReleaseConfiguration by tasks.registering {
    description = "Fails a Play release build that lacks production ad configuration."
    val problems = releaseConfigurationProblems
    doLast {
        if (problems.isNotEmpty()) {
            throw GradleException("Release configuration is incomplete:\n- " + problems.joinToString("\n- "))
        }
    }
}

tasks.configureEach {
    if (name == "preProdReleaseBuild") dependsOn(validateProdReleaseConfiguration)
}

// Release signing reads env/key.properties (storeFile relative to env/); without it release builds stay unsigned.
val releaseKeyProperties: Properties = envProperties("key.properties")

fun releaseKeyValue(name: String): String =
    releaseKeyProperties.getProperty(name)?.trim()?.takeIf { value -> value.isNotEmpty() }
        ?: throw GradleException("$name is missing in env/key.properties")

val hasReleaseKey: Boolean = !releaseKeyProperties.isEmpty

// A shared env/debug.keystore keeps the debug signature stable across machines; without it AGP uses ~/.android.
val sharedDebugKeystore: File = envDirectory.resolve("debug.keystore")

fun quotedBuildConfig(value: String): String = "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""

android {
    namespace = "com.dd.dual.space"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.dd.dual.space"
        minSdk = 29
        targetSdk = 37
        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        getByName("debug") {
            if (sharedDebugKeystore.isFile) {
                storeFile = sharedDebugKeystore
                storePassword = "android"
                keyAlias = "androiddebugkey"
                keyPassword = "android"
            }
        }
        if (hasReleaseKey) {
            create("release") {
                storeFile = envDirectory.resolve(releaseKeyValue("storeFile"))
                storePassword = releaseKeyValue("storePassword")
                keyAlias = releaseKeyValue("keyAlias")
                keyPassword = releaseKeyValue("keyPassword")
            }
        }
    }

    flavorDimensions += "distribution"
    productFlavors {
        create("prod") {
            dimension = "distribution"
            buildConfigField("String", "ADMOB_BANNER_AD_UNIT_ID", quotedBuildConfig(prodAdmobIds.bannerAdUnitId))
            buildConfigField("boolean", "IS_DEV_ENVIRONMENT", (prodAppEnvironment == "dev").toString())
            manifestPlaceholders["admobAppId"] = prodAdmobIds.appId
        }
        create("dev") {
            dimension = "distribution"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            buildConfigField("String", "ADMOB_BANNER_AD_UNIT_ID", quotedBuildConfig(devAdmobIds.bannerAdUnitId))
            buildConfigField("boolean", "IS_DEV_ENVIRONMENT", (devAppEnvironment == "dev").toString())
            manifestPlaceholders["admobAppId"] = devAdmobIds.appId
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    bundle {
        language {
            enableSplit = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        release {
            if (hasReleaseKey) signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    lint {
        disable += setOf("AndroidGradlePluginVersion", "GradleDependency", "NewerVersionAvailable", "ObsoleteSdkInt")
    }
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

dependencies {
    implementation(project(":the-universe-core"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.google.play.services.ads)
    implementation(libs.play.app.update)
    implementation(libs.google.ump)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.biometric)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.espresso.core)
}
