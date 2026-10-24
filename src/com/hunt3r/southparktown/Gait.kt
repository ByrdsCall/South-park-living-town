package com.hunt3r.southparktown

import kotlin.math.PI
import kotlin.math.floor

// ============================================================
// Gait — v0.16.0e, Part 2 item 1+3: the movement spec's
// headline steal (sot-movement-animation-spec.md, B3+B4).
//
// NO ROOT MOTION, VELOCITY-DRIVEN PLAYBACK RATE. A figure's
// position comes from its controller alone (PaperWalk for
// the player, the engine's grid walker for travelers,
// Wander's slide for the crowd); the walk cycle plays IN
// PLACE and its phase advances with the DISTANCE actually
// travelled — never with the wall clock. Before this build
// the traveler phase was `(now % 1256) / 200` — a free-
// running clock every walker shared, so feet slid against
// the ground at whatever speed the walker happened to
// move, and the crowd's phase ran at gaitHz whether the
// figure was crossing its slot or standing in the pause
// at its end. Both are the same bug, and this is the fix:
//
//   phase += distancePx * phasePerPx(legLen, boxH, fhPx)
//   phasePerPx = GAIT_K * boxH / (legLen * fhPx)
//
// The rate derives from the figure's OWN leg geometry
// (FigureGeometry's plan: legLen over boxH, at the
// figure's drawn height), so a long-legged adult cycles
// slower per metre than a stub-legged kid, and every
// walker at every speed keeps the same phase-per-metre.
// GAIT_K is the one tuned constant of the stride model:
// the drawn swing is a shuffle (the show's waddle — the
// ankle's horizontal amplitude is only SWING * legLen),
// so the cycle is NOT the geometric zero-slip rate; K is
// calibrated so the reference kid (legLen 13 / boxH 128)
// at the player's established walk keeps the cadence the
// town has always used (~22 rad per world width at
// reference geometry), and it is pinned as a derivation,
// not a magic number. Idle rate is exactly zero: a
// standing figure's phase does not advance, by
// construction — no distance, no phase.
//
// FOOTSTEP NOTIFIES (B4, lite): a foot goes down each
// time the cycle phase crosses a multiple of PI — the
// near foot on even crossings, the far foot on odd ones.
// [footDowns] is the write-back channel: the ONLY thing
// animation ever fires back into the world (step sounds,
// snow puffs) travels through it, exactly as the spec's
// interlock contract demands. If the cycle stops, the
// steps stop — there are no timers anywhere in this.
// ============================================================

object Gait {

    /** The stride model constant: phase radians per
     *  leg-length of travel, folded through the plan's
     *  proportions (see the header). 1/(2π) keeps the
     *  reference kid's cadence at the town's established
     *  walk rate at reference geometry. */
    const val GAIT_K: Float = 0.15915f   // ≈ 1 / (2π)

    /** The ankle swing factor KidArt draws with — the
     *  stride amplitude is SWING * legLen plan units.
     *  Gait reads it; KidArt owns it (the suite pins the
     *  two together so they can never drift apart). */
    const val SWING: Float = 0.38f

    /** The drawn ankle-swing amplitude, px, for a figure
     *  whose plan leg is [legLen] of [boxH] units, drawn
     *  [fhPx] tall. */
    fun swingAmpPx(legLen: Float, boxH: Float, fhPx: Float): Float =
        SWING * legLen * fhPx / boxH

    /** Phase radians advanced per pixel travelled, for
     *  the figure's own leg geometry at its drawn height.
     *  A degenerate leg falls back to the kid plan's rate
     *  rather than dividing by nothing. */
    fun phasePerPx(legLen: Float, boxH: Float, fhPx: Float): Float {
        val leg = if (legLen > 0.5f) legLen else 13f
        val box = if (boxH > 1f) boxH else 128f
        val fh = if (fhPx > 1f) fhPx else 1f
        return GAIT_K * box / (leg * fh)
    }

    /** Phase radians advanced per world-width travelled
     *  (the unit PaperWalk's state moves in): [fhFrac] is
     *  the figure's drawn height as a fraction of the
     *  world width it walks across. */
    fun phasePerFrac(legLen: Float, boxH: Float, fhFrac: Float): Float {
        val leg = if (legLen > 0.5f) legLen else 13f
        val box = if (boxH > 1f) boxH else 128f
        val f = if (fhFrac > 0.001f) fhFrac else 0.001f
        return GAIT_K * box / (leg * f)
    }

    /** Phase advance for travelling [distPx] pixels. */
    fun phaseAdvance(distPx: Float, legLen: Float, boxH: Float,
                     fhPx: Float): Float =
        distPx * phasePerPx(legLen, boxH, fhPx)

    /** The foot-down index at [phase]: how many foot-downs
     *  the cycle has passed (one per PI of phase). */
    fun footIndex(phase: Float): Int =
        floor(phase / PI.toFloat()).toInt()

    /** The foot-down crossings strictly between [prev] and
     *  [new] phase, as their indices (even = near foot,
     *  odd = far foot). Empty when the phase did not
     *  advance past a crossing — and when it did not
     *  advance at all, which is what makes a stopped
     *  cycle silent. */
    fun footDowns(prev: Float, new: Float): List<Int> {
        if (new <= prev) return emptyList()
        val a = footIndex(prev)
        val b = footIndex(new)
        if (b <= a) return emptyList()
        return (a + 1..b).toList()
    }

    /** How fresh the latest foot-down is, 0..1, as a
     *  function of phase alone: 1 the instant a foot
     *  lands, fading to 0 over the next [PUFF_RAD] radians
     *  of cycle. GameView's snow puffs read this — a puff
     *  exists only while the cycle is turning through a
     *  landing, and a walker who stops mid-stride (phase
     *  frozen) is drawn standing, [walking] false, so no
     *  puff hangs in the air. */
    const val PUFF_RAD: Float = 0.45f

    fun puffIntensity(phase: Float, walking: Boolean): Float {
        if (!walking) return 0f
        val step = PI.toFloat()
        val past = phase - floor(phase / step) * step
        return (1f - past / PUFF_RAD).coerceIn(0f, 1f)
    }
}
