#!/usr/bin/env bash
# Scene captures + the golden-image regression gate (internal QA
# tooling; see Golden.kt). Usage:
#   bash tools/mirror/capture.sh [capture args...]
#       Plain QA captures — exactly run_mirror.sh --capture.
#   bash tools/mirror/capture.sh --golden [check|update] [--work DIR]
#       Render all 155 golden targets (27 exteriors + 27 interiors
#       + 101 sheets) under the pinned staging and compare against
#       tools/golden/hashes.tsv (check, the default; exit 1 on any
#       drift) or rewrite that baseline (update; per-target drift
#       allowances are preserved). Golden runs use a FRESH mirror
#       data dir inside the work dir, so prefs/appearance/settings
#       are the game's own defaults. Work dir defaults to
#       ${TMPDIR:-/tmp}/spt-golden and keeps its renders for
#       eyeballing.
set -euo pipefail
cd "$(dirname "$0")/../.."
ROOT="$PWD"

if [ "${1:-}" = "--golden" ]; then
    shift
    mode="check"
    work="${TMPDIR:-/tmp}/spt-golden"
    while [ $# -gt 0 ]; do
        case "$1" in
            --work) work="$2"; shift 2 ;;
            update|check) mode="$1"; shift ;;
            *) shift ;;
        esac
    done
    CLASSES="${MIRROR_CLASSES:-$ROOT/out/mirror-classes}"
    STDLIB="$HOME/build-tools/kotlin/kotlinc/lib/kotlin-stdlib.jar"
    export JAVA_HOME="${MIRROR_JAVA_HOME:-$HOME/build-tools/jdk-full-17}"
    export PATH="$JAVA_HOME/bin:$PATH"
    stamp="$CLASSES/.golden-built-stamp"
    newest=""
    if [ -f "$stamp" ]; then
        newest=$(find "$ROOT/src" "$ROOT/tools/mirror" -name '*.kt' \
            -newer "$stamp" 2>/dev/null | head -1)
    fi
    if [ ! -f "$stamp" ] || [ -n "$newest" ]; then
        bash tools/mirror/build_mirror.sh "$CLASSES"
        touch "$stamp"
    fi
    rm -rf "$work/data"
    mkdir -p "$work/data"
    SPT_MIRROR_DATA="$work/data" SPT_PROJECT="$ROOT" \
        java -cp "$CLASSES/mirror-classes:$CLASSES/shim-classes:$STDLIB" \
        com.hunt3r.southparktown.MirrorMain --golden "$mode" --work "$work"
    exit $?
fi

exec bash tools/mirror/run_mirror.sh --capture "$@"
