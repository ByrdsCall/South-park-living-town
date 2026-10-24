package com.hunt3r.southparktown

import kotlin.random.Random

// ============================================================
// GROUP CHATS — v0.10.0d (Queue #10 item 4)
//
// The phone learns to talk to more than one person at a
// time. Groups are NOT a new app and NOT a new store: a
// group is a THREAD KIND inside the existing Phone store —
// the same PhoneText records, the same caps, unread counts
// and badges, the same morning harvest. What changes is who
// a thread can be and what may speak in it:
//
//   "group:crew"    your crew's chat — exists while the
//                   roster (Crew.members, the one source)
//                   is non-empty; born when a crew forms,
//                   archived when it empties.
//   "group:family"  Mom + Dad + you in ONE thread — exists
//                   once the family has arrived (Family.
//                   arrived). The v0.10.0a solo threads stay
//                   exactly as they were; this thread is the
//                   SECOND voice: the parent who didn't
//                   solo-text adds the coordination line.
//   "group:play"    the current production's cast-and-crew
//                   channel — exists from the cast list to
//                   the morning-after review (Play's own
//                   record is the source), then quiet.
//
// The phone's laws, extended not broken:
//   - FACT-BOUND. Every reactive post is derived at
//     harvest from a recorded fact (Crew's tallies and
//     photo records, Family's dinner/allowance stamps,
//     Play's record and schedule) and STAMPED ONLY WHEN IT
//     LANDS — an undelivered fact stays due, never stale-
//     posted, never invented. The only unkeyed posts are
//     the crew's presence banter, whose lines claim no
//     event at all (the suite audits the bank for claim
//     words, place names and the spoiler denylist).
//   - SAME WINDOWS. Posts are minted only inside Phone's
//     morning harvest, spend the same MAX_NEW_PER_DAY
//     budget, stop at the same unread wall, and at most
//     MAX_GROUP_PER_DAY of them land per morning.
//   - REPLIES NEVER PAY. Solo quick replies still pay
//     their +1 (capped, Phone's rule); a GROUP reply moves
//     no friendship and no coins — a group chat is not a
//     bribe funnel. It can draw a short reaction from a
//     member actually in the thread (never someone who
//     left), and the reaction plants the solo pattern's
//     "texting" topic in the reactor's memory.
//   - CAPS. Groups add three threads to the inbox math:
//     (72 solo + 3 group) × 2 = 150 ≤ MAX_TEXTS 160 — the
//     dial does not move (Phone's comment carries the
//     derivation; the suite pins it).
//   - STATE. No new save shape: texts ride the "phone"
//     key with every other text; snapshots and stamps ride
//     life/lifeText under "gchat:…" keys, the established
//     pattern. Pre-0.10.0d saves gain each thread as its
//     condition is first met (the update-landing morning
//     reads as the thread's birthday).
// ============================================================

object GroupChats {

    // ---------------- thread ids & dials (suite-pinned) --

    const val CREW_ID = "group:crew"
    const val FAMILY_ID = "group:family"
    const val PLAY_ID = "group:play"

    val GROUP_IDS: Set<String> = setOf(CREW_ID, FAMILY_ID, PLAY_ID)

    /** Group posts that may land in one morning's harvest
     *  (they also spend Phone's shared daily budget). */
    const val MAX_GROUP_PER_DAY = 2

    /** Chance a live group draws a reaction to your quick
     *  reply (solo ACKs are 0.5; a room is livelier). */
    const val REACT_CHANCE = 0.6

    /** Chance a quiet crew morning gets one banter post. */
    const val BANTER_CHANCE = 0.35

    // ---------------- snapshot / stamp keys --------------
    // life/lifeText only — the established pattern. A
    // snapshot records what the chat has ALREADY said, so a
    // fact is posted once and an undelivered one stays due.

    private const val K_CREW_SNAP = "gchat:crewSnap"     // lifeText csv
    private const val K_CREW_OUT = "gchat:crewOut"       // lifeText name=n csv
    private const val K_CREW_PHOTO = "gchat:crewPhoto"   // lifeText photo id
    private const val K_CREW_WIT = "gchat:crewWit"       // life int
    private const val K_BANTER_DAY = "gchat:banterDay"   // life int
    private const val K_FAM_BORN = "gchat:famBorn"       // life int
    private const val K_FAM_DIN = "gchat:famDinDay"      // life int
    private const val K_FAM_WRAP = "gchat:famWrapDay"    // life int
    private const val K_FAM_ALLOW = "gchat:famAllowDay"  // life int

