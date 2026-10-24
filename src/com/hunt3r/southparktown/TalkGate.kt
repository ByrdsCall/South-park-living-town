package com.hunt3r.southparktown

// ============================================================
// TalkGate — v0.4.0g: the Talk rail, as data.
//
// One tap on a person can mean four different things, and
// the order the game tries them in IS a design decision
// (v0.4.0d stacked it): a holiday beat step is today-only,
// a fresh errand ask keeps until tomorrow, and the lines a
// person OWES you (a case's witness telling, an arc's
// current beat) ride inside the ordinary conversation,
// ahead of the small talk. Until now that order lived
// implicitly in GameActivity.openTalkSheet's control flow;
// it lives here now, pure over the GameState, so the suite
// can pin the full stack under fabricated contention — an
// NPC armed with every rail at once, peeled one layer at a
// time — and no future build can silently reshuffle the
// conversation stack.
//
// The rails, in the order gateFor tries them:
//   DELIVER       an accepted errand whose Talk-judge IS
//                 this person (the delivery settles first —
//                 you came holding their parcel; it also
//                 MASKS a beat step for the same person,
//                 exactly as the gate has always behaved).
//   HOLIDAY_BEAT  a step of today's holiday beat that a
//                 conversation with this person would land.
//   FINALE        (v0.5.0h) a season finale's live beat
//                 that a conversation with this person would
//                 land — a week a year, like the holiday beat
//                 it sits just below, and above the standing
//                 faction chains for the same reason: the
//                 finale's beats age out, the chains wait.
//   CLIMAX        (v0.7.0e) a shenanigan's playable climax,
//                 live only while its climax stage is on and
//                 only with one of the event's causers at the
//                 climax turf — it sits below the finale (in
//                 a finale week the daily event doesn't even
//                 roll, and the week's climax belongs to the
//                 finale's own machinery) and above the dinner
//                 for the same reason the finale sits above
//                 it: the climax is the day's peak, judged in
//                 its own hours, and the table will still be
//                 set at sundown.
//   DINNER        (v0.6.0d) a family evening's live beat
//                 that a conversation with this person would
//                 land — tonight-only, like the beats above
//                 it, so it outranks the standing chains; it
//                 sits below the finale because dinners defer
//                 to finales everywhere (rotation, gathering,
//                 judging), and below a delivery because you
//                 came holding their parcel.
//   WORK          (v0.6.0e) a town job's live business
//                 with this person — an application at
//                 their counter, a clock-in, a shift task
//                 (the regular's serve), a clock-out, or
//                 giving notice. Window work like the dinner
//                 above it (a shift that isn't clocked by
//                 its window is a missed shift), so it sits
//                 below the dinner and above the standing
//                 chains, which wait.
//   FACTION_MISSION (v0.5.0d) a faction chain's business:
//                 an armed beat whose TALK-judge IS this
//                 person lands first; failing that, the next
//                 beat this person fronts is offered. It sits
//                 below the holiday beat (today-only beats a
//                 standing chain) and above a fresh errand ask
//                 (a faction's set piece beats a street favor).
//                 Offers WAIT while a mystery finale is due —
//                 FactionMissions.offerFor enforces the road
//                 beats' silencing rule; armed beats still land.
//                 (v0.8.0b) The factions' CAPSTONES ride this
//                 same rail, in the same order — an armed beat
//                 lands (chain first, then capstone), then an
//                 offer goes out (chain first, then capstone) —
//                 and the two payloads can never collide,
//                 because a capstone is only ever offered where
//                 its chain is already done. Below the
//                 capstone gate, the senior's honest one-line
//                 hint rides owedLines instead of a menu.
//   EMERG_OFFER   (v0.8.0c) an emergency on offer from
//                 this person — the favor that can't wait,
//                 found you, and priced by the Walk before
//                 it was offered. It sits below the set
//                 pieces (an armed beat always lands
//                 first) and above a fresh ask (a small
//                 disaster outranks a standing favor).
//   RIVAL         (v0.8.0e) the rival's business with you —
//                 the crowning reveal, the standing's lock
//                 beat, a prank's answer, a showdown rematch
//                 owed, or today's challenge on offer. It
//                 sits below the emergency (a small disaster
//                 outranks a grudge) and above a fresh ask
//                 (a scoreboard outranks a street favor), and
//                 the module itself never fires it while the
//                 town's bigger moments own the hour.
//   ERRAND_ASK    a fresh ask on offer from this person
//                 (shopkeeper at their counter first, then
//                 the street ask — Errands.pendingAsk's own
//                 internal order, unchanged).
//   DIALOGUE      the ordinary conversation — which opens
//                 with owedLines below, not with small talk.
//
// Greeting barks are deliberately NOT a rail: a greeting
// is garnish on entering a space, decided by the bark
// engine before any tap, and a bark on cooldown neither
// blocks nor promotes any of this.
// ============================================================

