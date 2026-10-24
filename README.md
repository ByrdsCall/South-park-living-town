# South Park: Living Town (Android)

Portrait South Park adventure game, built like the show was: layered
construction-paper cut-outs, and you standing in the scene.
A non-commercial fan project — respect my authoritah.

## What it is
- Walk South Park as a paper cut-out town (v0.15.0c): every location is a
  layered paper scene — sky, far range, a street of buildings, the ground
  band, props, people — and YOUR kid stands in it. Tap the ground, a person,
  a prop, a door, a bus stop: the kid walks there and it happens. Travel
  between the 27 game-mirrored locations by the signposts, the 🗺 Go menu,
  or the bus; tap a kid to talk → South-Park-style speech bubbles.
- 70 characters live their day on an **in-game clock** (15-min ticks from
  8 AM, advances as you play/wait): the 5 original kids plus Wendy, Craig,
  Tweek, Clyde, Jimmy, Timmy, Tolkien, Bebe, the 3 goth kids, Randy, Sharon,
  Gerald, Sheila, Liane, Stuart, Carol, Stephen, Linda, Garrison, Mackey,
  Chef, Jesus, Jimbo, Father Maxi, and Towelie — joined in v0.3.0b by Heidi,
  Nichole, Red, Kevin, Francis, DogPoo, Mayor McDaniels, Officer Barbrady,
  Ms. Crabtree, Mr. & Mrs. Tweek, and Ned — in v0.7.0a by Karen, Scott,
  Firkle, Annie, Bradley, Skeeter, Principal Victoria, Grandpa Marvin,
  Mr. Tucker, and Steve Black — in v0.8.0a by Ike Broflovski,
  Kevin McCormick, Shelley Marsh, Laura Tucker, Dr. Mephesto,
  Sergeant Yates, Mr. Adler, and Lu Kim of City Wok — and in v0.9.0a by
  Roger Donovan, Ryan & Sarah Valmer, Alexis & Mr. Testaburger,
  Nathan, Sophie Gray, and Big Gay Al. Day/night + seasons +
  procedural weather follow the in-game date. (Real-device-time sync kept,
  disabled behind a Settings toggle defaulting OFF.)
- Animal Crossing structure, all South Park style: friendship levels per
  character, rotating date-seeded daily dialogue (12+ topics each), 25
  collectibles, 8 holiday events — each with its own playable
  signature beat since v0.4.0d — cozy no-fail pace.
- Character creator at first launch (Stick-of-Truth-styled, 13 categories
  since v0.1.9d): the **blank New Kid** (bald, in underwear — matches the
  games) is the default base; customize skin, build, hair, hat, top, bottom,
  shoes, eyewear, facial hair, gloves, back item and accessory from it —
  with a dice roll for a whole look and 3 preset slots for favorites.
- Dialogue: pick your LLM provider in Settings — **Gemini** (`gemini-2.5-flash`,
  free key from Google AI Studio) or **OpenRouter** (`deepseek/deepseek-v4-flash`
  default, editable model field, your own OpenRouter key). Keys are stored on
  the phone only, never logged. Without a key for the selected provider, a
  built-in mock brain takes over (pair scripts + line banks).
- All art is code-drawn on Canvas in one consistent style: the New Kid model —
  giant circle head (~2/3 of height), no neck, big touching white oval eyes
  with dot pupils, tiny stub body. Every character (kids and adults) uses it.
  No ripped assets; wiki pages used as text/visual reference only.

## Build (no Gradle — low-memory VM)
Requires: `~/build-tools/kotlin` (kotlinc 1.9.24), `~/android-sdk`
(build-tools 34.0.0, android-34), a JDK 17 (`~/build-tools/jdk-17.0.9+9-jre`).

```bash
cd ~/workspace/sptown-app
./build_apk.sh            # → out/southpark-town.apk (debug-signed)
```
Bump versions via `VERSION_CODE` / `VERSION_NAME` env vars.

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

## Status — v0.17.0a

THE CHARACTER SYSTEM REDO (versionCode 125).
Lucky's order (Oct 24, 2026): "completely redo
the character system with your new insights,
remake the world as well" — and his spgen
character-pipeline breakdown lands as the
governing spec. The 0.16 line closes; the
figures, the paper and the set pieces now all
run on ONE contract. Everything below is
gated in the suite (§10 roll-up in section
v0.17.0a). One new module joins the tree —
Rig.kt (the tree stands at 134 Kotlin files).

PART A — SCALE CONTRACT AS CODE (new file
Rig.kt: ScaleContract). One canonical unit:
the kid height. body_scale — kid 1.00, adult
1.35, cartman-class 0.95, toddler 0.70, elder
1.30 (plus the measured teen ladder our age
bands already used: young_teen 1.12, teen
1.22, older_teen 1.31). Outline weight is
specified in FINAL SCREEN PIXELS: window
[3,7] px at 1:1. Props are sized from the
WEARER (prop-relative: lance 1.20, shield
0.40, banner 0.90, crate 0.35, cape 0.62…),
never absolute — the fiction cape's length
now reads the contract instead of a magic
number. A density floor gates render
contexts: a context too small to hold the
outline window fails the floor rather than
upscaling mush.

MEASURED BODY_SCALE AUDIT (contract vs the
cast's composed anchors):

| class | contract | anchor | drift | verdict |
|---|---|---|---|---|
| kid | 1.00 | 1.00 | 0% | exact |
| young_teen | 1.12 | 1.12 | 0% | exact |
| teen | 1.22 | 1.22 | 0% | exact |
| older_teen | 1.31 | 1.31 | 0% | exact |
| adult | 1.35 | 1.38 | +2.2% | within gate — the town's adult band is its measured reality; the doc's 1.35 is the class centre |
| elder | 1.30 | 1.33 | +2.3% | within gate — Marvin's own sheet is his class anchor; per-character law |
| cartman | 0.95 | 1.00 | +5.3% | LOGGED divergence: Eric's bulk is carried in WIDTH (head-body width ratio), never height — his sheet is law |
| toddler | 0.70 | 0.80 | +14.3% | LOGGED divergence: Ike's measured sheet (heightRel 0.80, head share 0.655) beats the generic class — his sheet is law |

Any future class drifting past 5% without a
logged, measured reason now FAILS the build.

PART B — OUTLINE ENGINE v2 (OutlineEngine).
The figure rim is no longer a uniform-width
offset: width is computed PER CONTOUR POINT
from the contour's own curvature — thickest
at bends, light on straight runs, interior
detail at 0.6× — clamped to the [3,7] px
window, baked into the cut ribbon at render
time. PER-CONTEXT REGENERATION: the rim is
computed in the context that draws it (world
figures at world weight, PaperDoll portraits
at the doll's heavier bias) — never drawn
once and stretched. The old uniform rim
formula is removed whole. Parity is pinned:
the same window holds at toddler, kid and
adult figure heights alike.

PART C — REGION-AWARE PAPER. PaperCut's
fields and Paper's material pass are
region-aware: SKIN (fine grain, whisper
strength, a slight saturation lift — worn
on heads, under the features, which stay
clean), CLOTH (coarse fibre, stronger weave
bias, a touch of speckle — worn on torsos
and the fiction banner), METAL (sparse
grain + a pale edge sheen — trash-can-lid
shields, set pieces), SURFACE (the existing
treatment, unchanged, for architecture and
ground). 100% procedural as always — the
scanned paper kit is never used. The
one-fibre-per-jittered-cell construction is
kept in every region, so no clustered
pattern can form (suite-pinned minimum
spacing).

PART D — PARTS + RIGGER. Figure assembly is
formalized: ONE shared 20-joint skeleton (no
fingers, no facial bones); every part is a
card {id, bone, pivot, z, coverage,
made_from, region}; assembly sorts by z —
the front stack is resolved AT ASSEMBLY, not
by painter order. KidArt assembles every
figure through the rigger first and draws
limbs from the assembled cards' pivots, so
the painter and the gate read the same
joints. PIVOT GATE: every cast member, the
player, generated figures and every fiction
prop must assemble with every pivot inside
its silhouette, on its joint — a part that
would detach fails the build. Poses,
ActionAnims, MotionRegistry overrides, side
profiles, seated legs, the gait, costumes
and the PaperDoll all run on the rig
unchanged.

PART E — EXPRESSIONS MANIFEST (FaceManifest).
Faces are a named grid: 8 emotions
(neutral, happy, angry, scared, smug, sad,
shocked, deadpan) × 6 mouths (closed,
small_o, wide, grin, teeth, frown) = 48
stable human-readable ids (angry_wide).
State picks the emotion row, lip-sync the
mouth column. Every existing expression in
all three face sets maps onto the grid
(suite-pinned, total). Out-of-grid states
are logged extensions with reasons: talk
(per-frame lip-sync, not a stored face),
hurt, sleepy (eyelid-driven), and the whole
animal set (a parallel non-human grammar).
Faces stay texture-style swaps. SAVE
COMPATIBILITY: FaceCompat resolves every
identifier a pre-redo save can hold (sheet
face names like nervous/worried/braces, grid
ids, unknown strings) — resolution never
fails, pinned by a pre-redo save fixture
round-trip.

PART F — WORLD REMAKE: SET PIECES (SetPieces
in WorldScale.kt). Town props are registered
as THREE-CARD stacks (back/mid/front) in the
Depth stage bands, under the same contract,
outline engine and region paper: bench,
trash can (METAL), mailbox, hydrant (METAL),
bush, fence, pine, tree, lamp (METAL), bus
sign (METAL) — each piece's composed height
is audited against its contract value; drift
over 5% fails. Fiction dressings are unified
into the same system — the cardboard crate
and the banner stand are set pieces with
provenance (refrigerator box, bedsheet), and
dressings carry regions and wearer-relative
sizes from the one registry. Facades,
storefronts and interiors from v0.16.0e are
untouched — they already wear the materials;
only their props migrated.

PER-PART LEDGER:
- Implemented: A (contract + audit + drift
  gate + density floor), B (outline engine,
  per-context regeneration, parity pins), C
  (four region treatments, figures + set
  pieces), D (skeleton, part cards, rigger,
  pivot gate, painter rewired), E (manifest,
  total legacy mapping, extensions logged,
  compat resolver + fixture), F (three-card
  set pieces, drift audit, fiction unified).
- Substituted: the density floor — our parts
  are procedural vector, not texel art, so
  there is no bitmap to upscale; the floor is
  enforced as the outline window per context
  plus a render-scale floor on the reference
  unit (kid height ≥ 186 px at 1:1), gated.
  The dev-console/FractureLoader analogue is
  the Linux mirror build, used as the live
  check for this build's captures. Palette-
  in-theme (§10) rides the existing per-sheet
  colour truth pins.
- Not applicable (with reasons): exporters
  to the real games' formats (.dds/.ogd/
  .mmb + appearance-manifest registration) —
  we ship an original fan game, not mods of
  theirs; SheetGen remains the neutral
  export. Dev-console hot reload — the suite
  + Linux mirror cover the loop. The spgen
  CLI as an external tool — spec data →
  painters IS our generator; the disciplines
  live in the engine and its gates instead.

SAME AS EVER: save-compatible (additive +
the Part E compat mapping), mystery spoiler
discipline absolute, keystore outside out/,
lorebook byte-identical.

## Status — v0.16.0e

THE RE-DRESS + THE GAIT (versionCode 124). Two
of Lucky's spec docs land in one build: the
v0.16.0c fidelity audit's three structural gaps
close on the v0.16.0d paper materials, and his
Stick of Truth movement/animation breakdown
(Oct 24, 2026) lands its headline steal —
velocity-driven walk cycles with no root
motion. A second pass (the costume system)
folds into this same version below.

PASS 1 — THE STRUCTURAL RE-DRESS:

- HOUSE FACADES: the flat one-story facade
  painter is removed whole. Houses now draw
  two stories in the games' language: an eaves
  board or fascia trim under the roofline, a
  floor band at the storey split, a dressed
  door (frame, transom pane, two panel
  insets, knob, stone step — the leaf stays
  exactly the spec's door rect, so every
  door's placement and reach is unchanged), and
  two rows of framed windows with trim,
  muntin crosses, sills and seeded shutters.
  Per-house variation rides the existing spec
  data (palette, door colour, shutter seed),
  and occupancy lighting / TV flicker / shades
  work per window exactly as before. Every
  street's neighbor row is re-dressed the
  same way.
- SHOP STOREFRONTS: the awning-over-two-slab-
  windows storefront is removed whole. Shops
  now open with a full-width GLASS FRONT:
  an interior hint behind the glass (back
  wall, a counter silhouette, shelf blocks),
  a framed mullion grid, a diagonal sheen,
  the striped awning seated above the glass,
  and the location's sign riding a fascia
  band above that. A shut shop pulls a shade
  with slats instead. Main Street, every
  facade branch, the neighbor row's downtown
  blocks, and the token-layer structures
  (diner, mall, loft) all wear it.
- INTERIOR FURNITURE: the front-facing
  furniture slab loop in GameView is removed
  whole. `FurnitureArt` (in InteriorArt.kt)
  dresses every piece: a silhouette with a
  painted outline and a shaded side, top
  faces that catch the light, legs or a
  plinth, and per-kind grammar — bed
  headboard/blanket/pillow, sofa cushions and
  arms, chair backs, bench slats, pew end
  panels, counter overhangs with cabinet
  insets, shelf boards with a few large book
  blocks (sparse by construction — the
  trypophobia rule), desk drawers, table
  aprons. Trim colour follows the room
  family; the seat line every sitter uses is
  the same line the slab drew to, so sitting
  never moved. All 27 interiors dress from
  the one painter; hit rectangles are
  untouched.
- All of it wears the v0.16.0d PaperCut
  materials, painted outlines and scissor-cut
  edges through the shared painters. Town
  layout unchanged; every named location
  exactly once; route prices unchanged.

FIDELITY-GAP STATUS after pass 1: house
facades CLOSED, shop storefronts CLOSED,
interior furniture language CLOSED, outline
weight CLOSED (v0.16.0d). The one remaining
logged refinement: side profiles still shade
their parts instead of carrying the ink rim.

PASS 1 — MOVEMENT & ANIMATION (the ledger,
per Lucky's movement breakdown):

- B3 VELOCITY-DRIVEN RATE, NO ROOT MOTION —
  IMPLEMENTED, the headline fix. A new pure
  module, `Gait.kt`: phase-per-pixel =
  K · boxH / (legLen · drawn height), K
  calibrated so the reference kid keeps the
  legacy ~22 rad-per-world-width cadence.
  The player integrates phase from the
  distance each input step actually moved
  (PaperWalk.step takes the rate as a
  parameter); route travelers integrate from
  Travel.pose progress over the whole route
  at their own leg geometry; wanderers
  integrate over the out-and-back path they
  actually walk. The shared wall-clock walk
  phase `(now % 1256L) / 200f` is removed
  whole — no actor's cycle is clock-driven
  any more, idle rate is 0, and position
  still comes only from the controllers.
  Pinned: phase advance proportional to
  distance at kid and adult geometries,
  idle frozen, shorter step = smaller
  advance.
- A0/C ONE-OWNER CONTROLLER CONTRACT —
  IMPLEMENTED as `MoveControl.kt`: pure
  resolvers name exactly one owner per
  actor per moment (FREE_ROAM / TRAVEL /
  BUS_RIDE / INSIDE_SECTION / SETTLED) with
  the handoff sites documented at the three
  real seams (Travel.maintain starting from
  the actor's actual position; bus boarding
  locking input; section entry/exit).
  Animation reads movement state; its only
  write-back is the notify channel below.
  Pinned: resolver truth table, input lock
  on the bus handoff, exactly-one-claim.
- B4 CLIP NOTIFIES, LITE — IMPLEMENTED.
  `Gait.footDowns` turns phase crossings of
  the π multiples into footstep events (one
  per foot per cycle — pinned: a full cycle
  fires exactly two, a half exactly one, no
  advance fires none). The player hears the
  existing synthesized step sounds and, in
  snow, pops a small puff at the foot that
  fades as the stride rolls on; travelers
  are throttled and quiet; wanderers get the
  visual puff only — thirty simultaneous
  steppers would drown the mix, and the
  notify is visual-first for crowds. Not
  timers: stop the cycle and the steps stop.
- B7 STEPPED-TRANSITION AUDIT — the one
  true figure lerp found is now stepped:
  the sit-down transition quantizes into
  three held sub-poses instead of easing
  (the games snap; so do we). Deliberately
  left smooth, with reasons: breath and
  talk oscillation (that is the show's own
  held-pose additivity — the figure breathes
  ON a held pose, it does not travel between
  poses), and the bus pull-up ease (a
  vehicle, not a figure). UI fades untouched
  per the brief.
- PINNED PARITY (already true, now locked
  against regression): A3 authored walkable
  strips (sidewalk-only walking + authored
  crosswalks), A1 ground-anchor placement
  through the Depth stage bands, B5 face
  swaps, B6 the mouth as a discrete
  two-glyph swap on its own layer,
  independent of body state.
- NOT APPLICABLE (ledgered, not built):
  A9/B8 combat board (no combat in this
  game), A4 companion follow (no follower),
  A7 ragdoll/prop physics, A6 traversal
  abilities.

PASS 2 — THE COSTUME SYSTEM (Lucky's docs 3
+ 4, folded into this version by his order,
Oct 24, 2026): the town is a constant, the
kids' game is a variable, the diff is the
output. One new pure module, `Fiction.kt`,
plus a phone-door activity. The composition:

- FROZEN BASE: the parcel model derives from
  the existing town — one parcel per location
  plus one per house slot on every residential
  street (the same StreetHouse identities the
  painter and the door registry share).
  Runtime code never writes to base; the
  suite runs a full fiction over it and pins
  the drift at zero. Slots fixed; only slot
  contents vary.
- DIFF LAYER: dressings resolve as a pure
  function of (fiction, start tick, tick)
  over the doc-3 channels — surface (palette
  wash), props, population (costumes),
  signage. Latest state wins by construction
  because there is only ever the CURRENT
  resolution; reverting is resolving at an
  earlier tick, pinned both directions.
- FICTION AS DATA: `Fiction.Def` — id,
  theme, vocabulary, palette, prop kit, rank
  names, claim/flex bias, escalation curve,
  budget. ONE complete playable fiction
  ships: doc 4's fantasy (Kingdom of Kupa
  Keep vs the Elves; you are the New Kid;
  cardboard_wall / bedsheet_banner /
  broom_lance / lid_shield / stick_sword).
  The suite proves a theme swap is data-only.
- RETHEME: a single pure
  dress() → the tick's dressing set, consumed
  by the painters (facades, the neighbor row,
  structures) and by GameView's costume pass.
  Global, simultaneous, theme-consistent.
- DIEGESIS LEDGER: belief is seeded per
  (fiction, adult) and stable all run:
  > 0.75 wears the full cape, > 0.45 wears a
  towel as a cloak (knot at the hip,
  grumbling implied), else unimpressed. Kids
  are always full costume. Institutions lean
  in through signage: City Hall posts BY
  ORDER OF THE KING, the church renames its
  bake sale (YE OLDE — TITHES OF CAKE
  ACCEPTED), the school is HOME OF THE
  KNIGHTS, Main Street is the Kupa Keep High
  Road.
- PROP PROVENANCE: every dressing carries
  fiction_prop / made_from / coverage /
  attachment / wear. Coverage < 1.0 is a
  SUITE PIN — the renderer draws the real
  building first and the costume over it, so
  the refrigerator box shows around the
  castle wall, the tape tabs stay visible.
- CRAFT BUDGET: finite (46 at the start,
  refilled along the escalation curve at the
  LIVE and ESCALATING thresholds). Dress
  order is claims first, flex anchors next,
  then a seeded order — when the purse runs
  dry the tail of the order (the far side of
  town) stays stubbornly normal. Pinned with
  empty and lean test purses.
- CLAIMS: City Hall → the King's Keep,
  the school → the Training Grounds, the
  church → the Chapel of Holding, SoDoSoPa
  → the Bandit Camp. Signage + palette +
  props only. No geometry, no topology, ever.
- FABRICATION: new sites only in reserved
  flex zones — the cardboard fort at Stark's
  Pond, the farm's dressing — and interiors
  behind real doors: the cardboard door
  inside City Hall leads DOWN to the dungeon
  set (torch hall, dungeon, throne nook);
  the one in your house leads to the garage
  lair. Fabricated interiors are sections —
  overlay state, never base locations — and
  vanish on collapse.
- TRANSITION DIRECTOR: REAL → DRESSING
  (2 game hours: banners go up parcel by
  parcel at partial coverage, a kid on the
  job at each, the purse draining) → LIVE
  (a day) → ESCALATING (a day, refills land)
  → COLLAPSING (the set drains back out) →
  RESIDUE. The player starts the game from
  the phone's new 🎭 Play tile and can end
  it early there (jump to COLLAPSING).
  RESIDUE is a capped list of five parcels,
  one cardboard something still standing at
  each — the ONLY fiction state a save
  stores, beside the fiction id, the start
  tick and the end tick (four additive keys
  riding the life maps, as ever).
- GUARDRAILS, pinned: the population channel
  overlays costume states only — cast
  schedules and the life sim are never
  rewritten (Fiction.kt holds no write path
  into them, source-pinned); landmarks are
  protected (dressed, never removed);
  everything derives from (seed, fiction,
  parcel, tick), so a replay dresses the
  town identically.

THE COSTUME LEDGER:
- IMPLEMENTED: frozen base + parcels; the
  diff resolution (pure, revert = resolve
  earlier); fiction-as-data with the full
  fantasy fiction; the retheme pass; the
  diegesis ledger with its three tiers worn
  in the world — standing crowd and walking
  cast alike, indoors and out (the game does
  not stop at doors); institution signage; prop
  provenance with coverage < 1.0 pinned;
  the craft budget with escalation refills;
  claims; fabrication (flex anchors +
  fabricated interior sets); the transition
  director with visible DRESSING and
  COLLAPSING; capped persisted residue;
  phone-tile entry + early end.
- SUBSTITUTED: doc 3's beat/rule tables —
  the scenario BEATS are generated by the
  Director's schedule from the fiction def
  rather than authored as YAML beat lists;
  the op stream the renderer consumes is
  the resolved dressing set either way, and
  a hand-authored beat list is a data file
  away when a second fiction wants one.
  The dressing kids of the DRESSING phase
  are drawn at their parcel (a kid on the
  job, tape roll at his feet) rather than
  simulated walking between parcels —
  the crowd system owns cast movement, and
  the fiction never touches it (that is the
  guardrail working, not a shortcut).
- NOT BUILT: a second fiction (the def is
  data and the swap is suite-proven; one
  complete playable fiction was the brief);
  the timeline-scrub UI (revert-by-resolve
  makes it possible; it wants a screen of
  its own and wasn't in the ask).

LINUX TEST BUILD: `south-park-town-v0160e-linux`
refreshed from the same sources and
selftested (the Windows .exe is NOT
refreshed this build, per the brief).

## Status — v0.16.0d

THE PAPER PIPELINE (Queue #16, fourth build;
versionCode 123). Lucky's direct order (Oct 23,
2026): "Implement the Stick of Truth rendering
pipeline in our game wherever possible —
replace our systems with this when you can."
His own stage-by-stage breakdown of that
pipeline is the spec (saved with the goal's
notes); this build ports every stage that a
2D canvas engine can honestly hold, and logs
the rest with its reason. Nothing was ripped:
the source game's scanned texture kit was
never touched, never downloaded, never
bundled — every material below is procedural
and original, generated in code.

WHAT LANDED, BY SYSTEM:

- PAPER MATERIALS (the core replacement): a
  new pure module, `PaperCut.kt`, computes a
  procedural construction-paper stock — a
  seeded fibre field plus soft mottling — and
  `Paper.applyMaterial` washes it over the
  game's large surfaces: building facades,
  the neighbor row, outdoor structures,
  ground and snow, the street strip, interior
  walls and floors, both foreground strips,
  and photo ground. The fibre field is a
  jittered grid — exactly one fibre per cell —
  so fibres can never cluster: the grain reads
  as paper fibre at scene scale, with no
  stipple fields and no clustered dots
  anywhere (the trypophobia rule, enforced by
  construction and pinned). Faces and small
  detail stay clean. The retired per-pixel
  speckle grain tile — a stipple field by
  another name — is removed whole; fibrous
  grain replaces it on surfaces, in rooms,
  and over the finished frame.
- PAINTED OUTLINES: the outline is now part
  of the shape. Buildings carry a filled
  ribbon outline (`Paper.paintedOutline`)
  whose weight varies along its length like a
  marker's, seeded per building; figures carry
  an ink-underlay silhouette — their head,
  torso and leg masses cut once more as
  scissor-cut contours pushed out by the same
  varying weight, the flat parts painted inset
  inside the rim, seeded per character. The
  facade's old uniform 2px edge stroke is
  removed whole. This closes the fidelity
  audit's outline-weight gap by technique,
  not by scaling stroke widths.
- SCISSOR-CUT SILHOUETTES: PaperCut contours
  (seeded edge wobble, genuinely rounded
  corners) — the same character or building
  is cut the same way every frame; boil never
  re-rolls an edge.
- THE DIORAMA POST PASS (GameView): band
  depth-cue washes (background band a breath
  greyer, foreground a whisper, the action
  band never touched), a stepped vignette,
  fibrous film grain, and the BOIL — a seeded
  ±1px breathing of the painted backdrop on a
  3-frame cycle, backdrop linework only
  (figures, faces and UI never ride it),
  toggleable in Settings ("Paper boil",
  default ON; one new additive Prefs key,
  `post_boil` — saves untouched otherwise).
- THE PAPER-DOLL PATH: stills no longer reuse
  the world figure renderer. `PaperDoll.kt`
  is a second, dedicated path — front pose,
  fixed still clock, full detail, painted
  outline at portrait weight — and the album,
  yearbook pages, darkroom prints (all through
  PhotoView) plus the character creator route
  through it. PARTIAL #14 CLOSED: stills posing
  front was never a bug — they were dolls
  rendered by the wrong path, and the doll
  path now exists, pinned as its own code
  path.

THE STAGE LEDGER (all eleven stages of the
breakdown, as ordered — implemented, or the
concrete reason not):

- Stage 0 (engine constraints): NOT PORTABLE.
  Onyx/DirectX 9/Wwise/Scaleform are a
  different engine; nothing to port. Its
  discipline — everything is data — was
  already this game's architecture (specs,
  manifests and registries as pure data) and
  stays.
- Stage 1 (world as a stage set): IMPLEMENTED.
  The four-band stage is now explicit data:
  `Depth.StageBand` (GROUND → BACKGROUND
  flats → MIDGROUND action → FOREGROUND
  props) over the canonical layer stacks,
  with the one honest interleave documented —
  the ground plane paints after the backdrop
  it grounds and before the flats standing
  on it. Ordering pinned by the suite.
- Stage 2 (camera): SUBSTITUTED. A 2D canvas
  has no perspective FOV to narrow; the
  doll-house read comes from the multiplane
  parallax planes, walk-band depth scaling,
  and the stage bands. The fixed stage view
  is by design; there is no auto-orient
  analogue.
- Stage 3 (materials): IMPLEMENTED — see
  PAPER MATERIALS above. Procedural only;
  the scanned kit was not used in any form.
- Stage 4 (cut silhouettes): IMPLEMENTED —
  see SCISSOR-CUT SILHOUETTES above (figures
  and buildings; profiles keep their drawn
  masses, see Stage 5's note).
- Stage 5 (painted outlines): IMPLEMENTED —
  see PAINTED OUTLINES above. Scope note:
  front-view figures and all buildings carry
  the painted rim; side profiles keep their
  existing part shading and far-limb
  darkening (their masses are strokes by
  construction, and a rim there is a future
  refinement, not a silent skip).
- Stage 6 (flat lighting): IMPLEMENTED as a
  rule, by audit. The one-ambient + one-
  directional logic already lived in
  SceneLight. The audit's ruling: figures
  are never weather-worn or night-mixed —
  KidArt holds no wear transform at all,
  and every scene tint paints BEFORE the
  figure pass — so faces stay near-unlit and
  expressions legible at every hour, pinned.
  Shadows verified flat (projected ground
  shapes) and unchanged.
- Stage 7 (sorting): IMPLEMENTED. Scene
  sorting is the Depth stacks; within a
  figure, depth is part treatment, never
  draw-order luck — the profile's far leg
  rides offset and a shade darker, the front
  stack order is the sheets' own. Pinned.
- Stage 8 (post pass): SUBSTITUTED in part.
  Band desaturation cueing replaces the DOF
  blur (a real blur needs an offscreen pass
  per frame — not phone-viable; the wash is
  the accepted substitute and the action
  band is never washed). Vignette and grain
  implemented as above; boil implemented as
  above. Bloom: NOT PORTABLE at canvas cost —
  lamp and sign glow stay faked in the light
  layer, as before.
- Stage 9 (character assembly): IMPLEMENTED,
  by audit against 9.1–9.6. One shared rig:
  FigureGeometry derives every figure from
  its spec (9.1, 9.2 bone scaling = the
  derived plans; 9.3 flat parts = HeadStack
  ink layers). Faces and mouths are swaps:
  Expressions + the glyph mouth library
  (9.4) — with ONE real divergence found and
  fixed: the talking mouth used to
  interpolate its height per frame (a
  morph); it now swaps between two fixed
  glyphs on the speech phase. Limited
  animation discipline (9.5) stays in
  ActionAnims/MotionRegistry, unchanged.
  The New Kid is an appearance ID like any
  other (9.6) — the source game's class
  presets have no analogue here; this game
  has no classes, its creator is
  appearance-based. Flat projected shadows
  (9.7) verified.
- Stage 10 (separate UI/doll path):
  IMPLEMENTED — see THE PAPER-DOLL PATH
  above. PARTIAL #14 CLOSED with it.

THE FIDELITY AUDIT, REFRESHED: the
outline-weight gap is CLOSED (painted
outlines, above). The remaining named gaps —
house facades (flat vs the games' dressed
two-story), shop storefronts (glass fronts),
interior furniture language — now INHERIT
the paper materials and painted outlines
automatically through the shared painters
(every surface those painters touch wears
them as of this build); their STRUCTURAL
re-dress is the next build's scope and was
deliberately not started here. The
severe-weather mid-trip re-route quirk is
untouched, still open, still out of scope.

BUILDS: the Linux mirror is refreshed from
the same `src/` — `out/south-park-town-v0160d-linux/`
(run.sh / capture.sh / selftest as before) —
and the games-style reference sheets are
regenerated from the new rendering (the
figure rim and materials change the read;
sheets and street still cannot drift, being
one renderer). The Windows .exe is NOT
refreshed this build — it remains
v0.16.0b's artifact, as named in that
build's section.

SUITE NOTE (disclosed bound recalibration):
the lifetime soak's lifeText bound moves
310 → 330 in this build for a measurement
reason, not growth. This build adds NO
lifeText keys (it is a rendering build; no
simulation file changed). Investigation
showed the soak's micro-trajectory is
sensitive to compiled-artifact hash
ordering: the unmodified v0.16.0c tree
measured 298 in its own build environment
and 307 re-measured in this one, and class
overlays of untouched sources swing an
isolated soak's peak across 293–318. This
tree measures 316, so the bound tracks the
measured peak with the same ~4% headroom
the v0.16.0c move used (298 → 310). The
full reasoning is written into the check's
own message in the suite.

## Status — v0.16.0c

THE LINUX BUILD, THE WALK BETWEEN, AND TRUE
PROFILES (Queue #16, third build; versionCode
122). Lucky (Oct 18, 2026): "Make it work on
android and Linux so you can test it" — and the
Big Update's two carried partials close.

THE LINUX MIRROR (internal QA tooling, NOT a
shipped product): `out/south-park-town-v0160c-linux/`
is the identical game as a first-class,
launchable Linux build — `app.jar` + `shim.jar`
+ `kotlin-stdlib.jar` + `assets/`, packaged by
`tools/mirror/build_linux.sh` from the same
compiled mirror classes as every other mirror
artifact (ONE mirror implementation, the same
`src/` tree as the APK). `run.sh` opens the real
game window (mouse input, the game's own save
bytes at `~/.sptown-mirror`); on a headless box
it starts its own Xvfb. `capture.sh` is the
sweep harness. Verified on this box by actually
launching the packaged build: `run.sh --selftest`
opened the window under Xvfb, grabbed the frame,
delivered a mouse press at the window center,
and read the live engine state back (see the
build's `VERSION.txt`).

THE WINDOWS .EXE IS STALE, NAMED PLAINLY: the
.exe remains `out/south-park-town-v0160b.exe`
(v0.16.0b's game), so its payload was NOT
refreshed this build. (Correction, parent pass
Oct 19: the v0.16.0b Windows toolchain DOES
survive at ~/workspace/.tmp-v0160b/toolchain/
(jdk-win.zip + launcher sources) — the build's
first account said otherwise in error. What is
missing is an in-tree packaging script; an .exe
refresh from the recovered toolchain is a clean
follow-up task.)
The Linux build above — same classes, same
tree — is the current test copy; the .exe is
kept only as v0.16.0b's artifact.

MIRROR PARITY ACCOUNT (refreshed): shared —
the identical `src/` tree: model, Engine,
GameView/SceneArt/KidArt/InteriorArt renderers,
Prefs save format (the same bytes the APK
loads). Platform adapters — Android: the real
android.jar + the activities; desktop: the
`tools/mirror` android.* shim over Java2D/AWT
plus the MirrorMain host, which re-implements
GameActivity's wiring at GameView's callback
seams. Named divergences, unchanged: Java2D
rasterizer (not Skia); AWT font metrics;
widget-only screens (phone, paper, journal)
reachable only as far as the view layer goes;
audio stubs silent.

ITEM #12 CLOSED — CAST VISIBLY TRAVEL. A new
pure module, `Travel.kt`: when the engine's
dispatcher sends an agent between schedule
blocks, `Engine.updatePositions` stamps a trip
record in `lifeText` (new key `trip:<name>` ->
`origin|destination|startLen`, additive only,
cleared on arrival — the build's one new save
key family). Presence is derived purely from
the record + the agent's live grid walk: the
route is `Walk.route` over the street network,
progress is 1 − remaining/departure path
length (the grid walker IS the clock — nobody
travels instantly), and each route leg maps
half to its origin scene, half to its
destination, so a traveler enters a scene at
one edge, crosses its sidewalk band in profile
at scene scale, and leaves at the far edge.
`GameView.drawFigures` draws travelers only in
their pose scene, interleaved with the crowd by
feet line; settled crowds never include a
walker, so nobody pops mid-scene. The engine's
`location` field keeps its hub convention
mid-trip (forty-plus systems read it);
`Travel.pose` is the presence truth the street
renders and the suite pins — routes exist for
every home↔agenda pair, and a lived day is
pinned tick by tick: pose in a route scene,
progress monotone, records shed on arrival,
no settled pop, real arrivals counted.

ITEM #18 CLOSED — TRUE SIDE PROFILES FOR SLEEP
+ HERO LOOKS. The v0.16.0a stopgap (mirror-
flipping the FRONT view for the sleep/hero
looks when walking) is removed whole from
GameView; walking facing now flows to all three
looks exactly as it always has to the day look.
The sleep look needed no new art: pajamas and
the nightcap are spec geometry (Sleepwear's
derivation) and the profile pass draws them
natively, both facings. The hero look gains a
real profile overlay in KidArt: a mask band
wrapping the turned head from the nose side
past the ear (the front band's own eye
padding), one pupil looking out, and the
shared chest emblem foreshortened (×0.55) at
the chest's facing edge — geometry pure in
`KidArt.heroProfileGeom`, pinned per sheet ×
facing for the whole cast, mirror-symmetric.
The cape was already back-item geometry and
the profile pass already draws it behind the
torso. The front band is untouched, front-only.

GAMES-STYLE REFERENCE SHEETS (Lucky: "Make new
reference sheets using the South Park games
style"): `tools/mirror/SheetGen.kt` renders a
new sheet set from the game's own specs through
the game's own renderer — HEAD / TORSO / ARMS /
STAND / SIT / PROFILE / EYES OPEN / HURT /
SLEEP / EYELIDS / mouths / assembled, in the
games' cleaner construction read — so a sheet
and the street can never drift apart. The set
(98 cast + the New Kid avatar sheet + a live-
looks sheet + contact sheet) lives at
`~/workspace/goals/south-park-interactive-town/files/sheets-games-style/`,
beside Lucky's original uploaded sheets, which
are untouched. Sheets are study documents —
never bundled into the APK. They are this
build's character-style audit source of truth.

THE FIDELITY AUDIT (Lucky: nearly 1:1 with
Stick of Truth / Fractured But Whole in
BUILDING STYLE, INTERIORS, and CHARACTER
STYLE; town layout stays CUSTOM and is not
audited): the launched Linux build was swept
scene by scene and read against the games'
read and the new sheets. (Findings and the
named remaining gaps are recorded in the
v0.16.0c changelog ledger below.)

## Status — v0.16.0b

THE MAIN STREET CORRECTION + THE SWEEP (Queue
#16, second build; versionCode 121). Lucky
playtested v0.16.0a and sent four screenshots
of Main Street and the Bus Stop at 3:38 PM —
storefront-scale slabs burying the shop signs,
a teal SOUTH PARK band across the scene, crowds
piled over the player and a pine with pale discs
over their heads, clipped sign fragments, and a
giant profile adult at the frame's edge — then
ordered a sweeping test of everything, fixes for
everything found, and any features judged
missing. The full ledger is the changelog entry
below; the one-line version: the slabs were a
retired-in-this-build "storefront-row" detail
whose sizes composed from the widened WORLD
width instead of SceneScale's unit width — the
same disease, hunted through every token layer
(dig/fix/fair/carnival/faction/holiday/season/
light/parity) with a draw-log on the mirror —
and the sweep found the interior twin: layout
furniture drawn at 2.2× its own kid-unit ladder,
now drawn AT the ladder in every room. One name
per building, on the building; sign text fits
its board or ellipsizes whole; the snow lies on
each head's own derived crown; cast composition
reserves the player's ground; props and pines
keep out of a facade's lower band. The diner's
windows, which never drew (their band inverted),
draw. Suite: 109,016 baseline → 109,034 checks
/ 0 failures (suite-final1.log).

THE WINDOWS MIRROR (internal QA tooling, NOT a
shipped product): `out/south-park-town-v0160b.exe`
(36,341,684 B) is the same game built for
Windows so the Android copy can be bug-tested
on a desktop. It is NOT a fork: the exe's
payload is the mirror build of the identical
`src/` tree — same model, same Engine, same
GameView/SceneArt/KidArt/InteriorArt renderers,
same Prefs save bytes — compiled against a
small `android.*` compatibility shim
(`tools/mirror/shim`, Java over AWT/Swing) and
bundled with a jlink'd Temurin 17 runtime image
(java.base + java.desktop) behind a tiny
self-extracting launcher (`tools/mirror/` +
`build_mirror.sh`; the capture harness is
`MirrorMain --capture`, the same mode that shot
every capture this build was verified with).
Forced divergences, named honestly: the final
rasterizer is Java2D, not Skia; font metrics
are AWT's; GameActivity's Android widget UI
does not run — the host re-implements its
wiring at GameView's callback seams, so
widget-only screens (phone, paper, journal)
are reachable only as far as the view layer
goes; audio stubs are silent. Lucky receives
the APK only; the mirror stays in the
workspace as the test copy of the identical
game. What was verified here: the payload jars
boot and render from a clean extraction on
this Linux box, and every scene in the sweep
was captured through them; the Windows
javaw.exe launch itself cannot execute on a
Linux box and is verified by construction
(PE32+ launcher, intact payload, real Temurin
binaries), not by a Windows run.

## Status — v0.16.0a

THE BIG UPDATE — Lucky's own twenty-item
directive (Oct 18, 2026, given item by item and
confirmed with "K push the update"), opening
Queue #16 and the 0.16.0 base. Three new pure
modules carry it: `LivingTown.kt` (the universal
expression registry — three authored sets for
children, adults and animals, each expression
defined for the front view AND the side profile;
per-character motion parameters for the whole
98 cast plus every ambient species; the
action-animation registry; the wander system;
the game-clock rule), `InteriorLayouts.kt`
(every building's interior as a multi-room
layout of proportioned, usable furniture), and
`ResidentialStreets.kt` (every house on a house
street given an identity, a palette and its own
interior section). The bus is boarded, not
teleported: a real school-bus interior with
Crabtree at the wheel and a real public-bus
interior, both ridden by tapping the route
board. Adult legs stay whole while sitting
(FigureGeometry.seatedLegs). The off-road #84
bus sweep and the strollFor shuffle are removed
whole. New persisted state (additive only):
`lifeText["busRide"]` ("kind|from|to" while
aboard a bus) and `lifeText["insideSection"]`
(the street-house section the player is inside)
— no other save keys; no new TalkGate rail; no
agenda-seat change; MAX_TEXTS still 220; the
headline ladder still ten tiers. Suite:
104,373 baseline → 109,016 checks / 0 failures
(run5.log). Per-item account in the changelog
below.

## Status — v0.15.0k

Queue #15 (the town, seen clearly) closes with
POLISH & PERFORMANCE PASS #15 — the queue audit,
in the polish tradition: no new features, no new
module, the suite grows the audit section, and
the record is set straight. One unattended
lifetime lives every Queue #15 surface on one
save — the preview readers, the pen-pal
correspondence through both its seasons (her
visit, then your Pinewood trip, beat by beat and
thanked by home mail), the gallery's shows
hanging and wrapping, all five apprenticeship
chains driven to mastery on the same life, the
shared birthdays whole (both beats on all four
shared days; May 26 yielding jointly to the
finale week with the reason stamped; your own
birthday shared with Stephen on October 3, the
sharing stamped) — and all THIRTEEN cases
retire in that one life, the thirteenth behind
a wall that really hung in it. Veteran XIV
re-measured; contention re-pinned from source
(twenty-three agenda claims in Engine's own
order — the gallery's and the apprenticeships'
seats included — with your birthday consulted
at the sleepover seat; TalkGate at eleven
rails; MAX_TEXTS 220; the sender set closed;
the headline ladder at ten tiers; the decor
table at seventeen). The Queue #15 tuning
ledger is swept dial by dial. The four carried
gaps are dispositioned with evidence: the
paper's two unreachable-order pairs (fair over
finale, finale over opening night) CLOSED AS
DESIGNED — proven unreachable in one lived
calendar space, 2026–2100, both real predicates
in the same week space; group-post morning
granularity CLOSED AS DESIGNED — the landing
write has exactly one caller in the tree, the
morning harvest; the stand permit bend CLOSED
AS DESIGNED — the permit is City Hall's in
fiction, price and record, the pitch dialog
merely its collection point; arc #11's
COMMITTED texture CLOSED AS DESIGNED —
re-proven on the case's own derivation (graded
on arrival where the settler is live; COMMITTED
at the locked boundary; the same standing
reading PROVEN when the context arrives).
Performance measured and stated in the
changelog. QUEUE #15 COMPLETE with this build.

## Status — v0.15.0j

Queue #15 item 7: MYSTERY #13, THE EXHIBITION
CASE — the thirteenth case, "Not What the Frame
Says", and the show's own shadow. A print hanging
in the wrapped gallery show is not the print its
frame's record says it is; the case opens off the
gallery show (v0.15.0f) and nowhere else. The
discovery arises from the game's REAL provenance
data — the Photos album records, the Darkroom
print records, and the frame log the show itself
wrote (every entry's frozen record, its credited
entrant, its kind and its place on the wall) —
never from invented parallel data: the case's
checks read the same records the show itself
wrote, and its one authored layer is the physical
audit itself (what the wall's backs and screw
heads witnessed), the way the earlier cases
authored their own ground truth. The spine
is the provenance chain: SIX FRAMES in hanging
order, each read through THREE DERIVED CHECKS —
THE PRINT (what the piece in the frame IS,
against the album record or the darkroom copy it
was made from), THE CHAIN (where it CAME FROM —
album to darkroom to wall, and the standing
figure the hanging actually witnessed), THE FRAME
(what its FRAME claims — the credited entrant
against the entrant the records support) — every
finding recomputed on every read and stored
nowhere. Each frame is read TRUE HANGING, HONEST
CROSS or SWAPPED PRINT, priced at two live
evidence, graded by its one settling piece:
verdicts derive (OPEN / COMMITTED / PROVEN /
REFUTED), standing readings final, refuted
readings strikeable. THE EVIDENCE stands at
twelve — five witness tellings on the TalkGate
owed-lines seam (the gate stays at eleven rails),
five look pieces at the hall and the darkroom
drawer, two hands-on participation pieces inside
the case's own chapters — exactly TWO
CONTEXT-LOCKED settlers, the two load-bearing
frames', inert until their chapters air the
context that makes them legible. THE FINALE is
"The Wall, Read Back", a set piece at the
Community Hall in the pattern of the Reading: the
audit read aloud frame by frame, gated in tiers —
the full read wants both load-bearing frames
PROVEN, all six read, none refuted, at least four
proven; the name call wants one proven frame;
letting the wall stand and walking away are
always on the table — with the gate re-checked
at the lock and again at the wrap, so a finish
short of it degrades rather than being forced.
Four landings, one aftermath voice apiece on the
seam, the full-read landing's delayed tail a week
later: the wall quietly rehung true. THE ARMING
RULE: the case queues ten days behind the twelfth
case's retirement AND only arms when a gallery
show has actually wrapped with six entries of six
distinct photographs that this save attended,
visited, or entered — the audited show is the most
recent qualifying one, pinned the day the queue
anchors. A player who missed the show sees the
queue wait, unanchored; a save that never
exhibited never arms the case at all — it waits
for the next show rather than fabricating one.
The attic's CASE_TOTAL is 13; the lifetime soak
proves a thirteen-case lifetime green. New save
keys, additive only: `arc13` (chapter, due,
started, retired, branch, choice, retiredDay,
postPending, postDay, posted, showId),
`mystery13Clues`, `arc13Calls`. The tree is at
123 Kotlin files. 🔴 Structure only, here and
everywhere: the case's truth stays sealed inside
its own content.

## Status — v0.15.0i

Queue #15 item 6: SHARED BIRTHDAYS, WHOLE. The
registry's four shared days (July 1: Cartman and
Sheila; May 26: Kyle and Gerald; September 11:
Butters and DogPoo; June 20: Tolkien and Michael —
audited against the shipped table and pinned by name)
no longer resolve to the roster-first name alone.
What yielded before, stated plainly: the paper's
weekly list and the gift bonus were always per-person,
but the phone's birthday text went to the first name
only, the morning banner, the location flavor and
the free-time buzz named and orbited the first only,
the conversation's birthday greeting answered only
the first, and the party went to the first household
in roster order while the second birthday person's
evening was mentioned nowhere — no stamp, no
sentence, no observance. Now: the phone carries BOTH
beats (each birthday person is their own sender, so
the per-sender rule never eats the second; the beats
stand ahead of the group chats and the paper's
summary — the two beats that defer whole by their
own design — and ahead of the weather/hello chatter
a crowded morning sheds, and the inbox cap sheds
oldest read texts — re-proven across a full-census
shared week, both July 1 beats landing);
the paper names the day AS shared in TOWN NEWS
beside the week's list; the party arbitration is
explicit and STAMPED at rollover (`bdayPlan`): two
members of one household sharing a day are JOINT
honorees at one party whenever the evening is free
(both names on the welcome, the toast, the cake,
the wrap — each honoree paid the honor rate) — and
on the shipped calendar the one such pair, Kyle and
Gerald, always lands inside the spring season-finale
week (May 25–31), so their party yields for BOTH
names at once, with the reason, each man's standing
printed; the joint rule and May 26's whole-stamped
yield are both pinned in-suite. And every other
household holding a birthday member
marks the day at its OWN table that evening — its
members gathered by the same claim, its venue line
and night line written (`bdayObs*`), no friendship
paid (the family marked it themselves; the town pays
only at tables the player sits at). A birthday with
no dinner table (DogPoo, Michael) gets its standing
stated per person on the Journal's 🎂 page instead
of silence. And when the player's own day-counted
birthday lands on a cast birthday — on a fresh save
it meets Stephen's October 3 in years two through
five — both observances stand, both morning notes
name the doubling, and the sharing is stamped
(`bdayShare`) in the words the Journal prints. NO
new TalkGate rail (the party still rides DINNER),
NO new agenda seat (the observance rides inside
Birthdays' own gathering hook; the stack stands at
twenty-three claims), and the headline ladder
stands at ten. New state is stamps only, all
life/lifeText: `bdayPlanDay`/`bdayPlan`,
`bdayObsDay:<household>`, `bdayObs:<household>`,
`bdayObsWrapDay:<household>`,
`bdayObsNight:<household>`, `bdayObservances`,
`bdayShareDay`/`bdayShare` — plus `bdayPerson:<household>`
now holding every honoree's name, not just the
first. No new module (the tree stays at 122 Kotlin
files).

## Status — v0.15.0h

Queue #15 item 5: APPRENTICESHIPS — town jobs grow a
second tier. Work a job site steady and its keeper
notices: shifts completed at that one counter are
counted off Jobs' own clock-out (the Capstones seam —
Jobs calls `Apprenticeships.onShiftCompleted`, and
`Jobs.weekPay`'s own line pays a mastered site's
master's rate), and at the threshold the site's NAMED
mentor — McDaniels at City Hall, Randy at Tegridy,
and the shop rota's three keepers (Tweek, Jimbo,
Chef), each the keeper Jobs' own data seats there —
offers a five-stage chain in person: the first
morning, a routed errand leg across town on the Walk
graph (a real walk to a real person, delivered by
Talk wherever they stand), the deep work, a judgment
with three honest roads (the fact-finding, the
decent, the hasty — the master's read prices the
stipend, never the completion), and the signing of
the mark. Day-gates (0/2/3/4/7) floor the chain at
sixteen days: it cannot be blitzed. Each site's
SKILL deepens by stages (Route Sense, Tegridy Hand,
Steady Hands, Counter Craft, Kitchen Rhythm), the
Journal's 🎓 APPRENTICESHIPS page shows counts,
chains and marks, and a completion is remarked on
three ways — the mentor's standing line, the
neighbors' gossip for three weeks, and one TOWN
NEWS line in the completion's week (section copy;
the headline ladder stands at ten). ONE new agenda
claim — the mentor's hours, 9-to-5 while an offer
stands or a stage waits on them — seats after Jobs
(the stack stands at twenty-three claims); NO new
TalkGate rail (the beats ride below the gate whole,
in the dialogue branch, like the clubhouse rides
the crew-talk channel); NO phone trigger (the
closed sender set stays closed, MAX_TEXTS 220
unmoved — a signed mark is public the way work is
public: in person and in the paper). One chain at a
time; declining is free and the offer returns
tomorrow; quitting costs nothing but the chain and
the count still stands. One completion per site.
Zero new save keys — the record rides the existing
`life`/`lifeText` maps (`appr*` keys, every read
clamped). One new module: `Apprenticeships.kt` (the
tree's 122nd Kotlin file).

## Status — v0.15.0g

THE SCALE & SPACE BUILD — Lucky's playtest round
2 on the overhaul (screenshots of v0.15.0d/e,
confirmed complete by him Oct 18 with "That's
everything — build it next"; playtest corrections
outrank the roadmap, so this build jumps the
Queue #15 line and item 5, apprenticeships, goes
next). Eight corrections, all measured against
his screenshots: areas 2–3× bigger, depth scaling
by position, the bus-stop crowd pile dissolved,
a default home for every cast member, a road you
can actually see, less traffic, sidewalk-only
walking with crosswalks, and button labels that
no longer wrap mid-word. Two new modules:
`CastHomes.kt` and `FigureCompose.kt` (the tree's
120th and 121st Kotlin files). **Zero new save
keys** — homes are derived data, and everything
else here is composition.

- **AREAS 2–3× BIGGER (`SceneScale`).** Every
  scene's world widens: interiors 2.0×, street
  scenes 2.5×, open land (pond, farm) 3.0× the old
  1.7-screen world — world widths of 3.4 / 4.25 /
  5.1 screens. Figures keep their v0.15.0d natural
  sizes; the world grows around them. Composition
  that was fraction-tuned to the old width (facade
  and structure bodies, dash rhythm, mow stripes,
  drift widths, seam spacing, the corner sign)
  anchors to the *unit width* — the pre-widening
  world width — so a building keeps its composed
  size at the scene's heart. Density stretches
  honestly: the neighbor row's building count and
  the standing-prop spots scale by the growth
  (the baseline's named buildings still stand
  nearest the facade; the row and the props tile
  on past them at the same per-screen density).
  GameView's old `WORLD_SCALE = 1.7` constant is
  retired into `SceneScale.OLD_FACTOR`, where the
  composition tables still read it as their unit.
  Structures (shelter, pavilion, barn…) moved from
  the narrow world's 0.68 offset to the scene's
  heart — in the wide world the offset stranded
  them down the street from the door, the crowd
  window and the name sign.
- **DEPTH SCALING (`WalkBand.depthScale`).** One
  continuous curve from feet position: **0.75× at
  the back of the band (up by the houses) swelling
  to 1.15× at the front (down by the street)** —
  his words: "if my character walks up to the
  house then he appears smaller against the house
  but when he's next to the street he's bigger."
  The same curve composes the player, every crowd
  figure (inside FigureCompose, at each figure's
  own feet) and every standing prop (their private
  0.82+0.18 breath is retired). Because depth
  multiplies everything at the same band by the
  same factor, the v0.15.0d ladder (house > tree >
  car > adult > kid > trash) holds at EVERY depth
  — pinned at five sampled bands, plus adult-vs-kid
  pairs at three depths in composed crowds. The
  old 0.80→1.00 curve and the crowd pass's
  per-row `rowScale` step are superseded by the
  one curve (re-pinned with reasons in the suite).
- **THE BUS STOP CROWD PILE (`FigureCompose`).**
  The blob had three causes and all three are
  fixed. (1) The whole town SPAWNED at the stop
  (see homes, below) — the 08:00 crowd is now the
  scheduled set: `SchoolBus.ridersAt` reads the
  engine's own effective agenda block, and the
  crowd at the stop is exactly the kids routed
  there plus the driver on her route (and any
  adult whose block is the stop — Barbrady's
  patrol). (2) Slot pitch: figures now compose
  through one pure pass in a crowd WINDOW 3.4
  screen-widths across the widened world, rows
  re-chunked to the capacity the window affords
  at body pitch (max 7 rows), and a spacing sweep
  holds every same-row pair at body pitch —
  bodies may brush shoulders, never merge; the
  suite pins the minimum pairwise pitch in the
  real 08:00 crowd's composed geometry. (3) The
  bus: waiting figures are pushed clear of the
  standing bus's span (`FigureCompose.busSpanFrac`
  off GameView's one `BUS_FX`), so the traffic
  plane can never cover a waiting body again —
  pinned: no composed rider intersects the bus.
- **DEFAULT LOCATIONS (`CastHomes`).** All 98
  cast have a HOME — spawn at day start, the
  night march's destination, and the default
  anchor whenever no agenda block claims them.
  The derivation, residence first per Lucky's
  "preferably house": (1) RESIDENCE, 22 rows —
  where the game's family data holds a house
  (the six family houses: Marsh, Broflovski,
  Cartman, McCormick, Stotch, Black), home is
  that house; (2) OCCUPATION, 75 rows — everyone
  else anchors at the workplace the town's data
  already records (students and staff at the
  Elementary, Mackey included; the Tweeks at
  Tweek Bros; officials at City Hall; clergy at
  the Church; Jimbo and Ned at the gun shop;
  Main Street for its working adults); (3) ONE
  HAND-PLACED ROW — Ms. Crabtree: her recorded
  anchor was the Bus Stop itself, and nobody
  defaults to the stop, so the driver anchors at
  the Elementary, where the bus sleeps. NOBODY's
  home is the Bus Stop — a character is there
  only when a schedule or the bus run puts them
  there. The 98-row map lives as data in
  `CastHomes.kt`, suite-pinned whole. The player
  still arrives at the stop (the intro beat).
- **THE ROAD IS VISIBLE.** The road is now a real
  band: asphalt from 0.885 of scene height to the
  bottom edge — **11.5% of the scene** — with a
  curb at both edges, worn center dashes, and
  zebra crossings striped across it. Vehicles
  drive on it (bus in the far lane at the curb,
  cars on their lanes inside the band). The walk
  band's front edge moved to the sidewalk's edge
  (0.875) to make room: `WalkBand.BOTTOM_OUT`
  0.925 → 0.875, `Depth.FG_TOP_OUT` 0.932 →
  0.885, `Depth.FRONT_FEET_OUT` 0.905 → 0.875
  (re-pinned with reasons; the occlusion budget
  still clears the worst chin by ≥ 0.15).
- **LESS TRAFFIC (`Traffic`).** The ambient rate
  is a pure derivation now: one pass slot per lane
  every 1,140 ticks (the crossing still takes
  210 — same driving speed). Expected passes per
  scene-minute: **≈ 7.1 → ≈ 2.1 (0.30× the old
  rate)**, and a lane stands empty ~82% of the
  time. Less, not zero — his word, read literally.
  The old inline 340-tick loop in GameView is
  removed.
- **SIDEWALK WALKABLE, ROAD NOT — EXCEPT
  CROSSWALKS.** Outdoors, the walkable band is the
  sidewalk strip + yards (bands 0–1, as composed);
  the road strip is bands 1–1.5 and rejects
  movement: a tap on the road clamps to the
  sidewalk at the tapped x — EXCEPT inside a
  marked crosswalk, where feet may cross to the
  far curb and back. Crosswalks are one data
  table (`PaperDressing.crosswalks`) that BOTH
  the zebra painter and the tap gate read, so
  markings and movement can't disagree: one at
  each street scene's ends, one at its heart
  (every facade's door, the station and the
  school front face the heart span), plus two
  mid-block crossings downtown. Bus boarding
  still resolves to the curb — a thing's standing
  spot is never on the road.
- **BUTTON LABELS (`BarFit` + GameActivity).**
  The bottom bar's seven buttons (Look / Talk /
  Selfie / Pack / Journal / Phone / Menu — none
  renamed, none dropped) are now single-line with
  auto-size text (14sp down to an 8sp floor),
  zeroed horizontal padding and min-width, and
  slimmer bar margins: the label shrinks to fit
  its share instead of breaking mid-word.
  `BarFit` (in Chrome.kt) carries the shipped
  layout numbers and a stated advance model, and
  the suite proves every label fits its share at
  the floor on a 320dp screen and settles at
  ≥ 9sp at 360dp — the strongest pin the layout
  code allows.

## Status — v0.15.0f

THE GALLERY SHOW (Queue #15 item 4). Four times a
year — once a season — the Community Hall hangs a
photo exhibition: a theme derived from the season
and the year, entries from your REAL album and
your held darkroom prints, a jury of three in the
yearbook-judge pattern, a hanging night as a set
piece, and the winner's frame holding the wall as
champion until the next show. New module:
`Gallery.kt` (the tree's 119th Kotlin file).

- **THE VENUE.** The town has no separate Community
  Center building; its community hall is at City
  Hall — the hall Mystery6's audit case already
  names ("COMMUNITY HALL", signed out by its own
  key). The show hangs there: the exhibition wall
  is drawn on the hall's long wall inside City
  Hall, left of the request board, and every
  surface calls it the Community Hall at City
  Hall. No 28th location was invented for it.
- **CADENCE.** One show per season. Submissions
  open on the season's first morning and close at
  the hanging — the SECOND FRIDAY of the season's
  middle month, 6–9 PM (Dance's formula). The show
  hangs seven days; the wrap lands the next
  morning. Winter is named for its December and
  straddles the year turn. If hanging night's
  evening belongs to the town (a case climax, a
  finale, a holiday beat), the frames go up
  quietly at dawn and the record says so — no
  date ever slides.
- **THEMES.** Twelve written themes, three per
  season, rotated by the show's index so a season
  never repeats last year's theme. Every theme's
  fit is a stated formula over the record's OWN
  fields (place, weather, sky by the yearbook's
  derivation, hour band, who stood in it and how,
  the commission stamp) — a theme can always be
  answered by a photograph the town can take.
- **ENTRIES — REAL OR NOTHING.** You enter up to
  two frames: album photographs taken THIS season,
  or darkroom prints you HOLD — prints are
  season-free, judged from their frozen copies,
  and LENT, never consumed (they come home when
  the show comes down; while lent, the darkroom
  won't gift them or hang them at home). Cast
  entries derive from the same album: a photo a
  cast member commissioned, or a frame they stood
  in (a photobomb is not a sitting), best-first
  down a stated ladder, cap 8, no photograph
  twice on the wall, jurors never entering their
  own show. Entries are stored as copies, so the
  wall survives the album cap. Own nothing
  eligible? The show hangs around you, and every
  surface says so; a season with no photographs
  at all hangs a bare wall, honestly.
- **THE JURY.** Six townsfolk in two leaning trios
  (craft / heart / light), rotating by season and
  year; the chair is the trio's first name. A
  score is derived, never rolled: twice the theme
  fit + the craft read + the three leanings at
  face value + 3 for a hand-developed print entry.
  Placements run score → fit → craft → the wall's
  own order. Jury notes are templates whose every
  factual claim is a slot filled from the record
  — the suite audits them the yearbook way.
- **PRIZES.** A keepsake first: the winner's
  frame is the hall's champion until the next
  hanging. Your win pays 10 🪙 from the City Hall
  arts line (the festival's free-admission
  precedent; entry is free) at the wrap; a cast
  winner takes +1 friendship at the wrap ONLY if
  you kept the hanging or came to see the show —
  the festival's played-through rule. Pays land
  exactly once.
- **THE CLAIM.** One new agenda claim, seated
  after the film festival and before the
  sleepovers (the stack grows to twenty-two
  claims; the clubhouse's call becomes the
  twenty-second in the stack — both pins re-pinned
  with the reasoning written at them): hanging
  night 6–9 PM and the show days' visiting
  afternoons (12–4) hold the jury, the entrants,
  and everyone standing in a hung frame. It
  stands down for a finale evening, a holiday
  beat, or a moment the town owns. TalkGate keeps
  its eleven rails and never learns the show's
  name.
- **PAPER + PHONE.** The paper gives the show
  TOWN NEWS lines — the announcement in its week,
  the winner in the closing week's edition — and
  NO headline tier (the ladder stands at ten;
  the festival precedent). The phone carries ONE
  text on the wrap's own morning under the new
  GALLERY trigger: from the winner, or from the
  jury's chair when the winner is you; an empty
  show sends nothing. MAX_TEXTS stays 220 — one
  text a season cannot press the worst week.
- **SAVES.** Exactly ONE new persisted key:
  `"gallery"` (the show records; the newest four
  wrapped shows whole, elders reduced to their
  winners line — the yearbook shelf's rule).
  Decode drops a show whose theme or season the
  tables don't know. Hanging-beat progress rides
  a pruned life key, like the festival's session
  keys. Nothing else persists: phases, scores,
  placements and the champion all derive.

## Status — v0.15.0e

THE LOGO BUILD. Lucky asked for a new app logo
(Oct 17 ~22:00 CDT), approved the artwork the same
night (~22:23), and pre-authorized this build
("just build it when ready"). No gameplay or
system changes of any kind — the approved sign,
wired in as the launcher icon and the title art,
plus the plumbing and pins that prove it.

- **THE ARTWORK.** A wooden town sign on two posts
  — SOUTH PARK on the main board, TOWN on a hanging
  plank — against layered paper mountains, pines
  and snow, drawn by Hunt3r as SVG in the game's
  paper cut-out style (the same idiom as the
  v0.15.0c overhaul). Two renders, approved as-is:
  the square icon (512×512, sha256 b9b9191a…) and
  the wide title art (1200×760, sha256 64be1865…).
  Neither was redrawn, recolored or resampled for
  this build — the files that ship are the files
  Lucky approved, byte for byte, and the suite
  hash-pins them.
- **THE LAUNCHER ICON.** `android:icon` still
  resolves to `@drawable/ic_launcher`, and that
  drawable now IS the approved icon (a single
  high-res drawable, as this pipeline has always
  shipped it; the artwork carries its own
  rounded-corner treatment — no extra mask crops
  the sign). The canonical master lives at
  `tools/logo/south-park-town-icon.png`, and the
  manifest carries no `roundIcon` (it never has),
  so nothing else points at an icon.
- **THE TITLE ART.** The splash (`MainActivity`)
  frames the wide logo on its parchment plaque,
  decoded from the bundled asset
  `assets/title_logo.png` via
  `BitmapFactory.decodeStream` — no network, no
  code-drawn substitute. The splash holds for the
  same beat and routes exactly as before (saved
  kid → GameActivity, new kid → CreatorActivity);
  nothing interactive moved or was overlapped.
- **Superseded suite pin, named:** the v0.2.0c
  photos pin ("no image files added — the only
  picture is the drawn icon") now admits exactly
  one more picture, the approved title art. Every
  other pin passes unmodified except the version
  pin (113 / 0.15.0e), which moves every build.
- **Removed in v0.15.0e** (Lucky's rule: old
  assets don't stick): the old code-drawn launcher
  icon (its bytes are gone from `res/drawable`)
  and the whole drawing half of
  `tools/gen_icon.py` — the script now only
  installs the approved master verbatim (and
  hash-checks it first), so no build step can
  redraw the icon; and the splash's old lettered
  title (the snow emoji and the SOUTH PARK /
  LIVING TOWN text views).
- **Saves:** ZERO new persisted keys. Fan sheets
  NOT regenerated — no spec or sprite data changed.

## Status — v0.15.0d

THE OVERHAUL CORRECTION. Lucky's on-device playtest of
v0.15.0c ("better but not right") named the defects from
his own screenshots; this build fixes them as one
correction and adds no features. Step 0 came first: the
stopped gallery-show build's partial work (`Gallery.kt`
and its wiring) was removed whole, the manifest restored
to 111 / 0.15.0c, and the restored tree re-proven green
(102,923 checks, 0 failures) before any correction code
was written. The gallery show stays re-queued.

- **ONE WORLD SCALE (`WorldScale.kt`, new).** Every
  sized thing in a scene composes from the figure: a
  kid is `KID_FRAC` 0.19 of scene height (Lucky's
  18–22% band); adults compose at 1.42× through their
  own measured `heightRel` (cast census re-pinned: every
  adult ≥ 1.28, every kid ≤ 1.15, teens between). The
  ladder, in kid units: house peak 2.62, pine 2.15, car
  length 2.00, adult 1.42, kid 1.00, trash can 0.40;
  doors at 1.73 (1.22× an adult). Deliberate exceptions
  are named in the data, not accidents: the sapling
  (0.80) and the Tegridy silo (3.10, a landmark). The
  crowd pass now sizes off `kidPx` with at most an 8%
  dip for slot width; the old head-clearance clamp —
  the adult-shrinker — is removed.
- **THE WALKABLE BAND (`WalkBand`).** Movement clamps
  to a per-scene ground band: outdoors, feet lines run
  0.685–0.925 of scene height (in front of the 0.66
  building line, short of the 0.932 street); indoors
  0.675–0.905, on the floor. Crowd rows pitch inside
  the band; walk targets clamp to it; standing-prop
  footprints (`PaperDressing.propSpots` /
  `footprintsFor`, the same spots the painter reads)
  block — a tap inside a footprint slides to its near
  edge. Feet never render above the band's top line.
- **DOORS ON FACADES; VEHICLES ARE VEHICLES
  (`FacadeGeom`).** One door geometry: SceneArt paints
  the door ON the facade, base on the ground line;
  GameView pulses and hit-tests the same rect. Outdoor
  and hub locations front the street with a drawn
  STRUCTURE carrying the entry — the bus shelter, the
  park pavilion, the ice-fishing hut, the Tegridy barn,
  the SoDoSoPa loft, the Main Street diner. The bus
  composes at ladder size (3.40 long × 1.50 high: long
  body, window band, wheels, a side door); cars at
  2.00 long. No scene composes a door or a vehicle as
  a lone rectangle anymore.
- **THE BUS RUNS.** The boarding failure was tap
  resolution: a kid standing at the curb (figures
  resolved first) and the exit signposts (registered
  last) both outranked the bus, so tapping the school
  bus talked to a kid or walked you away. `PaperTap`
  now resolves vehicles first — they ride the traffic
  plane, in front of the crowd — then figures by
  nearest center, then topmost. The suite pins the
  real verbs end to end: `SchoolBus.ride` lands the
  player at the Elementary on the morning run and
  back at the Bus Stop in the afternoon; `Walk.ride`
  lands every one of the 90 public station pairs.
- **THE BUS INTERIOR.** "Inside the School Bus" is
  rebuilt off `BusInteriorSpec` as a layered paper
  interior: ceiling band, a window band with the
  street rolling past outside, the route board, the
  driver area (dash, wheel, rear-view mirror), seat
  rows on the back furniture plane, a ribbed aisle
  floor, near seat backs on the front plane, the rear
  exit door. The player stands in the aisle at figure
  scale. The blank yellow void with floating shapes
  does not survive in any form.
- **GROUND WITH STRUCTURE.** The ground plane is
  built from pieces now: a yard band in front of the
  buildings, a packed path from each door to the
  sidewalk, sidewalk slabs with seams along the
  street, and snow laid as COVER — drift edges banked
  along the building line and the curb (summer: grass
  with mow stripes; fall: raked leaf drifts). The
  speck scatter is removed entirely.
- **THE MERGED-MAP BASELINE TOWN (`TownBaseline.kt`,
  new).** Lucky's two references — the IGN Stick of
  Truth map and the Fractured but Whole fandom map —
  merged into ONE baseline: every building on either
  map appears once. (The IGN interactive map blocks
  text fetches and the fandom page returned almost
  nothing machine-readable, so the merge follows the
  widely documented Stick of Truth layout, cross-
  checked against the Fractured but Whole geography;
  the game's own long-standing grid agrees street for
  street.) The baseline is the town's natural state:
  street arrangement, each building's look (baseline
  colors + facade signs, drawn new in the paper
  idiom), neighbor rows showing the ACTUAL adjacent
  buildings, and a corner street sign naming the
  street you're on. Event/season/turf/mark systems
  keep layering onto it exactly as before. Priced
  travel and the economy are untouched — no route
  flatly contradicts the baseline. The baseline,
  street by street:

  | Street | West → east |
  |---|---|
  | The boys' street | Kenny's House · Stotch Residence · Cartman's House · the Bus Stop · Marsh Residence · Broflovski Residence · Your House |
  | Rich Row | a mansion · Tolkien's House · a mansion |
  | Main Street | Skeeter's Bar · Freeman's Tacos · Tweek Bros. Coffeehouse · Tom's Rhinoplasty · Post Office · Main Street Diner · Bank of South Park · Jimbo's Guns · City Wok · Raisins · Bijou Theatre · South Park Mall |
  | Civic Row | South Park Church · City Hall |
  | School Row | South Park Elementary · Kid's Park |
  | The outskirts | Stark's Pond · SoDoSoPa · Tegridy Farms |

- **Saves:** ZERO new persisted keys. Fan sheets NOT
  regenerated — no spec or sprite data changed.
- **Removed in v0.15.0d** (Lucky's rule: old assets
  don't stick): the speck scatter (the ground's
  shadow dabs and the seasonal confetti); the
  free-standing door slab and its `drawEntryDoor`
  branch; the crowd head-clearance clamp; the old
  figure base and band math (`h × 0.205`, the
  `× 1.62` band span that put feet on the facades);
  the 46px shrub pines; the old neighbor-row heights
  (0.145–0.20 of scene height — houses shorter than
  the people in front of them); the sub-scale traffic
  (cars at 0.088 and the bus at 0.30 of world width);
  the generic-room bus interior (the yellow void);
  and the stopped gallery build's partial work
  (Step 0). Suite source-scans pin the retired
  pieces' absence.

## Status — v0.15.0c
THE VISUAL OVERHAUL — Lucky's direct playtest
directive (Oct 17), which preempts the roadmap:
the gallery show is re-queued behind this build.
The first-person POV is GONE. Every scene is
rebuilt from scratch as a PAPER CUT-OUT stack,
the movement system is redone around a player
who stands IN the scene, and the bus works —
the school run for real, plus a public network.
Two new modules (`PaperScene.kt`, `BusNetwork.kt`);
the tree stands at 116 Kotlin files. Saves
additive-only — in fact ZERO new save keys: the
walk state is transient scene state, and the
school-bus counter rides the existing `life`
map. Verified: the full JVM smoke suite passes
(baseline 102,291/0 on the untouched v0.15.0b
tree), and the clean APK builds and signs with
the standing key.

- **The layer model (`PaperScene.PaperPlane`).**
  Every exterior scene composes back to front:
  SKY (sky, sun/moon, stars, weather) → FAR
  (the mountain range + the Horizon silhouettes,
  parallax 0.55) → BUILDINGS (the location's
  facade lined with a neighbor row, parallax
  0.85) → GROUND (the ground plane + the street
  strip along the very bottom) → PROPS → FIGURES
  (the y-sorted crowd, you included) → TRAFFIC
  (cars and buses on the street, foreground) →
  FOREGROUND (the near frieze + an occasional
  near prop overlapping the street edge). The
  bands agree with `Depth`'s chain, which gained
  the `neighbors`, `street` and `traffic` layers.
  Parallax: the far range and the neighbor row
  ride their plane factors against the camera;
  the ground and everything standing on it are
  world-locked at 1.0. Interiors stack WALL →
  FLOOR → FURNITURE_BACK → FIGURES →
  FURNITURE_FRONT (`PaperRoomPlane`). Every
  cut-out piece keeps the paper idiom the scene
  painters already used: an offset paper shadow
  and a slightly darker cut edge.
- **A town, not a house (`PaperDressing`).** A
  data-driven dressing table dresses all 27
  locations systematically: the facade PLUS a
  neighbor row across the background plane
  (seeded houses with lit windows; downtown
  gets tall parapet buildings with storefront
  glass, awnings and sign boards), the street
  strip at the very bottom, and props by zone.
  Main Street is a commercial district; the
  pond and the farm have no street and no
  traffic. The suite pins a dressing census
  (facade + neighbors + props + far row ≥ 8)
  for every location, and ≥ 5 buildings across
  every street scene. Season, weather and time
  dressing (snow, night windows, lamps) ride
  the new painters exactly as before.
- **Figures, layered right (`FigurePieces`).**
  People are paper cut-outs assembled from the
  existing measured sheet specs — no proportions
  were invented. Within every figure the pieces
  stack in the sheets' assembled order: back
  hair/backpack → legs → torso → arms → head
  base → eyes → mouth → face extras (beard,
  glasses, brows, freckles) → hair → hat. The
  suite asserts that order for all 98 cast specs
  and proves, from `HeadStack`'s own geometry,
  that no hair or hat shape ever covers an eye
  or mouth anchor (`faceClear`) — Kenny's parka
  is the one covered mouth, by his sheet. Kids
  measure to the New Kid calibration; adults to
  head ~1/3, torso just under, legs just over.
- **You, on screen; movement redone
  (`PaperWalk`, `PaperTap`).** The New Kid
  stands in every scene, wearing what
  `LiveLook` says (day clothes, sleepwear on a
  live sleepover night, the hero costume in the
  patrol window — the mirror/preview/album
  derivation, now feeding the street too). Tap
  anything and the kid WALKS there — paper walk
  cycle (bob + phase from the walk machine),
  facing flipped by direction, camera following
  — and the interaction fires on arrival through
  the same callbacks the instant tap used.
  Figures resolve by nearest center (the crowd
  rule), other things topmost; the ground-plane
  draw is y-sorted, so you pass in front of and
  behind people and props correctly. Intra-scene
  walking is animation: it costs ZERO
  game-minutes, pinned. Inter-location travel
  keeps Walk's prices untouched.
- **The bus works (`BusNetwork`, `SchoolBus`).**
  The school bus stands at the Bus Stop on
  school mornings (06:50–08:20) and at the
  Elementary in the afternoon (15:05–16:40):
  walk to it, it pulls up, you ride — no pass
  needed. The PUBLIC network stands at 10
  stations (Bus Stop, Main Street, Elementary,
  City Hall, the Mall, Kids Park, Stark's Pond,
  the Church, SoDoSoPa, Tegridy Farms): tap a
  station sign, walk to it, the bus arrives,
  pick a destination, ride, step off there.
  Pricing is derived from the Walk graph at bus
  speed — `ceil(route raw minutes × 2/5)`,
  floored at 10 — so the bus beats walking any
  real route and never teleports; the parade
  through-street delay is never charged to a
  ride (the standing exemption). **This changes
  `Walk.rideCostMinutes`**: the old flat rule
  (the route's first leg, any distance) is
  retired; the Go menu and the stations charge
  the same derived price.
- **Re-pinned rendering contracts (reasons at
  the pins):** the v0.5.0a bus-price pins (the
  flat first-leg price → the bus-speed
  derivation, Lucky's steering); the v0.5.0g
  crowd tap pin (the nearest-center rule's home
  moved to `PaperTap`); the v0.14.0a reflections
  pins and the v0.15.0a preview pin (the
  spot-gated street costume and the
  outdoors-gated street pajamas are retired with
  the POV — `LiveLook` now has two readers in
  GameView, the mirror glass and the player
  figure, and `Heroes.streetCostumeCode` /
  `Sleepwear.streetCode` keep their Photos-side
  lives and pins); the manifest/file-count pins
  (111 / 0.15.0c, 116 files). Every behavioral
  pin — economy, gates, derivations — stands
  untouched.

## Status — v0.15.0b
Queue #15 continues with THE PEN-PAL VISIT,
RETURNED — the correspondence's second season:
the program alternates, and the top band's
second settlement sends YOU to Pinewood Flats.
No new module — `PenPals.kt` grows the season;
the tree stands at 114 Kotlin files. Saves
additive-only (the `palTrip*` keys). Verified:
the full JVM smoke suite passes (baseline
102,237/0 on the untouched v0.15.0a tree), and
the clean APK builds and signs with the
standing key.

- **The direction rule.** A top-band settlement
  stamps her visit (season one's shape) UNLESS
  she has already visited and no return trip
  stands on the books — then it stamps YOUR
  trip instead, and the invitation lands as a
  home letter, answerable any day in any season
  (the polish #14 rule rides whole). Below the
  band in a return season, the maybe-next
  letter is reworded for a second year: the
  jar stays on the counter, filling.
- **The plan, over days.** Her ordinary letters
  through a return season carry the plan one
  line at a time — the alternation, the JAR II
  fund, the route committee — so the invitation
  is built by the correspondence before it
  lands at the settlement.
- **The seam, in-fiction.** The trip starts
  June 16 — fifteen days after the June 1
  settlement — and the letters say why: the
  program's travel days ARE the seam between
  school years. Stated, never shortened.
- **Four hosted beats, one a day** (June
  16–19): the Turnaround (arrival, the journey
  narrated), the Tour, Counter Duty at the
  bait-and-gas, the Goodbye. Wren hosts in her
  own voice through her sheet machinery,
  anchored at the mailbox — the
  correspondence's front door — because the
  town map is fixed and gains no Pinewood
  locations. Each kept beat pays +2 in her
  warmth lane only; dormancy and revival ride
  unchanged; the town doesn't pause, exactly
  as it didn't for her visit.
- **No Pinewood photo, stated plainly:** the
  album's camera composes from where the
  player actually stands in town (`Photos`
  reads the player agent's location), so no
  print is forced. The trip's keepsakes are
  the letters themselves — the invitation and
  the thank-you kept whole on the 📬 page.
- **The symmetry home.** The morning after
  the trip, her thank-you rides home mail,
  answerable in any season — the exact shape
  of the letter she sent after visiting you.

## Status — v0.15.0a
Queue #15 (the town, seen clearly) opens with
THE PREVIEW PASS — the character-creator preview
and the mirror preview now show the look that is
actually live. One new module (`LiveLook.kt`);
the tree stands at 114 Kotlin files. Zero new
save keys; deciding a preview writes nothing.
Verified: the full JVM smoke suite passes
(baseline 102,202/0 on the untouched v0.14.0g
tree), and the clean APK builds and signs with
the standing key.

- **One derivation (LiveLook):** sleepwear on a
  sleepover night (the code is Sleepwear's own —
  the mirror's code, the shutter's player branch),
  hero costume on a patrol night (the code is
  Heroes' own player derivation), day look
  otherwise. The creator preview, the Journal
  YOU card and the Your House mirror glass all
  read it; in the creator the costume codes
  derive from the look UNDER EDIT, so changing
  your top on a sleepover night re-derives your
  pajamas from the edit. The preview's hero
  moment is the patrol window, not the spot —
  the mirror hangs at your house, which is not
  a spot; the street keeps its spot gate.
- **Pajamas on the street, in their one window:**
  on a live sleepover evening, stepping outdoors
  (the walk-home/escort window, while the night
  is still up) draws you in your sleepwear in
  street POV, and a street shutter freezes the
  same code — never in daytime, never indoors,
  never as a selectable outfit: no creator
  option and no wardrobe piece names sleep.
- **Two standing gaps CLOSED** (the creator
  preview showing only the day look, carried
  since v0.14.0a's statement of it; pajamas
  never on the street itself, carried since
  v0.12.0d). The changelog entry below is the
  record.

## Status — v0.14.0g
Queue #14 (the town, in the mirror) closes with
POLISH & PERFORMANCE PASS #14 — the queue audit,
in the polish tradition: no new features, the
suite grows the audit section, the record is set
straight, and the queue's two standing pen-pal
debts are dispositioned.

The one production change is disposition one:
the pen-pal post-settlement reply window is
FIXED in-fiction. The special letters — the
settlement's word, the thank-you after the June
visit — arrive at the HOME mailbox, not the
school box, so a reply now rides home mail:
`PenPals.deliverSpecial` stamps `palHomeMail`,
`canReply` honors it in any season, the send
clears it, and a fresh October signup folds a
still-waiting home letter into the archive.
Dormancy rides whole (a home-mail answer to a
dormant route is the revival letter). Before the
fix, polish #13's soak closed 61 of 62 letters
answered for exactly this seam; this audit's
soak closes the correspondence whole, 62 of 62.
Disposition two closes the fabricated-record
suite gap for the dance/talent/parade/carnival
pen-pal fact kinds with staged records through
the modules' own paths. The audit over the
queue: a 400-day soak over the reflections
readers, all three trips, the festival week,
the first birthday and the clubhouse afternoons;
the TWELVE-case lifetime re-driven (380 days);
Veteran XIII at 593,915 chars against the
655,360 ceiling; contention re-pinned from
Engine's source (twenty-one agenda claims, the
birthday party consulted at the sleepover seat,
TalkGate at eleven rails); the phone's worst
week with the three new triggers in and the
clubhouse's absence re-pinned; and the tuning
ledger completed for every Queue #14 dial.
Gaps are consolidated in the changelog, stated
plainly. Saves additive-only (one new
`palHomeMail` stamp inside PenPals' own key
family). QUEUE #14 COMPLETE with this build.

## Status — v0.14.0f
Queue #14 (the town, in the mirror) continues with
MYSTERY ARC #12 — "Back to the Wrong Owner", the
returns case. The Elementary's lost-and-found has
started RETURNING things: items are showing up back
in cubbies and desks, each with a short slip — except
the returns go to the wrong owners, and each slip is
written in the hand of the kid it went to. Nothing is
missing. Things are coming back.

The spine is the RETURNS LEDGER: six returned items
on a Journal board, each carrying three DERIVED
checks recomputed from the game's own records on
every read, never stored. THE LOSS — was the item
actually lost, when and by whom, against the box's
own spike of loss cards. THE HAND — does the slip's
hand match its recipient's authored HAND card
(letterforms, pressure, habits, kept per kid in the
SPEECH_RULES idiom), checked mechanically, with the
fall penmanship unit's copybook forms kept as a
record of their own — half the school wrote alike
for two weeks, and the ledger prices that in. THE
FIT — does the item actually belong with the
recipient: size, owner marks, paired-item facts,
the filing's claimed marks against the returned
thing's own. Per item the player commits a reading
— TRUE RETURN / KIND MISTAKE / PLANTED — priced at
2 live evidence; verdicts are DERIVED (OPEN /
COMMITTED / PROVEN / REFUTED), standing readings
final, refuted readings strikeable and re-readable.
Four chapters (the finale last, "The Reading", at
the Elementary), queuing 10 days after arc #11
retires with ~7-day gaps and holiday deferral;
12 evidence (5 witness / 5 look / 2 participation),
exactly two context-locked — and the locked pieces
are the two load-bearing items' settlers, one per
load-bearing item. The finale is a four-call tiered
gate in the arcs' pattern (full read: both
load-bearing items PROVEN + all read + none refuted
+ at least four of six proven; name call: ≥1 proven;
stand/walk ungated), re-checked at lock and wrap;
four landings with marks, bond deltas and faction
ripples; a standing aftermath line; ambient
callbacks; and a 7-day delayed tail on the full-read
landing only. The red herring is honest oddities — a
kid who genuinely lost the same-looking item twice,
a teacher's handwriting unit — and the obvious
suspect-shaped answer carries an evidence-supported
fully innocent reading in the evidence text. The
attic's CASE_TOTAL is 12; the twelve-case lifetime
holds in-suite. Saves are additive-only (new `arc12`
keys, decode-filtered to registered ids, readings
and branches, hostile-doc tested). The case
registers NO new phone-note kinds — NOTE_KINDS stays
closed, in the arcs 7–11 pattern. 🔴 Structure only,
here and everywhere: the case's truth stays sealed
inside its own content. The tree stands at
113 Kotlin files.

## Status — v0.14.0e
Queue #14 (the town, in the mirror) continues with THE
CLUBHOUSE — the fifth item, and the first place in town
that belongs to your crew outright. The crew (v0.9.0b)
has roamed with you for five queues; now it claims a
spot of its own, and the claim rotates with the town's
seasons, read off the one season truth (`seasonFor`):
spring and fall, the treehouse at Kid's Park; summer,
the garage at the Marsh Residence (the garage its own
record names — Randy is probably doing something in
it, and the crew's corner is the driveway side); winter,
the basement at your house. No new map location: every
spot is a fixture at a place the town already has, and
the crew is seated there by an agenda claim like every
claim in town.

Every day, 15:00–19:00, the hangout stands — if the
crew exists and the town's bigger moments don't own
the hour. The claim seats twenty-first in Engine's
stack, after the stand's and before the crew's own
hook: above only the crew hook, self-guarded on the
crew's occupied oracle read whole plus a veto pass over
every claim seated above it. And it is priced like a
rendezvous: a member is claimed only when
`Walk.routeMinutes` from where they actually stand
lands by the window's close — nobody teleports, and a
member whose walk doesn't fit simply isn't claimed
that day; the Journal page says the spot waits. The
afternoon has three beats in the town's verbs —
Settle In (Talk), The Project (Look at the spot: a
season's small work, authored per spot), Crew Business
(Talk) — and NO new TalkGate rail: the Talk beats ride
the conversation's crew-talk channel beside
`Crew.talkLines` (judged as they play, the witness-line
discipline), the Look beat hides in the Look, and the
gate stands at eleven, proven in the suite. At the
spot, members speak crew-only clubhouse lines — about
this spot, this season, who is actually here and what
the crew has actually done, off Crew's own tallies —
and non-members standing in the same place get silence
from those banks. The wrap pays nothing of its own
(Crew's arrival +1 is the day's friendship, paid by
Crew's machine and never twice), plants the
"clubhouse" topic in everyone who came, and writes the
afternoon to a capped record. An empty crew stands the
whole thing down politely — no claim, no beats, no
lines — and the Journal 🏠 THE CLUBHOUSE page says so
plainly. Records: the Journal page and ONE
morning-after line in the house report; no phone text
and no paper line, deliberately — a daily afternoon is
not an event, and the shared MAX_TEXTS budget is not
spent on routine. All state is additive-only, in
`life`/`lifeText` stamps under the `club*` keys, listed
at their sites in `Clubhouse.kt`. The tree stands at
112 Kotlin files.

## Status — v0.14.0d
Queue #14 (the town, in the mirror) continues with YOUR
BIRTHDAY — the fourth item, and the one the cast-birthday
build (v0.12.0c) explicitly left open when it noted the
player's character got no birthday from that registry. The
new kid's birthday is not a calendar date at all: it is the
anniversary of the save's arrival day — the Family module's
arrival record, the day this household's life in South Park
began — recurring every 365 game days from that record
alone. No new stored date, no date picker; and because the
derivation is day-counted arithmetic rather than an MMDD
registry entry, leap years cannot move it (the game's
calendar does grow February 29ths; the birthday never
notices). The first birthday a save can see is one full
year in — a younger save simply hasn't had one, nothing
arrives retroactively, and the Journal page says exactly
that.

The morning belongs to the household. At rollover into
the birthday, Mom's bake lands in your pack: exactly one
real Snacky Cake — the same item the town's parties run
on, read off `Birthdays.CAKE_ITEM` rather than re-typed —
stamped per year so a save/reload on the birthday morning
can never duplicate it; it's edible, giftable, and if
it's still in your pack at the evening party, the cake
beat serves it. A card from the household arrives the
same morning and is kept verbatim on the Journal's 🎂 YOUR
BIRTHDAY page. The first time you talk to each parent at
home that day, they mark the day before anything else —
an authored line of their own, and +2 in the family's own
famPts lane (never `state.friendship`: the family wall
holds). Across the day, cast members wish you well in
person through the conversation's owed-lines seam — a
seam, not a rail; TalkGate still stands at eleven —
derived by the wisher's friendship band off the town's
tier table (close friends warmest, friends glad,
acquaintances kind; strangers don't know it's your
birthday). A wish stamps only its own once-per-person-
per-birthday record and moves no friendship, no coins,
nothing else — pinned byte-for-byte in the suite.

In the evening — the dinner hours, at your own house —
IF your crew is non-empty and the town lets the night be
yours, your crew throws a small party on the
sleepover/dinner pattern: three beats (the Arrival, a
Talk; the Cake, a Look at your house, serving exactly
one real cake when one is held and stating Mom's
improvisation honestly when none is; the Send-off, a
Talk), riding the DINNER TalkGate rail with a payload
beside the cast parties' PartyHit. The guests are claimed
to your house on the sleepover claim's pattern, consulted
at the sleepover claim's own seat in the agenda stack —
NO new seat (the stack still stands at twenty) — and the
claim self-guards on the crew's occupied oracle, so every
bigger claim seated earlier still wins its people. The
wrap pays the sleepover discipline: +2 a guest for a full
night, +1 for a partial one, once, at rollover; a party
nobody reached leaves no trace. If the crew is empty, the
day stays household-only — stated plainly in the morning
report and on the page, punished nowhere. And a birthday
that lands on a holiday, in a finale week, on a
case-climax night, opening night, a fair or carnival
week, or a night your own table is spoken for (family
dinner, a hosted dinner, a planned sleepover) still
fires the whole morning — the household is yours — while
the evening party yields, its reason derived on the
birthday morning (the dance's guarded pattern), stamped
into the day's record, and stated wherever the player
would look. The records keep the queue's discipline:
ONE TOWN NEWS line in the birthday week's edition (the
headline ladder stands at ten), ONE phone text on the
birthday morning under a new `playerbday` trigger — from
your closest friend, or from Mom when no friend stands
close enough (the sender admission set stays closed
either way) — and the Journal 🎂 YOUR BIRTHDAY page (the
derivation in plain words, the card kept, who wished,
the party read-back). All state is additive-only, in
`life`/`lifeText` stamps under the `pb*` keys, listed at
their sites in `PlayerBirthday.kt`. The tree stands at
111 Kotlin files.

## Status — v0.14.0c
Queue #14 (the town, in the mirror) continues with the BIJOU
FILM FESTIVAL — the third item, built on movie nights the way
the talent show was built on the Bijou's stage. One week a
year — the December week that holds the 8th, Monday through
Friday (the Monday falls Dec 2–8, the Friday Dec 6–12) — the
Bijou runs five wholly original town prints, one a night,
before a three-person jury and whoever the room draws in. The
anchor was chosen for the pocket the calendar leaves empty
and swept 2026–2100 in one lived calendar space against the
talent show's Saturday, the dance, the play's whole schedule,
the fair and carnival weeks, the parade, all three field
trips, the finale windows and every holiday — clean
everywhere, and the suite re-proves it. The poster goes up a
week ahead in the paper and on the Bijou's marquee. A night a
case climax owns still yields — that night only, the festival
keeps the week — through MovieNights' own deference ladder,
which learned the festival as its last reason; the fact is
stamped where it's learned, in the dance's pattern.

The five prints are invented whole and denylist-scanned in
the suite; the slate's order is a seeded (year) shuffle,
stable within a year, and the stars are drawn the way
MovieNights draws its features, seeded by (year, film) so a
cast is the same on the poster, the night and the record.
The jury is the yearbook's rotation grown up: nine townsfolk
in three leaning trios (craft / heart / weird), one trio a
year, consecutive years sharing no juror. A film's score is
derived, never rolled at the wrap: authored qualities
weighted 4/4/2, plus each juror's leaning read at face value,
plus twice the night's buzz — yours if you sat the night,
the house's own (a pure function of year and film) if you
didn't. The Golden Popcorn falls down a stated ladder on
ties: audience, heart, title A–Z. The week rides the
theater's own machinery wherever it reaches — the session is
the feature's eleven beats scored by MovieNights' table, the
audience is MovieNights' draw under its house cap, the doors
are MovieNights' doors, and admission is MovieNights'
position: free. The one new mechanism is an agenda claim
seated twentieth — after the field trips', before the
sleepovers' — because Monday through Thursday there is no
screening claim to ride; TalkGate keeps its eleven rails.
The records discipline is the queue's: the paper carries the
festival in TOWN NEWS only (the announcement in its week,
the winner in the closing week's edition — the headline
ladder stands at ten by deliberate decision, stated in the
changelog); the yearbook gains a line in the volume the
December belongs to; the Journal gains a 🎬 FILM FESTIVAL
page with the jury's cards sealed until the wrap; the phone
carries one morning-after text under a new FESTIVAL trigger
(budget and sender set untouched); and the winning film's
stars take +1 friendship only if you sat closing night, paid
once at the wrap. Exactly one group photograph may be
minted at closing night, through the photo system's own
composition. State is additive-only — the year's record
rides `lifeText` in the talent show's pipe format, sessions
ride pruned day keys — and a mid-festival save/reload keeps
the slate, the attendance and the buzzes whole, pinned
through the real save path. The tree stands at
110 Kotlin files.

## Status — v0.14.0b
Queue #14 (the town, in the mirror) continues with the FIELD
TRIP — the second item, built the way the play and the fair
were: an announced seasonal school event, derived whole. Three
Fridays a school year, the fourth grade meets on a bus: fall's
nature study at Stark's Pond (the second Friday of October),
winter's quiet morning at the Park County Historical Room — the
town museum, inside City Hall, no new map location needed —
and spring's agriculture day at Tegridy Farms (the third
Friday of April). The poster goes up a week ahead
on the classroom board and the morning ticker; the anchors
were swept 2026–2100 in the same lived calendar space as the
dance, the talent show, the play, the fair, the carnival, the
parade, the finale windows and the holidays, and collide with
none of them — the February pocket (trips Feb 1–7, the dance
Feb 8–14) is the closest call and it holds every year. The one
way a trip still yields is a bigger owner taking the day at
runtime — a case climax, a finale, a holiday, in School's
standing priority — and the yield stamps its reason onto the
record, Dance's guarded pattern. The class is derived, never
typed: the 39 cast kids in the KID age band, chaperoned by the
school's own teacher rotation (Fridays are Garrison's). The
school day stands down whole on trip day (a new, lowest quiet
reason), and the class is held at the site by an agenda claim
seated nineteenth — after the parade's, before the
sleepovers' — 9:00 to 13:00; TalkGate keeps its eleven rails.
The player boards at the classroom board between first bell
and the half hour (the ride is 30 real game-minutes each way),
plays a taught choice beat at the site — the pond's Specimen
Scan, the museum's Docent Quiz (two rounds counted live off
the game's own registries), the farm's Harvest Help — takes
exactly one class photograph through the photo system's own
composition, and rides back from 12:30. Never boarding wraps
the day MISSED, stated plainly and punished nowhere. The
records discipline is the dance's: a played-through beat pays
the classmates physically present (+1, capped at eight) and
the chaperone only for sharp work; the Journal gains a
FIELD TRIPS page; the week's paper carries one TOWN NEWS line
(the headline ladder stands at ten); the morning after brings
one text under a new FIELDTRIP trigger (budget 220, sender
set untouched). State is additive-only — every trip fact
rides `life`/`lifeText` under `trip…` keys, and a mid-trip
save/reload keeps the ride, the open beat, the picks and the
photograph whole, pinned through the real save path. Sol
discipline held: all site copy newly authored (the farm's
included — no show dialogue quoted or approximated), and the
tree stands at 109 Kotlin files.

## Status — v0.14.0a

v0.14.0a (versionCode 102) is built and shelved —
**the reflections pass** (Queue #14 item 1). No
new modules: the tree stands at 108 Kotlin files,
and the changes are rendering only — two pure
decision functions (`Sleepwear.mirrorCode`,
`Heroes.streetCostumeCode`) and two thin drawing
calls in `GameView`. Zero new save keys; rendering
a surface writes nothing. Verified: the full JVM
smoke suite passes (baseline 101,672/0 on the
untouched v0.13.0g tree), and the clean APK builds
and signs with the standing key.

- **The mirror shows the night look:** while a
  sleepover night is live at Your House, your
  reflection in the house mirror wears your
  pajamas — the same code the shutter freezes
  onto a photo's player figure at the same
  moment, so the glass, the album, the street
  re-render and the darkroom agree
  figure-for-figure. On every other terms the
  mirror shows your ordinary day look.
- **Your hero costume shows on the street:**
  while you stand a patrol spot in costume, the
  street scene draws you in it — cape, mask and
  emblem from the same costume code the camera
  freezes — through KidArt's one hero pass. Off
  patrol the street stays first-person, as ever.
- **Two standing gaps CLOSED** (the mirror night
  look, carried since v0.12.0d; the hero costume
  invisible in street POV, carried since
  v0.10.0b). The changelog entry below is the
  record.

## Status — v0.13.0g

v0.13.0g (versionCode 101) is built and shelved —
**polish & performance pass #13** (Queue #13
item 7, the queue closer). No new modules and no
new features: the tree stands at 108 Kotlin files,
and the only production edits are the audit's two
sanctioned fixes — the talent show's and the
parade's post-night gossip lines are once-ever
stamped now, in the play's pattern, instead of
repeating inside their windows. Verified: the
full JVM smoke suite passes (baseline 101,567/0
on the untouched v0.13.0f tree), and the clean
APK builds and signs with the standing key.

- **The audit, in the polish tradition:** one
  400-day soak living every Queue #13 surface on
  one save (the family page and home barks, the
  dance, talent night, the parade, the pen-pal
  correspondence driven reply-by-reply, the
  eleventh case inside the never-two-cases
  discipline); the ELEVEN-case lifetime re-driven
  in one unattended life; contention re-pinned at
  the code's own seats (eleven TalkGate rails,
  the agenda stack at EIGHTEEN claims in Engine's
  order — Dance, Talent and Parade seated between
  Play and Sleepovers); the phone stressed through
  its worst week with the three new triggers in
  the harvest; the tuning ledger completed for
  every Queue #13 dial (the dance, the parade and
  pen pals join the ledger above); and Veteran
  XII — the saturated save carried forward with
  Queue #13's records laid over it, measured
  against the 640K ceiling. The measured numbers
  stand in the changelog entry below.
- **Queue #13 is complete** with this build: the
  family page, the school dance, the talent show,
  the town parade, pen pals, the voice case, and
  this audit — seven items, one town, celebrated.

## Status — v0.13.0f

v0.13.0f (versionCode 100) is built and shelved —
**mystery #11, the voice case** (Queue #13 item 6).
One new module (`Mystery11.kt`); the tree is at 108
Kotlin files. Verified: the full JVM smoke suite
passes (baseline 101,457/0 on the untouched
v0.13.0e tree), and the clean APK builds and signs
with the standing key.

- **The eleventh case is a voice ledger.** Letters
  have been surfacing around town signed with other
  people's names — some kind, some cruel in a way
  the signer would never be — and the investigation
  reads them against the voices they claim: six
  letters, each scanned mechanically against its
  claimed signer's voice profile (markers present,
  load-bearing marks, nevers violated, studied
  tells), every finding derived on every read. Each
  letter takes one of three readings — IN VOICE,
  OFF VOICE, or BORROWED VOICE, the studied imitation
  told from a stranger's miss by the markers it gets
  right — priced at 2 live evidence, verdicts
  derived, standing readings final, refuted
  strikeable. Twelve evidence pieces (5 witness /
  5 look / 2 participation), exactly two
  context-locked — the load-bearing letters'
  settling pieces; a finale of four calls at City
  Hall with tiered gates; four landings with marks,
  bond deltas, faction ripples, standing lines and
  a delayed tail on the full-read landing.
- **The pen-pal wall holds.** Nothing in the case
  touches the correspondence: no letter, piece,
  witness or settling fact lives behind a signup,
  and the suite drives the whole case end-to-end on
  a save that never signed up. The case queues ten
  days after the tenth retires, defers past
  holidays, registers in every deference list the
  earlier cases joined (and defers in turn), the
  paper can headline its closing day off the
  retired-day stamp the same way, and the attic
  keeps an eleventh case box in the same public
  shape. Saves are additive-only: a pre-0.13.0f
  save simply queues the case behind the tenth.

## Status — v0.13.0e

v0.13.0e (versionCode 99) is built and shelved — **pen
pals** (Queue #13 item 5). One new module
(`PenPals.kt`); the tree stands at
107 Kotlin files. Verified: the full JVM smoke suite
green (101,457 checks, 0 failures), a full
clean rebuild, badging at 99 / 0.13.0e, the standing
signer certificate verified, and the lorebook inside
byte-identical. It gives the year a friend it never
meets: **Wren Halloway of Pinewood Flats**, reached
only by post.

- **Not cast, on the parents' precedent.** Wren is a
  persona outside the 98 — census-pinned from both
  sides (she is in no cast list, no faction, no phone
  sender set, no sheet in the cast's fan-sheet set) —
  with a mini speech card, letter banks in her own
  dry, rating-everything-out-of-five voice, an authored
  SHEETS-style spec (rust ponytail, pine-green top,
  freckles) drawn through the same KidArt geometry as
  any cast kid, and a warmth lane of her own
  (`palPts`, the famPts pattern) that never touches
  `state.friendship`. Her town is wholly invented —
  one street, one blinking light, one water tower with
  a fish on it — and every authored surface is
  denylist-scanned for franchise material and real
  places.
- **The program.** Sign up at the Elementary's
  classroom board every **October 1–21** (the box was
  in a closet; the program starts late, and her first
  letter says so). Late joiners wait for next year's
  pairing — stated on the board, the page, and the
  signup card your dad stamps himself down at the Post
  Office, the letters' in-fiction route. Letters
  arrive through the **mailbox** at your house on a
  seeded **3–5 day** cadence, announced on the morning
  ticker, with the flag up over the box while one
  waits unread.
- **Fact slots, honestly filled.** Incoming letters
  are composed from her banks (the Flats' news, the
  tooth market, lake season) plus follow-ups on facts
  **your own sent letters actually carried** — and your
  replies compose in front of you, at the mailbox, from
  your real records: your latest photo, a closed case's
  **public shape** (title + closed, read from the
  attic's boxes and nowhere else — the spoiler wall is
  suite-scanned over every bank and hundreds of
  composed letters), the stand, the garden, the fair,
  the carnival, the dance, the talent show, the parade.
  One of **three tones** frames the letter; the facts
  are identical under every tone, and the sent text is
  kept **verbatim**. A correspondence over an
  empty-records save runs on Pinewood Flats news and
  invents nothing about you — pinned by fabrication.
- **Silence has a shape.** Each reply pays +3 warmth
  (bands: a name on a school list → a writing-regular
  at 15 → a real friend at 45, flavoring her greetings
  and sign-offs). Unanswered letters stretch the
  cadence by 3 days and draw kind notes; **three
  unanswered** and the correspondence goes dormant —
  she stops filling your mailbox until you write, and
  one revival letter reopens the route.
- **The visit.** Top band at the May 31 settlement and
  she comes: **June 10–13**, a resident-class guest —
  your house mornings and evenings, the park at midday,
  on the Family whereNow pattern — with talk lines in
  her own voice on her own deterministic sheet (the
  family precedent: no brain but the banks), a daily
  +1 for the first talk, gifts judged into her own
  lane, one album photo through the ordinary camera,
  and a goodbye beat that owns the last day's first
  hello. The morning after, a thank-you letter. Below
  the band, the year ends with her "maybe next summer"
  letter instead. No TalkGate rail was added (eleven
  stand) and the visit claims no agenda seat — guest
  presence only.
- **Records.** The Journal's 📬 PEN PALS page keeps
  the pairing, the standing, the visit's record, and
  the letters: the newest **16 in full** (either
  direction), older ones folded to a one-line summary
  written when the letter was composed — provenance
  survives the fold. **The paper does not cover the
  letters** (private correspondence, stated on the
  page) and **the phone gets nothing** (letters are
  letters). No mystery machinery of any kind grew on
  the correspondence. Saves are additive only — every
  fact rides the `life`/`lifeText` maps. Fan sheets
  are untouched: the pal is not cast, so no sheet joins
  the 98.

## Status — v0.13.0d

v0.13.0d (versionCode 98) is built and shelved — **the
town parade** (Queue #13 item 4). One new module
(`Parade.kt`); the tree stands at
106 Kotlin files. Verified: the full JVM smoke suite
green (101,267 checks, 0 failures), a full
clean rebuild, badging at 98 / 0.13.0d, the standing
signer certificate verified, and the lorebook inside
byte-identical. It gives May its day: every year, on
the **third Saturday of May**, the whole town rolls
down Main Street — the Founders' Day Parade.

- **The anchor, proven.** The date is pure calendar
  (the third Saturday of May — always May 15–21), the
  poster up a week ahead on a reviewing stand drawn
  on Main Street itself, and the suite sweeps
  2026–2100 proving it disjoint, in one lived time
  space, from the dance (February), the talent show
  (April — measured: always at least 31 days clear,
  the parade's nearest neighbor), the play's
  openings, the fair's week, the carnival's week,
  every finale window (the spring finale owns
  May 25–31; the parade is always at least 4 days
  clear of it) and **every** holiday window — May
  holds none at all. The runtime yields ride the
  standing pattern anyway: a case climax, a finale
  window or a holiday beat through School's quiet-day
  definition, or the play guard in the dance/talent
  form — the year is skipped whole, the reason
  stamped where learned and printed wherever the
  player would look, never postponed.
- **The procession, derived — never invented.** The
  order is built from the town's real structures on
  every read. Claim order (who keeps their people):
  the seven Dinners households first (families ride
  whole), then your stand's staff of the day, then
  your crew, then the Mayor's car (so no faction
  float can carry McDaniels off — she rides with
  Barbrady and Yates), then the Bijou's float with
  the day's feature's real stars, then the eight
  factions — each float carrying at most six riders
  (the first-listed unclaimed member always aboard,
  the rest seeded by year), the school staff's float
  presented AS the Elementary's. Procession order
  (who rolls when): the school leads, the households,
  the Bijou, the factions, your stand float (its line
  derived from the crate's real contents at your real
  prices), your crew's walking entry, and the Mayor's
  car last. A name lands in exactly one crew — the
  pool is claimed, never copied — and everyone not
  riding is the route crowd, assigned to the four
  curb spots by a stated seed. The suite pins the
  order, the priority and the partition, and proves
  two reads derive the same parade with no read
  creating a record.
- **Your day, two honest ways in.** **March** — with
  your crew if you have one (the entry stands even if
  every member was claimed by a family float: you
  carry the banner, they wave), else as a stated
  free-agent walker with the school entry; marching
  costs the afternoon and pays the walkers +1 at the
  wrap. **Watch** — from one of four named curb spots
  on the real frontage (the bank steps, the Bijou
  marquee, the post office corner, the City Hall
  steps): the floats pass YOUR spot in chunks of
  four, each with its line and a reaction from
  somebody actually standing near you. Either way
  the day ends at the Mayor's car — McDaniels'
  authored closing line in the finale-beat pattern
  (a closing card, no new rail) — and exactly **one
  photograph** through Photos.composeGroup: watching,
  the float of your choice, framed with its riders as
  they actually stand on the street; marching, your
  entry gathered at the City Hall steps (Main Street
  has no library; the reviewing stand is the route's
  ceremonial spot).
- **Candy, on a stated economy.** Three floats throw
  real pantry goods — the Cartman float throws snack
  cakes, the Bijou's float and the Mayor's car throw
  cheesy poofs, both the stand's own stock items at
  the stand's own prices (your own stand float throws
  nothing: your goods into your pockets would be a
  loop). Each thrower gives watchers one catch
  session of three throws on the showdown bar; caught
  candy lands in your real inventory, and the day's
  gain is capped at **6** — what your pockets can't
  hold goes to the kid next to you, stated. The suite
  COMPUTES the maximum possible haul at perfect play:
  9 throws exist, the cap binds at 6 items, worth at
  most **12 🪙** of pantry value, once a year. The
  parade can never be a pantry farm.
- **The claim and the closed street.** The agenda
  claim seats **eighteenth** — after the talent show,
  before sleepovers, pinned from Engine's own source —
  and holds every rider and watcher on Main Street
  for the daytime window, **10:00–13:30**. Street
  traffic yields honestly: Walk's route geography is
  untouched (a route through Main Street is still the
  route), but a trip PASSING THROUGH the street
  mid-trip waits one stated tick (**+15 minutes**),
  added identically by the picker (routeMinutes) and
  the charged leg (walkLeg) so quote and charge always
  agree; trips TO or FROM Main Street pay nothing,
  and the bus's raw-leg pricing skips the closure by
  design. The suite prices both sides of the street.
- **The records.** A Journal **🎺 THE PARADE** page
  (this year's derived order as posted or as rolled,
  your part stated honestly, past years one line each
  capped at three). The paper's next edition carries
  **one TOWN NEWS line** — the order's head and the
  Mayor's car named, your entry if you marched; the
  headline ladder stands at ten. **One morning-after
  phone text** through the existing harvest — a
  crewmate who marched with you, a curb neighbor you
  stood with, or a friend who was there when you
  missed it; fact-only, sender always one of the day's
  own derived people; MAX_TEXTS stands at 220.
  Riders talk their floats in the run-up week and the
  town quotes the day for a week after, as a seam on
  the talk chain — the TalkGate still stands at
  eleven rails. State is additive-only (the
  `life`/`lifeText` maps under `parade…` keys); a
  pre-0.13.0d save loads untouched and joins the next
  parade. One honest derivation note: the brief
  pictured four factions from an older town — the
  Factions table has stood at **eight** since cast
  wave 5, so the procession derives one float per
  faction from the table as it stands, all eight.

## Status — v0.13.0c

v0.13.0c (versionCode 97) is built and shelved — **the
talent show** (Queue #13 item 3). One new module
(`Talent.kt`); the tree stands at
105 Kotlin files. Verified: the full JVM smoke suite
green (101,189 checks, 0 failures), a full
clean rebuild, badging at 97 / 0.13.0c, the standing
signer certificate verified, and the lorebook inside
byte-identical. It opens April properly: every year,
on the **second Saturday of April**, the Bijou turns
its stage over to the town — ten kid acts, one
applause meter, and a winner the room derives itself.

- **The anchor, proven.** The date is pure calendar
  (the second Saturday of April — always Apr 8–14),
  the poster up a week ahead, and the suite sweeps
  2026–2100 proving it disjoint, in one lived time
  space, from the dance's February night, the play's
  fall openings, the fair's week, the carnival's
  week, every finale window, and **every** holiday
  window — April's second Saturday is the town's one
  quiet spring pocket, with no coincidence to yield
  to on the calendar at all. What can still yield it
  is the town itself: a case climax, a finale window
  or a holiday beat owning the day through the
  standing quiet-day definition, or the play's
  opening night reaching the date — the show yields
  that year whole, the reason printed on the poster,
  never postponed into another event.
- **The Bijou, shared honestly.** The show night is
  a screening Saturday, so the theater stands its
  own feature down through MovieNights' deference —
  registered both directions: the marquee goes dark
  *by name* ("the talent show has the stage"), the
  movie house gathers nobody, no screening text goes
  out that morning, and the movie's own claim returns
  null — while on a case/finale/holiday night the
  marquee names *their* reason, because the show
  yields to all three. The talent claim itself seats
  **seventeenth** in the agenda stack, after the
  dance and before sleepovers.
- **The field, derived.** Nobody is cast by hand:
  every kid's stage appetite is the play's own data
  — `STAGE_NERVE` and the bee's line skill, weighted
  3/2 with a seeded year's want — and the top ten
  take the bill in a seeded running order, posted on
  the poster a week ahead. Every act is authored:
  one original line per kid (all 42, so any year's
  field is covered), denylist-guarded against real
  songs, real musicals and franchise acts exactly
  like the play's and heroes' guards. Entrants talk
  their acts in the run-up, in the play's gossip
  pattern. The host is the stage desk of the
  yearbook's judging rotation — the play critic's
  own chair.
- **Your night, three ways.** *Enter:* sign up at
  the Bijou in poster week — sing, jokes, magic or
  dance, each with its own played shape on the
  showdown bar (jokes sweep fastest and hold
  hardest; magic is the slowest, shakiest five
  phrases) — and optionally **rehearse once** in the
  week: a real 30-minute beat on the Bijou's stage,
  worth a stated **+6** on the meter, banked even if
  you change your act. *Watch:* the audience track
  is complete — the same night, beat for beat, with
  the room's reaction prompts scored by MovieNights'
  own tables. *Skip it:* the show still happens; the
  wrap derives the town's winner without you, and
  the page says you never made it.
- **The judging.** Every NPC score is derived live
  and stated in code: the meter starts at a kind 34
  and adds nerve ×4, voice ×2, the entrant's
  chemistry with the room of fellow entrants ×14,
  and the night's own seeded wobble. The winner is
  read off the landed scores at the wrap — ties go
  to the room: highest chemistry, then nerve, then
  the alphabet. Prizes: a modest **15-coin** envelope
  from City Hall's arts line (yours in person, or
  settled to you if you performed and won), the
  winner's framed line on the Journal page, a
  yearbook line for the school year, and **ONE
  winner's photograph** through the photo system's
  own composition, captioned with the winning act.
  Friendship follows the movie pattern — paid when
  the night is played through (every entrant +1,
  the winner +2 more, a legendary house +1 again),
  never for a night the room finished without you.
- **The records.** A Journal **🎤 TALENT SHOW** page
  (the posted bill while live; the night's table as
  played, your act and placing stated honestly —
  including the empty slot of a no-show entry; past
  years one line each). The paper's next edition
  carries **one TOWN NEWS line** — winner and act,
  no invented color; the headline ladder stands at
  ten. And **one morning-after phone text** through
  the existing harvest: from the winner, or the host
  when the winner is you, or a friend who was there
  when you missed it — fact-only, moving nothing
  beyond the night's own pays.
- **The room is kind.** The judging never mocks a
  kid act — a low score is a quiet room, not a cruel
  one, and every write-up follows the play review's
  kind-truths register (a sideways act is "the story
  of the night", and having been up there counts
  double). Everything kid-innocent; every act
  invented. State is additive-only (the
  `life`/`lifeText` maps under `talent…` keys); a
  pre-0.13.0c save loads untouched and joins the
  next show. Nothing else moved: the phone's sender
  set gains only the existing cast as senders, the
  TalkGate still stands at eleven rails (the
  talent talk is a seam on the ambient chain, like
  the play's), the paper's ladder stands at ten,
  and the dance/play/fair/carnival calendars are
  exactly as they stood.

## Status — v0.13.0b

v0.13.0b (versionCode 96) is built and shelved — **the
school dance** (Queue #13 item 2). One new module
(`Dance.kt`); the tree stands at
104 Kotlin files. Verified: the full JVM smoke suite
green (101,115 checks, 0 failures), a full
clean rebuild, badging at 96 / 0.13.0b, the standing
signer certificate verified, and the lorebook inside
byte-identical. It opens February properly: every
year, on the **second Friday of February**, South Park
Elementary dresses its gym for the Sweetheart Dance —
and the week before it, the bravest errand in fourth
grade runs on the tap-person menu: **asking somebody
to go**.

- **The anchor, proven.** The date is pure calendar
  (the second Friday of February — always Feb 8–14),
  announced a week ahead, and the suite sweeps
  2026–2100 proving it disjoint, in one lived time
  space, from the play's October/November openings, the
  fair's week, the carnival's week (the dance clears
  even the carnival's latest close by twelve days),
  every finale window (the winter finale opens Feb 22;
  the dance is done by the 14th), and every holiday
  window in the town's own table. The ONE coincidence
  the arithmetic allows is Valentine's Day itself:
  when Feb 14 *is* the second Friday, the holiday beat
  owns the day and the dance **yields the whole year**,
  with the reason printed on the poster — never
  postponed, never squeezed into another night. A live
  case climax or a finale window reaching the day
  yields it the same way, through the town's standing
  quiet-day definition plus the play's live check.
- **The ask.** In the asking week, tapping any kid
  carries the dance option (adults get the option too,
  labeled as what it is — asking one gets you the
  flattered, final grown-up answer and touches no
  record). A yes needs a real friendship under it: the
  **Friend tier (30)** for the glad kids, **Close (55)**
  for the shy ones — below that, the shy give a soft
  *“ask me again when we're closer”* that spends
  nothing and finalizes nothing, and below the Friend
  tier the menu itself says “not yet” and no ask is
  spent (the crew discipline, stated in the menu's own
  label). Every kid's answer is authored in their
  voice: 22 glad yeses, 10 shy yeses (the soft-no bank
  beneath them), and 10 kind, final NOs — no grinding
  a no. One date at a time: a second yes **stands the
  first kid up** — allowed, authored, and remembered:
  **3 friendship** paid back at the switch, their
  punch-cup toast-that-isn't in the gym that night if
  they're in the room, the fact kept on the Journal
  page forever, and a re-ask answered finally. Going
  **stag** is a first-class plan — the poster, the
  page, the menu and the night itself all say so, and
  every beat is reachable stag.
- **The evening.** Friday 18:00–21:30 at the
  Elementary, held by an agenda claim **seated after
  the play and before sleepovers** (the stack is now
  sixteen claims; the suite pins the seating order out
  of Engine's own source): your date, a seeded 65% of
  the kid cast, and Principal Victoria with a
  clipboard. Four beats off the classroom board:
  **ARRIVAL** (the gym dressed — banner, punch table,
  DJ booth — and the crowd as it actually stands);
  **THE SLOW DANCE**, played on the showdown bar — four
  phrases, tap to step, and your steadiness is the
  zone's power widened by your partner's composure
  (the play's nerve table, wearing its second hat) —
  stag, the solo spotlight variant runs on the new
  kid's honest nerve of 5, and the song still pays out
  in standing and one good story; **THE PUNCH TABLE**,
  serving your own pantry — a lemonade poured as
  punch, a snack cake broken out — consumed exactly
  when taken, with the water cooler free forever; and
  **THE LAST SONG**, which reads the night back from
  the record and mints **ONE group photograph** through
  the photo system's own composition, captioned with
  the night and who you came with (or stag). A partial
  night wraps as the night it was; an untouched one as
  missed; a yielded one as yielded, with its reason.
- **The records.** A Journal **💃 THE DANCE** page
  (the year's poster and asking state while live —
  your plan, the stood-up fact, the nos and the
  ask-me-agains, each stated; the wrapped night read
  back whole after; past years one line each). The
  paper's next edition carries **one TOWN NEWS line** —
  names and the photograph's existence, no invented
  color; the dance is section copy and the headline
  ladder stands at ten. And **one morning-after phone
  text** through the existing harvest: from your date
  if you had one, else the closest friend who was in
  the room — fact-only, moving nothing (if you stood
  your date up by not showing, the text is theirs, and
  it is honest). Sleepovers' planner names the dance
  as its own refusal, so nobody is double-booked.
- **The tone wall, held.** These are elementary kids
  and the night is built that way: the date is
  courage-and-friendship framing end to end — asking
  takes nerve, the slow song is comedy and heart, and
  every outcome is friendship and standing only. No
  romance mechanics, no kiss beats, no couple systems,
  no jealousy engines; the one shadow is the honest
  stood-up consequence, because promises are real even
  in fourth grade. State is additive-only (the
  `life`/`lifeText` maps); a pre-0.13.0b save loads
  untouched and joins the next dance. Nothing about
  the town's other systems moved: the phone's sender
  set, the TalkGate's eleven rails, the paper's
  ladder, and the fair/carnival/play calendars are all
  exactly as they stood.

## Status — v0.13.0a

v0.13.0a (versionCode 95) is built and shelved —
**the family page** (Queue #13 item 1, opening the
v0.13.0 base). No new modules: the tree stands at
103 Kotlin files. Verified: the full JVM smoke
suite passes at **101,044 checks, 0 failures**
(baseline 101,001/0 on the untouched v0.12.0g
tree), and the clean APK builds and signs with
the standing key.

- **A 👨‍👩‍👧 YOUR FAMILY page in the Journal**,
  derived whole from the family's records on every
  read — arrival day, stance in the family's own
  words, who's home (a pure `whereNow` twin of
  `blockAt`, swept equal to the live block across
  the week's hours), warmth per parent, jobs,
  the dinner record (most recent night honest,
  full or unsat), Mom's allowance book, chores
  with their askers, and the frame read through
  the attic's own functions so the pages agree.
  Rendering a fresh or lived save writes nothing —
  byte-identical, pinned.
- **Home barks for Mom and Dad**: rhythm patterns
  in their own `Barks.HOUSEHOLD_PATTERNS` table
  (the cast table stays census-pinned at 98;
  100 distinct signatures across both), greeting
  and idle banks, and a pure `Family.barkDecide`
  under the cast's bark discipline. Two hundred
  landed decisions move no warmth, no coins, no
  state. The build's one new stored fact —
  per-parent chore counters — is named on the
  page itself wherever a total predates it.

## Status — v0.12.0g

v0.12.0g (versionCode 94) is built and shelved —
**polish & performance pass #12** (Queue #12 item 7,
the queue closer). No new modules and **zero
production-code changes**: the tree stands at 103
Kotlin files, and the audit needed none — the one
thing it surfaced is a design truth, stated in the
gaps, not a defect. Verified: the full JVM smoke
suite passes at **101,001 checks, 0 failures**
(baseline 100,883/0 on the untouched v0.12.0f tree),
and the clean APK builds and signs with the
standing key.

- **A 400-day soak over every Queue #12 surface:**
  57 editions with seven headline tiers lived and
  the other three fabricated beside the year until
  all ten stand proven; 10 birthday parties stood,
  5 played, 3 cakes eaten, 12 honest yields in five
  distinct stated reasons; 69 seed packets bought at
  the counter and planted packet-first beside 4
  supplier plantings, both money books balancing to
  the coin; 46 full sleepover nights with frozen
  frames (151 guests in pajamas) and a darkroom
  print carrying the codes figure-for-figure; 68
  birthday phone beats, each the day's own name on
  its own thread; the tenth case lived start to
  finish inside the year; and fifty-plus location-
  gate probes across the calendar with not one leak.
- **The whole lifetime, re-driven:** all ten cases
  retire in order in 316 unattended days, and the
  attic stands at ten boxes. Contention re-pinned:
  TalkGate still eleven rails (the party rides
  DINNER as a payload), the Engine agenda stack
  pinned at fifteen seats with the party second.
  The phone's worst week — a 206-text inbox through
  the densest birthday week of the year — held
  every cap every morning, and the stamped paper
  summary, the harvest's one lossless promise,
  landed inside its week. Veteran save XI: 577,225
  serialized chars against the 640×1024 ceiling
  (78,135 to spare), round-tripped whole with the
  attic reading it back page-identical. The
  tuning ledger is complete for every Queue #12
  dial; the queue's gaps are consolidated in the
  changelog, stated plainly. **Queue #12 is
  complete.**

## Status — v0.12.0f

v0.12.0f (versionCode 93) is built and shelved —
**mystery #10, the photo case** (Queue #12 item 6).
One new module (`Mystery10.kt`; the tree is at 103
Kotlin files). Verified: the full JVM smoke suite
passes at **100,883 checks, 0 failures** (baseline
100,778/0 on the untouched v0.12.0e tree), and the
clean APK builds and signs with the standing key.

- **The tenth case is a comparison.** A photograph
  surfaces that the town can't account for — real
  place, real people, hung beside the archive's own
  print of the same sitting and disagreeing with it
  by one face — and the investigation is the album
  itself: four photographs read against their own
  frozen fields, place, day and faces, every check
  derived on every read. Twelve evidence pieces
  (5 witness / 5 look / 2 participation), exactly
  two context-locked; a finale of four calls in the
  school hall with tiered gates; four landings with
  marks, bond deltas, faction ripples, standing
  lines and a delayed tail on the full-read landing.
- **The lifetime is ten.** The case queues ten days
  after the ninth retires, defers past holidays,
  registers in every deference list the earlier
  cases joined (and defers in turn), the paper can
  headline its closing day off the retired-day stamp
  the same way, and the attic keeps a tenth case box
  in the same public shape. The suite's long drive
  retires all ten cases in order in one unattended
  life. Saves are additive-only: a pre-0.12.0f save
  simply queues the case behind the ninth.

## Status — v0.12.0e

v0.12.0e (versionCode 92) is built and shelved —
**the attic** (Queue #12 item 5). One new module
(`Attic.kt`; the tree is at 102 Kotlin files).
Verified: the full JVM smoke suite passes at
**100,778 checks, 0 failures** (baseline 100,746/0 on
the untouched v0.12.0d tree), and the clean APK builds
and signs with the standing key.

- **The fixture.** Your House gains an upstairs
  fixture in the garden/paper pattern: a narrow door
  with three steps up, drawn with the house fixtures
  (solids only — plain wood, one brass knob), opening
  the attic page.
- **Derived contents only — nothing is stored.** The
  overflow shelf holds what the house has earned past
  the trophy shelf's two caps (12 collectible slots,
  6 feat cups) — when the downstairs display holds
  everything, the shelf up here is honestly bare, and
  the page says so. The family frame's outtakes are
  the home family photos that aren't in the frame —
  the album's, plus any a darkroom print still holds
  after the album's copy has faded — public facts
  only (the record's own caption and date). One
  stored box stands per FINISHED case, arcs 1–9, in
  the town-secrets pattern: case number, the title
  the Journal already publishes, the closing day
  where the record keeps one, the board's piece
  count — public shape only; an unretired case
  produces no box.
- **The walls.** The spoiler wall is absolute: no
  solution, no name the board hasn't said in public,
  no branch word, ever — the suite sets six different
  landings in a save and proves none of them prints.
  No new economy (the module holds no coin path at
  all), zero new save keys and zero new GameState
  fields — every read is pure, and a pre-0.12.0e
  save climbs into an attic that already knows what
  its owner has done. No dial moved: decor stands
  at 17, MAX_TEXTS at 220.

## Status — v0.12.0d

v0.12.0d (versionCode 91) is built and shelved —
**sleepwear** (Queue #12 item 4), closing the gap
v0.11.0c recorded ("Photos render normal clothes;
there is no sleepwear pass"). One new module
(`Sleepwear.kt`; the tree is at 101 Kotlin files).
Verified: the full JVM smoke suite passes at
**100,746 checks, 0 failures** (baseline 100,452/0 on
the untouched v0.12.0c tree), and the clean APK builds
and signs with the standing key.

- **The derivation (Heroes' spirit).** A pajama code —
  "top|bottom|cap" — derived deterministically from
  each figure's own sheet: top from their shirt color,
  bottoms from their pants color, both softened toward
  a warm pale neutral. Hats off, the spec's own hair —
  a floppy **nightcap** only for the five soft-knit day
  hats (beanie, pomhat, ushanka, chullo, trapper),
  in the pajama colors with one band and one bobble.
  Day construction strips (placket, buttons, pockets,
  tie, backpack, scarf); ONE trim survives — the collar
  band in the bottoms' color. Specials stay (Kenny
  sleeps in his parka hood). **Solids only: no dots,
  no patterns, no repeated motifs** — the suite scans
  the tables and the ink structurally.
- **The freeze.** `PhotoFigure` gains one additive
  field beside `hero`. A photo composed at Your House
  while a sleepover night is live (plan standing,
  evening open) freezes the code onto the guests in
  frame and onto you (your code derives from your
  creator appearance). Album, street re-render and
  darkroom prints draw the same pajamas through
  KidArt's one sleep pass. Any other frame freezes
  nothing — including noon the same Friday — and the
  pass is not retroactive.
- **The walls.** Adults and residents never freeze
  (the kid bands are re-checked at the seam, so a
  tampered plan can't dress a grown-up); the street
  stays first-person (your pajamas are invisible in
  POV, like your costume); no other night system
  borrows the pass; fan sheets unchanged — no measured
  spec moved.
- **Stated gap — the mirror.** The mirror at Your
  House does not show the night look: it renders no
  figure (a wardrobe dialog), and its only
  figure-rendering route is the creator's editor
  preview, which must show the look being saved. Not
  forced; your sleepwear shows in the night's photos,
  the album, and darkroom prints.

## Status — v0.12.0b

v0.12.0b (versionCode 89) is built and shelved — **the
general store counter** (Queue #12 item 2), the walk-to
buy surface the garden named as missing in v0.11.0b.
One new module (`Shop.kt`; the tree is at 99 Kotlin
files). Verified: the full JVM smoke suite passes at
**100,384 checks, 0 failures** (baseline 100,330/0 on
the untouched v0.12.0a tree, inside the known ±1 soak
wobble), and the clean APK builds and signs with the
standing key.

- **Two counters, both at the Mall** — the town's one
  retail floor, where every walk-up counter the game
  has ever had already stands. The garden centre's
  seed rack sits in the second-row band under the
  clothes rack (the ballot box's band); the snack
  shop's general-goods counter under the transit
  kiosk. Neither has a keeper gate — the Mall's
  counters never have (the Good Shelf, the capsule
  machine, the transit kiosk) — so a sale settles
  only at its own counter, in `Wardrobe.buy`'s
  vocabulary, and "notsold" anywhere else.
- **Seed packets are real inventory.** The rack sells
  them at the crops' EXACT stated prices, derived from
  `Garden.CROPS` (pumpkin 3 🪙, the rest 2 🪙).
  `Garden.plant` spends a held packet first — no coin,
  no new spend booked, the packet was the payment —
  and otherwise keeps its order-at-planting path at
  the same price, stated plainly in both places: the
  counter is the in-person option, not a tax on
  planting. Packets stack per crop, never expire, and
  carry NO ItemDef (the bus pass's pattern), so they
  can never be gifted, fetched, eaten, served or
  stocked — pinned.
- **The general-goods counter** is the Mall snack
  shop's counter — the supplier `Stand.STOCK` has
  named for cheesy poofs and snack cakes all along —
  selling the stand list's four pantry staples into
  the real pantry at the shops' own prices (1 / 1 /
  2 / 2 🪙, the same numbers Liane's booth charges).
  The eat/gift/dinner disciplines are untouched.
- **The loop proof is in the suite:** prices are
  derived, so counter == restock == booth for every
  staple and rack == planting for every crop — no
  spread exists; `Stand.order` provably never touches
  a pantry count with a stocked pantry on the table;
  the garden door still refuses shop goods; a packet
  can't enter the crate by either door; and `Shop`
  has no sell verb and awards no coins.
- **The books:** seed money feeds the garden's one
  lifetime counter (the garden page's total now tells
  the truth across both doors); snack money accrues
  in `shopGoodsSpent`, printed as the till's book on
  the counter's dialog. No daily or per-visit cap —
  the purse is the cap, one unit a tap, like the
  booth. No new Journal page — the garden page and
  the counter's own dialog carry the record, per the
  established pattern.
- Saves are additive-only (packets in the inventory
  map, one `life` key for the till); a pre-0.12.0b
  save holds no packets, its garden page reads
  exactly as before, and it plants by supplier as it
  always has. Fan sheets untouched.

## Status — v0.12.0a
Built and verified 2026-10-16: compiles clean, full suite green
(100,329 checks, 0 failures — read off the final run's on-disk
log; a first run read 100,330, the known ±1 soak wobble), APK signed + badging
verified (v0.12.0a / code 88, `apksigner verify` OK, signer cert
SHA-256 a9997c1e… — the standing cert from v0.3.0b on; the v0.3.0b
keystore incident means this line installs over v0.3.0b and later
only). This build is Queue #12 item 1, **the paper grows up**:
the Gazette's three folded headline tiers land (season finale,
caper close, stand review) and the edition is delivered — a
porch copy at Your House and a one-text phone summary on
publication morning. No new module and no new save keys — the
tree stays at 98 Kotlin files and every new fact rides the
`life`/`lifeText` maps the paper already kept. Shelved quietly;
Queue #12 continues.

## Status — v0.11.0g
Built and verified 2026-10-16: compiles clean, full suite green
(100,219 checks, 0 failures — read off the final run's on-disk
log), APK signed + badging
verified (v0.11.0g / code 87, `apksigner verify` OK, signer cert
SHA-256 a9997c1e… — the standing cert from v0.3.0b on; the v0.3.0b
keystore incident means this line installs over v0.3.0b and later
only). This build is Queue #11 item 7, **polish #11 — the Queue
#11 audit and closer**: no new features and no production-code
changes — the suite grows the audit sections (a 400-day soak
living every Queue #11 surface on one save, the 98-cast census
stressed at the phone's honest worst case, contention re-pinned
at the code's own seats, budgets and censuses re-measured,
veteran save X) and the tuning ledger is completed for every
Queue #11 dial, including the sleepovers entry this audit adds.
The tree stays at 98 Kotlin files. Shelved quietly; this build
completes Queue #11, so the combined Queue #11 delivery ships
from here.

## Status — v0.11.0f
Built and verified 2026-10-16: compiles clean, full suite green
(count read off the final run's on-disk log), APK signed + badging
verified (v0.11.0f / code 86, `apksigner verify` OK, signer cert
SHA-256 a9997c1e… — the standing cert from v0.3.0b on; the v0.3.0b
keystore incident means this line installs over v0.3.0b and later
only). This build is Queue #11 item 6, **mystery arc #9 — "One More
Than Counted"** — one new module (`Mystery9.kt`, pure Kotlin; the
tree is at 98 Kotlin files), hooks in `Engine` (chapter slot behind
the alibi case, wrap resolution, participation, the reprinted-roll
tail, ambient callbacks), `GameActivity` (the Reckoning and
site-call dialogs, the Look hook), `TalkGate` (witness/aftermath
lines), `JournalActivity` (the 🧮 ledger board), `Finales` /
`School` / `Walk` / `MovieNights` (the Reckoning joins every
deference list), `Newspaper` (the ninth case's closing day joins
the CASE CLOSED headline read), additive save state in the
established arc pattern (pre-0.11.0f saves simply queue the case
behind the eighth). Shelved quietly; the combined Queue #11
delivery ships when the queue completes.

## Status — v0.11.0e
Built and verified 2026-10-16: compiles clean, full suite green
(99,986 checks, 0 failures), APK signed + badging verified
(v0.11.0e / code 85, `apksigner verify` OK, signer cert SHA-256
a9997c1e… — the standing cert from v0.3.0b on; the v0.3.0b
keystore incident means this line installs over v0.3.0b and later
only). This build is Queue #11 item 5, **the winter carnival** —
one new module (`Carnival.kt`, pure Kotlin; the tree is at 97
Kotlin files), hooks in `Engine` (agenda claim seated with the
fair's, rollover beside the fair's), `Minigames.applyCatch`
(the ice-fishing funnel), `Stand` (the purchase chance reads
the kettle rush), `Phone` (the Mayor's announcement text),
`GameActivity` (Look, the first hole, sculpture sessions from
the pond hub, dressing tokens), `SceneArt`/`Depth` (the
carnival layer), and the Journal (🎪 WINTER CARNIVAL) — zero
new GameState fields and zero new Prefs keys: every carnival
record rides life/lifeText. One week in deep winter at Stark's
Pond (the third Saturday of January; disjointness from every
finale window, holiday window and the summer fair proven by a
forty-year sweep in ONE lived ordinal space), ice fishing on
the real catch table under the fair's exact ledger discipline,
a snow-sculpture contest judged like the yearbook off the
filed build record, and a hot-drinks synergy with the player's
stand at real prices. Shelved quietly; the combined Queue #11
delivery ships when the queue completes.

## Status — v0.11.0d
Built and verified 2026-10-16: compiles clean, full suite green
(99,913 checks, 0 failures), APK signed + badging verified
(v0.11.0d / code 84, `apksigner verify` OK, signer cert SHA-256
a9997c1e… — the standing cert from v0.3.0b on; the v0.3.0b
keystore incident means this line installs over v0.3.0b and later
only). This build is Queue #11 item 4, **cast wave 6 — the
evidence audit**: the lorebook was re-audited entry by entry
against the 70-cast roster at the wave-5 standard, and the
evidence admitted 28 more citizens — 10 kids and 18 adults —
at the full wave treatment (measured sheets, fan sheets, 9
lines and 18 topics a head, 41 new pairs, distinct bark
signatures, the rival pool's ninth seat). The town is now 98
(42 kids, 56 adults). The audit itself is pinned live in the
suite — every admit at its exact lorebook length, every
near-miss at its evidence — so a future lorebook change fails
a pin and forces a re-audit. One new data file
(`CastDataWave6.kt`; the tree is at 96 Kotlin files), zero new
GameState fields and zero new Prefs keys — pre-0.11.0d saves
load whole. Shelved quietly; the combined Queue #11 delivery
ships when the queue completes.

## Status — v0.11.0c
Built and verified 2026-10-16: compiles clean, full suite green
(93,315 checks, 0 failures), APK signed + badging verified
(v0.11.0c / code 83, `apksigner verify` OK, signer cert SHA-256
a9997c1e… — the standing cert from v0.3.0b on; the v0.3.0b
keystore incident means this line installs over v0.3.0b and later
only). This build is Queue #11 item 3, **sleepovers** — one new
module (`Sleepovers.kt`, pure Kotlin; the tree is at 95 Kotlin
files), hooks in `Engine` (agenda stack, tick, rollover),
`Crew` (the occupied oracle learns the claim), the Talk gate
(the beats ride the existing DINNER rail — no new rung),
`House` (the morning note), the Journal (🛌 SLEEPOVERS), one
new played view (`PillowFightView`), zero new GameState fields
and zero new Prefs keys: Friday-night sleepovers for your
crew's kids at your house — a plan that names every refusal,
four beats in the dinners' discipline (the real pantry, a
fact-tied movie, a played pillow fight, lights out), and a
morning that pays the stated rates, plants the topic, and
writes the capped record. Shelved quietly; the combined
Queue #11 delivery ships when the queue completes.

## Status — v0.11.0b
Built and verified 2026-10-16: compiles clean, full suite green
(92,713 checks, 0 failures), APK signed + badging verified
(v0.11.0b / code 82, `apksigner verify` OK, signer cert SHA-256
a9997c1e… — the standing cert from v0.3.0b on; the v0.3.0b
keystore incident means this line installs over v0.3.0b and later
only). This build is Queue #11 item 2, **your garden** — one new
module (`Garden.kt`, pure Kotlin; the tree is at 94 Kotlin
files), one hook in `Engine.onNewDay`, one new Journal section
(🌱 YOUR GARDEN), zero new GameState fields and zero new Prefs
keys: four beds at Your House whose growth is read off the ended
day's real weather alone, five crops from the Mall garden
centre's seed rack, a harvest of real pantry produce that the
hosted and family tables can serve and the stand's crate can
sell only against the garden's provenance count, and the
season's biggest pumpkin judged at the county fair's new
produce weigh-in off its growth record. Shelved quietly; the
combined Queue #11 delivery ships when the queue completes.

## Status — v0.11.0a
Built and verified 2026-10-16: compiles clean, full suite green
(92,620 checks, 0 failures), APK signed + badging verified
(v0.11.0a / code 81, `apksigner verify` OK, signer cert SHA-256
a9997c1e… — the standing cert from v0.3.0b on; the v0.3.0b
keystore incident means this line installs over v0.3.0b and later
only). This build is Queue #11 item 1, **the town newspaper** —
one new module (`Newspaper.kt`, pure Kotlin; the tree is at 93
Kotlin files), one hook in `Engine.onNewDay`, one new Journal
section (📰 THE PAPER), zero new GameState fields and zero new
Prefs keys: a weekly edition composed entirely from the record —
the paper's own sky log, a stated headline precedence (case >
fair > play > vote > rivalry > sky > quiet), sections that print
one honest line when the week gave them nothing, classifieds at
real prices only, and past editions kept as headline lines under
two caps. Shelved quietly; the combined Queue #11 delivery ships
when the queue completes. (Provenance: the first builder on this
item was cancelled mid-flight by a runtime timeout; a finisher
assessed the tree as found — module, wiring, suite section and
README already coherent — ran the suite on it untouched and
green, and completed the release steps without redesign.)

## Status — v0.10.0g
Built and verified 2026-10-15: compiles clean, full suite green
(92,545 checks, 0 failures), APK signed + badging verified
(v0.10.0g / code 80, `apksigner verify` OK, signer cert SHA-256
a9997c1e… — the standing cert from v0.3.0b on; the v0.3.0b
keystore incident means this line installs over v0.3.0b and later
only). This build is Queue #10 item 7, **polish & performance
pass #10** — the queue closer, an audit-only build in the
standing polish shape: the 400-day soak lived every Queue #10
surface in one save (the family's full year, patrols and the
closed caper, a full production with its poster printed, the
three group rooms born and archiving on their rules, a stand
traded and reviewed, the eight-case lifetime re-driven),
veteran save IX re-measured under the 512K ceiling, the
contention stack re-pinned at twelve agenda seats and eleven
rails, fit QA on the queue's screens, and the tuning ledger
swept for the queue's dials. Production-code changes: see the
changelog entry — the audit's findings are declared there,
plainly. With this build Queue #10 is complete; the combined
Queue #10 delivery ships from the parent.

## Status — v0.10.0f
Built and verified 2026-10-15: compiles clean, full suite green
(92,072 checks, 0 failures), APK signed + badging verified
(v0.10.0f / code 79, `apksigner verify` OK, signer cert SHA-256
a9997c1e… — the standing cert from v0.3.0b on; the v0.3.0b
keystore incident means this line installs over v0.3.0b and later
only). This build is Queue #10 item 6, **mystery arc #8** — an
eighth case in an eighth shape: an alibi graph. On Family Night
setup evening the Elementary's marquee is found re-lettered and
one letter is missing; seven people were around the school in
the stretch between, every one of them produces an alibi, and
every alibi is vouched for, honestly. The Journal's board draws
the seven alibis and the four pairs that share an
event-signature; each pair is judged ONE EVENT or TWO, each
judgment graded by the pair's one settling piece, verdicts
derived on every read and never stored. Twelve evidence pieces
in the arcs' mixed-verb pattern, two of them unreadable until
their context chapter; the finale is the reading at Family
Night's item six, its hardest call gated on the graph's final
derived state; every landing persists (marks, bonds, standing
lines, ambient callbacks, an objector in the hard landings, a
delayed tail on one), and the game never admits a call was
wrong. The changelog entry below has the structure. Queue #10
continues with polish #10 next, on the parent's commission.

## Status — v0.10.0e
Built and verified 2026-10-15: compiles clean, full suite green
(91,911 checks, 0 failures), APK signed + badging verified
(v0.10.0e / code 78, `apksigner verify` OK, signer cert SHA-256
a9997c1e… — the standing cert from v0.3.0b on; the v0.3.0b
keystore incident means this line installs over v0.3.0b and later
only). This build is Queue #10 item 5, **your stand** — a kid
business run honestly. A one-time City Hall permit (10 🪙) plus
a kit (15 🪙) buys a pitch at one of four real outdoor spots;
stock comes from the town's real shops at the pantry's real
prices into a crate that never enters your pockets; prices live
inside a stated band; perishables spoil on stated shelf lives
into the books, never silently; the stand sells only while
staffed — you standing at it, or a crewmate who walked the real
route under the crew's occupied rules at 0.7 efficiency for
friendship. Customers are the townsfolk actually present, and
each one's chance reads price, weather fit, authored loves and
friendship. The Journal's 💰 YOUR STAND page keeps the real
books (bought = sold + spoiled + on hand, always), and after
five staffed days the rival's crew reviews the stand off those
books — an honestly-good review lifts foot traffic ×1.5 for
seven days. The economy is suite-proven bounded: the staged
season of good play earned 879 🪙 against the ledger's stated
5× paper-route-season bound, and the gouging-and-hoarding
simulation lost money. The changelog entry below has the
details. Queue #10 continues with mystery arc #8 next, on the
parent's commission.

## Status — v0.10.0d
Built and verified 2026-10-15: compiles clean, APK signed + badging verified
(v0.10.0d / code 77, `apksigner verify` OK, signer cert SHA-256 a9997c1e… —
the standing cert from v0.3.0b on; the v0.3.0b keystore incident means this
line installs over v0.3.0b and later only). This build is Queue #10 item 4,
**group chats** — the phone learns to talk to more than one person at a
time. A group is a thread kind inside the existing Messages store (same
records, caps, unread discipline and morning harvest), with three rooms
whose existence is earned: your crew's chat (born with the roster, fed by
the crew's own tallies and the photo records, archived when the last chair
empties), the family thread (Mom + Dad + you in one room; the v0.10.0a solo
threads stay, and this room carries the other parent's second voice), and
the current production's cast-and-crew channel (alive from the cast list
to the review morning). Every reactive post keys off a recorded fact —
nothing is invented, and the crew's one unkeyed habit, presence banter, is
a bank the suite audits for claim words. Quick replies work in rooms and
draw reactions only from members actually in them, and they never move
friendship or coins. The cap math was re-derived honestly — (70 cast + 2
parents + 3 rooms) × 2 = 150 ≤ `MAX_TEXTS` 160 — and the dial did not
move. The changelog entry below has the details. Queue #10 continues with
your stand next, on the parent's commission.

## Status — v0.10.0a
Built and verified 2026-10-15: compiles clean, APK signed + badging verified
(v0.10.0a / code 74, `apksigner verify` OK, signer cert SHA-256 a9997c1e… —
the standing cert from v0.3.0b on; the v0.3.0b keystore incident means this
line installs over v0.3.0b and later only). This build is Queue #10 item 1,
**your family arrives** — the New Kid stops living alone. Mom and Dad move
into Your House as household NPCs in the generated-NPC pattern (explicitly
not cast: the 70-cast census, bark floors, pair scripts, faction tables and
the rival pool are untouched, and family warmth never touches
`state.friendship`). Their looks derive from your own creator appearance
(skin exact; hair color exact for Mom, darkened for Dad, whose beard wears
your hair color; Dad is bald iff you are), with jobs at real places (the
Bank, the Post Office) on resident-shaped agendas. A played arrival beat
(day 3 of a new save; the next eligible morning for older saves) carries a
three-way stance the parents remember in their lines. Family dinners run
at your table three nights a week on the complement of the cast
households' rhythm, with the Dinners window, hosting and climax-deference
rules and a seeded cast guest; the weekly **family allowance** (3 🪙,
Mom's dinner rule) is the build's only new coin source; chores are tiny
errand-pattern favors that pay warmth, never coins. Mom and Dad get
Messages threads inside the unchanged `MAX_TEXTS` (72 × 2 = 144 ≤ 160),
appear in photos at home, hang in a family frame that starts empty, and
take darkroom prints through the darkroom's own gift discipline. The
changelog entry below has the details. Queue #10 continues with hero
nights next, on the parent's commission.

## Status — v0.9.0g
Built and verified 2026-10-15: compiles clean, APK signed + badging verified
(v0.9.0g / code 73, `apksigner verify` OK, signer cert SHA-256 a9997c1e… —
the standing cert from v0.3.0b on; the v0.3.0b keystore incident means this
line installs over v0.3.0b and later only). This build is Queue #9 item 7,
polish & performance pass #9 — the Queue #9 closer, audit-only, the
v0.7.0g/v0.8.0g shape: **zero production-code changes** (declared plainly;
the audit proved no defect in shipped code). A 400-day audit soak lived
every Queue #9 surface in one save — the 70-cast town, crew outings on the
Walk graph, a county fair week attended and entered, a darkroom print
outliving its album source at the cap, crew-vs-crew contests settling on
the solo ledger, and the seventh case never opening over the sixth — and
a dedicated drive re-proved the seven-case lifetime. Veteran save VIII
re-measured the saturated save against the 512K ceiling; fit QA covered
the queue's five new screens; the eleven-rail TalkGate ladder was pinned
and peeled live again (Queue #9 added no rail); the peak budget and audio
census showed no drift; and the tuning-ledger sweep closed the two
documentation gaps it found (the rival's crew had no ledger entry at all;
the cases entry now closes with the seventh case). The changelog entry
below has the evidence. **Queue #9 is complete with this build** (pending
the parent's verification); the next base bump and Queue #10 are the
parent's call.

## Status — v0.9.0e
Built and verified 2026-10-15: compiles clean, APK signed + badging verified
(v0.9.0e / code 71, `apksigner verify` OK, signer cert SHA-256 a9997c1e… —
the standing cert from v0.3.0b on; the v0.3.0b keystore incident means this
line installs over v0.3.0b and later only). This build is Queue #9 item 5,
the rival's crew: once crowned, your rival gathers a crew of three of their
own — derived by an authored precedence over real data (their pair-script
partners, then their faction mates, then every other kid; the pair's real
chemistry decides inside a tier), recorded once in the save and never
re-derived. The four hold court on the rival's faction turf on a seeded
rhythm through a self-guarded agenda claim, and stand in crowds as their
own cluster on their own affinity. Three crew contests ride the Rival
rail — a relay priced off the Walk matrix from where everyone actually
stands (your anchor leg run for real), a photo showdown at the turf
judged on the yearbook's group scale, and a showdown bracket whose other
pairs settle on the duel's own dodge math — each landing on the solo 🆚
ledger exactly once, moving nothing but the school-year CREWS standings
(2 for a win, 1 each for a draw; the season's final, in the school year's
last three weeks, counts double and plays a captains' stance beat). The
edge cases are said plainly: no crew of your own means the offer tells
you to get one; a rival in your crew captains theirs against you; a kid
in both crews sits it out; a side that can't field two free crew waits.
Saves are additive-only: `rivalCrew*` keys in the established Rival
patterns; pre-0.9.0e saves derive the crew by the same rule. The
changelog entry below has the evidence.

## Status — v0.9.0d
Built and verified 2026-10-15: compiles clean, APK signed + badging verified
(v0.9.0d / code 70, `apksigner verify` OK, signer cert SHA-256 a9997c1e… —
the standing cert from v0.3.0b on; the v0.3.0b keystore incident means this
line installs over v0.3.0b and later only). This build is Queue #9 item 4,
the darkroom: Your House gains a working darkroom corner — a red-lit
alcove with trays and a line of drying prints beside a three-frame print
wall — where the album's photos become PRINTS: physical keepsakes that
carry their own frozen copy of the photo's record, so a print survives
the album cap whole (caption and all) after its source fades, while
developing never touches the source record, its star, or any judging.
Developing costs stated paper money (2 🪙 a print, 5 🪙 a framed
enlargement) and honest game time (30 / 60 minutes on the town's own
clock; a moment that owns the town won't let you start). Enlargements
can be gifted from the person menu's gift flow — judged by the photo's
real fields at stated caps, once per photo per person, with the holder
remembered in their Talk — and hosted-dinner guests now remark on wall
prints that feature their household, in fact-slot lines drawn only from
the print's own copy; a yearbook winner's print wears a brass plate
derived from the volumes on the shelf. Saves are additive-only: one
`prints` key; pre-0.9.0d saves simply have an empty print box. The
changelog entry below has the evidence.

## Status — v0.9.0c
Built and verified 2026-10-15: compiles clean, APK signed + badging verified
(v0.9.0c / code 69, `apksigner verify` OK, signer cert SHA-256 a9997c1e… —
the standing cert from v0.3.0b on; the v0.3.0b keystore incident means this
line installs over v0.3.0b and later only). This build is Queue #9 item 3,
the county fair: one week every summer — the third Saturday of August at
Kid's Park, shifted a week earlier in years it would touch the Regatta's
finale week, and saying so — the town dresses the park (booths, bunting,
a ferris silhouette, the dunk tank, all struck the morning after), bends
its agendas fairward during fair hours (set pieces still outrank it), and
runs three contests on rails the town already trusts: a photo contest
judged by the yearbook's own math and masthead over fairground frames, a
fishing contest on the real catch table at the pond (a fair result lands
on the rival's ledger exactly once, or not at all when a duel is armed),
and a dunk tank thrown on the real showdown zones against the sitter's
own dodge. Ribbons, coins, a feats pair, one derived house piece (the
ribbon display by the yearbook shelf), a snack booth, four fair errands,
a ribbon-cutting to open, fireworks through SkyState to close — and a
Journal 🎡 COUNTY FAIR page that keeps the newest fair whole, elders as
one lines, and a slept-through fair honestly marked missed. Fair state
is one additive save field; pre-0.9.0c saves simply haven't had a fair
yet. The changelog entry below has the evidence.

## Status — v0.9.0b
Built and verified 2026-10-15: compiles clean, APK signed + badging verified
(v0.9.0b / code 68, `apksigner verify` OK, signer cert SHA-256 a9997c1e… —
the standing cert from v0.3.0b on; the v0.3.0b keystore incident means this
line installs over v0.3.0b and later only). This build is Queue #9 item 2,
your crew: up to four cast members can be asked into the new kid's crew
(Friend tier, from the tap-person menu — an in-talk choice, not a new
TalkGate rail). The crew is a standing arrangement, not a teleport: when
you arrive somewhere, each free member walks there on the real Walk graph
and arrives after the true route minutes from wherever they actually
were, announced on arrival. Occupied is occupied, said out loud — school,
shifts, a family dinner, a case or ceremony or screening they're part of
all send a regret that's owed to you in Talk — and set pieces pull
members away mid-outing the same way, never silently. At a place, the
crew renders as your standing Crowds cluster, talks crew (the outing, the
place, each other), and shares the counts: photos with crew in frame,
showdowns and contests witnessed — the rival's page quotes the audience —
with +1 friendship once per member per day at the lesson rate and nothing
more. The Journal keeps a 👥 YOUR CREW page (members, since-when,
outings, photos together, places together, contests witnessed), the town
notices ("your crew" is a memory topic with admiring, jealous and
unimpressed Threads follow-ups, and a SPOTTED bark when two or more roll
in), and if three of your four share a faction, that faction has a line
about it — informational only. The rival can be asked too: their contests
still fire, and their crew lines know exactly what they are. Crew state
rides the `life`/`lifeText` maps only — pre-0.9.0b saves start crewless.

## Status — v0.9.0a
Built and verified 2026-10-15: compiles clean, APK signed + badging verified
(v0.9.0a / code 67, `apksigner verify` OK, signer cert SHA-256 a9997c1e… —
the standing cert from v0.3.0b on; the v0.3.0b keystore incident means this
line installs over v0.3.0b and later only). This build opens Queue #9 and
the v0.9.0 series with the last big cast wave: **eight more named
townsfolk (cast 62 → 70: 32 kids, 38 adults)** — Roger Donovan,
Ryan & Sarah Valmer, Alexis & Mr. Testaburger, Nathan, Sophie Gray,
and Big Gay Al — completing the Donovan, Valmer, and Testaburger
households and giving Jimmy & Timmy their born nemesis. Saves are
additive-only: a v0.8.0g save loads unchanged and simply meets the
newcomers around town. The changelog entry below has the evidence
for every pick.
JVM smoke suite: 89,843 checks, 0 failures (all prior coverage,
plus the v0.9.0a section: cast wave 5 pinned end to end —
the roster arithmetic at 70 (32 kids + 38 adults, disjoint),
every newcomer's full citizenship (profile, measured sheet,
9-line bank floor, 18 topics, voice card, greetings, responses,
sleepy lines, blip family, lore alias, exactly one faction),
the constellation homes and held workplaces, the measured-
figure bands, the rival pool's eighth seat in last place,
the phone cap arithmetic at 70, the Bijou never-list's honesty,
the third Terrance & Phillip verdict pinned against the
lorebook itself, no new dinner table without an address,
and the fan-sheet params at exactly 70;
plus the v0.8.0g section: the Queue #8 audit — the lifetime soak
living the whole queue in sectionV030g, veteran save VII,
fit QA on the queue's five new screens, the eleven-rail
TalkGate pinned and peeled live, the queue's seams, the
seventh distance check on the peak budget and census,
and the tuning-ledger sweep;
plus the v0.8.0f section: mystery #6 pinned end to end —
the conviction meter PROVEN derived by recomputation over
every subset of witnessed gossip (2^5), proven evidence
(2^12), and public stances (4^5), plus 300 seeded mixed
states, with unwitnessed events moving nothing and a voice
spent on an unheard telling counting for nothing; the
two-column board's separation over every evidence subset
(a belief stands untouched mid-case however much testing
evidence is held, and is remembered after the meeting as
presented, held-but-never-presented, or still standing);
the meeting's published arithmetic checked across the
meter × evidence × call grid against the suite's own math;
the pressure bands at their exact thresholds in Talk,
Look, and the once-per-crossing morning ticker; all four
landings driven through the real episode wrap, scars,
marks, the restored corner a week after a clearing, and
the standing objections; a six-case lifetime driven day by
day; and a spoiler scan over every pre-reveal word of the
case, changelog included;
plus the v0.8.0e section: the rival pinned end to end —
selection determinism off the save's creation identity and
the recorded crown never re-rolled, the played reveal on
its RIVAL Talk-gate rail with its three seeding answers,
contest fairness measured on the real rails (2,100
scripted showdown duels at a 0.43 aggregate player share
with the authored dodge table driving the outcome, 2,002
seeded fishing duels both ways with the boot rule, the
bee and climax funnels settling exactly once), the
honest-clock race's parity proof (ties arithmetically
impossible for every pace against every minute 1–60), the
free decline moving only the next-challenge day with the
rest of the save byte-identical, prank cycles byte-identical
outside rival keys, standings derived from fabricated
answer histories and locking at exactly ten contests, the
score line quoted verbatim by the topic, phone, gossip and
morning surfaces, deference to the town's big moments,
save round-trips mid-race, armed and locked, and a spoiler
scan over every bank and page;
plus the v0.8.0d section: the town yearbook pinned end to
end — the June-to-May school-year calendar against the
spring boundary Finales computes, all six category
formulas checked by hand off fabricated records, the star
bonus at exactly +15 and the tie-break order, judging
deterministic across a save/reload, the judges' notes
audited over the full editor × category × record matrix
(no name, place, sky or weather a record can't prove),
blank categories and pamphlet and covers-only years,
publication at the June 1 turn through a real onNewDay
crossing and exactly once, the legacy catch-up and the
never-lived year, the winners' and runners-up memory
topics with their Threads banks, the five-volume shelf
reducing to index lines, the derived house shelf, the
album cap keeping its stars, and save round-trips old
and new;
plus the v0.8.0c section: errand emergencies pinned end to
end — the eight-scenario registry across all four verbs
with its spoiler scan, the deadline formula checked against
the Walk matrix for every ordered pair in town and every
chained triple, the day generator's 400-day soak inside the
chance band, the offer's rails (gate order, offered-once,
the DELIVER outranking, quiet hours, the infeasible-evening
refusal, the entry shout), all six deference owners plus the
offer-level integration, rescues judged through all four
verbs at exact rescue rates with the asker's own thank-you
and the memory thread, the clock settled on real ticks and
on Walk-priced minutes (alive before, gone on the first
tick past, inside one tick), failure's no-penalty ceiling
with the once-only coping beat and both morning-report
lines, the plate's own seventh slot, the feats crossing at
1 and at 10, and the mid-emergency save round-trip to the
minute;
plus the v0.8.0b section: faction capstones pinned end to
end — the eight-chain registry and the uniform reward shape
(eight distinct traces claimed in Depth's BACKGROUND band
and painted in SceneArt, eight titles, the 🏛 CAPSTONES
Journal page), the Beloved gate at 60 earned compositionally
through real income only for two factions (the Boys' Crew:
chain 30 + four members' tier crossings = 66; the Church:
chain 30 + both members' crossings = 48 deterministic, the
rest closed by Walk-rate road pitches) plus its 59/60
boundary, the senior's hint honest and once a day inside
owedLines, a full capstone driven through its own judges
with the standing ledger watched (beats pay no standing;
finishing costs the rival its 4), the screening photo's
room-and-night discipline, the vote count's ballot
discipline, the job beat landing only on a real clock-out,
the ceremony's gathering override and once-only rite,
members' topics turning from "the big ask" to "our
capstone", the morning note firing once the day after,
all eight capstones set in one long save with no faction
locked out, and save round-trips old and new;
plus the v0.8.0a section: cast wave 4 pinned end to end — the
roster arithmetic at 62 (30 kids + 32 adults, Kevin McCormick
and Kevin Stoley distinct citizens), every newcomer's full
citizenship (profile, measured sheet, 9-line bank, 18 topics,
voice card, greetings, responses, sleepy lines, blip family,
lore alias), the constellation homes, the 7-block days with
workplaces held (Lu Kim's wok, Adler's shop, Yates's desk),
one faction each, the visitor pool excluding all eight by
regenerated alias, Ike the smallest figure in town and
Kevin McCormick towering over the core crew, dinner tables
reseated (Marsh + Shelley, Broflovski + Ike, the Black
household as wave 3 left it), fan-sheet params at exactly 62;
and the capacity fixes the bigger town exposed — the movie
house's never-come (Towelie, Crabtree) can no longer be cast
as a feature's stars, the bee's wipe rounds reinstate instead
of crowning nobody, and the inbox cap fits the town;
plus the v0.7.0g section: the lifetime soak completed — the
54-cast town lived a full year with town secrets found only
inside their gates (10 of 12; the aurora-gated and rain-gated
secrets never aligned in this particular year and stayed
scenery), movie nights attended across seasons (97 nights;
buzz tiers legendary, buzzing and dead all hit; the
vote-headline Saturday honored on 29 nights), school days
lived (243 lessons sat, 243 recesses refereed, 41 Friday
bees entered — 21 won, some lost), shenanigan climaxes
PLAYED at mixed tiers (36 played; heroic and fumbled both
in the album; skipped events staying absent/legacy) and a
FIVE-case lifetime with no arc overlap; veteran save VI
saturated with Queue #7's records at their caps and
measured at 330,433 chars against the 512K ceiling; fit QA
for the TOWN SECRETS, MOVIE NIGHTS and SCHOOL pages, the
fifth case's claim-ledger board (CONFIRMED, BROKEN and
UNTESTED side by side) and episode credits carrying a
climax tier line, at font scales 1.0 and 1.3; the nine-rail
TalkGate order pinned under fabricated total contention
(Randy holds all nine, the CLIMAX rail peeled live between
FINALE and DINNER); the seams — the Saturday headline
expiring with its vote outcome, the City Hall secret's
gate reading standing outcomes only and never history,
arc #5 standing down all four deference lists, a bee
Friday sharing one afternoon with an event climax, and
crowds at the full 54 with the MIN_SEP floor holding; the
peak budget's sixth distance check with the honest audio
census after wave 3 (54 bark signatures, 54 distinct, no
new audio family); and the tuning-ledger sweep over every
Queue #7 constant, which closed the two gaps it found
(the fifth case's four dials and the bee's weekday);
plus the v0.7.0f section: the four chapters as data behind the
gardener case (ten-day booking, weekly gaps, holiday deferral),
the claim ledger's invariant over all 256 subsets of the
testable evidence (a claim reads only what its own evidence
settled; a confession's core claim can break by collapse and
can never be confirmed), both confessions' claim sets fully
checkable through their named channels, the confrontation's
gates (finale-only, climax stage, eight-piece boundary) and
its seat in all four deference lists, all four landings at
the wrap with their marks, scars, ripples and standing
lines, the cleared ending's delayed homecoming firing once
a week out, save round-trips mid-case and closed, a driven
five-case lifetime with no arc overlap (and the 400-day
lifetime soak's fifth-case pin), and a spoiler scan over
every pre-reveal surface including this changelog;
plus the v0.7.0e section: the ten chains as data (three beats
each, every choice row a landed/close/missed spread, STAND's
three surges, the PHOTO/FIND writing), the tier math at its
thresholds, the CLIMAX rail's window/turf/causer gates and its
seat between FINALE and DINNER (the v0.6.0g contention pin
extended to nine rails in place), every event's heroic path
completing (tier, outcome, Journal line, +2 friendship, the
flattering topic, the turf's mark) and fumbled path completing
(the roast, no friendship moved), the absent path byte-unchanged
plus the v0.7.0e section: the ten chains as data (three beats
each, every choice row a landed/close/missed spread, STAND's
three surges, the PHOTO/FIND writing), the tier math at its
thresholds, the CLIMAX rail's window/turf/causer gates and its
seat between FINALE and DINNER (the v0.6.0g contention pin
extended to nine rails in place), every event's heroic path
completing (tier, outcome, Journal line, +2 friendship, the
flattering topic, the turf's mark) and fumbled path completing
(the roast, no friendship moved), the absent path byte-unchanged
for all ten (legacy outcome, mark, and credits; no topic
planted), interruption settling at the tier earned (one beat,
two beats, mid-STAND), settle-once across a mid-chain
save/reload and at the wrap unit itself, the STAND truth table
and its teach-once rule across events in one save, the PHOTO
and FIND deed gates (wrong turf, wrong window, shown-too-early),
the three retelling banks pairwise distinct, the legacy
before-record mapping, and a spoiler scan over all chain text;
plus the v0.7.0d section: the school calendar pinned (Mon–Fri,
260 school days a year, class 9–3, recess at midday, Victoria's
Wednesdays), the 14-topic bank's rotation over a school year
(deterministic, no repeat inside two school weeks, the whole
bank across the year), the full lesson on all three
participation outcomes with the sprinkle paid only to the
kids actually in the room and reactions naming only the
present; deference fabricated live for all three quiets —
holiday beat, due case climax, finale week — with the bee
deferring too; recess rotation (the snowball truce only
ever over snow) and its one-a-day settle; the bee bracket
as pure data — determinism, the field only thinning, the
champion statistics over 104 Fridays (Wendy & Kyle deep,
Cartman's disqualification found and pinned), perfect
spelling winning with purse + feat + wall trophy and a
first-letter miss losing to the bracket's own champion;
midnight settling a walked-away bee; the teachers' Talk
remarks incl. Victoria's gold star at twelve lessons; save
round-trips; a spoiler scan of the source and surfaces;
and a 200-day lived year of lessons, recess, and bees
holding every history cap with the save lean; plus the
v0.7.0c section: the cadence and windows pinned
(Friday + Saturday, doors 19:00, last seating 20:00, out by
21:30 ahead of the sleep march), the feature rotation over
120 slots — shape, determinism, six-slot no-repeat, and a
spoiler scan of every generated title and card; the full
reaction truth table and the buzz tiers at their thresholds
(2 / 4 / 6) with the stars' pay at 0 / 1 / 2 / 4; attendance
determinism — stars always, the never-come never, a star's
close friend drawn by a fabricated bond, a real arch-rival
drawn to heckle; the Saturday vote-headline rule and its
Friday/bare-Saturday negatives; deference fabricated live
for all four darks — finale window, holiday beat, the booth
case's two Bijou nights, and a due case-1 climax judged at
doors; the full perfect-card session to legendary credits
(free admission pinned by an unchanged coin count), the
whiffed session's midnight settle without star pay, audience
lines naming only the cast actually placed in the room,
save round-trips, and a 40-screening season holding the
history cap at 12 with the counter keeping every night;
plus the earlier v0.7.0b section: the twelve-secret registry pinned —
ids, locations, and all twelve (trigger, gate) pairs distinct;
every riddle scanned against a per-secret forbidden-word list and
against every location display name, and the fresh Journal page
pinned to carry no place name, no found title, and no trigger or
gate word; every player-facing secret text scanned against the
mystery denylist; each LOOK secret staged closed (ordinary Look,
nothing found) and open (fires once, coins settle exactly once,
second Look plain), the bench secret fired only by its own bench,
the traveling gnome secret fired only at the gnome's own yard at
night with its keepsake granted once and the pre-owned case paying
coins instead; a present witness reacting by name and taking the
"town secret" memory topic, with the follow-up bank rendering;
the Sleuth feat firing exactly once at the sixth find and the
secret-map decor claimed at twelve; the morning note landing only
the day after; hostile found-records clamped; the save round-trip
carrying finds with their places; and the 400-day soak attempting
every secret under its open configuration — found count climbing
by at most one per attempt, never past twelve, events at cap).
**Queue #7 — in progress** (item 1, cast expansion wave 3, shipped
in v0.7.0a; item 2, town secrets, in v0.7.0b; item 3, Bijou movie
nights, in v0.7.0c; item 4, school days, ships with this build).
**Queue #6 — COMPLETE** (item 1, photo assignments, shipped
in v0.6.0a; item 2, favor chains, in v0.6.0b; item 3, mystery
arc #4, in v0.6.0c; item 4, family evenings, in v0.6.0d; item 5,
town jobs, in v0.6.0e; item 6, town votes, in v0.6.0f; item 7,
polish & performance pass #6, in v0.6.0g;
Queue #5 completed with v0.5.0i).
(The v0.6.0d suite: 59,905 checks — the household registry pinned
against the cast data, Tucker/Testaburger honestly absent; rotation
determinism and seven-week coverage; the collision deferrals fabricated
live — finale gathering, holiday beat, case-chapter cast, the climax
courtesy; the full invited lifecycle with exact wrap pays and no
double-pay; hosting's qualification math read live; the spread judged
from true state only; the thank-you odds staged at all three outcomes;
the phone's invite and morning-after thanks; the Journal page; save
round-trips and hostile docs; and a 60-day drive of the whole machinery.)
(The v0.6.0b suite: 59,466 checks — the chain dials; deterministic
chain stamps scanned across board, direct and shop asks; full
walks of 2- and 3-chains through the real judges — one plate slot
throughout, the closing bonus paid to the coin, one morning text
at the end; stale links substituted at reveal; mid-chain save
round-trips; gossip on the owed-lines rail; and singles completing
exactly as singles always have.)
(The v0.6.0a suite: 59,195 checks — keyFromOrdinal round-trips
across leap days and 1,500 consecutive ordinals; a full year of
weekly commission boards driven through the real rollover, every
posting deterministic, well-formed, non-repeating per asker and
REACHABLE (subjects co-proven present from the cast agendas,
conditions satisfiable by construction); the judge's truth table
over fabricated records; the plate lifecycle with cost-free
expiry; completion through the real applyPhoto — pay, stamp,
first-print feat, the asker's morning text; one photo satisfying
two asks; the near miss's "close, but…" playing exactly once;
feats at 1/10/25 prints; prints surviving a full album; and the
save round-trip plus hostile-doc discipline.)
(The v0.5.0b suite: the 14-piece registry proven complete —
every piece a real creator option with a real, existing source
(feat ids read from their own family tables, holiday ids with real
signature beats, named triggers); grant idempotence and one
fabricated retro-grant per feat family, per seasonal trigger and
per special; the coin rails end to end (wrong counter, already
owned, insufficient funds at 11-of-12, the full 55-coin buyout);
300 dice rolls never dealing a wardrobe piece; sanitize/preset
ownership pins; the save round-trip with decode clamping against
an inflated doc; the worn-piece rendering path — every piece
reaches avatarSpec in its own slot; and the notice rail, generic
and named-NPC lines alike).
On-device playtesting still needed (no emulator on the build VM) — the
tuning ledger below lists everything still set by paper math;
Lucky's playthrough is the verdict, as ever.

## Tuning ledger — set on paper, awaiting a device
Every gameplay/visual constant below was chosen by math, symmetry or
taste at a desk; none has been verified on a real phone. When playtest
feedback lands ("dusk feels too dark", "the arcs drag"), this is the
list it maps to — each entry names its home so the fix is one line.
- **The big update's living layer (v0.16.0a, `LivingTown` /
  `InteriorLayouts` / `ResidentialStreets`):** wander reach
  0.30 of the slot pitch and the wander period
  (5.2–7.8 s ÷ half the gait) in `Wander`; the sit
  transition at 7 ticks eased by `ActionAnims.sitPhase`;
  interior growth 2.4× in `SceneScale` (supersedes
  2.0×, item 4); furniture ladder and seat height
  0.30 u in `InteriorLayouts`; per-character motion
  bases per band plus 31 overrides in
  `MotionRegistry` — if a character moves wrong on
  device, their override row is the one line to fix;
  the bus route-board tap target is the ride control
  in both bus interiors (`BusRideState`).
- **The shared birthdays, whole (v0.15.0i, `Birthdays`):** the
  registry's four shared days (July 1 Cartman/Sheila,
  May 26 Kyle/Gerald, September 11 Butters/DogPoo,
  June 20 Tolkien/Michael — audited off the shipped
  table, pinned by name) resolve whole in every
  system: the phone sends EACH birthday person's
  beat (distinct senders; birthdays stand ahead of
  weather/hello in the harvest's shed order; the
  inbox cap sheds oldest read — re-proven over a
  full-census shared week, and the soak/worst-week
  "never two in a day" pins re-pinned to the whole
  rule at the pin); the paper names the day AS
  shared in TOWN NEWS; the party arbitration is
  stamped at rollover (`bdayPlan`) — joint honors at
  one table whenever the evening is free (every
  honoree paid the honor rate at the wrap; the one
  shipped same-table pair, Kyle + Gerald, always
  lands in the spring finale week, so theirs yields
  whole, for both names, with the reason), a
  family-table observance at every
  other free household holding a birthday member
  (same gathering claim, night line at rollover,
  NO friendship paid — the family marked it
  themselves), per-person standings on the Journal
  page for the table-less and the yielded alike;
  and a player birthday landing on a cast birthday
  (a fresh save's meets Stephen's October 3 in
  years two through five) keeps both observances
  standing under a stamped sharing (`bdayShare`).
  No dials of its own: the party still runs on
  Dinners' window, scale and deference, the DINNER
  rail, and the existing claim seat.
- **The apprenticeships (v0.15.0h, `Apprenticeships`):**
  five programs, one per job site — CityHall/paper
  (mentor McDaniels, skill Route Sense, threshold 6
  counted shifts), TegridyFarms/farm (Randy, Tegridy
  Hand, 6), TweekBros/shop (Tweek, Steady Hands, 4),
  JimbosGuns/shop (Jimbo, Counter Craft, 4),
  FreemansTacos/shop (Chef, Kitchen Rhythm, 4). The
  count is `apprShifts:<site>`, written only by Jobs'
  clock-out through `onShiftCompleted` — no grind
  currency, partial shifts settled at rollover don't
  count. Chains: five stages on the shared skeleton
  (first morning / routed errand / deep work /
  judgment / the mark), day-gates `GAPS` 0/2/3/4/7
  (`MIN_SPAN` 16 days), stipends `STIPENDS` 2/3/3/2/5
  with the judgment adding its resolution's quality
  0–2 — a whole chain pays 15–17 🪙, proven in-suite
  at less than half of what the same counter's
  ordinary work pays over the chain's own span (the
  books check reads Jobs' own defs). The master's
  rate is `MASTER_BONUS` +1 🪙 a shift at a mastered
  site, paid inside Jobs' clock-out line (weekly
  worst: half or less of the ordinary week's pay).
  Skill rungs `SKILL_RUNGS` by stages landed; a
  quit resets the site's card to 0 and keeps its
  count. The claim is the mentor's hours,
  `CLAIM_FROM` 9:00 to `CLAIM_TO` 17:00, standing
  only while an offer is live or the current stage
  waits on the mentor or their site, self-guarded
  on `Emergencies.momentOwned`, seated directly
  after Jobs (stack: twenty-three claims). Remarks
  run `REMARK_DAYS` 21 days from a completion, once
  per neighbor per mark, stamps `apprSaid:<site>:<npc>`;
  the mentor's own master line is standing, never
  consumed. The paper line is the program's
  `paperText` in the completion's `Jobs.weekIndex`
  week, TOWN NEWS copy only. Save keys (all in
  `life`/`lifeText`, additive): `apprActive` (text),
  `apprStage:<site>`, `apprStageDay:<site>`,
  `apprSub:<site>`, `apprDone:<site>`,
  `apprShifts:<site>`, `apprDeclined:<site>`,
  `apprSaid:<site>:<npc>` — no Prefs key, no blob.
- **The scale & space build (v0.15.0g, `SceneScale` /
  `FigureCompose` / `CastHomes` / `Traffic` / `BarFit`):**
  scene width is `SceneScale.factor` — interiors
  `GROW_INTERIOR` 2.0×, street scenes `GROW_STREET`
  2.5×, open land `GROW_OPEN` 3.0×, all over
  `OLD_FACTOR` 1.7 screens; composition widths
  anchor to `SceneScale.unitW` (facades, structures,
  dash rhythm, stripes, seams). Depth is
  `WalkBand.depthScale`: 0.75× at band 0 to 1.15×
  at band 1, one curve for player, crowd and props.
  The road band: `WalkBand.BOTTOM_OUT` 0.875 (the
  sidewalk's edge), `ROAD_TOP_OUT` 0.885 (==
  `Depth.FG_TOP_OUT`), road feet to
  `ROAD_FEET_BOTTOM_OUT` 0.985 at `BAND_MAX_OUT`
  1.5; crosswalks are `PaperDressing.crosswalks`
  (ends + heart; +2 downtown) read by painter and
  tap gate alike. Crowds compose in
  `FigureCompose`: window `WINDOW_SCREENS` 3.4,
  body ratio `WIDTH_RATIO` 0.72, pitch
  `PITCH_SLACK` 1.02, `MAX_ROWS` 7, the bus span
  forbidden via `busSpanFrac(GameView.BUS_FX 0.55)`;
  the station sign stands at `PaperDressing.SIGN_FX`
  0.34. Homes are `CastHomes.HOME`: 22 residence
  rows (the family houses), 75 occupation rows,
  1 hand-placed row (Crabtree → Elementary); nobody
  homes at the Bus Stop. Traffic is `Traffic`:
  `PERIOD_TICKS` 1140, `PASS_TICKS` 210,
  `LANE_GAP` 570 — ≈ 2.1 expected passes per
  scene-minute against the retired loop's ≈ 7.1.
  The button bar fits by `BarFit`: auto-size
  `MIN_SP` 8 to `MAX_SP` 14, 7 buttons, bar padding
  4dp + margins 14dp in the fit math.
- **The gallery show (v0.15.0f, `Gallery`):** the
  cadence is calendar math in `GalleryCalendar` —
  submissions open the season's 1st, hanging night
  is the second Friday of the middle month
  (`HANG_FROM` 18:00–`HANG_TO` 21:00), the show
  runs `RUN_DAYS` 7, the wrap lands the morning
  after; a dark evening (case climax / finale /
  holiday beat, read off `MovieNights.darkReason`)
  hangs it quietly at dawn instead of sliding the
  date. The venue is the Community Hall at City
  Hall (the town's hall — no separate Community
  Center location exists; Mystery6's case already
  names it). Themes: 12 in `Gallery.THEMES`, 3 per
  season, `themeFor` rotating by the show's index;
  each fit is a formula over record fields only
  (the per-theme weights live in the table). Entry
  bounds: `PLAYER_ENTRY_CAP` 2 (album frames from
  the season of their own dayKey only),
  `CAST_ENTRY_CAP` 8 derived entries (commissioned
  first, then best-followers; photobombs never
  count as sittings), `WALL_CAP` 10; darkroom
  prints are season-free and lent, never consumed,
  at `PRINT_BONUS` +3. The jury: `JURY_TRIOS`, two
  trios of three leanings (craft/heart/light),
  rotated by season+year; `scoreOf` = 2× fit +
  craft + the three lean reads + print bonus, all
  derived and recomputed, never stored. Prizes:
  `WINNER_COINS` 10 to a player winner at the
  wrap; `WINNER_PAY` +1 friendship to a cast
  winner, only with `attendedHanging` or
  `visitedShow`, paid once. The claim seats after
  Festival's, before Sleepovers' (stack: 22
  claims); visiting window `VISIT_FROM` 12:00–
  `VISIT_TO` 16:00 on show days. The shelf keeps
  `FULL_SHOWS` 4 wrapped shows whole, elders
  index-only. Paper: TOWN NEWS lines only (no
  headline tier — the ladder stands at ten).
  Phone: one `GALLERY` text on the wrap morning;
  `MAX_TEXTS` 220 unmoved. ONE new save key:
  `"gallery"`. If a dial feels wrong on a device,
  it moves in `Gallery`'s dial block and nowhere
  else.
- **The logo (v0.15.0e, `MainActivity` /
  `tools/gen_icon.py`):** the artwork is fixed —
  approved renders, hash-pinned, never regenerated
  or resampled. The only paper-set numbers are
  presentational: the splash frames the title art
  at 0.84 of the display width inside the plaque,
  aspect preserved (`adjustViewBounds`), and the
  plaque keeps its −2° crook. If the title feels
  big or small on a real phone, the 0.84 in
  `MainActivity` is the one line to move — the
  artwork itself does not change.
- **The overhaul correction (v0.15.0d, `WorldScale` /
  `WalkBand` / `FacadeGeom` / `TownBaseline` /
  `BusInteriorSpec`):** the size ladder lives in
  `WorldScale`, in kid units — `KID_FRAC` 0.19 of
  scene height, `ADULT_RATIO` 1.42, `HOUSE_PEAK_U`
  2.62 over `HOUSE_EAVES_U` 1.95, `PINE_U` 2.15,
  `CAR_LEN_U` 2.00, the bus `BUS_LEN_U` 3.40 ×
  `BUS_H_U` 1.50, `DOOR_U` 1.73, `TRASH_U` 0.40;
  the named exceptions are `SAPLING_U` 0.80 and
  `SILO_U` 3.10. The walk band is `WalkBand` —
  `TOP_OUT` 0.685 / `BOTTOM_OUT` 0.925 outdoors,
  `TOP_IN` 0.675 / `BOTTOM_IN` 0.905 indoors;
  prop footprints ride `PaperDressing.footprintsFor`.
  Doors read `FacadeGeom` (`GROUND_LINE` 0.66; door
  height = `DOOR_U`). The bus fix is a resolution
  rule, not a schedule change: `PaperTap.resolve`
  takes vehicles before figures before topmost.
  Interior decision: a free-walk layered interior
  (kept over a seated ride view — the aisle walks
  inside the band). The merged baseline table is
  `TownBaseline.BUILDINGS` (streets: boys, rich,
  main, civic, school, outskirts); it owns streets,
  looks and neighbor rows only — priced routes were
  audited against it and none flatly contradicts
  it, so no price moved. If figures feel large,
  move `KID_FRAC`; if a door feels grand, move
  `DOOR_U`; if the band feels shallow, move
  `TOP_OUT` — one line each, at their homes.
- **The visual overhaul (v0.15.0c, `PaperScene` /
  `BusNetwork` / GameView):** the plane parallax
  factors are `PaperPlane`'s — FAR 0.55, BUILDINGS
  0.85, everything at and below the ground 1.0; the
  dressing floor is `PaperDressing.MIN_CENSUS` 8
  pieces per location with the far row at
  `FAR_ROW_MIN` 3; the walk runs `PaperWalk.SPEED`
  0.34 world-widths a second with `BAND_WEIGHT`
  0.45 on the depth axis and `ARRIVE_EPS` 0.008;
  intra-scene walking costs zero game-minutes by
  rule. The bus prices at `BusNetwork.BUS_SHARE`
  2/5 of the route's raw walking minutes, floored
  at `MIN_RIDE_MIN` 10; the school bus stands
  `MORNING_FROM` 06:50–`MORNING_TO` 08:20 at the
  stop and `EVENING_FROM` 15:05–`EVENING_TO` 16:40
  at the school, school days only. If the town
  feels sparse, raise the census; if the kid feels
  slow, raise SPEED; if rides feel cheap or dear,
  move BUS_SHARE — each is one line at its home.
- **Mystery #12 (v0.14.0f, `Mystery12`):** the returns
  case opens `OPEN_DELAY_DAYS` 10 after the eleventh
  case retires; chapters land every `CHAPTER_GAP_DAYS`
  7 with holiday deferral, and its one delayed tail
  runs `POST_DELAY_DAYS` 7 on the full-read landing
  only. The spine is the returns ledger: 6 returned
  items, three derived checks apiece (the loss, the
  hand, the fit) recomputed on every read and never
  stored. Every reading (TRUE RETURN / KIND MISTAKE /
  PLANTED) is priced at its item's `callPrice` 2 live
  evidence; verdicts derive (OPEN / COMMITTED /
  PROVEN / REFUTED), refuted readings strikeable. The
  finale gate is tiered: the full read wants both
  load-bearing items PROVEN + all six read + none
  refuted + `provenCount12(state) >= 4`; the name call
  wants ≥1 proven; stand/walk are ungated; the gate
  re-checks at lock and at wrap. Evidence stands at
  12 (5 witness / 5 look / 2 participation), exactly
  two context-locked — the two load-bearing items'
  settlers. Four landings; the attic's CASE_TOTAL is
  12 and the case registers no new phone-note kinds.
  All state rides the `arc12*` save keys, additive
  only.
- **Mystery #13 (v0.15.0j, `Mystery13`):** the
  exhibition case opens `OPEN_DELAY_DAYS` 10 after the
  twelfth case retires AND only once a gallery show
  has wrapped with six entries of six distinct
  photographs (attended, visited, or entered by the
  player) — the case is the show's shadow: a save
  that never exhibited has no frame log to audit, so
  the queue waits unanchored for the next qualifying
  show rather than fabricating one, and the audited
  show is pinned (`arc13ShowId`) the day the queue
  arms. Chapters land every `CHAPTER_GAP_DAYS` 7
  with holiday deferral, and its one delayed tail
  runs `POST_DELAY_DAYS` 7 on the full-read landing
  only. The spine is the provenance chain: 6 frames
  in hanging order, three derived checks apiece
  (the print, the chain, the frame) recomputed on
  every read from the show's own records — the
  frozen entry record, the live album record, the
  darkroom print's record, the hanging's witnessed
  cast — and never stored. Every reading (TRUE
  HANGING / HONEST CROSS / SWAPPED PRINT) is priced
  at its frame's `callPrice` 2 live evidence;
  verdicts derive (OPEN / COMMITTED / PROVEN /
  REFUTED), refuted readings strikeable. The finale
  gate is tiered: the full read wants both
  load-bearing frames PROVEN + all six read + none
  refuted + `provenCount13(state) >= 4`; the name
  call wants ≥1 proven; stand/walk are ungated; the
  gate re-checks at lock and at wrap. Evidence
  stands at 12 (5 witness / 5 look / 2
  participation), exactly two context-locked — the
  two load-bearing frames' settlers. Four landings;
  the attic's CASE_TOTAL is 13 and the case
  registers no new phone-note kinds. All state
  rides the `arc13*` save keys, additive only.
- **The clubhouse (v0.14.0e, `Clubhouse`):** the spot
  derives from `seasonFor(gameMonth)` alone — SPRING/FALL
  the treehouse at `KidsPark`, SUMMER the garage at
  `StanHouse`, WINTER the basement at `YourHouse` — all
  fixtures at existing locations, no new map location,
  and the wrap re-derives the spot from the ended day's
  season. Hangout window `HANG_FROM` 15:00 to `HANG_TO`
  19:00 daily. The agenda claim seats twenty-first
  (after the stand's, before the crew's hook),
  self-guarded on `Crew.busyReason` read whole plus a
  veto pass over the claims seated above it, and priced
  like a rendezvous: `priceFor` = `Walk.routeMinutes`
  from the member's actual location, claimed only when
  `timeMin + price ≤ HANG_TO` (`priceFits`; at the spot
  or en route to it the price reads as the spot's).
  Beats `STEP_COUNT` 3 — 0 Settle In (Talk) / 1 The
  Project (Look) / 2 Crew Business (Talk) — riding the
  crew-talk channel and the Look; TalkGate stays at
  eleven rails. The wrap pays NO friendship and NO coins
  (Crew's arrival +1 is the day's rate, paid by Crew's
  machine, never twice), plants the `clubhouse` topic
  in attendees, and keeps `HISTORY_CAP` 8 afternoons of
  record (`clubRec:<day>` pipe format over a `clubDays`
  index). Records: the Journal 🏠 page and one
  morning-after house-report line; NO phone trigger and
  NO paper line by design (MAX_TEXTS 220 untouched).
  All state is `life`/`lifeText` stamps under the
  `club*` keys.
- **Your birthday (v0.14.0d, `PlayerBirthday`):** the date
  derives from the arrival record alone —
  `Family.arrivalDayPreview` (the stamped `famArrivalDay`,
  or the day it will stamp) — and fires on
  `arrival + 365k` for k ≥ 1 (`YEAR_DAYS` 365). Day-counted,
  never calendar-dated, so leap years cannot move it; two
  birthdays can never stack (one day in every 365 by
  construction); the first birthday is one full year in
  and nothing is retroactive. The morning gift is exactly
  one `GIFT_ITEM`, read as `Birthdays.CAKE_ITEM`
  (`snack_cake`, Mom's bake — the shop counter's staple),
  stamped per birthday in `pbGiftYear` so reloads never
  duplicate it. The parents' morning marking pays
  `GREET_WARMTH` +2 per parent in the family's own famPts
  lane, once per parent per birthday (`pbGreet:<parent>`).
  Wish bands derive from `Relationships.tierOf`:
  CLOSE_FRIEND (≥ 60 pts) warmest, FRIEND (30–59) glad,
  ACQUAINTANCE (10–29) standard-kind, below 10 no wish —
  Kenny's and Timmy's registers override any band — and a
  wish moves nothing but its stamp (`pbWishK:<name>`).
  The party rides the dinner hours by reference
  (`PARTY_FROM`/`PARTY_TO` = Dinners' own 17:30–20:30) at
  Your House, beats 0 Arrival (Talk) / 1 Cake (Look;
  consumes exactly one real cake when held — `pbCake`
  records which way it went — and states the improvisation
  honestly when none is) / 2 Send-off (Talk), guests =
  `Crew.members` whole, claimed at the sleepover seat via
  `gatheringBlock` + the `Crew.busyReason` oracle (no new
  agenda seat; TalkGate stays at eleven rails). Party
  conditions: the birthday, a non-empty crew, and no
  standing claim from `partyBlock` (finale week, holiday
  beat, case climax — both oracles — opening night, fair
  week, carnival week, a hosted dinner, family dinner
  night, a planned sleepover at your house); the yield
  reason is derived on the birthday morning, stamped in
  `pbYield`/`pbYieldDay` (Dance's pattern), and stated in
  the morning report and on the Journal page. The wrap
  pays `PAY_FULL` +2 / `PAY_PARTIAL` +1 per attending
  guest (a beat-2+ party also plants the `birthday party`
  topic); a party with no beats leaves no record. Records:
  one TOWN NEWS line in the birthday week's edition, one
  `playerbday` phone text on the birthday morning (sender
  = closest cast friend at FRIEND tier or above, else
  Mom), the Journal 🎂 YOUR BIRTHDAY page. All state is
  `life`/`lifeText` stamps under the `pb*` keys.
- **The film festival (v0.14.0c, `Festival` /
  `FestivalCalendar`):** the anchor is the Monday of the
  week containing December 8 (Monday Dec 2–8, Friday
  Dec 6–12), one week a year, five nights Mon–Fri,
  announced `ANN_BACK` 7 days ahead in the paper and on
  the marquee; the sweep (2026–2100, one calendar space)
  is the standing proof of the pocket. Doors, last
  seating and the gather window are MovieNights' own
  constants read by reference (19:00 / 20:00 / 21:30),
  and admission is free — the arts-line position, not a
  price of zero typed twice. The slate's order is a
  seeded (year) shuffle of the five prints; stars are
  2–4 from KID_ORDER minus the never-come, seeded by
  (year, film). The jury rotates over nine townsfolk in
  three leaning trios by `year % 3` — consecutive years
  share no juror. Scoring: qualities (authored 1–10)
  weighted `W_CRAFT` 4 / `W_HEART` 4 / `W_WEIRD` 2;
  each juror adds the film's value in their leaning
  dimension × `JURY_PER_LEAN` 1; the audience adds the
  night's buzz (clamped −3..6) × `AUD_PER_BUZZ` 2 —
  your buzz if you sat the night, `houseBuzz` (a seeded
  0..6 pure in year + film) if you didn't. The tie
  ladder is total → audience → heart → title A–Z. The
  winning film's stars take `WINNER_STAR_PAY` +1, only
  if you sat closing night, paid once at the wrap; the
  wrap lands on the first morning after closing inside
  `WRAP_TAIL` 30 days. The agenda claim seats twentieth
  (after the field trips', before the sleepovers');
  TalkGate keeps eleven rails. One closing-night group
  photograph, through `Photos.composeGroup`. Records
  ride `lifeText` (`festRec:<year>` pipe format, a
  `festYears` index); sessions ride day keys pruned at
  rollover.
- **Field trips (v0.14.0b, `FieldTrip` / `TripCalendar`):** three
  Fridays a school year, in the published rotation — fall is the
  SECOND Friday of October (Stark's Pond, nature study), winter
  the FIRST Friday of February (the Park County Historical Room,
  the town's museum, inside City Hall — no new map location), spring
  the THIRD Friday of April (Tegridy Farms, agriculture day). The
  pockets were chosen so the whole 2026–2100 sweep is clean against
  the dance (2nd Fri Feb), the talent show (2nd Sat Apr), the play's
  schedule, the fair and carnival weeks, the parade, every finale
  window and every holiday; the suite sweeps all of it in one lived
  calendar space, and the one runtime way a trip still yields — a
  case climax, a finale or a holiday owning the day, in School's
  standing priority — stamps its reason for the Journal. `ANN_BACK`
  7 (the poster is up a week ahead, on the board and the ticker).
  The roster is derived, never typed: every cast kid in the KID age
  band (39 — the young teens stand outside), and the chaperone is
  the school's own teacher rotation for the day (Fridays are
  Garrison's). The school day stands down whole (`QUIET_TRIP`,
  the lowest quiet reason: lessons, recess and the bee keep), and
  the class is held at the site by an agenda claim seated
  NINETEENTH in Engine's stack — after the parade's, before the
  sleepovers' — `CLAIM_FROM` 9:00 to `CLAIM_TO` 13:00, roster and
  chaperone only. TalkGate keeps its eleven rails; the trip adds
  none. Boarding runs `BOARD_FROM` 9:00 to `BOARD_TO` 9:30 at the
  classroom board; the ride each way is `BUS_RIDE_MIN` 30 minutes
  of game time; the bus back boards from `BACK_FROM` 12:30. Missing
  the bus is an ordinary missed day — stated plainly on the record,
  the paper and the text, and punished nowhere. The beat
  (`ROUNDS` 3) is a taught choice at each site — the pond's
  Specimen Scan (three water signs, taught in the intro, rotated
  by the day), the museum's Docent Quiz (two of its three rounds
  counted live off the game's own registries: `LOCATIONS.size`
  buildings in the model, the whole cast on the portrait wall),
  the farm's Harvest Help (three rows read the same way). A
  played-through beat pays `BEAT_PAY` +1 to the classmates
  physically present at the site, capped `SPRINKLE_CAP` 8, pays
  the chaperone +1 only at `GOOD_SCORE` 2 or better, and plants
  the "field trip" topic with the class — Dance's discipline,
  whole. Exactly one class photograph per trip, minted through
  `Photos.composeGroup` like the yearbook's, its id on the record.
  Records are per-instance in `lifeText` under `tripRec:`; the
  Journal page, the week's single TOWN NEWS line (the headline
  ladder stands at ten) and the one morning-after text under the
  new `FIELDTRIP` phone trigger all derive from them; the phone
  budget stands at `MAX_TEXTS` 220 and the sender set is untouched.
- **The reflections pass (v0.14.0a, `Sleepwear.mirrorCode` /
  `Heroes.streetCostumeCode`):** a rendering pass, not
  a system — it set NO dial of its own and added no
  save keys. Both readers borrow their hours from
  their owners: the Your House mirror draws your
  pajama reflection while a sleepover night is live,
  read off `Sleepovers`' own live window (guests
  arrived, from `ARRIVE_FROM` 18:00), and the code it
  returns is `Sleepwear.figureCode`'s player branch,
  so the glass and the album agree figure-for-figure;
  your hero costume renders in street POV exactly
  while `Heroes.playerInCostume` holds — identity
  made, a patrol night (Wed/Sat), the patrol's own
  window `PATROL_FROM` 19:30 to `PATROL_TO` 22:00,
  outdoors at a patrol spot — and the code is
  `Heroes.figureCode`'s player branch read live, the
  emblem `POWER_EMBLEMS[heroPower]`. Both readers are
  pure (a read leaves the serialized save
  byte-identical, suite-pinned). The walls stand:
  pajamas never draw on the street itself, and the
  creator's preview keeps showing the day look.
- **Light**: dusk light level 0.65, night 1.0 (`SceneLight.levelForBand`);
  the 26 per-location warmth hues (`SceneLight` LOC_LIGHT); the LIGHT
  layer's lamp-pool sizes (`SceneArt`).
- **Season finales (v0.5.0h, `Finales`):** the window is
  `WINDOW_DAYS` 7 — the season's last week; the gathering
  override holds 9:00–18:00 (`GATHER_FROM`/`GATHER_TO`) on
  the opening and closing days only; beats pay +2 to their
  person as they land, and the wrap pays the host +3, the
  ensemble +2 and the credited winner +2 more, all through
  the one friendship rail. If a week ever feels too long
  or the ensemble too thin on-device, the dials are the
  window length and the per-finale ensemble lists.
- **The darkroom (v0.9.0d, `Darkroom`):** developing is
  priced like the town prices everything — stated up
  front, in capsule coins and real minutes: a print is
  `PRINT_COST` 2 🪙 and `PRINT_MINUTES` 30 (two town
  ticks), an enlargement `ENLARGE_COST` 5 🪙 and
  `ENLARGE_MINUTES` 60; the clock that moves is
  `Engine.advanceTime` itself, so weather, agendas and
  deadlines keep their rules mid-develop, and
  `Emergencies.momentOwned` gates the start like every
  honest activity. The box holds `BOX_CAP` 12 prints
  (box + wall together), the wall `WALL_CAP` 3, and
  `TOTAL_CAP` 24 records all-in — past that the oldest
  GIFTED print is forgotten (stated in the develop
  result, never silent). Gift judging: base
  `GIFT_BASE_POSER` 4 for a posed frame / `GIFT_BASE_BOMBER`
  2 for a bombed one, +1 per other poser in chemistry
  ≥ `Photos.HIGH_CHEMISTRY` with the recipient (cap
  `GIFT_CHEM_CAP` 2), +1 per `GIFT_FOLLOWER_STEP` 25
  followers the shot earned (cap `GIFT_FOLLOWER_CAP` 3),
  +`GIFT_STAR_BONUS` 1 for a starred copy, total cap
  `GIFT_CAP` 8 — the dials if prints ever feel too
  generous or too stingy are the base and the caps.
- **Your family (v0.10.0a, `Family`):** the arrival lands
  on `ARRIVAL_ABS_DAY` 3 of a new save — a pre-0.10.0a
  save's family arrives its first morning under the build
  (the day is stamped at first consult, never slides).
  Family dinners run three nights a week on
  `DINNER_WEEKDAYS` {0, 2, 4} (absDay % 7) — the exact
  complement of the cast households' {1, 3, 5}, so the
  two tables never claim the same evening — inside the
  shared dinner window (`Dinners.DINNER_FROM`/`TO`), with
  a seeded guest on `GUEST_CHANCE` 0.45 of nights (your
  crew first, then your best-friended school kid). The
  **family allowance** is `ALLOWANCE_COINS` 3 🪙, paid at
  rollover on `ALLOWANCE_WEEKDAY` 0 (the week's first
  morning) only when a family dinner was sat at within
  the last 7 days — Mom's rule, and the ONLY new coin
  source in this build: against a full paper-route week's
  30 🪙 (5 shifts × (2 base + 4 tasks)) it is a gesture,
  not a wage — a tenth of a worked week, for dinner. Chores
  pay warmth only (one active at a time, lapsing free at
  midnight). Family warmth lives in `famPts:<parent>`
  (-50..100), never in `state.friendship`. Phone: the
  family's two threads bring the inbox math to 72
  threads × 2 = 144 ≤ `MAX_TEXTS` 160 — the dial does
  not move.
- **The family page + home barks (v0.13.0a,
  `Family`/`Barks`):** the page carries no dial at
  all — every line is derived from records other
  systems keep, and its presence read (`whereNow`)
  is pinned to the live `blockAt` across the week's
  hours. The one cadence is the home bark: a
  lingering moment indoors at Your House draws an
  idle bark at `HOME_IDLE_CHANCE` 0.08 per consult,
  under the cast's own bark discipline (the shared
  `COOLDOWN_MINUTES` 20, `ARRIVAL_CHANCE` 0.25 for a
  parent who newly appears, quiet hours
  22:30–06:30). Home is the only stage: the barks
  fire nowhere else, and move nothing anywhere.
  The chore book gains per-parent counters
  (`famChoreDoneBy:<parent>`) — the build's only
  new stored fact, kept because who asked a chore
  exists nowhere else once it completes.
- **Talent night (v0.13.0c, `Talent`):** the anchor is
  the second Saturday of April (`TalentCalendar`),
  announced `ANN_BACK` 7 days ahead — the dial if
  April ever feels crowded is the Saturday's rank,
  and the suite's 2026–2100 sweep is the standing
  proof the pocket is empty. The field is pure
  derivation: `FIELD_SIZE` 10 off appetite = nerve ×3
  + voice ×2 + a seeded 0–9 want, ties by name; the
  house beyond the bill is seeded at `AUDIENCE_PCT`
  45. The meter: `SCORE_BASE` 34 + nerve ×4 + voice
  ×2 + room chemistry ×14 + a seeded wobble of ±5,
  clamped 5–99 — the weights if scores ever bunch are
  the ×4/×2/×14 spread; ties break chemistry, nerve,
  alphabet. Your acts: sing 4 phrases / 1150 ms /
  jitter 0.03, jokes 3 / 900 / 0.03 with the hold
  raised to 0.85, magic 5 / 1300 / 0.05, dance 4 /
  1150 / 0.04, steadiness = zone power × (0.72 +
  0.05 × nerve 5); your score is 42 + 50 × quality,
  quality half held-fraction and half mean steadiness.
  The rehearsal is once, 8:00–18:00 in poster week,
  30 real minutes, `REHEARSE_BONUS` 6. The evening
  runs `EVE_FROM` 18:30 to `EVE_TO` 21:30; room
  prompts land after acts 2, 5 and 8 (`REACT_AFTER`).
  Prizes: `WINNER_COINS` 15 to the winner; friendship
  pays on a played-through night only — `ENTER_PAY` 1
  to every entrant, `WINNER_PAY` 2 more to the winner,
  `LEGENDARY_BONUS` 1 more when the house is
  legendary. If the envelope ever feels rich, it is
  the 15; if the room feels thin, `AUDIENCE_PCT`.
- **Hero nights (v0.10.0b, `Heroes`):** patrols run on
  `PATROL_WEEKDAYS` {3, 6} — **Wednesday and Saturday**
  (`Jobs.weekdayOf`, 0 = Sunday) — inside
  `PATROL_FROM` 19:30 to `PATROL_TO` 22:00 (the sleep
  hour ends every patrol; no hero is ever out past it),
  at `PATROL_SPOTS` KidsPark, MainStreet, StarksPond,
  Elementary, Mall. The roster is 16 personas, all
  original, suite-guarded against a franchise denylist.
  The claim seats below the rival crew's turf claim and
  above your crew's hook, and self-guards wholesale:
  a town-moment guard carrying every part of
  `Emergencies.momentOwned` that owns the town's story
  (a case's critical hour, a finale window, a holiday,
  the bee hours, a capstone ceremony) — less one
  deliberate exception, a live Bijou screening: a film
  owns its cinema, not the streets, Saturday is a
  screening night, and the screening's audience is
  still claimed individually by the movie rail inside
  `Crew.busyReason` (dinner, work, movie, ceremony,
  finale, live climax, live emergency), which the
  patrol honors in full — plus the fair's claim, your
  family's dinner-guest claim, and a crewmate
  mid-rendezvous. Parents and dinner win, always. A night's incident
  waits at a spot with seeded chance 0.75 and settles in
  the Look; escorts are priced by `Walk.routeMinutes`
  rounded up to the tick (floor 15 minutes) and paid in
  time, never coins. Beats pay a HERO tally only —
  patrols stood, assists, incidents, escorts, read on
  the Journal's 🦸 HERO LOG page — **no new coin source
  in this build** (hero work is its own reward; the
  ledger notes the absence deliberate). The caper runs
  five movements, one beat per patrol night, chapters
  gated on patrol nights at their spots; its finale
  stance and helped-count (of 4) write the ending, the
  Journal keepsake line, and — on any close — the
  `hero_mask` house decor, the census's 17th piece.
- **The county fair (v0.9.0c, `Fair`/`FairCalendar`):** the
  week anchors on the **third Saturday of August**, 7 days,
  and shifts one week earlier iff it would touch the
  finale window (the shift always clears it — proven for
  2026–2045, so a second shift is structurally impossible);
  fair hours `FAIR_FROM` 10:00 / `FAIR_TO` 20:00, closing
  night to `WATCH_TO` 21:30; announced `ANNOUNCE_AHEAD`
  3 days out. Contest days: photo entries days 0–2
  (`PHOTO_LAST_DAY`), fishing day 3 (`FISH_DAY`), dunk
  day 5 (`DUNK_DAY`), `DUNK_THROWS` 3 — and since
  v0.11.0b the fourth contest, the produce weigh-in,
  on day 4 (`PRODUCE_DAY`) against a field of
  `PRODUCE_FIELD` 5, judged off the pumpkin's real
  growth record (the garden entry above keeps the
  weight formula). Prizes are
  `PLACE_COINS` 6/4/2 capsule coins by place; townsfolk
  photo entries score `25 + rnd·140` on the yearbook-math
  scale (a strong fair frame beats the field; a lone
  selfie loses to most of it — the fairness dial if the
  fair ever feels rigged either way); dunk NPC arms run
  `0.80–1.15` against the sitter's real `dodgeFor`. Booth
  prices 1/1/2/2 capsule coins are the sink dial. If the
  week ever feels thin or crowded on-device, the dials
  are the field sizes (`PHOTO_FIELD` 6, `FISH_FIELD` 6
  + authored, `DUNK_FIELD` 5) and the fair hours.
- **The winter carnival (v0.11.0e, `Carnival`/
  `CarnivalCalendar`):** the week anchors on the **third
  Saturday of January**, 7 days, **no shift** — the
  disjointness sweep (finales, holidays, the fair, one
  ordinal space, 2026–2065) is the standing proof it
  never needs one; carnival hours `CARN_FROM` 10:00 /
  `CARN_TO` 20:00, closing night to `SKATE_TO` 21:30;
  announced `ANNOUNCE_AHEAD` 3 days out. Ice fishing on
  day 3 (`FISH_DAY`), the fair's field shape
  (`FISH_FIELD` 6 + authored) and prize table. The
  sculpture formula is the fairness dial: `SCULPT_PER_SESSION`
  12, `SCULPT_DAY_BONUS` 10 per further day, weather per
  session `SCULPT_WX_SNOW` +6 / `SCULPT_WX_GREY` +2 /
  `SCULPT_WX_SUN` −7, at most `SCULPT_PER_DAY` 2 sessions
  of `SCULPT_MINUTES` 30 on build days 0–2
  (`SCULPT_LAST_DAY`); the town's band is
  `SCULPT_NPC_BASE` 20 + `SCULPT_NPC_SPREAD` 110 against
  a perfect build's 128, the places drawn from a field
  of `SCULPT_FIELD` 6 — if the contest ever feels
  rigged either way on-device, the spread is the dial.
  The hot-drinks synergy is `COCOA_RUSH` 1.6 (hot
  chocolate, pond pitch, carnival week only) — the
  stand's first seasonal multiplier; if pond-pitch weeks
  ever feel too rich, this is the line.
- **Family evenings (v0.6.0d, `Dinners`):** invitations
  land three evenings a week (`DINNER_WEEKDAYS` = absDay
  % 7 ∈ {1, 3, 5}) in a 17:30–20:30 window
  (`DINNER_FROM`/`DINNER_TO`, well before the 22:00 late
  night); hosting gates at `HOST_MIN_DECOR` 4 decor
  pieces and `HOST_MIN_TIER` (the Friend tier, friendship
  ≥ 30), with a `HOST_COOLDOWN_DAYS` 10 rest per
  household; a full
  invited evening pays host +3 / family +2, an early one
  +2/+1; the hosted spread scores served food 2 +
  conversation pieces (true state only) up to 2 + full
  beats 1, with `WELL_SCORE` 4 paying +4/+3 and a
  thank-you at 1-in-`THANK_EVERY` (4), a Pocket Pal
  one-in-`ERRAND_CARD_EVERY` of those. If evenings ever
  feel too rare or too rich on-device, the dials are the
  weekday set and the two pay tables.
- **Town jobs (v0.6.0e, `Jobs`):** wages are honest
  pocket money, set against the errand board's 2–3 🪙 a
  favor: paper route base 2 + 1 a paper × `ROUTE_SIZE` 4
  (6 🪙 a shift, 30 a perfect week); farmhand and shop
  help base 3 + 2 a task × 3 tasks (9 🪙 a shift, 27 a
  week) — and one job at a time, so no stacking. Pay
  lands on payday, the first rollover where the
  Thursday-anchored `weekIndex` turns (accrued per shift
  at clock-out; a partial shift pays its landed tasks
  only). Reliability pays: `STEADY_STREAK` 5 consecutive
  completed shifts earns +`STEADY_BONUS` 1 a shift,
  `TRUSTED_STREAK` 12 earns +`TRUSTED_BONUS` 2 (by the
  streak after the shift). The miss ladder is
  `MISS_LIMIT` 3 consecutive unexcused misses — reminder,
  warning, let go — and any attended shift resets it;
  a shift missed to a case climax, a finale week or a
  family table actually sat at (evening shifts only)
  is excused before it counts. Hiring gates at standing
  strictly above `HIRE_STANDING_FLOOR` −10 (Factions'
  Suspect line). Windows: paper clock-in 8:00–10:00,
  shift ends 12:00; farm 8:00–9:30, ends 12:00; the
  shop counters keep their keepers' hours (Tweek Bros
  and Freeman's in 15:00–16:00, ends 18:00; Jimbo's in
  16:30–17:30, ends 19:30) and the rota turns weekly
  (`shopFor` = `SHOPS[weekIndex % 3]`). If wages ever
  feel rich or thin on-device, the per-job base/perTask
  pairs in `JOBS` are the dial.
- **Town votes (v0.6.0f, `Votes`):** one civic question
  a week, posted Thursday-to-Thursday on Jobs' week.
  The rotation is least-recently-used over the
  12-question bank, held to a `NO_REPEAT_WEEKS` 6 floor.
  Ballots are authored: lean = `CHAMPION_LEAN` 6 for an
  option's own champion + `FACTION_LEAN` 2 when the
  voter's faction is flattered + `SCHOOL_LEAN` 1 when
  their Pocket Pal obsession matches, over a 0–1 seeded
  jitter — the same week always tallies the same way.
  Your ballot weighs by the Fixture rule (errand title:
  Town Fixture ×2, Indispensable ×3, else ×1). A count's
  winner stands for `OUTCOME_WEEKS` 8 weeks unless its
  question is recounted first; the Journal keeps
  `HISTORY_CAP` 8 past counts. If Cartman ever votes
  sensibly, `CHAMPION_LEAN` is too low.
- **Movie nights (v0.7.0c, `MovieNights`):** screenings
  on `SCREENING_WEEKDAYS` Friday + Saturday — Friday is
  the rotation's premiere, Saturday headlines the
  standing matinee vote when one stands. Doors at
  `DOORS_FROM` 19:00, last seating `LAST_SEATING` 20:00,
  the audience lets go at `GATHER_TO` 21:30 and the
  feature's nominal end is `FEATURE_END` 21:30, ahead of
  the 22:00 sleep march on purpose. Admission is free —
  City Hall's arts line bought the print (the same money
  the matinee vote spends); if a ticket price ever
  feels missing, that's the position, not a bug. The
  house caps at `HOUSE_CAP` 14 (stars included); a star
  draws friends at chemistry ≥ `FRIEND_CHEMISTRY` 3 on
  the Photos scale and rivals at the rival line, plus a
  1-in-6 seeded regulars roll; Towelie and Crabtree are
  the `NEVER_ATTENDS` set. Generated features hold
  `NO_REPEAT_SLOTS` 6 of identity clearance. Reactions
  score `SCORE_RIGHT` 2 / `SCORE_CLOSE` 1 / `SCORE_WRONG`
  0, heckling off the flop `SCORE_HECKLE_OFF` −1 (on the
  flop it's the right answer, worth 2); tiers break at
  `BUZZ_POLITE` 2 / `BUZZ_BUZZING` 4 / `BUZZ_LEGENDARY`
  6, and the stars are paid `PAY_DEAD` 0 / `PAY_POLITE`
  1 / `PAY_BUZZING` 2 / `PAY_LEGENDARY` 4 friendship.
  The Journal keeps `HISTORY_CAP` 12 nights. If houses
  ever feel empty or rowdy, `HOUSE_CAP` and the
  regulars roll are the dial; if legends come too easy,
  `BUZZ_LEGENDARY` is already the perfect card — check
  the prompts first.
- **School days (v0.7.0d, `School`):** school on
  `SCHOOL_WEEKDAYS` Mon–Fri, class `CLASS_FROM` 9:00 to
  `CLASS_TO` 15:00, recess `RECESS_FROM`–`RECESS_TO`
  12:00–13:00; Principal Victoria covers
  `VICTORIA_WEEKDAY` (Wednesday), Garrison the rest.
  The lesson topic strides the 14-topic bank by school
  days (stride 5, offset 3 — no repeat inside two
  school weeks, the whole bank inside three). A sat
  lesson pays `LESSON_PAY` 1 friendship to each
  classmate actually present, capped at `SPRINKLE_CAP`
  10 desks, plus `TEACHER_PAY` 1 to the teacher for a
  right-ish answer; the funny answer and the pass pay
  the room the same and cost nobody anything — if
  attendance ever feels farmable, `SPRINKLE_CAP` is
  the dial, not the pay. Recess: one beat a day,
  refereed between two present kids; the fair call
  pays 2/2 (note relay 1/2 — the recipient takes the
  extra), a sided call pays its side 2. The bee:
  `BEE_WEEKDAY` Fridays, `BEE_FROM`–`BEE_TO` 13:00–15:00,
  `FIELD_SIZE` 8 kid spellers drawn seeded from the
  class; falls roll per (day, round, speller) at
  p = 0.14 + 0.08×(round−1) − 0.04×(skill−5), floored
  0.05 / capped 0.80, skills authored per kid (Wendy
  and Kyle 9, Cartman 4); Cartman is disqualified in
  `CHEAT_ROUND` 3 if still standing (sleeve-spelling),
  and no bee runs past `MAX_ROUNDS` 8 before the
  showdown. The winner takes `BEE_WIN_COINS` 5 capsule
  coins, the 🐝 Bee Champion game feat, and the bee
  trophy decor; the runner-up takes `FINALIST_PAY` 2
  friendship. Journal histories cap at `HISTORY_CAP`
  20 lessons, `BEE_HISTORY_CAP` 12 bees,
  `RECESS_HISTORY_CAP` 12 recess beats. There are no
  grade dials on purpose — attendance is texture.
- **Town secrets (v0.7.0b, `Secrets`):** `SECRET_COUNT`
  12 finds, no two sharing a (trigger, gate) pair —
  triggers `TRIGGER_LOOK` / `TRIGGER_BENCH` /
  `TRIGGER_GNOME`; gates read the real world (light
  band, weather family, season, aurora, a standing vote
  outcome). Discovery is deterministic — gate open plus
  trigger means found, first time only; gate closed
  means ordinary scenery, never a tease. Rewards settle
  once: coins 2–4 on three finds, one existing
  keepsake, the rest flavor. `FEAT_THRESHOLD` 6 earns
  the `FEAT_NAME` "🔎 Town Sleuth" feat; all twelve
  claim the secret-map wall decor. The morning note
  fires only the day after (`secretBeatDay`). If a
  riddle ever reads as directions, the riddle is wrong
  — the page is the only sanctioned hint.
- **Scene depth (v0.5.0g, `Depth`):** the foreground strip's
  top edge sits at `FG_TOP_OUT` 0.932 of the scene outdoors
  (`FG_TOP_IN` 0.928 inside) — below the front row's feet,
  above nothing that matters; the occlusion budget's worst
  chin is computed from `FH_BASE_MAX` 0.205 × `FIG_SCALE_MAX`
  1.50 × (1 − `HEAD_SHARE_MIN` 0.32), all three grounded in
  the measured sheets by the suite. If the frieze ever reads
  too tall or too shy on-device, the two FG_TOP numbers are
  the whole fix.
- **Crowd clusters (v0.5.0g, `Crowds`):** clustering starts
  at `CLUSTER_MIN_TOTAL` 7 present, caps at `CLUSTER_MAX` 4;
  in-cluster pitch is `TIGHT` 0.66 of a slot, the gap step
  between groups `BETWEEN` 1.08 slots, and `MIN_SEP` 0.05 of
  the world width is the pinned floor under tap honesty.
  Affinity: script +50, bond ×3, same faction +8, rivals
  blocked. If crowds read too tight or too scattered, these
  five numbers are the dial. (v0.9.0a: `rowsFor` gains a
  fifth row above 64 present — at 70 cast the four-row
  classic overflowed the classic margins at the MIN_SEP
  pitch; five rows keep everyone inside.)
- **Figure merge (v0.5.0f, `HeadStack`):** hair rides `HAIR_PAD`
  2 units proud of the head ellipse, hats `HAT_PAD` 3.5 — the
  pads ARE the no-sliver guarantee; per-hat brim lines live in
  `HeadStack.brimBottomCenter`; the under-brim fringe strip is
  capped at eyeT+3. If hair ever reads detached again, these
  are the numbers to reach for.
- **Errands (v0.4.0b, `Errands`):** the board posts `2 + seeded 0/1`
  fresh postings a day (a pure function of the day seed — the same day
  always shows the same cork); unaccepted postings expire at rollover,
  accepted ones persist until done or dropped. Direct asks fire at
  `ASK_CHANCE 0.35` when an NPC's current goal kind makes one sensible
  (grudges never ask), once per NPC per day; shopkeeper asks at
  `SHOP_ASK_CHANCE 0.5` at their own counter. The plate caps at
  `MAX_ACTIVE 6`. Friendship by type: FETCH 6, DELIVER 6, FIND 7,
  FIX 5, PHOTO 5, PLAY 4. Capsule coins 2/2/3/2/1/0 — PLAY pays 0
  because the game itself already paid. A thank-you item lands when
  the errand's id hash % 4 == 0 (gift tier only) — since v0.5.0c,
  1 in 5 of those thank-yous is a Pocket Pal card instead
  (`PocketPals.ERRAND_CARD_EVERY`); a thread topic
  plants when % 3 == 0. Feat chain: 10 "Neighborly", 50 "Town
  Fixture", 150 "Indispensable". State caps: the accepted list
  decodes at 18, recent completions at 8, asked-marks prune nightly.
  FETCH targets come only from the suite-pinned obtainable universe
  (no capsule coins, no winter-only litter); FIND respawns as a
  Look-find at a sensible place, never a second copy of a unique.
- **Favor chains (v0.6.0b, `Errands`):** generated asks are
  stamped chain heads at `CHAIN_CHANCE_BOARD` 0.30 /
  `CHAIN_CHANCE_ASK` 0.30 / `CHAIN_CHANCE_SHOP` 0.15 (a pure
  hash of the ask — generation itself never re-rolls); of the
  heads, ~30% run the full `CHAIN_MAX_LINKS` 3, the rest 2.
  The walk from a landed link to the next asker: stay with
  the beneficiary half the time, else step to one of their
  relations. The closing bonus is
  `CHAIN_BONUS_COINS_PER_LINK` 2 🪙 and
  `CHAIN_BONUS_FRIENDSHIP_PER_LINK` 2 💛 per link, paid by
  the origin asker on top of the final link's own rates. If
  chains feel too common, too rare, or too flat at the end,
  these five numbers are the whole conversation.
- **Horizons** (`Horizon`): the band silhouette colors (day `#8fa9c8`,
  dusk `#57486b`, night `#131b30`), the sight ranges (fog 24, blizzard
  30, heavy snow 44 map units), the tier heights (0.115 / 0.082 / 0.055
  of scene height), the tower's 0.17, the ridge band's 0.10
  (`RIDGE_HEIGHT_FRAC`) and the landmark cutoff at 85 grid units
  (`LANDMARK_MAX`, past which the water tower stops being drawn) —
  how far the town reads on a bad day is set here.
- **Weather wear** (`SceneLight.wearFor`): fog desaturate 0.38, blizzard
  whiten 0.38 / desaturate 0.18, storm darken 0.18, rain darken 0.14 —
  how hard each sky leans on the palette.
- **Sky** (`SkyState`): aurora strength by hour (1.0 at 23:00–0:00 down
  to 0.35 by 6:00), aurora dates (7 in a canonical winter), meteor
  nights (9 a year on a 91-day cycle) and their streak density
  (`SceneArt`), moonlight steps 0.05→1.0 across the 29-day cycle, the
  harvest moon at 1.45×.
- **Story arcs** (the `StoryArcs` cadence block): stages live 2 days
  solo / 1 day nudged; the warm ending at half the stages nudged or a
  pair bond of 6+; arcs open on their `minAbsDay` gates with a day's
  rest between one closing and the next opening.
- **The cases**: pond chapters every 7 days from day 3, the truth gate
  at 7 of 12 clues (`Mystery`); the booth opens +7 after the pond
  closes, the accusation +4 after the marathon, its gate 6 of 10, the
  lost-and-found reel +7, the wrong-name scar 15 (`Mystery2`); the
  dig opens +10 after the booth closes, chapters every 7 days, the
  disposition gate 8 of 12, one ending's decision landing +1 day
  (`Mystery3`); the fourth case opens `OPEN_DELAY_DAYS` (10) after
  the dig closes, its chapters `CHAPTER_GAP_DAYS` (7) apart, the
  vigil gated at `VIGIL_MIN_CLUES` (8 of 12) from `VIGIL_FROM_MIN`
  (17:00), the last fix `LAST_FIX_DELAY_DAYS` (1) past the vigil,
  and the endings' later work scheduled +30/+60/+105 and +21/+55
  days out (`Mystery4`); the fifth case opens `OPEN_DELAY_DAYS`
  (10) after the gardener closes, its chapters `CHAPTER_GAP_DAYS`
  (7) apart, the confrontation gated at `CONFRONT_MIN_CLUES`
  (8 of 12), one landing's homecoming scheduled
  `LURE_RETURN_DELAY_DAYS` (7) out, and the endings' costs read
  `NAME_SCAR` (8) / `BELIEVE_SCAR` (6) (`Mystery5`); the sixth case
  opens `OPEN_DELAY_DAYS` (10) after the confessions close, its
  chapters `CHAPTER_GAP_DAYS` (7) apart, its meter reading
  `METER_BASE` (45) plus witnessed gossip, proven evidence and
  stances (`VOICE_BACK` +4 / `VOICE_QUESTION` −3 / `VOICE_SILENT`
  0), the meeting gated at `MEETING_MIN_CLUES` (6 of 12), its
  verdict bands `VERDICT_CONVICT_AT` (45) / `VERDICT_HUNG_AT`
  (15) with `BACK_BOOST` (10) and `SPEAK_RELIEF` (15), one
  landing's restoration scheduled `WALL_RETURN_DELAY_DAYS` (7)
  out, and the conviction's cost read `CONVICT_SCAR` (6)
  (`Mystery6`); the seventh case opens `OPEN_DELAY_DAYS` (10)
  after the opinion case closes, its chapters `CHAPTER_GAP_DAYS`
  (7) apart, its timeline's readings priced at two live pieces
  per movement, and one landing's rebuilt case scheduled
  `CASE_RETURN_DELAY_DAYS` (7) out (`Mystery7`); the eighth case
  opens `OPEN_DELAY_DAYS` (10) after the backwards case closes, its
  chapters `CHAPTER_GAP_DAYS` (7) apart, its pairs' judgments priced
  at two live pieces per pair, and one landing's returned letter
  scheduled `LETTER_RETURN_DELAY_DAYS` (7) out (`Mystery8`); the
  ninth case opens `OPEN_DELAY_DAYS` (10) after the alibi case
  closes, its chapters `CHAPTER_GAP_DAYS` (7) apart, its sites'
  calls priced at two live pieces per site, and one landing's
  reprinted roll scheduled `LIST_RETURN_DELAY_DAYS` (7) out
  (`Mystery9`).
- **Your house** (`House`): waking at 7:00 AM sharp; the mail — a
  friend's note on 3 of 10 mornings when a close friend exists, junk
  on 1 of 10; the doorstep knock, once per visitor per day
  (`GameActivity`).
- **Minigames** (`Minigames.Feel`): the fishing bite window 950 ms
  (820 in heavy snow, 700 in a blizzard), the snowball timing zones
  and per-kid dodge windows, Taco Catch's fall/interval ramps and
  coin tiers.
- **Audio** (`SoundGen.PeakBudget`, `Sound.kt`): chain gains (music
  0.7, ambience 0.45), render ceilings 0.48, the per-channel default
  volumes. The budget proves nothing clips at defaults; nobody has
  heard the mix.
- **Barks & mood blips** (`Barks`, `SoundGen.BlipMood`, v0.4.0e):
  the 20-minute per-NPC bark cooldown, the 1-in-4 arrival chance,
  quiet hours 22:30–06:30, the evening wind-down from 19:00 (×0.6)
  and indoors (×0.7), the 2.2 s hello bubble (geometry: pad 13,
  height 34, gap 10 above the head, clamped 6 from the view edges
  and 6 below the top chrome — fit-QA'd in v0.4.0g at font scales
  1.0/1.3), and the mood
  contours (excited +16% pitch / 0.82× length, sad −16% / 1.30×,
  angry grit ×1.7). Proven as data and waveforms; none has
  been HEARD — whether Cartman's bark is recognizably Cartman
  is an ears question, not a math one.
- **Social math**: photo follower bases by prominence and the
  milestone ladder (`Photos`); NPC goals on 2-day cycles with 4
  presence ticks (`Goals`); friendship/bond clamps and the daily
  gain caps (`Engine`/`Threads`).
- **Group photos** (`Photos`, v0.4.0c): the frame holds 3 (4 for
  exactly the core four) plus one crasher; first-timers pay at
  `GROUP_RATE` 0.6; the chemistry bonus is +10 per recognized
  pair debuting together, capped at 2 pairs; chemistry =
  npcBonds + 2 for a recognized pair, "high" at 4, rivals at
  bond −3 or a canon arch-rivals note; STACKED wants an all-kid
  trio (KID/YOUNG_TEEN bands) with a high-chemistry pair inside;
  SAY CHEESE counts 25 lifetime group shots. Composition geometry
  (`Photos.GroupGeom`, drawn by `PhotoView`, measured in v0.4.0g):
  ground line 0.955 of photo height; LINEUP cap 0.86; SQUAD
  back/front caps 0.60/0.80 with a 0.17 lift and 0.10 inset;
  STACKED caps 0.66/0.80 with a 0.30 lift and 0.19 inset; a row's
  height also caps at slot width × 1.18 × aspect and at the
  headroom line (tallest ink at ×1.05 box overhead, 0.005 margin).
  Polaroid frame (`PhotoView`): photo rect insets 5% / top 5% /
  bottom 82%, chin stamp 9% of height. Whether a posed frame
  *feels* better than the pile-in is a device question.
- **Holiday beats** (`HolidayBeats`, v0.4.0d): the two clock
  gates are paper-set — New Year's crowd joins from 8 PM and
  the countdown Look opens at 11:30 PM on Dec 31; the Big One
  wants a Look at Kid's Park from 7 PM. All live as `minMin`
  on the beat's steps, one number each. Payouts (per-beat data):
  step coins 0–3 (five beats pay 2–3), an item gift on several
  steps (a snack cake, a flower), step friendship +2..+4 spread
  across the beat's cast — deliberately small next to a day's
  ordinary gains. Whether a player naturally lingers on Main
  Street that late is a device question (the bed is always
  right there).
- **Walking & the bus** (`Walk`, v0.5.0a): legs cost
  `MINUTES_PER_UNIT` 0.6 per map unit (a 13-unit block = 8 min;
  the cross-town golden route = 87 min), banked into 15-minute
  ticks through `walkCarryMin` so two short hops ≈ one tick;
  a learned shortcut takes `SHORTCUT_SAVE_MINUTES` 15 off its
  leg forever, floored at `MIN_LEG_MINUTES` 2. Road beats roll
  `BEAT_CHANCE` 0.55 at a pass-through stop (one per trip;
  pool cooldowns 1–5 days; the argument/pitch/busker are the
  noisy ones and keep the town's sleep hours). The bus pass
  costs `BUS_PASS_PRICE` 25 capsule coins at the Mall kiosk
  (free at 50 errands — Town Fixture money: roughly a week of
  minigames at 1–5 a session); a ride costs the first leg's
  raw minutes of the same route, any distance. Whether 87
  minutes for the big crossing feels epic or tedious — and
  whether the pass reads as freedom or as skipping the game —
  are device questions, all of them.
- **Wardrobe prices** (`Wardrobe`, v0.5.0b): the coin shelf is
  12 / 18 at the Mall rack and 15 / 10 at Jimbo's counter — a
  55-coin total sink, a little over two bus passes, priced
  against the same 1–5 coins a minigame session and 0–3 an
  errand pay. The feat thresholds ride the families' own tables
  (errand 10, the squad photo, Taco 500, 10 trips); the
  collection milestone (`COLLECTION_MILESTONE`) is 15 of 25 —
  past halfway, short of a grind. Whether a rhinestone cowboy hat is worth 15 coins of
  anybody's money is a device question Jimbo has already
  answered in the affirmative.
- **Pocket Pals** (`PocketPals`, v0.5.0c): a pull is
  `MACHINE_COST` 2 coins; the drum's per-card weights are
  100 / 36 / 9 by rarity (commons ≈85.7% of pulls, uncommons
  ≈13.2%, rares ≈1.1% — a specific rare is ~1-in-180 pulls,
  so the chase is meant to be finished by trading, not
  grinding). Errand thank-you cards: 1 in 5 thank-yous (1 in
  20 completions). Trade values 1 / 3 / 9, obsession school
  ×2 to its fan, friendship swing capped at `TRADE_SWING_CAP`
  ±6, one trade per kid per week, a stash of `STASH_SIZE` 3,
  and any one card's count caps at `COUNT_CAP` 99.
  Whether a 2-coin pull *feels* priced right against 1–5 coins
  a minigame session — and whether the kids' binder math
  reads as charming or stingy — are device questions.
- **Faction missions** (`FactionMissions`, v0.5.0d): beat 2
  gates at standing 10 (Liked), beat 3 at 30 (Respected) —
  the standing labels' own thresholds; beats pay 8 / 10 / 12
  standing with their own faction, 2 / 3 / 5 capsule coins,
  and `BEAT_FRIENDSHIP` +4 friendship to the beat's person. Finishing a chain
  costs `RIVAL_COST` 4 with its named rival, telegraphed in
  beat 3's offer; `RIVAL_OF` is a perfect pairing, so a whole
  lifetime of chains costs any one faction at most 4, and
  standing refills at +2 a road pitch and +3 a friendship
  tier crossing. Two beats keep office hours — the Girls'
  posting wants an evening Look at the Mall (after 5 PM) and
  the Church's labeling a morning one (6 AM–noon). Whether
  the gates feel like belonging or like homework is a
  device question every faction has an opinion about.
- **Faction capstones** (`Capstones`, v0.8.0b): the big ask
  opens only at `CAPSTONE_MIN_STANDING` 60 — Beloved, the
  standing table's own top rung; the gate guards the ASKING
  only, so a dip mid-capstone strands nobody. Beats pay
  `BEAT_COINS` 3 / 4 / 6 and `BEAT_FRIENDSHIP` +4 like the chains',
  but **zero standing with any faction** — the capstone is
  standing's destination, not another faucet — and finishing
  costs the missions' `RIVAL_COST` 4 with the declared
  rival, telegraphed by name in the ceremony's offer. The
  ceremony window is 15:00–19:00 (`CEREMONY_FROM` /
  `CEREMONY_TO`), and the faction gathers at its mark's
  place for it. The suite proves the gate reachable through
  real income only (a finished chain's 30 + friendship tier
  crossings at +3 + Walk road pitches at +2 — the town's
  smallest faction, the Church, composes to exactly 48
  deterministic and closes the rest on the road), and all
  eight capstones completable in one long save. Whether
  Beloved feels earned or merely expensive is a device
  question the rivals have opinions about.
- **Errand emergencies** (`Emergencies`, v0.8.0c): a day
  carries an emergency on `EMERG_CHANCE` 0.40 of days —
  inside the 30–50% band on purpose, often enough that
  the town feels alive with small disasters, rare enough
  that a shout still means drop everything. The deadline
  is the route's true Walk minutes plus `WORK_MIN_PER_STOP`
  10 a stop and `SLACK_MIN` 20 flat, and the generator may
  only offer what finishes by `QUIET_START` (22:30); once
  stamped, nothing extends it. A landed rescue pays its
  verb's ordinary rates plus `RESCUE_FRIENDSHIP_BONUS` 3
  and `RESCUE_COIN_BONUS` 2, and the rescue feats count at
  1 and 10. Failure moves no number anywhere — the suite
  pins friendship, coins and counts at exactly their
  pre-expiry values. Whether the allowance feels fair or
  tight is a device question the clock has no opinion about.
- **The town yearbook** (`Yearbook`, v0.8.0d): once a
  year, at the school-year turn (the year runs June to
  May and closes on spring's last day), the Elementary
  judges the player's own album into six categories —
  Best Group Shot, Best Selfie, Most Dramatic Sky, Best
  Commissioned Print, Busiest Scene, Town Favorite —
  scored by stated formulas over photo-record fields
  alone, no randomness at judging time. The player stars
  photos from the Album (⭐ worth `STAR_BONUS` 15 in
  every category the photo enters; a starred photo also
  never fades). Three cast editors — Wendy, Jimmy,
  Cartman — sign notes that are templates over the
  winning record's own facts; a category with no
  qualifying photo stays honestly blank. Volumes live on
  the Journal's 📖 shelf with their winning records
  inside; the newest `FULL_VOLUMES` 3 stand whole, older
  ones reduce to index lines; a year under `THIN_COUNT`
  6 photos is a proud pamphlet and a photoless year is
  covers only. Publication plants "yearbook famous" /
  "yearbook snub" memory topics in the pictured and the
  runners-up, and the first volume raises a derived
  shelf in Your House.
- **The phone** (`Phone`, v0.5.0e): a morning delivers at
  most `MAX_NEW_PER_DAY` 4 texts, one per sender, and only
  into an inbox under `MAX_UNREAD` 30; the store caps at
  `MAX_TEXTS` 220 / `MAX_PER_THREAD` 20 / `MAX_NOTES` 24
  pending facts, texts at `MAX_TEXT_CHARS` 280. Hellos want
  the Friend tier (`HELLO_MIN_FRIENDSHIP` 30; feat/photo
  reactors 10), fire at `HELLO_CHANCE` 0.4 on mornings at
  least `HELLO_EVERY_DAYS` 2 apart, `HELLO_SENDER_GAP_DAYS`
  3 per friend; sunny mornings praise instead of complain
  at a seeded 0.3. Quick replies pay `REPLY_BONUS` +1 at
  most `REPLY_DAILY_CAP` 3 a day and ack half the time.
  Group posts (v0.10.0d, `GroupChats`) ride the same
  harvest: at most `MAX_GROUP_PER_DAY` 2 land per
  morning, inside the same budget of 4 and the same
  unread wall, and the inbox math ran (70 cast +
  2 parents + 3 group rooms) × 2 = 150 ≤ `MAX_TEXTS`
  160 — the dial did not move. (v0.11.0d: wave 6's
  cast of 98 forced the dial — the same formula now
  reads (98 + 2 + 3) × 2 = 206 > 160, so `MAX_TEXTS`
  stands at 220, still a whole number of threads.)
  Group quick replies
  pay nothing, ever: a room is not a bribe funnel.
  Whether four texts a morning feels like a buzzing pocket
  or a quiet town is the device's call now.
- **Photo commissions** (`Commissions`, v0.6.0a): the board
  rotates every `WEEK_DAYS` 7 with `POSTINGS` 5 fresh asks;
  the plate holds `MAX_PLATE` 3; an accepted ask lives
  `EXPIRY_DAYS` 7 from the day it's taken. Pay is
  `PAY_ONE` 2 / `PAY_PAIR` 3 / `PAY_GROUP` 5 capsule coins
  by subject count plus `COND_BONUS` 2 when the ask carries
  a condition, and `FRIENDSHIP` +5 with the asker on a
  landing. Whether a week of five asks feels like a lively
  corkboard or a chore list is a device question.
- **Playable climaxes (v0.7.0e, `Climaxes`):** every
  chain runs `BEATS_PER_CHAIN` 3 beats in the climax
  stage's window (stage 2, 12:00–18:00) on the climax
  turf; a beat scores `SCORE_LANDED` 2 / `SCORE_CLOSE` 1 /
  `SCORE_MISSED` 0 and pays `BEAT_PARTICIPATION` 2 into
  the event's participation ledger, so a fully played
  climax reaches 6 — past the standing rule's ≥5 line on
  its own. HOLD YOUR GROUND is the one new interaction:
  hold all three surges for 2, crack only on the third
  for 1, anything earlier 0; it appears in three chains
  of ten. Tiers: HEROIC iff every beat landed, FUMBLED
  iff most beats missed (unplayed beats count as missed
  once any beat resolved), else GOT IT DONE; an
  untouched climax is ABSENT and wraps legacy. The wrap
  pays the causers `PAY_HEROIC` 2 / `PAY_HANDLED` 1 /
  `PAY_FUMBLED` 0 friendship and plants the tier's
  retelling topic. If climaxes ever feel too easy to
  ace, the dial is the choice rows' close answers, not
  the tier line — heroic is meant to be earnable.
- **The town yearbook (v0.8.0d, `Yearbook`):** the book
  judges the school year — June 1 to May 31, named for
  its closing spring — on the first morning of summer.
  The ⭐ star the player sets from the Album is worth
  `STAR_BONUS` 15 inside every category the photo enters
  (unstarred photos compete on their fields alone; a
  starred photo also never fades from the album). The
  category weights, all over photo-record fields: Best
  Group Shot 12/npc + 6/back-to-back pair + 3/goofy +
  followers; Best Selfie followers + 6/goofy + 3/happy
  + 2/npc; Most Dramatic Sky the sky-special value
  (aurora 40 / meteors 35 / harvest moon 30) + a weather
  drama table (blizzard 25 down to sunny 2) + 3 at
  night; Best Commissioned Print followers + 5/npc +
  10 for the stamp; Busiest Scene 10/figure + 4/refusal
  + npcs; Town Favorite 2 × followers + 3/npc. Ties
  break by earlier day, then record id — the same album
  always prints the same book. The shelf keeps
  `FULL_VOLUMES` 3 volumes whole; older ones reduce to
  index lines. A year under `THIN_COUNT` 6 photos is a
  proud pamphlet; a photoless year is covers only. The
  post-publication argument lasts `ARGUMENT_DAYS` 4
  (the topics themselves fade by the usual memory
  pressure). If a category ever feels rigged on-device,
  its weights in `Yearbook` are the dials — and the
  judges' notes are all templates over record facts, so
  a wrong note is a wrong slot, never a hallucination.
- **The rival (v0.8.0e, `Rival`):** somebody measures
  themselves against you. The pick is a pure function of
  the creation identity; the reveal falls due on day
  `REVEAL_FROM` 1 plus the seed mod `REVEAL_SPREAD` 3, and
  the first challenge follows `FIRST_CHALLENGE_GAP` 2–3
  days later. Challenges recur every `CHALLENGE_GAP_MIN`
  7 + 0–`CHALLENGE_GAP_SPREAD`-1 (5) days — a few a month
  at most — and declining defers by `DECLINE_GAP` 5 +
  seed mod 3 and changes nothing else, forever. An armed
  contest lapses free after `PENDING_LAPSE_DAYS` 4. The
  four offered kinds are day-seeded with weather sense
  (showdown needs snow; fishing skips blizzards; the
  follower race needs a milestone above both tallies);
  the two standing kinds (bee, climax) grade shared days
  off the bracket's math and a nerve roll of
  nerve + 0–4 (heroic ≥ 12, handled ≥ 8). Each pool
  member's authored dials: `nerve` (Cartman 7, Kyle 8,
  Wendy 9, Craig 6, Clyde 5, Bebe 6, Tweek 4), `paceHalf`
  — always odd, the race's tie-proof — (Cartman −3,
  Kyle +1, Wendy −1, Craig −1, Clyde +5, Bebe +3,
  Tweek −5), `castLuck` (the fishing table's weight
  shift: Cartman +2, Wendy +1, Bebe +1, Clyde −1,
  Tweek −1, Kyle and Craig 0) and `postPace` followers a
  night (Cartman 3, Wendy 2, Clyde 2, Bebe 3, Kyle 1,
  Craig 1, Tweek 1). Races need `RACE_MIN_ROUTE` 10
  route-minutes to the mark. Pranks first fall due
  `PRANK_FIRST_DELAY` 9+ days after the reveal, then
  every `PRANK_GAP_MIN` 16 + 0–`PRANK_GAP_SPREAD`-1 (11)
  days at `PRANK_CHANCE` 0.6 a due morning, and expire
  unanswered after `PRANK_EXPIRE_DAYS` 5. Results stay
  talk-fresh `FRESH_DAYS` 3. The standing locks at
  `LOCK_AT` 10 contests; the ledger keeps `LOG_CAP` 24
  results. If contests ever feel too frequent, too rare,
  or a rival too strong on-device, the dials are the gap
  pair, the per-member authored stats (validated live
  against the duel and spelling tables), and the nerve
  thresholds — never a scripted outcome.
- **Your crew (v0.9.0b, `Crew`):** four chairs, no more —
  `MAX_CREW` 4, and the ask opens at exactly `INVITE_MIN_POINTS`
  30, the Friend tier line, never a point below. Joining,
  leaving and swapping are always free; the only cooldown
  in the system is social: a member who leaves can't be
  re-asked the same day. Rendezvous are the Walk graph's
  own arithmetic — `Walk.routeMinutes` from where the
  member stands when the call goes out, never a flat
  fare — and the friendship pays `+1` per member per day
  on arrival, the school-lesson rate, coerced into the
  town's −50..100 like every other adjustment. Witness
  tallies are uncapped by design (they're counts of things
  that happened, in the established honest-counter
  pattern) and the place set is naturally capped by the
  town's 27 locations. The crew's pull in a crowd is
  `Crowds.CREW_AFFINITY` 40 — under a pair script's 50,
  over a faction's 8, and it outranks an arch-rival block
  between two of its own. If the crew ever feels too
  eager or too slow on-device, the dials are the invite
  line and the arrival payoff — never the walk minutes,
  which belong to the graph.
- **The rival's crew (v0.9.0e, `RivalCrew`):** their crew
  challenges arrive every `CREW_GAP_MIN` 12 +
  0–`CREW_GAP_SPREAD`-1 (6) days, the first
  `FIRST_CREW_GAP` 3 days after their crew forms;
  declining defers by `DECLINE_CREW_GAP` 5, an offer a
  side can't field waits `WAIT_GAP` 2, and having no
  crew of your own defers by `NO_CREW_GAP` 4. The
  standings pay `POINTS_WIN` 2 a win / `POINTS_DRAW` 1
  each a draw, doubled in THE FINAL (`FINAL_MULT` 2),
  which needs `FINAL_MIN_PLAYED` 2 contests on the
  books and falls in the school year's last
  `FINAL_WINDOW_DAYS` 21 days; finished seasons keep
  `HISTORY_CAP` 4 lines. Their court sits on the
  rival's faction turf `TURF_FROM` 15:30–`TURF_TO`
  19:30, one day in three. If crew contests ever feel
  too frequent or the final too easy to reach
  on-device, the dials are the gap pair and the
  final's two gates — never a scripted scoreline.
- **The school play (v0.10.0c, `Play`):** one production
  per school year on the yearbook's June–May rhythm:
  the poster on the first school day on/after Oct 19 of
  the fall, auditions two school days later, `REHEARSALS`
  4 rehearsal afternoons (`REH_FROM` 15:00–`REH_TO`
  17:00, right after class lets out), opening night the
  first Friday after — house `FINAL_FROM` 17:30, the set
  piece presentable until `FINAL_TO` 21:00, slipping
  `GRACE_DAYS` 2 evenings behind case climaxes, season
  finales, holiday beats and capstone ceremonies, then
  wrapping offstage. Casting is greedy by role prestige
  over authored weights (nerve from `STAGE_NERVE`, the
  Rival pool's eight keeping their Rival nerve exactly;
  voice = `School.skillOf`; chemistry with the director
  = `Photos.chemistryBetween`, clamped −2..6), a seeded
  tiebreak under 1 point so the data decides. The
  player's pseudo-stats are 5/5 with the delivery
  bending one axis +3 and the director-friendship axis
  running friendship/6 (cap 10) — the suite pins the
  gradient: a stranger understudies, a known kid (30)
  makes the ensemble, the director's kid (60, deadpan)
  takes the
  heart role. NPC rehearsal attendance is seeded at
  `NPC_ATTEND_PCT` 82 per member per session; the
  player's is played (an attMask), and prep for the
  night weights coverage 0.6 town / 0.4 you. Shared
  rehearsals sprinkle `REHEARSAL_PAY` +1 friendship to
  the room once per session, and cast pairs sharing 2+
  sessions gain +1 bond at the wrap (cap 8), settled
  after judging so it's the NEXT show's head start. The
  verdict is one weighted read — prep .45, chemistry
  .25, your moment .20, the room .10 — TRIUMPH at
  ≥ 0.70, BEAUTIFUL DISASTER at ≤ 0.40 or on a beloved
  fumble in a warm room (ovation ≥ 0.55), SOLID between;
  the ovation itself is computed from the room as it
  stands (fullness against 26 seats, plus each audience
  member's best real chemistry with the cast). The
  critic rotates on `Yearbook.judgeFor(year, 5)`. If
  opening nights feel too easy to triumph or the
  disaster too rare on-device, the dials are the two
  verdict thresholds and the prep split — never a
  scripted outcome.
- **Your stand (v0.10.0e, `Stand`):** setup is
  `PERMIT_COST` 10 🪙 (City Hall's one-time permit —
  the Mayor signs it herself) + `KIT_COST` 15 🪙, and
  moving costs `MOVE_COST` 5 🪙; the pitches are
  `STAND_LOCS` (MainStreet, KidsPark, StarksPond,
  BusStop) and the hours `OPEN_FROM`–`OPEN_TO`,
  8 AM–8 PM. Stock is the pantry list at `Fair.BOOTH`
  prices (lemonade 1, hot_chocolate 1, cheesy_poof 2,
  snack_cake 2 🪙), with shelf lives of 2 / 3 / 6 / 6
  days and an `ORDER_CAP` of 12 per item per day —
  the shops are shops, not warehouses. Prices live
  inside the band: shop price to `PRICE_MAX_MULT`
  3×, never below, never above. Selling happens
  only while staffed, at `BASE_CHANCE` 0.30 per
  present person per tick, read through the price
  factor (1.0 at shop price, 0.62 at 2×, 0.24 at the
  3× ceiling), the weather/season fit (the same
  (weather, season) read `SkyState` takes), their
  authored loves (×1.5, the gift read), friendship
  (up to ×1.5) and the review lift; `BUY_CAP_DAY` 2
  per person. A crewmate staffs at
  `CREW_EFFICIENCY` 0.7 of the player and is paid
  `STAFF_FRIENDSHIP` 1 per staffed day — no coin
  cut. The rival's crew reviews after `REVIEW_DAYS`
  5 staffed days, then every `REVIEW_EVERY` 10: a
  GOOD verdict (sell-through ≥ 60%, spoilage
  ≤ 25%) opens `REVIEW_LIFT` 1.5 for
  `REVIEW_LIFT_DAYS` 7 days. **The economy bound,
  named:** margins are capped by the band against
  real shop prices, demand by who is actually
  present (≤ `BUY_CAP_DAY` units a person), supply
  by `ORDER_CAP`, and unsold perishables spoil at
  cost into the books; the suite's staged season of
  good play (best pitch, 2× prices, staffed every
  open hour, crowds rotating the way real agendas
  rotate) earns within 5× a paper-route season
  (30 🪙 a week × 13 = 390 🪙, by `Jobs`' own
  tables) — and per staffed hour it pays under a
  paper shift's ~6 🪙, because the stand is a
  twelve-hour day, not a loophole. The same
  simulation at maximum prices with maximum
  hoarding loses money. If the stand ever feels
  rich or poor on-device, the dials are
  `BASE_CHANCE`, the price-factor slope and
  `BUY_CAP_DAY` — never the band, which is the
  integrity.
- **The town newspaper (v0.11.0a, `Newspaper`):** the
  paper keeps the last `SKY_LOG_CAP` 14 nights of sky —
  two weeks, so a week's weather always survives to
  print — and the last `HEADLINE_CAP` 8 editions as
  headline lines (the latest edition is kept whole,
  frozen at its week's turn). Publication is guarded by
  the log itself: an edition prints only for a week the
  paper actually watched a night of, so a save that
  arrives at a boundary it never lived gets no paper
  for it. If an edition ever feels stale or the page
  too long on-device, the dials are the two caps —
  never the precedence, which is the integrity.
- **Your garden (v0.11.0b, `Garden`):** `BEDS` 4 in
  the yard at Your House. Five crops with stated
  planting windows (calendar months), growth spans in
  effective (watered) days — pumpkin 55 (Mar–May,
  tender), corn 40 (Apr–Jul, tender), sunflower 35
  (Mar–May, tender), tomato 30 (May–Jun, tender),
  potato 45 (Mar–Apr & Aug–Sep, HARDY — the one crop
  frost and winter can't take). Seeds are real stock
  from `SEED_SUPPLIER` (the Mall garden centre's rack)
  at 3 / 2 / 2 / 2 / 2 🪙 a packet, bought at planting
  (the stand's supplier pattern). Growth reads the
  ended day's real weather at the rollover: rain
  waters, the WATER beat waters (a stamp per bed),
  anything else is dry — growth only on watered days;
  a sunny summer day on an unwatered bed adds a
  scorch day; a blizzard kills a tender crop where it
  stands; the first rollover into winter takes every
  tender bed left, ripe or not. Weeds gain a level
  per `WEED_EVERY_DAYS` 7 since the bed was cleared,
  capped at `WEED_CAP` 3; each level costs a yield
  unit (floor 1) and a tenth of a show pumpkin's
  weight (weight = (8 + 0.40 × grown + 0.30 ×
  watered) × (1 − 0.10 × weeds), ≥ 5 lb; a perfect
  pumpkin is ~47 lb against the fair field's seeded
  10–44). The beats cost minutes on the real clock:
  `PLANT_MINUTES` / `WATER_MINUTES` / `CLEAR_MINUTES`
  / `HARVEST_MINUTES` at 15 each — a full care round
  for all four beds is ninety minutes of beats,
  well inside the waking day.
  Produce reference prices at the stand (the band is
  measured against them): pumpkin 3, sunflower 2,
  corn / tomato / potato 1 🪙, shelf lives 10 / 6 /
  4 / 3 / 12 days. **The economy bound, named:** the
  suite simulates a PERFECT season (Mar 1–Sep 30,
  every bed replanted the morning it frees, watered
  and weeded daily) and values the whole harvest at
  the stand band's CEILING (3× reference): it
  grosses under 390 🪙 — one paper-route season by
  `Jobs`' own tables (30 🪙/week × 13), the stand
  ledger's unit — and nets under 200 🪙 after seed
  costs, on roughly 45 minutes of beats a day all
  season. The garden feeds the tables (dinners serve
  it, the stand sells it); it is not a wage, and no
  tuning of it should make it one. If a crop ever
  feels slow or fast on-device, the dials are the
  `growDays` and the weight coefficients — never the
  weather rules, which are the integrity.
- **Sleepovers (v0.11.0c, `Sleepovers`):** Friday
  nights only — `SLEEPOVER_WEEKDAY` 5, one a week by
  construction, and the rhythm is disjoint from the
  family table's {0,2,4} and the patrol's {3,6} in
  the one lived time space (a Friday is always
  absDay % 7 == 1; the audit re-sweeps it). Guests
  are your crew's kids — grown-ups don't do
  sleepovers — claimed at your house from
  `ARRIVE_FROM` 18:00 to `LIGHTS_OUT` 22:00, the
  claim seated after the play's and before the
  stand's in the agenda stack, riding the existing
  DINNER TalkGate rail (no new rung). The plan runs
  the full occupied discipline and names every
  refusal with its reason; below `MIN_GUESTS` 2 free
  guests the plan proposes another Friday instead.
  The night is `STEP_COUNT` 4 beats in the dinners'
  discipline: snacks (the real pantry's first two
  stocked items, consumed and named — Mom covers
  from parental reserves when the family is home),
  the movie (tonight's real Bijou feature, else a
  home movie described from one of your own album
  records), a played pillow fight (three swings on
  the showdown bar against the guest's dodge), and
  lights out. The morning wrap pays `PAY_FULL` +2 a
  guest for a full night, `PAY_PARTIAL` +1 at
  `PARTIAL_MIN` 2 beats landed, 0 otherwise — once
  per guest who was actually in the house — plants
  the "sleepover" topic in every attendee, and
  writes the record under `HISTORY_CAP` 8 nights.
  If a night ever feels stingy or generous, the
  dials are the two pay rates — never the refusal
  discipline, which is the integrity.
- **The Mall counters (v0.12.0b, `Shop`):** the
  garden centre's seed rack and the snack shop's
  general-goods counter, both at `Shop.LOC` (the
  Mall), second-row fixtures under the clothes rack
  and the transit kiosk. There are deliberately NO
  price dials here: a seed packet costs exactly its
  crop's `seedPrice` (pumpkin 3, the rest 2 🪙 —
  derived from `Garden.CROPS`, never re-typed), and a
  staple costs exactly its `Stand.STOCK` shop price
  (lemonade 1, hot chocolate 1, cheesy poof 2, snack
  cake 2 🪙 — the same numbers `Fair.BOOTH` charges),
  so the counters can never open a spread against the
  stand's band or the fair booth. No daily or
  per-visit cap either — the purse is the cap, and
  the counters sell one unit a tap, like the booth.
  As sinks: seed money accrues in the garden's
  `gardenSeedSpent` counter (one book across both
  doors — rack and order-at-planting), snack money in
  `shopGoodsSpent`, printed as the till's book on the
  counter's dialog. If a counter ever feels wrong
  on-device, the fix is in the source tables
  (`Garden.CROPS`, `Stand.STOCK`) — the counters
  inherit, they don't negotiate.
- **The paper, delivered (v0.12.0a, `Newspaper` +
  `Phone`):** the headline ladder stands at ten
  tiers — case closed → the fair → season finale →
  opening night → the vote → caper close → stand
  review → the rivalry → the sky → quiet week. The
  finale tier reads its wrap morning off the
  calendar (the day after the season's end —
  `Finales` stores no wrap day) and quotes the
  mark only in its own year; the caper tier
  repeats only the Hero Log's public ending
  phrase, from the one shared source both pages
  quote; the review tier cites the standing
  review's verdict words and no numbers. Delivery
  adds no state of its own: the porch copy is the
  frozen edition itself, and the phone summary is
  ONE `Phone.PAPER` text on publication morning
  from the week's rotating cast subscriber (Kenny
  excluded — a muffled headline is no headline),
  stamped owed in `paperSummaryDue` and cleared
  only when the text lands, spending the shared
  `MAX_NEW_PER_DAY` budget like every other beat.
  `MAX_TEXTS` 220 does not move: the census math
  is (98 cast + 2 parents + 3 group rooms) × 2 =
  206 ≤ 220. If summaries ever feel chatty, the
  dial is the cadence — one per edition — never
  the stamp discipline.
- **Cast birthdays (v0.12.0c, `Birthdays`):**
  every cast member's date is authored in `DATES`
  — exactly one is `LOREBOOK_SOURCED` (Clyde
  Donovan, April 10; the suite re-derives the
  split from the lorebook file itself, so an
  authored date can never rot into canon) and the
  rest are seeded-authored, 6–11 a month, four
  shared days, no Feb 29. The phone beat is the
  standing BIRTHDAY trigger: one fact-only text,
  the roster-first name on a shared day, no
  friendship or coin movement. The gift dial is
  `GIFT_BONUS` +3 friendship, paid inside the
  real gift math on the recipient's day only —
  off-day gifts are the untouched baseline. The
  party stands at the guest of honor's `Dinners`
  household in Dinners' own evening window,
  `STEP_COUNT` 3 beats (the welcome, the toast,
  the cake) riding the DINNER TalkGate rail as a
  party payload — no new rail, no new agenda seat
  of its own beyond the claim Dinners' shape
  already proves. The cake beat consumes one
  real `CAKE_ITEM` (a snack cake) when the pantry
  holds one and improvises honestly when it
  doesn't. The wrap pays the invited-table scale
  plus the honor bump: `WRAP_HOST_FULL` 3 /
  `WRAP_MEMBER_FULL` 2 / `WRAP_HONOR_FULL` 2 on a
  full night, `WRAP_HOST_PART` 2 /
  `WRAP_MEMBER_PART` 1 / `WRAP_HONOR_PART` 1 on a
  partial. The yield set is code, not a dial —
  finale week, holiday beat, case climax,
  shenanigan climax, opening night, fair week,
  carnival week, a same-table invitation, a
  hosted dinner, family dinner, a planned
  sleepover — and every yield returns its reason
  in words the morning report repeats. If
  parties ever feel rare, loosen the yield set;
  never touch the disjointness proof.
- **Sleepwear (v0.12.0d, `Sleepwear`):** no
  stored look anywhere — a pajama code derived at
  the shutter from the figure's own measured
  sheet: top from the shirt color, bottoms from
  the pants color, each softened toward the warm
  neutral (216, 203, 188) at a 0.62 keep / 0.38
  fade blend, hue family kept. Hats come off
  except the five soft-knit day hats in
  `NIGHTCAP_HATS` (beanie, pomhat, ushanka,
  chullo, trapper), which keep a floppy
  `NIGHTCAP_ID` cap in the pajama colors; exactly
  one trim survives derivation — the collar band
  in the bottoms' color. The freeze rule is the
  dial: a frame composed at Your House while a
  sleepover night stands and the evening has
  opened (`Sleepovers.ARRIVE_FROM`), frozen onto
  you and the night's planned kid guests only —
  never adults, never residents, never
  retroactive. Solids only, suite-scanned
  structurally (the trypophobia discipline: no
  dots, no patterns, no repeated motifs). If the
  pajamas ever read too pale or too street on a
  device, the blend is the dial — never per-kid
  tables.
- **The attic (v0.12.0e, `Attic`):** no dials
  and no state — every read derives from records
  the house already keeps. The overflow shelf is
  `COLLECTIBLES` past `House.SHELF_SLOTS` plus
  feat cups past `House.MAX_CUPS`, read off
  House's own overflow count. The outtakes are
  the family frame's candidates minus the framed
  (newest) one, plus print-held family photos
  whose album copy has faded — never
  double-listed — printed to `OUTTAKE_LINES` 12
  before the page counts the rest honestly. The
  case boxes stand one per retired case to
  `CASE_TOTAL` 10, public shape only: number,
  the Journal-published title, the closing day
  where the record keeps one, the board's piece
  count. If the attic ever feels thin, the fix
  is downstairs — earn more — never a stored
  attic inventory.
- **Mystery #10 (v0.12.0f, `Mystery10`):** the
  photo case. Chapters air every
  `CHAPTER_GAP_DAYS` 7 from an opening
  `OPEN_DELAY_DAYS` 10 after the ninth case
  retires, with holiday deferral like every case
  before it. The board is four frames × three
  checks, every check derived from the frame's
  own frozen fields on every read; a reading
  (GENUINE / MISFILED / COMPOSITE) is priced at
  `callPrice` 2 live evidence, verdicts derived,
  standing readings final, refuted readings
  strikeable. The finale's full read is gated on
  both load-bearing frames PROVEN + all frames
  read + none refuted + at least three proven,
  re-checked at the lock and at the wrap; the
  full-read landing's delayed tail returns at
  `LABEL_RETURN_DELAY_DAYS` 7. 🔴 Structure only,
  here and everywhere: the case's truth stays
  sealed inside its own content.
- **Mystery #11 (v0.13.0f, `Mystery11`):** the
  voice case. Chapters air every
  `CHAPTER_GAP_DAYS` 7 from an opening
  `OPEN_DELAY_DAYS` 10 after the tenth case
  retires, with holiday deferral like every case
  before it. The board is six letters × a derived
  voice scan, every finding recomputed from the
  letter's own text against its claimed signer's
  profile on every read; a reading (IN VOICE /
  OFF VOICE / BORROWED VOICE) is priced at
  `callPrice` 2 live evidence, verdicts derived,
  standing readings final, refuted readings
  strikeable. The finale's full read is gated on
  both load-bearing letters PROVEN + all letters
  read + none refuted + at least four proven,
  re-checked at the lock and at the wrap; the
  full-read landing's delayed tail returns at
  `POST_DELAY_DAYS` 7. The case touches nothing
  behind the pen-pal signup: its evidence base is
  the town's public voice cards, available to
  every save. 🔴 Structure only, here and
  everywhere: the case's truth stays sealed
  inside its own content.
- **Mystery #12 (v0.14.0f, `Mystery12`):** the
  returns case. Chapters air every
  `CHAPTER_GAP_DAYS` 7 from an opening
  `OPEN_DELAY_DAYS` 10 after the eleventh case
  retires, with holiday deferral like every case
  before it. The board is six returned items ×
  three derived checks (the loss, the hand, the
  fit), every finding recomputed from the item's
  recorded fields and its recipient's authored
  hand card on every read; a reading (TRUE
  RETURN / KIND MISTAKE / PLANTED) is priced at
  `callPrice` 2 live evidence, verdicts derived,
  standing readings final, refuted readings
  strikeable. The finale's full read is gated on
  both load-bearing items PROVEN + all items
  read + none refuted + at least four proven,
  re-checked at the lock and at the wrap; the
  full-read landing's delayed tail returns at
  `POST_DELAY_DAYS` 7. The case registers no
  phone-note kinds: its evidence base is the
  box's public records, available to every save.
  🔴 Structure only, here and everywhere: the
  case's truth stays sealed inside its own
  content.
- **Mystery #13 (v0.15.0j, `Mystery13`):** the
  exhibition case. Chapters air every
  `CHAPTER_GAP_DAYS` 7 from an opening
  `OPEN_DELAY_DAYS` 10 after the twelfth case
  retires — and never before a gallery show has
  wrapped whole enough to audit: the case arms
  only off a real wrapped show (six entries, six
  distinct photographs), so a save that never
  exhibited simply never arms it; the case waits
  for the next show. The board is six frames in
  hanging order × three derived checks (the
  print, the chain, the frame), every finding
  recomputed from the show's own records on every
  read; a reading (TRUE HANGING / HONEST CROSS /
  SWAPPED PRINT) is priced at `callPrice` 2 live
  evidence, verdicts derived, standing readings
  final, refuted readings strikeable. The
  finale's full read is gated on both
  load-bearing frames PROVEN + all frames
  read + none refuted + at least four proven,
  re-checked at the lock and at the wrap; the
  full-read landing's delayed tail returns at
  `POST_DELAY_DAYS` 7. The case registers no
  phone-note kinds: its evidence base is the
  gallery's public provenance chain — album,
  darkroom and frame log — the records the show
  itself wrote. 🔴 Structure only, here and
  everywhere: the case's truth stays sealed
  inside its own content.
- **The school dance (v0.13.0b, `Dance`):** the
  anchor is the second Friday of February
  (`DanceCalendar`), announced `ANN_BACK` 7 days
  ahead; the one arithmetic coincidence in the
  2026–2100 sweep (Feb 14 AS the second Friday)
  is the yield year, skipped whole with the
  reason on the poster. The ask: `ASK_MIN_POINTS`
  30 of friendship opens it; the authored answers
  split the 42 kids GLAD (yes at 30) / SHY (yes at
  `SHY_MIN_POINTS` 55, a soft no below, re-askable)
  / NO (kind and final at any number). A second
  YES stands the first kid up at `STOOD_UP_COST`
  3, kept on the Journal page forever. The
  evening runs `EVE_FROM` 18:00 to `EVE_TO` 21:30
  in four beats (arrival / slow dance / punch /
  last song). The slow dance is 4 phrases on the
  showdown bar; steadiness = zone × (0.72 + 0.05 ×
  your date's stage nerve) + jitter ±0.05, and the
  verdicts pay friendship only — `DANCE_PAY_GOOD`
  2 at the top two grades, `DANCE_PAY_SIDEWAYS` 1
  at the third, nothing for a stumble or for stag.
  The room beyond the bill is seeded at
  `ATTEND_PCT` 65. If asking ever feels cheap it
  is the 30; if the gym feels empty, the 65.
- **The town parade (v0.13.0d, `Parade`):** the
  anchor is the third Saturday of May
  (`ParadeCalendar`), announced `ANN_BACK` 7 days
  ahead, swept clear of the talent show and the
  spring finale window. The procession is derived
  whole on every read — school, the seven dinner
  households, the Bijou's float, the eight faction
  floats (crews capped at `FACTION_CREW_CAP` 6),
  your stand's float, your crew's walking entry,
  the Mayor's car last — and riders plus the route
  crowd partition the 98 cast exactly. The window
  is `PARADE_FROM` 10:00 to `PARADE_TO` 13:30;
  trips THROUGH Main Street mid-route pay
  `PASS_DELAY_MIN` 15 (arrivals, departures and
  the bus exempt). Candy: three throwing floats ×
  `THROWS_PER` 3 showdown throws, and `CANDY_CAP`
  6 binds even at perfect play — the year's whole
  haul is 6 items / 12 🪙, once. Marching pays
  `MARCH_PAY` 1 to your walkers at the wrap;
  watching passes the floats in `CHUNK`s of 4.
  If the haul ever feels rich it is the cap; if
  the street feels slow, the 15.
- **Pen pals (v0.13.0e, `PenPals`):** the school
  box is out Oct 1–21 (`SIGNUP_MONTH` /
  `SIGNUP_FROM` / `SIGNUP_TO`); late joiners wait
  a year, stated on the box. Letters in arrive on
  a seeded cadence of `GAP_MIN` 3 + `GAP_SPAN`
  (3–5 days); after `QUIET_AFTER` 2 unanswered
  letters the gap stretches `QUIET_STRETCH` 3,
  and `DORMANT_AT` 3 unanswered sends the route
  dormant until one revival letter reopens it.
  Warmth is its own lane (`palPts`, never
  friendship): `PTS_PER_REPLY` 3 a reply,
  `PTS_REVIVAL` 1 for the revival, bands at
  `BAND_REGULAR` 15 (writing-regular) and
  `BAND_FRIEND` 45 (a real friend); a reply costs
  `REPLY_MINUTES` 15 real minutes at the mailbox.
  The Journal archive keeps `KEEP_FULL` 16 letters
  whole and `FOLDED_CAP` 12 folded one-liners. The
  top band at the May 31 settlement earns the
  visit: June 10 for `VISIT_DAYS` 4, worth
  `TALK_PTS_PER_DAY` 1 warmth on each day's first
  talk. If the friendship ever feels fast it is
  the 3; if the summers feel far, the 45.
  **Polish #14 disposition (v0.14.0g):** the two
  special letters — the settlement's word and the
  post-visit thank-you — arrive at the HOME mailbox,
  not the school box, and a reply to one now rides
  home mail: `deliverSpecial` stamps `palHomeMail`,
  `canReply` honors it in any season, the send
  clears it, and a fresh October signup folds a
  still-waiting home letter into the archive. The
  dormancy rules ride whole (a home-mail reply to a
  dormant route is the revival letter, +1). Before
  the fix, a thank-you landing after the June 1
  settlement could not be answered until October
  (polish #13's soak closed 61 of 62 for exactly
  this); the cadence, warmth and archive dials
  above are untouched. **Season two (v0.15.0b):**
  the return trip. Direction rule: a top-band
  settlement stamps the return trip when she has
  visited and no trip stands booked, else her
  visit; below the band in a return season the
  maybe-next letter is the year-two wording. The
  trip rides `TRIP_MONTH` / `TRIP_DAY` June 16 —
  fifteen days after the settlement, the travel
  days stated in the letters as the seam — for
  `TRIP_DAYS` 4, one hosted beat a day (arrival,
  tour, counter duty, goodbye) worth
  `TRIP_BEAT_PTS` 2 in her lane, stamped in
  `palTripDone`. If the summers feel far it is
  the 45, twice over; if the beats feel light
  it is the 2.

## Changelog

### v0.16.0c (versionCode 122) — THE LINUX BUILD, THE WALK BETWEEN, AND TRUE PROFILES (Queue #16)

Lucky (Oct 18, 2026): "Make it work on android
and Linux so you can test it" — plus the Big
Update's two carried partials, the games-style
reference sheets, and the fidelity audit.

**The Linux mirror** (internal QA tooling, not
a shipped product): `tools/mirror/build_linux.sh`
packages `out/south-park-town-v0160c-linux/`
(8,721,430 B directory: app.jar + shim.jar +
kotlin-stdlib.jar + assets/ + run.sh +
capture.sh + VERSION.txt) from the same
compiled mirror classes as every mirror
artifact — ONE implementation, the same `src/`
tree as the APK. Verified by launching the
packaged build itself on this box:
`run.sh --selftest` auto-started Xvfb, opened
the real window, Robot-grabbed the frame
(160,971 B), delivered a real mouse press at
the window center (the game advanced — traffic,
an NPC conversation, clock at 8:15), and read
the live state back: at BusStop, timeMin=495,
save file written (205,200 B). This box lacks
libXtst6 (apt unreachable); the real library
was extracted from the Ubuntu noble .deb into
the build's TMPDIR and put on LD_LIBRARY_PATH
for the selftest only — the build itself needs
nothing extra where libXtst exists. The
Windows .exe was NOT refreshed (its launcher
packaging was never scripted in-tree; the
v0.16.0b toolchain survives at
~/workspace/.tmp-v0160b/toolchain/ — see the
Status section's correction note); it
stays at v0.16.0b, labeled stale in the Status
section. Parity account refreshed there too.

**Item #12 closed — cast visibly travel.** New
pure module `Travel.kt`. Before this build the
engine's grid walker moved agents between
schedule blocks, but presence was binary and a
mid-trip agent's location read MainStreet
wherever it was: nobody was ever seen crossing
the scenes between. Now `Engine.updatePositions`
stamps a trip record per walking agent in
`lifeText` — key `trip:<name>`, value
`origin|destination|startLen` (the build's one
new save-key family; additive only, cleared on
arrival) — and presence is derived purely:
route = `Walk.route(origin, destination)` over
the street network; progress = 1 −
remaining/departure grid path length (the
walker is the clock; monotone per trip); each
route leg maps half to its origin scene (crowd
heart → exit edge) and half to its destination
(entry edge → crowd heart). `GameView` draws a
traveler only in the pose scene, outdoors, on
the sidewalk band (Travel.BAND = 0.92) in
profile at scene scale, interleaved with the
crowd by feet line; settled crowds still
exclude walkers, so nobody pops mid-scene.
Two record-hygiene rules the suite's lived day
forced and the code now keeps: a trip that
completes inside one tick sheds its record in
the SAME tick (updatePositions maintains the
record again after the move), and a re-route
that hands the walker a longer path to the
same destination restamps the record from
where the walker visibly stands, so progress
is honest per stamped segment. Proof: the
mirror trip harness (Capture `--trip`) staged
Stan's real 9:00 trip BusStop → Elementary and
photographed 8 frames — origin, en route in
BusStop, en route in **Main Street** (the
intermediate scene; Stan in profile crossing
past the bus-stop sign), entering Elementary
at its edge, arrived and settled. Suite pins:
routes exist for every home↔agenda pair and
consecutive-block pair (1,216 pairs, whole
cast), and a lived 96-tick day is pinned tick
by tick — pose in a route scene at every tick,
progress monotone per stamped segment (a
waypoint rebuild starts a new segment), every
landing a completed dispatch (419 observed
arrivals), records shed on arrival. Suite:
109,034 baseline → **109,249 checks / 0
failures** (suite-4.log). One known quirk the
pins surfaced, named not hidden: when severe
weather turns mid-trip, the weather rewrite
and the block dispatcher disagree for one tick
and a walker can be re-routed back via the hub
— he walks every step of the detour (never a
pop or a teleport), but the doubling-back is
visible; the root fix is coords-aware
re-dispatch and belongs to a future build.

**Item #18 closed — true side profiles for
sleep + hero looks.** The v0.16.0a stopgap in
GameView (mirror-flipping the FRONT for the
sleep/hero looks when walking left) is removed
whole; walking facing flows to all three looks.
Sleep: pajamas + nightcap are spec geometry
(Sleepwear), drawn natively by the profile
pass, both facings. Hero: KidArt gains a real
profile overlay — `heroProfileGeom` (pure,
FigureGeometry-derived) seats a mask band
wrapping the turned head (nose side past the
ear, the front band's own eye padding), one
pupil, and the shared emblem (`drawHeroEmblem`,
extracted from the front overlay so a hero
wears ONE badge) foreshortened ×0.55 at the
chest's facing edge; the cape was already
back-item geometry the profile pass draws
behind the torso. Suite pins: geometry per
sheet × facing for all 98 specs, mirror
symmetry on avatar + adult + kid, sleep-spec
profile anchors + nightcap retention, source
pins for the stopgap's removal. Proof:
`new-kid-looks-sheet.png` — the three live
looks × front/profile L/profile R drawn with
GameView's own calls.

**Games-style reference sheets** (Lucky: "Make
new reference sheets using the South Park
games style"): `tools/mirror/SheetGen.kt`
renders each sheet with the game's own KidArt
from the character's own SheetSpec — HEAD /
TORSO / ARMS / STAND / SIT / PROFILE / EYES
OPEN / HURT EYES / SLEEP EYES / EYELIDS /
mouths / assembled — into
`~/workspace/goals/south-park-interactive-town/files/sheets-games-style/`:
98 cast sheets + the New Kid sheet + the live-
looks sheet + a contact sheet (101 files).
Lucky's original uploaded sheets are untouched;
sheets are never bundled into the APK. One
sheet bug was caught by reading Stan's sheet
and fixed at the root: SheetGen rendered at
the blink instant (t=0, seed=0 satisfies the
blink formula), so every idle front wore
closed eyes under an EYES OPEN label — renders
now sit at timeMs=1000; the HEAD crop gained
headroom for hat pomps, the STAND crop starts
below the torso hem, and the MOUTHS label
fits its panel.

**The fidelity audit** (target clarified by
Lucky: building style, interiors, and
character style aim for nearly 1:1 with Stick
of Truth / Fractured But Whole; TOWN LAYOUT
STAYS CUSTOM and was not audited). Method: the
launched Linux build was swept whole — 27
exteriors, 27 interiors, 14 house-street
sections, both bus rides, night passes (72
captures, read individually) — against the
games' read and, for characters, the new
sheets. CLOSED this build: the character-style
items above (#18 profiles; the sheets
themselves as the drift-proof source of truth;
in-scene figures render from the same specs
the sheets print — no figure/sheet divergence
found anywhere in the sweep). NAMED REMAINING
GAPS (not rushed; each wants its own build):
(1) *House facades* — flat single-story
silhouettes with small unframed square windows
vs the games' two-story houses with framed,
curtained windows, siding, garages, yards
(evidence: sweep ext/StanHouse vs the games'
Marsh residence; root: SceneArt house painter
+ TownBaseline house looks; estimate: one
build for the house archetypes).
(2) *Shop storefronts* — Tweek Bros reads as a
flat wall with two porthole windows vs the
games' glass-front coffeehouse (evidence:
sweep ext/TweekBros; the Main Street focal
storefront already wears full glass — the
per-location facades lag the focal one;
estimate: days, per-shop detail painters).
(3) *Interior furniture language* — counters,
tables and seats draw as front-facing flat
slabs where the games dress rooms with
angled, object-level furniture and decor
(evidence: sweep int/Elementary cafeteria,
int/TweekBros; root: InteriorArt furniture
painters; estimate: one build per room family,
school + shops first).
(4) *Figure outline weight* — the games ink
every figure with a consistent dark outline at
scene scale; ours outlines selectively, so
far-band figures read flatter (evidence: sweep
ext/MainStreet crowd vs games' street scenes;
root: KidArt/Paper outline pass; estimate:
one build, with a full re-sweep — outline
changes touch every figure).

No TalkGate rail, agenda seat, MAX_TEXTS, or
headline-ladder change. Saves additive only:
the `trip:` lifeText family is the one new key.

### v0.16.0b (versionCode 121) — THE MAIN STREET CORRECTION + THE SWEEP (Queue #16)

Lucky's playtest verdict on v0.16.0a, in four
screenshots, fixed at the root — then the
sweep he ordered over everything, on the
Windows mirror of the identical game (see the
status section for the parity account). Every
fix below was verified in before/after mirror
captures (kept with the build logs); a fix
counted only when its capture read clean.

**The playtest five.**
(a) *The slabs on Main Street.* NOT interior
furniture in the street: `drawParityDetails`'
"storefront-row" painted a fake shop row whose
sizes composed from the widened world width
(3060) instead of `SceneScale.unitW` (1224),
so each flat slab ran 0.64 of a screen wide,
burying the real facades' signs; its teal
companion was (b)'s band. The row and the band
are removed whole (old assets don't stick);
Main Street's registry now wears
"crosswalk-zebra" (drawn by the street-strip
painter all along) and "gazette-boxes" (two
newspaper boxes flanking the diner). The
disease was hunted through every token layer
that composed sizes from `w`: parity (all
branches), dig, fix, fair, carnival, faction,
holiday, season, light — positions still
anchor to the world; sizes anchor to the unit
width, per SceneScale's own contract.
(b) *The band and the floating plaque.* The
band was (a)'s town-banner, deleted with it.
The "Main Street" plaque was drawFacade's
generic crooked name sign, drawn ABOVE the
roofline for every building — duplicating the
merged-map baseline sign each shop, civic
building and the school already wears ON its
facade. Rule now: one name per building, on
the building — the generic sign yields wherever
a baseline sign exists (houses, which carry
none, keep theirs). The school's "name-band"
(a second SOUTH PARK ELEMENTARY across the
brick) and SoDoSoPa's "district-arch" (the
loft's name again, over its door) are retired
the same way; entrance steps and courtyard
planters wear their registry seats. Locations'
structure name signs moved onto the structure,
under the roofline.
(c) *Piles and pale discs.* Three roots. The
discs were the snow-buildup cap: a flat
0.40-wide ellipse center-anchored for every
pose, floating behind/above profile heads.
It is now a thick arc stroked along each
figure's OWN derived head ellipse
(FigureGeometry.derive on its spec — the same
numbers KidArt draws from), so it cannot
float. The pile on the player: FigureCompose
never sees the player, so cast could compose
onto his exact spot — the composed cast now
reserves the player's ground at his row. The
pine in the street/shelter: standing props and
the ground-pine layer planted inside a
location's own footprint — both now keep the
facade/structure's lower band clear (an open
front — shelter, pavilion — keeps its whole
column clear; a tree rooted high behind a
roof still peeks over it).
(d) *Clipped sign fragments.* Paper.drawSign
now shrinks its text to fit its board (floor
55%), then ellipsizes whole — the fragment
class is closed at the one shared helper. The
sweep found the same wound in a second place:
City Hall's dome sat ON its sign, clipping
"CITY HALL" to fragments; the dome now seats
on FacadeGeom's roofline, clear of the board.
(e) *The giant edge profile.* NOT reproduced:
front and profile figures draw from the one
`fh` in KidArt, and mirror captures measure
same-band profiles identical to fronts (adults
≈330 px, the kid player ≈223 px at the front
band). What Lucky saw was a closest-depth
adult in profile at the frame's edge, enlarged
by depth against the back rows — with the (a)
slabs behind him. Depth is the game's design;
the captures now read clean with (a)–(c)
fixed. No scale code changed, because no scale
bug was found.

**The sweep** (every location's exterior AND
interior, house streets + section interiors,
both bus rides, a school morning, night
passes — all captured and read):
- *Interior furniture at 2.2× scale.* The
  layout painter multiplied InteriorLayouts'
  kid-unit ladder by 2.2 — bank counters stood
  1.6× a kid's height, bus seat-backs walled
  the aisle, every room in town wore slabs.
  The pieces now draw AT the ladder (the room
  growth lives in the world width and xFrac
  spread, never in the pieces). One addition,
  in the same idiom: a lit top edge on every
  furniture piece, so the flat solids read as
  surfaces (suite-pinned).
- *The diner's windows never drew*: their
  band's foot was anchored to the door's pixel
  height and inverted (winBot above winTop).
  Anchored to the front's own height; Main
  Street's diner has its storefront windows.
- *Tweek's cup-sign and menu board* hung off
  the facade's edges, over the neighbors'
  lots, out of frame; both now sit on Tweek's
  own front.
- *The shelter's threshold*: the entry pulse
  for open structures (shelter, pavilion) is a
  threshold line at the opening's feet, not a
  gold box around the air; the CLOSED sign is
  unit-width, not a world-width billboard.
- *The Bijou's velvet rope* sagged between
  four posts per world — a stray purple band
  over the road whenever the camera sat
  between posts. Posts now stand a rope's span
  apart (9 per world), sized off the strip.
- *The Post Office crate stack* was one
  world-fraction slab with X braces — a
  barricade across the street; it is two
  stacked crates now.

**The mirror.** `tools/mirror/` gains the
desktop host + capture harness as permanent
QA tooling (one harness, no duplicates), and
`out/south-park-town-v0160b.exe` bundles it:
self-extracting launcher (zig cc, PE32+ GUI)
+ jlink'd Temurin 17 runtime (java.base +
java.desktop) + the mirror jars + the game's
assets, extracting to
%LOCALAPPDATA%\sptown-mirror\v0160b and
launching the game window. Verified here:
payload jars boot and render from a clean
extraction; the whole sweep was shot through
them. Not verified (impossible on this Linux
box): an actual Windows launch. Known mirror
limits are listed in the status section
(Java2D rasterizer, AWT metrics, GameActivity's
widget screens unreachable in the host, silent
audio stubs).

**Discipline.** No save keys added; saves
additive-only posture unchanged; no new
TalkGate rail, agenda seat, MAX_TEXTS (still
220) or headline-ladder move. The 53
historical release-identity pins are
superseded to 121 / 0.16.0b, reason written at
the v0.16.0b pin block: the version bump
itself. Registry totals untouched (the two
retired detail tokens were swapped 1-for-1).
Suite: 109,016 → 109,034 / 0 failures.

### v0.16.0a (versionCode 120) — THE BIG UPDATE: Lucky's twenty items (Queue #16 opener)

Lucky's directive, item by item, and what shipped
for each:

1. **Bus Stop layers.** The scene stack was audited
   against Depth's exterior stack end to end; the
   two real offenders were the #84 route bus drawn
   in the MIDGROUND living layer at walk-band
   height (see item 2) and the crowd composing over
   the stop (see item 11). Buses now exist only in
   the FOREGROUND traffic layer; the station sign
   stands in the background cluster where
   PaperDressing puts it, clear of the bus span.
2. **The bus stays on the road.** The #84 sweep
   (drawn at h×0.80, through the grass and the
   crowd's band) is REMOVED whole. The route pass
   now runs in drawTraffic at h×0.958 — WalkBand's
   road band by its own geometry — with the
   standing and pulling-up buses on the same line.
3. **Every building gets a layout.** InteriorLayouts
   gives all 27 interiors a room plan: houses five
   rooms (entry, living, kitchen, bedroom, bath),
   the school five (hallway, classroom, cafeteria,
   library, office), shops/banks/offices/clinic
   three each, church and theater three, the bus
   three sections; only the ice hut and the park
   pavilion keep two-room plans, because one room
   plus a gear/grill end is what those places are.
4. **Interiors bigger.** SceneScale's interior
   growth rises 2.0× → 2.4× (the v0.15.0g pin is
   superseded at the pin, reason: this item), so
   each room keeps a screen of its own.
5. **Interiors match their buildings.** Room kinds
   and furniture are themed per building (above);
   the suite pins the school's classroom/cafeteria/
   library and every house's living/kitchen/bedroom
   by name.
6. **Furniture proportioned.** All furniture sizes
   come off one kid-unit ladder in InteriorLayouts
   (chair 0.46×0.62, desk 1.15×0.66, bed 1.55×0.72,
   counter 1.85×0.78, pew 2.3×0.72…), with seat
   height pinned at figure hip height (0.30 u).
7. **Furniture usable.** Every piece carries its
   action (sit / sleep / use / serve / work / read /
   watch / ride), is painted by GameView as a tap
   target, and is answered by GameActivity with the
   player's sit/use animations. Figures sit at the
   same furniture by the layout's seatNear.
8. **School-bus interior.** Boarding is a state
   (lifeText["busRide"]): you climb aboard into the
   bus layout — cab, Crabtree at the wheel, seat
   rows — sit down (animated), and tap the route
   board (or the door) to ride; the run itself is
   the same SchoolBus.ride as ever, with a fallback
   landing if the window closes while you're aboard.
9. **Public-bus interior.** The same shell in
   public livery: station picker and the Go-menu
   ride choice now board you; the route board
   names your destination; alighting runs Walk.ride
   unchanged (minutes, prices, ledgers identical).
10. **Adult legs.** FigureGeometry.seatedLegs gives
    every figure a two-segment seated leg (thigh +
    shin, ankle on the plan's own ankle line, length
    ≥ 90% of the standing leg for adults, pinned per
    character for all 98 sheets). KidArt draws them
    front and profile; the old seat-band + floating
    shoe-ovals sit read is retired whole.
11. **No crowd clumps.** Wander caps each figure's
    range at 0.30 of its crowd-slot pitch — two
    neighbours at full reach can never touch — and
    re-keys phases on the game clock's quarter
    hours, so the crowd reshuffles with the town's
    day. The Bus Stop huddle sort still orders the
    morning line; nobody stands on one spot.
12. **Continuous real-time motion.** Wander poses
    are continuous in real time (the suite proves
    movement with no teleport steps for all 98 cast
    over six seconds of poses), walking figures
    wear their profiles, talkers gesture, idlers
    breathe and sway on their own idle parameters.
    PARTIAL: figures still walk in place along
    their slot range rather than pathfinding across
    the scene between schedule blocks — the agenda
    teleport between blocks is untouched.
13. **Game-clock schedules.** GameClockRule pins
    it as data: schedules, crowds and wander keys
    read GameState.timeMin/absDay only; the
    realtime_sync default stays OFF in Prefs (the
    Settings toggle itself is unchanged — Lucky's
    standing rule is that sync stays off, and the
    game opens with it off).
14. **Side profiles for everyone.** KidArt's
    profile painter draws every figure from
    FigurePieces.PROFILE_STACK (back → legs → torso
    → arms → head → eye → mouth → face extras →
    hair → hat) with FigureGeometry.profileAnchors;
    coverage is pinned per sheet for all 98 cast,
    for generated-NPC and avatar specs, and for all
    six ambient species (the critters were always
    profile drawings — now they animate and emote
    as profiles too). PARTIAL: generated townsfolk
    and family figures profile through the same
    painter when they walk in a scene; the photo /
    yearbook stills still pose front.
15. **Universal expressions.** Expressions carries
    three authored sets — CHILD (12), ADULT (12),
    ANIMAL (12) — and they are three different
    vocabularies. Every expression defines front
    geometry (lid, pupils, mouth curve/open, brow)
    and profile geometry (profile pupil, muzzle
    curve/open, ear tilt). The front view's
    drawEyes resolves pupils/lids through the
    registry; the profile painter and the critter
    painter read the profile fields.
16. **Action animations, per character.**
    ActionAnims registers 21 actions (sit_down,
    seated, stand_up, use_object, pickup_trash,
    phone, board_bus, ride_bus, conversation, eat,
    sleep, work, play, photo, wave, dance, hurt,
    cheer, walk, idle, talk) and every interactive
    trigger resolves to one; MotionRegistry gives
    every cast member a DISTINCT motion signature —
    gait, stride, bob, arm swing, gesture kind,
    resting expression, phase — from their sheet +
    band with 31 hand-authored overrides (Cartman's
    waddle, Tweek's jitter pace, Craig's stillness,
    Timmy's chair rhythm…). Wired live: trash
    pickup bends and reaches, the phone comes up,
    talkers play conversation, boarders play
    board_bus.
17. **Player action animations.** The player's
    blanket set is the full registry subset
    (PLAYER_SET, 21 actions); sitting runs through
    GameView.beginSit/beginStand — an eased
    sitPhase descent/ascent over the animation's
    ticks, never a snap — and benchSit, furniture
    sits and bus seats all use it.
18. **Player profile.** The avatar walks in its
    own true side profile (PaperWalk facing →
    FigureAnim.facing → the profile painter, from
    the player's created sheet, with the player's
    own motion parameters and expressions).
    PARTIAL: the sleep and hero costume looks still
    mirror-flip the front view while walking, as
    before — only the everyday look got the true
    profile this build.
19. **The layer/scale sweep.** Audited: Depth's
    exterior + interior stacks (order re-pinned),
    SceneScale growth for every location (exteriors
    in the 2–3× band; interiors at the new 2.4×),
    the foreground/road line equality, every
    location's dressing census, furniture ladder
    bounds, profile layer order per sheet, and the
    bus lane geometry. Found and FIXED: the #84
    off-road midground bus (item 2); furniture
    existing only as wall props with no usable
    presence (items 6+7); interiors at 2.0× too
    narrow for their room plans (item 4); seated
    figures losing their legs (item 10); neighbor
    houses on house streets being unenterable
    backdrop (item 20); critters standing frozen
    (item 12). Also fixed in passing: baseline
    filler houses on Rich Row now answer with the
    palette the painter actually draws.
20. **House streets.** ResidentialStreets gives
    every house standing on a house scene an
    identity: the facade, the named baseline
    neighbours (doors lead to their own interiors),
    the baseline's unnamed houses (authored names —
    the Harrington and Whitfield houses on Rich
    Row), and the continuation row houses (authored
    family names, distinct body/roof pairings —
    pinned distinct per street). Section houses
    lead to their own interior section
    (lifeText["insideSection"]): a full house layout
    keyed to the section, its own arrangement
    variant; the door out returns you to the street.

New persisted state (additive only, both inside
the existing lifeText map): `busRide` while
aboard a bus, `insideSection` while inside a
street-house section. No new SharedPreferences
keys, no new TalkGate rail, no agenda-seat
change.

Suite: 109,016 checks / 0 failures (baseline
104,373; +4,643 new checks, the bulk of them the
per-character / per-species / per-location
coverage pins for items 3–7, 10, 14–16 and 20).

### v0.15.0k (versionCode 119) — Polish & performance pass #15: the Queue #15 audit (Queue #15 item 8, the closer)

- **The pass**: audit-first, in the polish #13/#14
  pattern. No new features, no new module (the tree
  stands at 123 Kotlin files), no new save keys;
  production code is untouched unless a line below
  says otherwise. The suite grows one audit section
  that re-proves the whole queue on one save and
  re-pins every contention point from source.
- **One lifetime soak lives every Queue #15
  surface on one save**, day by day through the
  real `Engine.onNewDay`: the pen-pal
  correspondence answered as it came across both
  seasons — her visit landed, and the return trip
  to Pinewood was booked at the second settlement,
  its four beats kept as they offered, and the
  thank-you answered by home mail; gallery shows
  were entered by the game's own hands on their
  seasons' entry mornings and wrapped inside the
  life, each naming its winner; on every shared
  birthday in the registry (July 1, May 26,
  September 11, June 20) BOTH phone beats landed
  in the lived mornings; on May 26 the joint
  party yielded to the spring finale week for
  Kyle and Gerald together, the reason stamped on
  the day's plan; your own birthday was shared
  with Stephen on October 3 with the sharing
  stamped; the inbox never once broke MAX_TEXTS
  across the whole life; the preview readers
  (LiveLook and the Journal pages) probed pure on
  the lived save; and afterwards, on the SAME
  life, all five apprenticeship chains were
  driven to mastery through the modules' own
  seams — shifts counted by the very call Jobs'
  clock-out makes, stages judged by the chains'
  own verbs.
- **The THIRTEEN-case lifetime, re-proven** in
  this pass's own soak: all thirteen cases retire
  in order in the one life, the thirteenth
  queueing behind the twelfth and arming only
  once a show had really hung and wrapped in that
  life; the attic's CASE_TOTAL stands at 13 and
  the shelf reads thirteen boxes.
- **Contention, re-pinned from source**: the
  agenda stack re-derived from `Engine`'s own
  consultation order — Dinners → cast Birthdays →
  Family dinner → Jobs → Apprenticeships → Movie‐
  Nights → Capstones → Finales → Fair → Carnival
  → RivalCrew turf → Heroes patrol → Play →
  Dance → Talent → Parade → Field trips → Film
  festival → Gallery → Sleepovers → [your
  birthday consulted at the sleepover seat] →
  Stand → Clubhouse → Crew last: TWENTY-THREE
  claims, Queue #15 having added exactly two
  (the gallery's claim and the mentor's hours).
  TalkGate stands at ELEVEN rails, unchanged.
  The phone stands at MAX_TEXTS 220 /
  MAX_PER_THREAD 20 with the sender admission set
  closed (player ∪ cast ∪ parents, three group
  rooms). The headline ladder stands at TEN
  tiers in its stated order, read off
  `composeEdition`'s own chain. The house decor
  table stands at SEVENTEEN pieces.
- **The tuning ledger is completed for every
  Queue #15 dial**, each swept in-suite against
  its shipped constant: the apprenticeships
  (thresholds 6/6/4/4/4, stipends 2/3/3/2/5,
  day-gates 0/2/3/4/7 for a sixteen-day floor,
  the master's +1, the claim at 9:00–17:00); the
  gallery (twelve themes, three a season; a
  seven-day run; a wall of ten — two of yours,
  eight of theirs; a jury of three; the winner's
  10 🪙; the lent print's +3); the shared
  birthdays' phone shed order (birthday beats
  harvested ahead of the group posts and the
  paper summary, the two beats that defer
  whole); and the overhaul dials (SceneScale
  ×2.0 / ×2.5 / ×3.0 over the retired 1.7; the
  depth curve 0.75× → 1.15×; traffic at a
  1140-tick period, ≈2.1 passes a scene-minute,
  under a third of the old rate; the crosswalk
  table at three spans a street, five downtown;
  CastHomes holding all 98 cast, nobody's home
  the Bus Stop).
- **Gaps, consolidated and dispositioned** (none
  silently dropped): (a) the paper's two
  unreachable-order pairs — CLOSED AS DESIGNED:
  every fair week, finale wrap week and play
  opening week of 2026–2100 was derived from the
  modules' own calendars into the one weekIndex
  space the paper reads (the polish #10 rule:
  both real predicates, one lived space), and
  neither pair ever shares a week; the rungs
  stand on the source order pinned above.
  (b) Group-post morning granularity — CLOSED AS
  DESIGNED: the landing write (`GroupChats.
  commit`) has exactly one caller in the tree,
  Phone's morning harvest, and the derivation
  (`duePosts`) is a pure read; a group post is a
  morning artifact exactly like a solo note, by
  construction. (c) The stand permit UX bend —
  CLOSED AS DESIGNED: the permit is City Hall's
  in fiction (the Mayor signs it), in price
  (10 🪙, named in the refusal line), and in
  record (the setup transaction deducts permit +
  kit, exactly 25, and stamps exactly that); the
  pitch dialog is the Hall's collection point,
  and no narration claims the player walked it
  over. (d) Arc #11's COMMITTED texture —
  CLOSED AS DESIGNED, re-proven on the case's
  own derivation: a reading on a non-locked
  letter grades PROVEN or REFUTED the moment it
  stands; a reading on a context-locked letter
  stands COMMITTED while its settler sits in
  hand unreadable, and the SAME standing reading
  grades PROVEN when the context chapter
  arrives — no new call made; the derivation
  supports all four verdicts uniformly and the
  finale still requires the settlers live.
- **Veteran XIV**: the saturated save carried
  forward — thirteen cases closed with their
  files full, two gallery shows wrapped (staged
  by the game's own hands), all five
  apprenticeships mastered, the shared-birthday
  stamps laid in, the pen-pal correspondence at
  its whole two-season count with the trip
  thanked — serializes under the 655,360-char
  ceiling with headroom stated in the suite's
  own line, round-trip verified whole.
- **Performance**: the lifetime soak's wall
  clock, the 23-claim agenda evaluation and the
  LiveLook derivation are timed in-suite on the
  saturated veteran and printed; all measure
  well within their tripwires, so this pass
  changes no production code for speed.
- **Verified:** the full JVM smoke suite passes
  at the count read off the final run's on-disk
  log (baseline 104,330/0 read off the on-disk
  log on the untouched v0.15.0j tree before any
  file was written), and the clean APK builds,
  badges 119 / 0.15.0k and signs with the
  standing key, the lorebook byte-identical
  inside. QUEUE #15 COMPLETE with this build.

### v0.15.0j (versionCode 118) — Mystery #13: the exhibition case (Queue #15 item 7)

The thirteenth case, the show's own shadow. A
print hanging in the wrapped gallery show is not
the print its frame's record says it is, and the
case that opens off it — "Not What the Frame Says"
— works the paper trail the show itself left
behind: the provenance chain. Every frame on the
audited wall is checked against three records:
the album record the photograph was taken from,
the darkroom copy where one was made, and the
frame log the show wrote when it hung — the same
records the game already keeps, read three ways:
THE PRINT (what the piece in the frame IS), THE
CHAIN (where it CAME FROM), THE FRAME (what its
FRAME claims). Six frames in hanging order,
twelve evidence (five witness, five look, two
participation), exactly two context-locked
settlers on the load-bearing frames, three
readings a frame — TRUE HANGING, HONEST CROSS,
SWAPPED PRINT — verdicts derived on every read,
a finale at the Community Hall in the pattern of
the Reading, and an attic that now holds thirteen
boxes. The case arms only off a real wrapped
show; a save that never exhibited waits for the
next one. 🔴 Structure only, here and everywhere:
the case's truth stays sealed inside its own
content.

### v0.15.0i (versionCode 117) — Shared birthdays, whole

Queue #15 item 6. The birthday registry (v0.12.0c)
holds 98 dates over 94 distinct days: exactly four
days are shared — July 1 (Cartman and Sheila
Broflovski), May 26 (Kyle and Gerald Broflovski),
September 11 (Butters Stotch and DogPoo), and June 20
(Tolkien Black and Michael) — audited against the
shipped table in this build's suite section and
pinned by name and date, so a future cast wave
re-opens the audit instead of silently inheriting it.

What the shared day did until now, characterized
honestly: the v0.12.0c header claimed every system
handled a shared day, and two of them did — the
paper's weekly TOWN NEWS list named both, and the
gift bonus was always per-person. Everything keyed
on `TownLife.birthdayOf` — the FIRST name in roster
order — took that name alone. The phone's one
fact-only BIRTHDAY text went to the first name (the
lifetime soak even pinned "never two in a day" as a
virtue). The morning banner and the town log named
the first; the location flavor named the first; the
free-time birthday buzz orbited the first; the
conversation's birthday greeting answered only when
you greeted the first. And the party — one slot, one
evening — went to the first dinner household in
roster order: on July 1 the Cartmans partied and
Sheila's birthday existed in the paper and nowhere
else; on May 26 Kyle was guest of honor at a party
his own father, whose birthday it also was, attended
as a mere member; DogPoo and Michael, holding no
dinner table, got the paper and silence. No stamp,
no sentence, no observance recorded the second name.
That silent yield is this build's retired asset,
removed in the source, not just in the behavior.

THE PHONE: the harvest now iterates
`Birthdays.birthdaysToday` — every birthday person
gets their own BIRTHDAY text on their own thread,
seeded per person as ever. The arbitration under
pressure is stated, not hidden: each birthday person
is a distinct sender, so the harvest's per-sender
rule can never eat the second name. The shedding
order is stated whole: story, mystery, family
evenings, the composed event beats and yesterday's
noted facts stand ahead of a birthday (on a morning
those fill, a birthday can still be the shed — in
the open, under the same rule a single birthday has
always lived under); the birthday beats now stand
ahead of the group chats and the paper's summary —
the two beats that defer whole by their own design,
since a birthday is bound to its day and they are
not (this build moves those two blocks below the
birthdays in the harvest; they lose nothing, they
land the next morning whole) — and ahead of the
holiday, weather and hello chatter, which a crowded
morning sheds first of all; and the stored-inbox
cap (MAX_TEXTS 220) sheds oldest READ texts to make
room — a birthday is never the thing dropped for a
second name's sake. Proven two ways in-suite: all
four shared days through the real `onNewDay`
harvest, and a full-census 206-text inbox marched
across the July 1 week with the caps audited every
morning and BOTH July 1 beats landing.
The three historical worst-week stagings and polish
#12's lifetime soak carried the old "never two
birthday texts in a day" clause; each is re-pinned
in place to the whole rule (never more birthday
texts than the day holds birthdays) with the reason
written at the pin — their staged weeks hold no
shared day, so their measured bounds are unchanged.

THE PAPER: beside the week's birthday list (which
already named both), a shared day in the week now
prints as shared — "🎂 One day, 2 birthdays: Cartman
and Sheila share July 1 — the town does double cake
duty." — inside TOWN NEWS in the edition's fixed
order. Section copy; the headline ladder stands at
ten tiers.

THE GATHERING: one evening still holds one party,
but the arbitration is explicit, derived whole, and
STAMPED at rollover (`bdayPlanDay`/`bdayPlan`, in
the derived-on-the-morning discipline of Dance's
yield stamp) and printed on the Journal's 🎂 page.
The party's guests of honor are now EVERY birthday
member of the chosen household: when two members
of one table share the day, the party is JOINT —
the host's welcome names them both ("Kyle and
Gerald"), a member's toast names both, talking to
either honoree directly makes the wish beat theirs
in their own name, the cake names both, and the
wrap pays EACH honoree the honor rate (the night
line and the record name both). On the shipped
calendar, the one such pair — Kyle and Gerald,
May 26 — always lands inside the spring season-
finale week (May 25–31, Finales' standing window,
the week every evening rail yields to, single
birthdays included), so their party yields: for
BOTH names at once, with the reason, each man's
standing printed on the 🎂 page and the arbitration
stamped like any other day's. The suite pins the
joint derivation and the joint wrap payment as well
as May 26's whole yield — the joint party is the
rule for a same-table pair on any freer date.
Every OTHER
dinner household holding a birthday member whose
evening is free holds a family-table OBSERVANCE the
same night: on July 1 the Broflovskis gather at
their own table for Sheila (the same gathering
claim — one hook, no new agenda seat; the stack
stands at twenty-three claims), a Look at their
house says what they're doing, the morning report
names both birthdays and the arbitration, and at
rollover the observance wraps to a night line and a
count (`bdayObservances`) — with NO friendship paid,
because the family marked it themselves and the
town pays only at tables the player sits at
(Dinners' discipline, kept whole). A household
whose evening is spoken for yields with its reason,
named per person. And a birthday with no dinner
table at all — DogPoo, Michael — receives its
standing in words on the Journal page ("no
dinner-table house — the day counts in the paper,
on the phone, and in a gift given today") and a
clause in the day's arbitration sentence, instead
of silence. The Journal's 🎂 page gives every
birthday person on a shared day their own standing
line. The party still rides the DINNER TalkGate
rail as a PartyHit — no new rail; the gate stands
at eleven.

THE PLAYER: the player's birthday is day-counted
(365-day years off the arrival record), the cast's
are calendar dates, so the two calendars drift
through each other — and on a fresh save they meet:
the player's second through fifth birthdays land on
October 3, Stephen Stotch's birthday. When the day
is shared with the player, both observances stand:
the crew's party at your house and the cast party
at its table both keep their claims (anyone wanted
at both tables goes where the night takes them),
your morning note names the cast birthday, the
cast's Journal page names yours, your Journal page
prints the sharing, and the arbitration is stamped
(`bdayShareDay`/`bdayShare`) in the same words. If
either side's party yields that night, the stamp
says so, with the reason — the day counts in full
for both regardless.

State: stamps only, all life/lifeText, additive —
`bdayPlanDay`, `bdayPlan`, per-household
`bdayObsDay:`, `bdayObs:`, `bdayObsWrapDay:`,
`bdayObsNight:`, the `bdayObservances` count,
`bdayShareDay`, `bdayShare`; and the party record
`bdayPerson:<household>` now holds every honoree's
name where it held the first. No new module: the
work rides inside Birthdays, Phone, Engine,
Newspaper and PlayerBirthday (the tree stays at
122 Kotlin files).

### v0.15.0h (versionCode 116) — Apprenticeships: the town jobs' second tier

Queue #15 item 5. The three town jobs were a
standing commitment; now they're a ladder. Every
shift you clock out at a job site lands on that
site's apprenticeship count (Jobs calls
`Apprenticeships.onShiftCompleted` at the clock-out,
the Capstones seam — a partial shift settled at
rollover never clocked out, so it never counts).
At the site's threshold — six routes at City Hall,
six mornings at Tegridy, four shifts at a shop
counter — the site's mentor makes an offer in
person: the NAMED cast member Jobs' own data seats
there (Mayor McDaniels; Randy Marsh; and for the
shared shop pool, the counter's own keeper — Tweek,
Jimbo, or Chef; the pool shares one job but keeps
three counters, and each keeper signs their own
card).

The chain is five stages on a shared skeleton,
authored per site in the mentor's voice: the first
morning (the real work, taught once, properly); a
routed errand (a bound Gazette archive to Principal
Victoria, Sharon's preserves to Chef, a bean trade
to Jimbo, the good knives to Chef's kitchen, a
restorative plate to Tweek — picked up from the
mentor, delivered by Talk wherever the recipient
stands, the walk between priced by the Walk graph
as ever); the deep work (a careful Look at the
site); the judgment (a problem the mentor poses — a
missing Tuesday paper, a trampled row, a short
till, swapped labels, an off tray — settled by one
of three enacted roads: the fact-finding way,
quality 2; the decent way, 1; the hasty way, 0 —
the choice prices the stipend, 2+quality, and can
never fail the chain); and the mark, signed in
person. Day-gates between stages (0/2/3/4/7) floor
the whole chain at sixteen days.

A named SKILL deepens with every stage (the
Journal's rungs: Green hand → Willing → Steady →
Journeyman → Master) and lands as something real:
the master's rate, +1 🪙 on every shift clocked
out at the mastered site, paid inside Jobs' own
pay line forever. The whole chain pays 15–17 🪙 in
stipends — proven in-suite, against Jobs' own
constants, at less than half of what the same
counter's ordinary work pays over the chain's own
span; there is no loop. The completion mark is
remarked on the way this town remarks on things:
the mentor's standing line in conversation, the
neighbors' gossip once each for three weeks, a
framed-card trace a careful Look reads at the
site, the work topic planted in the mentor's
memory — and one TOWN NEWS line in the Gazette
the week it happens (section copy; the headline
ladder stands at ten).

One at a time: a second site's offer waits while a
chain runs, and the Journal says so. Declining
costs nothing; the offer stands down for the day
and returns tomorrow. Quitting is a conversation
with the mentor: the chain ends, the stages reset,
the stipends already paid stay paid, and the count
that earned the offer still stands. One completion
per site per save.

Presence: ONE new agenda claim, the mentor's
hours — while an offer stands or a stage waits on
the mentor or their site, the mentor keeps their
counter 9-to-5, seated directly after Jobs in
Engine's stack (twenty-three claims; every set
piece below it still wins the person, and the
claim stands down whole while the town owns the
moment). TalkGate keeps its eleven rails and never
learns the module's name: offers, stages and the
standing talk ride below the gate in
GameActivity's dialogue branch. NO phone trigger:
the sender set stays closed and MAX_TEXTS 220
unmoved — a graduation is public the way work is
public, in person and in the paper. Saves are the
existing `life`/`lifeText` maps only (`appr*`
keys, disclosed in the tuning ledger); a mid-chain
state round-trips whole. Pins superseded, each
with its reason at the pin: the manifest (116 /
0.15.0h) and the tree count (121 → 122) across
their standing sites, and the contention block's
stack enumeration (twenty-two → twenty-three
claims; the mentor's hours gain their seat).

### v0.15.0g (versionCode 115) — The scale & space build: Lucky's playtest round 2

Lucky's second correction round on the overhaul
(his screenshots: the bus-stop crowd as one blob
of heads behind the bus, the road a sliver, the
whole town spawning at the stop, button labels
wrapping mid-word), confirmed complete by him
Oct 18 — "That's everything — build it next."
Playtest corrections outrank the roadmap: this
build ships directly, ahead of Queue #15 item 5.

Areas 2–3× bigger: `SceneScale` widens every
world — interiors 2×, streets 2.5×, open land 3×
the old 1.7-screen world — while figures keep
their v0.15.0d sizes; fraction-tuned composition
anchors to the unit width so buildings hold their
composed size at each scene's heart, and the
neighbor row + standing props tile to fill the
wider street at the same density. Depth scaling:
one curve, 0.75× up by the houses to 1.15× down
by the street, composing player, crowd and props
alike; the ladder holds at every depth. The bus
stop pile is dissolved three ways: the crowd is
the scheduled riders set (`SchoolBus.ridersAt`),
figures compose through `FigureCompose` at true
body pitch in a 3.4-screen window over up to 7
depth rows, and the standing bus's span is
forbidden ground. Default locations: `CastHomes`
gives all 98 cast a home — 22 residence rows
(family houses), 75 occupation rows, 1
hand-placed row (Crabtree → Elementary) — as
spawn, night-march destination and idle anchor;
nobody defaults to the Bus Stop. The road is a
real band now (11.5% of scene height, both curbs,
dashes, zebras) and vehicles drive on it; ambient
traffic falls to 0.30× the old rate (≈7.1 → ≈2.1
passes per scene-minute) via the pure `Traffic`
derivation. Walking: the sidewalk and yards are
walkable, the road is not — except at marked
crosswalks (one table for the paint and the
permission). The bottom bar's labels render on
one line (single-line + auto-size 8–14sp, fit
proven by `BarFit`). Removed, not dormant:
`GameView.WORLD_SCALE`, the old traffic loop, the
props' private depth breath, the crowd pass's
rowScale step, the all-at-the-stop spawn.
Re-pinned with reasons where the build genuinely
supersedes v0.15.0d: `WalkBand.BOTTOM_OUT`,
`Depth.FG_TOP_OUT` / `FRONT_FEET_OUT`, the depth
curve, the structure's center. Three test
stagings were also made explicit (they used to
lean on the whole town spawning at the stop —
the cold-open gathering, the classroom's roster,
and the stand pitches' resident-free street now
place their people on purpose, assertions
unchanged), the one-scale removal pin follows
the scale into `FigureCompose`, and the lifetime
lifeText bound re-measured at 235 (bound 245) on
the re-anchored town. Zero new save keys.

### v0.15.0f (versionCode 114) — The gallery show: the Community Hall's seasonal exhibition

Queue #15 item 4, in the festival/yearbook
pattern. A derived theme per season (12 written
themes, 3 per season, rotated by the show's
index; every fit a stated formula over the
record's own fields). Entries from the player's
real album only — this season's photographs, up
to two — plus held darkroom prints as a
distinguished, season-free, lent-never-consumed
class (the darkroom's second life; a lent print
can't be gifted or home-hung while the show is
up). Cast entries derive from cast members' own
album history (commissions and sittings, never
photobombs, never fabricated), cap 8, jurors
excluded from their own show. A jury of three
leaning jurors (two trios rotating by season and
year) scores derived criteria — 2× theme fit +
craft + leanings + 3 for a print entry — with
placements down a stated ladder and fact-slotted
jury notes. Hanging night (second Friday of the
season's middle month, 6–9 PM) is a three-beat
set piece at the Community Hall — the town's
hall at City Hall, the venue the game already
had — with a quiet dawn hanging when the evening
is the town's. One new agenda claim seated after
the festival, before the sleepovers (stack: 22
claims; two pins re-pinned with reasons). The
paper headlines nothing: TOWN NEWS lines only,
the ladder stands at ten. One GALLERY phone
text on the wrap morning (winner or chair
sending; empty shows send nothing); MAX_TEXTS
220 unmoved. Prizes: the champion frame until
the next hanging; 10 🪙 when you win; a cast
winner's +1 only if you attended or visited,
paid once at the wrap. One new save key,
`"gallery"`. The exhibition wall renders in the
City Hall interior with PhotoView's own renderer
off the entries' frozen record copies.

### v0.15.0e (versionCode 113) — The logo build: the approved sign, on the launcher and the title

Lucky commissioned a new app logo (Oct 17) and
approved the artwork as drawn: a paper cut-out
town sign — SOUTH PARK on the board, TOWN on the
hanging plank — over layered mountains, pines and
snow. The square render (512×512) ships as the
launcher icon (`@drawable/ic_launcher`, its bytes
hash-pinned to the approved file); the wide
render (1200×760) ships as `assets/title_logo.png`
and is the splash's title, decoded from the
bundled asset inside the same parchment plaque,
routing untouched. The old code-drawn icon and
its generator's drawing code are removed whole —
`tools/gen_icon.py` now installs the approved
master (`tools/logo/south-park-town-icon.png`)
verbatim. One pin superseded, named in the
status section (the v0.2.0c photos image pin now
admits the title art). Zero new save keys; no
gameplay changes.

### v0.15.0d (versionCode 112) — The overhaul correction: one scale, a walkable band, a bus that runs, the baseline town

Lucky's playtest corrections to v0.15.0c, taken
whole: world scale unified on the figure
(`WorldScale` — kid 0.19 of scene height, adults
1.42× by their own measured heights, the ladder
house > tree > car > adult > kid > trash pinned
from composed geometry); adults at adult scale
(the crowd clamp that shrank them is removed);
a walkable ground band with prop footprints
(`WalkBand`); doors on facades from one geometry
(`FacadeGeom`) and structures — shelter, pavilion,
hut, barn, loft, diner — where a slab used to
float; the school bus boardable end to end
(`PaperTap` resolves vehicles first) and every
public station pair pinned on the real ride
verbs; "Inside the School Bus" rebuilt as a
layered interior (`BusInteriorSpec`); structured
ground (yards, paths, sidewalk slabs, snow as
cover) with the speck scatter removed; and the
town itself merged from Lucky's two maps into
one baseline (`TownBaseline`) — streets, looks,
true neighbor rows, corner street signs. Zero
new save keys.

### v0.15.0c (versionCode 111) — The visual overhaul: paper town, the kid on screen, the bus

Lucky's direct playtest directive (Oct 17)
preempts the roadmap — the gallery show is
re-queued as the item after this one. The
visuals are removed and redesigned from
scratch; correctness first, layering above
flourish.

- **PAPER SCENES (`PaperScene.kt`, SceneArt,
  Depth).** Every location renders as a stack
  of flat construction-paper planes: sky → far
  range (parallax 0.55) → a street of buildings
  (the facade + `PaperDressing`'s neighbor row,
  parallax 0.85; downtown tall with storefronts,
  awnings, sign boards) → ground + the street
  strip along the very bottom → props → the
  y-sorted figures → traffic on the street →
  the near foreground. `Depth`'s chain gains
  `neighbors`, `street`, `traffic`. Interiors
  stack wall/floor/furniture around the same
  figure pass. Snow, night, lamps and weather
  theme-match onto the new painters.
- **FIGURES FROM THE SPECS (`FigurePieces`).**
  The 98 cast + your avatar assemble from the
  measured sheet data in the sheets' own piece
  order — torso plane, head base, eyes, mouth,
  face extras, hair framing (never covering)
  the face, hat above — suite-pinned for every
  spec, with hair/hat non-coverage proven from
  the head-stack geometry.
- **MOVEMENT REDONE (`PaperWalk`, `PaperTap`,
  GameView).** You stand in the scene in your
  `LiveLook` look. Taps become walk orders: the
  kid walks (bob, step phase, facing, camera
  follow) and the interaction fires on arrival
  — talk, doors, props, pickups, signposts,
  stations. Ground taps just walk. Zero
  game-minutes inside a scene; Walk's
  inter-location prices unchanged.
- **THE BUS (`BusNetwork.kt`).** The school
  bus runs Bus Stop ⇄ Elementary on its
  morning/evening windows, no pass needed. The
  public network: 10 stations, walk-to-it
  boarding, a destination picker, a visible
  pull-up and arrival beat. Rides cost the
  route at bus speed — `ceil(raw × 2/5)`,
  floor 10 — derived from the Walk graph, so
  `Walk.rideCostMinutes` changes with it (the
  flat first-leg price is retired); the parade
  exemption stands.
- **CONTRACTS.** Saves: zero new keys.
  Re-pinned with reasons at the pins: the bus
  price (v0.5.0a), the crowd tap rule's home
  (v0.5.0g), the street costume/pajamas
  framings (v0.14.0a/v0.15.0a — LiveLook's
  second GameView reader is the player figure
  itself), manifest + file counts. Fan sheets
  untouched — the specs never moved.

### v0.15.0b (versionCode 110) — The pen-pal visit, returned (Queue #15 item 2)

Queue #15's second item gives the
correspondence its second season. In v0.13.0e
Wren Halloway came to you: the top band at the
May 31 settlement earned her June visit. The
program, it turns out, alternates — nobody
told the first-years, because first-years have
no seniority. The return trip is saved for a
pair that writes its way in twice. No new
module; `PenPals.kt` grows the season, and the
tree stands at 114 Kotlin files. Saves are
additive-only: `palTripYear`, `palTripStart`,
`palTripDone`, `palTripThanksYear`,
`palTripNoteDay` join the `pal*` book.

- **THE DIRECTION RULE.** At a June 1
  settlement in the top band, the module asks
  one question: has she visited, and does no
  return trip stand on the books? If so, the
  settlement stamps YOUR trip (`palTripStart`
  = June 16 of the ended year) and delivers
  the invitation as a special home letter —
  `deliverSpecial`, so the polish #14 rule
  rides whole: it sits in the home mailbox,
  answerable any day, in any season, dormant
  route or not. If not, season one's shape
  stands untouched: her visit is stamped
  exactly as before. Below the band in a
  return season, the settlement's word is a
  new letter, `MAYBE_NEXT2_LETTER` — the jar
  that says JAR II is not full, and the
  program is strict about jars.
- **THE PLAN, OVER DAYS.** All season, while
  the trip is the live year's shape
  (`tripPending`), her ordinary letters carry
  one rotating plan line — the alternation
  stated early, the Return Trip Fund opened,
  the route committee's report on the fifteen
  travel days. Fact slots are untouched: the
  plan is her news, not a fact about you, and
  provenance by construction stands.
- **THE SEAM, IN-FICTION.** The trip starts
  fifteen days after the settlement, and the
  invitation says why in so many words: two
  buses, one transfer, the flattest road in
  the county — the program's travel days are
  the seam between one school year and the
  next. The gap is named, not shortened; and
  because the invitation is home mail, the
  seam is answerable from inside it.
- **FOUR HOSTED BEATS.** June 16–19, one a
  day, day-gated and honestly skippable (a
  day that goes by unkept is recorded as
  unkept): **the Turnaround** (the journey
  narrated — "🚌 The road" — then Kip, the
  flag, the tower), **the Tour** (she
  narrates, you rate; Mount Flatitude's
  tourist record falls to you), **Counter
  Duty** (the bait-and-gas on a Saturday:
  ring once for bait, twice for gas, three
  times if you're Kip), **the Goodbye** (dusk
  at the turnaround; write because you're
  home). The beats run through her own sheet
  machinery — script first, then her host
  talk/fact pools — anchored at the mailbox,
  the correspondence's front door since
  v0.13.0e. The town map is fixed and gains
  no Pinewood locations; no TalkGate rail, no
  agenda seat, and the town does not pause —
  the same seam her own visit lived with.
  Each kept beat stamps `palTripDone` (in
  the trip's own order) and pays
  `TRIP_BEAT_PTS` 2 in `palPts` — her lane
  only; `state.friendship` never learns of
  the trip, pinned in the suite.
- **THE SYMMETRY HOME.** The morning after
  the trip ends, her thank-you arrives by
  home mail (`TRIP_THANKS_LETTER`, stamped
  once per trip year): the visit, returned,
  rated a five. You can answer it in July,
  in October, whenever — the route that
  carried hers carries yours.
- **NO PHOTO, STATED.** The album's camera
  composes from the player agent's real
  location on the town map (`Photos.compose`),
  and the darkroom/yearbook machinery reads
  that map back; a Pinewood print would be a
  fabrication, so none is forced. The trip's
  keepsakes are the letters: the invitation
  and the thank-you sit whole in the archive
  on the 📬 page, which also gains the
  season's shape while it's pending, the
  STAMPED line inside the seam, the THERE
  line during the trip, and the kept-beats
  record after it.
- **UNCHANGED, ON PURPOSE.** Wren stays not
  cast, not a parent, no fan sheet; warmth
  carries across seasons whole (re-pairing
  is the same box, the same pal, the same
  `palPts`); dormancy and revival run by the
  season-one rules in season two, suite-
  pinned; the paper never covers the trip,
  the phone never hears of it, and no case
  machinery grows on the correspondence —
  the absences are re-pinned over the new
  surfaces in the suite.

### v0.15.0a (versionCode 109) — The preview pass (Queue #15 item 1)

Queue #15 opens by finishing the reflection
work Queue #14 started: the town saw you the
way you see it in the mirror and on the street;
now the previews agree too. One new module
(`LiveLook.kt`); the tree stands at 114 Kotlin
files. Zero new save keys — every reader here
is pure, and the suite pins the serialized save
byte-identical across every read this build
adds.

- **THE ONE DERIVATION (`LiveLook`).** What the
  player figure is actually wearing right now,
  derived once and read by every preview
  surface: SLEEP while a sleepover night is
  live (`Sleepwear.eveningLive` — the plan
  standing, the evening open — with the code
  from `Sleepwear.playerCode`, the shutter's
  own player branch, so on a live night
  `LiveLook`'s code EQUALS `Sleepwear.mirrorCode`
  figure-for-figure, pinned in the suite), HERO
  while a patrol night is in its window
  (identity made, Wed/Sat, 19:30–22:00, with
  the code from the new
  `Heroes.playerCostumeCodeFor` — `playerCostumeCode`
  itself, refactored to delegate, emblem off the
  same power the caper records), DAY otherwise.
  The two live kinds can never collide in the
  lived calendar (Fridays vs Wed/Sat — swept
  two years in the suite), and the precedence
  is stated anyway for a staged state: SLEEP
  first, HERO second, DAY last. LiveLook
  derives nothing itself: its source carries
  no softening and no emblem table, pinned.
- **The previews.** The character creator's
  preview (`AvatarPreviewView`, fed by
  `CreatorActivity.syncPreview` — the one place
  its inputs are set) draws the live kind
  through KidArt's own passes: sleep through
  the sleep pass, hero through the hero pass,
  day as ever. Because `lookFor` takes the
  appearance as a parameter, the creator
  derives the costume codes from the look
  UNDER EDIT — re-pick your top on a sleepover
  night and the preview's pajamas are the new
  top's pajamas, exactly what saving will wear.
  A fresh town (no save yet) previews the plain
  day look. The Journal's 🪪 YOU card figure
  reads `LiveLook.playerLook` the same way.
  The Your House mirror glass reads it too:
  the reflection now wears your hero costume
  on a patrol night, beside the pajamas it
  learned in v0.14.0a. THE BREADTH NOTE, stated
  plainly: the preview's hero moment is the
  patrol WINDOW, not the patrol SPOT — the
  mirror hangs at your house, which is not a
  spot, and a preview that caped you only at
  the park would never show the costume; the
  street keeps its spot gate because the
  street is a place, and a preview is a look.
- **Pajamas on the street — one window only.**
  `Sleepwear.streetCode`: while a sleepover
  night is live and you are OUTDOORS, the
  street scene draws you standing in your
  sleepwear (the walk-home/escort window —
  your guests are still up at your house and
  you stepped out in what you're wearing),
  through KidArt's sleep pass from
  `playerCode` read live. `Sleepwear.figureCode`'s
  player branch learns the same window, so a
  photo composed on the street in it freezes
  exactly the code the street draws — the
  album renders the walk home honestly, and
  guests abroad still freeze nothing (their
  pajamas live at the house). The walls,
  pinned on every side: before 18:00 the code
  is empty (never a daytime outfit, by the
  evening's own arithmetic), indoors it is
  empty, with no plan standing it is empty,
  and the morning after it is gone with the
  plan. And it is NEVER a wardrobe outfit:
  the suite scans every creator option and
  every wardrobe piece — no pajama, no
  sleepwear, no nightcap is pickable anywhere;
  the nightcap exists only inside the sleep
  spec's derivation, as ever.
- **Two standing gaps CLOSED.** "The creator
  preview shows the day look" (stated in
  v0.14.0a, consolidated through polish #14)
  and "pajamas never on the street itself"
  (carried since v0.12.0d) both close here,
  with the evidence above: the preview reads
  the live derivation on all three kinds at
  their exact boundaries (18:00 open, 19:30 /
  22:00 patrol edges), and the street draws
  and freezes the pajamas inside the one
  legitimate window and nowhere else.
- **Suite hygiene carried with the bump:**
  the manifest pins move to 109 / 0.15.0a,
  the file-count pins to 114 (including two
  older sections that count the tree without
  the ktCount variable), and three wiring /
  blanket pins are re-pinned to the new truth
  with their reasons written at the pin: the
  v0.14.0a GameView wiring (the glass reads
  LiveLook once, in its fixture block; the
  street's hero search is scoped to
  drawFigures, where that pin lives), the
  sleepwear section's "anywhere else freezes
  nothing" (now: cast freezes nothing abroad;
  the player freezes exactly streetCode —
  the gap closing IS the behavior change),
  and its "one drawSleepFigure in GameView"
  (now exactly two: the mirror's and the
  street's). The reflections section's
  behavioral pins (mirrorCode,
  streetCostumeCode, the shutter agreements)
  stand untouched and pass as written. First
  full run: 102,237 checks, 4 failures — the
  four re-pins above, nothing else; fixed and
  re-run green (final count in the roadmap).

### v0.14.0g (versionCode 108) — Polish & performance pass #14: the Queue #14 audit (Queue #14 item 7, the closer)

Queue #14's audit, in the polish tradition: the
suite grows the audit section, the record is set
straight, and the queue's two standing pen-pal
debts are dispositioned — one fixed in-fiction,
one closed with the tests it was missing. The
audit's findings:

- **Disposition one — the pen-pal reply window,
  FIXED.** Polish #13 measured the seam: a letter
  arriving after the school year settles — her
  thank-you, the morning after the June visit —
  could not be answered until October's box came
  out again, and its soak closed 61 of 62 for
  exactly that reason. The special letters arrive
  at the HOME mailbox, not the school box, so the
  reply now rides home mail: `deliverSpecial`
  stamps `palHomeMail`, `canReply` honors it in
  any season, the answering send clears it, and a
  fresh October signup folds a still-waiting home
  letter into the archive (kept, unanswered, the
  new year clean). The dormancy rules ride whole —
  a home-mail reply to a dormant route IS the
  revival letter (+1, route reopened) — and the
  cadence, the warmth lane and the archive are
  untouched. This is the build's only production
  change, and it is confined to `PenPals.kt`.
- **Disposition two — the fabricated-record gap,
  CLOSED.** The pen-pal fact slots for the dance,
  talent, parade and carnival kinds were pinned
  by their banks and by those modules' own
  sections but carried no fabricated-record test
  of their own. The audit stages all four records
  (the parade and talent through their modules'
  own record paths; the dance and carnival in
  their modules' own serialized formats) and
  proves the facts offered in order, identical
  under all three tones, carried verbatim by two
  replies, spent once ridden, and followed up
  from the sent letters' own meta — provenance
  by construction, now under test for all ten
  fact kinds.
- **One 400-day soak lives every Queue #14
  surface on one save:** the year's three trips
  — and next fall's — each wrapped unattended
  as missed, the bus on time; the festival week
  wrapped in December and crowned its winner;
  the first birthday fired on its derived
  morning (one real cake in the bag, the
  household card kept); the clubhouse stood
  across all three of its season's spots and
  was attended 8 of 9 staged afternoons — one
  afternoon the town's bigger moments owned the
  hour, as designed — with 8 wrapping onto the
  record and 8 house-report notes; the
  reflections readers answered pure all year
  (the mirror probed once on a staged sleepover
  night, the costume on four patrol nights,
  every read leaving the save byte-identical,
  and 400 ordinary noons dressing nobody); the
  family page read 57 times, byte-stable; the
  pen-pal year ran whole — 62 letters in, 62
  answered, the thank-you among them by home
  mail, every reply gap 3–5 days but the single
  15-day settlement seam, warmth at the cap;
  the phone, watched daily, peaked at 30 texts
  with its sender set closed — the trip's beat
  fired in the soak itself, the festival's and
  the birthday's stayed best-effort there and
  are proven whole below; and the same life
  retired all twelve cases inside the 400 days.
- **The TWELVE-case lifetime, re-driven:** all
  twelve arcs retire in order in one unattended
  life, 380 days end to end; the attic's
  CASE_TOTAL stands at 12, and the shelf reads
  twelve boxes for a twelve-case life.
- **Contention, re-pinned at the code's own
  seats:** the TalkGate ladder stands at ELEVEN
  rails — Queue #14 added none; the birthday
  party rides the DINNER rail on its bdayHit
  payload and the wishes the owed-lines seam;
  the clubhouse's beats ride the crew-talk
  channel and the Look. The agenda stack in
  `Engine` now seats TWENTY-ONE claims in its
  own order — Dinners → cast Birthdays → Family
  dinner → Jobs → MovieNights → Capstones →
  Finales → Fair → Carnival → RivalCrew turf →
  Heroes patrol → Play rehearsals → Dance →
  Talent → Parade → Field trips → Film festival
  → Sleepovers → Stand staffing → Clubhouse →
  Crew last — with your birthday party consulted
  at the sleepover seat, between the sleepovers'
  claim and the stand's, riding it rather than
  widening the stack.
- **The phone at its worst week:** the standing
  worst week re-staged with the FIELDTRIP,
  FESTIVAL and PLAYERBDAY triggers in the
  harvest — 24 new texts across seven mornings,
  one of Queue #14's derived beats landing even
  at that pressure, the weekly summary landing
  once, the inbox ending at exactly 220, the
  cap — no thread over its own ceiling and no
  sender outside the closed set (cast ∪ parents
  ∪ the three group rooms). The clubhouse's
  absence is re-pinned as an absence: no phone
  trigger, no paper line, no rail — a daily
  afternoon is not an event. Three companion
  mornings, staged through the real harvest on
  days with room, prove the new beats land whole
  when they can: the missed trip's text from a
  classmate who rides the bus, the festival
  winner's first star on the wrap morning, and
  your closest friend's text on your birthday's
  own morning, with Mom's cake beside it.
- **The tuning ledger is completed for every
  Queue #14 dial** — the reflections pass and
  Mystery #12 join the field trips, the film
  festival, your birthday and the clubhouse in
  the ledger above, each dial named, and the
  audit sweeps the dials against their constants
  (trips: a week's poster, 30 minutes each way,
  three rounds, the sprinkle at +1 capped 8, the
  claim 9:00–13:00; the festival: qualities
  4/4/2, jury leanings at face, the night's buzz
  doubled, admission free; your birthday: the
  365-day count, wish bands at 60/30/10 off the
  relationship tiers, the wrap at +2/+1; the
  clubhouse: 15:00–19:00 with the zero-pay wrap
  proven byte-for-byte; the returns case: every
  reading at call price 2, the full read gated
  at four of six proven). The reflections set no
  dial of their own: the mirror borrows the
  sleepover's 18:00 arrival and the costume the
  patrol's own 19:30–22:00 window, both read off
  their owners' constants.
- **Veteran XIII:** the saturated save carried
  forward one case — arc #12 retired over
  veteran XII's full saturation, with Queue
  #14's records laid in (three field trips, a
  wrapped festival, two birthdays with the card
  kept, the clubhouse record at its cap of eight
  afternoons, and the pen-pal archive driven
  through its whole year, the thank-you answered
  by home mail) — serializes at 593,915 chars
  against the 640×1024 = 655,360 ceiling: 61,445
  chars of headroom, round-trip verified.
- **Gaps, consolidated and stated plainly**
  (none silently fixed): the two reflections
  gaps polish #13 carried are CLOSED by this
  queue (the mirror shows the night look; the
  costume shows on the street) — what remains of
  them is by design: the character creator's
  preview shows the day look being saved, even
  on a sleepover night, because the preview
  keeps the look under edit while the mirror in
  the room shows the night look; and pajamas
  never draw on the street itself — the street
  is first-person, and the night look lives at
  the mirror and in the album. On a shared
  birthday the phone sends one beat from the
  roster-first name; the other birthday is still
  in the paper, still giftable, still
  party-eligible. Two pairs in the paper's
  ladder (fair over finale, finale over opening
  night) can never co-occur in the lived
  calendar, so those orderings stand pinned by
  source order rather than by fabrication. Group
  posts land at morning granularity, like the
  solo phone's notes. The stand's permit is
  narrated as City Hall's but bought at the
  pitch dialog — a stated UX bend. Arc #11's
  COMMITTED texture stands as polish #13 stated
  it: with exactly two pieces tagged per letter
  at a price of two, a reading on the four
  non-locked letters is graded PROVEN or REFUTED
  on arrival in normal play — the COMMITTED
  state is exercised on the context-locked
  pair's boundary; the derivation supports all
  four verdicts uniformly, and finale
  eligibility still requires the settlers live.
  One new note from this audit: the home-mail
  fix makes the settlement seam ANSWERABLE, not
  shorter — her letters still arrive on the
  school year's cadence, and the summer gap
  between the last program letter and the
  thank-you (15 days in this audit's soak) is
  the visit itself, stated rather than smoothed
  over. And the standing one: there is no
  emulator or on-device rendering proof on this
  machine — the suite is the audit's eyes, and
  Lucky's screenshots remain the only real QA.
- **Verified:** the full JVM smoke suite
  passes at **102,202 checks, 0 failures**
  (baseline 102,081/0 read off the on-disk log
  on the untouched v0.14.0f tree before any
  file was written; the final count read off
  the final run's on-disk log), and the clean
  APK builds, badges 108 / 0.14.0g and signs
  with the standing key, the lorebook
  byte-identical inside.

### v0.14.0f (versionCode 107) — Mystery #12: the returns case (Queue #14 item 6)
- **The premise**: the Elementary's lost-and-found has
  started RETURNING things — items showing up back in
  cubbies and desks, each with a short slip — except
  the returns go to the wrong owners, and each slip is
  written in the hand of the kid it went to. Nothing
  is missing. Things are coming back.
- **The returns ledger**: a Journal board of six
  returned items, each carrying three DERIVED checks
  recomputed from the game's own records on every
  read, never stored — THE LOSS (was it lost, when,
  by whom, against the box's spike of loss cards),
  THE HAND (the slip's recorded features checked
  mechanically against its recipient's authored
  HAND card — letterforms, pressure, habits — with
  the fall penmanship unit's copybook forms kept as
  their own record), and THE FIT (the filing's claimed
  marks against the returned thing's own facts).
  Readings — TRUE RETURN / KIND MISTAKE / PLANTED —
  are priced at 2 live evidence; verdicts derived
  (OPEN / COMMITTED / PROVEN / REFUTED), standing
  readings final, refuted readings strikeable.
- **The evidence**: 12 pieces (5 witness / 5 look /
  2 participation), two per item, one settler per
  item. Exactly two are context-locked, and they are
  the two load-bearing items' settlers — one held a
  chapter, one held to the finale's own week. The
  red herring is built from honest oddities, and the
  obvious suspect-shaped answer carries an
  evidence-supported fully innocent reading in the
  evidence text.
- **The shape**: four chapters (The Returns Desk /
  The Spike / The Big Sort / The Reading) queuing
  10 days after arc #11 retires, ~7-day gaps,
  holiday deferral, never booked over the eleventh
  case's open body; a four-call tiered finale gate
  at the Elementary (full read: both load-bearing
  items PROVEN + all read + none refuted + ≥4 of 6
  proven; name call ≥1 proven; stand/walk ungated),
  re-checked at lock and wrap; four landings with
  marks, bond deltas and faction ripples; a standing
  aftermath line, ambient callbacks, and a 7-day
  delayed tail on the full-read landing only. The
  attic's CASE_TOTAL rises to 12; the twelve-case
  lifetime holds in one unattended life. Saves
  additive-only; no new phone-note kinds (NOTE_KINDS
  stays closed, as in arcs 7–11). 🔴 Structure
  only: the case's truth stays sealed in its own
  content.

### v0.14.0e (versionCode 106) — The clubhouse (Queue #14 item 5)
- **The spot, by season**: the crew claims a spot that
  rotates off `seasonFor`, the town's one season truth —
  SPRING and FALL the treehouse at Kid's Park, SUMMER
  the garage at the Marsh Residence (the garage named
  in that house's own blurb and drawn details), WINTER
  the basement at Your House. No new map location: each
  spot is a fixture at an existing location, and the
  season's spot is derived on every read, never stored.
  The wrap derives the spot from the ENDED day's season,
  so a record always names the spot the afternoon
  actually claimed.
- **The claim, priced like a rendezvous**: one new
  agenda claim (`Clubhouse.gatheringBlock`), seated
  twenty-first in Engine's stack — after the stand's,
  before the crew's own hook — live 15:00–19:00 daily.
  It self-guards on `Crew.busyReason` read whole (school,
  dinners, shifts, screenings, ceremonies, finales,
  climaxes, emergencies, sleepovers, your birthday party)
  plus a veto pass over every claim seated above it that
  the oracle doesn't know (fair, carnival, rival crew's
  court, patrols, play, dance, talent, parade, field
  trips, festival, sleepovers again by claim, your
  birthday party again by claim, the stand's shift), and
  wholesale on `Emergencies.momentOwned`. The price:
  `Clubhouse.priceFor` is `Walk.routeMinutes` from where
  a member actually stands to the spot, and a member
  whose walk lands after 19:00 is not claimed that day —
  nobody teleports; a member already at the spot (or
  already walking to it under the claim) prices at the
  spot and stays claimed.
- **The beats, with no new rail**: three beats — 0
  Settle In (Talk), 1 The Project (Look at the spot; a
  season's small authored work per spot: the treehouse
  inspection, the garage sort, the basement council),
  2 Crew Business (Talk). The Talk beats ride the
  conversation's crew-talk channel — GameActivity's
  memoryPreLines calls `Clubhouse.talkLines` beside
  `Crew.talkLines`, and a pending beat IS the line,
  judged as it plays (the witness-line discipline);
  the Look beat hides in the Look at the spot, with a
  venue line while the hangout stands. TalkGate keeps
  its eleven rails and never learns the module's name —
  the suite pins both.
- **Crew-only talk**: at the spot in the window, members
  speak clubhouse banks — spot lines, and record lines
  quoting Crew's own tallies (places together, hangouts
  held) only when a record exists — stamped once per
  member per slot like Crew's own talk. Kenny muffled,
  Timmy by proclamation. A non-member in the same place,
  a member elsewhere, or anyone outside the window gets
  silence from the banks; the suite pins all three.
- **The wrap pays nothing, on purpose**: at rollover an
  afternoon with beats and a room goes on the record
  (spot, who came — beat-unioned presence, never
  invented — and the beat count), plants the "clubhouse"
  topic in everyone who came, and counts toward the
  capped history (8 afternoons). NO friendship moves at
  the wrap: the day's +1 for a member who came to you is
  Crew's arrival rate, paid by Crew's rendezvous machine
  when it fires, and the clubhouse does not pay it twice
  — pinned byte-for-byte. An afternoon nobody reached
  leaves no trace.
- **Empty crew, polite stand-down**: no crew → no claim,
  no beats, no lines, no record, and the Journal page
  states it plainly (the spot named anyway, the wait
  costed at nothing). The hangout also stands down
  whole while the town's bigger moments own the hour,
  and its beats stand down on a planned sleepover night
  or your own birthday-party night.
- **Records, deliberately light**: the Journal 🏠 THE
  CLUBHOUSE page (the rotation in plain words, today's
  standing with the rendezvous pricing stated, the
  record) and ONE morning-after line in the house
  report. NO phone text and NO paper line — a hangout
  is a daily, low-stakes afternoon, not an event: a
  text per hangout would spend the shared MAX_TEXTS
  220 budget on routine, and the paper headlines the
  town's occasions, not its afternoons. `Phone.kt` and
  `Newspaper.kt` never learn the module's name; the
  suite pins that too. State is `life`/`lifeText` only
  (`club*` keys) — no new save keys; a mid-hangout
  save/reload is pinned through the real encode/decode
  path. All copy newly authored and denylist-scanned
  in the suite. Sol discipline held: no sprite/spec
  data changed, so the 32 fan sheets were NOT
  regenerated.

### v0.14.0d (versionCode 105) — Your birthday (Queue #14 item 4)
- **The date, derived**: your birthday is the anniversary
  of the save's arrival day — the Family module's arrival
  record, the day this household's life in town began —
  recurring every 365 game days from that record alone.
  No new stored date, no date-picker UI, and because it
  is day-counted rather than calendar-dated (the cast
  registry in `Birthdays` is MMDD), leap years can never
  move, skip or double it; the suite pins the derivation
  (the date reads the arrival record and nothing else)
  and the spacing (exactly one day in every 365, so two
  birthdays never stack). The FIRST birthday a save can
  see is one full year in — stated honestly on the
  Journal page; nothing arrives retroactively.
- **The morning**: at rollover into the birthday, Mom's
  bake — exactly ONE real Snacky Cake, `Birthdays.CAKE_ITEM`
  read by reference — lands in your inventory, stamped
  per year (`pbGiftYear`) so a save/reload on the
  birthday morning yields exactly one, edible and
  giftable like any real item. A card from the household
  arrives in the mail the same morning and is kept
  verbatim on the Journal page. The first talk with each
  parent at home that day opens on their birthday line —
  authored, in their voices — and pays +2 in the family's
  own famPts lane, once per parent per birthday (never
  `state.friendship`: the family wall holds).
- **The wishes**: across the day, cast members wish you
  well in person via the conversation's owed-lines seam
  (TalkGate keeps its eleven rails). The wish is derived
  by the wisher's friendship band off the town's tier
  table — close friends get the warm, specific-feeling
  lines; friends get glad ones; acquaintances get the
  standard kind line; strangers and rivals don't know
  it's your birthday (the page says word travels by
  friendship). Kenny wishes muffled, Timmy by
  proclamation. A wish moves NO friendship, NO coins, no
  state beyond its once-per-person-per-birthday stamp —
  pinned in the suite, including a byte-exact
  before/after comparison of the friendship table.
- **The party (small)**: in the evening (the dinner
  hours, read off Dinners by reference), IF your crew is
  non-empty and the town lets the night be yours, your
  crew throws a party at Your House on the
  sleepover/dinner pattern: three beats — the Arrival
  (Talk), the Cake (a Look at your house; serves exactly
  ONE real Snacky Cake when one is held, exactly as the
  cast parties do, and states Mom's improvisation
  honestly when none is), the Send-off (Talk) — riding
  the existing DINNER TalkGate rail with a payload beside
  the cast parties' `PartyHit`. Guests are claimed to
  your house on the sleepover claim's pattern, consulted
  at the sleepover claim's own seat in the agenda stack:
  NO new seat (the stack stands at twenty), and the
  claim self-guards on the crew's occupied oracle
  (`Crew.busyReason` learned the "birthday party" claim
  beside the sleepover's), so every bigger claim seated
  earlier still wins its people. The wrap pays the
  sleepover discipline — +2 a guest for a full night,
  +1 for a partial one, once, at rollover — and a party
  nobody reached leaves no trace.
- **The yield, guarded**: a birthday landing on a
  holiday, in a finale week, on a case-climax night,
  opening night, a fair or carnival week, or a night
  your own table is spoken for (family dinner, a hosted
  dinner, a planned sleepover) still fires the whole
  morning — the household is yours — while the evening
  party yields, its reason derived on the birthday
  morning (Dance's guarded pattern), stamped into the
  day's record, and stated in the morning report and on
  the Journal page. A birthday on a school day reads
  normally: wishes happen where people are. An empty
  crew makes the day household-only — stated plainly,
  punished nowhere.
- **The records**: a Journal 🎂 YOUR BIRTHDAY page
  (birthdays held, the card text kept, who wished, the
  party read-back, the derivation in plain words); ONE
  TOWN NEWS line in the birthday week's edition ("the
  new kid's birthday" — the headline ladder stands at
  ten, deliberately); ONE phone text on the birthday
  morning via a new `playerbday` trigger through the
  existing harvest — from your closest friend, or from
  Mom when no friend stands close enough (the sender
  admission set stays closed either way; fact-only
  copy). Saves additive-only: every new key is a
  `life`/`lifeText` stamp under the `pb*` names, listed
  in `PlayerBirthday.kt` and the tuning ledger. The tree
  stands at 111 Kotlin files (PlayerBirthday.kt joined).

### v0.14.0c (versionCode 104) — The Bijou film festival (Queue #14 item 3)
- **One week a year, five nights**: the festival plays the
  December week that holds the 8th, Monday–Friday (Monday
  Dec 2–8, Friday Dec 6–12), announced a week ahead in the
  paper and on the marquee. The anchor was swept 2026–2100
  in one lived calendar space against the talent show's
  Saturday, the dance, the play's schedule (poster through
  opening), the fair and carnival weeks, the parade, all
  three field trips, the finale windows and the holidays —
  zero collisions, and the suite re-proves it every run.
  Runtime yields ride MovieNights' deference: a case
  climax (the only owner the sweep leaves possible) darks
  that one night, stamps the fact where it's learned, and
  the week plays on; `darkReason` learned `DARK_FESTIVAL`
  ranked last, so the regular screenings stand down for
  the week and the marquee names the bigger owner when
  there is one.
- **Five invented prints, a derived slate, a seated jury**:
  "The Last Lighthouse on Dry Land", "My Homework Ate the
  Mayor", "The Night Frequency", "Goat vs. Mountain" and
  "The Cartographer's Daughter" — titles, premises,
  program notes and scenes authored whole and
  denylist-scanned in the suite against real pictures and
  franchises. The slate's order is a seeded (year)
  shuffle; stars are drawn the MovieNights way (KID_ORDER
  minus the never-come), seeded by (year, film). The jury
  rotates in the yearbook's pattern over nine townsfolk
  in three leaning trios (craft / heart / weird) — one
  trio a year, no juror repeated across consecutive
  years.
- **Scoring, derived and recomputed**: a film's festival
  score is its authored qualities (craft/heart/weird at
  4/4/2) + the jury's leaning dimensions at face value +
  twice the night's buzz — the buzz you made if you sat
  the night (the session is the feature's own eleven
  beats, scored by MovieNights' reaction table, the room
  reacting from who is actually in it), the house's own
  seeded buzz if you didn't. The Golden Popcorn's tie
  ladder is stated and suite-proven level by level:
  total, then the audience, then heart, then title A–Z.
- **The claim and the records**: the festival house is
  gathered by a new agenda claim seated twentieth (after
  the field trips', before the sleepovers'), 19:00–21:30,
  because Mon–Thu has no screening claim to ride;
  TalkGate stays at eleven rails. The paper carries the
  festival in TOWN NEWS only — announcement in its week,
  winner in the closing week's edition — and NOT as a
  headline tier: the ladder has stood at ten since
  v0.12.0a, every Queue #13/#14 set piece before this one
  took section copy, and a festival the whole town
  attends is news, not a verdict; the decision is
  deliberate and pinned. The yearbook gains a line in
  the volume the December belongs to; the Journal gains
  a 🎬 FILM FESTIVAL page (scores sealed until the
  wrap); the phone carries one morning-after text under
  the new FESTIVAL trigger (sender set and budget
  untouched); the winning film's stars take +1
  friendship only if you sat closing night, paid once
  at the wrap; exactly one group photograph may be
  minted at closing night through Photos.composeGroup.
  Admission is free — MovieNights' arts-line position,
  held by reference to its doors and hours. Additive-
  only saves; a mid-festival save/reload keeps the
  slate, attendance and buzzes, pinned in the suite.

### v0.14.0b (versionCode 103) — The field trip (Queue #14 item 2)
- **Three Fridays a school year, announced a week ahead**: fall's
  second Friday of October (nature study at Stark's Pond),
  winter's first Friday of February (the town museum — the Park
  County Historical Room, inside the existing City Hall), spring's
  third Friday of April (agriculture day at Tegridy Farms). The
  poster lives on the classroom board and the morning ticker;
  the anchors were swept 2026–2100 in one lived calendar space
  against the dance, the talent show, the play's schedule, the
  fair and carnival weeks, the parade, the finale windows and
  the holidays — clean everywhere, and the suite re-proves it.
- **The day bends around the trip**: School gains its lowest
  quiet reason (lessons, recess and Friday's bee stand down
  whole), and an agenda claim seated nineteenth in Engine's
  stack — after the parade's, before the sleepovers' — holds
  the derived class (the 39 KID-band cast kids; the young teens
  stand outside) and its rotation chaperone at the site,
  9:00–13:00. TalkGate stays at eleven rails. A runtime owner
  (a case climax, a finale, a holiday) yields the trip whole
  and stamps the reason, Dance's pattern.
- **The played day**: board at the classroom board 9:00–9:30
  (30 game-minutes each way), a taught three-round choice beat
  per site — the pond's Specimen Scan, the museum's Docent
  Quiz with two rounds counted live off the game's own
  registries, the farm's Harvest Help — free time with the
  classmates actually present, exactly ONE class photograph
  minted through Photos.composeGroup, and the bus back from
  12:30. Missing the bus is an honest missed day, punished
  nowhere.
- **The records discipline**: the beat pays only on a
  played-through trip — present classmates +1 (cap 8), the
  chaperone +1 only for sharp work, the "field trip" topic
  planted with the class. Journal 🚌 FIELD TRIPS page, one
  TOWN NEWS line in the trip's week's edition only (headline
  ladder stays ten), one morning-after phone text under the
  new FIELDTRIP trigger (budget and sender set untouched).
  Additive-only saves; a mid-trip save/reload keeps the ride,
  the open beat and the photograph, pinned in the suite.

### v0.14.0a (versionCode 102) — The reflections pass (Queue #14 item 1)

Queue #14 opens by closing the two longest-standing
visual gaps — the town sees you the way you see it.
Rendering only: no derivation moved, no save key
added, and drawing a surface writes nothing (the
suite pins the serialized save byte-identical
across every render decision in this build).

- **The mirror shows the night look.** The Your
  House mirror's glass now holds your reflection.
  While a sleepover night is LIVE at the house —
  the plan standing, the evening open, the exact
  moment `Sleepwear.liveNow` reads for the
  shutter — the reflection wears your pajamas,
  from the new pure reader `Sleepwear.mirrorCode`:
  your own palette softened, the nightcap rule
  read off your day hat, solids only as ever. The
  code is the shutter's own player branch, so a
  photo composed at the house that night freezes
  the identical code onto your figure — the
  mirror, the album, the street re-render and
  the darkroom prints agree figure-for-figure,
  pinned in the suite against a real composed
  frame and its guest set. Outside a live night
  the mirror shows the ordinary day look. The
  gap v0.12.0d stated plainly ("the mirror does
  not show the night look — it renders no
  figure") is **closed**: the mirror renders
  exactly one figure now, yours, and only ever
  yours.
- **Your hero costume renders in street POV.**
  While you stand a patrol spot in costume —
  identity made, patrol night (Wed/Sat), patrol
  hours 19:30–22:00, outdoors at a spot, exactly
  `Heroes.playerInCostume`'s moment — the street
  scene draws you in the scene: cape, mask and
  emblem over your worn palette, through
  `KidArt.drawHeroFigure`, from the new pure
  reader `Heroes.streetCostumeCode`. The code is
  `figureCode`'s player branch read live — the
  same code the camera freezes at the same
  moment, emblem read off the same power the
  caper records — so the street and the album
  tell the same story about what you wore. The
  suite pins the boundary on every side: before
  19:30, at 22:00, on a non-patrol night, away
  from a spot, indoors, and with no identity
  made, the code is empty and the street stays
  first-person — you stay behind the camera, as
  ever. The gap carried since v0.10.0b ("your
  hero costume is invisible in street POV") is
  **closed**.
- **The walls that stay:** your pajamas still
  never draw on the street itself — the mirror
  is their one live surface, the frozen photo
  records the other — and the creator preview
  still shows the day look being saved, never
  the night's. Adults and guests gain no
  reflection: the glass holds one figure.

### v0.13.0g (versionCode 101) — Polish & performance pass #13: the Queue #13 audit (Queue #13 item 7, the closer)

Queue #13's audit, in the polish tradition: no new
features — the suite grows the audit section, the
record is set straight, and the two cosmetic
debts the queue itself recorded are paid. The
audit's findings:

- **The two sanctioned fixes.** The talent show's
  post-night gossip (the winner's line, the bill's
  score lines) and the parade's echo line were
  day-windowed: inside their windows a speaker
  could say the same line again on a later day.
  Both now stamp once-ever in the play's own
  pattern (`talentPost:<year>:<name>`,
  `paradePost:<year>:<name>` — the hearing is the
  stamping), and the audit pins each line at one
  firing per save while the run-up talk stays
  seeded and repeatable, as designed. These are
  the build's only production edits.
- **One 400-day soak lives every Queue #13
  surface on one save:** the family page read
  57 times, byte-stable on every read and
  writing nothing; home barks decided 395
  times in 10 distinct lines; the dance, the
  talent show and the parade each came due in
  2027 and, left unattended, wrapped as missed
  with their reasons stamped; the pen-pal year
  ran whole — 62 letters in, 61 answered (the
  one holdout is the new gap below), every
  reply gap 3–5 days but a single 15-day seam
  at the year's settlement, warmth at the top
  band with the lane capped at 100; the phone,
  watched daily, peaked at 30 texts with its
  sender set closed and never two cases open;
  and the same life retired all eleven cases
  inside the 400 days. None of the three new
  phone beats fired in the soak — the dance
  had no date, talent night no qualifying
  friend, the parade's morning-after harvest
  was full — best-effort by design; the phone
  section below proves each beat healthy.
- **The ELEVEN-case lifetime, re-driven:**
  all eleven arcs retire in order in one
  unattended life, 348 days end to end, the
  eleventh walking its unopened branch; the
  attic's CASE_TOTAL stands at 11, and the
  never-two-cases-open discipline held
  throughout.
- **Contention, re-pinned at the code's own
  seats:** the TalkGate ladder stands at ELEVEN
  rails — the queue added none; the dance ask,
  the talent seam and the parade seam all ride
  existing surfaces. The agenda stack in `Engine`
  now seats EIGHTEEN claims in its own order —
  Dinners → Birthdays → Family dinner → Jobs →
  MovieNights → Capstones → Finales → Fair →
  Carnival → RivalCrew turf → Heroes patrol →
  Play rehearsals → Dance → Talent → Parade →
  Sleepovers → Stand staffing → Crew last —
  pinned from the source, seat by seat.
- **The phone at its worst week:** the
  polish-#12 worst week re-staged with the new
  triggers in the harvest — 24 new texts
  across seven mornings, the weekly summary
  landing once, the inbox ending at exactly
  220, the cap — no thread over its own
  ceiling and no sender outside the closed set
  (cast ∪ parents ∪ the three group rooms).
  Three companion mornings, staged through the
  real harvest on days with room, prove the
  new beats land whole when they can: the
  dance's missed-date texts from Kyle, the
  talent bill friend's text, and the parade
  report from Stan, the closest friend.
- **The tuning ledger is completed for every
  Queue #13 dial** — the school dance, the town
  parade and pen pals join the family page,
  talent night and the eleventh case in the
  ledger above, each dial named, and the audit
  sweeps the dials against their constants.
- **Veteran XII:** the saturated save carried
  forward one case — arc #11 retired over
  veteran XI's full saturation, with Queue
  #13's records laid in (two talent years, two
  parade years, the transplanted dance wrap,
  the full pen-pal archive, the new gossip
  stamps) — serializes at 585,165 chars
  against the 640×1024 = 655,360 ceiling:
  70,195 chars of headroom, round-trip
  verified.
- **Gaps, consolidated and stated plainly**
  (none silently fixed): the mirror at Your
  House does not show the night look — it
  renders no figure, and its only figure route
  is the creator preview, which must show the
  look being saved. On a shared birthday the
  phone sends one beat from the roster-first
  name; the other birthday is still in the
  paper, still giftable, still party-eligible.
  Two pairs in the paper's ladder (fair over
  finale, finale over opening night) can never
  co-occur in the lived calendar, so those
  orderings stand pinned by source order rather
  than by fabrication. Group posts land at
  morning granularity, like the solo phone's
  notes. The stand's permit is narrated as City
  Hall's but bought at the pitch dialog — a
  stated UX bend. Your hero costume is invisible
  in street POV, which is first-person; it shows
  in photos, at the mirror, and on the YOU card.
  Arc #11's COMMITTED texture: with exactly two
  pieces tagged per letter at a price of two, a
  reading on the four non-locked letters is
  graded PROVEN or REFUTED on arrival in normal
  play — the COMMITTED state is exercised on
  the context-locked pair's boundary; the
  derivation supports all four verdicts
  uniformly, and finale eligibility still
  requires the settlers live. The pen-pal fact
  slots for the dance, talent, parade and
  carnival kinds are pinned by their banks and
  by those modules' own sections, but carry no
  fabricated-record suite test of their own.
  One new note from this audit: a pen-pal
  letter that arrives after the school year
  settles — her thank-you, the morning after
  the June visit — cannot be answered until
  October's box comes out again; the soak's
  correspondence closed 61 of 62 answered for
  exactly this reason. The letter keeps, the
  warmth keeps, and the reply waits for the
  new school year — stated, not silently
  fixed.
  And the standing one: there is no emulator or
  on-device rendering proof on this machine —
  the suite is the audit's eyes, and Lucky's
  screenshots remain the only real QA.
- **Verified:** the full JVM smoke suite
  passes at **101,672 checks, 0 failures**
  (baseline 101,567/0 read off the on-disk log
  on the untouched v0.13.0f tree before any
  file was written; the final count read off
  the final run's on-disk log), and the clean
  APK builds, badges 101 / 0.13.0g and signs
  with the standing key, the lorebook
  byte-identical inside.

### v0.13.0f (versionCode 100) — Mystery #11: the voice case (Queue #13 item 6)

One new module (`Mystery11.kt`); the tree stands at
108 Kotlin files. **The case:** "Signed in
Another's Voice" — notes and letters surfacing
around town signed with other people's names, and
an investigation that reads the letters against
the voices they claim rather than the hands that
wrote them. **The spine is a voice ledger:** six
surfaced letters, each claiming a signer; for each
letter the ledger derives its findings from the
letter's own text against the claimed signer's
authored voice profile — markers present, the
marks that must be there, the nevers violated, the
studied tells — recomputed on every read, never
stored. **The three-reading shape is the case's
signature:** IN VOICE (the signer's own — the hand
may be borrowed, the voice never left them), OFF
VOICE (a stranger's hand — nobody studied them),
BORROWED VOICE (a studied imitation — told from a
stranger's miss by the markers it gets RIGHT, and
from the true voice by the tell it leaves).
Readings are priced at 2 live evidence pieces in
the standing discipline; verdicts derive (OPEN /
COMMITTED / PROVEN / REFUTED), standing readings
final, refuted strikeable. **Evidence:** 12 pieces
(5 witness / 5 look / 2 participation), two tagged
to every letter, exactly 2 context-locked — and
the two locked pieces are the settling pieces of
the two load-bearing letters, so the letters that
matter most can only be settled late. The town's
honest voice oddities carry the red herring
(dictated letters, a secretary's-hand town,
muffled dictation, a voice box's flat print), and
the evidence supports a fully innocent reading of
the obvious suspect-shaped answer in plain sight.
**Finale:** the Open Letters at City Hall — four
calls (READ THE LEDGER ALOUD, gated on both
load-bearing letters PROVEN + all letters read +
none refuted + at least four proven; NAME THE
BORROWED HAND on any one proven; LET THE LETTERS
STAND; WALK AWAY), four landings (voiced / named /
standing / unopened) with marks, bond deltas,
faction ripples, standing lines, and a delayed
tail on the voiced landing (the correction cards,
a week on). The game never admits a call was
wrong. **The wall:** the case is fully playable
and solvable-shaped on a save that never signed
up for pen pals — the module's source is pinned
clean of the correspondence, and the suite drives
the case end-to-end on such a save. **Lifetime:**
eleven cases — the case queues ten days after the
photo case retires, defers past holidays,
registers in every deference list the earlier
cases joined (incl. the paper's CASE CLOSED read
off the new retired-day stamp), and the attic's
coverage extends to `CASE_TOTAL` 11. 🔴 Structure
only, here and everywhere: the case's truth stays
sealed inside its own content.

### v0.13.0e (versionCode 99) — Pen pals (Queue #13 item 5)

One new module (`PenPals.kt`); the tree stands at
107 Kotlin files. **The pal:** Wren Halloway of
Pinewood Flats — wholly invented, both of them, and
NOT cast: a VisitorPersona outside the 98 on the
parents' precedent (census-pinned from both sides),
an authored mini speech card and letter banks in her
own voice, an authored SHEETS-style spec drawn
through the KidArt geometry (no fan sheet joins the
cast set — she has none), and a warmth lane of her
own that never touches `state.friendship`. **The
program:** signup at the Elementary's classroom
board every October 1–21 (late joiners wait for next
year — stated), the pairing the same afternoon, one
authored line from your dad at the Post Office (the
letters' route). **Letters in** ride the mailbox on
a seeded 3–5 day cadence: her town's banks (the tower
fish, the tooth market, lake season) plus FACT
SLOTS — follow-ups on facts your sent letters
actually carried, so every fact about you in her
hand traces to a record she was sent. **Letters
out** compose in front of you at the mailbox from
your real records — latest photo, a closed case's
PUBLIC shape (title + closed, from the attic's boxes
only; the spoiler wall is scanned over every bank
and composed letter), the stand, the garden, the
fair/carnival/dance/talent/parade — under three
tones that color the frame and never the facts, 15
real minutes, the sent text kept verbatim. An
empty-records save gets Flats news and invented
nothing (fabrication-pinned). **Warmth:** +3 a
reply, bands at 15 (writing-regular) and 45 (a real
friend) flavoring her greetings and sign-offs;
unanswered letters stretch the gap +3 and are noted
kindly; three unanswered and the correspondence
goes dormant until one revival letter (+1) reopens
it. **The visit:** top band at the May 31 settlement
→ June 10–13 as a resident-class guest (your house
mornings/evenings, the park middays — the Family
whereNow pattern), her own deterministic talk sheet,
+1 a day for the first talk, gifts into her lane,
one album photo through the ordinary camera, a
goodbye beat on the last day, a thank-you letter the
morning after; below the band, a "maybe next
summer" letter instead. **Records:** the Journal's
📬 PEN PALS page — pairing, standing, visit record,
and the letters (newest 16 whole, the elders folded
to the one-line summaries written when they were
composed). The paper does NOT cover the letters;
the phone gets nothing; no TalkGate rail (eleven
stand); no agenda claim beyond the visit's guest
presence; no case machinery grew here. Saves
additive only. **Pen pal dials, the tuning ledger:**
signup Oct 1–21; gap 3–5 days seeded; quiet stretch
+3 from two unanswered; dormant at three; reply +3 /
revival +1; bands 15 / 45; keep 16 full letters, 12
folded lines; visit June 10–13 (4 days); reply 15
minutes; visit talk +1 a day.

### v0.13.0d (versionCode 98) — The town parade (Queue #13 item 4)

One new module (`Parade.kt`); the tree stands at
106 Kotlin files. **The parade:** the Founders' Day
Parade on the third Saturday of May (always May
15–21), the poster up a week ahead on a reviewing
stand drawn on Main Street — proven disjoint from
the dance, the talent show (measured minimum gap 31
days), the play's openings, the fair, the carnival,
every finale window (measured minimum clearance 4
days ahead of the spring finale's May 25 opening)
and every holiday window by a 2026–2100 sweep in one
lived time space; the runtime yields (case climax /
finale / holiday through the standing quiet-day
definition, plus the play guard in the dance/talent
form) skip the year whole, reason stamped where
learned and printed on the stand, the page and the
paper. **The procession is derived** from the town's
structures on every read: households claim first
(all seven Dinners families ride whole), then your
stand's staff, your crew, the Mayor's car (McDaniels
with Barbrady and Yates — claimed before the parents'
float can take her), the Bijou's float (the day's
feature's stars, off MovieNights' own rotation) and
the eight factions at a stated six riders per float
(first-listed unclaimed member always aboard, the
rest seeded by year; the school staff's float is
presented as the Elementary's, which leads). Your
stand float's line derives from the crate's real
contents at your real prices; your crew marches as
a walking entry even when every member was claimed
by a family float. Everyone unclaimed is route crowd
at one of four seeded curb spots (bank steps, Bijou
marquee, post office corner, City Hall steps) — the
suite partitions riders/crowd over the whole cast
and proves no double placement. **Your day:** march
(with your crew, or free-agent with the school;
+1 to your walkers at the wrap) or watch from a
curb spot (floats pass in chunks of four, reactions
from the people actually standing near you), ending
at the Mayor's car with her authored closing line
and exactly ONE Photos.composeGroup photograph
(your float of choice from the curb; your entry at
the City Hall steps when marching). **Candy:** three
throwers (Cartman float → snack cakes; Bijou float
and Mayor's car → cheesy poofs — the stand's own
stock at its own prices; your stand throws nothing),
one 3-throw showdown-bar catch session each for
watchers, caught goods into real inventory, the day
capped at 6 — suite-computed maximum haul 6 items /
12 🪙, once a year. **The claim** seats eighteenth
(after the talent show, before sleepovers) holding
Main Street 10:00–13:30; Walk's geography is
untouched while through-trips pay a stated +15 min,
priced identically by routeMinutes and walkLeg,
arrivals and departures exempt, the bus exempt by
design. A Journal 🎺 THE PARADE page, ONE TOWN NEWS
line (ladder at ten), ONE morning-after phone text
(MAX_TEXTS at 220), rider gossip as a TalkGate seam
(rails at eleven). **Parade dials, the tuning
ledger:** window 10:00–13:30 · poster 7 days ahead ·
chunks of 4 · faction riders cap 6 · throws 3 per
thrower × 3 throwers · CANDY_CAP 6 (max haul 12 🪙) ·
MARCH_PAY +1 · pass-through delay 15 min · photo
frame cap 12 · your catching nerve 5 (the new kid's
standing spotlight number). Saves additive-only.

### v0.13.0c (versionCode 97) — The talent show (Queue #13 item 3)

One new module (`Talent.kt`); the tree stands at
105 Kotlin files. **The show:** talent night at the
Bijou on the second Saturday of April, the poster up
a week ahead — proven disjoint from the dance, the
play's openings, the fair, the carnival, every finale
window and every holiday window by a 2026–2100 sweep
in one lived time space (April's pocket has no
calendar coincidence at all; the runtime yields — a
case climax, a finale, a holiday beat, the play's
night — ride the town's standing quiet-day definition
plus the play's live check, reason stamped where
learned and printed on the poster, the year yielded
whole, never postponed). The Bijou's Saturday
screening stands down that night through MovieNights'
own deference, registered both directions (dark
marquee by name, no movie claim, no screening text;
and the marquee names case/finale/holiday over the
show whenever one of them owns the night). **The
field** is derived from the play's own data — stage
nerve ×3 + the bee's voice ×2 + a seeded want, top
ten to the bill, the running order seeded and posted;
every kid's act authored (all 42, wholly invented,
denylist-guarded exactly like the play's and heroes'
guards — no real songs, no musicals, no franchise
acts) and every kind carrying the room's three seeded
reads. **Your entry** rides the Bijou screen in poster
week: four acts (sing / jokes / magic / dance), each a
distinct played shape on the showdown bar — phrases,
sweep, jitter and hold threshold all differ — plus
ONE rehearsal, a real 30-minute beat on the stage
worth a stated +6, banked through an act change. The
audience track is complete without entering: the same
beats, the room's prompts (after acts 2, 5 and 8)
scored by MovieNights' own tables into the night's
house. **The evening** (18:30–21:30, the Bijou) is an
agenda claim seated after the dance and before
sleepovers — the stack's seventeenth seat, pinned out
of Engine's source — seating the bill, the host (the
yearbook rotation's stage desk) and a seeded 45%
house. NPC scores are derived live (34 + nerve ×4 +
voice ×2 + chemistry-with-the-room ×14 + a seeded
±5 wobble, clamped 5–99; the suite re-derives them
from raw inputs), yours from your played session (42
+ 50 × quality + the rehearsal's 6). The winner is
derived at the wrap — ties by chemistry with the
room, then nerve, then the alphabet, proven level by
level in the suite — and takes a 15-coin envelope, the
framed line, the yearbook line, and ONE photograph
through Photos' own composition captioned with the
winning act (yours, if you won). Friendship pays only
on a night played through (entrants +1, winner +2,
a legendary house +1 again — the movie pattern). A
Journal 🎤 TALENT SHOW page (bill while live; the
table as played, your act and placing honestly
stated; elder years one line each), ONE TOWN NEWS
line in the show week's edition (section copy — the
ladder stands at ten), ONE morning-after phone text
(the winner; the host when you win; a friend who was
there when you missed it), entrant gossip in the
play's pattern (a TalkGate seam — the rails stand at
eleven), and the Bijou's Look text across the cycle.
The room is kind throughout: honest numbers, kind
truths, no mockery anywhere in the copy. Saves
additive-only.

### v0.13.0b (versionCode 96) — The school dance (Queue #13 item 2)

One new module (`Dance.kt`); the tree stands at
104 Kotlin files. **The dance:** an annual February
night — the Sweetheart Dance on the second Friday of
the month, poster up a week ahead — proven disjoint
from the play, the fair, the carnival, every finale
window and every holiday window by a 2026–2100 sweep
in one lived time space, with the single possible
coincidence (Valentine's Day landing on the second
Friday) yielding the whole year, reason printed on the
poster; the live-yield routes (case climax, finale,
holiday) ride School's quiet-day definition, plus the
play's live check, and stamp their reason where it's
learned. **The ask** rides the tap-person menu in the
asking week: authored dispositions for all 42 kids
(22 glad, 10 shy, 10 final-NO; yeses at Friend 30 /
Close 55, the shy soft-no below 55 recorded and
re-askable, a below-tier ask never spent), one date at
a time with the stood-up switch costed at 3 friendship
and remembered (the toast line in the gym, the Journal
fact, the final re-ask), adults answered off the
record, stag stated everywhere as a whole plan. **The
evening** (18:00–21:30, Elementary) is an agenda claim
seated after the play and before sleepovers — the
stack's sixteenth seat, pinned out of Engine's source —
with four beats: ARRIVAL in the dressed gym, the SLOW
DANCE played on the showdown bar as a real session
(zone power widened by the partner's nerve from the
play's own table; the stag spotlight at nerve 5; held
phrases at Feel's good threshold; steady/swayed/
glorious verdicts paying the date +2/+2/+1 friendship,
stag paying nobody), REFRESHMENTS consuming exactly
one real inventory item per serve (lemonade counted as
punch, snack cakes, the cooler free), and THE LAST
SONG reading the night back and minting exactly one
group photograph through Photos' own composition with
the night's caption. A Journal 💃 THE DANCE page (live
asking state, wrapped night, three elder years), ONE
TOWN NEWS line in the dance week's edition (a dance-only
week still headlines the quiet-week line — section copy,
never a tier), ONE morning-after phone text (your date,
else the closest friend who was there; a missed night
with a date gets the date's honest text; a yielded year
texts nobody), the sleepover planner's named refusal,
and the gym's Look text across the cycle. Tone held to
courage-and-friendship throughout; saves additive-only.

### v0.13.0a (versionCode 95) — The family page (Queue #13 item 1)

Queue #13 opens by closing Queue #10's last
standing gap: your family gets its page at last,
and a voice at home. No new module — the tree
stands at 103 Kotlin files.

- **👨‍👩‍👧 YOUR FAMILY — a Journal page of your own
  people.** Every line is derived from the family's
  real records on every read: the arrival day,
  remembered; the stance, stated in the family's
  own words (Mom's echo line, quoted); who's home
  right now, read through `whereNow` — a pure twin
  of `blockAt` the suite sweeps across the week's
  hours and pins equal to the live block, gate for
  gate, because a page must never write; each
  parent's warmth from its own `famPts` lane; a
  job line apiece; the table's record (dinners sat
  at, the most recent night — a full table labeled
  as one, and a more recent unsat night said so
  instead, honestly); Mom's allowance book (the
  lifetime total, the last earned morning, the
  rule as she states it); the chores with their
  askers; and the family frame, read through the
  same functions the attic reads, so the two pages
  agree about what's in it by construction. Before
  the arrival the page is an honest not-yet:
  they're coming, on the promised day, and the
  public shape of what's coming is all it says.
- **One new stored fact, named:** per-parent chore
  counters (`famChoreDoneBy:<parent>`). Who asked a
  finished chore existed nowhere — `famChoreBy` is
  transient and clears at completion — so the
  counters are kept from this build on, and the
  page says plainly when a total predates them
  rather than inventing names.
- **Home barks.** Mom and Dad gain greeting and
  idle barks at home, in the barks system's own
  pattern: a rhythm signature and hello apiece in
  `Barks.HOUSEHOLD_PATTERNS` — their own table,
  because `PATTERNS` is census-pinned to the 98
  cast and the family is not cast — resolved
  through `patternForAny` by the same SoundGen
  render and SoundPool load the cast uses; seeded
  lines from two new banks, `HOME_GREETINGS` and
  `HOME_IDLE` (five apiece per parent, each bank
  opening with its pattern hello so bubble and
  bank can't drift); and the decision itself pure
  in `Family.barkDecide`, mirroring `Barks.decide`'s
  discipline — the settings/quiet-hours gate
  first, a greeting for everyone home when you
  walk in, a newcomer greeted on the cast's
  `ARRIVAL_CHANCE`, a lingering murmur at
  `HOME_IDLE_CHANCE`, and the shared cooldown
  vetoing without a re-roll. Flavor only: the
  suite lands 200 decisions and the save is
  byte-identical — no warmth, no coins, no state.
- **The walls stand.** The family is still not
  cast (the census is pinned from both sides, page
  and barks notwithstanding); the page never reads
  `state.friendship` (pinned against a hostile
  friendship table); family conversation remains
  its own sheet — the TalkGate ladder stands at
  eleven rails; the phone gains no trigger; the
  family spoiler scan extends over the page and
  both bark banks.
- **Gaps, stated:** home barks fire only indoors
  at Your House — the one room the parents stand
  in; outdoors and at work they keep their
  conversation voices only. The page's presence
  lines are a present-tense read, not a forecast —
  tonight's dinner is still the morning report's
  news to break. And per-parent chore names only
  accumulate from this build forward, as the page
  itself says.

### v0.12.0f (versionCode 93) — Mystery #10: The Photograph Nobody Took (Queue #12 item 6)

The tenth case is a tenth shape: this one is a
comparison. One new pure module (`Mystery10.kt`),
additive save state in the established arc pattern
(pre-0.12.0f saves simply queue the case behind the
ninth), the manifest at 93 / 0.12.0f, and src grows
102 → 103 Kotlin files. The lorebook rides inside
unchanged.

- **The premise.** The town's photographs are
  records — every album frame and darkroom print
  carries its place, its day, and who was in it,
  frozen at the shutter. For the Elementary's
  anniversary, the photo club mounts "The Town in
  Photographs," and its centerpiece is a loaned
  group portrait whose penciled caption names a
  staff, a place, and a day. Hung beside the
  yearbook archive's own print of the same sitting,
  the two photographs disagree: the loaned print
  holds six figures, and the sitting's own record
  holds five. The town names its culprit at cape
  speed — the Bomber, the face at the edge of half
  its frames, finally graduated from sneaking into
  photographs to making them. The display committee
  would like the wall to make sense by the
  Rehanging, the display's official opening.
- **The spine is a comparison board.** The Journal
  board draws the four photographs as rows: the
  loaned print, the archive print, the Bijou's
  enlargement, and a family album's page. Each
  frame carries three **checks, derived** from its
  own frozen fields on every read, never stored:
  does its place agree with where its caption says
  it was taken? Does its day? Do the faces in the
  frame match the faces the moment's own record
  holds? For each photograph you commit a
  **reading**: GENUINE (one shutter, honestly
  captioned), MISFILED (one honest shutter wearing
  the wrong caption — the photograph is true, its
  label names another moment), or COMPOSITE (no
  single shutter holds it — the frame was made, out
  of more than one real moment). Each frame has
  exactly one piece of evidence that settles it,
  and **verdicts are derived** from the recorded
  reading and the live evidence on every read,
  never stored: **proven**, **refuted** (the
  reading stands struck; it can be struck and read
  again), or **committed**, standing untested.
  Two of the frames prove load-bearing at the
  finale, and they share only one failing check
  between them — learning which check discriminates
  is the case.
- **Twelve evidence pieces** (5 witness / 5 look /
  2 participation), three tagged to each frame, in
  the arcs' mixed-verb pattern — keepers and
  families to hear, backs and marquees and mount
  cards to find, a hanging and an examination to
  work with your own hands. Two pieces are
  **unreadable when found** — inert, honestly
  described as not-yet-making-sense — and become
  spendable only after the chapter that unlocks
  their context. An inert piece settles nothing,
  proves nothing, buys nothing.
- **The difficulty is that every oddity is honest.**
  The photo system has mundane engines to spare —
  prints that outlive faded originals, reprints
  pulled flat from aging negatives, photobombs
  frozen faithfully in real records, captions
  engraved from the wrong line of a book — and the
  case builds its red herring from them: a whole
  theory aimed at the most-suspect photograph on
  the wall, which turns out to be the one with its
  papers in order. An evidence-supported fully
  innocent reading of the town's suspect-shaped
  answer is on the board from the second week: a
  habit you can check, in dated and placed records,
  is not the same thing as a hand you can prove.
  And the frame the town trusts most — the one it
  grew up under — is the one whose own fields
  disagree with each other.
- **The finale is the Rehanging.** The display's
  official opening, in the school hall: the four
  photographs under their captions, a results
  board, a labeled space in the program — THE
  WALL, CORRECTED. Four calls: **read the frames
  aloud** (gated on the board's final derived
  state — the loaned print and the Bijou's
  enlargement standing proven, every frame read,
  no reading standing refuted, at least three
  frames proven), **name the Bomber** (the town's
  own answer; gated on any one frame standing
  proven), **let it hang as loaned**, or **walk
  away**. A closing nobody plays lands unexposed.
  Every landing persists in the arcs' style —
  marks, bonds, standing lines, ambient callbacks,
  an objection that stays in the record in the
  hard landings, a delayed tail on one landing —
  and, as ever, the game never admits a call was
  wrong.
- The case queues ten days after the ninth case
  retires, defers past holidays like every case
  before it, registers its climax in the standing
  deference lists (the Bijou's doors, the
  classroom, the walk home, the season finales),
  the paper can headline its closing day like any
  case's, the attic keeps its box in the same
  public shape, and the ten-case lifetime holds in
  the suite's long drive.

### v0.12.0g (versionCode 94) — Polish & performance pass #12: the Queue #12 audit (Queue #12 item 7, the closer)

Queue #12's audit, in the polish tradition: no new
features — the suite grows the audit sections and
the record is set straight. The audit's findings:

- **One 400-day soak lives every Queue #12 surface
  on one save:** birthday parties stand and yield
  across a full year — every eligible birthday
  either stands a party whose evening the audit
  re-verifies claim by claim, or yields with a
  real, named reason; there is no third outcome
  and no violation. The paper publishes weekly
  through the year and every headline it prints
  belongs to the ten-tier ladder; any tier the
  lived year doesn't reach is fabricated beside
  the soak (never forced into it), closing the
  union at all ten — the case tier among them now
  fires on the TENTH case's closing stamp. The
  general-store loop runs the garden year: seed
  packets bought at the rack and spent at the
  plot, supplier plantings beside them, and the
  garden's one seed book balances to the coin
  across both doors while the till's book matches
  every staple bought; the location gate is
  probed from across the boundary all year and
  never once sells. Sleepovers run their Fridays
  with the camera along: qualifying frames freeze
  pajamas end-to-end (album, street re-render and
  a darkroom print of the night's frame all carry
  the same codes), and the noon frame the same
  Friday freezes nothing. The attic is read at
  intervals all year — a pure read, byte-stable,
  its case boxes always exactly the retired
  count. Birthday phone beats are counted daily:
  never more than one a day, always the day's own
  roster-first name. The tenth case opens and
  retires inside the soak's life, and never once
  are two cases open together.
- **The TEN-case lifetime, re-driven:** all ten
  arcs retire in order in one unattended life,
  the tenth never over the ninth's open body, and
  the queue stands empty behind it.
- **Contention, re-pinned at the code's own
  seats:** the TalkGate ladder stands at ELEVEN
  rails — Queue #12 added none; the birthday
  party rides the DINNER rail as a payload, and
  the audit reads the party host's gate live to
  prove it. The agenda stack in `Engine` now
  seats FIFTEEN claims in its own order —
  Dinners → the birthday party → Family → Jobs →
  MovieNights → Capstones → Finales → Fair →
  Carnival → RivalCrew → Heroes → Play →
  Sleepovers → Stand → Crew — pinned in source.
- **The phone at its worst week:** a staged
  206-text inbox (the full 103-thread census, two
  apiece) lives through the densest birthday week
  of the year with a paper summary stamped due, a
  hero-patrol opinion owed and crew posts waiting
  — seven mornings of harvests, and every morning
  the caps hold: at most `MAX_NEW_PER_DAY` new
  texts, no thread past `MAX_PER_THREAD`, the
  inbox never past `MAX_TEXTS` 220, every sender
  inside the closed set, the summary landed
  inside its week, and never two birthday texts
  in a day. One honest finding the week exists to
  surface: the derived beats are best-effort
  inside the shared budget — of the week's four
  birthday mornings, one beat landed (family,
  dinner, story and group traffic ahead of it in
  the chain took the rest), and both staged hero
  opinions were crowded out by the parents' own
  family beats under the one-text-per-sender
  rule; a companion staging proves the hero beat
  lands whole on a morning with room. The
  stamped paper summary is the harvest's one
  lossless promise, and it kept it. Stated in
  the gaps below — not fixed, because it is the
  design working as written, not failing.
- **Economy, re-proven after a season of mixed
  buying:** counter == stand restock == fair
  booth == planting, price for price, off the
  source tables themselves; the stand's books —
  the grown column included — balance after
  packet-planted crops flow Garden → crate →
  books; `GIFT_BONUS` stands as the birthday
  dial at +3. Peak budget: no drift. Censuses
  stand: 98 distinct bark signatures, 17 decor
  pieces, the darkroom caps unmoved at 12 / 3 /
  24. A photo record carrying BOTH a hero code
  and a sleep code on the same figures round-
  trips whole, and the pair's serialized cost is
  measured and pinned — the two frozen fields
  coexist without bloat.
- **The tuning ledger is completed for every
  Queue #12 dial** — the paper's delivery, cast
  birthdays, sleepwear, the attic and the tenth
  case join the Mall counters in the ledger
  above, each dial named.
- **Gaps, consolidated and stated plainly** (none
  silently fixed): the mirror at Your House does
  not show the night look — it renders no figure,
  and its only figure route is the creator
  preview, which must show the look being saved.
  On a shared birthday the phone sends one beat
  from the roster-first name; the other birthday
  is still in the paper, still giftable at the
  bonus, still party-eligible — one beat per day
  is the discipline, not an oversight. Two pairs
  in the paper's ladder (fair over finale,
  finale over opening night) can never co-occur
  in the lived calendar, so those two orderings
  stand pinned by the ladder's source order
  rather than by fabrication. The family still
  has no Journal page of its own and no barks.
  Group posts land at morning granularity. The
  stand's permit is narrated as City Hall's but
  bought at the pitch dialog — a stated UX bend.
  Your hero costume (and now your pajamas) are
  invisible in street POV, which is first-person;
  both show in photos, the album, prints and the
  YOU card. And the standing one: there is no
  emulator or on-device rendering proof on this
  machine — the suite is the audit's eyes, and
  Lucky's screenshots remain the only real QA.

### v0.12.0e (versionCode 92) — The attic (Queue #12 item 5)

Your House gains an upstairs. One new module
(`Attic.kt`, pure Kotlin; the tree is at 102 Kotlin
files), one fixture drawn with the house fixtures in
`GameView` (a narrow door, three steps up, solids
only — plain wood and one brass knob), one dialog in
`GameActivity` in the garden/paper pattern. Nothing
else moves: no new save keys, no new GameState
fields, no economy, no dial.

**The model: derived contents only.** The attic
stores nothing — every shelf is read off records
that already exist, fresh on every climb, and the
suite proves a read writes nothing at all (the save
is byte-identical between two page reads).

- **The overflow shelf.** The trophy shelf
  downstairs holds `House.SHELF_SLOTS` collectibles
  and `House.MAX_CUPS` feat cups — honest caps the
  house has always kept. What the house holds past
  them lives up here: the collectibles past the
  shelf's slots, in registry order, and the feat
  cups past the cap, counted by House's own overflow
  number. When the downstairs display holds
  everything, the overflow shelf is bare and the
  page says exactly that — the attic never invents
  clutter to justify its stairs.
- **The family frame's outtakes.** The frame
  downstairs holds the newest home photo with a
  parent in it (`Family.latestFamilyPhoto`). The
  outtakes are the OTHER home photos with a parent
  in them — the album's, in album order — plus any
  family photo a darkroom print still holds after
  the album's own copy has faded past the cap (the
  print is the record then, and the line says so;
  a print whose album copy still lives is never
  listed twice). Public facts only: the record's
  own caption and its date — nothing a photo
  doesn't already say out loud downstairs.
- **The stored case boxes.** One box per FINISHED
  (retired) case, arcs 1–9, in the town-secrets
  pattern: a taped, labeled box that exists only
  because the record says the case closed. Each box
  carries the case's PUBLIC SHAPE and nothing else
  — its number, the title the Journal already
  publishes, the closing day where the record keeps
  one (the first case's record keeps none, and its
  box says so instead of inventing one), and the
  board's piece count. **The spoiler wall is
  absolute**: no solution, no name the board hasn't
  said in public, no branch or choice word, ever —
  the suite retires all nine cases on six different
  landings in one save and proves none of them
  prints; every box wears the one public stamp,
  CLOSED. An unretired case produces NO box.

Saves are additive-only in the strongest sense —
there is nothing to add: a pre-0.12.0e save loads
unchanged and its attic is already full of whatever
its owner has honestly earned. Fan sheets untouched;
no measured spec moved.

### v0.12.0d (versionCode 91) — Sleepwear (Queue #12 item 4)

Sleepover nights finally LOOK like sleepover nights,
closing the gap v0.11.0c recorded ("Photos render
normal clothes; there is no sleepwear pass"). One new
module (`Sleepwear.kt`, pure Kotlin; the tree is at
101 Kotlin files), built in Heroes' spirit exactly —
the hero pass's shape, pointed at bedtime.

**The derivation.** Every figure's pajamas are derived
deterministically from their own sheet — nothing is
authored per kid. The pajama top comes from their
shirt color and the bottoms from their pants color,
both softened (blended toward a warm pale neutral, hue
family kept) so they read as night clothes under a
lamp. Hats come OFF and the spec's own hair shows —
except a **nightcap** for the kids whose day hat is a
soft knit dome (beanies, pomhats, ushankas, chullos,
trapper hats): their silhouette honestly carries a
soft cap, so the night keeps a floppy one, in the
pajama top's color with the bottoms' color as its one
band and one bobble. The sleep spec strips the day
clothes' construction (no placket, buttons, pockets,
ties, backpacks or scarves; hands bare) and keeps
exactly ONE trim — the collar band wears the bottoms'
color, the way real pajama sets trim. Everything that
IS the kid stays: skin, hair, face, proportions, and
their specials (Kenny sleeps in his parka hood; Jimmy's
crutches and Timmy's wheelchair are untouched).
**Trypophobia discipline is absolute: solid colors
only — no dots, no checks, no clustered patterns, no
repeated motifs, in the art or in any code table**;
the suite scans the sleepwear module's source for
pattern vocabulary to keep it that way.

**The freeze.** `PhotoFigure` gains ONE additive field
beside `hero` — `sleep`, the pajama code
("top|bottom|cap") frozen at shutter time. A photo
composed at **Your House while a sleepover night is
live** (the plan stands for tonight and the evening
window has opened) freezes the code onto every
sleepover figure it seats — the night's planned kid
guests in frame, and you. Your own sleepwear derives
from your creator appearance the same way (your worn
palette, softened). From then on the record is the
truth: the album, the street re-render and the
darkroom prints all draw the same pajamas through
KidArt's one sleep pass. Photos taken anywhere else,
or at the house outside a live sleepover (including
earlier the same Friday — a noon selfie on a planned
night is a day photo), freeze nothing, and the pass is
**not retroactive**: records from before this build
keep exactly what they froze, which is nothing.

**The walls.** Sleepwear exists only on those frozen
figures. Adults never render it — they are never
sleepover guests, the family at home does not change,
and the freeze re-checks the kid bands itself.
Residents and visitors never freeze. The street stays
first-person, so your own pajamas are invisible in
POV, exactly like your hero costume; no other night
system (patrols, finales, movies) borrows the pass.
Fan sheets are unchanged — sleepwear is a derived
render pass like the hero pass, no measured spec
moved, so no sheet regenerates.

**Stated plainly — the mirror.** The mirror at Your
House does NOT show the night look during a sleepover,
and this build does not force it: the mirror renders
no figure at all (it is a wardrobe dialog), and the
only figure-rendering route out of it is the character
creator's editor preview, which must show the street
look being edited and saved — threading a pajama
preview through it would misrepresent the save. Your
sleepwear is visible where the freeze puts it: in the
night's photos, in the album, and in darkroom prints.
Saves stay additive-only (one new figure field,
defaulting to street clothes on every old record).

### v0.12.0c (versionCode 90) — Cast birthdays (Queue #12 item 3)

The town has kept twelve birthdays since feature #55 — a
banner, a text, a little gravity. This build puts all 98
cast members on the calendar and gives the day four
beats, in one new module (`Birthdays.kt`; the tree is at
100 Kotlin files).

**The dates, and where they came from — stated exactly.**
Every cast member gains a birthday (month/day) in
`Birthdays.DATES`. Exactly **one** is lorebook-sourced:
**Clyde Donovan — April 10**, stated in his own lorebook
entry ("born April 10 (per his Facebook profile in 'You
Have 0 Friends')"). The suite pins that against the
lorebook file read directly, and pins the sourcing split
as data (`LOREBOOK_SOURCED` = Clyde alone), so no
authored date can ever be mistaken for canon later. The
other eleven #55 dates stand unchanged, as shipped. The
remaining 86 are seeded-authored (seed 12003 — a shuffled
draw of days-of-year assigned in roster order): 6–11
birthdays a month, and four days shared by two people —
real towns share birthdays, so the systems handle a
shared day instead of forcing uniqueness (on a shared
day the roster-first name is the one the banner and the
phone announce; gifts and parties check the person, not
the announcement). No Feb 29 exists to leak into a leap
year. The PLAYER's character gets no birthday from any
of this — the roster is the cast, full stop.

**The paper.** The week's edition lists the week's
birthdays in TOWN NEWS — names + dates only ("🎂
Birthdays on the record this week: …"), derived whole
off the roster and the week's seven calendar keys, so
the printed dates trace to the registry the way every
other number in the paper traces to its record.

**The phone.** The one fact-only birthday text #55
already ran — the birthday person's own thread, under
the existing closed sender set and budgets, moving no
friendship and no coins — now covers the whole roster,
because it reads the same registry (`TownLife.BIRTHDAYS`
is `Birthdays.DATES` by reference now).

**The gift.** Handing a gift to the birthday person ON
their day pays a stated, flat **+3 friendship bonus**
inside `Engine.giveGift`'s existing math (the item is
the real item, consumed as gifts are; off-day gifts are
untouched — pinned both ways), and the conversation
answers with that person's authored birthday reaction
line — one per cast member, 98 of them, in voice.

**The party.** When the birthday person belongs to one
of the seven dinner households, a small birthday
gathering can stand at that house that evening — on
DINNERS' rails wholesale: the same 17:30–20:30 window,
the agenda claim seated immediately after Dinners' own,
beats on the same DINNER TalkGate rail (a `PartyHit`
payload beside the sleepover's `SleepHit` — no new
rail, no new agenda seat), and Dinners' deference
discipline: the party **yields** to an invited dinner
at that same table (an invitation elsewhere is yours
to choose between — both evenings are named, here and
in the morning report), a hosted dinner, a family dinner, a household
collision (finale/holiday/case at the table), any live
finale week, any holiday beat, a case climax due,
opening night, fair week, carnival week, or a planned
sleepover — and the yield is stated plainly in the
morning report and on the Journal's 🎂 page, where the
day still counts through the paper, the phone and the
gift. Three beats in the invited dinner's shape: **the
Welcome** (the host), **the Toast** (any member — or,
talked to directly, the guest of honor's wish), and
**the Cake**, a Look at the house that serves ONE real
Snacky Cake out of your own pack when one is held (the
Mall's snack counter sells them — the v0.12.0b synergy,
intended) and says so honestly when none is, while the
household improvises, proudly. The wrap lands at
rollover beside Dinners' and pays the invited table's
attendance scale (full evening: host +3, members +2;
partial: +2/+1), the guest of honor +2/+1 more on top —
it's their day. Sleepovers gain the mirror veto: a
standing party refuses a same-night sleepover plan by
name. Disjointness is proven in ONE lived time space
by a two-year sweep in the suite: on every one of 730
days, a standing party's evening is clear of every
rival claim, and every eligible birthday that got no
party names a real one.

**The record.** Journal cast cards carry the birthday
("🎂 Birthday: October 19", marked on the day itself),
and a new 🎂 BIRTHDAYS page beside FAMILY EVENINGS
states today, tonight (the party, or the yield and
why), the next five birthdays, and the running count
of parties you've been part of. Saves are additive
only — the dates are authored data, not state, so a
pre-0.12.0c save simply meets its first birthdays as
the calendar turns.

### v0.12.0b (versionCode 89) — The general store counter (Queue #12 item 2)

The walk-to buy surface the garden named as missing in
v0.11.0b ("no in-person general-goods buy surface
exists in this game") lands: two counters inside the
South Park Mall, one new module (`Shop.kt`; the tree
is at 99 Kotlin files), two fixtures, two dialogs —
and the seed packet becomes a real thing you can hold.

**The sites, on the fiction's own evidence.** Both
counters stand in the Mall, in the second-row band
under the wall fixtures (the ballot box's band at City
Hall): the garden centre's seed rack under the clothes
rack, the snack shop's counter under the transit
kiosk. The Mall is where the code's town already keeps
every walk-up counter it has — the Good Shelf, the
Pocket Pals machine, the transit kiosk — and it is the
garden centre's named home since v0.11.0b
(`Garden.SEED_SUPPLIER`). The general-goods counter is
the Mall snack shop's counter: `Stand.STOCK` has named
"the Mall snack shop" as the town supplier of cheesy
poofs and snack cakes all along, and this is that shop
with a till you can reach. Like the Mall's other
counters, neither has a keeper gate — the location is
the counter (the `Wardrobe.buy` vocabulary), and a
sale settles only at its own counter; anywhere else is
"notsold", suite-pinned.

**Seed packets are real inventory.** Buying at the
rack stocks a packet at the crop's EXACT stated price
(pumpkin 3 🪙, the rest 2 🪙 — derived from
`Garden.CROPS`, never re-typed, so the rack, the pack
and the planting order can never drift apart).
`Garden.plant` now has two doors at one price: a
packet held in the pack is spent and plants free;
without one, planting still orders from the same rack
at the same price, exactly as it has since v0.11.0b —
stated plainly in the rack's lines, the planting
picker and the garden page, so the counter reads as
the in-person option, never a tax. A refused planting
(out of window, occupied bed) spends neither packet
nor coin. Packets stack per crop and never expire
(forty rollovers, suite-pinned). They ride the pack
under their own ids with deliberately NO ItemDef —
the bus pass's pattern (`Walk.PASS_ITEM`) — which
keeps them out of the gift flow, the inventory's item
list, the errand fetch pool, the litter tables and
the stand's stock defs by construction: a packet is
good for planting, only. The garden page gains a pack
line when (and only when) packets are held, so a
pre-0.12.0b save's page reads exactly as before.

**The general-goods counter.** The snack shop sells
the stand list's four pantry staples — lemonade, hot
chocolate, cheesy poof, snack cake — into the real
pantry, at the shops' own prices read off
`Stand.STOCK` (1 / 1 / 2 / 2 🪙, the same numbers
Liane's booth charges in fair week). The eat/gift/
dinner disciplines are untouched: a counter-bought
lemonade is a pantry lemonade, servable at dinners
and sleepovers and giftable like any other.

**The loop proof, suite-pinned.** Prices are derived,
not set, so no spread exists anywhere: counter price
== stand restock price == booth price for every
staple, seed price == planting price for every crop.
The crate's walls stand behaviorally proven on a
stocked pantry: `Stand.order` still fills the crate
from coins alone and provably never touches a pantry
count; the garden door still refuses a shop staple
("provenance or nothing"); a seed packet can't enter
the crate by either door. Nothing in town buys from
the player, and `Shop` itself has no sell verb and
awards no coins — both tills are one-way sinks.

**The books.** Every purchase is a real coin sink,
named in the tuning ledger beside the others: seed
money feeds the garden's one lifetime counter
(`gardenSeedSpent` — packets and planting orders
alike, so the garden page's "seed spent so far" tells
the truth across both doors; planting FROM a packet
adds nothing, the spend already happened at the
rack), and snack money accrues in `shopGoodsSpent`,
printed on the counter's dialog as the till's book.
No daily or per-visit cap — the purse is the cap, and
the counters sell one unit a tap, like the fair booth.

Saves are additive-only: packets ride the inventory
map, the till's book is one `life` key, and no save
key changes meaning — a pre-0.12.0b save holds no
packets and plants by supplier exactly as before.
Fan sheets untouched.

### v0.12.0a (versionCode 88) — The paper grows up (Queue #12 item 1)

Queue #12 opens by closing the town newspaper's two
recorded gaps from v0.11.0a. No new module: `Newspaper`
grows the tiers and the delivery, `Stand` and `Heroes`
each lend the paper two small public reads, `Phone`
gains a trigger and a harvest beat, and Your House gains
a fixture. Zero new GameState fields, zero new Prefs
keys — the porch and the summary ride the paper's own
`life`/`lifeText` keys, plus one stamp of the paper's
own (`paperSummaryDue`).

**The folded tiers land.** The headline ladder, stated
in full for the first time since the paper folded three
tiers away, now runs: (1) CASE CLOSED; (2) THE FAIR;
(3) SEASON FINALE; (4) OPENING NIGHT; (5) THE VOTE;
(6) CAPER CLOSE; (7) STAND REVIEW; (8) THE RIVALRY;
(9) THE SKY; (10) A QUIET WEEK IN SOUTH PARK. The
existing tiers keep their relative order exactly.
Each new tier reads only its own record:

- **SEASON FINALE** fires for the finale instance whose
  wrap morning falls in the week. The wrap morning is
  derived, never stamped: the day after the season's
  last day, by the same calendar `Finales` resolves
  instances on (`seasonEndMonthDay` + the shared
  ordinal math). The town line names the def's own
  place and quotes the standing mark — but only while
  the mark still stands for that instance; a later
  year's mark is a later year's news, and the suite
  proves the quote drops while the headline stands.
- **CAPER CLOSE** fires on `Heroes`' own closing-day
  stamp. 🔴 PUBLIC SHAPE ONLY, the case tier's wall
  applied to the caper: the town line repeats the Hero
  Log's own public ending phrase and nothing else —
  never who, never what, never where it hid. The
  phrase itself now lives in one place,
  `Heroes.caperPublicEnding`, which the Hero Log's
  page and the paper both quote, so the two surfaces
  agree word for word by construction; the suite pins
  the agreement for all four endings and scans the
  headline, the caper line, whole editions printing a
  case close and a caper close together, and the
  module's own source against the caper's sealed terms.
- **STAND REVIEW** fires when the standing review's
  own recorded day falls in the week — `Stand` keeps
  only the latest review, so that is the review the
  paper can cite, in the week its day names. The
  headline carries the books' verdict and the town
  line adds the reviewer, both from the record
  (`Stand.reviewVerdict` / `reviewDay` / `reviewBy`).

Two ladder pairs can never share a week in the lived
calendar — the fair's own shift rule exists to dodge
the summer finale's window, and play openings never
fall in a finale wrap week — so fair-over-finale and
finale-over-opening-night are pinned in the composed
ladder's source order, and every pair the calendar
*can* seat is fabricated and eliminated tier by tier
in the suite: case > finale > vote off one state, and
case > vote > caper > review > rivalry > sky > quiet
off another. The number audit extends over the new
lines: an edition printing all three new town lines
alongside a 137-of-201 vote keeps every integer
traceable to a record — the new lines print no number
of their own.

**Delivery.** The ticker announcement stays; two
carriers join it. (1) *The porch copy:* while an
edition stands, a folded Gazette sits on the step
beside your door at Your House — a `paper` fixture in
the garden's pattern, drawn only while
`Newspaper.porchLines` reads a standing edition. The
tap opens the frozen edition whole, the same lines
the Journal holds; the next edition replaces the
copy, and the suite pins the porch and the Journal
to the same lines, before the first edition (no
copy, honestly) and across two publications. (2)
*The phone summary:* publication stamps a one-bit
debt (`paperSummaryDue`), and `Phone`'s harvest pays
it with ONE text on the publication morning — the
headline verbatim, in a subscriber's voice, spent
from the shared `MAX_NEW_PER_DAY` budget like every
derived beat. The sender rotates through the whole
cast by published week (`Newspaper.summarySender`);
Kenny never carries it, because the phone's own
render muffles Kenny and a muffled headline is no
headline. The stamp clears only when the text
actually lands — the fair announcement's discipline —
so a crowded morning defers the summary whole, and
the suite's lived drive lands exactly one summary
per edition with no debt left standing. The text
rides the subscriber's own thread: no new thread, no
new sender (the admission set stays closed — a text
from "The Gazette" decodes as malformed and drops),
and the cap math is re-proven, not moved:
(98 cast + 2 parents + 3 group rooms) × 2 = 206 ≤
`MAX_TEXTS` 220. The lines may react to the headline
but state no fact the edition doesn't — the headline
itself and the porch copy are the whole record.

Pre-existing saves begin with the next lived week, as
they always have: the new tiers simply have no
records to read until a finale wraps, a caper closes
or a review posts in the save's own life, and a save
whose latest edition predates the summary carries no
debt stamp — it is never texted about an old edition
after the fact (pinned). The manifest moves to
88 / 0.12.0a and the tree stays at 98 Kotlin files.

### v0.11.0g (versionCode 87) — Polish #11: the Queue #11 audit (Queue #11 item 7, the closer)

No new features. The audit build, in the polish tradition:
every Queue #11 surface re-proven on today's tree, and the
gaps the queue's builds recorded collected in one place.

- **The 400-day soak.** One save, one lived year, every
  Queue #11 surface in it: the paper publishing at the
  week's turn across the year with its headline archive at
  cap and its tiers drawn only from the ladder's own; the
  garden planted in its windows, rain- and hand-watered,
  picked across the year, a tended pumpkin kept as the
  fair's porch specimen off its growth record; the 2027
  fair wrapped with the produce weigh-in judged to a full
  table; the stand's grown column balancing (grown = sold
  + spoiled + on hand) after a year of crate traffic; full
  sleepovers landed on the year's Fridays with refusals
  named as they occurred; the 2027 carnival wrapped with
  the sculpture judged, sessions built and a real catch
  funneled through the ice on fishing day; and the cases
  watched daily — never two open at once, the ninth never
  over the eighth. Beside the soak, fabricated proofs: a
  blizzard taking a tender sunflower (deadWhy "frost")
  while the hardy potato shrugs it off, and winter's turn
  taking a ripe tender corn left standing while the
  fall-planted potato stands through; the paper's ladder
  fabricated whole (all seven tiers reachable, the case
  tier firing on the ninth case's own stamp, the case
  edition's TOWN NEWS naming no cast member); and the
  nine-case lifetime re-driven unattended to a clean
  close.
- **The 98-cast census, stressed.** The phone at its
  honest worst case: all 103 threads (98 cast + Mom +
  Dad + the three group rooms) holding two texts each —
  206 against `MAX_TEXTS` 220 — with `enforceCaps`
  shedding NOTHING, and a ballooned thread past the worst
  case trimming by its stated policy (oldest read first;
  every unread survives). The crowds at full cast: seven
  rows, all 98 figures placed inside the margins, the
  cluster partition covering the roster exactly once.
  Dinner rotations and the crew room proven with wave-6
  citizens in them. The wave-6 lorebook audit re-verified
  LIVE against the file on disk — 1,537,338 bytes, the
  joint Terrance & Phillip entry at its 186 chars with no
  standalone entry for either, the admitted sample at
  their exact lengths, all 98 cast resolving their lore.
- **Contention, re-pinned at the code's seats.** The
  TalkGate ladder stands at eleven rails — Queue #11
  added none: sleepovers ride DINNER through
  `Sleepovers.talkStepFor`, and the paper holds no rail
  and appears in neither file's source. The agenda stack
  re-read in `Engine.effectiveAgendaBlock`'s own order,
  fourteen claims, the carnival seated with the fair and
  the sleepover after the play and before the stand.
  Cadence pairs re-swept in ONE lived time space: 104
  Fridays against the family table and the patrols (the
  family's own `dinnerTonight` never fires on a Friday),
  the carnival week against finales, holidays and the
  fair across 2026–2065, and the garden's four 15-minute
  beats against the day clock (a full care round is
  ninety minutes).
- **Budgets and censuses.** The audio budget lines hold
  their v0.4.0e pins (named 0.947, double 0.978, bark
  0.947, every bed inside its allocation); the audio
  census is unchanged (21 effects, 7 blip families, 4
  moods, 6 beds); the bark census stands at 98 signatures,
  all distinct; the house catalogue holds 17 decor pieces
  with the fair's ribbon display still derived from a
  blue — the fourth contest feeds the same hook.
- **The tuning ledger, completed.** Every Queue #11 dial
  is now named in the ledger and pinned by the suite: the
  paper's caps, the garden's growth and weed dials and
  its four beat constants (named in the garden entry by
  this audit), the fair's produce dials (`PRODUCE_DAY` /
  `PRODUCE_FIELD`, added to the fair entry), `MAX_TEXTS`
  220 with its 98-cast derivation, the carnival dials,
  arc #9's dials — and the sleepovers entry itself, which
  v0.11.0c shipped without and this audit adds.
- **Veteran save X.** Veteran IX's saturated body carried
  whole, with Queue #11's state laid over it by real
  drives: a garden season grown and picked on the
  veteran's own state (the show pumpkin kept, the stand
  set up and its crate stocked from the beds against
  provenance), eight hosted sleepovers at the record's
  cap, a carnival week lived in a scratch town and
  transplanted by its own record, the paper's archive at
  its caps with the latest edition composed from the
  saturated state itself (it headlines the ninth case's
  closing), all nine cases closed with their files full,
  and a 196-text inbox. The whole save round-trips at
  569,660 serialized chars — past veteran IX's 536,885,
  with the 640K ceiling's headroom intact.
- **A gap closed.** The stand-shift sleepover refusal —
  implemented in v0.11.0c but never suite-fabricated — is
  now exercised end to end: a stand opened, a crew kid
  assigned to the Friday's shift that morning, and the
  evening's plan refusing them by name with the stand's
  reason.
- **Honest gaps, consolidated.** Carried forward: the
  paper's headline ladder stays folded (no season-finale,
  stand-review or caper-close tier, and those events get
  no TOWN NEWS lines either); the paper still has no
  phone or porch edition — publication is announced on
  the ticker and read in the Journal. Still open, with
  reason: garden seeds are bought through the Mall garden
  centre on the Stand's supplier pattern because no
  walk-to-a-counter general-goods buy surface exists in
  the game yet. Carried forward: sleepwear is not
  rendered — sleepover photos wear normal clothes. And
  the standing gap behind all of it: no on-device
  rendering proof exists on this machine — the builder's
  eyes are the suite, and the player's are the only real
  QA. Production-code changes in this build: **none** —
  the audit found nothing to fix, and says so rather
  than inventing one.

### v0.11.0f (versionCode 86) — Mystery #9: One More Than Counted (Queue #11 item 6)

The ninth case is a ninth shape: this one is an audit.
One new pure module (`Mystery9.kt`), additive save state
in the established arc pattern (pre-0.11.0f saves
simply queue the case behind the eighth), the manifest
at 86 / 0.11.0f, and src grows 97 → 98 Kotlin files.
The lorebook rides inside unchanged.

- **The premise.** Nothing was taken. In stock-take
  week, by the Mayor's order, the town counts every
  public counted thing against its book — the town
  keeps real books now (a paper with an archive, a
  stand with a ledger, a yearbook, fair records) — and
  four counts come back wrong, every one by exactly
  one, every one by one MORE than the record holds: a
  shelf of cups against the school's engraved wins
  list, the Bijou's seats against the seating chart on
  file, the bank's box wall against the rental ledger,
  the mayors' gallery against the mayors' roll. The
  town names its culprit at cape speed — the Adder.
  The Mayor wants the audit closed, with an
  explanation, a name, or a filing, by the Reckoning.
- **The spine is an audit ledger.** The Journal board
  draws the four count-sites as rows of a ledger: the
  counted number and the book number on each row, the
  **discrepancy derived** from the two on every read,
  never stored. For each site you commit a
  **reconciliation call**: THE COUNT IS WRONG (the
  counters miscounted; the room matches its book),
  THE RECORD IS WRONG (the book under-wrote it; the
  thing was always honestly there), or SOMEONE ADDED
  ONE (a real extra thing was placed there). Each
  site has exactly one piece of evidence that settles
  it, and **verdicts are derived** from the recorded
  call and the live evidence on every read, never
  stored: **proven**, **refuted** (the call stands
  struck; it can be struck and called again), or
  **committed**, standing untested. An extra can be
  born in any of a count's three moving parts — the
  things, the book, or the counting — and finding out
  which, four times, is the case.
- **Twelve evidence pieces** (5 witness / 5 look / 2
  participation), three tagged to each site, in the
  arcs' mixed-verb pattern — keepers to hear, things
  to find, counts and archives to work with your own
  hands. Two pieces are **unreadable when found** —
  inert, honestly described as not-yet-making-sense —
  and become spendable only after the chapter that
  unlocks their context. An inert piece settles
  nothing, proves nothing, buys nothing.
- **The difficulty is that every number is honest.**
  The case carries a red herring with a mundane engine
  (a first count taken at speed, by clicker, through a
  parka, that the town nearly built its theory on), at
  least one fully innocent reading of the town's
  apparent answer is evidence-supported, and its human
  sting is a face in a gallery that the book — not the
  wall — failed to keep. Every keeper in the case kept
  their book the way they understood it. Every counter
  counted everything they saw. The investigation is
  learning to hold both.
- **The finale is the Reckoning.** The audit's public
  closing, in the council chamber: the four books on
  the table, the results board brought in, a labeled
  space in the program — THE COUNT, CLOSED. Four
  calls: **read the audit aloud** (gated on the
  ledger's final derived state — the trophy case and
  the gallery standing proven, every site called, no
  call standing refuted, at least three sites proven),
  **name the adder** (the town's own answer; gated on
  any one site standing proven), **let the books
  stand**, or **walk away**. A closing nobody plays
  lands cold. Every landing persists in the arcs'
  style — marks, bonds, standing lines, ambient
  callbacks, an objection that stays in the record in
  the hard landings, a delayed tail on one landing —
  and, as ever, the game never admits a call was wrong.
- The case queues ten days after the eighth case
  retires, defers past holidays like every case before
  it, registers its climax in the standing deference
  lists (the Bijou's doors, the classroom, the walk
  home, the season finales), the paper can headline
  its closing day like any case's, and the nine-case
  lifetime holds in the suite's long drive.

### v0.11.0e (versionCode 85) — The winter carnival (Queue #11 item 5)

- **The county fair's winter counterpart: one week in deep
  winter at Stark's Pond** — the ice takes the fishing
  holes, the shore takes a row of snow sculptures, lantern
  posts line the bank, and the whole town gathers on the
  frozen pond. The week anchors on the **third Saturday of
  January** (7 days, Saturday to Friday) and — unlike the
  fair — carries **no shift rule**, because it never needs
  one: the suite sweeps forty years of real calendar dates
  (2026–2065) in **ONE lived ordinal space** and proves the
  carnival week disjoint from all four season-finale
  windows (the winter finale keeps the last week of
  February, a full month clear), from every holiday window
  on the town's books (Christmas, New Year, Valentine's
  thread between the carnival and the finale without
  touching either), and from the summer fair's week. The
  polish-#10 lesson — two rhythms in different day-spaces
  double-booked a Saturday for a month of builds — is the
  reason the sweep compares everything in the one space
  the town actually lives.
- **🎣 Ice fishing (day 3, judged the next morning)** on the
  **real FISH_TABLE** under the fair fishing contest's
  exact discipline: your pond catches that day funnel
  through `applyCatch` (best of rank, then size — a boot
  is not a fish, through ice either); the field — Jimbo,
  the rival when crowned, the seeded draw — each make ONE
  cast with their authored luck; places pay the fair's own
  capsule table (6/4/2). When both you and the rival hold
  entries the result lands on the 🆚 ledger through
  `Rival.recordResult` **exactly once** — and never when a
  rival fishing duel is already armed: the duel keeps the
  ledger, the carnival keeps its page, pinned both ways.
- **⛄ The snow-sculpture contest, judged like the
  yearbook.** You build YOUR piece in played sessions at
  the pond (build days 0–2, at most 2 a day, 30 real
  minutes each, spent tick by tick through
  `Engine.advanceTime` from the pond hub). Every session is
  **filed with the weather it was built under**, and the
  stated formula reads only that record: **12 a session,
  +10 for each further day you came back, and per session
  +6 in falling snow (it packs), +2 under a grey sky, −7
  in sun (it melts)** — a perfect build (six sessions,
  three days, all snow) scores exactly 128. The town's
  pieces are seeded 20–129 onto the same scale, so a fully
  worked piece can top the field and a one-session piece
  can't. The judge is the yearbook masthead's own rotation
  (`Yearbook.judgeFor(year, 4)`); a winning note cites only
  facts the filed record proves — sessions, days, snowy
  sessions, the score — and a town winner's note cites
  contest facts only. Judging lands the morning of day 3.
- **☕ Hot drinks: the stand synergy, at real prices.**
  Carnival foot traffic is real presence (the gathering
  claim puts the town physically at the pond), so a stand
  pitched at Stark's Pond — already one of the permit's
  four pitches — sells to the crowd through Stand's
  ordinary tick, crate and books untouched. One stated
  synergy on top: during carnival week, **hot chocolate at
  a pond pitch buys at `COCOA_RUSH` ×1.6** the ordinary
  purchase chance (Stand's `purchaseChance` reads the rule
  from Carnival; pinned exact against a July control, a
  lemonade control, and a park-pitch control). No carnival
  currency, no carnival prices, no conjured customers —
  and the Journal page states the synergy in the open.
- **Carnival life, in the fair's pattern.** Hours 10:00–
  20:00, closing night to 21:30 for the **lantern skate**;
  the agenda claim seats in Engine's stack **immediately
  after the fair's** and self-guards the same way (dinners,
  jobs, screenings, ceremonies, finales, the fair itself,
  live case work — and your crew's hook still takes a
  member first). Opening morning offers the played beat:
  **drilling the first ice hole with Jimbo** (his +2, once
  a year, from his person menu at the pond). Announced
  three days out on the ticker, the pond-hut poster, and a
  **Mayor's phone text** on her existing thread — the
  inbox math is the fair's: (98 cast + 2 parents + 3 group
  rooms) × 2 = 206 ≤ `MAX_TEXTS` 220, **the dial does not
  move**. Dressing tokens (`carn-banner`, `carn-holes` —
  three broad ellipses, wide apart, never a clustered
  pattern — `carn-lanterns`, `carn-sculptures`) ride
  TownInfo into a new Depth BACKGROUND layer drawn like
  the fair's; the morning after closing the map empties,
  which IS the strike.
- **Records with no new save shape.** One record per year
  serialized into **lifeText** (the Stand crate's
  discipline) — **zero new GameState fields, zero new
  Prefs keys**; the build record, contest guards and
  attendance stamps ride life/lifeText beside it. The
  Journal's **🎪 WINTER CARNIVAL** page shows the newest
  carnival whole and elders as one lines; a slept-through
  week still judges both contests off their records and
  wraps **honestly missed**. **Honest gaps:** the carnival
  mints no feats and no house decor (the fair's ribbon
  display stays the fair's); the newspaper carries no
  carnival headline tier yet (the Queue #11 item 1 gap
  list already owns that pattern); sculpture exists only
  as its filed record and the shore-row dressing — pieces
  are not rendered individually in the scene.

### v0.11.0d (versionCode 84) — Cast Wave 6: the evidence audit (Queue #11 item 4)

- **Twenty-eight more named townsfolk (cast 70 → 98: 42 kids,
  56 adults),** admitted by evidence, not appetite. The lorebook
  (3,322 entries; 2,225 of them characters, 2,155 of those
  outside the cast) was re-audited entry by entry against the
  roster at the wave-5 standard — a standalone entry with the
  substance to author the full treatment honestly — under the
  floor the earlier waves actually practiced: dense cast
  relations, or a relation plus plot substance across two or
  more episodes, at or above the weakest admitted precedents
  (Kevin McCormick 130, Mr. Testaburger 163). The embedded
  lorebook stays **byte-identical**; only the generator's cast
  table grew (cast matched 98/98, locations 27/27, the alias
  table at 125 entries). The audit is pinned live in the suite:
  every admit at its exact lorebook length, every near-miss
  below at its evidence, and the non-cast census in bands — a
  future lorebook update that changes any of it fails a pin
  and forces the re-audit.
- **The kids (10).** Pip Pirrip (339) — the show's original
  British import, in on the Chef precedent for canonical
  deaths (he was killed in "201"); Dougie O'Connell (352),
  admitted as himself — his Chaos-henchman persona was already
  in the book as a separate, thinner entry; Jason White (857,
  the longest entry admitted this wave; the same precedent
  covers his S23 death, and his own entry records the
  background appearances after); Terrance Mephesto (325) —
  Dr. Mephesto's other son, the four-assed menagerie heir, NOT
  the Canadian comedian (see the pass below); Jenny Simons
  (282); Flora Larsen (226); Filmore Anderson (161); Quaid
  (193); Mimsy (196) — passed in wave 5 as Nathan's lackey,
  admitted now because the completed cast made the evidence
  read differently: with Nathan in town he is the missing
  half of an admitted act, and his own entry carries the
  duo's episode record; and Tricia Tucker
  (160), Craig's little sister, completing the Tuckers.
- **The adults (18) — households first.** Linda Black (133)
  completes the Blacks: passed in wave 5, admitted now on the
  distinction the audit states plainly — Tolkien is a
  main-group kid, and his was the last main-group household
  with a parent missing; Harriet (278) & Mr. Biggle (225)
  complete Henrietta's house; Thomas Turner (292) & Nancy
  (214) complete Heidi's; Mr. Stevens (154) & Mrs. Stevens
  (144) complete Bebe's; Mr. Stoley (177) & Mrs. Stoley (179)
  complete Kevin Stoley's; Clark Malkinson (133) completes
  Scott's — admitted on his named presence in the household's
  own episode, not on the relation-stub pattern that passed
  the McArthurs and the Grays; Maggie Yates (221) completes
  the sergeant's house. Then the town's working chairs: Mr.
  Slave (191); Mitch Murphy (214), completing the police pair
  the mystery systems already lean on; Bob Johnson (297), the
  mayor's aide, taking the City Hall staff chair; Tom
  Pusslicker (498), the news anchor; Dr. Doctor (346), the
  town's doctor; Diane Choksondik (300), the fourth-grade
  teacher of the boys' S6 year — canonical death and all, on
  the same Chef precedent as Pip and Jason; and Strong Woman
  (328), the elementary school's vice principal.
- **Terrance & Phillip: passed over a fourth time, on the
  same evidence.** The lorebook still holds ONE joint entry
  of 186 chars and no standalone entry for either man;
  "Terrance" in the alias table is Terrance Mephesto, who is
  now cast on his own entry. The pin reads the lorebook file
  itself, so the verdict re-opens the day the data changes.
- **The chair rule, new this wave and applied twice:** where
  one narrative chair has had several occupants across the
  show's eras, the cast takes the defining occupant and the
  rest are near-misses. The anchor chair went to Pusslicker
  over Tom Thompson (226), Tom the newsman (269) and Tammy
  Thompson (230); the doctor's chair to Dr. Doctor over Dr.
  Gauche (365). Documented near-misses, all pinned: Kevin
  Mephesto (138 — a garbled two-sentence stub for a silent lab
  figure; there is no voice there to author); Betsy Donovan
  (420 — her admission would contradict the game's own record:
  Roger stands in the cast as Clyde's widowed father after
  "Reverse Cowgirl", and her entry itself flags her later
  sightings as likely continuity error); Casey Miller (447)
  and Heather Conduct (683) — length without substance, each a
  single episode's plot summary (Sophie Gray, admitted in
  wave 5, is the contrast: her entry states she continues);
  Wing Han Tsang (137 — Lu Kim's wife on one episode's
  substance; the Linda Black distinction above is the line);
  the mayor's other aide Janson (233, purely relational —
  City Hall fields McDaniels, Bob Johnson and Mitch Murphy
  instead); and the long tail of relation stubs, one-gag
  fixtures, background classmates, personas of cast members
  (the Coon, Mysterion, Professor Chaos and the rest are
  already in town, wearing their owners), groups, and
  non-resident visitors — 2,127 character entries remain
  outside the cast, the eight fattest of them named in the
  suite's census pin, every one of them short of the standard
  on substance, residency, or the game's own record.
- **Full wave standard, no exceptions** — the checklist per
  newcomer: a newly measured figure (kids in the big-head
  band, adults head ≈ ⅓; all 98 silhouettes distinct — Linda
  Black's first measure collided with Laura Tucker's and was
  re-measured), 7-block agenda + home, faction placement (the
  boys' crew, Craig's gang and the girls' circle all gain;
  parents and school staff likewise), 9 collision lines each
  (town floor 630 → **882**), 18 daily topics each (1,310 →
  **1,814**), a compact voice card (all 98 globally distinct),
  friend-tier greetings, response banks, sleepy lines, pair
  scripts (41 new pairs, 82 scripts — town totals 71 → **112
  pairs**, 144 → **226 scripts**, every scripted pair carrying
  a relations note), a distinct bark (98 signatures, pairwise
  distinct, every hello distinct), a SoundGen blip family,
  school skill and showdown dodge for the kids, and a fan
  reference sheet per Lucky's format (all 70 prior sheets
  byte-identical; contact sheet regenerated at 98;
  tools/params.json synced to exactly 98 characters).
- **Scaling, checked at 98:** crowds — `rowsFor` gains sixth
  and seventh rows (above 70 cast, then above 84), measured
  against the real tables: at 98, five rows pack 22 to a row
  and spill the margins, six still spill, seven pack 14–16
  with the worst span inside the world; the phone cap rises
  `MAX_TEXTS` 160 → **220** — the worst case now reads (98 cast
  + 2 parents + 3 group rooms) × 2 = 206 > 160, so the dial
  was forced, and 220 restores headroom as a whole number of
  threads; the rival pool gains its **ninth seat — Terrance
  Mephesto, appended last**, the authored eight keeping their
  seats and order so every recorded crown in an old save is
  untouched (his stage nerve equals his rival nerve, 7, by
  pin); dinners gain **one reseat only** — the Black table,
  which exists among the 27 locations, now seats Tolkien,
  Steve and Linda Black; the other 27 newcomers anchor by the
  Tucker precedent (Elementary, City Hall, Main Street,
  Tolkien House) and hold no table; the Bijou's NEVER_ATTENDS
  stays {Towelie, Crabtree} — all 28 are ordinary townsfolk,
  star-castable on both paths. The wave's data lives in one
  new file, `CastDataWave6.kt` — CastData's own class
  initializer hit the JVM's 64KB method limit with the wave
  aboard, so the block moved out whole rather than shaving
  content to fit (src 95 → **96 Kotlin files**). **Saves
  additive-only:** no new save keys, no key changes meaning;
  pre-0.11.0d saves load whole and meet the newcomers
  naturally.

### v0.11.0c (versionCode 83) — Sleepovers (Queue #11 item 3)

Queue #11's third item brings the crew home for the
night. One new module (`Sleepovers.kt`, pure Kotlin;
the tree is at 95 Kotlin files), hooks in
`Engine` (the agenda stack, the tick, the rollover),
`Crew` (the occupied oracle learns the claim), the
Talk gate (the beats ride the existing DINNER rail —
the ladder gains no rung), `House` (the morning
note), the Journal (🛌 SLEEPOVERS), one new played
view (`PillowFightView`), and zero new GameState
fields or Prefs keys — all sleepover state rides the
`life`/`lifeText` maps, so pre-0.11.0c saves simply
never hosted one.

**The night.** Sleepovers are a Friday-night
institution — at most one a week by construction.
The cadence was proven in ONE time space against
every rival claim (the polish-#10 lesson): patrol
nights are Wednesday/Saturday, the family table runs
absDay % 7 ∈ {0,2,4} and a Friday is always
absDay % 7 == 1, so no two of these evenings can
ever collide; the suite sweeps the calendar proving
it. Propose from your bed or a crew kid's person
menu. Guests are crew members who are KIDS (kids
and young teens) — grown-ups don't do sleepovers,
stated plainly in the plan, and an adult crew member
is simply never offered (their membership is
untouched). Every guest runs the full occupied
discipline (`Crew.busyReason` read on the evening):
a guest claimed by the Bijou, a shift, a ceremony, a
finale, a case, another table, your family's own
guest list or your stand's roster doesn't come — and
the plan NAMES them and the reason; nobody is
silently swapped. Below two free guests the plan
proposes a different Friday instead of running
empty. A finale window, a holiday beat, a case
coming to a head tonight, opening night, or fair
week cancels the night outright, with the reason
stated.

**The evening** (18:00–22:00, in the dinners'
discipline — four beats, an agenda claim that seats
after the play and yields to every bigger claim, and
`Crew.busyReason` reporting "sleepover" so the whole
town's occupied discipline sees the night). Guests
are claimed to Your House; the living room wears its
sleeping bags as temporary dressing in the decor
token pattern, struck in the morning. (1) SNACKS —
the spread is the REAL pantry: up to two stocked
items, consumed and named; a bare cupboard is stated
honestly, and when the family is home Mom covers
from the parental reserves, her treat, consuming
nothing. (2) THE MOVIE — if a Bijou screening stands
tonight, the TV runs the Bijou's own feature (its
real title, genre and stars); otherwise a home
movie: one of YOUR album photos, described from its
record alone — place, date, who was in frame. (3)
THE PILLOW FIGHT — played, not narrated: three
swings on the showdown bar against the guest you
tapped, judged against their own dodge (the dunk
tank's honest machine); two of three takes the
couch. (4) LIGHTS OUT at ten — sleeping
arrangements and any guest the evening never
actually saw resolve in the telling; the record only
ever names guests the tick saw standing in your
house.

**The morning.** The rollover wraps the night:
friendship once per guest who came, at the cheap
stated rates (+2 a full night, +1 for two or three
beats, nothing for a night that never got going), a
"sleepover" topic planted in every guest (the
Threads follow-up banks carry it), and the night
written to the record — the Journal's 🛌 SLEEPOVERS
page keeps the last 8 nights: who came, the snacks,
the movie, the pillow-fight result, and whether a
group photo from the night is in the album. If the
family is home, Mom makes pancakes — her treat,
consuming no pantry item, stated as the call. The
photo beat needs no machinery of its own: the
guests are physically at your house all evening, so
the photo system's own composition seats them in
the frame. Photos render normal clothes — there is
no sleepwear pass in this build, and the record
doesn't pretend otherwise.

### v0.11.0b (versionCode 82) — Your garden (Queue #11 item 2)

Queue #11's second item puts the town's weather to
work at home. One new module (`Garden.kt`, pure
Kotlin; the tree is at 94 Kotlin files), one hook in
`Engine.onNewDay` (after the stand's wrap, before the
paper — the beds and the paper's sky log read the
same ended day, so they can never disagree about a
night), one Journal section (🌱 YOUR GARDEN), a
fixture in the yard at Your House, five new pantry
items, and zero new GameState fields or Prefs keys —
all garden state rides the `life`/`lifeText` maps, so
pre-0.11.0b saves simply have bare beds.

**The plot.** Four beds in the yard, drawn in the
established fixture pattern (soil rows, the crop's
glyph by growth stage, weeds as strokes, a sparkle
when ripe, a wilt when the sky killed it). Five
crops, each with a stated planting window and growth
span: pumpkin (Mar–May, 55 days, tender), corn
(Apr–Jul, 40, tender), sunflower (Mar–May, 35,
tender), tomato (May–Jun, 30, tender), and potato
(Mar–Apr & Aug–Sep, 45, HARDY — the forgiving
Colorado staple, the one crop that shrugs off the
blizzard and the winter). Seeds are bought at
planting for real coins from a stated supplier at
stated prices (the Mall garden centre's seed rack,
2–3 🪙 a packet — the stand's supplier-stock pattern,
pinned like other stock).

**Growth reads the real sky, only.** Each bed's
counters are stored plainly and advanced one honest
day at a time by the rollover, off the ENDED day's
weather: rain waters for free; the player's WATER
beat (15 minutes on the real clock, like the other
home beats) waters on a dry day; an unwatered day
stalls growth — no loss, no progress; a sunny summer
day scorches an unwatered bed; a blizzard kills a
tender crop where it stands; and the first rollover
into winter takes every tender bed left standing,
ripe or not. Weeds creep a level a week since the
bed was last cleared (the CLEAR beat, 15 minutes),
capped at 3, each level costing a yield unit. The
garden never invents weather: every growth claim on
the 🌱 YOUR GARDEN page (crop, planted day, water
status, growth read, days to ripe, the harvest
record) traces to a day the sky actually did it, and
nothing dies silently — the bed record says why.

**The harvest is real food.** A ripe bed picks into
REAL items in the player's pantry (pumpkin, corn,
sunflower, tomato, potato — gift-tier, gift values
read like other foods, eat/gift discipline
unchanged), and the garden feeds the town's other
systems through their own seams: hosted dinners
serve a homegrown dish when the pantry holds produce
(produce joins `Dinners.PANTRY_ITEMS`, last, so the
old spread's habits are unchanged; the serve text
states the fact), and family dinners cook the
homegrown dish too — named at the table, one real
item served and consumed, its name kept for the
evening. The stand's crate can stock produce, but
ONLY what the garden provably grew: `Garden` keeps a
provenance counter, `Stand.stockFromGarden` moves
real items pack → crate against it (a tomato found
in a dumpster is lunch, never inventory), prices sit
in the stand's existing band against stated produce
REFERENCE prices (no shop sells produce —
`Stand.order` refuses it plainly), and the books gain
a grown column keeping the identity per item:
grown == sold + spoiled + on hand. Produce spoils on
its stated shelf lives into the same books.

**The fair tie-in.** The season's biggest harvested
pumpkin is kept whole on the porch as the entry for a
FOURTH fair contest, the 🎃 produce weigh-in
(`PRODUCE_DAY` 4, the fair's quiet day; judged on
the dunk morning, or at the wrap if slept through).
Its weight comes off its growth record alone —
(8 + 0.40 × days grown + 0.30 × days watered) ×
(1 − 0.10 × weed level), a perfect pumpkin ~47 lb
against the seeded town field's 10–44 — and it places
under the fair's ordinary rules: same ribbons, same
prize coins, counted in `blues()`. `FairRecord` gains
the weigh-in additively (pre-0.11.0b records read
with an empty table), the fair page and the year
index name the fourth winner, the paper's fair line
lists a produce placing, and Fair Sweep now honestly
reads "three blue ribbons at ONE fair — any three of
the four" (its threshold never moved). A produce
ribbon mints NO Pocket Pal card — the 24-card set
stays pinned, as ever.

**Economy, named.** The suite simulates a perfect
season — every bed worked Mar 1–Sep 30, watered and
weeded daily, the whole harvest valued at the stand
band's CEILING (3× reference prices) — and it
grosses under one paper-route season (390 🪙 by
Jobs' tables), netting under 200 🪙 after seed
costs. Shops don't buy produce at all; the only
seller of it is your own stand, inside the band. The
garden is a way to feed people, not a wage.

**Verification:** the v0.11.0a baseline re-confirmed
first (92,620 checks, 0 failures on the untouched
tree); the suite extends by a `v0.11.0b` section —
planting windows and prices, growth against scripted
weather (rain / hand-watering / dry stall / scorch /
blizzard / winter, through the real rollover),
weeds and yield, harvest into real items, both
dinner integrations staged and served, the stand's
provenance refusals and books identity (including
spoilage), the weigh-in judged from the record
(including the slept-through wrap), the season-long
economy simulation, legacy bare-bed saves, and the
save round-trips. Manifest pins and file counts
moved forward honestly (82 / 0.11.0b, 94 Kotlin
files).

### v0.11.0a (versionCode 81) — The town newspaper (Queue #11 item 1)

Queue #11 opens with the town in print. One new module
(`Newspaper.kt`, pure Kotlin), one hook in
`Engine.onNewDay` (after the stand's wrap, before
Mystery6 and the phone), one new Journal section (📰
THE PAPER, beside the yearbook shelf), zero new
GameState fields and zero new Prefs keys — every fact
the paper holds rides the `life`/`lifeText` maps, so
old saves simply have no paper yet and the page
explains itself.

The paper keeps its own weather log: each rollover
appends the ENDED day (absDay − 1) with the weather
that day actually had — at the module-rollover point
`weatherId` is still the ended day's, because the new
day's sky only drifts later, in `tickWeather` — and
the night's special from `SkyState.dateSpecial` for
the ended day's own date and season. The log caps at
`SKY_LOG_CAP` 14 nights. At a week's turn (weeks are
`Jobs.weekIndex`'s, Thursday-born like the town) the
edition for the week just lived is composed once,
frozen whole, announced on the ticker, and guarded by
`paperLastWeek`; it prints only for a week the save
actually lived — a player exists, and the paper's own
log already held a night of that week before the
turn's entry lands.

Composition is one pure function, `composeEdition`.
The headline follows a stated significance precedence
— highest tier present wins, ties within a tier to
the earliest recorded day: (1) CASE CLOSED, from the
arcs' own `*RetiredDay` stamps (arcs 2–8; arc 1's
record keeps no closing day, only a finished flag, so
the paper never guesses its week), printed in public
shape only — no names, no places tied to solutions,
no branch or choice words, no solution terms; (2) THE
FAIR, a wrapped `FairRecord` whose `FairCalendar`
window overlaps the week; (3) OPENING NIGHT, a
`PlayRecord` with a verdict whose opening — derived
from the record's own year through `PlayCalendar`, the
schedule the town played by — falls in the week; (4)
THE VOTE, `Votes.lastResult` counted in the week; (5)
THE RIVALRY, a `Rival.contestLog` entry in the week,
the headline quoting `Rival.scoreLine` verbatim; (6)
THE SKY, a special night in the week's log; (7) A
QUIET WEEK IN SOUTH PARK, honestly. The sections
always run TOWN NEWS, SPORTS, CLASSIFIEDS, WEATHER:
town news carries the week's vote / fair / play /
case / yearbook lines from the records' own fields;
sports quotes the score line, the week's contests,
the showdown's season card (labeled lifetime, never
weekly) and the crew standings through
`RivalCrew.crewsScoreLine`'s public sentence;
classifieds print real prices only — every
`Stand.STOCK` shop price and supplier, the player's
own set prices when a stand stands, and the
permit/kit line from `Stand`'s own constants; weather
prints one line per logged day of the week, dated by
`Commissions.keyForAbsDay`, labeled by the `WEATHER`
map, specials named. A section with no record in the
week prints one honest line saying so. Past editions
reduce to headline lines, capped at `HEADLINE_CAP` 8.

The suite's new section proves it the hard way:
headline precedence fabricated tier against tier
(including the within-tier tie to the earliest sky),
the number audit — an edition composed over
distinctive fabricated records (a 137-of-201 vote, set
stand prices, a 16–10 showdown card, crew points) in
which EVERY integer in the text must trace to the
records it came from — spoiler hygiene against the
sealed terms (in the edition and in the module's own
source), the weather log's truth driven through real
`Engine.onNewDay` calls against each day's lived
weather and `dateSpecial`, publication exactly once
per lived week and never for a week a teleported save
didn't live, both caps over ten lived weeks, and the
frozen edition's exact round-trip through the save
path. The manifest moves to 81 / 0.11.0a and the tree
to 93 Kotlin files.

### v0.10.0g (versionCode 80) — Polish & performance pass #10: the Queue #10 audit (Queue #10 item 7)

The queue closer, an audit build — the v0.9.0g shape, one
queue later. No new feature, no new save keys, src stays
at 92 Kotlin files, and the manifest moves to 80 /
0.10.0g. **Production-code changes: exactly ONE**, a defect
the audit itself proved before it was fixed (the polish
pattern's standing rule — a failing check first, in the
open): the family-dinner / patrol disjointness that
v0.10.0a and v0.10.0b both shipped as "disjoint by
construction" held only on paper. The family's {0,2,4}
rhythm turns on `absDay % 7` while the patrol cadence
turns on the calendar week (`Jobs.weekdayOf`), and in the
lived calendar the two share **Saturday**: every seventh
day was both a family-dinner night and a patrol night,
and the player's evening was double-booked. The audit's
soak caught it living (a dinner and a patrol on the same
evening) and a 730-day sweep of the two real predicates
confirmed it. The fix is one clause in the module's own
established style: `Family.dinnerTonight` gains a fourth
stand-down — on a patrol night the table stands down and
the folks eat early, beside the hosted-night, holiday
and climax stand-downs it already kept. The {0,2,4}
rhythm, the patrol's Wednesday/Saturday cadence and the
cast households' {1,3,5} are all untouched, and with the
clause every shipped claim about the three rhythms is
true in the lived code for the first time. Everything
else in `src/` is unchanged; the rest of the build is
the audit itself, in the JVM suite's new `sectionV0100g`,
plus this README's status and this entry.

- **Baseline gate, first:** the untouched v0.10.0f tree ran
  **92,072 checks / 0 failures** before a single file was
  written (exact, no wobble) — every number below is read
  off the final run's on-disk log, never a tail.
- **The audit soak — 400 lived days, one save:** a fresh
  town driven through the real rollover while the lifer
  used the whole queue. The family arrived (stance: warm),
  the table was sat at on its {0,2,4} rhythm — 103 full tables of 103 family nights
  across the year, deferred nights never
  faked — and the allowance landed on 55
  weekly mornings, earned by the table every time; chores
  were offered, taken and settled through the real verbs
  (108 done), and one was deliberately left undone
  and lapsed free at midnight. A hero was made and
  patrolled on Wednesdays and Saturdays (102
  nights out) until the caper closed and the hero mask
  derived onto its hook. A full production was lived —
  auditioned, rehearsed, opened, judged and reviewed — and
  opening night's poster was developed in the darkroom off
  the real record. The three group rooms were born of the
  life itself and kept talking inside their per-thread
  caps; at day 400 the crew and family rooms stand live
  and the production's room is archived, its season over.
  The stand was permitted, stocked, staffed by self and
  crew (26 crew-staffed days inside
  194 staffed days), traded (1,098 units),
  spoiled honestly into the books, and reviewed off those
  books; the crate identity holds to the unit, and
  lifeText stayed bounded (max 72) under the
  whole queue's stamps. Across all 400 days, never two
  cases open at once.
- **The eight-case lifetime, driven again:** one
  unattended life retires all eight cases in order
  (248 days), the alibi case last, opening only
  behind the backwards case, the slot empty after it.
- **Veteran save IX:** veteran VIII's saturated body
  carried whole, with Queue #10's state laid over it —
  the family arrived and warm (their photo in the album,
  the frame full), a hero with a season of patrol
  tallies and the caper closed, two productions played
  to their reviews in scratch towns and transplanted as
  records, a stand's books from a real traded stretch,
  the three group rooms' texts in the phone store, and
  the eighth case closed with its graph fully judged:
  **452,385 chars** through the real encode path,
  under the 512K ceiling, and the whole body round-trips.
- **Fit QA** at fontScale 1.0 and 1.3 for the queue's
  screens: the 🪪 YOU card with the hero line riding
  beside the earned titles, the family frame dialog in
  both faces (empty and filled), the 🦸 HERO LOG off a
  closed caper, the 🎭 SCHOOL PLAY page off a played
  production, the 💰 YOUR STAND books page off a traded
  season, a group thread screen (the play room's long
  member line — cast, ensemble and director), and the
  eighth case's alibi board with PROVEN, REFUTED and
  OPEN verdicts standing side by side, derived, never
  stored.
- **Total contention, re-pinned at the code's own
  seats:** the TalkGate ladder stands at **eleven rails**
  — Queue #10 added none (the family talk is its own
  sheet; hero lines ride the dialogue layer) — and the
  agenda claim stack now seats **twelve** claims in
  `Engine.effectiveAgendaBlock`, pinned in source in the
  order the code consults them: dinners, the family's
  table hard by them, jobs, movie nights, capstones,
  finales, the fair, the rival crew's turf, patrol,
  rehearsals, the stand, and your crew's hook **last** —
  set pieces take the person first, always. (An earlier
  brief's guessed order had the family seat lower; the
  code's order is the one pinned. The family's claim
  still yields in effect to finales and fairs — its own
  module defers to both, pinned since v0.10.0a.)
- **Seams, lived:** a hosted night stands the family's
  table down in its own house (and un-hosting brings it
  back — the stand-down was the hosting, nothing else);
  730 swept evenings hold no night that is both a family
  dinner and a patrol; rehearsals begin the minute class
  lets out (15:00) and the claim holds its cast at 4 PM
  and nobody at 2 PM; an occupied crewmate is refused
  stand duty in plain words naming the crew's own rule;
  a due reading quiets the school day like every climax
  before it, and an unjudged graph quiets nothing; two
  group rooms born of one week's life land inside the
  group ceiling (≤2 a morning) and the phone's whole
  morning budget (≤4).
- **Peak budget, ninth distance check:** no drift
  (named 0.947 / double 0.978 / bark 0.947); the audio
  census is honest at 21 SFX / 7 blip families / 4 moods
  / 6 beds — Queue #10 added no audio family — with bark
  signatures at **70, all distinct**; the house census
  stands at **17 decor pieces** (the hero mask the
  newest) over a 12-slot trophy shelf.
- **Tuning-ledger sweep:** every Queue #10 dial is named
  in the ledger with its value — the family's allowance
  and rhythm, the patrol cadence and window, the play's
  rehearsals and judging, the group ceiling, the stand's
  whole economy, and the eighth case's delays (the cases
  entry already closed with the eighth case in
  v0.10.0f's build; the sweep proves it stands).
- **Known gaps, refreshed — the queue's honest list,
  carried forward:** no on-device rendering proof exists
  on this VM (the cape/mask/emblem hero pass, the play's
  dialogs, the group screens and the stand fixture are
  compile- and suite-verified; Lucky's screenshots remain
  the real QA) — **kept**. The player's own costume is
  invisible in street scenes (first-person POV; it shows
  in photos, at the mirror, and on the YOU card) —
  **kept, by design**. The play poster's frame follows
  the photo system's group rules rather than a strict
  cast lineup (the caption carries the cast and the
  verdict) — **kept, deliberate**. Group posts land at
  morning granularity, like the solo phone's notes —
  **kept, the harvest's standing trade**. The stand's
  permit is narrated as City Hall's but bought at the
  pitch dialog rather than on a physical City Hall trip
  — **kept, a disclosed UX bend**. A hero escort's target
  is not agenda-held afterward (the beat is the walk and
  the tally) — **kept**. The family has no Journal page
  and no barks (their record rides the family thread,
  the frame, and their own talk sheet) — **kept**.
- JVM smoke suite: 92,545 checks, 0 failures.


### v0.10.0f (versionCode 79) — Mystery #8: Everybody Was Somewhere (Queue #10 item 6)

The eighth case is an eighth shape: this one is an
alibi graph. One new pure module (`Mystery8.kt`),
additive save state in the established arc pattern
(pre-0.10.0f saves simply queue the case behind the
seventh), the manifest at 79 / 0.10.0f, and src grows
91 → 92 Kotlin files. The lorebook rides inside
unchanged.

- **The premise.** On setup evening for Family Night,
  the Elementary's marquee — lettered that morning
  with the school's brand-new slogan — is found at
  the rehearsal doors re-lettered into a joke at the
  slogan's expense, and one letter is missing
  entirely: not on the sign, not in the tray. Seven
  people were in or around the building in the
  stretch between the sign crew going home and the
  doors opening. Every one of them produces an
  alibi, and every alibi is vouched for — honestly,
  by somebody who was there. The town's arithmetic
  says the sign changed itself. The principal wants
  a name, or an explanation, by Family Night.
- **The spine is an alibi graph.** The Journal board
  draws the seven alibis as nodes, and between them
  **four pairs** of alibis that share an
  event-signature — a sound two people heard, a
  figure seen from two sides, an object two people
  handled, a sound two streets heard. For each pair
  you **judge**: ONE EVENT (the two tellings describe
  the same moment) or TWO EVENTS (different moments
  that merely rhyme). Each pair has exactly one
  piece of evidence that settles it, and **verdicts
  are derived** from the recorded judgment and the
  live evidence on every read, never stored:
  **proven**, **refuted** (the judgment stands
  struck; it can be struck and judged again), or
  **committed**, standing untested. If two verified
  alibis describe the same event, the evening holds
  one fewer event than the town counted — and the
  counting is the case.
- **Twelve evidence pieces** (5 witness / 5 look / 2
  participation), three tagged to each pair, in the
  arcs' mixed-verb pattern — tellings to hear,
  things to find, shifts to work. Two pieces are
  **unreadable when found** — inert, honestly
  described as not-yet-making-sense — and become
  spendable only after the chapter that unlocks
  their context. An inert piece settles nothing,
  proves nothing, buys nothing.
- **Everybody is honest; that is the difficulty.**
  The case carries a red herring with a mundane
  engine, at least one fully innocent reading of
  the town's apparent answer is evidence-supported,
  and its human sting is an alibi that collapses by
  merger rather than by lying — a person who told
  the truth about a different moment than they
  thought. Every clock in the case is a little
  wrong, and every witness is exactly right, and
  the investigation is learning to hold both.
- **The finale is the reading.** Family Night's
  program reaches item six — *the matter of the
  sign* — with the column of names on the podium
  and the graph in your Journal. Four calls:
  **read the graph aloud** (gated on the graph's
  final derived state — the two load-bearing pairs
  standing proven, every pair judged, no judgment
  standing refuted, at least three pairs proven),
  **name the liar** (the town's own answer; gated
  on any one pair standing proven), **let the count
  stand**, or **walk away**. A program nobody plays
  lands cold. Every landing persists in the arcs'
  style — marks, bonds, standing lines, ambient
  callbacks, an objector in the hard landings, a
  delayed tail on one landing — and, as ever, the
  game never admits a call was wrong.
- The case queues ten days after the seventh case
  retires, defers past holidays like every case
  before it, registers its climax in the standing
  deference lists (the Bijou's doors, the classroom,
  the walk home, the season finales), and the
  eight-case lifetime holds in the suite's long
  drive.

### v0.10.0e (versionCode 78) — Your Stand (Queue #10 item 5)

The new kid goes into business — honestly, which in
this town is the hard way. One new module (`Stand.kt`),
src grows 90 → 91 Kotlin files, the manifest moves to
78 / 0.10.0e, **all state rides the `life`/`lifeText`
maps** — no new save keys, and pre-0.10.0e saves simply
have no stand. The lorebook rides inside unchanged.

- **The pitch.** Setup is a one-time **City Hall
  permit** (10 🪙 — Mayor McDaniels signs it herself)
  plus a stand kit (15 🪙), at one of four outdoor
  pitches where townsfolk genuinely pass on their real
  agendas: Main Street, Kid's Park, Stark's Pond, the
  Bus Stop. Moving later is allowed for a stated 5 🪙
  pitch fee. The hours are 8 AM–8 PM, and the stand
  sells **only while staffed** — an unstaffed stand
  sells nothing, and the game says so plainly, on the
  sign and on the page.
- **Stock & prices.** Stock is bought from the town's
  real shops at the pantry's real prices — the fair
  booth's own list (lemonade 1, hot chocolate 1,
  cheesy poofs 2, snack cakes 2 🪙) — into a crate
  that is **not** your pockets: crate stock can never
  be gifted, traded or resold elsewhere. The shops
  sell you at most 12 of an item a day. You set each
  price inside a stated band (shop price to 3×).
  Perishables spoil on stated shelf lives (lemonade
  2 days, hot chocolate 3, the sealed snacks 6) —
  and spoilage is **written into the books at cost**,
  never silently vanished.
- **Selling.** Staff it yourself and your presence is
  the claim: it sells while you stand at the pitch,
  and stops the moment you walk away. Customers are
  the townsfolk **actually present** — no conjured
  crowds. Each present person's chance reads real
  state: your price against the shop's (fair sells,
  gouging mostly doesn't), the weather/season fit
  (lemonade on bright days, hot chocolate in the
  snow), their authored loves, and your friendship
  with them. A regular who loves what they bought
  may say so. Nobody buys more than 2 a day.
- **Crew staffing.** A crewmate can be assigned to
  work the stand — a rendezvous on the Crew pattern:
  they walk the real Walk route, the crew's occupied
  rules apply in full (if their life calls them away,
  selling just stops), they sell at 0.7 of you, and
  they're paid in friendship (+1 a staffed day). No
  coin cut; crews don't skim.
- **The books.** The Journal's 💰 YOUR STAND page
  keeps real accounts — days open, staffed days,
  units bought/sold/spoiled/on hand, takings, costs
  (permit, kit, pitch fees, stock), the spoilage
  write-off, and profit or loss in plain words —
  every number derived from the recorded
  transactions. Bought always equals sold + spoiled
  + on hand.
- **The review.** After 5 staffed days, the rival's
  crew posts a review (it lands as a Threads topic,
  and the stand page quotes it): fact-slots filled
  from your real books — your average markup, your
  sell-through, your spoilage — kind or cutting as
  the numbers earn. An honestly-good review lifts
  your foot traffic ×1.5 for 7 days; a rough one
  lifts nothing, because tone never buys traffic.
  The stand can absolutely lose money — overprice
  and overstock, and the books will show the red,
  with the spoiled stock itemized.

### v0.10.0d (versionCode 77) — Group Chats (Queue #10 item 4)

Your phone learns to talk to more than one person at a
time. One new module (`GroupChats.kt`), src grows 89 →
90 Kotlin files, the manifest moves to 77 / 0.10.0d, **all
state rides the established patterns** — texts live in the
phone's own store under the existing `phone` save key, and
bookkeeping (snapshots/stamps) rides `life`/`lifeText`
under `gchat:…` keys, so there is no new save shape and
pre-0.10.0d saves gain each room as its condition is first
met. The lorebook rides inside unchanged.

- **Groups are a thread kind, not a new app.** A group
  thread is a `PhoneText` thread whose id names a room
  (`group:crew`, `group:family`, `group:play`) while each
  message's sender names the actual speaker. Caps, unread
  badges, per-thread shedding and the morning harvest all
  apply unchanged; the thread list shows the room's title
  and member line, and bubbles inside a room are labeled
  with the speaker's name. Group posts arrive under a new
  `GROUP` trigger, only inside the same morning harvest,
  spending the same `MAX_NEW_PER_DAY` budget and stopping
  at the same `MAX_UNREAD` wall, with a ceiling of their
  own (`MAX_GROUP_PER_DAY` 2) so a loud room can't eat the
  town's morning.
- **Your crew's chat.** Exists while the roster does
  (Crew's member list is the one source). It's born with a
  first member's post when a crew forms, welcomes joiners
  by name from a member already in, and notes a leaving in
  one factual line from who's left; when the last member
  leaves, they close the door with one line and the room
  archives — history stays readable, posts stop. Reactive
  posts key off recorded facts only: an outing (a member's
  outing tally rose; the line cites the last place the
  record says you stood together), a photo with ≥2 of them
  in frame (posted by one of the framed, citing the
  record's place), a contest the crew witnessed (quoting
  the rival scoreboard's own line verbatim). On quiet
  mornings the room may simply banter — presence lines
  that claim no event, name no place and quote no fact,
  audited line-by-line in the suite.
- **The family thread.** Mom + Dad + you in ONE thread,
  born when the family arrives. The v0.10.0a solo threads
  stay exactly as they were; this room is the second
  voice — the parent who did NOT send the solo text adds
  theirs here: tonight's dinner coordinated by the
  non-duty parent (naming the real dish), the morning
  after a dinner seconded (or the fridge plate reported)
  by the parent who didn't solo-text it, and allowance
  morning seconded by Dad off Family's own stamp.
- **The production's channel.** The current play's
  cast-and-crew thread, alive from the cast list to the
  review morning: the cast-list post (from the Mountain
  if a kid holds it, else the director), a line the
  morning after each rehearsal that knows whether you
  showed up (the record's own attendance mask), the
  director's places-call on opening morning, and a cast
  member quoting the verdict the record holds the
  morning after. Then the room archives with the cycle.
- **You can answer.** Rooms take the phone's quick-reply
  emojis. A reply lands as your bubble and, while the
  room is live, draws a short authored reaction from a
  member actually in the thread — never from someone who
  left — planting the solo pattern's "texting" topic in
  the reactor's memory. Group replies move **no**
  friendship and **no** coins (stated discipline, suite-
  pinned): the solo +1 economy stays solo. An archived
  room takes your bubble and answers with silence.
- **Fact discipline, audited.** Every reactive template
  spends only its fact's slots; the suite runs the
  template × event matrix (no photo record → no photo
  post, no witnessed delta → no contest post, flat
  tallies → banter at most), a spoiler scan over every
  new bank against the phone's denylist, and a
  determinism pass (same facts, same posts).
- **Cap math, re-derived.** (70 cast + 2 parents + 3
  group rooms) × 2 = 150 ≤ `MAX_TEXTS` 160 — **the dial
  does not move**; the tuning ledger says so too.
- **One quiet fix.** `PhoneText.fromMap`'s identity check
  admitted only cast names, so the parents' solo texts
  (threads "Mom"/"Dad") were silently dropped on every
  reload — in-session they worked, across a save they
  vanished. The check now admits the closed set it always
  meant: cast ∪ parents ∪ the three group rooms. A suite
  pin round-trips family texts end-to-end so it can't
  regress quietly again.

### v0.10.0c (versionCode 76) — The School Play (Queue #10 item 3)

Once a year, the Elementary puts on a show. One new module
(`Play.kt`), src grows 88 → 89 Kotlin files, the manifest
moves to 76 / 0.10.0c, **all state rides the
`life`/`lifeText` maps** (pre-0.10.0c saves join the cycle
at the next production year — a save living through a fall
this build didn't know gets no missed-play record), and the
lorebook rides inside unchanged.

- **The production — "The Mountain That Wouldn't."** An
  original play, written for this game: the valley votes to
  move the mountain six feet left for the new road; the
  mountain, consulted through its translator (a kid, with a
  shovel), declines; a goat settles the matter. Seven named
  roles (THE MOUNTAIN, THE MAYOR OF THE VALLEY, THE KID
  WITH THE SHOVEL, THE WEATHER, THE NEW ROAD, THE SNOWPLOW
  DRIVER, THE GOAT) plus a four-kid ensemble, directed by
  Principal Victoria. One production per school year on the
  yearbook's June–May rhythm: poster on the first school
  day on/after Oct 19 (a Look at the Elementary + a
  fact-only phone text from the director), auditions two
  school days later, four rehearsal afternoons, opening
  night the first Friday after — a ~2-week arc.
- **Casting from real data.** Deterministic and posted:
  each role's authored wants (nerve / voice / chemistry)
  are matched against an authored nerve table (the Rival
  pool's eight keep their Rival nerve exactly), the bee's
  line-handling skill (`School.skillOf`), and each kid's
  actual chemistry with the director. In 2027 the data
  casts Ike as the Mountain (nerve 8, fearless, and the
  Mountain only speaks three times), Wendy the Mayor,
  Kyle the Kid With The Shovel. The closest kid who gets nothing gets ONE
  honest, kind disappointment line — nobody is humiliated;
  it's a school play. Cast members talk their parts,
  "the play" becomes a Threads topic, and a cast kid's
  parent can mention it at their family's dinner table.
- **Your part.** At auditions you choose: **AUDITION**
  (a played line-reading — big-and-brave, small-and-true
  or deadpan — judged by the same casting math with your
  real friendship with the director standing in for
  chemistry; a smaller role, ensemble or understudy, is a
  real outcome, not a fail state), **CREW** (sets, lights
  or props — its own rehearsal duties and its own glory),
  or **AUDIENCE** (the production still happens around
  you, honestly smaller in your Journal). Rehearsals are
  agenda claims on school afternoons (15:00–17:00, so
  they can never fight a lesson); your attendance is
  tracked — lines learned, sets built — and shared
  sessions sprinkle +1 friendship and grow the cast's
  pair bonds a little, so rehearsing together literally
  raises the chemistry opening night is judged by.
- **Opening night.** A set piece at the very bottom of
  the safe-moment chain, gated to the Elementary in its
  house hours: it defers to case climaxes, season finales,
  holiday beats and capstone ceremonies and slips up to
  two evenings rather than fight them (a Bijou screening
  the same Friday does not defer it — the claim stack
  arbitrates the people; past the grace evenings the show
  wraps offstage and the record says so, kindly). The
  audience is the cast's real families, gathered by the
  agenda. The night is **judged live from real state**:
  cast chemistry, rehearsal coverage across the cast,
  the room actually present, and your ONE played beat
  (your line / your cue / your save — with one seeded
  mishap some years: a wandering goat, a leaning backdrop,
  a dead spotlight, a missing shovel). Three honest
  outcomes — **TRIUMPH / SOLID / BEAUTIFUL DISASTER** —
  all good nights; the standing ovation is computed from
  the room's fullness and its real chemistry with the
  cast, never scripted. (Halloween's orbit covers the
  2027 opening, Oct 30 — a holiday merely existing that
  week does not own the night; the show plays.)
- **The poster & the record.** Opening night mints ONE
  real cast photo — the posed finale, composed by the
  photo system's own group rules from the people actually
  in the room (no pixel costumes; the caption carries the
  production) and filed like any photo, so the darkroom
  can print it as the playbill for the print wall. The
  morning after, a named kid critic (the yearbook judges'
  rotation — Jimmy has the pencil in 2027) reviews it in
  fact-slot lines in the town log and the new Journal
  **🎭 SCHOOL PLAY** page, which keeps the current
  production whole — cast, your part, your rehearsal
  record, the verdict, the review quoted; past
  productions reduce to history lines.

### v0.10.0b (versionCode 75) — Hero Nights (Queue #10 item 2)

After dark, the kids put on capes. One new module
(`Heroes.kt`), src grows 87 → 88 Kotlin files, the manifest
moves to 75 / 0.10.0b, **all state rides the
`life`/`lifeText` maps** (pre-0.10.0b saves meet hero
nights on the next patrol night), and the lorebook rides
inside unchanged.

- **The roster — sixteen originals.** Stan, Kyle, Cartman,
  Kenny, Butters, Craig, Tweek, Clyde, Tolkien, Jimmy,
  Timmy, Wendy, Bebe, Heidi, Red and Scott each get a hero
  persona: an invented name (The Standard, The Verdict,
  The Heavyweight, Second Verse, The Good Sport,
  Flatline, The Jitterbug, Floodgate, The Upgrade, The
  Punchline, The Comet, The Brief, The Look, The Signal,
  The Spark, The Meter), an invented power, and a costume
  spec derived from the kid's own measured palette — cape,
  mask and emblem over their own colors, hats and hair
  untouched, rendered as overlays on the normal figure.
  **Copyright discipline:** every persona is original to
  this game — no show hero names, no show costume
  signatures, nothing traced — and the suite extends the
  Pocket Pals guard pattern to the roster: a franchise
  denylist scans every persona name, power, chapter and
  line in the module.
- **Your hero.** Early on, a safe-moment dialog founds
  your hero identity: a name from an authored list (or
  just be the New Kid, caped) and a power from an authored
  list — flavor, never mechanics-breaking. Your costume
  derives from your creator/wardrobe palette (cape = your
  worn top's color, mask its darker shade, emblem from
  your power). The identity is changeable at home via the
  mirror — the established re-open-creator route — and
  rides the 🪪 YOU card.
- **Patrol nights.** Wednesday and Saturday, 19:30 until
  the 22:00 sleep hour, at Kids Park, Main Street, Starks
  Pond, the Elementary and the Mall. The patrol is an
  agenda claim in the established stack — below the rival
  crew's turf, above your crew — and it yields like a
  low-stakes claim should: to finales, case climaxes and
  holidays wholesale, to dinners, jobs, movie nights,
  ceremonies and live emergencies, to the fair, and to
  your family's dinner table. **A patrol night never
  silently eats a family dinner:** the dinner claim wins
  and the patrol starts after. Under the costumes,
  everyone is still themselves — agendas, friendships and
  talk read the real kid; in-costume talk gains
  hero-register lines, once a night each, in the cast
  voice discipline.
- **Beats on the existing verbs.** A careful Look at a
  patrol spot finds the night's small "crime" — kid-scale
  (a trash-can raider, a runaway grocery cart, a TP'd
  tree, a dark pond lantern, wrong-way chalk arrows) — and
  settles it in the telling. Talk to someone out after
  dark offers an escort home, priced by the Walk graph's
  own minutes and spent on the real clock. The camera
  tells the truth: costume codes freeze onto photo
  figures at shutter time, so costumed group photos are
  real photos — the album, the darkroom prints and the
  yearbook see the capes honestly, forever. Beats pay a
  **hero tally** — patrols stood, assists, incidents,
  escorts — on a Journal 🦸 HERO LOG page. No coins,
  ever; hero work is its own reward (the ledger notes the
  absence deliberate).
- **The caper.** A season-long original caper in the
  StoryArcs pattern: five movements on patrol nights —
  chapters gated on patrol nights attended, one beat a
  night — concerning a kid-scale "villain" the patrol
  comes to call the Curator, whose rearrangements of the
  town are too careful to be vandalism. It is a story
  arc, not a mystery case: no evidence machinery, no
  accusation — the chapters carry it, and the finale
  night set piece at Kids Park ends it with a stance
  choice (name them / say nothing / lend a hand) whose
  marks persist: a Journal keepsake, ambient callbacks in
  hero-register talk and in the Look, and — honestly
  earned — the caper's one derived house trace, the
  Hero's Mask on a hook by the door (the decor census's
  17th piece, derived in `House.decorFor` like every
  piece on the wall). The caper's story secret stays a
  story secret: it is told only by the finale itself.
- **Crew & family.** Your crew patrols with you: a
  crewmate standing with you or walking to you is held by
  the crew's own claim above the patrol's, so they arrive
  at YOUR spot — in costume, or (when a set piece owns
  them) not at all, which the hero-register lines say
  plainly. Your parents have opinions: the morning after
  a patrol night you were out on, a `HERO` phone text
  lands in the family thread (warm under the curfew
  talk), and the next day each parent delivers their
  remark in person, once.
- **Verification:** the JVM suite grows a v0.10.0b
  section (roster originality + the denylist scan,
  costume derivation against the measured sheets,
  identity create/change rules, cadence + claim
  deference incl. the crew-rendezvous and
  family-dinner-guest guards, incident/escort judging,
  costume-line once-a-night discipline, photo figure
  round-trips with the frozen costume, an end-to-end
  caper drive to all three stances with the decor
  deriving, the parents' remark once-a-day, and the
  release pin) — plus the honest pin moves every build
  makes (manifest 74/0.10.0a → 75/0.10.0b in eight
  sections, the Kotlin file count 87 → 88 in four, the
  decor census 16 → 17) — all green (the count is read
  off the build's own smoke log, not estimated). Fan
  reference sheets untouched.

### v0.10.0a (versionCode 74) — Your Family Arrives (Queue #10 item 1)

Queue #10 opens, and the New Kid stops living alone. One new
module (`Family.kt`), src grows 86 → 87 Kotlin files, the manifest
moves to 74 / 0.10.0a, **zero new save keys** (all family state
rides the `life`/`lifeText` maps), and the lorebook rides inside
untouched. Baseline gate, first: the untouched v0.9.0g tree ran
**90,736 checks / 0 failures** before a single file was written;
the final suite runs **90,809 checks / 0 failures** (73 new
checks, all in `sectionV0100a`). The shelved APK is 3,043,895
bytes, md5 `79eb344c7ca52ab7e81843fa3a49aa55`, lorebook inside
byte-identical (1,537,338 bytes, md5 `acff1c04…`).

- **Two parents, household NPCs — explicitly not cast.** Mom and
  Dad are `VisitorPersona`s in the generated-NPC pattern (the
  residents', the arc-2 usher's), placed by `Family.presentAt`
  through the same `generatedHere()` surface as every generated
  soul. They are in no cast table: `AGENT_PROFILES`, `KID_ORDER`,
  the faction lists and the rival pool never name them, and the
  suite pins the 70-cast census from both sides. Family warmth
  lives in `life["famPts:<parent>"]` (-50..100) and **never**
  writes `state.friendship`, so no cast-facing system (mail
  lines, tier events, HELLO texts) can ever read a parent as a
  townsfolk. Citizenship limits, stated: no factions, no crews
  (either side), no contests, bees, fair events or cases, and
  no TalkGate rail — their talk runs on their own sheet.
- **Looks: derived where the save honestly holds genetics,
  authored elsewhere.** Both parents wear the player's exact
  creator skin hex; Mom's hair is the player's exact hair color;
  Dad's is the same hex darkened, his beard wears the player's
  exact color, and Dad is bald iff the player is. Hairstyles
  (Mom's bob, Dad's side part), clothes, faces and widths are
  authored; proportions ride the adult band fallback (tall,
  unmeasured — exactly like generated NPCs). The derivation
  takes an appearance map, so a photo record's frozen appearance
  derives the family as it looked that day (`PhotoView` hooks
  `Family.specFor` ahead of the visitor fallback).
- **Jobs and days.** Mom works the new-accounts desk at the
  Bank, Dad the counter at the Post Office — resident-shaped
  agendas: home mornings, work 9–17 on weekdays, home evenings,
  weekend outings, asleep offstage at night, and work yields to
  holidays the way cast agendas do. Each parent carries a
  41-line authored bank (5 greetings, 4 evening greetings,
  8 topics, 6 general, 3 serve, 3 table moments, 6 chore asks,
  3 ambient, 3 byes) plus fact-slot lines that quote the
  player's real life — the crew by name, the rivalry's own
  score line, fair ribbons counted, decor counted, the family
  print on the wall, a case's *public* shape only.
- **The arrival.** A played beat at your house on the arrival
  morning — day 3 of a new save; a pre-0.10.0a save's family
  arrives its first morning under this build (the day stamps at
  first consult and never slides). The safe-moment dialog offers
  one stance — warm / fine / complicated — stored for the life
  of the save and echoed in the parents' first real talk and
  their lines after. The house fills in from that moment (the
  GameView family corner, gated on `Family.arrived`): two coats
  on hooks in their own top colors, their mugs on the dining
  table, and a family frame on the left wall that starts
  **empty** and fills with the newest family photo.
- **Family dinners.** Three nights a week at YOUR table on
  `DINNER_WEEKDAYS` {0, 2, 4} — the exact complement of the cast
  households' {1, 3, 5}, so the two tables never collide — inside
  the shared `Dinners` window, with three beats (the serve, from
  whichever parent you talk to first; table talk, from the other
  end; the wash-up, a Look at home). The table stands down for
  a hosted night (your guests win your table), a holiday beat,
  and a climax pending — `Dinners.beatsDeferred`, the cast
  tables' own rule. On `GUEST_CHANCE` 0.45 of nights a guest is
  seated — your crew first, else your best-friended school kid —
  claimed in `Engine`'s agenda stack beside the dinners, where
  jobs, finales and the fair still win the kid.
- **The allowance.** 3 🪙 on the week's first morning
  (`ALLOWANCE_WEEKDAY` 0), only when a family dinner was sat at
  within the last 7 days — Mom's rule, stated in her lines, named
  in the tuning ledger, and the **only** new coin source in this
  build (a full paper-route week pays 30 🪙; the allowance is a
  tenth of a worked week, for dinner). Chores — set the table
  (the fixture tap), take a family photo (the shutter), fetch a
  pantry staple (the hand-over in Talk) — pay warmth only, one
  at a time, lapsing free at midnight.
- **Phone & photos.** Mom and Dad hold Messages threads
  (dinner calls with dish/house/window, the morning-after
  thanks or the fridge-plate report, the allowance note) inside
  the **unchanged** `MAX_TEXTS`: 72 threads × 2 = 144 ≤ 160,
  so the dial does not move and the comment says why. Quick
  replies stay cast-gated — family threads are receive-only.
  Parents stand in photos at home as household figures, the
  darkroom develops family photos like any other, and a print
  gifted TO a parent is judged by `Darkroom`'s own math and
  paid in family warmth (`givePrint` branches on the recipient,
  its dud rule intact: a print without Dad in it comes back
  with a polite no).
- **Spoiler discipline.** Family writing reacts only to a
  case's public shape — that one is open, that the town is
  talking — never to any solution; the suite scans `Family.kt`
  against the sealed terms.

### v0.9.0g (versionCode 73) — Polish & performance pass #9: the Queue #9 audit (Queue #9 item 7)

The queue closer, audit-only — the v0.7.0g/v0.8.0g shape. No new
feature, no new save keys, src stays at 86 Kotlin files, and the
manifest moves to 73 / 0.9.0g. **Production-code changes: ZERO** —
declared plainly; the audit below proved no defect in shipped
code, so nothing in `src/` changed. What the build contains is
the audit itself, in the JVM suite's new `sectionV090g`, plus
this README's ledger closures.

- **Baseline gate, first:** the untouched v0.9.0f tree ran
  **90,301 checks / 0 failures** before a single file was
  written (exact, no wobble) — every number below is read off
  the final run's on-disk log, never a tail.
- **The audit soak — 400 lived days, one save:** a fresh town
  driven through the real rollover while the lifer used the
  whole queue: a crew of four recruited at the Friend tier and
  rendezvoused weekly on the Walk graph's own priced minutes;
  the wave-5 newcomers met in the wild (asks and Bijou
  audiences); the August 2027 fair attended, its photo contest
  entered with a crew frame, its fishing contest entered at
  the pond, its dunk tank recorded — judged and wrapped with
  the record standing attended, not missed; a print developed
  off the fair frame, hung on the wall, and still whole after
  an album flood shed its source at the cap; crew-vs-crew
  contests taken as the rival brought them and settled
  through their own funnels (26 settles across two seasons,
  46 crew outing days, all 8 wave-5 newcomers met), standings
  moving, every settle landing on the solo 🆚 ledger exactly
  once; and across all 400 days the seventh case never once
  opened over the sixth — it opened behind it and retired
  inside the same save. A second, dedicated drive re-proved the **seven-case
  lifetime**: all seven arcs retire in one unattended life,
  the backwards case last, the slot empty after it.
- **Veteran save VIII:** veteran VII's saturated body carried
  over the 70-cast town with Queue #9's state at its caps — a
  full crew's season, two wrapped fairs (four blue ribbons),
  a print box at its 24-record total cap (wall full, a dozen
  gifted, one print the last whole copy of a faded source),
  the rival's crew with a played season and two finished
  seasons behind it, and the seventh case closed with its
  timeline written and its case rebuilt: **443,065
  chars** through the real encode path, under the 512K
  ceiling, and the whole body round-trips.
- **Fit QA** at fontScale 1.0 and 1.3 for the queue's five new
  screens: the 👥 YOUR CREW page off a full crew, the 🎡
  COUNTY FAIR page off a wrapped fair (with last year's miss
  as its index line), the darkroom print wall at its cap,
  the rival page's CREWS standings off a played season, and
  mystery #7's timeline board with CONFIRMED and BROKEN
  lines standing side by side.
- **Total contention, unchanged: eleven rails.** Queue #9
  added no rail (the crew ask is a menu choice; crew contests
  ride the RIVAL rail), so the ladder stands exactly as
  v0.8.0g left it — pinned in source (enum + try-order) and
  peeled live under fabricated total contention: Clyde's
  stack (DELIVER over EMERG_OFFER over the RIVAL reveal over
  ERRAND_ASK over DIALOGUE) peels in order again, and no NPC
  in town can hold all eleven (proven from the live data).
- **Seams:** the agenda stack's Queue #9 seats pinned in
  source — Fair after Finales, the rival crew's turf claim
  after Fair, your crew's hook **last** — and a due
  homecoming dissolving the rival crew's turf hangout live,
  the moment owned by the seventh case's climax; plus the
  album-cap × darkroom promise in miniature (flood sheds the
  source, the print stands whole).
- **Peak budget, eighth distance check:** no drift (named
  0.947 / double 0.978 / bark 0.947); the audio census is
  honest at 21 SFX / 7 blip families / 4 moods / 6 beds —
  Queue #9 added no audio family — with bark signatures at
  **70, all distinct** after wave 5.
- **Tuning-ledger sweep:** every Queue #9 dial is named in
  the ledger with its value; the sweep found exactly two
  gaps, both closed in this build as documentation — the
  rival's crew (v0.9.0e) had **no ledger entry at all** and
  now stands beside the solo rival's, cadence, standings and
  turf dials named; and the cases entry ended at the sixth
  case — it now closes with the seventh's delays,
  `CASE_RETURN_DELAY_DAYS` among them.
- **The audit's own findings, honestly:** one harness lesson,
  not a shipped defect — the soak's first staging crowned the
  rival on day 0, where `revealed()` reads a revealedDay of 0
  as not-yet and the rival's crew honestly never forms. In
  play the reveal is a day-1-to-3 beat, so the crown can
  never land on day 0; the lifer now lives two honest days
  before the crowning, the way the v0.9.0e section stages it.
- JVM smoke suite: 90,736 checks, 0 failures.

### v0.9.0f (versionCode 72) — Mystery #7: The Case Told Backwards (Queue #9 item 6)

The seventh case is a seventh shape: this one is told
backwards. One new pure module (`Mystery7.kt`), additive
save state in the established arc pattern (pre-0.9.0f saves
simply queue the case behind the sixth), the manifest at
72 / 0.9.0f, and src grows 85 → 86 Kotlin files.

- **The opening is the ending.** On Founders' Assembly
  morning, in front of the whole school, a silver trowel
  with a sixty-year absence is taken off its display
  table, a family is named, and the thing is carried out
  to applause. The town treats it as settled by lunch —
  a rescue, thirty witnesses deep. You were there. You
  saw all of it. The case is what you actually saw.
- **The investigation runs in reverse story-time.** Four
  chapters reconstruct progressively earlier movements:
  the taking itself, then the night before it, then the
  document that started it, then — last — the winter
  where it truly began. Testimony is gathered in the
  present about the past, and two of the twelve evidence
  pieces are **unreadable when found** — inert, honestly
  described as not-yet-making-sense — and become
  testimony-grade only after the chapter that unlocks
  their context. An inert piece can be spent on nothing:
  it confirms nothing, breaks nothing, buys nothing.
- **The board is a timeline, filled in backwards.** The
  Journal lays the story out in true chronological order
  (beginning → confrontation) while you fill it from the
  confrontation down. Each movement, once its live
  evidence is in hand, is **committed** as a written
  reconstruction — assembled from the pieces, in the
  ledger discipline — and a committed line is later
  **confirmed** or **broken** by what the past actually
  shows. That includes the confrontation's own line,
  committed first on the least evidence in the case.
  Verdicts are derived from the recorded inputs on every
  read, never stored; a broken line may be struck and
  written again, and the finale reads the timeline's
  *final* state, not its first impressions.
- **The finale is the confrontation, again.** The
  trowel's homecoming ceremony returns the story to the
  same room and the same people, and this time you play
  the scene with the whole timeline known. Four calls:
  **say what really started it** (gated on the beginning
  movement standing committed and confirmed), **let the
  original ending stand**, **change one thing** (a
  specific timeline-supported intervention), or **walk
  away**. A ceremony nobody plays lands cold. Every
  landing persists in the arcs' style — marks, bonds,
  standing lines, ambient callbacks, one person per
  landing saying the uncomfortable thing forever — and,
  as ever, the game never admits a call was wrong.
- **Twelve evidence pieces** (5 witness / 5 look / 2
  participation), each tagged to its movement; the case
  queues ten days after the sixth case retires, defers
  like every case before it, and the seven-case lifetime
  holds in the suite's long drive.

### v0.9.0e (versionCode 71) — The rival's crew (Queue #9 item 5)

Your rival has friends too. One new module (`RivalCrew.kt`), all state in
the established `rivalCrew*` life/lifeText patterns (no new GameState
field, no save-format change), the manifest at 71 / 0.9.0e, and src grows
84 → 85 Kotlin files.

- **Their crew, derived — never authored per save.** Once the rivalry
  exists, the rival's crew of **3** is picked by an authored precedence
  over real data — candidates are the kid cast, like the rival pool
  itself (an adult double act is family, not crew): (1) the rival's kid
  pair-script partners (the authored double acts), (2) the rival's
  faction mates, (3) every other kid.
  Inside a tier, `Photos.chemistryBetween` with the rival decides (live
  bonds plus the recognized-pair bonus), name A–Z breaks ties. The pick
  is recorded on first consult and read back forever — later bonds never
  reshuffle it — and a pre-0.9.0e save grows the same crew by the same
  rule. For every pool rival the first two tiers fill the three, so the
  crew is always partners and faction family.
- **Home turf.** The rival's faction's own authored turf, first entry.
  On a seeded rhythm (one day in three, 15:30–19:30) the four gather
  there through an agenda claim placed after the fair's and before your
  crew's in `Engine.effectiveAgendaBlock`, self-guarded like the fair's:
  it yields to `Emergencies.momentOwned` wholesale and to every
  `Crew.busyReason` (asleep, school, dinners, shifts, screenings,
  ceremonies, finales, live climaxes, live emergencies), and never holds
  a member standing with your crew. In crowds they render as their own
  `Crowds` cluster on a distinct affinity (`RIVAL_CREW_AFFINITY` 34 —
  under your crew's 40, over a faction's 8); the suite proves both
  cluster kinds form separately in one crowd and that MIN_SEP/span hold
  with both affinities live at the full 70 cast.
- **Three crew contests**, offered through the Rival scheduling
  discipline (RIVAL rail invitations, declining free, deference via
  `momentOwned` exactly as solo contests):
  - **🏃 Crew relay** — every leg priced off the Walk matrix from where
    the runner actually stands at the accept: your crew's posted legs
    (route × 2 + pace; pool kids on their authored `paceHalf`, everyone
    else's pace derived odd from their authored dodge), theirs the same,
    and YOUR leg is the anchor, run for real on the clock like the solo
    race. A short-handed captain runs the anchor twice, priced whole.
  - **📸 Crew photo showdown** — armed at the accept; your next group
    frame AT THE TURF with at least two fielded crew in it settles.
    Yours scores by `Yearbook` group math over the real record; theirs
    is rolled on the same scale from their members' real chemistry
    (12 a head, 6 a high-chemistry posed pair, 3 a goofy face, a seeded
    follower trickle), and the settle line cites contest facts only.
  - **❄️ Crew showdown bracket** — paired duels on authored dodge: you
    play the captains' duel for real, the other pairs settle on the
    same math (`Feel.POWER_GOOD` × (1 − dodge), first to 3, first throw
    alternating by pair).
  Every crew contest lands on the solo 🆚 ledger through
  `Rival.recordResult` exactly once — and the settle preserves the solo
  challenge calendar across the call, so a crew result makes the board
  without shoving the solo queue. Crew contests move the standings and
  the ledger ONLY: no friendship, no coins, no economy (the captains'
  duel is a real duel with its own settled rules, exactly as the solo
  showdown contest is). Fairness is suite-proven: over 24
  mediocre-anchor relays their crew takes a real share (never a
  walkover either way); their rolled frame beats a solid 57-point frame
  in an honest share of 400 rolls, bounded to the yearbook scale's own
  48–84 ground; equal dodges split the bracket pairs evenly.
- **The CREWS standings** anchor to the school year (the yearbook's
  June 1 – May 31): win 2, draw 1, shown on the 🆚 THE RIVAL page's new
  crews section with both rosters, the turf, and the season's shape.
  With at least two contests played, the school year's last three
  weeks turn the next due challenge into **THE FINAL** — double points
  and a played beat (a line from each captain; your stance choice books
  onto the same escalation/mercy ledgers the reveal feeds). At the June
  turn the season closes into ONE history line (kept four deep); stale
  arms lapse free at the solo contests' own window. "The crews" is a
  gossip topic in the Threads pattern: planted on both crews at every
  settle, with a bystander's owed line quoting the one factual
  standings sentence verbatim.
- **Edge cases, honestly.** No crew of your own: the offer still comes
  and says, in the rival's voice, that you need a crew first — solo
  contests unchanged, their crew still roams. The **frenemy** case (the
  rival is in YOUR crew): they captain their own side and don't count
  on yours, stated in the offer text ("BRACKET INTEGRITY") and their
  lines; your side fields who you have. A kid in **both** crews sits
  the contest out, loudly. A side that can't field **two free crew**
  (per `Crew.busyReason`) waits — the offer names who is holding it
  up. Their members stay full citizens: agendas, dinners and cases are
  untouched beyond the turf claim.

### v0.9.0d (versionCode 70) — The darkroom (Queue #9 item 4)

Your house learns to develop what your camera saw. One new pure
module (`Darkroom.kt`), one additive save key (`prints`), the
manifest at 70 / 0.9.0d, and src grows 83 → 84 Kotlin files.

- **The fixture.** A darkroom corner joins Your House's working
  fixtures (drawn with the bed, shelf, mirror, album and table in
  `GameView`): a red-lit alcove on the center wall — one safelight
  bulb, a line of drying prints (one paper per print you hold;
  blank paper while the box is empty), and a counter of three
  trays. Smooth shapes only. Beside it, under the calendar, hangs
  the **print wall**: a board with three frames next to the
  yearbook shelf and the ribbon display. A hung print is painted
  by `PhotoView`'s own renderer — `onDraw` was refactored onto a
  shared `drawInto(canvas, record, rect)`, so the wall shows the
  actual prints with the album's code, chin-stamp captions and
  all; a yearbook winner's print wears a small brass plate under
  its frame. The wall is a fixture, not a decor piece: `House`
  decor is earned achievement derived from records, and the wall
  is furniture whose contents are your own work — the derived
  decor census is unchanged.
- **Developing.** The darkroom opens the album (the same records,
  newest first): pick a photo, pick a size. A print is **2 🪙 and
  30 minutes**; a framed enlargement **5 🪙 and 60**. The clock
  that moves is the town's own (`Engine.advanceTime`, tick by
  tick): weather shifts, agendas walk, an emergency's deadline
  keeps its rules while you work — and a moment that already owns
  the town (`Emergencies.momentOwned`) won't let you start, in
  the established words. Costs are stated before you commit, the
  gate is one function (`Darkroom.developBlock`) the dialog and
  the suite both read, and the coins leave the capsule purse up
  front, booth-style.
- **Prints.** A print is a physical keepsake carrying **its own
  copy of the photo's record**, frozen at develop time — the
  yearbook-volume survival pattern. If the source later fades
  out of the album cap, the print survives whole, caption and
  all (the wall's readout says when a print is the only whole
  copy left). Developing reads the source and never writes it:
  the record, its star and every judging input are byte-identical
  after, and later album edits never rewrite a print. The box
  holds 12 prints (box + wall together), the wall 3 (swapping is
  free), and 24 records all-in — past that, the oldest *gifted*
  print is forgotten, and the develop result says so.
- **Gifts.** An enlargement can be given from the person menu's
  gift flow ("🖼 Give a framed print…", joining items, trades,
  crew and fair on the same menu — no new TalkGate rail). The
  judgment reads the print's own copy: posed in the frame pays
  base 4, a photobomb pays 2, +1 per other poser in real
  chemistry with the recipient (cap +2), +1 per 25 followers
  the shot actually earned (cap +3), +1 if the copy is starred,
  total cap 8 — through the one friendship rail. A print of a
  photo they're **not** in is a polite dud, said in character
  with the photo's real subjects named, and the print comes
  back to your box — never a silent success. Once per photo
  per person: the town remembers who holds which print, the
  holder's Talk gains a line about it (owed-line rail, once
  per print), and the gift also lands in the NPC memory store
  like any gift.
- **The house notices.** Hosted-dinner guests (the v0.6.0d
  machinery) now remark on wall prints that feature themselves
  or their household — `Darkroom.guestPrintLines` composes them
  from the print's own copy only (the featured members' names,
  the place, the date), slotted into beat 2 ahead of the house
  facts without ever crowding them out, and the hosted score
  is untouched. A print of a yearbook **winner** carries its
  brass plate into the line too — derived from the volumes'
  winning entries by record id, never stored on the print,
  never hand-set.
- **Saves & spoiler discipline.** Prints ride one additive
  `prints` key; a pre-0.9.0d save loads with an empty box and
  the corner already standing (the fixture needs no unlock
  beyond having photos). Nothing here touches any case: print
  lines quote photo facts only, and the suite's spoiler scans
  stand unmodified.

### v0.9.0c (versionCode 69) — The county fair (Queue #9 item 3)

- **The week.** Every summer the fair takes Kid's Park for
  seven days, opening on the **third Saturday of August**
  (`FairCalendar`, pure calendar math in the Finales
  pattern). The park over the pond: the pond keeps the
  Regatta, and its open lawn is where booths, a tank and
  a wheel actually fit — the fishing contest walks to the
  pond for its morning as the fair's excursion. **The fair
  never lands on a finale week**: in years where the third
  Saturday's week would touch the Regatta's week (2027:
  Aug 21–27; 2028: by one day), the fair **shifts one week
  earlier and says so** on the ticker, the fence poster,
  the Mayor's text and its Journal page. The suite proves
  the two windows disjoint for 2026–2045. Announced three
  days ahead: ticker, the poster in the park Look, and one
  phone text from Mayor McDaniels (a **FAIR phone
  trigger**). Dressing rides the **Depth dressing** pattern
  — booths, bunting, a ferris silhouette, the dunk tank,
  all BACKGROUND tokens at the fairground only, live
  exactly while the week stands: the strike is the
  calendar turning, nothing to clean up.
- **The contests (existing rails only).** 📸 **Photo**
  (days 0–2, judged day 3's morning): frames taken *at the
  fairground* during the entry days, entered by being
  taken, scored by the **yearbook's own math**
  (`Yearbook.scoreOf` over Town Favorite + Busiest Scene)
  against six seeded townsfolk entries on the same
  scale; judged by the yearbook masthead's rotation, the
  note in its fact-slot discipline — a player win cites
  the frame's real company and sky, a town win cites only
  contest facts. 🎣 **Fishing** (day 3, at the pond): your
  catches funnel through `applyCatch` beside the rival's
  own hook, best-by-rank-then-size stands; the field —
  Jimbo, the rival, seeded townsfolk — makes one cast
  each on the **real FISH_TABLE** with their authored
  luck (RivalDefs' `castLuck` where written). Ranked by
  `Rival.fishRank`, then size; a boot is not a fish. When
  both you and the rival hold entries the result lands
  on the 🆚 ledger through `Rival.recordResult` —
  **unless a rival fishing duel is already armed**, in
  which case the duel keeps the ledger: never
  double-counted. 🎯 **Dunk tank** (day 5): a seeded
  sitter (Cartman, Randy, Jimbo or Butters by year), five
  townsfolk and you, three throws each. Yours are *really
  thrown* — `DunkTankView` runs the **showdown's timing
  bar and zone math** against the sitter's own
  `dodgeFor`; theirs are simulated off the same zones and
  the same dodge. Places pay **capsule coins 6/4/2** and
  ribbons (blue/red/white) everywhere.
- **Prizes & the house.** A 🏆 **feats pair**: **Fair
  Ribbon** (any blue, any year) and **Fair Sweep** (all
  three blues at one fair — proven earnable in the
  arithmetic, expected rare in the living). One derived
  house piece: **the Ribbon Display**, hung beside the
  yearbook shelf by `House.decorFor` reading the fair
  records — the first blue starts it, like every piece,
  it's derived, never stored.
- **Fair life.** Fair hours (**10:00–20:00**, to **21:30**
  closing night) bend agendas to the fairground through
  `Engine.effectiveAgendaBlock`, placed after Finales
  and before the crew, and **self-guarded**: dinners,
  jobs, screenings, ceremonies, finales and live case
  causers all outrank the fair, and the crew's hook still
  takes a member to you first. Opening morning offers a
  played beat — **cutting the ribbon with the Mayor**
  from her tap-person menu (no new TalkGate rail; the
  eleven-rail ladder stands). **Liane's snack booth**
  sells the pantry's own food at honest fair prices in
  capsule coins (lemonade 1, hot chocolate 1, cheesy
  poofs 2, snack cake 2 — a small, honest sink). **Four
  authored fair errands** post across the week through
  the same menu (a ribbon-table delivery, a lost bottle
  cap, the tank bench, a poof run), sourced `FAIR` on the
  errand board's own funnels, objectives wearing 🎡.
  Closing night ends with **fireworks**: `SkyState`
  gains a FIREWORKS special keyed to the fair's calendar
  — the town gathers at the park to watch, and a photo
  taken that night honestly shows them (the yearbook's
  sky judges score fireworks 25, under the harvest
  moon's 30).
- **The record.** One `FairRecord` per year — the build's
  single additive save field (`state.fairs`) — written
  at the opening, filled as contests judge, wrapped the
  morning after closing: attended, or **honestly missed**
  ("you missed the fair" is a record too, contests
  judged without you). The Journal gains a 🎡 **COUNTY
  FAIR** page: the newest fair whole, elders as one
  lines — the yearbook shelf's cap discipline. A fair
  the game sleeps through for days still wraps whole:
  skipped judgings judge at the wrap off the same
  records and seeds. Trypophobia discipline held: the
  dressing is flat shapes, stripes and wide-spaced
  pennants — no clustered patterns anywhere. No new
  Pocket Pal cards; ribbons are keepsakes and decor.
  src grows 82 → 83 Kotlin files (`Fair.kt`).

### v0.9.0b (versionCode 68) — Your crew (Queue #9 item 2)

- **The crew.** The new kid doesn't have to roam alone anymore:
  any townsfolk at **Friend tier (friendship 30+)** can be asked
  into your crew from the tap-person menu — **up to four** —
  and leaving is always free (a member who leaves can't be
  re-asked the same day; crews run on not being weird about
  it). **The rival is askable like anyone**: their contests
  still fire from inside the crew, and their lines know it.
- **Rendezvous, honestly timed.** Crew members keep living
  their own lives. When you arrive somewhere, each free member
  sets out on the **real Walk graph** and arrives after the
  true **route minutes from wherever they actually are** —
  announced when they catch up, never teleported. Mid-route
  they're simply not there yet; if you move, the leg re-prices
  from where they stand. Going home or sleeping disperses the
  outing (not the membership) until the next one.
- **Occupied is occupied, said out loud.** School hours (off
  the member's own agenda — kids and the day's teacher alike),
  a boss's shift, a family dinner, a movie night, a capstone
  ceremony, a finale, a live case climax or a live emergency
  all send **regrets**: a line owed to you next time you talk,
  played once, before the small talk. While a bigger moment
  owns the town, no new rendezvous starts at all. And
  **deference is absolute**: a set piece claims a member
  mid-outing exactly the same way — the pull-away is said,
  never silent. Membership changes no one's pair scripts,
  arcs, dinners, jobs or case roles.
- **What a crew does.** At a place, the crew renders as your
  **standing Crowds cluster** (a new `Chem` input, affinity
  40 — cluster math re-proven at 70 cast with a crossing
  crowd). Members talk crew — the outing, the place, each
  other, at most a line in a while — and the shared things
  count: **photos** with crew standing with you who are in
  frame tally a photo together (the frame's chemistry stays
  Photos' own), **showdowns and rival contests** tally
  witnesses through their own modules' funnels, and the
  rival's Journal page now quotes how many contests your
  crew has stood and watched. Shared outings pay **+1
  friendship once per member per day** — the school-lesson
  rate, never more.
- **The record & the town.** The Journal gains a **👥 YOUR
  CREW** page: who's in, since when, and the honest tallies
  (outings, photos together, places visited together,
  contests witnessed). The town notices: **"your crew"** is
  a memory topic now — arrivals plant it on the witnesses,
  Threads carries admiring, jealous and unimpressed
  follow-ups, and a **SPOTTED** bark lands when two or more
  of your crew roll into a public place together. If **3+ of
  your crew share a faction**, that faction's members have
  a line about it (informational only — no standing moves).
  All crew state rides the existing `life`/`lifeText` save
  maps; **pre-0.9.0b saves start crewless**, and asking
  added **no TalkGate rail** — the eleven-rail ladder stands.
  src grows 81 → 82 Kotlin files (`Crew.kt`).

### v0.9.0a (versionCode 67) — Cast expansion wave 5 (Queue #9 item 1)

- **Eight more named townsfolk (cast 62 → 70: 32 kids, 38 adults),**
  picked from evidence exactly as waves 3–4 were — households first,
  then the richest standalone lore. The lorebook (3,322 entries,
  2,225 of them characters) was audited entry by entry against the
  roster, and the embedded lorebook stays **byte-identical**; only
  the generator's alias table grew (cast matched 70/70, locations
  27/27).
- **Roger Donovan — Clyde's dad completes the Donovans.** Clyde's
  profile has always orbited his widowed father; Roger's own entry
  (328 chars) carries the seat joke, the dating-again bravery, and
  the lemon bars. He anchors at MainStreet (the Donovans have no
  house on the town map — the Tucker precedent), joins the parents'
  faction, and gets pair scripts with Clyde and with Stephen, the
  neighborhood's other dad-pole.
- **Ryan & Sarah Valmer — Jimmy's parents complete the Valmers.**
  Ryan (245 chars) is the fire chief and a city councilor: loud,
  mustached, allergic to back rows, and still apologizing — by
  attendance — for the year he suspected his own son of gang
  membership. Sarah (165) is the notebook, the front row, and the
  only Valmer who reads a room faster than the chief. Ryan holds
  City Hall (station hours), Sarah MainStreet; their pair scripts
  cover the duo act, both parents with Jimmy, and the fundraiser
  that is basically their vows.
- **Alexis & Mr. Testaburger — Wendy's parents complete the
  Testaburgers.** Alexis (226) is the city planner on the council:
  the agenda, the minutes, and a dissent-filing daughter she
  respects on the record; she holds City Hall four blocks a day
  like Lu Kim holds the wok. Mr. Testaburger (163) is the quiet
  half — one-sentence paragraphs, a kettle, and stadium-volume
  pride kept at library register. Their scripts run the family
  debate circuit, including the bowl treaty.
- **Nathan — the kids' field gains its born nemesis.** The richest
  standalone kid entry left in the book (374 chars): Jimmy and
  Timmy's scheming rival, a fourth-grade supervillain with a
  five-year plan and a notebook of the duo's weaknesses (mostly
  "they are nice"). He joins Craig's gang (the gang he haunts),
  carries authored showdown dodge (0.33 — he planned where the
  snowball lands) and bee skill (7), scripts against both his
  nemeses, and takes the **rival pool's eighth seat** — appended
  last, so the authored seven keep their order and every recorded
  crown in an old save is untouched.
- **Sophie Gray — the new girl.** Scottsdale's correspondent
  (286 chars), diabetic like Scott Malkinson, which makes the two
  of them diplomats of the same small country with a snack treaty
  in their pair script. She joins the girls' circle as its
  Scottsdale bureau (Wendy runs the briefing), with dodge 0.29
  and bee skill 6.
- **Big Gay Al — the town's superest citizen.** A standalone
  fixture with 307 chars of lore: the animal sanctuary, the scout
  years, "I'm super! Thanks for asking!" He anchors at MainStreet,
  joins the parents' faction as the town's honorary uncle, wears
  the pageant grin and the white scarf, blips from the deep shelf,
  and scripts with Chef — two old friends from the boat ride,
  where the song is load-bearing.
- **Terrance & Phillip: passed over a third time, on evidence.**
  The lorebook's only entry for them is ONE joint entry of 186
  chars (birthdays and an episode cite); neither man holds a
  standalone entry, they complete no household and own no
  location, and a paired act would have cost two slots for the
  thinnest support of any candidate in any wave. The suite now
  pins the verdict against the lorebook file itself, so the next
  evaluation starts from evidence, not habit. Documented
  near-misses: Mimsy (196 — Nathan's lackey; Nathan's scripts
  carry his dramatic partners instead) and Linda Black (133 —
  Tolkien's mom; the Black dinner table stands as wave 3 seated
  it).
- **Full wave standard, no exceptions** — the v0.7.0a/v0.8.0a
  checklist per newcomer: creator spec and a newly measured
  figure (adults: head ≈ ⅓, legs ≥ torso; kids: the big-head
  band, all eight silhouettes distinct — Alexis's first measure
  collided with Laura Tucker's and was re-measured), 7-block
  agenda + home, family/faction placement (parents 18 → 24, the
  circle gains Sophie, the gang gains Nathan), 9 collision
  lines each (town floor 558 → **630**), 18 daily topics each
  (1,166 → **1,310**), a compact voice card (six first drafts
  ran over the 420-char ceiling and were trimmed to it),
  friend-tier greetings, five response banks, sleepy lines,
  pair scripts (13 new pairs, 26 scripts — town totals 58 → **71
  pairs**, 118 → **144 scripts**, every scripted pair carrying a
  relations note), a distinct bark (all 70 signatures pairwise
  distinct), a SoundGen blip family, and a fan reference sheet
  per Lucky's format (all 62 prior sheets byte-identical;
  contact sheet regenerated at 70; tools/params.json synced to
  exactly 70 characters, 0 field drift).
- **Scaling, checked at 70:** crowds — `rowsFor` gains a fifth
  row above 64, because the four-row classic packs 18 to a row
  at 70 cast and a crossing cluster takes it to 21, which at the
  MIN_SEP pitch spans more than the world and spills the classic
  margins (suite-proven; five rows pack 14, worst case span
  0.80); the bee at 32 kids still always crowns under the
  v0.8.0a wipe-round rules; the phone cap rises `MAX_TEXTS`
  140 → **160** — a full-cast inbox at 70 is 70 threads × 2 =
  140, which would sit exactly AT the old cap, and 160 restores
  eight full threads of headroom (the suite's fabrications
  derive from the dial, as before); the Bijou's NEVER_ATTENDS
  stays {Towelie, Crabtree} and no newcomer belongs on it — all
  eight are ordinary townsfolk, star-castable on both paths;
  dinners gain **no new table** (the Valmers, Testaburgers, and
  Donovans have no house among the 27 locations — parents join
  at their anchors; the seven seated households stand exactly
  as wave 4 left them); the yearbook's judges stay the authored
  Wendy/Jimmy/Cartman trio (newcomers appear as photo subjects),
  the eight emergency scenarios stay authored (newcomers hold
  ordinary errands), and the capstone ceremonies stay authored
  (newcomers join as faction members). **Saves additive-only:**
  no new save keys; existing saves meet the newcomers naturally.
  src stays at 81 Kotlin files.

### v0.8.0g (versionCode 66) — Polish & performance pass #8: the Queue #8 audit (Queue #8 item 7, queue closer)

The audit pass, same discipline as #1–#7: measure the queue the way the
town lives it, tighten what the measuring exposes, change no behavior.
**Zero production-code changes** — the audit proved no defect in the
shipped code; every finding landed in the suite or in this README.
Queue #8 is complete.

- **The lifetime soak grew the whole queue.** sectionV030g's 400-day
  year now also lives Queue #8, and this is what it honestly lived:
  all eight wave-4 newcomers met in the wild (their asks answered on
  the wave-3 cadence, their faces in the Bijou's audiences);
  capstones driven through the town's real income — five of the
  eight factions' big asks carried to their ceremonies inside the
  year, the remaining three left unfinished by the year (their gates
  and beats stand proven in v0.8.0b's own section), and the
  below-gate hint heard 653 times on the road, because the gate is
  income — chains, friendship tier crossings, road pitches — never
  a fabrication; emergencies on 163 of 400 days (the 0.40 dial's
  band): 123 offered and accepted, 113 landed through the errand
  verbs themselves, 10 let slip and settled by the morning tick's
  expiry — failure stayed free — 13 declined flat, and 27 mornings
  where a bigger moment owned the town and no emergency fired;
  every offered deadline recomputed exact against the Walk matrix
  at the lifer's own corner, all year; one yearbook volume
  published at the June turn — a full book, 60 photos judged,
  starred and unstarred crowned side by side, and all 14 of the
  lifer's stars still in the album at year's end (the cap kept
  every one); a rival crowned in a played moment, 32 contests
  across four kinds (showdown, fishing, race, and the spelling bee
  the school hook supplies), 16 pranks answered, the standing
  matured to FRIENDLY RIVAL and locked — two contest kinds (the
  follower race, the climax challenge) never aligned in the lived
  year and are named as such, their gates proven in v0.8.0e's own
  section; and a **six-case lifetime**: the sixth case opened only
  behind the fifth's retirement, overlapped it on none of the 400
  mornings, and — the lifer never driving it — retired on its own
  clock inside the same year.
- **Veteran save VII: 383,987 chars** through the real encode path
  (veteran VI: 354,758 on this tree) — Queue #8's capped records
  laid over the whole veteran body: five yearbooks in the shelf's
  own shape (three whole, two index-only), the rival ledger at its
  cap with the standing locked, all eight capstones complete, a
  rescue record past both feats, the sixth case's typed state
  closed — round-tripping whole, comfortably under the 512K
  ceiling.
- **Fit QA** on the queue's five new screens at fontScale 1.0 and
  1.3, the suite's Chrome discipline: the 🏛 CAPSTONES page (an
  earned title beside an armed beat's mark, and the veteran's full
  eight), the 📖 YEARBOOK shelf (three whole volumes rendered
  entry by entry the way the Journal renders them, the elders as
  their index lines), the 🆚 RIVAL page (the locked standing over
  a ledger at its cap), the sixth case's two-column board in three
  fabrications between them showing every standing a belief can
  wear, and the 🚨 emergency Journal line with its countdown
  running.
- **Total contention, final form: eleven rails.** The ladder grew
  twice this queue — EMERG_OFFER (v0.8.0c) and RIVAL (v0.8.0e),
  both below FACTION_MISSION and above ERRAND_ASK — and is now
  pinned in source, in the enum's order and in gateFor's try
  order. No single NPC can hold all eleven, proven from the live
  data: the rival pool and the emergency askers meet in exactly
  one name, who sits at no family's table, and the nine-rail
  holder stands outside the pool entirely. So the fullest stack
  the town can build is peeled live off Clyde — DELIVER →
  EMERG_OFFER → RIVAL → ERRAND_ASK → DIALOGUE, each rail judged
  away through its own module, the emergency declined rather than
  accepted precisely because a live one would deafen the rail
  below it. Randy's nine-rail stack stands in v0.7.0g's section,
  unchanged.
- **Seams, re-proven in lived dress:** the album cap's shed order
  keeps a starred photo inside its yearbook's judged pool; the
  three capstone cross-beats (a Bijou photo lands the screening
  beat only while the house is live — the same shutter at noon
  lands nothing; a real ballot lands the circle's vote beat at
  the week's count, and no ballot lands nothing; a real shift,
  worked on its own card, lands the parents' job beat at its
  clock-out); a live emergency deafens the rivalry until the
  plate clears, and speaks again when it does; the sixth case's
  due meeting defers to the same four lists every climax does —
  the Bijou dark for the case, the school day quiet for the
  case, the season finales held, the walk home case-critical —
  all on one fabricated evening; crowds laid out whole at every
  headcount from 55 to the full 62; the spelling bee at thirty
  kids crowning a champion on every Friday in a 1,500-day span,
  the whole class rotating through the field; and the reseated
  dinner tables lived — Shelley at the Marsh table and Ike at
  the Broflovski table, welcomed, with their table talk standing.
- **Peak budget & census, seventh distance:** no drift (named
  0.947 / double 0.978 / bark 0.947, beds inside the ambience
  allocation), the family census unchanged (21 named effects /
  7 blip families / 4 moods / 6 beds — Queue #8 added no audio
  family), and the bark census at 62 cast: 62 rhythm signatures,
  all 62 distinct.
- **Tuning-ledger sweep:** every Queue #8 dial is named in the
  ledger with its value; the sweep found exactly one gap, closed
  in this build as documentation — the capstone entry named the
  coin ladder (`BEAT_COINS`) but spoke of the +4 friendship each
  beat pays only in prose; it now names `BEAT_FRIENDSHIP` beside
  its siblings.
- **The audit's own findings, honestly:** the soak's first rescue
  accounting was wrong in an instructive way — an errand judge
  lands the *first* matching errand, so the driver now credits a
  rescue only when the emergency itself is the record marked
  done, and watches every other acceptance to its settlement;
  and an emergency's clock settles on game ticks (as v0.8.0c
  built it), so the year runs the one morning tick lived play
  always gets rather than letting a plate sit frozen between
  day-rolls. Both were harness lessons about how the shipped
  systems actually behave, not shipped defects — the behavior
  is what the pins now describe.
- JVM smoke suite: 87,729 checks, 0 failures.

### v0.8.0f (versionCode 65) — Mystery arc #6: the court of public opinion (Queue #8 item 6)

**Somebody painted over the town mural in the night — and by
breakfast, the town had already decided who.** The summer's
wall on Main Street (every face, every name, the children's
handprints in a row, a quiet corner for the people who aren't
here) is found blacked out on unveiling morning, and the
town's instant answer is a convenient one: the goth girl with
the dark poems and the sketchbook. The sixth case is the
first whose antagonist isn't a person. It's the town's
certainty — and this time the investigation is an argument
with it. New module `Mystery6.kt`; four chapters on the
episode machinery, queued ten days behind the confession
case like every case behind its predecessor; a Journal board
that keeps the town's mind in two columns that never merge.
- **The conviction meter — opinion is real, and it is
  honest arithmetic.** A meter for the accused (plus drifting
  suspicion values for the other names in play) that is
  DERIVED, never stored: a base for a juicy story, plus every
  gossip beat the player has personally witnessed, plus the
  opinion weight of every piece of evidence the player has
  actually proven (hard proof moves it a lot, impressions a
  little), plus the stances the player takes out loud. In
  case Talks the player may BACK THE STORY (+4 conviction),
  QUESTION IT (−3), or SAY NOTHING (0) — the amounts printed
  on the buttons, every stance remembered on the board.
  Nothing unwitnessed moves the needle. Nothing unproven
  moves it. The hidden truth never touches it at all — the
  suite proves the derivation by recomputing it from the raw
  records over every subset of its inputs.
- **THE STORY vs THE EVIDENCE.** The board's left column is
  what the town believes — each belief tagged with where it
  was heard. The right column is what the player has proven,
  by verb and place. A belief never migrates into the
  evidence column, and proven evidence never auto-corrects a
  belief: a belief contradicted by evidence the player HOLDS
  but never presents stays believed, marked on the board in
  those words — and the case remembers the difference after
  the meeting, belief by belief.
- **The evidence: 12 pieces** (5 witness tellings, 5 Look
  finds, 2 hands-on participation beats) in the arcs' mixed
  discipline, at least three of which exist solely to test
  the town's favorite story — its poem, its sighting, its
  arithmetic. One name the evidence seems to point at
  mid-case is clearable by proof, on two independent
  channels. At least one fully innocent reading of the
  accused is supported by evidence, not by a trick.
- **The pressure is the meter, lived.** As conviction
  climbs its bands — a story going around / hardening / all
  but decided / decided in everything but the vote — the
  accused's days visibly change: her lines and her circle's
  close ranks in Talk, the cafeteria's unwritten seating
  chart and the chalk war at the wall appear in Look text,
  and the morning ticker notes every hardening (and,
  quieter, every loosening).
- **The finale is THE TOWN MEETING** at City Hall, gated on
  the evidence's weight: the verdict lands by the meter
  UNLESS the player's presented evidence outweighs it. Four
  calls — PRESENT THE EVIDENCE (only what's proven can be
  presented; the gate is the evidence column itself), BACK
  THE TOWN, SPEAK FOR THE ACCUSED (character, not facts —
  it moves hearts, not the ledger), or LET IT LAND. The
  board publishes the verdict arithmetic in advance, so the
  record can never surprise anyone who read it. Endings
  persist in the arcs' style — marks, bonds, standing lines,
  ambient callbacks; in the wrong landings an objector says
  the uncomfortable thing forever, and the game never admits
  a call was wrong. A meeting that happens without the
  player is its own landing. Saves stay additive-only (one
  `arc6` record, the evidence set, the witnessed-gossip set,
  the voice map; pre-v0.8.0f saves simply queue the case);
  a six-case lifetime is pinned in the suite's long drive.

### v0.8.0e (versionCode 64) — The rival (Queue #8 item 5)

**Somebody in town has decided you are their measuring
stick.** One kid is crowned THE RIVAL in the save's first
days — not by menu, but in a played moment: you both reach
for the same small glory (the last good slice, the high
score's initials, the one perfect Bijou seat) and they take
it personally. New module `Rival.kt`; the Talk gate gains a
RIVAL rail (below the emergency, above a fresh ask); the
Journal gains a 🆚 THE RIVAL page.
- **The pick:** seven authored kids (Cartman, Kyle, Wendy,
  Craig, Clyde, Bebe, Tweek — each competitive in a way the
  town's data already proves), chosen by a pure function of
  the save's creation identity (name + creator appearance),
  so the same save setup always crowns the same kid and a
  different one may crown another. The pick is written down
  at the first midnight and never re-rolled — a mirror
  restyle can't re-crown. The crown takes the rival's own
  authored line ("respectfully, authors" was taken).
- **The contests — judged on existing rails:** ❄️ a
  showdown rematch settled by the real duel on their real
  dodge stat; 🎣 a fishing duel — your next real catch
  against their one cast on the same table (a boot is not a
  fish); 🐝 the spelling bee judged standing, off the
  bracket's own math, whenever you're both in the field;
  📸 a follower race to the next photo milestone (they post
  in the night at their authored pace; crossing days judge
  it); 🎬 climax tiers compared after any shared event —
  theirs rolled from their authored nerve, yours played, and
  legacy/absent tiers judge nothing. Declining or ignoring
  a challenge costs exactly nothing, forever.
- **The one new shape — the honest-clock race:** a
  cross-town race to a Walk-graph destination at least ten
  route-minutes out. You run it on the real clock (elapsed
  minutes, carry and all; teleports and the bus count as
  minutes, honestly); the rival's time is posted in
  half-minutes off an authored pace that is always odd —
  so a tie is arithmetically impossible, and the suite
  proves it for every pace against every minute 1–60.
- **Fairness is the design:** their outcomes derive from
  their authored stats exactly as yours derive from play;
  the suite simulates the contests and shows every pool
  member winning a real share — nobody is scripted to lose,
  and the player is scripted to win none of it.
- **The score:** a running ledger on the Journal's 🆚
  page (each contest, its day, who took it, the current
  tally in words). A "the rivalry" memory topic with
  Threads lines; the rival texts the morning after a
  result (a new Phone trigger — taunt when ahead, grudging
  respect when behind, outrage at arithmetic on a draw);
  barks and owed lines carry the latest result for a few
  days, and the town gossips the score — every surface
  quotes the one recorded score line, so no surface can
  disagree with the board.
- **Pranks and the arc:** sometimes the rival leaves you a
  prank (six authored — pine cones, a reserved bench sign,
  a chalk scoreboard, a snowtwin, flyers, a chalk outline)
  — found by Looking, answered in person: PRANK BACK
  (escalate; the counter lands by morning) or LET IT GO
  (mercy). Pranks are cosmetic strings and a choice: a full
  cycle leaves the rest of the save byte-identical, and the
  suite proves it. The pattern of your answers matures the
  rivalry into a recorded standing — WORTHY OPPONENT,
  NEMESIS, or FRIENDLY RIVAL — and at ten contests the
  standing locks with a small played beat: a handshake held
  a second too long, a shove and then tacos, or both of you
  cracking up. Contests never move friendship, coins, or
  any other system's state; the scoreboard is the whole
  stakes.
- **Integrity:** challenges never fire inside the town's
  big moments (case climax, finale window, holiday beat,
  live screening, bee hours, capstone ceremony, a live
  emergency's clock) — the same composed deference the
  emergencies use. The rival remains a full citizen
  everywhere (agendas, dinners, votes, audiences untouched
  except these added beats), and the pre-existing "rival"
  uses in the town — the pair-bond name, the factions' rival
  tax, Engine #57's whoopee prank — are three different
  things and stay exactly as they were.
- Saves: `life`-map keys only — no new save keys at all.
  A pre-0.8.0e save gains its rival by the same
  deterministic rule applied to its creation identity,
  with the reveal due at once.
- Verification: full JVM suite green — new v0.8.0e
  section pins selection determinism and pool honesty,
  contest fairness distributions (showdown shares, 2,000
  seeded fishing casts both ways, the climax nerve table,
  the race's parity proof), score/topic/phone factuality,
  prank reversibility byte-for-byte, standing derivation
  from fabricated answer histories, the free decline,
  deference non-firing, save round-trips (mid-race, armed,
  locked), a spoiler scan over every bank and page, and a
  played lifetime where the rivalry reveals, contests,
  and never strands. Manifest bumped to 64 / 0.8.0e (the
  suite's manifest pins updated); signed and verified
  with the standing key.

### v0.8.0d (versionCode 63) — The town yearbook (Queue #8 item 4)

**Every spring, the Elementary prints your album back to
you.** In June the school year turns — the last day of
spring is the school year's last day too — and the
yearbook staff judges every photo you took between last
June and this May, from the album and nothing else. New
module `Yearbook.kt` (pure); the Journal gains a 📖
YEARBOOK shelf; the Album gains a star.
- **The turn:** publication settles in `onNewDay` on the
  first morning of summer (June 1) for the year that
  ended the day before, announced on the morning-report
  ticker like every module's news and written to the town
  log. The stored volume is the once-only guard — a save
  that slept past the turn publishes its ended year on
  the next rollover, and the year before the save began
  is never published at all.
- **Judging — records only:** six categories scored by
  stated formulas over `PhotoRecord` fields alone (the
  tuning ledger carries every weight): Best Group Shot,
  Best Selfie, Most Dramatic Sky (the record's own sky
  special, recomputed from its date/hour/weather by
  `SkyState`, plus a weather-drama table), Best
  Commissioned Print, Busiest Scene, Town Favorite.
  Zero randomness at judging time; ties break by earlier
  day, then record id — the same album prints the same
  book forever, across save/reload included.
- **The star:** from the Album, ⭐ any photo. A starred
  record earns `STAR_BONUS` +15 inside every category it
  enters — a nudge, not a gate: unstarred photos compete
  on their fields — and a starred photo never fades from
  the album, like a commissioned print.
- **The judges:** Wendy (editor-in-chief), Jimmy (photo
  desk) and Cartman (critic-at-large) rotate by year and
  category. Their notes are authored templates whose
  every variable — the people, the place, the weather,
  the sky, the asker, the counts — is a slot filled from
  the winning record itself; the suite audits the whole
  editor × category × record matrix for a name, place,
  sky or weather the record can't prove. A category with
  no qualifying photo that year stays honestly blank:
  "no entry — the page is just a nice border."
- **The volume:** each category page renders its winning
  record through `PhotoView` with its caption, its score
  and its judge's note. The volume stores a copy of each
  winning record, so pages survive the album cap. The
  shelf keeps the newest three volumes whole
  (`FULL_VOLUMES`); older ones reduce to a one-line index
  entry — winner list and photo count intact. Under six
  photos the year is a thin pamphlet and says so proudly;
  zero photos publishes covers only, no fake pages.
- **The argument & the shelf at home:** publication
  plants memory topics in the cast the frames actually
  hold — winners carry "yearbook famous" and bask via a
  new `Threads` bank; cast in a runner-up frame carry
  "yearbook snub" and grumble via its own bank — fading
  like every topic. The first published volume also puts
  a derived yearbook shelf in Your House (House decor,
  in the established pattern), spines drawn in
  `InteriorArt`.
- Saves: additive only — one new `yearbooks` key and the
  records' `starred` flag. A v0.8.0c save loads with no
  volumes and unstarred photos; its album belongs to the
  current school year and will be judged at its turn.
- Verification: full JVM suite green — new v0.8.0d
  section pins the calendar against Finales' spring
  boundary, every formula by hand, star math and
  tie-breaks, determinism across save/reload, the
  note-factuality matrix, blank/pamphlet/covers-only
  years, once-per-year settlement (including a real
  `onNewDay` crossing and the dormant-save catch-up),
  topic planting and the argument window, the shelf cap,
  the derived decor, the album cap keeping stars, legacy
  loads, and a spoiler scan over every judge line, page
  and this entry. Manifest bumped to 63 / 0.8.0d (the
  suite's manifest pins updated); signed and verified
  with the standing key.

### v0.8.0c (versionCode 62) — Errand emergencies (Queue #8 item 3)

**The favor that can't wait** — the errand system's third expansion (asks
v0.4.0b, chains v0.6.0b). Some days, somebody in town is having a small
disaster *right now*: a permission slip that has to beat the bus roster, a
court filing racing the clerk's drawer, a golden ticket stub lost in the
bus-stop grass minutes before the matinee, a class-photo bench wobbling
toward nine horizontal children. They find *you* — and the clock starts.

- **It finds you, never the board.** The day's scenario is a pure function
  of the day seed (`EMERG_CHANCE` 0.40 of days — one a day, never two).
  Talking to the asker offers it on the Talk gate's new `EMERG_OFFER` rail
  (below the set pieces, above a fresh ask); walking into their space
  first earns a 🚨 shout, once a day, on the ticker. Declining — or just
  dismissing the dialog — spends the day, cost-free.
- **The clock is honest.** Accepting stamps a deadline: the route's *true*
  minutes off the Walk graph (chained stop to stop from where you stand,
  recipient's live location and all) plus a working allowance
  (`WORK_MIN_PER_STOP` 10 a stop, `SLACK_MIN` 20 flat). The generator may
  only offer what that arithmetic finishes **before quiet hours** — and
  once stamped, nothing extends it: walking costs Walk minutes, talk
  costs talk minutes, and `Engine.advanceTime` settles the errand on the
  first tick at or past the deadline, wherever you're standing.
- **Judged by the verbs you know.** All eight scenarios ride the existing
  funnels — DELIVER to a person, FIND at a place, FIX a bench or tonight's
  flickering lamp, FETCH lemonade from the park stand back to its asker.
  No new judge exists to cheat.
- **Success pays like a rescue.** The verb's ordinary rates plus
  `RESCUE_FRIENDSHIP_BONUS` +3 💛 and `RESCUE_COIN_BONUS` +2 🪙, the
  asker's own words as the thank-you, a "rescue" thread in their memory
  (Threads has a bank for it), and two new feats — 🚨 **First Responder**
  (1 rescue) and 🚨 **Steady Hands** (10) — on the Journal's errand page
  and the Collection hall alike.
- **Failure is the town coping, and nothing else.** The moment passes:
  the errand leaves the plate, a recent note records it, the asker's
  coping beat (somebody else handled it; it was smaller than it felt —
  each scenario's is its own) waits to play once at your next meeting,
  and the morning report says so in one line. Friendship, coins and
  counts move **zero** — the disappointment is the asker's line, once,
  exactly like putting an errand down by hand (which settles the same
  way).
- **Fairness rails.** An emergency takes its *own* plate slot (the cap
  is bypassed, never evicting a held errand or chain link — seven active
  is legal while one runs); never in quiet hours; never while a case
  climax, a season-finale week, a holiday beat, a live Bijou screening,
  the spelling bee's hours or a capstone ceremony owns the moment
  (`Emergencies.momentOwned` composes the established deferrals). The
  Journal shows it on its own 🚨 line with the deadline and minutes
  left. A save/load mid-emergency preserves the deadline to the minute.
- Saves: **additive-only** — two new fields written only on EMERG
  errands (ordinary errands serialize byte-for-byte as before), the
  day/rescue state in `life`/`lifeText` keys. A v0.8.0b save loads with
  no emergency pending and zero rescues.
- Verification: full clean rebuild; JVM suite 83,908 checks, 0 failures
  — new v0.8.0c section pins the eight-scenario registry (four verbs,
  real places/items/spots, no arc's answer anywhere in the prose), the
  deadline formula against the Walk matrix across *every* ordered pair
  in town and every chained triple, the offer's rails (gate order,
  offered-once, DELIVER still outranking it, quiet hours, the
  infeasible-evening refusal), all six deference owners plus the
  offer-level integration, rescues judged through all four verbs with
  exact rescue rates, the clock on real ticks *and* on Walk-priced
  minutes (alive before, settled on the first tick past, inside one
  tick of grace), failure's no-penalty ceiling and once-only coping
  beat, the plate's seventh slot, the feats at 1 and 10, the 400-day
  soak inside the chance band, and the mid-emergency save round-trip;
  badging 62 / 0.8.0c, signer the standing cert.

### v0.8.0b (versionCode 61) — Faction capstones (Queue #8 item 2)
**The chains get their endgame.** Every faction's three-beat
chain (v0.5.0d) now has a second, bigger ask behind it: one
**capstone** per faction, eight in all, in a new module
(`Capstones.kt`) that speaks the missions' language exactly —
same Talk-gate rail (FACTION_MISSION, chain business first,
then the capstone's), same judge verbs, same state pattern
(two more `life`-map entries per faction; still zero new
save keys, so a v0.8.0a save loads with every capstone
simply unasked).

- **The gate is Beloved.** A capstone is offered only at
  standing 60 — the standing table's own top rung — and
  only once its faction's chain is finished. The gate
  guards the *asking*; a capstone already taken on can be
  finished even if standing dips. Below the gate there is
  no menu and no nagging: the faction's senior says one
  honest line ("the town isn't ready to ask this of you
  yet", in eight voices), once a day, through the Talk
  gate's owed-lines. The suite proves the gate reachable
  through the town's *real* income only — a finished
  chain's 30 standing, friendship tier crossings at +3,
  Walk road pitches at +2 — with the arithmetic composed
  in the open for two factions: the Boys' Crew (30 + four
  members' full befriending = 66) and the Church, whose
  two members cap the deterministic income at exactly 48,
  the last 12 earned on the road.
- **The eight asks**, each drawn from its faction's own
  identity: **The Boys' Crew — "The Premiere Print"**
  (name a kid in the credits, catch his still at a live
  Bijou screening, and post the print at Stark's Pond);
  **the Goth Kids — "The Town's Obituary"** (read the
  church's notice board, take dictation from the
  smallest goth, and hold the reading behind the school
  at dusk); **the Girls' Circle — "The Charter"** (carry
  the binder, get it ratified by an actual town-vote
  count your ballot is in, and sign it into the Mall);
  **Craig's Gang — "The Long Whatever"** (win the
  rematch, land the golden carp, and raise the totem at
  the park); **the Parents — "The Watch, Official"**
  (fill the roster, work a full town-job shift on the
  record, and take the bell at Skeeter's); **the School
  Staff — "The Bell, Re-Cast"** (look the old bell in
  its housing, clear the 240 taco target, and hang the
  recast bell at the Elementary); **the Church Faithful
  — "The Quilt of the Late"** (ask the town's best
  listener, sit the third pew at dusk, and hang the
  quilt in the church); **the Tegridy Crew — "The
  Standard, Sworn"** (hear the standard from the man who
  named it, deliver the coffee empire's tribute, and set
  the bench at the farm).
- **Three capstones are judged by other systems' own
  funnels**: the premiere still only counts as a photo
  taken while a Bijou screening is actually live (right
  room, right night, doors to feature's end); the
  Charter's ratification only counts in a `Votes`
  count your ballot is part of; the Watch's record only
  counts on a real `Jobs` shift, clock-in to clock-out,
  its line riding the clock-out text.
- **The ceremony.** Every beat 3 is a rite at the
  faction's own place in a 15:00–19:00 window: the
  faction's members' agendas bend to the mark's location
  for the window (the agenda override, in the
  established pattern), the senior hosts, the authored
  exchange plays, and the trace is set in view.
- **One reward shape, eight times**: a permanent town
  trace at a faction place — a drawn token claimed in
  the Depth BACKGROUND band and painted in `SceneArt`,
  named by a Look line — plus a title on the 🪪 YOU card,
  plus the new 🏛 CAPSTONES Journal page (asked, gated,
  in progress, set). Marks and titles derive from the
  completion mask, claimed exactly once; the rite cannot
  be re-armed or re-landed.
- **Standing economics**: capstone beats pay coins
  (3/4/6) and friendship, but **no standing with any
  faction** — the capstone is where standing *goes*, not
  another way to earn it. Finishing costs the missions'
  `RIVAL_COST` (4) with the declared rival, telegraphed
  by name in the ceremony's offer text. The suite proves
  all eight completable in one long save — gates re-earned
  after every cost, nothing locked out.
- **Memory**: taking an ask on plants a *"the big ask"*
  topic in every member's memory; setting the capstone
  turns it to *"our capstone"* (both with their own
  Threads banks), and the morning report notes a
  completion once, the day after, by its title.
- Saves additive-only; fan sheets untouched; the
  manifest pins move to 61 / 0.8.0b.
- Verified: kotlinc zero errors, full clean rebuild,
  `aapt2 dump badging` (61 / 0.8.0b), signer cert SHA-256
  a9997c1e… (the standing cert), JVM smoke suite
  83,097 checks, 0 failures (all prior coverage plus the
  v0.8.0b section: the registry and the uniform reward
  shape, the gate earned compositionally for two factions
  plus its 59/60 boundary, the hint's once-a-day honesty,
  a full capstone through its own judges with the
  standing ledger watched, the screening photo's room
  and night discipline, the vote count's ballot
  discipline, the job shift's clock-out funnel, the
  ceremony's gathering and once-only rite, marks
  persisting and rendering in Look, all eight in one
  save, and save round-trips old and new).

### v0.8.0a (versionCode 60) — Cast expansion wave 4 (Queue #8 item 1)
**Queue #8 opens** — the town pushes back. Eight more named
townsfolk move in for good (the cast 54 → 62: 30 kids,
32 adults), each at the full wave standard: measured figure
through the sheet system, a 7-block day at real locations,
a voice card, 9 collision lines, 18 daily topics, friend
greetings, a response bank, sleepy lines, a bark signature,
one faction, pair scripts with their natural partners,
NPC_RELATIONS entries, and a fan reference sheet drawn in
the established format (all 54 prior sheets byte-identical;
the contact sheet regenerated at 62).

- **The eight**: **Ike Broflovski** (KyleHouse — completes
  the Broflovskis; the smallest figure in town, and the
  household's famous rule travels with him), **Kevin
  McCormick** (KennysHouse — completes the McCormicks; the
  eldest son Stuart's, Carol's and Karen's writing has named
  for a year; a teen, banded YOUNG_TEEN, and never confused
  with Kevin Stoley), **Shelley Marsh** (StanHouse —
  completes the Marshes; braces drawn, diary classified),
  **Laura Tucker** (MainStreet — completes the Tuckers;
  the smile to Thomas's silence), **Dr. Alphonse Mephesto**
  (MainStreet — the town's geneticist, in from the ranch),
  **Sergeant Harrison Yates** (CityHall — Barbrady finally
  has a sergeant; the parents faction's blurb claimed "the
  entire police force" and now it has one), **Mr. Adler**
  (Elementary — the shop teacher completes the school
  staff; the incident binder is open), and **Tuong Lu Kim**
  (CityWok — the restaurant stood ownerless the way
  Skeeter's bar once did; no longer).
- **Constellations completed**: the Marsh and Broflovski
  dinner tables reseat with their missing members; twelve
  new scripted pairs (Ike–Kyle, Shelley–Stan, Kevin–Kenny,
  Kevin–Karen, Laura–Mr. Tucker, Yates–Barbrady,
  Adler–Garrison, Lu Kim–Mr. Tweek…) and twenty new
  relation notes. Gerald's old line about Ike being "in
  Canada now" is corrected — born there, lives here.
- **Capacity fixes at 62** (the wave-3 class of bug, found
  the same way — at full cast): the Bijou's never-come
  (Towelie, Crabtree) could be cast as a generated feature's
  STARS and so attended as headliners; star casting now
  honors NEVER_ATTENDS on both the generated and the
  vote-headline paths. The spelling bee could crown nobody
  when a late round wiped the field — at 27 kids the seeded
  lottery hid it, at 30 it hit a third of Fridays; the bee
  now follows real bee rules (a wipe round eliminates
  nobody; the judges reinstate, the same fiction
  `winnerIfOut` always fell back on). The phone inbox cap
  `MAX_TEXTS` 120 → 140 so a full-cast inbox (62 threads)
  actually fits.
- **Floors moved honestly**: collision lines 486 → 558
  (62 × 9), daily topics 1022 → 1166, pair scripts
  93 → 118 across 58 pinned pairs, speech cards / bark
  signatures / sheet specs 54 → 62, veteran errand census
  54 → 62. The lorebook asset is byte-identical inside the
  APK (1,537,338 bytes); only the alias table grew.
  Veteran save VI re-measured at 353,559 chars against the
  512K ceiling. Saves additive-only.

### v0.7.0g (versionCode 59) — Polish & performance pass #7 (Queue #7 item 7)
The audit pass, and the end of Queue #7: no new features, no
new save keys, no production code changed — the queue's
surfaces measured the way the town actually lives them, and
what the measuring exposed tightened in the suite and in
this document. **Queue #7 completes with this build.**

- **The lifetime soak completes the set**: the 400-day soak
  now also lives Queue #7 — the lifer stands where each
  unfound secret lives and tries its real trigger daily
  (10 of 12 found across the year, every one through its
  own gate in its own window; the aurora-gated and
  rain-gated secrets never aligned in this particular year
  and stayed scenery, which is the gates working, not the
  year failing), takes a Bijou seat on every screening
  night the marquee isn't dark (97 attended; legendary,
  buzzing and dead houses all in the tally; 29 nights where
  Saturday's feature was the vote's standing headline),
  sits lessons and referees recess on school days (243 of
  each), enters Friday bees (41 in, 21 won — some won,
  some lost), plays the day's climax chain two days in
  three, alternating heroic and fumbled (36 played; the
  surviving album reads heroic and fumbled both, with the
  skipped third reading absent), and meets all ten wave-3
  newcomers as townsfolk — asked, attended, starred,
  barked. The five-case lifetime stands in the same save:
  every case opened behind the last and retired, none
  overlapping. A soak-hygiene note: the year's played
  climaxes plant retelling topics on every causer in the
  process-global memory store, so the soak now hands the
  store back clean of Queue #7's topics too — later
  sections are entitled to a store holding only what
  their own worlds wrote, and v0.7.0e's absent-event pins
  caught the leak before anything shipped.
- **Veteran save VI**: the saturated save re-measured with
  Queue #7's records at their caps — all twelve secrets
  found, a full movie history, the school histories full,
  climax tiers on the album, the fifth case closed — and
  it still fits under the 512K ceiling with room, every
  layer round-tripping whole.
- **Fit QA**: the TOWN SECRETS page at 12 of 12, the MOVIE
  NIGHTS page at its history cap, the SCHOOL page with its
  histories full, the fifth case's claim board (settled
  claims and standing core claims side by side), and
  episode credits carrying a climax tier line — all
  measured through the renderer's own ellipsis discipline
  at font scales 1.0 and 1.3.
- **The gate, final form**: TalkGate's nine rails pinned
  in source and peeled live on one man under fabricated
  total contention — Randy holds all nine now, the harvest
  climax his own event on his own farm, played out between
  the finale and the dinner through the session's own
  verbs. (Jimbo's five-rail stack and the seven-rail form
  stand as their sections left them.)
- **Seams**: the Saturday headline expires with the vote
  outcome that bought it; the City Hall secret's gate
  reads standing outcomes only and closes when the last
  one lapses; the fifth case's confrontation stands all
  four deference lists down on one live state (Bijou dark,
  school quiet, finales held, road quiet); a bee Friday
  and an event climax share one afternoon without eating
  each other — an event climax is not a case climax, and
  both books close honestly; and the crowd matrix extends
  to the full 54-cast town, the MIN_SEP floor holding at
  maximum realistic density.
- **Peak budget, sixth distance**: no drift on any named
  stack. The audio census, counted honestly after wave 3:
  no new audio family (21 effects, 7 blip families, 4
  moods, 6 beds, as the last pass counted) — and the bark
  table grew with the cast, one rhythm signature per
  cast member, all 54.
- **Tuning ledger**: swept for Queue #7's constants. Two
  gaps found and closed — the fifth case's dials
  (`CONFRONT_MIN_CLUES`, `LURE_RETURN_DELAY_DAYS`,
  `NAME_SCAR`, `BELIEVE_SCAR`) had never joined the
  ledger's cases entry, and the school entry named the
  bee's Friday in prose but never its `BEE_WEEKDAY` dial;
  both stand in the ledger now. The same
  sweep caught the source-layout header undercounting
  the tree by one file (75 written, 76 on disk); the
  header now agrees with the tree, and the suite pins
  the agreement.

### v0.7.0f (versionCode 58) — Mystery #5: "The Two Confessions" (Queue #7 item 6)
The fifth case, and a fifth shape: **confession forensics**.
Derby day at Stark's Pond ends with the winner's plaque lifted
for the handover — and the Golden Lure, the derby's trophy
for forty years, weighing nothing at all. It's been swapped
for a whittled wooden fake in craft-paint gold. Within days,
TWO townsfolk separately walk in and confess to the whole
thing — sincerely, in detail, and in writing. Both can't be
telling the whole truth. At most one of them did it.
- **The investigation is a ledger**: each confession is a
  statement the player can re-hear from its speaker any time,
  and every statement is full of checkable claims — where
  they were, what they touched, what the lure weighs, how
  it's mounted, the order things happened. The Journal's
  fifth case board scores every claim **CONFIRMED / BROKEN /
  UNTESTED** as the town's evidence comes in — witness
  timelines, Look artifacts, the incident's physical trail,
  and two finds earned hands-on at a chapter's turf. Claims
  are never marked true or false about a *person*; the core
  claim of each confession can be broken by collapse and can
  never be confirmed by any evidence — because surviving
  every check isn't the same as guilty, and this case knows
  it. One townsfolk also behaves exactly like a third culprit
  — bought gold paint, whittles decoys, was at the dock,
  profits from the scandal — and the evidence has opinions
  about him too.
- **Structure:** four chapters on the episode machinery,
  queued ten days after the Night Gardener retires (the
  five-case lifetime holds; the slot still carries one case
  at a time). The confessions arrive staggered — the first
  lands early and the town relaxes, the second lands
  mid-case and re-opens everything "solved." Twelve pieces
  of evidence (five witness tellings, five Look finds, two
  participation beats) test ten claims, eight of them
  checkable by a named verb at a named place; no claim
  hinges on any other case's content.
- **The finale is a confrontation**: both confessors in the
  meeting room at City Hall, the ledger on the table, the
  call yours — **name** the one whose claims all broke,
  **believe** the one whose claims all held, **clear them
  both** on the evidence and mean it, or never make the call
  and let two confessions go **cold**. A wrong call is never
  admitted by the game and sticks socially, in the booth
  case's discipline; the cleared ending alone gets a long
  tail a week out. Endings persist as marks, bond changes,
  standing lines from the people your call landed on, and
  ambient callbacks, like every case before it. Saves stay
  additive-only (one `arc5` record + the evidence set);
  pre-v0.7.0f saves simply queue the case behind the
  gardener's.

### v0.7.0e (versionCode 57) — Playable climaxes (Queue #7 item 5)
The ten shenanigans' peaks stop resolving on participation
alone. When an event reaches its climax stage (12:00–18:00)
and you're on its turf, talking to any of its causers plays
the climax as a set-piece: a chain of three judged beats
written for that event's own story — calling out the
glitter on Cartman's "ultra-rare" at the auction's peak,
reading the treasure map right and captaining Butters'
dig, chairing the kids' town meeting at City Hall,
working Randy's merch table under Sharon's gathering
tone, refereeing the silent final at the park, taking
Barbrady's statement straight in the Coon fog, emceeing
the 'memberberry reunion, carrying the mascot motion in
the imagination debate, holding the church through the
knocking, and catching Jimbo's thumb on the scale at the
Great Weigh-In. Saves stay additive-only: no new save key
at all — chains ride the `life`/`lifeText` maps.
- **The verbs are the town's own**: Talk and Look beats
  judge on three-answer choice rows (landed / close /
  missed); PHOTO beats are settled by the shutter itself —
  a photo taken on the climax turf while the climax is
  live, then shown to a host; FIND beats by a careful
  Look at the one spot that matters, carried back. Plus
  exactly ONE new interaction, used three times in the
  set: **HOLD YOUR GROUND** — three times the moment
  surges (the knocking peaks, the long silence springs
  its traps, the debate wobbles), three times the room
  wants you to jump early or cave; hold all three to
  land it, crack on the third for a near thing, crack
  earlier and it's a miss. Its first use in a save
  teaches the rule up front.
- **Outcomes remember HOW**: a played climax settles at
  the midnight wrap to a tier — ⭐ HEROIC (every beat
  landed), ✔ GOT IT DONE (mixed), 💫 FUMBLED (most beats
  missed — funny, never punishing: the event resolves,
  no friendship is lost, and the town's version of the
  story just stars your fumble). Played tiers bring
  their own outcome lines, pay the causers friendship
  by tier (+2 / +1 / +0), plant a retelling topic the
  causers carry into tomorrow's conversations
  ("the big save" flatters, "the big day" is warm,
  "the big fumble" roasts affectionately), and write
  the tier plus a one-line how-it-went onto the
  episode's Journal record. Beats also pay participation
  into the event's own ledger, so the standing rule
  still reads your hand in the day.
- **Missable-safe, exactly once**: never show and the
  climax settles ABSENT — the wrap is byte-for-byte the
  legacy wrap (participation outcome, mark, standing),
  with only the episode's tier field quietly recording
  "absent". A chain interrupted mid-way (sleep, day
  rollover) settles at the tier earned so far, unplayed
  beats counting as missed; settling happens once, at
  the wrap, behind a settled flag the save/reload
  round-trip preserves. Earlier stages of every event
  are untouched, and the mystery chapters' climaxes are
  not in the set — their machinery is their own.
- **Before-records**: episodes finished before this
  build carry no tier; they read as the legacy record —
  GOT IT DONE for any memory purpose, marked
  "before-record" wherever the tier is read
  (`Episodes.climaxTierOf`).
- A new TalkGate rail, CLIMAX, sits between FINALE and
  DINNER: the climax is the day's peak and judges in its
  own hours; the gate's contention pins were extended
  to nine rails in place.

### v0.7.0d (versionCode 56) — School days (Queue #7 item 4)
On weekdays, South Park Elementary stops being a facade:
the fourth grade actually meets, and the new kid can sit
in. Everything runs through the classroom board inside
the school — drawn on the wall, chalk and all — and the
records live on a new 🎓 SCHOOL Journal page. Saves stay
additive-only: no new save key at all.
- **The lesson.** During class hours (9–12 and 1–3) the
  board offers the day's lesson: a topic from a written
  bank of fourteen (the water cycle, fractions as pizza
  math, a volcano demo gone mild, current events, the
  spelling list, gravity, maps, dinosaurs, plants,
  civics, teeth, the moon, money, poetry), deterministic
  per day with a stride rotation that never repeats
  inside two school weeks. Garrison teaches; Principal
  Victoria covers Wednesdays. Two short beats, the kids
  **actually present** reacting in character — Craig's
  finger, Tweek's panic, Jimmy's act, Kenny muffled,
  Wendy's hand already up — then ONE participation
  moment: a right-ish answer (the lesson just taught
  it), a funny wrong one (which goes on the board of
  wrong answers, where it stays), or a pass, which is
  always safe. Sitting a lesson pays +1 friendship to
  each classmate in the room (the nearest ten desks)
  and plants the "school" memory thread; one lesson
  per school day.
- **Recess.** At the midday block the yard holds one
  rotating micro-beat, refereed between two kids
  actually there: a four-square ruling (the do-over
  is the only verdict recess respects), a snowball
  truce (snow days only — hot-chocolate terms), and
  a note relay across the yard. Flavor and a little
  friendship; no new economy; one a day.
- **The spelling bee.** Fridays, 1–3 PM, Victoria
  presiding, sleeves DOWN. Eight kid spellers drawn
  deterministically from the class, eliminated round
  by round on their own authored data: Wendy and Kyle
  run deep; Kenny's muffled spelling is a joke the
  judges somehow accept; Cartman, if he lasts to round
  three, is disqualified for spelling off his sleeve.
  The player spells letter by letter off a three-letter
  choice row — no keyboard — through five lengthening
  word tiers. Winnable and losable purely on spelling:
  a perfect run takes the trophy, 5 capsule coins, the
  🐝 Bee Champion game feat, and a gold cup that goes
  up on your house wall (the 14th earned decor piece);
  a miss hands the trophy to the bracket's own champion,
  never a rigged one. Walk away mid-bee and midnight
  settles it at the round you reached, like every other
  game in town. The record also rides the 🎮 GAMES page.
- **The teachers remark.** In Talk, Garrison and
  Victoria comment on your attendance record — once a
  day each, and only once there's a record to remark
  on. Victoria's approval is hard-won: at twelve
  lessons sat, the file gets a gold star. Do NOT touch
  the star. No grades anywhere — attendance is texture.
- **Deference.** A due case climax, a finale week, or
  a holiday with its own beat owns the school day
  outright: no lesson, no recess beat, no bee — the
  classroom just has a quiet day, and the board, the
  Look text, and the morning report all say so honestly.
- 🔴 **Spoiler discipline held.** No lesson, beat, or
  bee line touches any mystery arc's content at all —
  the suite scans the school source and surfaces
  against the denylist (it caught one ordinary word
  during development; the content was reworded, never
  the scan).

### v0.7.0c (versionCode 55) — Bijou movie nights (Queue #7 item 3)
Friday and Saturday nights, the Bijou shows a feature,
and the town turns out for it. Each screening is a
bounded set-piece inside the theater: five title cards,
the room reacting between them, three moments where
your timing is the show. Saves stay additive-only — the
system adds no new save key at all.
- **The night.** Doors at 7, last seating at 8, credits
  by 9:30 — ahead of the town's late-night hush by
  construction. Announced that morning three ways: the
  morning report, the lit marquee on the Bijou's Look
  text, and one phone text from the feature's first star.
  Admission is **free**: City Hall's arts line already
  bought the print (the same line the Saturday-matinee
  vote spends), so the town's ticket is showing up.
- **The feature.** A town-made picture, generated whole:
  title, genre (eight — space opera to sea adventure),
  2–4 cast members as its stars, five title cards
  including exactly one immortal flop scene. The
  rotation is deterministic with a six-slot no-repeat
  window. **Saturday headlines the vote:** when the
  town's Saturday-matinee pick is standing, that film
  plays the evening show, title and genre bent to the
  winning option — the ballot, in lights.
- **The audience.** Authored, never rolled from a hat:
  the stars come; their friends come (real relation
  data — the same chemistry the photo system reads);
  their arch-rivals come *to heckle*; a seeded handful
  of regulars keep a standing habit; Towelie never
  makes it and Ms. Crabtree has seen enough movies
  driving the bus. The house caps at 14. Between cards
  the room reacts — a friend's cheer, a rival's heckle,
  Kenny muffled, Ned by voice box — composed only from
  the people actually in it.
- **Playing it.** Three reaction prompts (laugh / gasp /
  applaud / heckle) score against each card's beat:
  right +2, near +1, whiff 0, heckle −1 — unless the
  card is the flop, where the heckle is the correct
  answer. The total is the night's **buzz**: a dead
  house, a polite house, a buzzing house, or a
  legendary one on a perfect card. At the credits the
  stars' friendship scales with the house (0/1/2/4),
  every attendee carries a "movie night" memory topic
  into tomorrow, and the Journal's new 🎬 MOVIE NIGHTS
  page keeps your screenings, the features with their
  stars, and your best house — plus the 🎬 Legendary
  House feat for a perfect card.
- **Deference.** A screening never collides with the
  town's bigger claims: a live case climax (the booth
  case's own nights at this theater above all), a
  season-finale evening, or a holiday with its own
  beat puts the marquee dark with one honest line —
  "dark tonight — town business" — and the cadence
  resumes at the next slot.
- 🔴 **Spoiler discipline held.** No feature, reaction,
  or Journal line touches any mystery arc's solution;
  the suite scans the generated banks for exactly that.

### v0.7.0b (versionCode 54) — Town secrets (Queue #7 item 2)
Twelve quiet things are hidden around town, each a small
authored discovery that fires once and stays found: a carved
census under the pond dock, a second plow seen only under an
aurora, tally marks on the confessional's other side, seat K9
at the Bijou at dusk, a bench that keeps two coins for the
ones still waiting, a tag on the traveling gnome, the class
that never met, the town's first ballot and its third box, a
ring that stays warm in the snow, admission priced at one
button in the rain, a height chart still gaining in spring,
and one thing at home that somebody put back. Saves stay
additive-only — the system adds no new save key at all.
- **Deterministic discovery, no grind.** Every secret pairs a
  trigger with a gate, all twelve (trigger, gate) pairs
  distinct. Triggers ride only what already exists: the Look
  chain, the bus-stop bench tap, the garden-gnome tap. Gates
  read the real world state — light band, weather family,
  season, an aurora overhead, a standing vote outcome. Gate
  open + trigger = found, first time only; gate closed = the
  ordinary Look text, no "nothing here" tease, ever.
- **The half-hidden Journal page.** A 🔎 TOWN SECRETS page
  lists found secrets as short lore blurbs (what, where,
  which day) and unfound ones as riddle lines only — a riddle
  never names its place, its trigger, or its gate, and the
  suite pins that: per-secret forbidden words, every location
  display name, and a whole-page leak scan. The riddle is the
  only sanctioned hint channel, plus one vague "town secret"
  follow-up bank in the NPC topic rotation.
- **Rewards modest, settled once.** Three finds pay small
  coin amounts (2–4); the gnome's tag grants an existing
  Collection keepsake (coins instead if already owned); the
  rest are flavor. Nothing re-fires, nothing farms. Finding
  six earns the 🔎 Town Sleuth feat; finding all twelve
  claims a new house decor piece — "The Town, Mapped," a
  hand-drawn route map that draws itself onto your wall.
- **The town notices.** A present NPC reacts by name in the
  reveal moment and takes the "town secret" memory topic;
  the morning report notes a discovery the day after in one
  line that names only what was found.
- 🔴 **Spoiler discipline held.** No secret reveals, hints
  at, or depends on any mystery arc's solution; the suite
  scans every player-facing secret text (reveals, blurbs,
  riddles, page lines, the morning note) against the cases'
  denylist.

### v0.7.0a (versionCode 53) — Cast expansion wave 3 (Queue #7 opens)
Queue #7 item 1, opening the v0.7.0 series. Ten more named townsfolk
join the town (cast 44 → 54), mined from the lorebook's named pool
with the wave-2 discipline: paraphrased personas, measured sheet
specs, full writing at the established floors, and eligibility
across every system that enumerates the cast. Saves stay
additive-only — a v0.6.0g save loads with the new cast simply
present.
- **The roster.** Kids: **Karen McCormick** (Kenny's little sister —
  Carol's and Stuart's banks had been talking about her for a year;
  the McCormicks are whole now), **Scott Malkinson** (lisp, diabetes,
  Captain Diabetes heroics; Craig's gang), **Firkle Smith** (the
  fourth Goth Kid — the goths are complete), **Annie Knitts** (the
  girls' circle's intelligence service), **Bradley Biggle**
  (Henrietta's little brother; quietly Mintberry Crunch). Adults:
  **Skeeter** (his bar stood ownerless since v0.1.0; Stuart's speech
  card already deflected with his invitations), **Principal
  Victoria** (Elementary's authority, heading school staff),
  **Marvin Marsh** (Stan's grandpa; every boy is Billy), **Mr.
  Tucker** (Craig's dad — the last core kid gains the parent every
  other core kid already had; the finger is hereditary), and **Steve
  Black** (Tolkien's dad).
- **Full integration, no second class.** Every newcomer carries a
  measured SheetSpec through the HeadStack system (Karen is the
  smallest standing figure in town; Marvin the shortest new adult),
  a seven-block agenda, 9 collision lines, 18 daily topics, a
  SPEECH_RULES voice card, a Barks rhythm + hello (54 patterns, all
  signatures distinct), friend greetings, response banks, sleepy
  lines, a blip family, a showdown dodge, lorebook alias (54/54
  matched; lorebook.json byte-identical), exactly one faction, and
  22 new pair scripts across 11 new scripted pairs — each pair
  carrying a relations note (23 new NPC_RELATIONS entries in all).
  Writing floors move honestly: 486 collision lines (54 × 9), 1,022
  daily topics (5×28 + 49×18), 93 pair scripts, 54 distinct speech
  cards. The generated-visitor pool excludes the ten by alias, as
  with every wave.
- **Steve seats the Black household at Dinners.** The Black table
  had a house, a host, and one member; it now seats father and son
  (members Tolkien + Steve, the welcome/moment prose adjusted from
  "two settings" to three). The Tucker and Testaburger households
  remain honestly absent — a parent alone doesn't build a house,
  and the town map has no Tucker or Testaburger address to seat.
- **Two capacity fixes the bigger cast exposed** (both pinned
  before, failing honestly at 54): Crowds' within-cluster pitch is
  now floored at MIN_SEP — at perRow 14 the slot-derived pitch fell
  to 0.044 and cluster members stood closer than the layout
  promises; the between-group gaps absorb the difference. And the
  lorebook generator's location table regained YourHouse (a regen
  had silently dropped the 27th location alias; the generator now
  matches 27/27 again, so future regens can't lose it).
- **Fan sheets.** Ten new reference sheets generated with the
  project's sheet tooling (params synced from the game specs, 0
  drift); **all 44 existing sheets byte-identical** (md5-verified);
  the contact sheet regenerates with all 54.

### v0.6.0g (versionCode 52) — Polish & performance pass #6 (Queue #6 completes)
Queue #6 item 7, the last of the queue. No new features — the audit
pass over everything Queue #6 built, in the polish line's standing
form: measure, pin the seams, and touch production code only where a
measurement says a real defect lives. **None did: this build changes
no production code and no save format at all** — a v0.6.0f save loads
untouched, and what shipped is the proof, not a patch. **Queue #6
completes with this build.**
- **The lifetime completes the set.** The unified 400-day soak in
  `sectionV030g` now also lives Queue #6: weekly commissions accepted
  off the board and shot through `Photos.applyPhoto` (every
  commissioned print still in the album at day 400 — the cap sheds
  anything else first; plate, list and recent caps held daily),
  chained favors running to closure (chains done > 0, no live link
  malformed), the fourth case opening behind the third and retiring
  inside the same year — **a four-case lifetime**, its standing
  traces still on the town — family evenings sat across the town
  and hosted at the lifer's own table, a farm season and a paper
  season both worked through the real judges and both quit clean
  (wages landing on counted paydays, the streak reaching Steady),
  and a ballot cast most weeks all year (history pinned at its cap,
  outcomes seen standing). End-state: nothing stuck, every cap held.
- **Veteran save V.** The saturated save re-measured with Queue #6's
  records at their caps — the commission list full with the plate
  held, ten stamped prints in the album, a live chain link mid-tail,
  the fourth case's typed state closed, seven households of dinner
  counts, a job record at the Trusted streak, and a vote history
  counted full through the module's own rollover: under the 512K
  ceiling, and round-tripping whole.
- **Fit QA, newest screens.** The COMMISSIONS page, the ERRANDS page
  with a live chain thread, the FAMILY EVENINGS page, the WORK page,
  and the TOWN VOTES page with standings standing — composed off the
  veteran state at maximum content, held to the standing Chrome
  invariants at font scales 1.0 and 1.3.
- **Rail order, final form.** TalkGate's complete order —
  DELIVER → HOLIDAY_BEAT → FINALE → DINNER → WORK → FACTION_MISSION
  → ERRAND_ASK → DIALOGUE — pinned in source, then proven live under
  the fullest contention the cast data allows: Randy holding seven
  rails at once on one spring evening (a delivery, the Fourth's
  rehearsal step, spring's confession, the Marsh table talk, the
  farm inspection and its owed clock-out, the Watch report, and his
  own ask), peeled top to bottom through each system's own judge.
  The v0.5.0i Jimbo stack passes unchanged alongside it.
- **Cross-system seams.** Dinners × jobs: a sat evening excuses the
  shop shift it covered and never a morning paper route — pinned at
  the decision function, at the rollover, and against a no-dinner
  control. Votes × finales: the morning report prints the finale's
  note above the count, in source and in a lived report on the one
  spring morning both speak. Commissions × chained PHOTO errands:
  one shutter settles both judges — the print stamped, the chain's
  next link revealed. The fourth case's standing dressing × Depth:
  every fix token in the background band, claimed exactly once in
  the registry enumeration. Jobs × the Walk: a full paper route
  leaves the walk ledger untouched, and every route house stands
  reachable on the walking graph.
- **Peak budget, fifth distance.** The named stacks re-verified
  (−0.947 / 0.978 class, no drift), and the audio census pinned —
  21 named effects, 7 blip families, 4 moods, 6 ambience beds:
  Queue #6 added no audio family, proven rather than said.
- **Tuning ledger.** Swept for every Queue #6 dial — commission
  plate/pay/expiry, chain rates and bonuses, the fourth case's
  windows (structure only), dinner windows/gates/scores, wage dials,
  tiers and the miss limit, vote leans/weights/weeks — each one
  named in the ledger section itself; the sweep is pinned scoped to
  the section, so a dial that only made a changelog fails it. The
  sweep found six names the ledger carried as prose only — the
  fourth case's five window dials and the dinners hosting tier —
  and the ledger now names them all; no values changed.

### v0.6.0f (versionCode 51) — Town votes
Queue #6 item 6. The town is full of opinions; now it has somewhere
official to put them (new pure module, `Votes.kt`, all state riding
the existing `life`/`lifeText` maps — no new save keys).
- **One question a week.** On Jobs' Thursday-anchored week, City Hall
  posts a single small civic question from a 12-question bank — name
  the pond's new dock, pick the Bijou's Saturday matinee, word the
  park bench plaque, choose the mascot's hat, the story-hour theme,
  the water tower's paint, the crosswalk mural, the fountain coins'
  charity, the winter lights, Friday's lunch, the garden corner's
  name, the bus stop poster. The rotation is least-recently-used, so
  nothing repeats within `NO_REPEAT_WEEKS` 6 and nothing starves.
- **The ballot box.** It stands inside City Hall under the request
  board: one ballot per citizen, changeable or retractable until the
  week turns. Your ballot weighs more once the town knows you — the
  Fixture rule: Town Fixture counts double, Indispensable triple.
  McDaniels' morning report carries the count (below the finales,
  where City Hall business belongs), and her office texts it too.
- **Authored ballots.** The cast votes its personas: each option
  declares a champion, the factions it flatters and the Pocket Pal
  school it smells like, and a voter's lean is built from exactly
  those signals — the same data that drives trades and crowd
  clusters. Chef carries the pancake movie; Cartman demands every
  color, twice; the same week always tallies the same way.
- **Standing outcomes.** The winner takes its place in town for
  `OUTCOME_WEEKS` 8 weeks (or until its question is recounted):
  the dock's sign actually reads what won, the marquee shows the
  picked movie, the plaque says the words — every option of every
  question carries a trace line that shows on Look at its place.
  The Journal's new 🗳 TOWN VOTES page keeps this week's paper,
  the standings in force, and the last `HISTORY_CAP` 8 counts —
  with the runner-up champion's gracious epitaph under the latest.
  Winners and runners-up carry the result into their talks (the
  "vote" thread). No coins move; nothing is gated; it's
  citizenship, not an economy.

### v0.6.0e (versionCode 50) — Town jobs
Queue #6 item 5. Errands are favors — one-off, whoever asks. A job is a
standing commitment: a boss who hired you, a schedule that comes around
every week, shifts you play live, and a reputation that accrues to
steady hands (new pure module, `Jobs.kt`, all state riding the existing
`life`/`lifeText` maps — no new save keys).
- **The three jobs.** 🗞 **Paper Route** — mornings for Mayor McDaniels
  at City Hall (Mon–Fri, clock in 8:00–10:00): pick up the bundle and
  walk the route, four houses a morning off a six-house ring (a pure
  function of the day), each paper landing by Look at the house.
  🌾 **Farmhand** — mornings at Tegridy for Randy (Tue/Thu/Sat, in
  8:00–9:30): tend the rows and mend the fence by Look, then survive
  Randy's inspection by Talk; Sharon keeps the wage book, and her
  opinion of your work lives in her talks like everything else true.
  🛎 **Shop Help** — afternoons (Mon/Wed/Fri) in the shops' shared help
  pool: one counter a week on a rota (Tweek Bros → Jimbo's Guns →
  Freeman's Tacos, a pure function of the week), stocking and the till
  by Look, the day's regular served by Talk — each counter keeping its
  keeper's own hours.
- **Getting and keeping one.** Apply by Talking to the boss at their
  counter in work hours (the Talk gate's new WORK rail, between DINNER
  and FACTION_MISSION) — granted unless the boss's circle counts you
  Suspect-or-worse (standing ≤ −10, Factions' own line), and a refusal
  is spoken once and not re-offered that day. One job at a time. Quit
  anytime via the boss, no penalty — the week's tab still pays out.
  Fired only for cause: `MISS_LIMIT` 3 consecutive unexcused misses,
  with the boss's word escalating reminder → warning → let go. Any
  attended shift resets the count; a partial shift (clocked in, day ran
  out) pays its landed tasks and holds the streak.
- **Reputation and pay.** A shift is clock in → a card of 3–5 judged
  tasks → clock out; pay accrues to the week's tab and lands on payday,
  the first morning of a new (Thursday-anchored) week, in the morning
  report. Streak tiers pay: at 5 consecutive completed shifts a Steady
  hand earns +1 a shift, at 12 a Trusted hand +2. A worked shift plants
  the `work` thread (Threads' new bank follows up on it) in the boss's
  memory — and in Sharon's on the farm, the served regular's at the
  counter. While you hold the shop job, the week's keeper is at their
  counter for your shift (the gathering override that finally puts Chef
  at Freeman's), and your regular is there while you're clocked in.
- **Excuses.** A shift missed because the town was having one of its
  days is excused before it counts: a case climax due, a finale week
  live that day, or a family table you actually sat at — evenings only,
  where the dinner window overlaps the shift. The set-pieces outrank
  work; every boss in town knows it.
- **The record.** The Journal gains a 💼 WORK page: the job (or the
  three doors and who to ask), the schedule, shifts and streak with its
  tier, the tab and lifetime wages, the last payday line, today's
  status, and any miss warning standing on the record.
- Tuning ledger: wages paper base 2 + 1 × `ROUTE_SIZE` 4, farm/shop
  base 3 + 2 × 3 tasks; tiers `STEADY_STREAK` 5/+1 and
  `TRUSTED_STREAK` 12/+2; `MISS_LIMIT` 3; hire floor
  `HIRE_STANDING_FLOOR` −10; the clock-in windows and shift ends as
  listed — all in `Jobs`, awaiting a device.

### v0.6.0d (versionCode 49) — Family evenings: dinner at theirs, dinner at yours
Queue #6 item 4. The town's households have eaten dinner behind their own
doors since v0.1 — the 18:00 agenda blocks were always a meal. Now the
table has a chair for the new kid (new pure module, `Dinners.kt`, all
state riding the existing `life`/`lifeText` maps — no new save keys).
- **The invitation.** Three evenings a week (absDay % 7 ∈ {1, 3, 5}), the
  rotation — a pure function of the calendar, anchored by the rollover
  like the errand board — seats one of seven households: the Marshes,
  Broflovskis, Cartmans, McCormicks, Stotches, Blacks and Tweeks, each
  with its own dish, ritual, table-talk flavor and one small household
  moment that lands at the table and sticks (agent memories, a Threads
  topic, the Journal page). Tucker and Testaburger have no house in town
  to eat in and are honestly absent. The invite rides the morning report
  and a fact-only phone text from the host (new triggers `dinner` /
  `dinnerthanks`, derived in the morning harvest like everything else).
- **The dinner.** 5:30–8:30 PM (ends well before the town's late night):
  the family gathers at home by schedule override, and three beats play
  through the existing verbs — the welcome (Talk the host, on the Talk
  gate's new DINNER rail, between FINALE and FACTION_MISSION), table
  talk (Talk any of the family), and lend a hand (a Look around the
  room). A full evening pays the host +3 / the family +2 at the midnight
  wrap; leaving early pays +2/+1 and is still warm; a no-show records
  nothing at all.
- **Hosting.** Once your house holds ≥ 4 decor pieces and a household's
  host counts you a Friend, the new dining-table fixture at Your House
  invites them to yours instead (10-day per-household cooldown; hosting
  supersedes that night's invitation). The same three beats, judged by
  what the house actually holds: the spread serves (and consumes) a
  stocked pantry item, and the guests' table talk is composed only from
  true state — the decor up, the shelf's count, the feat cups, the
  album's commissioned prints, the town's standing marks. Score ≥ 4 of 5
  is a well-hosted night (+4/+3, a "hosted" mark, and at the errands'
  own thank-you odds a gift or a Pocket Pal left on the table); a thin
  night is still a fine one (+2/+2); a stood-up table spends only the
  cooldown.
- **Deference, everywhere.** The rotation skips a household whose
  evening collides with a live finale (its gathering days, its place),
  a holiday beat at its table, or a case chapter whose cast is the
  family; the gathering override yields to the finale's; and while a
  case climax is on the table, dinner beats politely wait. The Journal
  gains a 🍽 FAMILY EVENINGS page: tonight's table, dinners at theirs
  and at yours, and who's still to visit or host.
- Tuning ledger: dinner evenings absDay % 7 ∈ {1, 3, 5} and the window
  17:30–20:30 (`Dinners.DINNER_WEEKDAYS` / `DINNER_FROM` / `DINNER_TO`);
  hosting gates at `HOST_MIN_DECOR` 4 pieces and the Friend tier
  (friendship ≥ 30, `Relationships.FRIEND`); `HOST_COOLDOWN_DAYS` 10;
  the spread's score (served 2 + conversation pieces up to 2 + full
  beats 1) with `WELL_SCORE` 4; pays full +3/+2, early +2/+1, hosted
  well +4/+3, hosted fine +2/+2; thank-you odds 1-in-`THANK_EVERY` (4),
  a card one-in-`PocketPals.ERRAND_CARD_EVERY` of those — all in
  `Dinners`, awaiting a device.

### v0.6.0c (versionCode 48) — Mystery arc #4: "The Night Gardener"

- **The fourth case** (`Mystery4.kt`, pure Kotlin): a warm case
  with a different skeleton — the investigation follows FIXES,
  not a crime. Around town, things are being quietly repaired
  overnight: a bench that wobbled stands solid, a dead fountain
  runs again, planters nobody planted bloom. No damage, no
  note, nobody taking credit. The town is delighted and nosy in
  equal measure, and Mayor McDaniels would very much like to
  throw somebody a thank-you ceremony.
- **Structure:** four chapters on the episode machinery, each
  opening after a new overnight fix appears somewhere in town —
  and the fix is real dressing: it appears in the scene,
  improves that spot's Look text, and STAYS fixed forever.
  Twelve clues in kinds the earlier cases underused — the
  pattern of which complaints got answered, materials traced
  to their sources, the same careful workmanship everywhere,
  witnesses who almost saw someone, and two finds earned by
  working a chapter's turf yourself. One suspect also moves
  around town at night, for reasons of his own that the
  evidence itself clears. Assembled, the deduction is
  mechanically real and re-checkable.
- **The finale is a vigil, not an accusation:** when the
  pattern says the fixer will come, you wait at the spot,
  after dark, with enough of the case in hand (8 of 12 clues,
  from 5 PM, at the last chapter's climax). What you learn
  there is the truth — and the choice is about GRATITUDE,
  not guilt. **Name them** and the town gets its ceremony;
  the fixes stop, but one last, personal fix lands at your
  own door the next morning. **Guard the secret** and the
  fixes keep quietly coming, a few a year, forever. **Join
  in** and a two-hands tradition starts — repairs signed by
  two different hands appear now and then, and the board
  marks the town "kept by two gardeners, one unknown." Never
  work the case and it closes cold one winter — the board
  wistful, the last spot never fixed.
- **Queue discipline:** the case cannot even schedule until
  the third case retires, then opens ten days later; chapters
  weekly, holiday-deferred, one case in the slot at a time —
  the Journal board now carries all four cases as town
  history. Saves are additive (`arc4` + `mystery4Clues`),
  decode-clamped; the season finales and the Walk defer to
  the vigil like every climax; the case's ending counts for
  the case-closer's wardrobe unlock.
- Tuning ledger: the case opens `OPEN_DELAY_DAYS` (10) days
  after the third case retires; chapters `CHAPTER_GAP_DAYS`
  (7) apart; the vigil needs `VIGIL_MIN_CLUES` (8) of 12 and
  `VIGIL_FROM_MIN` (17:00) dusk; the named ending's last fix
  lands `LAST_FIX_DELAY_DAYS` (1) morning later; the guarded
  ending's later fixes stand at +30/+60/+105 days, the joined
  ending's at +21/+55/+95.

### v0.6.0b (versionCode 47) — Errands #2: favor chains
Queue #6 item 2. An errand stops being a transaction and
becomes a **thread through the town**: some asks are the
head of a favor chain, and landing one favor pulls the
next one loose.

- **Chains, 2–3 links.** A share of generated asks
  (board and direct asks about a third of the time,
  shopkeeper asks rarely — the dials are named in
  `Errands`) are stamped chain heads when they're
  generated — a pure function of the ask, so the board
  stays the same deterministic board it always was,
  and nothing about single errands changes. Taking a
  head grows the whole tail behind it: link 2 is a
  favor for the first link's beneficiary (a delivery's
  recipient, a photo's subject — or the asker) or one
  of their relations, drawn by the same generator from
  the same obtainable universe; on a three-link chain
  the final link lands back on the origin asker's
  doorstep. The offer says so up front ("🧵 Town word:
  this one has the smell of a chain").
- **The chain is the unit.** It occupies ONE plate
  slot — only its live link rides the plate. Landing a
  link reveals the next with a voiced "one more thing…"
  at every judge surface (Give, Talk, Look, the fixture
  taps, and the Journal's lately list). The Journal's
  📋 ERRANDS page draws the thread — landed links
  checked, the live one lit, the origin asker waiting
  at the end (✓ → ▶ → … → 🏁). Dropping the live link
  drops the whole chain, with the origin asker's sigh
  and no other cost, as ever.
- **Links never strand.** Each link was composed from
  judge inputs that provably exist; at reveal it
  re-validates against the world as it now stands, and
  a stale link is substituted with an equivalent ask
  (same type, same person) rather than left impossible.
- **Pay.** Links pay the ordinary errand rates and
  count toward the errand feats individually; the
  final link adds a closing bonus from the origin
  asker (coins + friendship, per link in the chain).
  Two chain feats join the errand family in Collection
  (🧵 Thread Puller at 1 chain, 🧵 Town Weaver at 10).
- **The town notices.** Everyone involved carries a
  "favor chain" topic in their conversation memory
  (Threads has a follow-up bank for it); the morning
  report keeps a 🧵 chain beat; and a friend of the
  origin asker will gossip about the chain when you
  meet them — once per chain per day, never in quiet
  hours. The origin asker's thank and morning text
  arrive only when the final link lands.
- **Saves:** no new keys — chain state rides inside the
  errand records themselves (additive fields, absent on
  singles, which serialize exactly as before) and the
  finished-chain count in the capped `life` map. A
  v0.6.0a save loads untouched, its errands all singles.
On-device playtesting still needed (no emulator on the
build VM) — as ever, the tuning ledger lists what's set
by paper math.

### v0.6.0a (versionCode 46) — Photo assignments: Queue #6 opens
Queue #6 item 1. The town stops just posing for your album
and starts **commissioning specific pictures** (new pure
module `Commissions.kt`).

- **The weekly board.** Five fresh asks pin up every week —
  a pure function of the week, its calendar, and the askers'
  posting history, derived on every screen and stored
  nowhere (the errand board's discipline, at week scale).
  Each ask is a SUBJECT (one named townsfolk, a named pair,
  or a named trio — the asker may star in their own ask), a
  PLACE, and optionally a CONDITION: a posed group frame (a
  named composition where the composition math guarantees
  it, "any group frame" where chemistry decides), the
  season, or a weather family. Asks live on the album side:
  a new **📷 COMMISSIONS** page in the Journal, beside the
  album they end up in.
- **Schedule-proven, or it doesn't post.** The generator
  composes asks only from pieces the cast's own agendas
  guarantee: a subject set is offered at a place only when
  every subject is there, awake, at the same minute of some
  day (co-presence computed from the agenda intervals); a
  season condition only posts when the ask's whole
  two-week live window sits inside that season; a weather
  family only when the season's drift table can produce
  it. And an asker never repeats their last ask — each
  week's signatures fold into a no-repeat memory at the
  week boundary and the generator re-rolls until it moves.
- **The plate.** Take up to 3 asks on; an accepted ask
  lives 7 days from the day you take it, and lapsing (or
  letting go) costs nothing — no friendship hit, ever.
  While you hold someone's ask, they say so when you talk
  (once a day, not a nag).
- **Judged by the shutter itself.** Photos.applyPhoto
  settles commissions beside PHOTO goals and PHOTO errands,
  against the record's own facts: subjects by name, place
  by (location, inside/outside), condition by the record's
  composition, day stamp, or weather. A match pays Capsule
  Coins scaled by difficulty (2/3/5 by subject count, +2
  for a condition) + 5 friendship with the asker, notes the
  asker's morning text on your phone — and stamps the
  photo a **COMMISSIONED PRINT**: the asker and the ask
  itself, worn in the album forever. Prints never fade
  (the album cap sheds ordinary photos first). A near miss
  — right people, wrong place or frame — gets an honest
  "close, but…" from the asker next time you talk, once.
  One photo can satisfy two asks; the first owns the stamp.
- **Feats.** A commission family joins the photo feats in
  Collection: 📷 First Commission, 📷 Town Photographer
  (10 prints), 📷 The Fridge-Door Gallery (25).
- **Saves:** one additive key (`commissions`: plate,
  signatures, count, recent prints — decode-capped and
  registry-filtered) + the stamp's two additive fields
  inside photo records. A v0.5.0i save loads with an
  empty plate.
On-device playtesting still needed (no emulator on the
build VM) — as ever, the tuning ledger lists what's set
by paper math.

### v0.5.0i (versionCode 45) — Polish & performance pass #5: the Queue #5 audit
Queue #5 item 9, and the queue closes. Like passes #1–#4:
audit, measure, tighten — no new features, and **zero save
changes** (this build touches no production code at all;
the APK differs from v0.5.0h only in its version badging).

- **The soak completes the set.** The 400-day lifetime now
  also lives Queue #5's surfaces end to end: 66 walking
  trips (137 legs, 42 shortcuts learned, the bus pass
  earned inside the year and 18 rides taken), the wardrobe
  unlocking off lived feats and seasons (7 grants), a
  Pocket Pals collection pursued at the machine and the
  trade table (80 pulls, 13 trades with the last-copy rule
  watched on every one, and the binder complete — 24/24),
  faction chains pushed through their standing gates on
  organic income alone (16 beats landed, 3 chains finished),
  the phone texting all year under its caps (502 distinct
  texts seen; the store never passed 120, unread never
  passed 14), weekly crowds gathering and clustering
  (57 of 57 days), and all four season finales lived in
  the same year the cases, arcs and holidays were — autumn
  played and called, winter ignored outright (its mark
  says so), spring played and left to the town, summer
  played and called the other way.
- **Veteran save IV.** The saturated save re-measured with
  Queue #5's state at its caps — every directed street's
  shortcut learned, the full wardrobe, a binder at the
  99-per-card cap, all eight chains masked complete, a
  phone store sitting exactly on 120 texts / 24 notes,
  four standing finale marks: 286,249 chars serialized,
  comfortably under the 512K ceiling, and every Queue #5
  field proven to round-trip at its cap.
- **Newest screens fit QA.** The Phone home and a Messages
  list at maximum (a thread per cast member, every status
  flavor), the SEASON FINALES page with four standing
  marks, the Pocket Pals binder at full, and the WALKS page
  with a veteran's numbers — the longest real lines each
  screen can produce, at font scales 1.0 and 1.3, under the
  standing Chrome invariants.
- **The rail order, final form.** TalkGate's five rails
  pinned under fabricated total contention: Jimbo — the
  one townsfolk who can hold the whole stack — simultaneously
  holding a delivery, the Cow Days vigil's current step,
  the regatta's opening beat, the Tegridy chain's offer, a
  shop ask at his own counter, three owed lines (holiday
  ambient, finale ambient, an errand thank-you, in that
  order) and a bark on cooldown; the gate peels DELIVER →
  HOLIDAY_BEAT → FINALE → FACTION_MISSION → ERRAND_ASK →
  DIALOGUE, each through its own judge. Alongside, the
  finale verdict's climax deferral pinned on live state:
  due on the last day, deferred while the pond case's
  climax is on the table, and still there — landable —
  the moment the case settles.
- **Figure/depth seam audit.** The figure pass and the
  depth pass shipped back-to-back; their seams are now
  pinned where they meet: the crowd matrix run to the
  full 44 (the v0.5.0g harness stopped at 24) with the
  frieze chin-clearance and MIN_SEP floor holding at every
  headcount, indoors and out; 43 heads × 23 hats of worn
  combos rasterized (cast hair under every hat the creator
  can hand out — no hair through a covering dome, no hole
  at any crown; the towel is exempt by construction — it
  carries no head stack at all, and that is now pinned as
  the design it is); and group photos at the album's real
  aspect with merged figures — crowns keep their headroom
  and no chin dips into the foreground strip the photo
  paints last.
- **Peak budget, fourth distance.** All named stacks
  re-verified on the current render set: named 0.9469,
  double-SFX 0.9776, bark stack 0.9469 — no drift since
  v0.4.0e, and Queue #5 added no new audio family for the
  budget to hide.
- **Tuning ledger completeness.** The ledger swept for
  Queue #5: every paper-tuned dial now named with its home
  (six names the sweep found missing — `COLLECTION_MILESTONE`,
  `MACHINE_COST`, `COUNT_CAP`, `TRADE_SWING_CAP`,
  `STASH_SIZE`, `BEAT_FRIENDSHIP` — are in, and a suite pin
  keeps the ledger honest from here on).
- **Defects:** none found in production code. The audit's
  surprises were all in the audit's own fabrications (a
  faction chain misattributed, a phone store threaded with
  adults the kid-network decode rightly drops, a spring
  beat addressed before its predecessor) — each fixed in
  the suite, each now a pin. Suite: 57,511 checks,
  0 failures.

**Queue #5 is complete with this build.** The next queue
opens at v0.6.0.

### v0.5.0h (versionCode 44) — Season finales: every season ends with a set-piece
Queue #5 item 8. A daily episode is a day and a holiday is
a window; a finale is a WEEK — the last seven days of a
season by the game's own season boundaries (spring ends
May 31, summer Aug 31, fall Nov 30, winter the last day of
February) — and each of the year's four seasons now ends
with one (new pure module, `Finales.kt`, state riding the
existing `life`/`lifeText` save maps, capped on read):

- **🌷 Spring — The Great Garden Verdict** (Kid's Park,
  Sharon hosting): a judged garden show — plots staked,
  a suspicious bag of Tegridy Miracle Dust behind the
  kale, Sergeant Sprout's pep talk, Randy's confession.
  The call: crown Sharon's heritage tomatoes, or crown
  Butters' one brave bean. Either way a painted sign
  stands at the plot until next spring.
- **⛵ Summer — The Pond Days Regatta** (Stark's Pond,
  Jimbo & Ned): the paper-boat fleet musters — a newspaper
  dart, a boat made of a D-minus math test, a sheet of
  paper that identifies as a boat, and one cafeteria-tray
  "yacht" — with a Talk/Look/Talk chain and the official
  fleet PHOTO as its beats. The call: Stan's dart by a
  fold, or Kenny's homework boat, sunk twice and finished
  anyway. The winner's mark is varnished to the dock post
  under a brass plate.
- **🌾 Fall — The Harvest Weigh-In** (Tegridy Farms,
  Randy hosting): BEHOLD. THE SQUASH. — plus a backup
  squash in the barn, Stuart's honest pie-first pumpkin,
  and the farm scale's five-pound secret (the certified
  county hook exists, and Sharon has opinions). The call:
  weigh it true (a dead tie, a shared crown, two names on
  the farm-gate board) or let the generous scale stand
  (Randy by four pounds, with Stuart's penciled footnote).
- **🕯️ Winter — The Longest Night Lantern Walk** (Main
  Street, Father Maxi): the quiet closer — lanterns
  strung over Main Street, Butters' two-mitten candle
  system, Kenny's pickle-jar lantern with its undefeated
  candle stub. The call: carry the last lantern yourself,
  or hand it to Butters. The mark: the bench's bright hook
  and strings that stay up past the snow — or a small
  scorched lantern retired in the church window, labeled
  IT STAYED LIT. (MOSTLY.) (IT COUNTS.)

The shape is the same all year: an **opening day** (the
ensemble gathers at the finale's place by schedule
override, 9:00–18:00, on the opening and closing days
only; the host's declaration lands as the first beat and
the finale gets the full episode treatment — title card,
acts, credits), **four judged beats** across the week in
the holiday-beat idiom (Talk to the person, Look at the
place, one Photo for the Regatta — ordered, day-ranged,
expiring gracefully), and a **closing call** on the last
day. Machinery discipline, all of it pinned in the suite:
the finale **pre-empts the daily episode roll** for its
whole window (mystery chapters still take the slot first);
its call **defers while any case climax is due** — openings
wait a day rather than stack a title card, and the verdict
dialog presents last in the safe-moment chain, behind all
three cases; a Talk gate **FINALE rail** sits between
HOLIDAY_BEAT and FACTION_MISSION, under DELIVER as ever;
ignoring a finale is safe — it **wraps without you**, the
town makes the call, no friendship and no decor are paid,
and the Journal records the empty chair. Playing one pays
friendship across the ensemble (host +3, ensemble +2,
winner +2) and earns that season's **house decor piece**
(the House registry grows 8 → 12; all twelve drawn in the
room). The **standing mark** each call leaves lasts until
the same season's finale wraps again next year; the next
opening acknowledges whoever history credits — your call
or the town's — before the new week starts. Notices: the
bed's morning report announces the opening morning and
carries a summons on the closing morning; ensemble members
reference the week in conversation (once a day each); the
Journal gains a **❄ SEASON FINALES** page — live progress
in the window, and every standing mark with its year and
its author. All writing is original; no show material is
quoted. Saves additive-only, as ever.

### v0.5.0g (versionCode 43) — Scene depth pass: background, foreground & crowd clusters
Queue #5 item 7, and a direct playtest correction from
Lucky — his words are the whole mandate: "add a background
and foreground and move props and buildings accordingly —
also group large groups of people together into clusters
to avoid clutter." Until now a scene's depth was a habit:
the renderers drew in an order nobody had written down,
and a crowd spread itself into one even, cluttered row.
Now the depth is data (`Depth.kt`, pure like `Horizon`):

- **Three bands, one registry.** Every scene is BACKGROUND
  (sky, horizon, the buildings — the location's own facade
  and the neighbors' silhouettes are ONE background
  construction now, not two), MIDGROUND (the play space:
  light pools, tappable props, the crowd) and FOREGROUND
  (the near strip between the crowd and the camera).
  `Depth.EXTERIOR_LAYERS` / `INTERIOR_LAYERS` are the
  canonical stacks in draw order, and EVERY dressing token
  in the game — parity, season, light, silhouettes, dig,
  faction marks, interior details, the interior prop
  vocabulary, and the two new foreground registries —
  resolves to a band in `Depth.TOKEN_BANDS`. The suite
  pins the renderers' real call order against the stacks
  marker by marker, and fails if any token is bandless or
  claimed by two registries.
- **The foreground layer.** Each of the 27 locations wears
  its own near dressing along the scene's bottom strip —
  Kenny's broken fence tops, the Bus Stop snowbank lip,
  the Stark's Pond dock planks, the Elementary bike rack,
  the Bijou's rope line — and each interior theme its near
  furniture: the sofa's back at home, the counter front in
  cafes and diners and the bank, the pew backs at church,
  the seat backs at the Bijou and on the bus. Painted by
  one kind-driven painter per renderer (`FgKind`), colored
  from the location's palette or the room's floor/wall,
  weather-worn and night-mixed like every other layer,
  and drawn OVER the figures' feet. The strip lives under
  a pinned occlusion budget: its top edge sits ~0.03 below
  the front row's feet and clears the worst possible chin
  (tallest measured sheet × smallest head share) by more
  than 0.15 of the scene — it can touch ground and ankles,
  never a face or a tap target. PhotoView draws the same
  strip in group photos, from the same painters.
- **Props re-sorted, interactions untouched.** Bands are
  rendering only: the bench you sit on and the counter you
  tap keep their hits, their Look text and their fixtures;
  only the paint order knows. The signposts stand in the
  foreground band and paint after the frieze, so their
  boards and hints stay legible over it.
- **Crowd clusters (`Crowds.kt`).** At 7 or more present,
  the even spread gives way to conversational clusters of
  2–4, composed from the town's own social data: pair-script
  partners pull hardest, then bonds (3×), then same-faction;
  arch-rivals never share a cluster (complete-linkage: EVERY
  cross pair must be willing) unless a pair script makes
  them a double act. Clusters never split across rows, gaps
  between groups stay visibly wider than the tight in-group
  pitch, members right of their group's center draw mirrored
  so the huddle turns inward, clustered strolls shrink to a
  shuffle, and the layout is a pure function — same crowd,
  same chemistry, same huddle, every tick. Under 7, the
  classic spread runs byte-for-byte as before (the old slot
  math, stagger and #41 wide-figure shifts now live in a
  pre-pass so both paths share one draw loop).
- **Everyone stays tappable.** Figure taps no longer take
  the last-drawn rect: a tap resolves to the NEAREST figure
  center among the kid/visitor hits under the finger
  (ties break deterministically), and cluster geometry
  pins a minimum center separation (`Crowds.MIN_SEP`) so
  nearest-center is always an honest answer. Nothing is
  stored — placement is derived per frame, so saves gain
  zero keys.

### v0.5.0f (versionCode 42) — The figure pass: hair, hats & the seamless merge
Queue #5 item 6, and a direct playtest correction from
Lucky — his two messages are the whole mandate: (1) "The
hair is broken — make sure it covers the head correctly
and works with the hats." (2) "Make the characters a bit
more stylized in South Park and make sure that the
independent shapes of the people are seamlessly merged to
make the character."

- **The head stack is data now.** Behind-hair, head, hair
  and hat used to be four hand-drawn path piles in KidArt
  whose arcs were eyeballed per style — that is how domes
  ended up bare (long hair had NO front cap at all: Wendy,
  Heidi, Red and Jesus wore skin domes with two side
  locks), how a buzz cut's corners floated off the skull,
  and how Sheila's beehive stood on bare skin at its base.
  All of it is now pure ink shapes — `HeadStack` in
  `CharacterSystem.kt`, in the GroupGeom tradition: every
  cap and dome is built on an ellipse PADDED past the
  head (hair +2 units, hats +3.5), so the hair mass
  provably contains the dome sector it covers and a hat
  provably hugs the head it sits on. KidArt paints
  EXACTLY those shapes; the JVM suite rasterizes them
  with their own point tests — the measurement IS the
  drawing. Kenny's hood + face ring are head-stack shapes
  too; Towelie keeps his own renderer.
- **Hair rules, per the sheets.** Every style keeps its
  sheet hairline (fringe line at eyeT−6, per-style edge
  curves), now as a filled cap on the padded ellipse: no
  skin slivers inside the hair region, no floating
  corners. Long hair gains its missing front cap, and its
  behind panels fall past the shoulders, tucked under the
  torso. The afro gains a front cap over its halo.
  Garrison stays honestly bald — fringe bands at the
  sides, skin dome — because his sheet says so.
- **Hats over hair.** Covering hats (beanie, caps,
  pomhat, ushanka, chullo, trapper, bandana, santa,
  sleuth) no longer delete the hair: the cap mass yields,
  but the fringe survives as a strip wherever the sheet's
  hairline dips below the brim (the swoop's low side,
  curtains), temple tufts peek at both sides, flaps own
  the temples (Kyle's ushanka over his afro, Craig's
  chullo), and below-brim length — locks, bob frames,
  ponytails — stays. Perch hats (beret, yarmulke,
  earmuffs, headband, cowboy, wizard, crown, toque, taco)
  sit ON the full head of hair. Spikes and buns are
  swallowed by dome hats instead of poking through. The
  wizard's star now sits in his cone's bent tip instead
  of floating off its edge; accent puffs are reseated
  into their domes. And the creator's Lorde wig option —
  previously drawn by NOTHING — renders as a wig: a bob
  frame in the hat color over the wearer's hidden hair.
- **The seamless merge.** One silhouette: hair and hat
  masses overlap the head by construction, the chin's
  2-unit overlap and the sleeve roots buried in the torso
  are pinned by the suite, same-color parts share fills
  with no internal strokes, and face + details draw after
  the merged base, as before. Legs keep their split —
  it's part of the style. The stylization push is
  rendering-only: fuller masses, bolder and fewer
  micro-shapes (wider mop/messy scallops, seven long
  spikes in place of nine short ones, bigger curls); NO
  measured SheetSpec changed. PhotoView renders through
  the same pipeline, so album selfies change with the
  town — intended: Lucky's correction outranks the old
  byte-identity courtesy. Saves untouched.

### v0.5.0e (versionCode 41) — The phone: the town in your pocket
Queue #5 item 5. The New Kid gets a phone — not a new set of
systems, but a pocket door into the ones that exist, plus
the one room the town was missing: a phone that buzzes about
YOUR day. A new pure registry, `Phone.kt`, owns the hub, the
message store and the morning harvest; `PhoneActivity.kt`
renders it in the Journal's paper language.

- **The hub.** 📱 PHONE sits on the bottom bar and in the
  ☰ menu. The home screen is a status strip (the town's
  time, the sky's icon, your Capsule Coins, the unread
  badge) over six tiles — 📖 Journal, 📸 Album, 🗺 Map,
  💬 Messages, ⚙️ Settings, 🎮 Games — and every tile opens
  the EXISTING surface: the Journal itself, the Journal at
  its album and games sections (a new scroll anchor, same
  page), Settings, and the 🗺 Go Somewhere picker, which the
  phone reaches by handing its route back to GameActivity
  as an activity result. Nothing is reimplemented; the
  phone is a hub, not a rewrite.
- **Messages — the town texts you.** Overnight, the day's
  facts become short original texts in per-sender threads:
  a friend comments on a **feat** you earned (every feat
  family notes at its own award site — the shutter, the
  games, errands, the binder), the person **in your photo**
  saw it ("who took that?? (it's good)"), an **errand's**
  asker follows up, someone is jealous of an uncommon-or-
  better **Pocket Pal pull** (commons are beneath notice),
  a **story-arc** beat lands from the arc's own lead, a
  **mystery chapter** opening earns exactly one premise-
  vague text — never a case fact, guarded by a denylist
  scan over every template and a generated corpus — the
  **birthday** kid hints at their own day, a **holiday**
  warns the day before and reports on the day, **weather**
  worth complaining about gets complained about, and close
  friends (**Friend tier, 30+**) send plain hellos, paced:
  at most one hello morning in two, three days between one
  friend's hellos.
- **The rules that keep it honest.** Texts are harvested
  at rollover (`Engine.onNewDay`, right after the story
  arcs tick) from **noted facts + derived triggers**, and
  every word is a pure function of the fact that caused it
  — seeded per (day, trigger, subject) — so a save/reload
  can NEVER reroll a text, and the same morning never
  harvests twice. A morning caps at **4 new texts**, one
  per sender; facts that don't fit wait for tomorrow. The
  inbox caps at **120 texts / 20 per thread / 30 unread**
  (a full inbox stills the town rather than piling into the
  void), pending facts at 24, and everything trims
  oldest-read-first. Read state persists inside the one
  additive save key.
- **Replying.** Each thread carries a canned quick-reply
  row — 👍 😂 ❤️ (free typing is out of scope by design).
  A reply lands as your bubble, warms the friendship by
  **+1** (at most **3 paid replies a day**), plants a
  "texting" topic in that friend's memory for later talks,
  and half the time earns a canned ack — itself a pure
  function of the conversation, so it can't be rerolled
  either. **Kenny's** texts arrive Kenny-typed: lowercased,
  texting-shorthand, "mmph." up front — garbled but
  readable, audited word-by-word in the suite (97%+ of his
  words survive the muffle; the substitutions never delete
  a word). Thread lists order by most recent, and every
  sender shows a one-line status derived live from where
  they are and what they're up to — "at Tegridy Farms",
  "probably scheming", "💤 fast asleep" — no new sim, just
  the town, read aloud.

### v0.5.0d (versionCode 40) — Faction missions: eight chains of work
Queue #5 item 4. The eight factions have had standing, mood,
turf and opinions since v0.1.5 — now they have WORK for you:
one escalating three-beat chain each (an errand, a scheme, a
set piece), every beat in its faction's own obsession's
voice, in a new pure registry, `FactionMissions.kt`.

- **The chains.** The Boys' Crew claims the pond (a lookout
  to bribe, a war chest to fund at the Taco cabinet, colors
  to raise and photograph); the Goth Kids run a conformist
  census that ends in a portrait nobody may smile for; the
  Girls' Circle builds the list to end lists and posts it at
  the Mall in the evening; Craig's Gang executes the Great
  Whatever, defended at the Showdown fort; the Parents
  relaunch the Neighborhood Watch and re-hang its sign; the
  Staff restore the Hall of Honor, plaque by plaque; the
  Church labels its lost-and-found on a Sunday morning; and
  Tegridy & Local Business raises the Tegridy Standard, dead
  level, by the farm gate. 24 beats, judged ENTIRELY by verbs
  the game already had — Talk, Give, Look, Photo, Catch,
  Showdown, Taco — riding the same rails as errands and
  holiday beats (two beats keep office hours: an evening
  posting, a morning labeling).
- **Offers & gates.** A faction member offers their chain's
  next beat through the Talk gate's new **FACTION_MISSION**
  rail — pinned below the delivery and the today-only
  holiday beat, above a fresh errand ask. Beat 1 is open at
  any standing; beat 2 wants **Liked (10)**, beat 3
  **Respected (30)** — the standing labels' own thresholds.
  Declining costs nothing and strands nothing: the offer is
  simply still there next time. While a mystery finale /
  accusation / disposition is due, offers WAIT (the road
  beats' silencing rule); beats already taken on still land.
- **Stakes & marks.** A landed beat pays its faction
  8/10/12 standing, 2/3/5 coins, +4 friendship. Finishing
  beat 3 leaves a **permanent mark** — a small scene-dressing
  token at a named location (a claim flag at the pond, a
  poetry panel and a brass plaque at the school, a list board
  at the Mall, a banner at the park, a watch sign on Main
  Street, a labeled box at the Church, the Standard sign at
  the farm), drawn by a new SceneArt mark layer in smooth
  shapes only — plus a **faction title** you carry on the
  Journal's 🪪 YOU card and in the town log. It also costs
  **4 standing with the chain's named rival**, telegraphed
  in beat 3's offer text; the rival map is a perfect pairing
  (Crew↔Circle, Goths↔Church, Parents↔Tegridy, Craig's↔
  Staff), so a lifetime of chains can never cost any faction
  more than 4 — the suite runs all eight chains in sequence
  in one save to prove no lockout exists.
- **Surfaces.** The Journal gains an **⚔️ FACTION MISSIONS**
  page (chains, armed objectives, waiting beats with their
  gates named, finished marks and titles); the 🪪 YOU card
  carries earned titles; marks stand in the world via
  `TownInfo.factionMarkTokens`, derived — like the titles —
  from the chain masks.
- **Saves**: **zero new keys.** Progress is two entries per
  faction in the existing `life` map (a beat bitmask, the
  armed beat's number); marks and titles derive from the
  mask, so they appear exactly at beat-3 completion and can
  never be lost. A v0.5.0c save loads with every chain
  untouched.
- **Verification repair (post-build, same version)**:
  an independent re-run of the full suite against the
  shipped tree found 3 failures the original build's green
  claim missed, all fixed in place and the APK rebuilt:
  (1) the Goth Kids' beat-3 offer telegraphed its rival
  cost only by allusion ("the Father Maxi fan club") — it
  now names the Church outright, as the other chains name
  theirs; (2) Craig's Gang's beat-3 offer said "Staff" —
  it now names the School Staff; (3) the Talk-gate order
  pin searched TalkGate.kt's source unscoped, so its
  `indexOf` measured the header essay (which names
  `Errands.pendingAsk` while documenting it) instead of
  gateFor's body — the pin now scopes its search to the
  function body; the code order (DELIVER → HOLIDAY_BEAT →
  FACTION_MISSION → ERRAND_ASK) was already as designed
  and is unchanged. Final suite: **40,636 checks,
  0 failures**, re-verified from a clean rebuild.

### v0.5.0c (versionCode 39) — Pocket Pals: a 24-card binder economy
Queue #5 item 3. The town gets a collectible card game of its
own — **Pocket Pals**, 24 creatures invented whole for this
game (names, flavor, designs: original parody, guarded in the
suite against a franchise denylist; all art code-drawn on
Canvas from a per-card spec — body shape, palette, feature
flags — so the same data draws the same card forever, with
solid shapes, stripes and stars only). A new pure registry,
`PocketPals.kt`, owns the set, the machine, the trades and
the feats.

- **The set.** 24 cards in four schools (⚡ Spark, ❄️ Frost,
  🍃 Leaf, 🪨 Grit, six each) and three rarities — 14 common /
  7 uncommon / 3 rare. The chase rare is **Aurorath, the Storm
  That Stayed**; the other rares are The Pondmother and Old
  Man Tanglewood. Favorites at this desk: Sproutato ("Half
  potato, half ambition. Fully planted."), Cubecicle
  ("Perfectly square. Melts into a perfect puddle. No notes."),
  Quarrion and its collection of "the good rocks".
- **The faucet — the capsule machine.** A new fixture on the
  Mall's center wall (between the clothing rack and the
  transit kiosk) sells a pull for **2 Capsule Coins**: a
  weighted roll over the 22 machine cards (per-card weights
  100 common / 36 uncommon / 9 rare — rares land ≈1.1% of
  pulls), dupes allowed and counted. **Errand thank-yous** can
  land as a card instead of an item — 1 in 5 thank-yous, i.e.
  1 in 20 completions, machine stock only, pinned in
  `PocketPals.ERRAND_CARD_EVERY`. And exactly **2 cards are
  Look-find exclusives**: The Pondmother under the dock at
  Stark's Pond, The Pollenator in the playground garden at
  South Park Elementary — they never come from the machine.
- **The sink — trading.** Kids collect too: every cast member
  holds a deterministic **weekly stash** (3 cards, seeded by
  name + week) and a **valuation quirk** — their obsession
  school (deterministic per cast member) is worth **double to
  them**. Card values run 1 / 3 / 9 by rarity. From the Talk
  sheet, **🃏 Trade Pocket Pals** appears when a trade is on
  the table: you offer a dupe they covet (their school, or a
  card their stash lacks), they offer a stash card you lack —
  one-for-one, friendship moving by the value gap **capped at
  ±6** (they're kids, not brokers). One trade per kid per
  week; declining is free and funny; and **nobody trades away
  your last copy** — offers are dupes only, re-checked at
  settle time.
- **The album.** The Collection gains a **🃏 POCKET PALS**
  section: 24 slots drawn from their specs, owned with dupe
  counts (×3), missing as silhouettes carrying their hint
  (machine / trade / the two Look spots). Two feats, derived
  from ownership like the errand titles: **🃏 TRIPLE CROWN**
  (all 3 rares) and **🃏 FULL SET!** (all 24). The Journal's
  🎮 GAMES page counts lifetime pulls.
- **Saves**: one additive `pocketPals` key (id → count,
  registry-filtered, per-card cap 99); pulls ride the `life`
  map. A v0.5.0b save loads with an empty binder. The suite's
  seeded-collector lifetime (machine + both Look spots + the
  whole town's weekly trading) completes the full set with
  every coin accounted for and no last copy ever offered.
- **Also fixed**: the suite's release pin in `main()` still
  read 37 / 0.5.0a — it now pins 39 / 0.5.0c, so the baseline
  carries no stale failure into this version.

### v0.5.0b (versionCode 38) — Outfits & unlockables: a wardrobe the town can see
Queue #5 item 2. The creator's shelf grows by **14 pieces** — and
every one of them starts locked, with a named source and a
player-facing hint. They are ordinary creator options in the
existing category lists (creator total 114 → 128); a new pure
registry, `Wardrobe.kt`, owns where each comes from and the rails
that grant, sell and notice them. No parallel appearance system.

- **Bought (4) — Capsule Coins at real counters.** The Mall's new
  clothing rack sells the **Mall Rat Cap** (12 🪙) and the
  **Varsity Jacket** (18 🪙); Jimbo's counter at Jimbo's Guns
  sells the **Rhinestone Cowboy Hat** (15 🪙) and **Pro Camo
  Pants** (10 🪙) — a 55-coin sink next to the bus pass's 25.
  Wrong counter, already-owned and insufficient-funds all answer
  in the bus-pass vocabulary (`owned` / `bought` / `poor` /
  `notsold`).
- **Granted by feats (4) — one per feat family.** 📋 Neighborly
  (10 errands) knits the **Neighborly Scarf**; 📸 The Whole Squad
  earns the **Squad Hoodie**; 🌮 Taco Titan (500+ at Taco Catch)
  earns the **Taco Titan Hat**; 🚶 First Steps (10 trips) breaks
  in the **Road-Worn Jeans**. Crossing a feat grants its piece
  with a ticker line and a town-log line — and `syncUnlocks`
  grants **retroactively on load** for saves already past the
  feat, then re-syncs on refresh as new feats cross.
- **Seasonal (3).** Playing Halloween's signature beat (The Full
  Route) earns the **Pumpkin Sweater**; Christmas's Secret Santa
  Relay earns the **Santa Hat**; and the **Deep Winter Parka**
  comes from the season itself — its trigger, named in the data
  (`season_winter`), is the in-game calendar standing in winter
  (Dec–Feb), granted the moment the town sees a winter date.
- **Special (3).** Retiring **any** mystery case leaves the
  **Sleuth's Cap**; the 📋 Town Fixture title (50 errands) issues
  the **Town Fixture Vest**; finding **15 of the 25 collectibles**
  earns the **Completionist Scarf**.
- **Creator discipline.** Locked pieces render in the creator
  greyed, with their unlock hint printed on the cell, and cannot
  be picked; the **dice never rolls a wardrobe piece** (owned or
  not — earned is earned); presets can only save/hold owned sets
  (`Wardrobe.sanitizeAppearance` strips the unowned from any
  look on load, save and mirror-swap — a no-op by construction,
  pinned anyway). You can't wear what you don't own, so
  unlocking-while-worn is impossible by construction.
- **The mirror is the wardrobe.** Your house **MIRROR** now opens
  a wardrobe surface: one tap wears any saved preset set, one
  more opens the creator. Wearing a wardrobe piece an NPC hasn't
  seen rides the existing **outfit-comment rail** with
  piece-specific lines — and a few pieces carry named callouts:
  Jimbo reviews the Rhinestone Cowboy Hat and the Pro Camo,
  Cartman claims credit for the Squad Hoodie, Chef meets the
  Taco Hat, Butters checks the Santa Hat, and Mayor McDaniels
  acknowledges the Fixture Vest.
- **The Collection gains a 👕 WARDROBE section** (owned n/14;
  locked pieces as silhouettes carrying their hints) — chosen
  over the Journal's YOU page because the Collection is already
  the town's owned-vs-silhouette catalogue with feat sections;
  the closet belongs in the catalogue. All five new hats have
  real draw branches in the figure system; tops ride the
  existing style-token constructions, bottoms their styles, and
  the two scarves the scarf slot — so a worn piece renders in
  scenes, photos and the Journal exactly like any other option.
- **Saves**: one additive `wardrobe` key (the unlocked-piece
  set), decode-filtered to the registry — a hand-inflated doc
  can't invent a piece; a v0.5.0a save loads fully locked,
  then retro-grants whatever it has already earned.

### v0.5.0a (versionCode 37) — The Walk: the town becomes a place you cross (opens the 0.5.0 series)
Queue #5 item 1, and a new base version — Queue #4 closed with
v0.4.0g, so the letters restart. The headline is structural:
**travel stops teleporting.** Until now an exit tap moved you
along one graph edge instantly, for free. Now every trip runs on
the town's real geography — all rules pure, in a new `Walk.kt`.

- **Walking routes.** The new ☰ Menu entry, **🗺 Go Somewhere…**,
  lists every place in town priced in legs and minutes off the
  real graph (`Walk.route`: BFS shortest path over EXITS, neighbors
  in EXITS order — the same (from, to) always walks the same
  streets, so the geography is learnable; the suite pins three
  golden routes and proves all 27×27 pairs reachable, shortest,
  and deterministic). Picking a far place starts a trip and walks
  the first leg on the spot; signpost taps walk legs too. Each leg
  lands you IN the intermediate location for real — its people,
  weather, light, barks and every verb work there (suite-proven:
  presence, a photo, and a DELIVER errand all settle mid-route).
  Walking on is one more tap: the next leg's signpost wears a
  "🚶 next ▸" hint. Wandering off-route drops the plan, cheerfully.
- **Time is distance.** A leg costs minutes by true gx/gy distance
  (0.6 min/unit: a house block ≈ 8 min, Main Street ↔ Tegridy 33,
  the full Kenny's-to-the-pond crossing 87), banked into whole
  15-minute ticks through a minute carry — two short hops ≈ one
  tick between them — and run as real `inGameTick`s, so the town
  lives while you walk. (In real-time-sync mode the device clock
  owns time and legs don't tick.)
- **Shortcuts.** The first time you walk a directed leg you learn
  its cut-through — fence gap, lot cut, the way locals actually go —
  and that leg costs a full tick less forever after (floored at
  2 min). One discovery per trip (it spends the trip's beat slot),
  directional, capped at the town's edge count; the Journal's new
  🗺 WALKS page and the Collection's 🗺 WALK FEATS (First Steps 10
  trips, Commuter 50, Local 10 shortcuts) keep the tally.
- **Road beats.** At a pass-through stop (never the destination),
  at most ONE ambient beat per trip may fire from an 8-beat pool:
  an overheard argument between whoever's there (it plants a real
  topic in both memories), a dropped item or gutter coin, a
  faction kid's ten-pace recruiting pitch (+2 standing), a wave
  (+1 💛), directions for a lost visitor (+rep), a busker, the
  gnome (it travels too), and a dog that walks your leg and barks
  once, formally. The sky turning mid-leg pre-empts the pool.
  Per-beat cooldowns run in days; the loud beats keep quiet
  hours; nothing fires while a mystery finale choice is on the
  table. Yesterday's road gets one spoiler-free line in the
  bed's morning report.
- **The bus pass.** Instant travel returns — earned. The transit
  kiosk inside the Mall sells the pass for **25 capsule coins**
  (a new `bus_pass` KEY item: no loot table, gift pool or errand
  can ever deal it; ownership is a save flag, the card is the
  souvenir). Town Fixtures (50 errands) claim it free — including
  retroactively on load for saves that earned the title before
  the pass existed. With the pass, far picks in the picker offer
  **🚌 Ride**: one leg's minutes, any distance, no stops, no
  stories. Walking stays the default, and the only way shortcuts
  and road beats happen.
- **Saves**: one additive `walk` key, decode-clamped to real
  streets and real values (a hand-inflated doc can't invent a
  shortcut or a negative trip count); old saves load unwalked.

### v0.4.0g (versionCode 36) — Polish & performance pass #4: the Queue #4 audit
Queue #4 item 7, and the queue closes. Like passes #1–#3:
audit, measure, tighten — no new features, and **no save
changes at all** (the refactors below are behavior-identical;
a v0.4.0f save loads unchanged).

- **The soak completes its coverage.** The 400-day lifetime
  (in the v0.3.0g suite) now also lives Queue #4 end to
  end: board errands taken and run on a ten-day cadence,
  direct asks and shopkeeper asks on their own cadences,
  holiday beats driven in their windows, a GROUP photo
  every fourth Sunday with rotating frames (the core four,
  Craig's gang, a mixed trio, the girls, a duo), and barks
  attempted four times a day under the real rules. Final
  tallies: 117 errands completed through their judges
  (board 40 / asks 40 / shop 37), 15 group shots (lineup
  and squad both lived), 1,305 barks fired with the quiet
  hours absolute and no cooldown ever breached. The suite
  asserts floors, not the tallies — a cadence that
  silently stops firing now fails the build.
- **The rail order is pinned.** Talk's priority stack —
  an accepted delivery first (it settles before anything,
  masking even a beat step, exactly as the beat's own
  honesty note documented in v0.4.0d), then the holiday
  beat step, then the fresh errand ask, then the ordinary
  conversation, whose owed lines play case witnesses
  before story beats before the holiday's ambient
  reference before the errand thank-you — is now a data
  structure (`TalkGate`, new file) instead of call sites
  that agreed by habit, and the suite pins the peel under
  fabricated contention: one NPC armed with a delivery,
  a beat step, an ask, a witness telling and a bark on
  cooldown, all at once. Bark state provably cannot enter
  the decision — the gate never calls the bark engine, and
  a source pin holds that line. The bark decision itself
  (`Barks.decide`, extracted pure) gets the same
  treatment, plus a dense blast: an entry every 5
  game-minutes for 3 days, all 44 present, fired 546 of
  576 possible barks, zero in quiet hours, every per-NPC
  gap at or over the 20-minute cooldown.
- **A real defect, found by measuring: group-photo heads
  could clip.** The v0.4.0c composition layout was
  paper-tuned inside `PhotoView`; it now lives as pure
  geometry (`Photos.GroupGeom`, with the exact ink
  extents from the same `FigureGeometry` the classic
  selfie uses) so the suite can measure it. At the album
  card's real aspect (≈2.3–2.4:1 — the only surface where
  group photos render), the paper caps alone put a
  STACKED back row's heads several percent of the photo's
  height ABOVE the frame. Fixed with a third cap: every
  row's height now also answers to the headroom line —
  the tallest figure's ink (×1.05 box overhead) must fit
  under the frame's top with a 0.005 margin. Measured
  across 8 reachable frames × 7 aspects: the worst head
  top is exactly that margin, feet never pass the 0.955
  ground line toward the chin stamp, SQUAD's back row is
  a strict inset of its front, and a back-to-back mirror
  moves nobody (it flips around the figure's own
  center). A control in the suite proves the old caps
  clip — the headroom cap is load-bearing, not decoration.
- **Newest screens, fit QA'd.** The ERRANDS page at
  maximum (a full plate of the longest objectives the data
  can phrase, the top feat title showing), the HOLIDAYS
  page with every year counter warm and a window live,
  and all three mystery boards at once — every clue in,
  in every closing branch they can wear. Every line
  ellipsizes inside the card at font scales 1.0 and 1.3,
  and the longest bark hellos' bubbles stay inside the
  view at both scales with heads at the edges and high
  on screen.
- **Veteran save III.** Veteran II's saturated save
  rebuilt with everything since: the third case closed,
  the errand plate and history at their caps (18 / 8,
  every cast member asked), two instance years of full
  holiday beat masks for all eight holidays, and 24 group
  records among the 60 photos, compositions and
  back-to-back pairs and a crasher included. Serialized:
  **224,997 chars** (veteran II: 210,160) — still under
  half the 512K budget, and every Queue #4 collection
  round-trips at its cap. Nothing grows past a cap;
  nothing needed fixing.
- **Peak budget, third distance.** Named stack 0.9469,
  double-SFX 0.9776, bark stack 0.9469; bark and mood
  contours both peak at 0.2765 post-chain, under the
  plain blip; every ambience bed inside its allocation
  (loudest 0.1490). No drift since v0.4.0e.
- **Tuning ledger, swept.** Queue #4's constants are all
  listed now: the composition geometry and polaroid frame
  fractions, the horizon ridge height and landmark
  cutoff, holiday beat payouts, and the bark bubble's
  geometry numbers join their entries above. Nothing in
  the sweep was provably wrong beyond the clipping fixed
  above.
- **Test-infrastructure note.** The harness's `main()` hit
  the JVM's 64 KB method limit as sections accreted; the
  v0.2.0f mystery block now lives in its own function
  (`sectionMainMystery`). Same checks, new plumbing.
- **Honest gaps:** everything visual and audible in this
  pass is still proven by math, not by eye or ear — the
  group layouts have never been SEEN composed (the album
  is the only render path), and no emulator exists on the
  build VM. Lucky's device remains the verdict.

### v0.4.0f (versionCode 35) — Mystery arc #3: "What's Buried"
Queue #4, item 6. A third case for the 🔍 MYSTERY page — and
where the pond case was told by witnesses across a season and
the booth case was a whodunit in one room, this one is an
excavation: object-driven, slow, and spread over the land
itself. Tegridy breaks ground on a new field, and the diggers
keep pulling up things that shouldn't be there — traces of a
commune that farmed that valley in the seventies, in layers
that make no sense. What's down there becomes the town's
obsession for a month.
- **The shape** (`Mystery3.kt`, pure Kotlin): four chapters
  on the same episode machinery — the field, the pond's far
  bank, the old oak lot behind the school, and one last site
  — a week apart, queuing into the mystery slot ten days
  after the booth case retires. The cases never overlap:
  one slot, one case, always.
- **The sites change the map**: each opened site draws dig
  dressing at its location while the case runs — a pit, its
  spoil heap, the string grid, a work tent at the farm —
  registered scene tokens like every parity layer before
  them. When the case retires, the sites grass over; one
  ending leaves a small permanent marker at the farm.
- **The clues**: 12, and they are mostly things, not
  statements — **finds** at the sites as the chapters open
  them (beads, a key, a newsletter, a photo strip, a jar of
  seeds, and what the last pit has been keeping),
  **witnesses** who remember the valley (an old-timer, a
  hunter with a story about a cart that never came, and a
  suspicion somebody has never said out loud),
  **participation** (work a sift shift; eat at the fence-line
  picnic), and one **paper** clue — a file at City Hall with
  a gap in it where a page should be.
- **The finale**: not an accusation — a disposition. At the
  last pit's climax, a player holding at least **8 of the
  12 clues** decides what happens to what came up: take it
  to the town, put it back in the ground, or hand the
  decision to the one person it most belongs to — whose
  call lands a day later and is colored by how her own
  storyline with her husband resolved. Decide nothing, and
  the county decides the way counties do. Every landing
  retires the case permanently, writes its marks, and
  keeps talking in the town's ambient mouth — and one of
  them changes what a certain farmer says to you, forever.
- **The board**: the 🔍 MYSTERY page gains the case as its
  own section below the booth case's (both earlier boards
  stay readable — town history): the four digs, the four
  sites and their state, the finds, the theory the town
  currently believes, and the stamp it closed with.
- Saves: additive only (new `arc3` + `mystery3Clues` keys
  alongside the earlier cases'; invented clue ids and
  unknown disposition values are dropped or neutralized
  on load). Old saves become eligible the moment the booth
  case has been closed for ten days of game time. No new
  art or audio files — the disposition rides the existing
  act-sting synth, and the digs ride the scene renderer.

### v0.4.0e (versionCode 34) — Voice & barks: you hear who's around
Queue #4, item 5. The town has had voices since v0.2.0b — but only once
you tapped someone. Walking into a room was silent. Now the town greets
you out loud, in rhythm, before a word is read.

- **Bark patterns as data (`Barks.kt`, new file).** All 44 cast members
  get a rhythm signature — a short sequence of blip notes (count,
  semitone offsets, gaps) rendered by `SoundGen.renderBark` as ONE
  buffer on the character's existing blip family, so the rhythm can't
  drift and the voice is theirs: Cartman drawn-out and imperious (his
  first note drags two full beats), Kenny a six-note rapid patter
  through the MUFFLED family's parka lowpass, Butters ending higher
  than he started, Craig a flat two-note, Tweek's pitches jumping like
  his eyes, Chef two low slow notes from the basement. Ned is the
  table's only `electronic` voice — stepped pitch (the box doesn't
  bend), a squared-off harder clip, a 55 Hz buzz in the amplitude.
  Every hello is an original one-liner written for this game in each
  character's voice — the standing paraphrase discipline, no quotes.
  The suite pins the table whole: 44 patterns, unique signatures and
  hellos, the claimed shapes pinned by name.
- **Greeting barks in the scene.** Entering a space with cast present
  fires at most ONE bark: the pick is weighted toward your highest
  friendship present (`Barks.weightOf` = 1 + points/20, floored at 1 —
  a rival still can), sleepers never bark, every NPC cools down for
  20 in-game minutes, and someone arriving while you linger hails you
  only 1 time in 4. Volume is shaped by `Barks.volumeScale` (indoors
  ×0.7, evenings from 19:00 ×0.6, stacking) and quiet hours
  (22:30–06:30) close the gate entirely. The barker gets a paper
  speech bubble with their hello over their head for 2.2 s, drawn last
  in the figure stack, geometry from the pure pinned
  `Barks.bubbleRect`: inside the view, a gap above the head, clamped
  below the top chrome so it can never fight the HUD. Bark state is
  in-memory garnish only — cooldowns are never saved.
- **Dialogue blips learn the line's mood.** `SoundGen.moodOfLine`
  reads the line's own words and punctuation: caps-shouting is angry
  (except Timmy, whose caps are just Timmy), ellipses and the sad
  words drag, exclamations and lift-words lift — sad outranks excited
  when a line does both. The contour is baked into cached renders
  (excited +16% pitch / 0.82× length, sad −16% / 1.30×, angry sharper
  via a 1.7× soft-clip), and the suite pins the waveform ordering:
  sad longest and lowest, excited shortest and highest, angry
  measurably rougher-edged. NEUTRAL never moved — the refactor's
  multipliers are all exactly 1, and the suite byte-fingerprints
  every family's neutral render against the legacy lengths/peaks,
  so the SoundPool cache and the `v020g_r1` render stamp stand.
- **The budget, re-proven with the bark in the stack.** Barks take
  the blip's allocation (render ceiling 0.48, gain scale 0.9).
  Measured on the real renders: the loudest bark (Kenny, ceiling-
  limited through the parka) peaks exactly where the loudest blip
  does, the loudest mood blip likewise, and the bark stack
  (tune + bed + sting + bark) sums to 0.947 — the figure the named
  stack has carried since v0.2.0g.
- **Settings → 🔊 gains a 🗣 GREETING BARKS toggle** (one additive
  `audio_barks` pref, default ON; flipping it on previews Butters,
  who is never not ready).
- **Honest gaps:** bark pleasantness is waveform math until it
  meets ears — pattern distinctness is proven as data and as
  crest/crossing statistics, not as "you can tell it's Craig";
  only Lucky's ears grade that. The mood reader is a
  punctuation-and-wordlist heuristic, so a line played deadpan
  in the text blips neutral whatever the actor intended.
  Barks are cast-only: generated townsfolk have no patterns
  (the table is cast data) and stay silent greeters.
- **Save impact:** one additive settings key (`audio_barks`);
  no game-state fields. Source files 53 → 54 (`Barks.kt`).

### v0.4.0d (versionCode 33) — Holiday signature beats: every holiday gets its own game

Queue #4 item 4. Holidays already had dressing, gatherings and
episode framing; now each one has a BEAT — one small playable
story that exists only inside its window, runs entirely on
verbs the game already has (the Talk gate judges steps like an
errand delivery, Look finds things, the gift rail hands over,
the selfie and the fishing settle count), and leaves ONE
permanent mark: a line on the Journal's new 🎉 HOLIDAYS page.
Miss it and nothing happens — it returns next year. Play it
and the mark, the year count, and (first time ever) a garland
on your own wall are forever. Rewards stay at errand scale.

- 🎃 **Halloween — "The Full Route":** trick-or-treat all five
  kids' houses (knock with Look, catch the resident in costume
  with Talk, or bring them the good candy with Give — first
  come, first served; Liane's full-size bars, Sheila's audited
  toothbrushes, Kenny's self-counting porch bowl), then your
  own porch last.
- 🎄 **Christmas — "The Secret Santa Relay":** carry one parcel
  chain across town by Talk — Cartman's suspicious rattling
  gift → Kenny → Butters (tears, warm socks) → Liane (a
  handwritten coupon book) — and take the cookie tin home.
- 🎆 **New Year — "The Countdown":** join the Main Street crowd
  by Talk after 8 PM on Dec 31, then be back on Main Street
  for a Look in the last half hour as the cannons fire — a
  full half-second early.
- 💌 **Valentine's — "Cupid's Route":** hand-deliver four
  valentines in order — Wendy→Stan (his stomach is the tell;
  the line itself changes if their story arc resolved warm),
  Bebe→Clyde, Craig→Tweek (one sentence, devastating),
  Butters→Kenny (two pounds, popsicle-stick frame).
- 🎇 **4th of July — "The Big One":** catch the picnic fish at
  the pond, get deputized by Randy, then hold his flashlight
  at Kid's Park after 7 PM while he holds destiny. The next
  town's car alarms applaud.
- 🐄 **Cow Days — "The Vigil of the Patient Cow":** be weighed
  by the cow's blink (Look, Main Street), take Jimbo's orders,
  take her official portrait (a Main Street selfie), and let
  Ned close the ceremony through his voice box. She approves.
  Specifically you.
- 🍀 **St. Patrick's — "The Shamrock Hunt":** find five paper
  shamrocks by Look (school, church, park bench, Tweek's
  coffee cup) and the little paper pot under Skeeter's
  dartboard last — three chocolate coins, spent loud.
- 🦃 **Black Friday — "The Line Feast":** the Mall line's
  government is holding Thanksgiving and the casserole is at
  HOME — fetch it from Linda (Talk), deliver it to Butters on
  the line (Talk), and take the feast's fridge-door photo
  (a Mall selfie) before the doors open.

Around the beats: window conversation references them
(participants with a waiting step say their piece, once a
day); the bed's morning report names a holiday's beat on the
window's FIRST morning only; a played beat is credited in its
holiday episode's wrap-up recap; and Your House derives two
new decor pieces — the Holiday Garland (your first beat,
ever) and the Full Calendar (all eight, across the years).
State discipline: no new save fields — step masks and done
flags are capped keys in the existing `life` map, one pair
per (holiday, year). Fan sheets untouched.

### v0.4.0c (versionCode 32) — Group photos: the selfie grows up

Queue #4 item 3. The 📸 button learned to pose a frame. With 2+
townsfolk standing with you, the camera asks how — the classic
pile-in selfie, unchanged since v0.2.0c, or a GROUP shot — and
everything downstream (album, followers, feats, PHOTO goals,
PHOTO errands, event participation) reads the same record
either way. No parallel photo pipeline.

- **The frame.** Up to 3 townsfolk, chosen by how much they
  like you (points, name breaks ties) — with one exception:
  when Stan, Kyle, Cartman and Kenny are all here and willing,
  the frame is exactly the core four, all four seats. Refusers
  still refuse (and get told on in the caption); a rival who
  rolls the photobomb becomes a group CRASHER — uninvited,
  unseated, leaning in last over everybody, famous anyway.
- **Compositions.** Chosen by group size + chemistry, stored
  in the record: LINEUP (duos, and the gang), SQUAD (2 back /
  2 front), STACKED — the pile, for all-kid trios running hot
  on chemistry. PhotoView lays out by composition; classic
  records render by the original layout, byte for byte.
- **Chemistry in frame.** Pair chemistry is one read of the
  town's own ledgers — npcBonds plus the PAIR_SCRIPTS registry
  as the recognized-pair source (Craig+Tweek, Stan+Wendy,
  Jimbo+Ned…). Group records write their figures in FRAME
  ORDER, arranged by a scored permutation search: high-chemistry
  pairs stand adjacent, you never split them, rivals keep a
  gap — and the strongest mutual rivalry in frame gets the
  new backToBack pose: adjacent, mirrored, arms-crossed
  hurt faces. The caption tells on them ("are NOT posing
  together. (They are totally posing together.)").
- **Payouts.** First-timers in a group frame pay at the group
  rate (solo gain × 0.6; a crasher's half-pay halves first) —
  plus a chemistry bonus of +10 per recognized pair whose
  BOTH members make their photo debut in that frame, capped
  at 2 pairs. Re-photos trickle +1 as ever, bonus-free: the
  bonus is a debut prize, not a farm.
- **Feats (Collection, 📸 family, now 10).** FIRST CROWD
  (first group shot), THE GANG'S ALL HERE (the core four in
  one group frame — a crowded selfie doesn't count),
  FULL HOUSE! (5+ figures in a group shot, crasher included),
  SAY CHEESE (25 group shots, on a lifetime counter).
- **Captions name the group.** "The whole gang at Stark's
  Pond." / "Dogpile at Tweek Bros Coffee — you, Craig, Tweek
  & Clyde. Gravity lost." / "You & the gang at the Bus Stop:
  …" — deterministic from the record, like everything else
  in the album.
- **State discipline.** One additive record field
  (`composition`, default "" = classic selfie; old records
  load and render exactly as before — pinned through the
  JSON save format) and one counter in the existing `life`
  map. Fan sheets untouched.

### v0.4.0b (versionCode 31) — Errands: small work for townsfolk

Queue #4 item 2. Ordinary days get a spine: the town's small jobs,
from three sources, in six types, every one judged by a verb the
game already had — no parallel interaction systems.

- **The request board (City Hall).** A cork board on the free wall
  right of the minutes board, under McDaniels' portrait — drawn
  with its pinned notes (a bare cork in the room means a bare
  board in the dialog), tappable like the bed and the album at
  your house. It posts 2–3 fresh favors every morning as a pure
  function of the day seed: same day, same board, on any save.
  Unaccepted postings expire at rollover.
- **Direct asks.** Talk to someone whose current NPC goal makes an
  ask sensible and there's a chance (ASK_CHANCE 0.35) their
  conversation opens with a personal errand — marked offered for
  the day whether you take it or not, so nobody pesters. Grudges
  never ask; somebody already running an errand for you doesn't
  stack a second. Kenny's mom needs the good coupons; Randy wants
  a soil sample from the pond — comic, never gross, no real brands.
- **Shopkeeper asks.** At their own counters only: Tweek (beans,
  deliveries), Jimbo (a speckled trout for the wall, a snowball
  bet), Chef at Freeman's (a taco-score dare), Gerald at the Bank
  (the deposit run, the lost statements) — SHOP_ASK_CHANCE 0.5,
  trade-flavored.
- **Six types, six existing verbs.** FETCH brings an obtainable
  item — the generator may only pick from a suite-pinned
  obtainable universe (shop stock, catch-table fish, daily spawns,
  container loot; never the reward currency, never winter-only
  litter) — and the Give rail's hand-in consumes the pack copy.
  DELIVER settles by Talking to the recipient. FIND plants a
  named lost thing as a Look-find at a sensible place until a
  careful Look recovers it (velcro wallets, golden ticket stubs,
  Kenny's lost mitten). FIX rides the streetlight bonk (wherever
  tonight's flicker is) and the bench taps at the park and the
  bus stop. PHOTO is judged by Photos.applyPhoto itself — the
  named person in frame or the named place in the record. PLAY
  settles inside the three Minigames settle functions: catch the
  named fish, win the showdown, post the taco score.
- **The plate.** Up to 6 accepted errands; they persist across
  days until completed or abandoned — abandon at the board or
  from the Journal, free except the asker's disappointment line.
  Completion pays friendship scaled by type and capsule coins
  for most (PLAY pays 0 — the game already paid), sometimes a
  thank-you item (id-hash odds) or a thread topic planted in the
  asker's conversation memory, always the asker's thank-you line
  at your next conversation, and a beat in the morning report
  when you ran one yesterday.
- **Surfaces.** Journal 📋 ERRANDS (plate with objectives and
  where-to-go hints, today's board, lately's runs, lifetime
  count, current title; tap the card to drop one), Collection
  📋 ERRAND FEATS — 10/50/150 errands earn Neighborly / Town
  Fixture / Indispensable — and the board itself in City Hall.
- **State discipline.** Four additive save keys (accepted list,
  asked-marks, lifetime count, recent list), decode-capped
  (18 / 8) like every audited system; old saves load with an
  empty plate. The 400-day soak takes and runs a board errand
  every tenth day through its own judge, all year.

### v0.4.0a (versionCode 30) — Scene parity pass #4: horizons & sight-lines (opens the 0.4.0 series)
Queue #4 item 1. Passes #1–#3 made every place agree with
itself — its props, its palette, its light. This pass makes
the town agree with itself: the skyline behind every facade
is the actual neighborhood, derived from the map.
- **The derivation** (`Horizon.kt`, pure Kotlin): each
  exterior's horizon is a pure function of (location,
  weather) — every graph neighbor (EXITS) the weather lets
  you see, placed by its true bearing off gx/gy under the
  panorama rule (north centers, east right, west left, due
  south wraps to the seam), tiered by true map distance
  (near ≤ 24, mid ≤ 44, far beyond — the longest edge in
  town, Main Street ↔ Tegridy Farms at 55.2, is the only
  far pair). Nothing is stored anywhere; the light band
  deliberately does not move geography — it enters at
  draw time as color and windows, like every other layer.
- **The registry** (`LOC_SILHOUETTES`, 27 tokens): what
  each place looks like from a block away — the church's
  steeple, Tegridy's silo over its barn, the school block,
  the mall pylon, City Hall's dome, the pond's tree line,
  a cup over Tweek Bros, a taco shell arched over
  Freeman's — drawn flat by `SceneArt.drawHorizon` and
  cross-checked against the art source in the pass #1–#3
  discipline. Parity registries now total 336 tokens.
- **Two landmarks**: the town water tower, a fixed point
  north of downtown in range of every seat in town on a
  clear day (tiered by true distance, one steady red dot
  at night), and a nearer mountain ridge for the seven
  rim spots — the map's edge by rule, not by list:
  Kenny's, Tegridy, the pond, the school, Tolkien's,
  SoDoSoPa, the bus stop.
- **Light agreement**: silhouettes take SceneLight's band
  colors (day pale blue-grey, dusk dark cutouts, night
  near-black) run through the SAME weather wear as every
  other exterior color. When the lights come on, the far
  windows do too — one in a neighbor's own LOC_LIGHT
  warmth, two for a near neighbor — with the specials on
  their true bearings: the church's glass burning from a
  town away, Skeeter's neon a pink smudge over Main
  Street, Tegridy's beacon blinking on the farm's bearing.
  Occupancy holds at distance: an empty house and a shut
  shop stay dark on the skyline, like they do up close.
- **Weather cuts sight, not just color**: fog ends the
  world at the near tier — Main Street keeps its block
  and loses the farm, the school and the tower, while the
  park, 13 units from the tower, keeps it. A blizzard
  cuts at 30 units; heavy snow drops only the far tier.
  Rain cuts nothing — it only darkens.
- **Photos agree**: PhotoView draws its outdoor backdrop's
  horizon through the same function, off the record's own
  place, hour and weather — an album shot shows the
  skyline the scene showed. Interiors are unchanged;
  their windows keep the existing smudge system.
- No save changes, no new assets, NPC anchors/hotspots
  untouched, and the older layers keep their exact draws
  (the horizon paints from the derivation alone — it
  spends no Random from anyone else's stream).
- Honest gaps: the shapes are the facade's body language
  flattened — a block, a roof, one feature — so at far
  tier a few read as cousins (the gabled houses most).
  The dusk cutout color and the tier heights are paper
  math (ledger). And a due-south neighbor stands at the
  frame's edge — the panorama seam working as designed,
  not a bug.

### v0.3.0g (versionCode 29) — Polish & performance pass #3: the audit (Queue #3 complete)
Queue #3, item 7 — the final item of the queue. No new
features; instead, everything the queue built got audited,
measured, and tightened.
- **THE LIFETIME — the unified soak**: the fast-forward
  tradition grown to the whole modern game. One fresh town
  driven through the real daily stack for exactly 400 days
  (a full sky year and change, a photo taken weekly): the
  pond case retired (the town ending), the booth case
  opened, played, went cold and produced its lost-and-found
  reel, all five story arcs resolved, goal cycles kept
  turning over, episodes and photos rode their caps, and
  the sky's 8 special nights all became next-morning buzz.
  The calendar lands exact to the day, the house derives
  its poster and its one branch-appropriate mystery piece
  from the lived year, nothing is stuck — at day 400 no
  system still demands the mystery slot — and the lived
  save serializes to 169,312 chars.
- **The veteran, re-measured**: v0.2.0g's saturated-save
  measurement repeated against today's registries — 44
  cast at full friendship, both cases' complete state, all
  five story records with their helped/nudge/shown sets
  saturated, 60 photos carrying capture days, 500 wishes —
  through the real `encodeGameState` path: **210,160
  chars** (the v0.2.0g veteran measured 175,413), ~41% of
  the half-megabyte ceiling, round-tripping at every cap.
  No growth path regressed past its cap.
- **Real defect #1 — the morning report told story beats
  twice.** The midnight rollover appends a story's beats
  to the events ticker (NEW STORY / stage banners /
  STORY'S END) and the bed's report summarized the same
  beats in its own arc section, so sleeping through a
  page-turn printed it twice. The report now owns the
  transitions by construction: the ticker versions are
  filtered from its events block, the whole report is
  distinct()ed, and the suite drives real `dailyTick`s
  through slept nights — opening, page-turn, resolution —
  asserting each beat prints exactly once. The edges are
  pinned too: a case closed before last night is not
  re-reported; a finale, a special sky and pending
  chapters in one night yield one line each; a
  loader-clamped arc record throws neither the report
  nor the arcs page (its stage index is defended now);
  and the report stays spoiler-clean for BOTH cases —
  the booth case's lost-and-found beat reaches the page
  premise-only, like its board.
- **Real defect #2 — the ticker cap had holes.** The
  events feed's 40-cap was re-implemented at each add
  site, and the sites that forgot let a busy rollover
  bulge past it (the soak caught the feed at 42): both
  cases' clue grants, the finale choice, the faction-
  shift lines, the lost-and-found beat, goal obituaries,
  one story banner, and five GameActivity beats (the
  knock, snow war, a move-in, chalk, a leaf dive) now
  trim like everyone else; the loader also trims a
  hand-inflated doc on the way in; and a burst test pins
  the in-memory invariant at the fixed sites. (The save
  doc was always capped at encode — this lived in memory
  and on the ticker only.)
- **Newest-screen fit QA**: the 📖 STORY ARCS page
  (mid-arc and all-endings states), the Your House
  surfaces (the bed dialog, the trophy shelf's longest
  line, the morning report at maximum contributors), the
  🔍 board with BOTH cases at their longest, and the
  accusation dialog items (with a 28-character usher
  name) — the contrast pairs these screens actually use,
  their longest real lines fitted at font scales 1.0
  and 1.3.
- **Cadence, by constants**: the storylines' timing joins
  the named-constant discipline the minigames got in
  v0.2.0g — `StoryArcs.STAGE_DAYS_NUDGED` 1 /
  `STAGE_DAYS_SOLO` 2, the warm ending's helped fraction
  (1/2) and bond threshold (6) — values unchanged, the
  suite's pins reading the constants. The cases' numbers
  were already named (pond: chapters +7 from day 3, gate
  7; booth: +7 open, +4 accusation, gate 6, reel +7) and
  are re-pinned at their shipped values.
- **Audio, at a distance**: the v0.2.0g peak budget
  re-measured against the current render set — named
  stack 0.947 (headroom 0.053), double-SFX stack 0.978
  (headroom 0.022), unchanged — with every ambience bed
  now pinned individually inside the ambience allocation
  (the loudest, post-chain, is 0.149), the aurora night's
  hush bed included: the flavor spends no more than the
  crickets it replaces.
- **The tuning ledger**: a new section above this
  changelog inventories every gameplay/visual constant
  still tuned on paper — dusk light, weather-wear steps,
  aurora strength, arc pacing, case cadences, mail odds,
  knock timing, bed flow, the audio chains — so playtest
  feedback has a list to shoot at.
- Saves: no new keys, no changed values — additive-only
  in the strictest sense.
- Honest gaps: the soak drives the daily stack (the
  v0.3.0f lifetime's pattern) — tick-level churn keeps
  its own coverage in the 60-day sim; the peak budget is
  still math, not ears; and everything in the tuning
  ledger is still awaiting a device.

### v0.3.0f (versionCode 28) — Mystery arc #2: "The Locked Booth"
Queue #3, item 6. A second case for the 🔍 MYSTERY page —
and where the pond case sprawled across five weekly chapters,
this one is a compressed whodunit in a single room. The Bijou
hosts a one-night marathon: the kids' own home-movie — a
school project, months in the making — plus a donated prize
reel. Between acts, the home-movie reel vanishes from the
LOCKED projection booth. The door was never forced. Eight
named guests were there.
- **The shape** (`Mystery2.kt`, pure Kotlin): two beats on
  the same episode machinery as any event — the marathon
  night itself, then, four days later, a second screening
  where the town regathers. Between them: three interview
  days. The case queues into the mystery slot a week after
  the pond case retires and never overlaps it; both beats
  defer past holiday episodes like chapters do, and neither
  is in the random event pool.
- **The clues**: 10, gathered the pond case's three ways —
  **interviews** (talk to people across the interview days;
  stories get retold, and retellings drift), **evidence**
  (Look at the Bijou — it gives up two pieces, one per Look —
  and at the school), and **participation** (be at the
  marathon, early for one discovery and at its climax for
  another). One witness is the Bijou's usher: a generated
  soul picked from the town's own pool when the marathon
  airs, on shift at the theater for the whole case.
- **The accusation**: four suspects — the chaperone, two of
  the guests, and the usher, who is the obvious suspect,
  which in this town is practically an alibi. At the second
  screening's climax, holding at least **6 of the 10 clues**
  earns you the floor: say a name, or keep your seat. A name
  said right closes the case warm, with a confession, a
  screening, and a permanent mark at the Bijou. A name said
  wrong sticks socially — a scarred bond, a grudge that
  ambient talk keeps — and the case goes cold anyway. Say
  nobody and it goes cold clean. A week after a cold close,
  something quietly turns up in the Bijou lost-and-found.
  Every landing retires the arc permanently.
- **The board**: the Journal's 🔍 MYSTERY page gains the
  case as its own section under the pond case's board (the
  pond board stays readable — town history): the night and
  the days, the clues, the four suspects, the town's current
  theory, and the stamp it closed with.
- Saves: additive only (new `arc2` + `mystery2Clues` keys
  alongside the pond case's; invented clue ids and unknown
  suspect/branch values are dropped or neutralized on load).
  Old saves become eligible the moment the pond case has
  been closed for a week of game days. No new art or audio
  files — the accusation rides the existing act-sting synth.

### v0.3.0e (versionCode 27) — Sky parity: the sky is data, and the town notices
Queue #3 item 5. A new pure object, `SkyState.kt`, computes the whole
sky from (absDay, timeMin, weather, season) — no save state — and every
surface reads it: the scene, the selfie backdrop, the ambience picker,
and the town's own memory of last night. TownLife delegates its #44
moon and #49 aurora to it, so there is exactly one sky in the game.

- **The moon, with light attached**: the 29-day, 8-step cycle now
  also yields a MOONLIGHT level (new 0.05 → full 1.0). Full-moon
  nights genuinely read brighter: the moon throws a phase-scaled
  halo, and Stark's Pond's moonlit ice sheen in the LIGHT layer
  scales by it — a new moon leaves the dock lantern to work alone.
- **Aurora** (clear winter nights, sparse eligible dates — 7 in a
  canonical winter, suite-pinned over a simulated year; winter sun
  cuts that to the ~2 a player sees): drawn as wide smooth color
  washes in the sky band only — green into teal into a breath of
  violet — with strength weighted to the long dark hours (23:00–
  1:00 burn brightest). Flat shapes only; no clustered patterns.
- **Meteor showers**: fixed residue nights per season on a 91-day
  cycle — exactly 9 a year (2 spring, 3 summer, 2 fall, 2 winter),
  clear skies only, drawn as a few clean streaks. On those nights
  the pond takes **wishes**: Look at Stark's Pond and you wish —
  no menu, once a night, with whoever's standing there (they wish
  out loud) or solo. Wishes are counted forever on the Journal's
  🎮 GAMES page, riding the existing `life` counters. And the
  three-eyed fish likes meteor nights: rare-species weights scale
  ×1.5 in the fishing table (`Feel.METEOR_RARE_MULT`; a plain
  night replays the legacy table exactly, suite-pinned).
- **The harvest moon**: one autumn full moon (the designated
  full-moon block — exactly one 4-night block per canonical
  autumn) — blood-orange, 1.45× oversized, up from dusk.
- **The town notices.** Witnesses outdoors on a special night say
  so, unprompted. The midnight rollover records a SEEN special
  (a clouded-out sky records nothing) against the new day: the
  next day's daily topics gain the debate — solar wind vs.
  screensaver, space garbage vs. space magic, the horizon
  illusion — with per-kid positions (Kyle has a diagram), scored
  so the whole town is talking about it for exactly one day. The
  writing stays sky science and schoolyard logic throughout —
  a different subject from the pond case, never touching it.
  And if you slept through it, the bed's morning report says so:
  "While you slept: …".
- **Selfies capture the same sky**: photo records now carry their
  capture day (one additive field; pre-0.3.0e records load with a
  plain-moon fallback), and PhotoView re-renders the album shot
  under SkyState's sky for that moment — phase-shaped moon,
  harvest orange, aurora washes, meteor streaks, even the green
  wash in an indoor window on an aurora night.
- **One audio flavor**: on aurora nights the crickets stand down
  — `selectAmbience` takes the aurora as a flavor input and the
  clear night gets the soft-air hush instead (weather and indoors
  still outrank it; no new beds).
- Honest gaps: the rollover judges the night by the ending day's
  weather at midnight — a sky that cleared at 2 AM after a cloudy
  midnight goes unrecorded (the witnesses that night still saw
  it live). Frequencies are pinned for the canonical calendar
  alignment and range-checked for shifted ones; a given save's
  winter may deal it one aurora more or less. Wash/streak
  strengths are tuned on paper until a device says otherwise.

### v0.3.0d (versionCode 26) — Your House: the New Kid gets an address
Queue #3 item 4. A 27th location, the player's own house, integrated
like every other address in town — full parity treatment, four working
fixtures, earned decor — and a hard privacy rule: nobody is ever
inside it but you. New pure file `House.kt`; **no new save keys** —
everything about the house derives from records that already exist.

- **The place**: east end of the residential row (past Kyle's house,
  exits to and from Kyle's street; the graph still walks all 27
  locations from the Bus Stop). Plum field `#8a6a7d`, wall `#7d5a70`,
  floor `#4e3a44`, accent `#7ac74f` — every suite distance floor holds
  (field ≥ 27.5 from any other field, wall ≥ 31.5, floor ≥ 21.7,
  neighbour signature vs Kyle's ≈ 97.7). Pass #1 props: a mailbox
  lettered **THE NEW KID** on a nailed-on plaque, a bike leaning by
  the fence gathering snow (he walks everywhere — everywhere), and a
  door mat that reads WELCOME-ISH. Warmth `#ffd2a1` with a lit porch
  and one window that stays up latest; one seasonal token per season
  (spring doormat flowers, summer chalk driveway, an autumn pumpkin
  porch, a winter wreath). Weather wear flows through the standard
  transforms, and the house lights up at night when you're home —
  occupancy exactly like the cast's houses. Lorebook alias: "The New
  Kid's Residence". Interior (kid's room + living-room feel): sofa,
  TV, table, desk, lamp, plus a band poster, a cork board of notes,
  and two still-unpacked moving boxes — and a searchable kitchen
  trash can, because coverage is coverage.
- **The bed** — sleep until 7:00 AM. It isn't a fast-forward cheat:
  sleep is the engine's own natural tick run forward, so the midnight
  rollover (calendar, goals, storylines, the mystery, holidays,
  upkeep) happens exactly as if the night had passed in real time —
  the suite proves the slept night lands on the same morning as the
  lived one, field for field. Waking shows a **morning report**:
  overnight events, any storyline that turned a page or resolved,
  mystery chapter/case status (reported, never told), and the mail.
  Mid-dialogue or mid-episode-card, the bed defers politely, like
  every other dialogue-safe moment in town.
- **The trophy shelf** — three planks on the wall: the first twelve
  collectibles stand in collection order as you find them (empty
  slots stay honest empty shelf), and photo/game feats stand above
  as trophy cups (six, then "+N"). Tapping it reads the room: what's
  earned, what's still out there, which wall is still bare and why.
- **The album book** — a red 📸 book on the desk that opens the
  Journal directly on its album shelf (the section exists, with a
  patient empty state, even before your first selfie).
- **The mirror** — re-opens the character creator on your existing
  appearance, the same door the menu uses. No new appearance storage.
- **Decor unlocks** — six earned pieces, each a registered
  interior-art token that appears the moment its record exists:
  **Episode Poster** (finish an episode), **Photo Strip** (first
  photo feat), **Game Trophy** (first game feat), and the mystery
  finale's permanent mark — exactly one, branch-appropriate and
  spoiler-safe in name: a **Foil Star**, a **Sealed Envelope**, or a
  **Tiny Gavel**. The room literally fills as you play.
- **The mailbox & your mail**: your facade carries the same mailbox
  every house gets, lettered for you — and it works the way a
  mailbox should: things arrive in it overnight. The morning report
  carries the day's mail — a note from a friend some days, junk (an
  expired City Wok coupon, obviously) on others — a pure function of
  the day and who likes you, so the box is never a loot pinata, just
  breakfast reading. (The NPC mail-check ritual itself is unchanged:
  residents still check their own boxes; yours is checked by you,
  in bed, with coffee.)
- **Privacy, pinned**: NPCs are never inside your house. The one
  road in — a close friend's day bending toward your most-visited
  place — delivers them to your **doorstep**: talkable outside, gone
  the moment you step in, and answering the door gets a knock
  ("🚪 *knock knock*", once per visitor per day). Cast goals,
  generated visitors/residents, shenanigans, the rain dash — nothing
  targets the address; the suite pins the invariant structurally,
  behaviorally, and after a full lived day of best friends.
  Friends' ambient chatter already gossips about your place
  ("…the new kid's porch light was on at 3 AM again" is in the
  DAILY_TOPICS pool, v0.2.0d).

### v0.3.0c (versionCode 25) — Relationship storylines: five arcs of the heart
Queue #3 item 3. The town's key pairs get multi-day storylines, told
through the machinery that already exists (midnight rollover, ticker,
Journal, npcBonds, agent memories, relation notes) — a new pure object,
`Stories.kt`, and one additive save key. Nothing about the season
mystery is touched; the arcs are not events and never join the roll.

- **The five arcs** (one on stage at a time, in season order, gated so
  a fresh town meets them spaced out): **Stan–Wendy, "The Note in the
  Locker"** (a note, a Cartman-sourced heart rumor, a grand-ish gesture
  with a two-word speech); **Craig–Tweek, "Too Much Pressure"** (a
  cup-stacking fair demo, the town's terrible advice, Craig's one-cup
  firewall method); **Heidi, "Heidi, Herself"** (a solo arc: her own
  girls'-circle advice night with Nichole, Red and Bebe — explicitly
  NOT a re-pairing; the ex stays history, the suite pins Cartman out of
  her cast list); **Randy–Sharon, "The Line in the Sand"** (a Tegridy
  fundraiser 'synergy', Stan's crayon-labelled COLLEGE jar, and a
  compromise that costs Randy a public apology); **Jimbo–Ned, "The
  Quiet Hunt"** (a Florida letter, a loud knee, and a hunt that was
  never about the hunt — Ned's every line, callbacks included, comes
  through the voice box, suite-pinned).
- **Shape**: 3–4 stages each, every stage a beat with dialogue.
  Talking to an arc person plays their stage line once (before the
  small talk, like a mystery witness). Any witnessed interaction —
  Talk, gift, selfie — with the arc's core people is a **nudge**: a
  fully nudged stage turns over at the next midnight instead of the
  one after, so a player who shows up doubles an arc's pace. Nothing
  is gated: left alone, every arc still resolves on its own clock
  (the suite fast-forwards a whole unattended season to prove it).
- **Endings stick, two flavors each**: the warm ending (player nudged
  at least half the stages, or the pair's bond was already strong)
  pays a bigger bond, a warmer permanent relation note, and +3
  friendship with the pair; the solo ending is the town sorting it
  out messier and later. Either way the pair's bond moves, the
  Journal's 🔗 relation note is permanently overridden, the ending
  lands in the pair's agent memories, and the pair's ambient chatter
  gains speaker-tagged callback lines in the ending they actually got.
- **Journal**: a 📖 STORY ARCS page (it appears when the first story
  opens) lists all five with per-stage ✓/▶/· progress, the current
  beat's note and objective, a "you've nudged this beat" marker, and —
  once done — the outcome in one line.
- **Carry-in from v0.3.0b**: the wave-2 cast join the ten event defs'
  causer credits where canon fits — McDaniels declares Upside-Down
  Day (Crabtree strands it), Barbrady investigates the Mole Panic and
  the Coon Sightings, Ned rides the Tegridy Takeover, Heidi and
  DogPoo hold the Silent Challenge, Nichole joins Pirate Week, Kevin
  and Red chase Chinpokomon, Francis wades into the Imagination Leak,
  Mrs. Tweek watches Memberberry Fever, Mr. Tweek weighs in on Gold
  Fever. Stage geometry, effects and lineage are untouched; the suite
  pins every placement.
- Honest gaps: the arcs advance on the midnight rollover, so a stage's
  beat plays out over real play days rather than inside one sitting;
  and like everything else here, the pacing numbers (1-day nudged /
  2-day solo stages) are tuned on paper until a device says otherwise.

### v0.3.0b (versionCode 24) — Cast expansion wave 2: the town gets twelve more souls
Queue #3 item 2. The cast grows 32 → 44 with full first-class integration
for every newcomer — profile + persona (paraphrased from the lorebook),
seven-block daily schedule, MeasuredProps SheetSpec drawn by KidArt,
9 collision lines, 18 daily topics, a SPEECH_RULES voice card riding both
LLM prompt builders, friend greetings, response banks, sleepy lines, a
faction, a blip family, goal-pool eligibility, selfie tier, and
snowball-showdown stats.

- **The kids** (Elementary cohort; homes use the same Elementary
  convention as the first-wave kids without a canon house): **Heidi
  Turner** (long auburn hair, pink jacket — the girls' diplomat),
  **Nichole Daniels** (dark skin, black curl mane, mustard sweater),
  **Red McArthur** (the name is the spec: long red hair, teal jacket),
  **Kevin Stoley** (messy hair, freckles, Star Wars scholar), **Francis**
  (the wide, gentle ladder-holder, width 1.18), **DogPoo Petuski**
  (freckle-grime and philosophy; nobody notices him, which the suite now
  pins as a feature).
- **The adults**: **Mayor McDaniels** finally occupies City Hall (bob,
  teal pantsuit, press-conference energy); **Officer Barbrady** patrols
  Main Street/Bus Stop (navy cap with gold accent, opaque shades,
  mustache — the whole force); **Ms. Crabtree** IS the BusStop (huge dark
  curl mane, deadpan scowl, four of seven daily blocks on the bus);
  **Mr. Tweek** and **Mrs. Tweek** complete Tweek Bros (his coffee sig
  and sidepart serenity; her bun, pink blouse and load-bearing calm).
- **The twelfth pick: Ned Gerblansky**, Jimbo's hunting partner. Jimbo's
  profile already named him three times; now he stands at the gun
  counter (vest + glasses sig pairing with Jimbo), speaks only through
  his voice box in lines, pair scripts and speech rules alike — the
  suite pins that every one of his collision lines comes through the
  box — and gives Jimbo the partner his whole act was built around.
- **Writing totals move**: line banks 288 → 396 (44 × 9), daily topics
  626 → 842, pair scripts 57 → 71 with seven new scripted pairs (the
  Tweek family triangle ×3, Jimbo–Ned, Heidi–Cartman handled as history,
  McDaniels–Barbrady, Nichole–Tolkien), speech rules 32 → 44 distinct
  cards, and thirteen new NPC_RELATIONS notes.
- **Factions**: girls' circle +3 (Heidi, Nichole, Red), Craig's gang
  +Kevin, boys' crew +Francis & DogPoo, parents +McDaniels & Barbrady,
  school staff +Crabtree, Tegridy & local business +the Tweeks & Ned —
  every one of the 44 holds exactly one.
- **Lore**: the alias table is regenerated by `tools/gen_lorebook.py`
  (44/44 cast matched, lorebook.json byte-identical), which also drops
  the twelve from the generated-visitor pool — no more duplicate Heidi
  Turners wandering in as tourists.
- **Fan sheets**: twelve new reference sheets through the existing
  pipeline (the generator learned a `bun` hairstyle and cop-shade
  sunglasses for the occasion; a latent `collar` crash in
  `sync_params.py` was found and fixed on the way), the contact sheet
  now grids 44 across six rows, and the original 32 sheets verify
  byte-identical by checksum.
- **Causers, honestly**: the twelve join every generic system (episode
  clusters, participation, goals, photos). The ten existing event defs'
  pinned causer lists were left untouched rather than rebalanced
  silently — new dedicated beats for the newcomers belong with the
  relationship-storylines work next in the queue.

### v0.3.0a (versionCode 23) — Scene parity pass #3: exteriors & light (opens the 0.3.0 series)
Queue #3 item 1. Pass #1 (v0.1.9a) gave every exterior its signature
props; pass #2 (v0.2.0a) did the interiors. This pass gives every
exterior a **light identity** across the day and the year, in the same
registry discipline: `LOC_LIGHT` + `LOC_LIGHT_DETAILS` (54 tokens) and
`LOC_SEASON_DETAILS` (104 tokens), every token cross-checked against
the art source by the suite.
- **One light state, shared everywhere.** New pure object
  `SceneLight.kt`: the day bands (day 7–17, dusk 18–20, night 21–6 —
  `SceneArt.timeOfDay` now delegates to it), the light level (day 0,
  dusk 0.65, night 1.0), per-location warmth/sign-glow/lamps, and the
  weather-wear transforms. Scenes, interior windows and selfie
  backdrops all read it, so they can't contradict each other.
- **Night & dusk: the LIGHT layer.** A registered per-location layer,
  drawn only when the light level is above zero, alphas scaled by it
  (dusk is the town halfway lit): Skeeter's neon throws a pink halo
  and a pink pool on the snow; the Bijou marquee glows violet over
  lamp-lit poster cases; the church's stained glass burns from
  inside; City Hall keeps one office window lit; the Freeman's taco
  is outlined in bulbs along its shell arc; the mall pylon glows
  blue over two lot lamps; Stark's Pond gets a dock lantern and a
  moonlit ice sheen; Tegridy's silo blinks a red aircraft beacon;
  Tolkien's gate lanterns burn on timers and the chandeliers show
  through both windows; Cartman's TV window flickers blue while
  Kenny's leaks through the boards of his. Streetlamps with ground
  pools stand at the bus stop, down Main Street (over storefronts
  lit in the street's own warmth), at the park court and the mall
  lot. The facade's lit windows and porch lights now burn in each
  location's own warmth (26 distinct warmths in `LOC_LIGHT`) instead
  of one town-wide amber — and the light layer obeys the facade's
  occupancy rules: an empty house and a shut shop stay dark there
  too. Businesses whose signs would glow carry a `signGlowHex` and
  get a halo behind the name sign.
- **Seasonal exterior dressing** beyond snow cover, one registered
  piece per season per location: spring brings tulip rows at
  Butters', melt rivulets down Cartman's driveway, cleanout boxes
  at Stan's garage, garden rows at Kyle's, thaw cracks on the pond;
  summer brings sprinklers (Butters' perfectly even, Kenny's sad),
  a kiddie pool, patio tables at Freeman's, lily pads, tall Tegridy
  crop; autumn brings leaf piles and raked leaf bags, firewood at
  Stan's, an orange canopy over the park, rust-red cattails; winter
  brings icicles (Butters' suspiciously even; one long one dripping
  off the Rhinoplasty nose sign), snow loads on roofs, columns,
  the school slide and the roof taco.
- **Weather wear as measured palette transforms.** `PaletteWear` —
  desaturate, darken, whiten, in that order — applied to
  `LOC_PALETTES` data at draw time (facade body, parity details,
  ground dressing, selfie fields): rain darkens a step and grays
  the snow; fog mutes toward gray; blizzard whites out the edges.
  The base data is never mutated — sunny weather returns every hex
  byte-exact, and the suite pins five transform outputs plus the
  direction of every transform over all 26 fields.
- **Consistency.** Interiors with a lamped street outside (bus
  stop, Main Street, the mall, the park…) now smudge that lamp's
  warmth across the window's lower corner at night, matching the
  lamp the scene outside draws. The selfie backdrop dropped its
  private "night starts at 6 PM" rule for the shared band table —
  dusk photos now get a dusk sky and a low sun — and its ground
  runs through the same weather wear as the scene.
- No save changes, no new assets (still only the generated launcher
  icon), NPC anchors/hotspots untouched, and the new layers run on
  their own Random streams so passes #1/#2 keep their exact draws.

### v0.2.0g (versionCode 22) — Polish & performance pass #2: the audit (Queue #2 complete)
Queue #2, item 7 — the final item of the queue. No new
features; instead, everything the queue built got audited,
measured, and tightened.
- **Long-session robustness audit**: every growth path in
  save state inventoried and either proven bounded or capped.
  Already-bounded verified through the real save path (events
  40, town log 30, agent memories 10 at save, litter 40,
  episodes 30, photos 60, goals pruned at rollover, every
  keyed map tied to a fixed registry — locations, factions,
  the 33-agent roster). Three real leaks found and fixed:
  (1) **converted residents grew forever** — each move-in is
  permanent roster, so conversions now stop at
  `VisitorSystem.MAX_CONVERTED` (24, atop the 6 base
  residents); the gate blocks NEW move-ins only, a save past
  the cap keeps everyone. (2) **the `npc_memory` doc's entry
  count** was bounded only by the ~2k-name lorebook pool
  (per-entry contents were always capped) — the serialized
  doc now stops at 400 entries: cast and current residents
  always kept, generated folk by recency; runtime memory is
  untouched. (3) **`lifeText["lampFixed:<absDay>"]`** wrote
  a fresh key per lamp-night that nothing ever read again —
  the family is pruned at each rollover. The one deliberate
  non-cap: `photographed` (pool-bounded, ~30 B a head —
  trimming it would corrupt the follower trickle).
- **The veteran measurement**: `Prefs.saveGame`'s doc
  builder is extracted as the pure `encodeGameState`, so the
  suite measures the REAL save path. A synthetic heavy
  veteran (day 400, everything maxed: 33 agents × 25
  memories, 30 fat episodes, 60 photos, 24 converts, 300
  photographed, every registry saturated) serializes to
  **175,413 chars** — and a capped `npc_memory` doc to
  66,746. Both round-trip at their caps.
- **The 60-day fast-forward**: the suite now runs a fresh
  town through 5,760 real engine ticks. It ends green:
  calendar exact, goals churning (62 live records), 16
  episodes on the capped shelf, the season mystery aired
  all five chapters and **retired itself** (town ending),
  zero exceptions.
- **Audio peak budget**: measured on the real renders, the
  old worst-case stack summed to **1.60** full scale (tune
  0.245 + loudest bed 0.182 + two loudest one-shots at
  0.589 each) — a sting over a wind gust was clipping by
  design. `SoundGen.PeakBudget` now holds the allocations
  as pure math the suite pins: one-shots and blips are
  peak-limited at render (0.48 ceilings; quieter renders
  pass through byte-untouched), and the playback chains
  read the budget's own constants (music chain 0.8→0.7,
  ambience 0.55→0.45), so player and budget can't drift.
  Both designed stacks now sum under full scale: named
  (tune+bed+sting+blip) **0.947**, double-SFX **0.978**.
  Render stamp bumped — devices re-render their caches.
- **Flagged gaps, fixed**: SoundPool readiness is earned
  via `OnLoadCompleteListener` (the 120 ms settle sleep is
  gone; a failed load counts too, so one bad file can't
  mute the town); `tools/run_smoke.sh` surfaces kotlinc
  errors readably (the old `grep "^error"` never matched
  `path:line: error:` — compile breaks hid as
  class-not-found); ambience/music re-feed the moment
  weather flips (`Engine.onWeatherShift`), not at the next
  10-second UI tick; pair scripts join the freshness ring
  (per-pair, in-memory, same draw count — scripted pairs
  no longer rerun the same bit back-to-back).
- **Minigame feel, by constants**: every tuning number in
  the three games — bite delays/windows/factors, zone
  edges and powers, dodge window math, taco fall/interval
  ramps, kind rolls, stuns, points, the payout table —
  lives in one `Feel` object in Minigames.kt, values
  unchanged (the v0.2.0e pins read the same numbers). And
  each game now shows a one-line HOW-TO on its first entry
  per save, appended to its dialog subtitle.
- **Newest-screen fit QA**: the Mystery board, Games page,
  Album header and finale choice re-run through the Chrome
  invariants — their actual contrast pairs (ink and soft
  ink on cardstock, white on the plaque red, red titles on
  charcoal) and their longest real lines fitted at font
  scales 1.0 and 1.3.
- **Suite note**: at ~5,800 checks the suite's single
  `main()` hit the JVM 64 KB method limit; sections live
  in their own functions from here on (this one does).
- Honest gaps: the audio budget is math, not ears — the
  mix is quieter by design now and still unheard on a real
  device; the peak budget is proven at DEFAULT volumes
  (at maxed Settings sliders the OS mixer is the last
  line of defense, as in most apps).

### v0.2.0f (versionCode 21) — The season mystery: "The Lights Over Stark's Pond"
Queue #2, item 6. One long story told across five in-game
weeks through the machinery the town already has. Small round
lights bob over Stark's Pond at night; small things go missing
all over town — the Elementary flag, a case of Cheesy Poofs,
the Church bell's clapper, four left shoes. The town theorizes:
aliens (Jimbo's pamphlet), a gnome franchise (Tweek), a Tegridy
drone ad campaign (Kyle). The truth is smaller and dumber than
every theory — and it is findable, if you do the work.
- **The arc** (`Mystery.kt`, pure Kotlin): five chapters, each
  a full shenanigan — "Lights Over the Pond", "The Vanishing",
  "Everybody's an Expert", "The Photograph", "The Reckoning at
  the Pond" — 4 stages, causers, rotating episode titles, cold
  opens, the works. Chapters air on a weekly cadence (first one
  from day 3, then every 7 days) and take the day's event slot
  outright: never on the same day as a regular event (one slot)
  and never on a holiday episode's day (a due chapter defers,
  day by day, until the holiday clears). Regular events fill
  the other weeks exactly as before. The chapter defs resolve
  through `SHENANIGAN_BY_ID` like any event but are NOT in the
  random pool.
- **The clues**: 12, gathered three ways — **witnesses** (talk
  to Jimbo, Kenny, Wendy, Towelie or Stephen once their chapter
  has begun; their line rides the talk sheet's pre-lines and
  grants once), **evidence** (Look at Stark's Pond, Kid's Park,
  the Elementary, the Mall or Butters' house at the right point
  in the case), and **participation** (get your hands dirty at
  chapter 1's pond and chapter 3's town meeting). Every clue
  grants exactly once, hits the ticker and the town log, and
  points the same small, tired direction.
- **The board**: the Journal gains a 🔍 MYSTERY page — the
  chapters so far, every clue found with its flavor, how many
  are still out there, the theory the town currently believes
  (it shifts per chapter, from pamphlet to omen), and, once the
  case closes, the stamp it closed with.
- **The finale**: at chapter 5's climax, a player holding at
  least **7 of the 12 clues** gets a real choice (presented at a
  safe moment, like the episode cards): **present the truth** —
  it's Professor Chaos: one small boy, a foil-wrapped bike
  helmet, and a blanket-fort Fortress of Doom — or **say
  nothing** and let the town have its aliens. With fewer clues
  there is no choice: the town picks its own ending, and it
  picks aliens. All three landings write their own outcome,
  credits lines, faction ripples and permanent marks (the
  dismantled fort / the framed Pond Alien Ban, in two flavors)
  — and if the player never speaks up, the town decides.
- **The long tail**: after the finale the arc RETIRES — one
  arc per save, it never re-runs — regular cadence resumes
  forever, and ambient pairs keep telling the ending the town
  believes in (Threads-style callbacks, in two banks: the town
  that knows, and the town that banned the aliens).
- Saves: additive only (`arc` + `mysteryClues`); old saves
  start the case fresh, mid-arc saves keep progress, corrupt
  chapter counts clamp, invented clue ids are dropped on load.
  No new art or audio files — the finale choice rides the
  existing act-sting synth.

### v0.2.0e (versionCode 20) — Minigames: the town gets games of its own
Queue #2, item 5. Three little games on one shared scaffold —
a **pure seeded session** (`Minigames.kt`: state machine +
all rules, deterministic under its seed, the suite drives it
tick by tick), an Android view that draws and takes taps
(`MinigameViews.kt`), and a settle-exactly-once reward step
that pays through systems the town already has. No new
currencies, no new art style, three new synth sounds.

- **🎣 Fishing at Stark's Pond** — tap the pond, "Fish the
  pond". Cast, watch the bobber, tap the moment it dips.
  The bite delay bends with the world: **dawn (5–8) and dusk
  (17–20) bite best, rain is grandpa-approved excellent,
  midday sulks, a blizzard all but closes the pond** and
  tightens the hook window (950ms → 700ms). Tap early and
  you scare it off; tap late and it's a story. The catch
  table runs Pond Smelt → Mud Minnow → Speckled Trout →
  Rainbow Trout → **Golden Carp** → **Three-Eyed Fish** →
  one (1) **Old Boot**, each logged to the fishing log
  (species, count, biggest) and landed **in your pack as a
  giftable item** — Jimbo loves the carp, Randy has
  questions about the three-eyed one, Chef could do things
  with the trout. Bite plip and catch splash synthesized
  (`fish_splash`).
- **❄️ Snowball Showdown** — at Kid's Park or Main Street,
  **in snow weather only** (the menu says so when it isn't,
  because rules are rules): tap a kid for the formal duel,
  or tap the drawn fort — white blocks + ammo pile in snow,
  three planks and a hopeful sign the rest of the year — to
  pick your opponent. First to **3 hits**: your throw is a
  sweeping **timing bar** (release in the red; zones fall
  off perfect → good → ok → wild, then the kid's own dodge
  stat — Tweek is a ghost, Butters is a barn), their throw
  is a **dodge window** at the tail of the snowball's
  flight that shrinks as rounds wear on. Winner takes
  **5 🪙 Capsule Coins**, loser takes 1 for showing up;
  friendship swings through the town's own pipe (+3 for a
  win, +1 for a good loss) — except **Cartman, whose swing
  runs backwards either way (−2)** — beat him and he accuses
  you of cheating, in
  writing, by name; Kenny stays muffled-enthusiastic
  through the parka). W/L per opponent persists.
- **🌮 Taco Catch at Freeman's Tacos** — the drawn arcade
  cabinet inside (marquee lit, coin slot that has never
  once been fed) runs a **60-second catcher**: drag the
  tray, tacos +10, burritos +15, and dodge the falling
  **TVs (−25)** and **Memberberries (−15)** — hazards stun
  the tray briefly. The whole spawn schedule is rolled from
  the seed up front (faster and denser as the minute wears
  on). Score tiers pay capsule coins (80+ → 2 … 600+ → 12);
  high score and play count persist.
- **🪙 Capsule Coins** — an honest note on the design: the
  town never had a coin currency, so coins are **an
  inventory item** (gift-tier, Clyde/collector-loved)
  granted and counted like anything else you carry — they
  stack in the pack, gift through the normal gift system,
  and the Journal counts your pile. No parallel economy.
- **🎮 GAMES page in the Journal** — the one home: coin
  stack, the fishing log (uncaught species as ???), showdown
  W–L by opponent, taco high score + runs, and **6 game
  feats** (First Cast, Golden Touch, Local Legend, Fort
  Shutout, Sore Loser, Taco Titan at 500+).
- Sounds: `fish_splash`, `coin_ding` (the arcade two-bell
  payout), `taco_pop` — SFX kit now 21 strong, cache stamp
  bumped to v020e_r1. Saves additive-only (see Status). The
  suite pins everything above plus availability gating for
  all three games (location, inside/outside, snow).

### v0.2.0d (versionCode 19) — Writing depth pass #2: the town finds its voice, everywhere
Queue #2, item 4. v0.1.9e gave the town *structure* for talk
(memory, threads, goals); this pass deepens the *words themselves*
in every place the town speaks — mock brain, LLM prompts, small
talk, and generated NPCs. Everything newly written in each
character's voice (lorebook persona paraphrased, never copied
dialogue):

- **Voice banks doubled.** Every cast member's collision bank went
  4 → 9 lines (`MORE_LINE_BANKS` in CastData.kt, layered onto the
  base banks by per-key list concatenation — original lines
  untouched): 160 new lines grounded in each character's
  obsessions, relationships, and running gags — Kenny's are all
  muffled, Tweek's are all gnomes, Randy's all become schemes
  within two sentences. Town total: 288 collision lines.
- **Pair scripts: 6 pairs → 28.** `MORE_PAIR_SCRIPTS` adds two
  scripts each for 22 relationship/rival pairs the original six
  skipped, chosen from NPC_RELATIONS (Stan–Wendy, Craig–Tweek,
  Wendy–Bebe, Jimmy–Timmy, both married couples, Stan–Randy,
  Kyle–Sheila, Butters–Stephen, the Kenny trio-pairs,
  Cartman–Wendy, Garrison–Mackey, Randy–Towelie, Jesus–Father
  Maxi, Craig–Clyde, Michael–Pete, Randy–Gerald, Jimbo–Stuart,
  Tolkien–Craig, Stuart–Carol), plus a second script for the three
  pairs that shipped with one. 57 scripts total. One relations
  entry added: Cartman–Wendy, "classroom rivals."
- **SPEECH RULES in the LLM prompts.** A new per-character voice
  card for all 32 cast (`SPEECH_RULES` in CastData.kt: verbal
  tics, sentence habits, what they'd never say, deflection style)
  is spliced into both prompt builders as `VOICE — <name>: …`
  lines, so the cloud brain holds voice the way the mock brain's
  banks do. Generated NPCs keep their lorebook-entry personas.
- **Topics & threads.** DAILY_TOPICS grew: core five 24 → 28,
  the other 27 cast 12 → 18 (626 topics town-wide). Three new
  conversation-thread classes — "crushes & drama", "pets &
  critters", "tech & gadgets" — join the ten originals in
  `NpcMemoryStore.TOPIC_WORDS` (appended last, so existing
  classifications keep priority), each with follow-up templates;
  every original class also gained two more follow-ups, generic
  follow-ups 3 → 5, topic callbacks 3 → 8, rival pair jokes
  7 → 12.
- **Generated NPCs sound like themselves.** `NpcGenerator` now
  builds an ambient small-talk pool per persona from templates
  keyed to occupation (28 roles, 3 lines each, with role aliases
  like police officer → police), traits (24 traits × 2), and a
  goth set for goth entries — with a neutral fallback so no pool
  is ever empty. Visitor replies now draw from it alongside
  lorebook facts (and deep lines for friends), so a generated
  farmer and a generated goth no longer sound alike.
- **Freshness guard.** `Engine.freshLine` keeps a small
  per-speaker ring of the last 3 ambient lines they said
  (in-memory only — saves untouched) and steers mock-brain picks
  away from them, so the same kid doesn't open with the same
  line twice in a row; the suite also pins zero duplicate lines
  inside every bank and topic pool.

### v0.2.0c (versionCode 18) — The selfie system: say cheese, get famous
The Fractured But Whole's signature social mechanic, rebuilt in this
game's own terms (Queue #2, item 3). The town has been posed for,
talked at, and gifted — now it can be *photographed*, and it has
opinions about that.

- **📸 SELFIE on the bottom bar.** Whenever townsfolk are standing
  with you (the exact presence Talk uses — cast and generated
  folks, interior-aware), the shutter takes the shot: you front and
  center, everyone else arranged around you, the scene's own
  palette and weather as the backdrop. A synthesized
  click-clack + flash pop (`camera_shutter`, procedural like every
  sound since v0.2.0b — the SFX kit is 18 strong now) marks it.
- **Photos are records, not pixels.** Each shot stores who, where,
  when, the weather, your outfit, and every figure's mood and pose;
  `PhotoView` re-renders it deterministically with the game's own
  figure pipeline whenever you look at it. No bitmaps, no image
  files — the album weighs nothing and never rots.
- **Reactions follow the relationship.** Close friends go GOOFY —
  bunny ears behind the head, tongues out (a new `goofy` face in
  the expression system: squeezed ^^ eyes, grin, lolling tongue),
  and paired **hat swaps** between two goofy friends. Friends pose
  happy, acquaintances and strangers pose stiff, and rivals either
  refuse ("said no. Rude." — it goes in the caption) or, 1 time in
  4, **photobomb** you: leaning in huge from the frame's edge. A
  bomb still counts as being photographed. Generated residents and
  visitors react off their memory points, same as the cast.
- **Followers — the joke, with our numbers.** A first photo with
  someone pays out by prominence (cast 12, residents 6, visitors 4)
  scaled by affection (a close friend pays double, a photobomb
  half); re-photos trickle +1. Milestones (10/25/50/100/250/500/
  1000/2500) fire ticker celebrations, a town-log line, and a
  badge in the album header. The Journal's 📸 ALBUM keeps the
  newest 60 photos (older ones fade; the fame they earned stays),
  each stamped like a polaroid and tappable for the full view.
- **Tie-ins, all additions, nothing gated.** An NPC's current goal
  can now BE "get a photo with the new kid" (the Goals PHOTO kind,
  ~1 goal in 9, completed by a shutter click with them in frame);
  a selfie at a shenanigan's cluster location scores participation
  once per event; and six **📸 PHOTO FEATS** live on the Collection
  screen — the Whole Squad, a Famous Pair, a Full House (6+ in
  frame), Photobombed!, Holiday Spirit, Night Owls — pure
  bragging rights that never lock existing content.
- **Save impact:** four additive keys in the game doc; old saves
  load with an empty album and zero followers. File count in `src`
  goes 40 → 42 Kotlin files (Photos + PhotoView).

### v0.2.0b (versionCode 17) — The audio pass: the town learns to make noise
The game shipped silent for sixteen versions. The show and the games are
many things, but silent is not one of them. This build gives the town a
voice — under the same rule as the art: **nothing ripped, nothing
sampled, ZERO audio files in the APK.** Every sound is synthesized in
code at runtime (`SoundGen.kt`, pure Kotlin: additive harmonic plucks,
shaped noise, sine-partial bells) and played by `SoundEngine`
(`Sound.kt`: SoundPool one-shots rendered once to a cache-dir WAV set,
AudioTrack loops for music and ambience). The suite pins it: package
contents contain no audio, renders are deterministic and bounded.

- **The town tune (original composition).** A banjo-pluck loop written
  for this game as note data (`TownMusic`): a 30-note D-major pentatonic
  melody over D–G–D–A–Bm–G–D–A at 96 BPM, with a banjo-roll
  accompaniment and bass plucks, decay tail folded over the seam so it
  loops clean. It is an original line composed for the table below —
  not a transcription or arrangement of any existing tune. It plays
  softly outdoors by day, comes through the wall at 35% indoors, and
  goes to bed 11 PM–6 AM.
- **Speech blips.** Every dialogue bubble chirps in its speaker's pitch
  family: kids high, adults low, Chef from the basement, girls a touch
  brighter — and Kenny's blip runs through a heavy lowpass with a
  parka wobble, because of course it does. Generated townsfolk get a
  stable hash-picked family. Blips are rate-jittered per line and
  throttled so a four-line batch chirps instead of buzzing.
- **SFX kit (17 one-shots, all procedural).** UI tap/select/back;
  snow-crunch footsteps (four seeded variants) when you travel in snow
  weather or winter, floor taps indoors, soft steps otherwise; a
  creaking door with a latch click going in and a thud coming out;
  snowball whoosh → splat (330 ms later, right on cue); a two-bell
  gift chime (a *loved* gift adds the sparkle); the collectible
  "found it" pentatonic sparkle (completing the set adds the quest
  fanfare); quest-accept fifths and a four-pluck quest-complete;
  an error thud for refused exits and other dead ends.
- **Episode & event audio.** The title card gets a banjo-roll sting,
  act-break cards a dry two-pluck sting, the credits wrap-up a slow
  closing motif (filing it plays quest-complete), and the moment a
  shenanigans stage actually advances in the engine, an unresolved
  two-pluck stage cue sounds — the card's own sting still waits for
  its safe moment, as before.
- **Ambience beds that follow the world.** Seamless generated loops,
  crossfaded (never stacked), selected by weather/time/indoors:
  gusting filtered-noise wind (light for snow and fog, strong for
  heavy snow, blizzards, and wind weather), rain patter with seeded
  droplet ticks, two crickets trading chirps on clear nights, and a
  barely-there brown-noise room hush indoors. Clear days have no bed —
  the tune carries them.
- **Settings → 🔊 SOUND.** Master mute plus separate Tune / Effects &
  voices / Weather beds volumes (0–100 in 10-point steps, each row
  previews its own channel), persisted under additive `audio_*` keys,
  defaulting ON at modest volumes (60/80/60). Square-law gain taper.
- **Politeness & lifecycle.** Everything rides STREAM_MUSIC
  conventions (`USAGE_MEDIA`): the media volume keys rule, and when
  the ringer is on SILENT the tune and beds stay off entirely.
  Rendering happens once on a background thread at first launch;
  loops pause on `onPause` and resume on `onResume`; playback calls
  before warm-up are quietly dropped. No per-frame synthesis, ever.
- **Save impact:** none beyond the four settings keys. File count in
  `src` goes 38 → 40 Kotlin files (SoundGen + Sound).

### v0.2.0a (versionCode 16) — Scene parity pass #2: interiors depth & location identity
The 0.1.9 series finished its roadmap at v0.1.9f, so **this build opens
the 0.2.0 series** (Queue #2, item 1). Pass #1 (v0.1.9a) gave every
exterior its defining details and deliberately left the rooms alone —
they were "already individually themed." They were also five generic
props on a wall, four rooms shared one floor color, and the Mall and
the school shared a wall. Pass #2 goes inside:

- **Signature set-dressing for all 26 interiors.** A new two-layer
  dressing pass in `InteriorArt` (`drawInteriorWallDetails` /
  `drawInteriorFloorDetails`) adds **79 pieces** — 3+ per room, every
  one unique to its room — drawn in the same flat style: Kenny's rail
  of spare orange parkas and one bare bulb; Butters' chore chart (two
  stars earned, one row empty) and the chair in the corner; Cartman's
  plush frog, snack-bag drift and the cat's litter box (ears up);
  Stan's leaning guitar and maple-leaf poster, plus the dog's bed;
  Kyle's menorah shelf and a crayon masterpiece; Tolkien's gold
  portraits, chandelier and a grand piano; the school bus's rear-view
  mirror and lettered route board over a dark aisle; the diner's
  wall clock, pie case and jukebox; Tweek Bros' three-phase profit
  chart (phase 2: ???) and sacks lettered COFFEE; Jimbo's rifle rack,
  deer mount and an "IT'S COMING RIGHT FOR US!" sign; City Wok's
  lantern string, lettered CITY menu board and a steaming wok
  station; Skeeter's buzzing neon, beer taps and pool table; the
  Bijou's screen curtains, lobby posters and ticket booth; Tom's
  nose chart (✗, ✗, ✓) and a HOFF poster; the Bank's teller bars,
  an "AND... IT'S GONE" plaque and the red carpet runner to
  disappointment; the Post Office's WANTED poster, parcel scale and
  MAIL sacks; Freeman's menu boards, pepper ristra and the ON AIR
  narration booth; Raisins' big round sign, car-name staff plates
  and a FAMOUS WINGS A-frame; the Church's six-petal rose window,
  hymn board, organ pipes and collection stand; City Hall's mayoral
  portrait, the TOWN MEETING agenda board (1. NOTHING, 2. ADJOURN)
  and the town-seal rug; the Elementary's HALL OF HONOR memorial
  (a chef's hat under glass), cubby row and globe; the pavilion's
  KID'S PARK banner, notice board and LOST & FOUND box; the fishing
  hut's mounted fish, woodpile and tip-up flags; the Mall's SALE
  windows, YOU ARE HERE directory and a last-economy kiosk;
  SoDoSoPa's exposed-brick band, glowing CRAFT sign and vinyl
  crates; the Tegridy barn's lettered banner, jar shelf and feed
  sacks. A registry (`INTERIOR_ART_DETAILS`) is cross-checked
  against the art source by the smoke suite — same discipline as
  pass #1 — and every token belongs to exactly one room, so no
  interior can quietly fall back to being a template.
- **Interior signage has words now.** 25 of the pieces carry drawn
  lettering (`INTERIOR_SIGN_TEXTS`: sign → exact headline text),
  suite-pinned to the literal strings in the art source: menus,
  route boards, neon, name plates, plaques.
- **Palette identity.** Every location now carries a palette
  signature (`LOC_PALETTES` in GameData: field / wall / floor /
  accent), cross-checked against the values the renderers actually
  use so the record can't drift. 5 interior walls and 15 floors
  were re-tuned within their families to kill duplicates and
  near-duplicates (the Marsh walls went deeper sage, the bar went
  darker, the Mall turned cream, the diner floor is diner tile).
  Measured on the final values: every wall differs from every
  other wall by RGB distance ≥ 20.3, floors ≥ 18.2, fields ≥ 17.0,
  all 26 accents distinct, and map neighbors (EXITS pairs) differ
  by a combined signature distance ≥ 82.9 — the suite pins floors
  of 18 / 16 / 14 / 60 so identity can't erode back.
- Nothing else moved: NPC anchors, hotspots, the prop row, the
  weather/season/holiday layers and the pass-#1 exterior registry
  are untouched, and there is **no save change** in this build.

**Verification:** full clean rebuild; JVM suite 4,497 checks /
0 failures; badging 16 / 0.2.0a; same cert as v0.1.9f; lorebook
bundled; the only image in the APK is our own generated launcher
icon; fan sheets NOT regenerated (no sprite/spec data changed).

### v0.1.9f (versionCode 15) — Polish & performance (roadmap item 7, queue complete)
The unglamorous build: nothing new to DO in town, but everything
underneath is faster, harder to break, and easier on the eyes.
**This completes the update-train roadmap queue** (items 1–7).

- **A drawn launcher icon.** The APK shipped the stock Android
  icon until now. `tools/gen_icon.py` draws ours in code from the
  game's own palette — banded paper sky, snow-capped mountains,
  pines, little houses, and a crooked parchment SOUTH PARK sign —
  regenerated by `build_apk.sh` at build time (non-fatally: no
  PIL, the checked-in PNG ships). No external images, as ever.
- **Performance, honestly scoped.** GameView redraws the whole
  scene at 20fps, and the draw loops were re-parsing the same hex
  color strings on every paint creation — hundreds per frame.
  `Paper` now caches parsed colors and darker/lighter shades
  (pure functions; bit-identical output), and keeps its grain
  paint instead of allocating one per frame. Paints themselves
  are NOT pooled — callers legitimately mutate them (align/alpha/
  size), so pooling would have changed pixels. `Prefs.saveGame`
  also skips the write when the serialized doc is byte-identical
  to the last one (it has ~45 call sites, often back-to-back).
  No fps numbers are claimed: the JVM suite pins the structure
  (exactly one parse site left in Paper — the cache's own), and
  device feel is for playtest.
- **Corrupt saves degrade; they never crash or wipe.** The
  hand-rolled Json parser recursed without limit — a damaged
  value made of nothing but brackets threw StackOverflowError,
  an Error the loaders' `catch (Exception)` guards could not
  catch. It now has a depth cap that surfaces as a normal
  unreadable-document error. The doc→state conversion was
  extracted from `Prefs.loadGame` into pure `decodeGameState`
  (so the suite pins every era's save shape through the real
  code) and hardened field by field: time/date clamp into
  range, bogus location ids land at the Bus Stop (a corrupt
  PLAYER location used to mean a black screen), coords clamp
  to the grid, and a malformed `todayKey` is rebuilt from the
  date fields — the engine slices that key unguarded, and a
  short one crashed the first tick (found by the new day-0
  test; `TownLife.birthdayOf` is total now too, and
  `ordinalOfKey` rejects impossible months instead of indexing
  out of bounds). If the main doc is unreadable at all, the new
  `recoverGameState` salvages friendship, the collection and
  the dates from the standalone keys instead of returning
  "no save" — previously a fresh town's first save would have
  overwritten those survivors.
- **Save format, on the record.** The doc now stamps its
  format generation (`saveFormat`: 1 = the original doc,
  2 = the v0.1.5 additive doc, 3 = this build), `inferSaveFormat`
  reads any era, and Settings shows the line under 💾 SAVE DATA.
- **Accessibility: the system font scale reaches the canvas.**
  Activity text was already sp-based TextViews, but GameView's
  drawn chrome (event strip, speech bubble, name tags) used raw
  px sizes. It now reads `Configuration.fontScale` through
  `Chrome.fontScaleOf` (clamped 0.85–1.30): text follows the
  scale, geometry only ever GROWS, and at scale 1.0 every
  overlay number is byte-identical to the shipped layout — the
  v0.1.6 no-overlap invariants hold at every scale, pinned as
  arithmetic. Contrast pairs re-verified for the pairs actually
  in use (Settings body on charcoal, bubble ink on paper white,
  toggle greys, red titles as large text).

### v0.1.9e (versionCode 14) — Simulation depth: goals, threads, more stories
Roadmap item 6: the town's people stop being schedule-followers with
line banks. Everybody now has something they're UP TO, conversations
remember themselves, the story deck doubles down, and holidays become
things the town actually does.

- **NPC goals (`Goals.kt`, new).** Every NPC — all 32 cast, generated
  residents, even the rotating visitors — carries a current short-term
  goal derived from who they are: errands, meet-ups, small projects,
  grudges, cravings, shifts, schemes and faction business, weighted by
  profile keywords, lorebook relations, faction, traits and occupation.
  A goal is a pure function of (NPC, 2-day cycle), so the Journal, the
  renderer and both dialogue brains all agree on it with no
  coordination. Goals DRIVE behavior: leisure schedule blocks bend
  toward the goal's place (wearing "On a mission: …" as the activity),
  residents' hangouts and half of all visits bend the same way, NPCs
  talk about their goals in ambient chats and greetings, and standing
  at the goal place (or meeting the goal person) works it — finishing
  writes a ticker line, a town-log entry and a memory trace. Goals
  expire on the 2-day cycle rollover (a couple of unfinished ones get a
  quiet ⌛ obituary), and every WHO card — cast and generated — shows
  "🎯 Currently: …" (or "🎯 Wrapped up: …").
- **Conversation threads (`Threads.kt`, new).** Continuity now runs on
  the memory that's already there: per-NPC topics (≤6) are the player
  thread store, and a per-pair topic slot remembers what two NPCs last
  talked about. From the second real conversation on, NPCs open with
  follow-ups ("Did you ever sort out that money stuff?") instead of
  cold openers — a third conversation with someone reads different from
  the first, in the mock brain AND in the LLM prompt (the memory block
  carries the same THREAD context; collision prompts carry the pair's
  last topic, their running joke once the bond is real, and both
  speakers' current goals — Phase-1 #16 parity). Bonded pairs keep a
  seeded running gag, the same joke for the same pair.
- **Shenanigans 6 → 10.** Four new stories, each with the full kit
  (4-stage arc, causers, 2 rotating titles, cold open, weather locks,
  faction effects, participation, residue): **The Coon Sighting Panic**
  (a caped "hero patrol" leaves the town reporting a raccoon vigilante;
  fog climax), **Memberberry Fever** (Tegridy's nostalgia crop turns
  every conversation into "'Member when…"; new `member` dialogue tone),
  **The Imagination Leak** (something imaginary got out at Stark's
  Pond; the town debates it at the Church), and **Cash-for-Gold Fever**
  (a screaming TV ad empties every jewelry box in town; new `gold`
  tone). The new events join the lineage web both directions and the
  weather-weighted deck (fog favors the Coon and the Leak, sun favors
  the Berries).
- **Holiday traditions.** All 8 holidays gain a TRADITION: a gathering
  place and hours the town's schedules actually bend toward (caroling
  at the Church, trick-or-treating on Main Street, the Black Friday
  line forming its own government at the Mall at 5 AM…), per-place
  dressing written for Look AND drawn in SceneArt (tombstones,
  luminarias, confetti cannons, the patient Cow Days cow, sale
  banners, line tents — a registry cross-checked against the art
  source by the suite), and tradition dialogue beats any NPC can drop,
  so holidays are no longer lines only the scripted few can say. All of
  it rides the v0.1.9c episode machinery — title cards and credits
  wraps still fire.
- Saves: ONE additive key (`npcGoals` in the game_state doc; absent in
  old saves → goals derive fresh, nobody's memory is touched).

### v0.1.9d (versionCode 13) — Creator depth: dress like the games
Roadmap item 5: the character creator grows from 7 categories / 54 options
to 13 categories / 114 options, all of it flowing through ONE construction:
the appearance map is turned into a SheetSpec by the new pure
`avatarSpec()` (GameData), and every player-figure render (creator
preview, the Journal's 🪪 YOU card) draws it through
`FigureGeometry.derive()` — no second figure style (Phase-1 #40's rule).

- **Six new categories.** BUILD (below), SHOES (black shoes, three
  sneakers, boots, bare feet), EYEWEAR (glasses and sunglasses graduate
  from the accessory slot; new ski goggles and star shades join),
  FACIAL HAIR (chinstrap, full beard, mustache, goatee — styled beard
  variants in the shared head renderer), GLOVES (three mitten colors,
  two glove colors — they color the figure's hands), and BACK ITEM
  (backpack graduates from accessory; a hero cape and costume wings are
  drawn in a new behind-the-body layer in both figure builders).
- **Every old category grew.** Skin 6→8, hair styles 10→18 (bob,
  pigtails, top bun, flat top, curtains, shaggy, side braid, bowl cut —
  all generic cuts drawn in the sheet system's hair language; no cast
  signature look is on the shelf), hair colors 7→12, hats 9→13 (trucker,
  earmuffs, headband, bandana), tops 9→16, bottoms 6→12. Tops now wear
  their CONSTRUCTION: the style token maps to the sheets' torso details
  — jackets get placket + buttons + flap pockets + collar band, parkas
  a zipper placket, hoodies/sweaters a collar band, vests placket +
  buttons (previously every top rendered as a plain colored torso).
- **Build choice.** Five kid builds — Average (the classic unmeasured
  New Kid geometry, exact), Slight, Stocky, Tall-for-a-Kid, Petite —
  carried as MeasuredProps in the same slot the cast is measured into,
  so the preview and the YOU card wear your proportions through
  derive() (generic builds; no cast member's measured set is reused).
- **The games' creator behaviors.** A 🎲 Roll button deals a coherent
  look from the unlocked shelf only (locked finds are earned, never
  rolled; never the full underwear combo), and three preset slots save
  favorite looks (tap to wear, hold to save) in an additive
  `creator_presets` save key. The #42 collection unlocks (Mr. Hat cap,
  Lorde wig, foam finger) sit in their categories with the 🔒 treatment
  as before.
- **Old saves just work.** Appearance strings extend additively: a
  pre-0.1.9d string gains the new categories' defaults, and accessories
  that became categories migrate on load (glasses/sunglasses →
  eyewear, chinstrap → facial hair, backpack → back item; an explicit
  new-category value always wins). #48's outfit comments now cover all
  13 categories — the lines moved to the pure `outfitCommentLine()` so
  the suite pins them (the v0.1.8 lines are unchanged); #43's YOU card
  gained your drawn figure.

### v0.1.9c (versionCode 12) — Episode framing: every story is an episode
Roadmap item 4: shenanigans events (and holidays, through the same
machinery since #31) now PLAY like episodes of the show. A new pure-Kotlin
layer (`Episodes.kt`) owns the records and all generated text; the engine
only flips flags, the Activities only present. The 4-stage machinery,
participation, marks and quest reskinning underneath are untouched.

- **Title card.** When an event (or holiday) kicks off, an episode record
  opens and a full paper-chrome title card queues for the next safe
  moment: the SOUTH PARK header as drawn text on the crooked red plaque,
  a generated episode title (the def's title, rotating with two newly
  written alternates per story — repeat runs bill like returning bits),
  a rotating treatment line ("TONIGHT, ON SOUTH PARK…", "A VERY SPECIAL
  EPISODE"…), the causers credited under STARRING (new per-def data),
  and the four acts previewed. One chunky button starts the day.
- **Cold open.** If you're standing with the causers — or at the kickoff
  cluster — when it starts, the premise plays first as an opening beat:
  two newly written lines per story (Cartman: "I cornered the town's
  whole Chinpokomon supply…") land in the speech bubble and the ticker
  rolls a 🎬 COLD OPEN line, all before the title card. Not there? The
  title card alone carries it.
- **Act breaks.** Each stage transition queues an act-break sting —
  ACT I/II/III/IV card with the stage title and its one-line teaser.
  Stings NEVER interrupt: the engine sets a pending flag and the card
  presents only at a safe moment (scene entry, dialogue close — the
  sheet's new dismiss hook — resume, app start, or the previous card's
  dismissal). If you miss acts, one sting covers everything up to the
  current act; nothing stacks and nothing is lost mid-conversation.
- **Credits wrap-up.** At resolution the episode finalizes where the
  outcome always landed (midnight rollover): the recap diffs the
  faction influence snapshot taken at kickoff, prints your participation
  ("✋ Your hand in it: 7/10 — …"), the outcome line, the event witness,
  the 🗿 mark the event left and who kept a piece of it in their routine
  (eventResidue now reports its name). It's presented once, end-credits
  style, on the same show card.
- **Journal episode list.** A new 📺 EPISODES section shelves every
  episode, newest first (events and holiday specials alike; airing ones
  read ▶ NOW PLAYING). Tapping one opens its episode page — acts with
  ✓/▶ markers, the full recap once it has wrapped — the same card the
  wrap-up presented. Records persist in an additive `episodes` save key
  (capped at 30).
- **Old saves are non-blocking.** A save written mid-event before
  episodes existed gets a synthesized "joined in progress" record on
  load: title card and past acts pre-shown (nothing re-triggers), but
  the next act still stings and the credits still roll.

### v0.1.9b (versionCode 11) — UI / game-chrome parity pass
Roadmap item 3: the menus and chrome now speak the games' language
(Stick of Truth / Fractured But Whole), drawn in code in the same flat
paper discipline as the scenes. Two new files own the system:
`Chrome.kt` (pure Kotlin — design tokens + ALL chrome layout math, so the
JVM suite can pin it) and `UiKit.kt` (Android builders: cardstock panels,
chunky game buttons, red title plaques, and the paper dialog kit).

- **The paper dialog kit replaces every stock AlertDialog.** All 14 of
  GameActivity's dialogs (Look, sleepy kids, gift pickers, Talk/Gift,
  snowball choice, doorbell, shop-closed, talk-to-who, the pond, the
  swap table, both time-capsule dialogs, the ☰ Menu, Reset Town) plus
  Settings' reset confirm now open as a parchment panel with a crooked
  red title plaque and chunky parchment item buttons (red primary /
  parchment secondary actions). Same titles, same items, same handlers —
  long item lists scroll inside the panel.
- **HUD bands became paper.** The event ticker is a parchment news strip
  with ink text (was translucent black); the location/time/weather bar
  is a translucent parchment band (location still red, the clock moved
  to ink bold — yellow doesn't read on parchment); the bottom bar's five
  buttons are chunky parchment game buttons on charcoal. The quest chip
  is a themed paper plaque (rounded, darker same-hue edge) that still
  reskins to the event theme on every refresh.
- **In-scene overlays.** The event strip gained a darker same-hue edge
  line; the conversation bubble is warm paper-white with an outlined
  tail and the games' speaker treatment — a color bar under the
  speaker's name. All fit/truncation math (event strip, bubble lines,
  name tags) moved into `Chrome.fitText` / `Chrome.nameTagFit` with the
  EXACT shipped semantics, now suite-pinned.
- **Dialogue sheet.** Title rides a crooked parchment plaque, the input
  field is a cardstock field with an edge line, Send is a chunky red
  game button, bubbles are paper-white, and each speaker name gets its
  color bar (bar at +18..+21px, body at +26 — no overlap, by the numbers).
- **List screens.** Journal / Inventory / Quest / Lore cards are now
  cardstock panels (rounded parchment, darker edge); the Quest header
  band is a themed plaque; Journal's nav buttons and the game's bottom
  bar match; Collection cells are cardstock tiles (parchment found /
  dark panel lost); the Lorebook's search field and chips are dark
  panel chrome, and the selected category chip now actually shows
  selected (red plaque, white text — it had no selected state before).
- **Settings + Creator + splash.** Settings buttons/toggles/fields run
  on the kit; the Creator's title rides a crooked parchment plaque, its
  nav buttons are game buttons, and the preview card gained its edge
  line; the splash title is a crooked parchment plaque card.
- **The hard constraint held.** No layout geometry moved: ticker 4dp /
  top bar 32dp / chip 92dp, chip bottom → GameView chromeTop, event
  strip 26px, bubble offsets (36/10) and heights are the shipped numbers,
  now expressed as Chrome constants and asserted in the suite (stacking
  gaps, bubble-clears-strip at any chromeTop, content fits the bubble,
  tag fit, contrast ≥ 4.5 for the chrome's text pairs). A source
  cross-check fails the suite if any screen falls off the kit.
- Chrome only: no feature, control, or save changes. Fan sheets
  untouched (no sprite/spec data changed).

### v0.1.9a (versionCode 10) — The leftovers, finished + scene parity pass #1

### v0.1.9a (versionCode 10) — The leftovers, finished + scene parity pass #1
Two scopes: every v0.1.8 leftover completed, and the places get the same
treatment the characters got in v0.1.7 — measured against the show.

**Scope A — the leftovers, finished:**
- **#12 Mail check ritual** (was: not implemented) — every house now wears a
  mailbox on its facade; each resident has a seeded morning window (6:50–8:59)
  where they drift out to it, peek in — then hop (💌 a letter, 55%) or slump
  (🧾 a bill). Pairs with the Post Office's new collection box.
- **#16 Hat grabs** (was: not implemented) — on windy days, standing figures
  periodically snatch one arm up to their headwear before the gust takes it
  (kid + adult poses, staggered per person), and Kenny's hood drawstrings —
  he has no hat to grab — flap the whole time.
- **#62 Chalk drawings** (was: not implemented) — tappable sidewalk slabs at
  the bus stop, the school, the park and Main Street. Tap bare concrete to
  chalk one of 8 doodles; it lives 5 days, and every other day another kid
  adds to it (up to 3). Read in Look and on tap.
- **#93 Lost mitten tree** (was: not implemented) — lost mittens now turn up
  as winter litter (10% of snowy litter drops); a dedicated pine at Kid's
  Park wears every one you hang on it (tap, up to 12, +rep each), counted
  in Look.
- **#19 Rain dash** (was: pose only) — the moment rain starts, everyone
  caught outside is rerouted to the nearest door (per-location shelter or
  nearest building) at 1.75× dash pace, arms over heads, and the schedule
  dispatcher leaves them sheltered until the rain ends; marks clear when
  it stops. An event line calls the scramble.
- **#33 Snow shake-off** (was: constant whitening) — cover now builds per
  person over a 26-minute personal cycle (full coat by minute 20), then
  they stop and shake it off: wiggle, snow burst, back to a dusting.
- **#41 Bedtime cascade yawn** (was: cascade only) — as a house's bedtime
  lands and its windows die one by one, a 🥱 bubble pops over the glass
  for the first nine minutes.
- **#89 Alarm clock panic** (was: seeded kid only) — before 8 the day's
  oversleeper's alarm shakes 💢 at their window; an event names them; and
  during their morning trip (7:50–9:30) they travel at 1.85× sprint pace
  trailing 💨 and a floating "late!".
- **#92 Mystery lunchbox** (was: static prop) — the box now sits on the
  bench all morning until the day's retriever (10 kids, rotating) leaves
  the bus stop — they grab it on the way out, it's gone, an event closes
  the case (until tomorrow). Last call 10:55 if they linger.
- **#97 Fan club** (was: static trio) — with 3+ Close Friends the club now
  trails your route: they creep in at the screen's edge when you arrive
  somewhere, settle into position with notebooks out once you stop — and
  scatter, leaving a dropped 📓, the moment you "turn around" (a fast
  camera pan; POV judgment call, closest faithful read in this engine).
- **#98 3:33 AM sightings** (was: tap-only) — on ~half of nights, if you're
  simply out at 3:20–3:45 AM, a tiny gnome-dark silhouette with a red hat
  tip crosses the yard on its own; completing the crossing logs it under
  ??? in Town History (shared guard with the old window-tap path — never
  double-logged).
- **#35 Crowd density** (was: approximation) — now the strict form: the
  ambient-conversation cap is a pure function of each location's own
  occupancy × an hour weight (0.17 at 3 AM … 1.0 midday, capped 6),
  counted per location — no flat cap, no shared pool for a bazaar to drain.

**Scope B — scene/background parity pass #1:** all 26 exteriors audited
against the lorebook blurbs and the show's stock shots; a new
`SceneArt.drawParityDetails` layer gives every location its defining
construction-paper details, and a registry (`GameData.LOC_ART_DETAILS`,
59 details) is cross-checked against the art source by the smoke suite so
a location can't silently lose its identity again. Palette corrections:
Jimbo's Guns to its orange walls (#b85c2a), Raisins to orange-red (#d96a4a),
the Marsh house to dusty blue (#3e6b8a), the bank to blue-gray stone
(#8a94a0). Per location: Kenny's — railroad tracks across the lot, sagging
porch, boarded window; Butters' — picket fence + mow stripes; Cartman's —
cracked driveway + red wagon; Stan's — attached garage + oil-stained slab;
Kyle's — hedge row + swept walkway; Tolkien's — columns, iron gate,
"DARK MEADOWS" arch; Bus Stop — asphalt road strip with dashes + plowed
snowbank; Main Street — five-shop storefront row + lettered SOUTH PARK
banner; Tweek Bros — hanging coffee-cup sign + menu board; Jimbo's — wagon
wheel + door antlers; City Wok — pagoda eaves + red lanterns; Skeeter's —
pink neon BAR sign + amber window glow; Bijou — bulb marquee ("NOW
SHOWING") + poster boxes; Tom's Rhinoplasty — giant nose sign +
before/after posters; Bank — columns, steps, $ pediment; Post Office —
flag + blue MAIL collection box; Freeman's Tacos — giant roof taco +
menu board; Church — stained-glass window + clapboard siding; City Hall —
dome + columns; Elementary — lettered SOUTH PARK ELEMENTARY band, double
doors, playground slide; Kid's Park — swing set + sandbox (plus the
mitten tree and chalk slab from Scope A); Stark's Pond — dock, cattails,
wooden name sign; Mall — SP MALL sign pylon, glass entrance canopy,
parking stripes; SoDoSoPa — district arch sign, string lights, patio
umbrellas; Tegridy Farms — silo, post-and-rail fence, hemp rows; Raisins
— orange roof band + wing signs. Interiors were audited too: the per-room
registry was already individually themed (walls/floors/props), so this
pass leaves them as-is. All pre-existing interactive elements (signposts,
doors, windows, containers) untouched.

**Verification:** full clean rebuild; JVM suite 3,502 checks / 0 failures;
badging 10 / 0.1.9a; same cert as v0.1.8; lorebook bundled; no image files
in the APK (code-drawn only, launcher icon aside); fan sheets NOT
regenerated (no sprite/spec data changed — the two new poses reuse the
existing arm system).

### v0.1.8 (versionCode 9) — The End Version: everything connects, everything lives
One release carrying all three commissioned lists, in order.

**Phase 1 — the 50 connected changes (existing systems interlock).**
Day-of-week is real now (school lets out on weekends, shops keep hours with
their keepers, Thursday ≠ Saturday), weather has a forecast and factions have
weather moods, and everything reports to everything else: Look reads the
turf war, the forecast, birthdays, events and your hangout participation;
the quest chip rotates objectives/moods/forecasts; talking leaves ripples
(witnesses gossip your rudeness/kindness, hangouts unlock deep topics,
memory follows people when they tier up). Generated citizens became full
citizens: they have likes, convert to residents after real friendship,
bring favorites into the visitor rotation, bend toward faction turf, and
crowd events. Interiors all have searchable containers now, shops lock when
their keeper leaves, and the Journal grew Likes lines, full-day agenda
cards, a "🪪 YOU" card, Town History and Marks; the Lorebook shows live
whereabouts. Three creator items (Mr. Hat cap, Lorde wig, foam finger)
hide in the world as unlocks.

**Phase 2 — the 100 living features (a town that breathes).**
A new pure-Kotlin layer (`TownLife.kt`): reputation with tiers strangers
use when they greet you, birthdays the town bends toward, night owls out
until 2 AM, a moon with phases (and aurora on rare winter nights), a
traveling gnome, a stray cat running a route, lost-and-found at City Hall,
puddles that freeze, crack, host paper boats and shrink in the sun. Scenes
fill with life: Stark's Pond gets ducks you can feed, fish jumps and a
telescope watcher; houses smoke from chimneys when someone's home and
cooking, windows light/darken by occupancy and bedtime, TV windows
flicker; crows, butterflies, fireflies, leaf streams, snow tracks,
clotheslines, rain barrels, sprinklers, a flickering streetlight you can
fix, the church bell at noon, the bus on route, coyotes at 3 AM. Small
human moments everywhere: pocket fidgets, coffee steam trails, phone
zombies, cold-snap sneezes, sleep-talking windows, doorbell visits,
bench sitting, snowball wars, a fence feud to mediate, a harvest swap
table, a lemonade stand, and a time capsule under the park. The Journal
gained a Milestone Wall of framed firsts.

**Phase 3 — the 100 sprite changes (construction fidelity).**
The draw code moved to the sheets' construction: flat-chord double chin,
overlapping binocular eye ovals with low-center pupils (smaller on
adults), curved deadpan lids, the new SOFT half-lidded face, sagging
sleep arcs, two-flick lashes, a real glasses bridge, rounded-rect shades,
ear inner arcs, Kenny's layered hood rings, thinner forehead wrinkles,
trapezoid torsos with flat hems, slanted-parallelogram sleeves drawn
BEHIND the torso, big mitten hands (handRad 4.5→6.5 kids, 5→7 adults),
flat-sole wide shoes, talk-gesture arms, a rain pose (arms roofed over
the head), chullo weave strips, head-scaled crown band, and full avatar
rendering for the new creator unlocks. All 32 fan reference sheets were
regenerated from the final specs (params.json synced to the measured
KidArt.SHEETS: 130 field changes).

### v0.1.7 (versionCode 8) — Measured, not approximated
- **Specs are MEASURED from the supplied images.** A new tool
  (`tools/measure_specs.py`, Python + PIL: flood-fill background removal,
  connected-component figure isolation, center-column color-run analysis
  with semantic merging, width-profile splits, warm-mask chart
  segmentation) extracted palettes + proportions from Lucky's images;
  raw output in `tools/measurements.json`. The core six are ground truth:
  Kenny/Cartman/Craig/Stan/Kyle from their construction sheets, Garrison
  from his character-model photo. Big corrections vs the hand-approximated
  v0.1.6 specs: Stan's jacket is a muted brown-red #9C5B55 (was brown),
  his pants gray-blue #636182 and hat slate #5668A6; Kyle's jacket is a
  saturated orange #F66118 and ushanka a pure green #239637; Craig's
  whole outfit is one slate-blue family (#636AA2/#636DA6, was bright
  blue); Cartman's jacket #CA152C, hat teal #52B9BA; Kenny's parka
  #FC5C06; Garrison's shirt #55C624 and pants #2D6543 (legs 0.348 >
  torso 0.322, measured). The other 26 cast members had no usable
  base-model image in the supplied 398 (variants/alter-egos only), so
  they carry individualized design-derived proportions (below). The
  image files themselves are never bundled — data + code-drawn art only.
- **Everybody has their OWN proportion.** Each cast SheetSpec carries a
  `MeasuredProps` (head share, torso share, leg share, head aspect,
  head:body width, relative height) and the renderer builds the figure
  from exactly those values via `FigureGeometry.derive` — no cast
  member snaps to a shared age-band template anymore (bands remain
  ONLY as the generated-NPC fallback, now with seeded per-NPC size
  jitter). Two same-age kids render as different builds because their
  sheets measure different. Mackey's giant head is proportion data
  now (head share 0.460) — his old `headScale 1.3` is retired.
- **Playtest defect 1 — heights/weights now show.** GameView sized
  cast by shared BAND height and `drawKid` re-applied the band ladder
  over the spec; cast now sizes by their own measured heightRel
  (tallest kid 1.04 < Michael 1.14 < shortest adult 1.30), width
  applies to the whole silhouette, and the crowd head-clearance uses
  the tallest figure actually present.
- **Playtest defect 2 — hair/hat layering fixed.** Long hair was
  invisible from the front (behind-layer only, covered by the head);
  it now has front locks framing the face. Ears draw BEHIND the head
  (were blobs on the face edge). Behind-hair rides the head's exact
  talk-bob offset. Fixed-pixel hats (beret, yarmulke, chef hat, crown,
  cowboy, wizard) are now fractions of the head ellipse; cap/trucker
  brims are frontal bands instead of a tab sticking out one side;
  Kenny's hood + face slit anchor to his derived head/eye geometry
  in his measured parka orange. Glasses lenses had a unit/pixel mix
  in their radius — fixed.
- **Playtest defect 3 — no more balloon heads.** Heads were drawn as
  a single-radius CIRCLE (66% of figure height) with face features
  pinned to fixed coordinates while `headScale` inflated the circle
  around them. Heads are now ELLIPSES from each character's measured
  head share x aspect (core boys 0.90-1.27, Garrison 1.043), with
  eyes/mouth placed as fractions of the ellipse so features scale
  WITH the head. JVM assertions pin rendered head share/aspect to
  the measured values for the core six.

### v0.1.6 (versionCode 7) — Sheets, limbs & life
- **The Sheet Rule (standing art direction).** Every character's render
  spec is now a reference sheet in the construction-sheet format — HEAD /
  TORSO / NORMAL ARMS / ARMS 2 / STAND / SIT / EYES OPEN / HURT EYES /
  SLEEP EYES / EYELIDS / mouth set — stored as `SheetSpec` data grouped by
  slot (`KidArt.SHEETS` for the cast, `NpcGenerator` output for generated
  NPCs, whose assembled part combos ARE their sheets). The five supplied
  sheets (Kenny, Cartman, Craig, Stan, Kyle) and the worked Mr. Garrison
  sheet are transcribed as ground truth: Garrison follows his FINAL
  sheet exactly — bald dome with gray #9A9A9A side fringe (no hair on
  top), forehead wrinkles, gray #8E8E8E brows, glasses, ears, #FBD9B0
  skin; bright #52B84B button shirt with #43A03E collar, chest pocket
  and silver #C0C0C0 belt buckle; dark green #1F6038 pants, #151515
  shoes; tall, slim adult build;
  Stan/Kyle/Craig/Cartman gained their sheets' torso details (V collars,
  plackets, buttons, Kyle's flap pockets); Kenny's brown mittens moved
  onto his arms.
- **Limbs are ATTACHED.** Arms and legs are capsules rotated around
  shoulder/hip anchors (`FigureGeometry`, pure Kotlin) that tuck into the
  torso silhouette — no floating ovals at any pose, size band or width.
  Jimmy's crutches moved in so his hands grip the crossbars; Tweek's
  coffee cup sits in his (now moving) hand. A JVM geometry check proves
  the attachment invariant for every age band x body plan (and proves the
  old floating-arm numbers fail it).
- **Fully procedural animation.** All math, no assets, driven by each
  character's actual activity: walk cycles (legs scissor, arms
  counter-swing, body bob + waddle sway) while a figure strolls to a new
  spot; idle breathing; blinks using the sheets' EYELIDS; talking =
  oscillating talk mouth + head bobble; sleeping = SLEEP EYES + slow deep
  breathing; nervous faces fidget. Redraw tick tightened 150ms -> 50ms.
- **Crowds stand on the ground.** Placement is a ground plane now: rows
  run back-to-front with a shallow pitch and depth scale, front row
  biggest and drawn last, with a head-clearance rule so no figure's head
  covers another's face — the bus-stop pyramid stack is gone.
- **Name tags + header fixed for good.** Tags show only for the front
  row and current speakers, shrink then ellipsize to their slot — long
  generated names can't mush together. The event ticker moved into the
  Activity chrome; GameView's event strip and speech bubble stack below
  a measured chrome boundary, so the location title can never overlap
  dialogue again. Ambient conversations now hand the weather quip to
  whoever did NOT just speak (no more double-Stan bubbles).

### v0.1.5 (versionCode 6) — The Living Town update
- **Movement rebuilt: swipe-to-look + point-and-click.** Scenes render into
  a world 1.7x wider than the screen; drag to pan the camera. Tap a
  character to talk (or gift), tap a gold-outlined door to enter, tap a
  wooden signpost to travel, tap the ground to walk there (ripple +
  camera glide), tap shinies, trash containers, and litter. Exit arrows
  are gone — signposts line the bottom of every scene.
- **Interiors for all 26 locations.** Real rooms: houses, the school
  classroom, Tweek Bros cafe, church, bank, Bijou theater, clinic, plus
  shelter interiors for outdoor spots (bus interior, pavilion, ice hut,
  barn, loft) and a Main Street diner. NPCs are inside/outside per their
  schedules and can be found and talked to on either side of the door.
  Interior windows show the current weather and time of day.
- **Age-band proportion ladder.** Characters render by actual age class —
  KID (10-12) → YOUNG TEEN → TEEN → OLDER TEEN → ADULT — heads stay
  near-constant while bodies grow taller/slimmer. The 32 v0.1.4 signature
  designs are untouched; bands adjust rendering geometry only.
- **Character system revamp + parts anatomy.** One registry
  (`CharacterSystem.kt`): identity, signature spec, age band, home,
  lorebook link per character, driving rendering + placement everywhere.
  Figures are assembled from parts with pose states (stand / sit) and
  expression states (open / hurt / sleep eyes, talking mouths) wired into
  dialogue, reactions, and sitting in interiors.
- **Lorebook people — procedural NPC generator.** New NPC type populated
  FROM the lorebook: 1,985 non-cast character entries eligible. A
  deterministic generator scans each entry for appearance signals (hair
  color/style, hats, glasses, beards, clothing colors, age → age band)
  and personality (traits, role, facts) and builds a custom-drawn
  character — never using cast signature identifiers. 8 rotating visitors
  at a time + up to 6 generated residents with their own schedules.
  Talking surfaces their lore; LLM dialogue grounds in their own entry;
  fallback lines are derived from the entry text.
- **Clock: 40 real minutes per in-game day** (20 day / 20 night; 1 game
  hour ≈ 100 real seconds). 8 AM start kept; real-time sync stays OFF.
- **Shenanigans events.** Rolled weekly, guaranteed monthly; six ship
  (Chinpokomon Craze, Talk Like a Pirate Week, Upside-Down Day, Tegridy
  Takeover, The Mole Panic, The Silent Challenge). Each runs ~1 in-game
  day as a 4-stage linear arc (kickoff → escalation → climax →
  resolution): stages change routines, dialogue tone, weather, litter,
  and factions. Banners + Journal track the story; character memory keeps
  a trace; events can leave permanent schedule residue.
- **Per-NPC character memory (local).** Cast AND generated NPCs remember
  you: meeting count, relationship points, last-met time, topics, gifts,
  capped rolling summary. LLM prompts include it; fallback greetings
  acknowledge history. Stored as its own `npc_memory` doc.
- **Relationship tiers.** Rival → Stranger → Acquaintance → Friend →
  Close Friend, driven by talk, gifts, compliments, insults (rudeness
  breeds rivals). Tiers change greetings and lines, unlock deeper talk,
  and close friends sometimes hand YOU gifts. NPC↔NPC lorebook
  relationships flavor dialogue and pairings. Shown on Journal cards.
- **Inventory.** Collectibles fold into a real pack (Inventory screen).
  Sources: daily shinies, trash dives, litter, NPC gifts. Gifting happens
  from the tap menu and is remembered by the NPC.
- **Quest tracker with dynamic appearance.** A themed chip on the main
  screen opens the full tracker (event stage chain + standing objectives:
  friends, gifts, trash, visitors, shinies, litter). Chip, tracker, and
  the in-game event strip reskin to the active event's theme; Town Board
  theme otherwise.
- **Weather completed.** Fog and wind join clear/cloudy/rain/snow/heavy
  snow/blizzard (8 conditions), drifting over the day cycle; HUD shows
  icon + label; effects render outside and through interior windows.
  Severe weather drives people indoors; events can lock the weather.
- **Dynamic trash + NPC litter.** 19 searchable containers refresh each
  morning with re-rolled loot (junk / giftable / useful / rare lore
  treasures) and a rummaged visual state. NPCs litter as they wander —
  visitors most, cast rarely, neat freaks never, slobs often — and ground
  litter is pickable loot (cap 40, overnight cleanup). Storms and events
  scatter extra debris. Junk is giftable; some NPCs love weird stuff.
- **Factions.** Eight lorebook-derived factions (the boys' crew, goth
  kids, girls' circle, Craig's gang, parents, school staff, church
  faithful, Tegridy crew) with influence, player standing, moods, and
  alliances/rivalries — reshaped by event stages, nudged by your gifts
  and friendships, settling toward baseline between events. Journal view.
- **Real schedules + slow drift.** Placement flows from agenda blocks
  through drift/event/weather transforms — one source of truth. Schedules
  drift gradually (new hangouts, faction turf, friends' houses); Journal
  shows where everyone is now and where they'll be later. Generated
  residents get lorebook-derived schedules (surname → family home,
  occupation → workplace, interests → hangout).
- **NPC autonomy.** Co-located NPCs hold ambient conversations (max 2 per
  tick; witnessed ones are noted in their memories), NPC↔NPC bonds
  evolve, NPCs dig trash/litter, shelter in storms, and gather for event
  stages. Full behavior runs near the player; off-screen NPCs resolve at
  schedule level.
- Saves: every v0.1.5 field is additive with a default — v0.1.4 saves load
  and carry over unchanged.

### v0.1.4 (versionCode 5)
- **Character redesign — signature pass** (per Lucky: reinterpret freely,
  just be recognizable). Every one of the 32 cast members now carries an
  unmistakable silhouette, hat shape, hair, resting face and/or prop, so
  the cast reads at a glance WITHOUT name tags. Proportions stay
  calibrated to the blank New Kid reference (giant head, no neck, stub
  body); all art still newly drawn in code, no copied/traced assets.
- **Distinct hats, no more shared generics**: Cartman's teal pomhat with
  yellow band + puff, Stan's blue/red version of the same family but with
  red mittens, Kyle's squared green ushanka with long earflaps, Craig's
  blue chullo with hanging strings and yellow puff, Wendy's wider pink
  beret.
- **Signature silhouettes & props**: Cartman drawn 1.3× wide with double
  chin and smug smirk; Mackey's giant head (1.3× head, narrow body);
  Kenny's parka gains drawstrings; Timmy's wheelchair wheels enlarged;
  Jimmy's crutches now pair with a braces grin; Tweek vibrates — wilder
  spikes, jitter pupils, zigzag mouth, twitch marks, coffee cup in hand.
- **Hair & faces**: Butters' blond swirl + worried mouth, Bebe's curly
  mane, goth kids split three ways (Michael's diagonal swoop, Pete's
  black fringe with red-dyed tips, Henrietta's black bob + black
  lipstick), Craig's deadpan half-lids, Wendy's lashes, Sheila's glasses,
  Tolkien's gold chain, Clyde's jacket stripe, Jimbo's mustache + hunting
  vest, Jesus' red sash + halo, Towelie's bloodshot heavy-lidded eyes.
- **Crowd layout**: scenes now arrange characters in up to three
  staggered class-photo rows, sized to fit the location's population, so
  a full room reads as individuals instead of one overlapping blob.
  Name tags show on the front row; everyone stays tappable.
- Lorebook, dialogue, gameplay and saves untouched from v0.1.3.

### v0.1.3 (versionCode 4)
- **Lorebook integrated** — Lucky's 3,322-entry South Park lorebook
  (2,225 characters, 752 locations, 333 episodes, 8 movies/specials,
  4 games; all prose paraphrased from southpark.wiki.gg) ships as
  `assets/lorebook.json` (trimmed by `tools/gen_lorebook.py`, which also
  generates the verified `LoreAliases.kt` match tables) and is parsed at
  runtime by the pure-Kotlin `LoreBook` (JVM-tested: 3,322 entries,
  ~224 ms parse, 32/32 cast + 26/26 locations matched).
- **In play**: LLM dialogue prompts now carry a LOREBOOK canon-facts block
  for whoever's talking + where they are (mock brain unchanged); Journal
  WHO cards show a lore excerpt per character; 👀 Look appends a lorebook
  footnote for the current location.
- **New 📖 Lorebook screen** (Game menu + Journal button): browse and
  keyword-search all 3,322 entries by category; default view leads with
  the town's cast and places.

### v0.1.2 (versionCode 3)
- **Avatar proportions rebuilt 1:1 with the New Kid model**: giant circle
  head (~66% of height), no neck, big white oval eyes touching at center
  with dot pupils, eyes low-mid face, tiny stub body, nub arms, mitten
  hands. Verified against the New Kid reference frame side-by-side.
- **Character creator default = blank New Kid** (bald, white briefs,
  barefoot). Customize skin/hair/hat/top/bottom/accessory from that base.
- **Full cast: 32 characters** (was 5). New: Wendy, Craig, Tweek, Clyde,
  Jimmy, Timmy, Tolkien, Bebe, Michael/Pete/Henrietta (goth kids), Randy,
  Sharon, Gerald, Sheila, Liane, Stuart, Carol, Stephen, Linda, Garrison,
  Mackey, Chef, Jesus, Jimbo, Father Maxi, Towelie. Each has a lore-grounded
  persona, wiki-studied code-drawn look (all in the one New Kid art style),
  home location, 7-block daily agenda, line bank, 12 daily topics, response
  bank, friend greetings, sleepy lines.
- **In-game clock**: unsynced from device time — 15-min ticks from 8 AM,
  advances on play/wait; day/night + seasons follow game date (starts
  2026-10-01). Real-time sync kept behind a Settings toggle (default OFF).
- **Lore pass** (southpark.wiki.gg as text reference, original writing):
  deeper personas + voice direction, 12 new collectibles (Lorde's wig,
  Mr. Hat, Tegridy nugget, Shake Weight...), 2 new holidays (St. Patrick's
  Day, Black Friday), location blurbs rewritten with show history.
- Upgrading players keep their save; new NPCs merge in on load.
- Verified: kotlinc zero errors, `aapt2 dump badging` (3 / 0.1.2),
  `apksigner verify` OK, JVM engine tests all pass (32-agent cast,
  agendas, content maps, holidays, seasons, dialogue).
