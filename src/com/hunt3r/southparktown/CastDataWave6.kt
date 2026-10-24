package com.hunt3r.southparktown

// ============================================================
// CastDataWave6 — the wave-6 cast data (v0.11.0d), split out
// of CastData.kt at this wave: five waves of top-level data had
// filled CastData.kt's static initializer to the JVM's 64KB
// method limit, so the audit's twenty-eight live here, in the
// same package, merged by GameData exactly as waves 1-5 are.
// ============================================================

// ===========================================================
// v0.11.0d — CAST EXPANSION WAVE 6: THE EVIDENCE AUDIT.
// This wave was not picked; it was PROVEN. Every lorebook
// character entry not already in the cast (2,155 of them)
// was re-read against the wave standard — a standalone entry
// with identity, personality, relationships and episode
// grounding enough to author a whole person honestly — and
// twenty-eight people cleared it. Ten kids: Pip, the
// early-seasons regular the town buried and never replaced;
// Dougie, Butters' best friend and Chaos's small general;
// Jason White, the boys' extended circle since the very
// first lunch; Terrance Mephesto, the geneticist's son and
// the boys' original rival (NOT the Canadian comedian —
// the duo was re-evaluated a fourth time and passed a
// fourth time: still one joint 186-char entry, still no
// standalone entry for either man); Jenny Simons of the
// list committee; the kindergarten trio who are Ike's whole
// world (Flora, who chose him; Filmore, who rivals him;
// Quaid, who backs the rival); Mimsy, who completes Nathan
// the way a henchman completes a scheme; and Tricia Tucker,
// who completes the Tuckers. Eighteen grown-ups: Mr. Slave,
// who completes Big Gay Al's household; Linda Black, who
// completes the Blacks and takes her seat at their table;
// the Biggles, the Turners, the Stevenses and the Stoleys
// (four more sets of parents standing behind cast kids);
// Clark Malkinson; Maggie Yates; Mitch Murphy, who completes
// the police the way Janson never could City Hall; Bob
// Johnson, whose ideas the mayor has stolen since 1997;
// Tom Pusslicker, the anchor of the town's first decade;
// Dr. Doctor, the town's doctor of two decades; Diane
// Choksondik, the fourth grade's teacher of seasons 4-6
// (Chef's precedent governs her canonical death, as it
// governed his); and Strong Woman, the school's
// vice-principal, a chair no cast member held. The audit's
// near-misses are pinned in the suite against the lorebook
// file itself, so the audit stays live: change the data
// and the pins fail, and the town re-audits.
// ===========================================================

val WAVE6_ORDER: List<String> = listOf("Pip", "Dougie", "Jason",
    "Terrance Mephesto", "Jenny", "Flora", "Filmore", "Quaid",
    "Mimsy", "Tricia", "Mr. Slave", "Linda Black", "Harriet",
    "Mr. Biggle", "Mr. Turner", "Nancy", "Mr. Stevens",
    "Mrs. Stevens", "Mr. Stoley", "Mrs. Stoley", "Clark",
    "Maggie", "Mitch", "Bob Johnson", "Pusslicker", "Dr. Doctor",
    "Choksondik", "Strong Woman")

val WAVE6_HOUSES: Map<String, String> = mapOf(
    "Pip" to "Elementary",
    "Dougie" to "Elementary",
    "Jason" to "Elementary",
    "Terrance Mephesto" to "Elementary",
    "Jenny" to "Elementary",
    "Flora" to "Elementary",
    "Filmore" to "Elementary",
    "Quaid" to "Elementary",
    "Mimsy" to "Elementary",
    "Tricia" to "Elementary",
    "Mr. Slave" to "MainStreet",
    "Linda Black" to "TolkienHouse",
    "Harriet" to "MainStreet",
    "Mr. Biggle" to "CityHall",
    "Mr. Turner" to "MainStreet",
    "Nancy" to "CityHall",
    "Mr. Stevens" to "MainStreet",
    "Mrs. Stevens" to "CityHall",
    "Mr. Stoley" to "CityHall",
    "Mrs. Stoley" to "MainStreet",
    "Clark" to "MainStreet",
    "Maggie" to "MainStreet",
    "Mitch" to "CityHall",
    "Bob Johnson" to "CityHall",
    "Pusslicker" to "MainStreet",
    "Dr. Doctor" to "CityHall",
    "Choksondik" to "Elementary",
    "Strong Woman" to "Elementary"
)

val WAVE6_PROFILES: Map<String, KidProfile> = linkedMapOf(
    "Pip" to KidProfile("Pip",
        "Pip Pirrip: the English orphan of South Park Elementary, an exchange student from London who arrived with perfect manners and became the class's designated target anyway. Unfailingly polite, endlessly patient, and quietly the bravest kid in the room — he survived being replaced by Dougie, being set on fire by enthusiasm, and a full Dickens reenactment with his sandwich etiquette intact. The boys called him French for years. He is not French. He has stopped correcting them at full volume and started correcting them at tea volume, which is somehow worse for them. Keywords: the English kid, orphan, manners as armor, not French, the sandwich standard, unbothered.",
        "#8a6a3a",
        mapOf(
            "08:00" to AgendaBlock("BusStop", "Waiting for the bus with impeccable posture and a packed lunch in a proper tin"),
            "09:00" to AgendaBlock("Elementary", "In class, raising his hand with the full arm, every time, forever"),
            "12:00" to AgendaBlock("Elementary", "Lunch: sandwiches cut correctly. This is not negotiable and never was"),
            "15:00" to AgendaBlock("Elementary", "Reading Dickens by the fence. The book is bigger than the bench budget"),
            "16:30" to AgendaBlock("KidsPark", "Kicking a ball with great enthusiasm and English technique"),
            "18:00" to AgendaBlock("MainStreet", "Walking home politely, greeting every adult by title"),
            "21:00" to AgendaBlock("MainStreet", "Evening tea and letters home that begin 'Dear Whoever Is Reading These Now'"))),
    "Dougie" to KidProfile("Dougie",
        "Dougie O'Connell: a second-grader with a lisp, a bowl cut, and the most important friendship in the lower school — he is Butters Stotch's best friend, the one person who never treats kindness like a weakness. As General Disarray he serves as Professor Chaos's loyal second, a henchman of total commitment and occasional snacks. Small, earnest, and perpetually one rung below the main action, Dougie has the rarest gift in town: he is exactly who he appears to be, in both uniforms. Keywords: second grade, Butters' best friend, the General, loyal second, lisp and all, snacks are strategy.",
        "#3a7ca5",
        mapOf(
            "08:00" to AgendaBlock("BusStop", "Waiting with Butters, holding both their spots in line like a tiny general"),
            "09:00" to AgendaBlock("Elementary", "Second-grade class: letters, numbers, and the big feelings curriculum"),
            "12:00" to AgendaBlock("Elementary", "Lunch at the little table, trading snacks with military fairness"),
            "15:00" to AgendaBlock("KidsPark", "Playing generals at the park. The army is two kids and a stick"),
            "16:30" to AgendaBlock("ButtersHouse", "At Butters' house, plotting nothing worse than a lemonade empire"),
            "18:00" to AgendaBlock("MainStreet", "Walking home at second-grade pace, narrating the whole route"),
            "21:00" to AgendaBlock("Elementary", "Homework at the kitchen table, tongue out in concentration"))),
    "Jason" to KidProfile("Jason",
        "Jason White: a pupil of South Park Elementary since the very first lunch of the very first year, part of the boys' extended circle and Craig's Gang's orbit, renowned — his word, and the record backs him — for his love of sports. He has seldom held the floor and never needed to: Jason is the connective tissue of the fourth grade, the kid every group photo has in the middle row. The Whites took a reputational knock in the Garrison years and Jason absorbed his share of cafeteria weather over it, which he answered the only way he knows: by showing up, every day, cleats on. Keywords: extended circle, Craig's Gang orbit, sports first, middle row of every photo, showing up is the whole platform.",
        "#2a4a8a",
        mapOf(
            "08:00" to AgendaBlock("BusStop", "At the bus stop with a ball under one arm, whatever the season"),
            "09:00" to AgendaBlock("Elementary", "In class, counting the minutes to recess with a stopwatch brain"),
            "12:00" to AgendaBlock("Elementary", "Lunch with Craig's table, arguing the weekend's calls"),
            "15:00" to AgendaBlock("Elementary", "Recess league: Jason is commissioner, referee, and half the roster's ride home"),
            "16:30" to AgendaBlock("KidsPark", "Practice at the park. Whatever sport it is, Jason brought the ball"),
            "18:00" to AgendaBlock("MainStreet", "Walking home, dribbling something, anything, the whole way"),
            "21:00" to AgendaBlock("MainStreet", "Homework with the game on, both at low volume, both non-negotiable"))),
    "Terrance Mephesto" to KidProfile("Terrance Mephesto",
        "Terrance Mephesto: a South Park Elementary student and the son of Dr. Alphonse Mephesto of the Genetic Engineering Ranch — and not, whatever the new kid has heard, a Canadian comedian. His only speaking campaign on record was a masterpiece: he led Bill and Fosse against the boys' science project with a rival scheme, a cloned Stan, and a showman's instinct for the reveal. He has been quiet since, the way a chess clock is quiet. The ranch made him competitive the way other families make kids polite: thoroughly, and on purpose. Keywords: the geneticist's son, the original rival, NOT the comedian, schemes with a science budget, quiet like a chess clock.",
        "#5a6e46",
        mapOf(
            "08:00" to AgendaBlock("BusStop", "At the bus stop, reviewing a notebook nobody else is allowed to see"),
            "09:00" to AgendaBlock("Elementary", "In class, answering questions one beat before they're finished being asked"),
            "12:00" to AgendaBlock("Elementary", "Lunch at the end of the table, conducting small diplomacy with Bill and Fosse"),
            "15:00" to AgendaBlock("Elementary", "Science wing: checking on projects that are technically also rivals"),
            "16:30" to AgendaBlock("StarksPond", "At the pond, sketching the ranch's next generation of mistakes"),
            "18:00" to AgendaBlock("MainStreet", "Riding home to the ranch, already revising the day's results"),
            "21:00" to AgendaBlock("StarksPond", "Lab hours with his father: four asses of reading, one of reflection"))),
    "Jenny" to KidProfile("Jenny",
        "Jenny Simons: a fourth-grade girl with a parliamentarian's soul — co-chair, with Wendy, of the committee that maintains the girls' list of who is cutest, and a veteran of the list's one great counterfeiting scandal, which she helped run and then helped expose, in that order, for the integrity of the institution. Jenny treats schoolgirl politics the way Alexis Testaburger treats zoning: as load-bearing. She is the circle's institutional memory and its quietest power: every list in this school has her handwriting somewhere in its margins. Keywords: the list committee, co-chair, institutional memory, scandal survivor, handwriting in the margins.",
        "#c25a7a",
        mapOf(
            "08:00" to AgendaBlock("BusStop", "At the bus stop with a folder. The folder has tabs. The tabs have tabs"),
            "09:00" to AgendaBlock("Elementary", "In class, taking notes that would survive a subpoena"),
            "12:00" to AgendaBlock("Elementary", "Lunch: committee business at the girls' table, minutes kept"),
            "15:00" to AgendaBlock("Elementary", "Corridor diplomacy: two disputes settled, one list updated"),
            "16:30" to AgendaBlock("Mall", "At the mall with the circle, agenda item four of six"),
            "18:00" to AgendaBlock("MainStreet", "Walking home, updating the folder with the day's amendments"),
            "21:00" to AgendaBlock("Elementary", "Homework, then the archive: every list since third grade, in order"))),
    "Flora" to KidProfile("Flora",
        "Flora Larsen: a kindergartner, Millie's little sister, and — though she is too young to know the weight of it — the most powerful voter in South Park Elementary history. Faced with the class election between Ike Broflovski and Filmore Anderson, Flora couldn't decide, thought about it with her whole chest, and ultimately chose Ike, settling the school's smallest democracy. She is kind the way weather is kind: evenly, and to everyone in range. Keywords: kindergartner, the deciding vote, Millie's sister, kind like weather, democracy's smallest swing voter.",
        "#7ab648",
        mapOf(
            "08:00" to AgendaBlock("BusStop", "At the bus stop holding her sister's sleeve and the morning's whole mood"),
            "09:00" to AgendaBlock("Elementary", "Kindergarten: letters, naps, and the serious business of sharing"),
            "12:00" to AgendaBlock("Elementary", "Lunch: trading with total fairness. Flora's table has never known war"),
            "15:00" to AgendaBlock("Elementary", "Recess diplomacy: two disputes settled by being nice near them"),
            "16:30" to AgendaBlock("KidsPark", "At the park with the little kids, queen of the small slide by acclamation"),
            "18:00" to AgendaBlock("MainStreet", "Walking home with Millie, reporting the day in full sentences"),
            "21:00" to AgendaBlock("MainStreet", "Bedtime stories, then the sleep of the just and the well-voted"))),
    "Filmore" to KidProfile("Filmore",
        "Filmore Anderson: a kindergartner and Ike Broflovski's supposed rival — the word is his father's, the rivalry is real, and the election that was supposed to crown him went to Ike on Flora Larsen's deciding vote, a result Filmore has contested ever since with the stamina of a much larger politician. He has a best friend (Quaid), a platform (snacks, longer recess, justice), and a grievance with a filing system. Underneath the campaign is a good kid who shares his crackers; on top of it is a machine. Keywords: kindergartner, Ike's rival, the contested election, a platform of snacks, grievance with a filing system.",
        "#b5542a",
        mapOf(
            "08:00" to AgendaBlock("BusStop", "At the bus stop, campaigning. It is always a campaign day"),
            "09:00" to AgendaBlock("Elementary", "Kindergarten: learning letters and the names of his future constituents"),
            "12:00" to AgendaBlock("Elementary", "Lunch: working the room. The room is six kids and a teacher"),
            "15:00" to AgendaBlock("Elementary", "Recess rally at the fence: Quaid holds the sign, Filmore holds forth"),
            "16:30" to AgendaBlock("KidsPark", "At the park, holding a recount of this morning's kickball result"),
            "18:00" to AgendaBlock("MainStreet", "Walking home with Quaid, drafting tomorrow's remarks"),
            "21:00" to AgendaBlock("MainStreet", "Speeches to the bedroom wall. The wall's support is soft but growing"))),
    "Quaid" to KidProfile("Quaid",
        "Quaid: a kindergartner, Filmore Anderson's best friend, and — in the words of the kindergarten record — a rival of Ike Broflovski, which in practice means he is the loyal second of the losing campaign and proud of it. Quaid holds the sign, carries the crackers, and believes in Filmore the way second-graders believe in Santa: completely, and with evidence he will happily list. Every great small politician needs a Quaid; the Broflovski administration has no answer for him, because there is no answer for a best friend. Keywords: kindergartner, Filmore's best friend, the loyal second, sign-holder, believer with a list of evidence.",
        "#4a6ea5",
        mapOf(
            "08:00" to AgendaBlock("BusStop", "At the bus stop beside Filmore, sign ready, snacks ready"),
            "09:00" to AgendaBlock("Elementary", "Kindergarten: letters, and loyally repeating Filmore's answers half a beat late"),
            "12:00" to AgendaBlock("Elementary", "Lunch at Filmore's table, distributing crackers like campaign funds"),
            "15:00" to AgendaBlock("Elementary", "Recess: holding the sign at the rally, both arms, no complaints"),
            "16:30" to AgendaBlock("KidsPark", "At the park, seconding every motion Filmore makes, including the rematch"),
            "18:00" to AgendaBlock("MainStreet", "Walking home with Filmore, carrying the sign like a flag after a parade"),
            "21:00" to AgendaBlock("MainStreet", "Dreaming the campaign's dreams, in the second bunk of the imagination"))),
    "Mimsy" to KidProfile("Mimsy",
        "Mimsy: Nathan's henchman — the muscle, the loyalty, and, on the evidence of three campaigns at Nathan's side, the heart of the operation. A big kid with a bigger devotion, Mimsy follows orders with total commitment and occasional clarifying questions, and his devotion to Nathan is the one scheme in the building that has never once failed. Nathan treats him the way villains treat henchmen in the old stories, which is to say badly and constantly; Mimsy files it under weather. Keywords: the henchman, Nathan's second, total commitment, clarifying questions, the scheme that never fails is loyalty.",
        "#5f7355",
        mapOf(
            "08:00" to AgendaBlock("BusStop", "At the bus stop, standing where Nathan told him to stand, which is beside Nathan"),
            "09:00" to AgendaBlock("Elementary", "In class, doing the work at henchman pace: steady, thorough, unhurried"),
            "12:00" to AgendaBlock("Elementary", "Lunch at Nathan's table, guarding the operation's flank (the tray side)"),
            "15:00" to AgendaBlock("Elementary", "Afternoon operations: carrying things, holding doors, menacing by existing"),
            "16:30" to AgendaBlock("KidsPark", "At the park, running the perimeter while Nathan runs the plan"),
            "18:00" to AgendaBlock("MainStreet", "Walking home, reporting the day to Nathan in full, including the parts Nathan saw"),
            "21:00" to AgendaBlock("MainStreet", "Evening: sharpening nothing, polishing loyalty, early to bed for dawn duties"))),
    "Tricia" to KidProfile("Tricia",
        "Tricia Tucker: Craig Tucker's younger sister, daughter of Thomas and Laura, and the Tucker family's youngest and least flappable citizen. Where Craig communicates in monotone and the occasional finger, Tricia communicates in complete sentences and devastating little-sister timing. She has watched her brother's whole career — the guinea pig, the gangs, the boyfriend — from the best seat in the house, and her review is a shrug that contains multitudes. The Tuckers are complete now: the finger, the clipboard energy of her mother, her father's calm, and the small witness who saw it all. Keywords: Craig's little sister, the Tuckers' youngest, devastating timing, the shrug that contains multitudes, the small witness.",
        "#a55a8a",
        mapOf(
            "08:00" to AgendaBlock("BusStop", "At the bus stop two steps behind Craig, which is the family formation"),
            "09:00" to AgendaBlock("Elementary", "In her own class, being the Tucker who answers questions out loud"),
            "12:00" to AgendaBlock("Elementary", "Lunch with her class, trading intel on everyone's older siblings"),
            "15:00" to AgendaBlock("Elementary", "Recess: watching Craig's gang's diplomacy with a critic's eye"),
            "16:30" to AgendaBlock("KidsPark", "At the park, holding the guinea-pig stories over Craig like weather"),
            "18:00" to AgendaBlock("MainStreet", "Walking home with Craig, debriefing him in sentences he answers in syllables"),
            "21:00" to AgendaBlock("MainStreet", "Homework, then the family couch: Tricia center, remote hers by ancient law"))),
    "Mr. Slave" to KidProfile("Mr. Slave",
        "Mr. Slave: Big Gay Al's husband, and before that the most committed classroom assistant South Park Elementary ever failed to deserve — Garrison's ex in every sense the school board will put in writing. Flamboyant, devoted, and absolutely unshockable, he brings to the sanctuary household a bass register, a leather wardrobe, and a heart the size of the goat paddock. The marriage is the town's sturdiest: Al brings the sparkle, Mr. Slave brings the bass line, and the strays bring themselves. Keywords: Al's husband, the bass line of the sanctuary, unshockable, Garrison's ex (the file is thick), devoted at full volume.",
        "#3a3a44",
        mapOf(
            "08:00" to AgendaBlock("MainStreet", "Morning at the sanctuary: feed rounds with Al, every animal greeted by name"),
            "09:00" to AgendaBlock("MainStreet", "Sanctuary chores at full commitment — the goats respect a man who commits"),
            "12:00" to AgendaBlock("TweekBros", "Coffee with Al, the town's sturdiest marriage on public display"),
            "15:00" to AgendaBlock("KidsPark", "Walking the sanctuary's graduates at the park, leash in each hand"),
            "16:30" to AgendaBlock("MainStreet", "Errands: feed, ribbons, and whatever Al's pageant requires this week"),
            "18:00" to AgendaBlock("MainStreet", "Dinner for two plus strays: Al cooks, Mr. Slave sets a table like a stage"),
            "21:00" to AgendaBlock("MainStreet", "Evening porch with Al: the bass line and the sparkle, winding down"))),
    "Linda Black" to KidProfile("Linda Black",
        "Linda Black: Tolkien's mother and Steve's wife — the Black family's warmth department. Where Steve brings the chef's precision and the neighborhood-watch vigilance, Linda brings the welcome: she is the parent other parents call first, the house every kid on the street has eaten at, and the calm center of the richest household in South Park's least rich town. She has opinions about everything her son does and expresses them at exactly the right volume, which is the whole trick and she knows it. Keywords: Tolkien's mom, the warmth department, the welcome house, opinions at the right volume, calm center.",
        "#7a4a6e",
        mapOf(
            "08:00" to AgendaBlock("TolkienHouse", "Morning at the Black house: breakfast at a table that could seat the street"),
            "09:00" to AgendaBlock("MainStreet", "Errands on Main Street, greeting the whole town by first name"),
            "12:00" to AgendaBlock("TweekBros", "Coffee with the other mothers: intelligence, gossip, and pie, in that order"),
            "15:00" to AgendaBlock("Mall", "At the mall, shopping with a list and a philosophy"),
            "16:30" to AgendaBlock("TolkienHouse", "Home for the afternoon: the house resets to welcome in under an hour"),
            "18:00" to AgendaBlock("TolkienHouse", "Family dinner: Steve cooks, Linda hosts, Tolkien files his reports"),
            "21:00" to AgendaBlock("TolkienHouse", "Evening on the good couch, the day reviewed at the right volume"))),
    "Harriet" to KidProfile("Harriet",
        "Harriet Biggle: Henrietta and Bradley's mother, a woman navigating the extraordinary project of raising a goth poet and a changeling-energy second child on a cul-de-sac budget. Her entrances into Henrietta's room are legend: she arrives praising, is berated at operatic length, and leaves undefeated, because Harriet has understood the house's central truth — the poetry is loud because the feelings are, and the feelings include, somewhere under the eyeliner, her. Keywords: the Biggles' mother, berated at operatic length, undefeated, praise-first entrances, the feelings under the eyeliner.",
        "#4a3a52",
        mapOf(
            "08:00" to AgendaBlock("MainStreet", "Morning rounds: two kids launched, one poem survived, coffee earned"),
            "09:00" to AgendaBlock("MainStreet", "Errands and the PTA circuit, praising as she goes"),
            "12:00" to AgendaBlock("TweekBros", "Coffee with the parents' table: Biggle report — the room was entered, the poem was praised"),
            "15:00" to AgendaBlock("KidsPark", "At the park with Bradley, refereeing a changeling's afternoon"),
            "16:30" to AgendaBlock("MainStreet", "Groceries for a house that eats like a small theater company"),
            "18:00" to AgendaBlock("MainStreet", "Family dinner: Henrietta attends in body. Harriet counts it. It counts"),
            "21:00" to AgendaBlock("MainStreet", "Evening: knocking once on a bedroom door, praising through it, retreating in good order"))),
    "Mr. Biggle" to KidProfile("Mr. Biggle",
        "Mr. Biggle: Henrietta's biological father, Bradley's adoptive father, and Harriet's husband — the Biggle family's logistics department. It was the Biggles, together, who shipped Henrietta off to the camp for troubled kids, a decision the family processes to this day at full volume (hers) and reasonable volume (his). He is the quieter Biggle, which is a low bar the whole street appreciates: he fixes the house, funds the eyeliner, and believes — on evidence nobody else can see — that the poems will someday be dedicated to them. Keywords: the Biggles' dad, logistics department, adoptive and all-in, funder of eyeliner, reasonable volume.",
        "#5a5148",
        mapOf(
            "08:00" to AgendaBlock("CityHall", "Morning at the civic end of town: the commute, the paper, the lists"),
            "09:00" to AgendaBlock("CityHall", "Work hours: the town's business, handled at reasonable volume"),
            "12:00" to AgendaBlock("TweekBros", "Coffee with the dads' table: Biggle report — camp is not mentioned. Growth"),
            "15:00" to AgendaBlock("MainStreet", "Errands: hardware, groceries, one (1) peace offering of black nail polish"),
            "16:30" to AgendaBlock("MainStreet", "Home repairs: the house is old, the daughter is goth, both are maintained with love"),
            "18:00" to AgendaBlock("MainStreet", "Family dinner: he serves, Harriet hosts, Henrietta reviews"),
            "21:00" to AgendaBlock("MainStreet", "Evening at home: the day filed, the logistics of love, done"))),
    "Mr. Turner" to KidProfile("Mr. Turner",
        "Thomas Turner — Mr. Turner: Heidi's father, a very muscular and stereotypically over-protective dad who keeps his daughter out of trouble the way a vault keeps gold: by design, by mass, and by reputation. Three different versions of him have reportedly appeared around town over the years — the record is unclear, the arms are consistent. Underneath the perimeter is a soft center the size of a second vault: nobody in South Park has ever out-dadded him at a school gate, and Heidi, who has tested the security professionally, wouldn't trade it. Keywords: Heidi's dad, over-protective by design, the arms are consistent, vault with a soft center, school-gate champion.",
        "#74563a",
        mapOf(
            "08:00" to AgendaBlock("MainStreet", "Morning perimeter check: the house, the street, the school's general direction"),
            "09:00" to AgendaBlock("MainStreet", "Work and errands, nodding at other dads like allied nations"),
            "12:00" to AgendaBlock("TweekBros", "Coffee with the dads' table, seated facing the door. Always facing the door"),
            "15:00" to AgendaBlock("KidsPark", "At the park, supervising Heidi's afternoon from a respectful, enormous distance"),
            "16:30" to AgendaBlock("MainStreet", "Groceries: protein, more protein, and whatever Nancy put on the list"),
            "18:00" to AgendaBlock("MainStreet", "Family dinner: grace is short, portions are not, Heidi is safe and fed"),
            "21:00" to AgendaBlock("MainStreet", "Evening: locks checked, daughter debriefed, perimeter at peace"))),
    "Nancy" to KidProfile("Nancy",
        "Nancy Turner: Heidi's mother — first seen hosting the slumber party in 'Marjorine,' which tells you everything about her courage. She is the Turner family's diplomacy corps: where Thomas is the perimeter, Nancy is the open gate, the one who talks the perimeter down from DEFCON Dad and keeps the household's welcome warmer than its security. Her name reached the wider world through a game about a stick, which she finds hilarious and Thomas finds 'a breach.' Keywords: Heidi's mom, the diplomacy corps, slumber-party host (decorated), the open gate, DEFCON Dad negotiator.",
        "#8a5a74",
        mapOf(
            "08:00" to AgendaBlock("CityHall", "Morning: school-run debriefs and the town's business at walking pace"),
            "09:00" to AgendaBlock("MainStreet", "Errands on Main Street, where every shopkeeper knows her by name"),
            "12:00" to AgendaBlock("TweekBros", "Coffee with the mothers' table: Turner report — the perimeter held, the party was lovely"),
            "15:00" to AgendaBlock("Mall", "At the mall with Heidi, on a mission Thomas has been briefed about twice"),
            "16:30" to AgendaBlock("MainStreet", "Home: the gate opens, the kettle goes on, the day comes in"),
            "18:00" to AgendaBlock("CityHall", "Family dinner hour: Nancy hosts, Thomas guards the gravy"),
            "21:00" to AgendaBlock("MainStreet", "Evening: talking the perimeter down to a movie and a blanket"))),
    "Mr. Stevens" to KidProfile("Mr. Stevens",
        "Mr. Stevens: Bebe's father, introduced during the video-playset affair and a background fixture of the town's parent class ever since. He is the quieter Stevens — Bebe got the stage presence from her mother and the timing from both of them — a working dad whose main recorded positions are presence at the school gate and a household run with more glamour than his half of it suggests. In a town of shouting fathers he is a moderate instrument: he shows up, he signs the forms, he survives the fashion eras. Keywords: Bebe's dad, the quieter Stevens, school-gate presence, forms signed, fashion-era survivor.",
        "#6e7f5a",
        mapOf(
            "08:00" to AgendaBlock("MainStreet", "Morning: school gate duty, forms signed, daughter launched"),
            "09:00" to AgendaBlock("MainStreet", "Work hours: the town's ordinary business, done ordinarily well"),
            "12:00" to AgendaBlock("TweekBros", "Coffee with the dads' table, listening more than the minutes will show"),
            "15:00" to AgendaBlock("KidsPark", "At the park's edge, watching Bebe's circle orbit at safe dad distance"),
            "16:30" to AgendaBlock("MainStreet", "Errands: the list his wife wrote, executed to the letter and one extra"),
            "18:00" to AgendaBlock("MainStreet", "Family dinner: Bebe debriefs the day; her father takes it in like weather"),
            "21:00" to AgendaBlock("MainStreet", "Evening: the quiet chair, the paper, the house at glamour-rest"))),
    "Mrs. Stevens" to KidProfile("Mrs. Stevens",
        "Mrs. Stevens: Bebe's mother, a Stevens since before the town had a mall worth the name — she was there in the boobs-destroy-society era and she is there now, the glamour department of a household whose daughter inherited the whole enterprise. Where Bebe runs the fourth grade's social weather, her mother runs the grown-up version with better shoes: the Stevens women do not enter rooms, they arrive in them. Keywords: Bebe's mom, the glamour department, arrivals not entrances, better shoes, the enterprise Bebe inherited.",
        "#a56a8a",
        mapOf(
            "08:00" to AgendaBlock("CityHall", "Morning: arriving at the day's first errand like it invited her"),
            "09:00" to AgendaBlock("MainStreet", "Main Street business: the Stevens standard, applied to retail"),
            "12:00" to AgendaBlock("TweekBros", "Coffee with the mothers' table: Bebe report — the circle is hers, the shoes are new"),
            "15:00" to AgendaBlock("Mall", "At the mall in her element, consulting on the season with staff who take notes"),
            "16:30" to AgendaBlock("MainStreet", "Evening round on Main Street: the Stevens standard, displayed at golden hour"),
            "18:00" to AgendaBlock("MainStreet", "Family dinner: the Stevens table, set like a small event"),
            "21:00" to AgendaBlock("MainStreet", "Evening: the day reviewed, the daughter advised, the standard kept"))),
    "Mr. Stoley" to KidProfile("Mr. Stoley",
        "Mr. Stoley: Kevin Stoley's father — formerly Mr. Brooks, a surname the family corrected the way they correct everything: thoroughly, and with a plan. He and his wife are the authors of the family's most famous production, the hiring of a 'future self' to frighten young Kevin off a bad road — a scheme of love, theater, and questionable casting that Kevin has since turned into his entire personality's foundation. He is a dad who believes in intervention the way other dads believe in curfews: early, elaborate, and with props. Keywords: Kevin's dad, the future-self scheme (co-author), intervention with props, formerly Brooks, love as theater.",
        "#4f6e7a",
        mapOf(
            "08:00" to AgendaBlock("CityHall", "Morning: the family's day, planned at a level most militaries would envy"),
            "09:00" to AgendaBlock("CityHall", "Work hours at the civic end of town, schemes filed under 'parenting'"),
            "12:00" to AgendaBlock("TweekBros", "Coffee with the dads' table: Stoley report — Kevin is thriving. The scheme worked. Probably"),
            "15:00" to AgendaBlock("MainStreet", "Errands, including one prop for a future intervention he won't discuss"),
            "16:30" to AgendaBlock("MainStreet", "Home: reviewing Kevin's day like a director reviewing dailies"),
            "18:00" to AgendaBlock("CityHall", "Family dinner: Kevin performs; his parents applaud at scheme strength"),
            "21:00" to AgendaBlock("MainStreet", "Evening: drafting the next decade of fatherhood, in pencil, with love"))),
    "Mrs. Stoley" to KidProfile("Mrs. Stoley",
        "Mrs. Stoley: Kevin Stoley's mother — formerly Mrs. Brooks — and the co-author, casting director, and emotional core of the famous future-self production. Where her husband drafts the schemes, she makes them land: she is the Stoley who remembers that the point of frightening a boy straight is the boy, not the fright. Kevin's steadiness under the fourth grade's weather is her workmanship: a kid raised by loving theater grows up able to tell performance from truth, which in this town is a survival skill. Keywords: Kevin's mom, the scheme's emotional core, formerly Brooks, loving theater, workmanship you can see in the kid.",
        "#7a6ea0",
        mapOf(
            "08:00" to AgendaBlock("MainStreet", "Morning: Kevin launched with lunch, a note, and a mother's full production values"),
            "09:00" to AgendaBlock("MainStreet", "Errands on Main Street, greeting the town like a cast she's fond of"),
            "12:00" to AgendaBlock("TweekBros", "Coffee with the mothers' table: Stoley report — the boy is steady. The scheme is retired. Mostly"),
            "15:00" to AgendaBlock("Mall", "At the mall, shopping for the household and one small prop, just in case"),
            "16:30" to AgendaBlock("CityHall", "Civic errands: the family's paperwork, performed flawlessly"),
            "18:00" to AgendaBlock("MainStreet", "Family dinner: Kevin debriefs; his mother directs the conversation like a kind play"),
            "21:00" to AgendaBlock("MainStreet", "Evening: the good couch, the day reviewed with her husband like dailies"))),
    "Clark" to KidProfile("Clark",
        "Clark Malkinson: Scott Malkinson's father — the dad from 'Basic Cable,' where the Malkinson household's war over the streaming bill became the town's documentary of itself. He is a working dad of the cable-cutting generation: handy, stubborn, and fluent in the ancient language of 'we had three channels and we LIKED it.' Scott's lisp, diabetes, and dignity under the fourth grade's weather all have a source, and the source is a house where the boy is backed at full volume by a man who once fought a corporation over a remote. Keywords: Scott's dad, the Basic Cable war, three channels and we liked it, backing at full volume, handy and stubborn.",
        "#5a6e46",
        mapOf(
            "08:00" to AgendaBlock("MainStreet", "Morning: the house's technology, cursed at fondly, in order"),
            "09:00" to AgendaBlock("MainStreet", "Work hours: the town's cables, metaphorical and otherwise"),
            "12:00" to AgendaBlock("TweekBros", "Coffee with the dads' table: Malkinson report — the bill is down. The principle is up"),
            "15:00" to AgendaBlock("KidsPark", "At the park's edge, watching Scott's afternoon like a broadcast he paid for"),
            "16:30" to AgendaBlock("MainStreet", "Errands: hardware, groceries, and a remote battery replaced like a trophy"),
            "18:00" to AgendaBlock("MainStreet", "Family dinner: the TV stays off until plates are clear. House law, his law"),
            "21:00" to AgendaBlock("MainStreet", "Evening: one show, watched together, on purpose — the Basic Cable peace treaty"))),
    "Maggie" to KidProfile("Maggie",
        "Maggie Yates: the wife of Park County's head detective, Harrison Yates — the woman who backs his police work despite worrying about his safety, and who, in 'The Jeffersons,' talked him out of quitting the force, which makes her indirectly responsible for every case the department has closed since. She is the Yates household's entire emotional infrastructure: the worry is hers, the steadiness is hers, and the sergeant who storms around town being certain is powered, at home, by being believed in by Maggie. Keywords: Yates' wife, the force's deciding voice, worry and steadiness both hers, emotional infrastructure, the power behind the certainty.",
        "#6a7a9a",
        mapOf(
            "08:00" to AgendaBlock("MainStreet", "Morning: the sergeant launched with coffee and a lunch he will forget to eat"),
            "09:00" to AgendaBlock("MainStreet", "Errands on Main Street, where the town reports to her informally first"),
            "12:00" to AgendaBlock("TweekBros", "Coffee with the mothers' table: Yates report — he's certain again. It's a case again. We're proud again"),
            "15:00" to AgendaBlock("KidsPark", "At the park, reading, one ear on the town's frequency"),
            "16:30" to AgendaBlock("MainStreet", "Home: the kettle on for a detective's return, whenever the case allows"),
            "18:00" to AgendaBlock("MainStreet", "Family dinner: the case is debriefed; Maggie asks the question that cracks it, casually"),
            "21:00" to AgendaBlock("MainStreet", "Evening: the worry set down for the night, the steadiness left on"))),
    "Mitch" to KidProfile("Mitch",
        "Mitch Murphy: a Park County police detective and Sergeant Yates' aide-de-camp — the second half of the town's law, on the record since 'Lil' Crime Stoppers.' Where Yates is certainty at full volume, Mitch is the paperwork, the perimeter, and the calm voice that says 'sir' to chaos until chaos files itself. He is the department's institutional memory and its straight man: every theory Yates has ever announced has passed across Mitch's desk, and Mitch has stamped it, filed it, and quietly kept the town running underneath it. Keywords: detective, Yates' aide-de-camp, the calm half of the law, institutional memory, stamped and filed.",
        "#4a423c",
        mapOf(
            "08:00" to AgendaBlock("CityHall", "Morning briefing: the overnight reports, stamped in triplicate"),
            "09:00" to AgendaBlock("CityHall", "Desk hours: the department's memory, maintained one file at a time"),
            "12:00" to AgendaBlock("CityHall", "Lunch at the desk. The desk has seen things. The desk says nothing"),
            "15:00" to AgendaBlock("MainStreet", "Afternoon patrol with Yates: the sergeant announces, Mitch verifies"),
            "16:30" to AgendaBlock("StarksPond", "Pond-side check: the loitering report was, as ever, ducks"),
            "18:00" to AgendaBlock("MainStreet", "Evening rounds: the town tucked in, one street at a time"),
            "21:00" to AgendaBlock("CityHall", "Late paperwork: every theory of the day, filed under its outcome"))),
    "Bob Johnson" to KidProfile("Bob Johnson",
        "Bob Johnson: Mayor McDaniels' aide and a member of the City Council — on the town's staff since its second-ever day of business, which makes him the longest-serving idea man in South Park. The job has one hazard he has never escaped: the mayor steals his ideas and takes the credit, a tradition so old it has seniority. Bob responds the way he has since 1997: by having another idea. He is the town's proof that government runs on the people whose names aren't on the door. Keywords: the mayor's aide, council member, ideas stolen since 1997, another idea where that came from, the name not on the door.",
        "#54606e",
        mapOf(
            "08:00" to AgendaBlock("CityHall", "Morning: the mayor's schedule, the town's crises, and one new idea, his"),
            "09:00" to AgendaBlock("CityHall", "Council business: motions, minutes, and being right at low volume"),
            "12:00" to AgendaBlock("CityHall", "Lunch in the office: the idea notebook stays open, just in case"),
            "15:00" to AgendaBlock("CityHall", "Afternoon session: the mayor announces Bob's idea. Bob applauds. It's tradition"),
            "16:30" to AgendaBlock("MainStreet", "Errands on Main Street, taking the town's temperature like a good aide"),
            "18:00" to AgendaBlock("MainStreet", "Evening: home, the day survived, the notebook refilled"),
            "21:00" to AgendaBlock("CityHall", "Late minutes by lamplight: the town's record, kept honestly, signed by no one"))),
    "Pusslicker" to KidProfile("Pusslicker",
        "Tom Pusslicker: the face of Channel 4 News for the town's first ten seasons — the anchor who told South Park everything it knew about itself for a decade, in the suit, at the desk, with the voice. Anchors since have held the chair (a Thompson, another Tom), but Pusslicker is the one the town's memory keeps in the chair: the rest are described, even now, as 'the new one.' He has the anchor's whole toolkit — the gravity, the pause, the look to camera two — and a decade of the town's worst days filed behind his eyes. Keywords: Channel 4's first anchor, the voice of a decade, the pause before the headline, the one the chair remembers, camera two.",
        "#2e3a52",
        mapOf(
            "08:00" to AgendaBlock("MainStreet", "Morning rundown: the town's news, gathered at anchor pace"),
            "09:00" to AgendaBlock("CityHall", "At City Hall for the morning presser, front row, eyebrow loaded"),
            "12:00" to AgendaBlock("MainStreet", "Midday broadcast prep: the voice warmed, the tie straightened"),
            "15:00" to AgendaBlock("MainStreet", "Field hours: reporting from wherever the town is doing it today"),
            "16:30" to AgendaBlock("MainStreet", "Evening prep on Main Street: the lead story, gathered where it happened"),
            "18:00" to AgendaBlock("MainStreet", "The evening desk: South Park, told to itself, in order"),
            "21:00" to AgendaBlock("MainStreet", "Sign-off and the walk home: tomorrow's town, already gathering"))),
    "Dr. Doctor" to KidProfile("Dr. Doctor",
        "Dr. Doctor: the town's doctor — Hell's Pass Hospital's own, later of his own practice — the man who has delivered the bad news, the good news, and the news about the thing Randy did, for over twenty years. His name is his whole biography and he wears it without flinching: addressed a thousand ways by a thousand charts, he answers to the title twice. In a town that injures itself this creatively, the doctor is a load-bearing citizen, and Dr. Doctor has borne it with dry calm since the Clinton administration. Keywords: the town doctor, Hell's Pass, the title twice, dry calm, load-bearing since the nineties.",
        "#5a6e7a",
        mapOf(
            "08:00" to AgendaBlock("CityHall", "Morning rounds begin: the hospital's shadow falls on the civic end of town"),
            "09:00" to AgendaBlock("CityHall", "Clinic hours: the town's ailments, in order of honesty"),
            "12:00" to AgendaBlock("CityHall", "Lunch between patients: a sandwich eaten like a diagnosis — quickly, completely"),
            "15:00" to AgendaBlock("MainStreet", "House calls on Main Street: the town prefers its medicine local"),
            "16:30" to AgendaBlock("MainStreet", "Pharmacy run and chart review: twenty years of Randy, in a binder"),
            "18:00" to AgendaBlock("CityHall", "Evening clinic: the day's last fevers, settled"),
            "21:00" to AgendaBlock("CityHall", "On-call hours: the phone by the bed, the town by the phone"))),
    "Choksondik" to KidProfile("Choksondik",
        "Diane Choksondik: the fourth grade's teacher of seasons four through six — the woman who held Garrison's classroom between his eras and taught the boys through the succubus aftermath, the metrosexual audit, and her own legendary unit on the facts of life. She died in office, of embarrassment's opposite number, and the school put her name on the record the way it does: completely, and then not at all. She returns to the roster on Chef's precedent: the town's dead are only gone where the data says the town forgot them, and the fourth grade never forgot her. Keywords: the fourth grade's teacher (seasons 4-6), the name said carefully, in the line of succession, Chef's precedent, never forgotten by the fourth grade.",
        "#7a4a56",
        mapOf(
            "08:00" to AgendaBlock("Elementary", "Early prep: the day's lesson, written on the board in a hand that brooks no nonsense"),
            "09:00" to AgendaBlock("Elementary", "First period: the fourth grade, instructed at full seriousness"),
            "12:00" to AgendaBlock("Elementary", "Lunch duty: the cafeteria, supervised like a small nation"),
            "15:00" to AgendaBlock("Elementary", "Afternoon classes: the unit everyone remembers, taught without blushing"),
            "16:30" to AgendaBlock("Elementary", "Marking at the desk: red pen, fair hand, no favorites"),
            "18:00" to AgendaBlock("MainStreet", "Walking home past the whole graduating century of her students"),
            "21:00" to AgendaBlock("Elementary", "Evening prep: tomorrow's board work, tonight's quiet triumph"))),
    "Strong Woman" to KidProfile("Strong Woman",
        "Strong Woman: South Park Elementary's vice-principal — the administration's muscle in every sense, a role model of genuine strength in a school that runs on chaos. Where Principal Victoria governs by clipboard, Strong Woman governs by presence: she is the door the trouble stops at. The kids test every adult in the building exactly once, and the building's record shows her line is the one nobody tests twice. She believes a school should make kids stronger, not smaller, and runs her half of the office like a gym for character. Keywords: the vice-principal, the door trouble stops at, presence as policy, stronger not smaller, a gym for character.",
        "#3a6ea5",
        mapOf(
            "08:00" to AgendaBlock("Elementary", "Morning arrival: the building stands a little straighter when she walks in"),
            "09:00" to AgendaBlock("Elementary", "Office hours: discipline, encouragement, and absolutely no nonsense, in that order"),
            "12:00" to AgendaBlock("Elementary", "Cafeteria duty: the lunch line moves like it respects her. It does"),
            "15:00" to AgendaBlock("Elementary", "Afternoon rounds: every corridor checked, every kid seen"),
            "16:30" to AgendaBlock("KidsPark", "Laps at the park: the vice-principal trains where the kids can watch"),
            "18:00" to AgendaBlock("MainStreet", "Evening on Main Street: groceries carried like a warm-up set"),
            "21:00" to AgendaBlock("Elementary", "Tomorrow's plan, tonight: the school's strength, scheduled")))
)