    /** Play beat stamps live in the INT map (the day the
     *  beat landed) — one small key per beat per year, so
     *  a lifetime of productions never leans on the
     *  lifeText bound the suite's lifetime drive pins. */
    private fun playStamp(year: Int, beat: String) =
        "gchat:play:$year:$beat"

    // ---------------- identity ---------------------------

    fun isGroup(name: String): Boolean = name in GROUP_IDS

    fun titleFor(state: GameState, thread: String): String =
        when (thread) {
            CREW_ID -> "👥 Your Crew"
            FAMILY_ID -> "🏠 Family"
            PLAY_ID -> "🎭 ${Play.TITLE} — cast & crew"
            else -> thread
        }

    /** The NPC members of a thread, derived from the one
     *  source each: the crew roster, the arrived parents,
     *  the production's record (cast + ensemble + the
     *  director). Never stored — a leaver is out the moment
     *  the source says so. */
    fun membersOf(state: GameState, thread: String): List<String> =
        when (thread) {
            CREW_ID -> Crew.members(state)
            FAMILY_ID ->
                if (Family.arrived(state)) Family.PARENTS
                else emptyList()
            PLAY_ID -> {
                val rec = Play.currentRecord(state)
                if (rec == null || rec.cast.isEmpty()) emptyList()
                else (rec.memberKids() + Play.DIRECTOR).distinct()
            }
            else -> emptyList()
        }

    /** One-line member summary for the thread list and the
     *  thread header: who's in, and you. */
    fun memberLine(state: GameState, thread: String): String {
        val m = membersOf(state, thread)
        if (m.isEmpty()) return "just you, for now"
        val shown = m.take(4).joinToString(", ")
        val more = if (m.size > 4) " +${m.size - 4}" else ""
        return "$shown$more · and you"
    }

    /** A live thread still accepts posts and reactions.
     *  An archived one (crew emptied, play reviewed, family
     *  not arrived) keeps its history and takes your reply
     *  bubble, but nobody answers — the moment has passed. */
    fun live(state: GameState, thread: String): Boolean =
        when (thread) {
            CREW_ID -> Crew.members(state).isNotEmpty()
            FAMILY_ID -> Family.arrived(state)
            PLAY_ID -> {
                val rec = Play.currentRecord(state)
                if (rec == null || rec.cast.isEmpty()) false
                else {
                    val reviewDay =
                        state.life["playReviewDay:${rec.year}"]
                    reviewDay == null || state.absDay <= reviewDay
                }
            }
            else -> false
        }

    // ---------------- rendering --------------------------

    private fun playerName(state: GameState): String =
        state.agents.values.firstOrNull { it.isPlayer }?.name
            ?: "New Kid"

    private fun seedFor(day: Int, kind: String,
                        subject: String): Long =
        (day * 1_000_003L) + (kind.hashCode() * 31L) +
            subject.hashCode()

    private fun render(state: GameState, sender: String,
                       template: String,
                       slots: Map<String, String>): String {
        var t = template.replace("{you}", playerName(state))
        for ((k, v) in slots) t = t.replace("{$k}", v)
        if (sender == "Kenny") t = Phone.kennyfy(t)
        return t.take(Phone.MAX_TEXT_CHARS)
    }

    // ---------------- the posts --------------------------

    /** One due post. [onCommit] runs only when the post
     *  actually lands (Phone's budget may hold it a day):
     *  it stamps the fact so it is never posted twice. */
    class GroupPost(
        val thread: String,
        val sender: String,
        val text: String,
        val onCommit: (GameState) -> Unit
    )

    /** Every group post due this morning, in landing
     *  priority: day-locked coordination first, births and
     *  membership next, reactions after, banter last. Pure
     *  derivation — nothing here writes state. */
    fun duePosts(state: GameState): List<GroupPost> {
        val out = mutableListOf<GroupPost>()
        familyDinnerPost(state)?.let { out += it }       // day-locked
        playOpeningPost(state)?.let { out += it }        // day-locked
        crewMembershipPost(state)?.let { out += it }
        familyBirthPost(state)?.let { out += it }
        playCastPost(state)?.let { out += it }
        crewPhotoPost(state)?.let { out += it }
        crewContestPost(state)?.let { out += it }
        crewOutingPost(state)?.let { out += it }
        familyWrapPost(state)?.let { out += it }
        familyAllowancePost(state)?.let { out += it }
        playRehearsalPosts(state).forEach { out += it }
        playVerdictPost(state)?.let { out += it }
        if (out.none { it.thread == CREW_ID })
            crewBanterPost(state)?.let { out += it }
        return out
    }

