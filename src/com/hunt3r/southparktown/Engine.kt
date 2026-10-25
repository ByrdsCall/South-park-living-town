package com.hunt3r.southparktown

import kotlin.math.min
import kotlin.random.Random

// ============================================================
// Engine — pure Kotlin, zero android.* imports. JVM-testable.
// Clock modes: IN-GAME clock (default) = 15-minute ticks from 8 AM, advanced by
// play/wait/timer. REAL-TIME sync (opt-in setting) = town time follows the device
// clock via the Clock interface (UI layer provides SystemClock; tests can fake it).
// ============================================================

interface Clock {
    fun nowMinutes(): Int      // 0..1439
    fun todayKey(): String     // "yyyyMMdd"
    fun dayOfYear(): Int       // 1..366
    fun month(): Int           // 1..12
    fun hour(): Int            // 0..23
}

data class Line(val speaker: String, val text: String)

data class Animal(val id: String, var species: String, var locationId: String)

data class Agent(
    var name: String,
    var description: String,
    val color: String,
    var location: String,
    var coords: Pair<Int, Int>,
    val waypoints: MutableList<Pair<Int, Int>> = mutableListOf(),
    var destination: String,
    var activity: String,
    val agenda: MutableMap<String, AgendaBlock> = mutableMapOf(),
    val agendaBase: Map<String, AgendaBlock> = emptyMap(),
    val memories: MutableList<String> = mutableListOf(),
    var chatCooldownUntil: Int = 0,
    val isPlayer: Boolean = false,
    var appearance: Map<String, String> = emptyMap(),
    var asleep: Boolean = false
)

data class Spawn(
    val id: String,
    val collectibleId: String,
    val locationId: String,
    var wiggled: Boolean = false
)

data class GameState(
    var timeMin: Int = 480,
    // In-game calendar (used when real-time sync is OFF). Day 1 = Oct 1, 2026.
    var gameYear: Int = 2026,
    var gameMonth: Int = 10,
    var gameDay: Int = 1,
    val agents: MutableMap<String, Agent> = mutableMapOf(),
    val animals: MutableList<Animal> = mutableListOf(),
    var weatherId: String = "light_snow",
    val events: MutableList<String> = mutableListOf(),
    var todayKey: String = "",
    var lastPlayedDate: String = "",
    var spawnDate: String = "",
    var hasPlaced: Boolean = false,
    val friendship: MutableMap<String, Int> = mutableMapOf(),
    val dailyGain: MutableMap<String, Int> = mutableMapOf(),
    val collected: MutableSet<String> = mutableSetOf(),
    val spawns: MutableList<Spawn> = mutableListOf(),
    var lastConversation: List<Line> = emptyList(),
    // ---- v0.1.5 additions (all optional in saves; defaults keep old saves valid) ----
    var playerInside: Boolean = false,
    var shenaniganId: String? = null,
    var shenaniganDay: String = "",
    var lastShenaniganId: String? = null,
    var lastEventWeek: Int = -1,
    var eventMonthKey: String = "",
    var eventsThisMonth: Int = 0,
    var lastStageSeen: Int = -1,
    val factionStates: MutableMap<String, FactionState> = Factions.defaultStates(),
    val factionRelations: MutableMap<String, Int> = Factions.defaultRelations(),
    val inventory: MutableMap<String, Int> = mutableMapOf(),
    val trashSearched: MutableMap<String, String> = mutableMapOf(),
    val litter: MutableList<LitterDrop> = mutableListOf(),
    val npcBonds: MutableMap<String, Int> = mutableMapOf(),
    val questCounters: MutableMap<String, Int> = mutableMapOf(),
    val scheduleDrift: MutableMap<String, MutableMap<String, String>> = mutableMapOf(),
    // ---- v0.1.8 additions (all optional in saves; defaults keep old saves valid) ----
    var absDay: Int = 0,                       // total in-game days elapsed (upkeep/absence math)
    var holidayId: String? = null,             // today's holiday, run through the story machine
    var participation: Int = 0,                // player's hand in the active event (0..10)
    var eventWitness: String? = null,          // generated resident named witness/organizer of the event
    val relationPressure: MutableMap<String, Int> = mutableMapOf(), // ambient sparks -> faction relations
    val locVisits: MutableMap<String, Int> = mutableMapOf(),        // where the player haunts
    val convertedResidents: MutableList<String> = mutableListOf(), // visitors who moved in (lore names)
    val townLog: MutableList<String> = mutableListOf(),             // town history (events, holidays)
    val life: MutableMap<String, Int> = mutableMapOf(),             // living-world counters (rep, sightings...)
    val lifeText: MutableMap<String, String> = mutableMapOf(),      // living-world text state (marks, gnome...)
    val puddles: MutableMap<String, Int> = mutableMapOf(),          // locId -> puddle level 0..3
    val frozenPuddles: MutableSet<String> = mutableSetOf(),         // locIds whose puddles are iced
    // ---- v0.1.9c additions (additive; absent in old saves -> empty) ----
    val episodes: MutableList<EpisodeRecord> = mutableListOf(),     // episode framing history
    // ---- v0.1.9e additions (additive; absent in old saves -> empty) ----
    val npcGoals: MutableMap<String, NpcGoal> = mutableMapOf(),     // per-NPC current goal records
    // ---- v0.2.0c additions (additive; absent in old saves -> empty) ----
    val photos: MutableList<PhotoRecord> = mutableListOf(),        // the selfie album (records, not pixels)
    var followers: Int = 0,                                        // 📸 follower count
    val photographed: MutableMap<String, Int> = mutableMapOf(),    // name -> times photographed
    val photoFeats: MutableSet<String> = mutableSetOf(),           // completed photo-feat ids
    // ---- v0.2.0e additions (additive; absent in old saves -> empty) ----
    val fishLog: MutableMap<String, FishRecord> = mutableMapOf(),  // 🎣 species id -> count/biggest
    var fishCasts: Int = 0,                                        // lifetime casts at Stark's Pond
    val showdown: MutableMap<String, ShowdownRecord> = mutableMapOf(), // ❄️ opponent -> W/L
    var tacoHigh: Int = 0,                                         // 🌮 Taco Catch high score
    var tacoPlays: Int = 0,                                         // 🌮 Taco Catch runs played
    val gameFeats: MutableSet<String> = mutableSetOf(),            // completed minigame feat ids
    // ---- v0.2.0f additions (additive; absent in old saves -> a fresh case) ----
    var arcChapter: Int = 0,                                       // 🔍 mystery: NEXT chapter index to air (5 = all aired)
    var arcDueAbsDay: Int = -1,                                    // 🔍 absDay the next chapter is due (-1 = unscheduled)
    var arcStarted: Boolean = false,                               // 🔍 chapter 1 has aired (the case exists)
    var arcRetired: Boolean = false,                               // 🔍 finale wrapped — the arc never runs again
    var arcBranch: String = "",                                    // 🔍 "" | "truth" | "silent" | "town"
    var arcChoice: String = "",                                    // 🔍 the finale choice locked at the climax: "" | "truth" | "silent"
    val mysteryClues: MutableSet<String> = mutableSetOf(),         // 🔍 clue ids found (Mystery.CLUES)
    // ---- v0.3.0f additions (additive; absent in old saves -> the second case waits its turn) ----
    var arc2DueAbsDay: Int = -1,                                   // 🔍 booth case: absDay the marathon is due (-1 = unscheduled)
    var arc2Started: Boolean = false,                              // 🔍 the marathon has aired (the case exists)
    var arc2MarathonDay: Int = -1,                                 // 🔍 absDay the marathon aired (interviews count from here)
    var arc2AccuseDueAbsDay: Int = -1,                             // 🔍 absDay the second screening is due
    var arc2AccusationAired: Boolean = false,                      // 🔍 the second screening took the slot
    var arc2Retired: Boolean = false,                              // 🔍 accusation wrapped — the case never reopens
    var arc2Branch: String = "",                                   // 🔍 "" | "solved" | "wrong" | "cold"
    var arc2Accused: String = "",                                  // 🔍 the name said at the accusation (Mystery2.SUSPECT_KEYS)
    var arc2Declined: Boolean = false,                             // 🔍 the moment was offered and the player kept their seat
    var arc2Usher: String = "",                                    // 🔍 this save's generated usher (lore name; "" = none picked)
    var arc2AlibiDay: Int = -1,                                    // 🔍 absDay the last alibi telling was granted
    var arc2RetiredDay: Int = -1,                                  // 🔍 absDay the case closed (the lost-and-found clock)
    var arc2ReelFound: Boolean = false,                            // 🔍 the reel quietly turned up
    val mystery2Clues: MutableSet<String> = mutableSetOf(),        // 🔍 clue ids found (Mystery2.CLUES2)
    val arc2FlavorSeen: MutableSet<String> = mutableSetOf(),       // 🔍 suspect flavor heard (Clyde/Craig)
    // ---- v0.4.0f additions (additive; absent in old saves -> the third case waits for the second) ----
    var arc3Chapter: Int = 0,                                      // 🔍 buried case: NEXT chapter index to air (4 = all aired)
    var arc3DueAbsDay: Int = -1,                                   // 🔍 absDay the next chapter is due (-1 = unscheduled)
    var arc3Started: Boolean = false,                              // 🔍 chapter 1 has aired (the case exists)
    var arc3Retired: Boolean = false,                              // 🔍 finale wrapped — the arc never runs again
    var arc3Branch: String = "",                                   // 🔍 "" | "publish" | "bury" | "sharon" | "county"
    var arc3Choice: String = "",                                   // 🔍 the disposition locked at the climax: "" | "publish" | "bury" | "sharon"
    var arc3RetiredDay: Int = -1,                                  // 🔍 absDay the case closed
    var arc3Favor: Boolean = false,                                // 🔍 the "bury" landing: Randy owes you one, forever
    var arc3SharonPending: Boolean = false,                        // 🔍 the "sharon" landing: her decision hasn't landed yet
    var arc3SharonDay: Int = -1,                                   // 🔍 absDay her decision lands
    var arc3SharonText: String = "",                               // 🔍 her decision, in full (the board keeps it)
    val mystery3Clues: MutableSet<String> = mutableSetOf(),        // 🔍 clue ids found (Mystery3.CLUES3)
    // ---- v0.6.0c additions (additive; absent in old saves -> the fourth case waits for the third) ----
    var arc4Chapter: Int = 0,                                      // 🌱 gardener case: NEXT chapter index to air (4 = all aired)
    var arc4DueAbsDay: Int = -1,                                   // 🌱 absDay the next chapter is due (-1 = unscheduled)
    var arc4Started: Boolean = false,                              // 🌱 chapter 1 has aired (the case exists)
    var arc4Retired: Boolean = false,                              // 🌱 finale wrapped — the arc never runs again
    var arc4Branch: String = "",                                   // 🌱 "" | "name" | "guard" | "join" | "cold"
    var arc4Choice: String = "",                                   // 🌱 the call locked at the vigil: "" | "name" | "guard" | "join"
    var arc4RetiredDay: Int = -1,                                  // 🌱 absDay the case closed (the extra fixes count from here)
    var arc4LastFixPending: Boolean = false,                       // 🌱 the "name" landing: the last fix hasn't landed yet
    var arc4LastFixDay: Int = -1,                                  // 🌱 absDay the last fix lands
    var arc4LastFixLanded: Boolean = false,                        // 🌱 the last fix stands at your porch
    val mystery4Clues: MutableSet<String> = mutableSetOf(),        // 🌱 clue ids found (Mystery4.CLUES4)
    // ---- v0.7.0f additions (additive; absent in old saves -> the case queues fresh) ----
    var arc5Chapter: Int = 0,                                      // ⚖️ chapters aired (Mystery5.CHAPTERS5)
    var arc5DueAbsDay: Int = -1,                                   // ⚖️ absDay the next chapter may air (10d after arc #4 retired)
    var arc5Started: Boolean = false,                              // ⚖️ derby day has aired
    var arc5Retired: Boolean = false,                              // ⚖️ the case has closed, forever
    var arc5Branch: String = "",                                   // ⚖️ the landing: "" | "name" | "believe" | "clear" | "cold"
    var arc5Choice: String = "",                                   // ⚖️ the call locked at the confrontation: "" | "name" | "believe" | "clear"
    var arc5RetiredDay: Int = -1,                                  // ⚖️ absDay the case closed
    var arc5LurePending: Boolean = false,                          // 🎣 the cleared landing: the lure hasn't come home yet
    var arc5LureDay: Int = -1,                                     // 🎣 absDay the lure comes home
    var arc5LureReturned: Boolean = false,                         // 🎣 the lure is back on its plaque
    val mystery5Clues: MutableSet<String> = mutableSetOf(),        // ⚖️ evidence ids found (Mystery5.CLUES5)
    // ---- v0.8.0f additions (additive; absent in old saves -> the case queues fresh) ----
    var arc6Chapter: Int = 0,                                      // 🏛️ chapters aired (Mystery6.CHAPTERS6)
    var arc6DueAbsDay: Int = -1,                                   // 🏛️ absDay the next chapter may air (10d after arc #5 retired)
    var arc6Started: Boolean = false,                              // 🏛️ unveiling day has aired
    var arc6Retired: Boolean = false,                              // 🏛️ the case has closed, forever
    var arc6Branch: String = "",                                   // 🏛️ the landing: "" | "cleared" | "convicted" | "hung" | "cold"
    var arc6Choice: String = "",                                   // 🏛️ the call locked at the meeting: "" | "present" | "back" | "speak" | "land"
    var arc6RetiredDay: Int = -1,                                  // 🏛️ absDay the case closed
    var arc6Presented: Boolean = false,                            // 🏛️ the evidence was actually put on the table at the meeting
    var arc6WallPending: Boolean = false,                          // 🎨 the cleared landing: the quiet corner hasn't come back yet
    var arc6WallDay: Int = -1,                                     // 🎨 absDay the corner comes back
    var arc6WallRestored: Boolean = false,                         // 🎨 the corner is whole again, unsigned
    val mystery6Clues: MutableSet<String> = mutableSetOf(),        // 🏛️ evidence ids proven (Mystery6.CLUES6)
    val arc6Gossip: MutableSet<String> = mutableSetOf(),           // 🗣️ gossip beat ids WITNESSED (Mystery6.GOSSIP6) — the meter's ears
    val arc6Voice: MutableMap<String, String> = mutableMapOf(),    // 🎙️ gossip id -> the stance taken ("back"|"question"|"silent")
    // ---- v0.9.0f additions (additive; absent in old saves -> the case queues fresh) ----
    var arc7Chapter: Int = 0,                                      // 🕰️ chapters aired (Mystery7.CHAPTERS7)
    var arc7DueAbsDay: Int = -1,                                   // 🕰️ absDay the next chapter may air (10d after arc #6 retired)
    var arc7Started: Boolean = false,                              // 🕰️ the taking has aired
    var arc7Retired: Boolean = false,                              // 🕰️ the case has closed, forever
    var arc7Branch: String = "",                                   // 🕰️ the landing: "" | "origin" | "again" | "mended" | "cold"
    var arc7Choice: String = "",                                   // 🕰️ the call locked at the homecoming: "" | "origin" | "stand" | "change" | "walk"
    var arc7RetiredDay: Int = -1,                                  // 🕰️ absDay the case closed
    var arc7CasePending: Boolean = false,                          // 🏫 the origin landing: the trowel's case hasn't been mended yet
    var arc7CaseDay: Int = -1,                                     // 🏫 absDay the case is mended
    var arc7CaseBuilt: Boolean = false,                            // 🏫 the trowel is home, under new glass, with the whole story
    val mystery7Clues: MutableSet<String> = mutableSetOf(),        // 🕰️ evidence ids in hand (Mystery7.CLUES7; two sit inert until their context chapter)
    val arc7Commits: MutableMap<String, String> = mutableMapOf(),  // 🧾 movement id -> the reconstruction committed ("easy" | "checked"); verdicts derived, never stored
    // ---- v0.10.0f additions (additive; absent in old saves -> the case queues fresh) ----
    var arc8Chapter: Int = 0,                                      // 🔤 chapters aired (Mystery8.CHAPTERS8)
    var arc8DueAbsDay: Int = -1,                                   // 🔤 absDay the next chapter may air (10d after arc #7 retired)
    var arc8Started: Boolean = false,                              // 🔤 the sign has been read
    var arc8Retired: Boolean = false,                              // 🔤 the case has closed, forever
    var arc8Branch: String = "",                                   // 🔤 the landing: "" | "aligned" | "accused" | "stood" | "cold"
    var arc8Choice: String = "",                                   // 🔤 the call locked at the reading: "" | "read" | "name" | "stand" | "walk"
    var arc8RetiredDay: Int = -1,                                  // 🔤 absDay the case closed
    var arc8LetterPending: Boolean = false,                        // 🔤 the aligned landing: the missing letter hasn't come back yet
    var arc8LetterDay: Int = -1,                                   // 🔤 absDay the letter comes back
    var arc8LetterBack: Boolean = false,                           // 🔤 the tray is full again, unwitnessed
    val mystery8Clues: MutableSet<String> = mutableSetOf(),        // 🔤 evidence ids in hand (Mystery8.CLUES8; two sit inert until their context chapter)
    val arc8Pairs: MutableMap<String, String> = mutableMapOf(),    // ✒️ pair id -> the judgment written ("same" | "different"); verdicts derived, never stored
    // ---- v0.11.0f additions (additive; absent in old saves -> the case queues fresh) ----
    var arc9Chapter: Int = 0,                                      // 🧮 chapters aired (Mystery9.CHAPTERS9)
    var arc9DueAbsDay: Int = -1,                                   // 🧮 absDay the next chapter may air (10d after arc #8 retired)
    var arc9Started: Boolean = false,                              // 🧮 the count has come in
    var arc9Retired: Boolean = false,                              // 🧮 the case has closed, forever
    var arc9Branch: String = "",                                   // 🧮 the landing: "" | "reconciled" | "blamed" | "shelved" | "cold"
    var arc9Choice: String = "",                                   // 🧮 the call locked at the reckoning: "" | "read" | "name" | "stand" | "walk"
    var arc9RetiredDay: Int = -1,                                  // 🧮 absDay the case closed
    var arc9ListPending: Boolean = false,                          // 🧮 the reconciled landing: the roll hasn't been reprinted yet
    var arc9ListDay: Int = -1,                                     // 🧮 absDay the roll is reprinted whole
    var arc9ListRestored: Boolean = false,                         // 🧮 fourteen faces, fourteen names — the roll is whole
    val mystery9Clues: MutableSet<String> = mutableSetOf(),        // 🧮 evidence ids in hand (Mystery9.CLUES9; two sit inert until their context chapter)
    val arc9Calls: MutableMap<String, String> = mutableMapOf(),    // ✒️ site id -> the reconciliation call written ("count" | "record" | "added"); verdicts derived, never stored
    // ---- v0.12.0f additions (additive; absent in old saves -> the case queues fresh) ----
    var arc10Chapter: Int = 0,                                     // 📷 chapters aired (Mystery10.CHAPTERS10)
    var arc10DueAbsDay: Int = -1,                                  // 📷 absDay the next chapter may air (10d after arc #9 retired)
    var arc10Started: Boolean = false,                             // 📷 the hanging has aired
    var arc10Retired: Boolean = false,                             // 📷 the case has closed, forever
    var arc10Branch: String = "",                                  // 📷 the landing: "" | "complete" | "framed" | "hung" | "unexposed"
    var arc10Choice: String = "",                                  // 📷 the call locked at the rehanging: "" | "read" | "name" | "stand" | "walk"
    var arc10RetiredDay: Int = -1,                                 // 📷 absDay the case closed
    var arc10LabelPending: Boolean = false,                        // 📷 the complete landing: the true card hasn't been printed yet
    var arc10LabelDay: Int = -1,                                   // 📷 absDay the true card is printed
    var arc10LabelDone: Boolean = false,                           // 📷 the pair hangs together, cards honest
    val mystery10Clues: MutableSet<String> = mutableSetOf(),       // 📷 evidence ids in hand (Mystery10.CLUES10; two sit inert until their context chapter)
    val arc10Calls: MutableMap<String, String> = mutableMapOf(),   // ✒️ frame id -> the reading written ("genuine" | "misfiled" | "composite"); verdicts derived, never stored
    // ---- v0.13.0f additions (additive; absent in old saves -> the case queues fresh) ----
    var arc11Chapter: Int = 0,                                     // 🎙 chapters aired (Mystery11.CHAPTERS11)
    var arc11DueAbsDay: Int = -1,                                  // 🎙 absDay the next chapter may air (10d after arc #10 retired)
    var arc11Started: Boolean = false,                             // 🎙 the mail slot has aired
    var arc11Retired: Boolean = false,                             // 🎙 the case has closed, forever
    var arc11Branch: String = "",                                  // 🎙 the landing: "" | "voiced" | "named" | "standing" | "unopened"
    var arc11Choice: String = "",                                  // 🎙 the call locked at the open letters: "" | "voice" | "name" | "stand" | "walk"
    var arc11RetiredDay: Int = -1,                                 // 🎙 absDay the case closed
    var arc11PostPending: Boolean = false,                         // 🎙 the voiced landing: the correction cards haven't been posted yet
    var arc11PostDay: Int = -1,                                    // 🎙 absDay the correction cards are posted
    var arc11Posted: Boolean = false,                              // 🎙 two names stand in their own voices again
    val mystery11Clues: MutableSet<String> = mutableSetOf(),       // 🎙 evidence ids in hand (Mystery11.CLUES11; two sit inert until their context chapter)
    val arc11Calls: MutableMap<String, String> = mutableMapOf(),   // ✒️ letter id -> the reading written ("in" | "off" | "borrowed"); verdicts derived, never stored
    // ---- v0.14.0f additions (additive; absent in old saves -> the case queues fresh) ----
    var arc12Chapter: Int = 0,                                     // 📦 chapters aired (Mystery12.CHAPTERS12)
    var arc12DueAbsDay: Int = -1,                                  // 📦 absDay the next chapter may air (10d after arc #11 retired)
    var arc12Started: Boolean = false,                             // 📦 the returns desk has aired
    var arc12Retired: Boolean = false,                             // 📦 the case has closed, forever
    var arc12Branch: String = "",                                  // 📦 the landing: "" | "read" | "named" | "standing" | "unread"
    var arc12Choice: String = "",                                  // 📦 the call locked at the reading: "" | "read" | "name" | "stand" | "walk"
    var arc12RetiredDay: Int = -1,                                 // 📦 absDay the case closed
    var arc12PostPending: Boolean = false,                         // 📦 the read landing: the quiet correction hasn't come yet
    var arc12PostDay: Int = -1,                                    // 📦 absDay the quiet correction comes
    var arc12Posted: Boolean = false,                              // 📦 the staged returns stand corrected, home at last
    val mystery12Clues: MutableSet<String> = mutableSetOf(),       // 📦 evidence ids in hand (Mystery12.CLUES12; two sit inert until their context chapter)
    val arc12Calls: MutableMap<String, String> = mutableMapOf(),   // ✒️ return id -> the reading written ("true" | "mistake" | "planted"); verdicts derived, never stored
    // ---- v0.15.0j additions (additive; absent in old saves -> the case queues fresh) ----
    var arc13Chapter: Int = 0,                                     // 🖼 chapters aired (Mystery13.CHAPTERS13)
    var arc13DueAbsDay: Int = -1,                                  // 🖼 absDay the next chapter may air (10d after arc #12 retired, and a qualifying show wrapped)
    var arc13Started: Boolean = false,                             // 🖼 the wall audit has aired
    var arc13Retired: Boolean = false,                             // 🖼 the case has closed, forever
    var arc13Branch: String = "",                                  // 🖼 the landing: "" | "read" | "named" | "standing" | "unread"
    var arc13Choice: String = "",                                  // 🖼 the call locked at the reading: "" | "read" | "name" | "stand" | "walk"
    var arc13RetiredDay: Int = -1,                                 // 🖼 absDay the case closed
    var arc13PostPending: Boolean = false,                         // 🖼 the read landing: the quiet rehang hasn't come yet
    var arc13PostDay: Int = -1,                                    // 🖼 absDay the quiet rehang comes
    var arc13Posted: Boolean = false,                              // 🖼 the audited wall stands rehung true
    var arc13ShowId: String = "",                                  // 🖼 the audited show's id, booked when the queue anchors ("" until then)
    val mystery13Clues: MutableSet<String> = mutableSetOf(),       // 🖼 evidence ids in hand (Mystery13.CLUES13; two sit inert until their context chapter)
    val arc13Calls: MutableMap<String, String> = mutableMapOf(),   // ✒️ frame id -> the reading written ("true" | "mistake" | "swapped"); verdicts derived, never stored
    // ---- v0.3.0c additions (additive; absent in old saves -> arcs start fresh) ----
    val storyArcs: MutableMap<String, StoryArcState> = mutableMapOf(), // 📖 relationship arcs (Stories.ARCS)
    // ---- v0.4.0b additions (additive; absent in old saves -> an empty plate) ----
    val errands: MutableList<Errand> = mutableListOf(),        // 📋 accepted errands (done pruned at rollover)
    val errandAsked: MutableMap<String, Int> = mutableMapOf(), // 📋 npc -> absDay an ask was last offered
    var errandsDoneCount: Int = 0,                             // 📋 lifetime errands completed
    val errandsRecent: MutableList<ErrandDoneNote> = mutableListOf(), // 📋 last completions (capped)
    // ---- v0.5.0a additions (additive; absent in old saves -> nobody has walked anywhere yet) ----
    var walkDest: String = "",                                 // 🗺 active trip destination ("" = no trip)
    var walkCarryMin: Int = 0,                                 // 🗺 sub-tick minutes banked toward the next 15-min tick
    var walkTrips: Int = 0,                                    // 🗺 lifetime trips completed on foot
    var walkLegs: Int = 0,                                     // 🗺 lifetime legs walked
    val walkShortcuts: MutableSet<String> = mutableSetOf(),    // 🗺 "from>to" legs with a learned cut-through (capped at the edge count)
    var busPass: Boolean = false,                              // 🚌 the pass is yours (this flag is the authority; the card sits in the pack)
    // ---- v0.5.0b additions (additive; absent in old saves -> nothing unlocked yet) ----
    val unlockedWardrobe: MutableSet<String> = mutableSetOf(),  // 👕 Wardrobe piece ids owned (Wardrobe.PIECES; decode-filtered to the registry)
    // ---- v0.5.0c additions (additive; absent in old saves -> an empty binder) ----
    val pocketPals: MutableMap<String, Int> = mutableMapOf(),  // 🃏 Pocket Pal card id -> copies owned (PocketPals.CARDS; decode-filtered + capped)
    // ---- v0.5.0e additions (additive; absent in old saves -> a silent phone) ----
    val phoneTexts: MutableList<PhoneText> = mutableListOf(),  // 📱 the Messages store (Phone.MAX_TEXTS-capped)
    val phoneNotes: MutableList<String> = mutableListOf(),     // 📱 noted facts awaiting the morning ("absDay|KIND|a|b")
    val phoneMeta: MutableMap<String, Int> = mutableMapOf(),   // 📱 phone bookkeeping (rollover day, reply caps, case snapshots)
    // ---- v0.6.0a additions (additive; absent in old saves -> an empty plate, no posting memory) ----
    val commissions: MutableList<Commission> = mutableListOf(),       // 📷 accepted commissions (done pruned at rollover)
    val commissionLast: MutableMap<String, String> = mutableMapOf(),  // 📷 asker -> last posted ask's signature (no-repeat memory)
    var commissionsDone: Int = 0,                                     // 📷 lifetime commissioned prints landed
    val commissionRecent: MutableList<CommissionNote> = mutableListOf(), // 📷 last landed prints (capped)
    // ---- v0.8.0d additions (additive; absent in old saves -> no volumes yet) ----
    val yearbooks: MutableList<YearbookVolume> = mutableListOf(), // 📖 published yearbooks (Yearbook; full volumes capped, elders index-only)
    // ---- v0.9.0c addition (additive; absent in old saves -> no fairs yet) ----
    val fairs: MutableList<FairRecord> = mutableListOf(),      // 🎡 county fair records (Fair; one per year, newest whole on the page, elders one-line)
    // ---- v0.9.0d addition (additive; absent in old saves -> an empty print box) ----
    val prints: MutableList<DarkroomPrint> = mutableListOf(),   // 🖨 darkroom prints (Darkroom; each carries its own copy of a photo's record; box + wall + gifted, capped)
    val galleryShows: MutableList<GalleryShow> = mutableListOf() // 🖼 gallery shows (Gallery; newest FULL_SHOWS whole, elders index-only)
)

