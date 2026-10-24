package com.hunt3r.southparktown

// Mystery13.kt — v0.15.0j: Mystery Arc #13, the
// exhibition case. A structural sibling of Mystery12:
// it queues behind the twelfth case under the same
// rules — four chapters, the last the finale; a board
// of surfaced artifacts read against records the game
// already keeps; readings priced in live evidence;
// verdicts derived, never stored. The board here is
// the Community Hall's gallery wall after a wrapped
// show: six frames, audited. One print on that wall
// is not the print its frame's record says it is.
// The spine is the PROVENANCE CHAIN: every exhibited
// print carries its facts in three places at once —
// the ALBUM RECORD (the living source in the player's
// album), the DARKROOM COPY (the frozen record inside
// the print that was developed from it, when the
// frame is a lent print), and the FRAME LOG (the
// show's own stored entry: the copy the wall froze at
// entry time, under the entrant's name). The audit
// reads each frame three ways on every read — THE
// PRINT (what the hanging piece IS, its record
// against its source copy), THE CHAIN (where it CAME
// FROM: album to darkroom to wall, every link named
// and resolving), and THE FRAME (what its FRAME
// claims: the entrant the wall credits, against the
// entrant the records support, and the mount the
// audit found it hanging on). A reading is committed
// through the Calls machinery pattern, graded by the
// frame's one settling piece, and re-derived forever
// from facts: the board stores fields, never
// conclusions. The case's truth — which frame's piece
// was quietly replaced, and whose hands replaced it —
// is SEALED: it lives only in the sealed fields below
// and in post-reveal copy. The suite's spoiler wall
// stands over everything public.

object Mystery13 {

    const val CASE_TITLE = "Not What the Frame Says"
    const val FINALE13_ID = "arc13_ch4_the_wall_read_back"
    const val CHAPTER_GAP_DAYS = 7
    const val OPEN_DELAY_DAYS = 10
    const val POST_DELAY_DAYS = 7

    val CHOICE_KEYS = listOf("read", "name", "stand", "walk")
    val BRANCH_KEYS = listOf("read", "named", "standing", "unread")
    const val DEFAULT_BRANCH = "unread"
    val CALL_KEYS = listOf("true", "mistake", "swapped")

