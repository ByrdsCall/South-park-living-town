package com.hunt3r.southparktown

// ============================================================
// Barks — v0.4.0e: greeting barks. You hear who's around
// before you read a line.
//
// A bark is a RHYTHM SIGNATURE: a short sequence of blip notes
// (count, relative pitches, gaps) rendered by SoundGen on the
// character's existing blip voice (SoundGen.BLIP_FAMILIES), so
// a bark is recognizably THEM in pattern before a word is
// read — Cartman drawn-out and imperious, Kenny a rapid
// muffled patter (the MUFFLED family does the parka), Craig a
// flat two-note, Ned an electronic buzz-chirp through his
// voice box. The patterns are DATA (this table); the suite
// pins completeness, distinctness, and the per-character
// shape rules the table claims.
//
// Every hello below is an ORIGINAL one-liner written for this
// game in each character's voice — the game's standing
// paraphrase discipline: nothing is quoted from the show.
//
// This file is pure Kotlin (no android.* imports): the table,
// the trigger rules (caps, cooldowns, quiet hours, presence
// weighting, volume shaping) and the speech-bubble geometry
// all live here where the JVM suite can pin them. Sound.kt
// plays, GameView draws, GameActivity decides WHEN — via the
// gates below. Bark state (cooldowns) is in-memory garnish
// only; nothing here touches a save.
// ============================================================

object Barks {

    /** One blip note of a pattern: [semi] = semitone offset
     *  from the character's blip-family base pitch; [len] =
     *  note length in "beats", 1 beat = the family's blip
     *  duration (0.085–0.13 s), so patterns scale with voice. */
    data class Note(val semi: Int, val len: Float)

    /** A character's bark: the note sequence, the silence
     *  between notes (beats), whether it comes through an
     *  electronic voice box (Ned), and the original one-line
     *  hello the speech bubble shows while it sounds. */
    data class Pattern(
        val notes: List<Note>,
        val gap: Float,
        val electronic: Boolean = false,
        val hello: String
    )

    private fun n(semi: Int, len: Float) = Note(semi, len)
    private fun p(gap: Float, hello: String, vararg notes: Note,
                  electronic: Boolean = false) =
        Pattern(notes.toList(), gap, electronic, hello)