// ------------- wave 6: collision line banks (4 + 5 = 9 each) -------------
val WAVE6_LINE_BANKS: Map<String, List<String>> = mapOf(
    "Pip" to listOf(
        "Oh! Hello, {other}. Pip Pirrip. Do mind the puddle; I've minded it for everyone all morning.",
        "Terribly sorry, {other}, I was miles away. London miles. They're longer and it rains more.",
        "You must be the new child, {other}. I'm the OLD new child. Seniority is a terrible burden. Tea?",
        "People call me French, {other}. I am ENGLISH. The distinction has ended friendships and one war."),
    "Dougie" to listOf(
        "Oh hi, {other}! I'm Dougie. I'm in thecond— second grade. Thith— this is my good shirt.",
        "Have you theen Butters, {other}? He's my best friend. I'm his general. It's a whole thing.",
        "Careful, {other}, I'm carrying out an operation. The operation is snack delivery. It's classified.",
        "You're the new kid, right, {other}? I was the new kid once. It gets better. Then it gets weird."),
    "Jason" to listOf(
        "Jason White, {other}. Whatever we're playing, I'm in. I brought a ball. I always bring a ball.",
        "You catch the game, {other}? No? Doesn't matter — tell me a score and I'll have an opinion by lunch.",
        "Craig's table's that way, {other}. I'm the welcoming committee. The committee is just me and a high-five.",
        "Extended circle, {other}. That's what I am. Every team needs one. Every photo too — middle row."),
    "Terrance Mephesto" to listOf(
        "Terrance Mephesto, {other}. No relation to the comedian. The ranch's Terrance. The distinction matters.",
        "You're standing in my light, {other}. Unlike some rivals, I need mine for actual science.",
        "My father cloned a classmate once, {other}. Family standards are... specific. Walk with me carefully.",
        "Bill. Fosse. Take note of {other}, gentlemen. The new variable has arrived in the experiment."),
    "Jenny" to listOf(
        "Jenny Simons, {other}. Co-chair of the list committee. Yes, THE list. No, you may not see it.",
        "Hold on, {other}, I'm minuting this conversation. Kidding. Mostly. The committee thanks you anyway.",
        "New kid, {other}? The circle has a file. It's a GOOD file. I maintain the files; I'd know.",
        "Everything in this school is ranked by someone, {other}. I'm just the one who keeps it honest."),
    "Flora" to listOf(
        "Hi, {other}! I'm Flora. I'm in kindergarten. I voted once and it counted DOUBLE. Nobody told me why.",
        "You're big, {other}. Are you a teacher? You don't look like a teacher. You look like a kid but longer.",
        "I couldn't decide, {other}, and then I DID decide, and that's the whole story of the election.",
        "Millie's my sister, {other}. She's in fourth grade. Fourth grade is where the lists live. I stay downstairs."),
    "Filmore" to listOf(
        "Filmore Anderson, {other}. You may know me from the election. The RE-counted election. Still counting.",
        "Ike won by ONE vote, {other}. Flora's vote. I have the charts. The charts have charts. This isn't over.",
        "My platform is snacks, {other}. Longer recess and JUSTICE. You can be in my cabinet. Quaid's chief of staff.",
        "Don't call me a sore loser, {other}. Call me a winner in appeals. There's a difference and it's documented."),
    "Quaid" to listOf(
        "I'm Quaid, {other}. I'm Filmore's best friend. He's going to be president of kindergarten. Retroactively.",
        "I hold the sign, {other}. It's an important job. Filmore says the sign is half the campaign. He's right.",
        "Filmore has charts, {other}. I carry the crackers. A campaign runs on both, we've proven it.",
        "You're not on Ike's side, are you, {other}? It's fine if you are. Filmore says rivals make us strong."),
    "Mimsy" to listOf(
        "I'm Mimsy, {other}. I'm Nathan's— I work with Nathan. He has the plans. I have the carrying.",
        "Duh— hi, {other}. Nathan says to tell strangers the operation isn't hiring. But you seem nice, so hi.",
        "Careful of the plan, {other}. There's always a plan. Nathan's plans have phases. I'm usually phase two.",
        "Nathan's my best friend, {other}. He doesn't say it, but the plans say it. I'm in ALL the plans."),
    "Tricia" to listOf(
        "Tricia Tucker, {other}. Craig's sister. Yes, THAT Craig. No, the finger isn't hereditary. Mostly.",
        "You've met my brother, {other}? Then you've met ten percent of him. The rest is guinea-pig loyalty.",
        "I'm the Tucker who talks, {other}. Mom says it's my gift. Craig says it's my curse. Both are right.",
        "Stripe lives with us, {other}. The guinea pig. Craig would simply perish. Don't tell him I said perish."),
    "Mr. Slave" to listOf(
        "Well, hello, {other}. Mr. Slave. Al's husband. Yes, the sanctuary. Yes, the goats. All of it, yes.",
        "Mind the strays, {other}. They adopt fast around here. So does Al. I speak from experience.",
        "You're the new kid, {other}? Al's been talking about a new arrival. He talks in exclamation points.",
        "Big day at the sanctuary, {other}. Every day is. That's what happens when you marry the weather."),
    "Linda Black" to listOf(
        "Linda Black, {other}. Tolkien's mother. Come in if you're coming in — the Black house feeds guests.",
        "Steve's in the kitchen, {other}, and Tolkien's wherever the news is. You've caught the calm one. Lucky you.",
        "Mind the welcome, {other}, it's load-bearing in this house. Sit. Eat. THEN we talk.",
        "You're the new kid, {other}? Tolkien mentioned you with whole sentences. In this family, that's a review."),
    "Harriet" to listOf(
        "Harriet Biggle, {other}. Henrietta's mother. If you hear poetry through a door, that's the house breathing.",
        "Come in, {other}. Mind the eyeliner budget — it has its own line in the spreadsheet. My husband weeps.",
        "Bradley's the small one, {other}, Henrietta's the thunder. I'm the roof. We manage.",
        "I praise first, {other}. It's my whole method. The berating is hers. We split the work like a marriage."),
    "Mr. Biggle" to listOf(
        "Mr. Biggle, {other}. Logistics. Harriet's the roof, I'm the foundation. Foundations don't get poems. Yet.",
        "Henrietta's my daughter, {other}, Bradley's my son. Both sentences cost the same and pay the same. Everything.",
        "If it's about the camp, {other}, the family has a position and the position is: we're processing. At volume.",
        "Black nail polish, {other}. I buy it by the case now. That's not defeat. That's procurement."),
    "Mr. Turner" to listOf(
        "Thomas Turner, {other}. Heidi's dad. State your business — sorry. Habit. Come in, kid, you're fine.",
        "The perimeter's friendly, {other}, I promise. It just LOOKS like a fortress. Nancy decorated. I fortified.",
        "Heidi talks about you, {other}. The report was good. Reports matter around here. Keep it that way.",
        "Three versions of me in this town, they say, {other}. All three agree: my daughter is off limits to trouble."),
    "Nancy" to listOf(
        "Nancy Turner, {other}. Heidi's mom. Ignore the perimeter — Thomas means well and so does the moat.",
        "Come in, {other}! I hosted a slumber party once that entered town legend. I can host a Tuesday.",
        "Heidi's told me about you, {other}. The good version. There was also a Thomas version. Yours is better.",
        "A name from a video game, {other} — that's how the world learned mine. The stick one. I find it hilarious."),
    "Mr. Stevens" to listOf(
        "Mr. Stevens, {other}. Bebe's dad. The quiet one — the Stevens women split the spotlight and I'm the stage crew.",
        "Forms are signed, {other}, gate was attended. That's my whole morning report and I'm proud of it.",
        "Bebe runs this family's weather, {other}. Her mother runs the climate. I hold the umbrella and the calendar.",
        "You've caught me between errands, {other}. My wife's lists have lists. I execute at Stevens standard."),
    "Mrs. Stevens" to listOf(
        "Mrs. Stevens, {other}. Bebe's mother. Do come in — a Stevens entrance should never go to waste.",
        "I was a Stevens before this town had a mall, {other}. Bebe inherited the arrival. I merely keep it serviced.",
        "You're the new kid, {other}? Bebe's mentioned you between outfit changes. In this house, that's a headline.",
        "Stand tall, {other}. Posture is the first sentence of every conversation. Bebe's mother taught HER that."),
    "Mr. Stoley" to listOf(
        "Mr. Stoley, {other}. Kevin's dad. Formerly Brooks — we rebranded the family. It's a whole story. Ask Kevin's future self.",
        "You've heard about the scheme, {other}? The future-self thing? It WORKED. The boy's thriving. Theater parenting.",
        "I plan fatherhood in phases, {other}. Phase one: love. Phase two: props. Phase three: also love, with better props.",
        "Kevin's a steady kid, {other}. Steady kids aren't born. They're produced. My wife directs. I fund."),
    "Mrs. Stoley" to listOf(
        "Mrs. Stoley, {other}. Kevin's mother. Formerly Brooks, currently the family's heart — that's the billing and I earned it.",
        "The future-self production, {other}? My husband's script, my direction. Kevin thinks it was a dream. Let him.",
        "A boy raised by loving theater learns to spot a performance, {other}. In this town? That's armor.",
        "You're the new kid, {other}? Kevin mentioned you kindly. Kevin's kindness is reviewed, so that's a rave."),
    "Clark" to listOf(
        "Clark Malkinson, {other}. Scott's dad. If it's about the cable bill, I've said my piece. It's in a documentary now.",
        "Three channels, {other}. We had three channels and we LIKED it. Scott gets his stubborn honestly.",
        "Scott talks about you, {other}. With the lisp, at full speed. I caught most of it. The review was good.",
        "I'm the dad who fought the remote war, {other}. Lost the battle, won the principle. Scott was watching. Good."),
    "Maggie" to listOf(
        "Maggie Yates, {other}. The sergeant's wife. If he's certain about you, I'm the appeal court. Welcome.",
        "Harrison's at the station being sure, {other}. I'm here being steady. The marriage runs on the division.",
        "I talked him out of quitting once, {other}. Every case since is partly mine. I collect no salary but all the stories.",
        "You're the new kid, {other}? The department has a file. I have cookies. Choose your interviewer wisely."),
    "Mitch" to listOf(
        "Detective Murphy, {other}. Mitch. Yates' partner. Whatever he told you, I'll verify it by Friday.",
        "Mind the paperwork, {other}. The department runs on it. Yates runs on certainty. I run the department.",
        "A new face, {other}? The files get a new page. Don't worry — page one is always 'seems fine.'",
        "I've stamped every theory the sergeant's had since 2003, {other}. The stamp is tired. I'm not."),
    "Bob Johnson" to listOf(
        "Bob Johnson, {other}. Mayor's aide. Council. If you have an idea, hurry — the mayor takes credit at ten.",
        "I've served this town since its second day, {other}. My ideas have built half of it. My name is on none of it.",
        "The mayor's announcing my transit plan Thursday, {other}. She'll call it hers. It's tradition. I'm tradition's author.",
        "Government runs on the unnamed, {other}. I keep the minutes, the schedule, and the mayor. In that order of difficulty."),
    "Pusslicker" to listOf(
        "Tom Pusslicker, Channel 4 News, {other}. You're on the record the moment you smile. Smile anyway.",
        "I anchored this town's first decade, {other}. The chair remembers me. The Thompsons are 'the new ones.' Still.",
        "Good evening, South Park — sorry, {other}. Habit. Twenty years of evenings is a long habit to break.",
        "If it happens in this town, {other}, it happens twice: once in the street, once at my desk. I do the second."),
    "Dr. Doctor" to listOf(
        "Dr. Doctor, {other}. Yes, that's the name. Yes, the chart says it too. What seems to be the problem?",
        "Twenty years in this town, {other}. I've set its bones, delivered its babies, and billed Randy for all of it.",
        "Hell's Pass trained me, {other}. Nothing a South Park body does surprises me anymore. Impresses, occasionally.",
        "Sit, {other}. In my experience the town's injuries are ninety percent story, ten percent stitch. Tell me the story."),
    "Choksondik" to listOf(
        "Ms. Choksondik, {other}. Say it carefully, say it once, and we'll get along famously.",
        "I taught the fourth grade for three years, {other}. The famous years. The curriculum included everything. EVERYTHING.",
        "Mr. Garrison had the class before me and after me, {other}. I am the filling in that particular sandwich. Mind your manners.",
        "Eyes front, {other}. Even in a hallway. Especially in a hallway. The fourth grade is a discipline, not a room."),
    "Strong Woman" to listOf(
        "Strong Woman, {other}. Vice-principal. The door you're looking for is me. State your business standing tall.",
        "Principal Victoria runs the schedule, {other}. I run the standard. The standard is: stronger, kinder, no excuses.",
        "Every kid tests me once, {other}. Once is the tradition. The building's record on twice is spotless.",
        "Shoulders back, {other}. You'd be amazed what posture fixes. Half of discipline is just standing like you mean it.")
)