    /** Land a due post: append the text and stamp the
     *  fact. Called by Phone's harvest only. */
    fun commit(state: GameState, post: GroupPost): Boolean {
        Phone.appendText(state, post.thread, post.sender,
            Phone.GROUP, post.text)
        post.onCommit(state)
        return true
    }

    // ---------------- the crew thread --------------------

    private fun crewSnapMembers(state: GameState): List<String> =
        (state.lifeText[K_CREW_SNAP] ?: "")
            .split(",").map { it.trim() }.filter { it.isNotEmpty() }

    private fun crewOutSnap(state: GameState): Map<String, Int> =
        (state.lifeText[K_CREW_OUT] ?: "").split(",")
            .mapNotNull {
                val kv = it.split("=")
                if (kv.size == 2)
                    kv[0] to (kv[1].toIntOrNull() ?: 0) else null
            }.toMap()

    private fun syncCrewTallies(state: GameState) {
        state.lifeText[K_CREW_OUT] = Crew.members(state)
            .joinToString(",") {
                "$it=${Crew.outings(state, it)}" }
        state.life[K_CREW_WIT] = Crew.contestsWitnessed(state)
        state.lifeText[K_CREW_PHOTO] = newestCrewPhoto(state)?.id
            ?: (state.lifeText[K_CREW_PHOTO] ?: "")
    }

    private fun newestCrewPhoto(state: GameState):
        PhotoRecord? {
        val now = Crew.members(state)
        if (now.size < 2) return null
        return state.photos.lastOrNull { rec ->
            rec.npcs.count { it.name in now } >= 2 }
    }

    /** Birth / join / leave — one membership fact per
     *  morning, read off the roster diff against the
     *  snapshot of what the chat has already said. */
    private fun crewMembershipPost(state: GameState):
        GroupPost? {
        val now = Crew.members(state)
        val prev = crewSnapMembers(state)
        if (now.isEmpty() && prev.isEmpty()) return null
        if (prev.isEmpty() && now.isNotEmpty()) {
            // the thread is born: a crew formed (or the
            // update landed on a save that already had one)
            val speaker = now.first()
            val tpl = CREW_BORN_LINES[
                Random(seedFor(state.absDay, "gborn", speaker))
                    .nextInt(CREW_BORN_LINES.size)]
            return GroupPost(CREW_ID, speaker,
                render(state, speaker, tpl, emptyMap())) { s ->
                s.lifeText[K_CREW_SNAP] = now.joinToString(",")
                syncCrewTallies(s)
            }
        }
        val joined = now - prev.toSet()
        if (joined.isNotEmpty()) {
            val speaker = now.firstOrNull { it in prev }
                ?: now.first()
            val tpl = CREW_JOIN_LINES[
                Random(seedFor(state.absDay, "gjoin",
                    joined.first()))
                    .nextInt(CREW_JOIN_LINES.size)]
            return GroupPost(CREW_ID, speaker,
                render(state, speaker, tpl,
                    mapOf("name" to joined.first()))) { s ->
                s.lifeText[K_CREW_SNAP] = now.joinToString(",")
            }
        }
        val left = prev - now.toSet()
        if (left.isNotEmpty()) {
            val leaver = left.first()
            return if (now.isNotEmpty()) {
                val speaker = now.first()
                val tpl = CREW_LEAVE_LINES[
                    Random(seedFor(state.absDay, "gleave",
                        leaver)).nextInt(CREW_LEAVE_LINES.size)]
                GroupPost(CREW_ID, speaker,
                    render(state, speaker, tpl,
                        mapOf("name" to leaver))) { s ->
                    s.lifeText[K_CREW_SNAP] =
                        now.joinToString(",")
                }
            } else {
                // the last chair empties: the leaver closes
                // the door, and the thread archives behind
                // them (live() goes false with the roster)
                val tpl = CREW_LEAVE_LAST_LINES[
                    Random(seedFor(state.absDay, "glast",
                        leaver))
                        .nextInt(CREW_LEAVE_LAST_LINES.size)]
                GroupPost(CREW_ID, leaver,
                    render(state, leaver, tpl, emptyMap())) { s ->
                    s.lifeText[K_CREW_SNAP] = ""
                }
            }
        }
        return null
    }

