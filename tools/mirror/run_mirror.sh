#!/usr/bin/env bash
# Run the Windows mirror (internal QA tooling) on this Linux box — the
# same classes the .exe wraps. Usage:
#   bash tools/mirror/run_mirror.sh                      # interactive window (needs a display)
#   bash tools/mirror/run_mirror.sh --capture <args...>  # headless PNG captures (see Capture.kt)
set -euo pipefail
cd "$(dirname "$0")/../.."
export JAVA_HOME="${MIRROR_JAVA_HOME:-$HOME/build-tools/jdk-full-17}"
export PATH="$JAVA_HOME/bin:$PATH"
CLASSES="${MIRROR_CLASSES:-$PWD/out/mirror-classes}"
STDLIB="$HOME/build-tools/kotlin/kotlinc/lib/kotlin-stdlib.jar"
if [ ! -d "$CLASSES" ]; then
    echo "mirror not built yet — run: bash tools/mirror/build_mirror.sh" >&2
    exit 1
fi
exec java -cp "$CLASSES/mirror-classes:$CLASSES/shim-classes:$STDLIB" com.hunt3r.southparktown.MirrorMain "$@"