const val SLEEP_ACTIVITY = "Asleep. Zzz..."
const val MAX_LITTER = 40

/** Phase-2 voice hook — no-op stub (port of voice.py). */
object Speak {
    fun speak(lines: List<Line>) {
        // Milestone 1: text only. Phase 2 plugs TTS -> RVC -> playback here.
    }
}

fun minutesToTimeStr(m: Int): String {
    val mm = ((m % 1440) + 1440) % 1440
    val hrs = mm / 60
    val mins = mm % 60
    val ampm = if (hrs < 12) "AM" else "PM"
    var dh = if (hrs <= 12) hrs else hrs - 12
    if (dh == 0) dh = 12
    return "%02d:%02d %s".format(dh, mins, ampm)
}

fun timeStrToMinutes(s: String): Int {
    val parts = s.split(":")
    return parts[0].toInt() * 60 + parts[1].toInt()
}

fun isSleepHour(hour: Int): Boolean = hour >= 22 || hour < 7

fun dailySeed(todayKey: String): Long = todayKey.toLongOrNull() ?: todayKey.hashCode().toLong()

/** In-game todayKey ("yyyyMMdd") derived from the in-game calendar. */
fun gameTodayKey(state: GameState): String =
    "%04d%02d%02d".format(state.gameYear, state.gameMonth, state.gameDay)

private val MONTH_LENGTHS = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)

/** Ordinal day number for a "yyyyMMdd" key (proleptic Gregorian). */
fun ordinalOfKey(key: String): Int {
    if (key.length < 8) return 0
    val y = key.substring(0, 4).toIntOrNull() ?: return 0
    val m = key.substring(4, 6).toIntOrNull() ?: return 0
    val d = key.substring(6, 8).toIntOrNull() ?: return 0
    // v0.1.9f: a corrupt key ("20269901") used to index MONTH_LENGTHS
    // out of bounds below — out-of-range fields now read as "no date".
    if (m !in 1..12 || d !in 1..31) return 0
    val yy = y - 1
    var ord = yy * 365 + yy / 4 - yy / 100 + yy / 400
    for (mm in 1 until m) {
        ord += MONTH_LENGTHS[mm - 1]
        if (mm == 2 && isLeapYear(y)) ord++
    }
    return ord + d
}

/** Signed days from key [a] to key [b] (b - a). */
fun daysBetween(a: String, b: String): Int = ordinalOfKey(b) - ordinalOfKey(a)

/** v0.6.0a: the inverse of [ordinalOfKey] — a "yyyyMMdd" key
 *  for a proleptic Gregorian ordinal (1 = 0001-01-01), via
 *  Hinnant's civil-from-days. Round-trips are suite-pinned
 *  across leap days and the game's own epoch. */
fun keyFromOrdinal(ord: Int): String {
    val z = ord + 305  // ordinal -> days on the 0000-03-01 civil epoch
    val era = if (z >= 0) z / 146097 else (z - 146096) / 146097
    val doe = z - era * 146097
    val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
    val y = yoe + era * 400
    val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
    val mp = (5 * doy + 2) / 153
    val d = doy - (153 * mp + 2) / 5 + 1
    val m = if (mp < 10) mp + 3 else mp - 9
    val year = if (m <= 2) y + 1 else y
    return "%04d%02d%02d".format(year, m, d)
}

/** Day of week for the in-game date: 0=Sunday .. 6=Saturday.
 *  Anchor: 2026-10-01 (game day 1) is a Thursday (=4). */
fun dayOfWeek(state: GameState): Int {
    val diff = ordinalOfKey(gameTodayKey(state)) - ordinalOfKey("20261001")
    return ((4 + diff) % 7 + 7) % 7
}

fun isWeekend(state: GameState): Boolean = dayOfWeek(state) in setOf(0, 6)

/** The weather the drift table says is most likely NEXT (v0.1.8
 *  forecast): highest season-adjusted drift weight, current excluded. */
fun forecastWeather(state: GameState): String {
    val opts = WEATHER_DRIFT[state.weatherId] ?: return state.weatherId
    val mult = SEASON_WEATHER_MULT[seasonFor(state.gameMonth)] ?: emptyMap()
    return opts.filter { it.first != state.weatherId }
        .maxByOrNull { it.second * (mult[it.first] ?: 1.0) }?.first ?: state.weatherId
}

fun daysInMonth(y: Int, m: Int): Int =
    MONTH_LENGTHS[m - 1] + if (m == 2 && isLeapYear(y)) 1 else 0

/** The holiday whose DECORATIONS are up (#71 decorations creep):
 *  the real holiday on its day, creeping in 1–2 days early, and
 *  lingering one day late while they come down. */
fun displayHoliday(state: GameState): Holiday? {
    currentHoliday(state.todayKey.substring(4))?.let { return it }
    var y = state.gameYear; var m = state.gameMonth; var d = state.gameDay
    repeat(2) {
        d++
        if (d > daysInMonth(y, m)) { d = 1; m++; if (m > 12) { m = 1; y++ } }
        currentHoliday("%02d%02d".format(m, d))?.let { return it }
    }
    y = state.gameYear; m = state.gameMonth; d = state.gameDay - 1
    if (d < 1) {
        m--; if (m < 1) { m = 12; y-- }
        d = daysInMonth(y, m)
    }
    return currentHoliday("%02d%02d".format(m, d))
}

/** Append a line to the town's persistent history (Journal + marks). */
fun townLogLine(state: GameState, line: String) {
    state.townLog.add(line)
    while (state.townLog.size > 30) state.townLog.removeAt(0)
}

fun isLeapYear(y: Int): Boolean = (y % 4 == 0 && y % 100 != 0) || y % 400 == 0

class Engine(seed: Long = System.currentTimeMillis()) {
    private val random = Random(seed)
    private var callState: GameState? = null
    private var batchPresent: List<Agent> = emptyList()
    private var dailyRand: Random = Random(seed)
    var activeHoliday: Holiday? = null

    /**
     * Dialogue goes through here. UI sets it: mock (default) or LLM-backed.
     * kind: "collision" (a, b set), "playerSay" (a=player, batchPresent used by mock),
     *       "playerTalk" (a=target kid).
     */
    var dialogueProvider: (kind: String, a: Agent, b: Agent?, locName: String, playerLine: String?) -> List<Line> =
        { kind, a, b, locName, playerLine ->
            val s = callState
            if (s == null) emptyList() else mockProvide(s, kind, a, b, locName, playerLine)
        }

    /** v0.1.5: UI wires NpcMemoryStore in here — repeat-meeting greeting for a cast NPC, or null. */
    var memoryGreetingProvider: (String) -> String? = { null }

    /** v0.1.5: called when the player witnesses an NPC<->NPC chat (a, b at player's spot). */
    var onWitnessedChat: ((String, String) -> Unit)? = null

    /** v0.2.0b: fired the moment a shenanigans stage advances
     *  (the Activity plays the stage-shift cue; pure presentation). */
    var onStageShift: ((Int) -> Unit)? = null

    /** v0.2.0g: fired the moment the weather actually changes
     *  (the Activity re-feeds the ambience/music beds at once,
     *  instead of waiting for the next UI refresh tick). */
    var onWeatherShift: (() -> Unit)? = null

    private fun invokeProvider(
        state: GameState, kind: String, a: Agent, b: Agent?,
        locName: String, playerLine: String?
    ): List<Line> {
        callState = state
        return try {
            dialogueProvider(kind, a, b, locName, playerLine)
        } finally {
            callState = null
        }
    }

    // ---------------- State ----------------
    fun newState(): GameState {
        val state = GameState()
        for ((name, p) in AGENT_PROFILES) {
            // v0.15.0g: the cast spawns at HOME (CastHomes —
            // family house where one exists, else the
            // workplace anchor), never piled on the Bus
            // Stop. The day's first ticks walk each of them
            // from home to their 08:00 block like anyone
            // heading out in the morning — the kids whose
            // block IS the stop arrive there by schedule,
            // on foot, the way the stop actually fills.
            // (Through v0.15.0f every agent spawned AT the
            // stop; Lucky's screenshot of the whole town
            // standing on it was this line.)
            val home = CastHomes.homeOf(name) ?: "BusStop"
            val a = Agent(
                name = name, description = p.description, color = p.color,
                location = home, coords = LOCATIONS[home]!!.let { it.gx to it.gy },
                destination = home, activity = p.agenda["08:00"]!!.activity,
                agenda = p.agenda.toMutableMap(), agendaBase = p.agenda.toMap()
            )
            a.memories.add("Woke up. Another beautiful crap day in South Park.")
            state.agents[name] = a
            state.friendship[name] = 0
        }
        val me = Agent(
            name = PLAYER_DEFAULT_NAME,
            description = "Lucky, a new kid visiting South Park. Walks everywhere with a heavy backpack, " +
                "up for anything, friends with everybody. The kids are still deciding if he's cool.",
            color = PLAYER_COLOR,
            location = "BusStop", coords = LOCATIONS["BusStop"]!!.let { it.gx to it.gy },
            destination = "BusStop", activity = "Just rolled into town.",
            isPlayer = true
        )
        state.agents[PLAYER_DEFAULT_NAME] = me
        state.animals.addAll(spawnInitialAnimals())
        state.events.add("South Park, Colorado. Population: a bunch of hicks and five fourth-graders.")
        state.events.add("It's 08:00 AM. The kids are heading for the bus stop. Lucky just walked up.")
        state.todayKey = gameTodayKey(state)
        state.lastPlayedDate = state.todayKey
        return state
    }

    fun player(state: GameState): Agent =
        state.agents.values.first { it.isPlayer }

    private fun pushEvent(state: GameState, e: String) {
        state.events.add(e)
        while (state.events.size > 40) state.events.removeAt(0)
    }

    // ---------------- Real-time sync ----------------
    fun syncToDeviceTime(state: GameState, clock: Clock) {
        val today = clock.todayKey()
        if (state.lastPlayedDate != today) {
            state.dailyGain.clear()
            state.lastPlayedDate = today
        }
        state.todayKey = today
        state.timeMin = clock.nowMinutes()
        val monthDay = if (today.length >= 8) today.substring(4) else "0101"
        activeHoliday = currentHoliday(monthDay)
        state.holidayId = activeHoliday?.id // #31: holidays in the story machine
        spawnCollectibles(state, monthDay, today)
        val asleep = isSleepHour(clock.hour())
        for ((name, a) in state.agents) {
            if (a.isPlayer) continue
            if (asleep) {
                // v0.15.0g: home is CastHomes' anchor, as
                // in newState and the in-game night march.
                val home = CastHomes.homeOf(name) ?: a.location
                a.location = home
                a.destination = home
                a.waypoints.clear()
                a.coords = LOCATIONS[home]!!.let { it.gx to it.gy }
                a.activity = SLEEP_ACTIVITY
                a.asleep = true
                continue
            }
            a.asleep = false
            val block = effectiveAgendaBlock(state, a, state.timeMin) ?: continue
            if (!state.hasPlaced) {
                a.location = block.location
                a.destination = block.location
                a.waypoints.clear()
                a.coords = LOCATIONS[block.location]!!.let { it.gx to it.gy }
                a.activity = block.activity
            } else if (a.destination != block.location) {
                a.destination = block.location
                a.activity = "Walking to ${LOCATIONS[block.location]!!.name}."
                a.waypoints.clear()
                a.waypoints.addAll(calculateWaypoints(a.location, block.location))
                pushEvent(state, "🚶 $name took off toward ${LOCATIONS[block.location]!!.name}.")
            }
        }
        state.hasPlaced = true
    }

    fun currentAgendaBlock(a: Agent, timeMin: Int): AgendaBlock? {
        if (a.agenda.isEmpty()) return null
        var best: AgendaBlock? = null
        var bestKey = -1
        for ((k, v) in a.agenda) {
            val km = timeStrToMinutes(k)
            if (km <= timeMin && km > bestKey) {
                bestKey = km
                best = v
            }
        }
        return best ?: a.agenda.values.lastOrNull()
    }

    private fun agendaActivity(a: Agent, timeMin: Int): String =
        currentAgendaBlock(a, timeMin)?.activity ?: "Hanging around."

    private fun agendaActivity(state: GameState, a: Agent, timeMin: Int): String =
        effectiveAgendaBlock(state, a, timeMin)?.activity ?: agendaActivity(a, timeMin)

    /** The agenda key ("HH:MM") of the block in force at [timeMin]. */
    private fun agendaKeyAt(a: Agent, timeMin: Int): String? {
        var best: String? = null
        var bestKey = -1
        for (k in a.agenda.keys) {
            val km = timeStrToMinutes(k)
            if (km <= timeMin && km > bestKey) { bestKey = km; best = k }
        }
        return best ?: a.agenda.keys.lastOrNull()
    }

    /**
     * THE schedule lookup (v0.1.5 single source of truth): base agenda
     * (hand-anchored, lore-derived) -> schedule-drift overrides (slow
     * mutation over days) -> active shenanigans stage transform
     * (cluster / homes / invert). Location population everywhere
     * derives from this — GameView renders wherever this puts people.
     */
    fun effectiveAgendaBlock(state: GameState, a: Agent, timeMin: Int): AgendaBlock? {
        var block = currentAgendaBlock(a, timeMin) ?: return null
        val key = agendaKeyAt(a, timeMin)
        if (key != null) {
            val driftLoc = state.scheduleDrift[a.name]?.get(key)
            if (driftLoc != null && driftLoc in LOCATIONS) {
                block = AgendaBlock(driftLoc, block.activity)
            }
        }
        // ---- v0.1.8 living transforms: weekends, seasons, mild
        // weather, and friendship bend the routine before the event
        // transform gets its say. (Night hours included so the
        // night-owl rule can run; the day rules are time-scoped.)
        if (!a.isPlayer) {
            block = livingTransform(state, a, block, key, timeMin)
            // ---- v0.6.0d: a family evening gathers its
            // household at its table during the dinner window
            // (the invited family at home, your guests at your
            // house) — and a finale's gathering below still
            // overrides it: dinners defer to finales, always.
            Dinners.gatheringBlock(state, a.name, timeMin)?.let { block = it }
            // ---- v0.12.0c: a birthday party gathers its
            // household at its own table on the same rails —
            // seated with the dinners because it IS dinner
            // discipline: the party module yields to every
            // claim the dinners yield to, and the claims
            // below still win its people.
            Birthdays.gatheringBlock(state, a.name, timeMin)?.let { block = it }
            // ---- v0.10.0a: on a family-dinner guest night, YOUR
            // family's table claims its one invited cast kid for
            // the window — placed with the dinners (a table is a
            // table), so jobs, screenings, ceremonies, finales
            // and the fair further down still win the kid.
            Family.gatheringBlock(state, a.name, timeMin)?.let { block = it }
            // ---- v0.6.0e: while the new kid holds the shop
            // job, the week's keeper is at their counter for
            // the shift (and the shift's regular is there
            // while the kid is clocked in) — work expects
            // its people. Still yields to the finale below.
            Jobs.gatheringBlock(state, a.name, timeMin)?.let { block = it }
            // ---- v0.15.0h: the mentor's hours — while an
            // apprenticeship offer stands for the new kid,
            // or the active chain has a stage unlocked that
            // waits on the mentor or their site, the mentor
            // keeps their counter through the working day
            // (9-to-5). Seated with the work family, right
            // after Jobs: it beats the base agenda and the
            // dinner claims' daytime silence, and every
            // set piece below (screenings, ceremonies,
            // finales, the fair, the shows) still wins the
            // person — an apprenticeship is an appointment,
            // not an event. The stack grows to twenty-three
            // claims on its strength.
            Apprenticeships.gatheringBlock(state, a.name, timeMin)?.let { block = it }
            // ---- v0.7.0c: movie night gathers its house at the
            // Bijou — the stars, their friends, their hecklers,
            // the regulars — from doors to the feature's end.
            // A dark house (finale, holiday, case) gathers
            // nobody, and the finale below still wins the room.
            MovieNights.gatheringBlock(state, a.name, timeMin)?.let { block = it }
            // ---- v0.8.0b: while a faction's capstone
            // ceremony beat is taken on, the faction gathers
            // at its place inside the ceremony window — the
            // rite is populated like a screening is. The
            // finale below still wins its own people.
            Capstones.gatheringBlock(state, a.name, timeMin)?.let { block = it }
            // ---- v0.5.0h: a season finale gathers its ensemble
            // at its place on the opening and closing days — the
            // set-piece is populated by override, like an event
            // cluster, but it yields to one: the shenanigan
            // transform below still has the final word for its
            // own causers.
            Finales.gatheringBlock(state, a.name, timeMin)?.let { block = it }
            // ---- v0.9.0c: the county fair gathers the town
            // at the fairground during fair hours — placed
            // AFTER the finale (set pieces outrank the
            // fair) and BEFORE the crew; Fair's claim is
            // self-guarded against dinners, jobs,
            // screenings, ceremonies, finales and live case
            // work, and the crew hook below still takes a
            // member to the player first — the fair never
            // holds a crewmate against the crew.
            Fair.gatheringBlock(state, a.name, timeMin)?.let { block = it }
            // ---- v0.11.0e: the winter carnival gathers the
            // town at the frozen pond during carnival hours —
            // seated immediately after the fair, its summer
            // counterpart, and self-guarded the same way
            // (dinners, jobs, screenings, ceremonies,
            // finales, the fair itself, live case work), so
            // the two festival claims read as one family in
            // the stack; the crew hook far below still takes
            // a member to the player first.
            Carnival.gatheringBlock(state, a.name, timeMin)?.let { block = it }
            // ---- v0.9.0e: the rival's crew holds court on
            // its turf on its seeded rhythm — placed after
            // the fair (which wins the room) and before your
            // crew (which still takes its own members
            // first); the claim is self-guarded against
            // every set piece and momentOwned wholesale: a
            // hangout is the lowest-stakes claim in town.
            RivalCrew.gatheringBlock(state, a.name, timeMin)?.let { block = it }
            // ---- v0.10.0b: the patrol claim — roster kids
            // take their posts in costume on patrol nights
            // (Heroes self-guards against momentOwned,
            // busyReason, the fair, family dinner guests and
            // standing crewmates). It seats below the turf
            // claim and above your crew's hook: your crew's
            // word still outranks the patrol's, so a crewmate
            // standing with you patrols WITH you, in costume,
            // at your spot.
            Heroes.gatheringBlock(state, a.name, timeMin)?.let { block = it }
            // ---- v0.10.0c: the school play's claim sits
            // with the set pieces, above the crew hook:
            // rehearsal afternoons seat the cast at the
            // Elementary (Play self-guards to its seeded
            // attendees, school afternoons only, so it can
            // never fight a lesson), and opening night
            // seats the cast, the director and the cast's
            // families — outranking, for its own people,
            // the dinner claims seated earlier, exactly one
            // evening a year.
            Play.gatheringBlock(state, a.name, timeMin)?.let { block = it }
            // ---- v0.13.0b: the school dance's claim — one
            // February evening a year, the gym holds your
            // date, the seeded room and the chaperone. It
            // seats after the play and before sleepovers,
            // with the set pieces: on its one night it
            // outranks the dinner claims and the sleeping
            // bags for its people, and Dance self-guards
            // to yielded years (a yielded year stands no
            // claim at all).
            Dance.gatheringBlock(state, a.name, timeMin)?.let { block = it }
            // ---- v0.13.0c: the talent show's claim — one
            // April Saturday a year, the Bijou's stage
            // holds the bill, the host and the seeded
            // house. It seats after the dance and before
            // sleepovers, with the set pieces: on its one
            // night it outranks the rehearsal room and
            // the sleeping bags for its people, and Talent
            // self-guards to live evenings (a yielded
            // year stands no claim at all, and the
            // theater's own screenings stand down that
            // night through MovieNights' deference).
            Talent.gatheringBlock(state, a.name, timeMin)?.let { block = it }
            // ---- v0.13.0d: the town parade's claim — one
            // May Saturday a year, Main Street holds the
            // whole derived procession (every float's
            // riders, in their entries' own words) and the
            // route crowd, 10:00–13:30. It seats
            // EIGHTEENTH — after the talent show, before
            // sleepovers — with the set pieces: on its one
            // morning it outranks the morning claims for
            // its people, and Parade self-guards to live
            // years (a yielded year stands no claim at
            // all). The street closure itself lives in
            // Walk's pricing, not here: the route geography
            // never moves.
            Parade.gatheringBlock(state, a.name, timeMin)?.let { block = it }
            // ---- v0.14.0b: the field trip's claim — three
            // Fridays a school year, the site holds the
            // whole derived class and its chaperone from
            // first bell (9:00) to the buses back (13:00).
            // It seats NINETEENTH — after the parade, before
            // sleepovers — with the set pieces: on its one
            // morning it outranks the morning claims for
            // its people, and FieldTrip self-guards to live
            // trip days (a yielded day stands no claim at
            // all — School goes quiet instead, and the bus
            // stays in the lot).
            FieldTrip.gatheringBlock(state, a.name, timeMin)?.let { block = it }
            // ---- v0.14.0c: the film festival's claim —
            // five December evenings a year, the Bijou
            // holds the festival film's stars and its
            // drawn house from doors to the feature's
            // end. It seats TWENTIETH — after the field
            // trips, before sleepovers — with the set
            // pieces: Mon–Thu there is no screening
            // claim to ride, and on its nights it
            // outranks the morning claims and the
            // sleeping bags for its people; Festival
            // self-guards to live festival nights (a
            // yielded night still gathers its room —
            // the session's own gate keeps the night
            // honest, the MovieNights split).
            Festival.gatheringBlock(state, a.name, timeMin)?.let { block = it }
            // ---- v0.15.0f: the gallery show's claim —
            // hanging night at the Community Hall (the
            // town's community hall at City Hall) holds
            // the jury, the entrants and the frames' own
            // subjects from six to nine, and the hung
            // show holds them through its visiting
            // afternoons. It seats after the film
            // festival, before sleepovers — the stack
            // grows to twenty-two claims on its strength —
            // with the set pieces: both the festival and
            // the hall own a framed evening built from
            // real entries, and the festival's came first
            // in the town's life, so the hall sits below
            // it and the sleepovers' later, quieter claim
            // loses the hall's people the same way it
            // loses the theater's. Gallery self-guards to
            // a locked wall (hanging night itself excepted,
            // when the lock lands) and stands down for a
            // finale evening, a holiday beat, or a moment
            // the town owns.
            Gallery.gatheringBlock(state, a.name, timeMin)?.let { block = it }
            // ---- v0.11.0c: the sleepover's claim — a
            // planned guest is at the new kid's house for
            // the Friday-evening window (18:00–22:00). It
            // seats after the play and before the stand,
            // and self-guards against every bigger claim
            // (its own veto list) plus the crew's occupied
            // oracle, so the night never double-books a
            // kid the town already owns.
            Sleepovers.gatheringBlock(state, a.name, timeMin)?.let { block = it }
            // ---- v0.14.0d: your birthday party's claim —
            // consulted at the sleepovers' own seat, the
            // way Birthdays' claim rides Dinners' (it takes
            // no seat of its own): a crewmate is at your
            // house for the birthday's evening window,
            // self-guarded on the crew's occupied oracle
            // plus the sleepovers' veto list, so every
            // bigger claim seated above still wins them.
            PlayerBirthday.gatheringBlock(state, a.name, timeMin)?.let { block = it }
            // ---- v0.10.0e: the stand's shift claim — an
            // assigned crewmate works the pitch during the
            // stand's hours. It seats above the crew hook
            // (your crew's word still takes them off the
            // shift) and self-guards on the crew's occupied
            // oracle, so every set piece above and every
            // busyReason claim keeps them first: a shift is
            // work, and work yields to the town's moments.
            Stand.gatheringBlock(state, a.name, timeMin)?.let { block = it }
            // ---- v0.14.0e: the clubhouse's claim — the
            // crew's seasonal spot (treehouse / garage /
            // basement) holds its members 15:00–19:00. It
            // seats after the stand's (the twenty-third
            // call in this stack since v0.15.0h seated the
            // mentor's hours above it, after Jobs; the
            // twenty-second since v0.15.0f seated the
            // gallery show above the sleepovers, and the
            // twenty-first call before that),
            // before the crew's own hook — above only the
            // crew hook, and self-guards on the crew's
            // occupied oracle read whole plus a veto pass
            // over every claim seated above it, priced
            // like a rendezvous off the Walk graph: a
            // hangout yields to everything and teleports
            // nobody.
            Clubhouse.gatheringBlock(state, a.name, timeMin)?.let { block = it }
            // ---- v0.9.0b: the player's crew stands LAST in
            // the stack, below every gathering claim above —
            // any set piece (dinner, shift, screening,
            // ceremony, finale) takes the person first, and
            // the crew hook only ever holds a member who is
            // standing with the new kid or waiting to set
            // out. The shenanigan transform below still has
            // the final word for its own causers, as ever.
            Crew.gatheringBlock(state, a.name, timeMin)?.let { block = it }
        }
        val def = state.shenaniganId?.let { SHENANIGAN_BY_ID[it] }
        if (def != null) {
            val stage = Shenanigans.stageOf(def, timeMin)
            block = when (stage.routine) {
                "cluster" -> AgendaBlock(
                    stage.clusterLoc ?: block.location,
                    stage.clusterActivity.ifEmpty { block.activity })
                "homes" -> AgendaBlock(
                    // v0.15.0g: "home" is CastHomes' word now
                    CastHomes.homeOf(a.name) ?: block.location,
                    "Holed up at home. " + stage.clusterActivity)
                "invert" -> currentAgendaBlock(a, (timeMin + 720) % 1440)
                    ?.let { AgendaBlock(it.location, it.activity) } ?: block
                else -> block
            }
        }
        return block
    }

