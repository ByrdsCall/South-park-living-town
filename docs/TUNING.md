# Tuning — South Park: Living Town

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