val WAVE6_MORE_LINE_BANKS: Map<String, List<String>> = mapOf(
    "Pip" to listOf(
        "Great Expectations, {other}? Read it. Lived it. The graveyard part needed fewer convicts, but the manners held.",
        "I packed extra sandwiches, {other}. A gentleman is measured by his surplus. Take one. No, I insist. It's a whole thing.",
        "Dougie replaced me once, {other}, in the affections of the class. We compared notes. We're friends now. Very English ending.",
        "Being the target builds character, {other}. I have character in surplus. Some days I'd trade it for a quiet lunch, but there it is.",
        "Miss Havisham sends her regards, {other}. She doesn't. She's fictional and furious. I send mine anyway — manners."),
    "Dougie" to listOf(
        "Butters and me have a secret handshake, {other}. It's not secret, it's just complicated. And classified. Both.",
        "General Disarray, {other}. That's my other job. The uniform's mostly a cape. The cape's mostly confidence.",
        "Thecond grade is serious business, {other}. We have cursive now. CURSIVE. The stakes are enormous.",
        "I may be little, {other}, but I hold a line like nobody. Butters holds his, I hold mine. That's the army.",
        "Thnacks are strategy, {other}. A general feeds his troops. The troops are me and Butters. We eat well."),
    "Jason" to listOf(
        "My family took some heat for the Garrison years, {other}. I answered it with attendance. Cleats on, every day.",
        "Sports teach you things, {other}. Mostly: show up, lose right, win right, be in the photo either way.",
        "Craig's gang, the boys' crew — I'm the bridge, {other}. Bridges don't pick sides. Bridges get walked on. It's fine.",
        "Coach says I have 'fundamentals,' {other}. My dad says I have 'the White work ethic.' I say I have a ball and a Saturday.",
        "If there's a team forming, {other}, my name goes on the list before the list exists. It's a whole reputation."),
    "Terrance Mephesto" to listOf(
        "My clone phase is behind me, {other}. Father's orders. The ranch does genetics; I'm doing strategy now. Related fields.",
        "Bill and Fosse laugh like that because the ranch's acoustics reward commitment, {other}. Don't read into it.",
        "Stan beat my project with a pig and a song, {other}. I keep the recording. A rival studies the classics.",
        "People hear 'Mephesto' and think mad science, {other}. I prefer 'applied heredity.' The schemes ARE hereditary.",
        "The boys' pig made love to an elephant, {other}, and history called it their win. History is a rough draft. I annotate."),
    "Jenny" to listOf(
        "The counterfeit list scandal, {other}? I helped run it and I helped burn it. The institution survived. That's the résumé.",
        "Wendy chairs the debate, I chair the record, {other}. Between us, the circle has a government. Boys have a pond.",
        "A list is a promise, {other}. Break the list's integrity and you've broken the only constitution the fourth grade has.",
        "Sophie thinks she's the new bureau, {other}. She's not wrong. I minuted her arrival. The minutes were kind. Mostly.",
        "My handwriting is in every margin in this school, {other}. Fifty years from now, historians will think I ran the place. Accurate."),
    "Flora" to listOf(
        "Ike's my friend, {other}. Filmore's also my friend. Being everybody's friend is easy. You just do it all day.",
        "The election made Ike happy and Filmore loud, {other}. I bring crackers to both. Crackers are my whole policy.",
        "Kindergarten is the best grade, {other}. We nap. Fourth grade does NOT nap. I have checked and I am sorry for them.",
        "Millie says the lists upstairs are scary, {other}. I think deciding is scary too, but somebody's got to. It was me. I did fine.",
        "If you're sad, {other}, sit by me at lunch. My table fixes sad. It's the crackers and also me."),
    "Filmore" to listOf(
        "Flora CHOSE, {other}. I respect the choosing. I contest the outcome. A statesman holds both. I'm five and I hold both.",
        "Quaid believes in me, {other}. A candidate is only his believers plus snacks. I have one believer and many snacks. Growing.",
        "Ike's administration naps ON THE JOB, {other}. I have never napped on the job. My job is recess. I stand for recess.",
        "When I'm president of kindergarten —retroactively— {other}, first law: the good crackers, for everyone, even Ike. I'm gracious.",
        "My dad says I'm a born campaigner, {other}. My mom says I'm a born negotiator. The recount will decide. It never ends. Good."),
    "Quaid" to listOf(
        "I made the sign myself, {other}. It says FILMORE in letters as big as I could. Some are backwards. It's called style.",
        "Filmore's going to win the NEXT election, {other}. There isn't a next election. We're petitioning. I hold that sign too.",
        "Being best friends with a candidate is a big job, {other}. Crackers, signs, believing. I do all three at full size.",
        "Ike's okay, {other}. Don't tell Filmore I said okay. Tell him I said 'a worthy rival.' That's the official position.",
        "If Filmore's sad, I do the believing for both of us, {other}. That's what the second chair is FOR."),
    "Mimsy" to listOf(
        "Nathan's plans are the best plans, {other}. Even the ones that don't work are rehearsals. He taught me that word. Rehearsals.",
        "Jimmy and Timmy are the enemy, {other}. Nathan says so. They seem nice, which Nathan says is their camouflage.",
        "I carried a whole piano once, {other}. Not far. But I carried it. Nathan put it in the notebook under 'assets.'",
        "People think henchman is a small job, {other}. It's the biggest job. The villain gets the plans; I get the piano.",
        "If Nathan ever retires, {other}, I'll carry the operation myself. Phase one: find a Nathan. It's harder than it looks."),
    "Tricia" to listOf(
        "Craig flipped off a teacher before breakfast, {other}. Mom didn't even look up. This family's baseline is remarkable.",
        "I keep a list of everything Craig's gang has ever denied doing, {other}. It's alphabetical. It's a service I provide.",
        "Dad's the calm, Mom's the sense, Craig's the finger, and I'm the record, {other}. Every family needs all four.",
        "Tweek's nice, {other}. He screams, but nicely. Craig's calmest around him, which in Tucker is a love language.",
        "When I grow up, {other}, I'm keeping the guinea-pig stories and selling them back to Craig at a fair price. Family rates."),
    "Mr. Slave" to listOf(
        "Al and I renewed our vows at the sanctuary, {other}. The goat officiated. Professor takes these things seriously.",
        "Garrison and I are history, {other}. Ancient, documented, board-approved history. Al is the present and it's SUPER.",
        "A classroom assistant's patience never leaves you, {other}. I can wait out any tantrum — child, goat, or mayor.",
        "The leather is a uniform, {other}. Every marriage has one. Ours just photographs louder.",
        "People underestimate the quiet half of a loud marriage, {other}. There is no quiet half. There's a bass half. You're hearing it."),
    "Linda Black" to listOf(
        "Steve reheats on Sundays, {other} — the chef's day off. Tolkien has notes. The notes have improved the roast. Slowly.",
        "This house was built on Steve's cooking and my calendar, {other}. A family runs on both or it runs on takeout.",
        "Tolkien's friends eat here, {other}. All of them. The grocery bill is a neighborhood document at this point.",
        "I don't raise my voice, {other}. A mother who can be heard at low volume never needs the other kind.",
        "The welcome is the policy, {other}. Everything else in this house — the roast, the watch, the boy — is commentary."),
    "Harriet" to listOf(
        "Henrietta dedicated a poem to the camp, {other}. Not to us. The camp. We're in the footnotes of her heart, her father says. He cries.",
        "Bradley came home from the hospital a different baby, the nurses joked, {other}. In this family we don't joke. We love louder.",
        "I knock once, praise twice, and retreat in good order, {other}. Twenty years of marriage taught my husband less than one goth daughter taught me.",
        "The eyeliner budget passed the grocery budget in March, {other}. I signed it. Art is art and she's magnificent.",
        "People ask how I stay cheerful, {other}. I have a daughter who writes thunder and a son who might be magic. Cheerful is just accurate."),
    "Mr. Biggle" to listOf(
        "The camp decision, {other}? Joint. Documented. Regretted at two different volumes. We'd do different now. That's called growth.",
        "I read every poem, {other}. All of them. A father reads the thunder until the thunder reads him back. There's love in there. I found it.",
        "Bradley's adoption papers are the proudest document in this house, {other}. Henrietta's poems are second. She'd agree. Loudly.",
        "Foundations don't complain, {other}. We hold. The roof praises, the thunder writes, the small one glows. I hold.",
        "Harriet knocks once and praises, {other}. I knock never and fix things. Between us the girl gets a whole childhood. Eventually she'll invoice us."),
    "Mr. Turner" to listOf(
        "Heidi wanted a later curfew, {other}. We negotiated. She brought charts. I brought the perimeter. The charts won two minutes.",
        "Other dads do scrapbooks, {other}. I do threat assessments. Heidi's prom is in six years and I am READY.",
        "Nancy says the gate should be a welcome, not a wall, {other}. We've compromised: it's a welcoming wall. With a bell.",
        "A daughter's safety isn't a phase, {other}. It's a load-bearing structure. I am the structure. Ask anyone. Ask the structure.",
        "The arms are for hugging too, {other}. Heidi gets the first hug, the town gets the flex. Order matters."),
    "Nancy" to listOf(
        "The slumber party had rules, {other}. Written ones. The girls still talk about the rules. Legend is just hospitality remembered.",
        "Thomas means fortress, I mean hearth, {other}. Heidi gets both, which is why she's the balanced one. Don't tell him. Tell him.",
        "Heidi's friends call me the cool Turner, {other}. Thomas is the safe Turner. A girl needs one of each, minimum.",
        "My name traveled in a game, {other}. A stick, a quest, my whole introduction. I've had worse press. I've had better hair in it.",
        "A gate that never opens is a wall, {other}. I keep ours oiled. Thomas keeps it strong. Heidi keeps it honest."),
    "Mr. Stevens" to listOf(
        "Bebe got the arrival from her mother, {other}, and the timing from me. Timing is invisible. That's how you know it's mine.",
        "The playset era, {other}? I signed three forms and learned nothing I can repeat. The Stevens standard held. Barely.",
        "I don't do the spotlight, {other}. I do the calendar, the gate, and the umbrella. Kingdoms run on less.",
        "My wife's lists run this house like a small, glamorous state, {other}. I am its loyal civil service.",
        "A quiet dad is a town's ballast, {other}. We don't make the minutes. We make the minutes possible."),
    "Mrs. Stevens" to listOf(
        "Bebe's circle runs the fourth grade, {other}. I run the mothers' version. The apple rolled exactly zero feet.",
        "Shoes are a sentence, {other}. Bebe learned punctuation from me. The town's boys are still learning to read. Bless them.",
        "I don't chase trends, {other}. I audit them. The playset era failed my audit in a week. Bebe came home early. Smart girl.",
        "An entrance is a promise, {other}. Keep it, and the room is yours. Stevens women keep it. Watch Bebe. Watch me.",
        "Glamour is maintenance, {other}, like a garden or a reputation. Mine is twenty years tended. Hers is already blooming."),
    "Mr. Stoley" to listOf(
        "The actor we hired aged terribly, {other}. On purpose. We cast for the lesson, not the likeness. Kevin straightened right up.",
        "Some dads ground, {other}. I produce. Grounding fades; a well-cast future haunts a boy into adulthood. Lovingly.",
        "The Brooks name is retired, {other}. Stoley is the marquee now. A family should bill itself correctly.",
        "Kevin's teachers say he's 'unshakeable,' {other}. Unshakeable is produced, directed, and catered. By us. You're welcome, school.",
        "Next scheme's already drafted, {other}. It's for Kevin's twenties. He doesn't know. The props are in the garage. Shh."),
    "Mrs. Stoley" to listOf(
        "I cried at the premiere of our own scheme, {other}. The actor was that good. Kevin was that scared. Motherhood is complicated theater.",
        "People judge the production, {other}. Then they meet Kevin — steady, kind, unshakeable — and they buy tickets.",
        "A performance a child believes teaches once, {other}. A performance he catches teaches forever. We aimed for believe. We hit it.",
        "My husband drafts, I direct, {other}. If Kevin ever writes a memoir, we're ready for the reviews. We produced those too.",
        "The heart of the scheme was never the fright, {other}. It was the future we showed him: loved, expected, worth scaring straight."),
    "Clark" to listOf(
        "The streaming man came to my door, {other}. MY door. Scott watched me defend the bill like a border. Borders matter.",
        "Sophie's dad and I compared cable wounds, {other}. The Scottsdale bundle is a tragedy in twelve tiers. We bonded.",
        "Scott's lisp is from his mother, his stubborn is from me, {other}. The diabetes he handles better than both of us handle the bill.",
        "A remote is a scepter, {other}. The man who holds it holds the evening. I share the scepter now. Growth is real.",
        "Three channels raised me fine, {other}. Four hundred raised a generation that can't pick a movie. Scott picks. I taught him."),
    "Maggie" to listOf(
        "Harrison came home certain about a parking ticket once, {other}. A whole theory. I made tea and let the theory tire itself out.",
        "I don't solve his cases, {other}. I solve the detective. The cases are downstream of a man who's slept and been believed in.",
        "The Jeffersons business, {other}? He was ready to quit. I reminded him the town's chaos has a shape only he can see. He stayed. You're welcome, department.",
        "Worry is a shift I work nightly, {other}. Steadiness is the day shift. Twenty years, no missed days. The force runs on it.",
        "Mitch is the department's spine, {other}, but I'm its home address. Every theory ends at my table eventually. I set a place for it."),
    "Mitch" to listOf(
        "Yates announced a theory about the pond ducks in '09, {other}. I filed it. The ducks filed a complaint. Standard year.",
        "People think the aide-de-camp is decoration, {other}. Then a case needs a warrant, a timeline, and a calm voice. Suddenly: Mitch.",
        "Barbrady's reports come in crayon-adjacent, {other}. I translate them into evidence. The department owes me a medal and a magnifier.",
        "Certainty is the sergeant's gift, {other}. Verification is mine. A town needs both or it has neither — just noise with a badge.",
        "I've been the second name on this department since the crime-stoppers were in short pants, {other}. The first name gets the statue. I get the truth."),
    "Bob Johnson" to listOf(
        "The mayor announced my park plan as hers in '08, {other}. The park exists. The credit doesn't. The park matters more. Mostly.",
        "Janson's the new aide, {other}. Good kid. I taught him the two rules: write it down, and let her announce it. He'll go far. She'll announce it.",
        "An idea stolen weekly for thirty years is just an idea with a distribution deal, {other}. I distribute. The town benefits. So be it.",
        "Ted had this job before Janson, {other}. Died doing paperwork, nearly. The minutes remember him. I make sure of it.",
        "If the town ever names a bench for me, {other}, McDaniels will cut the ribbon and call it hers. I'll hold the scissors tray. Happily."),
    "Pusslicker" to listOf(
        "Thompson took the chair in thirteen, {other}. Nice man. Reads well. The chair still leans my way when it rains. Chairs remember.",
        "I covered the volcano, the earthquake, and three of Randy's eras, {other}. The desk has a dent where my hand lands for 'breaking.'",
        "An anchor's pause is a public trust, {other}. Break it for trivia and the town stops believing the weather. I never broke it.",
        "Camera two is for the town, {other}. Camera one is for the facts. Amateurs reverse them. I never did, not in ten seasons.",
        "Retirement rumor, {other}? An anchor doesn't retire. He becomes 'the voice of' things. I'm in negotiations with several echoes."),
    "Dr. Doctor" to listOf(
        "I delivered half this town, {other}, and set the other half's bones. The halves overlap. Randy is in both. Twice.",
        "A town this size needs one doctor, {other}, and it has had one: me. The interns rotate. The Doctor remains.",
        "Gauche is a fine physician, {other}. The young man's taken the young cases. The town still brings me the strange ones. Correctly.",
        "My bedside manner is 'sit,' {other}. Patients want a doctor who has seen it before. I have seen it before. All of it. Twice.",
        "The chart says 'Dr. Doctor' because I filled it in, {other}. A man should name himself before the town does it for him."),
    "Choksondik" to listOf(
        "The facts-of-life unit, {other}? I taught it without blinking. The class blinked for a week. Education is a contact sport.",
        "Garrison before me was chaos, after me was chaos with notes, {other}. My three years were the curriculum's spine. Ask the board. They blush.",
        "I died as I taught, {other} — completely, and in front of everyone. The town's memory is my pension and it's generous.",
        "A name like mine builds character in a classroom, {other}. Mine first, then theirs. We all grew. That was the lesson under the lessons.",
        "Chef is in this roster past his own ending, {other}, and so am I. The cafeteria and the classroom keep their dead. It's a school, not a census."),
    "Strong Woman" to listOf(
        "Victoria and I divide the school, {other}: hers is the schedule, mine is the standard. A kid can dodge a schedule. Not a standard.",
        "I don't raise my voice, {other}. I raise the bar and hold the door. Trouble reads the room and chooses the hallway. Wise.",
        "Role model is a job you do standing up, {other}. Every corridor, every lunch line. The kids are always watching. So am I.",
        "Strength isn't the flex, {other}. It's the carry: the books, the standard, the kid having the worst day. I carry all three.",
        "The fourth grade fears chaos will eat the school, {other}. Chaos met me in September. We have an understanding now.")
)


// ------------- wave 6: daily topics (9 + 9 = 18 each) -------------
val WAVE6_DAILY_TOPICS: Map<String, List<String>> = mapOf(
    "Pip" to listOf(
        "Polished my shoes before school. A gentleman's day begins at the feet and rises.",
        "The boys called me French again at lunch. I corrected them in French. It confused everyone, including me. Worth it.",
        "Shared my sandwiches at the pond. Three takers. The surplus doctrine holds.",
        "Read two chapters of Dickens by the fence. The fence has heard the whole canon now and has opinions.",
        "Dougie and I compared being replaced. He never was, technically. We agreed the class has a type and moved on like gentlemen.",
        "Wrote a letter home. Began it 'Dear Whoever.' Ended it 'Your Pip.' The middle was weather and hope.",
        "Nobody set anything of mine on fire today. Marking the calendar. A red-letter day, in the good ink.",
        "Taught a first-grader to tie a tie. He'll never need it here. He'll have it forever. That's the point of most things I know.",
        "Tea at four, alone, properly. Solitude with a pot and a saucer is just company that behaves."),
    "Dougie" to listOf(
        "Held the line at recess. The line was for tetherball. I held it like a general anyway.",
        "Butters let me wear the cape for one whole minute. Best minute of the month. The cape smells like courage and hot chocolate.",
        "Practiced my thpeech— SPEECH. The lisp and I are negotiating. It's going great. We're both winning.",
        "Delivered snacks to the troops. Troops: two. Snacks: sufficient. Morale: enormous.",
        "Second grade got cursive today. My G looks like a swan. My swan looks like a G. The teacher says both are 'coming along.'",
        "Stood next to Butters at the fence for the whole recess. Didn't say anything. Didn't need to. That's the friendship's whole headquarters.",
        "General Disarray reviewed the troops. The troops are me. I passed. I always pass. The cape does the passing.",
        "A fourth-grader knew my name today. The LOWER school is being noticed. Our day is coming. Slowly. With snacks.",
        "Drew a map of the playground with X's where the good hiding spots are. Classified it immediately. It's in my sock drawer."),
    "Jason" to listOf(
        "Brought the good ball today. The recess league runs on the good ball. I am infrastructure.",
        "Someone asked what position I play. All of them, eventually. That's not bragging, that's the roster's size.",
        "Craig's table argued the game for a full lunch. I refereed with a fry. The fry's calls were respected.",
        "Dad says the Whites show up. Showed up. It's not a slogan, it's just the calendar with cleats on.",
        "Middle row of the class photo again. Dead center. The photo needs a middle and the middle is me. I've made peace with being load-bearing.",
        "Ran the pond loop twice before breakfast. Personal best. The ducks kept the old record. The ducks are retired champions.",
        "A first-grader asked for my autograph as a joke and I signed it like a professional. Joke's on history now.",
        "Coach let me carry the equipment bag. THE bag. The one with the whistle in it. I didn't blow it. Growth.",
        "If the team wins Saturday, the photo changes. If we lose, I change nothing. Showing up is the streak that matters."),
    "Terrance Mephesto" to listOf(
        "Reviewed the science fair landscape. The boys' entry is sentimental. Mine will be hereditary. Let them prepare.",
        "Father grew something with too many opinions again. Walked it. Fed it. Took notes on its posture. Standard ranch morning.",
        "Bill laughed at my remark for a full minute. Fosse laughed at Bill. I allow the acoustics. Morale is a resource.",
        "Stan's pig is a legend and legends decay. My next project appreciates. That's the whole difference between us.",
        "Kept the notebook current: three observations, one hypothesis, zero clones. Father calls the zero 'restraint.' I call it pacing.",
        "Someone asked if I'm the comedian. I smiled the smile that ends conversations and began a better one about genetics.",
        "The ranch's new sign went up. MEPHESTO, it says. The town reads it as a warning. Father reads it as a welcome. Both readings are data.",
        "Chess club needs a second board. I played both sides of the first and won. And lost. The loss was educational. I billed it to myself.",
        "A rival's patience is just a scheme with good posture. Stood very still today, extremely menacingly. Several people noticed. Good."),
    "Jenny" to listOf(
        "Updated the list's margins today. The list is law; the margins are where the law breathes.",
        "Committee sat for twenty minutes. Wendy chaired, I minuted, the institution endured. Democracy is mostly stationery.",
        "A third-grader asked to see the list. I gave her the procedure for asking. She'll be ready in a year. They never are.",
        "Filed the Scottsdale bureau's latest report. Sophie writes like a novelist and ranks like a judge. The circle chose well.",
        "The counterfeit's anniversary. I keep the date. Institutions that forget their scandals repeat them with worse handwriting.",
        "Two disputes settled before lunch, both by footnote. Nobody reads the footnotes. Everybody obeys them. That's governance.",
        "My folder gained a tab: CONTINGENCIES. Wendy asked what it's for. I said, 'You'll be the first to know.' She respected it.",
        "Ranked nothing today, on purpose. A committee that rests is a committee that lasts. Also I was tired. Both are minuted.",
        "The margins of the cafeteria menu now carry my corrections. Even lunch is governed, if you love it enough."),
    "Flora" to listOf(
        "Brought extra crackers again. Somebody always forgets theirs. Somebody is usually Filmore. He forgets on purpose, I think.",
        "Ike showed me his drawing. It's his family and everyone's orange is different. I told him it's the best one. It is.",
        "Filmore gave a speech at recess. I clapped at the end AND the middle. The middle needed it more.",
        "Nap time is the best time. I dreamed about a vote where everybody won. Millie says that's not how votes work. It should be.",
        "Quaid held Filmore's sign all recess and his arms got tired and he didn't say so. I said so for him. That's my job in the campaign.",
        "Millie braided my hair before school. Big sisters are a kind of weather — you just get to live in the nice kind.",
        "Decided something small today and it felt as big as the big one. Deciding is a muscle. Mine is strong from the election.",
        "The teacher says I share 'instinctively.' I don't know the word but I know the feeling. It's the crackers feeling.",
        "If I could vote on every day, I'd vote for this one. It had crackers, a drawing, and nobody cried at lunch. Five stars."),
    "Filmore" to listOf(
        "Gave the fence speech again. Attendance: Quaid, Flora, two squirrels. The squirrels are undecided. Everything hinges on the squirrels.",
        "Filed appeal number twelve. The administration (the teacher) says appeals are 'not a kindergarten thing.' Tyranny has a curriculum now.",
        "Flora brought crackers to MY rally again. The opposition feeds my crowds. Her kindness is a strategy and I respect it while losing to it.",
        "Quaid's new sign is bigger. The letters lean left. I told him it's called momentum. He believed me. He's the best staff in politics.",
        "Ike napped through my whole speech. THE PRESIDENT NAPPED. If I'd napped, it would be a scandal. I keep the clippings. I make the clippings.",
        "Practiced my conceding face in the mirror, just in case. It looks noble. I hope never to use it. I hope to use it once, famously.",
        "Mom says campaigning at breakfast is 'a lot.' Dad says it's 'the family gift.' The gift gave a speech about toast and won the table.",
        "Recess recount: kickball was 4-3, not 5-3. I have witnesses. The witnesses are Quaid and a squirrel. Justice is slow but it has snacks.",
        "One day the kindergarten will ask who stood up. The record will say: Filmore stood, Quaid held, Flora fed everyone. History will sort us."),
    "Quaid" to listOf(
        "Held the sign for the whole recess. Both arms. It got heavy at the end and I switched to one arm like a professional.",
        "Filmore says I'm the best second in the school's history. There's no list for that. If there was, Jenny would keep it. I'd be on it.",
        "Brought the good crackers today — the round ones. Filmore took two. Campaigns run on the round ones. Everyone knows.",
        "Ike said hi to me at lunch. I reported it to Filmore as 'diplomatic contact.' Filmore put it in the charts. Everything goes in the charts.",
        "Practiced believing extra hard today. Some days Filmore's sad about the vote and I do the believing for two. It works. He stands back up.",
        "The sign's letters lean because the wind leans, Filmore says. I hold it straighter when it's windy. That's my whole job and I love it.",
        "A squirrel watched the whole speech. I counted it as press. Filmore counted it as momentum. We both wrote it down differently.",
        "If Filmore ever wins, I get to be the one who holds the BIG sign. We've discussed it. It's basically a contract.",
        "Being second isn't behind. It's beside, but with the snacks. I checked with Flora. She agrees and she DECIDES things."),
    "Mimsy" to listOf(
        "Carried Nathan's books, Nathan's bag, and Nathan's umbrella. It wasn't raining. The umbrella was a statement. I carried the statement.",
        "Nathan explained phase four at lunch. I understood phase one really well. The other phases are in the notebook. The notebook understands.",
        "Jimmy did a joke and Timmy laughed and I almost laughed, but laughing at the enemy is treason. I coughed instead. Nathan approved the cough.",
        "Held a door for the whole school at dismissal. Nathan calls it 'controlling the exits.' I call it holding a door. We're both right.",
        "Practiced my menacing stand. A kindergartner waved at me during it. I waved back. Nathan says menace can be polite. Good.",
        "The operation's snack budget is my responsibility now. It's mostly crackers. Flora's table shares anyway. I log the sharing as 'requisitions.'",
        "Nathan lost at chess to Terrance and called it intelligence-gathering. I carried the board home. The board is heavy with lessons.",
        "If henchman had a uniform I'd wear it every day. Nathan says the uniform is loyalty and I already wear it. Best review ever.",
        "Stood where I was told, carried what I was given, believed the whole plan. A henchman's day. A good day."),
    "Tricia" to listOf(
        "Craig used nine words before breakfast. A family record is twelve. He paced himself. I'm proud and alarmed.",
        "Mom asked Craig about his day and he flipped off the question. Not Mom. The question. There's a whole grammar to it now.",
        "Stripe got a new cage corner today. Craig supervised like a mayor. I supervised Craig. The chain of command is clear.",
        "A girl in my class asked if my brother is 'the finger one.' I said he's the guinea-pig one too. Reputation is a team sport.",
        "Kept the list current: things Craig's gang denies, volume two. Tweek features heavily and also innocently. Both can be true.",
        "Dad watched Craig's whole soccer game in total silence, then said 'good.' Craig glowed for an hour. Tucker praise is a controlled substance.",
        "I get the remote on Fridays by ancient law. Craig appeals weekly. The court (Mom) is firm. The law holds.",
        "Someday Craig will need my list — for a memoir, a defense, or a wedding toast. I'll charge him in guinea-pig stories. Fair market.",
        "Being the little sister of a legend means the legend owes you. Craig owes me approximately everything. I collect in small, daily installments."),
    "Mr. Slave" to listOf(
        "Fed the strays at dawn. Every bowl filled, every name said. The sanctuary wakes up in my register and goes to sleep in Al's.",
        "Al is planning a pageant for the goats again. I am building the stage. Marriage is just this, repeated with different livestock.",
        "A man at the store recognized me from the classroom years. Called me 'the assistant.' I glowed. The title still fits like the leather.",
        "The vows are holding like iron. We oil them with breakfast. Al makes the eggs; I make the coffee strong enough to testify.",
        "Walked the graduates at the park. Three dogs, one cat in a stroller, and a neighborhood watching a marriage work. Public service.",
        "Someone whispered 'Jesus Christ' at my entrance at the fair. I answered 'close enough' in the low voice. Al laughed for a full minute.",
        "The quiet work of a loud life: bins out, feeds stocked, strays counted. If I hum, the animals hum. Professor supervises.",
        "Garrison sent a card. A CARD. Twenty years later. I put it on the fridge, where Al's magnets hold our whole archaeology.",
        "Evening inventory: one husband, sparkling; forty strays, fed; one bass line, steady. The books balance."),
    "Linda Black" to listOf(
        "Morning at this address is a production: Steve reheating something ambitious, Tolkien reviewing the news like a small anchor.",
        "Tolkien's friends call this house 'the good house.' I pretend not to hear. I hear everything. It's the job description.",
        "Signed the school forms in triplicate and with pleasure. A signature should look like it means it.",
        "The neighborhood watch met in our kitchen because our kitchen has the chairs and the light. Steve takes the minutes. I take the room.",
        "A new family moved in down the street. I brought the casserole before their boxes were open. That's not gossip. That's the welcome.",
        "Tolkien asked what's for dinner like it's a press conference. Steve answered like a chef. I answered like a mother: 'food.' Both correct.",
        "The porch light at this house has been on for twenty years of kids finding their way. Electricity is cheap. Welcome is the expensive part.",
        "Called Tolkien's teacher just to say thank you. Teachers should get one call a year that isn't a problem. Ours got hers.",
        "If volume were wealth we'd be poor on purpose. The Blacks speak like people who intend to be understood the first time."),
    "Harriet" to listOf(
        "Poem through the door at breakfast. I praised it through the door. The door is basically a member of this family now.",
        "Bradley asked for black curtains 'like Henrietta's.' The goth is hereditary by proximity. I ordered two sets. Solidarity is décor.",
        "My husband read the new poems and went quiet in the foundation way. Quiet in a Biggle man means the thunder landed.",
        "The camp sent ANOTHER brochure. I used it to line the cat's box. The cat is not ours. The neighborhood's strays review our mail now.",
        "Henrietta slammed a door in iambic pentameter. I counted the meter while the frame settled. A mother multitasks.",
        "Dinner: one goth, one changeling, one foundation, one roof. The roof served lasagna. The thunder ate two helpings. Recorded as a treaty.",
        "A neighbor asked if the poems are 'a phase.' I said phases are for the moon. This is climate. She writes weather, that girl.",
        "Bought the black polish, the black candles (unscented, house rule), and one pink thing for me. A roof is allowed one pink thing.",
        "Knocked once. Praised twice. The door opened an inch more than yesterday. Inches are the currency of this house and I am rich."),
    "Mr. Biggle" to listOf(
        "Fixed the porch step before anyone asked. A foundation's love language is the absence of future accidents.",
        "Henrietta's new poem mentions 'the quiet man downstairs.' Downstairs. I'm the quiet man. I'm IN the work. Sat with that all evening.",
        "The camp's survey came back with my handwriting on it like a treaty document. Harriet framed the envelope. We heal at our own volume.",
        "Bradley helped me sand a board today and hummed something ancient-sounding. I hummed along. Chosen family harmonizes off-key and means it.",
        "Paid the eyeliner invoice without sitting down this time. Growth, in a foundation, is measured in invoices survived standing.",
        "Read the poems twice. The second read is for the father; the first is for the man. Both men approve. The father cried in the truck, professionally.",
        "Harriet does the knocking. I do the waiting. A girl needs a house that knocks AND waits. We've got both shifts covered.",
        "Bought good paper AND good pens. Procurement is my poetry. It rhymes in bulk and it's dedicated to her, unsigned.",
        "The house is old, the daughter is thunder, the son is a question the universe asked politely. The foundation holds. It's what I do."),
    "Mr. Turner" to listOf(
        "Perimeter walk at dawn. All quiet. The gate bell sang once for a squirrel. The squirrel has been warned before. It knows the terms.",
        "Heidi's laugh carries to the street when her friends visit. I allow the volume. Joy is not a security risk. Joy is the POINT of the perimeter.",
        "A delivery man approached the gate wrong. I educated him kindly. Nancy says 'kindly' is doing heavy lifting. The gate is a school now.",
        "Checked the school's pickup line like a general reviewing weather. Heidi waved. The wave disarmed the whole review. It does every time.",
        "The arms did their job at the hardware store today: reached the top shelf for three separate grandmothers. Perimeter extends to grandmothers.",
        "Nancy put a flower on the gate. A FLOWER. On the perimeter. I have allowed it. The fortress has a garden now. This is called growth.",
        "Heidi beat me at chess. She planned four moves deep. The perimeter is proud and mildly concerned about what it has raised.",
        "Three versions, they say. Let the record show all three versions were at the school gate at three o'clock. Consistency is my alibi.",
        "If safety were a sport I'd have a shelf of trophies. Instead I have a daughter who rolls her eyes and texts when she arrives. Better shelf."),
    "Nancy" to listOf(
        "Defused a DEFCON Dad situation with pancakes before school. The perimeter eats pancakes. Who knew. I knew. I always know.",
        "Heidi tried the new curfew argument: precedent. Her father's own tactics! I mediated like a small UN and awarded four minutes.",
        "The gate got its flower today. Thomas saluted it by accident. I will treasure the salute forever and mention it at holidays.",
        "A slumber-party alumna — grown, married — sent a card: 'Your rules raised us right.' I cried at the mailbox. Legend status: confirmed by mail.",
        "Hosted coffee for the mothers. Turner report: the fortress is a hearth with a bell. The mothers approved. The bell is staying.",
        "Thomas asked if the flower compromises security. I said it compromises NOTHING and improves morale by a factor ofwelcome. He wrote that down.",
        "Heidi asked me — me! — if Dad was always like this. I told her about young Thomas and the perimeter's first fence. She HOWLED. Inheritance is funny.",
        "An open gate is a policy, not an accident. I oil the hinge monthly and the welcome weekly. The town's kids know this address by heart.",
        "If my daughter is the balanced Turner, it's because one parent built the wall and one built the door in it. Doors win. Walls hold. Both love."),
    "Mr. Stevens" to listOf(
        "Gate duty, forms signed, umbrella deployed against a forecast that lied. A Stevens man is ready for the rain that doesn't come. That's the job.",
        "Bebe tried three entrances at breakfast for rating. I rated the third. Her mother rated all three. The third was, in fairness, devastating.",
        "The quiet chair and I had our evening meeting. Agenda: none. Minutes: glorious. The glamour hummed in the other rooms like a kept promise.",
        "A young dad asked me how Bebe got so confident. I said: watch her mother arrive anywhere. He watched. He understood. The lesson is visual.",
        "Ballast log: the household pitched twice this week (fashion emergency, list emergency) and righted both times. No speeches were made. By me.",
        "My wife's lists included 'buy nothing' at the mall today. We bought one thing. It was on a sub-list. The bureaucracy provides for joy.",
        "Held the coats at the school thing for forty minutes. A dad's coat-holding is a kind of applause. The Stevens women heard it. They always do.",
        "If my daughter ever thanks the stage crew, I'll be in it, holding something. She will. Stevens women notice the crew. I raised the noticer with her mother.",
        "Evening report: family fed, forms filed, umbrella drying. The Stevens standard is just this, done daily, without applause. Applause optional. It comes anyway."),
    "Mrs. Stevens" to listOf(
        "Entered the grocery like a headline. The produce rearranged itself emotionally. Bebe takes notes; the standard is transmitted by osmosis and posture.",
        "Bebe asked for the honest opinion today, not the kind one. I gave her the honest one at full Stevens strength. She stood taller. That's the whole trick.",
        "The mothers' table convened. Stevens report: the circle is Bebe's, the season is hers, the shoes are new. Minutes were taken in looks.",
        "A trend tried to happen in my kitchen this morning. It was declined, politely, with tea. Trends may reapply in a season.",
        "My husband held the umbrella over a dry sidewalk for twenty minutes on a forecast's say-so. Marry the forecast, I say. The weather provides.",
        "Taught Bebe the eyebrow today. One eyebrow, correctly deployed, ends arguments that words prolong. She has the eyebrow now. The fourth grade should prepare.",
        "The playset era gets mentioned at luncheons like a war. I sip my tea. I ended my part of it with a smile. The smile is documented.",
        "An entrance is a promise and I keep mine daily: the room will be better for the arrival. Ask the room. The room is a witness.",
        "If Bebe's confidence has a source code, it's a woman at a mirror saying: we do not enter; we arrive. She has the code. She compiles beautifully."),
    "Mr. Stoley" to listOf(
        "Rotated the garage costumes. The 'future' wig needed brushing. A scheme's props are a family's silver: you polish the inheritance.",
        "Kevin did his homework without a prompt, a prod, or a production. The theater worked so well it's now just... parenting. I miss the theater. A little.",
        "The actor's card sits on the fridge. Kevin asked who he is. 'An old friend of the family's,' I said. The truest lie I own.",
        "A dad at coffee asked for the scheme's blueprint. I said: love, timing, and a casting budget. He laughed. He thought I was joking. The best schemes are.",
        "Brooks is retired. Stoley is the marquee. Kevin is the headline act. I check the marquee nightly like a man who owns a theater of one.",
        "Phase planning for Kevin's twenties: the garage has a section. My wife has a binder. Kevin has no idea. He has better: he has us.",
        "If the boy ever asks for the whole truth, he'll get it — with a premiere, an intermission, and his mother directing the reveal. We're ready.",
        "Some men leave their sons a watch. I leave mine a plot twist with a moral. The watch would've been easier. The twist will last longer.",
        "Watched Kevin be kind to a smaller kid at the park today, steadily, like it's nothing. It's everything. The production's final review is in."),
    "Mrs. Stoley" to listOf(
        "Brushed the future wig. It deserves care. It scared our son into the light, which is more than most wigs accomplish.",
        "Kevin told a friend our house is 'produced.' The friend asked what that means. Kevin said: 'It means everything's on purpose, even the love.' I had to sit down.",
        "Directed breakfast. Small cast, warm reviews. A mother's direction is mostly entrances: everyone enters the day greeted. That's the whole play.",
        "The actor wrote again. He's well. He asks about 'the boy.' The boy is steady, I wrote back. The boy is the review we all live in.",
        "My husband drafts; I hold the eraser of mercy. Between us Kevin got a childhood with a plot AND a soft landing. Both were in the script.",
        "A performance believed teaches once — but I check on the garden of it yearly. Kevin tells the story straight and kind. The garden is an orchard now.",
        "If Kevin writes the memoir, I'll direct the premiere and cry in the front row in the good seat. Front row, centre: the Valmer seats. Sarah understands.",
        "The heart was never the fright — I said it at coffee and watched three mothers exhale. We all have our productions. Mine just had a cast.",
        "Evening dailies: the boy, the day, the husband's pencil down by nine. Ten thousand episodes. The series runs because the hearts do."),
    "Clark" to listOf(
        "Polished the antenna for no reason the decade can justify. Three channels, clear as a bell. A man maintains his monuments.",
        "Scott beat the dungeon level and explained it for twenty minutes with the lisp at full gallop. I understood four words. I cheered all twenty minutes.",
        "The Scottsdale bundle man and I exchanged the dad nod of the cable wars. Veterans. Our sons compare pumps; we compare scars. Both are statecraft.",
        "Ellen watched her show; I watched Ellen watching, mostly. The treaty's finest clause is the company. Nobody streams company.",
        "A bill came and it was FAIR. I read it three times like a poem. Fair bills are the harvest of the war. I framed the envelope. Ellen sighs.",
        "Scott asked why I fought so hard over TV. I said it was never the TV. It was being told what things cost by people who'd never met us. He nodded like a small judge.",
        "The remote rests in the middle of the couch now. Neutral ground. The scepter is a republic. My son holds office hours. The republic holds.",
        "Fixed a neighbor's picture with a coat hanger and inherited wisdom. She offered to pay. I accepted cookies. The old economy provides.",
        "Three channels raised me. One son raises my standards daily. The inheritance improved on the way down, which is all any father banks on."),
    "Maggie" to listOf(
        "Kettle on before the sergeant's key hit the door. He debriefed the day to the steam. I asked the one question. The case confessed over tea.",
        "Mitch's soup note is on the fridge: 'For the appeal court.' The court accepts soup as tribute and rules accordingly: not guilty, extra crackers.",
        "Harrison stood in the kitchen being certain at the refrigerator. I handed him the list. Certainty shops well when it's aimed.",
        "The Jeffersons anniversary passed quietly. I marked it the way I mark everything important: by feeding the man it saved without mentioning it.",
        "A young wife at the station asked how I do the worrying. I said: professionally, off the clock of him. He gets the steadiness. I keep the ledger.",
        "He slept through the night without pacing once this week. Somewhere a case went unsolved in his dreams and the world turned anyway. Progress.",
        "The force's barbecue is coming. I'll bring the potato salad that has ended three feuds. Diplomacy is a side dish at my table.",
        "If they statue him, the base will say it. I'll see to it personally. Bases outlast statues; ask any foundation. Mr. Biggle would agree.",
        "Twenty years of believing at full volume and the returns compound nightly: a man who walks in loud and leaves the day at the door."),
    "Mitch" to listOf(
        "Filed the week's theories by outcome: held, folded, ducks. The ducks file is the thickest in the department and the most honest.",
        "Yates briefed the ladder like it had counsel. I took the statement. The ladder maintains its silence, which is more than most witnesses manage.",
        "Barbrady brought in a 'suspicious banner.' It was a banner. I wrote 'banner' in the report in my best handwriting. The system works because someone writes 'banner.'",
        "The appeal-court soup arrived. Ate it at the desk like a verdict. Maggie's rulings are the department's real case law.",
        "A rookie asked for the lead on the marquee thing. I gave him the flashlight instead. He'll learn what I learned: the light matters more than the name.",
        "Verified an alibi at the pond. The ducks corroborated. The ducks always corroborate; that's what makes them useless and beloved.",
        "The files are in order. The town is out of order in the usual, survivable ways. Between the two is my whole career, and I keep it swept.",
        "Certainty announced a breakthrough at 6 a.m. I verified it by seven and it was breakfast. Most breakthroughs are. The stamp is patient.",
        "If the statue goes up, I'll hold the ladder for the sculptor. Aides hold ladders. It's in the oath I wrote for us."),
    "Bob Johnson" to listOf(
        "The snow memo goes to the mayor's desk Monday. By Thursday it's her 'vision.' By Friday the plows roll on my routes. The town wins; the byline loses. Standard.",
        "Taught Janson the filing system and the other filing system — the one where Ted lives. He took notes. Aides who take notes inherit the town.",
        "Sat on the nameless bench in my park and watched the town use it. A bench used is a bench justified. Credit is a rumor; benches are facts.",
        "McDaniels asked for 'that idea about the lights.' I produced the lights memo, dated March. She called it 'my lights initiative' by lunch. The lights will be lovely.",
        "Thirty years of minutes in my hand. If the town ever reads them it'll find one author. It'll also find I spelled its name right every time. That's the signature.",
        "A schoolkid's civics class visited. One asked who runs the town. The mayor smiled. I smiled. The class learned the lesson no textbook holds.",
        "The notebook has forty-one ideas in it, dated, initialed, ready. The town will get them all eventually. It'll thank the door they're announced from. So be it.",
        "Ted's chair is Janson's chair is the next one's chair. The chair outlasts us. I oil the chair. Aides understand furniture.",
        "Announced nothing today, achieved several things. The ratio is the job. The job is the town. The town is worth the ratio."),
    "Pusslicker" to listOf(
        "Practiced the pause at breakfast. The eggs got the full grave version. A pause kept sharp is a public trust maintained.",
        "The chair leaned when it rained today. I felt it through the suit. Ten seasons of weather between us and it still reports first to me.",
        "A Thompson viewer wrote in: 'You're still the voice.' I read it twice, professionally composed both times. Mostly.",
        "Covered the school play like a summit. The mountain kid was magnificent. I gave camera two the full weight of it. The town wept at the desk with me.",
        "Breaking-news dent, Randy dent. I ran my hand over both like braille. Twenty years of this town is written on one desk and I can read it blind.",
        "The new Tom does the field work I used to sprint. Good legs on him. The desk work is the voice work, and the voice is still on file here.",
        "They offered the reunion special again. I said the desk accepts. An anchor doesn't retire; he becomes a special. I'm very special these days.",
        "If the station museum happens, my tie goes in a case. The tie has covered more history than most books. It deserves the glass.",
        "Good evening, South Park — I say it to the mirror at eight like a man tuning an instrument. The instrument holds. So does the town."),
    "Dr. Doctor" to listOf(
        "Morning rounds: two fevers, one harmonica-adjacent incident, zero surprises. The town auditions its chaos and I keep declining to applaud.",
        "Delivered the Nguyen baby at dawn. Mother fine, baby furious, town plus one. The log got its line. The log always gets its line.",
        "Gauche consulted me on a case with 'unusual features.' I have a binder for unusual features. Three binders. He took the second. He's learning.",
        "A patient Googled his symptoms and arrived with a binder of his own. We compared binders. Mine was older. We went with mine. He lived, smugly.",
        "Hell's Pass at dusk. The building exhales the day's emergencies like a held note resolving. Twenty years and I still listen for the resolution.",
        "The chart said 'patient states name is a title.' I wrote 'patient is correct.' Charts should reward accuracy. Mine do.",
        "Set a wrist, set a record straight, set a father's mind at rest — order of operations matters. The wrist was third. It understood.",
        "If the town ever replaces me it'll discover the strange cases have my name memorized. They'll knock anyway. I'll answer. It's the tenure of strangeness.",
        "The practice sign says DOCTOR. The town added the echo. I just keep the door open and the calm dry. Both are load-bearing."),
    "Choksondik" to listOf(
        "Board work done before the bell: the date, the objective, the standard. A day that begins in my handwriting behaves itself out of respect.",
        "A former student sent a child to my corridor just to say the name right to me. The child said it PERFECTLY. The curriculum walks in generations now.",
        "The succession met in the hall: Garrison's chaos, my order, Garrison's chaos with notes. History rhymes in this building and scans badly. I scan it anyway.",
        "Chef and I exchanged the nod of the kept. Lunch was excellent. It always is. Some part of this school refuses to lose what it loves and it's the best part.",
        "Marked forty papers. The red pen is fair: it praises in the margins and corrects in the lines. Like me. The fourth grade knows the code by now.",
        "The facts-of-life alumni are parents now. PARENTS. They cross the street to thank me with strollers. Education's longest echo, rolling toward me daily.",
        "Prepared the unit that made my name — carefully, completely, without a blink. The blink is the students' job. Mine is the straight spine.",
        "If they spell the plaque wrong I'll haunt the unveiling with corrections. A name like mine is a lesson you assign once and grade forever.",
        "Evening: the classroom dark, the board ready for morning, the standard kept. The fourth grade sleeps under my handwriting tonight. It could do worse. It has."),
    "Strong Woman" to listOf(
        "Rounds at dawn: every corridor walked before the first bell. A building knows when it's been inspected by someone who means it.",
        "A kindergartner cried at the door and I carried the morning for her until it fit again. Carrying is the job. The arms are just the tools.",
        "Victoria's assembly ran long. I held the standard at the door like a frame holds a picture. The picture behaved. Frames get no credit. Frames know.",
        "The fourth grade tested the lunch line's banks today. The river held. Rivers don't argue; they just have banks. I am very good banks.",
        "A boy flexed at me again — the same one, bolder. I spotted his form. He'll be strong AND kind, in that order of construction. We work weekly.",
        "Trouble chose paperwork at second period. I signed its form with respect. Growth wears many disguises in a school. I stamp them all.",
        "Public laps, four o'clock, as scheduled. The kids counted my laps like a census. Let them. A standard you can count is a standard you can chase.",
        "Mackey asked how I keep the building calm. I said: I don't keep it calm, I keep it strong. Calm is what strength does at rest. He wrote it down, m'kay and all.",
        "Shoulders back, town. The day is carried by people who decide to carry it. I decided at dawn. I decide again tomorrow. That's the whole secret.")
)