    private val LEISURE_LOCS = setOf("KidsPark", "Mall", "TweekBros", "StarksPond",
        "MainStreet", "Bijou", "SkeetersBar", "SoDoSoPa", "JimbosGuns")
    private val WORK_LOCS = setOf("Elementary", "Church", "CityHall", "Bank",
        "PostOffice", "TomsRhino", "CityWok", "FreemansTacos", "Raisins", "TweekBros",
        "JimbosGuns", "TegridyFarms", "Bijou", "Mall")

    /** The v0.1.8 living layer of the schedule: weekend shape (#25),
     *  seasonal rewrites (#26), mild-weather give (#27), and Close
     *  Friends bending a leisure block toward the player's haunts (#1).
     *  Deterministic per (npc, day, block) so the Journal, the renderer
     *  and the dialogue brain all read the same routine. */
    private fun livingTransform(state: GameState, a: Agent, block: AgendaBlock,
                                key: String?, timeMin: Int): AgendaBlock {
        var b = block
        val adult = CharacterSystem.isAdult(a.name)
        val seeded = Random(a.name.hashCode().toLong() * 31 +
            state.todayKey.hashCode() + (key?.hashCode() ?: 0))
        // #25 WEEKENDS EXIST
        if (isWeekend(state)) {
            val dow = dayOfWeek(state)
            when {
                b.location == "Elementary" -> {
                    val alt = if (seeded.nextBoolean()) "KidsPark"
                        else (CastHomes.homeOf(a.name) ?: "KidsPark")
                    b = AgendaBlock(alt, "Weekend! No school — freedom tastes like woodchips.")
                }
                adult && b.location in WORK_LOCS && b.location != KID_HOUSE[a.name] -> {
                    b = if (dow == 0 && timeMin in 9 * 60 until 13 * 60 && seeded.nextBoolean())
                        AgendaBlock("Church", "Sunday service. Singing the loud parts only.")
                    else AgendaBlock(
                        listOf("KidsPark", "MainStreet", "SkeetersBar", "Mall", "StarksPond")[seeded.nextInt(5)],
                        "Weekend hours. The town exhales.")
                }
            }
        }
        // #26 SEASONS REWRITE ROUTINES
        when (seasonFor(state.gameMonth)) {
            Season.SUMMER -> if (b.location == "Elementary") {
                b = AgendaBlock(if (seeded.nextBoolean()) "KidsPark" else "StarksPond",
                    "Summer break! School is a rumor.")
            }
            Season.FALL -> if (state.gameMonth in 9..10 && adult &&
                b.location in LEISURE_LOCS && timeMin in 14 * 60 until 18 * 60 &&
                seeded.nextDouble() < 0.3) {
                b = AgendaBlock("TegridyFarms", "Helping with the harvest. Randy's orders.")
            }
            Season.WINTER -> if ((state.gameMonth == 12 || state.gameMonth <= 2) &&
                b.location in setOf("KidsPark", "StarksPond", "BusStop") &&
                seeded.nextDouble() < 0.5) {
                val fb = INDOOR_FALLBACK[b.location] ?: "TweekBros"
                b = AgendaBlock(fb, "Deep winter. Too cold to loiter outside — thawing out.")
            }
            Season.SPRING -> Unit
        }
        // #27 MILD WEATHER MATTERS TOO
        when (state.weatherId) {
            "sunny" -> if (b.location in setOf("TweekBros", "Mall", "Bijou") &&
                seeded.nextDouble() < 0.25) {
                b = AgendaBlock("KidsPark", "Too nice to stay inside. Vitamin D run.")
            }
            "cloudy", "fog" -> if (b.location in OUTDOOR && seeded.nextDouble() < 0.3) {
                val fb = INDOOR_FALLBACK[b.location] ?: "TweekBros"
                b = AgendaBlock(fb, "Gray day. Drifting indoors early.")
            }
        }
        // #55 BIRTHDAY BUZZ: friends bend their free time toward
        // the birthday kid's current spot all day.
        // v0.15.0i: on a shared day the buzz considers EACH
        // birthday person — the first whose circle this agent
        // belongs to takes the bend (one candidate, one roll),
        // exactly as the single day always resolved.
        for (bday in Birthdays.birthdaysToday(state)) {
            if (bday == a.name) continue
            if (b.location !in LEISURE_LOCS) break
            val related = a.name in relationsOf(bday) ||
                (FACTION_OF_CAST[a.name] != null &&
                    FACTION_OF_CAST[a.name] == FACTION_OF_CAST[bday])
            if (!related) continue
            if (seeded.nextDouble() < 0.4) {
                val bdayAgent = state.agents[bday]
                val bdayLoc = bdayAgent?.let {
                    currentAgendaBlock(it, timeMin)?.location }
                if (bdayLoc != null && bdayLoc != b.location) {
                    b = AgendaBlock(bdayLoc,
                        "Birthday detail — orbiting $bday today.")
                }
            }
            break
        }
        // #1 SCHEDULES BEND TOWARD YOU
        val pts = state.friendship[a.name] ?: 0
        if (pts >= 60 && b.location in LEISURE_LOCS) {
            val haunt = state.locVisits.entries
                .filter { it.value >= 3 && it.key != b.location && it.key != KID_HOUSE[a.name] }
                .maxByOrNull { it.value }?.key
            if (haunt != null && seeded.nextDouble() < 0.35) {
                b = AgendaBlock(haunt, "Hanging around — half hoping to run into a certain someone.")
            }
        }
        // v0.1.9e HOLIDAY TRADITIONS: during the tradition's
        // hours, leisure bends toward the gathering place — the
        // town actually GOES to its holidays.
        Traditions.bendLoc(state, a.name, b.location, key, LEISURE_LOCS)?.let { tLoc ->
            val t = Traditions.of(state)
            if (t != null) b = AgendaBlock(tLoc, t.activity)
        }
        // v0.1.9e NPC GOALS: a leisure block may bend toward the
        // goal's place, wearing the mission as its activity —
        // goals you can SEE people pursuing.
        if (b.location in LEISURE_LOCS || b.location == KID_HOUSE[a.name]) {
            NpcGoals.deviationLoc(state, a.name, b.location, key,
                LEISURE_LOCS + setOfNotNull(KID_HOUSE[a.name]))?.let { gLoc ->
                val g = NpcGoals.record(state, a.name)
                b = AgendaBlock(gLoc, NpcGoals.deviationActivity(g))
            }
        }
        // #42 NIGHT OWLS: after 10 PM the owls leave for their
        // spots until 2 AM — the streets are different, not empty.
        // (Keyed on the day's LAST block: several owls' nominal
        // "home" in KID_HOUSE is a workplace fallback.)
        val owlSpot = OWL_SPOTS[a.name]
        val lastKey = a.agendaBase.keys.maxByOrNull { timeStrToMinutes(it) }
        if (owlSpot != null && (timeMin >= 22 * 60 || timeMin < 2 * 60) &&
            key != null && key == lastKey) {
            b = AgendaBlock(owlSpot, "Out late. The night is theirs.")
        }
        return b
    }

    /** Next scheduled stop after the current block (Journal "Later:"). */
    fun nextAgendaStop(state: GameState, a: Agent, timeMin: Int): String? {
        val keys = a.agenda.keys.map { it to timeStrToMinutes(it) }.sortedBy { it.second }
        val next = keys.firstOrNull { it.second > timeMin } ?: return null
        val locId = state.scheduleDrift[a.name]?.get(next.first)
            ?: a.agenda[next.first]?.location ?: return null
        return "${LOCATIONS[locId]?.name ?: locId} at ${minutesToTimeStr(next.second)}"
    }

    // ---------------- Ambient tick (every ~30s real time, + on player actions) ----------------
    fun ambientTick(state: GameState, clock: Clock) {
        syncToDeviceTime(state, clock)
        tickWeather(state, seasonFor(clock.month()))
        tickAnimals(state)
        updatePositions(state)
        checkInteractions(state)
        tickLivingWorld(state)
    }

    /** In-game 15-minute tick. Order: advanceTime, tickAnimals, updatePositions, checkInteractions. */
    fun step(state: GameState) {
        advanceTime(state)
        tickAnimals(state)
        updatePositions(state)
        checkInteractions(state)
        tickLivingWorld(state)
    }

    // ---------------- Pathfinding (via Main Street, like the games) ----------------
    fun calculateWaypoints(startLoc: String, endLoc: String): List<Pair<Int, Int>> {
        if (startLoc == endLoc) return emptyList()
        val sc = LOCATIONS[startLoc]!!.let { it.gx to it.gy }
        val ec = LOCATIONS[endLoc]!!.let { it.gx to it.gy }
        if (sc.first == ec.first || sc.second == ec.second) return listOf(ec)
        val hub = LOCATIONS[HUB]!!.let { it.gx to it.gy }
        return listOf(hub, ec)
    }

    // ---------------- Weather ----------------
    fun tickWeather(state: GameState, season: Season) {
        // Shenanigans stages can lock the weather (data-driven).
        val lock = state.shenaniganId?.let { SHENANIGAN_BY_ID[it] }
            ?.let { Shenanigans.stageOf(it, state.timeMin).weatherLock }
        if (lock != null) {
            if (state.weatherId != lock) setWeather(state, lock, announce = true)
            return
        }
        if (random.nextDouble() > WEATHER_SHIFT_CHANCE) return
        val cur = state.weatherId
        val mult = SEASON_WEATHER_MULT[season] ?: emptyMap()
        val opts = WEATHER_DRIFT[cur] ?: return
        var total = 0.0
        val cum = opts.map { (id, w) ->
            total += w * (mult[id] ?: 1.0)
            id to total
        }
        val r = random.nextDouble() * total
        val nxt = cum.firstOrNull { r < it.second }?.first ?: cur
        if (nxt == cur) return
        setWeather(state, nxt, announce = true)
    }

    fun setWeather(state: GameState, newWeather: String, announce: Boolean = false) {
        val old = state.weatherId
        state.weatherId = newWeather
        // v0.2.0g: the beds follow the sky the moment it turns
        if (newWeather != old) onWeatherShift?.invoke()
        val w = WEATHER[newWeather] ?: return
        if (announce) pushEvent(state, "${w.icon} WEATHER: ${w.label} — ${w.desc}")
        // #4 FACTION MOOD IS MECHANICAL: mood is READ as stored event
        // swing (FactionState.moodScore) + live weather pressure via
        // Factions.moodIndex — a weather turn therefore re-moods the
        // whole town at once, and greetings/ambient/drift downstream
        // all feel the same sky. (No write needed here.)
        if (newWeather in SEVERE_WEATHER && old !in SEVERE_WEATHER) {
            for ((_, a) in state.agents) {
                if (a.isPlayer) continue
                var moved = false
                for ((key, block) in a.agenda) {
                    if (block.location in OUTDOOR) {
                        val fb = INDOOR_FALLBACK[block.location] ?: "TweekBros"
                        a.agenda[key] = AgendaBlock(fb,
                            "Waiting out the ${WEATHER[newWeather]!!.label.lowercase()} at ${LOCATIONS[fb]!!.name}.")
                        moved = true
                    }
                }
                if (moved && a.destination in OUTDOOR) {
                    val fb = INDOOR_FALLBACK[a.destination] ?: "TweekBros"
                    a.destination = fb
                    a.waypoints.clear()
                    a.waypoints.addAll(calculateWaypoints(a.location, fb))
                    a.activity = "Hurrying to ${LOCATIONS[fb]!!.name} to escape the ${WEATHER[newWeather]!!.label.lowercase()}."
                }
            }
            pushEvent(state, "Everyone with half a brain is heading inside. (So, not Cartman. He'll complain instead.)")
        }
        // v0.1.9a #19 RAIN DASH: the moment rain STARTS, everyone
        // caught outside hurries to the nearest door — for real
        // this time: a marked trip at dash pace (paceFor), arms
        // over heads (GameView's "rain" pose), and the block
        // dispatcher leaves them sheltered until the rain lets up.
        if (newWeather == "rain" && old != "rain") {
            var dashed = 0
            for ((name, a) in state.agents) {
                if (a.isPlayer || a.asleep || a.waypoints.isNotEmpty()) continue
                if (a.location !in OUTDOOR) continue
                val shelter = INDOOR_FALLBACK[a.location] ?: nearestBuilding(a.location)
                if (shelter == a.location) continue
                state.lifeText["dash:$name"] = shelter
                a.destination = shelter
                a.waypoints.clear()
                a.waypoints.addAll(calculateWaypoints(a.location, shelter))
                a.activity = "Dashing to ${LOCATIONS[shelter]!!.name} — rain came out of nowhere!"
                dashed++
            }
            if (dashed > 0) {
                pushEvent(state, "🌧️ Rain! $dashed people just made a dash for the " +
                    "nearest door, arms over their heads.")
            }
        }
        if (old == "rain" && newWeather != "rain") {
            // rain's over: the dashes are done, schedules resume
            val marks = state.lifeText.keys.filter { it.startsWith("dash:") }
            for (m in marks) state.lifeText.remove(m)
        }
        if (old in SEVERE_WEATHER && newWeather !in SEVERE_WEATHER) {
            for ((_, a) in state.agents) {
                if (a.isPlayer) continue
                if (a.agendaBase.isNotEmpty()) {
                    a.agenda.clear()
                    a.agenda.putAll(a.agendaBase)
                }
            }
            pushEvent(state, "The sky gave up. Back to regularly scheduled programming.")
            // Storm debris: the storm leaves searchable junk behind.
            repeat(3) {
                val loc = OUTDOOR.toList()[random.nextInt(OUTDOOR.size)]
                dropLitter(state, loc, false, debrisItem(random), "the storm")
            }
        }
    }

    /** Add a piece of ground litter (bounded; oldest cleaned up past the cap). */
    fun dropLitter(state: GameState, locId: String, inside: Boolean, itemId: String, by: String) {
        // v0.17.0b: no litter drops indoors. A drop stores no
        // position, so indoors it rendered at a hash-random
        // floor spot — an item icon floating at floor level
        // (the sock in Lucky's school screenshots). Interiors
        // stay clean; outdoor litter is untouched.
        if (inside) return
        // #40 VISITED PLACES REMEMBERED: spots the player keeps clean
        // resist new mess — litter respawns slower where you tidied.
        if ((state.life["clean:$locId"] ?: 0) >= 3 && random.nextDouble() < 0.55) return
        if (state.litter.size >= MAX_LITTER) state.litter.removeAt(0)
        state.litter.add(LitterDrop(
            id = "lit${state.todayKey}_${random.nextInt(1_000_000)}",
            locId = locId, inside = inside, itemId = itemId,
            dayKey = state.todayKey, droppedBy = by))
    }

    fun weatherDesc(state: GameState): String {
        val w = WEATHER[state.weatherId] ?: return state.weatherId
        return "${w.icon} ${w.label}: ${w.desc}"
    }

    // ---------------- Animals ----------------
    private fun pickSpecies(): String {
        val pool = ANIMAL_SPECIES.keys.toList()
        val weights = pool.map { if (ANIMAL_SPECIES[it]!!.rare) 1.0 else 10.0 }
        val total = weights.sum()
        var r = random.nextDouble() * total
        for (i in pool.indices) {
            r -= weights[i]
            if (r <= 0) return pool[i]
        }
        return pool.last()
    }

    private fun newAnimal(species: String? = null): Animal {
        val sp = species ?: pickSpecies()
        val loc = ANIMAL_OUTDOORS[random.nextInt(ANIMAL_OUTDOORS.size)]
        return Animal("a${1000 + random.nextInt(9000)}", sp, loc)
    }

    private fun spawnInitialAnimals(): List<Animal> {
        val locs = ANIMAL_OUTDOORS.shuffled(random).take(min(4, ANIMAL_OUTDOORS.size))
        return locs.map { loc ->
            val a = newAnimal()
            a.locationId = loc
            a
        }
    }

    fun tickAnimals(state: GameState) {
        val holiday = activeHoliday
        for (a in state.animals) {
            if (random.nextDouble() < 0.30) {
                a.locationId = ANIMAL_OUTDOORS[random.nextInt(ANIMAL_OUTDOORS.size)]
            }
        }
        val cap = if (holiday?.flags?.contains("critters") == true) MAX_ANIMALS + 4 else MAX_ANIMALS
        if (state.animals.size < cap && random.nextDouble() < 0.10) {
            // Christmas: woodland critters everywhere.
            val sp = if (holiday?.flags?.contains("critters") == true && random.nextDouble() < 0.5)
                "christmas critter" else null
            state.animals.add(newAnimal(sp))
        }
        if (state.animals.size > 3 && random.nextDouble() < 0.05) {
            val gone = state.animals.removeAt(random.nextInt(state.animals.size))
            val act = ANIMAL_SPECIES[gone.species]?.actions?.let {
                it[random.nextInt(it.size)]
            } ?: "wandered off"
            pushEvent(state, "A ${gone.species} $act at ${LOCATIONS[gone.locationId]!!.name}, then wandered off.")
        }
    }

    fun animalsAt(state: GameState, locationId: String): List<Animal> =
        state.animals.filter { it.locationId == locationId }

    fun animalFlavor(state: GameState, locationId: String): String {
        val here = animalsAt(state, locationId)
        if (here.isEmpty()) return ""
        val a = here[random.nextInt(here.size)]
        val acts = ANIMAL_SPECIES[a.species]?.actions ?: return ""
        return "Nearby, a ${a.species} is ${acts[random.nextInt(acts.size)]}."
    }

    // ---------------- Engine nodes ----------------
    fun advanceTime(state: GameState) {
        val newTime = (state.timeMin + 15) % 1440
        val rolledOver = newTime < state.timeMin
        state.timeMin = newTime
        if (rolledOver) onNewDay(state)
        // #31: on dry days the puddles shrink a notch every hour
        if (newTime % 60 == 0 && state.weatherId !in setOf("rain", "storm")) {
            shrinkPuddlesHourly(state)
        }
        tickWeather(state, seasonFor(state.gameMonth))
        // Shenanigans stage progression (the little linear story unfolds).
        state.shenaniganId?.let { id ->
            SHENANIGAN_BY_ID[id]?.let { def ->
                val st = Shenanigans.stageFor(state.timeMin)
                if (st > state.lastStageSeen) {
                    state.lastStageSeen = st
                    pushEvent(state, def.stages[st].banner)
                    applyStageEffects(state, def, st)
                    // v0.1.9c: queue the act-break sting — the
                    // Activity presents it at the next safe moment.
                    Episodes.noteStage(state, def, st)
                    // v0.7.0e: when the climax stage opens, the
                    // ticker says where the playable climax is.
                    Climaxes.noteStage(state, def, st)
                    // v0.2.0b: the stage-shift cue sounds NOW (the
                    // card's own sting waits for its safe moment).
                    onStageShift?.invoke(st)
                }
            }
        }
        for ((name, a) in state.agents) {
            if (a.isPlayer || a.asleep) continue
            val block = effectiveAgendaBlock(state, a, newTime) ?: continue
            // v0.1.9a #19: a rain dash in progress is NOT overridden
            // by the block dispatcher — the dasher shelters at the
            // door they ran to until the rain lets up.
            if (state.weatherId == "rain" &&
                state.lifeText["dash:${a.name}"] == a.destination) {
                if (a.waypoints.isEmpty()) {
                    a.activity = "Sheltering from the rain at ${LOCATIONS[a.destination]!!.name}."
                }
                continue
            }
            if (a.destination != block.location) {
                a.destination = block.location
                a.activity = "Walking to ${LOCATIONS[block.location]!!.name}."
                a.waypoints.clear()
                a.waypoints.addAll(calculateWaypoints(a.location, block.location))
                pushEvent(state, "🚶 $name took off toward ${LOCATIONS[block.location]!!.name}.")
            } else if (a.waypoints.isEmpty()) {
                a.activity = block.activity
            }
            // v0.1.9e GOAL PROGRESS: standing at the goal place
            // works the goal a tick at a time; ending up in the
            // same place as the goal person settles people goals.
            val goal = NpcGoals.record(state, name)
            if (!goal.done && a.waypoints.isEmpty()) {
                if (goal.locId != null && a.location == goal.locId) {
                    NpcGoals.progressTick(state, name)
                } else if (goal.personName != null) {
                    val otherP = state.agents[goal.personName]
                    if (otherP != null && otherP.location == a.location &&
                        otherP.waypoints.isEmpty()) {
                        NpcGoals.contactTick(state, name, goal.personName)
                    }
                }
            }
        }
        progressGeneratedGoals(state)
        // v0.8.0c: an emergency's clock is honest — it is
        // checked on the same tick that moves everything
        // else, so walking, talking and waiting all spend
        // it, and when it runs out it settles where you
        // stand. (Deadlines never cross midnight: offers
        // only price routes that finish before quiet hours.)
        Emergencies.checkExpiry(state)
        // v0.9.0b: the crew's rendezvous machine runs on
        // the same tick — calls go out when the player
        // arrives somewhere, journeys land on the Walk
        // graph's own minutes, and a set piece taking a
        // member mid-outing is noticed on this tick, never
        // a tick late and never silently.
        Crew.tick(state)
        // v0.10.0b: the patrol's tick — the night's start is
        // announced once, and standing a patrol spot with
        // the town's heroes stamps the night as one you
        // were OUT (the rollover counts it).
        Heroes.tick(state)
        // v0.10.0e: the stand's tick — if a seller is
        // standing at the pitch (you, present, or your
        // assigned crewmate, arrived and unclaimed), the
        // townsfolk physically there get their honest
        // chance to buy. Unstaffed, it sells nothing.
        Stand.tick(state)
        // v0.11.0c: the sleepover's tick — presence is
        // evidence: a planned guest standing at your house
        // during the window is stamped as here, so the
        // morning's record names who actually came.
        Sleepovers.tick(state)
    }

    /** v0.1.9e: generated folks work their goals by presence too —
     *  a resident standing at their goal place (their schedule
     *  and the goal bend in residentsNow put them there) ticks it. */
    private fun progressGeneratedGoals(state: GameState) {
        val weekJ = (state.gameDay + (state.gameMonth - 1) * 31) / 7
        val night = isSleepHour(state.timeMin / 60)
        val present = VisitorSystem.residentsNow(state.timeMin, weekJ, night,
            dayOfWeek(state), state.todayKey) +
            VisitorSystem.visitorsFor(state.todayKey, state.timeMin)
        for (npc in present) {
            val g = NpcGoals.record(state, npc.persona.loreName)
            if (!g.done && g.locId != null && npc.locId == g.locId) {
                NpcGoals.progressTick(state, npc.persona.loreName)
            }
        }
    }

    /**
     * v0.3.0d: THE BED. Sleep at your house until morning
     * (House.WAKE_MIN) — implemented as the natural tick itself,
     * run forward: the same inGameTick the town lives by, so the
     * midnight rollover (onNewDay: calendar, goals, storylines,
     * the mystery, holidays, litter cleanup) happens EXACTLY as
     * it would had the night passed in real time. From before
     * midnight this crosses one rollover; from the small hours
     * it just walks to morning. Already morning = no-op.
     * Returns the ticks slept. Overnight conversations are wiped
     * — you were asleep; you didn't hear them.
     */
    fun sleepUntilMorning(state: GameState): Int {
        if (state.timeMin == House.WAKE_MIN) return 0
        val startAbs = state.absDay
        val needRoll = state.timeMin > House.WAKE_MIN
        var ticks = 0
        while (ticks < 200) {
            inGameTick(state)
            ticks++
            val rolled = state.absDay > startAbs
            if ((rolled || !needRoll) &&
                state.timeMin in House.WAKE_MIN until House.WAKE_MIN + 15) {
                // exact on the in-game clock (always :00/:15/:30/
                // :45); the snap only ever bites if device-time
                // sync left the clock between quarter hours.
                state.timeMin = House.WAKE_MIN
                break
            }
        }
        state.lastConversation = emptyList()
        return ticks
    }

