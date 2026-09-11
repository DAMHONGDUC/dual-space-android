# Parallel_app

Android-first multi-session gaming workspace built with Kotlin and Jetpack Compose.

## Current milestone

Version 0.5 companion MVP: adaptive phone/tablet workspace, persistent personal/work-profile sessions, profile-aware app launching, launch-state feedback, system/light/dark themes, 11 locales, and a local 180-hour quota estimate. Android does not allow arbitrary third-party game activities to be embedded inside Parallel_app tabs without game-side opt-in, so games open as separate Android tasks.

## Build

```sh
./gradlew :app:assemblePlayDebug
./gradlew :app:assembleDirectDebug
```

The `play` flavor uses Android managed profiles and excludes the container runtime. The `direct` flavor retains The Universe for separately reviewed distribution and uses the `.direct` application ID suffix.

## Test

```sh
./gradlew :app:testPlayDebugUnitTest
./gradlew :app:connectedPlayDebugAndroidTest
```

Read [docs/store-readiness.md](docs/store-readiness.md) before producing a store artifact.

## Product boundaries

- No auto-click, gameplay macros, memory editing, fake GPS, or anti-cheat bypass.
- A future container must reject apps declaring `REQUIRE_SECURE_ENV`.
- Free usage is 180 session-hours per monthly cycle.