    /** A photo with ≥2 of the crew in frame, posted by one
     *  of the framed members, citing the record's place. */
    private fun crewPhotoPost(state: GameState): GroupPost? {
        val now = Crew.members(state)
        if (now.size < 2) return null
        if (!state.lifeText.containsKey(K_CREW_SNAP)) return null
        val rec = newestCrewPhoto(state) ?: return null
        if (rec.id == state.lifeText[K_CREW_PHOTO]) return null
        if (rec.absDay > state.absDay) return null
        val framed = rec.npcs.map { it.name }
            .filter { it in now }
        if (framed.size < 2) return null
        val speaker = framed[
            Random(seedFor(state.absDay, "gphoto", rec.id))
                .nextInt(framed.size)]
        val place = LOCATIONS[rec.locId]?.name ?: rec.locId
        val tpl = CREW_PHOTO_LINES[
            Random(seedFor(state.absDay, "gphotoline", rec.id))
                .nextInt(CREW_PHOTO_LINES.size)]
        return GroupPost(CREW_ID, speaker,
            render(state, speaker, tpl,
                mapOf("place" to place))) { s ->
            s.lifeText[K_CREW_PHOTO] = rec.id
        }
    }

    /** A contest the crew witnessed, posted by a witness,
     *  quoting the rival scoreboard's own line. */
    private fun crewContestPost(state: GameState): GroupPost? {
        val now = Crew.members(state)
        if (now.isEmpty()) return null
        if (!state.lifeText.containsKey(K_CREW_SNAP)) return null
        val wit = Crew.contestsWitnessed(state)
        if (wit <= (state.life[K_CREW_WIT] ?: 0)) return null
        // a witnessed contest means a rival stood revealed
        // (the settle funnel requires the crowning) — the
        // scoreboard line is only quoted once that's true
        if (!Rival.revealed(state)) return null
        val witnesses = now.filter { Crew.witnessed(state, it) > 0 }
        val speaker = (witnesses.ifEmpty { now }).first()
        val tpl = CREW_CONTEST_LINES[
            Random(seedFor(state.absDay, "gwit", "$wit"))
                .nextInt(CREW_CONTEST_LINES.size)]
        return GroupPost(CREW_ID, speaker,
            render(state, speaker, tpl,
                mapOf("score" to Rival.scoreLine(state)))) { s ->
            s.life[K_CREW_WIT] = wit
        }
    }

    /** An outing that actually happened (a member's outing
     *  tally rose), posted by one who went, citing the last
     *  place they stood with you. */
    private fun crewOutingPost(state: GameState): GroupPost? {
        val now = Crew.members(state)
        if (now.isEmpty()) return null
        if (!state.lifeText.containsKey(K_CREW_SNAP)) return null
        val snap = crewOutSnap(state)
        val risers = now.filter {
            Crew.outings(state, it) > (snap[it] ?: 0) }
        if (risers.isEmpty()) return null
        val speaker = risers.firstOrNull {
            Crew.lastPlaceTogether(state, it) != null }
            ?: return null
        val placeId = Crew.lastPlaceTogether(state, speaker)
            ?: return null
        val place = LOCATIONS[placeId]?.name ?: placeId
        val tpl = CREW_OUTING_LINES[
            Random(seedFor(state.absDay, "gout", speaker))
                .nextInt(CREW_OUTING_LINES.size)]
        return GroupPost(CREW_ID, speaker,
            render(state, speaker, tpl,
                mapOf("place" to place))) { s ->
            s.lifeText[K_CREW_OUT] = Crew.members(s)
                .joinToString(",") {
                    "$it=${Crew.outings(s, it)}" }
        }
    }

    /** Presence banter: the ONLY unkeyed crew posts, on a
     *  seeded share of quiet mornings. The bank claims no
     *  event, names no place, quotes no fact — the suite
     *  audits it line by line. */
    private fun crewBanterPost(state: GameState): GroupPost? {
        val now = Crew.members(state)
        if (now.isEmpty()) return null
        if ((state.life[K_BANTER_DAY] ?: -1) == state.absDay)
            return null
        val rnd = Random(seedFor(state.absDay, "gbanter",
            now.joinToString(",")))
        if (rnd.nextDouble() >= BANTER_CHANCE) return null
        val speaker = now[rnd.nextInt(now.size)]
        val tpl = CREW_BANTER_LINES[
            rnd.nextInt(CREW_BANTER_LINES.size)]
        return GroupPost(CREW_ID, speaker,
            render(state, speaker, tpl, emptyMap())) { s ->
            s.life[K_BANTER_DAY] = s.absDay
        }
    }