    /** In-game midnight rollover: advance the calendar, reset daily state. */
    fun onNewDay(state: GameState) {
        // v0.3.0e: capture the night that's ENDING before the
        // calendar moves — its sky (SkyState, judged on the
        // ending day's date + weather) becomes tomorrow's
        // record: the buzz the town wakes up with, and the
        // bed report's while-you-slept line.
        val endedAbsDay = state.absDay
        val endedSeason = seasonFor(state.gameMonth)
        val endedWeather = state.weatherId
        var dim = MONTH_LENGTHS[state.gameMonth - 1]
        if (state.gameMonth == 2 && isLeapYear(state.gameYear)) dim = 29
        state.gameDay++
        if (state.gameDay > dim) {
            state.gameDay = 1
            state.gameMonth++
            if (state.gameMonth > 12) {
                state.gameMonth = 1
                state.gameYear++
            }
        }
        state.todayKey = gameTodayKey(state)
        state.lastPlayedDate = state.todayKey
        state.dailyGain.clear()
        state.absDay++
        val md = state.todayKey.substring(4)
        activeHoliday = currentHoliday(md)
        spawnCollectibles(state, md, state.todayKey)
        pushEvent(state, "🌅 A new day in South Park: ${state.gameMonth}/${state.gameDay}.")
        // #31 HOLIDAYS JOIN THE STORY MACHINE: a holiday day gets the
        // same banner / Journal / memory treatment as an event day.
        val newHolidayId = activeHoliday?.id
        if (newHolidayId != state.holidayId) {
            // v0.1.9c: the outgoing holiday's episode wraps first —
            // yesterday's talk counter is still live at this point
            // in the rollover, so bank it before anything clears it.
            if (state.holidayId != null) {
                Episodes.noteHolidayTalks(state)
                Episodes.finishHoliday(state)
            }
            state.holidayId = newHolidayId
            if (activeHoliday != null) {
                pushEvent(state, "🎉 HOLIDAY: ${activeHoliday!!.name} in South Park!")
                townLogLine(state, "🎉 ${activeHoliday!!.name} — ${state.gameMonth}/${state.gameDay}")
                Episodes.beginHoliday(state, activeHoliday!!)
            }
        } else if (newHolidayId != null) {
            // v0.1.9c: the same holiday rolls on — bank the talks
            // and count the day on its episode.
            Episodes.noteHolidayTalks(state)
            Episodes.noteHolidayDay(state)
        }
        // #10 FRIENDSHIPS NEED UPKEEP + faction mood swings settle.
        upkeepFriendships(state)
        for ((_, fs) in state.factionStates) {
            fs.moodScore += when {
                fs.moodScore > 0 -> -1
                fs.moodScore < 0 -> 1
                else -> 0
            }
        }
        // ---- v0.1.5 daily systems ----
        val monthKey = state.todayKey.substring(0, 6)
        if (state.eventMonthKey != monthKey) {
            state.eventMonthKey = monthKey
            state.eventsThisMonth = 0
        }
        state.questCounters.clear()
        state.litter.removeAll { it.dayKey != state.todayKey } // overnight street cleaning
        // v0.2.0g: a fixed streetlamp only stays fixed for the
        // night it was fixed ("lampFixed:<absDay>" — GameView
        // reads today's key only), so yesterday's key is dead
        // weight. Prune the whole family at rollover instead of
        // letting one key per lamp-night accumulate in the save.
        state.lifeText.keys.removeAll { it.startsWith("lampFixed:") }
        Factions.driftTowardBaseline(state.factionStates)
        rollShenanigan(state)
        rollScheduleDrift(state)
        // v0.1.9e: goal cycles roll over — yesterday's unfinished
        // errands expire (a couple get a quiet obituary in the
        // ticker) and stale generated-NPC records are pruned.
        NpcGoals.dailyRollover(state)
        // v0.4.0b: errands turn over too — finished ones leave
        // the plate (history is the count + recent list) and
        // yesterday's unaccepted offers lapse.
        Errands.dailyRollover(state)
        // v0.6.0a: commissions turn over with the errands —
        // landed prints leave the plate, stale asks lapse
        // free, and a crossed week boundary folds the
        // outgoing postings into the no-repeat memory.
        Commissions.dailyRollover(state)
        // v0.3.0c: the relationship storylines turn a page at
        // midnight too — stages live out their length and arcs
        // resolve (or open) whether or not the player showed up.
        StoryArcs.dailyTick(state)
        // v0.6.0d: family evenings wrap at midnight (the
        // friendship, the records, the thank-you on the
        // table) and today's invitation is anchored — BEFORE
        // the phone harvests, so the host's invite and
        // morning-after thanks texts derive from the records
        // this rollover just wrote.
        Dinners.dailyRollover(state)
        // v0.12.0c: last night's birthday party wraps beside
        // the dinners it rode — the table is paid off
        // yesterday's beat record and the night goes on the
        // 🎂 page, before the phone harvests.
        Birthdays.dailyRollover(state)
        // v0.10.0a: your own family's table wraps beside the
        // other family evenings — yesterday's family dinner
        // settles, a stale chore lapses, and the weekly
        // allowance lands before the phone harvests, so Mom's
        // morning texts derive from these records.
        Family.dailyRollover(state)
        // v0.11.0c: last night's sleepover wraps beside the
        // family — the friendship is paid once per guest
        // who actually came, the topic is planted, the
        // night goes on the record, and the sleeping bags
        // are struck before the phone harvests.
        Sleepovers.dailyRollover(state)
        // v0.14.0d: your own birthday settles beside the
        // home systems — yesterday's party (if it stood and
        // beats landed) pays its guests and goes on the
        // record, and when today IS a birthday the gift,
        // the card and the yield stamp land before the
        // phone harvests, so the morning's text derives
        // from them.
        PlayerBirthday.dailyRollover(state)
        // v0.6.0e: town jobs settle at midnight too —
        // yesterday's shift record (partial shifts pay their
        // tasks), payday when the week turns, and the
        // missed-shift ladder, excused by the town's bigger
        // claims. Runs after Dinners (its wrap records are
        // the dinner excuse's evidence) and before the
        // phone, like every morning fact.
        Jobs.dailyRollover(state)
        // v0.6.0f: town votes count at the week's turn —
        // the outgoing week's ballots are tallied, the winner
        // takes its place in town, and the result is a morning
        // fact before the phone harvests (McDaniels texts it).
        Votes.dailyRollover(state)
        // v0.7.0c: movie nights settle at midnight — a
        // session left mid-feature becomes the night's
        // record at the buzz it reached, and yesterday's
        // session keys are pruned. Runs before the phone
        // so tonight's screening text is a morning fact.
        MovieNights.dailyRollover(state)
        // v0.7.0d: school settles at midnight too — a bee
        // left mid-round settles at the round it reached,
        // an open lesson evaporates (no credit without the
        // participation moment), and yesterday's school day
        // keys are pruned. Before the phone, like the Bijou,
        // so the morning report can announce today's class.
        School.dailyRollover(state)
        // v0.8.0d: the yearbook settles at the school-year
        // turn — on the first morning of summer the book for
        // the year that ended yesterday is judged from the
        // album, shelved, announced on the ticker (so the
        // morning report carries it), and argued about.
        // Exactly once per year: the stored volume is the
        // guard (Yearbook owns the rules).
        Yearbook.dailyRollover(state)
        // v0.8.0e: the rivalry turns its page at midnight —
        // the pick is recorded, a live race and stale arms
        // lapse free, the follower race accrues and settles,
        // a counter-prank lands, a due prank plants, and a
        // fresh challenge day announces itself. Before the
        // phone, like every morning fact, so last night's
        // result can be texted by breakfast.
        Rival.dailyRollover(state)
        // v0.9.0e: the rival's crew turns its page too —
        // the crew forms on the first midnight after the
        // crowning, a finished season closes into its one
        // history line, and stale crew arms lapse free.
        RivalCrew.dailyRollover(state)
        // v0.9.0b: the crew disperses overnight — sleep
        // does that to a crew. Membership, since-days and
        // tallies persist; every standing arrangement and
        // call stamp clears, so the new day asks fresh.
        Crew.dailyRollover(state)
        // v0.14.0e: yesterday's clubhouse afternoon wraps
        // beside the crew's own dispersal — a hangout with
        // beats and a room goes on the record and plants
        // its topic; it pays nothing of its own (Crew's
        // arrival rate is the day's pay, never twice).
        Clubhouse.dailyRollover(state)
        // v0.10.0b: hero nights' bookkeeping — a patrol
        // night you were out on counts once, becomes the
        // morning-after fact the phone reads, and the
        // caper's first chapter opens under a made hero.
        Heroes.dailyRollover(state)
        // v0.9.0c: the fair's mornings — the announcement,
        // the opening, the contest judgings, the wrap —
        // before the phone, so the Mayor's fair text can
        // land the same morning the poster goes up.
        Fair.dailyRollover(state)
        // v0.11.0e: the winter carnival's mornings — the
        // announcement, the opening, the judgings, the
        // wrap — beside the fair's, before the phone, so
        // the Mayor's carnival text can land the same
        // morning the poster goes up.
        Carnival.dailyRollover(state)
        // v0.10.0c: the school play's mornings — the
        // poster's ticker on announcement morning, casting
        // for a player who never auditioned, the offstage
        // wrap once the grace evenings pass, and the
        // morning-after review — before the phone, so the
        // director's announcement text can land the same
        // morning the poster goes up.
        Play.dailyRollover(state)
        // v0.13.0b: the school dance's mornings — the
        // poster's ticker on announcement morning and the
        // night's wrap on the morning after — beside the
        // play's, before the paper and the phone, so the
        // edition line and the morning-after text derive
        // from the wrapped record.
        Dance.dailyRollover(state)
        // v0.13.0c: the talent show's mornings — the
        // poster's ticker on announcement morning and
        // the night's wrap on the morning after —
        // beside the dance's, before the paper and the
        // phone, so the edition line and the morning-
        // after text derive from the wrapped record.
        Talent.dailyRollover(state)
        // v0.13.0d: the parade's mornings — the poster's
        // ticker a week out and the morning-after wrap —
        // beside the talent show's, before the paper and
        // the phone, so the edition line and the
        // morning-after text derive from the wrapped
        // record.
        Parade.dailyRollover(state)
        // v0.13.0e: the pen pal's mornings — the program's
        // October note, the June settlement, the post-visit
        // thank-you, and letter arrivals on their cadence —
        // beside the parade's, before the paper and the
        // phone: the paper never covers the letters, but
        // the ticker carries the arrival like any mail.
        PenPals.dailyRollover(state)
        // v0.14.0b: the field trips' mornings — the
        // poster's ticker on announcement morning and the
        // wrap on the morning after — beside the parade's
        // and the pen pals', before the paper and the
        // phone, so the edition line and the morning-
        // after text derive from the wrapped record.
        FieldTrip.dailyRollover(state)
        // v0.14.0c: the film festival's mornings — the
        // poster's ticker on announcement morning, each
        // past festival night settled (attended at its
        // buzz, yielded when a case owned it), and the
        // year's wrap on the first morning after
        // closing night — beside the field trips',
        // before the paper and the phone, so the
        // edition line and the morning-after text
        // derive from the wrapped record.
        Festival.dailyRollover(state)
        // v0.15.0f (Gallery): the show's own mornings —
        // the record opens on the season's first day, the
        // wall locks after hanging night, the wrap lands
        // the morning after the show comes down; after
        // the festival's, before the paper and the phone
        // (their gallery lines derive from these facts)
        Gallery.dailyRollover(state)
        // v0.10.0e: the stand's wrap — yesterday's
        // perishables spoil INTO the books (never silently),
        // a staffed day counts and pays its friendship, the
        // rival crew's review comes due off the real books,
        // and the stand closes with the night. Before the
        // phone, like every morning fact.
        Stand.dailyRollover(state)
        // v0.11.0b: the garden lives the ended day too —
        // each bed grows, stalls, scorches or dies by the
        // weather the ended day actually had (weatherId is
        // still the ended day's here), so the beds and the
        // paper's sky log can never disagree about a night.
        Garden.dailyRollover(state)
        // v0.11.0a: the town paper turns its page too — the
        // ended day's sky joins the paper's own weather log
        // (weatherId is still the ended day's here; the new
        // day's sky drifts later, in tickWeather), and at a
        // week's turn the edition for the week just lived is
        // composed from the record, frozen, and announced
        // on the ticker. Before the phone, like every
        // morning fact.
        Newspaper.dailyRollover(state)
        // v0.8.0f: the opinion case's morning beat — if the
        // town's mind crossed a band overnight (a telling
        // witnessed, a fact proven, a stance taken), the
        // ticker says so once, in the story's own language.
        // The meter itself is never stored; only the last
        // REPORTED band is remembered, here.
        Mystery6.noteNewDay6(state)
        // v0.5.0e: the phone's morning harvest — yesterday's
        // noted facts and today's derived triggers become
        // texts (deterministic per fact; Phone owns the rules).
        // Runs after the arcs tick so a stage that landed at
        // midnight is textable by breakfast.
        Phone.dailyRollover(state)
        // v0.5.0h: the season finales turn their page at
        // midnight too — last week's finale wraps (the call,
        // the mark, the credits), and if the calendar just
        // entered a season's last week, this year's finale
        // opens — unless a case's climax owns the morning,
        // in which case it opens tomorrow instead.
        Finales.dailyRollover(state)

        // ---- v0.1.8 Phase 2: the living world's daily bookkeeping ----
        updatePuddlesDaily(state)
        // v0.3.0e: 🌌 LAST NIGHT'S SKY — recorded against the
        // NEW day (absDay has already advanced): today the
        // town buzzes about it (SkyState.buzzKind), and if
        // the player slept through it, the morning report
        // says so. A plain or cloudy night clears the record.
        val skyKind = SkyState.dateSpecial(endedAbsDay, endedWeather, endedSeason)
        if (skyKind != SkyState.NONE) {
            state.lifeText[SkyState.LAST_SKY_KEY] = skyKind
            state.life[SkyState.LAST_SKY_DAY_KEY] = state.absDay
        } else {
            state.lifeText.remove(SkyState.LAST_SKY_KEY)
            state.life.remove(SkyState.LAST_SKY_DAY_KEY)
        }
        val seasonNow = seasonFor(state.gameMonth)
        // #35 mud season: spring + a snowy yesterday = muddy paths
        if (seasonNow == Season.SPRING &&
            state.weatherId in setOf("light_snow", "heavy_snow", "blizzard")) {
            state.life["mudDay"] = state.absDay
        }
        // #38 freeze-thaw memory for tomorrow's icicles
        if (state.weatherId in setOf("light_snow", "heavy_snow")) {
            state.life["snowYesterday"] = state.absDay
        }
        // #36 rain barrels: storms overflow, rain fills, dry drains
        state.life["barrel"] = when (state.weatherId) {
            "storm" -> 3
            "rain" -> ((state.life["barrel"] ?: 1) + 1).coerceAtMost(3)
            "sunny", "partly_cloudy" -> ((state.life["barrel"] ?: 1) - 1).coerceAtLeast(0)
            else -> state.life["barrel"] ?: 1
        }
        // #39 a windy day leaves a flyer stuck to a fence somewhere
        if (state.weatherId in setOf("windy", "storm")) {
            val spots = listOf("MainStreet", "KidsPark", "BusStop", "StarksPond", "Elementary")
            state.lifeText["flyerLoc"] = spots[state.absDay % spots.size]
        } else {
            state.lifeText.remove("flyerLoc")
        }
        // #73 the season's first snow breaks the truce
        if (seasonNow == Season.WINTER && state.weatherId in
            setOf("light_snow", "heavy_snow", "blizzard") &&
            state.life["firstSnowYear"] != state.gameYear) {
            state.life["firstSnowYear"] = state.gameYear
            state.life["firstSnowDay"] = state.absDay
            pushEvent(state, "❄️ FIRST SNOW! The annual truce is broken — snowball rules are OFF.")
        }
        // #99 the impossible snowman appears after heavy snow...
        if (state.weatherId == "blizzard" || state.weatherId == "heavy_snow") {
            val owner = KID_ORDER[state.absDay % KID_ORDER.size]
            state.lifeText["snowman"] = "$owner|${KID_HOUSE[owner] ?: "MainStreet"}|${state.absDay}"
        }
        // ...and melts two days later
        state.lifeText["snowman"]?.let { sm ->
            val born = sm.split("|").getOrNull(2)?.toIntOrNull() ?: 0
            if (state.absDay - born >= 2) state.lifeText.remove("snowman")
        }
        // #96 meteor nights: something glows by the pond after dark
        if (state.absDay % 9 == 3) {
            state.spawns.add(Spawn("meteor_${state.todayKey}", "shiny_rock", "StarksPond"))
            pushEvent(state, "☄️ Something fell by Stark's Pond last night. Probably.")
        }
        // #57 a rival leaves a prank at your favorite haunt
        maybePlantPrank(state)
        // #95 fence feud lifecycle
        updateFeud(state)
        // #55 birthdays get the banner treatment too —
        // v0.15.0i: every birthday today gets the banner,
        // not just the first name on the record: a shared
        // day banners both.
        for (b in Birthdays.birthdaysToday(state)) {
            pushEvent(state, "🎂 It's $b's birthday today!")
            townLogLine(state, "🎂 $b's birthday — ${state.gameMonth}/${state.gameDay}")
        }
        if (state.holidayId != null) {
            state.life["holidaysSeen"] = (state.life["holidaysSeen"] ?: 0) + 1
        }
    }

    /** #31/#32 PUDDLES THAT LIVE AND DIE: rain leaves them behind,
     *  dry days shrink them, cold snaps freeze what's left over. */
    fun updatePuddlesDaily(state: GameState) {
        when (state.weatherId) {
            "rain", "storm" -> {
                val rand = Random(state.absDay * 131L + 7)
                val spots = OUTDOOR.toList().shuffled(rand).take(3 + rand.nextInt(3))
                for (loc in spots) state.puddles[loc] = 3
            }
            "sunny", "partly_cloudy" -> {
                for (k in state.puddles.keys.toList()) {
                    val nv = (state.puddles[k] ?: 0) - 2
                    if (nv <= 0) state.puddles.remove(k) else state.puddles[k] = nv
                }
            }
        }
        state.frozenPuddles.clear()
        if (seasonFor(state.gameMonth) == Season.WINTER ||
            state.weatherId in setOf("light_snow", "heavy_snow", "blizzard")) {
            state.frozenPuddles.addAll(state.puddles.keys)
        }
        while (state.puddles.size > 8) state.puddles.remove(state.puddles.keys.first())
    }

    /** Hourly shrink on dry days (#31). */
    private fun shrinkPuddlesHourly(state: GameState) {
        for (k in state.puddles.keys.toList()) {
            val nv = (state.puddles[k] ?: 0) - 1
            if (nv <= 0) state.puddles.remove(k) else state.puddles[k] = nv
        }
    }

    /** #57 RIVAL PRANKS: a Rival-tier kid booby-traps your haunt. */
    private fun maybePlantPrank(state: GameState) {
        val rival = state.friendship.entries.firstOrNull {
            Relationships.tierOf(it.value) == Relationships.RIVAL
        }?.key ?: return
        val haunt = state.locVisits.maxByOrNull { it.value }?.key ?: return
        state.lifeText["prank:$haunt"] =
            "$rival left a prank waiting here: a whoopee cushion on the best seat. Classic."
    }

    /** #95 THE FENCE FEUD: erupts some weeks between two neighbor
     *  houses, burns for five days, then burns out (or is mediated). */
    private fun updateFeud(state: GameState) {
        val cur = state.lifeText["feud"]
        if (cur != null) {
            val start = cur.split("|").getOrNull(2)?.toIntOrNull() ?: 0
            if (state.absDay - start >= 5) {
                state.lifeText.remove("feud")
                townLogLine(state, "🧱 The great fence feud burned itself out")
                pushEvent(state, "🧱 The fence feud finally burned out. Peace (and normal fence heights) return.")
            }
            return
        }
        if (state.absDay % 11 == 4) {
            val a = HOUSE_LOCS[state.absDay % HOUSE_LOCS.size]
            val b = HOUSE_LOCS[(state.absDay + 2) % HOUSE_LOCS.size]
            state.lifeText["feud"] = "$a|$b|${state.absDay}"
            pushEvent(state, "🧱 A fence feud erupts between ${LOCATIONS[a]?.name} and ${LOCATIONS[b]?.name}!")
        }
    }

    private fun gameDayOfYear(state: GameState): Int {
        var d = state.gameDay
        for (m in 1 until state.gameMonth) {
            d += MONTH_LENGTHS[m - 1]
            if (m == 2 && isLeapYear(state.gameYear)) d++
        }
        return d
    }

    /** #10 FRIENDSHIPS NEED UPKEEP: a tier slowly slips when you
     *  vanish for in-game weeks (2 points/day past a 14-day grace,
     *  floored at 0). Any recorded visit refreshes lastMetDay, which
     *  is what stops the slide — kept friends stay kept. */
    private fun upkeepFriendships(state: GameState) {
        for ((name, pts) in state.friendship.entries.toList()) {
            if (pts <= 0) continue
            val mem = NpcMemoryStore.get(name) ?: continue
            if (mem.lastMetDay.isEmpty()) continue
            val gap = daysBetween(mem.lastMetDay, state.todayKey)
            if (gap >= 14) {
                val nv = (pts - 2).coerceAtLeast(0)
                if (nv != pts) {
                    state.friendship[name] = nv
                    pushEvent(state, "🥀 You haven't seen $name in $gap days — the friendship is slipping.")
                }
            }
        }
        // generated NPCs keep their points in their memory doc
        for ((key, mem) in NpcMemoryStore.all()) {
            if (key in state.friendship) continue
            if (mem.points <= 0 || mem.lastMetDay.isEmpty()) continue
            val gap = daysBetween(mem.lastMetDay, state.todayKey)
            if (gap >= 14) mem.points = (mem.points - 2).coerceAtLeast(0)
        }
    }

    /** #33 YOUR HAND IN THE OUTCOME: event-time actions at the
     *  cluster (talks, gifts, cleanups) score participation. */
    fun bumpParticipation(state: GameState, locId: String) {
        val def = state.shenaniganId?.let { SHENANIGAN_BY_ID[it] } ?: return
        val stage = Shenanigans.stageOf(def, state.timeMin)
        if (stage.clusterLoc == locId && state.participation < 10) {
            state.participation++
            // v0.2.0f: hands dirty at a chapter's turf can turn
            // up one of the case's participation clues.
            Mystery.noteParticipation(state, def.id)
            // v0.3.0f: the marathon's own discoveries work the
            // same way (the program early, the booth search late).
            Mystery2.noteParticipation2(state, def.id)
            // v0.4.0f: a shift worked at a dig chapter's turf
            // turns up the buried case's participation finds.
            Mystery3.noteParticipation3(state, def.id)
            // v0.6.0c: hands dirty at a gardener chapter's turf
            // turn up its participation finds the same way.
            Mystery4.noteParticipation4(state, def.id)
        // v0.7.0f: the confession case's hands-on evidence.
        Mystery5.noteParticipation5(state, def.id)
        // v0.8.0f: the opinion case's hands-on evidence — the
        // unveiling up close, the shop counted to the brushful.
        Mystery6.noteParticipation6(state, def.id)
        // v0.9.0f: the backwards case's hands-on evidence — the
        // assembly up close, the archive boxes in the ledger week.
        Mystery7.noteParticipation7(state, def.id)
        // v0.10.0f: the alibi case's hands-on evidence — the
        // crowd under the marquee, the setup shift worked.
        Mystery8.noteParticipation8(state, def.id)
        // v0.11.0f: the audit case's hands-on evidence — the
        // seats counted by your own hand, the archive boxes.
        Mystery9.noteParticipation9(state, def.id)
        // v0.12.0f: the photo case's hands-on evidence — the
        // frames handled at the hanging, the prints in hand
        // under the darkroom lamp.
        Mystery10.noteParticipation10(state, def.id)
        // v0.13.0f: the voice case's hands-on evidence — the
        // second copy in the papers, the letter read to its
        // signer.
        Mystery11.noteParticipation11(state, def.id)
        // v0.14.0f: the returns case's hands-on evidence — the
        // unclaimed bin sorted by hand, the fair-copy book
        // among the sort's paper.
        Mystery12.noteParticipation12(state, def.id)
        // v0.15.0j: the exhibition case's hands-on evidence —
        // the crossed pair's prints under the darkroom lamp,
        // the swapped frame handled at the rehang.
        Mystery13.noteParticipation13(state, def.id)
        }
    }