    // ---------------- the chapters ----------------
    // Ch1: the wall as hung — a wrapped show's wall is
    // ordered read against its own log, and two frames
    // in the champion's row won't sit still under the
    // reading. Ch2: the chain of copies — the darkroom
    // end of the provenance: the print box, the sleeves,
    // and the hands a photograph passes through on its
    // way to a wall. Ch3: the rehang — the whole wall
    // comes down and goes back up in public, and the
    // town's theory hardens on the kid who hung it.
    // Ch4: the wall, read back — the audit read aloud
    // at the hall, and the town decides what a frame
    // is worth.
    val CHAPTERS13: List<ShenaniganDef> = listOf(
        ShenaniganDef(
            id = "arc13_ch1_the_wall_as_hung",
            title = "The Wall as Hung",
            cause = "A wrapped show's wall, read against its own log — and two frames won't match it.",
            blurb = "The Gallery Show came down the way shows do: placements argued, the winner's line printed, the prints gone home. What stayed behind is the wall's paperwork — and the jury, doing its due diligence, has ordered the wall AUDITED: every frame in the champion's row read three ways, against the album record, the darkroom copy and the frame log itself. Most frames will read clean. The audit exists because two of them already don't.",
            theme = QuestTheme("🖼", "#6a8caf", "THE WALL AS HUNG"),
            tintHex = "#6a8caf",
            causers = listOf("Victoria", "Mephesto"),
            episodeTitles = listOf("Six Frames, Three Records",
                "The Audit Opens"),
            coldOpen = listOf(
                Line("Victoria", "The show is wrapped, the wall is down, and the log is a public record. An audit is not an accusation. It is a reading, in the open, of what the wall said it was."),
                Line("Mephesto", "Three records for every frame, and in my experience at least one of them is always lying politely. We shall see which.")),
            stages = listOf(
                ShenaniganStage("The Log Book Opens",
                    "🖼 THE WALL AS HUNG: the show's frame log, opened on the hall table — six frames in the champion's row, entered for the audit.",
                    "Every frame the show hung carries a line in the log: who entered it, what photograph it claims, which print was lent for it. The audit reads the log first, because the log is what the wall SAID. Whether the wall was telling the truth is the rest of the case.",
                    "Read the frame log at the Community Hall.",
                    routine = "cluster", clusterLoc = "CityHall",
                    clusterActivity = "Reading the frame log, line by line.",
                    tone = "whisper",
                    factionEffects = listOf(FactionEffect("parents", 1))),
                ShenaniganStage("The Wall, Remembered",
                    "🖼 The hall is empty and the frames are stacked — but everybody in town walked that wall for a week.",
                    "A wall that hung for seven days lives in a hundred memories, and memories disagree about everything except what surprised them. The audit collects the surprises first.",
                    "Talk to the people who saw the wall up.",
                    routine = "cluster", clusterLoc = "CityHall",
                    clusterActivity = "Remembering the wall, frame by frame.",
                    tone = "whisper",
                    factionEffects = listOf(FactionEffect("parents", 1))),
                ShenaniganStage("The Ladder's Foot Speaks",
                    "🖼 THE WALL AS HUNG: hanging a wall takes two people — one on the ladder, one at its foot, holding it honest.",
                    "The kid who held the ladder has been shaking since the notices went up, which everyone agrees is normal for him and proves nothing either way. The audit hears him anyway. A ladder's foot sees the underside of everything.",
                    "Hear the ladder's foot out at the hall.",
                    routine = "cluster", clusterLoc = "CityHall",
                    clusterActivity = "Holding an invisible ladder, out of habit.",
                    tone = "whisper",
                    factionEffects = listOf(FactionEffect("craigs_gang", 1))),
                ShenaniganStage("The Audit Is Open",
                    "🖼 Six frames, three records apiece, one wall that may not have been the wall in the log. The audit is open.",
                    "The jury has ruled the audit's shape: each frame read three ways — THE PRINT, THE CHAIN, THE FRAME — in the open, before anyone is allowed to say the word 'stolen' in the hall again.",
                    "The exhibition case is open.",
                    routine = "normal",
                    factionEffects = listOf(FactionEffect("parents", 1))))),
        ShenaniganDef(
            id = "arc13_ch2_the_chain_of_copies",
            title = "The Chain of Copies",
            cause = "The audit walks the provenance chain backwards — from the wall to the darkroom tray.",
            blurb = "A photograph on a wall has traveled: out of the album, through the darkroom, into a frame. Every hand it passed through left the chain a link richer or a link poorer. The audit walks the chain backwards this week — the print box, the negative sleeves, and the people who can say what a copy is worth — because a frame is only ever as honest as the road it took to the wall.",
            theme = QuestTheme("🧪", "#6a8caf", "THE CHAIN OF COPIES"),
            tintHex = "#6a8caf",
            causers = listOf("Mephesto", "Craig"),
            episodeTitles = listOf("Album, Tray, Wall",
                "Every Link Named"),
            coldOpen = listOf(
                Line("Mephesto", "A print is a rumor a photograph tells about itself. The darkroom is where we check the rumor's sources."),
                Line("Craig", "I hung what was handed to me, in the order on the list. That's the whole statement. It's going to stay the whole statement.")),
            stages = listOf(
                ShenaniganStage("The Print Box, Opened",
                    "🧪 THE CHAIN OF COPIES: the darkroom's print box, opened under the audit — every developed print the wall ever borrowed, or should have.",
                    "Prints live in the box between their lives: developed, lent, gifted, hung at home. The box keeps them in their sleeves with their records inside. What the box holds — and what it should hold — is the chain's middle link, read plain.",
                    "Open the print box in the darkroom.",
                    routine = "cluster", clusterLoc = "YourHouse",
                    clusterActivity = "Reading the print box, sleeve by sleeve.",
                    tone = "whisper",
                    factionEffects = listOf(FactionEffect("parents", 1))),
                ShenaniganStage("The Sleeve Drawer",
                    "🧪 THE CHAIN OF COPIES: before a print is a print, it's a sleeve in a drawer — the darkroom's memory of everything it ever developed.",
                    "The sleeves are the darkroom's own log: one per sitting, facts on the card. A photograph with no sleeve was never developed here. A sleeve with no print is a story the audit wants told carefully.",
                    "Read the sleeve drawer in the darkroom.",
                    routine = "cluster", clusterLoc = "YourHouse",
                    clusterActivity = "Filing sleeves that were never out of order.",
                    tone = "whisper",
                    factionEffects = listOf(FactionEffect("craigs_gang", 1))),
                ShenaniganStage("The Hanger, Heard",
                    "🧪 THE CHAIN OF COPIES: the kid who hung the wall gives the audit his whole account — a list, a ladder, an evening.",
                    "His account is short enough to be suspicious and consistent enough to be true, which the town has decided is the same thing. The audit writes it down whole and prices it against the ladder's own marks.",
                    "Hear the hanger out at the hall.",
                    routine = "cluster", clusterLoc = "CityHall",
                    clusterActivity = "Explaining a list, a ladder, an evening.",
                    tone = "whisper",
                    factionEffects = listOf(FactionEffect("craigs_gang", 1))),
                ShenaniganStage("The Chain, Priced",
                    "🧪 Half the wall's chain reads clean. The other half is why there is an audit.",
                    "Every link the audit has priced is in the record now: which photographs resolve, which prints answer to their names, and which frame's road to the wall still has a gap in it that nobody has explained.",
                    "Take stock at the hall.",
                    routine = "normal",
                    factionEffects = listOf(FactionEffect("parents", 1))))),
        ShenaniganDef(
            id = "arc13_ch3_the_rehang",
            title = "The Rehang",
            cause = "The whole wall, down and up again in public — and the town finds its suspect.",
            blurb = "The jury has ordered the wall rehung in the open: every frame down, every print handled, every mount read, the wall back up under the town's eye. A public rehang turns up what a log can't hold — the weight of a frame, the freshness of a back — and gives the town a name to be wrong about, in the shape of the one kid who hung the wall in the first place.",
            theme = QuestTheme("🔨", "#6a8caf", "THE REHANG"),
            tintHex = "#6a8caf",
            causers = listOf("Victoria", "Craig"),
            episodeTitles = listOf("Everything Off the Wall",
                "The Kid with the Ladder"),
            coldOpen = listOf(
                Line("Victoria", "On Friday the wall comes down and goes back up in front of everyone. If the wall has an author besides its entrants, the rehang will leave them nowhere to stand."),
                Line("Craig", "A public rehang. Great. In my experience, the only thing a public rehang proves quickly is who the room already suspects.")),
            stages = listOf(
                ShenaniganStage("The Wall Comes Down",
                    "🔨 THE REHANG: six frames off their hooks, handled one at a time, in front of the whole hall.",
                    "Every frame the audit covers is down in the light now, being read on both sides by a dozen hands at once. Whatever the wall has been hiding, it's hiding it in plain sight.",
                    "Join the rehang at the Community Hall.",
                    routine = "cluster", clusterLoc = "CityHall",
                    clusterActivity = "Taking a wall down, in public.",
                    tone = "hype",
                    factionEffects = listOf(FactionEffect("parents", 1))),
                ShenaniganStage("The Ladder Comes Up",
                    "🔨 THE REHANG: it takes the room about an hour to start saying one name — the kid who hung the wall, ladder, list and all.",
                    "He did hang it. Everyone knows it; he has never once hidden it. The question the audit has to answer is whether a crossed pair of frames is the same thing as a replaced print — or just the easiest shape in the room.",
                    "Hear the ladder's owner out at the hall.",
                    routine = "cluster", clusterLoc = "CityHall",
                    clusterActivity = "Explaining the ladder, to a jury of neighbors.",
                    tone = "hype",
                    factionEffects = listOf(FactionEffect("craigs_gang", 1))),
                ShenaniganStage("What the Rehang Can't Settle",
                    "🔨 THE REHANG: two frames still won't read — their evidence is all in hand, and their meaning still depends on company it hasn't had yet.",
                    "Some records only settle in combination: a hang list read whole, a print handled in the reading's own week. The rehang ends with the audit heavier and two frames still warm.",
                    "Take stock at the hall.",
                    routine = "cluster", clusterLoc = "CityHall",
                    clusterActivity = "Reading two frames for the twentieth time.",
                    tone = "whisper",
                    factionEffects = listOf(FactionEffect("parents", 1))),
                ShenaniganStage("The Reading Is Announced",
                    "🔨 The audit will be read aloud at the hall — every frame, all three checks, in the open. One week.",
                    "The jury has set the day. Whatever the wall is — honest hanging, honest error, or something quieter — it will be read in front of the whole town, and the town will have to decide what to do with what it hears.",
                    "The reading is coming.",
                    routine = "normal",
                    factionEffects = listOf(FactionEffect("parents", 1))))),
        ShenaniganDef(
            id = FINALE13_ID,
            title = "The Wall, Read Back",
            cause = "The audit, read aloud — and the town decides what a frame is.",
            blurb = "The whole hall, one audit, six frames. Read them truly and the town finally learns which piece on its wall was never the piece in the record — and whose hands put it there. Read them halfway and the room will settle for the name it already has warmed up — the kid with the ladder, who has been carrying the town's suspicion all week on the strength of having hung the wall in the open.",
            theme = QuestTheme("📖", "#6a8caf", "THE WALL, READ BACK"),
            tintHex = "#6a8caf",
            causers = listOf("Victoria", "Mephesto"),
            episodeTitles = listOf("Six Frames, Read Aloud",
                "The Piece That Wasn't"),
            coldOpen = listOf(
                Line("Victoria", "The audit is complete, the hall is seated, and I will remind everyone once: we are reading FRAMES. Whoever they belong to when we're done is a decision, not a discovery."),
                Line("Mephesto", "I examined the copies. Whatever the audit says the wall is — I'll stand by the reading. The light in this hall hides nothing twice.")),
            stages = listOf(
                ShenaniganStage("The Hall Takes Its Seats",
                    "📖 THE WALL, READ BACK: the whole hall, seated, for six frames and the roads they took to the wall.",
                    "Every person in the room walked past at least one of these frames while it hung. The audit holds the print, the chain and the frame of each — derived, checked, and ready to be said out loud.",
                    "Be at the hall for the reading.",
                    routine = "cluster", clusterLoc = "CityHall",
                    clusterActivity = "Seated, with a program nobody printed.",
                    tone = "whisper",
                    factionEffects = listOf(FactionEffect("parents", 1))),
                ShenaniganStage("The Frames Go Round Once More",
                    "📖 The six frames make one last circuit of the room — so that whatever is decided is decided with the evidence in everyone's hands.",
                    "The pair that hung crossed. The piece that came out of the tray too clean. The log that swears to all of it. The room is holding all of it now.",
                    "Let the frames finish their circuit.",
                    routine = "cluster", clusterLoc = "CityHall",
                    clusterActivity = "Passing a frame along, quietly.",
                    tone = "whisper",
                    factionEffects = listOf(FactionEffect("parents", 1))),
                ShenaniganStage("The Audit Is Read",
                    "📖 THE WALL, READ BACK: print, chain and frame — six frames, read aloud, in order.",
                    "This is the moment the case was built for: the audit, complete, said where everyone can hear it — or the room settling for the name it brought with it.",
                    "Make the call while the hall is listening.",
                    routine = "cluster", clusterLoc = "CityHall",
                    clusterActivity = "Listening to the audit, read aloud.",
                    tone = "whisper",
                    factionEffects = listOf(FactionEffect("parents", 1))),
                ShenaniganStage("What a Frame Is Worth",
                    "📖 It's over. The frames have been read, and the town knows what hung on its wall — and in whose frames.",
                    "However it landed, the wall goes back up tomorrow, the log goes back to its honest work, and the town files the week under things that weren't what their frames said.",
                    "The exhibition case is closed.",
                    routine = "normal",
                    factionEffects = listOf(FactionEffect("parents", 1)))))
        )


    // ---------------- the audited show ----------------
    // The case is the show's shadow: it arms only on a
    // show that has actually RUN — wrapped, with a wall
    // of at least six frames over six distinct
    // photographs, and the player somewhere in it (a
    // frame of their own entered, the hanging kept, or
    // a visit paid). A save that never exhibited has no
    // frame log to contradict, so the case waits —
    // season after season if it must — rather than
    // fabricate a wall. The audit is BOOKED on the most
    // recent qualifying show (its wall is the one still
    // standing in the town's memory), and the booking
    // pins the show's id into the save at the moment
    // the queue anchor is set: later shows hang and
    // wrap around the case without ever moving its
    // wall.
    fun showQualifies13(show: GalleryShow): Boolean =
        show.wrapped && show.entries.size >= 6 &&
            show.entries.map { it.photoId }.distinct().size >= 6 &&
            (show.attendedHanging || show.visitedShow ||
                show.entries.any { it.entrant == Gallery.YOU })

    fun qualifyingShow13(state: GameState): GalleryShow? =
        state.galleryShows.filter { showQualifies13(it) }
            .lastOrNull()

    fun auditedShow13(state: GameState): GalleryShow? {
        if (state.arc13ShowId.isNotEmpty())
            return state.galleryShows.firstOrNull {
                it.id == state.arc13ShowId }
        return qualifyingShow13(state)
    }

