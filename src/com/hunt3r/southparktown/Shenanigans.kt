package com.hunt3r.southparktown

// ============================================================
// Shenanigans — the weekly-to-monthly town-wide event system.
//
// Lucky's rule: stories must be at least a little LINEAR. So an
// event is not a toggle — it's a 4-stage mini-arc that unfolds
// across one in-game day on the 40-minute clock:
//   stage 0 KICKOFF   (00:00-06:00) the boys set it in motion
//   stage 1 ESCALATION(06:00-12:00) the town starts giving in
//   stage 2 CLIMAX    (12:00-18:00) peak chaos
//   stage 3 RESOLUTION(18:00-24:00) it blows over; town settles
// At the next midnight the event ends, routines restore (they
// were only ever computed through the stage transform), and it
// may leave drift residue + faction shifts behind.
//
// Cadence: rolled on each new in-game day with a week gate —
// at most one event per in-game week, ~50% roll when eligible,
// and GUARANTEED by day 20 of a month if none has run yet.
//
// Everything is data: stages carry routine transforms, dialogue
// tone, weather locks, faction effects, litter bursts and the
// quest-tracker theme, so new events plug in by adding a def.
// ============================================================

data class QuestTheme(
    val icon: String,
    val colorHex: String,
    val header: String
)

data class FactionEffect(
    val factionId: String,
    val influenceDelta: Int = 0,
    val standingDelta: Int = 0,
    val relationWith: String? = null,
    val relationDelta: Int = 0,
    val note: String = ""
)

data class ShenaniganStage(
    val title: String,
    val banner: String,          // in-game banner / ticker text
    val journalNote: String,     // Journal + tracker flavor
    val objective: String,       // quest tracker current objective
    val routine: String = "normal", // normal | cluster | homes | invert
    val clusterLoc: String? = null,
    val clusterActivity: String = "",
    val tone: String = "none",   // none | pirate | whisper | fad | hype
    val weatherLock: String? = null,
    val litterBurst: Int = 0,
    val factionEffects: List<FactionEffect> = emptyList()
)

data class ShenaniganDef(
    val id: String,
    val title: String,
    val cause: String,           // who started it
    val blurb: String,
    val theme: QuestTheme,
    val tintHex: String? = null, // subtle scene tint while active
    val stages: List<ShenaniganStage>, // exactly 4
    // ---- v0.1.9c episode framing (presentation data only) ----
    val causers: List<String> = emptyList(),      // credited on the title card
    val episodeTitles: List<String> = emptyList(), // rotating alt episode titles
    val coldOpen: List<Line> = emptyList()         // opening-beat lines, if you're there
)

val DEFAULT_QUEST_THEME = QuestTheme("📋", "#f2c230", "TOWN BOARD")

