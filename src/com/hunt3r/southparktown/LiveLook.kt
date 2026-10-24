package com.hunt3r.southparktown

/**
 * v0.15.0a: LIVE LOOK — the preview pass (Queue #15
 * item 1). ONE derivation for what the player figure
 * is actually wearing right now, shared by every
 * surface that shows YOU to you:
 *
 *  - DAY   — your ordinary look (the creator's day
 *    clothes), the default on every other terms.
 *  - SLEEP — your pajamas, while a sleepover night
 *    is LIVE (Sleepwear.eveningLive: the plan stands,
 *    the evening has opened). The code is
 *    Sleepwear.playerCode — the shutter's own player
 *    branch, the mirror's own code since v0.14.0a.
 *  - HERO  — your costume, while a patrol night is
 *    in its window (identity made, Wed/Sat, 19:30–
 *    22:00). The code is Heroes.playerCostumeCodeFor
 *    — figureCode's own derivation, emblem off the
 *    same power the caper records.
 *
 * The previews read THIS and nothing else: the
 * character creator's preview, the Journal YOU
 * card's figure, and the Your House mirror glass
 * (GameView) all take their kind + code from
 * playerLook, so a preview can never show a look
 * the town isn't living. The street and the shutter
 * keep their own stricter VISIBILITY gates on top
 * of the same codes — Heroes.streetCostumeCode
 * still asks for a patrol spot before the street
 * draws you, Sleepwear.streetCode still asks for
 * outdoors before the street draws your pajamas,
 * Photos still freezes through figureCode — but
 * the CODE each surface draws is derived here and
 * nowhere else. There is no second derivation.
 *
 * The two live kinds can never collide in the
 * lived calendar — sleepovers are Fridays, patrols
 * are Wednesday/Saturday — and the precedence is
 * still stated for a staged or tampered state:
 * SLEEP first (a night at home in pajamas is the
 * more specific fact), HERO second, DAY last.
 *
 * The breadth note, stated plainly: the preview's
 * hero moment is the patrol WINDOW, not the patrol
 * SPOT — the mirror hangs at your house, which is
 * not a spot, and a preview that only caped you at
 * the park would never show the costume it exists
 * to show. The street keeps the spot gate because
 * the street is a place; a preview is a look.
 *
 * Pure Kotlin (state reads only) — rendering a
 * preview writes nothing, and the suite pins the
 * whole table.
 */
object LiveLook {

    const val DAY = "day"
    const val SLEEP = "sleep"
    const val HERO = "hero"

    /** The live look: a kind ([DAY] / [SLEEP] /
     *  [HERO]) and its costume code ("" for DAY —
     *  the day look IS the appearance itself). */
    data class Look(val kind: String, val code: String)

    /** Is a patrol night's window live for a preview?
     *  Heroes' own cadence constants, read whole:
     *  identity made, a patrol weekday, inside the
     *  patrol hours. (The spot gate is the street's,
     *  not the look's — see the header.) */
    fun heroLive(state: GameState): Boolean =
        Heroes.heroCreated(state) &&
            Heroes.isPatrolNight(state.absDay) &&
            Heroes.inWindow(state.timeMin)

    /** THE derivation: what [appearance] is wearing
     *  in [state] right now. [appearance] is a
     *  parameter, not a read, because the creator
     *  previews the look UNDER EDIT — its sleep and
     *  hero codes derive from the edited palette,
     *  exactly as the saved look's codes will derive
     *  from the saved palette. Every other surface
     *  passes the worn appearance (playerLook). */
    fun lookFor(state: GameState,
                appearance: Map<String, String>): Look {
        if (Sleepwear.eveningLive(state))
            return Look(SLEEP, Sleepwear.playerCode(appearance))
        if (heroLive(state))
            return Look(HERO,
                Heroes.playerCostumeCodeFor(state, appearance))
        return Look(DAY, "")
    }

    /** The worn look: [lookFor] read off the player
     *  agent's own appearance — the mirror's and the
     *  YOU card's door. A state with no player reads
     *  as the plain day look. */
    fun playerLook(state: GameState): Look {
        val me = state.agents.values.firstOrNull { it.isPlayer }
            ?: return Look(DAY, "")
        return lookFor(state, me.appearance)
    }
}
