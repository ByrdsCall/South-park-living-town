#!/bin/bash
# Manual APK build for South Park Living Town — NO Gradle (low-memory VM).
# Pattern adapted from ~/workspace/pokedex-apk/build_apk.sh, with kotlinc.
set -u
export JAVA_HOME=$HOME/build-tools/jdk-17.0.9+9-jre
export PATH=$JAVA_HOME/bin:$PATH

KOTLINC=$HOME/build-tools/kotlin/kotlinc/bin/kotlinc
STDLIB=$HOME/build-tools/kotlin/kotlinc/lib/kotlin-stdlib.jar
SDK=$HOME/android-sdk
BT=$SDK/build-tools/34.0.0
AJAR=$SDK/platforms/android-34/android.jar

APP=$HOME/workspace/sptown-app
SRC=$APP/src
RES=$APP/res
MANIFEST=$APP/AndroidManifest.xml
OUT=$APP/out
mkdir -p $OUT/gen $OUT/classes $OUT/dex

# v0.18.0a (D7): version identity lives ONLY in Version.kt —
# parse both consts from it; fail loudly when unparseable so
# a drifted or renamed declaration can never ship silently.
VERSION_KT=$APP/src/com/hunt3r/southparktown/Version.kt
VERSION_CODE=$(sed -n 's/.*const val VERSION_CODE: Int = \([0-9][0-9]*\).*/\1/p' "$VERSION_KT" | head -1)
VERSION_NAME=$(sed -n 's/.*const val VERSION_NAME: String = "\([^"]*\)".*/\1/p' "$VERSION_KT" | head -1)
[ -n "$VERSION_CODE" ] && [ -n "$VERSION_NAME" ] || { echo "cannot parse version from Version.kt"; exit 1; }
echo "version: $VERSION_NAME (code $VERSION_CODE) from Version.kt"

echo "--- checking tools ---"
[ -x "$KOTLINC" ] || { echo "kotlinc missing at $KOTLINC"; exit 1; }
[ -f "$AJAR" ] || { echo "android.jar missing"; exit 1; }
[ -f "$STDLIB" ] || { echo "kotlin-stdlib.jar missing"; exit 1; }

echo "--- launcher icon (drawn in code: tools/gen_icon.py) ---"
# v0.1.9f: our own icon, regenerated from the game's palette at
# build time. Non-fatal: without python3/PIL the checked-in PNG
# (produced by the same script) ships as-is.
if python3 -c "import PIL" 2>/dev/null; then
  python3 $APP/tools/gen_icon.py $RES/drawable/ic_launcher.png || \
    echo "icon regen failed — keeping checked-in icon"
else
  echo "PIL unavailable — keeping checked-in icon"
fi

echo "--- aapt2 compile res ---"
$BT/aapt2 compile --dir $RES -o $OUT/compiled_res.zip || exit 1

echo "--- aapt2 link ---"
$BT/aapt2 link -o $OUT/base.apk \
  -I $AJAR \
  --manifest $MANIFEST \
  -A $APP/assets \
  --java $OUT/gen \
  --min-sdk-version 26 --target-sdk-version 34 \
  --version-code $VERSION_CODE --version-name $VERSION_NAME \
  $OUT/compiled_res.zip || exit 1

echo "--- kotlinc ---"
KT_FILES=$(find $SRC -name "*.kt")
GEN_FILES=$(find $OUT/gen -name "*.java")
echo "kotlin files: $(echo "$KT_FILES" | wc -l), gen files: $(echo "$GEN_FILES" | wc -l)"
# v0.15.0c: heap 1g -> 2g — the 116-file tree outgrew the
# 1g cap (deterministic psi2ir OutOfMemoryError at 1g;
# the identical compile peaks between 1g and 1.4g). The
# suite's run_smoke.sh already standardizes on -J-Xmx2g
# for compiles on this VM; this aligns the build to it.
$KOTLINC -J-Xmx2g -J-Xms64m -jvm-target 1.8 -no-reflect \
  -cp "$AJAR" -d $OUT/classes \
  $KT_FILES || exit 1

echo "--- d8 ---"
$BT/d8 --lib $AJAR --min-api 26 --output $OUT/dex \
  $(find $OUT/classes -name "*.class") "$STDLIB" || exit 1

echo "--- package ---"
cp $OUT/base.apk $OUT/unsigned.apk
(cd $OUT/dex && zip -q -j $OUT/unsigned.apk classes.dex)
$BT/zipalign -f 4 $OUT/unsigned.apk $OUT/aligned.apk || exit 1

# v0.3.0b lesson, learned the hard way: the signing keystore must
# NOT live in $OUT — a full `rm -rf out` clean once destroyed the
# original key (cert 21d9d201...) and with it in-place updates.
# The keystore now lives at $APP/debug.keystore (durable); a legacy
# $OUT copy is adopted on sight, and a fresh key is generated only
# when neither exists.
KS=$APP/debug.keystore
if [ ! -f $KS ] && [ -f $OUT/debug.keystore ]; then
  cp $OUT/debug.keystore $KS
fi
if [ ! -f $KS ]; then
  echo "--- generating debug key ---"
  $JAVA_HOME/bin/keytool -genkeypair -keystore $KS \
    -alias androiddebugkey -storepass android -keypass android \
    -keyalg RSA -keysize 2048 -validity 10950 \
    -dname "CN=Android Debug,O=Android,C=US" || exit 1
fi

echo "--- apksigner ---"
$BT/apksigner sign --ks $KS --ks-pass pass:android \
  --key-pass pass:android \
  --out $OUT/southpark-town.apk $OUT/aligned.apk || exit 1
$BT/apksigner verify --print-certs $OUT/southpark-town.apk | head -3
ls -la $OUT/southpark-town.apk
echo "=== APK BUILD DONE ==="