    // ---------------- the family thread ------------------
    // The solo family texts stay exactly as v0.10.0a
    // shipped them; this thread carries the OTHER parent's
    // voice on the same recorded facts, so the two of them
    // are ever in one room on your phone.

    private fun familyDuty(absDay: Int): String =
        if (absDay % 2 == 0) Family.MOM else Family.DAD

    private fun otherParent(name: String): String =
        if (name == Family.MOM) Family.DAD else Family.MOM

    private fun familyBirthPost(state: GameState): GroupPost? {
        if (!Family.arrived(state)) return null
        if ((state.life[K_FAM_BORN] ?: 0) == 1) return null
        val tpl = FAM_BORN_LINES[
            Random(seedFor(state.absDay, "gfamborn", "fam"))
                .nextInt(FAM_BORN_LINES.size)]
        return GroupPost(FAMILY_ID, Family.MOM,
            render(state, Family.MOM, tpl, emptyMap())) { s ->
            s.life[K_FAM_BORN] = 1
        }
    }

    /** Tonight's dinner, coordinated by the parent who is
     *  NOT on solo-text duty (the duty parent's solo call
     *  still lands in their own thread). Day-locked: due
     *  only on the dinner day itself. */
    private fun familyDinnerPost(state: GameState):
        GroupPost? {
        if (!Family.arrived(state)) return null
        if (!Family.dinnerTonight(state)) return null
        if ((state.life[K_FAM_DIN] ?: -1) == state.absDay)
            return null
        val speaker = otherParent(familyDuty(state.absDay))
        val tpl = FAM_DINNER_LINES[
            Random(seedFor(state.absDay, "gfamdin", speaker))
                .nextInt(FAM_DINNER_LINES.size)]
        return GroupPost(FAMILY_ID, speaker,
            render(state, speaker, tpl,
                mapOf("dish" to Family.dishOf(state.absDay)))) { s ->
            s.life[K_FAM_DIN] = s.absDay
        }
    }

    /** The morning after a family dinner: the parent who
     *  did NOT send the solo wrap text adds the second
     *  voice here, off the same wrap fact. */
    private fun familyWrapPost(state: GameState): GroupPost? {
        if (!Family.arrived(state)) return null
        val wrap = Family.wrapFor(state, state.absDay - 1)
            ?: return null
        if ((state.life[K_FAM_WRAP] ?: -1) == state.absDay - 1)
            return null
        val attended = wrap.first
        // Phone's solo wrap sender: attended → the OTHER of
        // today's duty; missed → today's duty. The group
        // voice is the remaining parent, by construction.
        val soloSender = if (attended)
            otherParent(familyDuty(state.absDay))
        else familyDuty(state.absDay)
        val speaker = otherParent(soloSender)
        val bank = if (attended) FAM_AFTER_ATTENDED_LINES
            else FAM_AFTER_MISSED_LINES
        val tpl = bank[Random(seedFor(state.absDay, "gfamwrap",
            speaker)).nextInt(bank.size)]
        return GroupPost(FAMILY_ID, speaker,
            render(state, speaker, tpl, emptyMap())) { s ->
            s.life[K_FAM_WRAP] = s.absDay - 1
        }
    }

    /** Allowance morning: Mom's solo text announces it;
     *  Dad seconds it here, off Family's own stamp. */
    private fun familyAllowancePost(state: GameState):
        GroupPost? {
        if (!Family.arrived(state)) return null
        if ((state.life["famAllowanceDay"] ?: -1) !=
            state.absDay) return null
        if ((state.life[K_FAM_ALLOW] ?: -1) == state.absDay)
            return null
        val tpl = FAM_ALLOWANCE_LINES[
            Random(seedFor(state.absDay, "gfamallow", "dad"))
                .nextInt(FAM_ALLOWANCE_LINES.size)]
        return GroupPost(FAMILY_ID, Family.DAD,
            render(state, Family.DAD, tpl,
                mapOf("x" to "${Family.ALLOWANCE_COINS}"))) { s ->
            s.life[K_FAM_ALLOW] = s.absDay
        }
    }

    // ---------------- the play thread --------------------
    // Existence: the current production's record exists
    // and is cast. Beats key off the schedule and the
    // record — the cast list, each rehearsal the morning
    // after, opening morning, and the verdict morning.

