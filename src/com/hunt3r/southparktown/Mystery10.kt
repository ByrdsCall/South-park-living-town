package com.hunt3r.southparktown

import kotlin.random.Random

// ============================================================
// Mystery10 — the tenth case (v0.12.0f, Queue #12 #6):
// "The Photograph Nobody Took".
//
// The pond case was witness-driven, the booth case a
// compressed whodunit, the buried case an excavation, the
// gardener case followed acts of maintenance, the
// confession case was built of claims, the opinion case
// was a meter, the backwards case was a timeline, the
// alibi case was a graph, and the audit case was a ledger.
// This one's shape is a COMPARISON. The town's photographs
// are records now: every album frame and darkroom print
// carries its place, its day and who was in it, frozen at
// the shutter — and the town has learned to read them the
// way it learned to read its books, which is to say,
// completely, and at each other.
//
// For the Elementary's anniversary, the photo club mounts
// "The Town in Photographs": framed prints loaned from the
// town's albums and walls, hung in the school hall. Its
// centerpiece is a group portrait whose penciled caption
// names a committee, a place and a day. Hung beside the
// yearbook archive's own print of the same sitting, the
// two photographs disagree — the loaned print holds SIX
// figures; the sitting's own record holds FIVE. The town
// names its culprit at cape speed: the Bomber — the face
// at the edge of half the town's frames has finally, the
// town says, graduated from sneaking into photographs to
// MAKING them.
//
// The board is the comparison: four photographs, each
// carrying three checks, every one DERIVED from the
// frame's own frozen fields on every read, never stored —
// does its place agree with where its caption says it
// was taken? Does its day? Do the faces in the frame
// match the faces the moment's own record holds? For each
// photograph the player commits a READING: GENUINE (one
// shutter, honestly captioned), MISFILED (one honest
// shutter wearing the wrong caption), or COMPOSITE (no
// single shutter holds it — the frame was made, out of
// more than one real moment). Readings are recorded;
// VERDICTS are derived, never stored — each photograph
// has one piece of evidence that settles it, and a
// reading standing with its settling piece live is
// PROVEN, a reading standing against it is REFUTED (and
// can be struck and read again), anything else is
// COMMITTED, untested.
//
// Evidence: 12 pieces — 5 witness tellings, 5 Look finds,
// 2 participation beats — three tagged to every
// photograph. Two pieces are UNREADABLE when found: they
// go into the evidence set honestly inert (a gift card in
// a careful hand, unsigned; a loan-and-label form whose
// job number points at a log not yet read) and become
// spendable only once the chapter that unlocks their
// context has begun. A piece that is not live settles
// nothing, proves nothing. The suite proves it.
//
// The finale is the display's official opening — the
// Rehanging, in the school hall, where the town decides
// what the wall will say. The player makes one of four
// calls. READ THE FRAMES ALOUD (gated on the board's
// final derived state: the loaned print and the Bijou's
// enlargement PROVEN, every photograph called, none
// refuted, at least three proven), NAME THE BOMBER (the
// town's own answer — faces don't add themselves, so a
// hand added them; gated on at least one photograph
// PROVEN), LET IT HANG AS LOANED, or WALK AWAY. A closing
// nobody plays is its own landing: unexposed. The game
// never admits a call was wrong; the photographs just
// keep hanging where they hang.
//
// The solution, in one breath, for the payoffs only:
// the loaned print is a composite — and it was made as a
// gift, in daylight, by the one person who could. The
// staff portrait's sitting was taken by the photo club's
// sponsor, the young teacher who ran the club darkroom —
// Principal Victoria — so the portrait of the staff held
// five, and she was not among them. The staff asked her
// to complete it: the portrait, they said, wasn't whole
// without the woman behind the camera. She married the
// sitting's negative to a frame of herself from the same
// week's picnic roll, in the club darkroom, and the
// finished portrait hung in the front hall for years as
// "the staff, complete" — the town's memory quietly
// promoted it to the original sitting. Her penciled
// caption on the loan is memory doing what memory does:
// it names the day the portrait was unveiled, not the
// rehearsal afternoon it was taken. The archive print is
// exactly what its mount card says — a reprint, pulled
// years later from the sitting's own scratched negative,
// flat and honest. The Bijou's enlargement is a true
// photograph of the anniversary week wearing the
// reopening night's plate, because the Bijou's
// loan-and-label book copied its caption from the entry
// above it. And the album page is the record correcting
// the family: five in the yard on a clear day, one of
// them at the edge, uninvited and unbothered — the storm
// and the missing man are both the family's memory,
// editing. The man at the edge of the album frame is the
// man the town will name if it gets its way. He has been
// at the edge of frames for forty years. It is where he
// stands. It is not a crime scene; it is a habit.
//
// Structure:
//  - CHAPTERS10: four full ShenaniganDefs on the same
//    episode machinery, outside the random pool,
//    resolving in SHENANIGAN_BY_ID like every case.
//  - Gating: the case queues into the mystery slot TEN
//    days after the audit case retires. While any
//    earlier case is open this one does not even
//    schedule.
//  - Retirement: every landing retires the arc forever.
//    The completed landing alone gets the long tail: a
//    week on, the loaned print is rehung with its true
//    card, and the frame it was completed from hangs
//    beside it, loaned openly this time.
//
// PURE Kotlin (no android imports): the engine and the
// Activities call in; the JVM suite pins the behavior.
// All text newly written; the premise is ours.
// ============================================================

object Mystery10 {

    const val CASE_TITLE = "The Photograph Nobody Took"
    const val FINALE10_ID = "arc10_ch4_the_rehanging"
    const val CHAPTER_GAP_DAYS = 7
    const val OPEN_DELAY_DAYS = 10     // after the audit case retires

    val CHOICE_KEYS = listOf("read", "name", "stand", "walk")
    const val DEFAULT_BRANCH = "unexposed"
    val BRANCH_KEYS = listOf("complete", "framed", "hung", "unexposed")

    const val LABEL_RETURN_DELAY_DAYS = 7

    /** A reading, as the dialog and the save file name
     *  it: the frame is genuine, the frame is misfiled,
     *  or the frame was made. The verdict is never the
     *  player's to pick — the evidence grades the
     *  reading. */
    val CALL_KEYS = listOf("genuine", "misfiled", "composite")

    // ---------------- the four chapters ----------------
    // They air a week apart in the player's present: the
    // hanging (two prints side by side, and the faces
    // don't add up), the frames examined one by one
    // (place, day, faces, under the lamp), the records
    // themselves (a negative log, a loan book, a frame's
    // backing), and — in the finale — the Rehanging,
    // where the town decides what the wall will say.