val SHENANIGANS: List<ShenaniganDef> = listOf(
    ShenaniganDef(
        id = "chinpo_craze",
        title = "The Great Chinpokomon Craze",
        cause = "Cartman cornered the town's Chinpokomon supply and started scalping.",
        blurb = "A new Chinpokomon wave hits town and every kid loses their mind at once.",
        theme = QuestTheme("🐭", "#e8c53a", "CHINPOKOMON CRAZE"),
        stages = listOf(
            ShenaniganStage("Kickoff: The Drop",
                "🐭 CHINPOKOMON CRAZE: Cartman's selling 'rare' Chinpokomon behind the school!",
                "Word is Cartman got a shipment of 'ultra-rare' Chinpokomon. The line starts at the school dumpster.",
                "Check out the fuss at the Mall — everyone's hunting Chinpokomon.",
                routine = "cluster", clusterLoc = "Mall",
                clusterActivity = "Hunting Chinpokomon. Gotta buy 'em all.",
                tone = "fad",
                factionEffects = listOf(
                    FactionEffect("boys_crew", influenceDelta = 6, note = "The Boys' Crew is running the Chinpokomon market."))),
            ShenaniganStage("Escalation: Gotta Buy 'Em All",
                "🐭 The craze spreads — kids are trading lunches for Chinpokomon!",
                "Trading has turned cutthroat. Butters traded his lunch, his backup lunch, and next Tuesday's lunch.",
                "Watch the trading frenzy at the Mall.",
                routine = "cluster", clusterLoc = "Mall",
                clusterActivity = "Trading Chinpokomon like stockbrokers.",
                tone = "fad", litterBurst = 2,
                factionEffects = listOf(
                    FactionEffect("girls_circle", influenceDelta = -4, note = "The Girls' Circle is officially Over It."))),
            ShenaniganStage("Climax: The Ultra-Rare Drop",
                "🐭 CLIMAX: Cartman announces ONE ultra-rare Chinpokomon. Main Street is a war zone!",
                "Cartman is auctioning the 'ultra-rare' one on Main Street. Stan suspects it's a regular one with glitter on it. It is.",
                "Get to Main Street for the ultra-rare auction.",
                routine = "cluster", clusterLoc = "MainStreet",
                clusterActivity = "Bidding everything on the ultra-rare Chinpokomon.",
                tone = "hype", litterBurst = 3,
                factionEffects = listOf(
                    FactionEffect("boys_crew", influenceDelta = 8, relationWith = "girls_circle",
                        relationDelta = -1, note = "The auction split the Crew and the Girls' Circle."))),
            ShenaniganStage("Resolution: Buyer's Remorse",
                "🐭 The craze deflates. It's just a penguin with glitter, you guys.",
                "Kyle proved the ultra-rare was a Pengin with craft glitter. Cartman denies everything. The town drifts home.",
                "Watch the town come down from the craze.",
                routine = "normal", tone = "none",
                factionEffects = listOf(
                    FactionEffect("boys_crew", influenceDelta = -5, note = "The Crew's market cred took a hit.")))
        ),
        causers = listOf("Cartman", "Kevin", "Red"),
        episodeTitles = listOf("Gotta Buy 'Em All", "The Pengin With Glitter On It"),
        coldOpen = listOf(
            Line("Cartman", "I cornered the town's whole Chinpokomon supply. By lunch, I own this town."),
            Line("Kyle", "It's a stuffed penguin, Cartman."),
            Line("Cartman", "An ULTRA-RARE stuffed penguin."))
    ),
    ShenaniganDef(
        id = "pirate_week",
        title = "Talk Like a Pirate Week",
        cause = "The boys bet Butters he couldn't stay in pirate character for a whole day. He could. It spread.",
        blurb = "One bet later, the entire town is legally obligated to talk like pirates.",
        theme = QuestTheme("🏴‍☠️", "#8a6a3a", "PIRATE WEEK"),
        tintHex = "#3a5a8a",
        stages = listOf(
            ShenaniganStage("Kickoff: The Bet",
                "🏴‍☠️ PIRATE WEEK: Butters won't stop saying 'arr'. It's spreading.",
                "Butters is winning the bet. Stan has started saying 'arr' by accident. Kyle is furious about it.",
                "Talk to people — everyone's going pirate.",
                routine = "normal", tone = "pirate"),
            ShenaniganStage("Escalation: Arr Takes Over",
                "🏴‍☠️ Half the town is in pirate speak. The teachers have given up correcting it.",
                "Garrison tried to ban pirate talk and got voted off his own classroom's plank.",
                "Hear the arr-pocalypse spread through town.",
                routine = "normal", tone = "pirate", litterBurst = 1,
                factionEffects = listOf(
                    FactionEffect("school_staff", influenceDelta = -5, note = "School Staff authority is walking the plank."))),
            ShenaniganStage("Climax: The Great Treasure Hunt",
                "🏴‍☠️ CLIMAX: Someone buried 'treasure' at the park. The whole town is digging!",
                "Cartman swore there's real treasure at Kid's Park. There is not. Nobody cares. Digging is fun.",
                "Join the treasure hunt at Kid's Park.",
                routine = "cluster", clusterLoc = "KidsPark",
                clusterActivity = "Digging for Cartman's definitely-real treasure.",
                tone = "pirate", litterBurst = 3,
                factionEffects = listOf(
                    FactionEffect("boys_crew", influenceDelta = 5, relationWith = "girls_circle",
                        relationDelta = -1, note = "The Crew hogged the treasure map; the Girls' Circle noticed."))),
            ShenaniganStage("Resolution: Landlubbers Again",
                "🏴‍☠️ The 'treasure' was a lunchbox of old batteries. Pirate week sinks quietly.",
                "Butters won the bet. He spent the winnings on a foam parrot. Worth it.",
                "Watch the town return to landlubber life.",
                routine = "normal", tone = "none",
                factionEffects = listOf(
                    FactionEffect("school_staff", influenceDelta = 3, note = "School Staff authority restored. Barely.")))
        ),
        causers = listOf("Butters", "Stan", "Kyle", "Nichole"),
        episodeTitles = listOf("Arr, There Be Batteries", "Butters Wins The Bet"),
        coldOpen = listOf(
            Line("Butters", "Arr. Arr arr arr. I can do this all day, fellas."),
            Line("Stan", "He's been saying it since breakfast. It's spreading."))
    ),
    ShenaniganDef(
        id = "upside_down_day",
        title = "Upside-Down Day",
        cause = "Stan bet the town couldn't function if everyone swapped routines for a day. The town took the bet.",
        blurb = "Morning people live at night, workers play, kids run errands. The schedule is upside down.",
        theme = QuestTheme("🙃", "#6a4a8a", "UPSIDE-DOWN DAY"),
        stages = listOf(
            ShenaniganStage("Kickoff: The Swap",
                "🙃 UPSIDE-DOWN DAY: The town agreed to swap routines. This will go great.",
                "The bet is on: everyone swaps their day around. Mackey is already confused.",
                "Find people — nobody's where they should be.",
                routine = "normal", tone = "hype"),
            ShenaniganStage("Escalation: Nobody's Anywhere",
                "🙃 Routines are fully inverted. The bar is full at breakfast. The school is napping.",
                "Adults are at recess. Kids are doing taxes. South Park has never run worse, or happier.",
                "Visit a few spots and enjoy the wrongness.",
                routine = "invert", tone = "hype", litterBurst = 2,
                factionEffects = listOf(
                    FactionEffect("craigs_gang", influenceDelta = 5, note = "Craig's Gang thrives in chaos."),
                    FactionEffect("school_staff", influenceDelta = -5, note = "School Staff is upside-down too."))),
            ShenaniganStage("Climax: Peak Wrongness",
                "🙃 CLIMAX: The mayor declared recess. For the adults. The kids are holding a town meeting.",
                "The kids' town meeting was somehow more productive than any real one. Motions passed: more recess.",
                "Catch the kids' town meeting energy at City Hall.",
                routine = "invert", clusterLoc = "CityHall",
                clusterActivity = "Holding a town meeting. Being weirdly competent.",
                tone = "hype", litterBurst = 2),
            ShenaniganStage("Resolution: Right Side Up",
                "🙃 The town flips back. Stan won the bet and somehow everyone lost.",
                "Routines restore themselves. Randy refuses to give back the recess bell.",
                "Watch routines settle back to normal.",
                routine = "normal", tone = "none")
        ),
        causers = listOf("Stan", "Kyle", "Cartman", "Kenny", "McDaniels", "Crabtree"),
        episodeTitles = listOf("Recess For The Adults", "The Town Takes The Bet"),
        coldOpen = listOf(
            Line("Stan", "I bet this town couldn't swap routines for one day without catching fire."),
            Line("Kyle", "You're on. Somehow, you're on."))
    ),
    ShenaniganDef(
        id = "tegridy_takeover",
        title = "The Tegridy Town Takeover",
        cause = "Randy launched a Tegridy 'town appreciation day'. The boys are running the merch table.",
        blurb = "Tegridy branding eats the town for a day. Everyone drifts farmward.",
        theme = QuestTheme("🌿", "#7aa84a", "TEGRIDY TAKEOVER"),
        stages = listOf(
            ShenaniganStage("Kickoff: Appreciation Day",
                "🌿 TEGRIDY TAKEOVER: Randy's 'appreciation day' has banners. So many banners.",
                "Randy says it's about community. Towelie says it's about towels. The boys are charging for autographs.",
                "Drift out to Tegridy Farms and see the spectacle.",
                routine = "cluster", clusterLoc = "TegridyFarms",
                clusterActivity = "Appreciating Tegridy. So much appreciating.",
                tone = "hype",
                factionEffects = listOf(
                    FactionEffect("tegridy_crew", influenceDelta = 7, note = "Tegridy Crew is having a day."))),
            ShenaniganStage("Escalation: Farm Fever",
                "🌿 Half the town's at the farm. Skeeter's is empty. The dads have defected.",
                "Sharon has said 'Randy.' in THAT tone four times before noon.",
                "Check the farm crowds — and the empty bar.",
                routine = "cluster", clusterLoc = "TegridyFarms",
                clusterActivity = "Living that farm life. Temporarily.",
                tone = "hype", litterBurst = 3,
                factionEffects = listOf(
                    FactionEffect("parents", influenceDelta = -5, standingDelta = -3,
                        note = "The Parents are split over the takeover."))),
            ShenaniganStage("Climax: The Big Harvest Party",
                "🌿 CLIMAX: Randy's harvest party peaks under a suspiciously perfect sun!",
                "The harvest party is in full swing. Towelie brought towels for everyone. Nobody asked him to.",
                "Party at Tegridy Farms while the sun holds.",
                routine = "cluster", clusterLoc = "TegridyFarms",
                clusterActivity = "Harvest party! (Randy's idea. Obviously.)",
                tone = "hype", weatherLock = "sunny", litterBurst = 3,
                factionEffects = listOf(
                    FactionEffect("tegridy_crew", influenceDelta = 6, relationWith = "parents",
                        relationDelta = -1, note = "Tegridy and the Parents are feuding again."))),
            ShenaniganStage("Resolution: The Come-Down",
                "🌿 The banners come down. Everyone smells faintly of farm. Nobody discusses it.",
                "Randy calls the takeover 'a brand journey'. Sharon calls it 'Tuesday'. The town goes home.",
                "Watch the town drift back from the farm.",
                routine = "normal", tone = "none",
                factionEffects = listOf(
                    FactionEffect("tegridy_crew", influenceDelta = -6, note = "The Tegridy glow fades.")))
        ),
        causers = listOf("Randy", "Towelie", "Ned"),
        episodeTitles = listOf("A Brand Journey", "Banners. So Many Banners."),
        coldOpen = listOf(
            Line("Randy", "Today, this town appreciates Tegridy. There are banners. So many banners."),
            Line("Towelie", "I brought towels. For the appreciation."))
    ),
    ShenaniganDef(
        id = "mole_panic",
        title = "The Great Mole People Panic",
        cause = "Kenny swore he saw something under the ice at Stark's Pond. Cartman turned it into a town panic.",
        blurb = "Rumor says mole people are coming up through the pond ice. The town barricades itself indoors.",
        theme = QuestTheme("🕳️", "#5a8ac8", "MOLE PEOPLE PANIC"),
        tintHex = "#2a3a5a",
        stages = listOf(
            ShenaniganStage("Kickoff: Something Under the Ice",
                "🕳️ MOLE PANIC: Kenny saw something move under the pond ice. Probably.",
                "Kenny's story has grown in the retelling. It now has 'a thousand teeth' and 'a union'.",
                "Check Stark's Pond — if you dare.",
                routine = "normal", tone = "none"),
            ShenaniganStage("Escalation: Barricades",
                "🕳️ The town is barricading indoors. Father Maxi is doing a brisk business in blessings.",
                "Windows are being boarded with varying competence. Randy's barricade is a lawn chair.",
                "Visit people indoors — everyone's hiding.",
                routine = "homes", tone = "whisper", litterBurst = 1,
                factionEffects = listOf(
                    FactionEffect("church_faithful", influenceDelta = 7, note = "Fear is good for the Church's numbers."))),
            ShenaniganStage("Climax: The Knocking",
                "🕳️ CLIMAX: There was KNOCKING under the floor at the Church. The town is holding its breath.",
                "The knocking was the Church's old pipes. The pipes have been exorcised, just in case.",
                "Gather at the Church for the big reveal.",
                routine = "cluster", clusterLoc = "Church",
                clusterActivity = "Investigating the knocking. Bravely. From behind the pews.",
                tone = "whisper", litterBurst = 2,
                factionEffects = listOf(
                    FactionEffect("church_faithful", influenceDelta = 5, note = "The Church handled the 'crisis'."))),
            ShenaniganStage("Resolution: Just Pipes",
                "🕳️ It was pipes. It was always pipes. The town un-barricades, sheepish.",
                "Cartman insists the mole people 'retreated because of him'. Kenny just smiles. Well. Parka-smiles.",
                "Watch the town come back outside.",
                routine = "normal", tone = "none",
                factionEffects = listOf(
                    FactionEffect("church_faithful", influenceDelta = -4, note = "Panic over; attendance normalizes.")))
        ),
        causers = listOf("Kenny", "Cartman", "Barbrady"),
        episodeTitles = listOf("It Was Pipes", "A Thousand Teeth (Allegedly)"),
        coldOpen = listOf(
            Line("Kenny", "(muffled) I'm telling you guys, something moved under the ice."),
            Line("Cartman", "A thousand teeth, people! Barricade everything!"))
    ),
    ShenaniganDef(
        id = "silent_challenge",
        title = "The Great Silent Challenge",
        cause = "The boys bet a whole pizza on who could stay silent longest. The town joined in.",
        blurb = "A pizza bet turns into a town-wide vow of silence. The goth kids are naturals.",
        theme = QuestTheme("🤫", "#9a9a9a", "SILENT CHALLENGE"),
        stages = listOf(
            ShenaniganStage("Kickoff: The Bet Is On",
                "🤫 SILENT CHALLENGE: shhh. The whole town is doing it. For pizza.",
                "Rules are simple: first to talk, loses. Cartman lasted nine seconds and claims he was 'coughing'.",
                "Talk to people — get whispered replies.",
                routine = "normal", tone = "whisper"),
            ShenaniganStage("Escalation: Charades Economy",
                "🤫 The town now communicates entirely in charades. Commerce continues, badly.",
                "City Wok took an order that was forty minutes of mime. It was wrong. It was eaten anyway.",
                "Enjoy the quiet around town.",
                routine = "cluster", clusterLoc = "MainStreet",
                clusterActivity = "Miming an entire conversation.",
                tone = "whisper", litterBurst = 1,
                factionEffects = listOf(
                    FactionEffect("goth_kids", influenceDelta = 6, note = "The Goth Kids were born for this."))),
            ShenaniganStage("Climax: The Last Two",
                "🤫 CLIMAX: Only Kenny and Henrietta remain. The town gathers to watch them NOT talk.",
                "A silent showdown at the park. Kenny's parka says nothing. Henrietta's stare says less.",
                "Witness the silent showdown at Kid's Park.",
                routine = "cluster", clusterLoc = "KidsPark",
                clusterActivity = "Competing in silence. Elite level.",
                tone = "whisper", litterBurst = 2,
                factionEffects = listOf(
                    FactionEffect("goth_kids", influenceDelta = 4, relationWith = "boys_crew",
                        relationDelta = 1, note = "Respect earned: Goths and Crew bonded over silence."))),
            ShenaniganStage("Resolution: Pizza, Loudly",
                "🤫 Henrietta blinked first — er, talked first. Pizza for Kenny. The town is LOUD again.",
                "Kenny shared the pizza. Character growth. Cartman claims the whole thing was his plan.",
                "Hear the town find its voice again.",
                routine = "normal", tone = "none")
        ),
        causers = listOf("Stan", "Kyle", "Cartman", "Kenny", "Heidi", "DogPoo"),
        episodeTitles = listOf("The Quiet Game", "Nine Seconds (Cartman)"),
        coldOpen = listOf(
            Line("Cartman", "Whole pizza says I out-silence every one of you."),
            Line("Kyle", "Nine seconds. That's his record."))
    ),
    ShenaniganDef(
        id = "coon_sightings",
        title = "The Coon Sighting Panic",
        cause = "Cartman's costumed 'hero patrol' left the town reporting a raccoon vigilante.",
        blurb = "Something with a cape is knocking over trash cans at night. The town is terrified. The raccoons are confused.",
        theme = QuestTheme("🦝", "#4a4a5a", "COON SIGHTINGS"),
        tintHex = "#23232e",
        stages = listOf(
            ShenaniganStage("Kickoff: The First Sighting",
                "🦝 COON SIGHTINGS: Something caped was seen by the park. It had a cape. And claws. Possibly.",
                "A caped figure knocked over three trash cans at Kid's Park. Jimbo is calling it 'basically Bigfoot with a tail'.",
                "Investigate the first sighting at Kid's Park.",
                routine = "cluster", clusterLoc = "KidsPark",
                clusterActivity = "Examining the 'crime scene'. Taking it very seriously.",
                tone = "hype",
                factionEffects = listOf(
                    FactionEffect("boys_crew", influenceDelta = 5, note = "The Boys' Crew is running the investigation — loudly."))),
            ShenaniganStage("Escalation: The Night Watch",
                "🦝 Sightings multiply. Everyone's seen it. Nobody's seen it. Craig's gang has 'questions'.",
                "Craig's gang thinks the Crew is staging the sightings for attention. Which is rich, coming from them.",
                "Walk Main Street and collect 'witness reports'.",
                routine = "cluster", clusterLoc = "MainStreet",
                clusterActivity = "Giving dramatic witness statements about the Coon.",
                tone = "whisper", litterBurst = 2,
                factionEffects = listOf(
                    FactionEffect("craigs_gang", influenceDelta = 4, relationWith = "boys_crew",
                        relationDelta = -1, note = "Craig's Gang is openly calling the Coon a hoax."))),
            ShenaniganStage("Climax: Fog Over Main Street",
                "🦝 CLIMAX: Fog rolls in and the WHOLE TOWN spots the Coon at once. It waves. IT WAVES.",
                "In the fog, a small caped shape crossed Main Street and waved at the crowd. Every can in town is now evidence.",
                "Get to Main Street for the big sighting.",
                routine = "cluster", clusterLoc = "MainStreet",
                clusterActivity = "Watching the fog for the Coon. Screaming, politely.",
                tone = "whisper", weatherLock = "fog", litterBurst = 3,
                factionEffects = listOf(
                    FactionEffect("goth_kids", influenceDelta = -3, note = "The Goth Kids refuse to care, publicly and at length."))),
            ShenaniganStage("Resolution: Just Raccoons",
                "🦝 It was raccoons. Several. In a trench-coat situation nobody will explain. Case closed.",
                "The 'Coon' was three raccoons and a cape someone lost. Cartman is suspiciously unavailable for comment.",
                "Watch the town sweep up the evidence.",
                routine = "normal", tone = "none",
                factionEffects = listOf(
                    FactionEffect("craigs_gang", influenceDelta = 3, relationWith = "boys_crew",
                        relationDelta = 1, note = "Vindicated, Craig's Gang allows the Crew one (1) dignity.")))
        ),
        causers = listOf("Cartman", "Barbrady"),
        episodeTitles = listOf("Basically Bigfoot With A Tail", "Three Raccoons In A Cape"),
        coldOpen = listOf(
            Line("Cartman", "This town needs a hero. A hero with a cape and zero accountability."),
            Line("Kyle", "It's raccoons, Cartman. It's always raccoons."))
    ),
    ShenaniganDef(
        id = "memberberry_fever",
        title = "Memberberry Fever",
        cause = "Randy's nostalgia-berry crop got into the town's water-adjacent snacks. Everyone only talks in flashbacks now.",
        blurb = "A bumper crop of Tegridy 'memberberries makes the whole town talk like a reunion special.",
        theme = QuestTheme("🫐", "#6a4a8a", "MEMBERBERRY FEVER"),
        tintHex = "#4a3560",
        stages = listOf(
            ShenaniganStage("Kickoff: First Berries",
                "🫐 MEMBERBERRY FEVER: Randy's new crop is out. Everyone's saying ''member'. Nobody can stop.",
                "The berries taste like childhood and marketing. The whole farmers' table sold out by nine.",
                "Drift out to Tegridy Farms and try not to 'member anything.",
                routine = "cluster", clusterLoc = "TegridyFarms",
                clusterActivity = "'Membering. Just, so much 'membering.",
                tone = "member",
                factionEffects = listOf(
                    FactionEffect("tegridy_crew", influenceDelta = 6, note = "Tegridy Crew is riding the nostalgia wave."))),
            ShenaniganStage("Escalation: Reunion Town",
                "🫐 The whole town now communicates exclusively in 'member when'. The present is having a day off.",
                "Conversations have stopped moving forward entirely. Sharon tried to discuss dinner and got 1987 back.",
                "Talk to people — if you can get a word in about the past.",
                routine = "cluster", clusterLoc = "MainStreet",
                clusterActivity = "Trading flashbacks like currency.",
                tone = "member", litterBurst = 2,
                factionEffects = listOf(
                    FactionEffect("parents", influenceDelta = 5, note = "The Parents are having the time of their lives, retroactively."))),
            ShenaniganStage("Climax: Peak Nostalgia",
                "🫐 CLIMAX: Peak nostalgia under a perfect sun. The town holds an unplanned class reunion for classes that never existed.",
                "Randy is selling 'vintage' berries at triple price. They are from this morning. Nobody cares. ''Member?'",
                "Join the reunion on Main Street.",
                routine = "cluster", clusterLoc = "MainStreet",
                clusterActivity = "Attending the reunion. Class of Whenever.",
                tone = "member", weatherLock = "sunny", litterBurst = 2,
                factionEffects = listOf(
                    FactionEffect("school_staff", influenceDelta = -4, note = "School Staff cannot teach history to people living in it."))),
            ShenaniganStage("Resolution: Berry Hangover",
                "🫐 The berries wear off. The town wakes up in the present, mildly embarrassed, holding memorabilia.",
                "Everyone agrees the past was great and also that lunch is now. Randy is developing a follow-up crop. Pray.",
                "Watch the town rejoin the present day.",
                routine = "normal", tone = "none",
                factionEffects = listOf(
                    FactionEffect("tegridy_crew", influenceDelta = -5, note = "The memberberry glow fades; the merch remains.")))
        ),
        causers = listOf("Randy", "Towelie", "Mrs. Tweek"),
        episodeTitles = listOf("Class Of Whenever", "I 'Member"),
        coldOpen = listOf(
            Line("Randy", "These berries taste like the good old days, Sharon. THE GOOD OLD DAYS."),
            Line("Towelie", "'Member when I wasn't a towel? ...Me neither."))
    ),
    ShenaniganDef(
        id = "imagination_leak",
        title = "The Imagination Leak",
        cause = "Butters daydreamed too close to Stark's Pond and something imaginary got out.",
        blurb = "There's a leak in the town's imagination and it's dripping into the pond. Only kids can half-see what's loose.",
        theme = QuestTheme("🌈", "#c86ab8", "IMAGINATION LEAK"),
        tintHex = "#3a2a4a",
        stages = listOf(
            ShenaniganStage("Kickoff: Something at the Pond",
                "🌈 IMAGINATION LEAK: Kids swear something shimmering came out of Stark's Pond. Adults see 'a duck, probably'.",
                "Butters apologized to the pond eleven times. The shimmering has not accepted. It left glitter. Actual glitter.",
                "Check Stark's Pond for the shimmering.",
                routine = "cluster", clusterLoc = "StarksPond",
                clusterActivity = "Staring at the shimmering. Describing it worse every time.",
                tone = "whisper",
                factionEffects = listOf(
                    FactionEffect("boys_crew", influenceDelta = 4, note = "The Crew is on imaginary-creature duty."))),
            ShenaniganStage("Escalation: The Fog Gets Ideas",
                "🌈 The fog over the pond is doing shapes now. The town's kids have formed a 'welcoming committee'.",
                "Whatever leaked has been given three names, a fan club, and a parking dispute. Father Maxi is taking notes, alarmed.",
                "Visit the pond in the fog — bring a kid translator.",
                routine = "cluster", clusterLoc = "StarksPond",
                clusterActivity = "Translating for the imaginary. Fluently.",
                tone = "whisper", weatherLock = "fog", litterBurst = 2,
                factionEffects = listOf(
                    FactionEffect("church_faithful", influenceDelta = 5, note = "The Church is monitoring the situation with holy water and pamphlets."))),
            ShenaniganStage("Climax: The Town Decides",
                "🌈 CLIMAX: The town gathers at the Church to decide if the imaginary thing is a miracle, a menace, or a mascot.",
                "The debate format is: one speech, one pie break, one speech. The shimmering has sent glitter as its opening statement.",
                "Gather at the Church for the big decision.",
                routine = "cluster", clusterLoc = "Church",
                clusterActivity = "Debating the imaginary. Pie at intermission.",
                tone = "hype", litterBurst = 2,
                factionEffects = listOf(
                    FactionEffect("goth_kids", influenceDelta = 4, note = "The Goth Kids respect something that refuses to be perceived."))),
            ShenaniganStage("Resolution: Imagined Away",
                "🌈 The shimmering waved once — kids insist — and went back where imaginations live. The pond is just a pond. Probably.",
                "Butters has been grounded from daydreaming near water. The glitter, however, is forever. Glitter is always forever.",
                "Watch the town un-imagine, slowly.",
                routine = "normal", tone = "none",
                factionEffects = listOf(
                    FactionEffect("church_faithful", influenceDelta = -3, note = "Crisis imagined away; attendance normalizes.")))
        ),
        causers = listOf("Butters", "Stan", "Kyle", "Francis"),
        episodeTitles = listOf("A Duck, Probably", "Glitter Is Forever"),
        coldOpen = listOf(
            Line("Butters", "Fellas, I think my daydream got out. It waved at me. From the pond."),
            Line("Stan", "Butters, your imagination is not supposed to have an exit."))
    ),
    ShenaniganDef(
        id = "gold_fever",
        title = "Cash-for-Gold Fever",
        cause = "A screaming TV ad convinced the town that grandma's jewelry is just money wearing a disguise.",
        blurb = "Everyone in town is suddenly a gold prospector with a jewelry box and a folding chair.",
        theme = QuestTheme("🪙", "#c8a028", "GOLD FEVER"),
        stages = listOf(
            ShenaniganStage("Kickoff: The Ad",
                "🪙 GOLD FEVER: The cash-for-gold ad ran six times before breakfast. The town has heard the call.",
                "The ad promised CASH for GOLD in a voice that cannot be argued with. Cartman is already weighing his mom's earrings.",
                "Check the line forming at the Mall.",
                routine = "cluster", clusterLoc = "Mall",
                clusterActivity = "In line with a jewelry box and big dreams.",
                tone = "gold",
                factionEffects = listOf(
                    FactionEffect("boys_crew", influenceDelta = 5, note = "The Boys' Crew is running a competing appraisal empire."))),
            ShenaniganStage("Escalation: Everything Is Gold",
                "🪙 The town is appraising everything now. Fillings are being discussed. FILLINGS.",
                "Stan did the math: the ad pays nine cents on the dollar and a free keychain. The line got LONGER.",
                "Watch the appraisal economy consume the Mall.",
                routine = "cluster", clusterLoc = "Mall",
                clusterActivity = "Getting heirlooms appraised. Loudly.",
                tone = "gold", litterBurst = 3,
                factionEffects = listOf(
                    FactionEffect("parents", influenceDelta = -4, standingDelta = -2,
                        note = "The Parents are discovering what their kids sold."))),
            ShenaniganStage("Climax: The Great Weigh-In",
                "🪙 CLIMAX: The bank hosts the Great Weigh-In. One scale. Four hundred heirlooms. Zero chill.",
                "The weigh-in has a commentator now. It's Jimbo. He's brought a microphone and strong feelings about karats.",
                "Get to the Bank for the Great Weigh-In.",
                routine = "cluster", clusterLoc = "Bank",
                clusterActivity = "Weighing grandma's brooch. For America.",
                tone = "gold", litterBurst = 2,
                factionEffects = listOf(
                    FactionEffect("boys_crew", influenceDelta = 4, relationWith = "parents",
                        relationDelta = -1, note = "The Crew's keychain profits are not winning the Parents over."))),
            ShenaniganStage("Resolution: Nine Cents",
                "🪙 The payouts land: nine cents on the dollar and a keychain. The town holds its heirlooms and its keychains.",
                "Stan's expose ran in the school paper. Nobody read it, but the keychains are genuinely nice. Cartman kept the scale.",
                "Watch the town count its keychains.",
                routine = "normal", tone = "none",
                factionEffects = listOf(
                    FactionEffect("parents", influenceDelta = 3, note = "The Parents have glued the jewelry boxes shut. Healing begins.")))
        ),
        causers = listOf("Stan", "Cartman", "Mr. Tweek"),
        episodeTitles = listOf("Nine Cents On The Dollar", "The Great Weigh-In"),
        coldOpen = listOf(
            Line("Stan", "That ad is a scam, you guys. They pay nine cents on the dollar."),
            Line("Cartman", "Nine cents is nine cents, Stan. That's just math."))
    )
)