    private fun playCastPost(state: GameState): GroupPost? {
        val rec = Play.currentRecord(state) ?: return null
        if (rec.cast.isEmpty()) return null
        val sched = PlayCalendar.scheduleFor(rec.year)
        if (state.absDay < sched.castDay) return null
        val key = playStamp(rec.year, "cast")
        if (state.life.containsKey(key)) return null
        val lead = Play.actorOf(rec, "mountain")
        return if (lead != "You" && lead != "—" &&
            lead in rec.memberKids()) {
            val role = rec.cast.entries
                .firstOrNull { it.value == lead }
                ?.let { Play.roleOf(it.key)?.title } ?: "cast"
            val tpl = PLAY_CAST_MEMBER_LINES[
                Random(seedFor(state.absDay, "gplaycast", lead))
                    .nextInt(PLAY_CAST_MEMBER_LINES.size)]
            GroupPost(PLAY_ID, lead,
                render(state, lead, tpl, mapOf(
                    "role" to role,
                    "title" to Play.TITLE))) { s ->
                s.life[key] = s.absDay
            }
        } else {
            val tpl = PLAY_CAST_DIRECTOR_LINES[
                Random(seedFor(state.absDay, "gplaycastd", "v"))
                    .nextInt(PLAY_CAST_DIRECTOR_LINES.size)]
            GroupPost(PLAY_ID, Play.DIRECTOR,
                render(state, Play.DIRECTOR, tpl, mapOf(
                    "title" to Play.TITLE))) { s ->
                s.life[key] = s.absDay
            }
        }
    }

    private fun playRehearsalPosts(state: GameState):
        List<GroupPost> {
        val rec = Play.currentRecord(state) ?: return emptyList()
        if (rec.cast.isEmpty()) return emptyList()
        val sched = PlayCalendar.scheduleFor(rec.year)
        val members = rec.memberKids()
        if (members.isEmpty()) return emptyList()
        val out = mutableListOf<GroupPost>()
        for (idx in sched.rehearsalDays.indices) {
            if (sched.rehearsalDays[idx] != state.absDay - 1)
                continue
            val key = playStamp(rec.year, "reh$idx")
            if (state.life.containsKey(key)) continue
            val speaker = members[
                Random(seedFor(rec.year, "gplayreh", "$idx"))
                    .nextInt(members.size)]
            val involved = rec.track in
                listOf("cast", "ens", "under", "crew")
            val attended = involved &&
                ((rec.attMask shr idx) and 1) == 1
            val bank = when {
                attended -> PLAY_REH_ATTENDED_LINES
                involved -> PLAY_REH_MISSED_LINES
                else -> PLAY_REH_NEUTRAL_LINES
            }
            val tpl = bank[
                Random(seedFor(state.absDay, "gplayreh", "$idx"))
                    .nextInt(bank.size)]
            out += GroupPost(PLAY_ID, speaker,
                render(state, speaker, tpl,
                    mapOf("n" to "${idx + 1}", "of" to
                        "${Play.REHEARSALS}"))) { s ->
                s.life[key] = s.absDay
            }
        }
        return out
    }

    private fun playOpeningPost(state: GameState): GroupPost? {
        val rec = Play.currentRecord(state) ?: return null
        if (rec.cast.isEmpty()) return null
        val sched = PlayCalendar.scheduleFor(rec.year)
        if (state.absDay != sched.openingDay) return null
        if (rec.night != "") return null
        val key = playStamp(rec.year, "open")
        if (state.life.containsKey(key)) return null
        val tpl = PLAY_OPENING_LINES[
            Random(seedFor(state.absDay, "gplayopen", "v"))
                .nextInt(PLAY_OPENING_LINES.size)]
        return GroupPost(PLAY_ID, Play.DIRECTOR,
            render(state, Play.DIRECTOR, tpl,
                mapOf("title" to Play.TITLE))) { s ->
            s.life[key] = s.absDay
        }
    }