    val PATTERNS: Map<String, Pattern> = mapOf(
        // ---------------- the kids ----------------
        "Stan" to p(0.35f, "Hey, dude. What's goin' on?",
            n(0, 1.0f), n(0, 0.7f), n(2, 1.0f)),
        "Kyle" to p(0.30f, "Hey! You need something?",
            n(0, 0.8f), n(2, 0.8f), n(0, 1.2f)),
        // drawn-out and imperious: the first note drags two
        // full beats before he deigns to continue
        "Cartman" to p(0.30f, "Well, well. Look who showed up.",
            n(0, 2.0f), n(-2, 0.7f), n(0, 0.9f)),
        // rapid muffled patter — six short notes through the
        // parka (his family IS the lowpass)
        "Kenny" to p(0.12f, "Mmf mmph— mmph mmph!",
            n(0, 0.45f), n(2, 0.45f), n(0, 0.45f),
            n(3, 0.45f), n(1, 0.45f), n(0, 0.6f)),
        // cheerful up-lilt: ends higher than he started
        "Butters" to p(0.30f, "Oh, hey there, fella!",
            n(0, 0.8f), n(2, 0.8f), n(4, 1.3f)),
        "Wendy" to p(0.28f, "Hi! Good to see you!",
            n(0, 0.8f), n(3, 0.8f), n(5, 1.0f)),
        // flat two-note. That's the whole greeting. It's Craig.
        "Craig" to p(0.40f, "Oh. Hey.",
            n(0, 1.0f), n(0, 1.0f)),
        // jittery staccato, pitches jumping like his eyes
        "Tweek" to p(0.15f, "Gah! Oh— hi! Hi!",
            n(2, 0.4f), n(5, 0.4f), n(2, 0.4f), n(7, 0.5f)),
        "Clyde" to p(0.35f, "Yo. 'Sup.",
            n(4, 0.9f), n(2, 0.9f), n(0, 1.1f)),
        // the showman repeats his first note — a stammer built
        // into the rhythm, then lands the flourish
        "Jimmy" to p(0.25f, "W-wow, great audience— hi!",
            n(0, 0.5f), n(0, 0.5f), n(4, 1.2f)),
        "Timmy" to p(0.30f, "TIMMY! …Timmy!",
            n(0, 1.0f), n(5, 1.3f)),
        "Tolkien" to p(0.30f, "Hey. Good timing.",
            n(0, 0.9f), n(1, 0.9f), n(3, 1.0f)),
        "Bebe" to p(0.22f, "Hiii! Oh my gosh, hi!",
            n(3, 0.6f), n(5, 0.6f), n(3, 0.6f), n(7, 1.0f)),
        "Michael" to p(0.45f, "…Hey. Whatever.",
            n(-2, 1.1f), n(-2, 0.9f), n(-4, 1.2f)),
        "Pete" to p(0.50f, "Ugh. Hi, I guess.",
            n(-1, 1.0f), n(-3, 1.1f)),
        "Henrietta" to p(0.50f, "Oh. It's you. …Hi.",
            n(2, 1.1f), n(0, 1.3f)),
        "Heidi" to p(0.28f, "Hi hi! Over here!",
            n(0, 0.7f), n(4, 0.7f), n(2, 1.1f)),
        "Nichole" to p(0.30f, "Hey! How's your day going?",
            n(0, 0.8f), n(2, 0.8f), n(4, 1.0f)),
        "Red" to p(0.20f, "Hey hey! What's up?",
            n(5, 0.5f), n(5, 0.5f), n(2, 1.0f)),
        // the pedant starts a lecture and catches himself
        "Kevin" to p(0.20f, "Oh, hey. Did you know— never mind.",
            n(0, 0.6f), n(1, 0.6f), n(2, 0.6f), n(0, 1.0f)),
        "Francis" to p(0.50f, "Oh… hiya.",
            n(-1, 1.2f), n(0, 1.4f)),
        "DogPoo" to p(0.40f, "Hey. …You can see me? Cool.",
            n(-2, 0.8f), n(-2, 0.8f), n(-1, 1.0f)),

        // ---------------- the adults ----------------
        "Randy" to p(0.25f, "Well hey there! Big day, huh?",
            n(0, 0.8f), n(3, 0.8f), n(5, 0.8f), n(7, 1.2f)),
        "Sharon" to p(0.35f, "Hello, dear! Everything alright?",
            n(0, 0.9f), n(2, 1.1f)),
        "Gerald" to p(0.30f, "Ah, hello there. All good?",
            n(0, 0.8f), n(0, 0.8f), n(2, 1.0f)),
        "Sheila" to p(0.22f, "Well, hello there, sweetie!",
            n(2, 0.7f), n(4, 0.7f), n(2, 0.7f), n(5, 1.1f)),
        "Liane" to p(0.40f, "Oh, hello, sweetie~",
            n(3, 1.0f), n(5, 1.2f)),
        "Stuart" to p(0.45f, "Oh. Hey, kid.",
            n(-2, 1.0f), n(-2, 1.0f), n(-3, 1.2f)),
        "Carol" to p(0.20f, "Oh! Hi, hon— watch the ice!",
            n(1, 0.6f), n(3, 0.6f), n(1, 0.6f), n(3, 0.9f)),
        "Stephen" to p(0.45f, "Evening. Behaving yourself?",
            n(0, 1.0f), n(-2, 1.1f)),
        "Linda" to p(0.30f, "Hi there, hon!",
            n(0, 0.8f), n(4, 1.1f)),
        "Garrison" to p(0.25f, "Well, hello there, kid.",
            n(0, 0.7f), n(2, 0.7f), n(0, 0.7f), n(3, 1.1f)),
        "Mackey" to p(0.50f, "Hey there. Doin' okay?",
            n(0, 1.2f), n(1, 1.4f)),
        // Chef sings from the basement: two low, slow notes
        "Chef" to p(0.40f, "Mmm, hello there, child.",
            n(0, 1.1f), n(-3, 1.4f)),
        "Jesus" to p(0.55f, "Peace be with you, little friend.",
            n(0, 1.3f), n(2, 1.5f)),
        "Jimbo" to p(0.28f, "Well hey there, kiddo!",
            n(0, 0.7f), n(0, 0.7f), n(3, 1.1f)),
        "Father Maxi" to p(0.40f, "Ah, hello, my child.",
            n(0, 1.0f), n(2, 1.2f)),
        "Towelie" to p(0.42f, "Oh… heyyy. What's goin' on, man?",
            n(2, 0.9f), n(5, 0.9f), n(3, 1.3f)),
        "McDaniels" to p(0.24f, "Ah, a constituent. Hello.",
            n(0, 0.7f), n(2, 0.7f), n(4, 1.0f)),
        "Barbrady" to p(0.30f, "Move along— oh. Hello, citizen!",
            n(0, 0.8f), n(0, 0.8f), n(2, 1.2f)),
        "Crabtree" to p(0.20f, "What?! Oh. It's you. Hi.",
            n(1, 0.6f), n(1, 0.6f), n(0, 1.0f)),
        "Mr. Tweek" to p(0.32f, "Ah, hello. Smell that roast?",
            n(0, 0.9f), n(2, 0.9f), n(1, 1.1f)),
        "Mrs. Tweek" to p(0.33f, "Well hello, dear.",
            n(0, 0.9f), n(3, 1.1f)),
        // the voice box: an electronic buzz-chirp — stepped
        // pitch, squared wave, a little mains hum in the AM.
        // Marked electronic; SoundGen renders him differently
        // from every flesh-and-blood voice in town.
        "Ned" to p(0.18f, "Bzzt— hey there, kid.",
            n(0, 0.5f), n(0, 0.5f), n(4, 0.8f), electronic = true),

        // ---------------- wave 3 (v0.7.0a) ----------------
        // small, soft, rising — a little sister's hello, brave
        // at the end of it
        "Karen" to p(0.30f, "Hi! I'm Karen!",
            n(0, 0.7f), n(2, 0.7f), n(4, 1.1f)),
        // earnest hero patter: four quick notes, rehearsed
        "Scott" to p(0.18f, "Hi! I'm Thcott— Scott!",
            n(0, 0.5f), n(2, 0.5f), n(1, 0.5f), n(3, 0.9f)),
        // two low notes at knee height. The whole greeting.
        "Firkle" to p(0.50f, "…Hi. Whatever.",
            n(-3, 1.0f), n(-4, 1.2f)),
        // bright broadcast chatter, comes in already mid-news
        "Annie" to p(0.22f, "Hii! Annie! Hi hi!",
            n(2, 0.6f), n(4, 0.6f), n(5, 0.6f), n(4, 1.0f)),
        // shy, shy — then the crunch: a leap up at the end
        "Bradley" to p(0.32f, "Oh— hi! Bradley!",
            n(0, 0.8f), n(0, 0.6f), n(7, 1.2f)),
        // a bar-owner's boom: starts low, opens the door wide
        "Skeeter" to p(0.28f, "Well hey now! C'mon in!",
            n(-1, 0.9f), n(0, 0.7f), n(0, 0.7f), n(3, 1.2f)),
        // crisp, measured, clipboard-paced: three even notes
        "Victoria" to p(0.45f, "Hello. Principal Victoria.",
            n(0, 0.9f), n(2, 0.9f), n(0, 1.1f)),
        // the slowest hello in town: two unimpressed drawls
        "Marvin" to p(0.55f, "Eh? …Oh. It's you.",
            n(-2, 1.3f), n(-2, 1.1f)),
        // Craig's flatness, inherited: two notes, one of them
        // lower, both of them final
        "Mr. Tucker" to p(0.42f, "Oh. Hey there.",
            n(0, 1.1f), n(-1, 1.1f)),
        // smooth exec warmth: unhurried, everything annotated
        "Steve" to p(0.33f, "Well, hello there.",
            n(0, 0.9f), n(3, 0.7f), n(2, 1.2f)),

        // ---------------- wave 4 (v0.8.0a) ----------------
        // toddler patter: high, quick, delighted with itself
        "Ike" to p(0.16f, "Ba ba— hi! Ike!",
            n(4, 0.4f), n(6, 0.4f), n(4, 0.6f), n(7, 0.9f)),
        // the eldest McCormick: a teen drawl, low for a kid,
        // in no hurry whatsoever
        "Kevin McCormick" to p(0.38f, "Oh. Hey. Kevin.",
            n(-2, 0.9f), n(-2, 0.8f), n(0, 1.1f)),
        // teen contempt, three falling notes: the turds can wait
        "Shelley" to p(0.30f, "Ugh. What do YOU want?",
            n(2, 0.6f), n(0, 0.6f), n(-2, 1.2f)),
        // the smiling half of the Tuckers: warm, even, unhurried
        "Laura" to p(0.34f, "Well, hello there, hon.",
            n(0, 0.9f), n(2, 0.8f), n(1, 1.1f)),
        // theatrical mad science: a rising arpeggio that lands
        // like a presentation
        "Mephesto" to p(0.26f, "Ah! A visitor! Excellent!",
            n(0, 0.6f), n(3, 0.6f), n(5, 0.8f), n(2, 1.1f)),
        // clipped cop bark: four short bursts, the last one
        // an order
        "Yates" to p(0.20f, "Alright. Who's in charge here?",
            n(0, 0.5f), n(0, 0.5f), n(1, 0.7f), n(0, 1.0f)),
        // the shop teacher's sigh, set to three weary notes
        "Adler" to p(0.44f, "Yeah. Hi. Watch the bandsaw.",
            n(0, 1.1f), n(-1, 1.0f), n(-1, 1.2f)),
        // the wok's welcome: bright, quick, already turning
        // back to the stove
        "Lu Kim" to p(0.20f, "Herro! Welcome to City Wok!",
            n(2, 0.5f), n(4, 0.5f), n(2, 0.6f), n(5, 1.0f)),
        // the widower's porch greeting: three slow notes,
        // the middle one lower, like a man setting something
        // down carefully
        "Roger" to p(0.50f, "Hey. Yeah — hi there.",
            n(0, 1.0f), n(-2, 1.0f), n(0, 1.4f)),
        // the chief's hail: two on the same note, then up —
        // a roll call of one
        "Ryan" to p(0.18f, "Morning! Chief Valmer!",
            n(4, 0.5f), n(4, 0.5f), n(7, 0.9f)),
        // front-row mom: a quick warm staircase, then the
        // step back down like settling into the seat
        "Sarah" to p(0.22f, "Oh! Hello, hon!",
            n(5, 0.5f), n(7, 0.5f), n(9, 0.8f), n(7, 1.0f)),
        // the planner's hello: two identical notes, minuted,
        // then the decision note
        "Alexis" to p(0.30f, "Hello. Mind the clipboard.",
            n(2, 0.7f), n(2, 0.7f), n(5, 1.1f)),
        // the quiet Testaburger: one long low note, one
        // slightly higher — the whole speech, in bark form
        "Mr. Testaburger" to p(0.40f, "Hey. …Hi.",
            n(0, 1.2f), n(3, 1.2f)),
        // the nemesis: flat, low, patient — the same cold
        // note twice, then the drop
        "Nathan" to p(0.35f, "Oh. It's you. Wonderful.",
            n(-3, 0.9f), n(-3, 0.9f), n(-5, 1.3f)),
        // the new girl: a bright little arc, up and back,
        // like a wave from a bus window
        "Sophie" to p(0.25f, "Oh — hi! I'm Sophie.",
            n(3, 0.5f), n(5, 0.6f), n(3, 0.6f), n(8, 1.0f)),
        // SUPER: four notes straight up the stairs, the
        // last one held like a pose
        "Big Gay Al" to p(0.15f, "I'm super! Thanks for asking!",
            n(7, 0.4f), n(9, 0.4f), n(12, 0.4f), n(12, 1.1f)),
        // ---- wave 6 (v0.11.0d): the audit's voices ----
        // Pip: a polite two-step, up, then a bow of a note
        "Pip" to p(0.30f, "Oh! Hello!",
            n(4, 0.5f), n(7, 0.5f), n(5, 0.9f)),
        // Dougie: small, bright, a salute in three hops
        "Dougie" to p(0.22f, "Oh — hi! I'm Dougie!",
            n(7, 0.45f), n(9, 0.45f), n(12, 0.45f), n(9, 0.85f)),
        // Jason: a referee's two beats and a whistle lift
        "Jason" to p(0.28f, "Jason! I'm in!",
            n(2, 0.4f), n(2, 0.4f), n(6, 0.9f)),
        // Terrance Mephesto: level, cold, a half-step down —
        // the rival's patience, not Nathan's (his drops lower)
        "Terrance Mephesto" to p(0.38f, "Terrance. Mephesto.",
            n(-2, 0.8f), n(-2, 0.8f), n(-4, 1.2f)),
        // Jenny: crisp, minuted — tick, tick, filed
        "Jenny" to p(0.26f, "Jenny Simons. Noted.",
            n(5, 0.5f), n(5, 0.5f), n(8, 0.9f)),
        // Flora: a tiny deciding arc, up and settled
        "Flora" to p(0.24f, "Hi! I'm Flora!",
            n(9, 0.5f), n(12, 0.5f), n(10, 0.9f)),
        // Filmore: a campaign fanfare of three rising notes
        "Filmore" to p(0.27f, "Filmore Anderson!",
            n(3, 0.45f), n(6, 0.45f), n(9, 0.95f)),
        // Quaid: the second chair echoes one beat behind
        "Quaid" to p(0.29f, "I'm Quaid!",
            n(6, 0.45f), n(6, 0.45f), n(3, 0.85f)),
        // Mimsy: big, slow, loyal — two low thumps and a lift
        "Mimsy" to p(0.40f, "I'm Mimsy.",
            n(-4, 0.7f), n(-4, 0.7f), n(-1, 1.1f)),
        // Tricia: quick, bright, a witness's shorthand
        "Tricia" to p(0.25f, "Tricia Tucker.",
            n(6, 0.4f), n(9, 0.4f), n(7, 0.55f), n(10, 0.85f)),
        // Mr. Slave: the bass line — two deep notes, held
        "Mr. Slave" to p(0.35f, "Well, hello.",
            n(-7, 0.9f), n(-5, 1.3f)),
        // Linda Black: warm and level, a welcome in two notes
        "Linda Black" to p(0.30f, "Linda Black. Come in.",
            n(1, 0.7f), n(4, 1.15f)),
        // Harriet: praise first — an up-note, then another
        "Harriet" to p(0.28f, "Harriet Biggle!",
            n(3, 0.5f), n(6, 0.5f), n(6, 0.9f)),
        // Mr. Biggle: the foundation — one low note, settled
        "Mr. Biggle" to p(0.42f, "Mr. Biggle.",
            n(-3, 1.0f), n(-1, 1.2f)),
        // Mr. Turner: the perimeter — a firm knock of notes
        "Mr. Turner" to p(0.33f, "Thomas Turner.",
            n(0, 0.5f), n(0, 0.5f), n(3, 1.0f)),
        // Nancy: the open gate — a swinging two-note welcome
        "Nancy" to p(0.26f, "Nancy Turner!",
            n(4, 0.45f), n(8, 0.45f), n(6, 0.9f)),
        // Mr. Stevens: quiet ballast — soft, even, brief
        "Mr. Stevens" to p(0.36f, "Mr. Stevens.",
            n(0, 0.7f), n(2, 1.0f)),
        // Mrs. Stevens: an arrival — a rising flourish
        "Mrs. Stevens" to p(0.24f, "Mrs. Stevens.",
            n(2, 0.4f), n(5, 0.4f), n(9, 0.4f), n(7, 0.9f)),
        // Mr. Stoley: a producer's cue — tap, tap, lights
        "Mr. Stoley" to p(0.31f, "Mr. Stoley.",
            n(1, 0.45f), n(1, 0.45f), n(5, 1.0f)),
        // Mrs. Stoley: the director's cue — softer, centered
        "Mrs. Stoley" to p(0.29f, "Mrs. Stoley.",
            n(3, 0.5f), n(1, 0.5f), n(5, 0.95f)),
        // Clark: three channels — three flat notes, decided
        "Clark" to p(0.34f, "Clark Malkinson.",
            n(-1, 0.5f), n(-1, 0.5f), n(2, 1.0f)),
        // Maggie: the kettle's calm — a low warm pair
        "Maggie" to p(0.32f, "Maggie Yates.",
            n(0, 0.6f), n(3, 1.1f)),
        // Mitch: the aide's stamp — two even thuds, filed
        "Mitch" to p(0.37f, "Detective Murphy.",
            n(-2, 0.55f), n(-2, 0.55f), n(0, 1.0f)),
        // Bob Johnson: an aide's brisk triple, dated and filed
        "Bob Johnson" to p(0.30f, "Bob Johnson.",
            n(2, 0.45f), n(4, 0.45f), n(2, 0.9f)),
        // Pusslicker: the anchor's pause — one note, a beat,
        // then the headline
        "Pusslicker" to p(0.45f, "Tom Pusslicker. Channel 4.",
            n(1, 0.8f), n(5, 1.2f)),
        // Dr. Doctor: dry and clinical — sit, then the story
        "Dr. Doctor" to p(0.39f, "Dr. Doctor.",
            n(-1, 0.6f), n(1, 1.1f)),
        // Choksondik: eyes front — a strict, level pair
        "Choksondik" to p(0.33f, "Ms. Choksondik.",
            n(2, 0.6f), n(2, 0.6f), n(4, 1.0f)),
        // Strong Woman: the standard — a strong rising pair,
        // held tall
        "Strong Woman" to p(0.27f, "Strong Woman.",
            n(0, 0.5f), n(4, 0.5f), n(7, 1.1f))
    )

