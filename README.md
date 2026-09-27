# Dual Space

Android multi-session gaming workspace built with Kotlin and Jetpack Compose. The Google Play build uses Android managed profiles; games open as separate Android tasks because Android does not allow arbitrary third-party activities to be embedded without game-side support.

## App identity

| Field | Value |
| --- | --- |
| App name | `Dual Space` |
| Application ID | `com.dd.dual.space` |
| Namespace | `com.dd.dual.space` |

Kotlin sources, `R`, and `BuildConfig` all live under `com.dd.dual.space`.

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
ADMOB_APP_ID=
ADMOB_BANNER_AD_UNIT_ID=
```

`.env.local` is ignored by Git. Gradle/CI properties take precedence over `.env.local`, so CI can supply the same keys with `-PKEY=value` or its protected Gradle properties file.

For local ad testing, `.env.example` contains Google's sample AdMob IDs. Replace them with production IDs only in the protected release environment.

## Run from Android Studio

1. Open the repository root, not the `app` directory.
2. Run **File → Sync Project with Gradle Files**.
3. Open **Build Variants** and select `devDebug` for the `app` module.
4. Select the shared **dev** run configuration.
5. Select an API 29+ device and press Run.

The repository also provides **prod**, which runs `:app:bundleProdRelease` directly from Android Studio.

Use the **dev** configuration for local testing; it builds `devDebug`, mirrors the managed-profile behavior of `prod`, and installs as `com.dd.dual.space.dev`. Use **prod** for the Google Play release.

If Android Studio tries to execute `:app:assembleDebug` and cannot find `app-debug.apk`, sync Gradle again and reselect `devDebug`; the actual APK is flavor-qualified.

## Run from terminal

Build and install the Play debug app:

```sh
./gradlew :app:installDevDebug
```

Build both debug flavors:

```sh
./gradlew :app:assembleDevDebug
./gradlew :app:assembleProdDebug
```

Generated APKs:

```text
app/build/outputs/apk/dev/debug/app-dev-debug.apk
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
2. Provide production AdMob configuration through the protected release environment; `prodRelease` fails while any ID is missing or still a Google test ID.
3. Confirm the AdMob app and banner-ad unit belong to the production package.
4. Run unit tests, connected UI tests, `scripts/run_guest_e2e.sh`, `scripts/release_smoke.sh`, and lint.
5. Review [store readiness](docs/store-readiness.md) and the [Google Play launch plan](docs/google-play-launch-plan.md).

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
| `prod` | `com.dd.dual.space` | Google Play-safe managed-profile build |
| `dev` | `com.dd.dual.space.dev` | Local debug build with the same managed-profile behavior as `prod` |

Do not upload the `dev` flavor to Google Play.

## Product and policy boundaries

- No auto-click, gameplay macros, memory editing, fake GPS, anti-cheat bypass, or silent installation.
- Both app flavors run games through the virtualization engine in `packages/the_universe`.
- The app is free: games launch without a time limit, and a small banner appears outside the launch path. There is no sign-in or purchase.
- Every saved account has a distinct local color, last-opened timestamp, launch confirmation, and launcher shortcut.
- Launch readiness is checked before Android opens the managed-profile game; diagnostics never include credentials or game data.
- Privacy Lock uses the device credential or strong biometrics and is opt-in.
- A future container must reject apps declaring `REQUIRE_SECURE_ENV`.