object TalkGate {

    enum class Rail { DELIVER, HOLIDAY_BEAT, FINALE, CLIMAX, DINNER, WORK, FACTION_MISSION, EMERG_OFFER, RIVAL, ERRAND_ASK, DIALOGUE }

    /** The gate's verdict: the rail, plus the payload the
     *  errand rails play (the delivery to settle, the ask to
     *  offer) and, since v0.5.0d, the faction payload (the
     *  armed TALK beat to land, or the chain beat on offer —
     *  exactly one of the two is set on a FACTION_MISSION gate). */
    data class Gate(val rail: Rail, val delivery: Errand? = null,
                    val ask: Errand? = null,
                    val missionTalk: FactionBeat? = null,
                    val missionOffer: FactionBeat? = null,
                    val jobStep: JobHit? = null,
                    val capstoneTalk: CapstoneBeat? = null,
                    val capstoneOffer: CapstoneBeat? = null,
                    val emergOffer: EmergOffer? = null,
                    val rivalHit: Rival.RivalHit? = null,
                    val sleepHit: SleepHit? = null,
                    val partyHit: Birthdays.PartyHit? = null,
                    val bdayHit: PlayerBirthday.BdayHit? = null)

    /** Which rail a Talk with [npcName] takes right now.
     *  Pure queries only — judging (delivering, firing the
     *  beat, marking the ask) stays with the callers, in
     *  the same places it has always happened. */
    fun gateFor(state: GameState, npcName: String,
                playerLocId: String): Gate {
        val delivery = Errands.active(state).firstOrNull {
            it.type == "DELIVER" && it.targetName == npcName
        }
        if (delivery != null) return Gate(Rail.DELIVER, delivery = delivery)
        if (HolidayBeats.talkStepFor(state, npcName) != null)
            return Gate(Rail.HOLIDAY_BEAT)
        // v0.5.0h: a live season-finale beat for this person —
        // window-only work, aged out by the calendar if it's
        // not taken, so it outranks the standing chains.
        if (Finales.talkStepFor(state, npcName) != null)
            return Gate(Rail.FINALE)
        // v0.7.0e: a shenanigan's playable climax, hosted by
        // one of its causers at the climax turf while the
        // climax stage is live — hours-only story work, the
        // day's peak; the module owns the window and the turf.
        if (Climaxes.talkStepFor(state, npcName, playerLocId))
            return Gate(Rail.CLIMAX)
        // v0.6.0d: a live dinner beat for this person —
        // tonight-only work at a family's table; the dinner
        // module itself defers its beats while a case climax
        // is on the table, so this rail never stacks moments.
        if (Dinners.talkStepFor(state, npcName) != null)
            return Gate(Rail.DINNER)
        // v0.11.0c: a sleepover beat for this person rides
        // the DINNER rail — tonight-only set-piece work at
        // your own table's house, judged by its own module.
        // The ladder gains no new rung; the sleepover module
        // defers its own beats while a case climax is on the
        // table, same as the dinners above it.
        val sleepHit = Sleepovers.talkStepFor(state, npcName)
        if (sleepHit != null)
            return Gate(Rail.DINNER, sleepHit = sleepHit)
        // v0.12.0c: a birthday party beat for this person
        // rides the same DINNER rail, judged by its own
        // module — tonight-only set-piece work at a family's
        // table, on dinners' claim discipline wholesale
        // (the party itself yields to both rails above it,
        // and to everything partyBlock names).
        val partyHit = Birthdays.talkStepFor(state, npcName)
        if (partyHit != null)
            return Gate(Rail.DINNER, partyHit = partyHit)
        // v0.14.0d: your OWN birthday's party beats ride
        // the same DINNER rail, judged by their own module
        // — tonight-only set-piece work at your own house,
        // tried below the cast parties (a guest at somebody
        // else's table is at that table; the two evenings
        // can't stand on the same night anyway).
        val bdayHit = PlayerBirthday.talkStepFor(state, npcName)
        if (bdayHit != null)
            return Gate(Rail.DINNER, bdayHit = bdayHit)
        // v0.6.0e: a town job's business with this person —
        // hiring, clocking, serving, clocking out, notice.
        // Window work, like the dinner above it; the module
        // itself decides what (if anything) is live.
        val jobStep = Jobs.talkStepFor(state, npcName, playerLocId)
        if (jobStep != null)
            return Gate(Rail.WORK, jobStep = jobStep)
        // v0.5.0d: faction business — an armed TALK beat for
        // this person lands before a new offer is made, and
        // both outrank a fresh errand ask. offerFor itself
        // enforces the finale silencing (offers wait while a
        // case choice is on the table); talkBeatFor does not,
        // so work already taken on never stalls behind a case.
        val missionTalk = FactionMissions.talkBeatFor(state, npcName)
        if (missionTalk != null)
            return Gate(Rail.FACTION_MISSION, missionTalk = missionTalk)
        val missionOffer = FactionMissions.offerFor(state, npcName)
        if (missionOffer != null)
            return Gate(Rail.FACTION_MISSION, missionOffer = missionOffer)
        // v0.8.0b: capstone business rides the same rail,
        // below the chain it completes — an armed capstone
        // TALK beat (the ceremony above all) lands first;
        // failing that, the next capstone beat this person
        // fronts is offered. A capstone can only exist where
        // its chain is finished, so the two payloads never
        // compete for the same faction; offerFor enforces
        // the same finale silencing as the chains'.
        val capstoneTalk = Capstones.talkBeatFor(state, npcName)
        if (capstoneTalk != null)
            return Gate(Rail.FACTION_MISSION, capstoneTalk = capstoneTalk)
        val capstoneOffer = Capstones.offerFor(state, npcName)
        if (capstoneOffer != null)
            return Gate(Rail.FACTION_MISSION, capstoneOffer = capstoneOffer)
        // v0.8.0c: an emergency on offer from this person.
        // It found you, it is priced and honest, and it
        // will not wait — but everything armed above it
        // still lands first, and it outranks only the
        // ordinary ask below.
        val emergOffer = Emergencies.pendingOffer(state, npcName, playerLocId)
        if (emergOffer != null)
            return Gate(Rail.EMERG_OFFER, emergOffer = emergOffer)
        // v0.8.0e: the rival's business — one named kid, a
        // scoreboard, and an arc. The module decides which
        // beat (if any) is live; it never fires while a case
        // climax, a finale, a holiday beat, a screening, the
        // bee's hours, a capstone ceremony or a live
        // emergency owns the moment.
        val rivalHit = Rival.talkStepFor(state, npcName)
        if (rivalHit != null)
            return Gate(Rail.RIVAL, rivalHit = rivalHit)
        val ask = Errands.pendingAsk(state, npcName, playerLocId)
        if (ask != null) return Gate(Rail.ERRAND_ASK, ask = ask)
        return Gate(Rail.DIALOGUE)
    }