    val CHAPTERS10: List<ShenaniganDef> = listOf(
        ShenaniganDef(
            id = "arc10_ch1_the_hanging",
            title = "The Hanging",
            cause = "It's the Elementary's anniversary " +
                "season, and the photo club is mounting " +
                "'The Town in Photographs' in the school " +
                "hall: framed prints loaned from the town's " +
                "albums and walls — the yearbook's frames, " +
                "the darkroom's enlargements, one family's " +
                "album opened flat under glass. The " +
                "centerpiece goes up this morning, on loan " +
                "from the school's own storeroom: the staff " +
                "portrait, the anniversary sitting, six " +
                "faces under a penciled caption that reads " +
                "'the whole staff, complete.' Beside it, " +
                "for company, the committee hangs the " +
                "yearbook archive's own print of the same " +
                "sitting. By noon, half the school is " +
                "standing in front of the pair of them, " +
                "counting.",
            blurb = "Two prints of one sitting, side by " +
                "side, and they do not agree: the loaned " +
                "portrait holds six figures and the archive " +
                "print holds five, and the sitting's own " +
                "record — the club's mount card, in the " +
                "secretary's hand — says five, as taken. " +
                "The town has already solved it, the way " +
                "the town does: somebody has been MAKING " +
                "photographs, and the town has a name warmed " +
                "up for whoever it is — the Bomber, the face " +
                "at the edge of half the frames in town, " +
                "finally graduated from sneaking into " +
                "photographs to building them whole. The " +
                "display committee would like the wall to " +
                "make sense — by the Rehanging.",
            theme = QuestTheme("📷", "#5a6e4a", "THE PHOTOGRAPH NOBODY TOOK"),
            tintHex = "#161c12",
            stages = listOf(
                ShenaniganStage("Kickoff: Two Prints, One Wall",
                    "📷 THE SCHOOL HALL, HANGING MORNING: The " +
                        "display goes up the way displays do — " +
                        "ladders, levels, a great deal of " +
                        "stepping back. The centerpiece is " +
                        "last, by design: the staff portrait, " +
                        "in its good frame, with its penciled " +
                        "caption under the glass — 'the whole " +
                        "staff, complete.' Beside it the " +
                        "archive print, smaller, flatter, in " +
                        "the club's standard mount. The " +
                        "committee steps back. The hall steps " +
                        "forward. Somebody — it is never " +
                        "established who, and it matters that " +
                        "it doesn't matter — counts the faces " +
                        "in the big one out loud, the way you " +
                        "count anything you expect to catch: " +
                        "one, two, three, four, five — six.",
                    "A discrepancy of one face is the worst " +
                        "kind: too small to be a theft, too " +
                        "exact to be a shrug. Count the " +
                        "archive print and you get five. Read " +
                        "the sitting's own record — the mount " +
                        "card under the archive print, filled " +
                        "in at the sitting, in the club " +
                        "secretary's hand — and it says five, " +
                        "as taken, and names them. The big " +
                        "print says six, and shows them, and " +
                        "one of the six is the woman who has " +
                        "run this school for forty years, " +
                        "standing in the middle of her staff " +
                        "like she was always in the frame. " +
                        "The question the hall is asking, at " +
                        "whisper speed, is not whether she's " +
                        "in it. It's how she got there.",
                    "Count the faces at the display, Elementary hall.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Counting faces out loud. Stepping back. Counting again.",
                    tone = "hype",
                    factionEffects = listOf(
                        FactionEffect("school_staff", influenceDelta = 2,
                            note = "The Staff has counted the portrait six times and gotten six every time. The Staff is starting to enjoy the archive print's confidence less and less."))),
                ShenaniganStage("Escalation: The Print Everyone Remembers",
                    "📷 THE PORTRAIT, BY CROWD: The strange " +
                        "thing is not that the big print shows " +
                        "six. The strange thing is that the " +
                        "town REMEMBERS six. It hung in the " +
                        "front hall for years, this portrait, " +
                        "before the repaint — generations of " +
                        "children walked past it daily — and " +
                        "every adult in this hallway can " +
                        "picture it without looking: the " +
                        "staff, complete, the principal in " +
                        "the middle of them. The archive " +
                        "print, meanwhile, is the stranger: " +
                        "flat-toned, scratched, five-faced, and " +
                        "nobody in the hall has ever seen it " +
                        "before today, because it has spent " +
                        "the decades in a drawer. The town is " +
                        "being asked to believe the photograph " +
                        "it has never seen over the one it " +
                        "grew up under. The town is not " +
                        "inclined to.",
                    "Memory is the town's favorite record " +
                        "and its least reliable one, and this " +
                        "case will live in the difference. A " +
                        "photograph can be checked: it has a " +
                        "place, a day, and a set of faces, " +
                        "frozen at the shutter, and those can " +
                        "be held up against everything it " +
                        "claims. Memory only has a story, and " +
                        "the story has had forty years of " +
                        "retelling. The board in your Journal " +
                        "has opened: four photographs, three " +
                        "checks apiece — place, day, faces — " +
                        "and for each one a reading you will " +
                        "have to commit to and defend: it's " +
                        "genuine, it's misfiled, or no single " +
                        "shutter holds it.",
                    "Hold the portrait up to the archive print — and to your memory of it.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Remembering six faces. Meeting the five-faced stranger from the drawer.",
                    tone = "hype", litterBurst = 1,
                    factionEffects = listOf(
                        FactionEffect("parents", influenceDelta = 2,
                            note = "The Parents remember the portrait perfectly and describe it in detail, in unison, including a seventh person who is a coat stand."))),
                ShenaniganStage("Climax: The Town Names the Method",
                    "📷 CLIMAX: By the climax of hanging " +
                        "day the hallway has a theory, and the " +
                        "theory has a name, and the name is " +
                        "the one the town keeps oiled for " +
                        "unexplained presences: the Bomber. " +
                        "Somebody who has spent years at the " +
                        "edges of this town's frames — thumb " +
                        "up, half in, uninvited and unbothered " +
                        "— has, the hallway says, graduated. " +
                        "Why sneak into photographs when you " +
                        "can build them? The word goes round " +
                        "at whisper speed and comes back " +
                        "wearing a darkroom apron. Nobody " +
                        "mentions that the portrait is older " +
                        "than most of the whisperers. Theories " +
                        "don't take questions. Theories take " +
                        "nominations.",
                    "A bomber is in the records, though — " +
                        "that is the thing about this town's " +
                        "photographs: every bomb that ever " +
                        "landed is frozen in a real frame, " +
                        "with its place and its day on it, " +
                        "checkable. A habit you can check is " +
                        "not the same thing as a hand you can " +
                        "prove. The investigation starts " +
                        "where the whispering stops: with the " +
                        "four photographs themselves, read the " +
                        "way the darkroom reads — place, day, " +
                        "faces, and nothing taken on faith, " +
                        "including faith.",
                    "Hear the town's theory out — then start with the frames.",
                    routine = "cluster", clusterLoc = "MainStreet",
                    clusterActivity = "Whispering 'the Bomber' at speed. Nominating theories.",
                    tone = "hype",
                    factionEffects = listOf(
                        FactionEffect("boys_crew", influenceDelta = 1,
                            note = "The Boys' Crew has a sketch of the Bomber already. It is mostly a thumb, at the edge of a page."))),
                ShenaniganStage("Resolution: The Wall Stays Up",
                    "📷 The committee closes the morning the " +
                        "way the display will close three " +
                        "weeks from now: on the record. The " +
                        "wall stays up — every print in its " +
                        "place, every caption as written, " +
                        "nothing quietly corrected while the " +
                        "case is open, by the committee's " +
                        "order, in pencil, which in this " +
                        "school is the binding kind. 'Examine " +
                        "them,' the principal says, 'one at a " +
                        "time. Place, day, faces. Then read " +
                        "the records behind them. Then we'll " +
                        "open the display — all of it, out " +
                        "loud, with the wall saying whatever " +
                        "it turns out the wall has to say.'",
                    "An open case is a kind of gift: it " +
                        "means the town will hold still while " +
                        "you work. The board is in your " +
                        "Journal — four photographs, each " +
                        "with its claims and its frozen " +
                        "fields, each waiting for a reading. " +
                        "Start where looking can be done with " +
                        "your own eyes: the frames themselves.",
                    "Work the board: for each frame — genuine, misfiled, or made?",
                    routine = "normal", tone = "none")
            ),
            causers = listOf("Victoria", "Jimmy"),
            episodeTitles = listOf("The Hanging", "Six Faces, Five Names"),
            coldOpen = listOf(
                Line("Victoria", "For the record — and this " +
                    "display is nothing but record — the " +
                    "portrait at the center of that wall hung " +
                    "in this school's front hall for years, " +
                    "and every child in this town walked " +
                    "past it twice a day, and it shows the " +
                    "staff of this school complete, myself " +
                    "among them. The archive print beside " +
                    "it, which this office has kept in a " +
                    "drawer since the sitting, shows five. " +
                    "I hang what the school has. I have " +
                    "always hung what the school has. One " +
                    "of those prints is the sitting. I intend " +
                    "to know which, before the opening, in " +
                    "writing."),
                Line("Jimmy", "As editor of the yearbook, I " +
                    "have a professional interest, which I " +
                    "am declaring up front, the way the " +
                    "professions do. The yearbook's archive " +
                    "is the aggrieved party here. Somebody " +
                    "is saying our drawer kept the wrong " +
                    "photograph for forty years. Drawers " +
                    "don't keep photographs, editor's note: " +
                    "people file them, and people, in my " +
                    "experience of people, should be " +
                    "prepared to show their work."))),
        ShenaniganDef(
            id = "arc10_ch2_frame_by_frame",
            title = "Frame by Frame",
            cause = "A week on, the case goes to the " +
                "frames themselves, one at a time, under " +
                "the lamp: the Bijou opens its lobby on a " +
                "dark afternoon so its enlargement can be " +
                "read in place — plate, marquee and all — " +
                "and the album page comes out from under " +
                "its glass at the school so the family that " +
                "loaned it can say, in person, what day " +
                "they remember it being.",
            blurb = "Examined closely, the photographs " +
                "refuse to behave like a scandal: the " +
                "enlargement's own background dates it, in " +
                "print, to a different week than its " +
                "plate claims; the album page's mount card " +
                "disagrees with the family that loaned it, " +
                "politely, in writing, from the day the " +
                "print was made; and the archive print — " +
                "the town's suspect — turns out to wear " +
                "its oddity on its back, stamped, dated, " +
                "and entirely mundane. If any frame on " +
                "this wall was made rather than taken, it " +
                "will not be the one the town is pointing " +
                "at. It never is. That's not evidence. It's " +
                "just the way the pointing usually goes.",
            theme = QuestTheme("🔍", "#4a5a6e", "FRAME BY FRAME"),
            tintHex = "#12161c",
            stages = listOf(
                ShenaniganStage("Kickoff: The Lobby, in the Dark",
                    "🔍 THE BIJOU, HOUSE LIGHTS HALF: A lobby " +
                        "in the daytime is all velvet and " +
                        "dust, and the enlargement hangs over " +
                        "the doors the way it has hung since " +
                        "it went up: two couples, smiling, " +
                        "under a brass plate that calls it " +
                        "the reopening night. The case reads " +
                        "it the way the darkroom reads — not " +
                        "the faces first, the frame first: " +
                        "the rope, the poster case, the " +
                        "marquee through the glass doors at " +
                        "the photograph's own edge, lettered, " +
                        "lit, and naming a feature. The " +
                        "feature is the kind of detail a " +
                        "photograph keeps without being " +
                        "asked. Photographs keep everything " +
                        "without being asked. It's their " +
                        "whole profession.",
                    "A photograph's background is its " +
                        "honestest part: nobody poses a " +
                        "marquee. Whoever took this frame " +
                        "froze the lobby as it stood that " +
                        "night — rope, posters, the feature on " +
                        "the board — and the frame has been " +
                        "telling anyone who reads past the " +
                        "faces exactly which night it was, " +
                        "in letters a foot high, for years. " +
                        "The plate says one night. The frame " +
                        "says another. Only one of them was " +
                        "there.",
                    "Read the enlargement in place, at the Bijou.",
                    routine = "cluster", clusterLoc = "Bijou",
                    clusterActivity = "Reading past the faces. Spelling a marquee out loud.",
                    tone = "hype",
                    factionEffects = listOf(
                        FactionEffect("tegridy_crew", influenceDelta = 2,
                            note = "Local Business is reading the backgrounds of its own photographs this week, all over town, just in case."))),
                ShenaniganStage("Escalation: The Family, Remembering",
                    "🔍 THE ALBUM PAGE, OUT FROM UNDER GLASS: " +
                        "The McCormicks' album lies open at " +
                        "the yard page, and the family stands " +
                        "around it the way families stand " +
                        "around evidence: protectively, and " +
                        "in slight disagreement with each " +
                        "other about the facts. The photograph " +
                        "shows four of them posed in the yard " +
                        "and a fifth figure at the frame's " +
                        "edge, thumb up, uninvited, in the " +
                        "act of being somewhere else by the " +
                        "next second. The family remembers " +
                        "this photograph completely. They " +
                        "remember the day of the big storm. " +
                        "They remember it as four. The print " +
                        "under the glass has been showing " +
                        "five people and a clear sky the " +
                        "whole time, to anyone, for years.",
                    "Nobody is lying in this scene, which " +
                        "is what makes it evidence. The " +
                        "family's memory is a good memory — " +
                        "it kept the feeling of the day, the " +
                        "yard, the people who mattered, and " +
                        "it filed the photograph under the " +
                        "storm because the storm is the day " +
                        "the week is remembered by. The " +
                        "mount card under the print, filled " +
                        "in when the print was made, kept " +
                        "other things: a date, a weather " +
                        "line, and a count that includes the " +
                        "edge. Records and memories are both " +
                        "honest. They are honest about " +
                        "different things.",
                    "Hear the family remember the album page.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Remembering a storm. Counting five, slowly, with surprise.",
                    tone = "hype", litterBurst = 1,
                    factionEffects = listOf(
                        FactionEffect("parents", influenceDelta = 2,
                            note = "The Parents are checking their own albums this week. Several have found a man at the edge of them. He appears to live there."))),
                ShenaniganStage("Climax: The Suspect Print, in Hand",
                    "🔍 CLIMAX: The archive print comes out " +
                        "of its drawer at last, into the " +
                        "light, on a cloth, in the principal's " +
                        "office, with the portrait's six faces " +
                        "looking down from the wall behind it. " +
                        "It is flat the way the whole town has " +
                        "been calling suspicious. It is " +
                        "scratched the way evidence is " +
                        "supposed to be scratched. Turn it " +
                        "over — carefully, by the edges, the " +
                        "way prints ask — and its back tells " +
                        "its whole biography in one stamped " +
                        "line and a date: whose stamp, what " +
                        "kind of print, and when it was " +
                        "pulled. The room goes a little quiet " +
                        "around it, the way rooms do when the " +
                        "suspect turns out to have papers.",
                    "The town's favorite story needs the " +
                        "archive print to be the plant: the " +
                        "flat one, the drawer one, the " +
                        "stranger. The stamp on its back is " +
                        "not interested in the story. A " +
                        "photograph made later, from an older " +
                        "negative, by a documented process, " +
                        "is not a fabrication — it is a " +
                        "reprint, which is the most ordinary " +
                        "object in any darkroom in the world. " +
                        "Prints diverge from their originals " +
                        "honestly, all the time, in tone, in " +
                        "crop, in survival itself. If the " +
                        "case has a made thing in it, it is " +
                        "not the print with the paperwork.",
                    "Turn the archive print over — carefully, by the edges.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Turning a print over by its edges. Going quiet around its papers.",
                    tone = "hype",
                    factionEffects = listOf(
                        FactionEffect("school_staff", influenceDelta = 1,
                            note = "The Staff has started saying 'the reprint question' in the tone it uses for weather."))),
                ShenaniganStage("Resolution: Frames Read, Records Next",
                    "🔍 The frames week closes with the " +
                        "photographs standing by their fields " +
                        "and the captions standing by theirs, " +
                        "and the case changes its question in " +
                        "the minutes: not WHAT the frames " +
                        "show — the frames have answered that, " +
                        "under the lamp, one at a time — but " +
                        "what stands behind them: the " +
                        "negative a print was pulled from, " +
                        "the book a caption was copied out " +
                        "of, the hand that wrote on a frame's " +
                        "backing and did not sign it. Next " +
                        "week the case reads the records " +
                        "behind the records.",
                    "The frames are done talking; they only " +
                        "ever say their fields. The paperwork " +
                        "is the talkative part: every print " +
                        "on this wall arrived by a road — a " +
                        "negative, a loan, a form, a hand — " +
                        "and roads, unlike frames, have " +
                        "authors. Read the roads the way " +
                        "you'd read a witness: for what they " +
                        "were doing the day they were written.",
                    "Work the board: the frames have said their fields — now read what stands behind them.",
                    routine = "normal", tone = "none")
            ),
            causers = listOf("Victoria", "Butters"),
            episodeTitles = listOf("Frame by Frame", "The Suspect Has Papers"),
            coldOpen = listOf(
                Line("Butters", "Oh, I was AT the Bijou the " +
                    "week that big picture went up, yes I " +
                    "was — I handed out the programs, on " +
                    "account of my dad sayin' a boy with a " +
                    "job is a boy out of trouble, which " +
                    "turned out to be mostly true. I held " +
                    "the foot of the ladder myself. I " +
                    "remember the week on account of the " +
                    "picture they were showin' — it's right " +
                    "there IN the photograph, on the " +
                    "marquee, big as anything. Folks keep " +
                    "askin' me about the reopenin' night " +
                    "and I keep sayin': I wasn't at any " +
                    "reopenin'. I was at the anniversary. " +
                    "There's a difference. It's about a " +
                    "year."),
                Line("Victoria", "The archive print is out " +
                    "of its drawer this week, under " +
                    "supervision, and I will tell you what " +
                    "I told the committee: look at it as " +
                    "long as you like. Photograph your " +
                    "conclusions afterward. A print is not " +
                    "a rumor, and a drawer is not an " +
                    "argument, and I am tired of being the " +
                    "only person in this case who has " +
                    "turned the thing over."))),
        ShenaniganDef(
            id = "arc10_ch3_the_negative_drawer",
            title = "The Negative Drawer",
            cause = "A week on, the case stops reading " +
                "prints and starts reading what prints come " +
                "from: the photo club's negative log, kept " +
                "in the darkroom drawer since the club kept " +
                "one — every roll, every frame, in the " +
                "sponsor's careful hand; the Bijou's " +
                "loan-and-label book, whose entries were " +
                "drawn up by a lawyer's office a generation " +
                "ago; and the portrait's own frame, which " +
                "comes off the wall for the first time " +
                "since the hanging, backing and all.",
            blurb = "Every print in this case arrived by a " +
                "road, and the roads are written down. The " +
                "negative log keeps the sitting's roll frame " +
                "by frame — and the roll's other frames, the " +
                "ones nobody has thought to ask about, " +
                "taken the same week, in a different place. " +
                "The loan book keeps the enlargement's " +
                "entry in two lines that do not say the " +
                "same thing. And behind the portrait, " +
                "tucked against its backing the way things " +
                "are tucked when they are meant to be " +
                "found by the right person eventually, " +
                "there is a card — in a careful hand, " +
                "warm, specific, and unsigned.",
            theme = QuestTheme("🗂️", "#6e5a4a", "THE NEGATIVE DRAWER"),
            tintHex = "#1c1612",
            stages = listOf(
                ShenaniganStage("Kickoff: The Log, Open",
                    "🗂️ THE DARKROOM DRAWER, AT THE SCHOOL: " +
                        "The negative log is a book built " +
                        "like the negatives it describes: " +
                        "sleeve after sleeve, frame after " +
                        "frame, each with a roll number, a " +
                        "place, a day, and a count — kept " +
                        "in one careful hand across the whole " +
                        "of the club's keeping. The sitting's " +
                        "entry is exactly where it should " +
                        "be, and it says what the mount card " +
                        "says: the front hall, the rehearsal " +
                        "afternoon, five in the frame, as " +
                        "taken. The case turns the page " +
                        "because pages are for turning — and " +
                        "the roll does not end at the " +
                        "sitting. Rolls never do. A roll is a " +
                        "week, not an event.",
                    "A negative log is a darkroom's memory " +
                        "with an index, and this one remembers " +
                        "more than its readers asked it. The " +
                        "sitting's roll kept shooting after " +
                        "the sitting: the same hand, the same " +
                        "week, a different field — frames of " +
                        "the staff as they actually spent the " +
                        "anniversary, off duty, outdoors, at " +
                        "ease. Whoever wants to know what " +
                        "else was on that roll has to read to " +
                        "the end of the roll. Books in this " +
                        "town keep punishing people who stop " +
                        "early. It is practically a curriculum.",
                    "Read the sitting's roll to its end, in the negative log.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Turning a log's pages with clean hands. Reading to the end of the roll.",
                    tone = "hype",
                    factionEffects = listOf(
                        FactionEffect("school_staff", influenceDelta = 2,
                            note = "The Staff's negative log is being read this week with the attention usually reserved for scripture."))),
                ShenaniganStage("Escalation: Two Lines That Disagree",
                    "🗂️ CITY HALL, THE LOAN BOOK, ON THE " +
                        "COUNTER: The Bijou's loan-and-label " +
                        "book is a handsome volume — drawn up " +
                        "by a lawyer's office when the " +
                        "theater's walls were first hung, " +
                        "every loan in it given two lines: " +
                        "the item, described; the caption, as " +
                        "engraved. The enlargement's entry is " +
                        "in its place, in the book's fair " +
                        "hand, and its two lines do not " +
                        "describe the same night. The item " +
                        "line describes a frame by the " +
                        "feature on its marquee. The caption " +
                        "line names a celebration a year " +
                        "earlier. Nobody at the counter " +
                        "remarks on it. The book has been " +
                        "sitting here, saying both things, " +
                        "in company, for years.",
                    "A caption copied from the wrong line " +
                        "of a book is not a conspiracy; it " +
                        "is a Tuesday, in an office, a " +
                        "generation ago — and it is exactly " +
                        "the kind of error that hardens, " +
                        "once engraved on brass, into " +
                        "history. The book does not decide " +
                        "which of its lines is true. Books " +
                        "never do. The frame decides that: " +
                        "the frame was there, and the frame " +
                        "has been telling the truth about " +
                        "its night, in letters a foot high, " +
                        "to anyone who reads past the plate.",
                    "Read the enlargement's entry — both lines of it.",
                    routine = "cluster", clusterLoc = "CityHall",
                    clusterActivity = "Reading two lines that disagree. Not remarking on it, in company.",
                    tone = "hype", litterBurst = 1,
                    factionEffects = listOf(
                        FactionEffect("parents", influenceDelta = 2,
                            note = "The Parents are divided on the enlargement: half want its plate corrected by Friday, half want it kept wrong for the mystery."))),
                ShenaniganStage("Climax: The Backing Card",
                    "🗂️ CLIMAX: The portrait comes off the " +
                        "wall at last — two people, white " +
                        "gloves, a table cleared for it — and " +
                        "goes over, carefully, the way prints " +
                        "ask. The backing is fresh board, " +
                        "newer than the frame's fellows, and " +
                        "against it, tucked flat where only " +
                        "a reframing hand or an investigator " +
                        "would ever find it, is a card. The " +
                        "hand on it is careful, practiced, " +
                        "the hand of somebody who wrote on " +
                        "school paper for a living. It is a " +
                        "gift card. It thanks somebody. It " +
                        "names the occasion. It does not — " +
                        "the room notices this together, in " +
                        "the quiet — it does not sign itself.",
                    "Objects keep what people were doing, " +
                        "in the order they did it, with their " +
                        "hands on them — which is more than " +
                        "any witness in this town has ever " +
                        "managed. The card says the portrait " +
                        "was finished as a present, and says " +
                        "for whom, and says why, in the " +
                        "warmest language in the whole case. " +
                        "Whose hand held the pen is a story " +
                        "the card assumes its reader already " +
                        "knows. Cards always assume that. " +
                        "It's how the past hides: in " +
                        "handwriting that thinks it's " +
                        "familiar.",
                    "Turn the portrait over — and read what its backing kept.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Turning a portrait over in white gloves. Reading an unsigned card in the quiet.",
                    tone = "hype",
                    factionEffects = listOf(
                        FactionEffect("tegridy_crew", influenceDelta = 1,
                            note = "Local Business has started checking the backs of its own frames this week. Business finds the backs more honest than the fronts, and says so."))),
                ShenaniganStage("Resolution: One Hand Still Unnamed",
                    "🗂️ The records week closes with the " +
                        "roads all read and one hand still " +
                        "unnamed: the hand in the log, the " +
                        "hand on the card — the same hand, " +
                        "the case is prepared to say, in the " +
                        "way cases say things before rooms " +
                        "do — kept by the case against the " +
                        "day its owner is standing in front " +
                        "of the wall it belongs to. Next week: " +
                        "the Rehanging, the display's official " +
                        "opening, where the town decides what " +
                        "the wall will say — with the readings " +
                        "read aloud, or a name named, or the " +
                        "captions let stand exactly as " +
                        "written, by the only person who has " +
                        "read all of them.",
                    "A board with three frames read and one " +
                        "hand unnamed is not a finished board " +
                        "— the Rehanging's terms are on the " +
                        "board, and they are strict about " +
                        "finished. Whatever can still be " +
                        "looked at, handled or read, read it " +
                        "now. What the town does with " +
                        "half-read photographs is not the " +
                        "town's fault. Towns run on what " +
                        "they're handed.",
                    "Work the board: read every frame before the Rehanging — a half-read wall opens badly.",
                    routine = "normal", tone = "none")
            ),
            causers = listOf("Gerald", "Jimmy"),
            episodeTitles = listOf("The Negative Drawer", "The Hand in the Log"),
            coldOpen = listOf(
                Line("Gerald", "I drew up that loan book, " +
                    "you know. Young lawyer, first theater " +
                    "client, very proud of the two-line " +
                    "system: the item, described; the " +
                    "caption, as engraved. Every frame the " +
                    "Bijou ever borrowed stands in it, two " +
                    "lines apiece, and the system has never " +
                    "once been wrong in the item line. I " +
                    "make no such claim for the caption " +
                    "lines. Captions were copied by " +
                    "clerks, at speed, from the line above " +
                    "more often than a lawyer likes to " +
                    "admit in public. Turn to the " +
                    "enlargement's entry, counselor. Read " +
                    "BOTH lines. It's why we gave every " +
                    "frame two."),
                Line("Jimmy", "Editor's note, and I'll keep " +
                    "it brief because the drawer deserves " +
                    "the time: the yearbook has kept this " +
                    "town's negatives since before there " +
                    "was a yearbook, in sleeves, in order, " +
                    "in one hand. I have been through the " +
                    "anniversary rolls this week, frame by " +
                    "frame, in my professional capacity, " +
                    "and I will say this for the drawer: " +
                    "it has never once lied to me. It has " +
                    "occasionally declined to be " +
                    "understood on the first reading. " +
                    "There's a difference, and the " +
                    "difference is the whole job."))),
        ShenaniganDef(
            id = FINALE10_ID,
            title = "The Rehanging",
            cause = "Three weeks of comparison close " +
                "tonight: the school hall dressed for the " +
                "display's official opening, the four " +
                "photographs in their places under their " +
                "captions, the results of the comparison " +
                "kept in one Journal, and a room full of " +
                "townsfolk who have come to watch a wall be " +
                "opened. The committee intends to open the " +
                "display in public — with the readings read " +
                "aloud, or the Bomber named, or the " +
                "captions let stand — and it has left a " +
                "space in the program, between the welcome " +
                "and the refreshments, labeled in the " +
                "secretary's fair hand: THE WALL, CORRECTED.",
            blurb = "The Rehanging: one hall, four " +
                "photographs, and every caption in town " +
                "standing exactly where it stood. The board " +
                "in your Journal is either finished or it " +
                "isn't — every frame read, its reading " +
                "graded by the piece that settles it. Read " +
                "the frames aloud and the town gets its " +
                "wall back, true. Name the Bomber and the " +
                "room gets the name it came for. Let it " +
                "hang as loaned and the wall keeps the " +
                "story it arrived with, question marks and " +
                "all. Walk away, and the display opens the " +
                "way displays do without their reader: by " +
                "ribbon, by applause, by the nearness of a " +
                "face at a frame's edge.",
            theme = QuestTheme("🖼️", "#7a4a4a", "THE REHANGING"),
            tintHex = "#1c1214",
            stages = listOf(
                ShenaniganStage("Kickoff: The Hall, Dressed",
                    "🖼️ THE SCHOOL HALL, OPENING NIGHT: The " +
                        "four photographs in a row under the " +
                        "hall's good lights — a portrait, an " +
                        "archive print, an enlargement " +
                        "brought down from the Bijou for the " +
                        "night, an album under glass — and " +
                        "under each, its caption, as written, " +
                        "as loaned, as engraved. The room " +
                        "fills the way it fills for a naming. " +
                        "Rooms can't help it. It's why they " +
                        "have chairs, and programs, and a " +
                        "space in the program labeled like a " +
                        "verdict waiting for handwriting.",
                    "Every photograph in this case is " +
                        "standing, tonight, exactly where it " +
                        "stood: six faces in a portrait, five " +
                        "in its archive twin, four in a lobby, " +
                        "five in a yard with clear skies. Four " +
                        "frames, four captions, and one board " +
                        "that knows — or doesn't — how each " +
                        "of them came to say what it says. " +
                        "The committee is already looking at " +
                        "the empty space in the program. The " +
                        "space is looking back.",
                    "Take your place at the Rehanging.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Filling a hall the way rooms fill for a naming.",
                    tone = "hype",
                    factionEffects = listOf(
                        FactionEffect("parents", influenceDelta = 2,
                            note = "The Parents have come for a name and brought a frame, in case. The frame has been asked to wait in the hall."))),
                ShenaniganStage("Escalation: The Face at the Edge",
                    "🖼️ THE ROOM WARMS ITS HANDS: An " +
                        "investigation without a villain is a " +
                        "hard sell at an opening, and the room " +
                        "has been quietly solving that problem " +
                        "the way rooms do: by nearness. Whose " +
                        "face is at the edge of the album " +
                        "frame? Whose thumb is in half the " +
                        "photographs in this town? The " +
                        "questions are not accusations. They " +
                        "are just the shape a room makes when " +
                        "it reaches for the nearest face — " +
                        "and the nearest face, tonight, is a " +
                        "man who has stood at the edge of " +
                        "frames for forty years, who has not " +
                        "been asked a single question by this " +
                        "case, and who is standing very still " +
                        "by the refreshments.",
                    "Nearness is not evidence; it's just " +
                        "geography with confidence. The man " +
                        "by the refreshments is in the frames " +
                        "because he was AT the frames — every " +
                        "one of them checkable, dated, placed, " +
                        "in the records, at the shutter. The " +
                        "whole of his involvement in this " +
                        "case is that the town's theory, run " +
                        "carelessly, lands on the face it can " +
                        "see without reading. The board is " +
                        "the only thing in the room that can " +
                        "out-argue a room. It had better be " +
                        "finished.",
                    "Watch the room reach for the nearest face.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Reaching for the nearest face. Standing very still by the refreshments.",
                    tone = "hype", litterBurst = 1,
                    factionEffects = listOf(
                        FactionEffect("parents", influenceDelta = 1,
                            note = "The Parents are sitting with the McCormicks tonight, in a row, on purpose. Rows are also a kind of frame."))),
                ShenaniganStage("Climax: The Wall, Corrected",
                    "🖼️ CLIMAX: The committee chair raps " +
                        "the lectern and the program reaches " +
                        "its labeled space: THE WALL, " +
                        "CORRECTED. The captions are up. The " +
                        "hall's lamps are lit. Somewhere " +
                        "between the welcome and the " +
                        "refreshments, the display opens — " +
                        "by a reading, by a name, by a " +
                        "ribbon, or by a silence shaped " +
                        "exactly like whichever of those the " +
                        "room wanted most. The secretary's " +
                        "pen is up. Pens, at openings, are " +
                        "how walls get their handwriting.",
                    "Everything lookable has been looked " +
                        "at twice. Everything readable is " +
                        "read except one form, whose context " +
                        "arrived with the week, the way " +
                        "contexts do. What remains is the " +
                        "call — not about what the frames " +
                        "show; the frames have been showing " +
                        "it all along — but about what the " +
                        "town will say they mean: the whole " +
                        "comparison, read aloud; or one " +
                        "name, for the face the room is sure " +
                        "it sees; or nothing at all, the " +
                        "captions standing as written, the " +
                        "question standing as penciled. The " +
                        "pen is up. The wall is yours, or it " +
                        "isn't.",
                    "Open the wall: read the frames, name the face, let it hang, or walk.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Holding a pen up over a program. Opening a wall in handwriting.",
                    tone = "hype",
                    factionEffects = listOf(
                        FactionEffect("school_staff", influenceDelta = 1,
                            note = "The Staff has been promised the frame stays in the hall whatever happens. It is not a comfort to the man by the refreshments."))),
                ShenaniganStage("Resolution: Under the Good Lights",
                    "🖼️ The Rehanging ends the way openings " +
                        "end: in handwriting. Whatever was " +
                        "said is said into the program, in " +
                        "the secretary's fair hand, and the " +
                        "captions are corrected or kept by " +
                        "the same authority. The enlargement " +
                        "goes back to its lobby. The album " +
                        "goes home under its glass. And the " +
                        "portrait keeps hanging where it " +
                        "hangs — explained, or named at, or " +
                        "let stand — while the town walks " +
                        "home past the school in the dark, " +
                        "developing nothing, for once, at " +
                        "all.",
                    "An opening settles a wall, not a " +
                        "town. The portrait will be in the " +
                        "hall tomorrow. The yard will keep " +
                        "its clear sky. The lobby will keep " +
                        "its marquee. What tonight decides is " +
                        "which story the town tells about " +
                        "them — the board's story, or the " +
                        "room's. It has always, only, ever " +
                        "been that.",
                    "The wall is opened. Walk home past all four of them.",
                    routine = "normal", tone = "none")
            ),
            causers = listOf("Victoria", "Jimmy"),
            episodeTitles = listOf("The Rehanging", "The Wall, Corrected"),
            coldOpen = listOf(
                Line("Victoria", "Three weeks ago this town " +
                    "hung two prints of one sitting side by " +
                    "side and counted six faces against " +
                    "five. Tonight the display opens, in " +
                    "this hall, on the record — with the " +
                    "whole comparison read aloud, or with " +
                    "the name of the hand that helped those " +
                    "faces multiply, or with the captions " +
                    "standing as written and the question " +
                    "standing as penciled. Those are the " +
                    "endings on the program. There is also " +
                    "the ending where nobody reads " +
                    "anything. I have seen that ending. It " +
                    "wears a ribbon."),
                Line("Stuart", "I was told there'd be " +
                    "questions for me tonight, on account " +
                    "of the — the face at the edge of " +
                    "things. I've got the face because the " +
                    "yard is also mine, and the ladder, " +
                    "and standin' where the work is, which " +
                    "— before anybody starts — is usually " +
                    "the edge. I hold ladders. I carry " +
                    "things. Nobody handed me a camera, a " +
                    "darkroom or a forty-year-old " +
                    "portrait, and I'd like the program to " +
                    "show I said that standin' up, by the " +
                    "refreshments, which is where I've " +
                    "been the whole case.")))
    )