    // ---------------- the audit layer ----------------
    // The audit's own recorded observations of the
    // PHYSICAL wall — what the examiners found hanging,
    // as opposed to what the log says hangs. Six
    // frames, in wall order. Five of them the audit
    // finds exactly where the log left them. Two hang
    // CROSSED: each holds the other's photograph, the
    // classic hanging-night error, provable because
    // the records on both sides of the swap are the
    // game's real ones. One hangs SUBSTITUTED: the
    // photograph is the photograph — every record
    // agrees about the picture — but the physical
    // piece is a fresh printing wearing the lent
    // original's frame, and the mount says so.
    val MOUNT_TELLS13: List<String> = listOf(
        "box-fresh stock, still smelling of the tray",
        "no hang shadow on the back")

    data class Audit13(
        val kind: String,          // "none" | "cross" | "substitute"
        val partner: Int,          // cross partner's board index
        val mountFacts: List<String>
    )

    val AUDIT13: List<Audit13> = listOf(
        Audit13("none", -1, listOf(
            "an even hang shadow across the back",
            "the log's pencil tick on the mount")),
        Audit13("cross", 2, listOf(
            "an even hang shadow across the back",
            "the frame's label in the hanger's pencil")),
        Audit13("cross", 1, listOf(
            "an even hang shadow across the back",
            "the frame's label in the hanger's pencil")),
        Audit13("none", -1, listOf(
            "hang shadow, lighter at one corner",
            "the log's pencil tick on the mount")),
        Audit13("substitute", -1, listOf(
            "box-fresh stock, still smelling of the tray",
            "no hang shadow on the back",
            "mount corners unmarked by the hook")),
        Audit13("none", -1, listOf(
            "an even hang shadow across the back",
            "the log's pencil tick, double-struck"))
    )

    // Per-frame chapter gates for committing a reading,
    // in wall order — the audit's own pacing.
    val FRAME_MIN_CHAPTER13: List<Int> =
        listOf(1, 2, 1, 1, 2, 1)

    // ---------------- the board ----------------
    // A frame on the audited wall: the show's real
    // entry (entrant, kind, the photo it names, the
    // print it lent, the record the log froze), read
    // against the audit layer above. Everything the
    // board prints about a frame's RECORD comes from
    // the game's own persisted copies — the frame log,
    // the album, the print box — recomputed live on
    // every read.
    data class Frame13(
        val id: String,
        val index: Int,
        val entry: GalleryEntry,
        val auditKind: String,
        val partner: Int,
        val mountFacts: List<String>,
        val minChapter: Int,
        val callPrice: Int = 2
    ) {
        val entrant: String get() = entry.entrant
        val kind: String get() = entry.kind
        val photoId: String get() = entry.photoId
        val printId: String get() = entry.printId
        val proofBy: String get() = SETTLER13[index]
    }

    // The one settling piece per frame, in wall order.
    val SETTLER13: List<String> = listOf(
        "log_first_page", "hang_list_whole", "ladder_marks",
        "negative_sleeve", "rehang_hands", "frame_backs")

    fun boardFrames13(state: GameState): List<Frame13> {
        val show = auditedShow13(state) ?: return emptyList()
        val seen = mutableSetOf<String>()
        val picked = mutableListOf<GalleryEntry>()
        for (e in show.entries) {
            if (e.photoId in seen) continue
            seen += e.photoId
            picked += e
            if (picked.size >= 6) break
        }
        if (picked.size < 6) return emptyList()
        return picked.mapIndexed { i, e ->
            Frame13(
                id = "frame_${i + 1}", index = i, entry = e,
                auditKind = AUDIT13[i].kind,
                partner = AUDIT13[i].partner,
                mountFacts = AUDIT13[i].mountFacts,
                minChapter = FRAME_MIN_CHAPTER13[i])
        }
    }

    fun frameById13(state: GameState, id: String): Frame13? =
        boardFrames13(state).firstOrNull { it.id == id }

    // A photograph's identity, as the records keep it:
    // where, when, in what weather, who stood in it and
    // how. Two copies describe the same photograph when
    // every one of these agrees.
    fun samePhoto13(a: PhotoRecord, b: PhotoRecord): Boolean =
        a.locId == b.locId && a.inside == b.inside &&
            a.weatherId == b.weatherId && a.absDay == b.absDay &&
            a.timeMin == b.timeMin &&
            a.composition == b.composition &&
            a.figures.map { "${it.name}|${it.kind}|${it.pose}|${it.reaction}" } ==
                b.figures.map { "${it.name}|${it.kind}|${it.pose}|${it.reaction}" }

    fun recordLabel13(rec: PhotoRecord): String {
        val place = LOCATIONS[rec.locId]?.name ?: rec.locId
        val names = rec.npcs.map { it.name }
        val who = when {
            names.isEmpty() -> "a solo frame"
            names.size == 1 -> names[0]
            names.size == 2 -> "${names[0]} and ${names[1]}"
            else -> "${names[0]}, ${names[1]} and ${names.size - 2} more"
        }
        return "$place — $who (${rec.dateLabel})"
    }

    fun frameLabel13(frame: Frame13): String =
        "FRAME ${frame.index + 1} · " +
            (if (frame.entrant == Gallery.YOU) "your frame"
            else "${frame.entrant}'s frame") + " · " +
            recordLabel13(frame.entry.record)

    // ---------------- the three checks, derived ----------------
    // Recomputed from the game's real records and the
    // audit layer on EVERY read, never stored.
    // THE PRINT reads the hanging piece (the audit's
    // observed record: the frame's own log copy — or,
    // for a crossed frame, the photograph the audit
    // found in it, which is the partner frame's real
    // log copy) against the piece's SOURCE copy: the
    // darkroom print's frozen record for a lent print,
    // the live album record for the rest. A source that
    // has faded out of the album doesn't convict the
    // print — it breaks the CHAIN, which is the next
    // check's business.
    // THE CHAIN walks album → darkroom → wall: the
    // album record must exist; a lent print must exist
    // in the box, name this photograph, and carry the
    // album's own facts; a cast frame's entrant must
    // actually stand in (or have commissioned) the
    // photograph the album holds.
    // THE FRAME reads the wall's claim: the entrant
    // the frame credits, against the entrant the
    // records support — and the mount the audit found,
    // whose tells a fresh printing cannot hide.
    fun albumRec13(state: GameState, frame: Frame13): PhotoRecord? =
        state.photos.firstOrNull { it.id == frame.photoId }

    fun printRec13(state: GameState, frame: Frame13): DarkroomPrint? =
        if (frame.kind != Gallery.KIND_PRINT) null
        else state.prints.firstOrNull { it.id == frame.printId }

    fun sourceCopy13(state: GameState, frame: Frame13): PhotoRecord? =
        if (frame.kind == Gallery.KIND_PRINT)
            printRec13(state, frame)?.record
        else albumRec13(state, frame)

    fun observedRec13(state: GameState, frame: Frame13): PhotoRecord {
        if (frame.auditKind == "cross" && frame.partner >= 0) {
            val frames = boardFrames13(state)
            val p = frames.getOrNull(frame.partner)
            if (p != null) return p.entry.record
        }
        return frame.entry.record
    }

    fun castStanding13(rec: PhotoRecord, entrant: String): Boolean =
        rec.commissionAsker == entrant || rec.figures.any { f ->
            f.name == entrant && f.kind != "player" &&
                f.reaction != Photos.PHOTOBOMB }

    data class Findings13(
        val printState: String,   // "match" | "differ" | "gone"
        val crossed: Boolean,
        val chainOk: Boolean,
        val frameOk: Boolean,
        val mountTells: List<String>
    ) {
        val printOk: Boolean get() = printState != "differ"
    }

    fun findings13(state: GameState, frame: Frame13): Findings13 {
        val observed = observedRec13(state, frame)
        val src = sourceCopy13(state, frame)
        val printState = when {
            src == null -> "gone"
            samePhoto13(observed, src) -> "match"
            else -> "differ"
        }
        var crossed = false
        if (frame.auditKind == "cross" && frame.partner >= 0) {
            val frames = boardFrames13(state)
            val partner = frames.getOrNull(frame.partner)
            val ownSrc = src
            val partnerSrc = partner?.let {
                sourceCopy13(state, it) }
            if (partner != null && ownSrc != null &&
                partnerSrc != null &&
                !samePhoto13(observed, ownSrc) &&
                samePhoto13(observed, partnerSrc)) crossed = true
        }
        val album = albumRec13(state, frame)
        val chainOk = when {
            album == null -> false
            frame.kind == Gallery.KIND_PRINT -> {
                val p = printRec13(state, frame)
                p != null && p.photoId == frame.photoId &&
                    samePhoto13(p.record, album)
            }
            frame.kind == "cast" ->
                castStanding13(album, frame.entrant)
            else -> samePhoto13(album, frame.entry.record)
        }
        val frameOk = when (frame.kind) {
            "cast" -> album != null &&
                castStanding13(album, frame.entrant)
            else -> frame.entrant == Gallery.YOU
        }
        return Findings13(
            printState = printState,
            crossed = crossed,
            chainOk = chainOk,
            frameOk = frameOk,
            mountTells = frame.mountFacts.filter {
                it in MOUNT_TELLS13 })
    }

