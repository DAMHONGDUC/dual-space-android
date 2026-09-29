#!/usr/bin/env bash
# Clears every build output, rebuilds from scratch, and produces the Play release bundle (AAB).
#
# Usage:
#   scripts/build_release_aab.sh            # prod bundle for Google Play, ads from env/env.prod.properties
#   FLAVOR=dev scripts/build_release_aab.sh # dev bundle, ads from env/env.dev.properties; pipeline check only
#
# Signing: Gradle signs the bundle with env/key.properties (storeFile, storePassword, keyAlias, keyPassword),
# storeFile relative to env/ (release.jks). Without it the bundle is left unsigned and must be signed before upload.
set -euo pipefail

root="$(cd "$(dirname "$0")/.." && pwd)"
flavor="${FLAVOR:-prod}"
case "$flavor" in
  dev|prod) ;;
  *) echo "Unknown flavor: $flavor (FLAVOR must be dev or prod)" >&2; exit 1 ;;
esac
flavor_title="$(printf '%s' "${flavor:0:1}" | tr '[:lower:]' '[:upper:]')${flavor:1}"
bundle="$root/app/build/outputs/bundle/${flavor}Release/app-${flavor}-release.aab"
output_dir="$root/build/release"
key_properties="$root/env/key.properties"

if [ -z "${JAVA_HOME:-}" ] && [ -d "/Applications/Android Studio.app/Contents/jbr/Contents/Home" ]; then
  export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
fi
jarsigner="${JAVA_HOME:+$JAVA_HOME/bin/}jarsigner"

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
version_name="$(grep -E '^[[:space:]]*versionName[[:space:]]*=' "$root/version.properties" | tail -1 | sed -E 's/^[^=]*=[[:space:]]*//; s/[[:space:]]+$//')"
version_code="$(grep -E '^[[:space:]]*versionCode[[:space:]]*=' "$root/version.properties" | tail -1 | sed -E 's/[^0-9]//g')"
result="$output_dir/dual-space-${flavor}-${version_name}-${version_code}.aab"

echo "==> 4/4 Verifying signature"
cp "$bundle" "$result"
if [ -f "$key_properties" ]; then
  "$jarsigner" -verify "$result" >/dev/null
  echo "Signed with the release key from $key_properties"
else
  echo "WARNING: $key_properties not found; the bundle is UNSIGNED and cannot be uploaded to Play yet." >&2
fi

echo
echo "Bundle:  $result"
echo "Size:    $(du -h "$result" | cut -f1)"
echo "SHA-256: $(shasum -a 256 "$result" | cut -d' ' -f1)"