    val CHAPTER10_BY_ID: Map<String, ShenaniganDef> =
        CHAPTERS10.associateBy { it.id }

    fun chapterIndexOf10(id: String): Int =
        CHAPTERS10.indexOfFirst { it.id == id }

    fun isChapter10(id: String?): Boolean =
        id != null && id in CHAPTER10_BY_ID

    // ---------------- the board's rows: four photographs ----------------
    // A frame is a photograph and its claim: the portrait
    // and its caption, the archive print and its mount
    // card, the enlargement and its plate, the album page
    // and its family's memory. The claim fields and the
    // shown fields are facts of the case; the CHECKS are
    // not stored anywhere — each is the comparison of two
    // fields, recomputed wherever it's shown. What a
    // frame is FOR is the reading: which way the
    // comparison settles — the frame is what it claims,
    // the frame is true but its label names another
    // moment, or no single shutter holds it at all.

    data class Frame10(
        val id: String,
        /** Board order: the portrait, the archive print,
         *  the enlargement, the album page. */
        val index: Int,
        val title: String,
        /** Where the photograph hangs (a LOCATIONS id). */
        val place: String,
        /** Who keeps it, as the town recorded it. */
        val keeper: String,
        /** The claim, in words: what its caption, plate
         *  or memory says it is. */
        val claimLabel: String,
        /** Where the caption says it was taken. */
        val placeClaim: String,
        /** Where the frame's own frozen backdrop says
         *  it was taken. */
        val placeShown: String,
        /** The occasion the caption claims. */
        val dayClaim: String,
        /** The occasion the frame's own frozen details
         *  date it to. */
        val dayShown: String,
        /** Everyone frozen in the frame, in frame order. */
        val figures: List<String>,
        /** How many faces the moment's own record holds. */
        val facesRecorded: Int,
        /** The evening as it actually ran (sealed): which
         *  reading this frame's evidence settles on. */
        val truth: String,
        /** The one piece that settles this frame. */
        val proofBy: String,
        /** 0-based chapter that must have BEGUN before
         *  this frame can be read (called). */
        val minChapter: Int,
        /** Live pieces tagged to this frame that a
         *  reading costs. */
        val callPrice: Int = 2
    )