    // ---------------- the household (v0.13.0a) ----------------
    // Mom and Dad's bark patterns live in their OWN table on
    // purpose: PATTERNS above is census-pinned to the 98 cast
    // (keys == CAST, 98 signatures), and the family is NOT
    // cast (Family.kt's header states the wall). The household
    // table follows the same shape rules — an original hello,
    // a rhythm signature distinct from every cast signature —
    // and Sound/SoundGen resolve through patternForAny, so a
    // parent barks with the same machinery, on their own
    // hash-derived blip voice, without ever entering the cast
    // table. The hello lines double as the first line of
    // Family's home-bark banks.
    val HOUSEHOLD_PATTERNS: Map<String, Pattern> = mapOf(
        // Mom: a warm staircase, unhurried — up, up, and the
        // landing note held a breath, like being glad in
        // three stages
        "Mom" to p(0.32f, "Well, look who's home.",
            n(0, 0.9f), n(2, 0.9f), n(4, 1.1f)),
        // Dad: a steady low pair, then the lift — a man
        // looking up from the porch project and being
        // pleased about what he sees
        "Dad" to p(0.38f, "Hey — the kid's home.",
            n(-2, 1.0f), n(-2, 0.9f), n(1, 1.1f))
    )

    fun patternFor(name: String): Pattern? = PATTERNS[name]

