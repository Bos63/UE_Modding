#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

pick_stable_java() {
  for CANDIDATE in \
    "/root/.local/share/mise/installs/java/21.0.2" \
    "/root/.local/share/mise/installs/java/21" \
    "/root/.local/share/mise/installs/java/17.0.2" \
    "/root/.local/share/mise/installs/java/17"
  do
    if [[ -d "$CANDIDATE" ]]; then
      export JAVA_HOME="$CANDIDATE"
      export PATH="$JAVA_HOME/bin:$PATH"
      return 0
    fi
  done
  return 1
}

# If JAVA_HOME is missing OR points to too-new JDK (e.g. 25), force a stable one.
if [[ -z "${JAVA_HOME:-}" ]]; then
  pick_stable_java || true
else
  JAVA_VER="$("$JAVA_HOME/bin/java" -version 2>&1 | head -n1 || true)"
  if echo "$JAVA_VER" | rg -q '"(2[5-9]|[3-9][0-9])\.'; then
    pick_stable_java || true
  fi
fi

echo "Using JAVA_HOME=${JAVA_HOME:-<system-default>}"

echo "Building debug APK..."
./gradlew --no-daemon clean :app:assembleDebug

echo "APK output:"
find "$PROJECT_DIR/app/build/outputs/apk" -type f -name "*.apk" -print
