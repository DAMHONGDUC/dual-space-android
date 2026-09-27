#!/usr/bin/env bash
# Clears every build output, rebuilds from scratch, and produces the Play release bundle (AAB).
#
# Usage:
#   scripts/build_release_aab.sh            # prod bundle for Google Play
#   FLAVOR=dev scripts/build_release_aab.sh # dev bundle, for checking the pipeline only
#
# Signing (optional; without it the bundle is left unsigned and must be signed before upload):
#   UPLOAD_KEYSTORE=/path/upload.jks UPLOAD_KEY_ALIAS=upload \
#   UPLOAD_STORE_PASSWORD=... UPLOAD_KEY_PASSWORD=... scripts/build_release_aab.sh
# Passwords are read from the environment and never passed on the command line.
set -euo pipefail

root="$(cd "$(dirname "$0")/.." && pwd)"
flavor="${FLAVOR:-prod}"
flavor_title="$(printf '%s' "${flavor:0:1}" | tr '[:lower:]' '[:upper:]')${flavor:1}"
bundle="$root/app/build/outputs/bundle/${flavor}Release/app-${flavor}-release.aab"
output_dir="$root/build/release"

if [ -z "${JAVA_HOME:-}" ] && [ -d "/Applications/Android Studio.app/Contents/jbr/Contents/Home" ]; then
  export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
fi

cd "$root"

echo "==> 1/4 Clearing build outputs"
./gradlew --stop >/dev/null 2>&1 || true
./gradlew clean
rm -rf "$root/build" "$root/app/build" "$root/packages/the_universe"/*/build "$root/tools"/*/build
find "$root/packages/the_universe/core/src/main" -maxdepth 1 -name ".cxx" -prune -exec rm -rf {} +
rm -rf "$root/packages/the_universe/core/.cxx"

echo "==> 2/4 Running unit tests"
./gradlew --no-build-cache ":app:test${flavor_title}DebugUnitTest" ":the-universe-core:testDebugUnitTest"

echo "==> 3/4 Building ${flavor}Release bundle"
# prodRelease stops here with the missing key names if AdMob production IDs are not configured.
./gradlew --no-build-cache ":app:bundle${flavor_title}Release"

if [ ! -f "$bundle" ]; then
  echo "Bundle not found: $bundle" >&2
  exit 1
fi

mkdir -p "$output_dir"
version_name="$(grep -E '^\s*versionName\s*=' app/build.gradle.kts | head -1 | sed -E 's/.*"([^"]+)".*/\1/')"
version_code="$(grep -E '^\s*versionCode\s*=' app/build.gradle.kts | head -1 | sed -E 's/[^0-9]//g')"
result="$output_dir/dual-space-${flavor}-${version_name}-${version_code}.aab"

echo "==> 4/4 Signing and verifying"
if [ -n "${UPLOAD_KEYSTORE:-}" ]; then
  : "${UPLOAD_KEY_ALIAS:?UPLOAD_KEY_ALIAS is required when UPLOAD_KEYSTORE is set}"
  : "${UPLOAD_STORE_PASSWORD:?UPLOAD_STORE_PASSWORD is required when UPLOAD_KEYSTORE is set}"
  : "${UPLOAD_KEY_PASSWORD:?UPLOAD_KEY_PASSWORD is required when UPLOAD_KEYSTORE is set}"
  jarsigner -keystore "$UPLOAD_KEYSTORE" \
    -storepass:env UPLOAD_STORE_PASSWORD -keypass:env UPLOAD_KEY_PASSWORD \
    -sigalg SHA256withRSA -digestalg SHA-256 \
    -signedjar "$result" "$bundle" "$UPLOAD_KEY_ALIAS" >/dev/null
  jarsigner -verify "$result" >/dev/null
  echo "Signed with upload key alias: $UPLOAD_KEY_ALIAS"
else
  cp "$bundle" "$result"
  echo "WARNING: UPLOAD_KEYSTORE not set; the bundle is UNSIGNED and cannot be uploaded to Play yet." >&2
fi

echo
echo "Bundle:  $result"
echo "Size:    $(du -h "$result" | cut -f1)"
echo "SHA-256: $(shasum -a 256 "$result" | cut -d' ' -f1)"
