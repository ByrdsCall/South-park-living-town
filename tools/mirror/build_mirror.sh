#!/usr/bin/env bash
# The Windows mirror (v0.16.0b) — INTERNAL QA TOOLING, not a shipped product.
# Compiles the game's own src/ tree against the desktop android.* shim
# (tools/mirror/shim, plain Java) plus the mirror host, into class dirs.
# This build NEVER touches the APK pipeline: build_apk.sh compiles src/
# against the real android.jar; this script compiles the same src/
# against the shim. kotlinc reads Java sources for symbols but emits no
# bytecode for them, so the shim gets its own javac pass first.
# Usage: bash tools/mirror/build_mirror.sh [outDir]
#   outDir/shim-classes   the android.* compatibility layer
#   outDir/mirror-classes the game + mirror host
set -euo pipefail
cd "$(dirname "$0")/../.."
ROOT="$PWD"
OUT="${1:-$ROOT/out/mirror-classes}"
export JAVA_HOME="${MIRROR_JAVA_HOME:-$HOME/build-tools/jdk-full-17}"
export PATH="$JAVA_HOME/bin:$PATH"
KOTLINC="$HOME/build-tools/kotlin/kotlinc/bin/kotlinc"
mkdir -p "$OUT/shim-classes" "$OUT/mirror-classes"
find "$ROOT/tools/mirror/shim" -name '*.java' > "$ROOT/tools/mirror/.shim-srcs.txt"
javac -encoding UTF-8 -nowarn -d "$OUT/shim-classes" @"$ROOT/tools/mirror/.shim-srcs.txt"
find "$ROOT/src" -name '*.kt' > "$ROOT/tools/mirror/.srcs.txt"
ls "$ROOT"/tools/mirror/*.kt >> "$ROOT/tools/mirror/.srcs.txt"
# shellcheck disable=SC2046
"$KOTLINC" -J-Xmx2g -nowarn -cp "$OUT/shim-classes" @"$ROOT/tools/mirror/.srcs.txt" -d "$OUT/mirror-classes"
echo "mirror classes -> $OUT"
