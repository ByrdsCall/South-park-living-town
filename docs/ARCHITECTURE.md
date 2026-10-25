# Architecture — South Park: Living Town

How the tree is laid out and why. The tuning ledger lives in
`docs/TUNING.md`; the changelog in `docs/CHANGELOG.md`; the
invariants in `docs/INVARIANTS.md`.

## Source layout
`src/com/hunt3r/southparktown/` — 133 Kotlin files, zero dependencies.
Pure Kotlin (no android.* imports, JVM-testable): `SoundGen.kt` (all
synthesized audio: SFX, voice blips, ambience, the town tune), `Barks.kt`
(v0.4.0e: the greeting-bark pattern table for all 44 cast, the trigger
rules — presence weighting, cooldowns, quiet hours, volume shaping —
and the hello-bubble geometry), `GameData.kt`,
`CastData.kt`, `Engine.kt`, `Clock.kt`, `GeminiClient.kt`, `LlmClient.kt`,
`LoreBook.kt`, `LoreAliases.kt`, `CharacterSystem.kt`, `NpcGen.kt`,
`NpcMemory.kt`, `Shenanigans.kt`, `Factions.kt`, `Items.kt`, `Quests.kt`,
`Goals.kt`, `Errands.kt` (v0.4.0b: small work for townsfolk — the City
Hall request board as a pure function of the day, goal-driven direct
asks, shopkeeper trade asks; six types each judged by an existing verb:
Give/Talk/Look/fixture taps/selfie/minigame settles; capped plate,
additive save keys; v0.6.0b: favor chains — generated asks stamped as
2–3-link chain heads, the tail walking beneficiary-to-relation, one
plate slot per chain, reveal-time revalidation with substitution, a
closing bonus from the origin asker, and the town's gossip about it), `HolidayBeats.kt` (v0.4.0d: one signature playable beat
per holiday — Talk/Look/Give/selfie/catch rails judge the steps, completion
marks live in capped per-(holiday, year) keys in the `life` map),
`Threads.kt`, `Episodes.kt`, `Mystery.kt` (the season arc), `Mystery2.kt` (the
second case: a two-beat locked-booth whodunit queued behind the first),
`Mystery3.kt` (the third case: a four-chapter excavation queued ten
days behind the second, its dig sites drawn as scene tokens),
`Mystery4.kt` (the fourth case: a four-chapter case of overnight
repairs queued ten days behind the third, its fixes drawn as
permanent scene dressing that never comes down),
`Mystery5.kt` (the fifth case: a four-chapter confession-forensics
case queued ten days behind the fourth, its spine a claim ledger
the Journal board scores claim by claim),
`Mystery6.kt` (the sixth case: a four-chapter court-of-public-opinion
case queued ten days behind the fifth, its antagonist the town's
own certainty — a conviction meter derived only from witnessed
gossip, proven evidence, and the player's public stances, a
two-column Journal board that never lets a belief pass as proof,
and a town meeting where the verdict lands by the meter unless
the presented evidence outweighs it),
`Mystery7.kt` (the seventh case: a four-chapter case told backwards
queued ten days behind the sixth, its spine a timeline the Journal
fills in reverse — each movement committed as a written
reconstruction and confirmed or broken by the past),
`Mystery8.kt` (the eighth case: a four-chapter alibi-graph case
queued ten days behind the seventh, its spine a graph of seven
vouched alibis and the four pairs that share an event-signature —
each pair judged one event or two, each judgment graded by its one
settling piece, verdicts derived on every read),
`Mystery9.kt` (the ninth case: a four-chapter audit case queued
ten days behind the eighth, its spine a ledger of four count-sites
— a counted set against its book, the discrepancy derived from the
two numbers — each site called count-wrong, record-wrong or
added-to, each call graded by its one settling piece, verdicts
derived on every read),
`Dance.kt` (v0.13.0b: the school dance — the second
Friday of February, announced a week ahead, proven
disjoint from the play's openings, the fair, the
carnival and every finale window by a multi-decade
sweep in one lived time space; the ONE thing that can
own its Friday is Valentine's Day itself, which yields
the year whole with the reason printed on the poster,
and a live case climax or finale reaches it the same
way through the town's own quiet-day definition. The
ask rides the tap-person menu: a YES at the Friend
tier for the glad, at Close for the shy (a soft "ask me
again" below it), a kind in-voice NO — final — from the
rest, all 42 kids' answers authored; one date at a
time, and a second yes stands the first up — allowed,
costed (+3 friendship paid back at the switch),
remembered on the Journal page and by the stood-up kid
in the gym that night. Stag is a first-class plan,
stated on the poster, the page and the menu. The
evening (18:00–21:30, the Elementary gym) is an agenda
claim seated after the play and before sleepovers,
seating your date, a seeded share of the kid cast and
Principal Victoria chaperoning; four beats — ARRIVAL
in the dressed gym, the SLOW DANCE played on the
showdown bar (your partner's composure literally
widens your margin; stag, the solo spotlight), the
PUNCH TABLE serving your own lemonade-as-punch and
snack cakes, consumed exactly, and THE LAST SONG,
which reads the night back and mints ONE group photo
through the photo system's own composition. A Journal
💃 THE DANCE page, one TOWN NEWS line in the paper's
next edition (section copy — the headline ladder
stands at ten), and ONE morning-after phone text from
your date or a friend who was there. The framing is
courage-and-friendship throughout — outcomes are
friendship and standing only; all state rides the
`life`/`lifeText` maps — no new save keys),
`Talent.kt` (v0.13.0c: talent night at the Bijou — the second
Saturday of April, the poster up a week ahead, proven
disjoint from the dance, the play's openings, the fair,
the carnival, every finale window and every holiday
window by a 2026–2100 sweep in one lived time space;
a bigger town moment reaching the date yields the show
the whole year, reason printed, never postponed. The
field is derived from the play's own data — stage nerve
and the bee's voice, weighted and seeded — the top ten
take the bill, every act authored (one original line
per kid, denylist-guarded like the play's and the
heroes' guards), the running order posted on the
poster. The Bijou's Saturday screening stands down for
the night through MovieNights' own deference. You can
sign up at the screen in poster week — four authored
acts, each with its own played shape on the showdown
bar — and rehearse once, a real 30-minute beat worth a
stated +6; or just watch, with the room's reactions
scored by MovieNights' own tables. The evening
(18:30–21:30) is an agenda claim seated after the dance
and before sleepovers; NPC scores are derived live
(nerve, voice, chemistry with the room, a seeded
wobble) and the winner is derived at the wrap — ties by
the room: chemistry, then nerve, then the alphabet.
Prizes: a 15-coin envelope, the winner's framed line, a
yearbook line, ONE photograph through the photo
system's own composition; friendship pays only on a
night played through. A Journal 🎤 TALENT SHOW page,
one TOWN NEWS line in the paper's next edition (the
ladder stands at ten), and ONE morning-after phone text
— fact-only, from the winner, the host, or a friend who
was there. The room is kind: the judging never mocks a
kid act. All state rides the `life`/`lifeText` maps —
no new save keys),
`Mystery10.kt` (v0.12.0f: mystery #10 — the photo case, queuing
ten days behind the ninth, its spine a comparison of four
photographs — the loaned print, the archive print, the Bijou's
enlargement, the album page — each carrying three derived
checks (its place, its day, its faces, each recomputed from the
frame's own frozen fields on every read), each frame read
genuine, misfiled or composite, each reading graded by its one
settling piece, verdicts derived on every read),
`Mystery11.kt` (v0.13.0f: mystery #11 — the voice case, queuing
ten days behind the tenth, its spine a voice ledger of six
surfaced letters — the thank-you, the warning, the counselor's
note, the ranking, the apology, the long letter — each scanned
against its claimed signer's authored voice profile (markers,
load-bearing marks, nevers, studied tells, each recomputed
from the letter's own text on every read), each letter read in
voice, off voice or borrowed voice, each reading graded by its
one settling piece, verdicts derived on every read),
`Mystery12.kt` (v0.14.0f: mystery #12 — the returns case,
queuing ten days behind the eleventh, its spine a returns
ledger of six returned items — the mitten, the scarf, the
lunchbox, the cartridge, the diary, the folded paper — each
carrying three derived checks (the loss, against the box's own
spike of loss cards; the hand, against its recipient's
authored hand card of letterforms, pressure and habits; the
fit, claimed marks against the returned thing's own), each
item read TRUE RETURN, KIND MISTAKE or PLANTED, each reading
graded by its one settling piece, verdicts derived on every
read),
`Mystery13.kt` (v0.15.0j: mystery #13 — the exhibition case,
queuing ten days behind the twelfth AND behind a gallery
show that has actually wrapped, its spine the provenance
chain of the audited show's wall: six frames in hanging
order, each read against the records it claims — the album
record the photograph was taken from, the darkroom copy
where one was made, and the frame log the show itself
wrote — through three derived checks (the print, what the
piece in the frame IS; the chain, where it CAME FROM; the
frame, what its FRAME claims), each frame read TRUE
HANGING, HONEST CROSS or SWAPPED PRINT, each reading
graded by its one settling piece, verdicts derived on
every read from the same records the show itself wrote),
`House.kt` (your house: the bed's sleep, the morning report, the derived
decor/shelf/mail — everything read off records that already exist),
`Attic.kt` (v0.12.0e: the attic — an upstairs fixture at
Your House whose three shelves are derived on every read
and stored nowhere: the overflow shelf (collectibles
and feat cups past the trophy shelf's caps), the family
frame's outtakes (the home family photos that aren't
in the frame, plus any a darkroom print still holds),
and one stored box per finished case in the
town-secrets pattern, public shape only; no economy,
no new save keys),
`TownLife.kt`, `Chrome.kt`, `Stories.kt` (the relationship storylines:
five multi-day pair arcs on the goals/threads/episodes machinery),
`SceneLight.kt` (the shared light state: day bands, light level,
weather-wear palette transforms, per-location warmth),
`Horizon.kt` (the sight-line derivation: every exterior's skyline off
the map graph + coords — neighbors on true bearings, weather-cut
sight, the water tower, the rim ridge),
`SkyState.kt` (the sky as data: moon phase + moonlight, aurora /
meteor / harvest-moon specials at pinned frequencies, last night's
record — one pure function every surface reads),
`Photos.kt` (the selfie system: reactions, follower math, feats,
and since v0.4.0c the posed group frames — compositions,
pair chemistry, the group rate — the album itself is
re-rendered from records by the android-side
`PhotoView.kt`, never stored as pixels),
`Walk.kt` (v0.5.0a: travel on the real street graph — shortest-path
routing over EXITS, distance-priced minutes banked into the town's
ticks, learned shortcuts, the pass-through road beats, and the bus
pass economy), `Wardrobe.kt` (v0.5.0b: the 14 unlockable creator
pieces — their sources, prices, feat/seasonal/special triggers,
the coin rails, the retroactive grant sync, and the piece-specific
lines the outfit-comment rail speaks), `PocketPals.kt` (v0.5.0c:
the 24-card original set — names, flavor, schools, rarities and
the drawing specs the android-side `PocketPalArt.kt` renders —
plus the capsule-machine roll, the errand-card rail, the two
Look exclusives, and the kids' weekly-stash trading with its
obsession-school valuation and last-copy protection),
`FactionMissions.kt` (v0.5.0d: the eight faction chains —
three escalating beats per faction, offered through the Talk
gate's FACTION_MISSION rail and judged by the existing verbs;
standing gates at the Liked/Respected thresholds, a rival
cost on each set piece, and the permanent marks + titles a
finished chain leaves, all derived from two `life`-map
entries per faction — no new save keys), `Capstones.kt`
(v0.8.0b: the chains' endgame — one three-beat capstone per
faction, gated at Beloved standing, judged by the mission
verbs plus three other systems' own funnels (a Bijou
screening PHOTO, a town-vote count, a worked job shift);
beat 3 is a ceremony at the faction's place in a late-
afternoon window, and the uniform reward is a permanent
drawn trace — Depth BACKGROUND, painted in `SceneArt` — plus
a title; capstone beats pay no standing at all and finishing
costs the missions' rival cost; below the gate the senior's
one-line hint rides the Talk gate's owed-lines; state rides
two more `life`-map entries per faction — still no new save
keys), `Emergencies.kt` (v0.8.0c: the favor that can't
wait — errand emergencies: eight day-seeded scenarios
judged by the errand verbs themselves, a deadline priced
off the Walk graph's own minutes plus a working allowance
(never offered unless the route honestly finishes before
quiet hours), the offer on the Talk gate's new EMERG_OFFER
rail with a location-entry shout, rescue rates and the
rescue feats on success, the town-copes settlement — a
coping beat on the owed-lines rail, a morning-report
line, and nothing else — on expiry; state rides two
additive fields on EMERG errands only plus `life`-map
keys), `Yearbook.kt` (v0.8.0d: the town yearbook — the
school year (June to May, closing on spring's last day)
judged from the album alone at the summer turn: six
categories scored by stated formulas over photo-record
fields, a ⭐ star worth a flat bonus in every category,
three cast editors whose notes are templates filled only
from the winning record's own facts, volumes stored with
their winning records inside (the newest few whole, the
elders index lines), publication topics for the pictured
and the runner-up; state is one additive `yearbooks` key
plus the records' additive `starred` flag), `Rival.kt` (v0.8.0e:
the rival — one of seven authored kids crowned by a pure
function of the save's creation identity in a played
reveal beat; contests judged on the existing rails (the
real showdown duel, the bee's bracket, the catch table,
photo milestones, played climax tiers against a
nerve-rolled tier) plus one honest-clock cross-town race
on the Walk graph (ties arithmetically impossible); a
capped contest ledger, a "the rivalry" topic, a Phone
trigger, pranks that are pure dressing strings, and
three standings derived from the player's own answer
history, locking at ten contests; state rides the
`life`/`lifeText` maps only — no new save keys),
`Crew.kt` (v0.9.0b: your crew — up to four cast members
you ask in person (Friend tier, the tap-person menu, an
in-talk choice rather than a TalkGate rail; the rival is
askable like anyone, and their contests still fire);
rendezvous priced by the Walk graph's own route minutes
from wherever the member actually is, walking the
agenda layer (Crew's gathering hook sits LAST in
Engine's stack, below dinners, jobs, screenings,
ceremonies and finales — and returns null whenever one
of those claims the member, so set pieces pull people
away, with the regret owed in Talk, never silently);
occupied is priced honestly across school, shifts,
dinners, gatherings, live climaxes and emergencies, and
town-owned moments gate new calls outright; arrivals pay
+1 friendship once per member per day (the lesson rate),
tally the outing and the place, and the town notices —
a "your crew" topic planted on the witnesses, a SPOTTED
bark when two or more roll in together; photos, showdowns
and rival contests tally witnesses through the modules'
own funnels; the Journal's 👥 YOUR CREW page reads the
same counters; state rides the `life`/`lifeText` maps
only — no new save keys),
`Darkroom.kt` (v0.9.0d: the darkroom — a working fixture
in Your House (a red-lit corner with trays and a drying
line, drawn with the house fixtures in `GameView.kt`,
its print wall beside the yearbook shelf painted by
`PhotoView`'s own renderer off each print's frozen
record); developing a photo costs stated coins and
honest minutes on the town's own clock and files a
PRINT: a physical keepsake carrying its own copy of
the photo's record, so it survives the album cap whole,
caption and all, while the source record is never
touched; a print box capped with the wall, enlargements
giftable through the person menu's gift flow — judged
by the photo's real fields (presence, live chemistry,
the followers the shot earned, the star), once per
photo per person, the holder remembered and their Talk
saying so once; hosted-dinner guests remark on wall
prints featuring their household, fact-slot lines only
from the print's own copy; a yearbook winner's print
wears a brass plate derived from the volumes, never
hand-set; one additive `prints` save key),
`Family.kt` (v0.10.0a: your family — Mom and Dad as
household NPCs in the generated-NPC pattern
(`VisitorPersona`/`ActiveNpc`), explicitly NOT cast:
no `AGENT_PROFILES` entry, no `state.agents` row, no
`state.friendship` key ever, so the 70-cast census,
bark floors, pair scripts, faction tables and the
rival pool are untouched; their looks derive from the
player's own creator appearance — skin exact, hair
color exact for Mom and darkened-with-your-beard-color
for Dad, Dad bald iff you are — with hairstyles,
clothes and faces authored; jobs at real places (Mom
the Bank's new-accounts desk, Dad the Post Office
counter) on resident-shaped agendas that yield to
weekends and holidays; a played arrival beat with a
three-way stance the parents' lines remember; family
dinners at YOUR table three nights a week on the
complement of the cast households' rhythm, riding the
Dinners window/hosting/deference rules with a seeded
cast guest claimed in the agenda stack beside the
dinners; a weekly family allowance of 3 🪙 — the only
coin source this build adds — earned at the table;
chores as tiny errand-pattern favors (set the table,
take a family photo, fetch a pantry staple) that pay
warmth, never coins; Mom/Dad Messages threads inside
the unchanged `MAX_TEXTS` (72 threads × 2 = 144 ≤ 160);
family photos at home that hang in a family frame and
print/gift through the darkroom's own discipline, the
gift paying family warmth instead of cast friendship;
all state rides the `life`/`lifeText` maps — no new
save keys),
`Heroes.kt` (v0.10.0b: hero nights — sixteen ORIGINAL
hero personas for the kid cast, every name, power and
costume invented for this game and derived from each
kid's own measured palette, guarded in the suite by a
franchise denylist in the Pocket Pals pattern; patrol
nights on **Wednesday and Saturday, 19:30–22:00**, at
five lamplit spots, as an agenda claim that seats below
the rival crew's turf and above your crew — a crewmate
standing with you patrols WITH you, in costume — and
self-guards against every bigger moment, dinner, job,
fair claim and family-dinner guest, so a patrol never
eats a family dinner; your own hero identity, founded
in a two-step dialog and changeable at the mirror, its
costume derived from your worn palette; beats on the
existing verbs — a Look finds the night's kid-scale
"crime" and settles it, Talk offers a Walk-priced
escort home, and the camera tells the truth: costumed
figures carry their costume ON the photo record, so
the album, darkroom and yearbook see the capes; a
five-movement original caper on patrol nights with a
finale stance and persistent marks; hero tallies pay
no coins, ever; all state rides the `life`/`lifeText`
maps — no new save keys), `Play.kt` (v0.10.0c: the
school play — one original production a school year,
"**The Mountain That Wouldn't**", written for this
game; Principal Victoria directs and casts seven named
roles plus ensemble deterministically from the town's
real data — an authored nerve table that keeps the
Rival pool's nerve exactly, the spelling bee's
line-handling skill, and each kid's actual chemistry
with the director; the player auditions into the same
pool with a played line-reading (bold / true / deadpan),
or joins the crew (sets / lights / props), or takes
the audience honestly; four rehearsal afternoons ride
an agenda claim at the Elementary and attendance is
tracked on both sides of the footlights; opening night
is a safe-moment set piece judged live from cast
chemistry, rehearsal coverage, the real room and the
player's one played beat — TRIUMPH, SOLID or BEAUTIFUL
DISASTER, all good nights; the night mints one real
cast photo through the photo system's own group rules
for the darkroom to print; a rotating kid critic
reviews it the next morning; all state rides the
`life`/`lifeText` maps — no new save keys),
`GroupChats.kt` (v0.10.0d: group chats — three earned
rooms inside the phone's own message store: your crew's
chat, born with the roster and archived when it
empties; the family thread, where the parent who didn't
solo-text adds the second voice; and the current
production's cast-and-crew channel, alive from the cast
list to the review morning. Every reactive post is
derived in the phone's own morning harvest from a
recorded fact — Crew's tallies and the photo records,
Family's dinner and allowance stamps, Play's record —
and stamped only when it lands; quick replies draw
reactions from members actually in the room and never
move friendship or coins; all state rides the
`life`/`lifeText` maps — no new save keys), `Stand.kt` (v0.10.0e: your stand —
the City Hall permit and the four pitches, crate stock
bought from the town's shops at the pantry's real
prices with stated shelf lives, player prices inside
the stated band, presence-only selling (you at the
pitch, or a crewmate who walked the real route under
the crew's occupied rules), the books derived from
the recorded transactions, and the rival crew's
review off those books with its traffic lift; all
state rides the `life`/`lifeText` maps — no new save
keys), `Newspaper.kt` (v0.11.0a: the town newspaper — a
weekly edition composed entirely from the record: the
paper's own weather log of ended days, a headline chosen
by a stated significance precedence over the week's real
records (a closed case, the fair, opening night, the
vote, the rivalry, the sky — or an honestly quiet week),
TOWN NEWS / SPORTS / CLASSIFIEDS / WEATHER sections in
which every name and number traces to a record and the
classifieds print only the stand's real prices; editions
freeze at the week's turn, past editions keep their
headline lines, and the Journal renders the 📰 THE
PAPER page; all state rides the `life`/`lifeText` maps
— no new save keys), `Garden.kt` (v0.11.0b: your garden — the
four beds at Your House and their per-bed records,
five crops with stated planting windows whose seeds
are real stock from the Mall garden centre's rack,
growth advanced at the rollover off the ended day's
real weather alone (rain and the WATER beat water,
dry days stall, summer sun scorches the unwatered, a
blizzard and winter's turn kill the tender, weeds
creep weekly and cut yield), harvest into real
pantry produce with a provenance count the stand's
crate honors, the season's biggest pumpkin kept for
the fair's produce weigh-in, and the 🌱 YOUR GARDEN
Journal page; all state rides the `life`/`lifeText`
maps — no new save keys), `Shop.kt` (v0.12.0b: the
Mall counters — the garden centre's seed rack, selling
Garden's seed packets as real inventory at the crops'
exact stated prices (planting spends a held packet
first and keeps its order-at-planting path at the same
price), and the snack shop's general-goods counter,
selling Stand.STOCK's four pantry staples into the
real pantry at the shops' own prices; both tills are
one-way coin sinks with derived prices, so no
buy-low/sell-high circuit exists against the stand
band or Liane's booth, and seed packets ride the pack
with no ItemDef — the bus pass's pattern — so they can
never be gifted, fetched or stocked; purchases accrue
in the garden's seed-spent book and the snack till's
own `shopGoodsSpent` line), `Sleepwear.kt` (v0.12.0d:
the sleepwear derivation — a pajama code ("top|bottom|
cap") derived deterministically from each figure's own
sheet (top from their shirt color, bottoms from their
pants color, both softened for lamplight; hats off, a
nightcap only for the soft-knit day hats), frozen onto
PhotoFigure beside the hero code at shutter time when
a photo is composed at Your House on a live sleepover
night, and rendered by KidArt's one sleep pass so the
album, the street re-render and the darkroom all draw
the same pajamas; solids only — no dots, no clustered
patterns, no repeated motifs; state reads only, no new
save keys), `LiveLook.kt` (v0.15.0a:
the live-look derivation — the ONE reader for what
the player figure is actually wearing right now:
sleepwear while a sleepover night is live (the code
is Sleepwear.playerCode itself), hero costume while
a patrol night is in its window (the code is
Heroes.playerCostumeCodeFor itself), the day look
otherwise; the creator preview, the Journal YOU card
and the mirror glass all read it, deriving the
costume codes off the appearance passed in — in the
creator, the look under edit — while the street and
the shutter keep their stricter visibility gates
over the same codes; state reads only, no new
save keys), `Phone.kt` (v0.5.0e:
the phone's brain — the six-tile hub registry, the message
store, the morning harvest that turns noted facts and
derived moments into deterministic texts, the quick-reply
economy and Kenny's muffle filter; rendered by the
android-side `PhoneActivity.kt`, a hub over the existing
surfaces plus the one new room, Messages).
`Commissions.kt` (v0.6.0a: photo assignments — the weekly
board of asks as a pure function of the week, its
schedule-proven subject/place/condition generator with
the askers' no-repeat memory, the capped plate with its
free expiry, the record judge that rides applyPhoto, and
the commissioned-print stamp).
`Dinners.kt` (v0.6.0d: family evenings — the seven
households and their tables, the rotating invitation as a
pure function of the calendar with its collision
deferrals, the three judged beats at theirs and at yours,
the hosting gates and the spread's honest judging, the
midnight wraps; all state in the `life`/`lifeText` maps).
`Jobs.kt` (v0.6.0e: town jobs — the three standing jobs
and their bosses, the weekly schedules and the shops'
shared rota as pure functions of the calendar, the live
shift — clock in, the task card, clock out — judged by
the Talk gate's WORK rail and the Look chain, the streak
tiers, the kind miss ladder with its set-piece excuses,
and payday at the week's turn; all state in the
`life`/`lifeText` maps).
`Votes.kt` (v0.6.0f: town votes — the weekly civic
question at City Hall as a pure function of the week
(least-recently-used rotation over a 12-question bank),
authored cast ballots from champion/faction/obsession
leans over the persona data, the player's ballot
weighted by the Fixture rule, the count at the week's
turn, and standing outcomes whose traces live on Look
at the places they belong to; all state in the
`life`/`lifeText` maps).
`Secrets.kt` (v0.7.0b: town secrets — the twelve hidden
finds as a pure registry: each a trigger on the existing
Look chain or an existing prop tap (bench, garden
gnome), a gate read off the real world state (light
band, weather family, season, aurora, a standing vote),
a one-time reveal with a present NPC's reaction, and a
modest reward settled exactly once; the found-set, the
Journal riddle page, the morning note, and the Sleuth
feat; all state in the `life`/`lifeText` maps).
`MovieNights.kt` (v0.7.0c: Bijou movie nights — Friday
premieres and Saturday headline showings at the theater:
a feature generated whole (title, genre, 2–4 stars, five
title cards, one immortal flop) from seeded banks with
a no-repeat rotation, the standing Saturday-matinee
vote headlining Saturday's show; an authored house —
the stars, their friends, their heckling arch-rivals,
seeded regulars — gathered at the Bijou by agenda
override; three reaction prompts scored against each
card's beat into the night's buzz (dead / polite /
buzzing / legendary); star friendship, a "movie night"
memory topic, and a 🎬 MOVIE NIGHTS Journal page at the
credits; dark nights deferred to case climaxes, finale
evenings, and holiday beats; all state in the
`life`/`lifeText` maps).
`School.kt` (v0.7.0d: school days at the Elementary —
on weekdays the fourth grade actually meets: the
day's lesson off a 14-topic bank (deterministic,
stride-rotated by school days), taught by Garrison
with Principal Victoria covering Wednesdays, in two
beats with the kids actually present reacting in
character, plus one participation moment (right-ish /
funny / pass — passing always safe); a midday recess
beat refereed between two kids actually there; the
Friday spelling bee, Victoria presiding — eight kid
spellers eliminated on their own data, Cartman
disqualified for sleeve-spelling, the player spelling
letter by letter off the choice row; records on the
Journal's 🎓 SCHOOL page; quiet days deferred to case
climaxes, finales, and holiday beats; all state in
the `life`/`lifeText` maps).
`Climaxes.kt` (v0.7.0e: playable event climaxes — when a
shenanigan reaches its climax stage (12:00–18:00) and
you're on its turf, talking to any of its causers plays
the peak as a chain of three judged beats written for
that event's own story (Talk and Look choice rows, PHOTO
beats settled by the shutter on the turf, FIND beats
settled by a careful Look, and one new interaction —
HOLD YOUR GROUND, a three-surge stand used three times
in the set and taught by its first use); every beat
scores landed / close / missed and pays participation
into the event's own ledger; the midnight wrap settles
a played chain to a tier — HEROIC / GOT IT DONE /
FUMBLED — with tier outcomes, causer friendship, a
tier-flavored retelling topic for tomorrow's Threads,
and the tier plus a one-line how-it-went on the
episode's Journal record; an unplayed climax settles
ABSENT and the wrap is byte-for-byte the legacy wrap;
pre-v0.7.0e episode records read as before-records
(GOT IT DONE for memory purposes); all state in the
`life`/`lifeText` maps).
`Depth.kt` (v0.5.0g: the scene depth pass as data — the
three bands every layer and every dressing token in the
game belongs to, the exterior/interior layer stacks in
draw order, the per-location/per-theme foreground kinds,
and the foreground occlusion budget as pinned geometry),
`Crowds.kt` (v0.5.0g: the crowd-cluster layout — a pure,
deterministic grouping of big crowds into conversational
clusters of 2–4 off the town's own chemistry: pair
scripts, bonds, factions, rival blocks),
`PenPals.kt` (v0.13.0e: pen pals — a friend you've never
met, in a town that doesn't exist anywhere else. Wren
Halloway of Pinewood Flats is NOT cast: a VisitorPersona
outside the 98 on the parents' precedent, with her own
authored voice (a mini speech card and letter banks),
her own authored SHEETS-style spec drawn through the
same KidArt geometry, and a warmth lane of her own that
never touches the friendship book. The Elementary's
program takes signups at the classroom board every
October 1–21 (late joiners wait for next year, stated);
letters ride your dad's Post Office route into your
mailbox on a seeded 3–5 day cadence. Incoming letters
are composed from her banks plus FACT SLOTS — follow-ups
on facts your own sent letters actually carried, and
your replies compose in front of you from your real
records (latest photo, a closed case's public shape —
title + closed, from the attic's boxes and nowhere
else — the stand, the garden, the fair, the carnival,
the dance, the talent show, the parade) under one of
three tones, the sent text kept verbatim on the
Journal's 📬 PEN PALS page (the newest 16 whole, the
elders folded to a line). Silence stretches the cadence
and is noted kindly; three unanswered letters and the
correspondence goes dormant until you write. At the top
warmth band on May 31 she VISITS — four June days as a
resident-class guest at your house and the park, talk
in her own voice, an album photo through the ordinary
camera, a goodbye beat; below the band, a "maybe next
summer" letter. The paper never covers the letters and
the phone gets nothing: letters are letters. All state
rides the `life`/`lifeText` maps — no new save keys).
Persistence keys in `Prefs.kt` (`sptown` SharedPreferences) are stable
across updates: `player_name`, `player_appearance`, `gemini_key`,
`gemini_model`, `llm_provider`, `openrouter_key`, `openrouter_model`,
`game_state`, `friendship`, `collected`, `last_played_date`, `spawn_date`,
plus v0.1.5's separate `npc_memory` document (additive only — old saves
load unchanged).