    private fun playVerdictPost(state: GameState): GroupPost? {
        val rec = Play.currentRecord(state) ?: return null
        if (rec.verdict == "") return null
        val sched = PlayCalendar.scheduleFor(rec.year)
        if (state.absDay <= sched.openingDay) return null
        val key = playStamp(rec.year, "verdict")
        if (state.life.containsKey(key)) return null
        val members = rec.memberKids()
        if (members.isEmpty()) return null
        val speaker = members[
            Random(seedFor(rec.year, "gplayverdict", "cast"))
                .nextInt(members.size)]
        val tpl = PLAY_VERDICT_LINES[
            Random(seedFor(state.absDay, "gplayverdict", "line"))
                .nextInt(PLAY_VERDICT_LINES.size)]
        return GroupPost(PLAY_ID, speaker,
            render(state, speaker, tpl, mapOf(
                "verdict" to Play.verdictWord(rec.verdict),
                "critic" to rec.critic.ifEmpty { "the critic" },
                "title" to Play.TITLE))) { s ->
            s.life[key] = s.absDay
        }
    }

    // ---------------- quick replies ----------------------

    /** The phone's quick-reply pattern, in a room: your
     *  emoji lands as your bubble; a member ACTUALLY in
     *  the thread may answer with a short authored
     *  reaction (seeded, once), planting the solo
     *  pattern's "texting" topic in their memory. Group
     *  replies move NO friendship and NO coins — stated
     *  discipline, pinned in the suite. Archived threads
     *  take the bubble and answer with silence. */
    fun quickReply(state: GameState, thread: String,
                   emoji: String): Phone.ReplyResult {
        if (thread !in GROUP_IDS) return Phone.ReplyResult(false, null)
        if (emoji !in Phone.QUICK_REPLIES)
            return Phone.ReplyResult(false, null)
        // your bubble lands read, like the solo pattern's —
        // you're looking at it
        Phone.appendText(state, thread, "you", Phone.REPLY,
            emoji, emoji).read = true
        var ack: String? = null
        if (live(state, thread)) {
            val members = membersOf(state, thread)
            val rnd = Random(seedFor(state.absDay, "gack",
                "$thread:${state.phoneTexts.size}"))
            if (members.isNotEmpty() &&
                rnd.nextDouble() < REACT_CHANCE) {
                val reactor = members[rnd.nextInt(members.size)]
                val bank = GROUP_ACK_LINES[emoji]
                    ?: GROUP_ACK_LINES["👍"]!!
                ack = render(state, reactor,
                    bank[rnd.nextInt(bank.size)], emptyMap())
                Phone.appendText(state, thread, reactor,
                    Phone.ACK, ack).read = true
                if (reactor in KID_ORDER) {
                    // the solo pattern's topic plant, in the
                    // reactor's memory ("texting" — the
                    // follow-up banks treat it like any
                    // shared topic)
                    val mem = NpcMemoryStore.getOrCreate(reactor)
                    mem.topics.remove("texting")
                    mem.topics.add("texting")
                    while (mem.topics.size > 6)
                        mem.topics.removeAt(0)
                }
            }
        }
        Phone.enforceCaps(state)
        return Phone.ReplyResult(false, ack)
    }

    // ---------------- banks ------------------------------
    // Every reactive line spends ONLY its fact's slots.
    // Slot legend: {name} a person the fact names, {place}
    // a place a record names, {score} the rival scoreboard
    // line verbatim, {dish} tonight's dish, {x} the
    // allowance count, {role}/{title}/{n}/{of}/{verdict}/
    // {critic} the production record's own fields.

    val CREW_BORN_LINES = listOf(
        "okay. group chat. I'm making it because a crew " +
            "this size needs one room to be unserious in",
        "the crew has a group chat now. this is it. this " +
            "is the chat. amazing things will be said here",
        "group chat established. first order of business: " +
            "snacks. second order of business: everything else")

    val CREW_JOIN_LINES = listOf(
        "{name} is in the chat now. {name}, the chat has " +
            "one rule and it's snacks",
        "added {name}. the crew chat grows. the snack " +
            "budget does not",
        "{name} has entered the group chat. pretend " +
            "everything above this was normal")

    val CREW_LEAVE_LINES = listOf(
        "{name}'s out of the crew. the chat keeps their " +
            "chair. metaphorically. there are no chairs",
        "{name} left the crew — noted in the chat for the " +
            "record. we ride on",
        "crew update: {name} stepped out. the chat abides")

    val CREW_LEAVE_LAST_LINES = listOf(
        "last one out. the chat was a good chat. closing " +
            "the door gently",
        "crew's empty, so the chat rests. it happened. it " +
            "was real. it had snacks")

    val CREW_PHOTO_LINES = listOf(
        "the photo of us at {place}?? putting it in the " +
            "chat so it's official. iconic behavior by everyone",
        "chat. CHAT. the {place} photo. my hair did a " +
            "whole thing and I am at peace with it",
        "for the record: the {place} picture goes in the " +
            "crew archives. the archives are this chat")