    // ---------------- Shenanigans lifecycle ----------------
    private fun rollShenanigan(state: GameState) {
        state.shenaniganId?.let { old ->
            SHENANIGAN_BY_ID[old]?.let { d ->
                pushEvent(state, "🎪 '${d.title}' is over. South Park returns to normal. Mostly.")
                // v0.2.0f: the mystery's finale resolves its branch
                // here — the outcome line, the credits and the
                // ripples all speak the branch's language, and the
                // arc retires with this wrap, forever.
                val fin = if (d.id == Mystery.FINALE_ID)
                    Mystery.resolveFinale(state) else null
                // v0.3.0f: the booth case resolves its branch at
                // ITS wrap the same way — confession, stuck name,
                // or cold drawer — and retires with it, forever.
                val acc = if (d.id == Mystery2.ACCUSATION_ID)
                    Mystery2.resolveAccusation(state) else null
                // v0.4.0f: the buried case resolves its disposition
                // at ITS wrap the same way — publish, bury, Sharon,
                // or the county's catalogue — and retires with it.
                val dis = if (d.id == Mystery3.FINALE3_ID)
                    Mystery3.resolveDisposition(state) else null
                // v0.6.0c: the gardener case resolves its vigil at
                // ITS wrap the same way — named, guarded, joined,
                // or the cold winter — and retires with it.
                val vig = if (d.id == Mystery4.FINALE4_ID)
                    Mystery4.resolveVigil(state) else null
                // v0.7.0f: the confession case's finale resolves
                // here too — the confrontation's call becomes the
                // landing before the wrap is worded.
                val conf = if (d.id == Mystery5.FINALE5_ID)
                    Mystery5.resolveConfrontation(state) else null
                // v0.8.0f: the opinion case's finale resolves
                // here too — the meeting's call (or its absence)
                // becomes the landing before the wrap is worded.
                val meet = if (d.id == Mystery6.FINALE6_ID)
                    Mystery6.resolveMeeting(state) else null
                // v0.9.0f: the backwards case's finale resolves
                // here too — the homecoming's call (or its
                // absence) becomes the landing before the wrap
                // is worded, gated on the timeline's final state.
                val back = if (d.id == Mystery7.FINALE7_ID)
                    Mystery7.resolveRestage(state) else null
                // v0.10.0f: the alibi case's finale resolves
                // here too — the reading's call (or its
                // absence) becomes the landing before the wrap
                // is worded, gated on the graph's final state.
                val ali = if (d.id == Mystery8.FINALE8_ID)
                    Mystery8.resolveReading(state) else null
                // v0.11.0f: the audit case's finale resolves
                // here too — the reckoning's call (or its
                // absence) becomes the landing before the wrap
                // is worded, gated on the ledger's final state.
                val aud = if (d.id == Mystery9.FINALE9_ID)
                    Mystery9.resolveAudit(state) else null
                // v0.12.0f: the photo case's finale resolves
                // here too — the rehanging's call (or its
                // absence) becomes the landing before the
                // wrap is worded, gated on the board's
                // final state.
                val han = if (d.id == Mystery10.FINALE10_ID)
                    Mystery10.resolveHanging(state) else null
                // v0.13.0f: the voice case's finale resolves
                // here too — the Open Letters' call (or its
                // absence) becomes the landing before the wrap
                // is worded, gated on the ledger's final state.
                val voi = if (d.id == Mystery11.FINALE11_ID)
                    Mystery11.resolveVoicing(state) else null
                // v0.14.0f: the returns case's finale resolves
                // here too — the reading's call (or its absence)
                // becomes the landing before the wrap is worded,
                // gated on the ledger's final state.
                val ret = if (d.id == Mystery12.FINALE12_ID)
                    Mystery12.resolveReturns(state) else null
                // v0.15.0j: the exhibition case's finale resolves
                // here too — the reading's call (or its absence)
                // becomes the landing before the wrap is worded,
                // gated on the audit's final state.
                val exh = if (d.id == Mystery13.FINALE13_ID)
                    Mystery13.resolveVerdict(state) else null
                // v0.7.0e: a shenanigan's playable climax settles
                // here too — a played chain supplies its own
                // outcome line (its friendship pays and memory
                // topics land inside the wrap, before the credits
                // below); an unplayed one settles ABSENT with no
                // outcome, and this wrap stays exactly the legacy
                // wrap. The mystery branches above keep their
                // precedence — their defs are never in CHAINS.
                val climax = if (d.id in Climaxes.CHAINS)
                    Climaxes.wrap(state, d) else null
                // #33/#38: the outcome remembers what YOU did, and the
                // climax turf keeps the mark until the next event.
                val outcome = fin?.outcome ?: acc?.outcome ?: dis?.outcome ?:
                vig?.outcome ?: conf?.outcome ?: meet?.outcome ?:
                back?.outcome ?: ali?.outcome ?: aud?.outcome ?:
                han?.outcome ?: voi?.outcome ?: ret?.outcome ?:
                exh?.outcome ?: climax?.outcome ?: when {
                    state.participation >= 5 -> "You were in the thick of it — the town noticed."
                    state.participation >= 2 -> "You lent a hand where it counted."
                    else -> "You mostly watched it burn from a safe distance."
                }
                pushEvent(state, "🎪 Outcome: $outcome")
                townLogLine(state, "🎪 ${d.title} — $outcome")
                val markLoc = d.stages[2].clusterLoc ?: d.stages[1].clusterLoc
                if (markLoc != null) {
                    state.lifeText["mark:$markLoc"] =
                        "${d.title} ended here. $outcome"
                }
                // v0.1.9c: snapshot the episode facts BEFORE the
                // resets below clear them.
                val epPart = state.participation
                val epWitness = state.eventWitness
                val epMark = if (markLoc != null)
                    "${d.title} ended here. $outcome" else ""
                val epMarkLocName = markLoc?.let { LOCATIONS[it]?.name } ?: ""
                if (state.participation >= 5) {
                    for (fx in d.stages[3].factionEffects) {
                        state.factionStates[fx.factionId]?.let { fs ->
                            fs.playerStanding = (fs.playerStanding + 3).coerceIn(-50, 100)
                        }
                    }
                }
                state.participation = 0
                state.eventWitness = null
                val residueName = eventResidue(state, d)
                // v0.2.0f: the finale's branch ripples land BEFORE
                // the credits are written, so the wrap-up's faction
                // diff counts them too.
                if (fin != null) Mystery.applyFinaleEffects(state, fin.branch)
                if (acc != null) Mystery2.applyAccusationEffects(state, acc.branch)
                if (dis != null) Mystery3.applyDispositionEffects(state, dis.branch)
                if (vig != null) Mystery4.applyVigilEffects(state, vig.branch)
            // v0.7.0f: the confrontation's ripples + marks land
            // with the other finales', before the credits diff.
            if (conf != null) Mystery5.applyConfrontationEffects(state, conf.branch)
            // v0.8.0f: the meeting's ripples + marks land with
            // the other finales', before the credits diff.
            if (meet != null) Mystery6.applyMeetingEffects(state, meet.branch)
            // v0.9.0f: the homecoming's ripples + marks land
            // with the other finales', before the credits diff.
            if (back != null) Mystery7.applyRestageEffects(state, back.branch)
            // v0.10.0f: the reading's ripples + marks land
            // with the other finales', before the credits diff.
            if (ali != null) Mystery8.applyReadingEffects(state, ali.branch)
            // v0.11.0f: the reckoning's ripples + marks land
            // with the other finales', before the credits diff.
            if (aud != null) Mystery9.applyAuditEffects(state, aud.branch)
            // v0.12.0f: the rehanging's ripples + marks land
            // with the other finales', before the credits diff.
            if (han != null) Mystery10.applyHangingEffects(state, han.branch)
            // v0.13.0f: the Open Letters' ripples + marks land
            // with the other finales', before the credits diff.
            if (voi != null) Mystery11.applyVoicingEffects(state, voi.branch)
            // v0.14.0f: the reading's ripples + marks land
            // with the other finales', before the credits diff.
            if (ret != null) Mystery12.applyReturnsEffects(state, ret.branch)
            // v0.15.0j: the reading's ripples + marks land
            // with the other finales', before the credits diff.
            if (exh != null) Mystery13.applyVerdictEffects(state, exh.branch)
                // v0.1.9c: roll credits on the episode.
                val epDone = Episodes.finishEvent(state, d, epPart, outcome,
                    epMark, epMarkLocName, residueName, epWitness,
                    fin?.recapLines ?: acc?.recapLines ?: dis?.recapLines ?:
                    vig?.recapLines ?: conf?.recapLines ?:
                    meet?.recapLines ?: back?.recapLines ?:
                    ali?.recapLines ?: aud?.recapLines ?:
                    han?.recapLines ?: voi?.recapLines ?: ret?.recapLines ?:
                    exh?.recapLines ?: emptyList(),
                    climaxTier = climax?.tier ?: "",
                    climaxHow = climax?.how ?: "")
                // v0.8.0e: the rivalry keeps score at the
                // climax too — if the rival caused this one
                // and your tier was played, theirs is rolled
                // from their nerve and compared (Rival owns
                // the guards; legacy/absent tiers judge nil).
                Rival.onClimaxSettled(state, epDone)
            }
        }
        // v0.3.0f: a cold-closed booth case leaves one last
        // quiet beat — a week on, the reel turns up in the
        // Bijou lost-and-found and nobody ever knows.
        Mystery2.noteReelFound(state)
        // v0.4.0f: a buried case handed to Sharon leaves one
        // last beat too — a day on, her decision lands at
        // City Hall and the town hears about it.
        Mystery3.noteSharonDecision(state)
        // v0.6.0c: a named gardener leaves one last beat too —
        // a morning on, the final fix lands on the player's
        // own porch, privately, the way he gives everything.
        Mystery4.noteLastFix(state)
        // v0.7.0f: a cleared confession case leaves one last
        // beat too — a week on, the lure comes home to the
        // bait-shop step, anonymously. Its own once-only
        // machinery; no-ops otherwise.
        Mystery5.noteLureReturned(state)
        // v0.8.0f: a cleared opinion case leaves one last beat
        // too — a week on, the wall's quiet corner is whole
        // again, painted in the night, unsigned. Its own
        // once-only machinery; no-ops otherwise.
        Mystery6.noteWallRestored(state)
        // v0.9.0f: a beginning told aloud leaves one last beat
        // too — a week on, the trowel's case is mended at last
        // and the trowel goes home under new glass. Its own
        // once-only machinery; no-ops otherwise.
        Mystery7.noteCaseRebuilt(state)
        // v0.10.0f: a graph read aloud leaves one last beat
        // too — a week on, the marquee tray is full again,
        // the missing letter back in its slot, unwitnessed.
        // Its own once-only machinery; no-ops otherwise.
        Mystery8.noteLetterReturned(state)
        // v0.11.0f: an audit read aloud leaves one last beat
        // too — a week on, the mayors' roll is reprinted
        // whole, the fourteenth name back in its line.
        // Its own once-only machinery; no-ops otherwise.
        Mystery9.noteListRestored(state)
        // v0.12.0f: a wall read aloud leaves one last beat
        // too — a week on, the portrait's true card is
        // printed whole, and the frame it was completed
        // from hangs beside it, loaned openly. Its own
        // once-only machinery; no-ops otherwise.
        Mystery10.noteLabelReprinted(state)
        // v0.13.0f: a ledger read aloud leaves one last beat
        // too — a week on, the correction cards go up where
        // the borrowed letters were found, one line each in
        // the signers' own voices. Its own once-only
        // machinery; no-ops otherwise.
        Mystery11.noteNamesPosted(state)
        // v0.14.0f: a ledger read aloud leaves one last beat
        // too — a week on, the two staged returns are
        // quietly corrected: home to their true owners,
        // with plain slips in nobody's studied hand. Its
        // own once-only machinery; no-ops otherwise.
        Mystery12.noteReturnsCorrected(state)
        // v0.15.0j: a wall read aloud leaves one last beat
        // too — a week on, the audited wall is quietly
        // rehung true: the pair uncrossed, the original
        // home in its frame. Its own once-only machinery;
        // no-ops otherwise.
        Mystery13.noteWallCorrected(state)
        state.shenaniganId = null
        state.lastStageSeen = -1
        val week = gameDayOfYear(state) / 7
        val rand = Random(dailySeed(state.todayKey) + 4242)
        val eligible = week != state.lastEventWeek
        val forced = state.eventsThisMonth == 0 && state.gameDay >= 20
        // v0.2.0f: a due mystery chapter takes the slot outright.
        // It can never share the day with a regular roll (this IS
        // the only slot) and Mystery.dueChapter defers it past
        // holiday episodes, so the shelf never doubles up either.
        val chapterDef = Mystery.dueChapter(state)
        if (chapterDef != null) {
            startEvent(state, chapterDef, rand, week)
            Mystery.noteChapterStarted(state, chapterDef)
            val chNum = Mystery.chapterIndexOf(chapterDef.id) + 1
            pushEvent(state, if (chNum == 1)
                "🔍 A NEW CASE OPENS: '${chapterDef.title}'. " +
                    "The Journal has opened a 🔍 MYSTERY page."
            else
                "🔍 THE CASE CONTINUES: chapter $chNum of " +
                    "${Mystery.ARC_CHAPTERS.size} — '${chapterDef.title}'. " +
                    "The 🔍 MYSTERY page is keeping score.")
        }
        // v0.3.0f: the booth case's beats queue into the same
        // slot behind the pond chapters (the two cases never
        // overlap — this one can't even schedule until the
        // pond case retires).
        val beatDef = if (chapterDef == null) Mystery2.dueBeat(state) else null
        if (beatDef != null) {
            startEvent(state, beatDef, rand, week)
            Mystery2.noteBeatStarted(state, beatDef)
            pushEvent(state, if (beatDef.id == Mystery2.MARATHON_ID)
                "🔍 A NEW CASE OPENS: '${beatDef.title}' at the Bijou tonight. " +
                    "The 🔍 MYSTERY page has a second case."
            else
                "🔍 THE BOOTH CASE: tonight, '${beatDef.title}'. " +
                    "If you've done the interviews, the floor is yours.")
        }
        // v0.4.0f: the buried case's chapters queue into the
        // same slot behind the booth case — it can't even
        // schedule until the booth case retires, and its first
        // chapter waits ten days past that retirement.
        val digDef = if (chapterDef == null && beatDef == null)
            Mystery3.dueChapter3(state) else null
        if (digDef != null) {
            startEvent(state, digDef, rand, week)
            Mystery3.noteChapterStarted3(state, digDef)
            val digNum = Mystery3.chapterIndexOf3(digDef.id) + 1
            pushEvent(state, if (digNum == 1)
                "🔍 A NEW CASE OPENS: '${digDef.title}' at Tegridy Farms. " +
                    "The 🔍 MYSTERY page has a third case."
            else
                "🔍 THE BURIED CASE CONTINUES: chapter $digNum of " +
                    "${Mystery3.CHAPTERS3.size} — '${digDef.title}'. " +
                    "The 🔍 MYSTERY page is keeping score.")
        }
        // v0.6.0c: the gardener case's chapters queue into the
        // same slot behind the buried case — it can't even
        // schedule until the buried case retires, and its
        // first chapter waits ten days past that retirement.
        val gardDef = if (chapterDef == null && beatDef == null &&
            digDef == null) Mystery4.dueChapter4(state) else null
        if (gardDef != null) {
            startEvent(state, gardDef, rand, week)
            Mystery4.noteChapterStarted4(state, gardDef)
            val gardNum = Mystery4.chapterIndexOf4(gardDef.id) + 1
            pushEvent(state, if (gardNum == 1)
                "🌱 A NEW CASE OPENS: '${gardDef.title}'. Somebody is " +
                    "fixing this town in the night. The 🔍 MYSTERY page " +
                    "has a fourth case."
            else
                "🌱 THE NIGHT GARDENER: chapter $gardNum of " +
                    "${Mystery4.CHAPTERS4.size} — '${gardDef.title}'. " +
                    "The 🔍 MYSTERY page is keeping score.")
        }
        // v0.7.0f: the confession case's chapters queue into the
        // same slot behind the gardener case — it can't even
        // schedule until the gardener case retires, and its
        // first chapter waits ten days past that retirement.
        val confDef = if (chapterDef == null && beatDef == null &&
            digDef == null && gardDef == null) Mystery5.dueChapter5(state)
            else null
        if (confDef != null) {
            startEvent(state, confDef, rand, week)
            Mystery5.noteChapterStarted5(state, confDef)
            val confNum = Mystery5.chapterIndexOf5(confDef.id) + 1
            pushEvent(state, if (confNum == 1)
                "⚖️ A NEW CASE OPENS: '${confDef.title}'. Something " +
                    "happened at the derby, and the 🔍 MYSTERY page " +
                    "has a fifth case."
            else
                "⚖️ THE TWO CONFESSIONS: chapter $confNum of " +
                    "${Mystery5.CHAPTERS5.size} — '${confDef.title}'. " +
                    "The 🔍 MYSTERY page is keeping the ledger.")
        }
        // v0.8.0f: the opinion case's chapters queue into the
        // same slot behind the confession case — it can't even
        // schedule until the two confessions retire, and its
        // first chapter waits ten days past that retirement.
        val courtDef = if (chapterDef == null && beatDef == null &&
            digDef == null && gardDef == null && confDef == null)
            Mystery6.dueChapter6(state) else null
        if (courtDef != null) {
            startEvent(state, courtDef, rand, week)
            Mystery6.noteChapterStarted6(state, courtDef)
            val courtNum = Mystery6.chapterIndexOf6(courtDef.id) + 1
            pushEvent(state, if (courtNum == 1)
                "🎨 A NEW CASE OPENS: '${courtDef.title}'. The town " +
                    "mural is black, the town has a name already, and " +
                    "the 🔍 MYSTERY page has a sixth case."
            else
                "🏛️ THE COURT OF PUBLIC OPINION: chapter $courtNum of " +
                    "${Mystery6.CHAPTERS6.size} — '${courtDef.title}'. " +
                    "The 🔍 MYSTERY page is keeping two columns now.")
        }
        // v0.9.0f: the backwards case's chapters queue into the
        // same slot behind the opinion case — it can't even
        // schedule until the court of public opinion retires,
        // and its first chapter waits ten days past that
        // retirement. Its chapters air in the present and
        // reconstruct the past, latest movement first.
        val backDef = if (chapterDef == null && beatDef == null &&
            digDef == null && gardDef == null && confDef == null &&
            courtDef == null) Mystery7.dueChapter7(state) else null
        if (backDef != null) {
            startEvent(state, backDef, rand, week)
            Mystery7.noteChapterStarted7(state, backDef)
            val backNum = Mystery7.chapterIndexOf7(backDef.id) + 1
            pushEvent(state, if (backNum == 1)
                "🕰️ A NEW CASE OPENS: '${backDef.title}'. You were " +
                    "there when it ended. The 🔍 MYSTERY page has a " +
                    "seventh case — and a timeline to fill in backwards."
            else
                "🕰️ THE CASE TOLD BACKWARDS: chapter $backNum of " +
                    "${Mystery7.CHAPTERS7.size} — '${backDef.title}'. " +
                    "The 🔍 MYSTERY page is keeping the timeline.")
        }
        // v0.10.0f: the alibi case's chapters queue into the
        // same slot behind the backwards case — it can't even
        // schedule until the case told backwards retires, and
        // its first chapter waits ten days past that
        // retirement. Its chapters collect the vouches and
        // re-count the evening, pair by pair.
        val aliDef = if (chapterDef == null && beatDef == null &&
            digDef == null && gardDef == null && confDef == null &&
            courtDef == null && backDef == null)
            Mystery8.dueChapter8(state) else null
        if (aliDef != null) {
            startEvent(state, aliDef, rand, week)
            Mystery8.noteChapterStarted8(state, aliDef)
            val aliNum = Mystery8.chapterIndexOf8(aliDef.id) + 1
            pushEvent(state, if (aliNum == 1)
                "🔤 A NEW CASE OPENS: '${aliDef.title}'. Everybody " +
                    "was somewhere, and every somewhere checks. " +
                    "The 🔍 MYSTERY page has an eighth case — " +
                    "and a graph to draw."
            else
                "🔤 EVERYBODY WAS SOMEWHERE: chapter $aliNum of " +
                    "${Mystery8.CHAPTERS8.size} — '${aliDef.title}'. " +
                    "The 🔍 MYSTERY page is keeping the graph.")
        }
        // v0.11.0f: the audit case's chapters queue into the
        // same slot behind the alibi case — it can't even
        // schedule until everybody was somewhere retires,
        // and its first chapter waits ten days past that
        // retirement. Its chapters count the rooms, then
        // read the books, site by site.
        val auditDef = if (chapterDef == null && beatDef == null &&
            digDef == null && gardDef == null && confDef == null &&
            courtDef == null && backDef == null && aliDef == null)
            Mystery9.dueChapter9(state) else null
        if (auditDef != null) {
            startEvent(state, auditDef, rand, week)
            Mystery9.noteChapterStarted9(state, auditDef)
            val auditNum = Mystery9.chapterIndexOf9(auditDef.id) + 1
            pushEvent(state, if (auditNum == 1)
                "🧮 A NEW CASE OPENS: '${auditDef.title}'. Nothing " +
                    "was taken; the counts say things were added. " +
                    "The 🔍 MYSTERY page has a ninth case — " +
                    "and a ledger to reconcile."
            else
                "🧮 ONE MORE THAN COUNTED: chapter $auditNum of " +
                    "${Mystery9.CHAPTERS9.size} — '${auditDef.title}'. " +
                    "The 🔍 MYSTERY page is keeping the ledger.")
        }
        // v0.12.0f: the photo case's chapters queue into the
        // same slot behind the audit case — it can't even
        // schedule until one more than counted retires,
        // and its first chapter waits ten days past that
        // retirement. Its chapters hang the display, then
        // compare the frames, field by field.
        val photoDef = if (chapterDef == null && beatDef == null &&
            digDef == null && gardDef == null && confDef == null &&
            courtDef == null && backDef == null && aliDef == null &&
            auditDef == null) Mystery10.dueChapter10(state) else null
        if (photoDef != null) {
            startEvent(state, photoDef, rand, week)
            Mystery10.noteChapterStarted10(state, photoDef)
            val photoNum = Mystery10.chapterIndexOf10(photoDef.id) + 1
            pushEvent(state, if (photoNum == 1)
                "📷 A NEW CASE OPENS: '${photoDef.title}'. Two " +
                    "prints of one sitting disagree by a face. " +
                    "The 🔍 MYSTERY page has a tenth case — " +
                    "and a wall to compare."
            else
                "📷 THE PHOTOGRAPH NOBODY TOOK: chapter $photoNum of " +
                    "${Mystery10.CHAPTERS10.size} — '${photoDef.title}'. " +
                    "The 🔍 MYSTERY page is keeping the comparison.")
        }
        // v0.13.0f: the voice case's chapters queue into the
        // same slot behind the photo case — it can't even
        // schedule until signed in another's voice retires,
        // and its first chapter waits ten days past that
        // retirement. Its chapters surface the letters, then
        // read them against the voices they claim.
        val voiceDef = if (chapterDef == null && beatDef == null &&
            digDef == null && gardDef == null && confDef == null &&
            courtDef == null && backDef == null && aliDef == null &&
            auditDef == null && photoDef == null)
            Mystery11.dueChapter11(state) else null
        if (voiceDef != null) {
            startEvent(state, voiceDef, rand, week)
            Mystery11.noteChapterStarted11(state, voiceDef)
            val voiceNum = Mystery11.chapterIndexOf11(voiceDef.id) + 1
            pushEvent(state, if (voiceNum == 1)
                "🎙️ A NEW CASE OPENS: '${voiceDef.title}'. Six " +
                    "letters have surfaced with the town's " +
                    "names under them. The 🔍 MYSTERY page has " +
                    "an eleventh case — and a ledger of voices."
            else
                "🎙️ SIGNED IN ANOTHER'S VOICE: chapter $voiceNum of " +
                    "${Mystery11.CHAPTERS11.size} — '${voiceDef.title}'. " +
                    "The 🔍 MYSTERY page is keeping the ledger.")
        }
        // v0.14.0f: the returns case's chapters queue into
        // the same slot behind the voice case — it can't
        // even schedule until signed in another's voice
        // retires, and its first chapter waits ten days
        // past that retirement. Its chapters surface the
        // returns, then read them against the losses, the
        // hands and the fits they claim.
        val returnsDef = if (chapterDef == null && beatDef == null &&
            digDef == null && gardDef == null && confDef == null &&
            courtDef == null && backDef == null && aliDef == null &&
            auditDef == null && photoDef == null && voiceDef == null)
            Mystery12.dueChapter12(state) else null
        if (returnsDef != null) {
            startEvent(state, returnsDef, rand, week)
            Mystery12.noteChapterStarted12(state, returnsDef)
            val returnsNum = Mystery12.chapterIndexOf12(returnsDef.id) + 1
            pushEvent(state, if (returnsNum == 1)
                "📦 A NEW CASE OPENS: '${returnsDef.title}'. Six " +
                    "things have come back at the Elementary — " +
                    "to the wrong owners, in their own " +
                    "handwriting. The 🔍 MYSTERY page has a " +
                    "twelfth case — and a ledger of returns."
            else
                "📦 BACK TO THE WRONG OWNER: chapter $returnsNum of " +
                    "${Mystery12.CHAPTERS12.size} — '${returnsDef.title}'. " +
                    "The 🔍 MYSTERY page is keeping the ledger.")
        }
        // v0.15.0j: the exhibition case's chapters queue into
        // the same slot behind the returns case — it can't
        // even schedule until back to the wrong owner
        // retires, and its first chapter waits ten days past
        // that retirement AND for a gallery show to have
        // wrapped with a wall worth auditing. Its chapters
        // surface the frames, then read them against the
        // album, the darkroom and the log they claim.
        val exhibitDef = if (chapterDef == null && beatDef == null &&
            digDef == null && gardDef == null && confDef == null &&
            courtDef == null && backDef == null && aliDef == null &&
            auditDef == null && photoDef == null && voiceDef == null &&
            returnsDef == null)
            Mystery13.dueChapter13(state) else null
        if (exhibitDef != null) {
            startEvent(state, exhibitDef, rand, week)
            Mystery13.noteChapterStarted13(state, exhibitDef)
            val exhibitNum = Mystery13.chapterIndexOf13(exhibitDef.id) + 1
            pushEvent(state, if (exhibitNum == 1)
                "🖼 A NEW CASE OPENS: '${exhibitDef.title}'. Six " +
                    "frames on a wrapped show's wall don't read " +
                    "the way their log says they should. " +
                    "The 🔍 MYSTERY page has a thirteenth case — " +
                    "and a wall to audit."
            else
                "🖼 NOT WHAT THE FRAME SAYS: chapter $exhibitNum of " +
                    "${Mystery13.CHAPTERS13.size} — '${exhibitDef.title}'. " +
                    "The 🔍 MYSTERY page is keeping the audit.")
        } else if (chapterDef == null && beatDef == null &&
            digDef == null && gardDef == null && confDef == null &&
            courtDef == null && backDef == null && aliDef == null &&
            auditDef == null && photoDef == null && voiceDef == null &&
            returnsDef == null && exhibitDef == null &&
            // v0.5.0h: a season-finale window pre-empts the
            // regular roll for its whole week — the finale IS
            // the week's episode. (The cases above still take
            // the slot first; they outrank everything.)
            !Finales.suppressesDailyRoll(state) &&
            ((eligible && rand.nextDouble() < 0.5) || forced)) {
            val pool = SHENANIGANS.filter { it.id != state.lastShenaniganId }
            // #2 WEATHER STACKS THE EVENT DECK + #32 EVENT LINEAGE:
            // the sky and the last story both weight this pick.
            val weighted = pool.map { def ->
                var wgt = 1.0
                when (state.weatherId) {
                    "heavy_snow", "blizzard", "light_snow" ->
                        if (def.id == "mole_panic") wgt *= 3.0
                    "sunny" -> {
                        if (def.id == "tegridy_takeover") wgt *= 3.0
                        if (def.id == "memberberry_fever") wgt *= 2.0
                    }
                    "fog" -> {
                        if (def.id == "silent_challenge") wgt *= 3.0
                        if (def.id == "coon_sightings") wgt *= 2.0
                        if (def.id == "imagination_leak") wgt *= 2.0
                    }
                    "rain" -> if (def.id == "pirate_week") wgt *= 2.0
                }
                EVENT_LINEAGE[state.lastShenaniganId]?.get(def.id)?.let { wgt *= it }
                def to wgt
            }
            val totalW = weighted.sumOf { it.second }
            var roll = rand.nextDouble() * totalW
            var def = weighted.last().first
            for ((d, wgt) in weighted) {
                roll -= wgt
                if (roll <= 0) { def = d; break }
            }
            startEvent(state, def, rand, week)
        }
    }