// v0.2.0f: the season-mystery chapters (Mystery.ARC_CHAPTERS)
// resolve here too — every id-based lookup (engine, quests,
// episodes, views) treats a chapter exactly like any other
// event. They are NOT in SHENANIGANS, so the random weekly
// roll can never pick one; only Mystery's cadence airs them.
// v0.3.0f: the booth case's two beats (Mystery2.BEATS) join
// on the same terms — slot-rolled by Mystery2's gating only.
// v0.4.0f: the buried case's four chapters (Mystery3.CHAPTERS3)
// join on the same terms again — slot-rolled by Mystery3's
// gating only, behind both earlier cases.
// v0.6.0c: the gardener case's four chapters (Mystery4.CHAPTERS4)
// join on the same terms once more — slot-rolled by Mystery4's
// gating only, behind all three earlier cases.
// v0.7.0f: the confession case's four chapters (Mystery5.CHAPTERS5)
// join on the same terms a fifth time — slot-rolled by Mystery5's
// gating only, behind all four earlier cases.
// v0.8.0f: the opinion case's four chapters (Mystery6.CHAPTERS6)
// join on the same terms a sixth time — slot-rolled by Mystery6's
// gating only, behind all five earlier cases.
// v0.9.0f: the backwards case's four chapters (Mystery7.CHAPTERS7)
// join on the same terms a seventh time — slot-rolled by
// Mystery7's gating only, behind all six earlier cases.
// v0.10.0f: the alibi case's four chapters (Mystery8.CHAPTERS8)
// join on the same terms an eighth time — slot-rolled by
// Mystery8's gating only, behind all seven earlier cases.
// v0.11.0f: the audit case's four chapters (Mystery9.CHAPTERS9)
// join on the same terms a ninth time — slot-rolled by
// Mystery9's gating only, behind all eight earlier cases.
// v0.12.0f: the photo case's four chapters (Mystery10.CHAPTERS10)
// join on the same terms a tenth time — slot-rolled by
// Mystery10's gating only, behind all nine earlier cases.
// v0.13.0f: the voice case's four chapters (Mystery11.CHAPTERS11)
// join on the same terms an eleventh time — slot-rolled by
// Mystery11's gating only, behind all ten earlier cases.
// v0.14.0f: the returns case's four chapters (Mystery12.CHAPTERS12)
// join on the same terms a twelfth time — slot-rolled by
// Mystery12's gating only, behind all eleven earlier cases.
// v0.15.0j: the exhibition case's four chapters (Mystery13.CHAPTERS13)
// join on the same terms a thirteenth time — slot-rolled by
// Mystery13's gating only, behind all twelve earlier cases.
val SHENANIGAN_BY_ID: Map<String, ShenaniganDef> =
    (SHENANIGANS + Mystery.ARC_CHAPTERS + Mystery2.BEATS +
        Mystery3.CHAPTERS3 + Mystery4.CHAPTERS4 +
        Mystery5.CHAPTERS5 + Mystery6.CHAPTERS6 +
        Mystery7.CHAPTERS7 + Mystery8.CHAPTERS8 +
        Mystery9.CHAPTERS9 + Mystery10.CHAPTERS10 +
        Mystery11.CHAPTERS11 + Mystery12.CHAPTERS12 +
        Mystery13.CHAPTERS13).associateBy { it.id }