    /** Cast or household: the pattern any bark-capable name
     *  resolves to. Cast lookups are unchanged (PATTERNS is
     *  consulted first); only Mom and Dad resolve further. */
    fun patternForAny(name: String): Pattern? =
        PATTERNS[name] ?: HOUSEHOLD_PATTERNS[name]

    /** A pattern's total length in beats (notes + the gaps
     *  between them) — the render-length contract lives here. */
    fun totalBeats(pat: Pattern): Float =
        pat.notes.sumOf { it.len.toDouble() }.toFloat() +
            pat.gap * (pat.notes.size - 1).coerceAtLeast(0)

    /** The pattern's identity as one string — the suite's
     *  distinctness floor is that no two cast members share
     *  one of these. */
    fun signature(pat: Pattern): String =
        pat.notes.joinToString(",") { "${it.semi}:${it.len}" } +
            "|g${pat.gap}|e${if (pat.electronic) 1 else 0}"

    // ---------------- trigger rules ----------------

    /** Per-NPC cooldown, in in-game minutes: after someone
     *  barks at you, they won't again for a while, however
     *  often you re-enter their space. */
    const val COOLDOWN_MINUTES = 20

    /** A lingering player only sometimes gets hailed by a new
     *  arrival: 1-in-4 presence changes, before the other
     *  gates. Entry barks are not chance-gated — walking into
     *  a room is the moment a greeting is FOR. */
    const val ARRIVAL_CHANCE = 0.25

