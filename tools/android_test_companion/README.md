# Android Test Companion

One-button, user-started UI regression test for Parallel Game Space.

## Build and install

```sh
./gradlew :test-companion:assembleDebug
adb install -r tools/android_test_companion/build/outputs/apk/debug/android_test_companion-debug.apk
```

On first use, tap **ENABLE ACCESSIBILITY**, enable **Android Test Companion**, return,
and tap **START AUTOMATED TEST**. The safe scenario verifies onboarding, the workspace,
Settings, and About without adding or deleting user data.

This QA utility does not sign in to Google, join a Play test, spoof IP/device identity,
or replace Google Play's real tester requirement. Keep it sideloaded; Accessibility API
use is restricted for apps distributed through Google Play.
