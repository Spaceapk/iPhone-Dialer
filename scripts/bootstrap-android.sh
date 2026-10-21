#!/usr/bin/env bash
set -euo pipefail
: "${ANDROID_SDK_ROOT:=$PWD/.android-sdk}"
export ANDROID_SDK_ROOT
mkdir -p "$ANDROID_SDK_ROOT/cmdline-tools"
if [[ ! -x "$ANDROID_SDK_ROOT/cmdline-tools/latest/bin/sdkmanager" ]]; then
  curl -fL --retry 3 https://dl.google.com/android/repository/commandlinetools-linux-13114758_latest.zip -o /tmp/android-tools.zip
  unzip -q /tmp/android-tools.zip -d "$ANDROID_SDK_ROOT/cmdline-tools"
  mv "$ANDROID_SDK_ROOT/cmdline-tools/cmdline-tools" "$ANDROID_SDK_ROOT/cmdline-tools/latest"
fi
# Run this only after agreeing to the Android SDK license terms.
set +o pipefail
yes | "$ANDROID_SDK_ROOT/cmdline-tools/latest/bin/sdkmanager" --licenses >/dev/null
set -o pipefail
"$ANDROID_SDK_ROOT/cmdline-tools/latest/bin/sdkmanager" 'platforms;android-36' 'build-tools;36.0.0' 'platform-tools'