    /** The hello bubble's on-screen life. Long enough to read
     *  a short line, short enough to never become furniture. */
    const val BUBBLE_MS = 2200L

    /** Quiet hours: no barks at all. The town is asleep —
     *  most of it literally (the sleepers are excluded anyway);
     *  this covers the night owls. Matches the hours the tune
     *  goes to bed, roughly: late night through early morning. */
    const val QUIET_START = 22 * 60 + 30  // 22:30
    const val QUIET_END = 6 * 60 + 30    // 06:30

    /** The evening wind-down: barks soften from here until
     *  quiet hours begin. */
    const val EVENING_START = 19 * 60    // 19:00
    const val EVENING_SCALE = 0.6f

    /** Indoors, a greeting carries less — smaller rooms,
     *  closer walls, other people's conversations. */
    const val INDOOR_SCALE = 0.7f

    fun inQuietHours(timeMin: Int): Boolean =
        timeMin >= QUIET_START || timeMin < QUIET_END

    /** The volume a bark plays at, relative to the SFX
     *  channel: indoors softer, evening quieter, the two
     *  stack. Quiet hours never reach here (gated earlier),
     *  so this never returns 0. */
    fun volumeScale(inside: Boolean, timeMin: Int): Float {
        var s = 1f
        if (inside) s *= INDOOR_SCALE
        if (timeMin >= EVENING_START && timeMin < QUIET_START) s *= EVENING_SCALE
        return s
    }

