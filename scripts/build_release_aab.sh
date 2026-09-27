#!/usr/bin/env bash
# Clears every build output, rebuilds from scratch, and produces the Play release bundle (AAB).
#
# Usage:
#   scripts/build_release_aab.sh            # prod bundle for Google Play, ads from env/prod.env.properties
#   FLAVOR=dev scripts/build_release_aab.sh # dev bundle, ads from env/dev.env.properties; pipeline check only
#
# Signing: reads env/keystore.properties (storeFile, storePassword, keyAlias, keyPassword). Without it the
# bundle is left unsigned and must be signed before upload. KEYSTORE_PROPERTIES overrides the path.
# Passwords are passed to jarsigner through the environment, never on the command line.
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
keystore_properties="${KEYSTORE_PROPERTIES:-$root/env/keystore.properties}"

# Reads one key from a .properties file without evaluating it as shell code.
property() {
  grep -E "^[[:space:]]*$1[[:space:]]*=" "$keystore_properties" | tail -1 | sed -E "s/^[[:space:]]*$1[[:space:]]*=[[:space:]]*//; s/[[:space:]]+$//"
}

if [ -f "$keystore_properties" ]; then
  properties_dir="$(cd "$(dirname "$keystore_properties")" && pwd)"
  store_file="$(property storeFile)"
  case "$store_file" in
    /*) UPLOAD_KEYSTORE="$store_file" ;;
    *) UPLOAD_KEYSTORE="$properties_dir/$store_file" ;;
  esac
  UPLOAD_KEY_ALIAS="$(property keyAlias)"
  UPLOAD_STORE_PASSWORD="$(property storePassword)"
  UPLOAD_KEY_PASSWORD="$(property keyPassword)"
  export UPLOAD_STORE_PASSWORD UPLOAD_KEY_PASSWORD
  if [ ! -f "$UPLOAD_KEYSTORE" ]; then
    echo "Keystore not found: $UPLOAD_KEYSTORE (from $keystore_properties)" >&2
    exit 1
  fi
fi

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
version_name="$(grep -E '^[[:space:]]*versionName[[:space:]]*=' "$root/env/version.properties" | tail -1 | sed -E 's/^[^=]*=[[:space:]]*//; s/[[:space:]]+$//')"
version_code="$(grep -E '^[[:space:]]*versionCode[[:space:]]*=' "$root/env/version.properties" | tail -1 | sed -E 's/[^0-9]//g')"
result="$output_dir/dual-space-${flavor}-${version_name}-${version_code}.aab"

echo "==> 4/4 Signing and verifying"
if [ -n "${UPLOAD_KEYSTORE:-}" ]; then
  : "${UPLOAD_KEY_ALIAS:?keyAlias is missing in $keystore_properties}"
  : "${UPLOAD_STORE_PASSWORD:?storePassword is missing in $keystore_properties}"
  : "${UPLOAD_KEY_PASSWORD:?keyPassword is missing in $keystore_properties}"
  "$jarsigner" -keystore "$UPLOAD_KEYSTORE" \
    -storepass:env UPLOAD_STORE_PASSWORD -keypass:env UPLOAD_KEY_PASSWORD \
    -sigalg SHA256withRSA -digestalg SHA-256 \
    -signedjar "$result" "$bundle" "$UPLOAD_KEY_ALIAS" >/dev/null
  "$jarsigner" -verify "$result" >/dev/null
  echo "Signed with upload key alias: $UPLOAD_KEY_ALIAS"
else
  cp "$bundle" "$result"
  echo "WARNING: $keystore_properties not found; the bundle is UNSIGNED and cannot be uploaded to Play yet." >&2
fi

echo
echo "Bundle:  $result"
echo "Size:    $(du -h "$result" | cut -f1)"
echo "SHA-256: $(shasum -a 256 "$result" | cut -d' ' -f1)"
