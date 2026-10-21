#!/usr/bin/env bash
set -euo pipefail
for name in ANDROID_KEYSTORE_PATH ANDROID_KEYSTORE_PASSWORD ANDROID_KEY_ALIAS ANDROID_KEY_PASSWORD; do
  if [[ -z "${!name:-}" ]]; then echo "Missing protected signing variable: $name" >&2; exit 1; fi
done
[[ -f "$ANDROID_KEYSTORE_PATH" ]] || { echo 'Signing keystore not found' >&2; exit 1; }
# Never print passwords or enable shell tracing here.