    // The audit's reading of a frame, derived whole:
    // SWAPPED when the photograph is right and the
    // piece is new — the records agree about the
    // picture and the mount convicts the printing;
    // TRUE HANGING when the print matches its source,
    // the chain resolves and the frame's credit is the
    // records' own; HONEST CROSS when the piece in the
    // frame is another frame's photograph, or when a
    // link in the chain has honestly failed — a
    // crossed hanging, a faded source, a filing the
    // records can't carry. Nothing else exists: a
    // frame always reads one of the three.
    fun shape13(state: GameState, frame: Frame13): String {
        val f = findings13(state, frame)
        return when {
            frame.auditKind == "substitute" &&
                f.mountTells.isNotEmpty() && f.printOk -> "swapped"
            f.crossed -> "mistake"
            frame.auditKind == "cross" -> "mistake"
            f.printOk && f.chainOk && f.frameOk -> "true"
            else -> "mistake"
        }
    }

    fun shapeLabel13(shape: String): String = when (shape) {
        "true" -> "TRUE"
        "mistake" -> "CROSSED"
        else -> shape.uppercase()
    }

    // A reading's display text — what the call MEANS,
    // printed wherever a committed reading shows.
    fun readingText13(call: String): String = when (call) {
        "true" -> "TRUE HANGING — the piece in the frame " +
            "is the piece in the record: print, chain and " +
            "credit all reading the same way"
        "mistake" -> "HONEST CROSS — the records are " +
            "honest and the hanging wasn't: the piece in " +
            "the frame is another photograph's, or its " +
            "road to the wall honestly broke"
        "swapped" -> "SWAPPED PRINT — the photograph is " +
            "the photograph in every record, and the " +
            "piece itself is a fresh printing: somebody " +
            "replaced the original and kept the frame's face"
        else -> call
    }

    // ---------------- the evidence ----------------
    // Twelve pieces: five carried by witnesses, five
    // found by looking, two worked by the player's own
    // hands inside chapter climaxes. Two pieces per
    // frame; one of the two settles it. The settling
    // pieces of the two load-bearing frames are
    // context-locked: their text exists from the moment
    // they're held, but it only SAYS anything live in
    // the company of a later chapter — the hang list
    // reads whole only once the rehang is underway,
    // and the swapped frame's truth is worked by hand
    // only in the rehang itself, telling its story in
    // the reading's own week. Context is a kind of
    // provenance.
    enum class Kind13 { WITNESS, LOOK, PARTICIPATION }

    data class Clue13(
        val id: String,
        val kind: Kind13,
        val minChapter: Int,
        val site: Int,
        val proof: Boolean,           // settles its frame
        val unlockChapter: Int = 0,   // > minChapter: context-locked
        val title: String = "",
        val detail: String = "",
        val inertDetail: String = detail,
        val npc: String? = null,      // WITNESS: who carries it
        val spoken: String? = null,   // WITNESS: their telling
        val locId: String? = null,    // LOOK: where it surfaces
        val foundText: String? = null, // LOOK: the Look beat
        val chapterDefId: String? = null // PARTICIPATION: the beat
    )

    val CLUES13: List<Clue13> = listOf(
        // --- frame one (site 0): the clean frame ---
        Clue13("log_first_page", Kind13.LOOK, 0, 0, true,
            title = "The frame log's first page",
            detail = "THE PRINT and THE CHAIN: the log's first page, read against the album in the open. The frame log's copy of the first frame's photograph agrees with the album record in every particular the records keep — place, weather, who stood in it and how — and the chain under it resolves link by link: album, copy, wall. This is what a true hanging looks like on paper. The audit keeps the page as its yardstick: six frames were promised this. The audit intends to count how many got it.",
            locId = "CityHall",
            foundText = "🖼 THE LOG'S FIRST PAGE: the first frame, read against the album — every particular agreeing."),
        Clue13("ladder_foot", Kind13.WITNESS, 0, 0, false,
            title = "The ladder's foot, on hanging night",
            detail = "THE FRAME: his telling is all underside. From the foot of the ladder the wall went up in the log's order, one frame at a time, called up and ticked off — the first frame he remembers clearest, because the evening was still young and nobody was tired yet. Whatever went wrong on that wall, on his telling, it didn't go wrong at the start of the night.",
            npc = "Tweek",
            spoken = "I HELD THE LADDER. That's the whole job — you hold it and you DON'T let go and you watch the frames go up one at a time, in the list's order, and the first ones went up PERFECT, man, I was RIGHT THERE. It's the LATER part of the night you want to ask about, and I was SHAKING TOO HARD TO— okay, that's not helping. GAAAH."),
        // --- frame two (site 1): load-bearing, crossed ---
        Clue13("hang_list_whole", Kind13.LOOK, 1, 1, true,
            unlockChapter = 2,
            title = "The hang list, read whole",
            detail = "THE PRINT: the hanger's working list, read as one document against the wall as hung. The list hung the champion's row in the log's order — except at one adjacent pair, where the list's own pencil does something the rest of the page never does: it numbers them, crosses the numbers out, and numbers them again, the other way up. Read beside the two frames' records, the wall agrees with the RENUMBERED list, not the log: each of the pair holds the other's photograph, exactly, record for record. A crossed hanging, in the hanger's own hand, on the night. No third hand touched the pair after — the hang shadows on both backs are even and unbroken.",
            inertDetail = "THE HANG LIST: the hanger's working list, complete — every frame of the row, in order, with pencil all over it, which is a chore to read and says nothing yet. A list is just a list until the wall it hung is standing in the same room being taken down. For now the list is just a list.",
            locId = "CityHall",
            foundText = "🖼 THE HANG LIST: one adjacent pair, numbered twice in pencil — the second numbering the other way up."),
        Clue13("hanger_on_the_cross", Kind13.WITNESS, 2, 1, false,
            title = "The hanger, on the crossed pair",
            detail = "THE FRAME: his telling puts the cross where the ladder marks will later agree it lives — late in the hanging, the light going, two frames of a size on the same hook row, and a list he read the way tired people read: confidently, and once. He offers the audit the fact he has clearly been carrying since: he noticed the pair hung heavy-side-reversed at the time, decided it was the frames' shapes playing tricks, and said nothing, because saying something meant another hour on the ladder.",
            npc = "Craig",
            spoken = "Fine. Yes. The pair by the door: I hung them the way the list's numbers landed after I re-did the numbers, and the list was smudged, and I was four hours up a ladder. If two photographs spent a week in each other's frames, that's on me. It's the ONLY thing on me. Write that down too."),
        // --- frame three (site 2): crossed, the other half ---
        Clue13("ladder_marks", Kind13.LOOK, 2, 2, true,
            title = "The pencil marks on the ladder rail",
            detail = "THE FRAME: the ladder the wall was hung from keeps its own log — a pencil tick on the rail for every frame that went up, in the order they went up, made by the foot at the ladder's base out of pure nerves. Read against the hang list, the rail's ticks agree with the renumbered pair: the two frames went up in the swapped order, once, and the ladder never went back to that row all night. Whatever the pair is, it was done once, in the open, at hanging height — not twice, and not in the dark.",
            locId = "CityHall",
            foundText = "🖼 THE LADDER RAIL: a tick per frame, in order — and the pair's ticks land the way the renumbered list says."),
        Clue13("pair_handled", Kind13.PARTICIPATION, 1, 2, false,
            title = "The pair's prints, handled under the lamp",
            detail = "THE PRINT: the crossed pair's pieces, in your own hands under the darkroom lamp during the chain week. Both are honest printings of their own photographs — the paper right, the backs marked with the hanging's weather, the wear of a real week on a real wall. Nothing about either piece suggests it was anywhere but the wall it hung on. The cross is a hanging error and only a hanging error: two true prints, each in the other's frame, exactly as their records describe them — once you read each record against the OTHER frame's log line.",
            chapterDefId = "arc13_ch2_the_chain_of_copies"),
        // --- frame four (site 3): the clean chain ---
        Clue13("negative_sleeve", Kind13.LOOK, 0, 3, true,
            title = "The negative sleeve, in the drawer",
            detail = "THE CHAIN: the darkroom's sleeve for this frame's sitting, card and all. The sleeve's facts are the album record's facts in the darkroom's own hand — same sitting, same frame of the roll, nothing developed from it that the sleeve doesn't know about. Read against the album and the frame log, this frame's chain is the audit's second yardstick: album to sleeve to print to wall, every link present, every link named. A chain CAN be kept whole. This wall kept exactly one of them that way, and the audit has the sleeve to prove the standard exists.",
            locId = "YourHouse",
            foundText = "🧪 THE SLEEVE DRAWER: one sitting's sleeve, facts matching the album line for line — a whole chain, kept."),
        Clue13("chain_keeper", Kind13.WITNESS, 1, 3, false,
            title = "The chain's keeper, on links",
            detail = "THE CHAIN: his telling is arithmetic. A print is its photograph plus its road: album, darkroom, frame — and a road can be checked at every stop by anybody willing to walk it. He walked this frame's road himself during the show, out of the suspicion that is his whole personality, and found every stop keeping the same facts. His conclusion, offered at volume: if the audit is hunting a broken road, it is hunting somewhere else, and he would like that in writing.",
            npc = "Kyle",
            spoken = "I CHECKED this one myself during the show — album, print, wall, every stop, because SOMEBODY should. It's clean. It's so clean it's boring. So when the audit finds the frame whose road DOESN'T check out, I want it noted that I called the method, not the frame. There's a difference."),
        // --- frame five (site 4): load-bearing, substituted ---
        Clue13("light_test", Kind13.WITNESS, 2, 4, false,
            title = "The examiner's light test",
            detail = "THE PRINT: his telling is about paper, not pictures. During the show he examined the wall the way he examines everything — against the light. Five of the audited frames wore the week the way paper wears a week: dust in the grain, the faintest curl at the mount. One frame's piece, on his telling, drank the light like it had been printed that morning — because, he says carefully, in his professional opinion as a man who has grown things in dishes, freshness is a fact and facts are all he is offering.",
            npc = "Mephesto",
            spoken = "I am a scientist. I do not say a print is new; I say its paper has not lived the week the others have. Five frames aged. One frame… glistened. Draw the audit's own conclusions; I merely hold the lamp."),
        Clue13("rehang_hands", Kind13.PARTICIPATION, 2, 4, true,
            unlockChapter = 3,
            title = "The swapped frame, handled at the rehang",
            detail = "THE PRINT and THE FRAME: the piece, in your own hands at the rehang, out of its frame under the hall's lights. The photograph is the photograph in every record the game keeps — album, darkroom copy, frame log, all describing this picture and no other. The PIECE is wrong. The stock is box-fresh, smelling of the tray; the back carries no hang shadow, though the frame's other five pieces wear a full week's; the mount's corners have never met the hook. And the original it replaces is accounted for by the box: the print this frame names never came home from the show — because it never went. What hung in its frame for seven days was a reprinting, run off after the hanging, by hands that had the run of the darkroom's stock and the wall's trust. The records were never lied to. The records were simply never asked about the paper.",
            inertDetail = "A FRAME, HANDLED: at the rehang the frame comes down into your hands like the other five — glass, mount, a print of its photograph, weight unremarkable. Whatever it proves can't be read off it in a crowd with the wall coming down around it. Some pieces only tell their story in the reading's own company. It goes back on the pile, unweighed.",
            chapterDefId = "arc13_ch3_the_rehang"),
        // --- frame six (site 5): the clean credit ---
        Clue13("frame_backs", Kind13.LOOK, 1, 5, true,
            title = "The frame backs' labels",
            detail = "THE FRAME: the backs of the wall's frames, read as one document. Each frame's back carries its label — entrant, photograph, the log's line — in the hanging's own pencil, and five of the six agree with the log exactly. This frame's label is the audit's third yardstick: the entrant the wall credits is the entrant the album record supports, standing in their own photograph (or holding its commission slip), and the double-struck tick on the mount says the checker checked it twice. A frame CAN credit truly. The audit holds this one up whenever the room starts claiming none of them can.",
            locId = "CityHall",
            foundText = "🖼 THE FRAME BACKS: six labels in the hanging's pencil — and this one, checked twice, agreeing entirely."),
        Clue13("guest_book", Kind13.WITNESS, 1, 5, false,
            title = "The guest book's keeper, on the wall's week",
            detail = "THE FRAME: her telling is the wall's public life. The guest book she kept at the hall door holds the week's traffic in the town's own hands — who came, who lingered, who stood in front of which frame the longest. This frame's week, on her record, was exactly the week its label promised: its entrant photographed beside it by three separate visitors, pleased as punch, telling the story of taking it. Whatever the wall did wrong, the town watched this frame be right about it for seven days.",
            npc = "Wendy",
            spoken = "I kept the guest book all week — every visitor, every comment, in their own writing. This frame's entrant stood next to it in three different people's photos, glowing. You don't glow like that next to a picture that isn't yours. Whatever the audit finds on that wall, it doesn't find it here.")
    )