/**
 * v0.1.8 EVENT LINEAGE: the last event's resolution weights the next
 * pick — sequels, backlashes, grudges — so a month of chaos reads as
 * one long story instead of disconnected rolls. Multiplier per
 * (previous event id -> next event id); unlisted pairs weigh 1.0.
 */
val EVENT_LINEAGE: Map<String, Map<String, Double>> = mapOf(
    "chinpo_craze" to mapOf("pirate_week" to 2.0, "tegridy_takeover" to 1.5, "mole_panic" to 0.5,
        "gold_fever" to 1.5),
    "pirate_week" to mapOf("chinpo_craze" to 1.5, "upside_down_day" to 1.5),
    "upside_down_day" to mapOf("chinpo_craze" to 1.5, "silent_challenge" to 0.5,
        "imagination_leak" to 1.5),
    "tegridy_takeover" to mapOf("mole_panic" to 1.5, "pirate_week" to 0.5, "chinpo_craze" to 1.5,
        "memberberry_fever" to 2.0),
    "mole_panic" to mapOf("silent_challenge" to 2.0, "tegridy_takeover" to 0.5,
        "coon_sightings" to 1.5, "imagination_leak" to 1.5),
    "silent_challenge" to mapOf("upside_down_day" to 1.5, "mole_panic" to 1.5),
    // v0.1.9e: the new stories join the lineage web
    "coon_sightings" to mapOf("mole_panic" to 1.5, "silent_challenge" to 1.5,
        "imagination_leak" to 0.5),
    "memberberry_fever" to mapOf("tegridy_takeover" to 1.5, "gold_fever" to 1.5,
        "pirate_week" to 0.5),
    "imagination_leak" to mapOf("mole_panic" to 1.5, "coon_sightings" to 1.5,
        "silent_challenge" to 0.5),
    "gold_fever" to mapOf("chinpo_craze" to 2.0, "memberberry_fever" to 1.5,
        "tegridy_takeover" to 0.5)
)

