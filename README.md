# Parallel_app

Android multi-session gaming workspace built with Kotlin and Jetpack Compose. The Google Play build uses Android managed profiles; games open as separate Android tasks because Android does not allow arbitrary third-party activities to be embedded without game-side support.

## Requirements

- Android Studio Quail 4 (`2026.1.4`) or newer with AGP `9.4.0` support.
- Android Studio bundled JDK 21.
- Android SDK 37 and an Android device or emulator running API 29+.
- Use the repository Gradle wrapper (`./gradlew`); do not install a separate Gradle version.

In Android Studio, set **Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK** to `jbr-21` or `Embedded JDK`.

## Local setup

Clone the repository, then create the ignored environment file:

```sh
cp .env.example .env.local
```

Fill these values in `.env.local`:

```properties
REVENUECAT_GOOGLE_API_KEY=
REVENUECAT_ENTITLEMENT_ID=premium
FIREBASE_WEB_CLIENT_ID=
ADMOB_APP_ID=
ADMOB_REWARDED_AD_UNIT_ID=
```

`.env.local` is ignored by Git. Gradle/CI properties take precedence over `.env.local`, so CI can supply the same keys with `-PKEY=value` or its protected Gradle properties file.

For Firebase Authentication:

1. Register Android package `com.duplicateapp.gamespace` in Firebase.
2. Enable the Google sign-in provider.
3. Add debug and release SHA-1/SHA-256 fingerprints.
4. Download `google-services.json` to `app/google-services.json`.
5. Copy the Web OAuth client ID to `FIREBASE_WEB_CLIENT_ID`.

`google-services.json` is ignored and must never be committed. The app still builds without Firebase configuration, but Google sign-in remains unavailable.

For local ad testing, `.env.example` contains Google's sample AdMob IDs. Replace them with production IDs only in the protected release environment.

## Run from Android Studio

1. Open the repository root, not the `app` directory.
2. Run **File → Sync Project with Gradle Files**.
3. Open **Build Variants** and select `qaDebug` for the `app` module.
4. Select the shared **appDebug** run configuration.
5. Select an API 29+ device and press Run.

The repository also provides **appRelease**, which runs `:app:bundleProdRelease` directly from Android Studio.

Use `qaDebug` for local testing; it includes the container runtime and uses application ID `com.duplicateapp.gamespace.qa`. Use `prodDebug` for Google Play-safe verification.

If Android Studio tries to execute `:app:assembleDebug` and cannot find `app-debug.apk`, sync Gradle again and reselect `qaDebug`; the actual APK is flavor-qualified.

## Run from terminal

Build and install the Play debug app:

```sh
./gradlew :app:installQaDebug
```

Build both debug flavors:

```sh
./gradlew :app:assembleQaDebug
./gradlew :app:assembleProdDebug
```

Generated APKs:

```text
app/build/outputs/apk/qa/debug/app-qa-debug.apk
app/build/outputs/apk/prod/debug/app-prod-debug.apk
```

## Tests and static checks

Run the JVM logic tests:

```sh
./gradlew :app:testProdDebugUnitTest
```

Run Compose UI tests on a connected device or emulator:

```sh
./gradlew :app:connectedProdDebugAndroidTest
```

Run Android lint:

```sh
./gradlew :app:lintProdDebug
```

Reports are written under `app/build/reports/`.

## Prepare a release

Before every release:

1. Update `versionCode` and `versionName` in `app/build.gradle.kts`.
2. Provide production Firebase, RevenueCat, and AdMob configuration through the protected release environment.
3. Configure RevenueCat's Google Play product and attach it to the `premium` entitlement.
4. Confirm the AdMob app and rewarded-ad unit belong to the production package.
5. Run unit tests, connected UI tests, and lint.
6. Review [store readiness](docs/store-readiness.md) and the [Google Play launch plan](docs/google-play-launch-plan.md).

Build the Play release bundle:

```sh
./gradlew :app:bundleProdRelease
```

Generated bundle:

```text
app/build/outputs/bundle/prodRelease/app-prod-release.aab
```

The repository does not contain an upload keystore or signing passwords. Configure release signing with protected CI/Gradle credentials, or use Android Studio **Build → Generate Signed App Bundle or APK → Android App Bundle** and select the Play upload key. Never commit the keystore, aliases, or passwords.

Before uploading, verify the signed bundle and inspect the final manifest:

```sh
jarsigner -verify app/build/outputs/bundle/prodRelease/app-prod-release.aab
./gradlew :app:processProdReleaseMainManifest
```

Upload the AAB to an Internal testing track first, finish App content/Data safety declarations, run the Play pre-launch report, then promote with a staged rollout.

## Distribution flavors

| Flavor | Application ID | Purpose |
| --- | --- | --- |
| `prod` | `com.duplicateapp.gamespace` | Google Play-safe managed-profile build |
| `qa` | `com.duplicateapp.gamespace.qa` | Local testing build with The Universe runtime |

Do not upload the `qa` flavor to Google Play.

## Product and policy boundaries

- No auto-click, gameplay macros, memory editing, fake GPS, anti-cheat bypass, or silent installation.
- The `prod` flavor must remain independent from `packages/the_universe`.
- Premium removes ads and the monthly play-time limit.
- Free users receive 180 session-hours per monthly cycle and can earn additional time through rewarded ads.
- A future container must reject apps declaring `REQUIRE_SECURE_ENV`.