    val CLUE13_BY_ID: Map<String, Clue13> =
        CLUES13.associateBy { it.id }
    val CLUE13_IDS: Set<String> = CLUES13.map { it.id }.toSet()

    fun clueCount13(state: GameState): Int =
        state.mystery13Clues.count { it in CLUE13_IDS }

    // A piece is AVAILABLE once its chapter has begun
    // (arc13Chapter counts chapters begun, like its
    // siblings). LOOK and WITNESS pieces wait for their
    // places and people; PARTICIPATION pieces wait for
    // their chapter's own climax machinery.
    fun clueAvailable13(state: GameState, clue: Clue13): Boolean {
        if (!state.arc13Started || state.arc13Retired) return false
        if (clue.id in state.mystery13Clues) return false
        return state.arc13Chapter > clue.minChapter
    }

    // A piece is LIVE once held and its context has
    // arrived — the context-locked pieces sit inert in
    // the hand until their chapter begins.
    fun clueLive13(state: GameState, clue: Clue13): Boolean =
        clue.id in state.mystery13Clues &&
            state.arc13Chapter > clue.unlockChapter

    fun clueText13(clue: Clue13, live: Boolean): String =
        if (live) clue.detail else clue.inertDetail

    // Grant a held piece: the events feed and the town
    // log carry it, in the arcs' wording — live pieces
    // announce themselves; locked ones announce only
    // that they are in hand.
    fun grantClue13(state: GameState, id: String): Boolean {
        val clue = CLUE13_BY_ID[id] ?: return false
        if (id in state.mystery13Clues) return false
        state.mystery13Clues.add(id)
        val live = clueLive13(state, clue)
        state.events.add(if (live)
            "🧩 EVIDENCE ${state.mystery13Clues.size}/${CLUES13.size}: ${clue.title}. " +
                "(Journal → 🔍 MYSTERY)"
        else
            "🧩 IN HAND, NOT YET READABLE (${state.mystery13Clues.size}/${CLUES13.size}): " +
                "${clue.title}. It doesn't make sense yet — its context is still " +
                "coming, and in this case context is a kind of provenance.")
        while (state.events.size > 40) state.events.removeAt(0)
        townLogLine(state, if (live) "🧩 Evidence proven: ${clue.title}"
            else "🧩 Evidence in hand, not yet readable: ${clue.title}")
        return true
    }

    // WITNESS: TalkGate calls this when the carrier is
    // spoken to; a witness tells once, in a Line.
    fun witnessLine13(state: GameState, npcName: String): Line? {
        for (clue in CLUES13) {
            if (clue.kind != Kind13.WITNESS) continue
            if (clue.npc != npcName) continue
            if (!clueAvailable13(state, clue)) continue
            if (!grantClue13(state, clue.id)) continue
            return Line(npcName, clue.spoken ?: "")
        }
        return null
    }

    // LOOK: the Look action at a location surfaces the
    // earliest available piece waiting there.
    fun lookClueText13(state: GameState, locId: String): String? {
        for (clue in CLUES13) {
            if (clue.kind != Kind13.LOOK) continue
            if (clue.locId != locId) continue
            if (!clueAvailable13(state, clue)) continue
            grantClue13(state, clue.id)
            return clue.foundText
        }
        return null
    }

    // PARTICIPATION: Engine's participation hook calls
    // this as a chapter's stages advance — every
    // available piece tied to the running chapter is
    // worked (and granted) by the player's own hands.
    fun noteParticipation13(state: GameState, defId: String) {
        for (clue in CLUES13) {
            if (clue.kind != Kind13.PARTICIPATION) continue
            if (clue.chapterDefId != defId) continue
            if (!clueAvailable13(state, clue)) continue
            grantClue13(state, clue.id)
        }
    }


    // ---------------- readings & verdicts ----------------
    // The Calls machinery pattern, applied to frames:
    // a reading is committed per frame at a price of
    // live evidence, graded by the frame's settling
    // piece, and its verdict re-derived on every read.
    fun liveEvidence13(state: GameState, frame: Frame13): Int =
        CLUES13.count { it.site == frame.index &&
            clueLive13(state, it) }

    fun liveHeld13(state: GameState): Int =
        CLUES13.count { clueLive13(state, it) }

    // A frame can be read once its chapter has come
    // and its live evidence covers the price.
    fun frameCallable13(state: GameState, frame: Frame13): Boolean =
        state.arc13Started && !state.arc13Retired &&
            state.arc13Chapter > frame.minChapter &&
            liveEvidence13(state, frame) >= frame.callPrice

