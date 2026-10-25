# South Park: Living Town (Android)

Portrait South Park adventure game, built like the show was: layered
construction-paper cut-outs, and you standing in the scene.
A non-commercial fan project — respect my authoritah.

## What it is
- Walk South Park as a paper cut-out town (v0.15.0c): every location is a
  layered paper scene — sky, far range, a street of buildings, the ground
  band, props, people — and YOUR kid stands in it. Tap the ground, a person,
  a prop, a door, a bus stop: the kid walks there and it happens. Travel
  between the 27 game-mirrored locations by the signposts, the 🗺 Go menu,
  or the bus; tap a kid to talk → South-Park-style speech bubbles.
- 70 characters live their day on an **in-game clock** (15-min ticks from
  8 AM, advances as you play/wait): the 5 original kids plus Wendy, Craig,
  Tweek, Clyde, Jimmy, Timmy, Tolkien, Bebe, the 3 goth kids, Randy, Sharon,
  Gerald, Sheila, Liane, Stuart, Carol, Stephen, Linda, Garrison, Mackey,
  Chef, Jesus, Jimbo, Father Maxi, and Towelie — joined in v0.3.0b by Heidi,
  Nichole, Red, Kevin, Francis, DogPoo, Mayor McDaniels, Officer Barbrady,
  Ms. Crabtree, Mr. & Mrs. Tweek, and Ned — in v0.7.0a by Karen, Scott,
  Firkle, Annie, Bradley, Skeeter, Principal Victoria, Grandpa Marvin,
  Mr. Tucker, and Steve Black — in v0.8.0a by Ike Broflovski,
  Kevin McCormick, Shelley Marsh, Laura Tucker, Dr. Mephesto,
  Sergeant Yates, Mr. Adler, and Lu Kim of City Wok — and in v0.9.0a by
  Roger Donovan, Ryan & Sarah Valmer, Alexis & Mr. Testaburger,
  Nathan, Sophie Gray, and Big Gay Al. Day/night + seasons +
  procedural weather follow the in-game date. (Real-device-time sync kept,
  disabled behind a Settings toggle defaulting OFF.)
- Animal Crossing structure, all South Park style: friendship levels per
  character, rotating date-seeded daily dialogue (12+ topics each), 25
  collectibles, 8 holiday events — each with its own playable
  signature beat since v0.4.0d — cozy no-fail pace.
- Character creator at first launch (Stick-of-Truth-styled, 13 categories
  since v0.1.9d): the **blank New Kid** (bald, in underwear — matches the
  games) is the default base; customize skin, build, hair, hat, top, bottom,
  shoes, eyewear, facial hair, gloves, back item and accessory from it —
  with a dice roll for a whole look and 3 preset slots for favorites.
- Dialogue: pick your LLM provider in Settings — **Gemini** (`gemini-2.5-flash`,
  free key from Google AI Studio) or **OpenRouter** (`deepseek/deepseek-v4-flash`
  default, editable model field, your own OpenRouter key). Keys are stored on
  the phone only, never logged. Without a key for the selected provider, a
  built-in mock brain takes over (pair scripts + line banks).
- All art is code-drawn on Canvas in one consistent style: the New Kid model —
  giant circle head (~2/3 of height), no neck, big touching white oval eyes
  with dot pupils, tiny stub body. Every character (kids and adults) uses it.
  No ripped assets; wiki pages used as text/visual reference only.

## Build (no Gradle — low-memory VM)
Requires: `~/build-tools/kotlin` (kotlinc 1.9.24), `~/android-sdk`
(build-tools 34.0.0, android-34), a JDK 17 (`~/build-tools/jdk-17.0.9+9-jre`).

```bash
cd ~/workspace/sptown-app
./build_apk.sh            # → out/southpark-town.apk (debug-signed)
```
Bump versions in `src/com/hunt3r/southparktown/Version.kt` — the single
source of version identity; `build_apk.sh` derives both the code and the
name from it.

## Status — v0.18.0d

Restructure Phase 2 — CANONICAL (versionCode 130): defect D2
closed. The lifetime soak no longer bounds lifeText by a raw
count (which swung 293–318 across identical trees and was
recalibrated for measurement reasons at v0.16.0d). Keys are
sorted before measuring, and the bound is a function of the
canonical key set: `CanonKeys` reduces each key to its
template (stamps to #, dates to @#, registry-named segments
to *), and the year's templates are pinned two-sided (74
required lifetime shapes + 1 allowed winter shape; 50 + 1 in
the Q10 audit soak) with each shape's peak daily count
bounded by a structural ceiling — registry arithmetic
(literal shapes at 1, registry shapes at the live census,
stamped shapes at their chain/record bounds), never a
sampled maximum — and replay pins prove the measurement
identical across insertion and hash orders. No game code
changed. Full entry:
`docs/CHANGELOG.md`. Suite: 110,538 checks, 0 failures
(SMOKE OK) — identical across the phase's three verification
recompiles, canonical bounds stable in all three.
APK: southpark-town.apk, versionCode 130 / versionName 0.18.0d (aapt2 badging verified); signer SHA-256 a9997c1e73d8b38ab5e099b5cbb6c17ac0a9347c67c37399f3c6011562826ba6; lorebook md5 acff1c04e84dfd583dbc5b2e20ed50b5.

## Invariants

- C1 — Nothing below the platform line imports android.* beyond the drawing layer.
- C2 — Store decisions, derive state.
- C3 — One writer per state channel.
- C4 — `TownBaseline` is frozen.
- C5 — One figure geometry source.
- C6 — One width (`SceneScale.unitW`).
- C7 — One skeleton, texture faces, baked outlines.
- C8 — Determinism from seed.
- C9 — Event-sourced saves: additive keys only; an old save loads whole.
- C10 — Validation gate: nothing ships that fails the validator.
- C11 — Priority is declared, not positional.
- C12 — A pin reads code, never prose.

Full text with enforcement notes: `docs/INVARIANTS.md`.

## Docs

- `docs/CHANGELOG.md` — the full status history and changelog (the old README's history lives here, verbatim).
- `docs/TUNING.md` — the tuning ledger: every paper-set constant with its file and line.
- `docs/ARCHITECTURE.md` — the source layout.
- `docs/INVARIANTS.md` — the twelve invariants C1–C12 with their enforcement notes.
- `docs/LORE.md` — the lorebook byte-identity rule and re-audit checklist.
- `docs/CHARACTER_BIBLE.md` — the character draw, rig and animation bible.

The full history that used to live in this README is in
`docs/CHANGELOG.md` — nothing was deleted, it moved.