    /** The lines [npcName] owes you before any small talk,
     *  in the order they have always played: the three
     *  cases' witness tellings (pond, booth — with its
     *  suspects' flavor — and the buried case, with its
     *  standing favor line), a relationship arc's current
     *  beat, a holiday beat's window reference, and an
     *  errand asker's thank-you (played exactly once — the
     *  removal from lifeText IS the playing). The telling
     *  grants its clue as it plays, once ever; that side
     *  effect is the witnesses' own, unchanged. */
    fun owedLines(state: GameState, npcName: String): List<Line> {
        val out = mutableListOf<Line>()
        // v0.10.0b: a roster kid standing in costume speaks
        // hero register first — once a night each (Heroes
        // stamps it). The kid underneath is unchanged; every
        // rail below still reads the real them.
        Heroes.costumeLine(state, npcName)?.let { out.add(it) }
        // a mystery witness speaks before the small talk
        // does — the telling grants its clue, once, ever
        // (it works even on a first meeting; a case doesn't
        // wait for intros).
        Mystery.witnessLine(state, npcName)?.let { out.add(it) }
        // the booth case's interviews ride the same rail
        // (the alibi retells itself a day at a time), and
        // its other suspects get their one flavor line each.
        Mystery2.witnessLine2(state, npcName)?.let { out.add(it) }
        Mystery2.suspectFlavorLine(state, npcName)?.let { out.add(it) }
        // the buried case's witnesses ride the same rail —
        // and in the ending where Randy owes the player,
        // the standing favor line rides it too, forever.
        Mystery3.witnessLine3(state, npcName)?.let { out.add(it) }
        Mystery3.favorLine3(state, npcName)?.let { out.add(it) }
        // the gardener case's witnesses ride the same rail —
        // and after the vigil, the one who knows has his
        // standing almost-nothing to say to the one who
        // knows, in whichever ending the town got.
        Mystery4.witnessLine4(state, npcName)?.let { out.add(it) }
        Mystery4.gardenerNodLine4(state, npcName)?.let { out.add(it) }
        // the confession case's evidence rides the same rail —
        // a witness's telling grants its piece; each confessor
        // will re-read their own statement to you while the
        // case is open (the record is the game); and after
        // the confrontation, the cleared say thank you and
        // the wronged say the uncomfortable thing, standing.
        Mystery5.witnessLine5(state, npcName)?.let { out.add(it) }
        Mystery5.confessionLine5(state, npcName)?.let { out.add(it) }
        Mystery5.aftermathLine5(state, npcName)?.let { out.add(it) }
        // the opinion case rides the same rail — a witness's
        // telling grants its proof; a carrier's telling is
        // WITNESSED (the meter hears it, once, from the
        // player's own ears); the accused and her circle
        // speak off the meter's band while the case runs;
        // and after the meeting, one person per landing says
        // the uncomfortable thing, standing, forever.
        Mystery6.witnessLine6(state, npcName)?.let { out.add(it) }
        Mystery6.gossipLine6(state, npcName)?.let { out.add(it) }
        Mystery6.pressureLine6(state, npcName)?.let { out.add(it) }
        Mystery6.aftermathLine6(state, npcName)?.let { out.add(it) }
        // the backwards case rides the same rail — a witness's
        // telling grants its piece (the son who heard the exact
        // words, the patrol who heard the wrapping, the keeper
        // herself, the principal, the oldest memory in town);
        // and after the homecoming, one person per landing
        // says the uncomfortable thing, standing, forever.
        Mystery7.witnessLine7(state, npcName)?.let { out.add(it) }
        Mystery7.aftermathLine7(state, npcName)?.let { out.add(it) }
        // the alibi case rides the same rail — a witness's
        // telling grants its piece (the counselor's buzzers,
        // the driver's two sightings, the crossing itself,
        // the ladder's hinge, the phone call's two streets);
        // and after the reading, one person per landing
        // says the uncomfortable thing, standing, forever.
        Mystery8.witnessLine8(state, npcName)?.let { out.add(it) }
        Mystery8.aftermathLine8(state, npcName)?.let { out.add(it) }
        // the audit case rides the same rail — a witness's
        // telling grants its piece (the list's keeper, the
        // clicker's confession, the wall's counter, the
        // ledger's drafter, the gallery's keeper); and after
        // the reckoning, one person per landing says the
        // uncomfortable thing, standing, forever.
        Mystery9.witnessLine9(state, npcName)?.let { out.add(it) }
        Mystery9.aftermathLine9(state, npcName)?.let { out.add(it) }
        // the photo case rides the same rail — a witness's
        // telling grants its piece (the portrait's keeper,
        // the archive's editor, the ladder's foot, the man
        // at the edge, the album's keeper); and after the
        // rehanging, one person per landing says the
        // uncomfortable thing, standing, forever.
        Mystery10.witnessLine10(state, npcName)?.let { out.add(it) }
        Mystery10.aftermathLine10(state, npcName)?.let { out.add(it) }
        // the voice case rides the same rail — a witness's
        // telling grants its piece (the fair copyist, the
        // dictation-taker, the locker's finder, the
        // signer's mother, the signer's father); and after
        // the Open Letters, one person per landing says
        // the uncomfortable thing, standing, forever.
        Mystery11.witnessLine11(state, npcName)?.let { out.add(it) }
        Mystery11.aftermathLine11(state, npcName)?.let { out.add(it) }
        // the returns case rides the same rail — a witness's
        // telling grants its piece (the filing's proctor, the
        // dictation-taker, the pond's loser, the notebook's
        // owner, the library's finder); and after the
        // reading, one person per landing says the
        // uncomfortable thing, standing, forever.
        Mystery12.witnessLine12(state, npcName)?.let { out.add(it) }
        Mystery12.aftermathLine12(state, npcName)?.let { out.add(it) }
        // the exhibition case rides the same rail — a witness's
        // telling grants its piece (the ladder's foot, the
        // hanger, the chain's keeper, the examiner, the guest
        // book's keeper); and after the reading, one person
        // per landing says the uncomfortable thing, standing,
        // forever.
        Mystery13.witnessLine13(state, npcName)?.let { out.add(it) }
        Mystery13.aftermathLine13(state, npcName)?.let { out.add(it) }
        // a relationship arc's current beat plays before
        // the small talk too — once per person per stage.
        StoryArcs.talkLine(state, npcName)?.let { out.add(it) }
        // during a holiday window, the beat gets referenced
        // in conversation — a participant whose step is
        // still waiting says their piece (once a day each).
        HolidayBeats.ambientLine(state, npcName)?.let { out.add(it) }
        // during a finale week, the ensemble talks about
        // nothing else (once a day each).
        Finales.ambientLine(state, npcName)?.let { out.add(it) }
        // v0.6.0b: a favor chain past its first link is town
        // gossip — a friend of its origin asker says the
        // town's piece (once per chain per day, quiet hours
        // respected inside Errands.chainGossipLine).
        Errands.chainGossipLine(state, npcName)?.let { out.add(it) }
        // an asker's post-errand thank-you — written by
        // Errands.complete, played exactly once, here,
        // before the small talk like every other thing
        // they owe you.
        state.lifeText.remove("errandThanks:$npcName")?.let { thanks ->
            out.add(Line(npcName, thanks))
        }
        // v0.8.0c: an emergency that got away — the town
        // coped, and the asker tells you how it went,
        // once, the next time you meet. Written by
        // Emergencies.settleExpired; the removal IS the
        // playing, like the thank-you above.
        state.lifeText.remove("emergCoping:$npcName")?.let { coping ->
            out.add(Line(npcName, coping))
        }
        // v0.9.0b: a crew member who couldn't come — school,
        // a shift, a family table, the town's bigger
        // business — says so here, once, the next time you
        // talk. The removal IS the playing, like the
        // coping line above (Crew owns the words).
        Crew.owedLine(state, npcName)?.let { out.add(it) }
        // v0.9.0d: someone holding a framed print you made
        // them says so here, once per print — the darkroom's
        // gift remembered out loud (Darkroom owns the words
        // and the consuming; a print nobody holds says
        // nothing, like every owed line).
        Darkroom.owedLine(state, npcName)?.let { out.add(it) }
        // v0.7.0d: the teachers remark on your attendance
        // record — Garrison grudgingly, Victoria through the
        // file (once per teacher per day, and only once
        // there's a record to remark on; School owns it).
        School.ambientLine(state, npcName)?.let { out.add(it) }
        // v0.8.0b: a faction senior's below-gate hint — the
        // chain is done, the big ask exists, and you are not
        // yet family. One line, once a day, no menu; the
        // stamping is the playing, like the thank-you above.
        Capstones.hintLine(state, npcName)?.let { out.add(it) }
        // v0.8.0e: the rivalry, talked about — the rival's
        // own latest-result line while it's fresh (a gloat,
        // a grudge, a draw's complaint), their challenge-day
        // nudge, and the town's gossip: one carrier a day
        // states the last result and the score, in the score
        // line's own factual words.
        Rival.ambientLine(state, npcName)?.let { out.add(it) }
        // v0.9.0e: the crews, talked about — while a crew
        // result is fresh, one carrier a day (never a
        // participant) repeats the crews standings line
        // verbatim: the town has a second scoreboard now
        // and it keeps up with it
        RivalCrew.gossipLine(state, npcName)?.let { out.add(it) }
        // v0.10.0c: the school play's owed lines — the
        // cast mid-rehearsals, the passed-over kid's one
        // kind word, a cast parent at their family's
        // dinner table, and the verdict quoted back after
        // the show (Play owns the stamps and the facts).
        Play.gossipLine(state, npcName)?.let { out.add(it) }
        // v0.13.0c: the talent show's talk — the bill
        // quoting its acts in the run-up (seeded days,
        // Play's pattern), and the winner and the bill
        // remembering the meter out loud after the night.
        // A seam on this chain, not a new rail: the gate
        // still stands at eleven.
        Talent.gossipLine(state, npcName)?.let { out.add(it) }
        // v0.13.0d: the parade's talk — riders naming
        // their floats in the run-up week, and the day
        // remembered out loud for a week after. A seam on
        // this chain, not a new rail: the gate still
        // stands at eleven.
        Parade.gossipLine(state, npcName)?.let { out.add(it) }
        // v0.14.0d: on your birthday, anyone who knows you
        // well enough to know the day wishes you well —
        // once per person per birthday, friendship-band
        // derived, moving nothing but its own stamp. A
        // seam on this chain, not a new rail: the gate
        // still stands at eleven.
        PlayerBirthday.wishLine(state, npcName)?.let { out.add(it) }
        return out
    }
}
