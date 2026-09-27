plugins {
    alias(libs.plugins.android.application)
}

// Minimal guest app installed on the host so instrumented tests can drive a real launch inside the engine.
android {
    namespace = "com.dd.dual.space.guestfixture"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.dd.dual.space.guestfixture"
        minSdk = 29
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