    /** Kick an event off — shared by the weekly roll and by a
     *  mystery chapter taking the slot (v0.2.0f), so a chapter is
     *  born with the identical witness / episode / cold-open
     *  treatment as any rolled event. */
    private fun startEvent(state: GameState, def: ShenaniganDef, rand: Random, week: Int) {
        state.shenaniganId = def.id
        state.shenaniganDay = state.todayKey
        state.lastShenaniganId = def.id
        state.lastEventWeek = week
        state.eventsThisMonth++
        pushEvent(state, "🎪 SHENANIGANS: ${def.title} — ${def.cause}")
        // #21 GENERATED FOLKS GET EVENT ROLES: one generated
        // resident is named the event's witness/organizer.
        val residents = VisitorSystem.residents()
        if (residents.isNotEmpty()) {
            val witness = residents[rand.nextInt(residents.size)]
            state.eventWitness = witness.loreName
            pushEvent(state, "🎪 ${witness.loreName} is right in the middle of it — " +
                "the town's unofficial witness.")
        }
        // v0.1.9c: open the episode BEFORE stage-0 effects so
        // its faction snapshot covers the whole day's shifts.
        val ep = Episodes.beginEvent(state, def)
        applyStageEffects(state, def, 0)
        state.lastStageSeen = 0
        // v0.1.9c COLD OPEN: if you're standing with the causers
        // (or at the kickoff cluster) when it starts, the premise
        // plays as an opening beat before the title card.
        val me = player(state)
        val cluster0 = def.stages[0].clusterLoc
        val presentForIt = (cluster0 != null && me.location == cluster0) ||
            def.causers.any { state.agents[it]?.location == me.location }
        if (presentForIt) {
            val lines = Episodes.coldOpenLines(def)
            if (lines.isNotEmpty()) {
                state.lastConversation = lines
                ep.coldOpen = true
                pushEvent(state, "🎬 COLD OPEN: ${def.cause}")
            }
        }
    }

    /** Stage-entry effects: faction shifts, litter bursts. (Weather locks
     *  and routine transforms read the stage live elsewhere.) */
    fun applyStageEffects(state: GameState, def: ShenaniganDef, stageIdx: Int) {
        val stage = def.stages[stageIdx]
        for (fx in stage.factionEffects) {
            val fs = state.factionStates[fx.factionId] ?: continue
            fs.influence = (fs.influence + fx.influenceDelta).coerceIn(0, 100)
            fs.playerStanding = (fs.playerStanding + fx.standingDelta).coerceIn(-50, 100)
            // #4: a stage that lifts a faction lifts its MOOD too (and
            // a stage that dumps on it sours it) — mood then scales
            // greetings, ambient warmth, and drift until it decays.
            if (fx.influenceDelta != 0) {
                fs.moodScore = (fs.moodScore +
                    if (fx.influenceDelta > 0) 1 else -1).coerceIn(-2, 2)
            }
            if (fx.note.isNotEmpty()) {
                fs.mood = fx.note
                pushEvent(state, "⚔️ FACTION SHIFT: ${fx.note}")
            }
            if (fx.relationWith != null && fx.relationDelta != 0) {
                val k = Factions.relationKey(fx.factionId, fx.relationWith)
                val cur = state.factionRelations[k] ?: 0
                state.factionRelations[k] = (cur + fx.relationDelta).coerceIn(-2, 2)
            }
        }
        repeat(stage.litterBurst) {
            val locs = listOf("MainStreet", "KidsPark", "Mall", "Elementary", "BusStop")
            dropLitter(state, locs[random.nextInt(locs.size)], false,
                debrisItem(random), "the '${def.title}' mess")
        }
    }

    /** After an event ends, one participant may keep an event-flavored
     *  routine block as permanent schedule drift (residue). */
    /** Returns the name of whoever kept a piece of the event in
     *  their routine (v0.1.9c: the episode recap credits it). */
    private fun eventResidue(state: GameState, def: ShenaniganDef): String? {
        val climax = def.stages[2]
        val loc = climax.clusterLoc ?: return null
        if (random.nextDouble() > 0.35) return null
        val cast = state.agents.values.filter { !it.isPlayer }
        if (cast.isEmpty()) return null
        val a = cast[random.nextInt(cast.size)]
        val drift = state.scheduleDrift.getOrPut(a.name) { mutableMapOf() }
        drift["16:30"] = loc
        pushEvent(state, "🌀 ${a.name} kept a piece of '${def.title}' in their routine — " +
            "they hang at ${LOCATIONS[loc]?.name} now.")
        return a.name
    }

    /**
     * Slow schedule drift: each day, ~1 in 10 NPCs mutates ONE afternoon
     * block toward faction turf or a friend's place. Persists (max 3
     * drifted blocks per NPC) so routines feel alive, never random.
     */
    private fun rollScheduleDrift(state: GameState) {
        val leisure = listOf("KidsPark", "Mall", "TweekBros", "StarksPond", "MainStreet", "Bijou")
        for ((name, a) in state.agents) {
            if (a.isPlayer) continue
            if (random.nextDouble() > 0.10) continue
            val drift = state.scheduleDrift.getOrPut(name) { mutableMapOf() }
            if (drift.size >= 3) continue
            val candidates = a.agendaBase.keys.filter {
                it !in setOf("08:00", "09:00", "21:00") && it !in drift.keys
            }
            if (candidates.isEmpty()) continue
            val key = candidates[random.nextInt(candidates.size)]
            val turf = FACTION_OF_CAST[name]?.let { FACTION_BY_ID[it]?.turf?.firstOrNull() }
            val friendHome = relationsOf(name)
                .maxByOrNull { state.npcBonds[bondKey(name, it)] ?: 0 }
                ?.let { KID_HOUSE[it] }
            val target = when {
                turf != null && random.nextDouble() < 0.45 -> turf
                friendHome != null && random.nextDouble() < 0.5 -> friendHome
                else -> leisure[random.nextInt(leisure.size)]
            }
            if (target != a.agendaBase[key]?.location) {
                drift[key] = target
            }
        }
    }

    fun bondKey(a: String, b: String): String = if (a < b) "$a|$b" else "$b|$a"

    /**
     * In-game clock tick (real-time sync OFF): classic 15-minute step plus
     * sleep/wake handling driven by in-game time. Mirrors syncToDeviceTime's
     * sleep logic but never touches the device clock.
     */
    fun inGameTick(state: GameState) {
        step(state)
        val asleep = isSleepHour(state.timeMin / 60)
        for ((name, a) in state.agents) {
            if (a.isPlayer) continue
            if (asleep) {
                // #42: night owls are exempt from the forced march
                // home until 2 AM — they're at their spots, awake.
                val owlSpot = OWL_SPOTS[name]
                if (owlSpot != null &&
                    (state.timeMin >= 22 * 60 || state.timeMin < 2 * 60)) {
                    a.asleep = false
                    if (a.location != owlSpot && a.destination != owlSpot) {
                        a.destination = owlSpot
                        a.activity = "Out late. The night is theirs."
                        a.waypoints.clear()
                        a.waypoints.addAll(calculateWaypoints(a.location, owlSpot))
                    }
                    continue
                }
                if (!a.asleep) {
                    // v0.15.0g: the night march goes HOME —
                    // CastHomes' anchor (family house or
                    // workplace), the same home newState
                    // spawns at. KID_HOUSE read here before;
                    // its one Bus Stop row (the driver) would
                    // have sent her to sleep at the stop.
                    val home = CastHomes.homeOf(name) ?: a.location
                    a.location = home
                    a.destination = home
                    a.waypoints.clear()
                    a.coords = LOCATIONS[home]!!.let { it.gx to it.gy }
                    a.activity = SLEEP_ACTIVITY
                    a.asleep = true
                }
                continue
            }
            if (a.asleep) {
                // wake up, head to whatever the agenda says
                a.asleep = false
                val block = effectiveAgendaBlock(state, a, state.timeMin)
                if (block != null && a.destination != block.location) {
                    a.destination = block.location
                    a.activity = "Walking to ${LOCATIONS[block.location]!!.name}."
                    a.waypoints.clear()
                    a.waypoints.addAll(calculateWaypoints(a.location, block.location))
                } else if (block != null) {
                    a.activity = block.activity
                }
            }
        }
        // Ambient littering (cast): rare, personality-flavored. The town's
        // visitors (VisitorSystem side) litter far more — see GameActivity.
        for ((name, a) in state.agents) {
            if (a.isPlayer || a.asleep || a.waypoints.isNotEmpty()) continue
            val rate = CharacterSystem.litterRate(name)
            if (rate > 0 && random.nextDouble() < rate) {
                // v0.1.9a #93: in snowy weather, some of what gets
                // dropped is a lost mitten — its partner is always
                // still on somebody's hand, briefly.
                val item = if (state.weatherId in setOf("light_snow", "heavy_snow", "blizzard") &&
                    random.nextDouble() < 0.10) "lost_mitten"
                    else LITTER_ITEM_IDS[random.nextInt(LITTER_ITEM_IDS.size)]
                dropLitter(state, a.location, CharacterSystem.npcInside(a.location),
                    item, name)
            }
        }
    }

    /**
     * v0.1.9a living-world engine hooks — idempotent, called at
     * the end of step() and ambientTick() so they fire on both
     * the in-game clock and the real-time ambient loop.
     */
    fun tickLivingWorld(state: GameState) {
        // #92 MYSTERY LUNCHBOX: it sits on the bus-stop bench all
        // morning until today's kid's route takes them AWAY from
        // the stop — they grab it on their way out, right on
        // schedule. (If they're still lingering at 10:55, they
        // take it then, sheepishly.)
        if (state.life["lunchboxDay"] != state.absDay &&
            state.timeMin in 430..660) {
            val kidName = TownLife.lunchboxKidFor(state)
            val kid = state.agents[kidName]
            if (kid != null && !kid.asleep) {
                val bus = LOCATIONS["BusStop"]!!
                val dx = kid.coords.first - bus.gx
                val dy = kid.coords.second - bus.gy
                val nearStop = dx * dx + dy * dy < 26 * 26
                val departedWithIt = kid.location != "BusStop" && nearStop &&
                    kid.waypoints.isNotEmpty()
                val lastCall = state.timeMin >= 655 && kid.location == "BusStop" &&
                    kid.waypoints.isEmpty()
                if (departedWithIt || lastCall) {
                    state.life["lunchboxDay"] = state.absDay
                    kid.memories.add("Grabbed the mystery lunchbox off the bench. Finders keepers.")
                    pushEvent(state, "🎒 The mystery lunchbox is GONE — $kidName grabbed it " +
                        "off the bus-stop bench, right on schedule. Case closed (today).")
                }
            }
        }
        // #89 ALARM CLOCK PANIC: the day's oversleeper is named
        // once, just before their sprint window opens.
        if (state.timeMin in 475..495 && state.lifeText["alarmDay"] != state.todayKey) {
            state.lifeText["alarmDay"] = state.todayKey
            val kid = TownLife.alarmKidFor(state)
            pushEvent(state, "⏰ $kid overslept AGAIN. The alarm never stood a " +
                "chance — watch the doors around 8 for the sprint.")
        }
    }

    /** #47 PACE TELLS THE STORY: gait scales with age band — late
     *  kids hurry, adults stride, Towelie ambles — and with how late
     *  the walker already is for the block they're headed to. */
    fun paceFor(state: GameState, a: Agent): Double {
        if (a.name == "Towelie") return 7.0
        var v = when (CharacterSystem.bandOf(a.name)) {
            AgeBand.KID -> 9.0
            AgeBand.YOUNG_TEEN -> 10.0
            AgeBand.TEEN -> 10.5
            AgeBand.OLDER_TEEN -> 11.0
            AgeBand.ADULT -> 11.5
        }
        val key = agendaKeyAt(a, state.timeMin)
        if (key != null && state.timeMin - timeStrToMinutes(key) >= 45) v *= 1.35
        // v0.1.9a #19 RAIN DASH: a marked dash to shelter runs at
        // full hurry pace for as long as the trip lasts.
        if (state.lifeText["dash:${a.name}"] == a.destination &&
            a.waypoints.isNotEmpty()) v *= 1.75
        // v0.1.9a #89 ALARM SPRINT: today's oversleeper covers
        // their morning trip at a dead sprint.
        if (a.name == TownLife.alarmKidFor(state) &&
            state.timeMin in 470..570 && a.waypoints.isNotEmpty()) v *= 1.85
        return v
    }

    /** Nearest enterable building to a location, by map distance. */
    private fun nearestBuilding(locId: String): String {
        val from = LOCATIONS[locId] ?: return "TweekBros"
        return LOCATIONS.values
            // v0.3.0d: your porch is not public shelter — the rain
            // dash never camps a stranger at the player's house.
            .filter { it.kind in setOf("house", "shop", "civic", "school") &&
                it.id != YOUR_HOUSE }
            .minByOrNull {
                val dx = it.gx - from.gx
                val dy = it.gy - from.gy
                dx * dx + dy * dy
            }?.id ?: "TweekBros"
    }

    fun updatePositions(state: GameState) {
        for ((name, a) in state.agents) {
            // v0.16.0c (item 12 closed): the trip record —
            // Travel.maintain stamps a departure when the
            // dispatcher has set a walker walking, restarts
            // it honestly on a mid-trip redirect, and clears
            // it on arrival, so the street can render the
            // walker IN a scene on the route at every tick.
            Travel.maintain(state, a)
            if (a.waypoints.isEmpty()) {
                if (a.location != a.destination) {
                    a.location = a.destination
                    a.activity = agendaActivity(state, a, state.timeMin)
                    pushEvent(state, "📍 $name rolled up to ${LOCATIONS[a.location]!!.name}.")
                }
                continue
            }

            val (cx, cy) = a.coords
            val (tx, ty) = a.waypoints[0]
            val dx = tx - cx
            val dy = ty - cy
            val dist = kotlin.math.sqrt((dx * dx + dy * dy).toDouble())
            if (dist <= 10) {
                a.coords = tx to ty
                a.waypoints.removeAt(0)
                if (a.waypoints.isEmpty()) {
                    a.location = a.destination
                    a.activity = agendaActivity(state, a, state.timeMin)
                    pushEvent(state, "📍 $name rolled up to ${LOCATIONS[a.location]!!.name}.")
                } else {
                    a.location = HUB
                }
            } else {
                var speed = paceFor(state, a)
                // #49 GROUPS TRAVEL TOGETHER: a bonded partner headed
                // the same way sets the convoy pace — nobody gets
                // left behind, they arrive together.
                for (otherName in relationsOf(name)) {
                    val o = state.agents[otherName] ?: continue
                    if (o.waypoints.isNotEmpty() && o.destination == a.destination &&
                        (state.npcBonds[bondKey(name, otherName)] ?: 0) >= 3) {
                        speed = minOf(speed, paceFor(state, o))
                    }
                }
                val r = speed / dist
                a.coords = (cx + dx * r).toInt() to (cy + dy * r).toInt()
                a.location = HUB
            }
            // v0.16.0c: a trip that completes inside this
            // tick (a short hop) sheds its record in the
            // SAME tick — post-step state never carries a
            // trip record for a settled agent.
            Travel.maintain(state, a)
        }
    }

    fun checkInteractions(state: GameState) {
        dailyRand = Random(dailySeed(state.todayKey))
        val agents = state.agents
        val groups = mutableMapOf<String, MutableList<String>>()
        for ((name, a) in agents) {
            if (a.isPlayer) continue
            if (a.asleep) continue
            if (a.location == HUB || a.waypoints.isNotEmpty() || state.timeMin < a.chatCooldownUntil) continue
            groups.getOrPut(a.location) { mutableListOf() }.add(name)
        }
        // #35 CROWD DENSITY BREATHES (strict form, v0.1.9a): the
        // cap is a pure function of THIS location's occupancy and
        // the hour (TownLife.densityCap), counted PER LOCATION —
        // no flat cap, no shared pool for one busy spot to drain.
        // Bazaars buzz; 3 AM doesn't.
        val convByLoc = mutableMapOf<String, Int>()
        for ((loc, names) in groups) {
            if (names.size < 2) continue
            val capHere = TownLife.densityCap(names.size, state.timeMin / 60)
            if ((convByLoc[loc] ?: 0) >= capHere) continue
            convByLoc[loc] = (convByLoc[loc] ?: 0) + 1
            val p1 = names[0]
            // #36: pair toward shared faction / known relations — a
            // rising faction's people find each other in the crowd.
            val p2 = names.drop(1).firstOrNull { other ->
                relationNote(p1, other) != null ||
                    (FACTION_OF_CAST[p1] != null && FACTION_OF_CAST[p1] == FACTION_OF_CAST[other])
            } ?: names[1]
            val locName = LOCATIONS[loc]!!.name
            var dialogue = invokeProvider(state, "collision", agents[p1]!!, agents[p2]!!, locName, null)
            if (random.nextDouble() < 0.30) {
                val quips = WEATHER_QUIPS[state.weatherId] ?: emptyMap()
                // the weather quip goes to whoever did NOT just speak —
                // a script ending on Stan plus a random quip used to put
                // two "Stan:" lines in a row in the speech bubble
                val lastSpeaker = dialogue.lastOrNull()?.speaker
                val who = listOf(p1, p2).firstOrNull { it != lastSpeaker && quips.containsKey(it) }
                    ?: listOf(p1, p2).firstOrNull { it != lastSpeaker }
                if (who != null) quips[who]?.let { dialogue = dialogue + Line(who, it) }
            }
            // #51 GOSSIP CHAIN: a topic you raised with someone today
            // starts jumping between pairs all over town.
            if (dialogue.size >= 2 && random.nextDouble() < 0.30) {
                val gossip = NpcMemoryStore.all().values
                    .filter { it.lastMetDay == state.todayKey }
                    .mapNotNull { it.topics.lastOrNull() }
                    .lastOrNull()
                if (gossip != null) {
                    val last = dialogue.last()
                    dialogue = dialogue.dropLast(1) + Line(last.speaker,
                        "Everyone's still talking about $gossip, by the way.")
                }
            }
            // v0.2.0f: the closed case stays in the town's mouth —
            // ambient pairs still tell the ending they believe in
            // (wrong theories included), long after the finale.
            if (dialogue.size >= 2 && state.arcRetired &&
                random.nextDouble() < 0.15) {
                Mystery.ambientCallback(state, random)?.let { cb ->
                    dialogue = dialogue + Line(dialogue.last().speaker, cb)
                }
            }
            // v0.3.0f: a name said wrong at the Bijou is kept,
            // too — the wrongly-named carry the naming into
            // ambient talk whenever they're in the pair.
            if (dialogue.size >= 2 && state.arc2Branch == "wrong" &&
                random.nextDouble() < 0.30) {
                Mystery2.grudgeCallback(state, p1, p2)?.let { cb ->
                    dialogue = dialogue + cb
                }
            }
            // v0.4.0f: the buried case's ending stays in the
            // town's mouth too — the trust, the stone, the
            // catalogue, or the whistling.
            if (dialogue.size >= 2 && state.arc3Retired &&
                random.nextDouble() < 0.15) {
                Mystery3.ambientCallback3(state, random)?.let { cb ->
                    dialogue = dialogue + Line(dialogue.last().speaker, cb)
                }
            }
            // v0.6.0c: the gardener case's ending stays in the
            // town's mouth too — the plaque, the pair of ghosts,
            // the kept secret, or the bare flagpole.
            if (dialogue.size >= 2 && state.arc4Retired &&
                random.nextDouble() < 0.15) {
                Mystery4.ambientCallback4(state, random)?.let { cb ->
                    dialogue = dialogue + Line(dialogue.last().speaker, cb)
                }
            }
            // v0.7.0f: the confession case's ending stays in the
            // town's mouth too — the grounded boy, the believed
            // man, the clearing, or the drawer nobody opens.
            if (dialogue.size >= 2 && state.arc5Retired &&
                random.nextDouble() < 0.15) {
                Mystery5.ambientCallback5(state, random)?.let { cb ->
                    dialogue = dialogue + Line(dialogue.last().speaker, cb)
                }
            }
            // v0.8.0f: the opinion case's ending stays in the
            // town's mouth too — the cleared name, the repainted
            // wall, the blink, or the four-minute meeting.
            if (dialogue.size >= 2 && state.arc6Retired &&
                random.nextDouble() < 0.15) {
                Mystery6.ambientCallback6(state, random)?.let { cb ->
                    dialogue = dialogue + Line(dialogue.last().speaker, cb)
                }
            }
            // v0.9.0f: the backwards case's ending stays in the
            // town's mouth too — the plaque, the amendment, the
            // rerun, or the story told fire-first at last.
            if (dialogue.size >= 2 && state.arc7Retired &&
                random.nextDouble() < 0.15) {
                Mystery7.ambientCallback7(state, random)?.let { cb ->
                    dialogue = dialogue + Line(dialogue.last().speaker, cb)
                }
            }
            // v0.10.0f: the alibi case's ending stays in the
            // town's mouth too — the re-count, the name, the
            // standing count, or the unread graph.
            if (dialogue.size >= 2 && state.arc8Retired &&
                random.nextDouble() < 0.15) {
                Mystery8.ambientCallback8(state, random)?.let { cb ->
                    dialogue = dialogue + Line(dialogue.last().speaker, cb)
                }
            }
            // v0.11.0f: the audit case's ending stays in the
            // town's mouth too — the reconciliation, the
            // name, the filed drawer, or the unclosed count.
            if (dialogue.size >= 2 && state.arc9Retired &&
                random.nextDouble() < 0.15) {
                Mystery9.ambientCallback9(state, random)?.let { cb ->
                    dialogue = dialogue + Line(dialogue.last().speaker, cb)
                }
            }
            // v0.12.0f: the photo case's ending stays in the
            // town's mouth too — the completed wall, the
            // name, the captions standing, or the unexposed
            // opening.
            if (dialogue.size >= 2 && state.arc10Retired &&
                random.nextDouble() < 0.15) {
                Mystery10.ambientCallback10(state, random)?.let { cb ->
                    dialogue = dialogue + Line(dialogue.last().speaker, cb)
                }
            }
            // v0.13.0f: the voice case's ending stays in the
            // town's mouth too — the returned voices, the
            // name, the signatures standing, or the
            // unopened folder.
            if (dialogue.size >= 2 && state.arc11Retired &&
                random.nextDouble() < 0.15) {
                Mystery11.ambientCallback11(state, random)?.let { cb ->
                    dialogue = dialogue + Line(dialogue.last().speaker, cb)
                }
            }
            // v0.14.0f: the returns case's ending stays in
            // the town's mouth too — the corrected returns,
            // the name, the slips standing, or the box's
            // counsel kept.
            if (dialogue.size >= 2 && state.arc12Retired &&
                random.nextDouble() < 0.15) {
                Mystery12.ambientCallback12(state, random)?.let { cb ->
                    dialogue = dialogue + Line(dialogue.last().speaker, cb)
                }
            }
            // v0.15.0j: the exhibition case's ending stays in
            // the town's mouth too — the wall rehung true,
            // the name, the frames standing, or the log's
            // counsel kept.
            if (dialogue.size >= 2 && state.arc13Retired &&
                random.nextDouble() < 0.15) {
                Mystery13.ambientCallback13(state, random)?.let { cb ->
                    dialogue = dialogue + Line(dialogue.last().speaker, cb)
                }
            }
            // v0.3.0c: a finished storyline lives on the same way —
            // the pair keeps telling the ending they got.
            if (dialogue.size >= 2 && random.nextDouble() < 0.18) {
                StoryArcs.ambientCallback(state, p1, p2, random)?.let { cb ->
                    dialogue = dialogue + cb
                }
            }
            // v0.1.9e THREADS: reunions reference the pair's last
            // topic or their running joke — then THIS conversation
            // becomes the thread's latest entry.
            if (dialogue.size >= 2 && random.nextDouble() < 0.30) {
                val bondNow = state.npcBonds[bondKey(p1, p2)] ?: 0
                Threads.pairCallback(state, p1, p2, bondNow, random)?.let { cb ->
                    val who = if (dialogue.lastOrNull()?.speaker == p1) p2 else p1
                    dialogue = dialogue + Line(who, cb)
                }
            }
            Threads.recordPairTopic(state, p1, p2,
                dialogue.joinToString(" ") { it.text })
            // v0.1.9e GOALS, SPOKEN ALOUD: ambient lines about
            // what they're currently up to.
            if (random.nextDouble() < 0.22) {
                val who = if (random.nextBoolean()) p1 else p2
                val g = NpcGoals.record(state, who)
                if (!g.done) dialogue = dialogue + Line(who, NpcGoals.mention(g))
            }
            // #94 THE HANDSHAKE EPIDEMIC: invented at school, spreads
            // week by week through the yard.
            if (loc == "Elementary" && state.timeMin in 9 * 60 until 15 * 60 + 30 &&
                random.nextDouble() < minOf(0.40, 0.03 + 0.04 * (state.absDay / 7))) {
                pushEvent(state, "🤝 $p1 and $p2 just did the fancy handshake. It's spreading.")
            }
            Speak.speak(dialogue)
            pushEvent(state, "💬 $p1 and $p2 are yapping at $locName!")
            agents[p1]!!.memories.add("Yapped with $p2 at $locName.")
            agents[p2]!!.memories.add("Yapped with $p1 at $locName.")
            // NPC<->NPC bonds evolve a little: rivals grate, friends bond.
            val bk = bondKey(p1, p2)
            val rivalPair = relationNote(p1, p2) == "arch-rivals"
            var delta = if (rivalPair && random.nextDouble() < 0.5) -1 else 1
            // #4: faction mood warms or chills ambient bonds.
            val moodHere = listOfNotNull(FACTION_OF_CAST[p1], FACTION_OF_CAST[p2])
                .map { fid -> state.factionStates[fid]?.let {
                    Factions.moodIndex(fid, it, state.weatherId) } ?: 0 }
            if (moodHere.any { it <= -1 } && random.nextDouble() < 0.3) delta = -1
            if (moodHere.any { it >= 1 } && delta > 0 && random.nextDouble() < 0.25) delta = 2
            state.npcBonds[bk] = ((state.npcBonds[bk] ?: 0) + delta).coerceIn(-5, 10)
            // v0.1.9e: meeting face-to-face settles people goals
            // (the meet-up happens; the grudge gets its moment).
            NpcGoals.contactTick(state, p1, p2)
            NpcGoals.contactTick(state, p2, p1)
            // #34 AMBIENT SPARKS MOVE FACTIONS: spats and cross-faction
            // kindnesses build pressure on the pairwise relation until
            // it tips a whole point, even between events.
            val f1 = FACTION_OF_CAST[p1]; val f2 = FACTION_OF_CAST[p2]
            if (f1 != null && f2 != null && f1 != f2) {
                val rk = Factions.relationKey(f1, f2)
                val rel = state.factionRelations[rk] ?: 0
                val push = when {
                    delta < 0 -> -1
                    delta > 0 && rel < 0 -> 1
                    else -> 0
                }
                if (push != 0) {
                    val np = (state.relationPressure[rk] ?: 0) + push
                    when {
                        np <= -6 && rel > -2 -> {
                            state.factionRelations[rk] = rel - 1
                            state.relationPressure[rk] = 0
                            pushEvent(state, "⚔️ FACTION FRICTION: ${FACTION_BY_ID[f1]?.name} and " +
                                "${FACTION_BY_ID[f2]?.name} are colder after a public spat.")
                        }
                        np >= 6 && rel < 2 -> {
                            state.factionRelations[rk] = rel + 1
                            state.relationPressure[rk] = 0
                            pushEvent(state, "⚔️ FACTION THAW: ${FACTION_BY_ID[f1]?.name} and " +
                                "${FACTION_BY_ID[f2]?.name} warmed up in public.")
                        }
                        else -> state.relationPressure[rk] = np
                    }
                }
            }
            // Player witnessing: character memory notes it (UI hook).
            val me = player(state)
            if (loc == me.location &&
                CharacterSystem.npcInside(loc) == state.playerInside) {
                onWitnessedChat?.invoke(p1, p2)
            }
            // Occasionally an NPC digs through a trash container here (flavor;
            // doesn't consume the player's loot — they find their own junk).
            if (random.nextDouble() < 0.10) {
                val cs = containersAt(loc, CharacterSystem.npcInside(loc))
                if (cs.isNotEmpty()) {
                    val who = if (random.nextBoolean()) p1 else p2
                    pushEvent(state, "🗑️ $who dug through the ${cs[0].name.lowercase()} " +
                        "and pocketed something questionable.")
                }
            }
            val cool = state.timeMin + 60
            agents[p1]!!.chatCooldownUntil = cool
            agents[p2]!!.chatCooldownUntil = cool
            if (dialogue.isNotEmpty()) state.lastConversation = dialogue
        }
    }