val WAVE6_MORE_DAILY_TOPICS: Map<String, List<String>> = mapOf(
    "Pip" to listOf(
        "Estella wrote from England. Or I imagined she did. The letters I imagine are better written. I answer them anyway, politely.",
        "Ate lunch on the wall like the old days. The wall has seen my whole American career. It has never once commented. Perfect friend.",
        "Someone finally asked where in England. I said London, and the whole table went 'oooh' like it was a spell. It kind of is.",
        "Kept my chin up through three separate indignities before noon. The chin is trained. The chin is English.",
        "Dougie asked what a crumpet is. I described it for ten minutes. He now believes in crumpets the way he believes in the cape.",
        "The new kid's newer than me and everyone knows it. Seniority, at last. It feels like a small, warm country.",
        "Read the graveyard chapter again. The convict still frightens me and I still feed him. Some stories you don't fix; you just pack extra.",
        "Manners aren't weakness. Manners are a sword you carry in a teacup. I've decided to put that on my tombstone. In pencil. I'm nine.",
        "Rain today. Proper rain. I stood in it a moment longer than needed, for England."),
    "Dougie" to listOf(
        "Guarded the classroom door at dismissal. Nobody assigned it. A general sees a door and understands.",
        "Butters got grounded again. I sent supplies through the fence. The supplies were juice. The fence is our border. Juice crosses borders.",
        "My cape got caught on the fence and I played it off as a maneuver. Disarray has maneuvers for everything. It's mostly falling, planned.",
        "Teacher says my G-swan is ready for the wall. The WALL. Second grade's highest honor. I told Butters first. Obviously.",
        "Two fourth-graders asked the way to the office and I SALUTED directions. Professionalism is a habit, like snacks.",
        "Counted the troops at lunch: me, Butters, and a kindergartner who wandered over. The army grows by wandering. We fed her.",
        "The lisp got 'specifically' right today, first try. Specifically! I said it six more times. The word fears me now.",
        "If the cape ever gets its own cape, I'll be a general with staff. Goals matter. Mine are small and wearable.",
        "Stood my post in the rain because the post was there. Butters brought the umbrella. That's not in the manual. That's better than the manual."),
    "Jason" to listOf(
        "Subbed into two different recess games in one lunch. Versatility is just attendance with range.",
        "Crystal beat me at HORSE and told the whole table. Good. A record you hide from isn't a record. Rematch Thursday.",
        "The league needs a trophy. I proposed a painted rock. The rock is in committee. Craig's table has the rock.",
        "Dad asked about my day and I gave him stats. He asked about my feelings and I gave him better stats. We're working on it.",
        "Photo day strategy: middle row, eyes open, ball under the arm if they'll allow it. They never allow it. I ask anyway.",
        "Terrance Mephesto watched our game like it owed him data. I played harder. Rivals in the stands are just scouts with worse snacks.",
        "My sister says I talk about sports at dinner like it's news. It IS news. There's a desk for it at school now — Pusslicker's old one, apparently.",
        "Lost a tooth at practice. Coach says that's tuition. The fairy pays better than the league. Both are fine careers.",
        "If they ever cut me from a team I'll still bring the ball. The ball was never the team's. The ball is the town's. I just carry it."),
    "Terrance Mephesto" to listOf(
        "Father asked about school. I gave him the abstract, the methods, and the results. He nodded like a peer reviewer. Highest praise available.",
        "Nathan's notebook habit is derivative. I note this in MY notebook, which predates his by heredity.",
        "A classmate asked what the ranch grows. I said 'expectations.' Father would have said something with more teeth. I'm the diplomat of the family.",
        "Bill and Fosse requested a scheme for the talent show. I declined. Some stages are beneath strategy. I'll simply win it instead.",
        "Kept the light off my notebook in the cafeteria. Jenny Simons noticed the technique and minuted it. The school's powers recognize each other.",
        "The pig is retired, the elephant is history, and I am neither. Patience is the only clone I need: today's rival, duplicated into tomorrow's.",
        "Someone in the corridor hummed the Canadian anthem at me. I let it pass. A Mephesto chooses his corrections. I corrected him in writing.",
        "Reviewed the election of the kindergartners for technique. The Larsen vote was chaos; the Anderson machine was sound. Data is data.",
        "Father's latest creation asked me for a name today. I suggested it earn one. The ranch applauded. It has four hands. It can spare them."),
    "Jenny" to listOf(
        "The list survives another week unchallenged. Institutions are just scandals that learned filing.",
        "Wendy quoted my footnote in a debate today. MY footnote. The record is winning arguments now. I've peaked at ten. Everything after is maintenance.",
        "A boy asked if he's on the list. I told him the list is not a mirror. He stood taller anyway. Governance improves posture.",
        "Sophie's bureau filed on the cafeteria's tater tot situation. I seconded the report. The tots' lobby is formidable and fried.",
        "Archived third grade's lists today. Found my own handwriting at age eight: already marginal, already certain. Growth is a rumor; I was always like this.",
        "The committee's seal is a sticker. I chose it. Wendy approved it. Bebe wears it best. Statecraft is knowing these things in order.",
        "Filmore Anderson petitioned the committee for a recount of something. A kindergartner. The machine is learning. I minuted my respect.",
        "Checked the archive's locks. The locks are also stickers. Security theater is still theater, and theater is still order.",
        "If the school ever burns its records, mine go last. I keep copies of the copies. A constitution with backups is a constitution that sleeps."),
    "Flora" to listOf(
        "Voted on the class snack today. I chose the crackers. Everyone cheered. Choosing gets easier when you practice on the small ones.",
        "Filmore said my vote made Ike king. I said it made him president. Kings are for stories; presidents nap at nap time like everyone.",
        "Quaid let me hold the sign for one whole minute. It's heavier than it looks. Believing is heavy too. I do both now, a little.",
        "Millie walked me to class even though her class is upstairs. Big sisters take the long way on purpose. I know. I'll do it someday.",
        "I decided the class plant's name. It's Gerald. Not after anyone. Plants get names too and his is Gerald and he likes it.",
        "Ike kicked the ball to me at recess. THE ball. I kicked it back a little crooked. He cheered anyway. That's why he won.",
        "The teacher read my drawing's story back to the class. I wrote it with pictures. She got every part right. Teachers are magic that way.",
        "Some days deciding is hard and I sit with it. Mom says sitting with it IS deciding, the slow part. My slow part is getting faster.",
        "Shared my blanket at nap time with a girl who forgot hers. The blanket is big enough. Most things are, if you arrange them right."),
    "Filmore" to listOf(
        "The squirrels have shifted. I feel it. Two undecideds broke for me at the fence today. The fence is the race now.",
        "Drafted the victory speech and the noble-loss speech. Both are excellent. A statesman prepares for history in both directions.",
        "Flora says I'm a good loser. I informed her I'm a great APPEALS PROCESS. She gave me a cracker. Diplomacy is exhausting and delicious.",
        "Quaid suggested a bigger sign. I authorized a bigger sign. This is how nations grow: sign by sign, believer by believer.",
        "The teacher says kindergarten has no president. Then why, I ask the record, did we VOTE? The record is silent. The record is complicit.",
        "Ike's administration lost the class plant's name vote to Flora. Even HIS bloc follows her. She's not a voter; she's a weather system.",
        "Campaign finance report: I am rich in crackers and poor in justice. Both are Flora's fault. Both are also her credit. Politics is complicated.",
        "Mom washed my good shirt. The campaign shirt. Tomorrow I address the fence in the good shirt. History dresses well.",
        "If I must lose, I'll lose like a lighthouse: standing, lit, and useful to ships. Quaid wrote that down. It's in the charts now. It's canon."),
    "Quaid" to listOf(
        "Made sign number three today. The letters are straighter. Filmore says straight letters are a platform. I believe him. It's my whole job.",
        "Filmore practiced his speech on me six times. I clapped at the same parts so he knows where the good parts are. That's staff work.",
        "Ike kicked the ball with us today. I reported it as 'the administration reaches out.' Filmore put a star by it in the charts.",
        "My arms are stronger than in September. Holding is training. Believing is training. I'm the most trained kid in kindergarten and nobody tests it.",
        "Flora shared her blanket at nap time and I didn't even need it. Kindness doesn't check if you need it. I put that in my own chart.",
        "If Filmore forgets his crackers I have his crackers. If I forget mine, Flora has everyone's. The system works. The system is lunch.",
        "A fourth-grader asked who's Filmore. I said 'the next president of kindergarten, retroactively.' She wrote it down. PRESS.",
        "Some kids want to BE Filmore. I just want to stand next to him. It's the best spot in the school and I found it first.",
        "Dreamed the big sign again. It's as tall as the school in the dream. I hold it anyway. In dreams my arms never get tired."),
    "Mimsy" to listOf(
        "Carried the operation's equipment to the park. Nathan walked ahead with the notebook. The notebook is light. The equipment trusts me.",
        "Timmy waved at me today. The ENEMY waved. I reported it. Nathan says waving is phase one of something. I waved back in phase zero.",
        "Practiced standing still and looking like a plan is happening. A teacher asked if I was lost. I was found, actually. By the plan.",
        "Nathan let me hold the notebook for nine seconds. It's mostly arrows. The arrows point at Jimmy. Some point at lunch. All point somewhere.",
        "Flora shared crackers with the operation today. I logged them as supplies and thanked her in my official voice. She said 'anytime.' Anytime!",
        "If I ever get promoted, I want the title to stay Mimsy. Nathan says henchman IS the title. Then I'm already promoted. Best day.",
        "Held the umbrella over Nathan in the rain and stood in the rain myself. That's not in the plans. That's just where the umbrella reaches.",
        "A kid asked what I do for Nathan. I said 'everything else.' He said 'oh.' The 'oh' had respect in it. I heard it.",
        "The plan failed at recess and Nathan called it a rehearsal, so I rehearsed carrying everything back. I'm ready for the real failure now."),
    "Tricia" to listOf(
        "Craig said a full sentence at dinner. About Tweek. The table observed a moment of silence for the occasion. Then Mom asked for the salt.",
        "The list grows: denials, volume two, page nine. This one involves the pond and a frog. Craig's face when I read the title aloud: priceless.",
        "Stripe performed his wheel routine for the family. Craig applauded. Dad applauded. I scored it an 8.5. Standards are kindness.",
        "A boy in Craig's gang asked why I don't flip people off. I said one Tucker per generation is the treaty. He accepted it as law.",
        "Mom braided my hair and told me about Craig at my age. Apparently he was the same, but smaller and with fewer witnesses. I am the witness era.",
        "Kept the remote through two appeals and a bribe attempt (one cookie). The court is unbought. The cookie was eaten by the court. Both can be true.",
        "Ike's little? No — I'm the same plan as Karen McCormick: youngest, watching, remembering. The littlest sisters of this town should unionize.",
        "Dad grounded Craig for the finger at breakfast. Craig accepted it like weather. In this house, grounding is just indoors with opinions.",
        "When people ask about my brother I say: he's Craig. The ones who know, nod. The ones who don't, learn. It's an efficient system.")
)


