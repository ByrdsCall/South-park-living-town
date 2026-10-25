# Invariants — South Park: Living Town

The twelve invariants from the Restructure Order, Part 1. Each one
names how the build enforces it; a violation fails the suite.

- **C1.** **Nothing below the platform line imports android.* beyond the drawing layer.** android.graphics.* is the drawing vocabulary (shimmed by the Linux mirror) and is allowed anywhere; every other android package is confined to the platform allowlist named in the suite's purity scan (Activities, GameView host, DialogueSheet, UiKit, MinigameViews, PhotoView, PocketPalArt, Sound, LoreLoad, AndroidSaveStore) plus Paper.kt's single android.content.res.Resources exception. *Enforcement: the suite's purity-wall scan over `src/` (sectionV0180a).*
- **C2.** **Store decisions, derive state.** No module stores a value it can compute from `(seed, absDay, timeMin, log)`. *Enforcement: per-module scan plus a "read leaves the save byte-identical" pin on every pure reader.*
- **C3.** **One writer per state channel.** A key has exactly one owning module. *Enforcement: the `SaveKeys.kt` registry (Restructure Order Part 8).*
- **C4.** **`TownBaseline` is frozen.** Runtime code never writes base; a full fiction run over it pins drift at zero. *Enforcement: the existing fiction drift pin, extended to every new system.*
- **C5.** **One figure geometry source.** World figure, still doll, and reference sheet are three *contexts* of one renderer, never three renderers. *Enforcement: single `FigureRender.render(ctx, spec, pose)`.*
- **C6.** **One width.** Composition reads `SceneScale.unitW`, never the live world width `w`. *Enforcement: lint — `w *` is banned in painter composition (Part 2, D19).*
- **C7.** **One skeleton, texture faces, baked outlines.** No morphing, no per-frame outline stretching. *Enforcement: `Rig` pivot gate plus outline parity pins.*
- **C8.** **Determinism from seed.** A replay of `(seed, fiction, parcel, tick)` dresses identically. *Enforcement: the fiction replay pin, extended to all derived systems.*
- **C9.** **Event-sourced saves.** Additive keys only; an old save loads whole. *Enforcement: the save-format fixture corpus.*
- **C10.** **Validation gate.** Nothing ships that fails the asset/assembly validator. *Enforcement: `validate()` on every generated spec.*
- **C11.** **Priority is declared, not positional.** Claims carry a band; order derives from the band. *Enforcement: the Agenda/TalkGate band resolver (Part 3).*
- **C12.** **A pin reads code, never prose.** No `indexOf` against a file's text for ordering. *Enforcement: enum ordinals as the single source (Part 2, D6).*
