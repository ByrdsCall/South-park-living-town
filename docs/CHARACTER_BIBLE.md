# CHARACTER DRAW, RIG & ANIMATION BIBLE

**File:** `docs/CHARACTER_BIBLE.md` · **Version:** 1.0 · **Governs:** `Rig.kt`, `KidArt.kt`, `FigureGeometry`, `FigurePieces`, `HeadStack`, `Expressions`, `ActionAnims`, `MotionRegistry`, `Gait.kt`, `Sleepwear.kt`, `Heroes.kt`, `PaperDoll.kt`, `SheetGen.kt`

This is the source of truth. Code reads it; code does not define it. If a number here and a number in the tree disagree, this file wins and the tree is wrong.

> **Hunt3r receipt note (Oct 24, 2026):** first copy received ~18:01 CDT ended mid-sentence in Part 12.1; Lucky ruled 18:06 CDT: "Just use what you have, ignore what you don't," and Parts 7–9 were steered into the in-flight v0.17.0b fix build as the acceptance reference for the crowd-variety fix. A second, fuller copy arrived 18:16 CDT extending the text through Parts 12.2–12.4, 13 (poses) and 14 (animation), ending mid-sentence in Part 14.5 (Timmy's motion-override row); a third fragment at 18:23 CDT completed Timmy's row, the registry rules, and the gesture bank, ending mid-row at the `none` gesture. The saved file now holds everything received; anything after the gesture bank is not in force and not awaited, per the same ruling. Lands in the repo as `docs/CHARACTER_BIBLE.md` with the Phase 0 doc split.

---

## PART 0 — The one rule

**The show is the oracle. The games are not.**

The games' characters are 3D-printable figurines with sculpted volume. The show's characters are **flat pieces of construction paper cut out with scissors, laid on a table, and filmed from directly in front.** Every rule below serves that read. When a proportion is ambiguous, choose the one that looks more like a cutout.

Consequences that never bend:

- **No perspective.** No foreshortening inside a figure. A limb that goes "back" is drawn the same size, just sorted behind.
- **No shading on the figure.** Flat fill, one ink rim, one flat projected shadow. Lighting happens *around* the figure, never on it.
- **No volume.** The head is a flat ellipse with a flat fill, not a sphere.
- **No gradients.** Ever. Not on skin, cloth, metal, or the outline.
- **No sub-pixel softness.** Edges are crisp. The ink rim is a shape, not a blur.

---

## PART 1 — Units, coordinates, reference frame

| Term | Definition |
|---|---|
| **k** (kid unit) | The canonical length unit. One nominal kid standing height = **1.000 k**. |
| **Reference scale** | 1.000 k = **186 px** at 1:1. |
| **Origin** | Feet centre. `(0, 0)`. |
| **Axes** | `+y` up. `+x` = viewer's right = the character's **left**. |
| **Angles** | Degrees. Positive rotation swings a limb **forward** (toward the viewer). |
| **Facing** | `FRONT` or `PROFILE`. `PROFILE` is authored facing right; left-facing is a mirror of the *whole figure*, never a per-part flip. |
| **Depth** | `z` is a small integer sort key inside a figure. Larger = nearer the camera. |
| **Precision** | Three decimals. Anything finer than 0.001 k is noise at 186 px. |

**The density floor.** A render context must give the reference unit ≥ 186 px of drawn height. Below that, the outline window (Part 12) can't hold and the context fails rather than upscaling mush. The doll portrait context runs at 2.4× the reference and the sheet context at 3.0×.

---

## PART 2 — The scale contract

`ScaleContract` in `Rig.kt`. One canonical unit, one ladder, hard gates.

```
body_scale:  toddler 0.70 · kid 1.00 · cartman-class 0.95 · young_teen 1.12
             teen 1.22 · older_teen 1.31 · adult 1.35 · elder 1.30
```

**Props are sized from the wearer, never absolutely.** A lance is `1.20 × wearer height`; a shield `0.40`; a banner `0.90`; a crate `0.35`; a cape `0.62`. A magic number in a prop is a bug.

**Drift gate.** Any class whose composed anchor drifts more than **5%** from the contract **fails the build** unless the divergence is logged with a measurement. Two are logged and lawful:

| Character | Class | Contract | Anchor | Drift | Law |
|---|---|---|---|---|---|
| Eric Cartman | cartman-class | 0.95 | 1.00 | +5.3% | His bulk lives in **width** (head-body width ratio 0.74, hem 0.92), never height. |
| Ike Broflovski | toddler | 0.70 | 0.80 | +14.3% | His measured sheet (head share 0.655) beats the generic class. |

**Outline weight is specified in final screen pixels, not in k.** Window `[3, 7]` px at 1:1. A context that can't hold the window fails the density floor.

---

## PART 3 — The skeleton

**Twenty joints. No fingers. No facial bones. One skeleton for the entire cast, the player, generated townsfolk, family, and visitors.**

Rest pose for a nominal kid (1.000 k), absolute coordinates, origin at feet centre.

| # | Joint | Parent | rest x | rest y | Segment | Rotation limits |
|---|---|---|---|---|---|---|
| 1 | `root` | — | 0.000 | 0.000 | — | translate only |
| 2 | `hips` | root | 0.000 | 0.262 | 0.262 | ±14° bend, ±10° twist |
| 3 | `spine` | hips | 0.000 | 0.322 | 0.060 | ±10° bend |
| 4 | `chest` | spine | 0.000 | 0.398 | 0.076 | ±8° bend |
| 5 | `head` | chest | 0.000 | 0.460 | 0.062 | ±24° tilt, ±30° roll |
| 6 | `hat` | head | 0.000 | 1.000 | — | inherits head, no local rotation |
| 7 | `back` | chest | 0.000 | 0.430 | — | inherits chest, no local rotation |
| 8 | `shoulder.L` | chest | +0.148 | 0.432 | — | ±155° swing, ±72° lift |
| 9 | `elbow.L` | shoulder.L | +0.180 | 0.358 | 0.081 | 0 → 105° flex, one direction |
| 10 | `wrist.L` | elbow.L | +0.196 | 0.283 | 0.077 | ±28° |
| 11 | `shoulder.R` | chest | −0.148 | 0.432 | — | mirror of 8 |
| 12 | `elbow.R` | shoulder.R | −0.180 | 0.358 | 0.081 | mirror of 9 |
| 13 | `wrist.R` | elbow.R | −0.196 | 0.283 | 0.077 | mirror of 10 |
| 14 | `hip.L` | hips | +0.070 | 0.255 | — | −38° → +48° swing |
| 15 | `knee.L` | hip.L | +0.078 | 0.150 | 0.106 | 0 → 112° flex, one direction |
| 16 | `ankle.L` | hip.L→knee | +0.084 | 0.052 | 0.099 | ±26° |
| 17 | `hip.R` | hips | −0.070 | 0.255 | — | mirror of 14 |
| 18 | `knee.R` | hip.R | −0.078 | 0.150 | 0.106 | mirror of 15 |
| 19 | `ankle.R` | knee.R | −0.084 | 0.052 | 0.099 | mirror of 16 |
| 20 | `prop.R` | wrist.R | 0.000 | 0.000 | — | rides the hand; the only joint a held object binds to |

**The skeleton IS the proportion table.** `head` sits at `1.000 − head_share`; `hips` sits at `leg_share`; the torso is the gap between. Change a spec's head share and the joint moves. There is no separate skeleton per class.

**Hard constraints:**

- Elbows and knees flex **one direction only**. A hyperextended elbow is a bug.
- No IK. Feet are placed by the pose, not solved.
- No root motion. `root` translates only when a controller moves the actor; the cycle never drives it (Part 14).
- `hat`, `back` and `prop.R` carry **no local rotation** — they inherit their parent exactly. A hat that counter-rotates is a bug.

---

## PART 4 — Part cards and the assembly algorithm

A figure is **not drawn**. It is **assembled**, then drawn. Every visible thing on a figure is a card:

```
Part(id, bone, pivot, z, coverage, made_from, region)
```

| Field | Meaning | Rule |
|---|---|---|
| `id` | Stable string. | Unique inside a spec. |
| `bone` | The joint it rides. | Must be one of the twenty. |
| `pivot` | Local offset on that bone, normalised to segment length so it scales. | **Must sit within 0.020 k of its bone's axis** — the pivot gate. |
| `z` | Integer sort key. | Front stack resolves **at assembly**, never by painter order. |
| `coverage` | 0.0–1.0. How much of what's behind it the card covers. | `< 1.0` means the thing behind shows around it. |
| `made_from` | Provenance: what this costume is literally built from. | `"bedsheet"`, `"refrigerator box"`, `"cardboard"`, `"sheet"`. |
| `region` | `SKIN` · `CLOTH` · `METAL` · `SURFACE`. | Selects the paper material (Part 12). |

**The standard card set** (a spec may add `accessory` cards; it may not reorder these bands):

| z | Card | Bone | Region |
|---|---|---|---|
| 0 | `shadow` | root | SURFACE |
| 10 | `back_item` | back | CLOTH |
| 15 | `accessory_back` (wheelchair, backpack) | root / back | varies |
| 20 | `back_hair` | head | CLOTH |
| 30 / 32 / 34 | `leg_upper.R` / `leg_lower.R` / `foot.R` | hip.R / knee.R / ankle.R | CLOTH |
| 36 / 38 / 40 | same, `.L` | mirrored | CLOTH |
| 45 | `accessory_mid` (crutches) | wrist.L / wrist.R | METAL |
| 50 | `torso` | chest | CLOTH |
| 55 / 57 / 59 | `arm_upper.R` / `arm_lower.R` / `hand.R` | shoulder.R / elbow.R / wrist.R | CLOTH → SKIN on hand |
| 60 / 62 / 64 | same, `.L` | mirrored | — |
| 68 | `ear.R` / `ear.L` | head | SKIN, coverage 0.60 |
| 70 | `head_base` | head | SKIN |
| 76 | `eye.R` / `eye.L` | head | SURFACE (white) |
| 78 | `pupil.R` / `pupil.L` | head | SURFACE (ink) |
| 80 | `brow.R` / `brow.L` | head | CLOTH (hair colour) |
| 82 | `mouth` | head | SURFACE (ink) |
| 84 | `face_extra` (glasses, beard, freckles, braces) | head | varies |
| 88 | `hair_front` | head | CLOTH (hair colour) |
| 90 | `hair_top` | head | CLOTH (hair colour) |
| 94 | `hat` | hat | CLOTH |
| 96 | `prop.R` (held object) | prop.R | varies |

**Assembly rules:**

1. Sort cards by `z`. Ties break by `id`, alphabetically — deterministic, always.
2. The `coverage < 1.0` cards draw the real thing first, then the costume over it, so a `made_from: "refrigerator box"` castle wall shows the actual brick at the edges and the tape tabs stay visible.
3. **Pivot gate:** every card's pivot must resolve **inside its own silhouette and on its joint**. A card that would detach fails the build.
4. `KidArt` assembles every figure through the rigger **first**, then draws limbs from the assembled cards' pivots — so the painter and the gate read the same joints.
5. Pose, `ActionAnims`, `MotionRegistry` overrides, profile stack, seated legs, the gait, costume overlays and `PaperDoll` all run on the **assembled** figure. Nothing bypasses assembly.

---

## PART 5 — The proportion ladder

Every figure is derived from its own measured sheet. Classes exist **only** as the generated-NPC fallback and as the sanity band.

| Class | body_scale | head share | torso share | leg share | head aspect (w:h) | shoulder ÷ head w | hem ÷ shoulder | upper arm r ÷ head w | thigh r ÷ head w | hand r ÷ head w | foot len ÷ head w |
|---|---|---|---|---|---|---|---|---|---|---|---|
| toddler | 0.70 | 0.660 | 0.130 | 0.210 | 1.08 | 0.60 | 0.82 | 0.105 | 0.125 | 0.135 | 0.30 |
| kid | 1.00 | 0.540 | 0.200 | 0.260 | 1.05 | 0.62 | 0.80 | 0.110 | 0.130 | 0.140 | 0.32 |
| cartman-class | 0.95 | 0.550 | 0.200 | 0.250 | 1.22 | 0.74 | 0.92 | 0.120 | 0.140 | 0.150 | 0.33 |
| young_teen | 1.12 | 0.480 | 0.220 | 0.300 | 1.02 | 0.64 | 0.82 | 0.110 | 0.120 | 0.130 | 0.30 |
| teen | 1.22 | 0.440 | 0.240 | 0.320 | 1.00 | 0.66 | 0.84 | 0.105 | 0.115 | 0.125 | 0.29 |
| older_teen | 1.31 | 0.400 | 0.250 | 0.350 | 0.98 | 0.68 | 0.86 | 0.100 | 0.105 | 0.115 | 0.28 |
| adult | 1.35 | 0.350 | 0.280 | 0.370 | 0.94 | 0.70 | 0.88 | 0.095 | 0.095 | 0.105 | 0.26 |
| elder | 1.30 | 0.360 | 0.280 | 0.360 | 0.95 | 0.70 | 0.90 | 0.095 | 0.090 | 0.100 | 0.25 |

Head share + torso share + leg share = 1.000 for every row. Verify it whenever you touch this table.

**Torso geometry.** A **trapezoid**, not a rectangle. Shoulder width from the table, hem width = shoulder × `hem ÷ shoulder`. Flat hem, no curve. **Torso is a trapezoid with flat hems** — that is the show's read.

**Arms and legs are capsules** rotated about their shoulder/hip anchors, rooted **inside** the torso silhouette so no floating oval can appear at any pose, size band or width. The attachment invariant is pinned for every class × band × width.

**Width applies to the whole silhouette.** A wide character is wide in the head, torso, hem, limbs and hands by the same factor. Width never touches height (that is the Cartman law).

---

## PART 6 — Draw order

**Front figure:**

```
shadow → back_item → accessory_back → back_hair → legs(R then L) → accessory_mid
→ torso → arms(R then L) → ears → head_base → eyes → pupils → brows → mouth
→ face_extra → hair_front → hair_top → hat → prop
```

**Profile figure:**

```
shadow → back_item → accessory_back → back_hair → far leg → far arm → torso
→ near leg → near arm → ear → head_base → eye → pupil → brow → mouth
→ face_extra → hair_front → hair_top → hat → prop
```

The profile's **far leg rides offset and one shade darker**; the near limb is full. That is the entire depth illusion inside a figure — there is no other.

**Scene order:** sky → far range (parallax 0.55) → buildings (parallax 0.85) → ground + street strip → props → **figures, y-sorted by feet line** → traffic → foreground frieze. The foreground's top edge must clear the worst possible chin by ≥ 0.15 of scene height.

**Nothing in the figure pass is ever weather-worn or night-mixed.** Every scene tint paints *before* the figure pass, so faces stay near-unlit and expressions stay legible at every hour. This is pinned.

---

## PART 7 — Head construction

The head is **one ellipse**, filled flat, region `SKIN`, carrying the ink rim.

- Width = `head_share × head_aspect × standing_height`. Height = `head_share × standing_height`.
- **No neck.** The head's bottom edge overlaps the torso's top by **2 units** — the chin is tucked, never floating.
- Face anchors are **fractions of the ellipse**, so features scale with the head. No fixed pixel offsets.

The head is a **stack**, painted in this order:

| Layer | Contents | Rule |
|---|---|---|
| 1. Behind-hair | long panels, ponytails, the beehive's base | Falls **past the shoulders**, tucked under the torso. |
| 2. Ears | small ellipses on the side edges | Drawn **behind** the head base, never as blobs on the face edge. |
| 3. Head base | the ellipse | SKIN. |
| 4. Features | eyes, pupils, brows, mouth, face extras | Stay **clean** — no paper grain over them. |
| 5. Hair front | the fringe cap, side locks | Built on a padded ellipse. |
| 6. Hair top | the mass | Built on the same padded ellipse. |
| 7. Hat | headwear | Built on a further-padded ellipse. |

**Pads are the no-sliver guarantee and they are not optional:**

- Hair mass is built on an ellipse padded **+2 units** past the head ellipse, so the hair mass provably **contains** the dome sector it covers. No skin sliver inside a hair region. No floating corner.
- Hat mass is built on an ellipse padded **+3.5 units**, so a hat provably hugs the head it sits on.

**Hairline** sits at `eye_top − 6 units`, per style.

**Face clear:** no hair and no hat shape may cover an eye or a mouth anchor. Pinned for all specs by rasterising the head stack against the face anchors. **Kenny's parka hood is the one lawful covered mouth** — his sheet says so.

---

## PART 8 — The face

### 8.1 The grid

`FaceManifest`. **8 emotions × 6 mouths = 48 stable, human-readable ids.**

Rows (emotion): `neutral` · `happy` · `angry` · `scared` · `smug` · `sad` · `shocked` · `deadpan`
Columns (mouth): `closed` · `small_o` · `wide` · `grin` · `teeth` · `frown`

Id format: `emotion_mouth`, e.g. `angry_wide`, `happy_grin`, `sad_closed`.

State picks the **row**. Lip-sync picks the **column**. They are independent channels.

### 8.2 Geometry — exact

All values are fractions of the head ellipse. `hw` = head width, `hh` = head height.

| Feature | Spec |
|---|---|
| Eye ovals | Width `0.190 × hw` each. Height `0.230 × hh`. Centres at `x = ±(eyeWidth ÷ 2)` so the two ovals **touch exactly at the centreline**. Combined span `0.380 × hw`. Centre y at `0.546 × hh` **from the top** (i.e. head-local `y = +0.245 × hh`). |
| Pupils | Diameter `0.30 × eyeWidth`. Seated at the **inner-lower third**: `x` offset `+0.30 × eyeWidth ÷ 2` toward the centreline, `y` offset `−0.20 × eyeHeight`. |
| Brows | Width `0.150 × hw`, at head-local `y = +0.335 × hh` — i.e. `0.028 k` above the eye top. Only drawn when the expression names a brow shape. Four shapes: `flat`, `raised`, `angry`, `worried`. |
| Mouth | Width `0.175 × hw`, centred on the head's vertical centreline, at head-local `y = +0.118 × hh`. |
| Nose | **None** for kids. Adults may carry one small mark; an authored per-character exception. |
| Ears | Width `0.070 × hw`, height `0.110 × hh`, at head-local `y = +0.245 × hh`, `x = ±0.470 × hw`. Behind the head base. |

**Eyes are white ovals with black dot pupils. Nothing else. No iris, no highlight, no lash except the two-flick lash on lash-carrying characters.**

### 8.3 Mouth glyphs

Each column is a fixed glyph, never a morph:

| Column | Path |
|---|---|
| `closed` | Flat line, width `0.175 × hw`, stroke weight = rim weight × 0.9. |
| `small_o` | Ellipse `0.060 × hw` × `0.055 × hh`. |
| `wide` | Ellipse `0.130 × hw` × `0.110 × hh`. |
| `grin` | Upward arc, chord `0.190 × hw`, sagitta `0.055 × hh`. |
| `teeth` | `grin` plus a white bar `0.150 × hw` × `0.028 × hh` inside the upper lip. |
| `frown` | Downward arc, chord `0.165 × hw`, sagitta `0.045 × hh`. |

### 8.4 Lip sync

The mouth swaps between **two fixed glyphs** on the speech phase. Never interpolates height.

| Voice register | Glyph pair | Rate |
|---|---|---|
| normal | `closed` ↔ `small_o` | 7.5 Hz, seeded jitter ±0.9 Hz |
| raised | `closed` ↔ `wide` | 8.5 Hz |
| muffled (parka) | `closed` ↔ `small_o`, `×0.82` width | 6.8 Hz |
| electronic (voice box) | `closed` ↔ `wide` | **flat** rate, no jitter |

**Talk is a channel over the mouth column, not a stored face.** That is why it is not a grid row.

### 8.5 Eyelids and blinks

- Idle blink: seeded per character, interval 2.0–5.0 s. Closed 90 ms, half 60 ms. A double-blink 1 in 6.
- `sleep`: sagging arcs covering `0.55` of the eye height.
- `hurt`: squeezed to `0.25` with a downward outer corner.
- `deadpan`: lids at `0.40`, flat.

### 8.6 Compat

`FaceCompat` resolves **every** identifier a pre-redo save can hold — legacy sheet face names (`nervous`, `worried`, `braces`), grid ids, unknown strings. Resolution **never fails**. Pinned by a pre-redo save fixture round-trip.

---

## PART 9 — Hair and hats

**Two hat classes, and the rule differs.**

**Covering hats** — beanie, cap, pomhat, ushanka, chullo, trapper, bandana, santa, sleuth:

- The cap mass yields, but the **fringe survives as a strip** wherever the hairline dips below the brim.
- **Temple tufts peek at both sides.**
- **Flaps own the temples** (Kyle's ushanka over his afro, Craig's chullo).
- **Below-brim length stays**: locks, bob frames, ponytails.
- Spikes and buns are **swallowed** by the dome, never poking through.

**Perch hats** — beret, yarmulke, earmuffs, headband, cowboy, wizard, crown, toque, taco:

- Sit **on** the full head of hair. Hair is not thinned.

**Style rules:**

- Every style keeps its sheet hairline and its own edge curves.
- Long hair has a **front cap** (this was the bug that left four characters with skin domes) plus behind panels falling past the shoulders.
- The afro has a front cap over its halo.
- A buzz cut's corners must sit **on** the skull, never float.
- Garrison stays honestly bald — side fringe bands, skin dome — because his sheet says so.
- Hats that are accents (the wizard's star) seat **inside** the shape that owns them, never floating off an edge.

---

## PART 10 — Limbs, hands, feet

**Arms.** Upper and lower capsules, radius from the table, rooted inside the torso. Sleeve roots are **buried in the torso** so the same-colour fill merges with no internal stroke. The figure has **one silhouette** — hair and hat overlap the head, chin overlaps the torso, sleeves merge into the torso, and same-colour parts share fills with **no internal strokes**.

**Legs keep their split.** The visible gap between legs is part of the style and is never closed.

**Hands are big mittens.** No fingers, no thumb split. A single rounded mass, radius from the table, at the wrist. Flat.

**Feet are wide flat shoes.** Length from the table, height `0.055 × hw`, sole flat, toe slightly wider than heel.

**Accessories bind to bones, not to the skeleton:**

| Accessory | Bone | z |
|---|---|---|
| Crutches | `wrist.L` / `wrist.R` | 45 |
| Wheelchair | `root` | 15 |
| Backpack | `back` | 10 |

**Towelie is the one figure with his own renderer** and is exempt from this part.

---

## PART 11 — Back items and held props

- Back items (cape, wings, backpack) draw on a **behind-the-body layer** at z 10, bound to `back`, which inherits `chest` exactly.
- Held props bind to `prop.R` at z 96, sized **from the wearer** (Part 2).
- The cape's length reads the contract, never a magic number.
- In profile, the cape draws behind the torso and the held prop foreshortens by a **declared** factor (chest emblem `×0.55`), never by perspective math.

---

## PART 12 — Outline and materials

### 12.1 Outline engine v2

The rim is **part of the shape**, not a stroke applied afterward.

- Width is computed **per contour point from that contour's own curvature**: thickest at bends, light on straight runs, interior detail at `0.6×`.
- Clamped to `[3, 7]` px in **final screen pixels**.
- **Baked into the cut ribbon at render time.**
- **Regenerated per context** — world figures at world weight, `PaperDoll` portraits at the doll's heavier bias, sheets at sheet weight. **Never drawn once and stretched.**
- Parity is pinned: the same window holds at toddler, kid and adult heights.

### 12.2 Scissor-cut contours

Contours carry seeded edge wobble and **genuinely rounded corners**. The same character is cut the same way every frame — **boil never re-rolls an edge**.

### 12.3 Region-aware paper

100% procedural. The scanned kit is never used.

| Region | Treatment | Worn on |
|---|---|---|
| `SKIN` | Fine grain, whisper strength, slight saturation lift | Heads; **under** the features, which stay clean |
| `CLOTH` | Coarse fibre, stronger weave bias, a touch of speckle | Torsos, the fiction banner |
| `METAL` | Sparse grain + a pale edge sheen | Lid shields, trash cans, hydrants, lamps |
| `SURFACE` | The existing treatment, unchanged | Architecture, ground |

**One fibre per jittered cell, in every region**, so no clustered pattern can form. Minimum spacing is pinned — the trypophobia rule.

### 12.4 Shadow

Flat projected ellipse under the figure. Width `0.340 × figure width`, height `0.055 × figure width`, ink at **0.18 alpha**, offset `0.020 k` down-right. Flat. Never soft.

---

## PART 13 — Poses

Nine poses. Every pose is a **bone rotation set**; nothing is a separate drawing.

| Pose | Definition |
|---|---|
| `stand` | All bones at rest. Breath and sway are additive (Part 14). |
| `walk` | Legs scissored ±26°, arms counter-swinging ±22°, body bob `0.018 k` at 2× step rate, waddle sway ±3°. |
| `sit` | Hips lowered to the seat line; **two-segment seated leg** (thigh + shin), ankle on the furniture plan's own ankle line, length ≥ 90% of the standing leg for adults. |
| `sit_down` / `stand_up` | **Three held sub-poses** over 7 ticks. Stepped, not eased. |
| `profile` | Authored facing right. Far limb offset and one shade darker. |
| `sleep` | Lids sagged `0.55`, breath `0.020 k` at 4.6 s. |
| `hurt` | Lids squeezed `0.25`, torso recoil 6°, one hop. |
| `talk` | Head bobble ±2.5° at 4.2 Hz; mouth on the speech channel; one gesture per 2.5 s from the gesture bank. |
| `carry` | Elbows flexed 78°, hands in front of the chest. |

**Legs stay whole while sitting for adults** — no knee-lock, no lost shins. Pinned per character for the whole cast.

---

## PART 14 — Animation

### 14.1 Velocity-driven rate, no root motion

```
phasePerPx = K · boxH / (legLen · drawnHeight)
```

`K` is calibrated so the reference kid keeps the legacy ~22 rad-per-world-width cadence.

- The player integrates phase from the distance each input step **actually moved**.
- Route travellers integrate from `Travel.pose` progress over the whole route, at their own leg geometry.
- Wanderers integrate over the out-and-back path they actually walk.
- **Idle rate is 0.** No actor's cycle is clock-driven. Position comes **only** from the controllers.
- Pinned: phase advance proportional to distance at kid and adult geometries; idle frozen; shorter step = smaller advance.

### 14.2 One-owner controller contract

`MoveControl.kt`. Exactly **one owner per actor per moment**: `FREE_ROAM` · `TRAVEL` · `BUS_RIDE` · `INSIDE_SECTION` · `SETTLED`. Handoffs are documented at the three real seams: `Travel.maintain` starting from the actor's actual position; bus boarding locking input; section entry/exit. Animation **reads** movement state; its only write-back is the notify channel. Truth table pinned.

### 14.3 Clip notifies

`Gait.footDowns` turns phase crossings of the π multiples into footstep events: **exactly one per foot per cycle — two per full cycle, one per half, none without advance.**

- The player hears the synthesized step and, in snow, pops a `0.020 k` puff at the striking foot that fades as the stride rolls on.
- Travellers are throttled and quiet.
- Wanderers get the **visual puff only** — thirty simultaneous steppers would drown the mix.

**Not timers.** Stop the cycle and the steps stop.

### 14.4 Action registry

Twenty-one actions, every interactive trigger resolving to exactly one: `idle` `walk` `talk` `sit_down` `seated` `stand_up` `use_object` `pickup_trash` `phone` `board_bus` `ride_bus` `conversation` `eat` `sleep` `work` `play` `photo` `wave` `dance` `hurt` `cheer`.

### 14.5 Motion registry

Every cast member gets a **distinct motion signature**: `{gait, stride, bob, armSwing, gestureKind, restingExpression, phase}` from their sheet + band, with **31 authored overrides**. A character whose motion is wrong on device is **one line** in their override row. Lawful examples: Cartman waddles (sway `×1.8`, stride `×0.92`); Tweek jitters (bob `×2.4`, phase noise `0.06`); Craig is still (sway `×0.3`); Timmy has a chair rhythm (bob 0 — the chair moves, not him; wheel pulse on the chair card).

**Registry rules:**

- Every value is **derived** from the sheet + band, then **overridden** by an authored row if one exists. Derive-then-override, never author-from-scratch.
- The signature must be **globally distinct** across the cast: no two members share all seven values. Pinned pairwise.
- `restingExpression` must be a valid `FaceManifest` id and must be the face the character wears at idle.
- `gestureKind` selects from the gesture bank below. A character with no authored `gestureKind` gets the band default.
- `phase` offsets the gait so a crowd never marches in lockstep.

**Gesture bank** (used by `talk`, `wave`, `cheer`, `play`):

| Kind | Motion | Period |
|---|---|---|
| `point` | Forearm to 62°, hold 0.6 s, return | 2.8 s |
| `shrug` | Both shoulders +18°, palms up, 0.9 s | 3.4 s |
| `thumb` | Wrist rotates 30°, forearm 40° | 2.2 s |
| `jab` | Forearm 0→55° three times | 1.9 s |
| `sweep` | Arm crosses the chest 70°, once | 3.0 s |
| `fidget` | Hand micro-oscillation 4°, continuous | 1.1 s |
| `none` | No gesture

*[third received fragment ends here, mid-row in the gesture bank — see receipt note]*