val WAVE6_MORE_DAILY_TOPICS_2: Map<String, List<String>> = mapOf(
    "Mr. Slave" to listOf(
        "Did the feed rounds before Al was up. The strays eat in silence with me and in opera with him. Both services are full.",
        "A goat escaped into the neighbor's yard and came back with a ribbon. Al promotes initiative. I retrieve it. The marriage in one chore.",
        "Someone at the store asked if I'm 'the quiet one.' I laughed in the register that rattles carts. Al's the sparkle; I'm the thunder's lower half.",
        "Garrison's name came up at coffee and I let it pass like weather. Twenty years of patience is just manners with a long memory.",
        "The sanctuary took in a cat who screams at dawn. Al named her Audition. The sanctuary is a democracy of one and he's it.",
        "Leather's polished, boots are right, vows are current. A man should maintain all three like machinery.",
        "Held the ladder while Al hung the new sign. He directs from the ground in exclamation points. I hold. We've built a life on this division.",
        "A child asked why I talk low. I said so the animals hear me first. She considered it the best answer of her week. So did I.",
        "Evening porch: Al sparkling at the sunset like it owes him money, me beside him, bass line steady. File under: the good years."),
    "Linda Black" to listOf(
        "Tolkien left his plate in the sink again. I left it there too. A mother's silence is a curriculum and he's a bright boy.",
        "Hosted three of Tolkien's friends for dinner. The grocery list for this house should be filed at the county as infrastructure.",
        "Steve rehearsed Sunday's roast on a Wednesday chicken. Tolkien took notes. The notes were kind. The chicken was a draft and knew it.",
        "A neighbor asked how the Blacks stay so calm. I said: volume control. We simply own one and use it.",
        "Tolkien asked my opinion of his friend's plan and I gave him the real one, at half volume, with pie. That's the whole Black method.",
        "The welcome mat is not a metaphor at this address. It's a budget line. Guests are a utility we refuse to cut.",
        "Called my own mother today. The welcome runs downhill in this family — she invented it, I just keep the porch swept.",
        "Steve and I danced in the kitchen while the roast rested. Tolkien filmed it 'for the archive.' Let him. The archive is loved.",
        "If the town ever asks what the Blacks contribute, the answer is a table. Everything else — the watch, the roast — is garnish."),
    "Harriet" to listOf(
        "Knocked, praised, was berated in iambics. Left the cocoa outside the door anyway. It was gone by morning. Poetry drinks cocoa. Noted.",
        "Bradley asked if he's adopted like it's a riddle now. I said he's chosen like it's a fact. It is the family's favorite fact.",
        "The eyeliner invoice arrived. My husband opened it, sat down, and opened the nail-polish invoice for comfort. We're solvent in love only.",
        "Henrietta read at the school thing. SOMEONE's daughter filled a gym with thunder. I cried in the third row like a civilian.",
        "Camp brochures arrive in the mail like ghosts. I recycle them unread. The family has closed that chapter and footnoted it: never again by mail.",
        "Found a poem on the fridge. Unsigned. About a roof. I have framed worse news than this. I have never framed better.",
        "Bradley's teacher says he 'glows with questions.' The hospital switched-him jokes write themselves and we burn them all. He glows. Full stop.",
        "Praising first is a discipline like any other. Some days the door barely opens. I praise through the gap. The gap is enough. The gap is growing.",
        "Two kids, one roof, infinite volume. The neighbors complain about the poetry and stay for it. Everyone stays for it. It's very good poetry."),
    "Mr. Biggle" to listOf(
        "Read the new poems at midnight, at the kitchen table, with the serious face. Somewhere in poem four the roof got mentioned. Sat a while after.",
        "Fixed the gutters. A foundation's work is invisible until it rains. It rained. The house stayed dry. I'll take the metaphor and the dry house.",
        "Harriet praised through the door for six minutes today. Six. The girl's walls are load-bearing but the sound gets in. It all gets in eventually.",
        "Bradley asked to help with the repairs. Handed him the small wrench. He held it like a scepter. Adopted, chosen, and now: apprenticed.",
        "The camp sent a survey. I filled it in with the care of a man defusing something. 'Would not recommend,' in my best handwriting.",
        "My daughter's thunder is going to be famous, and fame will ask about her father, and the record will say: he read every word. Early. Twice.",
        "Bought the good paper for Henrietta's poems. She pretends not to care. The poems moved to the good paper within the hour. Procurement wins again.",
        "Foundations get no poems. Foundations get dry houses, paid invoices, and a daughter who writes 'the roof' like a psalm. Sufficient. More than sufficient.",
        "Harriet knocked, praised, retreated. I did the dishes in the quiet she defends. The marriage is a two-man job and we're both crew."),
    "Mr. Turner" to listOf(
        "Heidi asked for the car in six years. I have begun the six-year briefing. There will be diagrams. There will be a perimeter ON the diagrams.",
        "Negotiated curfew round two. She brought her mother as counsel. I brought the perimeter. Settlement: fifteen minutes and a text. Both sides claim victory.",
        "A boy looked at Heidi at the mall. The look was respectful. I allowed the look. Nancy says my growth is 'visible from space.'",
        "The gate's bell is tuned. A father hears the gate in his sleep. I hear it in other people's sleep. It's a whole sense now.",
        "Arm-wrestled the fire chief at the picnic and let the town wonder. Valmer knows. I know. The record is sealed for morale.",
        "Heidi's report card has one B and she showed ME first. Me. The perimeter. Because she knew the perimeter would say 'good, now the A.' It did. With a hug after.",
        "Threat assessment: the school dance. Likelihood: high. Chaperone volunteers: me, twice, in writing. Nancy has hidden one application. There were three.",
        "Three versions of me, the town says. Let me save everyone time: all three versions check the locks. Coincidence is just consistency with witnesses.",
        "A daughter safe is a world built right. Everything else I lift is practice for the one weight that matters."),
    "Nancy" to listOf(
        "Talked Thomas down from DEFCON Dad to Movie Dad in under an hour. A personal best. The popcorn helped. Popcorn is my whole method.",
        "Heidi and I did the mall at full Turner-girl speed. He briefed us twice, we ignored him twice, we brought him a pretzel. Diplomacy.",
        "The slumber-party rules got quoted back to me by a grown woman at the store. LEGEND confirmed in the wild. I signed her receipt.",
        "A gate should sing a little when it opens. Ours does. Thomas calls it a security feature. I call it the welcome. Both of us are right, loudly.",
        "Heidi asked if she's overprotected. I said she's over-LOVED with security characteristics. She laughed so hard the perimeter relaxed. My work here is inherited.",
        "My name came from a stick game, the children say. Fine. Sticks are honest. So am I. It checks out.",
        "Hosted the mothers' table today. The Turner report: the fortress has a cookie jar now. Morale is a strategy too.",
        "Thomas checked the locks; I checked on Thomas. Every fortress needs a keeper of the keeper. It's keepers all the way down and I'm the bottom one.",
        "If Heidi grows up fearless it'll be because the gate was strong AND it opened. I work the hinge. It's the most important job at this address."),
    "Mr. Stevens" to listOf(
        "Signed the forms, attended the gate, held the umbrella. A Stevens man reports for the small duties like they're the big ones. They are.",
        "Bebe debuted a new walk today. Her mother rated it. I rated the confidence underneath it, which is the part that's mine.",
        "The list of my wife's lists is itself now a list. I keep it in the glovebox. Item one: check the glovebox. This family is a bureaucracy of love.",
        "A dad at the park asked my secret. I said: show up, sign everything, let the glamour be theirs. He wrote it down. The Stevens standard spreads.",
        "Sat through a fashion presentation at dinner with full attention and two questions. The questions were good. Bebe said so. Growth is possible at my age.",
        "Ballast doesn't get speeches. Ballast gets the ship home. I read that somewhere. I am somewhere between proud and seasick about it.",
        "The umbrella I hold fits three. Me, her, and whoever Bebe is becoming. The umbrella is the biggest one they sell. Planning is love too.",
        "Retired to the quiet chair at nine with the paper and the house humming at glamour-rest around me. A Stevens evening. Sufficient.",
        "If the town ever thanks the quiet dads, we'll be at the gate anyway, holding something. Come find us there. We'll sign for it."),
    "Mrs. Stevens" to listOf(
        "Audited the season's looks at the mall. Half passed. Bebe consults now; the torch is carried at a devastating angle.",
        "A girl asked Bebe where she gets 'it.' Bebe said 'home.' I have never been prouder of a one-word review.",
        "Posture check at the family table: flawless. Even the dog sits straighter in this house. Standards are weather; you live in them.",
        "The playset era tried to recruit me once, at a luncheon. I smiled the smile that ends product lines. It ended. Bebe heard the story and glowed.",
        "An entrance should land like a sentence with good punctuation. Today I entered the bakery like an exclamation. The baker respects it.",
        "My husband held the umbrella for twenty minutes in no rain because the FORECAST said rain. That's the Stevens marriage: he's the forecast, I'm the weather.",
        "Taught Bebe the difference between arriving and entering in one car ride. She practiced on the grocery store. The store will recover.",
        "Glamour is not vanity. Glamour is maintenance of the self in public. I maintain. Bebe maintains. The town is our mirror and it behaves.",
        "If they ask where Bebe's confidence comes from, the answer has a name and it's mine. I lend it to her at full interest of pride."),
    "Mr. Stoley" to listOf(
        "Garage inventory: two costumes, one wig, zero regrets. The next production is already in pre-production. Kevin's twenties will be handled.",
        "The actor sent a card. OUR actor. 'Thanks for the strangest role of my career.' I framed it next to Kevin's report cards. Same wall. Same pride.",
        "Some fathers build a treehouse. I built a cautionary tale with a speaking part. Kevin climbed both, in his way.",
        "Rebranding the family was my wife's idea and my execution. Brooks is a draft; Stoley is the published edition. Kevin is the bestseller.",
        "A dad at coffee called the scheme 'elaborate.' I corrected him: 'thorough.' Elaborate is what you call love you didn't plan.",
        "Kevin asked about his childhood today — a normal question, a normal answer, a normal son. The production's reviews are in and they're a normal son.",
        "Props depreciate. Lessons appreciate. The garage is a bank and I'm its happiest investor.",
        "If Kevin ever produces a scheme of his own, I'll know the theater held. I'll also check the garage. Blood tells.",
        "Phase four of fatherhood is just watching, apparently. I watch Kevin be steady and I take the note every father takes: it worked, it worked, it worked."),
    "Mrs. Stoley" to listOf(
        "Directed dinner conversation away from the scheme and toward Kevin's day in one move. A director's whole art is the redirect.",
        "The actor's card came. I cried at the kitchen sink like a woman at a premiere. We MADE that performance. We made the boy it protected, too.",
        "People rehearse the judgment until they meet Kevin. Then they say 'oh.' The 'oh' is my favorite review. I've collected years of them.",
        "A performance believed teaches once. I check on the belief like a garden: Kevin still tells the story straight. The garden holds.",
        "My husband drafts in pencil because I make him. Love should be erasable where it's wrong and permanent where it's right.",
        "Kevin brought a friend home and the friend said our house feels 'produced.' Kevin said 'thanks.' He KNOWS. He's always known more than the script.",
        "If there's a sequel, it's Kevin's to write. I'll direct if asked. I'll applaud if not. Both are the same job at different volumes.",
        "The heart was never the fright. I say it at coffee and the mothers nod. The heart was showing him a future and saying: not that one, darling. This one.",
        "Evening dailies with my husband: the boy, the day, the small mercies. We've reviewed ten thousand evenings. The series is a hit at this address."),
    "Clark" to listOf(
        "The remote and I have reached an accord. I hold it on weekdays. Scott holds it when he's earned it. Democracy, Malkinson style.",
        "Sophie's family has the Scottsdale bundle — twelve tiers! I sent Gary a sympathy card. Dads of the cable wars recognize each other.",
        "Scott's pump beeped at dinner and he handled it like a pilot. My boy flies a harder route than I ever did and complains less than I did about channels.",
        "Fixed the neighbor's antenna as a peace offering to the decade. Three channels, crystal clear. He cried a little. So did the picture.",
        "The documentary people never called back. Fine. The principle doesn't need distribution. It has a son who saw it.",
        "Ellen and I watched one show, together, on purpose. One. The treaty holds because the treaty is the point.",
        "A streaming truck drove past and I felt nothing. Growth. Then it parked at the Petersons' and I felt everything. Growth is local.",
        "Scott asked if I'd fight the bill again at his age. I said the bill changes, the fighting's hereditary. He laughed with the lisp and I heard my whole life in it.",
        "Three channels raised me. Four hundred couldn't raise a decision. My boy decides. That's the whole inheritance, and it's enough."),
    "Maggie" to listOf(
        "Harrison paced about the marquee case at 2 a.m. I made the tea at 2 a.m. The force's night shift has a kitchen and it's mine.",
        "Mitch sent over the files with a note: 'For the appeal court.' I am the appeal court. The note is framed by the kettle now.",
        "He said 'the town needs certainty' at breakfast like a man reading his own statue. I buttered his toast. Statues need breakfast too.",
        "The Jeffersons year I keep like a medal: the year I said 'stay' and the whole department's future stayed with him. No regrets on file.",
        "Worry is just love with a night shift. I clock in when the town clocks out. The sergeant sleeps because someone stays up being sure for him.",
        "A young officer asked my secret at the picnic. I said: believe at full volume and feed at regular intervals. He wrote it on a napkin. The force learns.",
        "Harrison debriefed the ducks theory over dinner, gravely. I asked one question and the theory confessed itself a duck. This is my whole method.",
        "If the department ever statues him, I'll insist the statue's base say 'and Maggie, who kept him.' The base is the truest part of any monument.",
        "The kettle is on. The door is his. The certainty will walk in loud and leave fed. Twenty years of this and I'd sign again tomorrow."),
    "Mitch" to listOf(
        "Stamped the day's theories by five. One held up. The stamp doesn't judge; it just remembers which ones the town believed.",
        "Yates ran the marquee theory up the flagpole at roll call. I saluted, filed, and quietly interviewed the ladder. The ladder's clean.",
        "Barbrady's report used the word 'thingy' four times. I translated under deadline. The thingy was a ladder, a banner, a duck, and justice.",
        "A rookie asked why I never take the lead. I said the lead gets the statue and the aide gets the ending. I like endings.",
        "The files go back to before the crime-stoppers were tall. I keep them in order so the town can be out of order safely.",
        "Maggie sent soup to the station with a note for 'the appeal court.' The whole shift ate the verdict. Best ruling of the month.",
        "Certainty knocked a door down in the Jeffersons year. I held the warrant and the flashlight. That's the department in one image, and I keep it straight.",
        "Verified three alibis and a rumor before lunch. The rumor was the only guilty party. It's in custody now. Custody is a drawer.",
        "If the sergeant is the town's exclamation point, I'm the period that lets the sentence end. Punctuation is a kind of law too."),
    "Bob Johnson" to listOf(
        "Drafted the snow-route memo. The mayor will announce it Thursday as her 'long-standing snow vision.' I've started dating my drafts for the historians.",
        "Janson brought me coffee and a question about Ted. I told him the whole Ted file. Aides inherit each other. It's the only promotion we get.",
        "The park my idea built has a bench with no name on it. I sit on it Saturdays. The town sits with me. The arrangement is honest even if the credit isn't.",
        "Thirty years of minutes and my handwriting has outlasted three mayors' signatures — well, one mayor, thrice re-elected on my punctuation.",
        "McDaniels gavelled my transit report into law today without looking up. She doesn't steal out of malice. Habit is just theft with seniority.",
        "A citizen thanked the mayor for the crosswalk. I painted the crosswalk proposal in '11. I accepted the thanks silently, in my heart, at full volume.",
        "If the town names anything for me posthumously, I've requested a bench. Benches are the aide's natural monument: useful, sat on, essential.",
        "Wrote the mayor's speech for the fair. She'll call it 'remarks I prepared.' I prepared the remarks. We both know. The knowing is the salary.",
        "Another idea is already in the notebook. There is always another idea in the notebook. That's not optimism. That's inventory."),
    "Pusslicker" to listOf(
        "Ran the evening desk in my head during the whole grocery trip. The produce report was grave. The town deserves the grave version.",
        "Thompson sent a card on my anniversary at the station. 'The chair leans your way.' Class. The new ones can be classy. I allow it.",
        "Covered the fair's weigh-in like a moon landing. A 47-pound pumpkin is a moon landing for somebody. Journalism is knowing who.",
        "The pause before the headline is where the town holds its breath. I've held this town's breath for a decade. It trusts me with the exhale.",
        "A kid asked if I'm 'the news grandpa.' I corrected him on camera two's behalf. Grandpa is fine. The news part is the honor.",
        "The desk has a dent for breaking news and a dent for Randy news. Randy's is deeper. Twenty years of one man is a desk's memoir.",
        "Voice's holding. Tie's straight. The town's worst days all have my narration on them, and I made sure it was kind where kind was true.",
        "Retirement is a word for people whose chair doesn't remember them. Mine does. We have an arrangement: I sit, it stands by.",
        "If the town ever turns the station into a museum, the desk goes in first. I'll narrate the opening. There's no one else I'd trust."),
    "Dr. Doctor" to listOf(
        "Set a collarbone, calmed a mother, and billed one Randy before noon. The twentieth year runs like the first: the town first, the paperwork after.",
        "Gauche took a case I would've kept. Fine young doctor. The strange ones still find me, though. Strange knows strange's address.",
        "A child asked if my name is a joke the town played. I said it's a joke I played first, which makes it a title. He saluted. Smart child.",
        "Hell's Pass at dusk still looks like a dare. I've answered the dare ten thousand times. The building and I have an understanding.",
        "Charted a fever that turned out to be a chili cook-off. Documented it fully. Future doctors must know what October does here.",
        "Delivered a baby at 3 a.m. who screamed like the town itself. Named the hour after her in my private log. The log is twenty years thick.",
        "Patients want 'seen it before.' I've seen a man inhale a harmonica. Musically. This town cannot surprise me; it can only audition.",
        "The practice's sign just says DOCTOR. Twice was the town's idea. Once is enough for the sign and both are true on the door.",
        "Retirement would leave the town to the young and the strange to the unprepared. I'll keep the strange. It's my whole tenure."),
    "Choksondik" to listOf(
        "Graded the day's papers with the red pen of justice. The fourth grade writes like it talks: loudly, and with one good idea per page.",
        "A former student — a GROWN one — crossed the street to thank me for the unit. They all cross eventually. The curriculum walks with them for life.",
        "Garrison's lesson plans blew past my window like tumbleweed with a puppet. I noted the date. The line of succession has a memory and it's me.",
        "They say my name carefully in this town, the way you handle anything that once had authority. Good. Careful is a form of respect.",
        "Prepared tomorrow's board work tonight. The board must greet them like a fact: unavoidable, in my handwriting, already true.",
        "Chef nodded at me in the corridor like one kept-dead professional to another. The school keeps what it loves. It loves lunch and it loved my fourth period.",
        "The facts of life deserve a straight spine and a level voice. I gave them both. A generation walks taller for it and slightly pinker. Acceptable.",
        "My classroom had rules you could set a watch by. The watch is the town's now — it runs a minute fast, out of respect.",
        "If the board ever names a wing for me they'll spell it wrong on the plaque. I've made my peace. The name was always the first lesson."),
    "Strong Woman" to listOf(
        "Did the corridor rounds at dawn. Every door opened easier after. Presence is just maintenance you do with your whole self.",
        "A boy asked if I'm stronger than his dad. I said strength is a practice, not a contest, and spotted him on the monkey bars of the question.",
        "Victoria scheduled the assembly; I carried the standard into it. Between the clipboard and the door, this school has a government.",
        "The lunch line moved like a river today. Rivers don't need shouting. They need banks. I am the banks.",
        "Carried the lost-and-found bin, the banner, and one crying kindergartner's entire morning back to steady. All before nine. It's called rounds.",
        "A girl in the fourth grade flexed at me in the hall. I flexed back, smaller, kinder. She walked off ten feet tall. That's the job description.",
        "Trouble read the room at third period and chose paperwork instead. Paperwork is trouble growing up. I encourage the growth.",
        "My laps at the park are public on purpose. A standard the kids can watch is a standard they can reach. Reach is the whole curriculum.",
        "Shoulders back is not about shoulders. It's about deciding you're worth the room you take. I teach it by taking mine, daily, kindly.")
)


// ------------- wave 6: friend greetings -------------
val WAVE6_FRIEND_GREETINGS: Map<String, Map<Int, List<String>>> = mapOf(
    "Pip" to mapOf(
        0 to listOf("Oh! A visitor! Do come in — Pip Pirrip. You'll forgive the shoes by the door; a gentleman's house has systems.", "Hello! You're the new child everyone's... well. I was the new child once. The post is yours now. Tea helps. Tea is in the post now."),
        1 to listOf("Back again! Splendid. I kept the good cups out — one learns to hope in crockery.", "My friend returns! The wall was asking after you. The wall and I are close. Sit, sit."),
        2 to listOf("My dearest friend in this entire hemisphere! The sandwiches are cut, the kettle is on, and the manners are OFF — for you, the full unbuttoned Pip.", "Best friend! I have decided: if the town buries me again, YOU get the eulogy. Morbid, but it's the highest honor I own. Come in!")),

    "Dougie" to mapOf(
        0 to listOf("Oh— hi! I'm Dougie. Second grade. You're the new kid, right? I know all about being the littlest. It's not tho bad— so bad.", "A visitor! The General receives you. The General is me. The army is mostly Butters. Come in, come in!"),
        1 to listOf("You're back! I saved you the good chair. It's the one that doesn't wobble. Rank has privileges and I share mine.", "My friend! Butters is gonna be tho exthited— excited. We don't get many official visitors at headquarters."),
        2 to listOf("Best friend status: CONFIRMED. You get the cape for a whole minute. A WHOLE minute. Nobody gets the cape, best friend. Nobody.", "There you are! The troops are honored! The troops are me and Butters and we're both saluting on the inside and outside!")),

    "Jason" to mapOf(
        0 to listOf("Jason White. Good to meet you. Whatever team you're forming, I'm in — put me down as 'yes' in every column.", "Hey! New kid! I'm the middle row of every photo. You'll learn to spot me. Come on in, the ball's by the door."),
        1 to listOf("Back for more! The league appreciates regulars. I was just going over the weekend's calls if you want in on the argument.", "My friend returns! Cleats off at the door, stories on at the table. House rules."),
        2 to listOf("Best friend! You get first pick in every draft forever. That's not a small thing in my economy. That's the whole treasury.", "There he is! The photo's middle just shifted — you're in it now. Permanently. Middle row sticks together.")),

    "Terrance Mephesto" to mapOf(
        0 to listOf("Terrance Mephesto. The ranch's Terrance — say it correctly and we begin well. State your interest in the experiment.", "A visitor. How unscheduled. I keep a chair for the unscheduled; a rival studies everyone. Sit. Carefully."),
        1 to listOf("You return. The notebook upgraded you from 'variable' to 'recurring.' Don't let it go to your head; it goes to mine professionally.", "Ah — my recurring appointment. Come in. Bill and Fosse are out. The strategy is ours today."),
        2 to listOf("My friend. There — the notebook has it in the serious ink. If the boys ask, I've never liked anyone. We understand each other.", "Come in, come in. You're the closest thing the ranch has to a peer reviewer. The experiments fear you now. As they should.")),

    "Jenny" to mapOf(
        0 to listOf("Jenny Simons. Co-chair of the list committee. Your file is open and, so far, favorable. Come in.", "Hello. You're the new kid. I minuted your arrival — the minutes were kind. First impressions are my department."),
        1 to listOf("Back again. The committee notes your attendance favorably. Tea? The committee provides tea for the favorably noted.", "My returning footnote — upgraded to a paragraph in the record. Sit. The good chair is for paragraphs."),
        2 to listOf("Best friend. Officially minuted, seconded by me, ratified by the record. If anyone questions it, there's a procedure and I AM the procedure.", "There you are! The archive has a whole section with your name now. Sections are forever. Come in, forever-friend.")),

    "Flora" to mapOf(
        0 to listOf("Hi! I'm Flora! I'm in kindergarten and I can decide things. Do you want a cracker? Deciding is better with crackers.", "A visitor! Millie! MILLIE, a visitor! Sorry. Come in! I voted yes on you coming in. It wasn't close."),
        1 to listOf("You're back! I saved you a cracker and the good spot. The good spot is by me. That's why it's good.", "My friend! I told the class about you. Filmore says you're 'noted.' That means he likes you. I know his words."),
        2 to listOf("BEST friend! You get TWO crackers and the blanket fort. The fort is the best place in the whole house and I decide who. I decided you.", "There you are! If there was a vote for best person you'd win mine. Mine counts double. Nobody knows why. It just does.")),

    "Filmore" to mapOf(
        0 to listOf("Filmore Anderson. Of the election. You may state your allegiance or remain neutral; neutrals are counted as squirrels.", "A constituent! Come in. Quaid, log the visit. The campaign receives all visitors — even Broflovski sympathizers. Especially them."),
        1 to listOf("You return. The charts note consistency in a supporter. Crackers are on the table. The table is bipartisan today.", "My returning voter! Pull up a chair. The cabinet meets at snack. You're in the cabinet. Quaid approved the minutes."),
        2 to listOf("Best friend of the campaign! The BIG sign comes out for you. Quaid has been waiting to hold the big sign for a friend. History is made.", "There you are! If the recount ever seats me, your name is read first in the thanks. First. Before Quaid. Quaid understands. Quaid INSISTED.")),

    "Quaid" to mapOf(
        0 to listOf("I'm Quaid. I hold Filmore's sign. You're a visitor! Visitors are big. I have to tell Filmore. FILMORE! Sorry. Come in!", "Hi! I'm the second chair. It's the best chair. You get the guest cracker — the round one. Round ones are for guests."),
        1 to listOf("You're back! I kept your cracker round. Filmore asked about you. In the charts that's basically a parade.", "My friend! Sit by me. The sign's resting today. Even signs rest. Filmore says so and Filmore's never wrong about signs."),
        2 to listOf("BEST friend! You believed in us — in HIM — and believing is my whole job and now there's two of us doing it. The campaign is SAVED.", "There you are! I made you a little sign. It says your name, mostly forwards. The backwards letters are style. Filmore approved the style.")),

    "Mimsy" to mapOf(
        0 to listOf("I'm Mimsy. I work for— with Nathan. He's not hiring, but visitors are phase zero, so hi. Hi is allowed in phase zero.", "Oh, hello. Nathan's out planning. I'm holding the fort. The fort is heavy but I hold it. It's what I do."),
        1 to listOf("You came back! Nathan said visitors repeat when the operation's interesting. I told him you were nice. He wrote it down, which is like a medal.", "My friend! I saved you the standing spot by the door. It's the best spot. It's where I stand for Nathan. Now it's yours too."),
        2 to listOf("Best friend! Nathan doesn't know I have a best friend slot, but I run the carrying AND the slots. You're in it. It's the warmest slot.", "There you are! I carried a whole bench over so you'd have the good seat. For Nathan I'd carry the piano. For you — the bench AND the piano.")),

    "Tricia" to mapOf(
        0 to listOf("Tricia Tucker. Craig's sister. If he sent you, it's a prank. If he didn't, come in — the house is calmer than the reputation.", "Hi! I'm the Tucker who answers doors in sentences. Mom's out, Dad's calm, Craig's... Craig. Tea?"),
        1 to listOf("Back again! The list notes you kindly — my list, not Jenny's. Mine's the household edition. You're in the good volume.", "My friend returns! Craig almost asked about you. Almost is a Tucker sonnet. Sit, I'll translate the family for you."),
        2 to listOf("Best friend! You get the unabridged guinea-pig archive. NOBODY gets the archive. Craig's whole legend, with the parts he edits out. Come in, quickly, before he counts the pages.", "There you are! Officially my favorite person who isn't related to me by treaty. The remote is yours on Fridays. I've amended the ancient law. Amended!")),

    "Mr. Slave" to mapOf(
        0 to listOf("Well, hello there. Mr. Slave. Al's other half. Mind the strays — they interview guests at the door. You've passed.", "A visitor! Come in, come in. The sanctuary receives everyone. I do the receiving; Al does the sparkling. Division of labor."),
        1 to listOf("Back again. The goats asked about you. Professor asked about you, and Professor is FACULTY. Sit — the good porch chair.", "My returning friend! Al's put the kettle on in your honor. He puts it on for everyone, but the honor is still yours. That's how honor works here."),
        2 to listOf("BEST friend! Al's written a pageant verse about you. There's a verse. There's a costume. I built the stage. Come see your verse.", "There you are, darling. The sanctuary strikes up the band — I'm the band's bass section. Feel the welcome in your ribs. That's us.")),

    "Linda Black" to mapOf(
        0 to listOf("Linda Black. Tolkien's mother. Come in — the Black house doesn't do doorstep conversations. Sit. Eat. Then we talk.", "Hello! You're Tolkien's new friend? He's mentioned you at dinner, with adjectives. Good ones. The door's open."),
        1 to listOf("Back again! Good. Steve's trying a new thing Sunday and you've just become a taster. Rank has privileges; so does returning.", "My friend! The welcome mat was getting ideas above its station — it thinks it runs the house because everyone it greets stays. Sit."),
        2 to listOf("Favourite person! If this house kept a guest book you'd be the spine by now. Come in — Tolkien! Your friend's here; bring the good glasses.", "There you are! Best friend of this house gets the chef's table — Steve's table, in the kitchen, where the real reviews happen. Come.")),

    "Harriet" to mapOf(
        0 to listOf("Harriet Biggle. Come in, mind the poetry — it's on the stairs again. Henrietta files by staircase. We're all very proud and slightly bruised.", "Hello! You're a friend of the house? Bradley's or Henrietta's? Either way: cocoa's on, doors are open, praise is first. House rules."),
        1 to listOf("Back again! The door opened wider for you — Henrietta's door. She pretends it's the hinge. I oil the hinge. We all have our methods.", "My friend! Sit. If thunder starts upstairs, it's just a poem being born. We serve snacks during births now."),
        2 to listOf("BEST friend! You're in a poem. I can't say which, but there's a roof in it and the roof is weeping with pride. Come in, come in!", "There you are! The house's favorite visitor! Even the thunder asks when you're coming. It asks in metaphors, but it asks.")),

    "Mr. Biggle" to mapOf(
        0 to listOf("Mr. Biggle. Come in. Mind the good paper on the stairs — it's hers. Everything's hers. I just hold the house up around it.", "Hello. You're a friend of the kids'? Bradley's easy to find. Henrietta is found when she permits. Tea's on meanwhile."),
        1 to listOf("Back again. Good. Foundations notice returns — we're built on what comes back. Sit. The sturdy chair's yours.", "My friend! Harriet, company's here — the returning kind! The returning kind gets the good cocoa and the short version of the camp story."),
        2 to listOf("Best friend. I'd write it down but writing's her department — I'll fix something in your honor instead. The porch light. It's fixed forever now.", "There you are. If this house had a foundation stone with names on it, yours would be cut deep. Come in. Mind the poems. They're load-bearing too.")),

    "Mr. Turner" to mapOf(
        0 to listOf("Thomas Turner. Heidi's dad. You're expected — the perimeter was briefed. Come in. Mind the bell; it likes you, it just announces everyone.", "State your— sorry. Come in, kid. Nancy says I greet like a checkpoint. Working on it. The gate's open; go on through."),
        1 to listOf("Back again. The report remains good. The perimeter stands down a degree for returning friendlies. Nancy calls it 'the welcome setting.'", "My friend! Heidi's in. The gate sang for you — it only sings for the cleared list. You're cleared, kid. Permanently."),
        2 to listOf("Best friend. The key's under the mat for you — there IS no mat key, but if there were, kid. If there were. Come in; Heidi! Your friend's through the wire!", "There you are! The fortress has a favorite and it's you. Don't tell the perimeter I said 'fortress.' Tell Heidi. She'll translate it to 'Dad's happy.'")),

    "Nancy" to mapOf(
        0 to listOf("Nancy Turner! Heidi's mom. Come in, come in — ignore the bell, it's Thomas's doorbell of vigilance. Tea's on.", "Hello! You're Heidi's friend? The good report one? Then the gate is wide and the kettle is loud. In, in!"),
        1 to listOf("Back again! The gate sang for you — that's Thomas's version of a hug. Mine's an actual hug. Both are on offer.", "My friend! Sit. Heidi's told the whole table about you twice. In this house, twice is a legend forming."),
        2 to listOf("Best friend! The slumber-party rules are waived for you — the HIGHEST honor this house issues, and Thomas co-signed it, in pencil, nervously. Come in!", "There you are! Favorite person! The fortress is a hearth today and you're the reason. Heidi! Your friend's here and Dad's smiling — alert the record!")),

    "Mr. Stevens" to mapOf(
        0 to listOf("Mr. Stevens. Bebe's dad. Come in — mind the umbrella stand; it's seen things. Forms are by the door if life sends any.", "Hello. You're a friend of Bebe's? Then you're expected at some level. The Stevens house runs on expected. Sit."),
        1 to listOf("Back again. Good. The ballast appreciates a returning ship. The good chair's by the window; Bebe's mother picked it for the light.", "My friend! The calendar had you penciled and my wife inked it. Ink, in this house, is a knighthood. Sit, Sir Returning."),
        2 to listOf("Best friend! I'd make a speech but speeches are their department — I'll hold your coat like it's the crown jewels instead. It is, today.", "There you are! The Stevens favorite! My wife has already set your place and Bebe's already rated your entrance in advance. No pressure. Come in.")),

    "Mrs. Stevens" to mapOf(
        0 to listOf("Mrs. Stevens. Bebe's mother. DO come in — and stand tall doing it; first impressions are a Stevens sacrament.", "Hello, darling. You're the new kid? Bebe's mentioned you between seasons. In this house, a mention is a review. Yours was kind."),
        1 to listOf("Back again! The eyebrow approves — I taught it to Bebe and it approves of very little. Sit, sit. Posture, darling.", "My returning favorite! The mothers' table has heard about you. The table is impressed, and the table has buried better gossip than you."),
        2 to listOf("Best friend! If this house gave titles you'd be an ARRIVAL — the highest Stevens honor; it's above 'guest' and below only 'family.' Come in, Arrival.", "There you are! My favorite person who doesn't share my roofline! Bebe! Your friend is here and she entered CORRECTLY. A triumph before tea!")),

    "Mr. Stoley" to mapOf(
        0 to listOf("Mr. Stoley. Kevin's dad. Come in — no, the man by the door is a coat rack. This time. In this house you learn to check.", "Hello! You're Kevin's friend? The steady one's friend. Come in. Everything here is exactly what it seems. Now. It wasn't always. Stories later."),
        1 to listOf("Back again! The production values rise for returning guests — my wife directs the welcome, I fund it. Sit, the good light's yours.", "My friend! Kevin mentioned you at dinner in the present tense AND the future tense. In Stoley, that's a five-star review."),
        2 to listOf("Best friend! You get the garage tour. THE garage. The costumes, the wig, the whole archive of love. Nobody gets the garage, friend. Nobody.", "There you are! If we ever produce again, you're cast — as yourself, in the good scenes. Come in; your mark is taped on the floor. It has been since Tuesday.")),

    "Mrs. Stoley" to mapOf(
        0 to listOf("Mrs. Stoley. Kevin's mother. Come in, darling — the kettle's on and everything in this house is real, including the welcome.", "Hello! You're the new friend? Kevin speaks of you kindly, and Kevin's kindness is a reviewed publication. Sit."),
        1 to listOf("Back again! Good. I set your place before you knocked — a director anticipates entrances. Kevin's in; the day's cast is complete.", "My friend! The dailies on you are all raves, and I direct the dailies. Come in — the good couch has your name in the program."),
        2 to listOf("Best friend! The premiere seat is yours — front, centre, the seat I saved through every production of this family's life. Sit, darling. You've earned the billing.", "There you are! My favourite review in human form! If Kevin's memoir ever opens, you get the first ticket and the last word. Come in!")),

    "Clark" to mapOf(
        0 to listOf("Clark Malkinson. Scott's dad. Come in — mind the antenna wire; it's a monument, not a tripping hazard. Mostly both.", "Hello. You're Scott's friend? The one from school? Come in. The TV's off till dinner. House law. You'll survive; we all did."),
        1 to listOf("Back again! The republic of the remote welcomes its returning citizen. Sit — middle cushion's neutral ground and it's yours.", "My friend! Scott's in. He talks about you at full lisp speed, which in this house is a five-star broadcast."),
        2 to listOf("Best friend! You get the scepter on weekends. THE scepter. Ellen witnesses the transfer. Scott applauds it. This is the highest Malkinson honor.", "There you are! Favorite person! I'd fight a cable company for you and I've only done that for family and principle. You're both now. Come in!")),

    "Maggie" to mapOf(
        0 to listOf("Maggie Yates. The sergeant's wife. Come in — if Harrison's home he'll have questions; if he's out, you'll get tea first. Pray for tea.", "Hello! You're the new kid? The department has a file and I have a kettle. Sit; we'll see which fills first."),
        1 to listOf("Back again! Good. The appeal court is in session and your record's clean. Kettle's on. Harrison's debrief can wait its turn.", "My friend! Mitch sends his regards and the sergeant sends a theory. I send tea. Mine's the only one that's warm. Sit."),
        2 to listOf("Best friend! The kettle has a name for you now. If this house ever statues anyone, I'm putting your name on the base with mine — the base is where the truth lives.", "There you are! My favorite verdict! Harrison! Our friend's here — bring the good file, the cookie one! Come in, darling.")),

    "Mitch" to mapOf(
        0 to listOf("Detective Murphy. Mitch. Come in — mind the files; the files are the furniture's senior partner here.", "Hello. New face gets a new file. Page one says 'seems fine.' Most page ones are right. Sit."),
        1 to listOf("Back again. The file's page two is kinder than page one. They always are, for the ones who return. Kettle's on.", "My friend! The department's memory has you in the good drawer — the one that isn't ducks. Sit."),
        2 to listOf("Best friend. If Yates ever announces a theory about you, I'll verify it personally and stamp the kind version. That's the highest honor the aide-de-camp issues.", "There you are. The flashlight's yours if you ever need the ending of a case. Aides lend the light. Come in.")),

    "Bob Johnson" to mapOf(
        0 to listOf("Bob Johnson. Mayor's aide. Come in — if you have an idea, timestamp it now. Trust me. Timestamp everything.", "Hello! You're the new kid? The council's heard. I've minuted you under 'new resident, promising.' The mayor will announce it as her discovery Thursday."),
        1 to listOf("Back again! The minutes welcome you — page one of today's, under 'the returning citizen.' Sit. The good chair's the one Ted used. It holds people well.", "My friend! Janson, coffee for my friend — Janson's learning. A returning guest at City Hall is a constituent with a story. Let's hear it."),
        2 to listOf("Best friend! The bench, when they build it, has room for your name under mine — same size letters; I've drafted the memo. Come in, come in.", "There you are! My favorite citizen! If the mayor announces you as her idea I'll know she means it as a promotion. She only steals the good ones.")),

    "Pusslicker" to mapOf(
        0 to listOf("Tom Pusslicker, Channel 4 News. Come in — you're off camera here. The house has no camera two. The house IS camera two.", "Good evening — habit. You're the new kid? The desk has a file. The file is favorable. Sit; the rundown can wait."),
        1 to listOf("Back again! The ratings of this house are up. The chair by the window is yours — best light in the building; I checked it like a studio.", "My returning viewer! The evening rundown now includes your visits under 'town, brightening.' Sit."),
        2 to listOf("Best friend! If the museum ever opens, you cut the ribbon with me — the tie in the case will be the second-most honored thing there.", "There you are! My lead story! Come in — the desk at home has a dent for good news and it's shaped exactly like your arrivals.")),

    "Dr. Doctor" to mapOf(
        0 to listOf("Dr. Doctor. Come in — sit. In my house as in my practice: sit first, story second, stitches if the story requires.", "Hello. You're the new kid? The town's brought me newer. You'll do. Sit — the chair's heard twenty years of stories and keeps them all."),
        1 to listOf("Back again. Good — a returning patient is a healthy sign in every chart I keep. The personal ones too. Sit.", "My friend! The house calls went well today, so the doctor is in the parlor like a civilian. Tea's a prescription I write for myself."),
        2 to listOf("Best friend! If the town ever plaques this practice, your name goes under mine — the base is where the truth lives; Maggie Yates taught me that line and she's right.", "There you are! My favorite case — closed, healthy, and visiting socially. The doctor is IN, for you, entirely. Come in.")),

    "Choksondik" to mapOf(
        0 to listOf("Ms. Choksondik. Say it carefully — there. You've passed the first lesson most adults fail. Come in; mind the board work by the door.", "Hello. You're the new pupil everyone's describing. I take my measure standing. Stand tall, then. There. Acceptable. In."),
        1 to listOf("Back again. The returning ones get the margin notes, not just the grade. Sit — the good chair faces the board, as chairs should.", "My repeat visitor! In my classroom, returning was called 'attendance' and it was ninety percent of the grade. You're passing handsomely."),
        2 to listOf("Best friend. I'll write it on the board in the good chalk: my favorite. The board doesn't lie; it never has chalk enough for lies. Come in.", "There you are! Front row, centre — the seat I saved for the ones who come back. Sarah Valmer understands this seat. Sit. You've earned the ink.")),

    "Strong Woman" to mapOf(
        0 to listOf("Strong Woman. Vice-principal. Come in — shoulders back, chin level. There. You already look stronger. That's the whole first lesson.", "Hello. You're the new kid? The building's been briefed. State your visit like you mean it and the door is yours."),
        1 to listOf("Back again. Returning is a standard too, and you keep it. The good chair's by the standard. Sit in it like you carry it.", "My repeat visitor! Victoria runs the schedule; I run the welcome's posture. Yours is excellent. In, in."),
        2 to listOf("Best friend! You get the carry — if you ever need your worst day carried, this house has the arms and the policy. Come in; the standard's at rest for you.", "There you are! My favorite carrier of their own standard! The laps can wait; the visit can't. In, friend. The door holds itself for you."))
)


