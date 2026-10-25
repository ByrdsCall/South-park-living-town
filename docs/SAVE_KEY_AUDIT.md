# Save-key audit — the unread-key report (Phase 3, v0.18.0e)

Generated for Restructure Phase 3 (Part 8.1; defect D12's
per-queue audit). Method: the registry in `SaveKeys.kt` was
cross-checked against live source scans of the final tree —
every store accessor call in `Prefs.kt`, the encode key set
of `encodeGameState`, every `doc["..."]` read in `Prefs.kt`,
and the `toMap`/`fromMap` key sets of every record module —
and the suite re-pins the same equalities at runtime
(`sectionV0180e`), so this report cannot silently go stale:
a key added on any side without the registry fails the build.

## Census

- Store keys (SaveStore level): **21**, all declared in `SaveKeys.STORE`, each with one owner.
- game_state doc keys (top level): **107**, all declared in `SaveKeys.DOC`, each with one owner.
- Nested key groups: **69** declared by path in `SaveKeys.GROUPS` (59 closed key sets, 10 open groups whose keys are dynamic by design — names, times, ids).
- Record modules with `toMap`/`fromMap` pairs: Commissions.kt, Darkroom.kt, Episodes.kt, Errands.kt, Fair.kt, Gallery.kt, Goals.kt, Minigames.kt, Phone.kt, Photos.kt, Stories.kt, Yearbook.kt.

## Store keys: read/write balance

All 21 store keys are both written and read through the Prefs API (the suite's API sweep touches exactly the registered set — no unregistered key is used and no registered key is dead). Two balance notes:

- `friendship`, `collected`, `spawn_date` are written by `saveGame` on every save and read ONLY on the salvage path (`loadGame` when the main doc is unreadable) plus nowhere else. They are deliberate shadow copies, not dead keys: they are the reason a damaged save recovers the town's bonds, collection and founding date (damage corpus fixtures 1, 2 and 10).
- `last_played_date` is likewise a shadow of the doc key, and is additionally read by `getLastPlayedDate`.
- `game_state` is the only store key ever deleted (`clearGame`); `wipeAll` clears the whole store.

## Doc keys: written vs read

`encodeGameState` writes 107 keys; `decodeGameState` reads 106 of them directly. Written but not read by `decodeGameState` itself: **saveFormat** — read instead by `inferSaveFormat` (via `storedSaveFormat`), i.e. the doc's self-recorded format generation. Not dead; accounted for.

Read but never written: **none**. Every key decode consults is a key encode emits (old saves simply lack the additive keys, and decode defaults them — the era behaviour the fixture corpus pins).

## Nested groups: the one real finding

Every nested group's written key set equals its read key set, with two exceptions — both in `gallery[]` (GalleryShow, owner Gallery):

- **`hangDay` — WRITTEN, NEVER READ.** `GalleryShow.toMap` persists it; `fromMap` ignores it and re-derives the hang day from `GalleryCalendar.cal(year, season)`.
- **`closeDay` — WRITTEN, NEVER READ.** Same shape: re-derived from the calendar on load.

Both are stored values the game can compute from (seed, year, season) — a C2 ("store decisions, derive state") tension, harmless today: the derived value is the value that would have been stored, because both come from the same calendar function. They are documented here, registered in `SaveKeys` with the finding on the key, and NOT removed in this phase — retiring persisted keys is Phase 11 work, and an old save's copy must keep loading (it does: the keys are simply ignored).

Two conditional-write shapes, symmetric by design (readers accept all keys; writers emit them only when they carry information):

- `errands[]`: the six `chain*` keys are written only for chain links (chainLen > 0); the two `emerg*` keys only for emergency errands. `fromMap` reads all 22.
- `errands[].chainRest[]`: nested links decode with chain fields zeroed (the depth rule in `Errand.fromMapDepth`), so their maps carry only the 14 unconditional keys (plus `emerg*` for an emergency link).

Helper-mediated reads (verified by hand against source, since the readers consult keys through local helpers rather than literal `m["..."]`): `Episodes.fromMap` (`strList`/`intOf`) reads all 30 EpisodeRecord keys; `Stories.fromMap` likewise reads all 10 StoryArcState keys; `Fair.fromMap`'s `places` helper reads all four place lists of the 16 FairRecord keys.

## Open key spaces (by design, not drift)

These doc values are maps whose KEYS are data, declared as open groups in the registry: `life` and `lifeText` (feature tallies and marks — `lifeText`'s shape space is canonicalized by the Phase 2 CanonKeys templates), `scheduleDrift` (agent -> time -> location), `phone.meta` (phone counters), `commissions.last`, `episodes[].startInfluence`, and the entity-keyed record maps `agents`, `factionStates`, `fishLog`, `showdown`. Their value shapes are registered groups; their key spaces are the game's registries (cast, factions, species), not free text.

## Damage corpus coverage (D11)

`sectionV0180e` feeds one damaged save per failure class through the real load path and asserts the landed state: truncated doc, bracket bomb (parser depth cap), bad location id, short todayKey, NaN coords, impossible month, inflated maps (ticker/errand/phone caps + a 10k-entry open map), hostile ids, the arc decoders' hostile-doc cases, type confusion, blank/absent docs, npc_memory garbage, and repair idempotence (a repaired save re-saves byte-identically). Salvage paths exercised for the first time: `loadGame`'s fall-through to `recoverGameState` with live shadow keys, and recover's per-input failure branches (garbage friendship alone, garbage collection alone, nulls). Decode clamps newly exercised: saneLoc on agents and walk dest, todayKey rebuild, coords coercion, month/day/timeMin coercion, the events/errands/phone decode caps, and every registry filter (clues, wardrobe, pocket pals, factions, arc calls/commits/pairs/voices).

## Disposition

No key is retired in this phase. The audit's only purge candidates are `gallery[].hangDay` and `gallery[].closeDay`, held for the Phase 11 dead-key purge with this report as evidence.
