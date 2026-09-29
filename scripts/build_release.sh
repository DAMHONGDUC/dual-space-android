#!/usr/bin/env bash
# Clears every build output, rebuilds from scratch, and replaces the release APK or AAB in Release/ with the new one.
#
# Usage (normally through scripts/build_release_apk.sh or scripts/build_release_aab.sh):
#   scripts/build_release.sh apk|aab            # prod build, ads from env/env.prod.properties
#   FLAVOR=dev scripts/build_release.sh apk|aab # dev build, ads from env/env.dev.properties; pipeline check only
#
# A repository path with spaces is built from a temporary copy, since ndk-build cannot handle spaces.
#
# Signing: Gradle signs the build with env/key.properties (storeFile, storePassword, keyAlias, keyPassword),
# storeFile relative to env/ (release.jks). Without it the output is left unsigned and must be signed before upload.
set -euo pipefail

root="$(cd "$(dirname "$0")/.." && pwd)"
format="${1:-}"
case "$format" in
  apk|aab) ;;
  *) echo "Usage: $0 apk|aab" >&2; exit 1 ;;
esac
flavor="${FLAVOR:-prod}"
case "$flavor" in
  dev|prod) ;;
  *) echo "Unknown flavor: $flavor (FLAVOR must be dev or prod)" >&2; exit 1 ;;
esac

# ndk-build cannot handle spaces in paths, so a checkout like ".../Jetpack Compose/..." builds from a
# space-free temporary mirror; the result still lands in this checkout's Release/ and the mirror is deleted.
if [[ "$root" == *" "* ]]; then
  mirror="$(mktemp -d)"
  if [[ "$mirror" == *" "* ]]; then
    echo "The temporary directory $mirror has a space too; set TMPDIR to a path without spaces." >&2
    exit 1
  fi
  trap 'rm -rf "$mirror"' EXIT
  echo "==> Repository path has a space; building from mirror $mirror"
  rsync -a --exclude .git --exclude build --exclude .gradle --exclude .cxx --exclude .kotlin --exclude Release "$root/" "$mirror/"
  status=0
  RELEASE_OUTPUT_DIR="$root/Release" "$mirror/scripts/build_release.sh" "$format" || status=$?
  "$mirror/gradlew" -p "$mirror" --stop >/dev/null 2>&1 || true
  exit "$status"
fi

flavor_title="$(printf '%s' "${flavor:0:1}" | tr '[:lower:]' '[:upper:]')${flavor:1}"
output_dir="${RELEASE_OUTPUT_DIR:-$root/Release}"
key_properties="$root/env/key.properties"

if [ -z "${JAVA_HOME:-}" ] && [ -d "/Applications/Android Studio.app/Contents/jbr/Contents/Home" ]; then
  export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
fi
jarsigner="${JAVA_HOME:+$JAVA_HOME/bin/}jarsigner"
sdk="${ANDROID_HOME:-$HOME/Library/Android/sdk}"

cd "$root"

echo "==> 1/4 Clearing build outputs"
./gradlew --stop >/dev/null 2>&1 || true
# Native caches go first: stale .cxx metadata (e.g. after moving the repo) makes gradle clean fail.
rm -rf "$root/build" "$root/app/build" "$root/packages/the_universe"/*/build "$root/tools"/*/build
find "$root/packages/the_universe/core/src/main" -maxdepth 1 -name ".cxx" -prune -exec rm -rf {} +
rm -rf "$root/packages/the_universe/core/.cxx"
./gradlew clean

echo "==> 2/4 Running unit tests"
./gradlew --no-build-cache ":app:test${flavor_title}DebugUnitTest" ":the-universe-core:testDebugUnitTest"

echo "==> 3/4 Building ${flavor}Release $format"
# prodRelease stops here with the missing key names if AdMob production IDs are not configured.
if [ "$format" = "apk" ]; then
  ./gradlew --no-build-cache ":app:assemble${flavor_title}Release"
  apk_dir="$root/app/build/outputs/apk/$flavor/release"
  artifact="$apk_dir/app-${flavor}-release.apk"
  [ -f "$artifact" ] || artifact="$apk_dir/app-${flavor}-release-unsigned.apk"
else
  ./gradlew --no-build-cache ":app:bundle${flavor_title}Release"
  artifact="$root/app/build/outputs/bundle/${flavor}Release/app-${flavor}-release.aab"
fi

if [ ! -f "$artifact" ]; then
  echo "Build output not found: $artifact" >&2
  exit 1
fi

# Release/ keeps only the latest build of each format; the other format's file is left alone.
mkdir -p "$output_dir"
find "$output_dir" -maxdepth 1 -type f -name "*.$format" -print -delete | sed 's/^/Removed old build: /'
version_name="$(grep -E '^[[:space:]]*versionName[[:space:]]*=' "$root/env/version.properties" | tail -1 | sed -E 's/^[^=]*=[[:space:]]*//; s/[[:space:]]+$//')"
version_code="$(grep -E '^[[:space:]]*versionCode[[:space:]]*=' "$root/env/version.properties" | tail -1 | sed -E 's/[^0-9]//g')"
result="$output_dir/dual-space-${flavor}-${version_name}-${version_code}.$format"
cp "$artifact" "$result"

echo "==> 4/4 Verifying signature"
if [ ! -f "$key_properties" ]; then
  echo "WARNING: $key_properties not found; the $format is UNSIGNED and cannot be uploaded or installed yet." >&2
elif [ "$format" = "apk" ]; then
  build_tools="$(ls -d "$sdk"/build-tools/* | sort -V | tail -1)"
  "$build_tools/apksigner" verify "$result"
  echo "Signed with the release key from $key_properties"
else
  # jarsigner exits 0 for an unsigned bundle too, so require its success message.
  "$jarsigner" -verify "$result" | grep -q "jar verified"
  echo "Signed with the release key from $key_properties"
fi

echo
echo "Output:  $result"
echo "Size:    $(du -h "$result" | cut -f1)"
echo "SHA-256: $(shasum -a 256 "$result" | cut -d' ' -f1)"