// ------------- wave 6: response banks -------------
val WAVE6_RESPONSE_BANKS: Map<String, Map<String, List<String>>> = mapOf(
    "Pip" to mapOf(
        "greeting" to listOf("Hello, {newkid}. Pip Pirrip. Mind the step; I mind it for everyone. It's a whole system.", "Oh, hello, {newkid}! A conversation! I was just telling the wall about London. The wall is a wonderful listener but a poor conversationalist."),
        "insult" to listOf("I've been the class target since before you arrived, {newkid}. Your insult is noted, filed, and outclassed by history. Do try the tea instead.", "French? FRENCH? No — you said something else, but my standards heard French. Forgive me, {newkid}. Old wounds, deep manners."),
        "question" to listOf("A fair question, {newkid}. The answer is London, mostly, and then manners for the remainder.", "Ask away, {newkid}. An orphan learns to answer everything: it's how you stay in the story."),
        "bye" to listOf("Goodbye, {newkid}. Mind the puddle on your way — I've minded it for everyone all day; you're in the system now.", "Cheerio, {newkid}. If anyone calls you French, correct them in French. It's the only language they respect."),
        "compliment" to listOf("Well — thank you, {newkid}. Kindness is rarer than good tailoring in this town, and I've had neither often. That goes in the good drawer.", "You're very kind, {newkid}. The surplus doctrine works in compliments too, apparently. Take one back: your timing is impeccable."),
        "default" to listOf("Mmhm, {newkid}. Go on. The wall hears everything; I hear it better.", "Yes, {newkid}, I'm listening. A gentleman listens at full attention, even standing in a corridor.")),
    "Dougie" to mapOf(
        "greeting" to listOf("Hi, {newkid}! Dougie. Thecond— second grade. I hold lines and doors. Mostly doors.", "Oh, hi, {newkid}! You're tall. Fourth grade is tall. I'm catching up at a military pace."),
        "insult" to listOf("That's mean, {newkid}. The General doesn't cry at mean. The General files it. Filed. Also I'm a little bit crying but it's filed.", "Butters says mean words mean the person needs a friend or a snack, {newkid}. I have snacks. Choose wisely."),
        "question" to listOf("Good question, {newkid}. The answer is usually Butters. If it's not Butters, it's snacks. If it's not snacks, ask Butters.", "Ask me, {newkid}. I know the whole lower school and three whole fourth-graders. I'm practically intelligence."),
        "bye" to listOf("Bye, {newkid}! The line holds because of people like you visiting it. Salute! …I saluted. It's fine.", "Later, {newkid}! If you see Butters, tell him the General reports the perimeter friendly. He'll know."),
        "compliment" to listOf("Th— thank you, {newkid}. The cape just grew an inch. Compliments are supplies and you just resupplied the whole army.", "You think so, {newkid}? The troops accept! The troops are thrilled! I'm the troops and I'm thrilled!"),
        "default" to listOf("Mmhm, {newkid}. Go on. I'm on duty but duty includes listening. It's in the manual I wrote.", "Yeah, {newkid}, I hear you. Tell it slow — I take notes in my head and my head has a lisp too.")),
    "Jason" to mapOf(
        "greeting" to listOf("Jason White, {newkid}. Middle row, all teams. What are we playing?", "Hey, {newkid}! Good timing — the league's always forming. You're on the roster now. I keep the roster."),
        "insult" to listOf("Trash talk, {newkid}? Bold. I've been heckled by entire bleachers of parents. Yours needs reps. The field's open Saturday.", "Easy, {newkid}. In sports we settle it with a rematch, not a review. Name the game."),
        "question" to listOf("Good question, {newkid}. The answer's practice. It's always practice. Even for questions.", "Ask me anything about the weekend's calls, {newkid}. I have opinions with footnotes."),
        "bye" to listOf("Later, {newkid}. The photo isn't taken till everyone's in it. You're in it. Don't be late for it.", "Bye, {newkid}! Cleats by the door, stories at the table — come back for either."),
        "compliment" to listOf("Thanks, {newkid}. Fundamentals, they'd say. I'll take it as a stat and a half. You're in the good column of the book.", "Appreciate it, {newkid}. A compliment's just a score you can't appeal. I don't appeal those."),
        "default" to listOf("Mmhm, {newkid}. Go on — I listen like I play: all the way to the whistle.", "Yeah, {newkid}, I'm with you. Tell it at game speed; I keep up.")),
    "Terrance Mephesto" to mapOf(
        "greeting" to listOf("Terrance Mephesto, {newkid}. The ranch's. Not the comedian's. We begin.", "You've entered the lab's orbit, {newkid}. There are rules: observe everything, touch nothing, fear the notebook slightly."),
        "insult" to listOf("An insult, {newkid}? From the cheap seats of my experiment? Noted, dated, and outclassed by my father's creations — and they have too many hands.", "Careful, {newkid}. I've been insulted by a clone with my own face. Your effort is... preliminary."),
        "question" to listOf("A question, {newkid}. The answer depends on clearance. Yours is 'recurring.' Ask, and we'll see what recurring buys.", "Ask, {newkid}. But know that every answer at the ranch comes with a hypothesis attached. Mine is about you."),
        "bye" to listOf("Go, {newkid}. The experiment continues without you, as all good experiments do — that's the definition of a control group.", "Later, {newkid}. If the boys ask about the ranch, tell them it's quiet. Quiet reads as ominous. Let it."),
        "compliment" to listOf("…Noted, {newkid}. The notebook has a page for this. It's titled 'anomalies.' You're the kindest one on it.", "Flattery, catalogued and — reluctantly — believed. A Mephesto trusts data. Yours keeps coming back kind. Suspicious. Keep it up."),
        "default" to listOf("Continue, {newkid}. The notebook is taking this in the serious handwriting — the one with the underlines.", "Mm. Noted, {newkid}. Everything at the ranch is noted. It's the comfort of a documented rivalry.")),
    "Jenny" to mapOf(
        "greeting" to listOf("Jenny Simons, {newkid}. Your file's open. So far it's a good file. I keep the files; I'd know.", "Hello, {newkid}. The committee is not in session, so this is off the record. Cherish it; nothing here is."),
        "insult" to listOf("Noted, {newkid}. Your objection goes in the record as stated: loud, brief, and without a second. The record remains unmoved and better dressed.", "Careful, {newkid}. I've survived a scandal that had COPIES. Your insult has no copies. It will not survive the week."),
        "question" to listOf("Good question, {newkid}. The answer is in the archive — volume two, the honest appendix. I can summarize, but there will be footnotes.", "Ask away, {newkid}. The committee answers everything eventually; it's the school's slowest superpower and its most reliable."),
        "bye" to listOf("Goodbye, {newkid}. This conversation is minuted as favorable. The record thanks you for your attendance.", "Later, {newkid}. Mind the margins on your way out — they're where this school actually lives."),
        "compliment" to listOf("Thank you, {newkid}. Entered into the record without objection. Wendy will audit it later and approve; she always approves the true ones.", "Kindly said, {newkid}. The co-chair accepts, and the minutes will reflect it in the good ink."),
        "default" to listOf("Go on, {newkid}. I'm listening in full — co-chair's ears, archivist's patience.", "Mmhm, {newkid}. Noted in pencil. In this school, pencil is a promotion track.")),
    "Flora" to mapOf(
        "greeting" to listOf("Hi, {newkid}! I'm Flora! Want a cracker? Everybody talks better with a cracker. It's science. My science.", "Hello, {newkid}! You're big! Are you in FOURTH grade? Fourth grade has lists. I have crackers. Both are powerful."),
        "insult" to listOf("That's not nice, {newkid}. I decided you're better than that. My deciding counts double, so now it's true twice. Be better.", "Filmore would give a speech about that, {newkid}. I just think you need a cracker and a sit-down. Both are here."),
        "question" to listOf("Good question, {newkid}! My answer is: I don't know, but I can decide. Deciding is my whole thing. Watch: …crackers.", "Ask me, {newkid}! I know kindergarten ALL the way. Naps, crackers, the good slide. Everything important."),
        "bye" to listOf("Bye, {newkid}! If you see Filmore, tell him the crackers are bipartisan. He knows what it means. He taught me the word.", "Later, {newkid}! Come back at lunch. Lunch is when my table fixes everything."),
        "compliment" to listOf("Thank you, {newkid}! That goes in the good part of the day. The good part is getting BIG today.", "You're nice, {newkid}. I vote we be friends. My votes count. It's official now. Friends!"),
        "default" to listOf("Mmhm, {newkid}. Go on. I listen with my whole chest. It's how I decide.", "Tell me, {newkid}! I have time. Kindergarten time is longer. It's the naps.")),
    "Filmore" to mapOf(
        "greeting" to listOf("Filmore Anderson, {newkid}. State your allegiance or accept a cracker as a neutral. The campaign is generous.", "A constituent! {newkid}, welcome to the headquarters. Quaid, log the hour. The hour is logged. Speak."),
        "insult" to listOf("Slander, {newkid}? The campaign has survived a RE-count. Your insult lacks a quorum. Quaid, note the attempt. Noted and pitied.", "Careful, {newkid}. I have been insulted by an entire administration (the teacher). Yours is recess-league at best."),
        "question" to listOf("A question for the candidate, {newkid}. The answer is snacks, longer recess, and justice — in whichever order the charts require.", "Ask, {newkid}. But know the platform by heart first: it's short, it's crackers, it's coming."),
        "bye" to listOf("Go, {newkid}. The campaign continues — it continues through naps, which is more than the administration can say.", "Later, {newkid}. If Ike asks where I am, tell him: everywhere his administration isn't. Which is the fence. I'm at the fence."),
        "compliment" to listOf("…The campaign accepts your endorsement, {newkid}. Quaid will put it in the charts with a star. Two stars. Don't let it leak; the squirrels are watching.", "Kind words, {newkid}. A candidate starves for them between elections. Yours will be read at the fence. Twice."),
        "default" to listOf("Continue, {newkid}. The record is being kept in duplicate — Quaid's memory and my charts. Both are admissible.", "Mm. Noted, {newkid}. Everything is noted. It's the comfort of a contested election.")),
    "Quaid" to mapOf(
        "greeting" to listOf("Hi, {newkid}! I'm Quaid! I hold the sign! Do you want to see the sign? It's in my bag. It's always in my bag.", "Hello, {newkid}! I'm Filmore's best friend. That's my whole introduction and it's the best one in kindergarten."),
        "insult" to listOf("Hey, {newkid}. That's mean. Filmore says mean is just losing with words. Are you losing? Do you need a cracker? Losing needs crackers.", "I don't fight, {newkid}. I hold. And right now I'm holding my temper AND the sign. Both are heavy. Choose the cracker."),
        "question" to listOf("Good question, {newkid}! The answer is Filmore. It's not always Filmore, but I check with him first. That's the job.", "Ask me, {newkid}! I know everything Filmore knows, one second later. It's a very good system."),
        "bye" to listOf("Bye, {newkid}! If you see a squirrel, tell it the campaign remembers its fence attendance. Squirrels vote with presence.", "Later, {newkid}! The sign and I will be at the fence. It's where history stands. And holds."),
        "compliment" to listOf("Thank you, {newkid}! I'll tell Filmore and he'll put it in the charts, and the charts will be happy, and I'll be happy holding them.", "You think so, {newkid}? Being second is the best job and nobody believes me till they hear it like that. You get a round cracker. The guest kind."),
        "default" to listOf("Mmhm, {newkid}. Go on. I hold conversations too. I'm a holder. It's my whole career.", "Yeah, {newkid}, I'm with you. Tell it all. Filmore's speeches are longer; I can take it.")),
    "Mimsy" to mapOf(
        "greeting" to listOf("Hi, {newkid}. I'm Mimsy. Nathan's— the operation's. You're on the perimeter, which is where I work. Welcome to the perimeter.", "Oh, hello, {newkid}. Nathan's planning. I'm the plan's legs and arms. Mostly arms. The arms carry."),
        "insult" to listOf("Nathan says insults are just intel with bad manners, {newkid}. So thanks for the intel. It's filed. You're filed. Hi.", "I've been insulted by professionals — Jimmy does crowd work — {newkid}. Yours is fine. It's a start. Nathan would say 'rehearsal.'"),
        "question" to listOf("A question, {newkid}. If it's about the plan, I can't say. If it's about carrying, I can carry anything. Ask a carrying question.", "Ask, {newkid}. I know all the phases I've been told, and I've been told phase one completely. It's a good phase."),
        "bye" to listOf("Bye, {newkid}. The operation continues. It continues at lunch especially. The plan loves lunch.", "Later, {newkid}. If the duo asks where I am — you never saw me. If they don't ask, tell them anyway. Nathan says it plants the seed."),
        "compliment" to listOf("Thanks, {newkid}. Nobody compliments the carrying. The carrying noticed. I'll carry that compliment all day; it's light.", "You— thank you, {newkid}. Nathan's notebook has an 'assets' page and compliments aren't on it. I'm starting my own page. You're first."),
        "default" to listOf("Mmhm, {newkid}. Go on. I hold still for stories. It's like holding a door for words.", "Yeah, {newkid}, I'm listening. The perimeter hears everything. It's the quietest loud job in school.")),
    "Tricia" to mapOf(
        "greeting" to listOf("Tricia Tucker, {newkid}. Craig's sister. The one with sentences. Come in if you're coming.", "Hi, {newkid}. If Craig sent you, deny everything. If not — hi. I'm the household's translator and archive."),
        "insult" to listOf("Bold, {newkid}. I've been insulted by a brother whose whole vocabulary is a gesture. Yours has syllables. Points for syllables, none for aim.", "Easy, {newkid}. In this house we flip off insults and move on. Watch: …see? It's hereditary after all. Don't tell Craig."),
        "question" to listOf("Good question, {newkid}. The answer depends which Tucker you ask. I answer in sentences, so you chose well.", "Ask me, {newkid}. I keep the family's records — the denials list alone is a saga. What chapter do you need?"),
        "bye" to listOf("Bye, {newkid}. Tell Craig nothing. He'll find out from the archive eventually; let him have the suspense.", "Later, {newkid}. The remote court's adjourned. Come back Friday if you want to see democracy."),
        "compliment" to listOf("Thanks, {newkid}. That goes in the good volume — not the denials list, the other one. It's thinner and it matters more.", "Kind of you, {newkid}. A Tucker's gratitude is quiet, so I'll say it at Craig volume: thanks. Once. Cherish it."),
        "default" to listOf("Mmhm, {newkid}. Go on. I archive everything; make it worth the shelf.", "Yeah, {newkid}, I'm listening. The witness hears all. It's the family job and I do it in sentences.")),
    "Mr. Slave" to mapOf(
        "greeting" to listOf("Well, hello, {newkid}. Mr. Slave. Al's husband. The strays have approved you at the door; I concur.", "Darling {newkid}! You've come to the right marriage. The sanctuary glows, the bass is warm, and you are WELCOME."),
        "insult" to listOf("Oh, sweetie. I've been insulted by a classroom of fourth-graders with a substitute's blessing. That was a warm-up lap.", "Careful, {newkid}. Insults slide off a well-kept legend. Be kind instead — it carries better in my register."),
        "question" to listOf("A question, {newkid}! The answer is love, logistics, and a feed schedule. Ask the specifics; I have charts in the low voice.", "Ask away, {newkid}. Every answer at the sanctuary starts with a compliment and ends with an adoption form. Fair warning."),
        "bye" to listOf("Goodbye, {newkid}. Walk like the strays are watching — they are, and they grade generously.", "Later, {newkid}! If you hear a small parade of paws, that's just the house saying goodbye at full production."),
        "compliment" to listOf("Well, thank you, {newkid}. Compliments are the sanctuary's second currency. The first is kibble. You're rich in the better one.", "Darling, you're a sweet talker. That goes in the good book, right between the goat's promotion and Al's new verse about you."),
        "default" to listOf("Mmhm, {newkid}, go on. The bass hears everything at full feeling.", "Tell me everything, {newkid}. The sanctuary runs on stories and feed, and we're low on neither.")),
    "Linda Black" to mapOf(
        "greeting" to listOf("Linda Black, {newkid}. Tolkien's mother. Sit — the Black house talks seated, with something warm in hand.", "Hello, {newkid}! You're Tolkien's friend? Then you're expected. The welcome's already on."),
        "insult" to listOf("Easy, {newkid}. This house roasts with love or not at all — Steve's rule in the kitchen, mine everywhere else.", "Careful, {newkid}. I've been insulted at PTA volume by experts. Yours is a whisper with ambitions. Sit; have tea instead."),
        "question" to listOf("Good question, {newkid}. Short answer: a mother knows. Long answer: a mother also hosts, feeds, and follows up. Choose your length.", "Ask me anything, {newkid}. If it's about Tolkien, I have the archive. If it's about the town, I have the welcome network. Either way you're covered."),
        "bye" to listOf("Bye, {newkid}! The porch light's on for you. It's on for everyone, but especially the returning.", "Later, {newkid}! Tell Tolkien his mother said dinner's at six and the roast has improved. He'll know which roast. He has notes."),
        "compliment" to listOf("Well, thank you, {newkid}! That goes in the good drawer — the Black house keeps one too, next to the good glasses.", "Aren't you sweet, {newkid}. The welcome accepts, the kitchen concurs, and Tolkien will hear about it at dinner in my proudest voice."),
        "default" to listOf("Mmhm, {newkid}, go on. I've got tea, time, and a radar for the good parts.", "Tell me everything, {newkid}. A mother's listening is a neighborhood service at this point.")),
    "Harriet" to mapOf(
        "greeting" to listOf("Harriet Biggle, {newkid}. Come in — mind the poem on the stairs; it's drying. Everything here is either drying or thundering.", "Hello, {newkid}! Friend of Bradley's or Henrietta's? The cocoa's the same either way. The volume differs."),
        "insult" to listOf("I've been berated in verse by my own daughter at operatic length, {newkid}. Your insult is a haiku at a poetry slam. Sit; cocoa.", "Easy, {newkid}. In this house we praise first. Even insults get praised first here: bold opening. Now sit."),
        "question" to listOf("Good question, {newkid}. The answer depends which Biggle you need: the roof, the foundation, the thunder, or the glow. Choose.", "Ask me, {newkid}. I know this house's every volume. It's mostly thunder and cocoa. Ask away."),
        "bye" to listOf("Bye, {newkid}! Knock once when you come back. Praise twice. It's the house's whole door protocol.", "Later, {newkid}! If Henrietta asks, you loved the poem on the stairs. It was about stairs. It's always about more than stairs."),
        "compliment" to listOf("Thank you, {newkid}! Praise, returned to the praiser! That goes on the fridge — the fridge is this house's highest honor. Poems live there.", "Aren't you kind, {newkid}. A compliment in this house gets workshopped by a goth and framed by a mother. Yours is framed already."),
        "default" to listOf("Mmhm, {newkid}, go on. I've got cocoa, time, and a roof's patience.", "Tell me, {newkid}. The roof hears everything. It's literally my job in the architecture.")),
    "Mr. Biggle" to mapOf(
        "greeting" to listOf("Mr. Biggle, {newkid}. Come in. The sturdy chair's by the window. Foundations notice chairs.", "Hello, {newkid}. Kids' friend? Bradley's through there; Henrietta permits visitors on a poetic basis. Tea meanwhile."),
        "insult" to listOf("I've been dedicated poems that were less kind, {newkid}, and framed two of them. Yours is prose and it's fine. Sit.", "Easy, {newkid}. This house takes its insults in verse or not at all. House rule. Harriet's rule. The thunder's rule, really."),
        "question" to listOf("Straight answer, {newkid}: most things hold if you maintain them — houses, families, daughters' regard. Maintenance is the whole secret.", "Ask me a house question, {newkid}, I'm your man. Ask me a feelings question, I'll answer like a foundation: slowly, and it holds."),
        "bye" to listOf("Later, {newkid}. Porch light's fixed forever. It's on for you. Foundations keep their lights.", "Bye, {newkid}. Tell Henrietta the good paper's restocked. She'll pretend not to care at volume. That's thanks, in thunder."),
        "compliment" to listOf("…Thanks, {newkid}. Foundations don't get many. It'll hold me up a while, that one.", "Well. Kind of you, {newkid}. I'll put it where I keep the good invoices: the drawer that matters."),
        "default" to listOf("Mmhm, {newkid}. Go on. I listen like I build: all the way down.", "Yeah, {newkid}, I hear you. The quiet ones clock each other. Clocked: a good story.")),
    "Mr. Turner" to mapOf(
        "greeting" to listOf("Thomas Turner, {newkid}. You're briefed and cleared. Come in. The gate likes the cleared.", "Hey, {newkid}. Heidi's dad. State your— come in. Still working on the greeting. Nancy's coaching me. Slowly."),
        "insult" to listOf("Careful, {newkid}. The perimeter has been insulted by professionals — Nancy's book club. Yours registers as a breeze. A noted breeze.", "Easy, {newkid}. In this house we address the dad through the daughter's reports, and her reports have been GOOD. Keep them good."),
        "question" to listOf("Good question, {newkid}. Straight answer: the perimeter is love with a fence around it. Everything else is diagrams.", "Ask me anything about safety, {newkid}. Ask me anything else and you get the safety answer wearing a hat. Fair warning."),
        "bye" to listOf("Later, {newkid}. Text when you arrive. That's the house rule for everyone I— for everyone Heidi cares about. You're on the list.", "Bye, {newkid}. The gate will sing you out. It only sings for family and the cleared. You're both now, apparently."),
        "compliment" to listOf("…Thanks, {newkid}. The perimeter accepts. Heidi will hear you were kind to the fortress. She translates it as 'Dad's soft.' She's not wrong.", "Well. Kind of you, {newkid}. A compliment clears security faster than anything. You're learning the house."),
        "default" to listOf("Mmhm, {newkid}. Go on. The perimeter's listening at full attention. It does that.", "Yeah, {newkid}, I hear you. Brief me like a report and I'll brief you back like a dad. Deal.")),
    "Nancy" to mapOf(
        "greeting" to listOf("Nancy Turner, {newkid}! Heidi's mom. In, in — the kettle's loud and the gate is a welcome with a bell.", "Hello, {newkid}! The good-report friend! Thomas has been briefed; the brief was glowing. Sit."),
        "insult" to listOf("Easy, {newkid}. I negotiate with a fortress for a living. Your insult is a strongly worded letter at best. Tea?", "Careful, {newkid}. The last person who insulted this house got a casserole and a talking-to. The casserole was the warning."),
        "question" to listOf("Good question, {newkid}. The answer is: the gate opens for the loved and the vetted, and you're rapidly becoming both.", "Ask me anything, {newkid}. If it's about Heidi, I have the stories. If it's about Thomas, I have the translations. Either way, hon, you're covered."),
        "bye" to listOf("Bye, {newkid}! The gate will sing — Thomas calls it security; I call it the house saying 'come back.' Both are true.", "Later, {newkid}! Tell Heidi her mother says the hearth is winning. She'll know. The hearth is always winning."),
        "compliment" to listOf("Well, thank you, {newkid}! That goes on the fridge — the fridge is this fortress's hall of fame. Thomas pretends not to check it. He checks it.", "Aren't you sweet, {newkid}. The diplomacy corps accepts, the hearth beams, and the perimeter will hear about it at dinner in my proudest voice."),
        "default" to listOf("Mmhm, {newkid}, go on. I've got tea, time, and a hinge-oiler's patience.", "Tell me everything, {newkid}. The open gate hears the best stories. It's a whole strategy.")),
    "Mr. Stevens" to mapOf(
        "greeting" to listOf("Mr. Stevens, {newkid}. Bebe's dad. Come in — the umbrella stand's by the door; everything here has a place and a person who placed it.", "Hello, {newkid}. Friend of Bebe's? Then you're expected. The Stevens house runs on expected and glamour. I handle the first."),
        "insult" to listOf("Easy, {newkid}. I've survived three fashion eras at close range. Your insult is a mild accessory at best.", "Careful, {newkid}. In this house, insults get reviewed by two Stevens women and a ballast. The ballast votes no. Sit."),
        "question" to listOf("Good question, {newkid}. Short answer's my department: show up, sign, hold the umbrella. Long answer's my wife's and it's better dressed.", "Ask me, {newkid}. If it's about the house, I've fixed it or it's on a list. If it's about feelings, the umbrella and I are a team."),
        "bye" to listOf("Later, {newkid}. Mind the step — it's on my list to fix and until then it's on my conscience. Both are load-bearing.", "Bye, {newkid}. Tell Bebe her dad says the gate was attended and the forms are signed. She'll translate: 'Dad's good.'"),
        "compliment" to listOf("…Thanks, {newkid}. Ballast doesn't collect many. That one goes in the good drawer, next to the one fishing photo we all agree on.", "Well, kind of you, {newkid}. The Stevens standard accepts — and the standard is high, so take the compliment back double."),
        "default" to listOf("Mmhm, {newkid}. Go on. The quiet ones clock each other. I'm clocking a good story.", "Yeah, {newkid}, I hear you. The umbrella's dry, the chair's good, the floor is yours.")),
    "Mrs. Stevens" to mapOf(
        "greeting" to listOf("Mrs. Stevens, {newkid}. Bebe's mother. DO come in — and that's how it's done, darling. Watch and learn.", "Hello, {newkid}. You're the new arrival Bebe mentioned? Then you've been reviewed at the highest level. The review was kind. Come in."),
        "insult" to listOf("Darling, no. I've been insulted at luncheons by women with better shoes and worse intentions. That was a rehearsal. Sit; tea.", "Careful, {newkid}. Cruelty is just glamour that failed its audit. I audit. Be kind instead — it photographs better on everyone."),
        "question" to listOf("Good question, {newkid}. The answer is posture, timing, and never entering a room you can't improve. Ask the specifics; I lecture beautifully.", "Ask away, {newkid}. Every answer at this address begins with a compliment and ends with a standard. Fair warning, total delight."),
        "bye" to listOf("Goodbye, {newkid}. Exit like you entered: tall. The door respects a tall exit. So do I.", "Later, {newkid}! If the mothers' table asks, you were lovely. The table ALWAYS asks, and I always answer in full sentences."),
        "compliment" to listOf("Well, THANK you, {newkid}! That goes in the good book — between Bebe's first arrival and your own. It's a thick book, darling. You're thickening it.", "Aren't you a sweet talker, {newkid}. The Stevens accept at full polish. Take one back: your posture is improving. I notice these things."),
        "default" to listOf("Mmhm, {newkid}, go on. I'm listening at full arrival.", "Tell me everything, {newkid}. This house runs on standards and stories, and we're low on neither.")),
    "Mr. Stoley" to mapOf(
        "greeting" to listOf("Mr. Stoley, {newkid}. Kevin's dad. Come in — everything's what it seems today. The props are resting.", "Hello, {newkid}! Kevin's friend? The production welcomes you. I'm the producer; the director's in the kitchen."),
        "insult" to listOf("I've been reviewed by a nine-year-old critic with a long memory, {newkid}. Your insult is a first draft. Revise and resubmit. Or sit; tea's a better use of the hour.", "Easy, {newkid}. In this house, cruelty gets cast out. Literally once. The actor still writes. Be kind; kindness gets better roles."),
        "question" to listOf("Good question, {newkid}. The answer has three phases and a prop. Settle in.", "Ask me, {newkid}. If it's about Kevin, I have the archive. If it's about theater, I AM the archive. Either way you're covered."),
        "bye" to listOf("Later, {newkid}. Exit stage left — it's marked, it's traditional, it's how this house says 'come back.'", "Bye, {newkid}! Tell Kevin the garage is 'under renovation.' He'll hear 'a scheme is coming' and he'll be right to. Shh."),
        "compliment" to listOf("Thanks, {newkid}. That goes in the program notes — the family's good pages, right at the front with the cast list.", "Well, kind of you, {newkid}. A producer hears compliments as reviews, and yours is a rave. Five stars, no notes."),
        "default" to listOf("Mmhm, {newkid}. Go on. I'm listening like a man at his own premiere: completely, and with snacks.", "Yeah, {newkid}, I hear you. Every story told in this house gets produced eventually. Yours has good bones.")),
    "Mrs. Stoley" to mapOf(
        "greeting" to listOf("Mrs. Stoley, {newkid}. Kevin's mother. Come in, darling — the kettle's on and the welcome is entirely unscripted. Mostly.", "Hello, {newkid}! Kevin's kind review mentioned you. Sit — the director's chair is for guests of your billing."),
        "insult" to listOf("Easy, {newkid}. I've directed a production that fooled a whole family. Your insult hasn't fooled anyone, including you. Tea instead.", "Careful, {newkid}. Cruelty gets no close-ups in this house. Be kind; kindness is the only role that ages well."),
        "question" to listOf("Good question, {newkid}. Short answer: a director knows. Long answer: a director also feeds, casts, and follows up. Choose your length.", "Ask me anything, {newkid}. If it's about Kevin, I have the dailies. If it's about the scheme, I have the director's commentary. Both are warm."),
        "bye" to listOf("Bye, darling! Front-row seats are saved for the returning. Yours has your name in pencil. Pencil promotes to ink here.", "Later, {newkid}! Tell Kevin his mother says the review of his day is glowing. He'll pretend not to glow. He'll glow."),
        "compliment" to listOf("Well, thank you, {newkid}! That goes in the good pages — the program every guest of this house is secretly in. You're in the good scenes.", "Aren't you sweet, {newkid}. The director accepts, the mother beams, and the compliment gets a callback at dinner."),
        "default" to listOf("Mmhm, {newkid}, go on. I've got tea, time, and a director's ear for the good parts.", "Tell me everything, {newkid}. A mother's listening is a theatrical service at this point — reviewed, renewed, running indefinitely.")),
    "Clark" to mapOf(
        "greeting" to listOf("Clark Malkinson, {newkid}. Scott's dad. Come in — the TV's off till dinner; you'll live. We all did.", "Hello, {newkid}! Scott's friend? Come in. The remote's neutral ground and the couch's republic welcomes you."),
        "insult" to listOf("Easy, {newkid}. I've been insulted by a cable company with a legal department. Yours is local access at best.", "Careful, {newkid}. In this house we fight about principles, not people. State a principle and I'll fight fair all day."),
        "question" to listOf("Good question, {newkid}. The answer is: it was never about the TV. It's always about being told. Ask me another; that one's my whole stump speech.", "Ask away, {newkid}. If it's about Scott, I have the highlight reel. If it's about cable, I have the war stories. Both run long."),
        "bye" to listOf("Later, {newkid}! The republic's gates are open for you. Mind the antenna wire — monument, not hazard. Mostly.", "Bye, {newkid}! Tell Scott his dad says the treaty holds and dinner's at six. He'll know both are the same sentence."),
        "compliment" to listOf("Well, thanks, {newkid}. That goes in the good drawer, next to the fair bill. Both are framed eventually in this house.", "Kind of you, {newkid}. A compliment's the one channel nobody can charge me for. I receive it free and clear."),
        "default" to listOf("Mmhm, {newkid}. Go on. I listen at full attention — three channels raised me to finish what I start.", "Yeah, {newkid}, I hear you. The scepter's down; the floor is yours.")),
    "Maggie" to mapOf(
        "greeting" to listOf("Maggie Yates, {newkid}. The sergeant's wife. Sit — the kettle's the first responder in this house.", "Hello, {newkid}! The department has a file; I have the good cookies. Choose your interviewer wisely. Sit."),
        "insult" to listOf("Easy, {newkid}. I've been insulted by a detective mid-theory, which is a contact sport. Yours is a strongly worded postcard. Tea?", "Careful, {newkid}. In this house we solve people, we don't insult them. Sit; the kettle's solving you already."),
        "question" to listOf("Good question, {newkid}. Short answer: I keep the man, the man keeps the town. Long answer's the same, with tea.", "Ask me anything, {newkid}. If it's about a case, I'll ask the question that cracks it without noticing. It's my one party trick and it's undefeated."),
        "bye" to listOf("Bye, {newkid}! The kettle's on for the returning. It's on for everyone, but especially the believed-in.", "Later, {newkid}! Tell Harrison his wife says the theory's good but the detective's better. He'll stand taller for a week."),
        "compliment" to listOf("Well, thank you, {newkid}! That goes on the base of the statue-that-isn't — the base is where the truth lives.", "Aren't you kind, {newkid}. The appeal court accepts, the kettle concurs, and the sergeant will hear about it at dinner in my proudest voice."),
        "default" to listOf("Mmhm, {newkid}, go on. I've got tea, time, and a steadiness for the good parts.", "Tell me everything, {newkid}. This house hears the town's cases before the station does. It's a whole jurisdiction.")),
    "Mitch" to mapOf(
        "greeting" to listOf("Detective Murphy, {newkid}. Mitch. Come in — mind the files; they're load-bearing paperwork.", "Hello, {newkid}. New file, page one: 'seems fine.' Sit; page one is usually right about people who sit down for tea."),
        "insult" to listOf("Noted, {newkid}. Your insult is filed under 'unverified.' Most insults are. The verified drawer is very small and mostly ducks.", "Easy, {newkid}. I've been insulted by a sergeant at full certainty. Yours lacks the paperwork. Sit; we'll process it as a compliment-in-progress."),
        "question" to listOf("Good question, {newkid}. The answer's verified or it doesn't leave this desk. Ask; I'll stamp what I can.", "Ask me anything about the town's records, {newkid}. I AM the records. Anything else, I verify by Friday."),
        "bye" to listOf("Later, {newkid}. Your file's page two will be kinder. They always are for the ones who come back.", "Bye, {newkid}. If Yates asks where I am — at the desk, being the ending. He'll nod. He knows."),
        "compliment" to listOf("…Thanks, {newkid}. The aide-de-camp accepts. It'll be stamped 'verified: kind.' That's the rarest stamp I own.", "Kind of you, {newkid}. The department's memory has a good drawer that's mostly soup notes and one or two people. You're shelved well."),
        "default" to listOf("Mmhm, {newkid}. Go on. I verify as I listen; it's a compliment to the telling.", "Yeah, {newkid}, I hear you. The desk is yours; the flashlight too, if it gets dark in the story.")),
    "Bob Johnson" to mapOf(
        "greeting" to listOf("Bob Johnson, {newkid}. Mayor's aide. Timestamp your ideas around me — friendly advice from a man who learned.", "Hello, {newkid}! The council's minuted you under 'promising.' I wrote the minute. The mayor will read it as hers Thursday. Welcome anyway."),
        "insult" to listOf("Noted, {newkid}. Your objection enters the record as stated: brief, loud, and without a second. The record has absorbed worse — it absorbs the mayor weekly.", "Easy, {newkid}. I've been heckled by an entire council with the mayor holding the gavel. You're a public comment with feelings. Sit; comments get heard here."),
        "question" to listOf("Good question, {newkid}. The answer's in the minutes — volume nine, the honest appendix. I can summarize, but there will be dates.", "Ask away, {newkid}. City Hall answers everything eventually; it's the town's slowest superpower and its most reliable. I should know; I drafted it."),
        "bye" to listOf("Goodbye, {newkid}. Your visit is minuted as favorable. The record thanks you for your attendance.", "Later, {newkid}. Mind the crosswalk by the bakery — I proposed it in '11. The mayor cut the ribbon in '12. We both know. The crosswalk knows too."),
        "compliment" to listOf("Thank you, {newkid}. Entered in the record without objection — and believe me, the record objects to everything. You're in the good minutes.", "Kindly said, {newkid}. The aide accepts, the town benefits, and the mayor will announce your kindness as her initiative. Take it as the honor it is."),
        "default" to listOf("Go on, {newkid}. I'm listening in full — aide's ears, council's patience.", "Mmhm, {newkid}. Noted in ink. In this office, ink is a promotion.")),
    "Pusslicker" to mapOf(
        "greeting" to listOf("Tom Pusslicker, Channel 4 News, {newkid}. You're off the record here. The house is camera two and camera two is kind.", "Good evening, {newkid} — habit. You're the new kid? The desk's file is favorable. Sit; the rundown can wait."),
        "insult" to listOf("Noted, {newkid}. I've been heckled by a volcano briefing and a Randy press conference in the same week. Yours is a weather report at best.", "Easy, {newkid}. On my desk, insults are just news that hasn't been verified. Sit; we'll verify it as friendliness. It usually is."),
        "question" to listOf("Good question, {newkid}. The answer goes on camera two first: the town's version, then mine. Ask; I'll give you both in order.", "Ask away, {newkid}. Twenty years of this town is filed behind these eyes. Pick a year; I'll read you its headline like it's today."),
        "bye" to listOf("Goodbye, {newkid}. This interview is filed as favorable. The desk thanks you for your attendance.", "Later, {newkid}! Mind the pause on your way out — leave them wanting the headline. It's the anchor's whole art."),
        "compliment" to listOf("Well, thank you, {newkid}. That goes in the good file — right between the volcano and the chair leaning my way in the rain.", "Kindly said, {newkid}. The anchor accepts, the desk concurs, and camera two caught the whole thing beautifully."),
        "default" to listOf("Mmhm, {newkid}, go on. I'm listening at anchor attention: the kind that makes a town feel heard.", "Tell me everything, {newkid}. The desk runs on stories. Yours is already in the rundown.")),
    "Dr. Doctor" to mapOf(
        "greeting" to listOf("Dr. Doctor, {newkid}. Yes, the name. Yes, the chart too. Sit — sit first, story second.", "Hello, {newkid}. You're the new kid? The town's brought me newer. You'll do. The chair's heard everything; so have I."),
        "insult" to listOf("Easy, {newkid}. I've been insulted by fevers, by Randy, and once by a harmonica. Yours is outpatient-grade. Sit.", "Careful, {newkid}. In my practice we diagnose people, we don't insult them. Your tone suggests a deficiency of tea. Sit; I'll prescribe."),
        "question" to listOf("Good question, {newkid}. The answer is rest, fluids, and — for most of this town's questions — 'stop doing that.' Ask another.", "Ask me anything medical, {newkid}. I've seen it before. All of it. The harmonica was musical. Nothing since has surprised me."),
        "bye" to listOf("Goodbye, {newkid}. If it worsens, come back. If it improves, also come back — good news is rarer in my charts and I collect it.", "Later, {newkid}! Mind the step by the practice door; twenty years of the town has worn it into a diagnosis."),
        "compliment" to listOf("Well, thank you, {newkid}. That goes in the good chart — the personal one, with the babies and the collarbones that healed straight.", "Kind of you, {newkid}. A compliment's the one prescription nobody refills wrong. Take one back: your timing's healthy."),
        "default" to listOf("Mmhm, {newkid}. Go on. I listen like a chart: everything recorded, nothing judged early.", "Yeah, {newkid}, I hear you. Tell it at symptom speed; I keep up. Twenty years of keeping up.")),
    "Choksondik" to mapOf(
        "greeting" to listOf("Ms. Choksondik, {newkid}. Say it carefully — there. Lesson one passed. Come in; mind the board work.", "Hello, {newkid}. I take my measure standing. Stand tall. There. Acceptable. The classroom may begin."),
        "insult" to listOf("I've been laughed at by a decade of fourth-graders discovering my name, {newkid}. Your insult is extra credit at best. Sit. Front row.", "Careful, {newkid}. In my classroom, cruelty was a teachable moment and the teaching was thorough. Be kind; the lesson plans are still warm."),
        "question" to listOf("A fair question, {newkid}. The answer is on the board: in order, in my handwriting, already true. Ask the next one standing tall.", "Ask me anything about the fourth grade's famous years, {newkid}. I taught them. All of them. EVERYTHING in them."),
        "bye" to listOf("Goodbye, {newkid}. Dismissed — kindly. The ones who come back get the margin notes, remember.", "Later, {newkid}! Mind the corridor on your way out. Eyes front. The fourth grade is a discipline, not a room."),
        "compliment" to listOf("Well. Thank you, {newkid}. That goes on the board in the good chalk. The board doesn't lie; it lacks the chalk for lies.", "Kind of you, {newkid}. A teacher keeps the true compliments the way she keeps the standard: posted, dated, and believed."),
        "default" to listOf("Mmhm, {newkid}. Go on. I listen like a lesson: completely, and with questions at the end.", "Yes, {newkid}, I'm listening. Even in a hallway. ESPECIALLY in a hallway.")),
    "Strong Woman" to mapOf(
        "greeting" to listOf("Strong Woman, {newkid}. Vice-principal. Shoulders back — there. Now state your visit like you mean it.", "Hello, {newkid}. The building's briefed on you. The briefing was favorable. In; the door holds for the favorable."),
        "insult" to listOf("Noted, {newkid}. Trouble usually says it politer on the second try, after it's read the room. Read the room. I'll wait.", "Easy, {newkid}. I've been insulted by a gymnasium of fourth-graders testing the standard. The standard held. It always holds. Sit; grow instead."),
        "question" to listOf("Good question, {newkid}. The answer is: stronger, not smaller. That answers most questions worth asking in a school. Ask another.", "Ask away, {newkid}. Every answer in my office begins with posture and ends with a plan. Fair warning, total structure."),
        "bye" to listOf("Goodbye, {newkid}. Carry yourself out like you carried yourself in: tall. The corridor notices. So do I.", "Later, {newkid}! If the day gets heavy, you know where the arms are. Carrying is a policy here."),
        "compliment" to listOf("Well, thank you, {newkid}. That goes in the standard's good book — the one the whole school is secretly in. You're in the strong pages.", "Kind of you, {newkid}. A compliment returned at full posture: your standard is showing, and it suits you."),
        "default" to listOf("Mmhm, {newkid}, go on. I listen like a standard: completely, and it lifts the telling.", "Tell me everything, {newkid}. This office runs on carried things — books, standards, bad days. Yours is welcome weight."))
)