    // ---------------- Mock brain ----------------
    private fun mockProvide(
        state: GameState, kind: String, a: Agent, b: Agent?,
        locName: String, playerLine: String?
    ): List<Line> {
        return when (kind) {
            "collision" -> mockDialogue(a, b!!, locName, state)
            "playerSay" -> batchPresent.map { kid ->
                Line(kid.name, mockPlayerLine(state, kid, playerLine ?: ""))
            }
            "playerTalk" -> {
                val target = a
                val out = mutableListOf(Line(target.name, mockPlayerLine(state, target, playerLine ?: "")))
                val others = presentWithPlayer(state).filter { it.name != target.name && !it.asleep }
                if (others.isNotEmpty() && random.nextDouble() < 0.35) {
                    val buttIn = others[random.nextInt(others.size)]
                    out.add(Line(buttIn.name, mockPlayerLine(state, buttIn, playerLine ?: "")))
                }
                out
            }
            else -> emptyList()
        }
    }

    /**
     * Public mock fallback for the UI's LLM provider: runs the mock brain for
     * [kind] with an explicit [present] batch (used on LLM failure).
     */
    fun mockBatch(state: GameState, kind: String, a: Agent, b: Agent?, locName: String,
                  playerLine: String?, present: List<Agent>): List<Line> {
        batchPresent = present
        return try {
            mockProvide(state, kind, a, b, locName, playerLine)
        } finally {
            batchPresent = emptyList()
        }
    }

    // ---------------- v0.2.0d: freshness ring ----------------
    // Per-speaker ring of the last few ambient lines they said,
    // IN-MEMORY ONLY (it lives and dies with this Engine, i.e.
    // with the app session — saves are untouched). Ambient picks
    // steer away from a speaker's recent lines so the same kid
    // doesn't open with the same line twice in a row.
    private val heardRing = mutableMapOf<String, ArrayDeque<String>>()

    /** Pick a line from [bank] for speaker [name], avoiding the
     *  last 3 lines they produced (when the bank is big enough
     *  to allow a choice). The pick is recorded on the ring. */
    fun freshLine(name: String, bank: List<String>, rnd: Random = random): String {
        if (bank.isEmpty()) return "..."
        val ring = heardRing.getOrPut(name) { ArrayDeque() }
        val fresh = if (bank.size > 3) bank.filter { it !in ring } else emptyList()
        val pool = if (fresh.isNotEmpty()) fresh else bank
        val pick = pool[rnd.nextInt(pool.size)]
        ring.addLast(pick)
        while (ring.size > 3) ring.removeFirst()
        return pick
    }

    // v0.2.0g: pair scripts get the same freshness treatment —
    // a per-pair memory (in-memory only, like heardRing) of the
    // last script played, so a scripted pair doesn't rerun the
    // same bit back-to-back within a session. One rng draw,
    // exactly like the un-steered pick, so the daily draw
    // sequence downstream is unchanged.
    private val heardScriptRing = mutableMapOf<Set<String>, Int>()

    /** Pick a pair-script index for [pairKey] out of [count],
     *  steering away from the pair's last-played script when
     *  there is a choice. The pick is recorded on the ring. */
    fun freshScriptIndex(pairKey: Set<String>, count: Int, rnd: Random = dailyRand): Int {
        if (count <= 1) return 0
        var idx = rnd.nextInt(count)
        if (idx == heardScriptRing[pairKey]) idx = (idx + 1) % count
        heardScriptRing[pairKey] = idx
        return idx
    }

    fun mockDialogue(a1: Agent, a2: Agent, locName: String, state: GameState): List<Line> {
        val n1 = a1.name
        val n2 = a2.name
        val newkid = player(state).name
        fun fmt(t: String, other: String) = t
            .replace("{other}", other).replace("{location}", locName).replace("{newkid}", newkid)
        val key = setOf(n1, n2)
        PAIR_SCRIPTS[key]?.let { scripts ->
            val script = scripts[freshScriptIndex(key, scripts.size)]
            return script.map { (who, line) ->
                Line(who, fmt(line, if (who == n1) n2 else n1))
            }
        }
        if ("Kenny" in key) {
            val other = if (n1 == "Kenny") n2 else n1
            val kl = fmt(freshLine("Kenny", KENNY_LINES), other)
            val react = KENNY_REACTIONS[other] ?: "What did he say?"
            return if (random.nextBoolean()) {
                listOf(Line("Kenny", kl), Line(other, react))
            } else {
                val first = LINE_BANKS[other]?.let {
                    fmt(freshLine(other, it), "Kenny")
                } ?: "..."
                listOf(Line(other, first), Line("Kenny", kl))
            }
        }
        val (first, second) = if (random.nextBoolean()) a1 to a2 else a2 to a1
        return listOf(first to second, second to first).map { (selfA, otherA) ->
            val bank = LINE_BANKS[selfA.name] ?: listOf("...")
            Line(selfA.name, fmt(freshLine(selfA.name, bank), otherA.name))
        }
    }

    fun dailyTopicFor(kid: String, dayOfYear: Int): String {
        val topics = DAILY_TOPICS[kid] ?: return "..."
        return topics[dayOfYear % topics.size]
    }

    /** #17 TOPICS FOLLOW YOUR LIFE: the daily-topic pool is weighted
     *  by what this NPC remembers talking about with you, today's
     *  weather, and whether the town's factions are tense — instead
     *  of a pure date seed. */
    fun weightedTopicFor(state: GameState, kid: String): String {
        // v0.3.0e: the morning after a special sky, the sky
        // joins the topic pool — with a heavy score, so the
        // whole town is debating it for a day, then it's gone.
        val buzz = SkyState.buzzKind(state)
        val skyLines = buzz?.let { SkyTopics.topicsFor(kid, it) } ?: emptyList()
        val topics = (DAILY_TOPICS[kid] ?: return "...") + skyLines
        val skySet = skyLines.toSet()
        val tags = NpcMemoryStore.get(kid)?.topics ?: emptyList()
        val weatherWords = (WEATHER[state.weatherId]?.label ?: "")
            .lowercase().split(" ").filter { it.length > 3 }
        val tense = state.factionRelations.values.any { it <= -1 }
        fun score(t: String): Int {
            val lt = t.lowercase()
            var s = 0
            for (tag in tags) {
                if (NpcMemoryStore.topicWords(tag).any { it in lt }) s += 3
            }
            if (weatherWords.any { it in lt }) s += 2
            if (tense && ("fight" in lt || "versus" in lt || "team" in lt)) s += 1
            if (state.shenaniganId != null && "everyone" in lt) s += 1
            if (t in skySet) s += 4 // v0.3.0e: last night's sky is THE topic
            return s
        }
        val scored = topics.map { it to score(it) }
        val best = scored.maxOf { it.second }
        val pool = scored.filter { it.second >= best - 1 }.map { it.first }
        return pool[dailyRand.nextInt(pool.size)]
    }

    /** #16 THE MOCK BRAIN GETS THE SAME CONTEXT: after the event
     *  tone wrap, season the line with the speaker's memory topics
     *  and their faction's mechanical mood — the same signals the
     *  LLM prompt carries. */
    private fun finishLine(state: GameState, kid: Agent, line: String): String {
        var out = toneWrap(state, kid.name, line)
        val mem = NpcMemoryStore.get(kid.name)
        if (mem != null && mem.meets > 0 && random.nextDouble() < 0.10) {
            mem.topics.lastOrNull()?.let { topic ->
                out += " Still thinking about $topic, by the way."
            }
        }
        val fid = FACTION_OF_CAST[kid.name]
        if (fid != null && random.nextDouble() < 0.12) {
            val fs = state.factionStates[fid]
            if (fs != null) {
                val mood = Factions.moodIndex(fid, fs, state.weatherId)
                out += when {
                    mood >= 1 -> " (The ${FACTION_BY_ID[fid]?.name} are in good spirits today.)"
                    mood <= -1 -> " (The ${FACTION_BY_ID[fid]?.name} are in a mood today. Watch it.)"
                    else -> ""
                }
            }
        }
        return out
    }

    /** Wrap a finished mock line with the active shenanigans stage tone. */
    private fun toneWrap(state: GameState, speaker: String, line: String): String {
        val def = state.shenaniganId?.let { SHENANIGAN_BY_ID[it] } ?: return line
        val tone = Shenanigans.stageOf(def, state.timeMin).tone
        return Shenanigans.applyTone(line, tone, speaker)
    }

    private fun mockPlayerLine(state: GameState, kid: Agent, text: String): String {
        val newkid = player(state).name
        if (kid.asleep) {
            val lines = SLEEPY_LINES[kid.name] ?: listOf("Zzz...")
            return lines[random.nextInt(lines.size)]
        }
        val pts = state.friendship[kid.name] ?: 0
        val cat = classifyPlayerLine(text)
        // #54 FACTION COLD SHOULDER: you insulted one of theirs
        // today — the whole faction's greetings run cold.
        val kidFaction = FACTION_OF_CAST[kid.name]
        if (kidFaction != null && state.life["cold:$kidFaction"] == state.absDay &&
            cat == "greeting" && random.nextDouble() < 0.6) {
            return finishLine(state, kid,
                "Oh. It's you, $newkid. Word gets around, you know.")
        }
        // #55: wish the birthday kid properly.
        if (Birthdays.isBirthday(state, kid.name) && cat == "greeting" &&
            random.nextDouble() < 0.5) {
            return finishLine(state, kid,
                "It's my BIRTHDAY, $newkid. Cake-based gifts are being accepted.")
        }
        // #73: first-snow day is all snowball talk.
        if (state.life["firstSnowDay"] == state.absDay && random.nextDouble() < 0.12) {
            return finishLine(state, kid,
                "Truce's broken, $newkid! First snow means snowball rules are OFF.")
        }
        // #35: the neat freaks have mud opinions.
        if (state.life["mudDay"] == state.absDay &&
            kid.name in setOf("Wendy", "Butters", "Kyle") && random.nextDouble() < 0.15) {
            return finishLine(state, kid,
                "Ugh, mud season. My shoes will never recover from this, $newkid.")
        }
        // #99: the snowman's owner has opinions about the snowman.
        val snowmanOwner = state.lifeText["snowman"]?.split("|")?.getOrNull(0)
        if (snowmanOwner == kid.name && random.nextDouble() < 0.25) {
            return finishLine(state, kid,
                "Somebody built a snowman wearing MY stuff, $newkid. It has my energy, I'll give it that.")
        }
        // v0.3.0e: 🌌 WITNESSES — on a special-sky night, anyone
        // outdoors with you is looking UP, not at you.
        if (!state.playerInside) {
            val skyNow = SkyState.now(state.absDay, state.timeMin,
                state.weatherId, seasonFor(state.gameMonth))
            if (skyNow.special != SkyState.NONE && random.nextDouble() < 0.30) {
                val pool = SKY_WITNESS_LINES[skyNow.special]
                if (pool != null) return finishLine(state, kid,
                    pool[random.nextInt(pool.size)].replace("{newkid}", newkid))
            }
        }
        // #5: the stray cat's regulars have started noticing it.
        if (random.nextDouble() < 0.04) {
            return finishLine(state, kid,
                "Saw that orange cat again. It has a whole route, I swear. Fences, roofs, everything.")
        }
        // #44: full-moon nights make Towelie stranger than usual.
        if (kid.name == "Towelie" && TownLife.isFullMoon(state) &&
            isSleepHour(state.timeMin / 60) && random.nextDouble() < 0.4) {
            return finishLine(state, kid,
                "The moon's FULL full tonight, $newkid. I can feel it in my fibers.")
        }
        // RIVAL tier: being repeatedly rude has consequences.
        if (Relationships.tierOf(pts) == Relationships.RIVAL) {
            val pool = if (cat == "greeting") Relationships.RIVAL_GREETINGS
                else if (random.nextDouble() < 0.55) Relationships.RIVAL_LINES else null
            if (pool != null) return finishLine(state, kid,
                pool[random.nextInt(pool.size)].replace("{newkid}", newkid))
        }
        if (cat == "greeting") {
            // Character memory: repeat meetings greet differently (UI-wired store).
            memoryGreetingProvider(kid.name)?.let { g ->
                if (random.nextDouble() < 0.6) return finishLine(state, kid,
                    g.replace("{newkid}", newkid))
            }
            val tier = friendGreetTier(pts)
            val greets = FRIEND_GREETINGS[kid.name]?.get(tier)
            if (!greets.isNullOrEmpty()) return finishLine(state, kid,
                greets[random.nextInt(greets.size)].replace("{newkid}", newkid))
        }
        // Shenanigans talk: NPCs reference the event + each other's doings.
        val def = state.shenaniganId?.let { SHENANIGAN_BY_ID[it] }
        if (def != null && random.nextDouble() < 0.22) {
            val st = Shenanigans.stageOf(def, state.timeMin)
            return finishLine(state, kid,
                "This whole '${def.title}' thing? ${st.journalNote.substringBefore(".")}. " +
                    "Wild, right, $newkid?")
        }
        if (random.nextDouble() < 0.12) {
            val yap = kid.memories.lastOrNull { it.startsWith("Yapped with") }
            if (yap != null) return finishLine(state, kid,
                "I was just talking to ${yap.removePrefix("Yapped with ").substringBefore(" at")} " +
                    "before you showed up, $newkid. Town's small, dude.")
        }
        // #37: morning small talk plans around the forecast.
        if (state.timeMin < 10 * 60 && random.nextDouble() < 0.10) {
            val fc = forecastWeather(state)
            if (fc != state.weatherId) {
                return finishLine(state, kid,
                    "Supposed to turn to ${WEATHER[fc]?.label?.lowercase()} later, $newkid. Plan accordingly.")
            }
        }
        if (random.nextDouble() < 0.10) {
            WEATHER_QUIPS[state.weatherId]?.get(kid.name)?.let {
                return finishLine(state, kid, it.replace("{newkid}", newkid))
            }
        }
        val holiday = activeHoliday
        if (holiday != null && random.nextDouble() < 0.25) {
            val hl = HOLIDAY_LINES[holiday.id]?.get(kid.name)
            if (!hl.isNullOrEmpty()) return finishLine(state, kid,
                hl[random.nextInt(hl.size)].replace("{newkid}", newkid))
        }
        // v0.1.9e TRADITION BEATS: NPCs without scripted holiday
        // lines still have holiday things to say.
        Traditions.of(state)?.let { t ->
            if (random.nextDouble() < 0.22) return finishLine(state, kid,
                t.beats[random.nextInt(t.beats.size)].replace("{newkid}", newkid))
        }
        // v0.1.9e THREADS: from the second real conversation on,
        // people ask after the last thing you talked about — a
        // third chat with someone reads different from the first.
        if (random.nextDouble() < 0.16) {
            NpcMemoryStore.get(kid.name)?.let { mem ->
                Threads.followUp(mem, random)?.let { fu ->
                    return finishLine(state, kid, fu)
                }
            }
        }
        // v0.1.9e GOALS IN GREETINGS: people mention what
        // they're currently up to.
        if (random.nextDouble() < 0.13) {
            val g = NpcGoals.record(state, kid.name)
            if (!g.done) return finishLine(state, kid, NpcGoals.mention(g))
        }
        if (pts >= 50 && dailyRand.nextDouble() < 0.40) {
            return finishLine(state, kid,
                weightedTopicFor(state, kid.name).replace("{newkid}", newkid))
        }
        val bank = RESPONSE_BANKS[kid.name] ?: RESPONSE_BANKS["Stan"]!!
        val lines = bank[cat] ?: bank["default"]!!
        return finishLine(state, kid,
            lines[random.nextInt(lines.size)].replace("{newkid}", newkid))
    }

