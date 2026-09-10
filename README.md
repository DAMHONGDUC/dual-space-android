# Parallel_app

Android-first multi-session gaming workspace built with Kotlin and Jetpack Compose.

## Current milestone

Version 0.3 companion MVP: adaptive phone/tablet workspace, persistent personal/work-profile sessions, profile-aware app launching, launch-state feedback, and a local 180-hour quota estimate. Android does not allow arbitrary third-party game activities to be embedded inside Parallel_app tabs without game-side opt-in, so games open as separate Android tasks.

## Build

```sh
./gradlew :app:assembleDebug
```

## Product boundaries

- No auto-click, gameplay macros, memory editing, fake GPS, or anti-cheat bypass.
- A future container must reject apps declaring `REQUIRE_SECURE_ENV`.
- Free usage is 180 session-hours per monthly cycle.