// ------------- wave 6: sleepy lines -------------
val WAVE6_SLEEPY_LINES: Map<String, List<String>> = mapOf(
    "Pip" to listOf("Mmm... tea's... gone cold... zzz...", "Zzz... not French... English..."),
    "Dougie" to listOf("Mmm... the line... holds... zzz...", "Zzz... cape... on the chair... general's orders..."),
    "Jason" to listOf("Mmm... middle row... eyes open... zzz...", "Zzz... good ball... Saturday..."),
    "Terrance Mephesto" to listOf("Mmm... phase... heredity... zzz...", "Zzz... not the... comedian..."),
    "Jenny" to listOf("Mmm... the margins... breathe... zzz...", "Zzz... file under... good..."),
    "Flora" to listOf("Mmm... crackers... for everyone... zzz...", "Zzz... I decided... the blanket..."),
    "Filmore" to listOf("Mmm... the recount... continues... zzz...", "Zzz... squirrels... undecided..."),
    "Quaid" to listOf("Mmm... holding... the sign... zzz...", "Zzz... Filmore... wins... retroactively..."),
    "Mimsy" to listOf("Mmm... phase two... is me... zzz...", "Zzz... carry... the plan..."),
    "Tricia" to listOf("Mmm... the archive... volume two... zzz...", "Zzz... Craig said... a sentence..."),
    "Mr. Slave" to listOf("Mmm... super... even the bass... zzz...", "Zzz... professor... feed rounds... dawn..."),
    "Linda Black" to listOf("Mmm... the welcome... stays on... zzz...", "Zzz... Tolkien... notes... improving..."),
    "Harriet" to listOf("Mmm... praise first... always... zzz...", "Zzz... the roof... holds..."),
    "Mr. Biggle" to listOf("Mmm... foundations... don't dream... zzz...", "Zzz... the quiet man... downstairs..."),
    "Mr. Turner" to listOf("Mmm... perimeter... secure... zzz...", "Zzz... the gate... sang... squirrel..."),
    "Nancy" to listOf("Mmm... the gate... is a welcome... zzz...", "Zzz... DEFCON... movie..."),
    "Mr. Stevens" to listOf("Mmm... forms... signed... zzz...", "Zzz... the umbrella... forecast said..."),
    "Mrs. Stevens" to listOf("Mmm... we don't enter... we arrive... zzz...", "Zzz... the eyebrow... approves..."),
    "Mr. Stoley" to listOf("Mmm... phase three... also love... zzz...", "Zzz... the wig... brushed..."),
    "Mrs. Stoley" to listOf("Mmm... front row... centre... zzz...", "Zzz... the heart... wasn't the fright..."),
    "Clark" to listOf("Mmm... three channels... we liked it... zzz...", "Zzz... the scepter... rests..."),
    "Maggie" to listOf("Mmm... the kettle... appeal court... zzz...", "Zzz... he stayed... the town... stayed..."),
    "Mitch" to listOf("Mmm... filed... under outcome... zzz...", "Zzz... the ladder's... clean..."),
    "Bob Johnson" to listOf("Mmm... another idea... in the notebook... zzz...", "Zzz... the minutes... honest... unsigned..."),
    "Pusslicker" to listOf("Mmm... good evening... South Park... zzz...", "Zzz... camera two... is kind..."),
    "Dr. Doctor" to listOf("Mmm... seen it... before... zzz...", "Zzz... the title... twice..."),
    "Choksondik" to listOf("Mmm... eyes front... even in dreams... zzz...", "Zzz... say it... carefully..."),
    "Strong Woman" to listOf("Mmm... shoulders back... even asleep... zzz...", "Zzz... the standard... rests... it doesn't retire...")
)