    private fun dayOfYearOf(state: GameState): Int {
        // todayKey = yyyyMMdd; approximate day-of-year from month/day.
        return try {
            val m = state.todayKey.substring(4, 6).toInt()
            val d = state.todayKey.substring(6, 8).toInt()
            intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)[m - 1] + d
        } catch (e: Exception) { 1 }
    }

    fun mockPlayerResponse(charName: String, playerText: String, newkidName: String): String {
        val cat = classifyPlayerLine(playerText)
        val bank = RESPONSE_BANKS[charName] ?: RESPONSE_BANKS["Stan"]!!
        val lines = bank[cat] ?: bank["default"]!!
        return lines[random.nextInt(lines.size)].replace("{newkid}", newkidName)
    }

    // ---------------- LLM prompt builders (UI sends these to GeminiClient) ----------------
    fun buildCollisionPrompt(a1: Agent, a2: Agent, locName: String, timeStr: String,
                             weather: String, critter: String,
                             state: GameState? = null): String {
        var scene = "$locName, $timeStr. $weather."
        if (critter.isNotEmpty()) scene += " $critter"
        val rel = relationNote(a1.name, a2.name)?.let { " They are $it." } ?: ""
        // v0.1.9e THREAD + GOAL CONTEXT: the LLM gets the same
        // continuity the mock brain seasons its lines with
        // (Phase-1 #16 parity).
        var thread = ""
        if (state != null) {
            val bond = state.npcBonds[bondKey(a1.name, a2.name)] ?: 0
            Threads.pairTopic(state, a1.name, a2.name)?.let { topic ->
                thread += "\nCONTINUITY: ${a1.name} and ${a2.name} last talked about " +
                    "$topic — one of them may reference it."
            }
            if (bond >= 4) thread += "\nRUNNING JOKE between them: " +
                "\"${Threads.pairJoke(a1.name, a2.name)}\" — it may come up."
            val g1 = NpcGoals.record(state, a1.name)
            if (!g1.done) thread += "\n${a1.name} is currently: ${g1.text}"
            val g2 = NpcGoals.record(state, a2.name)
            if (!g2.done) thread += "\n${a2.name} is currently: ${g2.text}"
        }
        // v0.2.0d SPEECH RULES: each speaker's voice card (tics,
        // habits, never-says, deflection) so the LLM holds voice
        // the way the mock brain's line banks do.
        val voices = speechRulesBlock(listOf(a1.name, a2.name))
        return "$VOICE_DIRECTION\n\n" +
            "SCENE: $scene\n" +
            "${a1.name}: ${a1.description}\n" +
            "${a2.name}: ${a2.description}$rel$thread\n" +
            (if (voices.isNotEmpty()) "$voices\n" else "") + "\n" +
            "${a1.name} and ${a2.name} just bumped into each other. Write 4-6 short " +
            "lines of dialogue between them, alternating speakers. They address each other " +
            "by name. Format each line exactly as: NAME: line"
    }

    fun buildPlayerPrompt(state: GameState, present: List<Agent>, playerLine: String,
                          locName: String, timeStr: String, weather: String): String {
        val me = player(state)
        val cast = present.joinToString("\n") {
            "${it.name}: ${it.description} (Relationship with ${me.name}: " +
                "${Relationships.tierLabel(state.friendship[it.name] ?: 0)}.)"
        }
        val names = present.joinToString(", ") { it.name }
        // v0.1.9e: goal context for everyone present (thread
        // context rides in via the memory block — promptBlock
        // carries the same hints the mock brain uses, #16 parity).
        val threads = present.joinToString("") { p ->
            val g = NpcGoals.record(state, p.name)
            if (!g.done) "\n${p.name} is currently: ${g.text}" else ""
        }
        // v0.2.0d SPEECH RULES for everyone present (cast only —
        // generated NPCs carry their lorebook entry instead).
        val voices = speechRulesBlock(present.map { it.name })
        return "$VOICE_DIRECTION\n\n" +
            "SCENE: $locName, $timeStr. $weather.\n" +
            "${me.name} (${me.description}) just walked up and said: \"$playerLine\"\n" +
            "Present: $names.\n$cast$threads\n" +
            (if (voices.isNotEmpty()) "$voices\n" else "") + "\n" +
            "Write each present kid's in-character reply (1-2 lines each). Cartman calls " +
            "${me.name} 'Douchebag' as a not-so-affectionate nickname; the others use his " +
            "real name. Format each line exactly as: NAME: line"
    }

    fun parseLlmLines(text: String?, validNames: List<String>): List<Line> {
        val out = mutableListOf<Line>()
        val lower = validNames.associateBy { it.lowercase() }
        val re = Regex("^([^:]{1,24}):\\s*(.+)$")
        for (raw in (text ?: "").lines()) {
            var r = raw.trim().trim('*').trim()
            if (r.isEmpty()) continue
            val m = re.matchEntire(r) ?: continue
            val who = m.groupValues[1].trim()
            val said = m.groupValues[2].trim()
            val canon = lower[who.lowercase()]
            if (canon != null && said.isNotEmpty()) {
                out.add(Line(canon, said.take(280)))
            }
            if (out.size >= 8) break
        }
        return out
    }

    // ---------------- Player API ----------------
    fun findLocation(query: String?): String? {
        val q = (query ?: "").trim().lowercase().replace("'", "")
        if (q.isEmpty()) return null
        LOCATION_ALIASES[q]?.let { return it }
        for ((key, loc) in LOCATIONS) {
            if (q == key.lowercase() || q == loc.name.lowercase()) return key
        }
        for ((key, loc) in LOCATIONS) {
            if (q in key.lowercase() || q in loc.name.lowercase()) return key
        }
        return null
    }

    fun playerGo(state: GameState, where: String): Pair<Boolean, String> {
        val me = player(state)
        val key = findLocation(where)
            ?: return false to "🤷 Never heard of '$where'. Try the glowing arrows, dude."
        if (key == me.location) return true to "You're already at ${LOCATIONS[key]!!.name}, dude."
        me.location = key
        me.destination = key
        me.waypoints.clear()
        me.coords = LOCATIONS[key]!!.let { it.gx to it.gy }
        me.activity = "Just got here."
        state.playerInside = false // travel always lands you outside, street view
        // #1 data: where the player haunts (Close Friends bend toward it)
        state.locVisits[key] = (state.locVisits[key] ?: 0) + 1
        pushEvent(state, "📍 ${me.name} walked into ${LOCATIONS[key]!!.name}.")
        return true to "🚶 ${me.name} heads to ${LOCATIONS[key]!!.name}. (${weatherDesc(state)})"
    }

    fun playerLook(state: GameState, season: Season? = null, holiday: Holiday? = null): String {
        val me = player(state)
        if (me.waypoints.isNotEmpty()) {
            return "🚶 You're on the road to ${LOCATIONS[me.destination]!!.name}. ${weatherDesc(state)}"
        }
        val loc = LOCATIONS[me.location]!!
        val bits = mutableListOf(
            "📍 ${loc.name} — ${minutesToTimeStr(state.timeMin)}",
            loc.blurb,
            weatherDesc(state)
        )
        season?.let { bits.add(SEASON_LINES[it] ?: "") }
        holiday?.let { bits.add("🎃 It's ${it.name} in South Park. ${holidayFlavor(it)}") }
        // v0.3.0e: the sky gets its line when it's doing
        // something — outdoors only; roofs don't count.
        if (!state.playerInside) {
            val skyNow = SkyState.now(state.absDay, state.timeMin,
                state.weatherId, season ?: seasonFor(state.gameMonth))
            SkyState.lookLine(skyNow.special)?.let { bits.add(it) }
        }
        // v0.1.9e: the tradition's dressing, written for THIS
        // place — Look describes the gathering, not just a banner.
        Traditions.dressingLine(state, me.location)?.let { bits.add(it) }
        val here = state.agents.values.filter {
            !it.isPlayer && it.location == me.location && it.waypoints.isEmpty() &&
                CharacterSystem.npcInside(it.location) == state.playerInside
        }
        if (state.playerInside) {
            bits.add("🏠 Inside: ${interiorOf(me.location)?.name ?: loc.name}")
        }
        if (here.isNotEmpty()) {
            for (a in here) bits.add("• ${a.name} — ${a.activity}")
        } else {
            bits.add("Nobody else around. Just you and the crushing silence of Colorado.")
        }
        // #6 LOOK IS A LIVING READOUT: whose turf, what mood they're
        // in, whether the event is HERE, and the forecast.
        val turf = FACTIONS.firstOrNull { me.location in it.turf }
        if (turf != null) {
            val fs = state.factionStates[turf.id]
            val moodTxt = fs?.let {
                Factions.moodLabel(Factions.moodIndex(turf.id, it, state.weatherId))
            } ?: "even-keeled"
            bits.add("⚔️ Turf: ${turf.name} — $moodTxt today" +
                (fs?.let { " (influence ${it.influence})" } ?: "") + ".")
        }
        val activeDef = state.shenaniganId?.let { SHENANIGAN_BY_ID[it] }
        if (activeDef != null) {
            val stg = Shenanigans.stageOf(activeDef, state.timeMin)
            if (stg.clusterLoc == me.location) {
                bits.add("🎪 The event is HERE: ${stg.objective}")
            }
        }
        // #38: the last event's mark stays on its turf.
        state.lifeText["mark:${me.location}"]?.let { bits.add("🗿 $it") }
        // #57 RIVAL PRANKS: found once, at your favorite haunt.
        state.lifeText["prank:${me.location}"]?.let {
            bits.add("💢 $it (You sit down VERY carefully for a week.)")
            state.lifeText.remove("prank:${me.location}")
        }
        // #55: it's someone's birthday — say it with cake.
        // v0.15.0i: a shared day names them all.
        val bdNames = Birthdays.birthdaysToday(state)
        if (bdNames.size == 1) {
            bits.add("🎂 It's ${bdNames[0]}'s birthday today. There will be cake opinions.")
        } else if (bdNames.size > 1) {
            bits.add("🎂 It's ${Birthdays.joinNames(bdNames)}'s birthday today — all of them. There will be cake opinions.")
        }
        // #64 LOST & FOUND: today's lost thing waits at City Hall.
        if (me.location == "CityHall" && state.lifeText["lostClaimed"] != state.todayKey) {
            TownLife.lostToday(state)?.let { (owner, itemId, _) ->
                bits.add("📦 Lost & found: somebody turned in a " +
                    "${itemById(itemId)?.name ?: itemId}. Tag says it belongs to $owner.")
            }
        }
        val fc = forecastWeather(state)
        if (fc != state.weatherId) {
            bits.add("🔮 Forecast: likely turning to ${WEATHER[fc]?.label ?: fc} later.")
        }
        val crit = animalFlavor(state, me.location)
        if (crit.isNotEmpty()) bits.add("🐾 $crit")
        val shiny = state.spawns.filter { it.locationId == me.location && it.collectibleId !in state.collected }
        if (shiny.isNotEmpty()) bits.add("✨ Something shiny is lying around here... (${shiny.size})")
        return bits.joinToString("\n")
    }

    /** v0.3.0e: 🌠 THE WISH. On a meteor night, Looking at
     *  Stark's Pond makes a wish — no picking, no menu: the
     *  sky does the work. Once a night (the pond enforces it
     *  politely), counted forever in life["wishes"] for the
     *  Journal's 🎮 GAMES page. Whoever's standing at the pond
     *  with you wishes too — out loud, usually. Returns the
     *  beat, or null when tonight isn't a wish night, you're
     *  not at the pond, or you're inside the hut. */
    fun pondWish(state: GameState): String? {
        val me = player(state)
        if (me.location != Minigames.POND_LOC || state.playerInside) return null
        if (!SkyState.meteorNow(state.absDay, state.timeMin,
                state.weatherId, seasonFor(state.gameMonth))) return null
        if (state.life["wishNight"] == state.absDay) {
            return "☄️ You already wished tonight. The pond is holding " +
                "it for you — one per customer, sky rules."
        }
        state.life["wishNight"] = state.absDay
        state.life["wishes"] = (state.life["wishes"] ?: 0) + 1
        val n = state.life["wishes"] ?: 1
        val companion = presentWithPlayer(state)
            .firstOrNull { !it.asleep }?.name
        return if (companion != null) {
            "☄️ A meteor rips the sky in half overhead. $companion " +
                "gasps and wishes OUT LOUD, then claps both hands over " +
                "their mouth. Yours, you keep quiet. The pond keeps " +
                "both. (Wish #$n.)"
        } else {
            "☄️ A meteor tears across the sky. Alone at the pond, you " +
                "close your eyes and wish — properly, with your whole " +
                "chest. The water doesn't ripple. It doesn't have to. " +
                "(Wish #$n.)"
        }
    }

    private fun holidayFlavor(h: Holiday): String = when (h.id) {
        "halloween" -> "Crude pumpkins everywhere. Kenny's wearing a cape."
        "christmas" -> "Tacky lights on everything. The critters are multiplying."
        "newyear" -> "Somebody's already lighting fireworks. It's Randy."
        "valentine" -> "Paper hearts taped to everything. Kenny made you one."
        "july4" -> "Bunting and fireworks. Randy's in charge. Pray."
        "cowdays" -> "The whole town is mooing. Don't fight it."
        else -> ""
    }

    /** Who shares the player's exact space: same location AND same
     *  inside/outside side of the door (v0.1.5 interiors). */
    fun presentWithPlayer(state: GameState): List<Agent> {
        val me = player(state)
        return state.agents.values.filter {
            !it.isPlayer && it.location == me.location && it.waypoints.isEmpty() &&
                CharacterSystem.npcInside(it.location) == state.playerInside
        }
    }

    fun playerSay(state: GameState, text: String): List<Line> {
        val me = player(state)
        val clean = text.trim()
        if (clean.isEmpty()) return emptyList()
        val present = presentWithPlayer(state)
        if (present.isEmpty()) {
            val lonely = ANIMAL_SPECIES.keys.toList()[random.nextInt(ANIMAL_SPECIES.size)]
            val act = ANIMAL_SPECIES[lonely]!!.actions.let { it[random.nextInt(it.size)] }
            return listOf(Line("🌲",
                "You say \"$clean\" to the empty ${LOCATIONS[me.location]!!.name}. " +
                    "A $lonely $act. It understands you better than Cartman would."))
        }
        batchPresent = present
        val lines = invokeProvider(state, "playerSay", me, null,
            LOCATIONS[me.location]!!.name, clean)
        batchPresent = emptyList()
        Speak.speak(lines)
        val responders = lines.map { it.speaker }.toSet()
        val rude = classifyPlayerLine(clean) == "insult"
        for (kid in present) {
            if (kid.name in responders && !kid.asleep) {
                if (rude) relAdjust(state, kid.name, -6) else addFriendship(state, kid.name, clean)
            } else if (!kid.asleep && lines.isNotEmpty()) {
                // #13 COMPANY COUNTS: friends standing nearby share a
                // little of the moment's credit (or the insult's sting).
                relAdjust(state, kid.name, if (rude) -1 else 1)
            }
            kid.memories.add("${me.name} said: '${clean.take(60)}' at ${LOCATIONS[me.location]!!.name}.")
        }
        if (rude) TownLife.repAdd(state, -1) // #52: yelling costs rep
        // #33 participation + #31 holiday talk, counted where you stand
        bumpParticipation(state, me.location)
        if (state.holidayId != null) {
            state.questCounters["holidayTalks"] = (state.questCounters["holidayTalks"] ?: 0) + 1
        }
        me.memories.add("Said '${clean.take(60)}' at ${LOCATIONS[me.location]!!.name}.")
        if (lines.isNotEmpty()) {
            state.lastConversation = listOf(Line(me.name, clean)) + lines
        }
        return lines
    }

    fun playerTalk(state: GameState, target: String, text: String): List<Line> {
        val me = player(state)
        val clean = text.trim()
        if (clean.isEmpty()) return emptyList()
        val tkey = state.agents.keys.firstOrNull {
            it.equals(target.trim(), ignoreCase = true) && !state.agents[it]!!.isPlayer
        } ?: return listOf(Line("🌲", "There's no '$target' here, dude. Check 'who'."))
        val them = state.agents[tkey]!!
        if (them.asleep) {
            // #14 HOME HOURS: waking someone at home after bedtime
            // costs you — sleep pose, annoyed sleeper, real points.
            if (them.location == KID_HOUSE[tkey] && state.playerInside) {
                relAdjust(state, tkey, -2)
            }
            val lines = SLEEPY_LINES[tkey] ?: listOf("Zzz...")
            return listOf(Line(tkey, lines[random.nextInt(lines.size)]))
        }
        if (them.location != me.location || them.waypoints.isNotEmpty()) {
            return listOf(Line("🌲",
                "$tkey isn't here right now — last seen at ${LOCATIONS[them.location]!!.name}. Walk over first."))
        }
        if (CharacterSystem.npcInside(them.location) != state.playerInside) {
            return listOf(Line("🌲",
                if (CharacterSystem.npcInside(them.location))
                    "$tkey is inside ${LOCATIONS[them.location]!!.name} — tap the door to head in."
                else "$tkey is outside — step out through the door to reach them."))
        }
        val lines = invokeProvider(state, "playerTalk", them, null,
            LOCATIONS[me.location]!!.name, clean).toMutableList()
        Speak.speak(lines)
        if (lines.any { it.speaker == tkey }) {
            if (classifyPlayerLine(clean) == "insult") {
                relAdjust(state, tkey, -6)
                TownLife.repAdd(state, -1) // #52: rudeness is reputation
            } else addFriendship(state, tkey, clean)
            // #14 HOME HOURS: a daytime visit in their own home pays
            // a small hospitality bonus — home turf, home rules.
            val hour = state.timeMin / 60
            if (them.location == KID_HOUSE[tkey] && state.playerInside && hour in 8..20) {
                relAdjust(state, tkey, 2)
            }
            // #13 COMPANY COUNTS for whoever else is standing here.
            for (other in presentWithPlayer(state)) {
                if (other.name != tkey && !other.asleep) relAdjust(state, other.name, 1)
            }
            // #15 HANGOUTS UNLOCK DEPTH: settled in together inside,
            // their deeper daily topic surfaces a tier early.
            if (isHangout(state, them) && lines.none { it.speaker == "🌲" }) {
                val pts = state.friendship[tkey] ?: 0
                if (Relationships.tierOf(pts) >= Relationships.ACQUAINTANCE &&
                    Relationships.tierOf(pts) < Relationships.FRIEND) {
                    lines.add(Line(tkey, dailyTopicFor(tkey, dayOfYearOf(state))
                        .replace("{newkid}", me.name)))
                }
            }
        }
        // #58 WINGMAN EFFECT: their best friend, standing right
        // here, chimes in with a bonus line.
        val bestFriend = relationsOf(tkey).firstOrNull()
        if (bestFriend != null && lines.any { it.speaker == tkey }) {
            val bfAgent = presentWithPlayer(state)
                .firstOrNull { it.name == bestFriend }
            if (bfAgent != null && !bfAgent.asleep && random.nextDouble() < 0.5) {
                lines.add(Line(bestFriend,
                    WINGMAN_LINES[random.nextInt(WINGMAN_LINES.size)]
                        .replace("{name}", tkey).replace("{newkid}", me.name)))
            }
        }
        bumpParticipation(state, me.location)
        // v0.3.0c: a real talk with an arc person is a nudge —
        // the storyline notices you showed up for them.
        StoryArcs.noteInteraction(state, tkey, "talk")
        if (state.holidayId != null) {
            state.questCounters["holidayTalks"] = (state.questCounters["holidayTalks"] ?: 0) + 1
        }
        // Close friends sometimes hand YOU a gift.
        if (Relationships.tierOf(state.friendship[tkey] ?: 0) == Relationships.CLOSE_FRIEND &&
            random.nextDouble() < 0.18) {
            val giftPool = ITEMS.filter { it.tier == "gift" || it.tier == "junk" }
            val g = giftPool[random.nextInt(giftPool.size)]
            state.inventory[g.id] = (state.inventory[g.id] ?: 0) + 1
            lines.add(Line(tkey, "Oh wait — I got you something, ${me.name}. Here: ${g.icon} ${g.name}!"))
            pushEvent(state, "🎁 $tkey gave ${me.name} a ${g.name}!")
        }
        if (lines.isNotEmpty()) {
            state.lastConversation = listOf(Line(me.name, clean)) + lines
        }
        return lines
    }

    /**
     * v0.1.5 relationship adjustment (gifts, insults, events): can move
     * points DOWN as well as up, clamps to -50..100, and a tier change
     * nudges the NPC's faction standing with the player + posts an event.
     */
    fun relAdjust(state: GameState, npcName: String, delta: Int) {
        val cur = state.friendship[npcName] ?: 0
        val nv = (cur + delta).coerceIn(-50, 100)
        // #54 FACTION COLD SHOULDER: a real insult today chills the
        // whole faction's greetings toward you until tomorrow.
        if (delta <= -5) {
            FACTION_OF_CAST[npcName]?.let { fid ->
                state.life["cold:$fid"] = state.absDay
            }
        }
        if (nv == cur) return
        val before = Relationships.tierOf(cur)
        state.friendship[npcName] = nv
        val after = Relationships.tierOf(nv)
        if (after != before) {
            pushEvent(state, "💛 $npcName now sees you as a ${Relationships.tierLabel(nv)}.")
            FACTION_OF_CAST[npcName]?.let { fid ->
                state.factionStates[fid]?.let { fs ->
                    fs.playerStanding = (fs.playerStanding +
                        if (after > before) 3 else -3).coerceIn(-50, 100)
                }
            }
            // #11 MEMORIES TRAVEL BETWEEN FRIENDS: crossing INTO
            // Friend tier, your stories reach their bonded friends —
            // one of your shared topics lands in their memory, so a
            // first meeting with them can start warm.
            if (after >= Relationships.FRIEND && after > before) {
                val topic = NpcMemoryStore.get(npcName)?.topics?.lastOrNull()
                if (topic != null) {
                    for (friend in relationsOf(npcName)) {
                        val fm = NpcMemoryStore.getOrCreate(friend)
                        fm.topics.remove(topic)
                        fm.topics.add(topic)
                        while (fm.topics.size > 6) fm.topics.removeAt(0)
                    }
                    pushEvent(state, "🗣️ Word travels — $npcName's friends have heard about you.")
                }
            }
        }
    }

    /** #15: settled inside a furnished room together, on the NPC's
     *  own time — the hangout state. */
    fun isHangout(state: GameState, a: Agent): Boolean {
        if (!state.playerInside) return false
        val def = interiorOf(a.location) ?: return false
        if (def.theme !in setOf("home", "cafe", "restaurant", "diner", "loft", "bar", "hut")) {
            return false
        }
        val block = effectiveAgendaBlock(state, a, state.timeMin) ?: return false
        return block.location == a.location
    }

    /** Cast keepers of enterable places (#29 PLACES KEEP HOURS). */
    val KEEPERS: Map<String, String> = mapOf(
        "TweekBros" to "Tweek", "JimbosGuns" to "Jimbo",
        "Elementary" to "Garrison", "Church" to "Father Maxi",
        "TegridyFarms" to "Randy"
    )

    fun shopKeeper(locId: String): String? = KEEPERS[locId]

    /** Null = open (or no keeper). Otherwise the door's story: why
     *  it's shut, where the keeper is, and where they live. */
    fun shopClosedReason(state: GameState, locId: String): String? {
        val keeper = KEEPERS[locId] ?: return null
        val agent = state.agents[keeper] ?: return null
        val block = effectiveAgendaBlock(state, agent, state.timeMin)
        val atWork = block?.location == locId || agent.location == locId ||
            agent.destination == locId
        if (atWork && !agent.asleep) return null
        val where = LOCATIONS[agent.location]?.name ?: agent.location
        val home = KID_HOUSE[keeper]?.let { LOCATIONS[it]?.name } ?: "home"
        return "🔒 Closed — $keeper isn't in. They're over at $where; they live at $home. " +
            "Hours: while $keeper is here."
    }

    // ---------------- Friendship (cozy: gains only, never decay) ----------------
    fun addFriendship(state: GameState, kid: String, playerText: String) {
        val gained = state.dailyGain.getOrDefault(kid, 0)
        if (gained >= 20) return
        var add = if (gained == 0) 12 else 4
        if (classifyPlayerLine(playerText) == "compliment") add += 6
        add = min(add, 20 - gained)
        state.friendship[kid] = (state.friendship[kid] ?: 0) + add
        state.dailyGain[kid] = gained + add
    }

    // ---------------- Collectibles: 5 spawns/day, seeded by todayKey ----------------
    fun spawnCollectibles(state: GameState, monthDay: String, todayKey: String) {
        if (state.spawnDate == todayKey) return
        state.spawnDate = todayKey
        state.spawns.clear()
        val rand = Random(dailySeed(todayKey))
        val holiday = currentHoliday(monthDay)
        val pool = COLLECTIBLES.filter { !it.christmasOnly || holiday?.id == "christmas" }
        val locIds = LOCATIONS.keys.toList()
        // #44 SHINIES FOLLOW THE LIFE: placement favors the day's real
        // foot traffic (agenda blocks across the whole cast), so
        // treasure gathers where the town gathers.
        val traffic = mutableMapOf<String, Int>()
        for ((_, a) in state.agents) {
            if (a.isPlayer) continue
            for ((_, blk) in a.agendaBase) {
                traffic[blk.location] = (traffic[blk.location] ?: 0) + 1
            }
        }
        val weights = locIds.map { 1.0 + (traffic[it] ?: 0) / 4.0 }
        val totalW = weights.sum()
        repeat(5) { i ->
            val c = pool[rand.nextInt(pool.size)]
            var roll = rand.nextDouble() * totalW
            var loc = locIds.last()
            for (j in locIds.indices) {
                roll -= weights[j]
                if (roll <= 0) { loc = locIds[j]; break }
            }
            state.spawns.add(Spawn("s${todayKey}_$i", c.id, loc))
        }
    }

    /** Returns (collected collectible or null, message). Crab Person wiggles away on first tap. */
    fun collectSpawn(state: GameState, spawnId: String): Pair<Collectible?, String> {
        val s = state.spawns.firstOrNull { it.id == spawnId }
            ?: return null to "Huh. It's gone. Like your motivation."
        val c = collectibleById(s.collectibleId)
            ?: return null to "Weird. That wasn't supposed to exist."
        if (c.id == "crab_person" && !s.wiggled) {
            s.wiggled = true
            return null to "The Crab Person scuttles sideways with a wiggle! Quick, tap it again!"
        }
        state.spawns.remove(s)
        state.collected.add(c.id)
        // v0.1.5: collectibles fold into the inventory system too.
        state.inventory[c.id] = (state.inventory[c.id] ?: 0) + 1
        state.questCounters["shinies"] = (state.questCounters["shinies"] ?: 0) + 1
        pushEvent(state, "✨ ${player(state).name} found ${c.icon} ${c.name}!")
        return c to "Got ${c.icon} ${c.name}! ${c.flavor}"
    }

    // ---------------- Trash diving + litter + gifting (v0.1.5) ----------------

    fun trashAvailable(state: GameState, containerId: String): Boolean =
        state.trashSearched[containerId] != state.todayKey

    /** Search a container: empty until the next in-game day refreshes it. */
    fun searchTrash(state: GameState, container: TrashContainerDef): Pair<ItemDef?, String> {
        if (!trashAvailable(state, container.id)) {
            return null to "Picked clean. The town's trash refreshes with the new day."
        }
        state.trashSearched[container.id] = state.todayKey
        state.questCounters["trash"] = (state.questCounters["trash"] ?: 0) + 1
        val ck2 = "clean:${container.locId}"
        state.life[ck2] = (state.life[ck2] ?: 0) + 1
        bumpParticipation(state, container.locId)
        val big = container.kind == "dumpster" || container.kind == "pile"
        // #70: a lucky coin in the pond blesses today's treasure odds
        val eventBoost = state.shenaniganId != null ||
            state.life["luckDay"] == state.absDay
        val rand = Random(state.todayKey.hashCode().toLong() * 31 + container.id.hashCode())
        // #5 TRASH FEEDS THE FACTIONS: on a faction's turf, the
        // table bends toward that faction's flavor of find.
        val turfFaction = FACTIONS.firstOrNull { container.locId in it.turf }
        val flavored = if (turfFaction != null && rand.nextDouble() < 0.30)
            TURF_LOOT[turfFaction.id]?.let { it[rand.nextInt(it.size)] } else null
        val itemId = flavored?.takeIf { itemById(it) != null }
            ?: rollContainerLoot(rand, big, eventBoost)
            ?: return null to "You dig through the ${container.name.lowercase()}... nothing but regrets."
        val item = itemById(itemId) ?: return null to "You find... something unidentifiable."
        state.inventory[item.id] = (state.inventory[item.id] ?: 0) + 1
        pushEvent(state, "🗑️ ${player(state).name} dumpster-dived a ${item.icon} ${item.name}!")
        return item to "You found ${item.icon} ${item.name}! ${item.desc}"
    }

    /** Pick up a piece of ground litter. */
    fun pickupLitter(state: GameState, litterId: String): Pair<ItemDef?, String> {
        val drop = state.litter.firstOrNull { it.id == litterId }
            ?: return null to "It's gone. The wind took it."
        state.litter.remove(drop)
        state.questCounters["litter"] = (state.questCounters["litter"] ?: 0) + 1
        // #40: the town remembers where you clean — and stays cleaner
        val ck = "clean:${drop.locId}"
        state.life[ck] = (state.life[ck] ?: 0) + 1
        TownLife.repAdd(state, 1) // #52
        val foundId = rollLitterFind(random, drop.itemId)
        val item = itemById(foundId) ?: return null to "You pick it up. It's trash."
        state.inventory[item.id] = (state.inventory[item.id] ?: 0) + 1
        return if (foundId == drop.itemId) {
            item to "Picked up ${item.icon} ${item.name}. One person's trash..."
        } else {
            item to "Under the trash: ${item.icon} ${item.name}! Somebody dropped something good!"
        }
    }

    /** Give an inventory item to a CAST npc. Returns a result line. */
    fun giveGift(state: GameState, npcName: String, itemId: String): String {
        val item = itemById(itemId) ?: return "You don't have that."
        val count = state.inventory[itemId] ?: 0
        if (count <= 0) return "You don't have a ${item.name}."
        state.inventory[itemId] = count - 1
        if (state.inventory[itemId] == 0) state.inventory.remove(itemId)
        val (basePts, loved) = giftValue(item, npcName, emptyList())
        // v0.12.0c: the birthday dial — a gift handed to
        // the birthday person ON their day lands warmer by
        // a stated, flat bonus inside this same math; the
        // item is consumed exactly as any gift is, and an
        // off-day gift is untouched (pinned both ways).
        val birthday = Birthdays.isBirthday(state, npcName)
        val pts = basePts + if (birthday) Birthdays.GIFT_BONUS else 0
        relAdjust(state, npcName, pts)
        state.questCounters["gifts"] = (state.questCounters["gifts"] ?: 0) + 1
        // v0.3.0c: a gift to an arc person nudges their storyline
        StoryArcs.noteInteraction(state, npcName, "gift")
        bumpParticipation(state, player(state).location)
        TownLife.repAdd(state, 1) // #52: generosity is reputation too
        pushEvent(state, "🎁 ${player(state).name} gave $npcName a ${item.name}" +
            if (loved) " — they LOVED it!" else ".")
        return if (loved) "$npcName's eyes light up — they LOVE the ${item.name}! (+$pts)"
        else if (birthday) "$npcName takes the ${item.name}. " +
            "(+$pts relationship — 🎂 it's their birthday!)"
        else "$npcName takes the ${item.name}. (+$pts relationship)"
    }

    // ---------------- Character creator wiring ----------------
    fun createPlayer(state: GameState, name: String, appearance: Map<String, String>): Agent {
        val cleanName = name.trim().ifEmpty { PLAYER_DEFAULT_NAME }
        val me = player(state)
        val oldKey = state.agents.entries.first { it.value.isPlayer }.key
        val app = validateAppearance(appearance)
        me.name = cleanName
        me.appearance = app
        me.description = "$cleanName, the new kid in South Park (${describeAppearance(app)}). " +
            "Walks everywhere with a heavy backpack, up for anything, friends with " +
            "everybody. The kids call him the new kid and are still deciding if he's cool."
        me.activity = "Just rolled into town."
        if (oldKey != cleanName) {
            state.agents[cleanName] = state.agents.remove(oldKey)!!
        }
        pushEvent(state, "🌟 $cleanName just moved to South Park. The town will never recover.")
        return me
    }
}