    fun pendingFrame13(state: GameState): Frame13? {
        for (frame in boardFrames13(state)) {
            val call = state.arc13Calls[frame.id]
            if (call != null &&
                frameVerdict13(state, frame) != "REFUTED") continue
            if (frameCallable13(state, frame)) return frame
        }
        return null
    }

    fun committedCall13(state: GameState, frame: Frame13): String? =
        state.arc13Calls[frame.id]?.takeIf { it in CALL_KEYS }

    // DERIVED, like every verdict before it: OPEN (no
    // standing reading), COMMITTED (read, ungraded),
    // PROVEN (read, and the settling piece — held live —
    // confirms the derived shape), REFUTED (read against
    // what the settling piece proves: strikeable).
    fun frameVerdict13(state: GameState, frame: Frame13): String {
        val call = committedCall13(state, frame) ?: return "OPEN"
        val proof = CLUE13_BY_ID[frame.proofBy] ?: return "COMMITTED"
        if (!clueLive13(state, proof)) return "COMMITTED"
        return if (call == shape13(state, frame)) "PROVEN" else "REFUTED"
    }

    fun chooseCall13(state: GameState, frameId: String,
                     call: String): Boolean {
        val frame = frameById13(state, frameId) ?: return false
        if (call !in CALL_KEYS) return false
        if (!frameCallable13(state, frame)) return false
        val standing = committedCall13(state, frame)
        if (standing != null &&
            frameVerdict13(state, frame) != "REFUTED") return false
        state.arc13Calls[frame.id] = call
        return true
    }

    fun provenCount13(state: GameState): Int =
        boardFrames13(state).count {
            frameVerdict13(state, it) == "PROVEN" }

    fun boardCalled13(state: GameState): Boolean {
        val frames = boardFrames13(state)
        return frames.size == 6 &&
            frames.all { committedCall13(state, it) != null }
    }

    fun boardClean13(state: GameState): Boolean =
        boardFrames13(state).none {
            frameVerdict13(state, it) == "REFUTED" }

    // ---------------- the finale: the wall, read back ----------------
    // The reading stands due when the finale chapter is
    // airing, its climax stage has come, and every
    // frame carries a reading. The full read is the
    // gated call: both load-bearing frames PROVEN, the
    // whole board read, nothing standing refuted, and
    // at least four of the six proven besides — the
    // returns case's ratio, carried over whole.
    fun verdictDue(state: GameState): Boolean {
        if (state.arc13Retired) return false
        if (state.arc13Choice.isNotEmpty()) return false
        if (state.shenaniganId != FINALE13_ID) return false
        if (Shenanigans.stageFor(state.timeMin) < 2) return false
        return boardCalled13(state)
    }

    fun finishEligible13(state: GameState): Boolean {
        val frames = boardFrames13(state)
        if (frames.size < 6) return false
        return frameVerdict13(state, frames[1]) == "PROVEN" &&
            frameVerdict13(state, frames[4]) == "PROVEN" &&
            boardCalled13(state) && boardClean13(state) &&
            provenCount13(state) >= 4
    }

    fun nameEligible13(state: GameState): Boolean =
        provenCount13(state) >= 1

    fun chooseFinale13(state: GameState, choice: String): Boolean {
        if (choice !in CHOICE_KEYS) return false
        if (!verdictDue(state)) return false
        if (choice == "read" && !finishEligible13(state)) return false
        if (choice == "name" && !nameEligible13(state)) return false
        state.arc13Choice = choice
        return true
    }

    data class VerdictOutcome(val branch: String, val outcome: String,
                              val recapLines: List<String>)

    // At the wrap the gates are checked AGAIN — a choice
    // whose proof collapsed between the lock and the
    // close lands degraded, never upgraded.
    fun resolveVerdict(state: GameState): VerdictOutcome {
        val choice = state.arc13Choice
        val complete = boardCalled13(state)
        val branch = when {
            choice == "read" && complete && finishEligible13(state) ->
                "read"
            choice == "name" && complete && nameEligible13(state) ->
                "named"
            choice == "stand" && complete -> "standing"
            else -> DEFAULT_BRANCH
        }
        state.arc13Branch = branch
        state.arc13Retired = true
        state.arc13RetiredDay = state.absDay
        return when (branch) {
            "read" -> VerdictOutcome(
                "read",
                "📖 THE WALL, READ BACK: the audit is read aloud, all six frames, and the two that wouldn't read read loudest of all. The crossed pair is a hanging error — list, ladder and light all agree, in the hanger's own pencil, and he stands up in front of the whole hall and takes it like a fence post. The fifth frame is another matter: right photograph, wrong paper — a reprinting, run off fresh, hung in the lent original's place by hands that wanted the original itself. Randy Marsh doesn't deny it. He'd been offered real money for an 'original champion-wall print, certified,' and the reprint was, in his words, 'a victimless duplication, since the picture was IDENTICAL.' The original comes back out of a Tegridy gift box. The hanger is entered into the record as a witness, which he accepts as the nicest thing the hall has ever said about him.",
                listOf("🖼 The wall was read: one honest cross, one quiet swap — and the hand behind the swap has a name at last.",
                    "🖼 The ladder stands cleared in the same record that names the hand."))
            "named" -> VerdictOutcome(
                "named",
                "📖 THE WALL, READ BACK: the room does what rooms do — it settles for the name it brought with it. The kid with the ladder. He stands up, in front of the whole hall, and says the sentence that will follow him to graduation: I hung what was handed to me, in the order on the list, and the list is available for inspection. It is a very good sentence. It changes nothing. The case closes on the easiest shape in the room.",
                listOf("🖼 The exhibition case closed on a name — the ladder's owner, cleared by nothing but his own list.",
                    "🖼 The wall goes back up. One piece on it was never explained to anyone's satisfaction but the room's."))
            "standing" -> VerdictOutcome(
                "standing",
                "📖 THE WALL, READ BACK: you let the wall stand. The frames stay as they hung, the crossed pair stays crossed, the fresh piece stays fresh, and the town decides — by saying nothing, together — that a frame is a frame, and asking whose print it holds is the kind of question that loses friends. The jury chair closes the log with visible relief and writes nothing in it.",
                listOf("🖼 The wall stands, unread and uncorrected, and the hall keeps its secrets by acclamation.",
                    "🖼 Somewhere on that wall, one new piece of paper goes on being the oldest-looking thing in the room."))
            else -> VerdictOutcome(
                "unread",
                "📖 THE WALL, READ BACK: the day ends with the audit unopened. The hall files the wall under weather — a strange fortnight, best not examined — and the frames go back up with six small mysteries riding their backs. Whatever the wall was, it is the log's business now.",
                listOf("🖼 The wall was never read back. The log keeps its own counsel, and the town keeps walking past it."))
        }
    }

    fun applyVerdictEffects(state: GameState, branch: String) {
        fun ripple(factionId: String, delta: Int, note: String) {
            val fs = state.factionStates[factionId] ?: return
            fs.influence = (fs.influence + delta).coerceIn(0, 100)
            if (delta != 0) {
                fs.moodScore = (fs.moodScore + if (delta > 0) 1 else -1).coerceIn(-2, 2)
            }
            if (note.isNotEmpty()) {
                fs.mood = note
                state.events.add("⚔️ FACTION SHIFT: $note")
                while (state.events.size > 40) state.events.removeAt(0)
            }
        }
        fun befriend(name: String, delta: Int) {
            state.friendship[name] =
                ((state.friendship[name] ?: 0) + delta).coerceAtLeast(0)
        }
        when (branch) {
            "read" -> {
                befriend("Craig", 3)
                befriend("Victoria", 1)
                val frames = boardFrames13(state)
                val swapped = frames.getOrNull(4)
                if (swapped != null &&
                    swapped.entrant != Gallery.YOU)
                    befriend(swapped.entrant, 2)
                ripple("craigs_gang", 1,
                    "Craig's Gang watched a full hall hear their ladder-man cleared by the records themselves — a list, a ladder rail, a lamp — and the one quiet swap was neither of the things the room had guessed. The Gang is revising its opinion of paperwork. The Gang revises opinions the way other people change shirts: rarely, and all at once.")
                ripple("parents", -1,
                    "The Parents came for a theft and got a provenance audit instead. Half the Parents are re-telling it already. They're telling the paper part wrong, but they're telling it.")
                state.arc13PostPending = true
                state.arc13PostDay = state.absDay + POST_DELAY_DAYS
                state.lifeText["mark:CityHall"] = "🖼 The wall was " +
                    "read back aloud here — one honest cross, one " +
                    "quiet swap — and the hall's door still wears " +
                    "the reading's ring. (the exhibition case)"
                townLogLine(state, "🖼 READ BACK: the audit was " +
                    "read at the hall. No print was swapped on a " +
                    "stage. The wall was read against its records, " +
                    "in public, and the checks held up.")
            }
            "named" -> {
                befriend("Craig", -4)
                befriend("Victoria", 1)
                ripple("parents", 1,
                    "The Parents got their name at the reading, before the bell, in the record, spelled right. The Parents are satisfied. The Parents call the objection a detail, a little, in private.")
                ripple("craigs_gang", -1,
                    "Craig's Gang keeps the record of the naming in a hand slightly tighter than usual. One ladder in this town keeps a different record — a tick for every frame, in order, in pencil — and it is available for inspection. It has always been available for inspection. None of it was ever hung on a wall.")
                state.lifeText["mark:CityHall"] = "🖼 The wall was " +
                    "closed by name here — the ladder's, over his " +
                    "standing objection. (the exhibition case)"
                townLogLine(state, "🖼 NAMED: the reading closed on " +
                    "the ladder's owner. His objection is on file. " +
                    "It was always going to be on file.")
            }
            "standing" -> {
                befriend("Mephesto", 1)
                befriend("Victoria", 1)
                ripple("parents", 1,
                    "The Parents are relieved the hall asked nothing. A frame is a frame, the Parents say, at pickup, in the tone of people closing a door.")
                state.lifeText["mark:CityHall"] = "🖼 The wall was " +
                    "left standing here, unread and uncorrected, " +
                    "by the hall's own silence. (the exhibition case)"
                townLogLine(state, "🖼 STANDING: the wall stands " +
                    "as it hung. The log keeps its frames, and " +
                    "the hall keeps its peace.")
            }
            else -> {
                befriend("Craig", -2)
                ripple("parents", -1,
                    "The Parents heard the reading never happened and have decided not to wonder about it, together, out loud, several times.")
                state.lifeText["mark:CityHall"] = "🖼 The wall was " +
                    "never read back here; the log keeps its " +
                    "counsel by the hall door. (the exhibition case)"
                townLogLine(state, "🖼 UNREAD: the audit shut " +
                    "unopened. The wall goes back up with six " +
                    "small mysteries riding its frames.")
            }
        }
    }