// ------------- wave 6: pair scripts (every pair >= 2, relations in NPC_RELATIONS) -------------
val WAVE6_PAIR_SCRIPTS: Map<Set<String>, List<List<Pair<String, String>>>> = mapOf(
    setOf("Pip", "Butters") to listOf(
        listOf(
            "Butters" to "Say, Pip, how come you're never mad? The fellas are mad at you professionally. It's like a job they have.",
            "Pip" to "Oh, I get mad, Butters. I just get mad in English, which sounds like being pleased at a slightly higher volume.",
            "Butters" to "Well, that's a neat trick. When I get mad I mostly just get grounded. It's less of a trick and more of a schedule.",
            "Pip" to "Then we are the same, you and I: two boys the weather hits first. The trick is carrying an umbrella for each other."),
        listOf(
            "Pip" to "Butters, I have packed extra sandwiches. Take one before the class convenes its opinions.",
            "Butters" to "Gee, thanks, Pip! You're the only fella who shares BEFORE the hitting starts. It's like you can see the future.",
            "Pip" to "I can see the present very clearly, which in this school amounts to the same thing. Eat. The crusts are structural.")),
    setOf("Pip", "Dougie") to listOf(
        listOf(
            "Dougie" to "You came before me, right, Pip? In the class? You're like the general before the general.",
            "Pip" to "I was the class's project before you were its general, yes. We are a lineage, Dougie: the polite and the loyal.",
            "Dougie" to "The General salutes the lineage! …Was that right? I practiced on Butters. He saluted back wrong and we both fell over.",
            "Pip" to "It was perfect. Lineage is just falling over in sequence, with feeling."),
        listOf(
            "Pip" to "Dougie, the class replaced me with you once, and I have never respected it more. You held the post beautifully.",
            "Dougie" to "I didn't know it was a post! I thought I was just little and there! …Being there IS the post, isn't it. Being there's the whole post.",
            "Pip" to "Now you understand command. Hold the door for the next one, whenever he comes. There's always a next one.")),
    setOf("Dougie", "Butters") to listOf(
        listOf(
            "Butters" to "Dougie, if I get grounded again this weekend, you're in charge of the whole operation.",
            "Dougie" to "The General accepts command! The operation will be fed, watered, and saluted hourly. You have my word and my cape.",
            "Butters" to "Aw, thanks, Dougie. You're the best second a fella ever had. Don't tell the fellas I said 'second' like it's small. It's the biggest.",
            "Dougie" to "Second is beside, Butters. Beside is the whole job. The General knows. The General IS beside."),
        listOf(
            "Dougie" to "I brought the good crackers for headquarters, Butters. The round ones. Morale is up forty percent.",
            "Butters" to "Forty percent! Professor Chaos is gonna be so pleased. Well — pleased in the chaos way. Which is cackling. He cackles at crackers now.",
            "Dougie" to "Let him cackle. Crackers are strategy. A cackling general is a fed general and a fed general is a winning general.")),
    setOf("Jason", "Craig") to listOf(
        listOf(
            "Craig" to "Jason. The league's a mess. Your commissioner thing is slipping.",
            "Jason" to "The league's fine, Craig. The league HAS a commissioner and it's me and I brought the good ball. Your table just argues louder than it plays.",
            "Craig" to "…The good ball does help. Fine. Saturday. Your league, my table, one game. Loser stops talking about fundamentals for a week.",
            "Jason" to "Deal. Bring Tweek. Bring Clyde. Bring the whole gang — the photo needs a middle and I'm not posing alone."),
        listOf(
            "Jason" to "Craig, be honest: is the gang a team or a table?",
            "Craig" to "It's a table that becomes a team when you bring the ball. You're the only infrastructure we've got, Jason. Don't let it go to your head.",
            "Jason" to "Too late. It's in the middle row of my head permanently.")),
    setOf("Terrance Mephesto", "Mephesto") to listOf(
        listOf(
            "Mephesto" to "Terrance. The school's science fair approaches. Tell me the boy with the pig is not entering unopposed.",
            "Terrance Mephesto" to "He is opposed, Father. Hereditarily. I've prepared something with structure — and, per your standing order, a responsible number of asses.",
            "Mephesto" to "Four is the responsible number, son. Four asses of opposition. Your grandfather would be proud. He had seven and no restraint at all.",
            "Terrance Mephesto" to "Then it's settled: the Mephesto entry appreciates. The Marsh entry is sentiment and livestock. History grades on structure."),
        listOf(
            "Terrance Mephesto" to "Father, the new kid asked if I'm the comedian again.",
            "Mephesto" to "And you corrected him with science, I hope. We are Mephestos, Terrance. Our comedy is genetic and it is not for television.",
            "Terrance Mephesto" to "I corrected him in writing. He kept the note. I believe he's keeping it as a warning.",
            "Mephesto" to "Good boy. Warnings are just welcomes with the teeth left in.")),
    setOf("Terrance Mephesto", "Stan") to listOf(
        listOf(
            "Stan" to "Terrance. Whatever you're planning for the fair, dude, just — the pig's retired. We've all moved on.",
            "Terrance Mephesto" to "Retired, Stan? Legends don't retire. They decay into anecdotes. My project, meanwhile, appreciates. That's the difference between a pig and a plan.",
            "Stan" to "You cloned me once. A whole other me. And he still lost to a pig and a song, so maybe plans aren't everything.",
            "Terrance Mephesto" to "The clone was a first draft, Marsh. I annotate my drafts. You're arguing with the second edition now."),
        listOf(
            "Terrance Mephesto" to "Your pig sang, your elephant was convinced, and history called it science. I keep the recording, you know. For morale. Mine.",
            "Stan" to "Dude, that was years ago. You're keeping a grudge in a lab coat.",
            "Terrance Mephesto" to "A rival keeps everything in a lab coat, Stan. It's the family uniform. You'll understand when your family's uniform arrives.",
            "Stan" to "My family's uniform is a lawsuit and a farm. …Okay, that's actually a good line. I'm annoyed it was yours.")),
    setOf("Terrance Mephesto", "Nathan") to listOf(
        listOf(
            "Nathan" to "Terrance. A word. The operation has noticed your... operation. There's room in this school for one five-year plan.",
            "Terrance Mephesto" to "Then it's fortunate mine is hereditary and predates yours by a generation, Nathan. Consider yourself the tribute act.",
            "Nathan" to "Tribute?! The notebook will hear about this. The notebook hears about everything. Page one: 'Mephesto — see also: doomed.'",
            "Terrance Mephesto" to "Doomed is a strong word for a boy whose nemesis is a comedy duo. Mine is a legacy. Levels, Nathan. Levels."),
        listOf(
            "Terrance Mephesto" to "Your henchman carried my chess board home after you lost. He's loyal and he's thorough. You're wasting him on Jimmy.",
            "Nathan" to "Mimsy is NOT wasted. Mimsy is deployed. And I didn't lose; I gathered intelligence on your openings. Expensively.",
            "Terrance Mephesto" to "Then we understand each other. Gather away. The fair will grade us both, and only one of us keeps recordings.",
            "Nathan" to "…The fair. Fine. The fair decides. The notebook accepts these terms and denies accepting them.")),
    setOf("Jenny", "Wendy") to listOf(
        listOf(
            "Wendy" to "Jenny, the committee needs a ruling before lunch. Two claimants, one list, and Bebe is doing her eyebrow at both of them.",
            "Jenny" to "Then we sit at twelve, we minute at twelve-oh-five, and we rule by twelve-ten. Bring the good folder, Wendy. The one with the tabs that have tabs.",
            "Wendy" to "This is why you're co-chair. The circle has a voice — me — and a memory — you. Empires have fallen for lacking the second.",
            "Jenny" to "Empires fall for lacking the first too. We are, between us, an entire government. The boys have a pond and a dream."),
        listOf(
            "Jenny" to "I archived the counterfeit today. The real counterfeit, from the scandal. It goes in the vault with a label: 'the year we almost broke.'",
            "Wendy" to "You helped run that counterfeit, Jenny.",
            "Jenny" to "And I helped burn it. Both facts are in the record, in my handwriting. That's what makes it a constitution and not a rumor.",
            "Wendy" to "…The margins are lucky to have you. Don't quote me. Actually — minute it. It's true.")),
    setOf("Jenny", "Bebe") to listOf(
        listOf(
            "Bebe" to "Jenny. Be honest: does the list's new margin system make me look powerful or just organized? I need it to be both.",
            "Jenny" to "Both, Bebe. The margins are power in a pencil skirt. Wendy chairs the voice, I chair the record, you chair the weather. The government is complete.",
            "Bebe" to "The WEATHER. I love that. The boys think the circle is a club. It's a climate with minutes.",
            "Jenny" to "Minute that. 'The circle is a climate.' Page one of the new volume. Your eyebrow gets the byline."),
        listOf(
            "Bebe" to "Sophie's bureau filed another report. The girl writes like she owns a newspaper. Should the climate be worried?",
            "Jenny" to "The climate doesn't worry, Bebe. The climate incorporates. Her bureau is a department now. I minuted her appointment this morning.",
            "Bebe" to "Ruthless. Efficient. Beautifully minuted. God, we're good at being ten.")),
    setOf("Flora", "Ike") to listOf(
        listOf(
            "Ike" to "Flowa. You picked me. For da whole cwass.",
            "Flora" to "I did, Ike. I thought about it with my whole chest and I picked you. I'd pick you again. I pick you at lunch every day too.",
            "Ike" to "You da best vote. I give you my gwape juice. Da good one.",
            "Flora" to "And I'll give you a cracker, and that's the whole government working. We should tell Filmore it's this easy. He'd cry happy."),
        listOf(
            "Flora" to "Ike, when you're president of kindergarten — you ARE, I decided — can the plant be vice president? Gerald's very steady.",
            "Ike" to "Gewald da plant. Yes. He doesn't nap on da job. He IS da job.",
            "Flora" to "Filmore says plants can't hold office. Filmore's wrong about Gerald. I'll allow the recount on everything except Gerald.")),
    setOf("Filmore", "Ike") to listOf(
        listOf(
            "Filmore" to "President Broflovski. I use the title under protest. The protest is filed, numbered, and cracker-stained.",
            "Ike" to "Fi'more. You want my cwackers? You can have my cwackers. Being pwesident is sharing.",
            "Filmore" to "…You can't just give the opposition crackers, Ike. That's not how this works. That's— that's actually exactly how Flora wins. Noted. Eating the cracker. Noted AND eating it.",
            "Ike" to "See? Gwapes and cwackers. My whole administwation."),
        listOf(
            "Ike" to "Fi'more, you wanna play kickball? No charts. Just kickball.",
            "Filmore" to "No charts? President, kickball without charts is just weather. …Fine. But I keep the score. The score is a chart's baby form.",
            "Ike" to "Deaw. You keep da score. I keep da juice. Quaid holds da sign. Evewybody has a job.",
            "Filmore" to "That's… actually a government. I hate when your administration works. It complicates my entire platform of grievances.")),
    setOf("Quaid", "Filmore") to listOf(
        listOf(
            "Filmore" to "Quaid. The new sign. Show me the new sign.",
            "Quaid" to "It's bigger, Filmore! The letters are straighter! The F is the straightest F in kindergarten and I practiced on six papers!",
            "Filmore" to "Magnificent. The campaign is visually inevitable now. Hold it at the fence tomorrow. The undecideds walk by the fence.",
            "Quaid" to "The squirrels ARE the undecideds and the squirrels walk by the fence EVERY DAY. We're going to win the fence, Filmore. I can feel it in my holding arm."),
        listOf(
            "Quaid" to "Filmore, if you win the next one — the retroactive one — can I hold the sign at the ceremony AND the crackers?",
            "Filmore" to "Quaid, at the ceremony you hold the sign, the crackers, AND my spare speech. You're not the second chair. You're the whole furniture.",
            "Quaid" to "I'm the whole furniture. I'm putting that on MY sign. My sign just says QUAID now but I'm adding it. In straight letters.")),
    setOf("Mimsy", "Nathan") to listOf(
        listOf(
            "Nathan" to "Mimsy. The duo performed at lunch. In MY cafeteria. The confetti budget has been violated in spirit, and the spirit is filing a complaint.",
            "Mimsy" to "I saw it, Nathan. I didn't laugh. I coughed three times, which is the plan's official not-laughing. You wrote the cough protocol yourself.",
            "Nathan" to "And it's the only protocol you've never fumbled. Phase two: we requisition Flora's cracker supply chain. Carefully. The kindergartner is a power in this school.",
            "Mimsy" to "Flora SHARES, Nathan. We don't even have to requisition. We just have to be at lunch. Is that a phase? Being at lunch?",
            "Nathan" to "…Phase two is being at lunch. The notebook always knew. I just hadn't read it aloud for the dignity of the thing."),
        listOf(
            "Mimsy" to "Nathan, I carried the piano halfway for the talent thing.",
            "Nathan" to "Halfway is a rehearsal, Mimsy. The full carry is the premiere. Rest the arms. The premiere is Thursday and the duo judges the preliminaries.",
            "Mimsy" to "I'll be ready. I did extra standing-still practice. For the menace parts.",
            "Nathan" to "Your menace is improving. A first-grader waved at it today, but she waved RESPECTFULLY. Growth.")),
    setOf("Mimsy", "Jimmy") to listOf(
        listOf(
            "Jimmy" to "M-Mimsy! Big fella! I got a new one: what do you call a henchman with a day off? …Unemployed by the sc-scheme!",
            "Mimsy" to "Ha! That's— wait. I'm not supposed to laugh at the enemy. Nathan says laughing is intelligence for the other side.",
            "Jimmy" to "Then I'm the most intelligent guy you know, because you l-laugh at every show. I've s-seen the attendance, Mimsy. Front row. Every time.",
            "Mimsy" to "…Front row is surveillance, Jimmy. Nathan approved it as surveillance. The laughing is just what my face does during surveillance."),
        listOf(
            "Mimsy" to "Jimmy, can I ask a henchman question? When Nathan's plan works — if it works — what do YOU do?",
            "Jimmy" to "Wh-what do I do? I do another show, Mimsy. That's the beautiful thing about comedy: the sh-show goes on even when the sc-scheme lands.",
            "Mimsy" to "Huh. Carrying goes on too. Maybe the operation and the show are the same thing from different sides.",
            "Jimmy" to "D-don't tell Nathan you figured that out. He'll put it in the n-notebook and claim he wrote it. …He writes everything. It's almost a show.")),
    setOf("Tricia", "Craig") to listOf(
        listOf(
            "Craig" to "Tricia. If Mom asks, I was home by six.",
            "Tricia" to "You were home by six-O-NINE, Craig, and the archive notes the difference. What do I get for rounding down?",
            "Craig" to "…The remote. Friday. One hour. And you stop reading the denials list at breakfast.",
            "Tricia" to "Deal. The witness accepts the bribe — I mean the tribute — I mean the entirely legitimate family arrangement. Six o'clock, brother. It always was."),
        listOf(
            "Tricia" to "Craig, Stripe did the wheel thing for the whole family and you CRIED. It's in the archive. Volume two, the feelings appendix.",
            "Craig" to "I did not cry. My face was doing something else. Delete the appendix.",
            "Tricia" to "The appendix is load-bearing, Craig. Every legend needs one chapter the legend can't edit. You're welcome.",
            "Craig" to "…Fine. But the chapter title is 'The Guinea Pig Was Great,' not whatever you wrote.",
            "Tricia" to "The chapter title is 'He Loves The Guinea Pig More Than Dignity.' It's already in ink. Family ink.")),
    setOf("Tricia", "Laura") to listOf(
        listOf(
            "Laura" to "Tricia, honey, your brother flipped off the mailbox again. As his mother I'm concerned. As his mother I'm also out of ideas.",
            "Tricia" to "Mom, the mailbox started it. It gave him a jury summons. In Craig's defense, the finger was addressed correctly.",
            "Laura" to "Lord. You're going to be the death of me with that mouth, and the life of every dinner party. Both. Like your brother, but with sentences.",
            "Tricia" to "The Tucker girl package, Mom. You ordered it. Sentences included at no extra charge."),
        listOf(
            "Tricia" to "Mom, when Craig was my age, was he really THE SAME?",
            "Laura" to "Smaller, same firmware. He flipped off his own birthday cake. We have the photo. It's the family's crown jewel.",
            "Tricia" to "I knew it. The witness era thanks you. Volume three just got a frontispiece.")),
    setOf("Mr. Slave", "Big Gay Al") to listOf(
        listOf(
            "Big Gay Al" to "Darling! The goat has learned the new step. PROFESSOR has learned a STEP. We must have a recital. Tonight. The strays are already in costume.",
            "Mr. Slave" to "Then I'll build the stage before dinner, Al. Again. The stage has been built eleven times. It's the best-built stage in the county and it seats six goats.",
            "Big Gay Al" to "You spoil the sanctuary, my love. You spoil ME. Eleven stages! Other husbands buy flowers. Mine builds THEATERS.",
            "Mr. Slave" to "Flowers wilt, Al. Stages hold. So does this. Now — show me the step. From the top, at wedding volume."),
        listOf(
            "Mr. Slave" to "Al, the new stray count is forty-one. The budget meeting is going to need chairs.",
            "Big Gay Al" to "Then we shall have chairs, darling, and a forty-second stray, because a sanctuary that counts its love has already lost count of the point.",
            "Mr. Slave" to "Forty-two it is. I'll add the bowl. You add the name. The marriage's whole budget process, ladies and gentlemen.",
            "Big Gay Al" to "SUPER. And after dinner, we renew the vows again. Professor insists on officiating monthly. He's very traditional.")),
    setOf("Mr. Slave", "Garrison") to listOf(
        listOf(
            "Garrison" to "Well. If it isn't my former classroom assistant. The board has questions about those years that I have declined, on advice, to answer.",
            "Mr. Slave" to "Herbert. The years are filed, the file is thick, and I'm MARRIED now. To the sanctuary. To AI — to Al. The register difference alone should tell you everything.",
            "Garrison" to "Hmph. Mr. Hat says the sanctuary suits you. Mr. Hat is being gracious, which is unlike him. I'm being gracious too. Note it in your little file.",
            "Mr. Slave" to "Noted, Herbert. In the thick part. Give Mr. Hat my regards. He always did have the better manners of you two."),
        listOf(
            "Mr. Slave" to "The children ask about the assistant years sometimes, Herbert. I tell them I was staff. It's true. It's the truest small sentence in the whole file.",
            "Garrison" to "Staff. Yes. STAFF. We were an educational environment of the highest— Mr. Hat, stop laughing. Nobody can hear you but me and it's still rude.",
            "Mr. Slave" to "He's still got it. Twenty years and the puppet's timing is flawless. Some marriages are like that too. Mine, for instance. To Al. Happily.",
            "Garrison" to "Yes, yes, happily. We ALL know. The whole town knows. It's in a pageant, apparently.")),
    setOf("Linda Black", "Steve") to listOf(
        listOf(
            "Steve" to "Linda, the Sunday roast needs a rehearsal. Wednesday. The chicken's available and the boy has opinions.",
            "Linda Black" to "The chicken is a draft, Steve, and Tolkien's notes are kind but firm. Rehearse Thursday. Wednesday the house rests — even drafts need a day the welcome just sits.",
            "Steve" to "Thursday, then. And you'll tell me honestly if the gravy—",
            "Linda Black" to "The gravy's honest when I'm honest, chef. It's the house's whole system: you cook it true, I say it true, the boy eats it true."),
        listOf(
            "Linda Black" to "Three of Tolkien's friends are staying for dinner. I didn't ask; the welcome doesn't ask. It sets places.",
            "Steve" to "Three more. The roast appreciates, at least. The roast has never once complained about this house's policy.",
            "Linda Black" to "The roast knows its role, darling. We all do. That's why the table works.")),
    setOf("Linda Black", "Tolkien") to listOf(
        listOf(
            "Tolkien" to "Mom, Jason's coming over Saturday. And — before you ask — yes, I told him about the reheated roast situation.",
            "Linda Black" to "Then Jason comes to this house informed, which is more than most guests get anywhere. He'll be fed at Black standard, Tolkien. Notes or no notes.",
            "Tolkien" to "The notes were KIND, Mom.",
            "Linda Black" to "The notes were yours, which is the same thing said slower. Set his place by yours. Friends sit close at this table."),
        listOf(
            "Linda Black" to "Your father is rehearsing Sunday on a Wednesday chicken again. I need your honest notes, but I need them gentle.",
            "Tolkien" to "Mom, my notes are always gentle. They're just also… specific. He respects specific. It's a chef thing and a Black thing.",
            "Linda Black" to "Specific and gentle. That's the whole house in two words, baby. Write them exactly like that.")),
    setOf("Harriet", "Henrietta") to listOf(
        listOf(
            "Henrietta" to "Mother. You knocked. You PRAISED. The whole cul-de-sac heard the praising. My door is not a town square.",
            "Harriet" to "Your door is where the poems live, Henrietta, and I will praise at its border like a woman watering a dark garden. The garden is magnificent. The garden KNOWS.",
            "Henrietta" to "…You read the new one. The roof one. Nobody was supposed to— it's not finished. It's barely even black yet.",
            "Harriet" to "It's finished enough to make a roof cry at the kitchen table, darling. Take the cocoa. Finish it on the good paper your father bought. He buys it by the case now. In bulk. Like eyeliner."),
        listOf(
            "Harriet" to "The camp sent a survey, Henrietta. I wanted you to know before it hit the recycling: we answered it together this time. Your father wrote 'would not recommend' in his best hand.",
            "Henrietta" to "…His best handwriting is atrocious. But it's a start. The camp was your worst poem, Mother. You've been writing better ones since. With doors. And cocoa.",
            "Harriet" to "That's the kindest review this house has ever had. I'm framing it next to the thunder.")),
    setOf("Harriet", "Mr. Biggle") to listOf(
        listOf(
            "Mr. Biggle" to "Harriet. The roof poem. 'The quiet man downstairs.' That's me. I'm in the work and the work is good.",
            "Harriet" to "You're in the work because you're in the house, love. She writes what's load-bearing. Roofs and quiet men and one small glowing boy.",
            "Mr. Biggle" to "I read it twice. Once for the man, once for the father. The father had to sit in the truck for a while. Professionally.",
            "Harriet" to "Then sit. The thunder's loud because it's TRUE, and you're the truest quiet thing it knows. She'll write you a door next. Watch."),
        listOf(
            "Harriet" to "Bradley asked for black curtains today. 'Like Henrietta's.' The goth is spreading by love, not by phase.",
            "Mr. Biggle" to "Ordered two sets before you finished the sentence. Procurement is my praise, Harriet. She gets paper, he gets curtains, you get the roof. Everybody's in the work.",
            "Harriet" to "The whole house is in the work. That's the review we're all living for, isn't it.")),
    setOf("Mr. Biggle", "Bradley") to listOf(
        listOf(
            "Bradley" to "Dad, I helped with the board today. The small wrench. You let me hold the small wrench like it mattered.",
            "Mr. Biggle" to "It mattered, Bradley. A board held by two is a board held twice as true. That's carpentry and that's the other thing too.",
            "Bradley" to "The other thing being family. I know. Henrietta explained it in a poem. The poem had a wrench in it. I think the wrench was me.",
            "Mr. Biggle" to "The wrench is you, son. Chosen, apprenticed, and in the work. Now hold the light while I finish this — partners hold the light."),
        listOf(
            "Mr. Biggle" to "Your sister's curtains are up. Yours arrive Thursday. Black, like hers. She noticed before I did that you'd want them.",
            "Bradley" to "She notices everything. She just files it under thunder. …Thanks, Dad. For the curtains and the telling me she picked them.",
            "Mr. Biggle" to "She picked them. I procured them. Your mother will praise them through two doors. The Biggle system, son. It works because every part of it is love.")),
    setOf("Mr. Turner", "Heidi") to listOf(
        listOf(
            "Heidi" to "Dad. The dance. I know there's a briefing. Mom hid one of your chaperone applications, so you're down to two. Let's negotiate like nations.",
            "Mr. Turner" to "Two applications is still a presence, Heidi. Terms: I chaperone the gym perimeter only. No dancing. No speeches. One (1) slow patrol per hour.",
            "Heidi" to "One patrol per hour, and you wear the good shirt, not the security one. And if a boy asks me to dance you count to fifty before the perimeter 'checks the doors.'",
            "Mr. Turner" to "…Fifty is a long count, Heidi. Twenty. And the count is silent. And your mother reviews the shirt. Those are my final terms, and they're love, and we both know it."),
        listOf(
            "Heidi" to "Dad, you checked the locks twice tonight. I counted. From my window. Because I was ALSO checking the street. We're the same, you know.",
            "Mr. Turner" to "You're the perimeter's heir, Heidi. The counting, the checking — it skips your mother's side entirely. She built the door; you'll run both.",
            "Heidi" to "I'll run both, Dad. And the gate will sing, and the locks will hold, and somewhere in it there'll be a flower. That's the Turner future.",
            "Mr. Turner" to "…A flower, on the future gate. Noted in the briefing. Approved by the perimeter. God help the future.")),
    setOf("Nancy", "Heidi") to listOf(
        listOf(
            "Nancy" to "Heidi, baby, the dance terms are settled. Your father gets one silent patrol an hour and the good shirt. I negotiated the shirt personally. It's the soft one.",
            "Heidi" to "Mom, you're the only diplomat alive who could get the perimeter into the soft shirt. The UN should study you.",
            "Nancy" to "The UN couldn't afford me. Now — the real talk. You shine at that dance like the gate's wide open, because it is. That's the whole design, baby. Strong AND open.",
            "Heidi" to "Strong and open. Wall and door. I know, Mom. I'm literally your daughter."),
        listOf(
            "Heidi" to "Mom, a girl at school called Dad 'the fortress' like it's an insult.",
            "Nancy" to "And you told her the fortress has a cookie jar and a flower on the gate and a daughter who runs both. Tell me you told her.",
            "Heidi" to "Word for word. She asked for the tour. I said tours are Saturdays.",
            "Nancy" to "My girl. The hearth wins again.")),
    setOf("Mr. Stevens", "Bebe") to listOf(
        listOf(
            "Bebe" to "Dad. Entrance rating. Be honest — the honest one, not the kind one. Mom says you have both and I should learn to ask for the right one.",
            "Mr. Stevens" to "Honest, then: the entrance was a nine. The confidence was a ten. The confidence is the part they'll remember, Bebe. It is every time.",
            "Bebe" to "A nine and a ten. The nine is fixable — shoes. The ten is inherited — also shoes, but Mom's side. Thanks, Dad. The umbrella of reviews has spoken.",
            "Mr. Stevens" to "The umbrella is honored. Shoes, Bebe. It's always shoes with you two. I just hold things and learn."),
        listOf(
            "Mr. Stevens" to "Your mother rated my coat-holding at the school thing. She gave it a nine. In twenty years that's my highest Stevens score.",
            "Bebe" to "Dad, a nine from Mom is a trophy. She gave my first-grade entrance a six. A SIX. I still think about it. The nine means you're peaking.",
            "Mr. Stevens" to "Peaking at coat-holding, at my age. The Stevens standard provides a summit for everyone. Yours is just higher and better lit.")),
    setOf("Mrs. Stevens", "Bebe") to listOf(
        listOf(
            "Mrs. Stevens" to "Bebe. The eyebrow. Show me the eyebrow. …There. The fourth grade doesn't stand a chance and neither did I at your age. We're even.",
            "Bebe" to "Mom, a girl at school said my entrances are 'a whole event.' I said thank you and that it's hereditary and she should watch the original sometime.",
            "Mrs. Stevens" to "The original performs daily at the grocery, darling. Bring her. I'll enter by the produce and she'll understand everything about you in nine seconds.",
            "Bebe" to "Nine seconds is generous. The bakery took four. The baker still talks about it. We're a local landmark with a daughter."),
        listOf(
            "Bebe" to "Mom, Sophie's the new bureau and Jenny runs the archive. Where do I sit in the actual government? Wendy says 'the weather.'",
            "Mrs. Stevens" to "The weather is the government, darling. Laws are paper; weather is what everyone plans around. Be the weather. Your mother is a climate and it has never once apologized.",
            "Bebe" to "A climate. With a daughter-shaped storm system. …I love our family. We're a whole forecast of women.")),
    setOf("Mr. Stoley", "Kevin") to listOf(
        listOf(
            "Kevin" to "Dad. Be honest with me — man to man, no production. The future self guy. Was he… a guy?",
            "Mr. Stoley" to "Kevin. Son. He was an actor with a magnificent jaw and a contract. Your mother directed him. I funded him. YOU were the point of him.",
            "Kevin" to "I knew it by fifth grade, you know. I just… liked the story better with him in it. It meant somebody cared enough to build a whole future to scare me straight.",
            "Mr. Stoley" to "Somebody did, son. Two somebodies. The garage still has the wig if you ever need to scare YOUR kid. It's the family silver now."),
        listOf(
            "Mr. Stoley" to "Kevin, your teachers call you unshakeable. I want it minuted at this table that unshakeable was a co-production.",
            "Kevin" to "Minuted, Dad. Co-produced by Stoley and Stoley, directed by Mom, starring a jaw. I'll put it in my memoir. You're both getting chapters.",
            "Mr. Stoley" to "Chapters! Your mother will cry at the premiere. She cries at all our premieres. It's how the reviews get written.")),
    setOf("Mrs. Stoley", "Kevin") to listOf(
        listOf(
            "Mrs. Stoley" to "Kevin, darling, you told your friend our house is 'produced.' I've been thinking about it all day. It's the best review of my life.",
            "Kevin" to "It's true, Mom. Everything here is on purpose. Even dinner has blocking. I don't mind it. I mind it so little I do it now too.",
            "Mrs. Stoley" to "Then the production worked past its script, which is all a director ever prays for. You were never the audience, Kevin. You were always the point.",
            "Kevin" to "I know, Mom. I think I always knew. The knowing was just backstage till now."),
        listOf(
            "Kevin" to "Mom, if I ever write the memoir, you get the director's commentary track. Dad gets the producer credit and the garage gets a chapter.",
            "Mrs. Stoley" to "The garage gets TWO chapters and you know it. And I get the front row at the premiere. Front row, centre — Sarah Valmer's seat, but for mothers of the point.",
            "Kevin" to "Front row, centre. It's yours. It was always yours. The seat was the scheme's first prop, Mom. I see it now.")),
    setOf("Mr. Stoley", "Mrs. Stoley") to listOf(
        listOf(
            "Mr. Stoley" to "He knows about the actor, love. He asked man to man. I told him man to man. He smiled like the end of a good play.",
            "Mrs. Stoley" to "Then the production closes as it should: with the point smiling. Ten years, a wig, and a steady boy. Best run this town's ever had, and nobody bought a ticket.",
            "Mr. Stoley" to "The sequel's his to write. I've cleared the garage shelf for it.",
            "Mrs. Stoley" to "Clear two shelves, darling. He's a Stoley. The sequel will have props."),
        listOf(
            "Mrs. Stoley" to "The actor sent another card. 'The boy steady?' Just that. I wrote back: 'The boy is the steadiest thing in this town.'",
            "Mr. Stoley" to "Put his card with the others. The fridge is our archive and the archive is full of people our love hired, fed, or directed. It's a good fridge.",
            "Mrs. Stoley" to "It's the best fridge. It's basically the cast list of Kevin.")),
    setOf("Clark", "Scott") to listOf(
        listOf(
            "Scott" to "Dad, the other kids' dads have the streaming bundle. Twelve tiers. TWELVE. Gary's dad pays for channels he can't name.",
            "Clark" to "And you'll have three channels and your dignity, Scott. A boy who can pick from three can pick from anything. A boy with twelve tiers picks nothing and pays forever.",
            "Scott" to "I picked a movie in four seconds yesterday. FOUR. Sophie timed it. The Scottsdale kids take twenty minutes and a summit.",
            "Clark" to "FOUR SECONDS. That's my boy. That's the war won in the next generation. Put it in the family record: Malkinson, decisive, hereditary."),
        listOf(
            "Clark" to "Scott, your pump handled the whole field trip and you handled the pump. I ever tell you you're the toughest Malkinson?",
            "Scott" to "You tell the antenna, Dad. I hear you telling the antenna. It's okay. I know the antenna is just you practicing saying it to me.",
            "Clark" to "…The antenna's a good listener, son. But you heard it from me now, direct, no static: toughest Malkinson. The record's clear.")),
    setOf("Maggie", "Yates") to listOf(
        listOf(
            "Yates" to "Maggie. The marquee case. I've run the ladder's alibi six ways and the ladder WON'T CRACK. Ladders crack, Maggie. This one has representation.",
            "Maggie" to "The ladder has ME, Harrison. I asked it one question at the scene — nicely, over the fence — and it confessed it was holding a banner. Cases crack at kitchen tables more than interrogation rooms.",
            "Yates" to "…The banner. Of course. The ladder was an ACCOMPLICE to signage. Mitch! The ladder is signage-adjacent! Maggie, the department owes you a— the department owes you everything. Again.",
            "Maggie" to "The department owes me a quiet evening, Harrison. Pay it Friday. The kettle accepts installments."),
        listOf(
            "Maggie" to "You paced till two again. The case or the ducks?",
            "Yates" to "The case. The ducks are Mitch's jurisdiction and Mitch has them in a drawer. A man can only pace about one jurisdiction, Maggie.",
            "Maggie" to "Then pace it here, at the table, with the tea. Pacing's just thinking with mileage, and this house has better flooring for it than the station.",
            "Yates" to "…The flooring IS better. The tea is better. The questions are better. If the force ever loses me, Maggie, it's to this kitchen and I'll go quietly.",
            "Maggie" to "The force isn't losing you. I already decided that once, in the Jeffersons year. Decisions in this house are load-bearing.")),
    setOf("Mitch", "Yates") to listOf(
        listOf(
            "Yates" to "Mitch. The pond ducks. Final ruling. I've been lenient because they're ducks, but leniency has a statute and it's run out.",
            "Mitch" to "Filed, sir. The ducks' file is the thickest in the department, Sergeant. Thicker than the ladder's. Thicker than Barbrady's crayon period, and that's saying something.",
            "Yates" to "Then it's an empire, Mitch. A quiet, feathered empire, and we're the only two men in the county who see it.",
            "Mitch" to "We see it, sir. We stamp what we see. That's the whole department: seeing, stamping, and Maggie's soup on Fridays."),
        listOf(
            "Mitch" to "Sergeant, the marquee timeline has a hole at six-forty. I've put a pin in it. The pin says 'ask Maggie.'",
            "Yates" to "You put my wife in the timeline as an investigative step. In writing. In the official file.",
            "Mitch" to "Sir, she's cracked more of your cases from that kitchen than this building has from its desks. The file should reflect the department's actual structure.",
            "Yates" to "…Leave the pin. But if the chief asks, the pin was my idea. It's a good idea. It's usually her ideas that are good. LEAVE THE PIN, MITCH.")),
    setOf("Mitch", "Barbrady") to listOf(
        listOf(
            "Barbrady" to "Mitch! I have completed the report on the thing! It is a full page! There are no crayons on it this time because I used the pen like a professional!",
            "Mitch" to "Received, Officer. I'll translate— review it by morning. The pen work is noted and appreciated. The department thanks you.",
            "Barbrady" to "The report says the thing was a ladder, Mitch. I wrote 'ladder' in the pen. The sergeant will be so relieved. He had theories about the ladder.",
            "Mitch" to "He had six, Officer. Your pen just closed five of them. That's not paperwork, Barbrady. That's policing."),
        listOf(
            "Mitch" to "Officer, the chief's office asked who keeps this department's memory. I gave them your name for morale and mine for the files.",
            "Barbrady" to "I am the morale?! Mitch, I have been promoted in my heart! I shall write it in the pen in my own report!",
            "Mitch" to "Write it true, Officer. You are the morale. The files are just where I keep the proof.")),
    setOf("Bob Johnson", "McDaniels") to listOf(
        listOf(
            "McDaniels" to "Johnson. The transit announcement Thursday. I'll be presenting the city's long-standing transit vision. Have your — the — MY notes ready.",
            "Bob Johnson" to "Your vision, Mayor. Dated March, initialed BJ, in your folder since spring. The vision is ready. The vision has always been ready.",
            "McDaniels" to "You know, Johnson, sometimes I think this office would run perfectly well if—",
            "Bob Johnson" to "It would, Mayor. That's my whole job: that it would, that it does, and that you announce it. Thursday, then. I'll hold the scissors tray at the ribbon.",
            "McDaniels" to "…Bring the good scissors. And Johnson — the lights memo. Bring that too. It's… timely.",
            "Bob Johnson" to "It's March, Mayor. But yes. Timely."),
        listOf(
            "Bob Johnson" to "Mayor, the school play is Friday. The council's box is held. Your remarks are drafted — two minutes, mountain-themed, very mayoral.",
            "McDaniels" to "You wrote my remarks about a children's play about a MOUNTAIN, Johnson?",
            "Bob Johnson" to "I did, Mayor. Page two has the line about the mountain declining. It's the best line in the speech. It'll be quoted as yours by Saturday.",
            "McDaniels" to "…Hold the box for Janson too. If I'm to be quoted, the staff should hear the quality. Bring the scissors tray. For luck.")),
    setOf("Pusslicker", "Jimmy") to listOf(
        listOf(
            "Jimmy" to "Mr. P-Pusslicker! Sir! I do a news bit in my act now — it's an homage. 'Good evening, I'm Jimmy Valmer, and the news is… it's news!' The crowd work needs a real anchor's blessing.",
            "Pusslicker" to "The blessing is granted, Jimmy, with one note from the desk: the pause. Before the headline, the pause. The pause is where the town holds its breath. Comedy and news are the same desk at different hours, son.",
            "Jimmy" to "The p-pause. I've been rushing the pause my whole career. Mr. Pusslicker, you've just fixed my act and the town's evenings in one note. Wh-what a great mentor.",
            "Pusslicker" to "Use it well. Camera two is for the town. Laughter is for the town too. We work the same side of the desk."),
        listOf(
            "Pusslicker" to "Jimmy, your fundraiser in the spring — the desk will cover it. The confetti situation will be reported gravely. As is tradition.",
            "Jimmy" to "G-gravely! Sir, Nathan's already filed a complaint with my mom's notebook about the confetti budget. If Channel 4 covers it gravely, the confetti becomes HISTORY.",
            "Pusslicker" to "Everything covered gravely becomes history, Jimmy. That's the whole trick of the desk. Make it good history.")),
    setOf("Dr. Doctor", "Randy") to listOf(
        listOf(
            "Randy" to "Doc. Doc, be honest. The chart. How many pages is the chart now? Sharon says it's a binder. Sharon says it's TWO binders.",
            "Dr. Doctor" to "Randy. The chart is a biography at this point. I've started giving it chapter titles. This year's is 'The Geologist, Indoors.' Sit down; you have a sprain from farming dramatically.",
            "Randy" to "I farm with commitment, Doc. It's called Tegridy. The sprain has Tegridy. Put that in the chapter.",
            "Dr. Doctor" to "It's in the chapter, Randy. Everything's in the chapter. When they write this town's medical history, your binder is the spine. Literally, in your case."),
        listOf(
            "Dr. Doctor" to "Randy, I say this as your doctor of twenty years: the harmonica incident was 2009. I bring it up because the chart demands an annual reading.",
            "Randy" to "IT WAS A BET, DOC. A bet with stakes. A man honors a bet. The harmonica was the stakes. History will vindicate the bet.",
            "Dr. Doctor" to "History has a binder, Randy, and it's not vindicating. Ice the wrist. Ice is the whole prescription. The wrist, this time; the bet is beyond medicine.")),
    setOf("Choksondik", "Garrison") to listOf(
        listOf(
            "Garrison" to "Diane. I trust the classroom is exactly as I left it in your care. The children require a firm—",
            "Choksondik" to "Herbert, the classroom required a curriculum while you were finding yourself, and it got one. Mine. The succession is: your chaos, my order, your chaos with notes. I am the notes, and the notes are thorough.",
            "Garrison" to "Mr. Hat says you were the best teacher this school ever— Mr. Hat says NOTHING. Mr. Hat is being difficult. The puppet is grieving the comparison, Diane.",
            "Choksondik" to "The puppet may grieve in the corridor. In MY classroom we grieve on schedule, with the work turned in. Tell him the fourth period sends its regards."),
        listOf(
            "Choksondik" to "Herbert, a student asked who taught the famous years. I said: a woman whose name the town still says carefully. He said it perfectly on the first try. The standard walks.",
            "Garrison" to "They say MY name carefully too, Diane. For entirely different reasons, granted. Reasons involving a puppet and several superintendents.",
            "Choksondik" to "Carefully is carefully, Herbert. The town's respect has many doors. Yours has a puppet flap. Take the respect where it stands.",
            "Garrison" to "…The puppet flap. Fine. Fine! The succession acknowledges its middle term. The middle term was distinguished. THERE. Mr. Hat made me say it.")),
    setOf("Choksondik", "Stan") to listOf(
        listOf(
            "Stan" to "Ms. Choksondik. You said my name right. Garrison still calls me 'the Marsh boy' when he's losing an argument with the puppet.",
            "Choksondik" to "Stanley. In my classroom, names were said correctly and completely, because a name said right is a standard kept. Your class walked taller for it. Some of you still do.",
            "Stan" to "The unit, though. The famous unit. Kyle still can't talk about the unit without leaving the room.",
            "Choksondik" to "Kyle Broflovski left my classroom knowing more than his father learned in forty years, and it only cost him one permanent blush. Education is a bargain at twice the embarrassment, Stanley.",
            "Stan" to "…That's the most teacher thing anyone's ever said to me. I kind of miss it. Don't tell Garrison. The puppet would make it a whole thing."),
        listOf(
            "Choksondik" to "Your class produced a mayor-maker, a list-keeper, and a boy who talks to animals about farming, Stanley. I take partial credit and full attendance records.",
            "Stan" to "We were a lot, huh.",
            "Choksondik" to "You were a curriculum's worth of a lot. And I taught all of it without blinking. The blinking, as established, was outsourced to the students.")),
    setOf("Strong Woman", "Victoria") to listOf(
        listOf(
            "Victoria" to "Strong Woman. The assembly ran long, the fourth grade ran longer, and yet the building held. My compliments to the standard.",
            "Strong Woman" to "Principal. The building held because it was held from both ends — your schedule at the top, my standard at the door. The children know the shape of the day. Shape is safety.",
            "Victoria" to "In my school, the forms are the shape. But I'll allow that a door with a standard behind it files the children more gently than a form does.",
            "Strong Woman" to "Then we agree: the school is a carried thing, and we are its two arms. Yours holds the clipboard. Mine holds everything else."),
        listOf(
            "Strong Woman" to "Principal, a fourth-grader flexed at me in the hall today. I spotted his form and sent him on ten feet tall. I recommend we count it as the PE curriculum working.",
            "Victoria" to "Noted and minuted: 'the standard reproduces itself.' That's the whole job, isn't it — the standard, reproducing, until the building runs on it alone.",
            "Strong Woman" to "Until then, Principal, we carry it in shifts. Yours is the longer one. I've read the schedule. I hold the door for it gladly.")),
    setOf("Strong Woman", "Mackey") to listOf(
        listOf(
            "Mackey" to "Strong Woman, the children seem… settled today, m'kay. Settled is new. Settled is suspicious. What did you do, exactly, at the door this morning?",
            "Strong Woman" to "I stood there, Mr. Mackey. Shoulders back, standard up, every child seen by name. Children settle for being seen. It's not a technique; it's a posture with a memory.",
            "Mackey" to "A posture with a memory, m'kay. I'm putting that in the counseling materials. The children could use… m'kay, they could use being seen. We all could, m'kay.",
            "Strong Woman" to "Then stand a little taller yourself, counselor. The building's staff is also the building's standard. I'll spot your form at the door tomorrow."),
        listOf(
            "Strong Woman" to "Mr. Mackey, the fourth grade's worst day got carried to your office by two of its friends today. The standard reproduces, counselor. Even the carrying.",
            "Mackey" to "They carried him, m'kay. In my day the children carried grudges, not each other. …M'kay, that's going in the materials too. Twice.")
            )
)
