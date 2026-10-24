package com.hunt3r.southparktown

import android.graphics.Canvas

// ============================================================
// PaperDoll — v0.16.0d (spec stage 10): the SECOND rendering
// path. The source pipeline keeps a genuinely separate 2D
// paper-doll system for its menus and stills, distinct from
// the world renderer — poses, materials and detail rules of
// its own. Ours now does too:
//
//  - WORLD path (KidArt.drawFigure, doll = false): figures
//    in the living town — facings and true profiles, depth
//    scaling, walk cycles, the full animation system.
//  - DOLL path (this object, doll = true): figures in
//    STILLS — photo album, yearbook pages, darkroom prints,
//    the character creator. Front pose only, a fixed still
//    clock (a doll never stands mid-stride), full detail,
//    and the painted outline at PORTRAIT weight (a wider,
//    more variable marker rim than the world rim).
//
// Stills posing front is CORRECT under doll rules — that is
// what closes partial #14: the stills were never broken
// world figures, they were dolls rendered by the wrong
// path. Every still surface routes through here now
// (PhotoView for album / yearbook / prints, CreatorActivity
// for the new-kid doll), and the suite pins the routing:
// no still surface may call the world entry points
// directly, and this object may never produce a profile.
//
// Costumes and sleepwear are still the same spec the world
// wears (Photos.bakeCostume / the sleeper's spec decide
// WHAT is worn); the doll decides HOW it is drawn.
// ============================================================
object PaperDoll {

    /** The doll's fixed clock: enough for the idle breath
     *  phase baked into FigureAnim, never a walk frame. */
    const val DOLL_TIME_MS = 1000L

    /** A standing doll: front pose, doll rules. */
    fun drawDoll(c: Canvas, spec: SheetSpec, cx: Float, cy: Float,
                 h: Float, mood: String, seed: Long) {
        KidArt.drawFigure(c, spec, cx, cy, h, mood, "stand",
            FigureAnim(timeMs = DOLL_TIME_MS, seed = seed.toInt()), doll = true)
    }

    /** A costumed doll: the record's frozen hero code drives
     *  the same hero pass the street uses — through the
     *  doll path. */
    fun drawDollHero(c: Canvas, spec: SheetSpec, code: String,
                     cx: Float, cy: Float, h: Float,
                     mood: String, seed: Long) {
        KidArt.drawHeroFigure(c, spec, code, cx, cy, h, mood, "stand",
            FigureAnim(timeMs = DOLL_TIME_MS, seed = seed.toInt()), doll = true)
    }

    /** A sleeping doll: the record's frozen sleepwear code,
     *  doll rules. */
    fun drawDollSleep(c: Canvas, spec: SheetSpec, code: String,
                      cx: Float, cy: Float, h: Float,
                      mood: String, seed: Long) {
        KidArt.drawSleepFigure(c, spec, code, cx, cy, h, mood, "stand",
            FigureAnim(timeMs = DOLL_TIME_MS, seed = seed.toInt()), doll = true)
    }

    /** The creator's new-kid doll. */
    fun drawDollAvatar(c: Canvas, appearance: Map<String, String>,
                       cx: Float, cy: Float, h: Float) {
        KidArt.drawPlayerAvatar(c, appearance, cx, cy, h,
            FigureAnim(timeMs = DOLL_TIME_MS), "stand", doll = true)
    }
}
