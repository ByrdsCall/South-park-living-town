#!/usr/bin/env python3
"""
gen_icon.py — install the app's launcher icon (v0.15.0e).

Until v0.15.0d this script DREW an icon in code (a parchment
SOUTH / PARK sign from the game's palette). Lucky asked for a
new logo (Oct 17), approved Hunt3r's paper cut-out town-sign
artwork, and the drawing code was retired whole — old assets
don't stick. There is exactly one icon now: the approved
artwork, kept in-repo at tools/logo/south-park-town-icon.png
(512x512, sha256
b9b9191a076e55f8f76d1faa1c716c066981c39db406090238a4497745fce0ab),
and this step installs it byte for byte as
res/drawable/ic_launcher.png. Nothing is redrawn, resampled
or recolored here — a straight copy, so what Lucky approved
is what ships.

The matching wide title art ships as assets/title_logo.png
(the same sign, 1200x760) and is decoded by MainActivity.

Wired into build_apk.sh; non-fatal there: if this step ever
fails, the build keeps the checked-in res/drawable PNG
(which is the same approved bytes).

Usage: python3 tools/gen_icon.py [output.png]
"""
import hashlib
import shutil
import sys
from pathlib import Path

MASTER = Path(__file__).resolve().parent / "logo" / "south-park-town-icon.png"
EXPECTED_SHA256 = "b9b9191a076e55f8f76d1faa1c716c066981c39db406090238a4497745fce0ab"


def main():
    out = Path(sys.argv[1]) if len(sys.argv) > 1 else Path("res/drawable/ic_launcher.png")
    digest = hashlib.sha256(MASTER.read_bytes()).hexdigest()
    if digest != EXPECTED_SHA256:
        raise SystemExit(f"icon master hash drifted: {digest} != {EXPECTED_SHA256}")
    shutil.copyfile(MASTER, out)
    print(f"icon installed (approved artwork, sha256 {digest[:12]}...): {out}")


if __name__ == "__main__":
    main()
