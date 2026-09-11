plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val revenueCatApiKey: String = providers.gradleProperty("REVENUECAT_GOOGLE_API_KEY").orElse("").get()
val revenueCatEntitlementId: String = providers.gradleProperty("REVENUECAT_ENTITLEMENT_ID").orElse("premium").get()
val firebaseWebClientId: String = providers.gradleProperty("FIREBASE_WEB_CLIENT_ID").orElse("").get()
val admobRewardedAdUnitId: String = providers.gradleProperty("ADMOB_REWARDED_AD_UNIT_ID").orElse("").get()
val admobAppId: String = providers.gradleProperty("ADMOB_APP_ID").orElse("ca-app-pub-3940256099942544~3347511713").get()

fun quotedBuildConfig(value: String): String = "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""

if (file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
}

android {
    namespace = "com.duplicateapp.gamespace"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.duplicateapp.gamespace"
        minSdk = 29
        targetSdk = 37
        versionCode = 5
        versionName = "0.5.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "REVENUECAT_API_KEY", quotedBuildConfig(revenueCatApiKey))
        buildConfigField("String", "REVENUECAT_ENTITLEMENT_ID", quotedBuildConfig(revenueCatEntitlementId))
        buildConfigField("String", "FIREBASE_WEB_CLIENT_ID", quotedBuildConfig(firebaseWebClientId))
        buildConfigField("String", "ADMOB_REWARDED_AD_UNIT_ID", quotedBuildConfig(admobRewardedAdUnitId))
        manifestPlaceholders["admobAppId"] = admobAppId
    }

    flavorDimensions += "distribution"
    productFlavors {
        create("play") {
            dimension = "distribution"
        }
        create("direct") {
            dimension = "distribution"
            applicationIdSuffix = ".direct"
            versionNameSuffix = "-direct"
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        release {
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
    "directImplementation"(project(":the-universe-core"))
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
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.google.id)
    implementation(libs.google.play.services.ads)
    implementation(libs.revenuecat.purchases)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
}
