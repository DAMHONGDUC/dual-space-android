#!/bin/sh
set -eu
: "${JAVA_HOME:?Set JAVA_HOME to a JDK with JNI headers}"
repo_dir=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
native_test_dir=$(mktemp -d)
trap 'rm -f "$native_test_dir/io_test"; rmdir "$native_test_dir"' EXIT HUP INT TERM
case "$(uname -s)" in
    Darwin) jni_platform=darwin ;;
    Linux) jni_platform=linux ;;
    *) echo "Unsupported host platform" >&2; exit 1 ;;
esac
core_dir="$repo_dir/packages/the_universe/core/src"
"${CXX:-clang++}" -std=c++17 -Wall -Wextra -Werror -pthread \
    -fsanitize=address,undefined -fno-omit-frame-pointer \
    -I"$JAVA_HOME/include" -I"$JAVA_HOME/include/$jni_platform" \
    -I"$core_dir/test/cpp/stubs" -I"$core_dir/main/cpp" \
    "$core_dir/main/cpp/IO.cpp" "$core_dir/test/cpp/io_test.cpp" \
    -o "$native_test_dir/io_test"
"$native_test_dir/io_test"