    val CREW_CONTEST_LINES = listOf(
        "crew witnessed that one with our own eyes. {score}",
        "scoreboard update, witnessed live by this very " +
            "crew: {score}")

    val CREW_OUTING_LINES = listOf(
        "yesterday's run to {place} is in the book. the " +
            "crew moves well. tell no one I said that",
        "the {place} outing: elite. the chat may discuss. " +
            "I will be taking no questions")

    /** Presence only — audited: no claim words, no place
     *  names, no digits, no facts to invent. */
    val CREW_BANTER_LINES = listOf(
        "roll call. I brought nothing but presence",
        "group chat status: operational. barely. beautifully",
        "thinking about snacks in a crew context. that's " +
            "the whole message",
        "if anyone asks, we were all reading. separately. " +
            "together. reading",
        "the chat has been quiet and I have taken that " +
            "personally. hi")

    val FAM_BORN_LINES = listOf(
        "making it official: the family group chat. your " +
            "father is here too. he types with one finger " +
            "and total confidence")

    val FAM_DINNER_LINES = listOf(
        "dinner at home tonight — {dish}. I'm handling my " +
            "side of it. your chair has been informed",
        "tonight: {dish}, at home, family rules. I call " +
            "the good seat. I always call it. I never get it")

    val FAM_AFTER_ATTENDED_LINES = listOf(
        "seconding last night's dinner in the official " +
            "chat. good table. it goes in the record",
        "for the family record: last night's table was a " +
            "success and I was present for all of it")

    val FAM_AFTER_MISSED_LINES = listOf(
        "the fridge plate has a label and the label has " +
            "your name. the chat has been notified",
        "dinner happened; your plate is in the fridge " +
            "under guard. the guard is a towel")

    val FAM_ALLOWANCE_LINES = listOf(
        "allowance day: {x} coins on the counter. the " +
            "system your mother built continues to provide",
        "the counter has {x} coins on it with your name " +
            "implied. the dinner economy works")

    val PLAY_CAST_MEMBER_LINES = listOf(
        "CAST LIST IS UP and this chat is where the show " +
            "lives now. I'm {role} in “{title}”. " +
            "rehearsals start tomorrow",
        "the list is posted. {role}, that's me. this " +
            "chat is the production now — see everyone at " +
            "rehearsal")

    val PLAY_CAST_DIRECTOR_LINES = listOf(
        "the cast list is posted, and this chat is the " +
            "production's official channel for “{title}”. " +
            "rehearsals are on the calendar. the goat has " +
            "been informed — v")

    val PLAY_REH_ATTENDED_LINES = listOf(
        "rehearsal {n} of {of}: in the book. {you} " +
            "actually stayed the whole time. the show can " +
            "feel it",
        "session {n} of {of} done, and {you} was THERE. " +
            "the mountain stands a little straighter")

    val PLAY_REH_MISSED_LINES = listOf(
        "rehearsal {n} of {of}: survived without {you}. " +
            "the chair where {you} goes remains marked",
        "session {n} of {of} in the book. {you} was " +
            "missed. the backdrop has opinions but we " +
            "press on")

    val PLAY_REH_NEUTRAL_LINES = listOf(
        "rehearsal {n} of {of}: survived. lines were " +
            "said. some of them were even the right ones",
        "session {n} of {of} in the book. the show grows " +
            "a spine, slowly, like all of us")

    val PLAY_OPENING_LINES = listOf(
        "tonight. “{title}” opens. house at 5:30, curtain " +
            "when the room settles. this chat goes quiet " +
            "at places. break legs, all of you — v")

    val PLAY_VERDICT_LINES = listOf(
        "{verdict}. that's the word from the room, and " +
            "{critic}'s review is out. keeping this chat " +
            "forever",
        "the verdict is in: {verdict}. the production " +
            "chat may now become a trophy case")

    val GROUP_ACK_LINES: Map<String, List<String>> = mapOf(
        "👍" to listOf(
            "👍",
            "seconded. the chat has spoken",
            "motion carried",
            "this is the most organized we have ever been"),
        "😂" to listOf(
            "😂😂",
            "STOP",
            "the chat is deceased",
            "screenshotted. framed. hung in the chat hall " +
                "of fame"),
        "❤️" to listOf(
            "❤️",
            "okay. group hug. nobody make it weird",
            "noted in the official record of us"))
}