    val FRAMES10: List<Frame10> = listOf(
        Frame10("frame_loaned", 0, "THE LOANED PRINT",
            "Elementary",
            "the school's own storeroom, captioned from memory at the hanging",
            "its penciled caption: 'the whole staff, complete'",
            "the Elementary's front hall",
            "the Elementary's front hall",
            "the anniversary day",
            "the anniversary rehearsal afternoon",
            listOf("Garrison", "Choksondik", "Adler",
                "Mackey", "Crabtree", "Victoria"),
            facesRecorded = 5,
            truth = "composite", proofBy = "gift_card",
            minChapter = 1),
        Frame10("frame_archive", 1, "THE ARCHIVE PRINT",
            "Elementary",
            "the yearbook's archive drawer, on the photo club's mount card",
            "its mount card: the sitting, five, as taken",
            "the Elementary's front hall",
            "the Elementary's front hall",
            "the anniversary rehearsal afternoon",
            "the anniversary rehearsal afternoon",
            listOf("Garrison", "Choksondik", "Adler",
                "Mackey", "Crabtree"),
            facesRecorded = 5,
            truth = "genuine", proofBy = "scratch_marks",
            minChapter = 1),
        Frame10("frame_enlargement", 2, "THE BIJOU'S ENLARGEMENT",
            "Bijou",
            "the Bijou's lobby wall, under its brass plate",
            "its brass plate: the reopening night",
            "the Bijou's lobby",
            "the Bijou's lobby",
            "the reopening night",
            "the anniversary week",
            listOf("Randy", "Sharon", "Gerald", "Sheila"),
            facesRecorded = 4,
            truth = "misfiled", proofBy = "loan_form",
            minChapter = 2),
        Frame10("frame_album", 3, "THE ALBUM PAGE",
            "Elementary",
            "the McCormick family album, loaned for the display",
            "the family's memory: the day of the big storm",
            "the McCormick yard",
            "the McCormick yard",
            "before the storm",
            "before the storm",
            listOf("Carol", "Kevin McCormick", "Karen",
                "Kenny", "Stuart"),
            facesRecorded = 5,
            truth = "genuine", proofBy = "album_weather",
            minChapter = 2)
    )

    val FRAME10_BY_ID: Map<String, Frame10> = FRAMES10.associateBy { it.id }

    /** A frame's three checks, derived from its fields
     *  on every read — the board stores fields, never
     *  conclusions. */
    fun placeAgrees10(f: Frame10): Boolean = f.placeClaim == f.placeShown
    fun dayAgrees10(f: Frame10): Boolean = f.dayClaim == f.dayShown
    fun facesAgrees10(f: Frame10): Boolean = f.figures.size == f.facesRecorded
    fun checksAgree10(f: Frame10): Int =
        listOf(placeAgrees10(f), dayAgrees10(f), facesAgrees10(f)).count { it }

    // ---------------- the evidence ----------------

    enum class Kind10 { WITNESS, LOOK, PARTICIPATION }

    data class Clue10(
        val id: String,
        val kind: Kind10,
        /** 0-based chapter that must have BEGUN for this
         *  evidence to exist in the world. */
        val minChapter: Int,
        /** The frame (index) this piece is tagged to —
         *  the row of the board it speaks to, however
         *  late it is found. */
        val site: Int,
        /** 0-based chapter that must have BEGUN for this
         *  piece to be LIVE — testimony-grade, spendable.
         *  Equals [minChapter] for ordinary pieces. For
         *  the two context-locked pieces it is a LATER
         *  chapter: the piece can be held, honestly
         *  inert, before the chapter that gives it its
         *  context. */
        val unlockChapter: Int,
        val title: String,
        /** The board's account of the piece once LIVE. */
        val detail: String,
        /** The board's account while held-but-inert. For
         *  ordinary pieces this equals [detail] (they
         *  are never seen inert). For locked pieces it is
         *  the honest shrug: what the thing IS, without
         *  what it MEANS. */
        val inertDetail: String,
        /** WITNESS: the cast member who tells you. */
        val npc: String? = null,
        /** WITNESS: the line they say (the telling IS the proof). */
        val spoken: String? = null,
        /** LOOK: the location to Look at. */
        val locId: String? = null,
        /** LOOK: the sentence the Look reports. */
        val foundText: String? = null,
        /** PARTICIPATION: the chapter def whose climax turf grants it. */
        val chapterDefId: String? = null
    )

    val CLUES10: List<Clue10> = listOf(
        // ---------- frame 0: the loaned print ----------
        Clue10("victoria_loan", Kind10.WITNESS, 0, 0, 0,
            "The portrait's keeper (Victoria)",
            "The portrait's keeper, on the record: the " +
                "print came out of the school's own " +
                "storeroom for the display, in its good " +
                "frame, with the penciled caption already " +
                "under the glass — a caption older than " +
                "the committee, in a hand she will only " +
                "describe as 'the school's.' She hung the " +
                "two prints side by side herself, and she " +
                "counted the faces in each the way she " +
                "counts everything: twice, out loud, and " +
                "then once more silently. Six and five. " +
                "Her storeroom, she will tell you, keeps " +
                "what the school was given. Somebody gave " +
                "the school that portrait, the way it is. " +
                "The school has never once asked who.",
            "The portrait's keeper (Victoria)",
            npc = "Victoria",
            spoken = "I'll save you the shoe leather: the " +
                "portrait came out of MY storeroom, in its " +
                "good frame, with that caption already " +
                "penciled under the glass, in the school's " +
                "old hand. I hung the two prints side by " +
                "side myself and I counted both. Six in " +
                "the portrait. Five in the archive print. " +
                "Five on the mount card, in the " +
                "secretary's hand, as taken. So. Either " +
                "the sitting had a sixth sitter the " +
                "secretary declined to write down — or " +
                "that portrait is not entirely a sitting. " +
                "I keep this school's records. Somebody " +
                "else, apparently, keeps its walls."),
        Clue10("hanging_help", Kind10.PARTICIPATION, 0, 0, 0,
            "The frames, in your hands",
            "Hanging the display, you handled every frame " +
                "on the wall, and the portrait's frame told " +
                "its own small story to your hands: its " +
                "backing board is fresh-cut and pale where " +
                "its fellows' are dark and foxed, its wire " +
                "is new, and its corners sit a fraction " +
                "prouder than a frame that has hung " +
                "undisturbed for decades should. A reframed " +
                "print is not a made one — prints get " +
                "reframed for honest reasons, the way books " +
                "get rebound — but this portrait has " +
                "plainly been OUT of its frame and back " +
                "in, carefully, by somebody who knew how. " +
                "The archive print, by contrast, has never " +
                "been out of anything but its drawer.",
            "The frames, in your hands",
            chapterDefId = "arc10_ch1_the_hanging"),
        Clue10("gift_card", Kind10.LOOK, 1, 0, 2,
            "A card behind the portrait",
            "The card from the portrait's backing, read " +
                "at last with the photo club's negative " +
                "log open beside it — the log is kept in " +
                "the same careful hand, and the hand gives " +
                "the card its context: it is a gift card, " +
                "in the club sponsor's own hand — the " +
                "hand that kept the log and worked the " +
                "camera at the sitting — tucked behind " +
                "the portrait on the day the portrait was " +
                "finished. Its words are for the staff it " +
                "shows: thanks, for asking; an apology, " +
                "for the liberty; and a hope that the " +
                "portrait would be allowed to be what it " +
                "was made to be — the staff, complete, " +
                "the woman behind the camera standing " +
                "with them at last. It is signed with a " +
                "role, not a name: the sponsor. Whoever " +
                "reads the log reads the hand. The " +
                "portrait was not found. It was finished, " +
                "as a present, in the open, by the person " +
                "whose job the camera was.",
            "A card tucked behind the portrait's " +
                "backing, in a careful, practiced hand: a " +
                "gift inscription — thanks, an apology for " +
                "a liberty taken, a hope about a portrait. " +
                "It signs itself with a role instead of a " +
                "name, and a role without a name is just " +
                "handwriting. Whose hand it is, the card " +
                "assumes you know. You don't. Yet.",
            locId = "Elementary",
            foundText = "📷 A CARD, BEHIND THE PORTRAIT: " +
                "fresh backing board, and tucked against " +
                "it a gift card in a careful hand — thanks, " +
                "an apology for a liberty, a hope about a " +
                "portrait. Signed with a role, not a name. " +
                "You can read every word and understand " +
                "none of it: hands are identified in " +
                "company, and this one's company is " +
                "elsewhere."),
        // ---------- frame 1: the archive print ----------
        Clue10("jimmy_archive", Kind10.WITNESS, 0, 1, 0,
            "The archive's editor (Jimmy)",
            "The yearbook's editor, on the record, with " +
                "his professional interest declared: the " +
                "archive print is the yearbook's own copy " +
                "of the sitting, and it has sat in the " +
                "archive drawer since the sitting's own " +
                "year, in the club's standard mount, with " +
                "the club's mount card filled in beneath " +
                "it. If one of the two prints on that wall " +
                "is the plant, he will tell you, it is not " +
                "the one with forty years of dust on its " +
                "drawer — and as for its flatness, the " +
                "town's great suspicion, he has heard it " +
                "before, about every reprint the yearbook " +
                "has ever pulled, and he refers the town " +
                "to the stamp on its back, which the town " +
                "has not yet, at the time of speaking, " +
                "turned over to read.",
            "The archive's editor (Jimmy)",
            npc = "Jimmy",
            spoken = "Editor's statement, for the record, " +
                "and the record is kind of my whole thing: " +
                "that print is the yearbook's archive copy " +
                "of the sitting. It has been in our drawer " +
                "since the sitting's year, in the club " +
                "mount, on the club card, filed by the " +
                "club. The big one on the wall — no " +
                "comment, at this time, from this office, " +
                "except this: everybody keeps calling my " +
                "print flat like it's a personality flaw. " +
                "Flat is what reprints ARE. There's a " +
                "stamp on its back says what it is and " +
                "when it was pulled. Critics used to READ " +
                "the material before reviewing it. Just " +
                "saying. Professionally."),
        Clue10("scratch_marks", Kind10.LOOK, 1, 1, 1,
            "The archive print, turned over",
            "The archive print in hand, by its edges: a " +
                "long scratch runs down its face — through " +
                "the emulsion, through every figure alike, " +
                "the way a scratch in a negative prints " +
                "onto everything it ever makes — and its " +
                "tones are flat the way reprints' tones " +
                "are, one generation removed from the " +
                "light. On its back, stamped in the photo " +
                "club's own ink, the print's whole " +
                "biography in one line: REPRINT — pulled " +
                "from the sitting's negative, for the " +
                "archive, with a date years after the " +
                "sitting. A copy of a true negative is a " +
                "true photograph; the scratch is the " +
                "negative's age, worn honestly by " +
                "everything descended from it. The town's " +
                "suspect has papers, and the papers are " +
                "boring, and boring is what innocence " +
                "usually looks like in a darkroom.",
            "The archive print, turned over",
            locId = "Elementary",
            foundText = "📷 THE ARCHIVE PRINT, TURNED " +
                "OVER: a scratch runs through emulsion " +
                "and figures alike, the way a negative's " +
                "scratch prints on everything it makes — " +
                "and on its back, in the photo club's " +
                "ink, one stamped line: REPRINT, pulled " +
                "from the sitting's negative for the " +
                "archive, dated years after the sitting."),
        Clue10("frames_in_hand", Kind10.PARTICIPATION, 1, 1, 1,
            "The prints, under the lamp",
            "Working the examination, you held both " +
                "prints under the darkroom lamp, and the " +
                "archive print's honesty turned out to be " +
                "something your hands could verify: its " +
                "paper is the club's old stock, its mount " +
                "the club's standard card, its edges worn " +
                "the soft way paper wears when it has been " +
                "in one drawer, undisturbed, for decades. " +
                "The mount card beneath it is filled in " +
                "the secretary's hand with the sitting's " +
                "own facts — the front hall, the rehearsal " +
                "afternoon, five, as taken — and the card " +
                "and the print and the negative log all " +
                "say them the same way, in three different " +
                "hands, across forty years. Whatever was " +
                "done to make the wall's arithmetic fail, " +
                "it was not done to this print. This print " +
                "was only ever kept.",
            "The prints, under the lamp",
            chapterDefId = "arc10_ch2_frame_by_frame"),
        // ---------- frame 2: the Bijou's enlargement ----------
        Clue10("butters_ladder", Kind10.WITNESS, 1, 2, 1,
            "The ladder's foot (Butters)",
            "The anniversary week's program boy, on the " +
                "record, complete and unrepentant: he " +
                "handed out programs at the Bijou all that " +
                "week and held the foot of the ladder the " +
                "morning the enlargement went up over the " +
                "lobby doors — the anniversary morning, in " +
                "the anniversary week, under the " +
                "anniversary marquee, which he remembers " +
                "because it is the week he was trusted " +
                "with a ladder, a fact he has dined out on " +
                "since. He has never, he will tell you " +
                "twice, been at any reopening. The " +
                "reopening was the year before he was " +
                "trusted with anything. What went up that " +
                "morning is what's in the photograph: the " +
                "lobby, the couples, and the marquee, big " +
                "as anything, naming the anniversary " +
                "feature — the plate underneath it saying " +
                "otherwise notwithstanding. Plates, in his " +
                "experience, are put up by grown-ups, and " +
                "grown-ups, in his experience, copy things " +
                "down wrong and then engrave it.",
            "The ladder's foot (Butters)",
            npc = "Butters",
            spoken = "Well, I held the foot of that " +
                "ladder myself, the mornin' that picture " +
                "went up over the doors — I handed out " +
                "programs all anniversary week, on " +
                "account of my dad sayin' a boy with a " +
                "job is a boy out of trouble, which " +
                "turned out to be mostly true. I wasn't " +
                "at any reopenin', though. The reopenin' " +
                "was the year before — nobody trusted me " +
                "with a ladder back then, on account of " +
                "the incident. The picture that went up " +
                "is the anniversary one: you can see the " +
                "marquee right in it, big as anything, " +
                "with the anniversary picture showin'. " +
                "The little plate underneath says " +
                "reopenin', but plates are put up by " +
                "grown-ups, and grown-ups copy things " +
                "down wrong and then they engrave it so " +
                "it's wrong forever, official."),
        Clue10("plate_marquee", Kind10.LOOK, 1, 2, 1,
            "The marquee, in the frame",
            "The enlargement read the darkroom's way — " +
                "the background first, because nobody " +
                "poses a background: through the lobby's " +
                "glass doors, lettered and lit, the " +
                "marquee names the feature the Bijou was " +
                "showing the night this frame was taken. " +
                "That feature played the theater's " +
                "anniversary week — a full year after the " +
                "reopening the brass plate claims, a fact " +
                "the Bijou's own program archive confirms " +
                "in one glance. The photograph dates " +
                "itself, honestly, in letters a foot high, " +
                "to anyone who reads what is IN it instead " +
                "of what is UNDER it. The faces agree " +
                "with the frame too: every person in it " +
                "was in that lobby that week, checkably — " +
                "it is only the night that's wrong, and " +
                "the night is the plate's contribution.",
            "The marquee, in the frame",
            locId = "Bijou",
            foundText = "📷 THE MARQUEE, IN THE FRAME: " +
                "through the glass doors behind the " +
                "smiling couples, the marquee names its " +
                "feature in letters a foot high — and " +
                "that feature played this theater's " +
                "anniversary week, a full year after the " +
                "reopening night engraved on the plate " +
                "below."),
        Clue10("loan_form", Kind10.LOOK, 2, 2, 3,
            "The loan-and-label book, both lines",
            "The enlargement's entry in the Bijou's " +
                "loan-and-label book, read at last with " +
                "the display's hanging list beside it — " +
                "the list is what gives the book's job " +
                "numbers their meaning: two lines, as the " +
                "book gives every frame two lines. The " +
                "item line describes this enlargement " +
                "exactly — the lobby, the couples, the " +
                "feature on the marquee — and dates it to " +
                "the anniversary week. The caption line, " +
                "in the clerk's fair copying hand, names " +
                "the reopening night — which is the " +
                "caption of the entry ABOVE it in the " +
                "book, the reopening still, copied down " +
                "one line too far and engraved on brass. " +
                "The photograph never lied. Its label was " +
                "miscopied by a clerk, at speed, a " +
                "generation ago, and brass has been " +
                "believing it ever since.",
            "The enlargement's entry in the Bijou's " +
                "loan-and-label book: two lines — the " +
                "item, described by the feature on its " +
                "marquee; the caption, naming a night a " +
                "year earlier. Its job number points at a " +
                "hanging list you haven't read yet, and " +
                "office paper only tells its story in " +
                "company.",
            locId = "Bijou",
            foundText = "📷 THE LOAN-AND-LABEL BOOK: the " +
                "enlargement's entry, in two lines — the " +
                "item described by the feature on its " +
                "marquee, the caption naming a night a " +
                "year before that feature played. You " +
                "can read every word and understand none " +
                "of it: its job number's list is " +
                "elsewhere, and paper this dull keeps " +
                "its meaning in company."),
        // ---------- frame 3: the album page ----------
        Clue10("stuart_edge", Kind10.WITNESS, 2, 3, 2,
            "The man at the edge (Stuart)",
            "The figure at the album frame's edge, " +
                "identified by the only authority on him: " +
                "it is Stuart McCormick, nineteen years " +
                "younger and in the same posture — edge of " +
                "the frame, thumb up, halfway out of the " +
                "photograph and entirely at home there. He " +
                "was holding the ladder that day, he will " +
                "tell you; he is holding something in half " +
                "the frames in this town, because holding " +
                "things is where a man stands when the " +
                "photograph happens. His presence in the " +
                "frame is in the print, in the mount " +
                "card's count, and in the negative log, " +
                "all three, from the day itself. The only " +
                "record he is missing from is his own " +
                "family's memory — which edited him out " +
                "the way it edits out everybody who was " +
                "holding the ladder. He bears this the " +
                "way he bears most things: at the edge, " +
                "thumb up.",
            "The man at the edge (Stuart)",
            npc = "Stuart",
            spoken = "That fella at the edge there? " +
                "That's me. Nineteen years younger and " +
                "the same amount in the picture, which " +
                "is: some. I was holdin' the ladder — " +
                "Carol wanted the yard spruced for the " +
                "photograph, and I was up and down that " +
                "ladder all mornin', and when the fella " +
                "with the camera said 'everybody,' " +
                "apparently I was part of everybody, " +
                "edge-wise. I've been in the edge of " +
                "half the photographs in this town, " +
                "son. Weddings, birthdays, one fire. " +
                "It's not a mystery. It's where the " +
                "work is. You want me in the middle of " +
                "a photograph, you're gonna have to " +
                "take it while I'm sittin' down, and I " +
                "don't sit down much."),
        Clue10("carol_memory", Kind10.WITNESS, 2, 3, 2,
            "The album's keeper (Carol)",
            "The album's keeper, on the record, with the " +
                "page open in front of her: she remembers " +
                "the day of the big storm — and pressed, " +
                "gently, by her own album, she remembers " +
                "the rest of it too: the storm was the " +
                "week AFTER the photograph, and the " +
                "photograph was the calm the family has " +
                "always meant by 'before the storm,' the " +
                "yard spruced, the kids clean, her husband " +
                "somewhere about the ladder. The date on " +
                "the mount card is her own mother's hand, " +
                "and she has read past it her whole life " +
                "the way you read past your own name. " +
                "Nobody deceived her. Her memory kept the " +
                "feeling and filed the picture under the " +
                "loudest day of the month — which is what " +
                "memories are FOR. The card kept the " +
                "date. Between the two of them, the whole " +
                "truth was in the house the entire time.",
            "The album's keeper (Carol)",
            npc = "Carol",
            spoken = "Well now, I have told that " +
                "photograph wrong for nineteen years, " +
                "and I'll own it in front of God and the " +
                "display committee. I said 'the day of " +
                "the storm' because that's the day that " +
                "week IS, in this family — the tree came " +
                "down, the fence went, your father's " +
                "ladder ended up in the Hendersons' " +
                "yard. But the photograph's the week " +
                "BEFORE. The calm. Look at the sky in " +
                "it — that's not a storm sky, that's a " +
                "laundry sky. And Stuart's in it, clear " +
                "as print, holdin' that same ladder. My " +
                "mama wrote the date on the card herself. " +
                "I have been readin' past my own mama's " +
                "handwriting for nineteen years. There's " +
                "probably a lesson in that. Don't tell " +
                "me what it is."),
        Clue10("album_weather", Kind10.LOOK, 2, 3, 2,
            "The album page's mount card",
            "The mount card under the album page's " +
                "print, filled in by the photo club when " +
                "the print was made — the club printed " +
                "half the town's albums in those years and " +
                "kept its cards the way it kept its " +
                "negatives: completely. The card's fields " +
                "read: the McCormick yard; the date, a " +
                "full week before the storm the family " +
                "remembers; the weather line, CLEAR; and " +
                "the frame's contents, counted the day " +
                "the print was made — five, one at the " +
                "edge, uninvited, noted in the card's " +
                "own parentheses and never once a " +
                "mystery until this month. Every field " +
                "the record keeps agrees with the frame " +
                "it describes. The photograph is exactly " +
                "what its record says it is. What slid — " +
                "a week, a sky, one whole husband — was " +
                "the remembering, and remembering is not " +
                "evidence, however warm it is.",
            "The album page's mount card",
            locId = "Elementary",
            foundText = "📷 THE ALBUM PAGE'S MOUNT CARD: " +
                "the club's fields, filled in when the " +
                "print was made — the McCormick yard; a " +
                "date a full week before the storm; the " +
                "weather line reading CLEAR; and the " +
                "contents: five, one at the edge, " +
                "uninvited, noted in parentheses.")
    )

