#!/usr/bin/env bash
# Builds the minified devRelease APK, signs it with the local debug key, and checks that it starts
# on the connected device without a crash. Catches R8 keep-rule regressions that debug builds hide.
set -euo pipefail

root="$(cd "$(dirname "$0")/.." && pwd)"
sdk="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
adb="$sdk/platform-tools/adb"
build_tools="$(ls -d "$sdk"/build-tools/* | sort -V | tail -1)"
package="com.dd.dual.space.dev"
unsigned="$root/app/build/outputs/apk/dev/release/app-dev-release-unsigned.apk"
signed="$(mktemp -d)/app-dev-release-smoke.apk"

cd "$root"
./gradlew :app:assembleDevRelease "$@"
"$build_tools/zipalign" -c -P 16 4 "$unsigned"
"$build_tools/apksigner" sign --ks "$HOME/.android/debug.keystore" --ks-pass pass:android \
  --key-pass pass:android --out "$signed" "$unsigned"
"$adb" install -r "$signed"
"$adb" logcat -b all -c
"$adb" shell am start -W -n "$package/com.dd.dual.space.MainActivity"
sleep 8

if [ -z "$("$adb" shell pidof "$package")" ]; then
  echo "release smoke: process is not running" >&2
  exit 1
fi
if "$adb" logcat -d -b crash | grep -q "Process: $package"; then
  "$adb" logcat -d -b crash >&2
  echo "release smoke: crash detected" >&2
  exit 1
fi
echo "release smoke: $package started without crashing"