    // The delayed tail of the full read, and only the
    // full read: a week on, the wall is rehung true —
    // the crossed pair uncrossed, the swapped frame
    // holding its own original again at last.
    fun noteWallCorrected(state: GameState) {
        if (!state.arc13PostPending) return
        if (state.arc13Posted) return
        if (state.absDay < state.arc13PostDay) return
        state.arc13Posted = true
        state.arc13PostPending = false
        state.events.add("🖼 A QUIET REHANG: the wall at the hall is " +
            "true this morning — the crossed pair hangs each in " +
            "its own frame, and the fifth frame holds its own " +
            "original again, back from a gift box, with a plain " +
            "unsigned card that just says RETURNED. No freshness " +
            "on either piece that anybody would bother to study.")
        while (state.events.size > 40) state.events.removeAt(0)
        townLogLine(state, "🖼 The audited wall was quietly " +
            "rehung true — every frame holding its own print " +
            "at last.")
        state.lifeText["mark:CityHall"] = "🖼 A week after the " +
            "reading, the audited wall was quietly rehung true " +
            "here — the pair uncrossed, the original home in " +
            "its frame, with a plain card in nobody's studied " +
            "hand. (the exhibition case)"
    }

    // ---------------- scheduling ----------------
    // The same posture as the twelve cases before it:
    // the case QUEUES ten days behind the twelfth
    // case's retirement, never starts over its open
    // body, chapters pace a week apart, and a holiday
    // defers the chapter without repricing the queue.
    // One gate is this case's own: the audit needs its
    // wall. Until a qualifying show has wrapped, the
    // queue waits unanchored — the anchor (and the
    // audited show's booking) is set on the first day
    // both the twelfth case is retired and a wall
    // stands ready to be read.
    fun dueChapter13(state: GameState): ShenaniganDef? {
        if (state.arc13Retired) return null
        if (!state.arc12Retired) return null
        if (state.arc13Chapter >= CHAPTERS13.size) return null
        if (state.arc13DueAbsDay < 0) {
            val show = qualifyingShow13(state) ?: return null
            val anchor = if (state.arc12RetiredDay >= 0)
                state.arc12RetiredDay else state.absDay
            state.arc13ShowId = show.id
            state.arc13DueAbsDay = maxOf(state.absDay,
                anchor + OPEN_DELAY_DAYS)
        }
        if (state.absDay < state.arc13DueAbsDay) return null
        if (state.holidayId != null) return null
        return CHAPTERS13[state.arc13Chapter]
    }

    fun chapterIndexOf13(id: String): Int =
        CHAPTERS13.indexOfFirst { it.id == id }

    fun noteChapterStarted13(state: GameState, def: ShenaniganDef) {
        val idx = chapterIndexOf13(def.id)
        if (idx < 0) return
        state.arc13Started = true
        state.arc13Chapter = idx + 1
        state.arc13DueAbsDay = if (idx == CHAPTERS13.size - 1) -1
            else state.absDay + CHAPTER_GAP_DAYS
    }


    // ---------------- the Journal board ----------------

    fun boardVisible13(state: GameState): Boolean =
        state.arc13Started

    fun statusStamp13(state: GameState): String = when {
        state.arc13Retired -> when (state.arc13Branch) {
            "read" -> "📖 READ BACK ALOUD"
            "named" -> "📖 CLOSED BY NAME"
            "standing" -> "🖼 LEFT STANDING"
            else -> "🖼 NEVER READ"
        }
        verdictDue(state) -> "📖 THE READING IS DUE"
        pendingFrame13(state) != null -> "🖼 A FRAME IS READABLE"
        state.arc13Started -> "🖼 THE WALL AUDIT"
        else -> "🖼 —"
    }

    // The town's current theory of the wall — the
    // loudest reading in the hall, which is data about
    // the town, not the case.
    fun currentTheory13(state: GameState): String {
        if (state.arc13Retired) return when (state.arc13Branch) {
            "read" -> "the room knows now: one pair hung " +
                "crossed by tired hands, and one print was " +
                "quietly replaced by a man with a buyer — " +
                "the records separated them in an afternoon"
            "named" -> "the ladder did it — or so the room " +
                "decided; his objections are on file, in his " +
                "own pencil, under 'inspection'"
            "standing" -> "a frame is a frame; the hall has " +
                "agreed, silently and together, not to ask paper"
            else -> "the wall did it, or the weather did; nobody " +
                "was asked, and nobody answered"
        }
        return when {
            state.arc13Chapter <= 1 -> "the hanger crossed the " +
                "whole wall, start to finish; the ladder did it"
            state.arc13Chapter == 2 -> "the darkroom did it — " +
                "copies everywhere, nobody can tell anything " +
                "from anything, which is what copies are FOR"
            state.arc13Chapter == 3 -> "the kid with the ladder " +
                "hung a fake and the town should say so; the " +
                "room has its name already"
            else -> "everything is read except what the rehang " +
                "couldn't settle; the hall is saving its " +
                "voice for the reading"
        }
    }

    fun forecastLine13(state: GameState): String {
        if (state.arc13Retired) return ""
        val bits = ArrayList<String>()
        val pending = pendingFrame13(state)
        if (pending != null) bits += "a frame is ready to be " +
            "read (${frameLabel13(pending).lowercase()})"
        val held = CLUES13.filter {
            it.id in state.mystery13Clues && !clueLive13(state, it) }
        if (held.isNotEmpty()) bits += "${held.size} held " +
            "piece(s) settle when their context comes"
        if (state.shenaniganId == FINALE13_ID &&
            !boardCalled13(state))
            bits += "the reading can't be called until every " +
                "frame carries a reading"
        if (state.arc13DueAbsDay > state.absDay)
            bits += "the next chapter is booked for " +
                "day ${state.arc13DueAbsDay}"
        return if (bits.isEmpty()) "" else "• next: " +
            bits.joinToString("; ") + "."
    }