    val CLUE10_BY_ID: Map<String, Clue10> = CLUES10.associateBy { it.id }
    val CLUE10_IDS: Set<String> = CLUES10.map { it.id }.toSet()

    fun clueCount10(state: GameState): Int = state.mystery10Clues.size

    /** Evidence exists in the world once its chapter has
     *  begun and the case is still open. */
    fun clueAvailable10(state: GameState, clue: Clue10): Boolean =
        !state.arc10Retired && state.arc10Chapter > clue.minChapter

    /** A piece is LIVE — spendable, settling — when it is
     *  in hand AND the chapter that unlocks its context
     *  has begun. Ordinary pieces are live the moment they
     *  exist. The two locked pieces sit in hand, honestly
     *  inert, until their context arrives: they settle
     *  nothing, prove nothing. Derived, never stored. */
    fun clueLive10(state: GameState, clue: Clue10): Boolean =
        clue.id in state.mystery10Clues &&
            state.arc10Chapter > clue.unlockChapter

    /** The ids of everything currently spendable. */
    fun liveClueIds10(state: GameState): Set<String> =
        CLUES10.filter { clueLive10(state, it) }.map { it.id }.toSet()

    /** Live pieces tagged to one frame — the only
     *  currency a reading can be bought with. */
    fun liveEvidence10(state: GameState, f: Frame10): Int =
        CLUES10.count { it.site == f.index && clueLive10(state, it) }

    /** Grant a piece of evidence exactly once. The ticker
     *  + the town log hear about it; the board keeps it
     *  forever. A locked piece announces its own
     *  unreadability — honestly, at the moment of finding,
     *  so no player ever mistakes holding for knowing. */
    fun grantClue10(state: GameState, clueId: String): Boolean {
        val clue = CLUE10_BY_ID[clueId] ?: return false
        if (clueId in state.mystery10Clues) return false
        state.mystery10Clues.add(clueId)
        val live = clueLive10(state, clue)
        state.events.add(if (live)
            "🧩 EVIDENCE ${state.mystery10Clues.size}/${CLUES10.size}: ${clue.title}. " +
                "(Journal → 🔍 MYSTERY)"
        else
            "🧩 IN HAND, NOT YET READABLE (${state.mystery10Clues.size}/${CLUES10.size}): " +
                "${clue.title}. It doesn't make sense yet — its context is still " +
                "coming, and in this case context is a kind of handwriting.")
        while (state.events.size > 40) state.events.removeAt(0)
        townLogLine(state, if (live) "🧩 Evidence proven: ${clue.title}"
            else "🧩 Evidence in hand, not yet readable: ${clue.title}")
        return true
    }

    // ---------------- discovery: witnesses, looks, hands ----------------

    /** Talk-to discovery: if [npcName] is a live witness
     *  right now, their line — and the evidence it grants. */
    fun witnessLine10(state: GameState, npcName: String): Line? {
        val clue = CLUES10.firstOrNull {
            it.spoken != null && it.npc == npcName
        } ?: return null
        if (!clueAvailable10(state, clue)) return null
        if (!grantClue10(state, clue.id)) return null
        return Line(npcName, clue.spoken ?: "")
    }

    /** Look discovery: the sentence a Look at [locId]
     *  reports the first time it finds each piece of
     *  evidence there — one at a time, in the order the
     *  case has opened them. A locked piece found early
     *  reports its inert face, and only its inert face:
     *  what it IS, never what it means. */
    fun lookClueText10(state: GameState, locId: String): String? {
        val clue = CLUES10.firstOrNull {
            it.kind == Kind10.LOOK && it.locId == locId &&
                it.id !in state.mystery10Clues &&
                clueAvailable10(state, it)
        } ?: return null
        if (!grantClue10(state, clue.id)) return null
        return clue.foundText
    }

    /** Hands-dirty discovery: called from Engine.bumpParticipation
     *  whenever a participation point lands during a chapter. */
    fun noteParticipation10(state: GameState, defId: String) {
        val clue = CLUES10.firstOrNull {
            it.kind == Kind10.PARTICIPATION && it.chapterDefId == defId
        } ?: return
        if (!clueAvailable10(state, clue)) return
        if (grantClue10(state, clue.id)) {
            state.events.add("🧩 Up close, something clicks: ${clue.title}")
            while (state.events.size > 40) state.events.removeAt(0)
        }
    }

    // ---------------- THE COMPARISON (derived, never stored) ----------------
    // The only stored facts are the readings themselves
    // and the evidence in hand. Every verdict below is a
    // pure function of those recorded inputs, recomputed
    // on every read — the ledger discipline of the audit
    // case, applied to photographs: a frame can't be
    // argued with, only re-read.

    /** A frame's verdict, derived: OPEN (unread),
     *  COMMITTED (read, standing, untested — its settling
     *  piece is not live), PROVEN (read as the frame
     *  actually ran, with the settling piece live in
     *  hand), or REFUTED (read against the frame, with
     *  the settling piece live — the reading stands
     *  struck and can be read again). A standing reading
     *  cannot be re-read; a refuted one must be, before
     *  the board can settle. */
    fun frameVerdict10(state: GameState, f: Frame10): String {
        val called = state.arc10Calls[f.id] ?: return "OPEN"
        if (f.proofBy !in liveClueIds10(state)) return "COMMITTED"
        return if (called == f.truth) "PROVEN" else "REFUTED"
    }

    fun calledCount10(state: GameState): Int =
        FRAMES10.count { it.id in state.arc10Calls }

    fun provenCount10(state: GameState): Int =
        FRAMES10.count { frameVerdict10(state, it) == "PROVEN" }

    fun refutedCount10(state: GameState): Int =
        FRAMES10.count { frameVerdict10(state, it) == "REFUTED" }

    /** Every frame has a reading written on it —
     *  standing, proven, or even refuted. (The finale's
     *  own gates read deeper than completeness; see
     *  hangingDue and chooseFinale10.) */
    fun boardCalled10(state: GameState): Boolean =
        FRAMES10.all { it.id in state.arc10Calls }

    /** No reading on the board stands refuted — the
     *  state the hard call is gated on, with completeness
     *  and the two load-bearing frames proven. */
    fun boardClean10(state: GameState): Boolean =
        refutedCount10(state) == 0

    /** Whether [f] can be read (or re-read) right now:
     *  its chapter has begun, enough live evidence tagged
     *  to it is in hand, and either it stands unread or
     *  its reading stands refuted. */
    fun frameCallable10(state: GameState, f: Frame10): Boolean {
        if (state.arc10Retired) return false
        if (state.arc10Chapter <= f.minChapter) return false
        if (liveEvidence10(state, f) < f.callPrice) return false
        val v = frameVerdict10(state, f)
        return v == "OPEN" || v == "REFUTED"
    }

    /** The frame the reading dialog should offer next:
     *  the first unread frame (in board order) whose
     *  evidence price is met — or, when nothing new can
     *  be read, the first REFUTED frame, offered for
     *  re-reading. New readings come before revisions,
     *  the way new writing came before revision in the
     *  cases before this one. */
    fun pendingFrame10(state: GameState): Frame10? {
        if (state.arc10Retired) return null
        for (f in FRAMES10) {
            if (f.id !in state.arc10Calls && frameCallable10(state, f))
                return f
        }
        for (f in FRAMES10) {
            if (frameVerdict10(state, f) == "REFUTED" &&
                frameCallable10(state, f)) return f
        }
        return null
    }

