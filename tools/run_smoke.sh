#!/bin/bash
# JVM smoke suite for the South Park town app (tools/SmokeTest.kt).
# Compiles src/ + the harness against android.jar (KidArt's SHEETS
# are pure data — no android methods are invoked) and runs it.
set -e
cd "$(dirname "$0")/.."
export JAVA_HOME=$HOME/build-tools/jdk-17.0.9+9-jre
export PATH=$JAVA_HOME/bin:$PATH
KOTLINC=$HOME/build-tools/kotlin/kotlinc/bin/kotlinc
STDLIB=$HOME/build-tools/kotlin/kotlinc/lib/kotlin-stdlib.jar
ANDROID_JAR=$HOME/android-sdk/platforms/android-34/android.jar
OUT=${TMPDIR:-/tmp}/spt-smoke
rm -rf "$OUT" && mkdir -p "$OUT"
echo "--- compile src + harness ---"
# kotlinc reports errors as "path:line: error: ..." — capture the
# log and surface those lines readably. (The old pipeline grepped
# for "^error", which never matches that format, so a compile
# break surfaced later as a class-not-found stack trace.)
COMPILE_STATUS=0
$KOTLINC -J-Xmx2g -J-Xms64m -jvm-target 1.8 -no-reflect \
    src/com/hunt3r/southparktown/*.kt tools/SmokeTest.kt \
    -cp "$ANDROID_JAR" -d "$OUT/classes.jar" > "$OUT/compile.log" 2>&1 || COMPILE_STATUS=$?
if [ "$COMPILE_STATUS" -ne 0 ]; then
    echo "--- compile FAILED (kotlinc exit $COMPILE_STATUS) ---"
    grep -iE ": error:" "$OUT/compile.log" | head -50 || true
    grep -qiE ": error:" "$OUT/compile.log" || cat "$OUT/compile.log"
    exit 1
fi
echo "--- run ---"
java -cp "$OUT/classes.jar:$STDLIB:$ANDROID_JAR" com.hunt3r.southparktown.SmokeTestKt "$PWD"
