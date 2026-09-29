#!/usr/bin/env bash
# Builds the minified devRelease APK, signs it with the debug key when Gradle left it unsigned, and checks that it starts
# on the connected device without a crash. Catches R8 keep-rule regressions that debug builds hide.
set -euo pipefail

root="$(cd "$(dirname "$0")/.." && pwd)"
sdk="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
adb="$sdk/platform-tools/adb"
build_tools="$(ls -d "$sdk"/build-tools/* | sort -V | tail -1)"
package="com.dd.dual.space.dev"
apk_dir="$root/app/build/outputs/apk/dev/release"
debug_keystore="$root/env/debug.keystore"
[ -f "$debug_keystore" ] || debug_keystore="$HOME/.android/debug.keystore"

cd "$root"
./gradlew :app:assembleDevRelease "$@"
# Gradle signs devRelease with env/key.properties when it exists; otherwise the APK is unsigned.
if [ -f "$apk_dir/app-dev-release.apk" ]; then
  signed="$apk_dir/app-dev-release.apk"
  "$build_tools/zipalign" -c -P 16 4 "$signed"
else
  unsigned="$apk_dir/app-dev-release-unsigned.apk"
  signed="$(mktemp -d)/app-dev-release-smoke.apk"
  "$build_tools/zipalign" -c -P 16 4 "$unsigned"
  "$build_tools/apksigner" sign --ks "$debug_keystore" --ks-pass pass:android \
    --key-pass pass:android --out "$signed" "$unsigned"
fi
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