    /** Has [name]'s cooldown elapsed by [nowAbsMin] (absolute
     *  in-game minutes)? Never barked = always ready. */
    fun cooledDown(lastAbsMin: Int?, nowAbsMin: Int): Boolean =
        lastAbsMin == null || nowAbsMin - lastAbsMin >= COOLDOWN_MINUTES

    /** Should a bark even be attempted right now? The master
     *  gate: the Settings toggle and the quiet hours. Presence,
     *  candidacy and cooldowns are checked by the caller. */
    fun gateOpen(enabled: Boolean, timeMin: Int): Boolean =
        enabled && !inQuietHours(timeMin)

    /** The whole greeting decision, pure (v0.4.0g): given the
     *  moment's facts and two uniform rolls in [0, 1), who
     *  barks — or null. GameActivity's maybeBark is a thin
     *  shell over this (it owns the cooldown map, the rolls,
     *  the sound, the bubble); the suite drives THIS over
     *  simulated years of entries, so the rules that ship
     *  are the rules that get measured. [present] is
     *  (name, friendship) for everyone awake here with a
     *  pattern; [newcomers] the subset who arrived while the
     *  player lingered; [cooldowns] name -> absMin of their
     *  last bark. Exactly the shipped flow: gate first; an
     *  ENTRY offers the whole room; a lingering ARRIVAL
     *  offers only the newcomers, and only when the arrival
     *  roll passes; one weighted pick; the cooldown vetoes
     *  the pick (it does not re-roll to a second choice). */
    fun decide(enabled: Boolean, timeMin: Int, entered: Boolean,
               present: List<Pair<String, Int>>, newcomers: Set<String>,
               cooldowns: Map<String, Int>, nowAbsMin: Int,
               arrivalRoll: Double, pickRoll: Double): String? {
        if (!gateOpen(enabled, timeMin)) return null
        val candidates = when {
            entered -> present
            newcomers.isNotEmpty() && arrivalRoll < ARRIVAL_CHANCE ->
                present.filter { it.first in newcomers }
            else -> return null
        }
        if (candidates.isEmpty()) return null
        val pick = pickBarker(candidates, pickRoll) ?: return null
        return if (cooledDown(cooldowns[pick], nowAbsMin)) pick else null
    }

