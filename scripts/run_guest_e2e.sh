#!/usr/bin/env bash
# Installs the guest fixture on the connected device, then runs the engine end-to-end tests.
set -euo pipefail

root="$(cd "$(dirname "$0")/.." && pwd)"
adb="${ANDROID_HOME:-$HOME/Library/Android/sdk}/platform-tools/adb"

cd "$root"
./gradlew :guest-fixture:assembleDebug "$@"
"$adb" install -r tools/guest_fixture/build/outputs/apk/debug/guest-fixture-debug.apk
./gradlew :app:connectedDevDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.package=com.dd.dual.space.features.virtualization "$@"