    /** Read (or re-read, after a refutation) a frame:
     *  genuine, misfiled, or composite. The reading is
     *  recorded; the verdict is NOT — it is derived on
     *  every read, so a reading written in week two can
     *  be refuted in week four without anything being
     *  edited: the settling piece simply arrived, or
     *  finished becoming readable. */
    fun chooseCall10(state: GameState, frameId: String,
                    call: String): Boolean {
        if (call !in CALL_KEYS) return false
        val pending = pendingFrame10(state) ?: return false
        if (pending.id != frameId) return false
        val revising = frameId in state.arc10Calls
        state.arc10Calls[frameId] = call
        val v = frameVerdict10(state, pending)
        state.events.add(when {
            revising ->
                "✒️ STRUCK AND RE-READ: '${pending.title}' — " +
                    "the board takes the new reading. ($v)"
            v == "PROVEN" ->
                "✒️ READ — AND PROVEN ON ARRIVAL: " +
                    "'${pending.title}'. The settling piece " +
                    "already in hand backs this reading."
            v == "REFUTED" ->
                "✒️ READ — AND REFUTED ON ARRIVAL: " +
                    "'${pending.title}'. The settling piece " +
                    "already in hand settles the frame the " +
                    "other way. The reading stands struck. " +
                    "It can be read again."
            else ->
                "✒️ READ: '${pending.title}' — the " +
                    "reading stands, untested. Its settling " +
                    "piece hasn't surfaced, or hasn't become " +
                    "readable. Yet."
        })
        while (state.events.size > 40) state.events.removeAt(0)
        townLogLine(state, "✒️ Comparison board: '${pending.title}' read ($v)")
        return true
    }

    // ---------------- scheduling ----------------

    /** The chapter that should take today's event slot, if
     *  any. Also SCHEDULES the case: the first ask after
     *  the audit case retires books chapter 1 ten days out
     *  from that retirement. While any earlier case is
     *  open this never fires — and never even books the
     *  date: the mystery slot has one case at a time.
     *  Chapters defer, day by day, past holiday episodes,
     *  like every case before them. */
    fun dueChapter10(state: GameState): ShenaniganDef? {
        if (state.arc10Retired) return null
        if (!state.arc9Retired) return null
        if (state.arc10Chapter >= CHAPTERS10.size) return null
        if (state.arc10DueAbsDay < 0) {
            state.arc10DueAbsDay = if (state.arc10Chapter == 0) {
                val anchor = if (state.arc9RetiredDay >= 0)
                    state.arc9RetiredDay else state.absDay
                maxOf(state.absDay, anchor + OPEN_DELAY_DAYS)
            } else state.absDay
        }
        if (state.absDay < state.arc10DueAbsDay) return null
        if (state.holidayId != null) return null // defer — holidays keep their day
        return CHAPTERS10[state.arc10Chapter]
    }

    /** Engine hook: a chapter just took the slot. Books it
     *  and sets the next chapter's due day a week out. */
    fun noteChapterStarted10(state: GameState, def: ShenaniganDef) {
        val idx = chapterIndexOf10(def.id)
        if (idx < 0) return
        state.arc10Started = true
        state.arc10Chapter = idx + 1
        state.arc10DueAbsDay = if (idx == CHAPTERS10.size - 1) -1
            else state.absDay + CHAPTER_GAP_DAYS
    }

    // ---------------- the finale: the rehanging ----------------

    /** The rehanging can be played: the opening is
     *  airing, its climax has begun, no call is locked
     *  yet, and the board is COMPLETE — every frame read.
     *  The committee waits on a finished board the way
     *  the chamber waited on a finished ledger: you
     *  cannot open a wall for the room until you have
     *  read it for yourself. */
    fun hangingDue(state: GameState): Boolean {
        if (state.arc10Retired || state.arc10Choice.isNotEmpty()) return false
        if (state.shenaniganId != FINALE10_ID) return false
        if (Shenanigans.stageFor(state.timeMin) < 2) return false
        return boardCalled10(state)
    }

    /** Whether READ THE FRAMES ALOUD is available: the
     *  board's final derived state can carry it — the
     *  loaned print and the Bijou's enlargement stand
     *  PROVEN (the two load-bearing frames: the made one
     *  the town can't account for, and the mislabeled one
     *  whose "impossible" faces prove that a wrong night
     *  is a caption's sin, not a frame's), every frame is
     *  read, no reading stands refuted, and at least
     *  three frames stand proven. The gate reads the
     *  FINAL state of the board, never first
     *  impressions: a board with a struck reading still
     *  standing on it buys nothing, and neither does a
     *  board the evidence never finished settling. */
    fun readEligible10(state: GameState): Boolean =
        frameVerdict10(state, FRAMES10[0]) == "PROVEN" &&
            frameVerdict10(state, FRAMES10[2]) == "PROVEN" &&
            boardCalled10(state) && boardClean10(state) &&
            provenCount10(state) >= 3

    /** Whether NAME THE BOMBER is available: at least
     *  one frame stands PROVEN — the town will follow a
     *  naming that has any settled frame under it, and
     *  it is reaching for a name either way. */
    fun nameEligible10(state: GameState): Boolean =
        provenCount10(state) >= 1

    /** Lock the rehanging's call. The per-call gates are
     *  enforced here, in the module, not just in the
     *  dialog: a call the board can't carry is refused
     *  outright. */
    fun chooseFinale10(state: GameState, choice: String): Boolean {
        if (choice !in CHOICE_KEYS) return false
        if (!hangingDue(state)) return false
        if (choice == "read" && !readEligible10(state)) return false
        if (choice == "name" && !nameEligible10(state)) return false
        state.arc10Choice = choice
        state.events.add(when (choice) {
            "read" ->
                "🗣️ You stand up in the hall, board in " +
                    "hand, and you read the frames aloud — " +
                    "print by print, check by check, the " +
                    "way the records settled each one. The " +
                    "room hears its own wall explained."
            "name" ->
                "⚖️ You give the room the name its theory " +
                    "has been reaching for — the face at " +
                    "the edge, the thumb in the frames. The " +
                    "room takes it the way rooms take " +
                    "names: all at once, and for good."
            "stand" ->
                "🖼️ You let it hang as loaned. Four " +
                    "photographs, four captions, and a " +
                    "board that keeps its readings where " +
                    "only its keeper can see them — the " +
                    "display opens the way it arrived: " +
                    "unexplained, under glass."
            else ->
                "🚶 You walk away. The program has the " +
                    "welcome, the refreshments, the labeled " +
                    "space. It doesn't have the board, and " +
                    "it doesn't have you."
        })
        while (state.events.size > 40) state.events.removeAt(0)
        return true
    }

    data class HangingOutcome(
        val branch: String,          // "complete" | "framed" | "hung" | "unexposed"
        val outcome: String,         // the wrap's outcome line
        val recapLines: List<String> // extra credits lines for the episode page
    )

    /** Resolve the finale at wrap time: pick the landing,
     *  stamp the arc RETIRED forever, hand the wrap its
     *  words. Calls are re-gated here against the final
     *  board, exactly as locked — verdicts are monotone
     *  in the inputs that matter (evidence is never
     *  un-gathered, chapters never un-air), so a call
     *  that was lawful when locked is lawful now;
     *  anything else degrades to the unexposed landing,
     *  the discipline of every case before this one. A
     *  closing nobody played is unexposed by definition:
     *  the wall, opened by ribbon and nearness. */
    fun resolveHanging(state: GameState): HangingOutcome {
        val chosen = state.arc10Choice
        val complete = boardCalled10(state)
        val branch = when {
            chosen == "read" && complete && readEligible10(state) -> "complete"
            chosen == "name" && complete && nameEligible10(state) -> "framed"
            chosen == "stand" && complete -> "hung"
            else -> DEFAULT_BRANCH
        }
        state.arc10Branch = branch
        state.arc10Retired = true
        state.arc10RetiredDay = state.absDay
        val held = clueCount10(state)
        return when (branch) {
            "complete" -> HangingOutcome(branch,
                "The frames, read aloud, hold up. Four " +
                    "photographs, explained in a full hall, " +
                    "from the records themselves: a reprint " +
                    "whose suspect flatness was a stamp on " +
                    "its back all along; an enlargement " +
                    "whose impossible night was a clerk's " +
                    "line copied one line too far; an album " +
                    "page whose record kept a date, a sky " +
                    "and a husband that memory had " +
                    "misfiled; and a portrait that was " +
                    "finished — as a gift, in the open, by " +
                    "the woman whose job the camera was, " +
                    "because her staff asked not to be " +
                    "remembered without her. The caption " +
                    "comes off the portrait tonight. In its " +
                    "place, in the secretary's fair hand: " +
                    "THE STAFF, COMPLETE — WITH THANKS.",
                listOf(
                    "📷 THE PORTRAIT, COMPLETED: It is the " +
                        "strangest thing a verdict can be " +
                        "in this town: an investigation that " +
                        "ends with a thank-you read aloud. " +
                        "The sponsor of the photo club took " +
                        "the sitting, as sponsors do, from " +
                        "behind the camera — five in the " +
                        "frame, as taken, exactly as the " +
                        "mount card says. The staff asked " +
                        "her to finish it properly, and she " +
                        "did, in the club darkroom, from the " +
                        "sitting's own negative and a frame " +
                        "of herself from the same week's " +
                        "roll. It hung in the front hall for " +
                        "years as the portrait of a staff " +
                        "that included its photographer. " +
                        "Tonight the town learned it had " +
                        "been looking at a gift and calling " +
                        "it a document. It can be both. It " +
                        "just has to say so on the card.",
                    "📷 WHAT THE ROOM DOES WITH IT: Nobody " +
                        "apologizes; this is still South " +
                        "Park. The man by the refreshments " +
                        "finishes his punch and leaves at a " +
                        "pace just under a statement, " +
                        "vindicated in a way he does not get " +
                        "to enjoy, because nobody was ever " +
                        "going to thank him for standing " +
                        "still in the right places for forty " +
                        "years. The frame in the hall goes " +
                        "home un-carried. And the portrait " +
                        "keeps its six faces — with one " +
                        "correction outstanding, which the " +
                        "committee has promised the room in " +
                        "writing: its true card, printed " +
                        "whole, within the week."))
            "framed" -> HangingOutcome(branch,
                "The room gets its name. The board had a " +
                    "settled frame in it — one photograph " +
                    "really had been made, once, somewhere — " +
                    "and the hall read that the only way " +
                    "halls read making: as a face, at the " +
                    "edge, with a thumb. The name goes into " +
                    "the program spelled right, which the " +
                    "program does for everyone, whatever the " +
                    "program is worth. The man at the edge " +
                    "says his piece, standing up, to the " +
                    "record: he was holding the ladder. The " +
                    "record keeps everything in this town, " +
                    "including the objection. It just keeps " +
                    "it in a smaller hand.",
                listOf(
                    "📷 THE NAME: The nearest face to the " +
                        "frames was the McCormick man's, and " +
                        "nearness, in a full room, is nine " +
                        "points of the law. The edge is his " +
                        "because the ladders are his. The " +
                        "thumb is his because the work is. " +
                        "The case is his because the room " +
                        "needed a case with a face in it, " +
                        "and the board's one made photograph " +
                        "gave the room its permission.",
                    "📷 WHAT THE ROOM DOES WITH IT: The " +
                        "captions stay up — all four, " +
                        "though the board in one Journal " +
                        "says three of the four frames were " +
                        "never a maker's work at all. The " +
                        "portrait keeps its six faces, " +
                        "unexplained, which the committee " +
                        "calls next season's problem in the " +
                        "tone of a woman filing a storm. " +
                        "The archive print goes back to its " +
                        "drawer. The album goes home. Faces " +
                        "all stay exactly where they stood. " +
                        "Only the story moved."))
            "hung" -> HangingOutcome(branch,
                "It hangs as loaned. The display opens " +
                    "with its four captions converted, by " +
                    "vote of the room, into four facts: the " +
                    "portrait keeps its six faces and its " +
                    "pencil, the plate keeps its wrong " +
                    "night, and the board that holds what " +
                    "each frame actually is goes home in " +
                    "one person's pocket, complete. In one " +
                    "Journal, the comparison says what " +
                    "each photograph is, frame by frame, " +
                    "proven. The wall does not subscribe " +
                    "to Journals.",
                listOf(
                    "📷 THE HANGING: It is not nothing, a " +
                        "wall. The town has opened displays " +
                        "worse ways than by letting them " +
                        "hang: by cape, by applause, by cape " +
                        "again. This time the board is " +
                        "complete — every frame read, " +
                        "$held pieces of the comparison in " +
                        "one person's hands — and it is " +
                        "hung complete, which means the " +
                        "truth of the four photographs now " +
                        "exists in exactly two places: the " +
                        "board, and the frames themselves, " +
                        "hanging there, being it.",
                    "📷 WHAT THE ROOM DOES WITH IT: The " +
                        "committee gets its opening, which " +
                        "is the ending it ordered, and " +
                        "thanks the comparison in the " +
                        "program for its service. The " +
                        "portrait's keeper walks home past " +
                        "her portrait and its sixth face " +
                        "and says nothing to it, the way you " +
                        "say nothing to a question you've " +
                        "stopped asking out loud."))
            else -> HangingOutcome(branch,
                "The wall opens the way openings open " +
                    "without the person holding the whole " +
                    "board: by ribbon, by feel, by the " +
                    "sound a hall makes instead of an " +
                    "answer. The labeled space in the " +
                    "program fills with applause. The " +
                    "captions go back to their frames " +
                    "un-corrected; the question stays " +
                    "penciled under the portrait a while " +
                    "longer, fading, the way pencil does; " +
                    "and four photographs go on hanging " +
                    "in their places, unexplained, " +
                    "unmoved, and — in the way of things " +
                    "nobody ever actually reads — slowly " +
                    "becoming part of the wall.",
                listOf(
                    "📷 THE UNEXPOSED WALL: There is a " +
                        "version of this display that adds " +
                        "up. It exists in one Journal, in " +
                        "one hand, finished or not. The " +
                        "town's version is shorter: somebody " +
                        "made a photograph, somewhere, and " +
                        "the man at the edge stood very " +
                        "still while the room decided how it " +
                        "felt about that. Feelings, " +
                        "programmed, are a kind of caption " +
                        "too. They are just not the kind you " +
                        "can check.",
                    "📷 WHAT THE ROOM DOES WITH IT: The " +
                        "frame stays in the hall. The " +
                        "refreshments empty without a word. " +
                        "And on the walk home, past the " +
                        "school, the theater, the album on " +
                        "its family's shelf, every " +
                        "photograph hangs in the dark being " +
                        "exactly what it always was — which " +
                        "the board knows, and the night " +
                        "doesn't."))
        }
    }