    // ---------------- presence weighting ----------------

    /** How strongly a present NPC draws the greeting: your
     *  closest friends hail you; strangers mostly don't get
     *  picked over them. Weight = 1 + a point per 20
     *  friendship, floored at 1 (a rival still CAN bark —
     *  Craig will not be out-silenced). */
    fun weightOf(friendship: Int): Float =
        1f + (friendship.coerceAtLeast(0) / 20f)

    /** Weighted pick from [candidates] (name -> friendship)
     *  for a uniform [roll] in [0, 1). Heaviest first; the
     *  roll walks the cumulative weights. Deterministic in
     *  the roll — the suite pins the mapping; the caller
     *  supplies the randomness. */
    fun pickBarker(candidates: List<Pair<String, Int>>, roll: Double): String? {
        if (candidates.isEmpty()) return null
        val weighted = candidates.map { it.first to weightOf(it.second) }
            .sortedByDescending { it.second }
        val total = weighted.sumOf { it.second.toDouble() }
        var r = roll.coerceIn(0.0, 0.999999) * total
        for ((name, w) in weighted) {
            if (r < w) return name
            r -= w
        }
        return weighted.last().first
    }

    // ---------------- the hello bubble ----------------

    /** Where the bubble goes, in view pixels: [left, top,
     *  right, bottom]. It hangs above the figure's head top
     *  ([headTopY]) with a small gap, centered on [headCx] but
     *  clamped inside the view, and never rides up into the
     *  Activity's top chrome — when the figure stands high on
     *  screen the bubble pins to [chromeTop] + margin instead
     *  (the head-clearance layout means that path is a
     *  formality on real screens). Pure math; GameView feeds
     *  it a measured text width and the suite pins the
     *  invariants. */
    data class BubbleRect(val left: Float, val top: Float,
                          val right: Float, val bottom: Float) {
        val width: Float get() = right - left
        val height: Float get() = bottom - top
    }

    const val BUBBLE_PAD_X = 13f
    const val BUBBLE_H = 34f
    const val BUBBLE_GAP = 10f       // head top -> bubble bottom
    const val BUBBLE_EDGE = 6f       // min distance to a view edge
    const val BUBBLE_CHROME_GAP = 6f // min distance below the top chrome

    fun bubbleRect(headCx: Float, headTopY: Float, textW: Float,
                   viewW: Float, chromeTop: Float, fontScale: Float = 1f): BubbleRect {
        val h = BUBBLE_H * fontScale
        val w = (textW + 2 * BUBBLE_PAD_X * fontScale)
            .coerceAtMost(viewW - 2 * BUBBLE_EDGE)
        var left = headCx - w / 2
        left = left.coerceIn(BUBBLE_EDGE, (viewW - BUBBLE_EDGE - w).coerceAtLeast(BUBBLE_EDGE))
        var top = headTopY - BUBBLE_GAP - h
        val minTop = chromeTop + BUBBLE_CHROME_GAP
        if (top < minTop) top = minTop
        return BubbleRect(left, top, left + w, top + h)
    }
}
