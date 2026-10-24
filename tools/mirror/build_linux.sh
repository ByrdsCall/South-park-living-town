#!/usr/bin/env bash
# The LINUX MIRROR — v0.16.0c: the identical game as a
# first-class, launchable Linux build (internal QA tooling,
# NOT a shipped product; Lucky receives the APK only).
#
# Lucky (Oct 18, 2026): "Make it work on android and Linux
# so you can test it." The Windows .exe (v0.16.0b) proved
# the mirror's payload jars boot and render on this Linux
# box; this script makes the Linux run a maintained
# artifact in its own right instead of a byproduct:
#
#   out/south-park-town-v<VERSION>-linux/
#     app.jar             the game + mirror host (tools/mirror/*.kt
#                         compiled from the SAME src/ tree as the APK)
#     shim.jar            the android.* compatibility layer
#     kotlin-stdlib.jar   the Kotlin runtime
#     assets/             the game's own assets (lorebook, title art)
#     run.sh              launch the game window (Xvfb if headless)
#     capture.sh          headless scene captures (the sweep harness)
#     VERSION.txt         what this is, its version, how it was verified
#
# ONE mirror implementation: the .exe payload and this
# Linux build are packaged from the same compiled mirror
# classes; neither is a fork of the game.
#
# Usage: bash tools/mirror/build_linux.sh [versionName]
#   versionName defaults to the AndroidManifest versionName.
set -euo pipefail
cd "$(dirname "$0")/../.."
ROOT="$PWD"
export JAVA_HOME="${MIRROR_JAVA_HOME:-$HOME/build-tools/jdk-full-17}"
export PATH="$JAVA_HOME/bin:$PATH"
STDLIB="$HOME/build-tools/kotlin/kotlinc/lib/kotlin-stdlib.jar"

VERSION="${1:-}"
if [ -z "$VERSION" ]; then
    VERSION=$(grep -o 'versionName="[^"]*"' AndroidManifest.xml | head -1 | cut -d'"' -f2)
fi
[ -n "$VERSION" ] || { echo "cannot determine version" >&2; exit 1; }
SHORT=$(echo "$VERSION" | tr -d '.')
DEST="$ROOT/out/south-park-town-v${SHORT}-linux"
WORK="${TMPDIR:-/tmp}/spt-linux-build"
CLASSES="$WORK/classes"

echo "--- compile the mirror (same src/ tree as the APK) ---"
bash tools/mirror/build_mirror.sh "$CLASSES"

echo "--- package $DEST ---"
rm -rf "$DEST"
mkdir -p "$DEST"
(cd "$CLASSES/mirror-classes" && jar cf "$DEST/app.jar" .)
(cd "$CLASSES/shim-classes" && jar cf "$DEST/shim.jar" .)
cp "$STDLIB" "$DEST/kotlin-stdlib.jar"
rm -rf "$DEST/assets"
cp -r "$ROOT/assets" "$DEST/assets"

cat > "$DEST/run.sh" <<'EOF'
#!/usr/bin/env bash
# Launch the South Park Town Linux mirror (internal QA).
# Same game as the Android APK, same save format
# (~/.sptown-mirror, or $SPT_MIRROR_DATA). Needs a display;
# on a headless box it starts its own Xvfb.
set -euo pipefail
cd "$(dirname "$0")"
export SPT_PROJECT="$PWD"
JAVA="${MIRROR_JAVA_HOME:-$HOME/build-tools/jdk-full-17}/bin/java"
[ -x "$JAVA" ] || JAVA=java
CP="app.jar:shim.jar:kotlin-stdlib.jar"
if [ -z "${DISPLAY:-}" ] && command -v Xvfb >/dev/null 2>&1; then
    Xvfb :99 -screen 0 900x1400x24 >/dev/null 2>&1 &
    XVFB_PID=$!
    trap 'kill $XVFB_PID 2>/dev/null || true' EXIT
    export DISPLAY=:99
    sleep 1
fi
exec "$JAVA" -cp "$CP" com.hunt3r.southparktown.MirrorMain "$@"
EOF
chmod +x "$DEST/run.sh"

cat > "$DEST/capture.sh" <<'EOF'
#!/usr/bin/env bash
# Headless scene captures through the Linux mirror build —
# the sweep harness (see tools/mirror/Capture.kt for the
# scene spec: LocId, LocId:in, StreetId:sec=..., :bus=...).
set -euo pipefail
cd "$(dirname "$0")"
export SPT_PROJECT="$PWD"
JAVA="${MIRROR_JAVA_HOME:-$HOME/build-tools/jdk-full-17}/bin/java"
[ -x "$JAVA" ] || JAVA=java
exec "$JAVA" -cp "app.jar:shim.jar:kotlin-stdlib.jar" \
    com.hunt3r.southparktown.MirrorMain --capture "$@"
EOF
chmod +x "$DEST/capture.sh"

SIZE=$(du -sb "$DEST" | cut -f1)
cat > "$DEST/VERSION.txt" <<EOF
South Park Town — LINUX MIRROR (internal QA tooling, not a product)
version: $VERSION (packaged from the sptown-app src/ tree)
artifact bytes (this directory): $SIZE
run: ./run.sh            (game window; Xvfb auto-started when headless)
     ./capture.sh <outDir> [opts] <scene...>   (headless PNG captures)
saves: ~/.sptown-mirror (the game's own Prefs format — the same bytes
the Android APK loads); override with SPT_MIRROR_DATA.
parity: the identical src/ tree as the APK, compiled against the
tools/mirror android.* shim over Java2D — same model, Engine,
GameView/SceneArt/KidArt/InteriorArt renderers, same save format.
The Windows .exe mirror is packaged from these same classes.
Named divergences: Java2D rasterizer (not Skia); AWT font metrics;
GameActivity's widget UI is hosted at GameView's callback seams,
not run; audio stubs are silent.
EOF
echo "linux mirror -> $DEST ($SIZE bytes)"