    fun boardLines13(state: GameState): List<String> {
        val out = ArrayList<String>()
        if (!boardVisible13(state)) return out
        val show = auditedShow13(state)
        val frames = boardFrames13(state)
        out += "🖼 CASE #13 — “${CASE_TITLE.uppercase()}”"
        out += statusStamp13(state)
        if (!state.arc13Retired)
            out += "A print on the wrapped show's wall is not " +
                "the print its frame's record says it is. The " +
                "audit reads each frame three ways: THE PRINT " +
                "(what the hanging piece IS, against its source " +
                "copy), THE CHAIN (where it CAME FROM — album, " +
                "darkroom, wall) and THE FRAME (what its frame " +
                "CLAIMS — the credited entrant, and the mount " +
                "it hangs on). The checks are derived off the " +
                "game's own records every time this board is " +
                "opened — the board keeps facts; it has never " +
                "once kept a conclusion."
        if (show != null)
            out += "• the audited wall: the ${Gallery.seasonLabel(
                Season.valueOf(show.season))} show of ${show.year} — " +
                "“${Gallery.themeOf(show)?.title ?: ""}”, " +
                "${show.entries.size} frames in the log, " +
                "six under audit."
        val heldCount = CLUES13.count { clueLive13(state, it) }
        val heldTotal = state.mystery13Clues.count {
            it in CLUE13_IDS }
        out += "• evidence held: $heldCount live of $heldTotal " +
            "held (${CLUES13.size} in the case). A reading is " +
            "priced at 2 LIVE pieces bearing on the frame — " +
            "a piece whose context hasn't come is in the hand, " +
            "but it isn't live."
        for (frame in frames) {
            val f = findings13(state, frame)
            val e = frame.entry
            out += ""
            out += "${frame.index + 1}. ${frameLabel13(frame)}"
            out += "   the frame log claims: entered by " +
                (if (e.entrant == Gallery.YOU) "you" else e.entrant) +
                " as a " + when (e.kind) {
                    Gallery.KIND_PRINT -> "lent darkroom print"
                    "cast" -> "cast entry"
                    else -> "album entry"
                } + " — ${recordLabel13(e.record)}."
            out += if (f.printState == "match")
                "   • THE PRINT: matches its source copy — " +
                    "the piece in the frame describes the same " +
                    "photograph as the record it answers to."
            else if (f.printState == "gone")
                "   • THE PRINT: its source has left the " +
                    "album — the frame log's copy is the only " +
                    "one still standing, and it convicts nobody."
            else
                "   • THE PRINT: DIFFERS from its source " +
                    "copy — the piece in this frame describes " +
                    "a different photograph than the one its " +
                    "record answers to."
            if (f.crossed)
                out += "     the piece in this frame is, " +
                    "record for record, the photograph in " +
                    "FRAME ${frame.partner + 1}'s log line — " +
                    "and that frame holds this one's."
            out += if (f.chainOk)
                "   • THE CHAIN: resolves — album, darkroom " +
                    "and wall each name the next, and the " +
                    "facts agree at every stop."
            else
                "   • THE CHAIN: broken — a link the records " +
                    "can't carry: the album's copy is gone, " +
                    "the print doesn't answer, or the entrant " +
                    "doesn't stand where the chain says."
            out += if (f.frameOk)
                "   • THE FRAME: credits truly — the entrant " +
                    "the wall names is the entrant the records " +
                    "support."
            else
                "   • THE FRAME: credits falsely — the wall " +
                    "names an entrant the records don't support."
            out += "     the mount, as the audit found it: " +
                frame.mountFacts.joinToString("; ") + "."
            if (f.mountTells.isNotEmpty())
                out += "     a fresh printing's tells ride " +
                    "this mount: ${f.mountTells.joinToString(", ")}"
            out += "   • THE AUDIT'S SHAPE, DERIVED: " +
                "${shapeLabel13(shape13(state, frame))} SHAPED"
            for (clue in CLUES13) {
                if (clue.site != frame.index) continue
                if (clue.id !in state.mystery13Clues) continue
                out += if (clueLive13(state, clue))
                    "   🔎 ${clue.title}: ${clue.detail}"
                else
                    "   🔒 ${clue.title}: held — its context " +
                        "hasn't arrived yet, and in this case " +
                        "context is a kind of provenance."
            }
            val call = committedCall13(state, frame)
            val verdict = frameVerdict13(state, frame)
            when {
                call == null -> {
                    val live = liveEvidence13(state, frame)
                    if (frameCallable13(state, frame))
                        out += "   → ready to be read: it can be " +
                            "committed at $live live pieces."
                    else if (state.arc13Chapter <= frame.minChapter)
                        out += "   → its chapter hasn't come."
                    else
                        out += "   → reading it takes " +
                            "${frame.callPrice} live pieces " +
                            "bearing on it; $live in hand."
                }
                verdict == "PROVEN" ->
                    out += "   ✅ PROVEN: ${readingText13(call)}"
                verdict == "REFUTED" ->
                    out += "   💔 REFUTED — strike it and read " +
                        "again: ${readingText13(call)}"
                else ->
                    out += "   📌 COMMITTED (ungraded): " +
                        readingText13(call)
            }
        }
        out += ""
        out += "🤔 town theory: ${currentTheory13(state)}"
        if (!state.arc13Retired) {
            val fc = forecastLine13(state)
            if (fc.isNotEmpty()) out += fc
            out += "THE READING'S TERMS, PUBLISHED: the full " +
                "read requires both load-bearing frames — " +
                "the crossed pair's lead and the substituted " +
                "frame — PROVEN, every frame read, none " +
                "standing refuted, and at least four of the " +
                "six proven. Naming the hand takes one proven " +
                "frame. Standing by the wall, or walking " +
                "away, takes nothing but the nerve."
        } else {
            out += when (state.arc13Branch) {
                "read" -> "🏁 closed: read back aloud — the " +
                    "pair uncrossed, the original home in its " +
                    "frame, and the ladder cleared in the same " +
                    "breath that named the hand."
                "named" -> "🏁 closed: by name — the ladder's " +
                    "owner carries the case, and his objection " +
                    "is filed where he always said it would be: " +
                    "in the open."
                "standing" -> "🏁 closed: left standing — the " +
                    "frames keep their wall, and the hall " +
                    "keeps its peace, and the log keeps " +
                    "everything else."
                else -> "🏁 closed: never read — the audit " +
                    "shut with its six frames inside it."
            }
            if (state.arc13PostPending)
                out += "• a week on, somebody still owes the " +
                    "audited wall a quiet rehang."
            if (state.arc13Posted)
                out += "• and a week on, the rehang came: " +
                    "the pair uncrossed, the original home " +
                    "at last, with a plain card in nobody's " +
                    "studied hand."
        }
        return out
    }

    // ---------------- aftermath & ambient ----------------
    // Standing lines: the ending stays in one person's
    // mouth at the hall, whichever way it landed.
    private val AFTERMATH13: Map<String, Map<String, List<String>>> =
        mapOf(
            "read" to mapOf("Victoria" to listOf(
                "For the record: the log was never wrong. The log is a record of what the wall SAID. Walls, unlike records, can be reached by anyone with a ladder and a quiet hour.",
                "The audit is filed, the wall is true, and the log has gained one page: the reading itself. Records accrete. That is what they are for.")),
            "named" to mapOf("Craig" to listOf(
                "The list remains available for inspection. It has always been available for inspection. I am having that printed on a card. People can read IT in the hall.",
                "Phase one of my plan was never 'swap a print and take the fall for the swap.' I keep my work on the ladder. The hall is welcome to check — that's still the difference.")),
            "standing" to mapOf("Mephesto" to listOf(
                "The wall still hangs, the pieces still glisten or don't, and nobody asks a thing where it came from. As a scientist, I find the arrangement… efficient.",
                "I still hold the lamp when asked. If a piece looks a little fresh, well — I am not the paper police. I merely note the freshness, in writing, in triplicate.")),
            "unread" to mapOf("Butters" to listOf(
                "Gee, I sure hope that wall's okay. Nobody ever did say what was wrong with it, and now I get nervous walkin' past the hall, fellas.",
                "Everybody says just enjoy the pictures. JUST ENJOY THEM. It's not ABOUT the pictures, it's about the PRINCIPLE of the pictures! ...It's a little about the pictures."))
        )

    fun aftermathLine13(state: GameState, npcName: String): Line? {
        if (!state.arc13Retired) return null
        val bank = AFTERMATH13[state.arc13Branch]
            ?: AFTERMATH13[DEFAULT_BRANCH] ?: return null
        val lines = bank[npcName] ?: return null
        if (lines.isEmpty()) return null
        val idx = ((state.absDay % lines.size) + lines.size) % lines.size
        return Line(npcName, lines[idx])
    }

    // The ending drifts into ordinary talk at the
    // hall, the way every ending in this town does.
    fun ambientCallback13(state: GameState,
                          random: kotlin.random.Random): String? {
        val banks = when (state.arc13Branch) {
            "read" -> listOf(
                "Turns out one pair hung crossed and one print was swapped for a copy nobody could tell from the wall. The hall checks paper now. The hall has STANDARDS now.",
                "The original went home and the copy went home and nobody new framed either. That's what a correction looks like when it's done right.")
            "named" -> listOf(
                "They closed the exhibition case on the ladder kid. He keeps saying the list's available for inspection. Honestly? A guilty kid would've burned the list.",
                "He wrote his objection on the back of a frame and hung it ON the wall. Filed. In his own pencil. You have to respect the system, even when it's eating him.")
            "standing" -> listOf(
                "The hall let the wall stand. The pair's still crossed, one piece is still too fresh, and everyone's agreed that's just how it is now.",
                "Ask anybody whose print is in the fifth frame and watch the conversation change the subject itself.")
            else -> listOf(
                "The exhibition case? Never did get read. The wall is still up at the hall. Frames still hang there. Nobody asks.",
                "Six frames and not one read aloud. In this town, that's practically a filing system.")
        }
        return banks[random.nextInt(banks.size)]
    }
}
