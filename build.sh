#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

TARGET="${1:-all}"
shift || true

build_android() {
  if [[ "${1:-}" == "--release" ]]; then
    shift
    ./gradlew :androidApp:assembleRelease "$@"
    echo "Android APK ready: $ROOT_DIR/androidApp/build/outputs/apk/release/androidApp-release.apk"
  elif [[ "${1:-}" == "--benchmark" ]]; then
    shift
    ./gradlew :androidApp:assembleBenchmark "$@"
    echo "Android profileable benchmark APK ready: $ROOT_DIR/androidApp/build/outputs/apk/benchmark/androidApp-benchmark.apk"
  else
    ./gradlew :androidApp:assembleDebug "$@"
    echo "Android APK ready: $ROOT_DIR/androidApp/build/outputs/apk/debug/androidApp-debug.apk"
  fi
}

build_ios() {
  if [[ "$(uname -s)" == "Darwin" ]]; then
    ./gradlew :gallery:linkDebugFrameworkIosSimulatorArm64 "$@"
    xcodebuild -project iosApp/KiraGallery.xcodeproj -scheme KiraGallery -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' build
  else
    ./gradlew :kira-theme:compileKotlinIosArm64 :kira-theme:compileKotlinIosSimulatorArm64 \
      :kira-components:compileKotlinIosArm64 :kira-components:compileKotlinIosSimulatorArm64 \
      :gallery:compileKotlinIosArm64 :gallery:compileKotlinIosSimulatorArm64 "$@"
    echo "iOS KLIBs compiled (iosArm64 + iosSimulatorArm64). Full .framework/.app build requires macOS + Xcode."
  fi
}

build_desktop() {
  if [[ "${1:-}" == "--installer" ]]; then
    shift
    ./gradlew :gallery:packageDistributionForCurrentOS "$@"
  else
    ./gradlew :gallery:createDistributable "$@"
  fi
  echo "Desktop build ready under: $ROOT_DIR/gallery/build/compose/binaries/main/"
}

case "$TARGET" in
  android) build_android "$@" ;;
  ios) build_ios "$@" ;;
  desktop) build_desktop "$@" ;;
  all)
    build_android "$@"
    build_ios "$@"
    build_desktop "$@"
    ;;
  *)
    echo "Usage: ./build.sh [android|ios|desktop|all] [--release|--installer]" >&2
    exit 1
    ;;
esac