object Shenanigans {
    /** Stage index 0..3 from minutes since the event's day started. */
    fun stageFor(timeMin: Int): Int = (timeMin / 360).coerceIn(0, 3)

    fun stageOf(def: ShenaniganDef, timeMin: Int): ShenaniganStage =
        def.stages[stageFor(timeMin)]

    /** Tone-wrap a mock line for the active stage. Keeps Kenny muffled. */
    fun applyTone(line: String, tone: String, speaker: String): String {
        if (speaker == "Kenny") return line
        return when (tone) {
            "pirate" -> {
                val core = line.trimEnd('.', '!')
                "Arr, $core, matey."
            }
            "whisper" -> "(whispered) " + line.lowercase().replace("!", ".")
            "fad" -> line + listOf(
                " Gotta buy 'em all!", " Do you have the penguin one?!",
                " Mine's ultra-rare, probably!").random()
            "hype" -> line + listOf(
                " This is the biggest thing to ever happen here!",
                " Everyone's talking about it!", " Historic, dude.").random()
            "gold" -> line + listOf(
                " They're paying CASH for gold!", " Grandma's jewelry is basically money!",
                " The line starts at the Mall!").random()
            "member" -> {
                val core = line.trimEnd('.', '!')
                "'Member? $core. I 'member."
            }
            else -> line
        }
    }
}