    fun applyHangingEffects(state: GameState, branch: String) {
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
            "complete" -> {
                befriend("Victoria", 3)
                befriend("Jimmy", 2)
                befriend("Carol", 1)
                ripple("school_staff", 1,
                    "The Staff watched a full hall hear a portrait explained from the records — a log, a mount card, an unsigned card read aloud — and nobody named a teacher. The Staff is framing the new caption. The Staff frames everything now. It's topical.")
                ripple("parents", -1,
                    "The Parents came for a forgery and got a thank-you note instead. Half the Parents are re-telling it already. They're telling the ladder part wrong, but they're telling it.")
                state.arc10LabelPending = true
                state.arc10LabelDay = state.absDay + LABEL_RETURN_DELAY_DAYS
                state.lifeText["mark:Elementary"] =
                    "The staff portrait hangs in the hall " +
                        "with its six faces — and under it, " +
                        "where the penciled question was, in " +
                        "the secretary's fair hand: THE STAFF, " +
                        "COMPLETE — WITH THANKS. One " +
                        "correction is still outstanding, " +
                        "promised in writing in the program: " +
                        "the portrait's true card, printed " +
                        "whole, and beside it the afternoon " +
                        "it was completed from, loaned openly " +
                        "this time."
                townLogLine(state, "📷 COMPLETE: the frames " +
                    "were read at the Rehanging. No frame " +
                    "was forged in the night. The wall was " +
                    "read to its backings, in public, and " +
                    "the comparison held up.")
            }
            "framed" -> {
                befriend("Stuart", -4)
                befriend("McDaniels", 1)
                befriend("Victoria", 1)
                ripple("parents", 1,
                    "The Parents got their name at the Rehanging, before the refreshments, in the program, spelled right. The Parents are framing the program. The Bomber cape was not needed. The Parents call that a shame, a little, in private.")
                ripple("school_staff", -1,
                    "The Staff keeps the program of the naming in a hand slightly tighter than usual. One family in this town keeps a different record — every ladder its man was actually holding, and when, and for whom — and has started keeping it in writing. He's held a lot of ladders, over the years. None of them was a camera.")
                state.lifeText["mark:Elementary"] =
                    "The portrait keeps its six faces, " +
                        "unexplained, and the display's file " +
                        "is closed by name: THE MAN AT THE " +
                        "EDGE, singular, in the program, in " +
                        "the secretary's fair hand, spelled " +
                        "right. In the McCormicks' yard the " +
                        "ladder keeps its place by the fence. " +
                        "Standing at the edge was the case, " +
                        "and the case is closed, and closed " +
                        "things keep their shape."
                townLogLine(state, "📷 THE NAME: the wall " +
                    "was closed at the Rehanging, by name. " +
                    "The ladder man's objection is not in " +
                    "the headline. It is in the record, " +
                    "standing up, where it will stay.")
            }
            "hung" -> {
                befriend("Victoria", 2)
                befriend("Jimmy", 1)
                ripple("parents", 1,
                    "The Parents kept the captions they arrived with, which are the captions the Parents trust most: the ones already written. The pool on the Bomber paid out to nobody. The Parents call that a fair result.")
                state.lifeText["mark:Elementary"] =
                    "By the hall door hangs a small new " +
                        "card, in the secretary's fair hand: " +
                        "DISPLAYED AS LOANED. The portrait " +
                        "keeps its faces, the enlargement " +
                        "its night, the album its storm — " +
                        "and the town keeps its favorite " +
                        "shape for a question: a caption, " +
                        "penciled, under glass, marked " +
                        "UNRECONCILED."
                townLogLine(state, "📷 AS LOANED: four " +
                    "photographs, four captions, no " +
                    "reading. The board stays proven " +
                    "where only its keeper can read it.")
            }
            else -> {
                befriend("Stuart", -2)
                ripple("parents", -1,
                    "The Rehanging ran long and settled thin, and the Parents applauded in the places where an answer should have been. A few of them noticed, afterward, that the applause had a hole in it shaped exactly like a face at a frame's edge.")
                state.lifeText["mark:Elementary"] =
                    "The portrait still hangs in the hall " +
                        "with its penciled question fading — " +
                        "a wall opened by a room that didn't " +
                        "have the board: by ribbon, by " +
                        "nearness, by the sound a hall makes " +
                        "instead of an answer. The board of " +
                        "that comparison exists in exactly " +
                        "one Journal. It is not this " +
                        "building's copy. This building " +
                        "doesn't have one."
                townLogLine(state, "📷 THE UNREAD BOARD: the " +
                    "Rehanging happened without the " +
                    "comparison. The wall kept the shape " +
                    "it arrived with. The shape is wrong " +
                    "in a way only one person can prove, " +
                    "and that person wasn't in the room.")
            }
        }
    }

    // ---------------- the true card is printed (the COMPLETE landing's week after) ----------------

    /** The delayed landing of the completed reading:
     *  called daily by the engine; fires exactly once, a
     *  week after the frames were read — the portrait's
     *  true card is printed whole at last, and beside
     *  the portrait, in a smaller frame, hangs the
     *  afternoon it was completed from: the sponsor's
     *  own frame from the sitting week's roll, loaned
     *  openly this time, with its own honest card. Six
     *  faces in one frame; one face in the other; the
     *  wall, in public, in print, finally adds up. */
    fun noteLabelReprinted(state: GameState): Boolean {
        if (!state.arc10LabelPending) return false
        if (state.absDay < state.arc10LabelDay) return false
        state.arc10LabelPending = false
        state.arc10LabelDone = true
        state.events.add("📷 THE TRUE CARD: the committee " +
            "unveiled the portrait's new card in the hall " +
            "this morning — printed entire, the sitting, " +
            "the finishing, and the thanks, in the " +
            "secretary's fair hand. Beside the portrait, " +
            "in a smaller frame, hangs a picnic afternoon " +
            "from the same week's roll: the staff off " +
            "duty, the grass, and at its center their " +
            "sponsor, laughing, mid-sentence, entirely " +
            "herself. Its card reads simply: THE OTHER " +
            "HALF OF THE PORTRAIT. School groups are " +
            "already being brought past the pair of them. " +
            "It takes three minutes. It is the only three " +
            "minutes in the curriculum about how " +
            "photographs work.")
        while (state.events.size > 40) state.events.removeAt(0)
        state.lifeText["mark:Elementary"] =
            "The staff portrait hangs complete: six faces " +
                "in the big frame, one in the smaller " +
                "beside it, the true card printed whole " +
                "beneath them both — the sitting, the " +
                "finishing, and the thanks — in the same " +
                "fair hand as everyone else's captions. " +
                "Under the glass, the committee's line " +
                "stays: THE STAFF, COMPLETE — WITH " +
                "THANKS. The school reads the card to the " +
                "new kids every September. It takes three " +
                "minutes."
        townLogLine(state, "📷 THE TRUE CARD: six faces, " +
            "one portrait, two frames — the case stays " +
            "closed, in the right order, at every check " +
            "it actually happened.")
        return true
    }

    // ---------------- the aftermath (standing lines) ----------------

    private val AFTERMATH_LINES: Map<String, Map<String, List<String>>> = mapOf(
        "complete" to mapOf(
            "Victoria" to listOf(
                "The card is printed, the pair of them " +
                    "hang together, and I will tell you the " +
                    "part I told the committee in closed " +
                    "session: I took that sitting. Sponsor " +
                    "of the photo club, keeper of the " +
                    "darkroom, the woman behind the camera — " +
                    "so the honest portrait of this school's " +
                    "staff held five, and I was not in it, " +
                    "and when they asked me to complete it I " +
                    "said yes before they'd finished " +
                    "asking. I married two true negatives " +
                    "in my own darkroom and I hung the " +
                    "result in my own hall and I have " +
                    "walked past it twice a day for years, " +
                    "waiting — some years proudly, some " +
                    "years not — for somebody to count the " +
                    "faces out loud. You read it the way I " +
                    "hoped it would be read: as a gift " +
                    "first. It was always a gift first. It " +
                    "just never said so on the card.",
                "They bring the school groups past the " +
                    "pair of them now — the portrait and " +
                    "its other half — and the children ask " +
                    "which one is the real photograph, and " +
                    "the answer takes three minutes and " +
                    "involves a camera, a darkroom and a " +
                    "staff that refused to be remembered " +
                    "incompletely. Three minutes, and " +
                    "every child in this town now knows " +
                    "what a composite is and what it costs " +
                    "to leave one unlabeled. I couldn't " +
                    "have bought that lesson with a bond " +
                    "issue. A hanging bought it with a " +
                    "card.")),
        "framed" to mapOf(
            "Stuart" to listOf(
                "For the record — since the record is " +
                    "what we keep — nobody handed me a " +
                    "camera. In forty years I've been handed " +
                    "ladders, paint rollers, a marquee bulb " +
                    "board, two flooded gutters and a piano " +
                    "with a raccoon position on the lid, and " +
                    "I have held or fixed every one of 'em " +
                    "in full view of a photographer. There " +
                    "is a boy in this town takin' notes of " +
                    "every ladder I ever held, and when, " +
                    "and for who. The case was welcome to " +
                    "it any time. It was welcome to it " +
                    "during the case, come to that. Nobody " +
                    "asked. The room had its face, and " +
                    "faces, apparently, don't take " +
                    "questions. I'll be in the yard. It's " +
                    "where I was. It's in the records.",
                "Funny thing about that portrait, since " +
                    "the town's so sure of its shapes: I " +
                    "was at the edge of the sittin' too, " +
                    "nearly — held the reflector for the " +
                    "fella... for the photographer, out " +
                    "front of the hall, that whole " +
                    "afternoon. Got asked to step OUT of " +
                    "the light for the official one, which " +
                    "I did, bein' staff-adjacent at best. " +
                    "So the one frame in this town I'm " +
                    "accused of makin', I'm not even IN. " +
                    "I think about that whenever the town " +
                    "tells its version. The town remembers " +
                    "the edge. It never did learn the " +
                    "reflector.")),
        "hung" to mapOf(
            "Jimmy" to listOf(
                "The captions stand, the wall is opened, " +
                    "and I am told that is what closure " +
                    "looks like in a school hall. My " +
                    "hanging list still says what it says: " +
                    "six in the portrait, five on the card, " +
                    "and a note in my own hand that says " +
                    "'see the board.' There is no board, " +
                    "for the public. There's a Journal " +
                    "somewhere that has the whole " +
                    "comparison, frame by frame, and it " +
                    "isn't hanging next to the portrait, " +
                    "and the portrait goes on being the " +
                    "town's favorite photograph of a day " +
                    "that never quite happened. You read " +
                    "the whole of it, I think. You're the " +
                    "only one who did. It's a strange " +
                    "thing, being the editor of a wall in " +
                    "a town that has decided walls are " +
                    "furniture. Mind you keep yours.",
                "A kid asked me this week which face in " +
                    "the portrait doesn't belong. I told " +
                    "her the truth the school can afford: " +
                    "that all of them belong, and one of " +
                    "them belongs by a road the caption " +
                    "doesn't have room for, and that a " +
                    "road longer than its caption is not " +
                    "the same as a lie. She considered " +
                    "this with the seriousness it " +
                    "deserved, which is more than the " +
                    "opening managed. The children will be " +
                    "fine. The children read to the end " +
                    "of things.")),
        "unexposed" to mapOf(
            "Kenny" to listOf(
                "Mrrph mrph mmmph — mmph mrrm mrph! (I'm " +
                    "in nine photographs in that display " +
                    "and I was AT all nine, that's my " +
                    "whole statement. Every frame I'm in, " +
                    "I was in — that's not a defense, " +
                    "that's just what a photograph IS. The " +
                    "one they say somebody MADE? I wasn't " +
                    "even alive for it. Nobody checked. " +
                    "They just needed a face at an edge " +
                    "and mine's the famous one. Write that " +
                    "down somewhere. In a caption.)",
                "Mmph mrrm, mrph mrrph mmph. (Stu's in " +
                    "the old ones, I'm in the new ones — " +
                    "it's a family business, apparently. " +
                    "Edge of the frame, thumb up, forty " +
                    "years of McCormicks, and the town " +
                    "calls it a crime wave. It's a " +
                    "seating position. Somebody should " +
                    "tell them. They're probably still " +
                    "looking at the portrait.)")))

    /** The ending's standing voice: in each landing, one
     *  person says the uncomfortable thing, forever, to
     *  anyone who'll stand still for it — the keeper who
     *  finished a portrait, the man the room named, the
     *  editor whose captions stood, the boy whose face is
     *  a seating position. Null before the case closes,
     *  and for everyone else, always. */
    fun aftermathLine10(state: GameState, npcName: String): Line? {
        if (!state.arc10Retired) return null
        val bank = AFTERMATH_LINES[state.arc10Branch]?.get(npcName) ?: return null
        val idx = ((state.absDay % bank.size) + bank.size) % bank.size
        return Line(npcName, bank[idx])
    }

    // ---------------- the Journal board ----------------

    /** The tenth case shows once it exists: chapter 1
     *  has aired, a piece of evidence is in hand, a frame
     *  is read, or the case is closed. */
    fun boardVisible10(state: GameState): Boolean =
        state.arc10Started || state.mystery10Clues.isNotEmpty() ||
            state.arc10Calls.isNotEmpty() || state.arc10Retired

    fun statusStamp10(state: GameState): String = when {
        state.arc10Retired && state.arc10Branch == "complete" ->
            "📷 COMPLETE — the wall tells the truth, card by card"
        state.arc10Retired && state.arc10Branch == "framed" ->
            "📷 NAMED — the wall was closed by name at the Rehanging"
        state.arc10Retired && state.arc10Branch == "hung" ->
            "📷 AS LOANED — four photographs, four captions, one pocket of doubts"
        state.arc10Retired -> "🧊 UNEXPOSED — the Rehanging happened without the readings"
        state.shenaniganId == FINALE10_ID -> "🖼️ THE REHANGING — THE WALL, CORRECTED"
        state.arc10Started ->
            "📷 OPEN — chapter ${state.arc10Chapter} of ${CHAPTERS10.size} aired; the display is being compared"
        else -> "🔍 A CASE IS COMING"
    }

    /** What the town currently believes, by where the
     *  case is — believed, as the town believes
     *  everything, in one breath and at applause speed. */
    fun currentTheory10(state: GameState): String = when {
        state.arc10Retired && state.arc10Branch == "complete" ->
            "CLOSED, and completed: the town now tells " +
                "the portrait story as a story about a " +
                "gift — a photographer who was asked to " +
                "stand in her own photograph, a reprint " +
                "with its papers on its back, a plate " +
                "engraved from the wrong line of a book, " +
                "and a family whose memory edited a " +
                "husband. It takes longer to tell this " +
                "way. Nobody minds, which around here is " +
                "basically a parade. The man at the edge " +
                "is 'the ladder' now, in the town's " +
                "mouth, which is not an apology, but it " +
                "rhymes with one."
        state.arc10Retired && state.arc10Branch == "framed" ->
            "CLOSED, by name: the man at the edge, the " +
                "thumb, the yard, settled. The town tells " +
                "it at the speed of relief — a maker, a " +
                "name, a closed file — and only gets quiet " +
                "in one yard in town, where a list of " +
                "every ladder the named man ever held, and " +
                "when, and for whom, is being kept in " +
                "writing by his own family. Yards keep " +
                "what programs drop. This one will keep " +
                "it for years."
        state.arc10Retired && state.arc10Branch == "hung" ->
            "HUNG, which the town calls opened: a " +
                "photograph 'appeared' with a person in " +
                "it who was never in the sitting, in a " +
                "window the file doesn't specify, and the " +
                "wall says COMPLETE in pencil the town " +
                "has learned to read as a punchline. In " +
                "one Journal the comparison is settled " +
                "properly, frame by frame, and adds up. " +
                "It has exactly one reader. It had " +
                "exactly one all along."
        state.arc10Retired ->
            "DECIDED, in a room you weren't in: the " +
                "Rehanging ran on the captions alone — " +
                "four frames and a room's sense of " +
                "nearness — and the wall got the shape " +
                "rooms give things they cannot read: the " +
                "nearest face. The board in your Journal " +
                "is the only settled copy of that " +
                "comparison in existence. It is complete, " +
                "or it isn't. Either way it is unpublished, " +
                "which in this town is a tense of its own."
        !state.arc10Started ->
            "Nothing yet. The prints say what they say, " +
                "the captions claim what they claim, and " +
                "two of them are about to disagree by " +
                "exactly one face, in public, at a " +
                "hanging. Give it an anniversary."
        state.arc10Chapter <= 1 ->
            "Settled, at cape speed: somebody is making " +
                "photographs — a principal here, a couple " +
                "there, a face in a hallway of frames — " +
                "and the only problem is the small one " +
                "that every made thing in the case was " +
                "found standing in plain sight, dusty, " +
                "old, and utterly unsurprised to be " +
                "looked at. The town is holding both " +
                "halves of that sentence without apparent " +
                "discomfort. Towns can hold anything, if " +
                "you let them hold it in one breath."
        state.arc10Chapter == 2 ->
            "The frames have been read in person and " +
                "they hold: one of them dates itself, in " +
                "letters a foot high, to a different week " +
                "than its plate claims, and another's " +
                "record corrects the family that loaned " +
                "it. The town has begun, uneasily, to " +
                "suspect the captions rather than the " +
                "faces, which for this town is a " +
                "philosophical breakthrough on the scale " +
                "of plumbing. Suspecting the captions " +
                "doesn't name anybody, though, and the " +
                "town still wants a name by the " +
                "Rehanging. Wants are doing a lot of the " +
                "framing now."
        state.arc10Chapter == 3 ->
            "Voices are done and the records are open, " +
                "and the town's position has quietly " +
                "split: the people who have read the " +
                "backs of things think the captions are " +
                "guilty; the people who only read the " +
                "fronts think a person is. The board says " +
                "both halves are asking the wrong " +
                "question. The board doesn't get a vote. " +
                "The board gets read, or it gets hung — " +
                "next week, in the hall, in front of " +
                "everyone."
        else ->
            "Everything lookable has been looked at " +
                "twice and everything readable is read " +
                "except one form, whose context arrived " +
                "with the week, the way contexts do. " +
                "Tonight the room hears one version of " +
                "the wall: the captions', or the board's. " +
                "The four photographs hang in their " +
                "places either way. They have been the " +
                "most patient witnesses in the case, and " +
                "the only ones that never changed their " +
                "fields."
    }

    /** The Rehanging's published terms, in the board's
     *  own words — the forecast a player can plan by. */
    fun forecastLine10(state: GameState): String {
        val called = calledCount10(state)
        val proven = provenCount10(state)
        return "🖼️ THE REHANGING, AS IT STANDS: the " +
            "opening waits on a finished board — " +
            "$called/${FRAMES10.size} frames read, $proven " +
            "standing proven. READ THE FRAMES ALOUD needs " +
            "the board's final state: THE LOANED PRINT " +
            "and THE BIJOU'S ENLARGEMENT proven, every " +
            "frame read, no reading standing refuted, and " +
            "at least three frames proven. NAME THE BOMBER " +
            "needs any one frame proven. LET IT HANG AS " +
            "LOANED and WALK AWAY need nothing but you. A " +
            "refuted reading can be struck and read again; " +
            "a standing one cannot. Nothing moves the " +
            "board but evidence, frame by frame, field by " +
            "field."
    }

    /** The tenth case's board as plain lines (the
     *  Journal renders them below the audit case's; the
     *  suite reads them). THE COMPARISON: four
     *  photographs as rows, each carrying its claim and
     *  its frozen fields with their derived checks, its
     *  evidence, its reading, and its reading's verdict
     *  — derived on the spot: the board never stores a
     *  verdict, it re-reads the frames every time you
     *  open it. */
    fun boardLines10(state: GameState): List<String> {
        val out = mutableListOf<String>()
        out.add("🔍 $CASE_TITLE")
        out.add(statusStamp10(state))
        out.add("📷 THE COMPARISON: For the Elementary's " +
            "anniversary, the photo club hung the town in " +
            "photographs — and its centerpiece, a loaned " +
            "staff portrait, holds SIX faces where the " +
            "sitting's own record holds FIVE. A face the " +
            "mount card doesn't know, in the school's " +
            "portrait. A night the marquee doesn't know, " +
            "on the Bijou's plate. A storm the sky doesn't " +
            "know, in a family's memory. Nothing was taken " +
            "from anywhere. This board is for the " +
            "comparing: every photograph carries three " +
            "checks — its place, its day, its faces, each " +
            "one derived from the frame's own frozen " +
            "fields, never stored — and each frame waits " +
            "for your reading on what it is: genuine, " +
            "misfiled, or made.")
        out.add("🧩 THE EVIDENCE — what you hold " +
            "(${clueCount10(state)}/${CLUES10.size})" +
            if (!state.arc10Retired &&
                CLUES10.any { it.id in state.mystery10Clues && !clueLive10(state, it) })
                " — some of it is in hand but not yet readable: " +
                    "its context hasn't arrived, and in this " +
                    "case context is a kind of handwriting" else "")
        out.add("📷 THE FRAMES — four photographs, four " +
            "claims. The checks on each row are derived " +
            "from the frame's fields on every read: the " +
            "board keeps fields, not conclusions.")
        fun mark(ok: Boolean) = if (ok) "✓" else "✗"
        for (f in FRAMES10) {
            val placeName = LOCATIONS[f.place]?.name ?: f.place
            out.add("— ${f.title} — at $placeName; kept by ${f.keeper}")
            out.add("• its claim: ${f.claimLabel}")
            out.add("• place: the caption says ${f.placeClaim} — " +
                "the frame shows ${f.placeShown} ${mark(placeAgrees10(f))} · " +
                "day: the caption says ${f.dayClaim} — the frame " +
                "shows ${f.dayShown} ${mark(dayAgrees10(f))} · " +
                "faces: the frame holds ${f.figures.size} " +
                "(${f.figures.joinToString(", ")}) — the moment's " +
                "own record holds ${f.facesRecorded} " +
                "${mark(facesAgrees10(f))}")
            val held = CLUES10.filter { it.site == f.index &&
                it.id in state.mystery10Clues }
            if (held.isEmpty()) {
                out.add("• (no evidence yet — this row's " +
                    "pieces haven't surfaced)")
            }
            for (clue in held) {
                if (clueLive10(state, clue)) {
                    out.add("• ${clue.title} — ${clue.detail}")
                } else {
                    out.add("• 🔒 ${clue.title} — ${clue.inertDetail}")
                }
            }
            val live = liveEvidence10(state, f)
            val called = state.arc10Calls[f.id]
            if (called == null) {
                out.add(when {
                    state.arc10Chapter <= f.minChapter ->
                        "✏️ READING: unwritten — this row's " +
                            "week hasn't come; the comparison " +
                            "takes its frames in order, like " +
                            "its sittings."
                    live >= f.callPrice ->
                        "✏️ READING: unwritten — you hold " +
                            "$live live pieces for this row. " +
                            "It can be read."
                    else ->
                        "✏️ READING: unwritten — " +
                            "$live/${f.callPrice} live pieces " +
                            "in hand. Inert evidence doesn't " +
                            "count: it hasn't become readable " +
                            "yet, meaning-wise."
                })
            } else {
                val text = when (called) {
                    "genuine" -> "GENUINE — one shutter, " +
                        "honestly captioned: frame and record agree"
                    "misfiled" -> "MISFILED — one honest " +
                        "shutter wearing the wrong caption: the " +
                        "photograph is true, its label names " +
                        "another moment"
                    else -> "COMPOSITE — no single shutter " +
                        "holds it: the frame was made, out of " +
                        "more than one real moment"
                }
                val stamp = when (frameVerdict10(state, f)) {
                    "PROVEN" -> "✅ PROVEN — the settling " +
                        "piece in hand grades this reading TRUE"
                    "REFUTED" -> "💔 REFUTED — the settling " +
                        "piece in hand settles this frame the " +
                        "other way. The reading stands struck; " +
                        "it can be struck and read again"
                    else -> "📌 COMMITTED — standing, " +
                        "untested; the settling piece hasn't " +
                        "surfaced, or hasn't become readable"
                }
                out.add("🖊️ $stamp: “$text”")
            }
        }
        out.add("🗣️ THE TOWN'S THEORY: ${currentTheory10(state)}")
        if (!state.arc10Retired && state.arc10Started) {
            out.add(forecastLine10(state))
        }
        if (state.arc10Retired) {
            out.add(when (state.arc10Branch) {
                "complete" -> "🏁 The frames were read " +
                    "aloud: every frame read, the " +
                    "load-bearing pair proven, no reading " +
                    "left standing struck — and the wall, " +
                    "explained in a full hall from the " +
                    "records themselves, held up for the " +
                    "first time. No frame was forged in " +
                    "the night. The pencil came off. The " +
                    "thanks went up in their place, in the " +
                    "same fair hand."
                "framed" -> "🏁 The room got its name. One " +
                    "frame on the board really was made, " +
                    "and the town read that the only way it " +
                    "reads making: as a face, at an edge, " +
                    "with a thumb. The objection is not on " +
                    "this board. It is in a yard, in a list " +
                    "of ladders held going back decades, " +
                    "where it will stay, saying what their " +
                    "holder was actually holding."
                "hung" -> "🏁 It hung as loaned. The board " +
                    "was complete — every frame read, its " +
                    "verdicts earned — and the room kept " +
                    "the captions it arrived with anyway. " +
                    "What is proven stays proven, on this " +
                    "board, in full. What is hung stays " +
                    "hung. The two documents now disagree " +
                    "under glass, forever, politely."
                else -> "🏁 The Rehanging happened without " +
                    "the board's voice. ${clueCount10(state)} pieces " +
                        "of the comparison were in one " +
                        "person's hands that night. The room " +
                        "ran on the captions alone, the way " +
                        "it did the first time. Rooms keep " +
                        "doing that until somebody reads " +
                        "them a frame."
            })
            if (state.arc10Branch == "complete") {
                if (state.arc10LabelPending) {
                    out.add("📷 One thing is still out " +
                        "there: the portrait's true card, " +
                        "promised in writing in the program. " +
                        "Some mornings are worth walking " +
                        "past the hall for.")
                } else if (state.arc10LabelDone) {
                    out.add("📷 The true card is printed — " +
                        "six faces, one portrait, two " +
                        "frames, the smaller one loaned " +
                        "openly at last, and for once " +
                        "nobody in town is demanding the " +
                        "pair of them be explained. The " +
                        "file stays closed: this is what a " +
                        "closed file looks like when it's " +
                        "developed right.")
                }
            }
        }
        return out
    }

    // ---------------- the long tail (ambient callbacks) ----------------

    private val CALLBACKS10 = mapOf(
        "complete" to listOf(
            "The portrait? Completed, read aloud, whole " +
                "hall. Turns out the extra face was the " +
                "photographer, put in as a thank-you, and " +
                "half the town's 'forgeries' were labels. " +
                "Town's still adjusting to a mystery with " +
                "a gift card.",
            "Seen the pair of frames at the school — the " +
                "portrait and its other half? There's a " +
                "picnic afternoon beside it now, loaned " +
                "open. Town reads the card to the school " +
                "groups. Three minutes, they say."),
        "framed" to listOf(
            "The wall's closed — named the man at the " +
                "edge, open and shut. Funny: his family's " +
                "keeping a list of every ladder he ever " +
                "held, in writing, open to anybody. " +
                "Nobody's asked. Ask him. Or don't. The " +
                "list'll be there.",
            "The Rehanging? Oh, they named the face with " +
                "the thumb. Nearest face to the frames, " +
                "that's the arithmetic the room used. " +
                "There's another arithmetic, in one " +
                "Journal somewhere, frame by frame. " +
                "Journals keep everything. It's the " +
                "reading that's scarce."),
        "hung" to listOf(
            "The display got hung as loaned, is the " +
                "official story. Six faces, one pencil " +
                "question, one card by the door that says " +
                "DISPLAYED AS LOANED in very small, neat, " +
                "printed-looking letters. Town tells it " +
                "as a ghost story now, basically. The " +
                "kids love it. The kids' hallway has the " +
                "portrait in it. Funny town.",
            "There's a card by the hall door bothers " +
                "people and nobody can say why. Small, " +
                "neat, printed-looking. Says AS LOANED. " +
                "It'll outlive the display."),
        "unexposed" to listOf(
            "The Rehanging? Hall was full. They opened " +
                "the wall the usual way — thinnest " +
                "caption loses. The board of it's in " +
                "somebody's Journal, complete as you " +
                "like. Journals keep everything.",
            "Some display at the school got opened by a " +
                "board with a pencil question on it, I " +
                "hear. The four photographs are all still " +
                "hanging where they hung. Frames are " +
                "patient. Longer the town waits, the " +
                "more they look like they belong. That's " +
                "how walls drift, probably."))

    /** A post-case ambient aside in the town's own voice —
     *  the ending the town lives with is the one it keeps
     *  telling, at every check it actually happened.
     *  Null until the case is closed. */
    fun ambientCallback10(state: GameState, rnd: Random): String? {
        if (!state.arc10Retired) return null
        val bank = CALLBACKS10[state.arc10Branch] ?: return null
        return bank[rnd.nextInt(bank.size)]
    }
}
