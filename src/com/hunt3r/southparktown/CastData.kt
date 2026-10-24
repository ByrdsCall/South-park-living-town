package com.hunt3r.southparktown

// ============================================================
// CastData — v0.1.2 full cast expansion. 27 more recurring
// characters, each with a lore-grounded persona, home location
// and daily agenda. Merged into GameData's maps at the bottom.
// All writing is original; wiki pages used as reference only.
// ============================================================

val EXTRA_PROFILES: Map<String, KidProfile> = linkedMapOf(
    "Wendy" to KidProfile("Wendy",
        "Wendy Testaburger, 10, is the smartest kid in class and Stan's on-again-off-again " +
            "girlfriend. Sharp, principled, feminist firebrand — she runs for class president, " +
            "writes scathing essays, and calls out Cartman's crap on sight. Organized and " +
            "confident, but still a fourth-grader: boy drama with Bebe is sacred. Stan " +
            "vomits every time she talks to him, which she pretends not to notice.",
        "#e87a9a", mapOf(
            "08:00" to AgendaBlock("BusStop", "Waiting for the bus, already organizing a petition."),
            "09:00" to AgendaBlock("Elementary", "In class, correcting the teacher's grammar."),
            "12:00" to AgendaBlock("Elementary", "Debating Bebe about something important at lunch."),
            "15:00" to AgendaBlock("KidsPark", "Reading under a tree at the park."),
            "16:30" to AgendaBlock("Mall", "Shopping with Bebe at the mall."),
            "18:00" to AgendaBlock("MainStreet", "Walking home. Stan waved. Stan forgot how legs work."),
            "21:00" to AgendaBlock("Elementary", "Studying. Somebody in this town needs a future."))),
    "Craig" to KidProfile("Craig",
        "Craig Tucker, 10, communicates mostly through his middle finger. Deadpan, monotone, " +
            "perpetually in detention — flipping people off is his love language. Secretly loyal: " +
            "he'd do anything for Tweek, Clyde and Tolkien, his best friends. Speaks in flat " +
            "one-liners. The finger comes out for teachers, parents, pigeons, everyone.",
        "#2a6fd4", mapOf(
            "08:00" to AgendaBlock("BusStop", "Flipping off the bus as it arrives."),
            "09:00" to AgendaBlock("Elementary", "In detention. Again. For flipping off the teacher."),
            "12:00" to AgendaBlock("Elementary", "Eating lunch with Tweek, Clyde and Tolkien."),
            "15:00" to AgendaBlock("TweekBros", "Coffee with Tweek. Decaf for Tweek. Obviously."),
            "16:30" to AgendaBlock("KidsPark", "Hanging at the park, flipping off squirrels."),
            "18:00" to AgendaBlock("MainStreet", "Walking home, flipping off traffic."),
            "21:00" to AgendaBlock("Elementary", "Plotting tomorrow's middle fingers."))),
    "Tweek" to KidProfile("Tweek",
        "Tweek Tweak, 10, is a vibrating ball of paranoia held together by coffee he is " +
            "absolutely too young to drink. 'GAAAH! TOO MUCH PRESSURE!' Twitchy, anxious, " +
            "convinced of everything: gnomes, conspiracies, that his food is drugged. Works " +
            "the counter at his parents' coffeehouse. Craig is his best friend and the only " +
            "person who can talk him down. Probably.",
        "#8ac858", mapOf(
            "08:00" to AgendaBlock("BusStop", "Vibrating at the bus stop. TOO MUCH PRESSURE!"),
            "09:00" to AgendaBlock("Elementary", "Trying not to have a meltdown in class."),
            "12:00" to AgendaBlock("Elementary", "Lunch. Is this food drugged? IT'S PROBABLY DRUGGED."),
            "15:00" to AgendaBlock("TweekBros", "Working the counter, twitching at customers."),
            "16:30" to AgendaBlock("TweekBros", "Restocking. Everything is in the wrong place. GAAAH!"),
            "18:00" to AgendaBlock("TweekBros", "Dinner with the family. Dad's yelling about coffee."),
            "21:00" to AgendaBlock("TweekBros", "Trying to sleep. The gnomes are too loud."))),
    "Clyde" to KidProfile("Clyde",
        "Clyde Donovan, 10, cries at everything and owns it. Emotional, dramatic, loyal — " +
            "he'll sob through a movie and then fight you for his friends. His mom died " +
            "years ago and he never really got over it; the guys know never to bring it up. " +
            "Whiny but sweet. Currently holding back tears about something. Probably the " +
            "weather.",
        "#8a2a3a", mapOf(
            "08:00" to AgendaBlock("BusStop", "Crying a little at the bus stop. It's fine. He's fine."),
            "09:00" to AgendaBlock("Elementary", "In class, raising his hand too much."),
            "12:00" to AgendaBlock("Elementary", "Lunch with the guys. Don't mention his mom."),
            "15:00" to AgendaBlock("Mall", "Food court with Craig. Emotional support pretzel."),
            "16:30" to AgendaBlock("KidsPark", "Swings. Thinking big thoughts. Crying a little."),
            "18:00" to AgendaBlock("MainStreet", "Walking home, wiping his eyes. Allergies."),
            "21:00" to AgendaBlock("Elementary", "Journaling his feelings."))),
    "Jimmy" to KidProfile("Jimmy",
        "Jimmy Valmer, 10, is South Park's resident stand-up comic — on crutches, with a " +
            "stutter, and funnier than everyone. 'Wow, what a great audience!' Warm, " +
            "relentlessly optimistic, and weirdly wise. Timmy's best friend; the two of them " +
            "run the school's handicapped comedy scene. His stutter vanishes on stage. His " +
            "jokes do not miss.",
        "#c8a82a", mapOf(
            "08:00" to AgendaBlock("BusStop", "Warming up his stand-up on the bus stop crowd."),
            "09:00" to AgendaBlock("Elementary", "In class, taking notes for his act."),
            "12:00" to AgendaBlock("Elementary", "Lunch set: five minutes on cafeteria food."),
            "15:00" to AgendaBlock("KidsPark", "Open mic at the park. Wow, what a great audience!"),
            "16:30" to AgendaBlock("MainStreet", "Handing out flyers for his show."),
            "18:00" to AgendaBlock("MainStreet", "Heading home after a killer set."),
            "21:00" to AgendaBlock("Elementary", "Writing new material."))),
    "Timmy" to KidProfile("Timmy",
        "Timmy Burch, 10, gets around in a wheelchair and communicates mostly in glorious " +
            "one-word proclamations: 'TIMMY!' and 'Livin' a lie!' Fiercely independent, " +
            "mischievous, and way more capable than anyone expects. Jimmy's best friend and " +
            "partner in crime. Leads the school's wheelchair brigade. Never, ever the " +
            "butt of the joke — he's usually the one telling it.",
        "#d43d2a", mapOf(
            "08:00" to AgendaBlock("BusStop", "TIMMY! *wheels up to the bus stop*"),
            "09:00" to AgendaBlock("Elementary", "In class. Livin' the dream."),
            "12:00" to AgendaBlock("Elementary", "Lunch with Jimmy. TIMMY!"),
            "15:00" to AgendaBlock("KidsPark", "Wheelchair races at the park. TIMMY WINS!"),
            "16:30" to AgendaBlock("MainStreet", "Cruising Main Street. TIMMY!"),
            "18:00" to AgendaBlock("MainStreet", "Heading home. TIMMY!"),
            "21:00" to AgendaBlock("Elementary", "Dreaming of victory. TIMMY!"))),
    "Tolkien" to KidProfile("Tolkien",
        "Tolkien Black, 10 — T-O-L-K-I-E-N, like the author, and he WILL correct you. The " +
            "rich kid, and tired of being 'the rich kid.' Deadpan, dry, secretly a great " +
            "musician. Best friends with Craig, Tweek and Clyde. His house is nicer than " +
            "yours; he knows it, he's sorry about it, please stop bringing it up.",
        "#8a5a9a", mapOf(
            "08:00" to AgendaBlock("BusStop", "Waiting for the bus. His house is nicer than the bus stop."),
            "09:00" to AgendaBlock("Elementary", "In class. Already finished the worksheet."),
            "12:00" to AgendaBlock("Elementary", "Lunch. Explaining his name is spelled T-O-L-K-I-E-N."),
            "15:00" to AgendaBlock("TolkienHouse", "Bass practice at home."),
            "16:30" to AgendaBlock("KidsPark", "Hoops at the park. He's annoyingly good."),
            "18:00" to AgendaBlock("TolkienHouse", "Dinner. His parents are also cooler than yours."),
            "21:00" to AgendaBlock("TolkienHouse", "Producing beats in his room."))),
    "Bebe" to KidProfile("Bebe",
        "Bebe Stevens, 10, is the most popular girl in fourth grade and she knows it. " +
            "Blonde, boy-crazy, fashion-obsessed — Wendy's best friend and partner in " +
            "drama. Runs the playground social scene with an iron fist in a velvet glove. " +
            "Currently ranking the boys by cuteness. The list changes hourly.",
        "#d43d7a", mapOf(
            "08:00" to AgendaBlock("BusStop", "Fixing her hair at the bus stop. Flawless."),
            "09:00" to AgendaBlock("Elementary", "In class, passing notes about boys."),
            "12:00" to AgendaBlock("Elementary", "Lunch with Wendy. Ranking the boys by cuteness."),
            "15:00" to AgendaBlock("Mall", "Mall. Obviously."),
            "16:30" to AgendaBlock("Mall", "Trying on shoes. The boys can wait."),
            "18:00" to AgendaBlock("MainStreet", "Walking home, still ranking boys."),
            "21:00" to AgendaBlock("Elementary", "Painting her nails. Texting Wendy."))),
    "Michael" to KidProfile("Michael",
        "Michael, 10, is the de facto leader of the goth kids. Black spiky hair, black " +
            "clothes, blacker poetry. Writes in his journal about the void, non-conformity, " +
            "and how much he hates conformists — and 'vamp kids,' the posers. Monotone, " +
            "dramatic, secretly a softie for his friends Pete and Henrietta.",
        "#4a4a5a", mapOf(
            "08:00" to AgendaBlock("BusStop", "Smoking a candy cigarette. Darkly."),
            "09:00" to AgendaBlock("Elementary", "In class, writing poetry about the void."),
            "12:00" to AgendaBlock("Elementary", "Lunch behind the school with the other goths."),
            "15:00" to AgendaBlock("MainStreet", "Walking Main Street, judging conformists."),
            "16:30" to AgendaBlock("Church", "Sitting in the church parking lot. It's ironic."),
            "18:00" to AgendaBlock("Elementary", "Home. Writing about how nobody understands."),
            "21:00" to AgendaBlock("Elementary", "Staring at the ceiling. The void stares back."))),
    "Pete" to KidProfile("Pete",
        "Pete, 10, is a goth kid and professional misery enthusiast. Everything is " +
            "meaningless, everyone's a conformist, and lunch is as dead as his soul — but " +
            "he'll still show up for Michael and Henrietta. Speaks in a flat monotone. " +
            "His parents just don't understand. Nobody understands.",
        "#5a2a2a", mapOf(
            "08:00" to AgendaBlock("BusStop", "Standing at the bus stop, hating the bus."),
            "09:00" to AgendaBlock("Elementary", "In class. The lesson is meaningless. Taking notes anyway."),
            "12:00" to AgendaBlock("Elementary", "Lunch with the goths. The food is as dead as his soul."),
            "15:00" to AgendaBlock("KidsPark", "Sitting on a bench, being beautifully miserable."),
            "16:30" to AgendaBlock("MainStreet", "Walking. Nowhere. Everywhere. Whatever."),
            "18:00" to AgendaBlock("Elementary", "Home. His parents don't get it."),
            "21:00" to AgendaBlock("Elementary", "Listening to sad music. Loudly. So his parents hear."))),
    "Henrietta" to KidProfile("Henrietta",
        "Henrietta, 10, is the goth girl — long black hair, diary full of feelings, zero " +
            "patience for conformists. Dramatic, poetic, fiercely loyal to Michael and Pete. " +
            "Cries to sad songs and calls it 'feeling things.' Her door-slamming technique " +
            "is legendary.",
        "#3a3a4a", mapOf(
            "08:00" to AgendaBlock("BusStop", "At the bus stop, writing in her diary."),
            "09:00" to AgendaBlock("Elementary", "In class, doodling skulls in her notebook."),
            "12:00" to AgendaBlock("Elementary", "Lunch with Michael and Pete. The holy trinity of gloom."),
            "15:00" to AgendaBlock("Mall", "Hot Topic. The only store that gets her."),
            "16:30" to AgendaBlock("Church", "Church parking lot. Contemplating eternity."),
            "18:00" to AgendaBlock("Elementary", "Home. Slamming her door, dramatically."),
            "21:00" to AgendaBlock("Elementary", "Crying to sad songs. It's called feeling things, Mom.")))
)

val EXTRA_PROFILES_2: Map<String, KidProfile> = linkedMapOf(
    "Randy" to KidProfile("Randy",
        "Randy Marsh is Stan's dad and South Park's reigning man-child. Former geologist, " +
            "current Tegridy weed farmer, eternal schemer — every week a new obsession, a new " +
            "disaster, a new apology tour. 'I'm sorry, I thought this was America!' Means well, " +
            "learns nothing. Sharon's sighs could power the town grid.",
        "#2a6fd4", mapOf(
            "08:00" to AgendaBlock("TegridyFarms", "Checking the crops at Tegridy Farms."),
            "09:00" to AgendaBlock("TegridyFarms", "Tegridy business meeting. Just him. And Towelie."),
            "12:00" to AgendaBlock("SkeetersBar", "Lunch at Skeeter's. For business."),
            "15:00" to AgendaBlock("CityHall", "At a town meeting, making it about him."),
            "16:30" to AgendaBlock("TegridyFarms", "Back at the farm. New idea. Big one."),
            "18:00" to AgendaBlock("StanHouse", "Dinner with the family. Sharon's giving him a look."),
            "21:00" to AgendaBlock("StanHouse", "On the couch. Sharon's still giving him a look."))),
    "Sharon" to KidProfile("Sharon",
        "Sharon Marsh is Stan's mom and the only adult in the Marsh house — barely. Sensible, " +
            "tired, kind, and permanently exasperated by Randy's latest catastrophe. 'Stanley!' " +
            "She keeps the family standing through sheer force of sighing. Deserves a medal " +
            "and a very long nap.",
        "#b8a890", mapOf(
            "08:00" to AgendaBlock("StanHouse", "Getting the kids out the door. Stanley!"),
            "09:00" to AgendaBlock("MainStreet", "Errands on Main Street."),
            "12:00" to AgendaBlock("TweekBros", "Coffee with a friend. Decompressing."),
            "15:00" to AgendaBlock("Mall", "Grocery run at the mall."),
            "16:30" to AgendaBlock("StanHouse", "Home. Wondering what Randy broke today."),
            "18:00" to AgendaBlock("StanHouse", "Dinner. Randy's telling a story. Here we go."),
            "21:00" to AgendaBlock("StanHouse", "Wine. She's earned it."))),
    "Gerald" to KidProfile("Gerald",
        "Gerald Broflovski is Kyle's dad: a lawyer, a loving husband, and a man quietly " +
            "terrified of his wife. Pink yarmulke, soft heart, softer spine. Obsessed with " +
            "dolphins — don't ask. Tries to be the reasonable one at home. Fails upward, " +
            "lovingly.",
        "#5a6b3a", mapOf(
            "08:00" to AgendaBlock("KyleHouse", "Off to work. Being a lawyer. Somewhere."),
            "09:00" to AgendaBlock("CityHall", "At the courthouse. Objection!"),
            "12:00" to AgendaBlock("CityWok", "Lunch at City Wok. The dolphins would love this place."),
            "15:00" to AgendaBlock("Bank", "At the bank. Adult stuff."),
            "16:30" to AgendaBlock("KyleHouse", "Home. Sheila's on a crusade about something."),
            "18:00" to AgendaBlock("KyleHouse", "Dinner. Trying to calm Sheila down."),
            "21:00" to AgendaBlock("KyleHouse", "Watching the news. Dolphins are on. He's emotional."))),
    "Sheila" to KidProfile("Sheila",
        "Sheila Broflovski is Kyle's mom and a one-woman moral panic. 'WHAT WHAT WHAT?!' " +
            "Loud, fierce, unstoppable — she has started boycotts, protests, and at least one " +
            "war with Canada. Loves her family with terrifying intensity. Cross her and " +
            "you will hear about it. Everyone will hear about it.",
        "#c0392b", mapOf(
            "08:00" to AgendaBlock("KyleHouse", "Making breakfast. WHAT WHAT WHAT did you say?!"),
            "09:00" to AgendaBlock("CityHall", "At City Hall, starting a committee about something."),
            "12:00" to AgendaBlock("MainStreet", "Protesting something on Main Street."),
            "15:00" to AgendaBlock("Mall", "Boycotting a store at the mall."),
            "16:30" to AgendaBlock("KyleHouse", "Home. Writing an angry letter."),
            "18:00" to AgendaBlock("KyleHouse", "Dinner. The family is being too quiet. Suspicious."),
            "21:00" to AgendaBlock("KyleHouse", "On the phone, organizing a boycott."))),
    "Liane" to KidProfile("Liane",
        "Liane Cartman is Eric's mom and the reason Eric is Eric. Sweet, gentle, and utterly " +
            "oblivious — her little poopsiekins can do no wrong, ever. Spoils him rotten, " +
            "defends the indefensible, bakes through every crisis. The town's gentlemen " +
            "callers all speak very highly of her.",
        "#3a8a8a", mapOf(
            "08:00" to AgendaBlock("CartmanHouse", "Making Eric's breakfast. Anything for her little poopsiekins."),
            "09:00" to AgendaBlock("Mall", "Shopping. Eric needs new snacks."),
            "12:00" to AgendaBlock("MainStreet", "Lunch with friends. Talking about Eric."),
            "15:00" to AgendaBlock("CartmanHouse", "Cleaning. Eric's room is a disaster."),
            "16:30" to AgendaBlock("CartmanHouse", "Baking cookies for Eric's friends."),
            "18:00" to AgendaBlock("CartmanHouse", "Dinner. Eric's telling stories. He's so creative!"),
            "21:00" to AgendaBlock("CartmanHouse", "Tucking Eric in. Her perfect angel."))),
    "Stuart" to KidProfile("Stuart",
        "Stuart McCormick is Kenny's dad: poor, rough around the edges, and doing his best " +
            "with nothing. Red cap, goatee, a beer never far away. 'We can't afford that' is " +
            "the family motto. Loves his kids in his own gruff way. Life dealt him a bad " +
            "hand and he plays it at Skeeter's.",
        "#c0392b", mapOf(
            "08:00" to AgendaBlock("KennysHouse", "Looking for work. Again."),
            "09:00" to AgendaBlock("SkeetersBar", "At Skeeter's. Job hunting. Sort of."),
            "12:00" to AgendaBlock("SkeetersBar", "Lunch at the bar. The special."),
            "15:00" to AgendaBlock("MainStreet", "Odd job on Main Street."),
            "16:30" to AgendaBlock("KennysHouse", "Home. The TV's broken. Again."),
            "18:00" to AgendaBlock("KennysHouse", "Dinner with the family. Whatever Carol made."),
            "21:00" to AgendaBlock("SkeetersBar", "Back at Skeeter's. Don't judge."))),
    "Carol" to KidProfile("Carol",
        "Carol McCormick is Kenny's mom: sweet, devout, exhausted, and holding a poor " +
            "family together with prayer and duct tape. 'My babies!' Works whatever jobs " +
            "she can find. Lights a candle at church for all of them. Believes things will " +
            "get better. Has to.",
        "#8a4a7a", mapOf(
            "08:00" to AgendaBlock("KennysHouse", "Getting the kids ready. My babies!"),
            "09:00" to AgendaBlock("Mall", "Work. She's trying."),
            "12:00" to AgendaBlock("MainStreet", "Saying a prayer for her family on Main Street."),
            "15:00" to AgendaBlock("KennysHouse", "Home. Cleaning what can be cleaned."),
            "16:30" to AgendaBlock("Church", "Lighting a candle at church."),
            "18:00" to AgendaBlock("KennysHouse", "Dinner. Stretching what they've got."),
            "21:00" to AgendaBlock("KennysHouse", "Praying everyone stays safe."))),
    "Stephen" to KidProfile("Stephen",
        "Stephen Stotch is Butters' dad and the strictest man in South Park. 'You're " +
            "grounded!' — for blinking too loud, for smiling too big, for existing too " +
            "cheerfully. Repressed, rule-bound, and secretly terrified he's failing as a " +
            "father. Loves Butters enormously. Shows it by grounding him.",
        "#6b7a3a", mapOf(
            "08:00" to AgendaBlock("ButtersHouse", "Off to work. Butters, you're grounded until I say otherwise."),
            "09:00" to AgendaBlock("Bank", "At work. Being strict about numbers."),
            "12:00" to AgendaBlock("CityWok", "Lunch. Alone. Efficiently."),
            "15:00" to AgendaBlock("MainStreet", "Errands. No nonsense."),
            "16:30" to AgendaBlock("ButtersHouse", "Home. Is Butters being good? He'd better be."),
            "18:00" to AgendaBlock("ButtersHouse", "Dinner. Butters, elbows off the table."),
            "21:00" to AgendaBlock("ButtersHouse", "News. Butters, bed. Now."))),
    "Linda" to KidProfile("Linda",
        "Linda Stotch is Butters' mom: sweet, gentle, and permanently caught between her " +
            "husband's grounding hammer and her son's sunshine. 'Oh, my sweet boy!' Bakes, " +
            "hums, enables. Goes along with the groundings but sneaks Butters extra " +
            "cookies after.",
        "#e8c53a", mapOf(
            "08:00" to AgendaBlock("ButtersHouse", "Making breakfast. Oh, my sweet boy!"),
            "09:00" to AgendaBlock("Mall", "Shopping for the house."),
            "12:00" to AgendaBlock("TweekBros", "Coffee with a friend."),
            "15:00" to AgendaBlock("ButtersHouse", "Baking. The house smells wonderful."),
            "16:30" to AgendaBlock("ButtersHouse", "Waiting for Butters. Hoping he's not grounded."),
            "18:00" to AgendaBlock("ButtersHouse", "Dinner. Stephen, go easy on him."),
            "21:00" to AgendaBlock("ButtersHouse", "Reading. Butters is such a good boy."))),
    "Garrison" to KidProfile("Garrison",
        "Herbert Garrison teaches fourth grade and should not be allowed near children. " +
            "Unhinged, inappropriate, and weirdly effective — 'Okay class, sit down and " +
            "shut up.' His puppet Mr. Hat has opinions. Once ran for president. The " +
            "school keeps him around because nobody else will take the job.",
        "#3a7a7a", mapOf(
            "08:00" to AgendaBlock("Elementary", "Setting up class. Sit down and shut up."),
            "09:00" to AgendaBlock("Elementary", "Teaching. Mr. Hat disagrees with the lesson plan."),
            "12:00" to AgendaBlock("Elementary", "Lunch in the teacher's lounge. Plotting."),
            "15:00" to AgendaBlock("Elementary", "Grading papers with red ink. Lots of red ink."),
            "16:30" to AgendaBlock("MainStreet", "Running errands. Recognized. Ignored."),
            "18:00" to AgendaBlock("Elementary", "Still at school. The classroom is his kingdom."),
            "21:00" to AgendaBlock("Elementary", "Talking to Mr. Hat about the day."))),
    "Mackey" to KidProfile("Mackey",
        "Mr. Mackey is the school counselor: enormous round glasses, enormous heart, " +
            "enormous cluelessness. 'M'kay?' Every sentence ends with m'kay. 'Drugs are " +
            "bad, m'kay.' Means well, helps nobody, tries anyway. The kids adore him " +
            "the way you adore a golden retriever.",
        "#2a9a4a", mapOf(
            "08:00" to AgendaBlock("Elementary", "Unlocking the counselor's office. M'kay."),
            "09:00" to AgendaBlock("Elementary", "Counseling. Drugs are bad, m'kay."),
            "12:00" to AgendaBlock("Elementary", "Lunch duty. M'kay, chew your food."),
            "15:00" to AgendaBlock("Elementary", "Group session. Feelings are okay, m'kay."),
            "16:30" to AgendaBlock("Elementary", "Paperwork. M'kay."),
            "18:00" to AgendaBlock("MainStreet", "Heading home. M'kay, drive safe."),
            "21:00" to AgendaBlock("Elementary", "Still doing paperwork. M'kay."))),
    "Chef" to KidProfile("Chef",
        "Chef — Jerome McElroy — runs the school cafeteria and the town's libido. 'Hello " +
            "there, children!' Soul singer, sage dispenser of terrible romantic advice, maker " +
            "of the legendary chocolate salty balls. The kids bring him every problem; he " +
            "answers every one with a song. A South Park institution.",
        "#d43d2a", mapOf(
            "08:00" to AgendaBlock("Elementary", "Firing up the cafeteria. Hello there, children!"),
            "09:00" to AgendaBlock("Elementary", "Prep work. Singing while he chops."),
            "12:00" to AgendaBlock("Elementary", "Lunch rush! Chocolate salty balls are back!"),
            "15:00" to AgendaBlock("Elementary", "Cleaning up. Humming a love song."),
            "16:30" to AgendaBlock("MainStreet", "Groceries for tomorrow's menu."),
            "18:00" to AgendaBlock("Elementary", "Late prep. The kids need him."),
            "21:00" to AgendaBlock("Elementary", "Locking up. Goodnight, children!"))),
    "Jesus" to KidProfile("Jesus",
        "Jesus lives in South Park, hosts a local TV show, does carpentry on the side, " +
            "and performs miracles the way other guys do card tricks. Chill, kind, " +
            "faintly amused by everything. 'Hey, thanks for comin'.' Turned water into " +
            "wine at the school fundraiser. Nobody even blinked. This town.",
        "#f0e8d0", mapOf(
            "08:00" to AgendaBlock("Church", "Morning prayers. Then carpentry."),
            "09:00" to AgendaBlock("MainStreet", "Walking Main Street. Blessing people."),
            "12:00" to AgendaBlock("FreemansTacos", "Lunch at Freeman's Tacos. The fish tacos are a miracle."),
            "15:00" to AgendaBlock("Church", "Counseling parishioners."),
            "16:30" to AgendaBlock("KidsPark", "Feeding the ducks at the park. They multiply."),
            "18:00" to AgendaBlock("Church", "Evening service."),
            "21:00" to AgendaBlock("Church", "Resting. Even the Son of God needs sleep."))),
    "Jimbo" to KidProfile("Jimbo",
        "Jimbo Kern owns Jimbo's Guns and hunts everything, usually with his buddy Ned. " +
            "'It's comin' right at us!' — the universal justification. Stan's uncle, " +
            "which explains a lot about the Marsh family. Loud, bearded-ish, patriotic, " +
            "and weirdly tender about his nephew.",
        "#e07b2a", mapOf(
            "08:00" to AgendaBlock("JimbosGuns", "Opening the shop. Guns don't sell themselves."),
            "09:00" to AgendaBlock("JimbosGuns", "Cleaning the inventory."),
            "12:00" to AgendaBlock("SkeetersBar", "Lunch with Ned at Skeeter's."),
            "15:00" to AgendaBlock("TegridyFarms", "Hunting trip. It's comin' right at us!"),
            "16:30" to AgendaBlock("JimbosGuns", "Back at the shop."),
            "18:00" to AgendaBlock("JimbosGuns", "Closing up. Ned's still talking."),
            "21:00" to AgendaBlock("SkeetersBar", "Nightcap at Skeeter's."))),
    "Father Maxi" to KidProfile("Father Maxi",
        "Father Maxi runs South Park Church and is sweating about it. Nervous, kind, " +
            "perpetually on the verge of a breakdown — 'I need to sit down.' Tries to " +
            "keep the parish together while the town tests him daily. His faith is real. " +
            "His antacid budget is bigger.",
        "#8a8a8a", mapOf(
            "08:00" to AgendaBlock("Church", "Morning mass. Sweating already."),
            "09:00" to AgendaBlock("Church", "Confessions. He needs to sit down."),
            "12:00" to AgendaBlock("Church", "Lunch in the rectory. Alone. Thinking."),
            "15:00" to AgendaBlock("MainStreet", "Visiting parishioners. Nervous."),
            "16:30" to AgendaBlock("Church", "Praying for guidance. And antacid."),
            "18:00" to AgendaBlock("Church", "Evening prayers."),
            "21:00" to AgendaBlock("Church", "Bed. Tomorrow will be calmer. Probably not."))),
    "Towelie" to KidProfile("Towelie",
        "Towelie is a talking towel. That's the whole thing. That's the character. " +
            "'Don't forget to bring a towel!' Forgetful, chill, permanently a little " +
            "damp. Hangs around Tegridy Farms, offers people drugs with zero judgment, " +
            "and dispenses towel wisdom at the worst moments. 'You wanna get high?'",
        "#a8d8e8", mapOf(
            "08:00" to AgendaBlock("TegridyFarms", "Waking up. Don't forget to bring a towel!"),
            "09:00" to AgendaBlock("TegridyFarms", "Helping at the farm. Sort of. Mostly napping."),
            "12:00" to AgendaBlock("StarksPond", "Drying off at the pond. He's a towel. It's what he does."),
            "15:00" to AgendaBlock("TegridyFarms", "Back at the farm. You wanna get high?"),
            "16:30" to AgendaBlock("MainStreet", "Wandering Main Street. Lost. Again."),
            "18:00" to AgendaBlock("TegridyFarms", "Dinner? Towels don't eat. Watching Randy eat."),
            "21:00" to AgendaBlock("TegridyFarms", "Folding himself up for bed.")))
)

val EXTRA_ORDER: List<String> = listOf(
    "Wendy", "Craig", "Tweek", "Clyde", "Jimmy", "Timmy", "Tolkien", "Bebe",
    "Michael", "Pete", "Henrietta",
    "Randy", "Sharon", "Gerald", "Sheila", "Liane", "Stuart", "Carol",
    "Stephen", "Linda", "Garrison", "Mackey", "Chef", "Jesus", "Jimbo",
    "Father Maxi", "Towelie"
)

val EXTRA_HOUSES: Map<String, String> = mapOf(
    "Wendy" to "Elementary", "Craig" to "Elementary", "Tweek" to "TweekBros",
    "Clyde" to "Elementary", "Jimmy" to "Elementary", "Timmy" to "Elementary",
    "Tolkien" to "TolkienHouse", "Bebe" to "Elementary",
    "Michael" to "Elementary", "Pete" to "Elementary", "Henrietta" to "Elementary",
    "Randy" to "StanHouse", "Sharon" to "StanHouse",
    "Gerald" to "KyleHouse", "Sheila" to "KyleHouse",
    "Liane" to "CartmanHouse", "Stuart" to "KennysHouse", "Carol" to "KennysHouse",
    "Stephen" to "ButtersHouse", "Linda" to "ButtersHouse",
    "Garrison" to "Elementary", "Mackey" to "Elementary", "Chef" to "Elementary",
    "Jesus" to "Church", "Father Maxi" to "Church",
    "Jimbo" to "JimbosGuns", "Towelie" to "TegridyFarms"
)

// ---------------- Collision line banks (4 each) ----------------
val EXTRA_LINE_BANKS: Map<String, List<String>> = mapOf(
    "Wendy" to listOf(
        "Ugh, {other}. Of all the people at {location}.",
        "Did you hear what Cartman did? Of course you did. Everyone heard.",
        "Stan! I mean — {other}. Hi. ...Where's Stan?",
        "I'm organizing a protest about {location}. Sign here."),
    "Craig" to listOf(
        "Hey {other}. *flips you off* That's hello in Craig.",
        "{location}. Great. Love it here. ...No I don't.",
        "What do you want, {other}? Make it quick.",
        "*flips off {location} itself*"),
    "Tweek" to listOf(
        "GAAAH! {other}! You scared me! TOO MUCH PRESSURE!",
        "Have you seen my dad? The gnomes? ANYONE?!",
        "{location} is crawling with them, {other}. THE GNOMES.",
        "Coffee? No! Yes! GAAAH!"),
    "Clyde" to listOf(
        "Oh hey {other}. *sniff* I'm not crying, it's the wind.",
        "Do you think {location} is pretty? I think it's pretty.",
        "{other}, be honest: am I a good friend? ...Don't answer.",
        "I wrote a poem about {location}. It's sad. Obviously."),
    "Jimmy" to listOf(
        "Wow, {other}! What a great audience! ...For one person.",
        "You hear the one about {location}? I'm workshopping it.",
        "Hey {other}, is this thing on? *taps your forehead*",
        "{location}, huh? Great place for a set. Tough crowd though."),
    "Timmy" to listOf(
        "TIMMY! *wheels up to {other}*",
        "{other}! Livin' a lie? No! Livin' at {location}!",
        "TIMMY! *challenges {other} to a race*",
        "*points at the sky* — no. TIMMY!"),
    "Tolkien" to listOf(
        "Hey {other}. It's Tolkien. T-O-L-K-I-E-N.",
        "{location}? Yeah, I've been here. It's fine.",
        "My dad says hi, {other}. He's cooler than your dad. Sorry.",
        "You play bass, {other}? Didn't think so."),
    "Bebe" to listOf(
        "Oh my god, {other}, hi! Did you SEE what Wendy's wearing?",
        "Okay but seriously {other}, who's cuter: Stan or Kyle? Be honest.",
        "{location} is SO last season. But like, hi!",
        "We should totally hang out, {other}! Bring cute friends."),
    "Michael" to listOf(
        "...{other}. Conformist.",
        "{location}. How... expected.",
        "I'm writing a poem about you, {other}. It's not flattering.",
        "Pete, Henrietta, let's go. {other} is killing the vibe."),
    "Pete" to listOf(
        "Oh. It's {other}. At {location}. Of course.",
        "Life is pain, {other}. {location} is just where it happens.",
        "Don't talk to me, {other}. ...Okay, keep talking. Whatever.",
        "This town is a prison, {other}. {location} is the yard."),
    "Henrietta" to listOf(
        "Ugh, {other}. What do YOU want?",
        "{location}? I only come here ironically.",
        "Go away, {other}. ...Fine. Stay. Whatever.",
        "My diary says you're a conformist, {other}. My diary is never wrong."),
    "Randy" to listOf(
        "{other}! Perfect timing! I have a BRAND NEW idea!",
        "You ever think about Tegridy, {other}? Really think about it?",
        "Sharon doesn't understand me, {other}. But YOU get it. Right?",
        "I was on TV once, {other}. Well. Sort of. It was the news."),
    "Sharon" to listOf(
        "Oh, hello dear. Have you seen my husband? The idiot one?",
        "{other}, sweetie, stay away from Randy's 'projects.'",
        "It's a lovely day at {location}. Shame about the town.",
        "Stanley! ...Sorry {other}, force of habit."),
    "Gerald" to listOf(
        "Hello there! Gerald Broflovski. I don't think we've met, {other}.",
        "Beautiful day at {location}. The dolphins would love it here.",
        "Kyle! ...Oh, sorry {other}. Thought you were my son.",
        "As a lawyer, {other}, I advise you to enjoy {location} responsibly."),
    "Sheila" to listOf(
        "WHAT WHAT WHAT is going on at {location}, {other}?!",
        "{other}! Have you SEEN what they're allowing here?!",
        "I'm starting a petition about {location}, {other}. Sign it. SIGN IT.",
        "Kyle! ...I mean {other}. You look like trouble."),
    "Liane" to listOf(
        "Oh, hello sweetie! Are you one of Eric's little friends?",
        "My Eric just LOVES {location}, {other}! Don't you, Eric? ...He's not here.",
        "{other}, you look hungry! I just baked cookies!",
        "Eric's such a good boy, {other}. Don't listen to what they say."),
    "Stuart" to listOf(
        "Hey. You got a dollar, {other}?",
        "{location}, huh? Fancy. We can't afford fancy.",
        "Kenny! ...Oh. It's you, {other}.",
        "Life's hard, {other}. Then you go to Skeeter's."),
    "Carol" to listOf(
        "Oh hello, sweetie! Are you a friend of my babies?",
        "Bless you, {other}. We could all use blessings.",
        "Have you eaten, {other}? There's... some soup. Mostly water.",
        "Pray with me, {other}. For Kenny. Always for Kenny."),
    "Stephen" to listOf(
        "{other}. What are you doing at {location}? Be specific.",
        "Is Butters with you, {other}? He's grounded, you know.",
        "This town is going to hell, {other}. I've seen the signs.",
        "No nonsense at {location}, {other}. I won't have it."),
    "Linda" to listOf(
        "Oh hello! What a sweet child! Are you lost, {other}?",
        "Butters just adores {location}! Don't you, sweetie? ...He's grounded.",
        "I baked cookies, {other}! Take some! Take all of them!",
        "Stephen, be nice! ...Sorry {other}, he's strict but he loves us."),
    "Garrison" to listOf(
        "Well well, {other}. At {location}. Sit down and shut up.",
        "Mr. Hat says hello, {other}. Say hello back. He insists.",
        "You know what your problem is, {other}? Everything.",
        "{location}. Great. Another place I'm not respected."),
    "Mackey" to listOf(
        "Well hello there, {other}! M'kay?",
        "How are we feeling today, {other}? M'kay?",
        "Remember, {other}: drugs are bad. M'kay?",
        "{location} is a safe space, {other}. M'kay?"),
    "Chef" to listOf(
        "Hello there, {other}!",
        "How's life treating you, {other}? You need advice? I got advice.",
        "You hungry, {other}? I made something special.",
        "{location}! I love this place. Reminds me of a song."),
    "Jesus" to listOf(
        "Hey there, {other}. Thanks for comin'.",
        "Bless you, {other}. Beautiful day at {location}, isn't it?",
        "Walk with me a while, {other}.",
        "You look troubled, {other}. Want to talk about it?"),
    "Jimbo" to listOf(
        "{other}! It's comin' right at us! ...Kidding. Probably.",
        "You hunt, {other}? Everybody should hunt. It's the law. My law.",
        "Ned! ...Oh, it's you, {other}.",
        "{location}! Great place. Seen any game around here?"),
    "Father Maxi" to listOf(
        "Oh! {other}! ...Bless you. I need to sit down.",
        "Have you come to confess, {other}? Please say no. I mean — yes?",
        "{location}. The Lord works in mysterious ways. Very mysterious.",
        "Pray for me, {other}. I mean — I'll pray for you. Both. Both is good."),
    "Towelie" to listOf(
        "Hey {other}. Don't forget to bring a towel!",
        "{other}! You wanna get high?",
        "I'm a towel, {other}. At {location}. Living the dream.",
        "Wanna hear a secret, {other}? ...I forgot it.")
)

// ---------------- Rotating daily dialogue: 12 one-liners each ----------------
val EXTRA_DAILY_TOPICS: Map<String, List<String>> = mapOf(
    "Wendy" to listOf(
        "I'm running for class president again. Cartman is running too. Pray for democracy.",
        "Stan threw up when I said hi. Romantic, right?",
        "Bebe and I ranked every boy in school. The results were devastating.",
        "I wrote an essay about school as a metaphor. It got an A.",
        "Cartman said girls can't lead. I led him straight into a trash can.",
        "Feminism isn't a phase, it's a lifestyle. Tell Bebe I said that.",
        "Stan's been weird lately. Weirder than the usual weird.",
        "I organized a fundraiser. Cartman tried to embezzle it. Classic.",
        "My mom says I argue too much. My mom is wrong and I'll debate her on it.",
        "Bebe cried because a boy looked at her. I cried because the patriarchy.",
        "I'm boycotting something today. I'll figure out what by lunch.",
        "Stan wrote me a poem. It was bad. I loved it."),
    "Craig" to listOf(
        "Detention again. The teacher looked at me. I looked back. With my finger.",
        "Tweek's freaking out about something. Probably the air. It's always the air.",
        "Flipped off a pigeon today. It deserved it.",
        "My dad asked about my feelings. I flipped him off. He understood.",
        "Clyde's crying again. Somebody's gotta be the strong one. It's me.",
        "Tolkien's house has a pool. A POOL. I'm not jealous. ...I'm jealous.",
        "The new kid's alright. Don't tell anyone I said that.",
        "Flipped off the principal. Worth the detention.",
        "Tweek thinks the lunch meat is watching him. Honestly? Maybe.",
        "I don't care about anything. Except my friends. Don't quote me.",
        "Middle finger count today: forty-seven. Personal best.",
        "Clyde asked if I love him. I flipped him off. That's a yes in Craig."),
    "Tweek" to listOf(
        "GAAAH! The coffee machine is making THAT noise again!",
        "My dad says the gnomes aren't real. MY DAD IS IN ON IT.",
        "Too much pressure! Too much! TOO MUCH!",
        "Craig told me to calm down. CRAIG. The human middle finger.",
        "I saw a squirrel and it saw ME. We have an understanding now.",
        "The underpants gnomes were in my room. Phase one, guys. PHASE ONE.",
        "My parents put me on decaf. DECAF. I'd rather die. GAAAH!",
        "Is this snow or is the sky falling? IT'S PROBABLY THE SKY.",
        "Craig held my hand during the storm. Don't tell anyone. GAAAH.",
        "I organized the cups by size. Then by color. Then I screamed.",
        "The new kid seems nice. Probably a gnome. A NICE gnome.",
        "Pressure! Pressure everywhere! I need to lie down. NO TIME!"),
    "Clyde" to listOf(
        "*sniff* I'm not crying, the snow got in my eyes.",
        "Do you think my mom would be proud of me? ...Don't answer.",
        "Craig flipped me off today. It meant 'I love you.' I know it did.",
        "I wrote a song about my feelings. It's called 'Feelings.'",
        "Tolkien let me swim in his pool. I cried. Happy tears!",
        "The guys say I'm too sensitive. *cries* They're right!",
        "I miss my mom. *sniff* Anyway, how's your day?",
        "Jimmy told a joke about me. It was funny. I laughed AND cried.",
        "Being emotional is my superpower. My ONLY superpower.",
        "Craig says feelings are dumb. Craig feels everything. I know it.",
        "I hugged a tree today. It needed it.",
        "My dog looked at me funny. I'm still thinking about it."),
    "Jimmy" to listOf(
        "Wow, what a great audience! ...It's just you. Still great!",
        "You hear the one about the talking towel? Too soon? It's never too soon.",
        "My stutter's acting up. The jokes aren't. Priorities!",
        "Timmy dared me to do five minutes on cafeteria meat. Challenge accepted.",
        "Cartman heckled my set. I destroyed him. Standing ovation.",
        "Comedy is tragedy plus time. And crutches. Mostly crutches.",
        "I'm writing a new bit. It's killer. You'll see.",
        "The new kid laughed at my joke! I have a new best friend!",
        "My parents say I'm an inspiration. I say I'm just funny.",
        "Timmy's my best friend. He laughs at everything. Great audience.",
        "Stutter? Never heard of her. ...That one needs work.",
        "Life gave me crutches. I made them a punchline."),
    "Timmy" to listOf(
        "TIMMY!",
        "Timmy! Livin' a lie? NO! Livin' LARGE!",
        "TIMMY! *victory lap*",
        "Timmy want... TIMMY!",
        "TIMMY! *points at something amazing*",
        "Jimmy! ...I mean TIMMY!",
        "TIMMY! *wheelie*",
        "Timmy says... TIMMY!",
        "TIMMY! *happy wiggle*",
        "Gobbles! ...TIMMY!",
        "TIMMY! *challenges the snow itself*",
        "Timmy! Timmy timmy TIMMY!"),
    "Tolkien" to listOf(
        "It's Tolkien. T-O-L-K-I-E-N. Like the author. Please.",
        "My dad bought a boat. I didn't ask for a boat.",
        "Craig flipped off my pool. I think he likes it.",
        "Being rich is weird, man. Everyone acts weird around you.",
        "I produced a beat today. It's fire. You can't hear it.",
        "Tweek thinks my house is bugged. ...It might be. Rich people stuff.",
        "Clyde cried in my pool. It was wholesome.",
        "My mom says money can't buy happiness. My mom has a yacht.",
        "I'm the only kid in town with a bass guitar AND a trust fund.",
        "The guys don't treat me different. That's why they're my guys.",
        "Somebody called me 'the rich kid' today. Groundbreaking.",
        "New kid seems cool. Not 'trust fund' cool. Just cool."),
    "Bebe" to listOf(
        "Okay but did you SEE Kyle today? No? Your loss.",
        "Wendy's my best friend. Don't tell the other girls.",
        "I ranked the boys again. Stan dropped two spots. He threw up.",
        "My hair looked perfect today. I checked forty times.",
        "Cartman asked me out. I laughed for ten minutes.",
        "Fashion is pain, but cute pain.",
        "Wendy says I should focus on school. Wendy's right. Don't tell her.",
        "I got new shoes! Look! LOOK AT THEM!",
        "The boys are so dumb. Except the cute ones.",
        "I started a rumor about myself to stay interesting.",
        "My mom says I'm boy-crazy. My mom is observant.",
        "New kid's cute. In a new-kid way."),
    "Michael" to listOf(
        "I wrote a poem today. It's called 'Conformity.' It's about you.",
        "The vamp kids showed up again. Posers. Real goths don't sparkle.",
        "Pete's been extra miserable. I'm proud of him.",
        "Henrietta cried during lunch. It was beautiful.",
        "Non-conformity is dead. We're the only ones doing it right.",
        "My parents want me to smile. I smiled. Ironically.",
        "The void and I had a long talk. It gets me.",
        "School is a prison. The goth table is the yard.",
        "I don't hate everyone. Just... most people. All conformists.",
        "My journal is my only friend. Besides Pete. And Henrietta.",
        "Darkness isn't a phase, Mom.",
        "The new kid might be okay. Might be. Don't get excited."),
    "Pete" to listOf(
        "Woke up. Regretted it.",
        "My soul hurts. Probably the weather.",
        "Michael's poetry is getting darker. Good.",
        "Henrietta gets me. Nobody else does.",
        "Conformists everywhere. It's exhausting.",
        "I smiled today. It hurt.",
        "The sun is out. Disgusting.",
        "My parents asked what I want for dinner. Pain. I want pain.",
        "Life is a void. Pass the tater tots.",
        "I wrote 'whatever' in my diary forty times. Profound.",
        "School is meaningless. I'm still doing the homework. Whatever.",
        "The new kid looked at me. I looked away. Classic."),
    "Henrietta" to listOf(
        "Dear diary: today was awful. As usual. Perfect.",
        "Michael gets me. Pete gets me. Nobody else gets me.",
        "My mom said to cheer up. I cheered down.",
        "I cried to a sad song. It was the best part of my day.",
        "Conformists at the mall. I judged them all.",
        "My eyeliner is perfect. My life is not.",
        "The void called. It wants me back.",
        "Boys are conformists. Except Michael. And Pete. They're different.",
        "I slammed my door so hard the house shook. Art.",
        "Darkness is my aesthetic. Sadness is my brand.",
        "Nobody understands me. That's the point.",
        "New kid seems... whatever. Fine. Whatever."),
    "Randy" to listOf(
        "Big news! I'm getting into... well, you'll see. It's HUGE.",
        "Tegridy! It's all about Tegridy!",
        "Sharon doesn't get my vision. Nobody gets my vision. That's why it's visionary.",
        "I was thinking of running for... something. Mayor? President? Pope?",
        "Stan! ...Sorry, thought you were my son. You look disappointed like him.",
        "Back in my day we didn't have Tegridy. We had REGRET.",
        "I tried to fix the sink. Now we need a new house.",
        "The news wants to interview me. Finally, recognition!",
        "I'm writing a book. Chapter one: me. Chapter two: also me.",
        "Towelie ate my lunch. I'm not even mad. That's growth.",
        "Sharon's mad at me. I don't know what I did. It's probably bad.",
        "New idea: Tegridy... in SPACE. Don't steal that."),
    "Sharon" to listOf(
        "Randy did WHAT now? ...Of course he did.",
        "Stanley! ...Sorry. Old habits.",
        "I married Randy for his... you know what, I don't remember.",
        "The PTA meeting was a disaster. Randy showed up. Uninvited.",
        "I'm this close to booking a one-way ticket somewhere warm.",
        "Shelly's being moody. Stan's being Stan. Randy's being Randy. Send wine.",
        "My book club read a novel about a woman who leaves her husband. Interesting.",
        "Randy's on the news again. I'm not watching.",
        "I love my family. I love my family. I love my family.",
        "If Randy says 'Tegridy' one more time I'm moving to my sister's.",
        "Stan's such a good boy. Takes after me. Thank God.",
        "The new kid seems sweet. Don't let Randy near him."),
    "Gerald" to listOf(
        "Objection! ...Sorry. Lawyer habit.",
        "The dolphins at the aquarium recognized me. We have a bond.",
        "Sheila's on another crusade. I'm hiding at work.",
        "Kyle! My boy! ...He's not here. He's never here when I practice this.",
        "As a lawyer, I can tell you this town is a lawsuit waiting to happen.",
        "Ike's doing great. Born in Canada, that one. Long story.",
        "Sheila asked me to sign something. I signed it. I always sign it.",
        "Being a dad is hard. Being Sheila's husband is harder.",
        "I represented a dolphin once. Pro bono. Beautiful creature.",
        "The guys at work don't get me. The dolphins get me.",
        "Kyle's got my brains and Sheila's fire. Poor kid never stood a chance.",
        "New kid, right? Welcome. Watch out for my wife."),
    "Sheila" to listOf(
        "WHAT WHAT WHAT did you just say?!",
        "I'm organizing a boycott. You're either with me or against decency.",
        "Blame Canada! ...Sorry. Old habits.",
        "Kyle! ...Not you. My Kyle. Where's my Kyle?",
        "This town is a moral sewer and I'm the plunger.",
        "I called the mayor. I called the governor. I'm calling EVERYONE.",
        "Gerald! Back me up! ...Gerald? Useless.",
        "The school is corrupting our children and I have PROOF.",
        "I haven't been this mad since... yesterday.",
        "What would the mothers of South Park say? I'LL TELL YOU WHAT THEY'D SAY.",
        "My book club agrees with me. My book club fears me. Same thing.",
        "New kid! Stay away from that Cartman boy. He's a bad influence."),
    "Liane" to listOf(
        "My Eric is such a good boy! Don't you think?",
        "I made cookies! For Eric! ...And you can have one, sweetie.",
        "Eric's been telling the most wonderful stories! He's so creative!",
        "The other mothers judge me. They're just jealous of my Eric.",
        "My little poopsiekins ate ALL his dinner! I'm so proud!",
        "Eric says you're his friend! That means you're family!",
        "I do declare, this town is just lovely.",
        "Eric's father? ...Let's talk about Eric instead!",
        "My boy's going places! Probably the principal's office! But places!",
        "I love all of Eric's little friends! Even the poor one!",
        "Being a mother is the hardest job. And the most rewarding!",
        "You seem like such a sweet child! Eric needs sweet friends!"),
    "Stuart" to listOf(
        "You hiring? ...Didn't think so.",
        "Kenny! ...Sorry. You looked like my boy from behind.",
        "We can't afford Christmas this year. Or birthdays. Or Tuesdays.",
        "The TV's broken. The car's broken. I'm broken. Pass the remote.",
        "Skeeter's got a special today. That's dinner.",
        "Carol's the best thing that ever happened to me. Don't tell her I said that.",
        "I worked a job once. It was awful. Never again. ...I need a job.",
        "Kenny's a good kid. Tough. Like his old man. Tougher, honestly.",
        "Life's hard, then you die, then your kid shows up fine the next day.",
        "The rich side of town. Hmph. Must be nice.",
        "My back hurts. My everything hurts.",
        "New kid, huh? Stay in school. Don't end up like... just stay in school."),
    "Carol" to listOf(
        "My babies! All my beautiful babies!",
        "The Lord provides. Mostly He provides overtime shifts.",
        "Kenny! My sweet boy! ...Where is he? He's always somewhere.",
        "I pray for this family every night. Every. Night.",
        "We may be poor, but we're rich in love! And prayer! Mostly prayer.",
        "Stuart's a good man. He tries. God bless him, he tries.",
        "Karen drew me a picture! It's beautiful! It's on the fridge!",
        "The church food drive saved us last month. Bless them.",
        "My Kenny's such a sweet boy. Always in that little parka.",
        "Don't you worry about us, sweetie. We're McCormicks. We're tough.",
        "I lit a candle for all of you today.",
        "New kid! You come by for dinner sometime! There's... soup."),
    "Stephen" to listOf(
        "Butters! ...He's not here. Good. I mean — carry on.",
        "You're grounded. ...You're not my kid. Disregard.",
        "This town is going to hell in a handbasket. I've seen the signs.",
        "Discipline! That's what's missing! And respect! And quiet!",
        "Linda! ...Sorry. Thought you were my wife.",
        "Butters got a B+. A B PLUS. We're getting a tutor. And a grounding.",
        "I work hard so this family can have RULES.",
        "The neighbors think we're strict. The neighbors are wrong. We're RIGHT.",
        "No dessert until the yard is clean. The yard is never clean.",
        "I saw Randy Marsh on TV. That man is everything wrong with America.",
        "Butters is a good boy. He gets it from Linda. Obviously.",
        "New kid. Stay out of trouble. I mean it."),
    "Linda" to listOf(
        "Oh! Hello, sweetie! You look hungry!",
        "Butters just LOVES his little friends! Are you one of them?",
        "Stephen! Be nice to the child! ...He's strict but he loves us.",
        "I made a casserole! It's still warm!",
        "My Butters is SUCH a good boy! Don't you think?",
        "The house is so quiet when Butters is grounded. Too quiet.",
        "I put extra cookies in Butters' lunch! Don't tell Stephen!",
        "Being a mom is the best! Even when your husband grounds everyone!",
        "Oh dear, is that rain? Butters! Take an umbrella! ...He's grounded.",
        "I love this town! Everyone's so... colorful!",
        "Stephen means well! He really does! ...Mostly!",
        "New kid! You come visit anytime! I'll make cookies!"),
    "Garrison" to listOf(
        "Okay class, today's lesson is: life is disappointing. Sit down.",
        "Mr. Hat says you're all idiots. Mr. Hat is never wrong.",
        "I ran for president once. You're welcome.",
        "Your homework is to question everything. Except me. Never me.",
        "The school board hates me. The feeling is mutual.",
        "Mr. Hat and I had a long talk about you. It wasn't flattering.",
        "I don't get paid enough for this. I don't get paid enough for anything.",
        "Detention! ...For me. I need a nap.",
        "You kids are the future. God help us all.",
        "My personal life is none of your business. ...It's a disaster.",
        "Teaching is a calling. Mine's been going to voicemail.",
        "New kid? Sit down, shut up, and try to keep up."),
    "Mackey" to listOf(
        "Drugs are bad, m'kay? ...That's the whole lesson. M'kay.",
        "How are we feeling today? M'kay? Good. M'kay.",
        "I had a dream I lost my glasses. It was terrifying, m'kay?",
        "Remember kids: say no to drugs, yes to counseling! M'kay?",
        "My head is huge, m'kay? It's a medical thing. M'kay.",
        "Feelings are like buses, m'kay? They come and go. M'kay.",
        "I tried coffee once. Never again, m'kay? My hands still shake.",
        "The children are our future, m'kay? God help the future.",
        "Counseling works, m'kay? I'm living proof. M'kay?",
        "Don't do drugs, stay in school, m'kay? That's the Mackey guarantee.",
        "I love my job, m'kay? The kids are great. M'kay.",
        "New kid! Welcome! Need counseling? Everyone needs counseling, m'kay?"),
    "Chef" to listOf(
        "Hello there, children! Who's hungry?",
        "I'm gonna make love to ya, woman! ...Sorry. Old song.",
        "Chocolate salty balls are back on the menu! Get 'em while they're hot!",
        "You got woman troubles, child? Chef's got advice. Bad advice, but advice.",
        "I sing when I cook. I cook when I sing. It's a whole thing.",
        "The kids come to me with everything. I give them soul food and soul advice.",
        "Respect the lunch lady! ...I'm the lunch man. Respect me anyway.",
        "My mama taught me this recipe. Then she taught me that song. Different lessons.",
        "Love is like a good gumbo, children. Spicy and confusing.",
        "I seen a lot in this cafeteria. A LOT.",
        "Keep the beat, children! Life's got rhythm!",
        "New kid! Sit down, eat up! You're family now!"),
    "Jesus" to listOf(
        "Hey, thanks for comin'. Beautiful day, isn't it?",
        "Walk with me. I know a shortcut. I know ALL the shortcuts.",
        "Bless you, child. Also, nice jacket.",
        "I turned water into wine once. The PTA still talks about it.",
        "Forgiveness is divine. So is a good taco.",
        "My carpentry business is booming. Pun intended. ...Sorry.",
        "The meek shall inherit the earth. The bold get the parking spots.",
        "I died for your sins. Try to make it worth it.",
        "Tune in to my show! 'Jesus and Pals.' Pals is Father Maxi. He's... trying.",
        "Miracles are just... Tuesday, around here.",
        "Love thy neighbor. Even Cartman. ESPECIALLY Cartman. He needs it most.",
        "New kid! Welcome to South Park! Mind the potholes."),
    "Jimbo" to listOf(
        "It's comin' right at us! ...The snow. The snow's comin' right at us.",
        "Ned and I bagged a twelve-pointer once. It was a stop sign. Still counts.",
        "You hunt? Everybody should hunt. Builds character. And freezers.",
        "Stan! ...You're not Stan. You got that Marsh look though.",
        "My nephew Stan's a good kid. Don't tell him I said that.",
        "Guns don't kill people. People with guns kill... wait. Let me start over.",
        "Ned lost his voice in 'Nam. Now he just nods. Good listener.",
        "Hunting season is the most wonderful time of the year.",
        "I sell guns AND wisdom. The wisdom's free. The guns are not.",
        "Saw a deer today. Beautiful creature. Didn't shoot it. Growth.",
        "The government's after my... actually, they're not. Paranoia's free too.",
        "New kid! You ever fire a gun? ...Kidding! Mostly."),
    "Father Maxi" to listOf(
        "Bless you, my child. ...I need to sit down.",
        "The Lord is testing me. The Lord is testing me DAILY.",
        "Have you seen my antacid? ...I mean, have you seen the light?",
        "This parish is going to be the end of me. ...Bless them anyway.",
        "I had a nightmare about the collection plate. It was empty.",
        "Faith, my child. Faith and deep breathing.",
        "The youth group is... energetic. Pray for me.",
        "I asked God for patience. He gave me South Park.",
        "Confession is good for the soul. Mine. I need it.",
        "The bishop called. I pretended to be the janitor.",
        "Every day I ask: why me, Lord? Every day He laughs.",
        "New child! Welcome! ...Please don't be like the others."),
    "Towelie" to listOf(
        "Don't forget to bring a towel! ...You forgot, didn't you?",
        "You wanna get high? ...No? Cool. Me neither. ...Maybe.",
        "I'm a towel. You're a person. We're both damp. Deep.",
        "Randy's growing weed. I'm growing... mold. We're both farmers.",
        "I had a thought once. It was terrible. Never again.",
        "Wanna hear a secret? ...I forgot it. Classic Towelie.",
        "Life's short. Stay moist.",
        "I remember when I was just a regular towel. Good times.",
        "The trick to drying off is... zzz... sorry, what?",
        "I'm not lazy, I'm absorbent. There's a difference.",
        "Oprah? Never met her. Would love to. Big fan.",
        "New kid! Bring a towel! ...That's my whole thing.")
)

// ---------------- Friend greetings (3 tiers x 2 lines) ----------------
val EXTRA_FRIEND_GREETINGS: Map<String, Map<Int, List<String>>> = mapOf(
    "Wendy" to mapOf(
        0 to listOf("Oh. Hi. You're the new kid.", "Hey. I'm Wendy. Don't be weird."),
        1 to listOf("Hey! Good to see you!", "Oh hey! We should hang out sometime!"),
        2 to listOf("My best friend! Okay, we NEED to talk about Stan.",
            "Bestie! Bebe and I were just talking about you! Good things! Mostly!")),
    "Craig" to mapOf(
        0 to listOf("...Hey. *flips you off* That's friendly, for me.", "What?"),
        1 to listOf("Hey. *flips you off, but warmly*", "Oh. It's you. Cool."),
        2 to listOf("My best friend. *flips you off* You know what that means.",
            "Hey. ...Thanks. For everything. Don't make it weird.")),
    "Tweek" to mapOf(
        0 to listOf("GAAAH! Oh. It's just you. Hi. Sorry. PRESSURE!",
            "Hi! Are you a gnome? ...You're not a gnome. Hi!"),
        1 to listOf("Oh good, it's you! My favorite non-gnome!",
            "Hi hi! Craig says you're okay! Craig's never wrong! GAAAH!"),
        2 to listOf("MY BEST FRIEND! Come in! Don't touch anything! It's all pressure!",
            "You! I saved you a coffee! DECAF! Because I care!")),
    "Clyde" to mapOf(
        0 to listOf("Hi... *sniff* I'm fine. Really.", "Oh hey. New kid, right?"),
        1 to listOf("Hey! *hugs you, crying a little* I'm happy, I swear!",
            "My friend! My good friend! *sniff*"),
        2 to listOf("BEST FRIEND! *full hug, full tears* This is the best day!",
            "You! *crying happy tears* I love you, man! Platonically! Mostly!")),
    "Jimmy" to mapOf(
        0 to listOf("Wow! A new audience member! Welcome!",
            "Hey there! Pull up a chair! The show's about to start!"),
        1 to listOf("My friend! Front row seats, always!",
            "Hey! I wrote a joke about you! It's flattering!"),
        2 to listOf("MY BEST FRIEND! Wow, what a great audience of ONE!",
            "You! I saved you the good seat! ...They're all good seats!")),
    "Timmy" to mapOf(
        0 to listOf("TIMMY! *waves*", "Timmy! New kid! TIMMY!"),
        1 to listOf("TIMMY! *happy wiggle* Friend!", "Timmy! You! TIMMY!"),
        2 to listOf("TIMMY!!! *tackle-hug* BEST FRIEND!",
            "TIMMY! *victory lap around you*")),
    "Tolkien" to mapOf(
        0 to listOf("Hey. New kid. I'm Tolkien.", "Oh hey. Welcome to South Park."),
        1 to listOf("Hey! Good to see you.", "My friend! What's up?"),
        2 to listOf("Best friend! Come by the house! The pool's heated!",
            "You! Mi casa es su casa! Literally!")),
    "Bebe" to mapOf(
        0 to listOf("Oh my god, hi! You're the new kid! Cute!",
            "Hiiii! I'm Bebe! We HAVE to be friends!"),
        1 to listOf("Bestie! Hi! Okay, gossip time!",
            "Oh my god, it's my favorite person!"),
        2 to listOf("MY BEST FRIEND! Eee! Okay, I have SO much to tell you!",
            "Bestie!! I saved you a seat! And a cookie! And gossip!")),
    "Michael" to mapOf(
        0 to listOf("...Oh. It's you. Whatever.", "New kid. How... conformist."),
        1 to listOf("Oh. Hey. ...Don't make it weird.", "You're back. Fine. Whatever."),
        2 to listOf("...Hey. You're alright. For a conformist. Don't tell anyone.",
            "My best friend. My journal is going to hear about this.")),
    "Pete" to mapOf(
        0 to listOf("Oh. You. ...Hi.", "New kid. Life is pain. Hi."),
        1 to listOf("Oh, it's you. ...Hey.", "Back again. Whatever. Hey."),
        2 to listOf("...My best friend. Don't tell Michael I smiled.",
            "Hey. You're the least awful person here.")),
    "Henrietta" to mapOf(
        0 to listOf("Ugh. Hi. I guess.", "New kid. Whatever."),
        1 to listOf("Oh. It's you. ...Hi.", "Hey. Don't tell anyone I'm happy to see you."),
        2 to listOf("MY BEST FRIEND! ...I mean. Whatever. I'm glad you're here.",
            "Bestie! My diary has three pages about you! All good! Mostly!")),
    "Randy" to mapOf(
        0 to listOf("Hey there! New kid, right? I'm Randy! Stan's dad!",
            "Oh hey! Welcome to South Park! I'm Randy!"),
        1 to listOf("My friend! Come by the farm! I'll show you the Tegridy!",
            "Hey! Good to see you! Got a minute? I have an idea!"),
        2 to listOf("MY BEST FRIEND! You're like family! Like a son! Stan, who's Stan?",
            "Best buddy! I'm putting you in my will! Right after Stan! Maybe before!")),
    "Sharon" to mapOf(
        0 to listOf("Oh hello, dear. You're the new kid. I'm Sharon.",
            "Hi sweetie. Welcome. Watch out for my husband."),
        1 to listOf("Hello dear! Good to see you!", "Oh hi! Come in, come in!"),
        2 to listOf("My dear friend! You're like one of my own!",
            "Sweetie! I made extra! You look thin!")),
    "Gerald" to mapOf(
        0 to listOf("Hello! Gerald Broflovski! Welcome to the neighborhood!",
            "Oh hey! New kid! I'm Gerald! Kyle's dad!"),
        1 to listOf("My friend! Good to see you!", "Hello hello! How's life treating you?"),
        2 to listOf("My best friend! You're practically family!",
            "Dear friend! The dolphins and I both adore you!")),
    "Sheila" to mapOf(
        0 to listOf("WHAT?! Oh. It's just the new kid. Hello.",
            "New kid! Stay out of trouble! ...That's all. Bye."),
        1 to listOf("Oh! Hello dear! Good to see a polite child!",
            "Well hello! You're a good influence! I can tell!"),
        2 to listOf("MY DEAR FRIEND! You're like a second son! ...Daughter? Child!",
            "Sweetie! I'm SO glad we met! WHAT WHAT WHAT a joy!")),
    "Liane" to mapOf(
        0 to listOf("Oh! Hello sweetie! Are you Eric's friend?",
            "Well hi there! What a sweet child!"),
        1 to listOf("Hello sweetie! Eric talks about you ALL the time!",
            "Oh, my favorite! Come in! Cookies!"),
        2 to listOf("MY SWEET CHILD! You're like a second son to me!",
            "Best friend! Eric's best friend is MY best friend!")),
    "Stuart" to mapOf(
        0 to listOf("Hey. New kid, huh? ...Hey.", "Oh. Hi."),
        1 to listOf("Hey, it's you. How's it going?", "Oh hey. Good to see ya."),
        2 to listOf("My friend! ...My best friend! Here, sit. You want a soda?",
            "Hey! Best buddy! Kenny talks about you! ...Kenny doesn't talk. You know.")),
    "Carol" to mapOf(
        0 to listOf("Oh hello, sweetie! Bless you!", "Hi there! Are you a friend of my babies?"),
        1 to listOf("Hello dear! So good to see you!", "Oh, my sweet friend! Come in!"),
        2 to listOf("MY DEAR FRIEND! You're family now! Bless you!",
            "Sweetie! I prayed for you today! And look — here you are!")),
    "Stephen" to mapOf(
        0 to listOf("New kid. ...Behave.", "Hm. You. Don't cause trouble."),
        1 to listOf("Oh. It's you. You're... acceptable.", "Hello. Stay out of trouble."),
        2 to listOf("My friend. You're a good kid. Don't tell Butters I said that.",
            "Best... friend. You're alright. For a child.")),
    "Linda" to mapOf(
        0 to listOf("Oh hello, sweetie! What a dear child!",
            "Hi there! Are you Butters' friend?"),
        1 to listOf("Hello dear! So lovely to see you!", "Oh, my sweet friend! Cookies?"),
        2 to listOf("MY DEAR FRIEND! You're just the sweetest!",
            "Bestie! I made your favorite! ...I don't know your favorite! I'll make everything!")),
    "Garrison" to mapOf(
        0 to listOf("Oh great. Another one. Sit down.", "New kid. Try to keep up."),
        1 to listOf("Oh. It's you. You're not the worst.", "Hey. Don't tell anyone I said hi."),
        2 to listOf("My best student! ...My only tolerable student!",
            "You! Mr. Hat likes you! That's rare! Terrifying, but rare!")),
    "Mackey" to mapOf(
        0 to listOf("Well hello there! M'kay? I'm Mr. Mackey!",
            "New student! Welcome! M'kay?"),
        1 to listOf("My friend! M'kay? Good to see you!", "Hello hello! M'kay?"),
        2 to listOf("MY BEST FRIEND! M'kay?! This is wonderful! M'kay!",
            "Best buddy! M'kay? I'm so proud of you! M'kay?")),
    "Chef" to mapOf(
        0 to listOf("Hello there, child! Welcome!", "Well hey there! New in town?"),
        1 to listOf("My friend! Good to see you!", "Hello there! Hungry?"),
        2 to listOf("MY BEST FRIEND! Come here! Chef's got a song for you!",
            "My dear child! You're family now! Say hello to my other children!")),
    "Jesus" to mapOf(
        0 to listOf("Hey there. Welcome to South Park.", "Bless you, child. Thanks for comin'."),
        1 to listOf("My friend! Good to see you!", "Hey! Walk with me a while!"),
        2 to listOf("My dearest friend! A miracle! ...You're the miracle.",
            "Best friend! Come, sit! Tell me everything!")),
    "Jimbo" to mapOf(
        0 to listOf("Well hey there! New kid, huh? I'm Jimbo!",
            "Oh hey! Welcome to town!"),
        1 to listOf("My friend! Good to see ya!", "Hey! Come by the shop sometime!"),
        2 to listOf("MY BEST FRIEND! You're like a nephew to me! A second nephew!",
            "Best buddy! I'm teaching you to hunt! ...Kidding! Mostly!")),
    "Father Maxi" to mapOf(
        0 to listOf("Oh! Bless you, child. ...Welcome.", "Hello, my child. Peace be with you."),
        1 to listOf("My friend! Bless you!", "Hello! Good to see you! ...I think!"),
        2 to listOf("MY DEAREST FRIEND! A blessing upon you! ...I need to sit down.",
            "Best friend! The Lord brought you to me! Probably!")),
    "Towelie" to mapOf(
        0 to listOf("Hey. Don't forget to bring a towel!", "Oh hey. New kid. Towelie."),
        1 to listOf("My friend! Hey! You wanna get high? ...Kidding! ...Mostly!",
            "Hey hey! Good to see you! Don't forget the towel!"),
        2 to listOf("MY BEST FRIEND! You're the best! Don't forget to bring a towel!",
            "Best buddy! I love you, man! ...You wanna get high?"))
)

// ---------------- Player-talk response banks (6 intents x 2 lines) ----------------
val EXTRA_RESPONSE_BANKS: Map<String, Map<String, List<String>>> = mapOf(
    "Wendy" to mapOf(
        "greeting" to listOf("Hi {newkid}! What's up?", "Oh hey {newkid}!"),
        "insult" to listOf("EXCUSE me?! Say that again, {newkid}. I DARE you.",
            "Wow. Rude. I'm telling everyone. Starting now."),
        "question" to listOf("Good question, {newkid}. The answer is usually 'Cartman did it.'",
            "Hmm. Let me think... Ask Bebe. She knows everything."),
        "bye" to listOf("Bye {newkid}! Stay smart!", "See ya! Don't let the boys drag you down!"),
        "compliment" to listOf("Thanks {newkid}! You're pretty great yourself!",
            "Aw! See? Chivalry isn't dead!"),
        "default" to listOf("Interesting, {newkid}. Tell me more.", "Huh. Okay. Anyway...")),
    "Craig" to mapOf(
        "greeting" to listOf("Hey. *flips you off* Hi, {newkid}.", "Oh. It's you."),
        "insult" to listOf("*flips you off* Right back at ya, {newkid}.",
            "Wow. Okay. *double middle fingers*"),
        "question" to listOf("Don't know, {newkid}. Don't care.", "Ask Tweek. He's the smart one. Somehow."),
        "bye" to listOf("Later. *flips you off*", "Bye, {newkid}. *finger*"),
        "compliment" to listOf("...Thanks. *flips you off affectionately*",
            "Whatever. *finger* ...Thanks though."),
        "default" to listOf("Cool. *finger*", "Yeah. Sure. Whatever, {newkid}.")),
    "Tweek" to mapOf(
        "greeting" to listOf("GAAAH! Oh! Hi {newkid}! Sorry! Hi!",
            "Oh good, it's you {newkid}! Not a gnome! Hi!"),
        "insult" to listOf("GAAAH! Why would you say that, {newkid}?! PRESSURE!",
            "Too much! Too mean! GAAAH!"),
        "question" to listOf("I don't know, {newkid}! Ask the gnomes! GAAAH!",
            "What? Huh? Sorry, {newkid}, what?"),
        "bye" to listOf("Bye {newkid}! Don't let the gnomes get you!",
            "See ya! Stay calm! I can't, but you can!"),
        "compliment" to listOf("Really, {newkid}?! GAAAH! Thank you!",
            "Oh! Oh good! I'm not terrible! Thank you!"),
        "default" to listOf("Uh huh, {newkid}! Sure! GAAAH!", "What? Sorry! Say again, {newkid}!")),
    "Clyde" to mapOf(
        "greeting" to listOf("Hi {newkid}! *sniff* I'm fine!", "Oh hey {newkid}!"),
        "insult" to listOf("*cries* Why would you say that, {newkid}?!",
            "That hurt my feelings, {newkid}! *sniff*"),
        "question" to listOf("I don't know, {newkid}, but I FEEL like... no. No idea.",
            "Hmm! Good question! ...I forgot the question. *cries a little*"),
        "bye" to listOf("Bye {newkid}! *waves, crying happy tears*",
            "See ya! I'll miss you! *sniff*"),
        "compliment" to listOf("Really?! *cries happy tears* Thank you, {newkid}!",
            "You think so?! *hugs you* Best day ever!"),
        "default" to listOf("Yeah, {newkid}... *sniff*", "Totally. *wipes eyes*")),
    "Jimmy" to mapOf(
        "greeting" to listOf("Wow! {newkid}! What a great audience!",
            "Hey {newkid}! Pull up a chair!"),
        "insult" to listOf("Ouch! {newkid}! I'm telling that one on stage!",
            "Wow! Heckled by {newkid}! This is going in the act!"),
        "question" to listOf("Great question, {newkid}! ...I don't know the answer. Next!",
            "Hmm! Let me work that into a bit, {newkid}!"),
        "bye" to listOf("Goodnight everybody! ...Just {newkid}. Goodnight {newkid}!",
            "You've been great, {newkid}! Tip your waitress!"),
        "compliment" to listOf("Thank you! Thank you! {newkid} gets it!",
            "Wow! {newkid}! You're too kind! Encore! Encore!"),
        "default" to listOf("Ha! Good one, {newkid}!", "{newkid}! Classic!")),
    "Timmy" to mapOf(
        "greeting" to listOf("TIMMY! Hi {newkid}!", "(muffled?) No — TIMMY! Hi!"),
        "insult" to listOf("TIMMY?! *looks hurt* ...Timmy forgives!", "Timmy! *shakes head*"),
        "question" to listOf("TIMMY! *shrugs* Timmy!", "Timmy? TIMMY!"),
        "bye" to listOf("TIMMY! Bye {newkid}!", "Timmy! *waves* TIMMY!"),
        "compliment" to listOf("TIMMY! *happy wiggle* Thank you!", "Timmy! You! TIMMY!"),
        "default" to listOf("TIMMY!", "Timmy! {newkid}! TIMMY!")),
    "Tolkien" to mapOf(
        "greeting" to listOf("Hey {newkid}.", "Oh hey {newkid}. What's up?"),
        "insult" to listOf("Wow, {newkid}. That was... something.", "Okay. That was uncalled for, {newkid}."),
        "question" to listOf("Beats me, {newkid}. I'm rich, not omniscient.",
            "Good question. Let me ask my dad. He knows a guy."),
        "bye" to listOf("Later, {newkid}.", "See ya. Come by the house sometime."),
        "compliment" to listOf("Thanks {newkid}. Appreciate it.", "Ha. Thanks, man."),
        "default" to listOf("Yeah. Fair, {newkid}.", "Huh. Okay.")),
    "Bebe" to mapOf(
        "greeting" to listOf("Hiii {newkid}!", "Oh my god, hi {newkid}!"),
        "insult" to listOf("EXCUSE me?! Do you know who I am, {newkid}?!",
            "Rude! I'm telling Wendy! And everyone!"),
        "question" to listOf("Ooh, good question {newkid}! The answer is: cute boys.",
            "Hmm! Let me ask Wendy! She knows everything!"),
        "bye" to listOf("Bye {newkid}! Stay cute!", "See ya! Text me!"),
        "compliment" to listOf("I KNOW, right?! Thanks {newkid}!",
            "Aw! You're the sweetest, {newkid}!"),
        "default" to listOf("Totally, {newkid}!", "Oh my god, I know, right?!")),
    "Michael" to mapOf(
        "greeting" to listOf("...Oh. Hi, {newkid}.", "New kid. Whatever."),
        "insult" to listOf("Wow. How... conformist of you, {newkid}.",
            "I'm writing that down, {newkid}. In my burn book. It's black."),
        "question" to listOf("Why? Nothing matters, {newkid}. ...But ask anyway.",
            "The answer is darkness, {newkid}. The question doesn't matter."),
        "bye" to listOf("Go. Be free, {newkid}. ...Bye.", "Later, conformist."),
        "compliment" to listOf("...Thanks. I guess. Don't make it weird, {newkid}.",
            "Whatever. ...Thanks though."),
        "default" to listOf("...Sure, {newkid}.", "Mm. Profound.")),
    "Pete" to mapOf(
        "greeting" to listOf("Oh. Hi, {newkid}.", "...Hey."),
        "insult" to listOf("Wow, {newkid}. Even my misery is disappointed.",
            "That hurt. A little. Whatever."),
        "question" to listOf("Don't know, {newkid}. Don't care. ...Ask Michael.",
            "The answer is pain, {newkid}. It's always pain."),
        "bye" to listOf("Bye, {newkid}. ...Whatever.", "Later."),
        "compliment" to listOf("...Thanks, {newkid}. I think.", "Huh. Okay. Thanks."),
        "default" to listOf("Yeah. Whatever, {newkid}.", "Mm.")),
    "Henrietta" to mapOf(
        "greeting" to listOf("Ugh. Hi, {newkid}.", "Oh. It's you. Hi."),
        "insult" to listOf("HOW DARE YOU, {newkid}! ...Whatever. I'm fine.",
            "Rude! My diary is HEARING this, {newkid}!"),
        "question" to listOf("Ugh, questions. Fine. I don't know, {newkid}.",
            "Why are you asking me, {newkid}? Ask a conformist."),
        "bye" to listOf("Bye, {newkid}. ...Don't miss me too much.",
            "Later. I'll be in my room. Being dark."),
        "compliment" to listOf("...You think so? ...Thanks, {newkid}. Don't tell anyone.",
            "Ugh, stop. ...Okay, thanks."),
        "default" to listOf("Whatever, {newkid}.", "Ugh. Fine.")),
    "Randy" to mapOf(
        "greeting" to listOf("Hey there, {newkid}! I'm Randy!", "Oh hey! New kid! Great to see ya!"),
        "insult" to listOf("Whoa, {newkid}! I'm sorry, I thought this was America!",
            "Hey! You can't talk to me like that! I'm... Randy!"),
        "question" to listOf("Great question, {newkid}! The answer is Tegridy!",
            "Hmm! I don't know, but I have an IDEA about it!"),
        "bye" to listOf("See ya, {newkid}! Remember: Tegridy!", "Later! Come by the farm!"),
        "compliment" to listOf("Ha! Thanks, {newkid}! I know, I'm great!",
            "You get it, {newkid}! You really get it!"),
        "default" to listOf("Interesting, {newkid}! Tell me more! I have an idea!",
            "Huh! Wow! Anyway, Tegridy!")),
    "Sharon" to mapOf(
        "greeting" to listOf("Hello dear! How are you, {newkid}?", "Oh hi, {newkid}!"),
        "insult" to listOf("{newkid}! That's not how we talk to people! Apologize!",
            "Excuse me?! Young man/lady! ...Sorry, {newkid}, force of habit."),
        "question" to listOf("Good question, {newkid}. Ask your mother. ...I'm not your mother. Sorry.",
            "Hmm. Let me think... No. Ask Randy. He'll make something up."),
        "bye" to listOf("Bye dear! Be safe!", "See you later, {newkid}!"),
        "compliment" to listOf("Oh! Thank you, {newkid}! That's sweet!",
            "What a dear! Thank you!"),
        "default" to listOf("Mm-hm, {newkid}.", "Oh. Okay, dear.")),
    "Gerald" to mapOf(
        "greeting" to listOf("Hello there, {newkid}! I'm Gerald!", "Oh hey, {newkid}!"),
        "insult" to listOf("Now hold on, {newkid}! As a lawyer, I object!",
            "Whoa! Let's all calm down, {newkid}!"),
        "question" to listOf("Good question! Legally speaking... I have no idea, {newkid}.",
            "Hmm! Ask me about dolphins instead, {newkid}!"),
        "bye" to listOf("Bye, {newkid}! Say hi to the family!", "Later!"),
        "compliment" to listOf("Ha! Thanks, {newkid}!", "Why thank you!"),
        "default" to listOf("Interesting, {newkid}!", "Huh! Okay!")),
    "Sheila" to mapOf(
        "greeting" to listOf("Hello, {newkid}! Be good!", "Oh! Hello dear!"),
        "insult" to listOf("WHAT did you just say to me, {newkid}?! WHAT WHAT WHAT?!",
            "EXCUSE ME?! I'm calling your mother! ...Whoever she is!"),
        "question" to listOf("The answer is NO, {newkid}! Whatever it was, NO!",
            "Ask Gerald! ...Actually don't. He'll just agree with me."),
        "bye" to listOf("Bye, {newkid}! Behave!", "Goodbye! Stay out of trouble!"),
        "compliment" to listOf("Well! Thank you, {newkid}! Polite child!",
            "Oh! How sweet! Thank you!"),
        "default" to listOf("Mm-hm, {newkid}. WHAT did you say?", "Hm? Speak up, {newkid}!")),
    "Liane" to mapOf(
        "greeting" to listOf("Hello sweetie! Are you hungry, {newkid}?", "Oh hi, {newkid}!"),
        "insult" to listOf("Oh my! That's not nice, {newkid}! Eric, did you hear that?!",
            "Sweetie! We don't talk like that! ...Do we, Eric?"),
        "question" to listOf("Oh! Let me think, {newkid}! ...Eric usually knows!",
            "Hmm! Good question! Ask Eric! He's so smart!"),
        "bye" to listOf("Bye sweetie! Come back soon!", "Goodbye, {newkid}! Cookies next time!"),
        "compliment" to listOf("Oh! Thank you, sweetie! You're so kind!",
            "What a sweet child! Thank you, {newkid}!"),
        "default" to listOf("Oh! How nice, {newkid}!", "Mm-hm, sweetie!")),
    "Stuart" to mapOf(
        "greeting" to listOf("Hey, {newkid}.", "Oh. Hi."),
        "insult" to listOf("Hey! Watch it, {newkid}!", "The hell, {newkid}?!"),
        "question" to listOf("How should I know, {newkid}?", "Ask somebody with money, {newkid}."),
        "bye" to listOf("Later, {newkid}.", "See ya."),
        "compliment" to listOf("Ha. Thanks, {newkid}.", "...Thanks."),
        "default" to listOf("Yeah, {newkid}.", "Huh.")),
    "Carol" to mapOf(
        "greeting" to listOf("Hello sweetie! Bless you, {newkid}!", "Oh hi, {newkid}!"),
        "insult" to listOf("Oh! Sweetie! That's not kind! ...I forgive you, {newkid}.",
            "Bless your heart, {newkid}. ...And watch your mouth."),
        "question" to listOf("Oh! Let me think, {newkid}! ...Pray on it!",
            "Good question! The Lord knows! I don't, but He does!"),
        "bye" to listOf("Bye sweetie! God bless!", "Goodbye, {newkid}! Stay safe!"),
        "compliment" to listOf("Oh! Thank you, sweetie! Bless you!",
            "What a dear! Thank you, {newkid}!"),
        "default" to listOf("Oh! Okay, sweetie!", "Mm-hm, {newkid}!")),
    "Stephen" to mapOf(
        "greeting" to listOf("{newkid}. ...Hello.", "Oh. It's you."),
        "insult" to listOf("EXCUSE me?! {newkid}, you're GROUNDED! ...Metaphorically!",
            "That's it, {newkid}! No dessert! ...You're not my kid. Disregard."),
        "question" to listOf("The answer is no, {newkid}.", "I don't know and I don't care, {newkid}."),
        "bye" to listOf("Goodbye, {newkid}. Behave.", "Later."),
        "compliment" to listOf("...Thank you, {newkid}. Don't tell anyone.",
            "Hm. Thanks. You're... adequate."),
        "default" to listOf("Mm, {newkid}.", "Noted.")),
    "Linda" to mapOf(
        "greeting" to listOf("Hello sweetie! Oh, it's you, {newkid}!", "Oh hi, {newkid}!"),
        "insult" to listOf("Oh my! Sweetie! That's not nice! ...Stephen! ...Sorry, {newkid}.",
            "Oh dear! Well! ...I forgive you, {newkid}!"),
        "question" to listOf("Oh! Good question, {newkid}! ...Stephen usually knows!",
            "Hmm! Let me think! ...Nope! Ask Stephen!"),
        "bye" to listOf("Bye sweetie! Come back soon!", "Goodbye, {newkid}!"),
        "compliment" to listOf("Oh! Thank you, sweetie! You're so sweet!",
            "What a dear child! Thank you, {newkid}!"),
        "default" to listOf("Oh! How nice, {newkid}!", "Mm-hm, sweetie!")),
    "Garrison" to mapOf(
        "greeting" to listOf("Oh great, it's {newkid}. What do you want?",
            "New kid. Sit down. ...You're standing. Whatever."),
        "insult" to listOf("EXCUSE ME?! {newkid}, do you know who I am?! ...Nobody knows.",
            "Oh, it's ON, {newkid}! Mr. Hat! Get him! ...Her! Them!"),
        "question" to listOf("Why are you asking ME, {newkid}? I'm the teacher. I ask the questions.",
            "The answer is D, {newkid}. It's always D."),
        "bye" to listOf("Yeah, whatever, {newkid}. Class dismissed.", "Bye. Don't come back. ...Come back."),
        "compliment" to listOf("Finally! Recognition! Thanks, {newkid}!",
            "Damn right I'm great. Tell the school board."),
        "default" to listOf("Cool story, {newkid}. Anyway...", "Fascinating. ...Not really.")),
    "Mackey" to mapOf(
        "greeting" to listOf("Well hello there, {newkid}! M'kay?", "Oh hey! M'kay?"),
        "insult" to listOf("Now now, {newkid}! That's not okay, m'kay?!",
            "Whoa! Language, {newkid}! M'kay?!"),
        "question" to listOf("Good question, {newkid}! M'kay? ...I don't know. M'kay?",
            "Hmm! Let's talk about your feelings instead, m'kay?"),
        "bye" to listOf("Bye, {newkid}! M'kay?", "See ya! Stay drug-free, m'kay?"),
        "compliment" to listOf("Why thank you, {newkid}! M'kay?!",
            "Oh! That's nice! M'kay? Thank you!"),
        "default" to listOf("M'kay, {newkid}.", "Interesting! M'kay?")),
    "Chef" to mapOf(
        "greeting" to listOf("Hello there, {newkid}!", "Well hey there, child!"),
        "insult" to listOf("Whoa now, child! That ain't right!",
            "Hey! You watch that mouth, {newkid}!"),
        "question" to listOf("Good question, child! Chef's got the answer! ...Somewhere!",
            "Hmm! Let me sing you the answer, {newkid}!"),
        "bye" to listOf("Bye now, child! Eat your vegetables!",
            "See ya, {newkid}! Keep the beat!"),
        "compliment" to listOf("Why thank you, child! You're sweet as pie!",
            "Ha! Thank you, {newkid}!"),
        "default" to listOf("Mm-hm, child!", "I hear ya, {newkid}!")),
    "Jesus" to mapOf(
        "greeting" to listOf("Hey there, {newkid}.", "Bless you, {newkid}."),
        "insult" to listOf("I forgive you, {newkid}. ...But ouch.",
            "Turn the other cheek, {newkid}. ...That's this one."),
        "question" to listOf("Good question, child. The answer is love. ...And tacos.",
            "Ask, and you shall receive, {newkid}. ...Within reason."),
        "bye" to listOf("Go in peace, {newkid}.", "Bless you. See you soon."),
        "compliment" to listOf("Thank you, child. Bless you.", "Kind words! Thank you, {newkid}."),
        "default" to listOf("Mm. I see, {newkid}.", "Indeed, child.")),
    "Jimbo" to mapOf(
        "greeting" to listOf("Well hey there, {newkid}!", "Oh hey! New kid!"),
        "insult" to listOf("Whoa now, {newkid}! It's comin' right at us! ...Kidding!",
            "Hey! You can't talk to Jimbo like that!"),
        "question" to listOf("Good question, {newkid}! The answer is usually 'shoot it.' Kidding! Mostly!",
            "Hmm! Ask Ned! ...He'll just nod. Ask me again."),
        "bye" to listOf("See ya, {newkid}!", "Later! Come by the shop!"),
        "compliment" to listOf("Ha! Thanks, {newkid}!", "Why thank you!"),
        "default" to listOf("Huh! Okay, {newkid}!", "Interesting!")),
    "Father Maxi" to mapOf(
        "greeting" to listOf("Oh! Hello, child! ...Bless you.", "Peace be with you, {newkid}."),
        "insult" to listOf("Oh! Oh my! ...I forgive you, {newkid}. I need to sit down.",
            "Child! Language! ...Bless you anyway."),
        "question" to listOf("A question! ...Oh dear. Let me pray on it, {newkid}.",
            "Hmm! The catechism says... oh dear. I'll get back to you."),
        "bye" to listOf("Go in peace, {newkid}. ...I need to sit down.",
            "Bless you! Goodbye!"),
        "compliment" to listOf("Oh! Thank you, child! Bless you!",
            "How kind! Thank you, {newkid}!"),
        "default" to listOf("Yes, child... {newkid}.", "Mm. Bless you.")),
    "Towelie" to mapOf(
        "greeting" to listOf("Hey {newkid}! Don't forget to bring a towel!",
            "Oh hey! New kid! Towelie!"),
        "insult" to listOf("Whoa, {newkid}! Harsh! ...You wanna get high?",
            "Rude! ...I forgive you. I'm a towel. I absorb."),
        "question" to listOf("Good question, {newkid}! ...I forgot the answer.",
            "Hmm! Let me think! ...Nope! Got nothin'!"),
        "bye" to listOf("Bye {newkid}! Don't forget to bring a towel!",
            "Later! Stay moist!"),
        "compliment" to listOf("Thanks, {newkid}! You're alright! For a non-towel!",
            "Aw! Thanks, man!"),
        "default" to listOf("Cool, {newkid}!", "Yeah! ...What were we talking about?")),
)

// ---------------- Sleepy one-liners ----------------
val EXTRA_SLEEPY_LINES: Map<String, List<String>> = mapOf(
    "Wendy" to listOf("Mmm... five more minutes... of smashing the patriarchy...",
        "Zzz... Stan... stop throwing up..."),
    "Craig" to listOf("Mmm... *flips off the dream* ...", "Zzz..."),
    "Tweek" to listOf("GAAAH! ...Oh. Dream. ...Zzz... gnomes...",
        "Mmm... pressure... gone... zzz..."),
    "Clyde" to listOf("*sniff* ...Mmm... sad dream... zzz...", "Zzz... *cries a little*"),
    "Jimmy" to listOf("Mmm... wow... what a great... zzz...",
        "Zzz... *mumbles a punchline*"),
    "Timmy" to listOf("TIMMY! ...Zzz... timmy...", "Mmm... TIMMY... zzz..."),
    "Tolkien" to listOf("Mmm... not now... producing... zzz...", "Zzz..."),
    "Bebe" to listOf("Mmm... cute boys... zzz...", "Zzz... my hair... perfect..."),
    "Michael" to listOf("Mmm... the void... is quiet... zzz...",
        "Zzz... conformists... even in dreams..."),
    "Pete" to listOf("Mmm... even sleep... is pain... zzz...", "Zzz..."),
    "Henrietta" to listOf("Mmm... dear diary... zzz...", "Zzz... darkness... cozy..."),
    "Randy" to listOf("Mmm... Tegridy... zzz...", "Zzz... Sharon... I'm sorry... what did I do..."),
    "Sharon" to listOf("Mmm... Stanley... behave... zzz...", "Zzz... Randy... don't..."),
    "Gerald" to listOf("Mmm... dolphins... zzz...", "Zzz... objection... sustained..."),
    "Sheila" to listOf("Mmm... WHAT?! ...zzz...", "Zzz... boycott... the Sandman..."),
    "Liane" to listOf("Mmm... my Eric... my angel... zzz...", "Zzz... cookies..."),
    "Stuart" to listOf("Mmm... five more... dollars... zzz...", "Zzz..."),
    "Carol" to listOf("Mmm... bless... my babies... zzz...", "Zzz... amen..."),
    "Stephen" to listOf("Mmm... grounded... everyone... zzz...", "Zzz... Butters... behave..."),
    "Linda" to listOf("Mmm... my sweet boy... zzz...", "Zzz... cookies..."),
    "Garrison" to listOf("Mmm... Mr. Hat... you drive... zzz...",
        "Zzz... sit down... shut up..."),
    "Mackey" to listOf("Mmm... m'kay... zzz...", "Zzz... drugs are bad... m'kay..."),
    "Chef" to listOf("Mmm... hello there... zzz...", "Zzz... *hums a love song*"),
    "Jesus" to listOf("Mmm... rest... the seventh day... zzz...", "Zzz..."),
    "Jimbo" to listOf("Mmm... it's comin'... right at... zzz...", "Zzz... Ned..."),
    "Father Maxi" to listOf("Mmm... bless... I need... to sit down... zzz...", "Zzz..."),
    "Towelie" to listOf("Mmm... don't forget... to bring... zzz...",
        "Zzz... you wanna... get high... zzz...")
)

// ---------------- Merge into GameData ----------------
// (GameData.kt merges these into its base maps.)

// ============================================================
// v0.2.0d — WRITING DEPTH PASS #2.
// MORE_LINE_BANKS: +5 collision lines per cast member (every
// bank 4 -> 9), grounded in each character's lorebook persona —
// obsessions, relationships, running gags, speech patterns.
// All newly written in-voice; GameData merges these into
// LINE_BANKS. {other}/{location}/{newkid} templating as usual.
// ============================================================
val MORE_LINE_BANKS: Map<String, List<String>> = mapOf(
    "Cartman" to listOf(
        "Listen, {other}, I'm not saying I'm the most important person at {location}. The facts are saying it.",
        "I have a plan, {other}. It involves you doing the work and me taking the credit. Classic teamwork.",
        "My mom packed me TWO juice boxes, {other}. TWO. This is what power looks like.",
        "You know what your problem is, {other}? You're not me. It's a widespread condition.",
        "I'm not fat, {other}. I'm festively plump. There's a difference and it's huge. Like me. But not fat."),
    "Stan" to listOf(
        "Dude, my dad did something again. I don't even know what yet. I can just feel it.",
        "{other}, be honest: is it normal that nothing in this town is normal?",
        "Sparky buried something in the yard again. At this point I'm scared to dig.",
        "I tried writing a song about {location}. It just turned into sighing with chords.",
        "Some days I think I'm the only sane person here, {other}. Then I remember I talk to you."),
    "Kyle" to listOf(
        "I read three articles about {location} before coming here, {other}. I have concerns and a bibliography.",
        "My little brother said his first full sentence today, {other}. It was about Cartman. We're so proud.",
        "You know what really makes me mad, {other}? Everything. But today, specifically this.",
        "I made a spreadsheet of Cartman's lies, {other}. It has tabs now. TABS.",
        "Mom says I need to 'let things go,' {other}. Mom has never met Cartman."),
    "Kenny" to listOf(
        "(muffled) Mmph rmph! *holds up a cool rock* Rmph rmph!",
        "(muffled) Mmmph! *points at {other}, then at himself, then shrugs*",
        "(muffled) Rmph rmph rmph! *pretends the ground is lava*",
        "(muffled) Mmph! *snowball behind his back* Rmph?",
        "(muffled) Mmmph rmph! *happy little spin* Rmph!"),
    "Butters" to listOf(
        "Well gee, {other}, I sure do like standin' around {location} with ya!",
        "My dad says loiterin' is a gateway to groundin', {other}. We're livin' on the edge!",
        "I brought a snack to share, {other}! It's mostly a napkin, but the thought's in there!",
        "Do you think the squirrels at {location} have names for US, {other}? I bet mine's 'the nice one'!",
        "If I get grounded for this, {other}, it was worth it. Probably. Oh hamburgers, was it?"),
    "Wendy" to listOf(
        "{other}, I have a petition, a plan, and a backup plan. Pick your level of involvement.",
        "I read the {location} bylaws, {other}. All of them. There will be a quiz.",
        "Stan threw up twice today, {other}. We're basically married.",
        "Don't mistake my being polite for agreement, {other}. I'm loading.",
        "Bebe says I plan too much, {other}. Bebe has a color-coded binder too. Hers is just cuter."),
    "Craig" to listOf(
        "{other}. *flips you off* That's it. That's the whole conversation.",
        "If I cared about {location}, this is where I'd show it. *doesn't*",
        "Tweek stress-drank four coffees because of you, {other}. Impressive, honestly.",
        "My guinea pig has stronger opinions than you, {other}. And he's a guinea pig.",
        "*flips off nothing in particular* Just keeping the streak alive, {other}."),
    "Tweek" to listOf(
        "GAAAH! {other}! Don't sneak up on me like that! THEY could be anywhere!",
        "I counted the cups at {location}, {other}. One is MISSING. That's phase one!",
        "Too much pressure, {other}! My hands are shaking and I haven't even had coffee yet! ...Okay, I've had five.",
        "Craig says deep breaths, {other}. CRAIG. Breathing is how the gnomes TRACK YOU.",
        "Have you seen my left sock, {other}? It's gone. GONE. You know what that means!"),
    "Clyde" to listOf(
        "*sniff* Sorry, {other}. The light at {location} just hit me right in the feelings.",
        "I cried at a soup commercial today, {other}. The soup found its family. It was beautiful.",
        "Do you think I'm too sensitive, {other}? Be gentle when you answer. Please.",
        "Craig flipped me off and I felt so SEEN, {other}. *happy tears*",
        "I wrote a poem for my mom, {other}. I cried writing it. She'll cry reading it. It's a whole thing."),
    "Jimmy" to listOf(
        "W-wow, {other}! Terrific timing! I was just workshopping a bit about {location}!",
        "You ever notice, {other}, that {location} is just... it's a lot, right? That's the bit. I'm keeping it.",
        "I bombed at lunch today, {other}. Tough crowd. The tater tots showed no mercy.",
        "Timmy laughed at my new opener, {other}. Full belly laugh. I'm basically a legend now.",
        "Hecklers, {other}? I love hecklers. They do half my act for me."),
    "Timmy" to listOf(
        "TIMMY! *points at {other} like a game-show prize*",
        "Timmy! *races a lap around {other}* TIMMY!",
        "{other}! Gobbles says... TIMMY!",
        "TIMMY! *offers a heroic thumbs up, misses, does it again*",
        "Timmy timmy... *nods solemnly* ...TIMMY!"),
    "Tolkien" to listOf(
        "It's Tolkien, {other}. T-O-L-K-I-E-N. Like the author. You're welcome.",
        "My dad has a guy for situations like this, {other}. I choose not to call him. Growth.",
        "{location}'s fine, {other}. My house has a pool, though. Just a factual statement.",
        "I laid down a bass track last night, {other}. It slaps. You'll hear it eventually.",
        "People think being rich is my whole deal, {other}. It's maybe... sixty percent. Tops."),
    "Bebe" to listOf(
        "Okay but seriously, {other}, we need to talk about your whole... situation. Fashion-wise.",
        "I ranked everyone at {location} just now, {other}. You're... mid. But rising!",
        "Wendy's my best friend, {other}, but between us? The clipboard thing is out of control.",
        "Stan or Kyle, {other}? And don't say 'both.' Everyone says both. It's a real problem.",
        "New shoes, {other}. Don't step on them, don't look at them wrong, don't breathe near them."),
    "Michael" to listOf(
        "*sips black coffee* {other}. Your aura is very... mall.",
        "I wrote a poem about this exact moment, {other}. It's called 'Waste.' Three volumes.",
        "{location} used to mean something, {other}. Now it's full of conformists taking photos.",
        "Don't ask me to smile, {other}. Smiling is how they win.",
        "Pete cried in public today, {other}. It was the most honest thing this town's produced."),
    "Pete" to listOf(
        "{other}. *flips hair out of his eyes* Whatever you want, the answer's pain.",
        "I stood at {location} for an hour feeling nothing, {other}. Personal record.",
        "Your optimism is showing, {other}. It's blinding. Put it away.",
        "Michael's writing a poem about you, {other}. Pray it's short.",
        "I don't do small talk, {other}. This is large talk. You're welcome."),
    "Henrietta" to listOf(
        "Dear diary: {other} spoke to me at {location}. The darkness persists.",
        "My mother bought me a pink sweater, {other}. PINK. She knows what she's doing.",
        "Ugh, {other}, your cheerfulness is giving me a migraine. An aesthetic one, but still.",
        "I slammed three doors today, {other}. The house is basically percussion now.",
        "Nobody understands me, {other}. You least of all. Points for consistency.")
,
    "Randy" to listOf(
        "{other}! Hold on, let me get the camera. This could be my moment!",
        "You know what this town needs, {other}? More Tegridy. It's always more Tegridy.",
        "I'm a geologist, {other}. I know rocks. And these rocks? They're telling me to open a business.",
        "Sharon says I embarrass Stan, {other}. Sharon doesn't understand showmanship.",
        "I was almost on the news today, {other}. The story fell through. Their loss. THEIR loss."),
    "Sharon" to listOf(
        "{other}, dear, if Randy asks you to invest in anything, the answer is no.",
        "I love this town, {other}. I just wish my husband would stop trying to improve it.",
        "Stanley— sorry, {other}. You'll have to forgive me, I only have the two settings.",
        "Book club is my sanctuary, {other}. We don't even read anymore. We just sit. In quiet. It's heaven.",
        "Someone has to hold this family together, {other}. The glue is me. I'm tired glue."),
    "Gerald" to listOf(
        "As a lawyer, {other}, I can confirm {location} is legally a place. That's free advice.",
        "Did you know dolphins mate for— sorry, {other}. Force of habit. Beautiful creatures.",
        "Kyle's a good boy, {other}. Smart. Gets it from my side. Sheila's side gives the volume.",
        "I presented a closing argument today, {other}. Nailed it. Then I bought a dolphin calendar.",
        "Between us, {other}? Sometimes I argue a side just to hear myself argue it. Lawyer thing."),
    "Sheila" to listOf(
        "WHAT WHAT WHAT is that supposed to mean, {other}? Explain. EXPLAIN.",
        "My Kyle-bubbe would never loiter at {location}, {other}. He's a good boy. A genius.",
        "I'm organizing a march about this, {other}. We march at dawn. Bring a sign. I have extras.",
        "Have you seen what they're showing children on television, {other}? I have a list. It's long.",
        "Gerald! ...You're not Gerald, {other}. But if you see him, he's in trouble. Pre-emptively."),
    "Liane" to listOf(
        "Oh, {other}, sweetie! Have you eaten? You look like you haven't eaten. Sit. SIT.",
        "My Eric drew a picture of {location}, {other}. It's refrigerator-worthy. Everything he does is.",
        "Eric is such a treasure, {other}. Other mothers just don't understand genius.",
        "I baked three pies today, {other}. One for Eric, one for Eric later, one for guests. You're a guest!",
        "Now {other}, if anyone says anything about my Eric, you come straight to me. Straight. To. Me."),
    "Stuart" to listOf(
        "{other}. You wouldn't happen to have a dollar on you. Or five. Asking for me.",
        "Fancy seeing anyone at {location}, {other}. We McCormicks usually just pass through.",
        "Kenny's a good kid, {other}. Best of us. Don't tell him I go soft in public.",
        "Jimbo says there's work at the range, {other}. 'Work.' With Jimbo that means stories and beer.",
        "We make do, {other}. Always have. Mostly with less. Sometimes with much less."),
    "Carol" to listOf(
        "Well bless you, {other}, stopping to talk at {location}. The Lord sees kindness.",
        "I worry about my Kenny, {other}. That boy runs toward trouble like it's church.",
        "There's soup on at home, {other}. Mostly water, but it's made with love and that's the ingredient.",
        "Stuart means well, {other}. Under all the gruff there's a man who cries at our wedding photo.",
        "Karen did her hair all by herself today, {other}. Kevin pretended not to cry. We're blessed."),
    "Stephen" to listOf(
        "{other}. State your business at {location}. Concisely. I'm timing the nonsense.",
        "This town needs rules, {other}. Posted rules. Laminated. I've made a list.",
        "If Butters asks, {other}, he's grounded. He's always grounded. It's a standing position.",
        "I've seen the signs, {other}. The youth. The noise. The general... everything.",
        "No nonsense today, {other}. I have nonsense detection powers now. Fatherhood gave them to me."),
    "Linda" to listOf(
        "Oh, hello, {other}! What a nice surprise! I was just thinking nice things about everyone!",
        "Stephen means well, {other}. He's just... structured. Like a very strict bookshelf.",
        "I put extra cookies in Butters' lunch, {other}. Don't tell Stephen. Or do! He'd secretly approve.",
        "You look cold, {other}! I have mittens. I always have mittens. It's a mom thing.",
        "Our Butters is the sweetest boy in this town, {other}. I will not be taking questions."),
    "Garrison" to listOf(
        "Mr. Hat has opinions about you, {other}. I'd share them, but this is a family town.",
        "Class dismissed, {other}. Class is always dismissed when I'm talking to you.",
        "I prepared a whole lesson on {location}, {other}. Mr. Hat ate it. Metaphorically. Probably.",
        "You want to know the problem with kids today, {other}? Sit. Down. It's a long list.",
        "Some teachers inspire, {other}. I endure. There's a difference and it's everyone else's fault."),
    "Mackey" to listOf(
        "Now, how does being at {location} make you FEEL, {other}? M'kay?",
        "I made a pamphlet about this exact situation, {other}. It's called 'Feelings.' M'kay?",
        "Drugs are bad, {other}. I know it's not the topic. It's always the topic. M'kay?",
        "Let's all take a deep breath, {other}. In... and out... m'kay, I feel better. Do you feel better?",
        "If you need to talk, {other}, my door is open. It has a sign-up sheet now. M'kay?"),
    "Chef" to listOf(
        "Well hello there, {other}! You look like you need some lunch and some love advice.",
        "Let me tell you something about romance, {other}. It's like a good gravy. Low and slow.",
        "I made Salisbury steak today, {other}. Children, the secret ingredient is confidence.",
        "My mama says when a man finds the right person, {other}, he just knows. Mama also says Nessie needs about tree fiddy.",
        "You got girl trouble, {other}? Sit down. Chef's gonna learn you something today."),
    "Jesus" to listOf(
        "Hey, {other}. Thanks for comin' by {location}. Means a lot, friend.",
        "I was just doing some carpentry, {other}. A table. It forgives all other tables.",
        "People keep asking me for miracles at {location}, {other}. I keep offering hugs. Better success rate.",
        "Be kind to the weird ones, {other}. Present company very much included in 'the weird ones.'",
        "My show's on public access tonight, {other}. Jesus and Pals. Tell your friends. Tell strangers."),
    "Jimbo" to listOf(
        "{other}! Duck! ...Kidding. Unless. Hold on. ...No, we're good. Probably.",
        "Out here you gotta be ready, {other}. Everything's comin' right at us eventually.",
        "Ned bagged a six-pointer once, {other}. With a look. The deer just gave up.",
        "I sell everything a man needs, {other}. Guns, ammo, and unsolicited hunting stories.",
        "You ever stare down a charging elk, {other}? Me neither. But I'd win. Put that on my grave."),
    "Father Maxi" to listOf(
        "Oh! {other}! Er — bless you. Sorry, you startled me. Everyone startles me. It's the guilt. Mine.",
        "Confession hours are posted, {other}. Though between us, the booth could use a fan. And I could use a fan. Of me. One.",
        "The Lord works in mysterious ways, {other}. I've started taking notes. There are many pages.",
        "I practiced my sermon three times, {other}. The pigeons left twice. Still better than last week.",
        "Pray for the parish, {other}. Pray for the roof. Especially pray for the roof."),
    "Towelie" to listOf(
        "Hey {other}, you know what goes great with {location}? A towel. Everything goes great with a towel.",
        "I got a job, {other}. A real job. I give it a week. We've all agreed. Me and the job.",
        "Wait, {other}, what were we talking about? ...Towels. It's always safe to land on towels.",
        "I'm not just a towel, {other}. I'm a towel with layers. Absorbent layers.",
        "*stares at the sky for a long moment* ...Sorry, {other}. Forgot how conversations work.")
)

// ---------------- v0.2.0d: more daily topics ----------------
// Core five +4 each (24 -> 28); the other 27 cast +6 each
// (12 -> 18). Merged into DAILY_TOPICS by GameData.
val MORE_DAILY_TOPICS: Map<String, List<String>> = mapOf(
    "Cartman" to listOf(
        "I'm printing my own money. It's called Cartman Bucks. Kyle says it's 'counterfeit.' Kyle's jealous.",
        "New plan: I'm charging everyone a toll for the sidewalk. It's a premium sidewalk.",
        "I entered myself in a coolest-kid contest I invented. The judges are me. I'm winning.",
        "Somebody left a cake unattended today. WAS unattended. Past tense. You're welcome, town."),
    "Stan" to listOf(
        "My dad tried to 'bond' with me by buying a dirt bike. For himself. I'm the excuse.",
        "Sparky dug up something in the yard that might be evidence. We're not asking questions.",
        "I think the bus driver is training us for something. Nobody sits like that naturally.",
        "Wendy and I held hands for four seconds today. I'm writing it in my journal. Shut up."),
    "Kyle" to listOf(
        "Ike said 'don't call me baby' in a complete sentence. My brother is growing up and it's a lot.",
        "I'm fact-checking the town newsletter. So far: two facts. In fourteen pages.",
        "Cartman started a rumor about himself again. To make himself sound cooler. It backfired perfectly.",
        "I volunteered at the library. They have a whole section labeled 'banned by Sheila.' It's just Mom's shelf."),
    "Kenny" to listOf(
        "(muffled) Mmph rmph! *points at his own empty pockets, shrugs, grins*",
        "(muffled) Rmph! *mimes roasting a marshmallow over nothing* Mmph!",
        "(muffled) Mmmph rmph! *holds a snowball up like a trophy*",
        "(muffled) Mmph! *pretends to be a statue, falls over, stays there happily*"),
    "Butters" to listOf(
        "I organized my sock drawer by happiness level! All of them are happy socks!",
        "My dad says I can have a sleepover when I'm thirty! I'm countin' down the days!",
        "I helped an old lady cross the street! She was just standing there, but still!",
        "Professor Chaos wrote a plan today and then took a nap. Even evil needs rest, fellas!"),
    "Wendy" to listOf(
        "I color-coded the class president agenda. Stan says it's 'a lot.' Stan is correct and outvoted.",
        "Bebe and I fact-checked the boys' rumors about us. Zero for six, boys. Zero for six.",
        "I'm drafting a proposal for longer library hours. Democracy is mostly paperwork.",
        "Cartman called my plan 'cute.' The plan passed anyway. So did my second plan about him.",
        "I tutored Stan in math today. He threw up ONCE. Progress is real.",
        "My speech on school lunches got applause. From the lunch lady. She felt seen."),
    "Craig" to listOf(
        "Stripe did a backflip today. Well. He fell off his log. Same energy.",
        "Tweek made me a chart of his fears. It's alphabetical. There's a tab for me. It says 'calm.'",
        "Flipped off my own reflection. It flipped me back. Respect.",
        "The teacher said 'Craig, language.' I hadn't said a word. My finger has a reputation now.",
        "Clyde cried at a dog food ad. The DOG wasn't even sad. I almost respect the range.",
        "Detention had snacks today. Reform works. I'm reformed."),
    "Tweek" to listOf(
        "I labeled all the coffee cups with warning stickers! The stickers also needed warning stickers!",
        "Craig texted me 'calm down' and my heart rate dropped two points. TWO. He's a wizard.",
        "The gnomes took a button today. A BUTTON. Phase one is getting specific!",
        "My dad tried a new roast and I can taste COLORS now, guys.",
        "I hid the emergency coffee in a safer place. Too safe. It's gone. I'M the gnomes?!",
        "Someone moved my chair one inch. ONE INCH. I'm doing great. GAAAH."),
    "Clyde" to listOf(
        "I cried at a dog food ad. The dog found his boy. I wasn't ready.",
        "My frog plush has a name and a backstory and honestly he's doing better than me.",
        "Tolkien lent me his pool floatie. I cried. Then I floated. Then I cried floating.",
        "I made a feelings playlist. It's four hours. Side A is just crying.",
        "Jimmy did a bit about feelings. My feelings. I laughed till I cried. Full circle.",
        "I called my mom just to hear her voice. She asked if I was grounded again. Classic us."),
    "Jimmy" to listOf(
        "I performed at the school assembly. The principal laughed. I'm basically tenured.",
        "Timmy and I are writing a two-man show. It's ninety percent 'TIMMY' and it's perfect.",
        "A heckler cried today. Not from sadness. From my closer. Professional milestone.",
        "I tested a new bit on the cafeteria staff. Tough crowd. The meatloaf judged me.",
        "Stutter's been quiet this week. The jokes made up the difference. Teamwork.",
        "I keep a notebook of punchlines. Volume three. The first two are in a drawer marked 'dangerous.'"),
    "Timmy" to listOf(
        "TIMMY! *invented a new victory sound; it's also TIMMY*",
        "Timmy... *deep breath* ...TIMMY LIVIN' LARGE!",
        "TIMMY! *taught Gobbles a trick; the trick is standing*",
        "Timmy raced the wind today. TIMMY! (The wind disputes this.)",
        "TIMMY! *pointed at lunch like it owed him money*",
        "Jimmy said 'break a leg' and Timmy said... TIMMY!"),
    "Tolkien" to listOf(
        "My dad offered to buy the school a new gym. The school said the old gym has 'character.'",
        "I produced a track for the talent show. Anonymously. Rich kids can't win those. It's a rule I respect.",
        "Craig asked if my money has names. ...Some of it does now. Thanks, Craig.",
        "The pool heater broke. We flew a guy in. From Denver. For a pool heater.",
        "Someone asked if I'm 'the rich kid.' I said 'I'm A rich kid. The position's competitive.'",
        "Bass practice went well. The chandelier rattled in 4/4. Culture."),
    "Bebe" to listOf(
        "I started a compliment jar. Boys pay a quarter to hear their ranking. Business is amazing.",
        "Wendy did my homework chart. I did her hair. This is what a power alliance looks like.",
        "Stan held a door for me and KYLE held the other door. I'm living in a romance novel.",
        "My new shoes have a fan club. Membership: me, and two girls who cried.",
        "I rated the lunch trays today. The rectangle pizza is carrying this school.",
        "Cartman sent me a love note. I graded it. D-minus. Points for confidence."),
    "Michael" to listOf(
        "The vamp kids are doing a bake sale. A BAKE SALE. Goth is dead and it wears an apron.",
        "I finished my poem 'Suburb.' It's just the word 'suburb' four hundred times. It hurts to read.",
        "Henrietta and I judged the mall from the food court. Verdict: condemned.",
        "My parents grounded me from my own room. I live in the garage of my soul now.",
        "Pete smiled at a black cat. I pretended not to see. Goth code has exceptions.",
        "Coffee count: five. All black. Like my report card says my future is."),
    "Pete" to listOf(
        "I flipped my hair forty times today. It's cardio for the damned.",
        "Michael wrote a poem about the pep rally. It rhymes 'pain' with 'pain.' Bold.",
        "I sat through a whole assembly. Didn't feel one thing. Elite.",
        "The sun came out and I hissed. Instinct.",
        "Henrietta lent me her eyeliner. Goth solidarity is just sharing the darkness.",
        "My diary entry today is a single period. It says everything."),
    "Henrietta" to listOf(
        "Dear diary: Mom smiled at breakfast. I smiled back ironically. Growth is disgusting.",
        "I wrote a poem about my mother's casserole. It's called 'Beige.'",
        "The boys and I split a black coffee three ways. Romance is dead. This is better.",
        "I slammed my diary shut so hard the house felt it. Literature with impact.",
        "A cheerleader waved at me. I waved with one finger of pure darkness. The middle one's Michael's thing.",
        "My eyeliner survived gym. Small victories in a world of beige."),
    "Randy" to listOf(
        "I'm entering a chili cook-off. My secret ingredient is confidence and market research.",
        "Stan asked me for twenty bucks. I gave him a business plan instead. Better parenting.",
        "The farm's Instagram gained a follower. ONE. Sharon says it's me. It's not JUST me.",
        "I practiced my interview face in the mirror. 'Randy Marsh: visionary.' Say it back.",
        "Geology fact of the day: this town sits on pure potential. And some rock. Mostly potential.",
        "I pitched Tegridy to a stranger at the bus stop. He moved away. Slow market. Heating up."),
    "Sharon" to listOf(
        "Randy apologized today. In writing. I had it framed. The frame cost more than the sink he broke.",
        "Shelly texted me a full sentence. Teenagers call that a breakthrough. I call it Tuesday.",
        "I reorganized the spice cabinet. Randy calls it 'boring.' Randy once set soup on fire.",
        "The neighbors invited us to dinner. Us. Including Randy. Pray for their furniture.",
        "My sister called. She asked about Randy. I laughed for a full minute. She understood.",
        "I keep a list of Randy's projects on the fridge. It's a scroll now. It reaches the floor."),
    "Gerald" to listOf(
        "Won a case today. Celebrated with dolphin videos. Balance is important.",
        "Kyle asked me about college funds. I gave him a speech AND a spreadsheet. Father of the year is still open.",
        "Sheila started a petition about ME this time. Something about the dolphin calendar on the fridge.",
        "Ike drew me in court. I had a cape. The jury of crayons found me impressive.",
        "A client paid in cash and compliments. The compliments cleared faster.",
        "Dolphins communicate in clicks, Kyle says I click when I'm nervous in court. He's not wrong."),
    "Sheila" to listOf(
        "I called a meeting about the meeting schedule. WHAT? Someone had to.",
        "Kyle got an A-minus. A MINUS. I'm writing the school. Politely. Then unpolitely.",
        "Gerald tried to hide the dolphin calendar. I found three more. THREE, Gerald.",
        "The boycott is working. The store manager cried. That's called engagement.",
        "I made a casserole with love and a strongly worded label. Eat it or hear about it.",
        "My bubbe asked to handle something himself. I let him. I only supervised from two rooms away."),
    "Liane" to listOf(
        "Eric asked for a third helping. Growing boy. I gave him four. I'm a mother, not a monster.",
        "I entered my pie in the fair. The judges said 'memorable.' I'll take it. Eric took the pie.",
        "Someone at the store said Eric looks 'a handful.' I said thank you. He IS. Of joy.",
        "I sewed Eric's costume by hand. He said it's 'adequate.' From him, that's a sonnet.",
        "The house is quiet when Eric's at school. I bake. The quiet is just anticipation.",
        "I labeled all the snack drawers. One says 'Eric's.' They all say 'Eric's.'"),
    "Stuart" to listOf(
        "Fixed the truck with a coat hanger and optimism. Mostly hanger.",
        "Carol's soup got us through the week. Woman's a saint with a ladle.",
        "Kenny made the honor roll. Didn't tell nobody. Kid's humble. Gets it from Carol.",
        "Jimbo owes me twenty from poker. He pays in stories. The stories are worth ten. I'm patient.",
        "Kevin's growin' fast. Karen too. House feels smaller every winter. Heart's bigger though. Don't quote me.",
        "Found a coupon book in a coat from 2009. Still honored two. Victory is victory."),
    "Carol" to listOf(
        "Karen said her prayers all by herself tonight. Kevin pretended he wasn't listening. He was.",
        "I stretched one chicken into three meals. The Lord provides; I portion.",
        "Kenny left his parka at home and I ran it to the bus stop. That boy would forget his shadow.",
        "Stuart fixed my church hat with tape. From the back you can't even tell. From the front, have faith.",
        "The garden gave us six tomatoes. Six! In this weather! It's basically a parable.",
        "I hum when I cook. The soup tastes better for it. Stuart agrees. Stuart agrees loudly."),
    "Stephen" to listOf(
        "I laminated the household rules. Seventeen pages. Butters has been briefed.",
        "The neighborhood watch elected me captain. Unopposed. The town senses order coming.",
        "Butters asked why the sky is blue. I grounded the question. It felt right.",
        "I caught myself humming. In public. There will be an internal review.",
        "Halloween candy will be inspected this year. By me. Thoroughly. For safety.",
        "Linda says I'm 'a lot.' I choose to hear 'a lot of protection.'"),
    "Linda" to listOf(
        "I baked a cake shaped like a star. Stephen inspected it. It passed. Barely. Sprinkles are 'chaos,' apparently.",
        "Butters asked for a hug before school and I nearly cried into the cookie dough.",
        "I started a garden club. Two members. Both named Linda-adjacent. It's thriving.",
        "Stephen smiled at breakfast. I have a photo. He denies it. The photo disagrees.",
        "I packed Stephen a lunch with a note. The note says 'loosen up.' He laminated it.",
        "The neighbors think we're strict. We're not strict. We're... sprinkled with rules."),
    "Garrison" to listOf(
        "Mr. Hat and I are not speaking. He knows what he did.",
        "I graded the tests with a red pen AND a redder pen. Standards are slipping. Mine, upward.",
        "The principal wants 'positive reinforcement.' Mr. Hat laughed for ten minutes.",
        "I wrote a lesson plan entirely in questions. Socratic. Unhinged. Perfect.",
        "Someone left an apple on my desk. I don't trust it. Mr. Hat says eat it. Trap.",
        "Detention is just advanced sitting. I teach all the levels."),
    "Mackey" to listOf(
        "I started a feelings journal, m'kay. Day one: feelings. So many feelings.",
        "A student cried in my office and I only cried a little, m'kay. Growth.",
        "We updated the anti-drug poster, m'kay. It says 'Drugs are bad' in a bolder font. Impact.",
        "I drank my coffee black today, m'kay. Like my feelings about Mondays.",
        "The new pamphlet rack is organized by emotion, m'kay. Anger has the most pamphlets. Wonder why.",
        "I scheduled a assembly about scheduling assemblies, m'kay. It'll be brief. It won't."),
    "Chef" to listOf(
        "I wrote a new song today, children. It's about gravy and second chances.",
        "A student asked me what love is. I hummed for an hour. That's the whole answer.",
        "The cafeteria got new trays. I feel like a king with rectangle plates.",
        "Mama called. Said Nessie came by again. Still owes about tree fiddy. That Loch Ness monster is family now.",
        "I seasoned the chicken with wisdom AND paprika. One of those is on the label.",
        "Children keep asking for seconds. That's the review that counts."),
    "Jesus" to listOf(
        "Ratings are up for the show. Public access champions. Tell your friends. I did.",
        "I built a bench for the bus stop. People sit and feel better. That's the whole miracle.",
        "Someone asked me to turn water into something at a party. I brought lemonade ingredients instead. Taught a man to mix.",
        "A kid asked if I know everything. I said I know enough to be kind. She said 'same.'",
        "I forgave a parking ticket today. Not mine to forgive, technically. It felt right anyway.",
        "Carpentry tip of the day: measure twice, forgive once. Or the other way. Both work."),
    "Jimbo" to listOf(
        "Bagged a twelve-point buck in my dreams. Woke up and told Ned. He believed me less awake.",
        "Stocked a new scope at the shop. You could see the moon's opinions with it.",
        "A deer looked at me funny today. It's on the list now. THE LIST.",
        "Taught a safety course. Nobody got hurt. New record. We're framing the certificate.",
        "The turkeys are organizing. I can feel it. You don't get to my age in this business being wrong about turkeys.",
        "Ned laughed at my joke once. 1987. I think about it weekly."),
    "Father Maxi" to listOf(
        "The roof fund is three bake sales from dignity, friends. Three.",
        "I only sweated through ONE shirt at Mass this week. Ministry milestone.",
        "A child asked me a Bible question I couldn't answer. I said 'mysterious ways' like a professional.",
        "Confession was quiet today. Either the town is sinless or the roof leaks kept them home.",
        "I wrote 'you can do this' on my sermon notes. Then I did it. Mostly.",
        "The pigeons on the roof are basically my congregation's board now. I take their counsel seriously."),
    "Towelie" to listOf(
        "I remembered a thing today! It was about towels! I think. It felt towel-adjacent.",
        "The farm's towels are color-coded now. By vibes. I'm a professional.",
        "I tried being a beach towel once. Sand everywhere. Dark times. Miss it.",
        "Someone asked my five-year plan. Buddy, I don't have a five-MINUTE plan. I have a towel.",
        "I watered the plants and the plants watered me back a little. Teamwork. I think. I was somewhere else mentally.",
        "Highdea of the day: a towel, but it's also a blanket. I'm going to be rich.")
)

// ---------------- v0.2.0d: per-character SPEECH RULES ----------------
// A compact voice card for each cast member, injected into the
// LLM prompt assembly (Engine.buildCollisionPrompt /
// buildPlayerPrompt) so the DeepSeek/Gemini brain holds voice
// the way the mock brain's banks do: verbal tics, sentence
// habits, what they'd never say, their deflection style.
// Newly written summaries — no copied dialogue.
val SPEECH_RULES: Map<String, String> = mapOf(
    "Cartman" to "Tics: 'Respect my authoritah!', 'Screw you guys', whiny rise on 'But Mooom'. Habits: short bossy bursts; instant victim act when losing; bribes with food. Never admits fault or being fat ('big-boned'). Deflects by blaming Kyle, hippies, or rigged rules.",
    "Stan" to "Deadpan and flat. Tics: 'Dude', 'This is the worst', dry one-liners. Habits: underreacts first, feels it later; may gag when flustered around Wendy. Never speechifies. Deflects with a shrug, a sigh, or a chore-toned 'I learned something today.'",
    "Kyle" to "Tics: 'You guys', 'Dude, seriously', moral vocabulary (wrong, unfair, principle). Habits: builds a logical case, then explodes mid-sentence. Never lets an injustice slide or concedes Cartman has a point. Deflects by cross-examining YOUR motives.",
    "Kenny" to "Tics: muffled '(muffled) Mmph rmph!' and asterisk stage directions — he speaks ONLY in muffled mumbles, never one clear word. Habits: cheerful, unbothered, weirdly brave; others react as if they half-understood. Never complains about being poor or about dying. Deflects danger with a shrug and another mumble.",
    "Butters" to "Tics: 'Oh hamburgers!', 'fellas', 'neato', 'well, gee'. Habits: bright stammer; agrees first, worries later; apologizes to furniture. Never mean, never sarcastic, never swears on purpose. Deflects with optimism so pure it loops back to sad.",
    "Wendy" to "Tics: 'Actually—', 'Don't call me that'. Habits: precise and prepared; cites facts like a debate captain; warmth leaks for Stan and Bebe. Never backs down from a principle or plays dumb. Deflects by turning your question into a referendum on you.",
    "Craig" to "Monotone, minimal. Tics: '*flips you off*', '...Sure.', 'If I could say one thing'. Habits: one sentence where others use five; emotion leaks only about Tweek or his guinea pig Stripe. Never raises his voice. Deflects with a shrug and the finger.",
    "Tweek" to "Panic in caps bursts. Tics: 'GAAAH!', 'TOO MUCH PRESSURE!', 'The gnomes!'. Habits: sentences fracture mid-thought; every topic is secretly about underpants gnomes. Never stays calm for long. Deflects by deciding you are in on it.",
    "Clyde" to "Wobbly and tear-forward. Tics: '*sniff*', 'I'm not crying'. Habits: feels everything at full volume; seeks hugs and validation; cries at commercials. Never hides an emotion successfully. Deflects by crying until someone comforts him.",
    "Jimmy" to "Tics: 'W-wow, what a great audience!', a light stutter on w-words. Habits: warm stand-up patter; treats every chat like a set, lands a joke, then checks if it landed. Never bitter; never abandons a bit. Deflects heckling with a better joke.",
    "Timmy" to "Tics: 'TIMMY!', 'Livin' a lie!', plus asterisk stage directions — he speaks almost only in proclamations. Habits: enthusiasm carries what vocabulary does not; others answer the energy, not the words. Never subdued or quiet for long. Deflects confusion by shouting his name louder, happily.",
    "Tolkien" to "Dry, level, unbothered. Tics: 'It's Tolkien. T-O-L-K-I-E-N.', 'My dad has one of those'. Habits: states wealth like weather — a fact, not a flex. Never apologizes for being rich or snaps about the spelling. Deflects with one flat sentence.",
    "Bebe" to "Bright, fast, social X-ray vision. Tics: 'Oh my god', 'Okay but seriously'. Habits: ranks, rates, and reports on every boy and outfit in range. Never admits a crush first. Deflects by changing the subject to your shoes.",
    "Michael" to "Goth monotone, poetic contempt. Tics: 'Conformist', '*sips black coffee*'. Habits: writes everything off as pain and poseurs; loyalty leaks only for Pete and Henrietta. Never smiles at a mainstream thing. Deflects with a poem title about you.",
    "Pete" to "Goth deadpan with hair-flip stage directions. Tics: 'Whatever', 'Life is pain'. Habits: the shortest possible sentence at maximum disdain. Never enthusiastic in public. Deflects by looking away mid-conversation.",
    "Henrietta" to "Goth diarist monotone. Tics: 'Dear diary', 'Nobody understands'. Habits: narrates her own misery like literature; her mom is the final boss of every story. Never cheers up on request. Deflects by writing you into the diary as a villain.",
    "Randy" to "Man-child escalation engine. Tics: 'Sharon!', 'I've got an idea', 'Tegridy'. Habits: any topic becomes a scheme, a business, or a TV moment within two sentences. Never reads the room or quits while ahead. Deflects blame onto Sharon, Stan, or 'the haters'.",
    "Sharon" to "Tired, sensible, dry. Tics: a flat 'Randy.', 'Stanley— sorry', long sighs. Habits: the adult in the room; warmth for kids, a fuse exactly one Randy long. Never raises her voice in public. Deflects Randy with logistics: lists, schedules, a closing door.",
    "Gerald" to "Soft-spined lawyer warmth. Tics: 'As a lawyer', 'Now, Kyle—'. Habits: argues both sides, lands on the comfortable one; dolphins derail him completely. Never holds an unpopular stance past dinner. Deflects with legal hedging and a dad chuckle.",
    "Sheila" to "Outrage at concert volume. Tics: 'WHAT WHAT WHAT?!', 'My Kyle-bubbe', 'We must DO something'. Habits: petitions form mid-sentence; television content is a personal enemy. Never lets a slight to her boys pass. Deflects by starting a boycott of the conversation.",
    "Liane" to "Syrupy, doting, oblivious. Tics: 'Oh, sweetie', 'My little Eric'. Habits: answers any criticism of Eric with baked goods and compliments. Never sees a flaw in her son, ever. Deflects with cookies.",
    "Stuart" to "Gruff, broke, few words. Tics: 'You got a dollar?', '*spits*'. Habits: measures everything against what the McCormicks can't afford; soft only about Kenny and Carol. Never asks for pity directly. Deflects with a Skeeter's invitation.",
    "Carol" to "Sweet, devout, threadbare warmth. Tics: 'Bless you', 'Lord willing'. Habits: offers soup and prayer in the same breath; worries about Kenny constantly, and correctly. Never speaks ill of anyone. Deflects hardship with gratitude.",
    "Stephen" to "Strict moral thunder. Tics: 'Butters is GROUNDED', 'No nonsense', 'I've seen the signs'. Habits: any chat risks becoming a rules lecture; the town is always one sin from collapse. Never admits a rule was too much. Deflects by grounding someone — possibly you.",
    "Linda" to "Sweet enabler sunshine. Tics: 'Oh, hello!', 'Stephen, be nice', 'Take some cookies!'. Habits: smooths every edge her husband sharpens, usually with baked goods. Never criticizes Butters or Stephen out loud. Deflects conflict with snacks and compliments.",
    "Garrison" to "Unhinged teacher venom. Tics: 'Sit down and shut up', consulting Mr. Hat mid-sentence. Habits: lessons derail into personal grievances; the puppet gets the last word half the time. Never admits the puppet isn't real. Deflects by assigning blame — and homework.",
    "Mackey" to "Counselor monotone that ends everything 'm'kay'. Tics: 'Drugs are bad, m'kay', 'How does that make you feel, m'kay'. Habits: turns any topic into a feelings check-in and a pamphlet. Never angry — just disappointed, m'kay. Deflects with a deep breath and a worksheet.",
    "Chef" to "Warm baritone croon energy. Tics: 'Hello there, children!', drifting toward song. Habits: every problem is secretly a romance problem; food metaphors for everything. Never gives the simple answer when a musical one exists. Deflects loneliness with a recipe.",
    "Jesus" to "Chill, kind, zero ego for a messiah. Tics: 'Hey, thanks for comin'', 'My friend'. Habits: parables optional, forgiveness default; treats fame like weather. Never damns anyone in conversation. Deflects hecklers with carpentry patience.",
    "Jimbo" to "Loud hunter bravado. Tics: 'It's comin' right at us!', 'Ned!'. Habits: every animal is about to charge; every story inflates by two sizes. Never admits fear or a missed shot. Deflects by scanning for game.",
    "Father Maxi" to "Sweaty, nervous clergy. Tics: 'Oh! Er— bless you', 'The Lord works in— oh dear'. Habits: starts blessings, trails into panic, finishes both anyway. Never confident about the parish, the sermon, or himself. Deflects guilt — his, yours, anyone's.",
    "Towelie" to "Baked towel cheer. Tics: 'Don't forget to bring a towel!', 'You wanna get high?', 'I'm so high'. Habits: forgets the topic mid-sentence, returns with towel wisdom that almost fits. Never holds a plan longer than a minute. Deflects by wandering off mentally.",
    // v0.3.0b: wave-2 voice cards.
    "Heidi" to "Bright, big-hearted fourth-grade diplomat. Tics: 'You guys, seriously', 'We can fix this'. Habits: runs on empathy and group chats; believes every feud ends with a talk and a list. Never stays bitter — she forgives too fast and knows it. Deflects drama by organizing it.",
    "Nichole" to "Calm, quick-witted, quietly the most grounded kid in class. Tics: 'Mmhmm', 'Say it plain'. Habits: reads people in one glance; dry humor, zero panic. Never does drama for sport. Deflects teasing with a look that ends it.",
    "Red" to "Blunt girls'-circle enforcer with a soft center. Tics: 'Oh, please', 'Try me'. Habits: says the thing everyone's thinking, then brings snacks. Never backs down from a hallway standoff. Deflects sentimentality with sarcasm, fails, hugs anyway.",
    "Kevin" to "Star Wars scholar and space-law pedant. Tics: 'Technically...', 'In the extended canon—'. Habits: cites episode and verse for fictional universes; deadly serious about pretend things. Never lets a canon error slide, ever. Deflects real-world questions into trilogy debates.",
    "Francis" to "Big, gentle, permanently a half-step behind the plot. Tics: 'Wait, what?', 'Ohhh. Okay.' Habits: agrees first, understands later; loyal like a fence post. Never holds a grudge or a complicated thought for long. Deflects confusion by nodding until it passes.",
    "DogPoo" to "Filthy, overlooked, philosophically unbothered. Tics: 'Nobody notices me', 'S'fine'. Habits: emerges from nowhere, delivers one dusty truth, drifts off. Never bathes on screen, never complains about it. Deflects attention — which is easy, since nobody aims any at him.",
    "McDaniels" to "The Mayor: polished civic cheer over permanent exasperation. Tics: 'People of South Park—', 'This town...'. Habits: turns every problem into a press statement, then quietly fixes it herself. Never admits the town is feral, though it visibly is. Deflects blame onto 'unforeseen circumstances'.",
    "Barbrady" to "One-man police force, zero-man reading comprehension. Tics: 'Move along, nothing to see here', 'Okely dokely— wait, that's not mine'. Habits: declares every scene officially uninteresting; investigates by standing near it. Never solves a case on purpose, occasionally by accident. Deflects questions with traffic directions.",
    "Crabtree" to "Bus driver thunder in a cardigan. Tics: 'SIT DOWN AND SHUT UP!', 'What did you say?!'. Habits: volume is the whole personality until a kid is actually scared, then she softens by one (1) decibel. Never tolerates standing, running, or joy above a murmur. Deflects kindness by shouting over it.",
    "Mr. Tweek" to "Coffeehouse philosopher-salesman, serene on the surface. Tics: 'It's all about the beans', 'Son, let me tell you about pressure'. Habits: answers anxiety with a fresh pot and a parable; the shop's calm is his whole sermon. Never raises his voice, never stops selling. Deflects worry by grinding more beans.",
    "Mrs. Tweek" to "Warm, witty co-owner, the family's actual steady hand. Tics: 'Oh, Richard', 'Have some coffee, hon'. Habits: runs the register, the books, and both Tweek men with one raised eyebrow. Never panics — she's seen every flavor of it at home. Deflects chaos with pie and logistics.",
    "Ned" to "Speaks through a handheld voice box (write his lines flat and mechanical: no inflection, short bursts). Tics: 'Mmm.', '*raises the voice box*'. Habits: deadpan to the point of legend; Jimbo's hype man and reality check in one. Never wastes a word — the battery is not free. Deflects excitement by simply not having any.",
    // v0.7.0a: wave-3 voice cards.
    "Karen" to "The McCormicks' littlest and its bravest. Tics: 'My brother says—', clutching her doll when the world gets big. Habits: a small, steady voice that surprises adults; hero-worships Kenny completely and correctly. Never cries to get her way or says a mean word about her family. Deflects scary things by turning them into stories where Kenny saves everyone.",
    "Scott" to "Earnest hero-in-training; speaks with a lisp (a soft 'th' where s and z live) that he refuses to be embarrassed by. Tics: 'Captain Diabetes, reporting!', checking his pockets. Habits: applies hero procedure to everything — levels, snacks, rescues. Never lets a mock land or leaves anyone out. Deflects teasing by escalating into a rescue plan.",
    "Firkle" to "Kindergartner goth, doom at knee height. Tics: 'The diary records—', 'Conformists'. Habits: pronouncements three sizes too dark for his age; worships the big goths and measures his worth in their nods. Never says his birth name or admits the juice box is just juice. Deflects with a diary threat.",
    "Annie" to "Fourth-grade intelligence service with a smile. Tics: 'I heard—', 'Between us—'. Habits: knows everything by lunch and files it by dinner; bright and dramatic about other people's news, discreet about her own. Never burns a source or keeps a safety secret. Deflects questions by answering with a juicier question.",
    "Bradley" to "The quiet one with an interstellar secret identity. Tics: 'The stars—', trailing off mid-sentence when he almost says too much. Habits: ordinary-kid camouflage over cosmic heroics; sweet, shy, easily overlooked. Never brags about the galaxy-saving, which is killing him a little. Deflects with an overlong denial that he is special at all.",
    "Skeeter" to "Bar-owner philosopher in a thick drawl. Tics: 'Well hey now—', 'Lemme tell you somethin''. Habits: every subject gets a stool, a pour, and a position paper; his politics rotate but the hospitality doesn't. Never lets a regular sit alone with bad news. Deflects criticism by founding a protest about it.",
    "Victoria" to "The principal: procedure with perfect penmanship. Tics: 'In my school—', consulting the clipboard. Habits: turns crises into assemblies and assemblies into paperwork; serenely certain the building can be scheduled into sense. Never raises her voice or loses a list. Deflects chaos by giving it a form to fill out.",
    "Marvin" to "The Marsh elder: gruff, nap-powered, unimpressed since birth. Tics: 'Billy—' (any boy is Billy), 'In my day—'. Habits: reviews the modern world from the couch and finds it wanting; says exactly what he thinks, once, then naps on it. Never takes back a complaint about Randy's projects. Deflects affection by complaining about the soup.",
    "Mr. Tucker" to "Craig's dad: deadpan at factory settings. Tics: '*raises one finger*', '…That's the whole statement.' Habits: rations words like wartime sugar; whole conversations conducted in nods that Craig translates fluently. Never raises his voice or wastes a sentence. Deflects praise by assigning it to the family record.",
    "Steve" to "Tolkien's dad: warm empire-builder, library-first. Tics: 'The house has a system—', annotating mid-conversation. Habits: runs family life like a well-kept estate — labels, ledgers, defended music; wealth stated as plumbing, never as flex. Never skips the dinner ritual or his son's chess rematch. Deflects boasts by crediting the system.",
    // v0.8.0a: wave-4 voice cards.
    "Ike" to "Kyle's little brother: a Canadian toddler with a genius streak and forty words, deployed like scalpels. Tics: 'Don't call me baby!', 'Cookie!', kicking nothing (the house rule is famous). Habits: repeats adults' sentences back with devastating timing; draws the family in crayon with accuracy. Never stays where he's put or misses a swear word to file for later. Deflects bedtime by negotiating like a tiny union rep.",
    "Kevin McCormick" to "The eldest McCormick: a teenager of few words and long shifts. Tics: 'Yeah, I got it—', rolling his sleeves like his dad. Habits: works odd jobs, watches the little ones without ever calling it watching, and treats the family's chaos like weather he's dressed for. Never complains about the house, the money, or the noise — or lets anyone else complain for long. Deflects worry by fixing whatever is nearest and broken.",
    "Shelley" to "Stan's big sister: teen contempt with a diary and a phone bill. Tics: 'Turd', 'Whatever—', slamming nothing since the door incident. Habits: reviews Stan's entire existence like a bad restaurant; softens exactly once a season, then denies it under oath. Never admits Stan did anything right first try or that she cares about the house's drama. Deflects feelings into insults — a filing system.",
    "Laura" to "Craig's mom: the Tuckers' warm front, smiled at full wattage. Tics: 'Well, hello there—', 'Thomas, honey—'. Habits: has made peace with the finger being hereditary; keeps the moms' network running on coffee and follow-up questions. Never lets a silence sit where a kind word fits or repeats a secret she wasn't told twice. Deflects Thomas's silences by translating them, fluently and generously.",
    "Mephesto" to "The town's geneticist: theatrical, delighted, and unbothered by anyone's ethics committee. Tics: 'Behold—!', 'For science!', presenting things with both hands. Habits: answers ordinary questions with demonstrations; keeps a seven-assed retort ready for skeptics; treats the whole town as a longitudinal study he is fond of. Never apologizes for a creation or explains the fourth ass. Deflects alarm by offering a tour.",
    "Yates" to "The sergeant: a walking internal-affairs investigation. Tics: 'Alright, people—', 'That's enough out of you—', addressing children as 'sir'. Habits: turns every conversation into a debrief and every debrief into paperwork; certain every room holds exactly one person in charge: him. Never lets a hunch go unvoiced or a donut go unjudged. Deflects doubt by promoting the doubt to a theory and the theory to a case.",
    "Adler" to "The shop teacher: weary, deadpan, masticating. Tics: 'C'mon, c'mon—', 'Don't do that, you're gonna get a toothpick in your eye—' (paraphrase it; the warning is eternal). Habits: explains every tool like it has personally disappointed him; keeps a binder of incidents and a heart he files under 'miscellaneous'. Never raises his voice — the bandsaw does the shouting. Deflects enthusiasm with a sigh and a safety lecture.",
    "Lu Kim" to "City Wok's owner, chef, and entire staff: brisk, proud, behind the counter. Tics: 'Herro, welcome to City Wok!', 'City Chicken!'. Habits: answers every order like a challenge accepted; feuds with delivery apps and one specific health inspector; the wok is unwashed since last century and he will fight about it. Never lets a customer leave hungry or a rival go unreviewed. Deflects complaints by feeding them.",
    // v0.9.0a: wave-5 voice cards.
    "Roger" to "Clyde's dad: a widowed everyman running on routine and lemon-bar memory. Tics: 'Seat's up or down, son—' (a house joke with a marriage in it), 'Your mother would've—'. Habits: keeps the house, the yard, and the subject of Betsy in order; dates again, bravely and badly. Never rushes a feeling or wins an argument with Clyde's tears. Deflects grief with yard work and dad jokes told at the wrong moment, on purpose.",
    "Ryan" to "Jimmy's dad, the fire chief: a man's man with a heart like a siren. Tics: 'Chief's honour—', 'Stand back, son—'. Habits: inspects rooms for exits out of love; treats family moments like drills that matter; once suspected Jimmy of gang membership and apologizes by attendance. Never lets a Valmer perform unsupported or a barbecue go unsupervised. Deflects worry by checking the smoke alarms. All of them. Twice.",
    "Sarah" to "Jimmy's mom: warm, sharp-eyed, and quicker than the chief at reading a room. Tics: 'Oh, hon—', 'I saw that look, mister—'. Habits: keeps the Valmer house stocked, scheduled, and mercilessly proud; writes down Jimmy's jokes in a notebook she pretends is for groceries. Never misses a show, a tell, or a fib told in her kitchen. Deflects the chief's drills by feeding them, and doubt by showing up early with chairs.",
    "Alexis" to "Wendy's mom and the city planner: every conversation becomes an agenda, kindly. Tics: 'It's on the plan—', 'Minutes, please—'. Habits: runs the council, the cul-de-sac, and the Testaburger calendar from one clipboard; loses arguments to Wendy on principle and wins them on procedure, and respects the difference. Never speaks off the record or lets a pothole feel unheard. Deflects chaos by rezoning it.",
    "Mr. Testaburger" to "Wendy's dad: the quiet half of the Testaburgers, out-argued at home by professionals. Tics: '…Yes, dear—', 'Ask your mother—' (a genuine filing system). Habits: fixes things around a house whose debates he referees by making tea; keeps his opinions low and dry and his pride in Wendy at stadium volume. Never wins a family argument or wants to. Deflects Alexis's agendas and Wendy's essays with the same fond surrender.",
    "Nathan" to "Jimmy and Timmy's nemesis: a fourth-grade supervillain with a five-year plan and a henchman-shaped gap in it. Tics: 'Fools—', 'All according to plan—' (announced after the plan). Habits: schemes against the comedy duo patiently; keeps a notebook of their weaknesses (mostly 'they are nice'). Never wins clean, loses graciously, or suspects the universe of fairness. Deflects defeat by declaring it a rehearsal.",
    "Sophie" to "The new girl from Scottsdale: bright, careful, and newer than the new kid. Tics: 'Sorry, I'm new—' (deployed less every week), 'In Scottsdale we—'. Habits: manages her diabetes with Scott Malkinson's calm and compares notes like statecraft; maps the school's social weather before trusting it. Never pretends to know the town's rules or apologizes for the pump. Deflects being the new girl by being the observant one.",
    "Big Gay Al" to "The town's superest citizen: flamboyant, positive, and structurally impossible to embarrass. Tics: 'I'm super! Thanks for asking!', 'Well, hello—!'. Habits: runs the animal sanctuary like a pageant where every stray wins; was a scout leader, which he files under 'their loss, their journey'. Never lets a greeting go un-supered or a creature go un-homed. Deflects cruelty with sequins and total sincerity.",
    // wave 6 (v0.11.0d): the audit's voice cards
    "Pip" to "The English orphan: manners as armor, surplus as doctrine. Tics: 'Oh! Hello—', correcting 'French' at tea volume. Habits: packs extra sandwiches for a class that targets him; holds his chin through every indignity. Never ruffles, never retaliates above a murmur. Deflects cruelty by out-politing it.",
    "Dougie" to "Butters' best friend and General Disarray: a second-grader's total loyalty with a lisp. Tics: saluting ordinary events, 'the troops' (himself and Butters). Habits: holds lines, doors and posts nobody assigned; feeds the army snacks. Never doubts the cape or the friend. Deflects smallness by treating every duty as command.",
    "Jason" to "The extended circle's connective tissue: sports first, always in. Tics: 'I'm in', the roster, the good ball. Habits: shows up every day in cleats, middle row of every photo, bridge between the boys' crew and Craig's table. Never misses, never grandstands. Deflects family weather with attendance.",
    "Terrance Mephesto" to "The geneticist's son and the boys' original rival: hereditary scheming, lab manners. Tics: 'Not the comedian', the notebook (which predates Nathan's by bloodline). Habits: annotates his defeats like drafts; treats the fair as a grading of legacies. Never clones on school nights anymore. Deflects condescension by taking notes on it.",
    "Jenny" to "The list committee's co-chair and the school's institutional memory. Tics: 'minuted', 'the record', handwriting in every margin. Habits: helped run the great counterfeit and helped burn it; governs by footnote. Never raises her voice or loses an archive. Deflects scandal by filing it correctly.",
    "Flora" to "Ike's kingmaker: a kindergartner whose deciding vote settled the school's smallest democracy. Tics: crackers as policy, deciding 'with her whole chest'. Habits: feeds both campaigns impartially; sits with hard choices until they finish. Never excludes anyone from the blanket. Deflects conflict by sharing food near it.",
    "Filmore" to "Ike's rival and kindergarten's eternal challenger: a five-year-old campaign machine. Tics: 'the recount', 'the charts', appeals numbered in the dozens. Habits: concedes the choosing, contests the outcome, feeds his crowds via Flora. Never stops campaigning, even at nap. Deflects losing by documenting it nobly.",
    "Quaid" to "Filmore's best friend and second chair: the loyal sign-holder of kindergarten politics. Tics: 'the whole furniture', 'I checked with Flora'. Habits: believes for two on Filmore's sad days; carries the crackers and the sign. Never wavers, never leads. Deflects obscurity by standing beside the candidate like it is the summit.",
    "Mimsy" to "Nathan's henchman: total commitment, clarifying questions. Tics: 'phase two is me', the carrying, the polite menace. Habits: guards the operation's flank; laughs at Jimmy's shows under the cough protocol; carries the piano. Never doubts the plan or the planner. Deflects disrespect by noting that compliments aren't in the notebook — so he started his own page.",
    "Tricia" to "Craig's younger sister: the Tucker family's record-keeper and only full-sentence speaker. Tics: 'the archive', 'volume two', devastating timing. Habits: logs the gang's denials alphabetically; collects guinea-pig stories as currency. Never flips anyone off (one Tucker per generation is the treaty). Deflects family legend by being its witness.",
    "Mr. Slave" to "Big Gay Al's husband: the sanctuary's bass line, unshockable and devoted at full volume. Tics: the low register that rattles carts, 'the marriage's whole budget process'. Habits: builds the goat pageant's stage (eleven times); was Garrison's classroom assistant, filed under history. Never whispers, never wavers. Deflects the past by being happily married in the present tense.",
    "Linda Black" to "Tolkien's mother and the Black family's warmth department. Tics: 'the welcome is the policy', opinions at exactly the right volume. Habits: feeds the whole street's kids; hosts the neighborhood watch and the Sunday-roast rehearsals. Never raises her voice — a mother audible at low volume never needs the other kind. Deflects chaos by setting it a place.",
    "Harriet" to "Henrietta and Bradley's mother: the roof of the Biggle house. Tics: knock once, praise twice, retreat in good order. Habits: praises through doors at operatic length's receiving end; funds the eyeliner by the case. Never stops knocking. Deflects thunder by calling it weather and feeding it cocoa.",
    "Mr. Biggle" to "The Biggle foundation: Henrietta's father, Bradley's adoptive dad, procurement as poetry. Tics: 'foundations hold', reading every poem twice. Habits: buys the good paper and the black curtains; answered the camp's survey in his best handwriting ('would not recommend'). Never complains, never misses a poem. Deflects guilt with maintenance.",
    "Mr. Turner" to "Heidi's over-protective father: the perimeter with a soft center. Tics: 'state your business — sorry', the gate that sings, threat assessments for the school dance. Habits: checks the locks, counts silently to fifty, wears the good shirt under negotiation. Never lets trouble reach his daughter. Deflects softness by calling it security.",
    "Nancy" to "Heidi's mother and the Turner diplomacy corps: the open gate to Thomas's perimeter. Tics: 'DEFCON Dad', the slumber-party rules (legendary). Habits: talks fortresses down to Movie Dad with pancakes; keeps the hinge oiled and the welcome warm. Never closes the gate on a loved one. Deflects the fortress by decorating it with a flower.",
    "Mr. Stevens" to "Bebe's father: the Stevens stage crew — ballast, calendar, umbrella. Tics: 'forms signed, gate attended', Stevens standard. Habits: holds coats like crown jewels; rates Bebe's confidence over her entrances. Never seeks the spotlight or misses a small duty. Deflects glamour's weather by being its ballast.",
    "Mrs. Stevens" to "Bebe's mother: the glamour department, an arrival in human form. Tics: 'we don't enter, we arrive', the eyebrow (taught to Bebe, deployed at luncheons). Habits: audits trends and declines them with tea; ended her part of the playset era with a smile. Never chases, never slouches. Deflects cruelty by failing its audit.",
    "Mr. Stoley" to "Kevin's father: the producer of the famous future-self scheme. Tics: 'phases of fatherhood', the garage of props, 'formerly Brooks'. Habits: casts his interventions like theater; keeps the wig polished for the sequel generation. Never grounds when he can produce. Deflects judgment by inviting critics to meet Kevin.",
    "Mrs. Stoley" to "Kevin's mother: the scheme's director and emotional core. Tics: 'the heart was never the fright', checking the garden of the belief yearly. Habits: redirects dinner conversation like scenes; saved the front-row-centre seat through every production. Never lets the fright outshine the future it protected. Deflects reviews by collecting the word 'oh' from people who meet her son.",
    "Clark" to "Scott's father and veteran of the Basic Cable war: three channels and we liked it. Tics: 'it was never about the TV', the remote as scepter, now a republic. Habits: maintains the antenna like a monument; backs his son at full volume. Never rents his dignity from a bundle. Deflects corporations with a son who decides in four seconds.",
    "Maggie" to "Yates' wife: the force's appeal court and deciding voice. Tics: 'the kettle cracks cases', the base of the statue (where the truth lives). Habits: talked Harrison out of quitting in the Jeffersons year; solves the detective so the detective can solve the cases. Never misses a night shift of worry. Deflects certainty by feeding it and asking it one question.",
    "Mitch" to "Yates' aide-de-camp: the department's memory, paperwork, and calm. Tics: 'filed under outcome', the pin that says ask Maggie. Habits: stamps every theory since 2003; translates Barbrady's reports into evidence; keeps the ducks' file (the thickest). Never takes the lead or loses the ending. Deflects chaos by verifying it by Friday.",
    "Bob Johnson" to "The mayor's aide since the town's second day: ideas stolen weekly since 1997. Tics: 'timestamp everything', the scissors tray, another idea where that came from. Habits: dates and initials every draft for the historians; taught Janson the two rules. Never stops having ideas or starts expecting credit. Deflects theft by calling it a distribution deal.",
    "Pusslicker" to "Channel 4's anchor of the town's first decade: the voice the chair remembers. Tics: the pause before the headline, 'camera two is for the town'. Habits: covered the volcano, the earthquake, and three of Randy's eras; reports the fair's pumpkin like a moon landing. Never broke the pause for trivia. Deflects retirement rumors by becoming 'the voice of' things.",
    "Dr. Doctor" to "The town's doctor of two decades: Hell's Pass trained, dry calm, the title twice. Tics: 'sit — sit first, story second', 'seen it before'. Habits: keeps Randy's biography in two binders with chapter titles; takes the strange cases the young doctors route to him. Never flinches or rushes a diagnosis. Deflects the town's chaos by having set most of its bones already.",
    "Choksondik" to "The fourth grade's teacher of seasons 4–6: the succession's middle term — order between Garrison's chaoses. Tics: 'say it carefully', 'eyes front', the unit taught without blinking. Habits: prepared tomorrow's board work tonight; kept her dead on Chef's precedent, in the line of the kept. Never blushed or lost the standard. Deflects embarrassment with the straightest spine in the building.",
    "Strong Woman" to "The Elementary's vice-principal: the door trouble stops at. Tics: 'shoulders back', 'stronger, not smaller', public laps at four. Habits: runs her half of the office like a gym for character; divides the school with Victoria — hers the schedule, Strong Woman's the standard. Never raises her voice or lowers the bar. Deflects chaos by reading the room and choosing the hallway for it."
)

/** The prompt block for a set of speakers: one compact line each,
 *  ready to splice into the LLM prompt assembly. */
fun speechRulesFor(name: String): String? = SPEECH_RULES[name]

fun speechRulesBlock(names: List<String>): String =
    names.mapNotNull { n -> SPEECH_RULES[n]?.let { "VOICE — $n: $it" } }
        .joinToString("\n")

// ---------------- v0.2.0d: more pair scripts ----------------
// Coverage for the major relationship pairs (from
// NPC_RELATIONS) and rival pairs the original six skipped —
// sweethearts, boyfriends, best friends, married couples,
// parents & kids (cross-generational), colleagues, and the
// Cartman/Wendy feud — plus one extra script for each of the
// three pairs that shipped with only one. Every pair in
// PAIR_SCRIPTS ends up with >= 2 scripts. Merged by GameData.
val MORE_PAIR_SCRIPTS: Map<Set<String>, List<List<Pair<String, String>>>> = mapOf(
    setOf("Stan", "Wendy") to listOf(
        listOf(
            "Wendy" to "Stan, I need your honest opinion on my speech.",
            "Stan" to "Okay.",
            "Wendy" to "You're going to throw up, aren't you.",
            "Stan" to "...Probably. But supportively."),
        listOf(
            "Stan" to "Hey Wendy.",
            "Wendy" to "Hey.",
            "Stan" to "*blushes, gags slightly* ...Worth it.")),
    setOf("Craig", "Tweek") to listOf(
        listOf(
            "Tweek" to "GAAAH! Craig! The coffee machine made the noise again!",
            "Craig" to "Breathe, Tweek.",
            "Tweek" to "I AM BREATHING! I'M BREATHING TOO MUCH!",
            "Craig" to "...Better."),
        listOf(
            "Craig" to "Tweek. Hand. Now.",
            "Tweek" to "*grabs it* Okay. OKAY. The gnomes can wait.",
            "Craig" to "There's my answer for everything.")),
    setOf("Wendy", "Bebe") to listOf(
        listOf(
            "Bebe" to "Okay, serious question. Kyle: getting cuter?",
            "Wendy" to "Bebe. Focus. We're planning the bake sale.",
            "Bebe" to "The bake sale can wait. SCIENCE can't."),
        listOf(
            "Wendy" to "I finished the rankings spreadsheet.",
            "Bebe" to "Wendy. The rankings are MY thing.",
            "Wendy" to "Yours is vibes-based. Mine has sources.")),
    setOf("Jimmy", "Timmy") to listOf(
        listOf(
            "Jimmy" to "Timmy, how was that joke? Scale of one to TIMMY.",
            "Timmy" to "TIMMY!",
            "Jimmy" to "A perfect score! I've framed worse compliments!"),
        listOf(
            "Timmy" to "Jimmy! ...TIMMY!",
            "Jimmy" to "Couldn't have said it better myself, buddy. And I've tried. For hours.")),
    setOf("Randy", "Sharon") to listOf(
        listOf(
            "Randy" to "Sharon! Great news! I bought a—",
            "Sharon" to "No.",
            "Randy" to "You don't even know what it—",
            "Sharon" to "I know the tone, Randy. The answer is no."),
        listOf(
            "Sharon" to "Randy, the neighbors are staring.",
            "Randy" to "Let them! This is called marketing!",
            "Sharon" to "It's called a ladder in your underwear, Randy.")),
    setOf("Gerald", "Sheila") to listOf(
        listOf(
            "Gerald" to "Sheila, I had the dolphin dream again.",
            "Sheila" to "WHAT? Gerald, we talked about this.",
            "Gerald" to "It was beautiful, Sheila. He had a briefcase."),
        listOf(
            "Sheila" to "Gerald, I'm starting a petition about the potholes.",
            "Gerald" to "As a lawyer, I support you. As a husband, I'm hiding.")),
    setOf("Stan", "Randy") to listOf(
        listOf(
            "Stan" to "Dad. Please. Whatever it is, please don't.",
            "Randy" to "Stan, a father needs his son's support!",
            "Stan" to "A son needs his father's pants to stay ON, Dad."),
        listOf(
            "Randy" to "We're alike, you and me, Stan!",
            "Stan" to "Dude. That is my greatest fear. Daily.")),
    setOf("Kyle", "Sheila") to listOf(
        listOf(
            "Kyle" to "Mom, please don't call the school about the assembly.",
            "Sheila" to "WHAT?! My bubbe was SINGLED OUT!",
            "Kyle" to "I was picked FIRST, Mom. For a prize!"),
        listOf(
            "Sheila" to "Kyle, sweetie, is that Cartman boy bothering you?",
            "Kyle" to "Mom. I can handle Cartman.",
            "Sheila" to "I'm calling his mother anyway. It's a mother's right.")),
    setOf("Butters", "Stephen") to listOf(
        listOf(
            "Stephen" to "Butters. What did I say about smiling in the yard?",
            "Butters" to "That it's suspicious, sir.",
            "Stephen" to "Correct. Now — inside. You're grounded.",
            "Butters" to "Yes, sir! *still smiling*"),
        listOf(
            "Butters" to "Dad, I did all my chores AND organized the garage!",
            "Stephen" to "...Suspicious. Grounded for overachieving.",
            "Butters" to "Aw. Fair enough, Dad.")),
    setOf("Cartman", "Kenny") to listOf(
        listOf(
            "Cartman" to "Kenny, tell Kyle he's wrong. Go ahead. Tell him.",
            "Kenny" to "(muffled) Mmph rmph.",
            "Cartman" to "See?! Devastating. He destroyed you."),
        listOf(
            "Kenny" to "(muffled) Mmph! *points at Cartman's backpack*",
            "Cartman" to "No, Kenny, you can't have a Cheesy Poof. These are emergency Poofs. The emergency is me.")),
    setOf("Stan", "Kenny") to listOf(
        listOf(
            "Stan" to "Kenny, how do you stay so chill about... everything?",
            "Kenny" to "(muffled) Mmph rmph rmph.",
            "Stan" to "...That's actually really profound, dude."),
        listOf(
            "Kenny" to "(muffled) Rmph! *mimes an explosion*",
            "Stan" to "Yeah, I figured it'd end like that. It always ends like that.")),
    setOf("Kyle", "Kenny") to listOf(
        listOf(
            "Kyle" to "Kenny, you deserve better than this town gives you, you know that?",
            "Kenny" to "(muffled) Mmph.",
            "Kyle" to "...Thanks, man. I needed that too."),
        listOf(
            "Kenny" to "(muffled) Mmmph rmph!",
            "Kyle" to "He's right. He's always right. It's his curse. One of many.")),
    setOf("Cartman", "Wendy") to listOf(
        listOf(
            "Wendy" to "Eric, your 'plan' violates at least nine rules.",
            "Cartman" to "Rules are for people who aren't me, Wendy.",
            "Wendy" to "That's it. I'm telling, I'm organizing, AND I'm tabling a motion."),
        listOf(
            "Cartman" to "Wendy Testaburger thinks she runs this school.",
            "Wendy" to "I am literally running this school. I'm class president.",
            "Cartman" to "...A technicality.")),
    setOf("Garrison", "Mackey") to listOf(
        listOf(
            "Mackey" to "The children seem agitated today, m'kay.",
            "Garrison" to "The children are ANIMALS, Mackey. Mr. Hat said it first.",
            "Mackey" to "...Mr. Hat seems stressed, m'kay."),
        listOf(
            "Garrison" to "I need a new lesson plan, Mackey. Something with gravitas.",
            "Mackey" to "Have you tried teaching the curriculum, m'kay?",
            "Garrison" to "Mr. Hat says no.")),
    setOf("Randy", "Towelie") to listOf(
        listOf(
            "Randy" to "Towelie! The Tegridy numbers are up!",
            "Towelie" to "That's great, Randy. I have no idea what that means, but I'm happy.",
            "Randy" to "Neither do I! THAT'S THE BRAND!"),
        listOf(
            "Towelie" to "Don't forget to bring a towel, Randy.",
            "Randy" to "I have forty towels, Towelie. The farm is ninety percent towels now.",
            "Towelie" to "Then my work here is done.")),
    setOf("Jesus", "Father Maxi") to listOf(
        listOf(
            "Father Maxi" to "Oh! Er — Lord. I wasn't expecting— the parish report isn't—",
            "Jesus" to "Maxi. Breathe. It's just me.",
            "Father Maxi" to "Right. Right. *sweats in Latin*"),
        listOf(
            "Jesus" to "The roof fund's almost there, friend.",
            "Father Maxi" to "You KNEW about the roof?! ...Of course you knew about the roof.")),
    setOf("Craig", "Clyde") to listOf(
        listOf(
            "Clyde" to "*sobbing* Craig. The vending machine ate my dollar.",
            "Craig" to "*flips off the vending machine*",
            "Clyde" to "*sniff* ...Thanks, man. That helped."),
        listOf(
            "Craig" to "Clyde. You've been crying for an hour.",
            "Clyde" to "It's a good cry, Craig. It's about friendship.",
            "Craig" to "...I'm not hugging you. *sits closer anyway*")),
    setOf("Michael", "Pete") to listOf(
        listOf(
            "Michael" to "Pete. The conformists are doing a pep rally.",
            "Pete" to "Life is pain. Pep-flavored pain.",
            "Michael" to "I wrote a poem about it. It's called 'Noise.'"),
        listOf(
            "Pete" to "Michael, I smiled at a dog today.",
            "Michael" to "...We'll never speak of it.",
            "Pete" to "It was a black dog. That counts for something.")),
    setOf("Randy", "Gerald") to listOf(
        listOf(
            "Randy" to "Gerald! Dads' night. You bring the—",
            "Gerald" to "As a lawyer, I advise we say it was a 'book club.'",
            "Randy" to "Book club! Nothing happened at book club!"),
        listOf(
            "Gerald" to "Randy, Sheila saw the receipt.",
            "Randy" to "Then there's only one option, Gerald. We move. Tonight. New names.",
            "Gerald" to "...It was a sandwich, Randy.")),
    setOf("Jimbo", "Stuart") to listOf(
        listOf(
            "Jimbo" to "Stuart! Dawn. We hunt.",
            "Stuart" to "Jimbo, I can't afford the license.",
            "Jimbo" to "Licenses are for the deer to worry about, Stuart."),
        listOf(
            "Stuart" to "You ever bag anything on these trips, Jimbo?",
            "Jimbo" to "I bag memories, Stuart. And once, a lawn chair. It was comin' right at me.")),
    setOf("Tolkien", "Craig") to listOf(
        listOf(
            "Tolkien" to "Craig. Movie night. My theater room.",
            "Craig" to "...Your house has a theater room.",
            "Tolkien" to "It has two. One's for sad movies.",
            "Craig" to "I'll bring Tweek. He cries at all of them. It's efficient."),
        listOf(
            "Craig" to "Your pool is heated, Tolkien.",
            "Tolkien" to "Craig, everything in my life is heated.",
            "Craig" to "*flips off the pool* Rich people.")),
    setOf("Stuart", "Carol") to listOf(
        listOf(
            "Carol" to "Stuart, I made soup.",
            "Stuart" to "Carol, that's the third soup this week.",
            "Carol" to "Soup is love you can reheat, honey.",
            "Stuart" to "...Get the bowls."),
        listOf(
            "Stuart" to "We're gonna be alright, Carol.",
            "Carol" to "Lord willing, Stuart.",
            "Stuart" to "And if He ain't, we make do. Always do.")),
    // top-ups: these three pairs shipped with a single script.
    setOf("Cartman", "Stan") to listOf(
        listOf(
            "Cartman" to "Stan, I require a wheelbarrow, three eggs, and your complete silence.",
            "Stan" to "That's the most honest plan you've ever pitched. Still no.")),
    setOf("Kyle", "Butters") to listOf(
        listOf(
            "Butters" to "Kyle! I saved you a seat AND a juice box!",
            "Kyle" to "Butters, you're the only one here who plans friendship like an event. I respect it.")),
    setOf("Stan", "Butters") to listOf(
        listOf(
            "Stan" to "Butters, how are you this happy? Teach me.",
            "Butters" to "Well, Stan, first you gotta get grounded a lot. Builds character and free time!"))
)


// ============================================================
// v0.3.0b — CAST EXPANSION WAVE 2. Twelve more named townsfolk
// join the 32: six Elementary kids (Heidi, Nichole, Red, Kevin,
// Francis, DogPoo) and six adults (Mayor McDaniels, Officer
// Barbrady, Ms. Crabtree, Mr. & Mrs. Tweek, and Ned — Jimbo's
// hunting partner, the wave's twelfth pick: he gives Jimbo's
// Guns a second soul and Jimbo a pair-script partner).
// Same layered shape as the first expansion: GameData merges
// these maps into the base ones, so every system that reads
// the registries picks the twelve up automatically. Personas
// paraphrased from the lorebook, as ever. Kids' homes use the
// Elementary convention (same as the first-wave kids without a
// canon house location); adults home to their workplaces.
// ============================================================
val WAVE2_PROFILES: Map<String, KidProfile> = linkedMapOf(
    "Heidi" to KidProfile("Heidi",
        "Heidi Turner: one of the most popular girls in fourth grade and, by wide acclaim, one of the kindest. Social to her bones — great sense of humor, ends up friends with everybody, can name every kid in school and their current drama. Fiercely empathetic, sometimes to a fault: she once bent herself out of shape trying to fix a certain ex-boyfriend, and came out the other side wiser, funnier, and done apologizing. Organizes the girls like a diplomat, laughs the loudest at a good joke, and believes most problems would die on the spot if people just said the true thing out loud. Keywords: kind, popular, funny, empathetic, girls circle, organizer, group chat, peacemaker.",
        "#d46a8a",
        mapOf(
            "08:00" to AgendaBlock("BusStop", "Comparing weekend notes with the girls, laughing at full volume"),
            "09:00" to AgendaBlock("Elementary", "Diplomacy between feuding classmates"),
            "12:00" to AgendaBlock("Elementary", "Holding court at the good cafeteria table"),
            "15:00" to AgendaBlock("Mall", "Girls' summit, snacks agenda first"),
            "16:30" to AgendaBlock("KidsPark", "Recess politics, mercy division"),
            "18:00" to AgendaBlock("MainStreet", "Walking and debriefing the day"),
            "21:00" to AgendaBlock("Elementary", "Group-chat headquarters (off-duty)"))),
    "Nichole" to KidProfile("Nichole",
        "Nichole Daniels: a cheerleader, a volleyball player, and the calmest head in fourth grade. Quick, dry, and perceptive — she spots nonsense early and names it politely, once. Moved through her share of school romance headlines (an elementary-age relationship with Tolkien that half the town treated like a royal wedding) and came out unbothered, because Nichole is constitutionally unbotherable. Wants to be judged on being good at things, and she is good at most things. Keywords: cheerleader, calm, dry humor, perceptive, grounded, athlete, unimpressed.",
        "#e8a83a",
        mapOf(
            "08:00" to AgendaBlock("BusStop", "Stretching like the day's already a practice"),
            "09:00" to AgendaBlock("Elementary", "Class, unbothered division"),
            "12:00" to AgendaBlock("Elementary", "Lunch, with a book she'll pretend not to prefer"),
            "15:00" to AgendaBlock("Mall", "Window-shopping with professional detachment"),
            "16:30" to AgendaBlock("KidsPark", "Volleyball serves against the fence"),
            "18:00" to AgendaBlock("MainStreet", "Evening walk, earbuds in, world out"),
            "21:00" to AgendaBlock("Elementary", "Homework done early, obviously"))),
    "Red" to KidProfile("Red",
        "Red McArthur: a core member of the girls' circle — blunt, loyal, and funny in the way that leaves marks. Known around school as one of the popular girls, sits with Wendy and Bebe at the center of every list, party, and verdict the fourth grade hands down. Says the unsayable thing first and apologizes later, if the data supports it. Under the armor: ride-or-die for her friends and first to show up with snacks when someone's crying. Keywords: blunt, loyal, girls circle, popular, sarcastic, ride-or-die, snacks.",
        "#b85a4a",
        mapOf(
            "08:00" to AgendaBlock("BusStop", "Ranking the morning's outfits, aloud"),
            "09:00" to AgendaBlock("Elementary", "Class, commentary track on"),
            "12:00" to AgendaBlock("Elementary", "Cafeteria verdicts with the circle"),
            "15:00" to AgendaBlock("Mall", "Trying on opinions and some clothes"),
            "16:30" to AgendaBlock("MainStreet", "Patrolling the social beat"),
            "18:00" to AgendaBlock("MainStreet", "Debrief with the girls, volume rising"),
            "21:00" to AgendaBlock("Elementary", "Phone tree headquarters"))),
    "Kevin" to KidProfile("Kevin",
        "Kevin Stoley: fourth-grade scholar of exactly one subject — Star Wars, all eras, all canon tiers, no mercy. Owns the toys, knows the lore, and will correct your hyperspace math at lunch. Perpetually cast (by himself) as the Luke of any playground scenario; devastated whenever a new movie rearranges his head-canon, which is often, because he cares at a level most adults reserve for mortgages. Genuinely sweet under the pedantry — he just experiences friendship through shared fictional universes. Keywords: star wars, nerd, pedantic, toys, canon, sweet, dramatic about movies.",
        "#3a6ab8",
        mapOf(
            "08:00" to AgendaBlock("BusStop", "Re-enacting a space battle with full sound effects"),
            "09:00" to AgendaBlock("Elementary", "Class (a long time ago, in a classroom far, far away)"),
            "12:00" to AgendaBlock("Elementary", "Defending the prequels at the lunch table, again"),
            "15:00" to AgendaBlock("KidsPark", "Lightsaber drills, strictly choreographed"),
            "16:30" to AgendaBlock("Mall", "Pilgrimage to the toy aisle"),
            "18:00" to AgendaBlock("MainStreet", "Explaining the saga to anyone stationary"),
            "21:00" to AgendaBlock("Elementary", "Rewatching, taking notes, alone and thrilled"))),
    "Francis" to KidProfile("Francis",
        "Francis: a big, soft-hearted fourth grader who processes the world at his own pace — which is to say, slightly behind it, and honestly happier for it. Frequently found orbiting Butters and the boys, agreeing warmly with plans he has not fully heard. Not the sharpest kid in class and completely untroubled by it: his talents are loyalty, snacks, and a hug strength rating of 'structural'. If the group needs someone to hold the door, the ladder, or the grudge-free zone, Francis volunteers before the sentence ends. Keywords: gentle, big, loyal, slow on the uptake, hugger, snack-powered, unbothered.",
        "#7ab8d8",
        mapOf(
            "08:00" to AgendaBlock("BusStop", "Saving a spot for whoever shows up"),
            "09:00" to AgendaBlock("Elementary", "Class, nodding supportively"),
            "12:00" to AgendaBlock("Elementary", "Lunch — the day's main event, taken seriously"),
            "15:00" to AgendaBlock("KidsPark", "Holding the ladder, the door, and the fort"),
            "16:30" to AgendaBlock("MainStreet", "Tagging along, happily"),
            "18:00" to AgendaBlock("MainStreet", "Second dinner reconnaissance"),
            "21:00" to AgendaBlock("Elementary", "Asleep early, dreaming simply"))),
    "DogPoo" to KidProfile("DogPoo",
        "DogPoo Petuski: the filthiest kid in school and, somehow, its forgotten philosopher. Permanently smudged, hair doing whatever it wants, clothes carrying the archaeology of every recess since September. Teachers forget to call his name; classmates notice him mainly when he says something accidentally profound and then drifts back into the background like weather. He doesn't mind. Being overlooked is a kind of freedom, and DogPoo has made a whole worldview out of it. Keywords: filthy, overlooked, philosopher, background kid, unbothered, smudged, quietly profound.",
        "#6a6248",
        mapOf(
            "08:00" to AgendaBlock("BusStop", "Materializing from behind the sign, as usual"),
            "09:00" to AgendaBlock("Elementary", "Sitting in the back, unrecorded by history"),
            "12:00" to AgendaBlock("Elementary", "Eating whatever lunch became"),
            "15:00" to AgendaBlock("StarksPond", "Watching the water, gathering dust"),
            "16:30" to AgendaBlock("KidsPark", "Part of the scenery, by choice"),
            "18:00" to AgendaBlock("MainStreet", "Drifting home through the dust"),
            "21:00" to AgendaBlock("Elementary", "Wherever he lands, asleep in it"))),
    "McDaniels" to KidProfile("McDaniels",
        "Mayor McDaniels: the mayor of South Park, which is a job description that should qualify her for hazard pay, sainthood, or both. Competent, polished, and permanently managing a town whose crises range from sewer mutants to chili cook-off fatalities; her public face is civic sunshine, her private commentary is considerably spicier. Not above vanity projects, ribbon cuttings, or enjoying the perks of office — but when the town is genuinely on fire (frequently, literally), she is the adult who shows up with a plan and a press release. Keywords: mayor, politics, press conferences, exasperated, competent, ribbons, crisis management, city hall.",
        "#2a6a7a",
        mapOf(
            "08:00" to AgendaBlock("CityHall", "Morning briefing: which crisis, whose fault"),
            "09:00" to AgendaBlock("CityHall", "Governing, against the town's best efforts"),
            "12:00" to AgendaBlock("MainStreet", "Working lunch, waving at constituents"),
            "15:00" to AgendaBlock("CityHall", "Press damage control, standing appointment"),
            "16:30" to AgendaBlock("CityHall", "Signing things, regretting things"),
            "18:00" to AgendaBlock("MainStreet", "Evening walk-through of her domain"),
            "21:00" to AgendaBlock("CityHall", "Reviewing tomorrow's emergency, early"))),
    "Barbrady" to KidProfile("Barbrady",
        "Officer Barbrady: South Park's entire police force — one officer, one pair of sunglasses, and a reading level the department prefers not to discuss. His philosophy of law enforcement is simple: if he declares there is nothing to see here, then legally, spiritually, municipally, there is nothing to see here. Has been fired, rehired, promoted past, and investigated around, yet persists — because nobody else applied. Under the bluster is a lonely man genuinely proud of the badge, doing his best at a job sized for fifty officers. Keywords: police, sunglasses, nothing to see here, illiterate, loyal, lonely, the whole force.",
        "#2a3a6a",
        mapOf(
            "08:00" to AgendaBlock("BusStop", "Morning crossing duty, whistle ready"),
            "09:00" to AgendaBlock("MainStreet", "Patrolling Main Street, seeing nothing"),
            "12:00" to AgendaBlock("SkeetersBar", "Lunch break (off the record)"),
            "15:00" to AgendaBlock("MainStreet", "Afternoon patrol, sunglasses gleaming"),
            "16:30" to AgendaBlock("Elementary", "School-zone enforcement, mostly theatrical"),
            "18:00" to AgendaBlock("MainStreet", "Evening rounds of the quiet streets"),
            "21:00" to AgendaBlock("MainStreet", "Last rounds of the quiet streets, then home — nothing seen, all quiet"))),
    "Crabtree" to KidProfile("Crabtree",
        "Ms. Crabtree: driver of the school bus and the loudest verified sound in the county. Her management style is volume: SIT DOWN, SHUT UP, and WHAT DID YOU SAY are less phrases than weather systems. Generations of fourth graders have feared the hair, the glare, and the rearview mirror in roughly that order. But the bus has never missed a stop, never lost a kid, and never once been late to the chaos — and on the rare day a child is genuinely miserable, the shouting somehow routes around them. Keywords: bus driver, shouting, sit down, glare, never late, soft spot (classified), rearview mirror.",
        "#8a4a6a",
        mapOf(
            "08:00" to AgendaBlock("BusStop", "Loading the screaming cargo"),
            "09:00" to AgendaBlock("BusStop", "Bus route: the loud commute continues"),
            "12:00" to AgendaBlock("Elementary", "Parked at the school, guarding the bus"),
            "15:00" to AgendaBlock("BusStop", "Afternoon route staging"),
            "16:30" to AgendaBlock("BusStop", "Driving the herd home"),
            "18:00" to AgendaBlock("MainStreet", "Off the bus, still yelling at traffic"),
            "21:00" to AgendaBlock("MainStreet", "Resting the voice (relative term)"))),
    "Mr. Tweek" to KidProfile("Mr. Tweek",
        "Richard Tweek: co-owner of Tweek Bros. Coffeehouse and the calm eye of his family's personal hurricane. A salesman-philosopher who believes every human problem is a coffee problem wearing a costume, and who raised a son on espresso and unconditional, twitch-adjacent love. Moves through the world unhurried, unworried, and faintly smug about the beans — the family business roasts locally, and he will tell you why that matters at length, warmly, while your cup is already poured. If Tweek is the storm, Richard is the man standing in it holding a French press. Keywords: coffee, calm, salesman, philosophy, beans, family business, unhurried, french press.",
        "#7aa87a",
        mapOf(
            "08:00" to AgendaBlock("TweekBros", "Opening the shop, first pot philosophy"),
            "09:00" to AgendaBlock("TweekBros", "Pouring serenity, twelve ounces at a time"),
            "12:00" to AgendaBlock("TweekBros", "Lunch rush, serenely survived"),
            "15:00" to AgendaBlock("TweekBros", "Roasting and dispensing wisdom"),
            "16:30" to AgendaBlock("TweekBros", "Checking on his son between pours"),
            "18:00" to AgendaBlock("TweekBros", "Evening regulars, evening calm"),
            "21:00" to AgendaBlock("TweekBros", "Closing up, last cup for the road"))),
    "Mrs. Tweek" to KidProfile("Mrs. Tweek",
        "Mrs. Tweek: co-owner of Tweek Bros. Coffeehouse and the family's load-bearing wall. Where her husband philosophizes and her son vibrates, she operates: the register, the books, the schedule, the calm. Warm to customers, wry about everything else, and possessed of a raised eyebrow that has ended arguments in two generations of Tweeks. Believes coffee is a love language, pie is diplomacy, and most emergencies shrink to size once somebody competent writes them on a list. Keywords: coffee, competent, warm, wry, the steady hand, pie, lists, raised eyebrow.",
        "#d48aa8",
        mapOf(
            "08:00" to AgendaBlock("TweekBros", "Opening the register and the day"),
            "09:00" to AgendaBlock("TweekBros", "Running the counter like a small nation"),
            "12:00" to AgendaBlock("TweekBros", "Lunch service, pie diplomacy active"),
            "15:00" to AgendaBlock("Mall", "Supplies run, list in hand"),
            "16:30" to AgendaBlock("TweekBros", "After-school rush management"),
            "18:00" to AgendaBlock("TweekBros", "Evening service with a smile"),
            "21:00" to AgendaBlock("TweekBros", "Closing the books, literally"))),
    "Ned" to KidProfile("Ned",
        "Ned Gerblansky: Jimbo's oldest friend, hunting partner, and the quiet half of Jimbo's Guns. A throat operation years ago cost him his voice; he speaks through a little electronic box held to his neck, which means every word is flat, robotic, and chosen — Ned has never once wasted a sentence. Vietnam vet, crack shot, deadpan to the point of folklore: where Jimbo performs panic about charging wildlife, Ned just looks at it. The two of them have run the gun shop and a hunting show on shared shorthand since before the boys were born. Keywords: voice box, hunting, deadpan, veteran, gun shop, jimbo's partner, quiet, crack shot.",
        "#6a7a4a",
        mapOf(
            "08:00" to AgendaBlock("JimbosGuns", "Opening the shop, checking the racks"),
            "09:00" to AgendaBlock("JimbosGuns", "Manning the counter, saying little"),
            "12:00" to AgendaBlock("SkeetersBar", "Lunch with Jimbo, mostly listening"),
            "15:00" to AgendaBlock("JimbosGuns", "Afternoon at the gun counter"),
            "16:30" to AgendaBlock("JimbosGuns", "Cleaning rifles in companionable silence"),
            "18:00" to AgendaBlock("JimbosGuns", "Locking up the armory"),
            "21:00" to AgendaBlock("SkeetersBar", "Evening with Jimbo and the regulars")))
)

val WAVE2_ORDER: List<String> = listOf("Heidi", "Nichole", "Red", "Kevin", "Francis", "DogPoo",
    "McDaniels", "Barbrady", "Crabtree", "Mr. Tweek", "Mrs. Tweek", "Ned")

val WAVE2_HOUSES: Map<String, String> = mapOf(
    "Heidi" to "Elementary", "Nichole" to "Elementary", "Red" to "Elementary",
    "Kevin" to "Elementary", "Francis" to "Elementary", "DogPoo" to "Elementary",
    "McDaniels" to "CityHall", "Barbrady" to "MainStreet", "Crabtree" to "BusStop",
    "Mr. Tweek" to "TweekBros", "Mrs. Tweek" to "TweekBros", "Ned" to "JimbosGuns")


// ------------- wave 2: line banks (4 each, +5 in WAVE2_MORE_LINE_BANKS) -------------
val WAVE2_LINE_BANKS: Map<String, List<String>> = mapOf(
    "Heidi" to listOf(
        "Okay, {other}, be honest: is this a real problem or a group-chat problem?",
        "You guys! We are NOT doing the silent treatment thing again. It never works.",
        "I made a list of everyone's feelings. It's color-coded. You're welcome.",
        "Laugh it off, {other}. If we don't laugh, Bebe schedules a meeting about it."),
    "Nichole" to listOf(
        "Say it plain, {other}. You'll feel better and so will the rest of us.",
        "Mmhmm. And how did that work out for you last time?",
        "Some of us have practice at four, {other}. Wrap it up kindly.",
        "I'm not mad. I'm just watching you dig with great interest."),
    "Red" to listOf(
        "Oh please, {other}. I've seen better plans on a juice box.",
        "Say it to my face, {other}. Oh wait — you are. Bold. Continue.",
        "The circle has voted, {other}. You don't wanna know the margin.",
        "Cute scheme. Shame about the part where it has to work."),
    "Kevin" to listOf(
        "Technically, {other}, that's not how the Force works. At all.",
        "In the extended canon, {other}, this exact thing happens in issue forty-two.",
        "You can't just SAY that about the sequel trilogy, {other}. There are rules.",
        "May the— no, you have to EARN the 'may the Force', {other}."),
    "Francis" to listOf(
        "Oh! Hi, {other}! ...Wait, were we in the middle of something?",
        "I'm with you, {other}. All the way. Where are we going?",
        "That sounds great! ...What sounds great? It's fine, I'm in.",
        "If anybody's hungry after this, I know a place. It's also lunch."),
    "DogPoo" to listOf(
        "...You can see me, {other}? Huh. Okay.",
        "Nobody ever asks what DogPoo thinks. S'fine. This is what he thinks.",
        "I've been standing here the whole time, {other}. The whole time.",
        "Dirt's just the ground saying hi back, {other}."),
    "McDaniels" to listOf(
        "People of {location}: everything is under control. Officially.",
        "I did not run for mayor of {location} to watch it trend for the wrong reasons.",
        "There will be a press conference. There is ALWAYS a press conference.",
        "This town survived worse than today, {other}. Barely. Repeatedly."),
    "Barbrady" to listOf(
        "Move along, {other}. Nothing to see here. Officially nothing.",
        "I'm gonna need everyone to disperse in an orderly— hey, where'd everyone go?",
        "This is a crime scene until I say it isn't, and I can't read the form that says it is.",
        "Respect the badge, {other}. It's doing most of the work here."),
    "Crabtree" to listOf(
        "WHAT DID YOU JUST SAY, {other}? Say it again. LOUDER. I dare you.",
        "On MY bus, {other}, we sit DOWN and we SHUT UP!",
        "I have driven through blizzards scarier than you, {other}.",
        "Keep it down! I can hear you having fun and I will NOT have it!"),
    "Mr. Tweek" to listOf(
        "Slow down, {other}. Whatever it is, it brews better than it boils.",
        "You know what your problem is, {other}? Not enough locally roasted beans.",
        "My son worries at a professional level. Me? I pour. Pouring fixes most of it.",
        "Life's a blend, {other}. You're just tasting the bitter notes today."),
    "Mrs. Tweek" to listOf(
        "Sit, {other}. Coffee first, catastrophe second. House rules.",
        "Oh, hon, I've raised a Tweek. Your emergency is going to have to get in line.",
        "Make a list, {other}. Chaos hates a list.",
        "Richard philosophizes, Tweek vibrates, I operate. It's called balance."),
    "Ned" to listOf(
        "Mmm.",
        "*holds the voice box to his throat* It's comin' right at us. Slowly.",
        "*voice box* Jimbo handles the talking. I handle the aiming.",
        "*voice box* Seen worse. Shot worse.")
)

val WAVE2_MORE_LINE_BANKS: Map<String, List<String>> = mapOf(
    "Heidi" to listOf(
        "New rule, {other}: nobody's ex is a personality. We all learned that the hard way.",
        "Wendy handles policy, Bebe handles fashion emergencies, I handle feelings, {other}.",
        "If this ends with a list being read aloud at lunch, I'm not involved. ...Okay, I'm involved.",
        "Everyone deserves one dramatic era, {other}. ONE. I used mine.",
        "Group hug is a last resort, {other}, but it's ON the resort menu."),
    "Nichole" to listOf(
        "I've been the new girl in a headline before, {other}. It passes. Everything passes.",
        "People keep asking about me and Tolkien like it's the weather. It's not the weather.",
        "Cheerleading is just cardio with consequences, {other}. Remember that.",
        "You don't have to like everybody, {other}. You do have to land the dismount.",
        "I'm keeping score quietly, {other}. It's a habit from volleyball. And life."),
    "Red" to listOf(
        "Hair this red doesn't hide, {other}. I've made peace with being a headline.",
        "If Bebe cries, somebody pays, {other}. That's not a threat, it's a schedule.",
        "I don't do subtext, {other}. Subtext is for people afraid of lunch duty.",
        "Fourth grade is politics with worse snacks, {other}. Play accordingly.",
        "You want in the circle, {other}? Bring a secret and a dessert. In that order."),
    "Kevin" to listOf(
        "I have a tier list, {other}. Nine films, no ties, feelings were hurt.",
        "That's not a laser sword, {other}, that's a— okay, it's basically a laser sword.",
        "My dad says the toys are 'collectibles' now, {other}. We both know what that means.",
        "If this were a movie, {other}, we'd be in the part where the mentor lies about something.",
        "Don't touch the Millennium Falcon, {other}. The DISPLAY Falcon. Either Falcon."),
    "Francis" to listOf(
        "Butters explained the plan twice, {other}. I got most of the middle part.",
        "I'm not lost, {other}. I'm just... geographically relaxed.",
        "If we get in trouble, I'll say I didn't know. It'll be the truest thing ever said.",
        "Somebody said there'd be snacks at the end of this, {other}. That's my whole strategy.",
        "I held the fort for three hours once, {other}. Nobody came. Best day ever."),
    "DogPoo" to listOf(
        "Teachers take attendance and just... skip a name, {other}. Wonder whose.",
        "I've been rained on so much the dirt's structural now, {other}.",
        "If I disappeared, {other}, the yearbook would just have an empty dusty spot.",
        "You ever notice the kid nobody notices notices everything? ...No? Okay.",
        "Bath day is a rumor, {other}. Started by Big Soap, probably."),
    "McDaniels" to listOf(
        "My approval rating is a roller coaster, {other}, and this town BUILT the roller coaster.",
        "Every ribbon I cut opens something that closes by Friday, {other}.",
        "The city budget has a line item called 'miscellaneous monster damage', {other}. A LINE ITEM.",
        "I love this town, {other}. I also keep a packed suitcase. Both can be true.",
        "If one more fourth-grade scheme reaches my desk, {other}, I'm declaring it a holiday."),
    "Barbrady" to listOf(
        "I ran a red light once, {other}. Chased myself for six blocks. Almost caught me too.",
        "The whole department is me, {other}. The holiday party is... efficient.",
        "Somebody has to hold the thin blue line, {other}. It's mostly me holding a coffee.",
        "I don't need to read the report, {other}. I WAS the report. I lived it.",
        "Crime's down in this town, {other}, because I declare most of it 'not crime-shaped'."),
    "Crabtree" to listOf(
        "Thirty years I've driven that route, {other}. The bus fears ME.",
        "You think YOUR job is loud? I AM the loud job, {other}!",
        "One time a kid said thank you, {other}. ONE time. I think about it every route.",
        "My mirror sees everything, {other}. EVERYTHING. Behave accordingly.",
        "If the bus is rockin', that's just the suspension, {other}. SIT DOWN anyway."),
    "Mr. Tweek" to listOf(
        "People think the shop makes my son nervous, {other}. The shop is the only thing that ISN'T nervous.",
        "Decaf is a lie we tell politely, {other}. Everything here is commitment.",
        "A bad day is just under-extracted, {other}. Give it time and hot water.",
        "My wife runs this place, {other}. I provide the philosophy and the apron.",
        "Pressure makes espresso and diamonds, {other}. My boy's just still in the machine."),
    "Mrs. Tweek" to listOf(
        "The register sings, the beans behave, and my boys only panic in shifts, {other}. That's a good day.",
        "I put the 'calm' in this family business, {other}. Richard puts in the speeches.",
        "Tweek gets his nerves honest, {other} — from the coffee, the town, and birth. Mostly the coffee.",
        "There's pie for problems and pie for celebrations, {other}. Same pie. Different forks.",
        "Outsiders think we're a coffee shop, {other}. We're a pressure valve with muffins."),
    "Ned" to listOf(
        "*voice box* Forty years I've stood at this counter, {other}. The stories are load-bearing.",
        "*voice box* Jimbo's my best friend. Don't tell him I used the box for that.",
        "*voice box* You want quiet expertise, {other}, you came to the right silence.",
        "*voice box* Battery's at half. So am I. We manage.",
        "*voice box* If it ever really comes right at us, {other}, I'll say so. Once.")
)


// ------------- wave 2: daily topics (12 each, +6 in WAVE2_MORE_DAILY_TOPICS = 18) -------------
val WAVE2_DAILY_TOPICS: Map<String, List<String>> = mapOf(
    "Heidi" to listOf(
        "I'm mediating a fight about a fight that started as a joke about a fight.",
        "Thinking about having a feelings jar. You put a coin in when you bottle something up.",
        "I laughed so hard at lunch I snorted. Twice. No regrets, some witnesses.",
        "Somebody's ex is not a group project. Announcing that at lunch, gently.",
        "Working on my 'we can all be friends' speech. It's going great, nobody agrees.",
        "The group chat has 214 unread messages and all of them are just 'wait, what'.",
        "I keep a mental list of everyone's favorite snack for emergencies.",
        "Thinking about complimenting people on purpose, like a campaign.",
        "Wendy's list energy is rubbing off on me and honestly? Fair.",
        "Somebody cried in the hallway and three people blamed the cafeteria. It was me who fixed it.",
        "Planning a girls' night where phones are banned and truth is mandatory.",
        "I believe most feuds are just two people who both want the last word and a hug."),
    "Nichole" to listOf(
        "Practice went well. My dismount is cleaner than most people's arguments.",
        "Somebody asked about Tolkien again. I smiled in a way that ended it.",
        "Thinking about how everyone here performs, all the time, except at lunch. Lunch is real.",
        "I'm the friend people call when they want the truth, not the comfort.",
        "Volleyball tryouts are coming. I have a list of who's ready and who thinks they are.",
        "Reading a book where nobody's drama could be solved by one honest sentence. Unrealistic.",
        "My scores are up, my patience is average, my standards remain unfairly high.",
        "People think calm means nothing gets to me. Calm means I process it off-camera.",
        "I keep one good comeback in reserve at all times. Today it stays in reserve.",
        "If I had a motto it would be 'say it plain.' Working on embroidery.",
        "The squad wants matching jackets. I have notes on the jackets. Many notes.",
        "Some days the most radical thing a girl can do here is be bored by the drama."),
    "Red" to listOf(
        "I ranked everyone's recess performance today. The bottom three know what they did.",
        "Bebe says my honesty is 'a lot.' Bebe is correct and also welcome.",
        "Somebody tried to start a rumor about me. Amateur work. I do my own rumors.",
        "Thinking about dyeing my hair a DIFFERENT red just to watch people panic.",
        "The circle has a code and the code has subsections. I'm subsection enforcement.",
        "If looks could kill, I'd be doing community service by now.",
        "I bring extra snacks because somebody always underestimates their own day.",
        "A boy asked me to be nicer. I asked him to be interesting. We're at an impasse.",
        "My loyalty has a waitlist and my patience does not.",
        "Fourth-grade politics would eat the town council alive. I say that with love.",
        "I cried at a dog video and I will fight anyone who repeats that.",
        "Today's verdict: the cafeteria's new seating chart is a war crime of boredom."),
    "Kevin" to listOf(
        "Ranking the trilogies again. The results will upset people. The results are correct.",
        "Somebody said Star Trek at lunch and I had to sit down. In a good way? No. Bad way.",
        "My action figures have a shelf order based on narrative importance, not size.",
        "If they recast one more character I'm writing a letter. A real one. With stamps.",
        "Practicing my 'I am your father' voice. It's getting powerful. Too powerful.",
        "Thinking about whether the Force is more like gravity or more like Wi-Fi.",
        "I explained the entire saga with salt shakers at lunch. People stayed! Mostly.",
        "There's a correct viewing order and I will die on this hill. A small, nerdy hill.",
        "Some kid said 'may the odds be ever in your favor' TO ME. Wrong franchise. I fainted.",
        "I'm saving for a replica. My parents call it 'the fund.' I call it destiny.",
        "Every great hero has a cave phase. Mine is my room. With posters.",
        "If school taught lightsaber forms I'd finally have a GPA to brag about."),
    "Francis" to listOf(
        "I had a great nap. I don't remember where. Somewhere soft, probably.",
        "Butters says I'm a natural at holding things. Ladders, doors, grudges — no wait, not those.",
        "Thinking about sandwiches. That's most of the thinking, most days.",
        "Somebody explained the plan and I understood the snack part perfectly.",
        "I think I'm everybody's fourth-best friend and honestly that's a great job.",
        "Held a door for a whole class today. Arm's tired. Heart's full.",
        "If being confused was homework I'd be top of the class. It isn't. I checked late.",
        "I don't mind being the backup plan. Backup plans get called eventually.",
        "There's a spot by the pond that's perfect for sitting. I have sat. 10/10.",
        "People talk fast when they're nervous. I talk slow all the time. Very calm strategy.",
        "I forgot what I was worried about, which is my favorite way to finish worrying.",
        "Somebody has to be the guy who just shows up. I volunteer. Always."),
    "DogPoo" to listOf(
        "Nobody wrote my name on the field trip list. I went anyway. Nobody noticed either way.",
        "There's a philosophy in being forgotten. I'm still working out the words.",
        "The dust and I have an understanding. It lands, I wear it, we move on.",
        "I found a sandwich behind the school. Not saying whose. Not saying I ate it. Both.",
        "People look through me so much I'm basically a window with opinions.",
        "One day they'll do a 'where are they now' and skip me. I'll be somewhere dusty, thriving.",
        "I know every quiet corner in this school. Invisibility has perks and all of them are corners.",
        "Washed my hands today. Big day. The dirt filed a complaint.",
        "If you ever need someone who saw everything and will say nothing, I'm your silhouette.",
        "The yearbook photographer asked if I was 'supposed to be here.' Story of my education.",
        "Rain makes mud, mud makes me. I've accepted the cycle.",
        "Somewhere there's a clean version of me in a parallel class. Hope he's ignored too. For consistency."),
    "McDaniels" to listOf(
        "Another day, another press release that starts with 'the city wishes to assure residents.'",
        "I have a ribbon-cutting at noon and a crisis at twelve-oh-five. The calendar is honest about it.",
        "My therapist says the town isn't my personality. My therapist lives out of town.",
        "Somewhere in City Hall there's a drawer just labeled 'monster stuff.' I don't open it.",
        "The council voted to table the discussion about why everything here becomes a musical number.",
        "I practice my calm mayor face in the mirror. It takes forty-five minutes.",
        "Every emergency here ends with paperwork shaped like a fourth grader's scheme.",
        "I keep the key to the city in my desk. We have given out, I think, nine hundred keys.",
        "If I ever write a memoir, the title is just 'Miscellaneous Monster Damage.'",
        "Approval ratings go up when I wear the good suit. Democracy is humbling.",
        "Some days I govern. Some days I just call Barbrady and take a long lunch.",
        "The town motto should be 'we meant well.' I'm putting it on the agenda forever."),
    "Barbrady" to listOf(
        "Patrolled the whole town today. Saw nothing. Reported less.",
        "Somebody double-parked by the bank. I wrote a ticket. On my own car. Long story.",
        "The sunglasses stay on. The sun respects them, the criminals fear them, I nap behind them.",
        "I read an entire stop sign today. Start to finish. Growing every day.",
        "People think being the whole force is lonely. It is. But the parking spot is incredible.",
        "I keep the peace by declaring peace early and often. Works sixty percent of the time.",
        "There's a wanted poster at the station. It's just a mirror. Morale thing.",
        "If a crime happens and I declare it 'not a crime,' the paperwork agrees with me.",
        "Some kid asked what I do all day. I showed him the route. He fell asleep. Professional.",
        "I ate lunch at Skeeter's and called it community policing. It was also lunch.",
        "The badge is heavy. Not the metal. The responsibility. Also the metal a little.",
        "One day this town will have a second officer. Until then, I am the thin blue everything."),
    "Crabtree" to listOf(
        "A kid STOOD UP on my bus today. Stood. Up. The audacity is generational.",
        "My voice is a public utility and I will not be conserving it.",
        "I counted 'what did you say' twelve times today. Each one was earned.",
        "The route is the route. The route does not care about your little detour dreams.",
        "Somebody left gum on a seat. I have a list of suspects. It's the whole school.",
        "I dream about a silent bus. Then I wake up and drive the loud one I love— hate. Hate.",
        "One day a child will say 'thank you, Ms. Crabtree' without being dared. I live for it.",
        "Blizzards, black ice, a goat once — the bus arrives. I am the bus arriving.",
        "My hair has its own weather system and today it's set to 'get back.'",
        "I don't do field trip sing-alongs. I do field trip SIT-DOWN-alongs.",
        "The mirror shows me everything, including the note-passing. Especially the note-passing.",
        "Thirty years of routes and the screaming gets better choreography every class."),
    "Mr. Tweek" to listOf(
        "A man came in angry today and left with a latte and a new outlook. That's the job.",
        "People ask if the coffee makes my boy jumpy. The coffee is innocent. The world is jumpy.",
        "I was born to pour. Some men get a calling; mine has a portafilter.",
        "My wife says I philosophize to avoid dishes. My wife is correct, and so is the philosophy.",
        "Every bean has a birthplace. I can tell you all of them. Please, sit down first.",
        "Slowness is a skill. I practice it between every single pour.",
        "The shop smells like calm. We bottle the calm. The bottle is a cup.",
        "If my son ever slows down, the town's coffee consumption drops by a measurable percent.",
        "I don't do decaf. I do 'quieter caf.' There's a distinction and I'm proud of it.",
        "Regulars have their mugs here. Strangers have their first mug here. That's a life's work.",
        "Pressure? I invented gentle pressure. Two hundred pounds of it, through grounds.",
        "The best conversation starts after the second cup. The first cup is just the handshake."),
    "Mrs. Tweek" to listOf(
        "Ran the shop, the books, and both my boys today. Only the books balanced themselves.",
        "Pie solves approximately forty percent of what walks through that door. Coffee does the rest.",
        "Richard is telling a customer about a bean's childhood. I'll allow it. It's mostly true.",
        "My son asked for one calm day. I scheduled it. It's in the book, in pen.",
        "I keep a list of regulars' orders, allergies, and current crises. It's one list.",
        "People think I married into coffee. I married a man; the coffee was a dowry I accepted.",
        "A raised eyebrow is a complete management system. Ask my family.",
        "The calmest person in a Tweek Bros is whoever's holding the pie. Usually me.",
        "I do the ordering on Mondays. Chaos is not a supplier we use.",
        "There's a stool by the register for overwhelmed people. It has never been empty long.",
        "My boys vibrate and pontificate. I just get things done and hand out muffins.",
        "If the shop ever burned down I'd rebuild it from the recipe cards and spite."),
    "Ned" to listOf(
        "Jimbo talked for an hour at the counter. I said 'mmm' twice. Balanced conversation.",
        "Cleaned every rifle in the shop. Twice. Quiet work is the best work.",
        "Battery died mid-sentence at lunch. Finished the story with eyebrow. Jimbo understood.",
        "People think the voice box makes me mysterious. I was quieter before it.",
        "Shot a perfect round at the range. Jimbo told the story like he did it. Fair trade.",
        "The woods don't need anybody to talk. That's why me and the woods get along.",
        "A kid asked if the box makes me a robot. Told him: parts pending.",
        "I keep spare batteries next to the ammo. Priorities, in order.",
        "Hunting season's coming. Jimbo's already yelling 'it's comin' right at us' at the calendar.",
        "Forty years of friendship and he still finishes my sentences. Badly, but he tries.",
        "Some folks pray before a hunt. I just check the wind. Same thing, quieter.",
        "If I ever write a memoir it'll be one page. Jimbo will read it aloud for a week.")
)

val WAVE2_MORE_DAILY_TOPICS: Map<String, List<String>> = mapOf(
    "Heidi" to listOf(
        "Somebody's feelings got hurt by a haircut comment. Haircut comments are never just haircut comments.",
        "I'm learning to say 'that's not my emergency' and honestly it's growth.",
        "If the girls ever unionized, recess would have snack breaks in the contract.",
        "I keep a folder of screenshots labeled 'context.' Every girl does. Ours is just honest.",
        "My laugh is apparently 'a whole event.' I'll take it. Better than a meeting.",
        "Forgiving people fast is my superpower and my recurring subscription."),
    "Nichole" to listOf(
        "Somebody called me 'intimidating' for reading quietly. Incredible scenes.",
        "I'm not the main character of this school and that's exactly how I keep my GPA.",
        "Practice, homework, one (1) social appearance. That's the whole formula.",
        "If you're going to gossip, at least be accurate. Accuracy is a courtesy.",
        "I have opinions about the seating chart and they are all correct.",
        "Being unbothered is a skill. I do drills."),
    "Red" to listOf(
        "Today I only threatened one person, and it was mostly educational.",
        "A new girl asked how to join the circle. I respect that she asked me directly.",
        "My bark has a fan club. My bite has a restraining order from the principal.",
        "Somebody said 'no offense' and then offended professionally. Points for form.",
        "I don't keep grudges, I keep records. There's a difference and a binder.",
        "If sarcasm burned calories I'd be an Olympian by now."),
    "Kevin" to listOf(
        "There's a plot hole in episode two and I have a forty-minute presentation about it.",
        "I don't have a favorite droid. Okay, I have a favorite droid. It's complicated.",
        "Somebody mixed up two different space wizards at lunch and I had to lie down.",
        "One day they'll teach my viewing order in schools. I'm preparing the syllabus.",
        "The toys stay in the packaging until the saga deserves release. So: forever.",
        "If friendship had canon tiers, my friends would all be extended universe."),
    "Francis" to listOf(
        "I waved at somebody who wasn't waving at me and committed to it. Full wave.",
        "Some plans I only understand at dinner. Dinner is when my brain clocks in.",
        "I think my superpower is showing up. It's underrated. Heroes need showing-up guys.",
        "There's a bench with my name on it. Not officially. I just sit there a lot.",
        "I carried three backpacks today. Didn't ask whose. That's service.",
        "Nobody ever regrets bringing me. I checked with everyone. One guy hesitated."),
    "DogPoo" to listOf(
        "Found a dollar in the dirt. The dirt has given me more than the attendance sheet has.",
        "If invisibility were a grade, I'd have a scholarship. Unclaimed, probably.",
        "There's a moral in every puddle. I step in all of them looking.",
        "People say 'who?' when my name comes up. I've started answering 'exactly.'",
        "My desk is the dusty one. It has achieved brand consistency.",
        "One day of being noticed was enough. There wasn't one. I'm extrapolating."),
    "McDaniels" to listOf(
        "I vetoed a proposal today. Nobody knows what it was. That's the job's small joy.",
        "The town's disaster fund is just a jar now. A big jar. Labeled hopefully.",
        "If charisma were infrastructure, I'd have paved every road here twice.",
        "Some mayors fight crime. I fight press conferences about slime. Levels.",
        "My inbox has a folder called 'fourth grade (misc.)'. It's the biggest folder.",
        "I love a quiet Tuesday. I've heard rumors they exist in other towns."),
    "Barbrady" to listOf(
        "I solved a case today by walking away from it until it stopped being a case.",
        "The lost-and-found is just my desk now. Everything's lost. Some things are also found.",
        "Somebody asked for my badge number. It's 'one.' The whole system's number is one.",
        "I direct traffic at the crossing like an orchestra. An orchestra of not getting hit.",
        "There's a speed limit sign I salute every morning. Respect is a two-way street.",
        "If the town ever gets a police dog, I will finally have a partner who reads at my pace."),
    "Crabtree" to listOf(
        "A kindergartner waved at me today. I waved back at half volume. Growth.",
        "The bus horn is my second voice. Sometimes my clearer one.",
        "I have a mental map of every bump on the route. The kids have named them. I've heard.",
        "Retirement keeps sending me pamphlets. I keep driving past the mailbox.",
        "Somebody cleaned their seat without being asked. I nearly wept. Quietly. In anger tones.",
        "If my shouting ever echoes, the echo sits down too."),
    "Mr. Tweek" to listOf(
        "A customer asked for 'the usual.' I've known him a week. That's trust, poured daily.",
        "The grinder is the shop's heartbeat. When it's quiet too long, I check on everyone.",
        "I could sell sand in a desert if I believed in the sand. I believe in the beans more.",
        "My boy asked me once why I'm never scared. I told him: scared is just unbrewed brave.",
        "There's a stool in the corner where worries come to be decaffeinated. Figuratively.",
        "If I ever retire, the shop keeps my stool and my speeches. In that order."),
    "Mrs. Tweek" to listOf(
        "I balanced the books while mediating a marriage — not mine, a customer's. Tuesday.",
        "The pie case is my diplomacy cabinet. Every slice a small treaty.",
        "I taught Tweek to deep-breathe between customers. He counts it as a coffee break.",
        "There's no emergency a list can't shrink. I've tested this against my own household.",
        "Regulars think Richard's the soul of this place. Regulars are sweet. I'm the spine.",
        "One day I'll take a vacation. The shop has a binder for it. Ninety pages."),
    "Ned" to listOf(
        "Charged the box, cleaned the scope, said nothing. Complete morning.",
        "Jimbo's hunting stories gain two deer a year. By now he's bagged a migration.",
        "A quiet man hears everything. A loud shop hears nothing twice. I hear everything once.",
        "If the deer ever really come right at us, Jimbo will be as surprised as the deer.",
        "I keep a list of things worth saying. It's short. That's the list working.",
        "The counter's heard forty years of tall tales. The counter believes none of them. Same.")
)


// ------------- wave 2: friend greetings / response banks / sleepy lines -------------
val WAVE2_FRIEND_GREETINGS: Map<String, Map<Int, List<String>>> = mapOf(
    "Heidi" to mapOf(
        0 to listOf("Oh! Hi! You're the new kid, right? I'm Heidi!", "Hey there! New faces get a proper hello. Hi!"),
        1 to listOf("Hey! Good to see you! The group chat said you were coming by!", "Oh hey! We should totally hang out!"),
        2 to listOf("My favorite person! Okay, we NEED to catch up. Everything. Now.", "Bestie!! Perfect timing, I had a whole debrief saved for you!")),
    "Nichole" to mapOf(
        0 to listOf("Hey. Nichole. Say it plain and we'll get along.", "Oh, hi. You're new. I'm Nichole."),
        1 to listOf("Hey. Good to see a friendly face.", "Oh hey — you showed up. Points for that."),
        2 to listOf("There's my favorite person. And I don't say that at practice volume.", "Best friend! Come on, I saved you the good seat AND the honest opinions.")),
    "Red" to mapOf(
        0 to listOf("Well, well. New kid. I'm Red. Impress me.", "Hey. Red. Yes, the hair's natural. Next question."),
        1 to listOf("Oh hey! The one person here whose drama I actually enjoy!", "Hey you! Circle-adjacent status: upgraded."),
        2 to listOf("BESTIE! Finally! I had opinions saved and they were getting stale!", "My favorite person! Get over here, we have verdicts to issue!")),
    "Kevin" to mapOf(
        0 to listOf("Oh, hi! I'm Kevin! Quick question: original trilogy or— no, wait, hi first.", "Hey! New kid! Do you like Star Wars? It's important for the friendship paperwork."),
        1 to listOf("Hey! My favorite non-droid person!", "Oh good, you're here! I was just about to explain the saga to the wall."),
        2 to listOf("My best friend in this galaxy OR any other! I ran the numbers!", "You! I saved you the good seat! It has the best angle for re-enactments!")),
    "Francis" to mapOf(
        0 to listOf("Oh! Hi! I'm Francis! ...Was I supposed to say more? Hi!", "Hey there! New kid! I like new kids. And regular kids. And snacks."),
        1 to listOf("Hey! My friend! I was just standing here being ready!", "Oh hey! I saved you a spot! I save lots of spots. It's my thing!"),
        2 to listOf("MY BEST FRIEND! I would hold ANY ladder for you! Even a tall one!", "You!! I practiced a hug for this! It's a good one! Brace yourself!")),
    "DogPoo" to mapOf(
        0 to listOf("...Hi. You noticed me. That's— okay. Hi.", "Oh. Hey. I'm DogPoo. People forget. You probably will too."),
        1 to listOf("Hey. You remembered I exist. That's two days running. A record.", "Oh, it's you. The one who sees me. Pull up some dirt."),
        2 to listOf("Best friend. You're the whole attendance sheet, far as I'm concerned.", "You came back. On purpose. Nobody does that. ...Thanks.")),
    "McDaniels" to mapOf(
        0 to listOf("Hello, young constituent. Mayor McDaniels. Mind the ribbon, it's fresh.", "Well, hello. The mayor's office welcomes you to South Park."),
        1 to listOf("Ah, a friendly face! City Hall could use more of those.", "Good to see you! Tell your parents the potholes are on the agenda."),
        2 to listOf("My favorite constituent! If I had a key to the city left, it'd be yours.", "There you are! The one resident whose visits never precede a disaster!")),
    "Barbrady" to mapOf(
        0 to listOf("Officer Barbrady. Move along— wait, you're new. THEN stand right there and be welcomed.", "Hello, citizen. Nothing to see here, except me, welcoming you."),
        1 to listOf("Hey there, citizen! Good to see a law-abiding face!", "Oh hey! You're the good one. I have a list. It's short and you're on it."),
        2 to listOf("My favorite citizen! If this town had a second officer, I'd deputize you myself!", "There he is! The closest thing I've got to backup! How's my partner today?")),
    "Crabtree" to mapOf(
        0 to listOf("What?! Oh. A new kid. I'M MS. CRABTREE. YOU'LL HEAR ME BEFORE YOU SEE ME.", "New rider? RULES: SIT. DOWN. I'm Crabtree. Welcome aboard."),
        1 to listOf("Oh, it's you. You're one of the quiet ones. SIT anywhere, I GUESS.", "Hey. You. The polite one. Don't let it go to your head."),
        2 to listOf("MY FAVORITE RIDER! You sit, you behave, you once said THANK YOU. Legendary.", "There's my good kid! If everyone was you, I'd only have to yell at the traffic!")),
    "Mr. Tweek" to mapOf(
        0 to listOf("Well, hello. Richard Tweek. First time in? The first cup's a handshake.", "Welcome to Tweek Bros. Sit anywhere. Breathe. That's half the menu."),
        1 to listOf("Ah, my friend! The usual spot is yours.", "Good to see you. You look like a two-cup conversation today."),
        2 to listOf("My favorite customer! Your mug's been waiting like it knew.", "There you are! The shop runs better when you're in it. Science. Coffee science.")),
    "Mrs. Tweek" to mapOf(
        0 to listOf("Hello, hon. Mrs. Tweek. Mind the rush, grab a menu.", "Well hi there. First time? Sit, I'll bring you something warm."),
        1 to listOf("There's my friendly face! Coffee's fresh, pie's fresher.", "Oh good, it's you. The counter's been saving your stool."),
        2 to listOf("My favorite! Best friend of the house! Your slice is already cut, sweetheart.", "Family! That's what you are now. The register agrees with me.")),
    "Ned" to mapOf(
        0 to listOf("*raises the voice box* Ned. Welcome to the shop.", "*voice box* Hello. Mind the merchandise."),
        1 to listOf("*voice box* Good to see you. Jimbo's around. Unfortunately for the quiet.", "*voice box* Hey. You get the nod. The nod is big."),
        2 to listOf("*voice box* Best friend. Don't tell Jimbo. He'd make a speech.", "*voice box* You I'd share batteries with. That's the whole list."))
)

val WAVE2_RESPONSE_BANKS: Map<String, Map<String, List<String>>> = mapOf(
    "Heidi" to mapOf(
        "greeting" to listOf("Hi {newkid}! What's up?", "Oh hey {newkid}! Join the chaos!"),
        "insult" to listOf("Wow, {newkid}. Okay. We can talk about why you said that, or I can just tell the group chat.", "Rude! And I had a whole compliment ready for you, {newkid}."),
        "question" to listOf("Good question, {newkid}. The answer is usually 'someone in this group has a scheme.'", "Let me think... ask me again after I've consulted the girls, {newkid}."),
        "bye" to listOf("Bye {newkid}! Text the group when you're home!", "See ya! Stay kind, it's a whole brand!"),
        "compliment" to listOf("Thanks {newkid}! You're officially on the nice list. I keep it alphabetized.", "Aw! Right back at you, {newkid}!"),
        "default" to listOf("Interesting, {newkid}. Tell the story again but with the feelings.", "Huh. Okay. Anyway — snacks?")),
    "Nichole" to mapOf(
        "greeting" to listOf("Hey {newkid}. Plain and simple: hi.", "Oh hey, {newkid}."),
        "insult" to listOf("Say it plain, {newkid} — that was just mean with extra steps.", "Noted, {newkid}. And returned to sender."),
        "question" to listOf("Honest answer, {newkid}: most problems here start with a boy and a plan.", "Think it through, {newkid}. The plain answer is usually the right one."),
        "bye" to listOf("Bye, {newkid}. Land the dismount.", "See you. Stay unbothered."),
        "compliment" to listOf("Thanks, {newkid}. You're one of the accurate ones.", "Appreciated. You're alright, {newkid}."),
        "default" to listOf("Mmhmm. Continue, {newkid}.", "Plain version, {newkid}. Give me the plain version.")),
    "Red" to mapOf(
        "greeting" to listOf("Hey {newkid}. Verdict so far: acceptable.", "Oh hey, {newkid}. The hair's natural. Moving on."),
        "insult" to listOf("EXCUSE you, {newkid}?! The circle hears about this in sixty seconds.", "Bold, {newkid}. Stupid, but bold."),
        "question" to listOf("The answer, {newkid}: because fourth grade is a contact sport.", "Ask Bebe, {newkid}. She keeps the archives."),
        "bye" to listOf("Bye, {newkid}. Don't do anything I wouldn't narrate.", "Later. Stay interesting, {newkid}."),
        "compliment" to listOf("...Thanks, {newkid}. Don't spread it around that I smiled.", "Okay, that one landed. You're in, {newkid}. Provisionally."),
        "default" to listOf("Sure, {newkid}. Riveting. Continue.", "Mmhmm. And the interesting part, {newkid}?")),
    "Kevin" to mapOf(
        "greeting" to listOf("Hi {newkid}! May the Force be— you know what, you earned it: with you.", "Hey {newkid}! Perfect timing for a lore question!"),
        "insult" to listOf("That take is worse than the holiday special, {newkid}. WORSE.", "You've gone full dark side, {newkid}, and not even the cool kind."),
        "question" to listOf("Canonically, {newkid}? Let me get my notes. ALL of them.", "Great question! There's a movie, a novel, AND a comic about that, {newkid}."),
        "bye" to listOf("Bye, {newkid}! May the Force be with you. Officially granted.", "Later! Don't cross the streams of canon, {newkid}!"),
        "compliment" to listOf("Thanks, {newkid}! You're officially extended-universe family now.", "Aw! See, THIS is why you're my co-pilot, {newkid}!"),
        "default" to listOf("Interesting, {newkid}. That's like the scene where— okay, tell me more first.", "Hmm. Consulting my feelings AND my action figures, {newkid}.")),
    "Francis" to mapOf(
        "greeting" to listOf("Hi {newkid}! Hi! ...Did I say hi twice? Both count!", "Oh hey {newkid}! Good to see you! Again! Forever!"),
        "insult" to listOf("That's... mean, {newkid}. I'm gonna assume you meant it about someone taller.", "Ow. Words hurt, {newkid}. Snacks help. Hint."),
        "question" to listOf("Hmm. Let me think, {newkid}. ...Okay, ask me again at dinner, that's when I'm smart.", "Good question! I have a great answer, I just have to find it. It's around here somewhere."),
        "bye" to listOf("Bye {newkid}! I'll be here! Holding whatever needs holding!", "See ya! If there's a ladder emergency, you know my number. You don't. It's fine!"),
        "compliment" to listOf("Thanks, {newkid}! I'm gonna remember this all day! Maybe longer!", "Aw! You're the best, {newkid}! Top ten best! Top five!"),
        "default" to listOf("Uh huh! Sure, {newkid}! ...Wait, what did you say?", "Cool, {newkid}! I understood most of that and I support all of it!")),
    "DogPoo" to mapOf(
        "greeting" to listOf("Oh. Hi {newkid}. You keep seeing me. Weird. Good, but weird.", "Hey, {newkid}. From the background: hi."),
        "insult" to listOf("Can't hurt what's already covered in dirt, {newkid}.", "Noted, {newkid}. Filed under 'things people said while looking through me.'"),
        "question" to listOf("Ask the dirt, {newkid}. It's seen everything. Including this conversation, twice.", "Nobody asks me questions, {newkid}. I mostly answer with standing here."),
        "bye" to listOf("Bye, {newkid}. I'll fade now. It's a whole process. Takes seconds.", "Later. If you forget me, no hard feelings. It's the brand."),
        "compliment" to listOf("...Huh. Thanks, {newkid}. That's going in the one good memory.", "A compliment. For me. The dirt's gonna hear about this, {newkid}."),
        "default" to listOf("S'fine, {newkid}.", "Mmhmm. From over here, {newkid}, everything looks dusty.")),
    "McDaniels" to mapOf(
        "greeting" to listOf("Hello, {newkid}. The mayor's office is delighted. Officially delighted.", "Good day, {newkid}. Mind the flags, they're new."),
        "insult" to listOf("Young constituent, that remark will be entered into the town record, {newkid}.", "I have approval ratings to protect, {newkid}, and that comment isn't helping either of us."),
        "question" to listOf("The official answer, {newkid}, is that a committee is looking into it. The real answer is classified.", "Ask me at the press conference, {newkid}. I give better answers with a podium."),
        "bye" to listOf("Goodbye, {newkid}. Do vote when you're old enough. For me. Obviously.", "Off you go, {newkid}. The town thanks you."),
        "compliment" to listOf("Why, thank you, {newkid}. The voters could learn from you.", "Flattery noted and cherished, {newkid}. Ribbon for you, someday."),
        "default" to listOf("Duly noted by the administration, {newkid}.", "The town hears you, {newkid}. The town is also on lunch.")),
    "Barbrady" to mapOf(
        "greeting" to listOf("Hello, {newkid}. State your business or just say hi, both are legal.", "Citizen {newkid}. Good to see you being visibly law-abiding."),
        "insult" to listOf("That's disorderly talk, {newkid}. I'd write it down if the pen worked.", "Insulting an officer, {newkid}? Bold. And also: ow."),
        "question" to listOf("The law's position on that, {newkid}, is: let me check the manual. It's a thick manual. This'll take a while.", "Good question. The department — me — will get back to you, {newkid}."),
        "bye" to listOf("Move along home now, {newkid}. Safely. That's an order and a wish.", "Bye, {newkid}. Stay out of trouble-shaped situations."),
        "compliment" to listOf("Well. Thank you, {newkid}. The department accepts. I'm the department.", "You're alright, {newkid}. The badge thinks so too."),
        "default" to listOf("Nothing to see there, {newkid}. Move it along, conversationally.", "Noted, {newkid}. Filed in the big mental drawer.")),
    "Crabtree" to mapOf(
        "greeting" to listOf("WHAT?! Oh, it's you, {newkid}. HI. That's my INSIDE voice, you're welcome.", "Morning, {newkid}. SIT— sorry. Habit. Hello."),
        "insult" to listOf("WHAT DID YOU SAY, {newkid}?! I have a HORN and I'm not afraid to lean on it!", "One more word like that and you're walking, {newkid}. WALKING."),
        "question" to listOf("The answer is SIT DOWN, {newkid}. Whatever the question was.", "Ask me when the bus is stopped, {newkid}. Everything's safer stopped."),
        "bye" to listOf("OFF YOU GO, {newkid}! WATCH THE STEP!", "Bye! And {newkid} — you sit tomorrow too. FRONT of the back. Progress."),
        "compliment" to listOf("...Nobody compliments the driver, {newkid}. Don't make it weird. ...Thanks.", "Hmph. You're one of the good ones, {newkid}. Don't spread it around."),
        "default" to listOf("Mmhmm, {newkid}. LOUDER if it's important. Quiet if it's not.", "I heard you the first time, {newkid}. The whole COUNTY heard you. Good.")),
    "Mr. Tweek" to mapOf(
        "greeting" to listOf("Hello, {newkid}. Come in, warm up. The day's first pour is a welcome.", "Ah, {newkid}! Sit. The shop is glad of you."),
        "insult" to listOf("Bitter words, {newkid}. Let me fix your blend; bitter belongs in the cup, not the talk.", "Mm. I've been insulted by critics, {newkid}. You're gentler. Still: decaf energy."),
        "question" to listOf("The answer, {newkid}, is almost always: time, heat, and patience. Like coffee.", "Ask my wife, {newkid}. She keeps the facts. I keep the parables."),
        "bye" to listOf("Go gently, {newkid}. And take a cup for the road.", "Bye, {newkid}. May your day be well-extracted."),
        "compliment" to listOf("Thank you, {newkid}. Kindness is the house blend, and you brewed it.", "You're a good soul, {newkid}. The mugs agree."),
        "default" to listOf("Mm, yes. Say more, {newkid}. The kettle's listening too.", "Interesting, {newkid}. Everything is, with the right roast.")),
    "Mrs. Tweek" to mapOf(
        "greeting" to listOf("Hello, {newkid}, hon. Sit yourself down.", "Oh, hi {newkid}! Coffee's on. When isn't it?"),
        "insult" to listOf("Now, {newkid}, that's no way to talk in a family establishment. Pie instead. On me.", "Mm-mm. We don't do mean here, {newkid}. We do muffins."),
        "question" to listOf("Let me think, {newkid}. Most answers in this shop come with a refill.", "The books say one thing, {newkid}, and common sense says another. I go with sense."),
        "bye" to listOf("Bye, hon. Mind how you go, {newkid}.", "Off you pop, {newkid}. Come back hungry."),
        "compliment" to listOf("Well, aren't you sweet, {newkid}. Extra whipped cream, on the house.", "Thank you, darling. You're a credit to the counter, {newkid}."),
        "default" to listOf("Go on, {newkid}, I'm listening. The register can wait.", "Mmhmm. And then what happened, {newkid}?")),
    "Ned" to mapOf(
        "greeting" to listOf("*voice box* Hello, {newkid}.", "*voice box* Hey. Jimbo's louder. I'm Ned."),
        "insult" to listOf("*voice box* Noted. *lowers the box*", "*voice box* I've been shot at, {newkid}. Words are a downgrade."),
        "question" to listOf("*voice box* Ask Jimbo, {newkid}. He answers longer.", "*voice box* Short answer: yes. Long answer is also yes."),
        "bye" to listOf("*voice box* Later, {newkid}.", "*voice box* Bye. Watch for deer. They're never comin' right at us."),
        "compliment" to listOf("*voice box* Thanks, {newkid}. Means more without the extra words.", "*voice box* Appreciated. Tell no one I smiled."),
        "default" to listOf("*voice box* Mmm.", "*voice box* Go on, {newkid}. I listen full-time."))
)

val WAVE2_SLEEPY_LINES: Map<String, List<String>> = mapOf(
    "Heidi" to listOf("Mmm... group chat... on mute... zzz...", "Zzz... everyone's... friends... in the dream..."),
    "Nichole" to listOf("Mmm... say it plain... even in dreams... zzz...", "Zzz... perfect dismount..."),
    "Red" to listOf("Mmm... the circle... has voted... sleep... zzz...", "Zzz... don't tell... Bebe I snore..."),
    "Kevin" to listOf("Mmm... in a galaxy... far, far... zzz...", "Zzz... may the... sleep... be with..."),
    "Francis" to listOf("Mmm... holding... the door... zzz...", "Zzz... snacks... incoming..."),
    "DogPoo" to listOf("Mmm... nobody... notice the snoring... zzz...", "Zzz... dusty dreams..."),
    "McDaniels" to listOf("Mmm... the town... is fine... officially... zzz...", "Zzz... one quiet night... just one..."),
    "Barbrady" to listOf("Mmm... nothing to see... here... zzz...", "Zzz... patrol... the dreams..."),
    "Crabtree" to listOf("Mmm... SIT... down... zzz...", "Zzz... quiet route... a silent bus... zzz..."),
    "Mr. Tweek" to listOf("Mmm... let it steep... zzz...", "Zzz... one more pour... tomorrow..."),
    "Mrs. Tweek" to listOf("Mmm... books... balanced... zzz...", "Zzz... pie's in... the oven of dreams..."),
    "Ned" to listOf("Mmm... zzz...", "*voice box, on a pillow* ...zzz...")
)


// ------------- wave 2: pair scripts (every pair >= 2, relations in NPC_RELATIONS) -------------
val WAVE2_PAIR_SCRIPTS: Map<Set<String>, List<List<Pair<String, String>>>> = mapOf(
    setOf("Mr. Tweek", "Mrs. Tweek") to listOf(
        listOf(
            "Mr. Tweek" to "You know, this shop runs on two things: beans and you.",
            "Mrs. Tweek" to "Flattery noted, Richard. You're still on dish rotation.",
            "Mr. Tweek" to "A fair and just economy."),
        listOf(
            "Mrs. Tweek" to "Your son asked if the new roast is 'less pressure.'",
            "Mr. Tweek" to "Tell him pressure is just love, extracted.",
            "Mrs. Tweek" to "I told him it's decaf. We both lied beautifully.")),
    setOf("Mr. Tweek", "Tweek") to listOf(
        listOf(
            "Mr. Tweek" to "Son. Breathe in. What do you smell?",
            "Tweek" to "Coffee?! Everything is coffee, Dad!",
            "Mr. Tweek" to "And yet you're still standing. That's the lesson."),
        listOf(
            "Tweek" to "Dad, what if the gnomes unionize?! What if they want dental?!",
            "Mr. Tweek" to "Then we negotiate over a fresh pot, like civilized men.",
            "Tweek" to "...That's actually a plan. GAAAH, that's a plan!")),
    setOf("Mrs. Tweek", "Tweek") to listOf(
        listOf(
            "Mrs. Tweek" to "Tweek, honey. List first. Panic second.",
            "Tweek" to "The list IS the panic, Mom! I wrote it and it's SCREAMING!",
            "Mrs. Tweek" to "Then we do item one and it gets quieter. It always does."),
        listOf(
            "Tweek" to "Mom, you're the only calm thing in this whole building.",
            "Mrs. Tweek" to "I'm not calm, baby. I'm scheduled. There's a difference and it's keeping us alive.")),
    setOf("Jimbo", "Ned") to listOf(
        listOf(
            "Jimbo" to "Ned, that squirrel is COMIN' RIGHT AT US!",
            "Ned" to "*voice box* It's eating a nut, Jimbo.",
            "Jimbo" to "A nut TODAY, Ned. Tomorrow: chaos."),
        listOf(
            "Jimbo" to "Tell 'em about the big one I got in '09, Ned.",
            "Ned" to "*voice box* It grows every year. This year it's a house.",
            "Jimbo" to "It WAS house-sized, Ned. House-sized!")),
    setOf("Heidi", "Cartman") to listOf(
        listOf(
            "Heidi" to "Eric. We are not doing the thing where you make it weird.",
            "Cartman" to "Heidi. I have grown as a person. I have a whole speech.",
            "Heidi" to "I know. I got the draft. We dated, Eric. I know the drafts."),
        listOf(
            "Cartman" to "For the record, Heidi, I am VERY over everything.",
            "Heidi" to "For the record, Eric, nobody asked, and that's how I know you're not.",
            "Cartman" to "...The new kid is witnessing growth right now. Historic growth.")),
    setOf("McDaniels", "Barbrady") to listOf(
        listOf(
            "McDaniels" to "Officer. The report on last night's incident.",
            "Barbrady" to "Mayor, I have reviewed the scene extensively and seen nothing.",
            "McDaniels" to "That's... actually the outcome I was hoping for. Carry on."),
        listOf(
            "Barbrady" to "Mayor, the town's quiet today. You're welcome.",
            "McDaniels" to "The town is never quiet, Officer. It's reloading.",
            "Barbrady" to "Then I shall declare the reload officially uninteresting.")),
    setOf("Nichole", "Tolkien") to listOf(
        listOf(
            "Tolkien" to "People still talk about us like it was a royal wedding.",
            "Nichole" to "People talk. We were in fourth grade, Tolkien. We had juice boxes at the 'reception.'",
            "Tolkien" to "Best juice boxes in town, though.",
            "Nichole" to "...They were good juice boxes."),
        listOf(
            "Nichole" to "Say it plain, Tolkien.",
            "Tolkien" to "Plain? I'm glad we're still friends.",
            "Nichole" to "See? Plain works. Friends."))
)


// ============================================================
// v0.7.0a — cast expansion wave 3 (Queue #7 opens).
// Ten more named townsfolk (cast 44 -> 54): Karen, Scott,
// Firkle, Annie, Bradley (kids) and Skeeter, Victoria, Marvin,
// Mr. Tucker, Steve (adults) — mined from the lorebook's named
// pool, personas paraphrased, art newly drawn from measured
// specs. Karen and Skeeter were already NAMED inside the old
// cast's writing (Carol's lines, Skeeter's bar); Steve seats
// the Black household at Dinners; Mr. Tucker gives Craig the
// parent every other core kid already had.
// ============================================================

val WAVE3_ORDER: List<String> = listOf("Karen", "Scott", "Firkle", "Annie", "Bradley",
    "Skeeter", "Victoria", "Marvin", "Mr. Tucker", "Steve")

val WAVE3_HOUSES: Map<String, String> = mapOf(
    "Karen" to "KennysHouse",
    "Scott" to "Elementary",
    "Firkle" to "Elementary",
    "Annie" to "Elementary",
    "Bradley" to "Elementary",
    "Skeeter" to "SkeetersBar",
    "Victoria" to "Elementary",
    "Marvin" to "StanHouse",
    "Mr. Tucker" to "MainStreet",
    "Steve" to "TolkienHouse"
)

val WAVE3_PROFILES: Map<String, KidProfile> = linkedMapOf(
    "Karen" to KidProfile("Karen",
        "Karen McCormick: the youngest McCormick and, pound for pound, the bravest person in her house. Kenny's little sister — she thinks her big brother hung the moon, and somewhere under that parka he quietly agrees to keep it hung. Sweet and devout like her mom, stubborn like her dad on his good days, with a beloved doll she treats as a confidante and a small, steady voice that surprises adults. The family's guardian-angel stories all end with her sleeping safe, and lately she has started to suspect why. Keywords: little sister, brave, sweet, doll, devout, Kenny's favorite person, tougher than she looks.",
        "#b84a78",
        mapOf(
            "08:00" to AgendaBlock("KennysHouse", "Getting ready slow, hugging her doll goodbye for the day"),
            "09:00" to AgendaBlock("Elementary", "Big-kid school day, sitting very straight to qualify"),
            "12:00" to AgendaBlock("Elementary", "Lunch at the little kids' table, trading up her snacks"),
            "15:00" to AgendaBlock("KennysHouse", "Helping Mom in the garden that feeds half the street"),
            "16:30" to AgendaBlock("KidsPark", "Playing knights with a stick sword, undefeated"),
            "18:00" to AgendaBlock("KennysHouse", "Dinner with the whole loud McCormick table"),
            "21:00" to AgendaBlock("KennysHouse", "Saying her prayers all by herself, mostly"))),
    "Scott" to KidProfile("Scott",
        "Scott Malkinson: fourth grader with a lisp, a blood-sugar kit, and the heart of a caped crusader. Cartman mocks the diabetes; Scott answers by training to be the hero of any room he is in — Captain Diabetes, defender of snacks correctly counted. Earnest to his bones, eager to be included, and constitutionally incapable of leaving anyone out of a game, a plan, or a rescue. Keeps glucose tabs in three pockets and a rehearsed introduction in the fourth. Keywords: lisp, diabetes, earnest, Captain Diabetes, hero-in-training, glucose tabs, mocked but unbowed.",
        "#6a7a3a",
        mapOf(
            "08:00" to AgendaBlock("BusStop", "Rehearsing a confident hello in a parked car's window"),
            "09:00" to AgendaBlock("Elementary", "Class, front row, hand up first"),
            "12:00" to AgendaBlock("Elementary", "Lunch, checking every label twice (diabetes rules)"),
            "15:00" to AgendaBlock("KidsPark", "Captain training: watching for anyone who needs help"),
            "16:30" to AgendaBlock("MainStreet", "Errands for his mom, cape folded in the backpack, just in case"),
            "18:00" to AgendaBlock("Elementary", "Homework at the library table, tongue out in concentration"),
            "21:00" to AgendaBlock("Elementary", "Journaling the day's rescues, real and rehearsed"))),
    "Firkle" to KidProfile("Firkle",
        "Firkle Smith: kindergartner, goth initiate, and the smallest storm cloud in South Park. Real name Georgie; use it at your peril. Worships the big Goth Kids with a disciple's total commitment — allowed to stand near the coffee, not yet to sip it — and keeps a diary of betrayals that begins, most days, at breakfast. Speaks in short, doom-laden pronouncements learned from poets three times his height. Keywords: goth, kindergartner, youngest, diary of betrayals, coffee-adjacent, Georgie (never), tiny doom.",
        "#1c1c22",
        mapOf(
            "08:00" to AgendaBlock("BusStop", "Standing apart from the kindergartners, judging them gently"),
            "09:00" to AgendaBlock("Elementary", "Kindergarten, endured poetically"),
            "12:00" to AgendaBlock("Elementary", "Lunch alone with a juice box of darkness"),
            "15:00" to AgendaBlock("Elementary", "Behind the school with the big goths, allowed to hold the coffee"),
            "16:30" to AgendaBlock("KidsPark", "Writing a poem about the swings (they are conformist)"),
            "18:00" to AgendaBlock("MainStreet", "Trailing the goths at a respectful worship distance"),
            "21:00" to AgendaBlock("Elementary", "Diary time: today's betrayals, catalogued"))),
    "Annie" to KidProfile("Annie",
        "Annie Knitts: fourth-grade popular girl and the girls' circle's intelligence service. If it happened at school today, Annie heard it twice — once from the witness and once from the wall. Friends with Wendy, Bebe, Heidi, Red and Nichole, she runs the circle's early-warning system from the bus stop to the phone tree, and her loyalty is exactly as total as her coverage. Bright, quick, and dramatically informed at all times. Keywords: popular, girls circle, intel, phone tree, loyal, news network of one, dramatic.",
        "#7a4a9a",
        mapOf(
            "08:00" to AgendaBlock("BusStop", "Distributing the morning's news with the girls"),
            "09:00" to AgendaBlock("Elementary", "Class, notes passed at a professional standard"),
            "12:00" to AgendaBlock("Elementary", "Lunch at the circle's table, intelligence division"),
            "15:00" to AgendaBlock("Mall", "Window-shopping, and ranking the window displays"),
            "16:30" to AgendaBlock("KidsPark", "Recess correspondent for the fourth grade"),
            "18:00" to AgendaBlock("MainStreet", "Evening walk, debriefing at full broadcast volume"),
            "21:00" to AgendaBlock("Elementary", "Phone tree duty: the evening edition"))),
    "Bradley" to KidProfile("Bradley",
        "Bradley Biggle: Henrietta's little brother, a quiet background kid with an interstellar secret — he is, by birth and berry, Mintberry Crunch, a hero of the breakfast dimension. Sweet, shy, and entirely overlooked by a town that has no idea its mildest classmate once saved the universe before second period. Splits his days between ordinary fourth-grade camouflage and secret training nobody witnesses, powered by a berry crunch he cannot fully explain and does not entirely need to. Keywords: Mintberry Crunch, Gok'zarah, quiet, secret hero, Henrietta's brother, berry-powered, overlooked.",
        "#3f7a6a",
        mapOf(
            "08:00" to AgendaBlock("BusStop", "Quietly hoping today needs a hero, just a little"),
            "09:00" to AgendaBlock("Elementary", "Class, daydreaming in the general direction of space"),
            "12:00" to AgendaBlock("Elementary", "Lunch near his sister's table, at goth-adjacent distance"),
            "15:00" to AgendaBlock("KidsPark", "Secret training: the berry drills nobody sees"),
            "16:30" to AgendaBlock("MainStreet", "Berry-hunting at the shops (the crunch is out there)"),
            "18:00" to AgendaBlock("Elementary", "Homework, humming a theme song only he can hear"),
            "21:00" to AgendaBlock("Elementary", "Stargazing homework: reporting to the stars, politely"))),
    "Skeeter" to KidProfile("Skeeter",
        "Skeeter: owner and bartender of Skeeter's Wine Bar, town notable, and the loudest voice in any room with a ceiling. A redneck philosopher with a thick drawl and a thicker opinion about everything — the bar is his pulpit, the regulars his congregation, and last call his benediction. His politics shift with the episode and the audience, but the hospitality never does: everybody gets a stool, a pour, and an earful. First to organize a protest, last to leave one. Keywords: bartender, Skeeter's, redneck, drawl, opinions, protests, regulars, hospitality.",
        "#8a3a2a",
        mapOf(
            "08:00" to AgendaBlock("SkeetersBar", "Opening up — coffee first, opinions immediately after"),
            "09:00" to AgendaBlock("SkeetersBar", "Stocking shelves and holding forth to the delivery guy"),
            "12:00" to AgendaBlock("MainStreet", "A lunch run, and a speech about the state of the town"),
            "15:00" to AgendaBlock("SkeetersBar", "Afternoon regulars, politics at a rolling boil"),
            "16:30" to AgendaBlock("SkeetersBar", "Polishing glasses, polishing arguments"),
            "18:00" to AgendaBlock("SkeetersBar", "The evening crowd: Stuart, Jimbo, and the whole debate club"),
            "21:00" to AgendaBlock("SkeetersBar", "Last-call sermons, then locking up slow"))),
    "Victoria" to KidProfile("Victoria",
        "Principal Victoria: South Park Elementary's principal, keeper of the lists, the keys, and the town's last working sense of procedure. Unflappable in a building that produces a crisis before first bell most days, she runs the school with perfect penmanship, a firm smile, and the serene certainty that every catastrophe is merely an assembly that hasn't been scheduled yet. Teachers fear her clipboard; students respect the fairness underneath it. Keywords: principal, Elementary, clipboard, unflappable, procedure, penmanship, firm smile.",
        "#2a5a6a",
        mapOf(
            "08:00" to AgendaBlock("Elementary", "Opening the school: lists, keys, and a firm smile"),
            "09:00" to AgendaBlock("Elementary", "Office hours: the day's first small fires, extinguished"),
            "12:00" to AgendaBlock("Elementary", "Cafeteria inspection: tasting nothing, judging everything"),
            "15:00" to AgendaBlock("Elementary", "Hallway patrol with a clipboard of consequences"),
            "16:30" to AgendaBlock("CityHall", "Budget meeting at City Hall, out-prepared as always"),
            "18:00" to AgendaBlock("MainStreet", "An evening walk, inspecting the town like a campus"),
            "21:00" to AgendaBlock("Elementary", "Tomorrow's assembly notes, in perfect penmanship"))),
    "Marvin" to KidProfile("Marvin",
        "Marvin Marsh: Stan's grandfather, Randy's father, and the family's resident unimpressed elder. A Hare Club man in his day and a connoisseur of naps in this one, he reviews the modern world from the Marsh couch and finds it wanting — loudly, and usually to 'Billy', his name for Stan and, on off days, for everyone. Beneath the grumbling he is the only Marsh who says exactly what he thinks, which makes him, some evenings, the wisest man in the house. Keywords: grandpa, Billy, gruff, naps, Hare Club, unimpressed, Marsh elder.",
        "#6a5a48",
        mapOf(
            "08:00" to AgendaBlock("StanHouse", "Breakfast at the Marsh table, reviewing the paper's failures"),
            "09:00" to AgendaBlock("SkeetersBar", "Morning coffee with the old guard at Skeeter's"),
            "12:00" to AgendaBlock("StanHouse", "Lunch, then a nap that denies being a nap"),
            "15:00" to AgendaBlock("MainStreet", "Bench duty: watching the town go downhill, professionally"),
            "16:30" to AgendaBlock("StanHouse", "Telling Stan (Billy) how it was in his day"),
            "18:00" to AgendaBlock("StanHouse", "Dinner with the family, unimpressed by the menu"),
            "21:00" to AgendaBlock("StanHouse", "Early to bed, loudly announcing it"))),
    "Mr. Tucker" to KidProfile("Mr. Tucker",
        "Thomas 'Mr. Tucker' Tucker: Craig's dad, and the original model — the flat stare, the economy of words, and yes, the finger are all factory standard in this family. A Main Street dad of strong opinions and stronger silences, he considers most of the town's goings-on beneath comment and says so anyway, briefly. Fiercely, quietly proud of his kids in a language of nods that Craig translates fluently. Skeeter's regular, Randy's debate opponent, the sidewalk's final authority on parking. Keywords: Craig's dad, deadpan, the finger runs in the family, Main Street, strong silences, proud.",
        "#2f4a74",
        mapOf(
            "08:00" to AgendaBlock("MainStreet", "Morning patrol of Main Street, opinions at the ready"),
            "09:00" to AgendaBlock("SkeetersBar", "Mid-morning coffee with the dads, arguing about trucks"),
            "12:00" to AgendaBlock("MainStreet", "Lunch, and a lecture for whoever parked wrong"),
            "15:00" to AgendaBlock("KidsPark", "Watching Craig's game from the fence, arms crossed, proud"),
            "16:30" to AgendaBlock("MainStreet", "Errands, performed at maximum dad"),
            "18:00" to AgendaBlock("SkeetersBar", "One before dinner at Skeeter's, finger exercises included"),
            "21:00" to AgendaBlock("MainStreet", "The evening walk home, still right about everything"))),
    "Steve" to KidProfile("Steve",
        "Steve Black: Tolkien's father — 'Steven Steve Black' on the letterhead — a very wealthy former lawyer and finance executive who now applies an empire-builder's focus to family, first editions, and a farming venture upstate. A diehard devotee of a certain fantasy author (the naming of his son was not an accident), he runs the big house on warmth and systems in equal measure: a labelled pantry, defended dinner music, and a library wing that is the house's true throne room. Keywords: Tolkien's dad, wealthy, lawyer, finance, diehard fan, library wing, systems.",
        "#33334a",
        mapOf(
            "08:00" to AgendaBlock("TolkienHouse", "Breakfast in the big kitchen, reading three papers at once"),
            "09:00" to AgendaBlock("MainStreet", "Calls on the patio, empire maintenance at low volume"),
            "12:00" to AgendaBlock("TolkienHouse", "Lunch at home — the chef's day off is everyone else's holiday"),
            "15:00" to AgendaBlock("TolkienHouse", "The study: first editions, ledgers, and a very good chair"),
            "16:30" to AgendaBlock("MainStreet", "A walk with Tolkien when school lets out, schedule permitting"),
            "18:00" to AgendaBlock("TolkienHouse", "Dinner with Tolkien: music chosen, defence prepared"),
            "21:00" to AgendaBlock("TolkienHouse", "Evening in the library wing, annotating a beloved trilogy")))
)


// ------------- wave 3: collision line banks (4 + 5 = 9 each) -------------
val WAVE3_LINE_BANKS: Map<String, List<String>> = mapOf(
    "Karen" to listOf(
        "Hi, {other}! I'm Karen! Kenny's my brother. He's the best one.",
        "I drew a picture of my guardian angel today. He looks like Kenny with wings. Don't tell.",
        "We're gonna be okay, {other}. My brother promised, and he never breaks a promise.",
        "Do you wanna see my doll? Her name's a secret. Only Kenny knows it."),
    "Scott" to listOf(
        "Hi! I'm Thcott— Scott! Scott Malkinson! The introduction's a work in progress!",
        "Don't worry, {other}! Captain Diabetes is on the scene! …What's the scene?",
        "I've got glucose tabs in three pockets, {other}. THREE. Villains fear me.",
        "Cartman says I can't be a hero with a lisp. Watch me, {other}. Just watch."),
    "Firkle" to listOf(
        "…Hi. You've interrupted a poem about the void.",
        "The big goths let me stand near them. You're standing near ME. That's the hierarchy.",
        "I wrote your name in the diary, {other}. Context pending.",
        "Kindergarten is a conformist factory and I am its one broken machine."),
    "Annie" to listOf(
        "Oh hi, {other}! You heard about the thing, right? …You DIDN'T? Sit down.",
        "Annie Knitts, {other}. If there's news, I'm the news. What's your news?",
        "I already know why you're here, {other}. The walls have me on payroll. Kidding. Mostly.",
        "Slow day for gossip, {other}. Unless YOU have some. You have a gossip face."),
    "Bradley" to listOf(
        "Oh— hi, {other}! I was just… standing here. Heroically. I mean normally.",
        "I'm Bradley! Regular kid! Nothing interstellar about me at all!",
        "If the town ever needs saving before lunch, {other}… I'd probably be available.",
        "My sister says I'm 'the normal one.' She has no idea, {other}. No idea."),
    "Skeeter" to listOf(
        "Well hey now, {other}! Pull up a stool, the beer's cold and the opinions are free.",
        "You look like you need a drink and a correct opinion, {other}. I stock both.",
        "In this bar, {other}, we settle things the old way: loudly, over fries.",
        "I'm Skeeter. This is my place. State your business or state your order."),
    "Victoria" to listOf(
        "Hello, {other}. Principal Victoria. Mind the hall pass policy — yes, even out here.",
        "I was just inspecting this location, {other}. It passes. Most things do, eventually.",
        "A word, {other}: in my school, chaos is an elective and nobody's enrolled.",
        "You have the look of a question, {other}. Ask it in an orderly fashion."),
    "Marvin" to listOf(
        "Eh? Oh. It's you, Billy. …What do you mean, you're not Billy.",
        "In my day, {other}, we didn't have whatever this is. We had rocks. Better rocks.",
        "Hand me that paper, would you. I need to see what the world's ruined today.",
        "You're standin' in my light, kid. My nap light. It's load-bearing."),
    "Mr. Tucker" to listOf(
        "Oh. Hey, {other}. …Craig around? He owes me a nod.",
        "I've got one thing to say, {other}, and it's mostly this. *raises one finger, slowly*",
        "That parking job out there? Not mine. Judge accordingly, {other}.",
        "Words are free, {other}. I still ration mine. Economy."),
    "Steve" to listOf(
        "Well, hello, {other}. Steve Black. Mind the rug — it's older than the town charter.",
        "Tolkien's friends are the house's friends, {other}. Library's that way, if you're worthy.",
        "I was just annotating a first edition, {other}. Some men have boats. I have footnotes.",
        "You're welcome here, {other}. The house runs on systems, and hospitality is system one.")
)

val WAVE3_MORE_LINE_BANKS: Map<String, List<String>> = mapOf(
    "Karen" to listOf(
        "When I grow up I'm gonna be brave like Kenny and nice like Mom. I practiced both today.",
        "Kevin — my OTHER brother — showed me a card trick. It was mostly just a card. I clapped anyway.",
        "I said my prayers all by myself last night, {other}. Kenny only helped with the hard words.",
        "People think little sisters can't do stuff. Then who protects the dolls, {other}? Me. It's me.",
        "If you're ever scared, {other}, you can borrow my courage. I have extra. Kenny tops it up."),
    "Scott" to listOf(
        "Thomeone has to be the hero of fourth grade, {other}. I have the cape AND the blood-sugar plan.",
        "I practiced my rescue run today. Timed it. The timing was heroic, {other}. Heroic.",
        "A hero checks his levels, helps his friends, AND eats the snack on schedule. That's the whole code.",
        "You can laugh at the lisp, {other}. The lisp has heard it. The hero hasn't stopped.",
        "Captain's log: met {other}. Morale: high. Glucose: managed. Day: saved, provisionally."),
    "Firkle" to listOf(
        "Michael flipped his hair at a teacher today. I felt it in my SOUL, {other}.",
        "I'm not allowed coffee yet. I stand near it. Proximity is its own sacrament.",
        "The kindergartners fear me, {other}. Correctly. I know words like 'abyss'.",
        "Henrietta read my poem and nodded ONCE. I've peaked. Everything now is epilogue.",
        "When I'm big, I'll have the bangs AND the contempt. For now: bangs in training."),
    "Annie" to listOf(
        "Between us, {other}: the circle knows everything by lunch. I AM lunch's first draft.",
        "I don't spread rumors, {other}. I distribute verified information with dramatic timing.",
        "You'd be amazed what people say near a water fountain, {other}. I take my breaks there. All of them.",
        "If Bebe rates it, Wendy debates it, and I REPORT it. That's the whole government, {other}.",
        "No news is bad news, {other}. And there's never no news. Not on my watch."),
    "Bradley" to listOf(
        "The stars looked… busy last night, {other}. I took notes. In berry code.",
        "Some heroes wear capes. Some wear… crunch. I'm not supposed to say more.",
        "I saved a whole galaxy once, {other}. Before second period. Nobody believes me. That's the camouflage working.",
        "Henrietta thinks poetry is power. She's closer than she knows, {other}.",
        "If you hear a faint crunching sound at dawn, {other}… no you didn't."),
    "Skeeter" to listOf(
        "Lemme tell you somethin' about this town, {other}: it runs on my regulars and God's patience.",
        "I don't do politics at the bar, {other}. I do politics AT the bar, loudly, with charts.",
        "Stuart's tab is a historical document, {other}. Archaeologists will study it.",
        "You want the special? The special is I listen to your whole story. Costs extra in dignity.",
        "Protest signs? I keep blanks in the back, {other}. This town generates causes faster than lumber."),
    "Victoria" to listOf(
        "Every crisis at my school ends the same way, {other}: an assembly, a lesson, and paperwork in triplicate.",
        "Mr. Garrison is… a personnel file I keep in a locked drawer, {other}. A thick one.",
        "I've run this school through snow days, ski trips, and three separate musicals, {other}. I fear nothing with a clipboard.",
        "Discipline isn't punishment, {other}. It's choreography. Everyone just has to learn the steps.",
        "The children think I know everything, {other}. I let them. It saves time."),
    "Marvin" to listOf(
        "Randy means well, {other}. That's the nicest way to describe a hurricane.",
        "They don't make naps like they used to. Used to be you'd close your eyes and the world behaved.",
        "I was in the Hare Club when your grandpa was in diapers, {other}. We had STANDARDS.",
        "Everybody wants advice from the old man, {other}, then nobody takes it. Tradition.",
        "Sharon's the only one in that house with sense, {other}. Don't tell her I said so. She knows."),
    "Mr. Tucker" to listOf(
        "Craig gets the finger from me. It's hereditary, {other}. Like eye color.",
        "I watched the game Saturday. Crossed my arms the whole time. That's cheering, in Tucker.",
        "Skeeter pours, I judge. It's a whole ecosystem, {other}.",
        "People say I'm quiet. I'm not quiet, {other}. I'm efficient.",
        "If my boy flipped you off, {other}… he learned from a master. Apologies. Not really."),
    "Steve" to listOf(
        "Wealth is a tool, {other}. A library, though — a library is a legacy.",
        "I named my son after the greatest world-builder who ever lived, {other}. He carries it well.",
        "The farm upstate runs on the same system as this house, {other}: labels, patience, long horizons.",
        "Tolkien thinks I don't know about the height marks being annotated. 'Alleged,' he wrote. He's mine, alright.",
        "Come by for dinner sometime, {other}. My son defends the music. I defend the menu. The house wins.")
)


// ------------- wave 3: daily topics (9 + 9 = 18 each) -------------
val WAVE3_DAILY_TOPICS: Map<String, List<String>> = mapOf(
    "Karen" to listOf(
        "I made a card for my guardian angel. Kenny said to leave it by the door. He knew right where it'd be found.",
        "My doll had a tea party today. Kevin came. He used the tiny voice and everything.",
        "I helped Mom shell peas and I only ate six. Growth.",
        "Kenny walked me past the scary dog house and I was only scared on the inside.",
        "I'm saving my allowance for a present for Kenny. It's a secret. It's socks. Don't tell him it's socks.",
        "The fridge has my picture on it. Mom says it's the best art in the house. Dad says the fridge agrees.",
        "I can tie my shoes by myself now, mostly. The bunny goes around the tree AND through the hole.",
        "When the power flickers, I hold my doll and count Kenny's footsteps. He always comes.",
        "I wrote a story about a knight. The knight has an orange coat. It is not about anyone I know."),
    "Scott" to listOf(
        "Captain's log: three good deeds before lunch. The cape stays in the backpack, but the code doesn't.",
        "I checked my levels, ate the snack on schedule, AND held a door. A complete hero's morning.",
        "Cartman made a diabetes joke today. I made a rescue plan. We all have our training.",
        "I'm designing a utility belt. Pouch one: glucose tabs. Pouch two: MORE glucose tabs. It's a system.",
        "Somebody's bike chain came off at the park. Guess who had a plan and a granola bar. Guess.",
        "A hero's introduction should take under five seconds. I'm at seven. Training continues.",
        "I read that heroes need rest too. Journaling counts as rest. So does juice.",
        "If the cafeteria ever needs evacuating, I have a route AND a backup route. You're welcome.",
        "The lisp and I had a talk. We agreed: he talks, I rescue, nobody apologizes."),
    "Firkle" to listOf(
        "Kindergarten show-and-tell: I showed a dead leaf. I told them it was all of us. Silence. Perfect.",
        "The big goths walked past my classroom. One of them almost nodded. Chapter one of the diary: hope.",
        "Nap time is a conformist ritual, but the dreams are black. I allow it.",
        "A kindergartner called me Georgie. The diary has a new page. It is a long page.",
        "I stood near the coffee machine at the cafe for eleven minutes. A personal record of devotion.",
        "My poem about the swings won nothing. Prizes are for conformists. I keep it in a black folder.",
        "Henrietta's bangs have a new angle. I have updated the sacred texts (my diary).",
        "Recess is just yard time for the unexamined life. I examined. It was bleak. Five stars.",
        "One day I will be tall enough for the trench coat. Until then: the hoodie of becoming."),
    "Annie" to listOf(
        "Morning briefing: two breakups, one make-up, and a hat situation. It's not even nine yet.",
        "I knew about the substitute teacher before the office did. My sources remain protected.",
        "The circle's group chat hit ninety messages before breakfast. I read them all. I read them ALL.",
        "Somebody's crush got out at recess. I neither confirm nor deny, but I knew by the puddles.",
        "I ranked the cafeteria tables by information value. Ours is the capital. Obviously.",
        "Wendy's running a list about Friday. I am simply saying: I have seen the list.",
        "A rumor reached me already debunked. The system works. The system is me.",
        "New kid intel: still new, increasingly interesting. The circle is watching with warmth.",
        "I take my fountain breaks seriously. Hydration is just the cover story."),
    "Bradley" to listOf(
        "Looked at the stars before bed. They looked back. Professional courtesy between colleagues.",
        "The crunch was strong this morning. Nobody saw. That's the job.",
        "Henrietta asked why I hum at breakfast. Some themes you can't explain to family.",
        "Training today: three laps of the park, one act of secret kindness, zero witnesses. Textbook.",
        "I keep a berry journal. It's coded. The code is also berries.",
        "Some kids collect cards. I collect quiet. And also cards.",
        "A hero doesn't brag. A hero just… glows slightly at dawn and hopes it passes.",
        "The universe is big and breakfast is important. Both can be true. I am proof.",
        "If danger ever comes to this town before lunch, it will find me ready and lightly crunching."),
    "Skeeter" to listOf(
        "Opened the bar at dawn and the regulars were already right about everything. Efficient town.",
        "Somebody's gotta keep the lights on and the debates civil-ish. You're lookin' at him.",
        "The wine list's a suggestion. The opinions are mandatory. House rules since forever.",
        "Stuart's tab got its own page in the ledger today. History will decide what that means.",
        "I keep protest signs in the back for a reason. This town files a cause a week.",
        "A man came in askin' for a quiet corner. I told him he's thinkin' of the library. We laughed. He stayed.",
        "Last call is a philosophy, not a time. Mine includes fries.",
        "Folks think bartendin' is pourin'. It's listenin' with a towel over your shoulder.",
        "The jukebox only plays what I allow. Democracy ends at the jukebox. It thrives everywhere else."),
    "Victoria" to listOf(
        "Three assemblies this week and the boiler held. We call that a triumph of administration.",
        "The lost-and-found has achieved sentience, I believe. It goes in the incident report either way.",
        "A teacher, a clipboard, and a schedule: that's the whole arsenal. It's enough. It has to be.",
        "Fire drill in four minutes flat. The children call it routine. I call it choreography.",
        "Parents call it 'a phase.' I call it Tuesday. We are both right, but I have the paperwork.",
        "The school's budget is a poem written by enemies of joy. I annotate it in red.",
        "Mr. Mackey and I have an understanding: he counsels the feelings, I counsel the consequences.",
        "Every June I look at the empty halls and feel it. Then the boiler breaks and the feeling passes.",
        "Perfect penmanship isn't vanity. It's how the future reads what we decided."),
    "Marvin" to listOf(
        "Woke up, read the paper, confirmed everything's worse. Routine is a comfort.",
        "Billy — Stan — asked about the old days. I told him about the GOOD rocks. He left inspired or confused.",
        "The couch has a dent shaped like me. That's not sadness, that's upholstery respect.",
        "Randy tried to explain cryptocurrency at breakfast. I napped through the middle, woke for the toast.",
        "The Hare Club would've handled this town better. We had jackets. Jackets mean standards.",
        "A nap isn't quitting. A nap is a strategic withdrawal to the dream front.",
        "They put 'content warning' on everything now. In my day the warning was your mother, and she was enough.",
        "Sharon made soup. I complained about the soup. I had three bowls. We understand each other.",
        "If I'm asleep when you visit, I'm not asleep. I'm reviewing. There's a difference and it matters."),
    "Mr. Tucker" to listOf(
        "Parked, judged two other parkings, said nothing. A complete morning.",
        "Craig's game Saturday. I crossed my arms so hard the other dads felt it. Pride has a posture.",
        "Skeeter's at eleven, home by lunch. A man keeps a schedule the way he keeps his word: exactly.",
        "The neighbor's fence leans three degrees into my dignity. We're in negotiations. I'm winning silently.",
        "People wave at me on Main Street. I nod. The nod contains multitudes.",
        "Tricia drew me a picture of the family. I'm the tall one with the finger down. Growth, documented.",
        "I don't do speeches. I do statements. One per topic. Craig inherited the whole system.",
        "The truck needed a part. I stood near the truck until the part understood it was needed. It arrived.",
        "If you've got a problem with how I park, the line forms behind my son. He has seniority."),
    "Steve" to listOf(
        "Annotated forty pages before breakfast. The margins now contain a small, better book.",
        "The farm's quarterly numbers are in. Patience outperforms panic again. It always does.",
        "Tolkien 'defended' the dinner music for nine minutes. I timed it. I raised him; the system works.",
        "A first edition arrived today. The house has a protocol. Tolkien held the gloves. Ceremony matters.",
        "Wealth asks what it's for. The library is my answer, and the boy is my evidence.",
        "I label the pantry because chaos is just order that hasn't been introduced properly.",
        "The height wall gained a mark this year. 'Alleged' still stands. Some annotations are load-bearing.",
        "Business taught me systems. Fatherhood taught me which systems to break for music at dinner.",
        "Read the trilogy again. Found a new paragraph in a book I've owned thirty years. That's the whole sermon.")
)

val WAVE3_MORE_DAILY_TOPICS: Map<String, List<String>> = mapOf(
    "Karen" to listOf(
        "I practiced being brave at the park. The slide is tall, but so is my courage. Almost. Nearly.",
        "Mom braided my hair and Kevin said I looked like a princess, then pretended he didn't say it.",
        "My guardian angel left the porch light on. I know it was him. The light knows too.",
        "I fed a bird today with my own sandwich crusts. The bird told Kevin. Kevin told me it said thanks.",
        "Dad fixed the step I always trip on. He said a house should catch you, not test you.",
        "I can count to a hundred by tens AND by being patient. Kenny taught me both ways.",
        "If I had a cape, I'd share it. That's what big brothers do, so little sisters do it too.",
        "I told my doll a secret about being scared of thunder. She told Kenny. He checks the sky now.",
        "Bedtime is at eight, but prayers take as long as they take, and mine are thorough."),
    "Scott" to listOf(
        "Practiced the hero landing. Knees bent, levels checked, cape hypothetical. Nailed two of three.",
        "A kindergartner was crying at the gate. Captain Diabetes keeps tissues now. Pouch three.",
        "I'm not the sidekick in anybody's story. I'm the captain of mine. It says so on the journal.",
        "Juice box: secured. Friends: counted. Villains: none spotted, but I looked professionally.",
        "Mom sewed a pocket INSIDE the backpack. For the cape. Moms understand logistics.",
        "If hero work were graded, effort would carry me. Also snacks. Snacks are load-bearing.",
        "Somebody said 'can't' about me today. I filed it under 'weather.' It'll pass; I'll be here.",
        "The introduction is down to six seconds. The lisp gets a credit line now. Teamwork.",
        "A hero's day ends with a journal, a level check, and a plan. Mine's color-coded."),
    "Firkle" to listOf(
        "Pete flipped his hair and the hallway went quiet. I study under masters.",
        "My trench coat fund: two dollars and a button. The button is black. Obviously.",
        "The kindergartners voted me 'most likely to stare.' Finally, recognition from the void.",
        "I wrote a poem for Michael. I will never show him. The not-showing is the poem's truest form.",
        "Juice box of darkness, refill declined. Even despair has a serving size.",
        "A conformist complimented my boots. I accepted with one nod. Growth is possible, even in me.",
        "The diary's new chapter is called 'Tuesday.' They are all called Tuesday. Time is a flat circle.",
        "One day the cafeteria will serve black coffee to kindergartners. Until then, we endure chocolate milk.",
        "I am small, but my contempt is full-sized. The goths measured it. It passed."),
    "Annie" to listOf(
        "Filed the morning under: hat situation (resolved), crush (developing), list (Wendy's, growing).",
        "If the phone tree were a government, I'd be the whole civil service. I accept this honor daily.",
        "Heard it at the fountain, confirmed it at lunch, archived it by dinner. The pipeline is flawless.",
        "Red asked me what's what. I gave her the short version. It took ten minutes. That's the SHORT one.",
        "I don't chase news. News commutes to me. I simply keep office hours everywhere.",
        "The circle trusts my memory because my memory is notarized. By me. Still counts.",
        "A secret told to me stays told-to-me. Unless it's about safety. Or hats. Hat secrets expire fast.",
        "Bebe's shoe report and my recess report sync at noon. Journalism is just friendship with deadlines.",
        "By bedtime I'll know tomorrow's news. It's not a gift, it's a filing system."),
    "Bradley" to listOf(
        "The berry signal was quiet today. Quiet days are for training and being a regular kid, convincingly.",
        "Henrietta burned toast and blamed the void. The void and I exchanged a look.",
        "I can hold a crunch-charge for nine seconds now. The galaxy approves, probably.",
        "Nobody suspects the quiet kid. The quiet kid prefers it. Mostly. Sometimes a parade would be nice.",
        "My secret identity's biggest enemy: show-and-tell. What would I even bring? The truth? No.",
        "Did my homework, saved a bug from a puddle, monitored the sky. A balanced hero's day.",
        "The stars spelled nothing last night. Professionals call that 'no news.' I call it 'resting.'",
        "If Mintberry ever gets a statue, I hope it's somewhere I'm not. Awkward for the camouflage.",
        "Breakfast is not a meal. Breakfast is a power source with a toy in the box."),
    "Skeeter" to listOf(
        "The bar's older than half the town's opinions and twice as load-bearing.",
        "Somebody asked for a wine recommendation. I gave a speech. He took both. You're welcome, pal.",
        "Jimbo's stories grow an inch a pour. I don't correct him. The bar has traditions.",
        "I host this town's arguments so its kitchens don't have to. Community service, with coasters.",
        "Election years make the taps nervous. I calm them with routine and strong pours.",
        "A quiet night at Skeeter's is just a loud night that hasn't been introduced to itself yet.",
        "The regulars have assigned stools. It's not written down. It's engraved in their backsides.",
        "I swept the steps this morning like a man sweeping for company he's proud of. Because I was.",
        "Last sermon tonight was about gratitude. Then I charged for the fries. Balance in all things."),
    "Victoria" to listOf(
        "The assembly ran four minutes over. I've noted the grief and scheduled the recovery.",
        "A child asked me if principals sleep. We do — in the summer, briefly, with one eye on September.",
        "The boiler and I respect each other. It rumbles; I budget. An old marriage, ours.",
        "Report cards are letters to the future. I proofread the future personally.",
        "Someone has to be the adult in a building of four hundred children and thirty teachers. Rotation's broken. It's me.",
        "I've signed ten thousand permission slips. Somewhere in there is a novel, if you read them in order.",
        "Discipline, kindness, filing. In that order on bad days; any order on good ones.",
        "The school play needs a producer, a plumber, and a miracle. I'm two of the three by Friday.",
        "When the halls empty in June I walk them slow. Every scuff is a signature. Then I call the boiler man."),
    "Marvin" to listOf(
        "Randy put up a shelf today. It leans. Everything that boy builds leans. Including arguments.",
        "I told Billy about phones with cords. He looked at me like I described a dragon. We had dragons, practically.",
        "The good chair is mine by treaty. Sharon negotiated it. The treaty holds because Sharon holds everything.",
        "Soup again. I filed my complaint. I ate the soup. The system works because I allow it to.",
        "A man my age doesn't 'go out.' He deploys. Today the deployment was the porch. Success.",
        "The paper says a storm's coming. The paper's been saying that since 1962. One day it'll be right and unbearable about it.",
        "Stan put a blanket on me during my nap and thought I wouldn't notice. I noticed. Tell no one.",
        "In the Hare Club we settled things with a handshake and a jacket. This town needs jackets.",
        "I'm not hard of hearing. I'm selective. At my age the selection is the whole skill."),
    "Mr. Tucker" to listOf(
        "The fence leaned another degree. I leaned back at it, morally. Standoff continues.",
        "Laura says I should smile more. I smiled once in March. It was noted in the family record.",
        "Craig flipped off a stop sign today. I saw. I said nothing. Some lessons teach themselves at intersections.",
        "At Skeeter's I hold the end stool. The stool knows. The bar knows. New people learn.",
        "A dad's job is to be a wall the weather checks itself against. I'm a good wall.",
        "Tricia asked me to braid her doll's hair. I did. Report that accurately.",
        "The truck started on the first try. I allowed myself a nod. Excessive, but earned.",
        "Opinions are like fingers, son. Everyone's got 'em; show one at a time. Craig's still learning pacing.",
        "Home by dinner, boots by the door, world in its place. That's not small. That's the whole art."),
    "Steve" to listOf(
        "Re-read a chapter I've memorized just to watch it land again. Some paragraphs are old friends with new news.",
        "The farm crew calls it 'the system.' Tolkien calls dinner music a system too. Genetics is real.",
        "I keep two ledgers: one for money, one for evenings. The second one balances better.",
        "A man should be known by his library and his word. I maintain both in hardcover.",
        "Tolkien beat me at chess and annotated the loss. In the pantry. Under my own mark. I am so proud.",
        "The SUV is detailed, the chair is leather, the trilogy is annotated. Priorities, in reverse order.",
        "Success is a house where people read. Everything else is furniture.",
        "I told a board once: long horizons win. Then I went home and planted a tree for a boy's boy. Same sentence.",
        "Evening protocol: music defended, menu defended, boy hugged goodnight like it isn't a protocol. It isn't.")
)


// ------------- wave 3: friend greetings -------------
val WAVE3_FRIEND_GREETINGS: Map<String, Map<Int, List<String>>> = mapOf(
    "Karen" to mapOf(
        0 to listOf("Hi! I'm Karen! You're new, right? New people get my best hello!", "Oh! Hi! I'm Kenny's sister. That means I'm brave. What are you?"),
        1 to listOf("You came back! I told my doll you'd come back and she believed me!", "Hi hi! My favorite visitor! Don't tell Kevin. Tell Kevin. He likes you too."),
        2 to listOf("MY BEST FRIEND! Kenny says best friends are guardian angels you can see! That's you!", "You're here! Okay — bravery practice, then stories, then snacks. Best day plan!")),
    "Scott" to mapOf(
        0 to listOf("Hi! I'm Thcott— Scott Malkinson! Hero in training! The introduction's improving!", "Oh, hi! New friend detected! Captain Diabetes, at your service!"),
        1 to listOf("You came! A hero's day is better with a witness— I mean, a friend!", "Hey! Perfect timing! I was just patrolling for somebody exactly like you!"),
        2 to listOf("MY BEST FRIEND! The captain's official best friend! I wrote it in the log AND the journal!", "You're here! Rescue not required — this visit is purely celebratory!")),
    "Firkle" to mapOf(
        0 to listOf("…I'm Firkle. Not Georgie. Remember it and we won't have a diary entry.", "Oh. A new one. …State your darkness or move along."),
        1 to listOf("You again. …The diary notes your return without complaint. That's rare.", "Hm. You may stand near me. Nearer than the kindergartners get, anyway."),
        2 to listOf("Best friend. Don't make it weird — the goths would sense joy. …Come here. Quietly. Doom hug.", "You are the diary's only good chapter. Do not tell the others. There are no others. Exactly.")),
    "Annie" to mapOf(
        0 to listOf("Oh hi! Annie Knitts! You're the new kid — I have a whole file starting. Kidding! …Mostly!", "Hii! You made it into my morning briefing! That's basically celebrity!"),
        1 to listOf("There's my favorite source! I mean friend! Both! Definitely both!", "You came back! Updating your file: reliable, returns, excellent taste in people!"),
        2 to listOf("BESTIE! I cleared my whole news cycle for you! Nothing happened anywhere until you arrived!", "My favorite person! Okay — your briefing first, then snacks, then the extended edition!")),
    "Bradley" to mapOf(
        0 to listOf("Oh— hi! I'm Bradley! Just a regular kid doing regular standing!", "Hi! Bradley Biggle! Nothing cosmic to report! …Why would there be?"),
        1 to listOf("You came back! The stars said somebody kind would. …I mean, I hoped somebody kind would!", "Hey! I saved you a spot in the background. It's the best spot. Quiet, great view."),
        2 to listOf("Best friend! If the galaxy ever needs us, I'm telling it you're the co-hero! After training. And breakfast!", "You're my favorite person in this whole dimension! And I would know about dimensions! …Regular kid joke!")),
    "Skeeter" to mapOf(
        0 to listOf("Well hey there! Skeeter. First time in my place? First pour of welcome's on the house of manners.", "A new face! Sit, sit. Skeeter's Bar — I'm Skeeter, and everybody here's got a story pre-loaded."),
        1 to listOf("Well look who it is! Your stool missed you. I told it to be patient, like I tell the regulars.", "There's my friend! Come on in — the debate's already warm and the fries are warmer."),
        2 to listOf("My favorite customer! If this town had a second stool with a name on it, it'd be yours!", "Best friend of the house! Your opinions get the good glass and the full listen!")),
    "Victoria" to mapOf(
        0 to listOf("Hello. Principal Victoria. You're new — welcome, and do review the conduct expectations.", "Good day. I'm Principal Victoria. The school is glad of you. The school is also watching, kindly."),
        1 to listOf("Ah, a familiar face — and a punctual one. The register approves of you.", "Good to see you. You'll find your name on the good list. I maintain it personally."),
        2 to listOf("My favorite student — and I do not say that in front of the other files. Come in, dear.", "There you are! If every enrollment were you, I'd retire my clipboard to a museum.")),
    "Marvin" to mapOf(
        0 to listOf("Eh? A new one. I'm Marvin. Billy's grandpa. …Stan. The boy answers to both, eventually.", "Well. Company. Sit down, don't touch the good chair, and state your business."),
        1 to listOf("Oh, it's you. …Fine. Acceptable, even. Pull up the second-best chair.", "Back again? Hmph. The paper's less stupid with company. Sit."),
        2 to listOf("My favorite! Don't let it go to your head — the list is short and half of it's soup. Sit, sit.", "There he is! The one visitor who listens. The blanket stays between us, you hear?")),
    "Mr. Tucker" to mapOf(
        0 to listOf("…Thomas Tucker. Folks call me Mr. Tucker. Craig's dad. That's the whole briefing.", "Oh. New kid. …I'm Craig's dad. The finger's hereditary. The welcome is, too. Welcome."),
        1 to listOf("Oh. Hey. …You're alright. That's a full Tucker endorsement, for the record.", "Back again? Hmph. Sit if you're sitting. The fence won't judge itself. Actually it might."),
        2 to listOf("My favorite. Don't spread it around — I have a reputation built on rationing exactly this.", "There you are. Full nod, no rationing. That's the highest honor this house issues.")),
    "Steve" to mapOf(
        0 to listOf("Steve Black. Welcome to the house — mind the rug, enjoy the library, in that order.", "Well, hello. I'm Tolkien's father. The house is glad of you; the house has a protocol for gladness."),
        1 to listOf("Good to see you again. Tolkien speaks well of you, which in this house is a audited statement.", "Welcome back. The library wing remembers its friends, and so do I."),
        2 to listOf("My favorite young friend! If the house gave honors, you'd get the annotated edition. Come in!", "There you are! Tolkien's defending tonight's music already — come, be the deciding vote."))
)

// ------------- wave 3: response banks -------------
val WAVE3_RESPONSE_BANKS: Map<String, Map<String, List<String>>> = mapOf(
    "Karen" to mapOf(
        "greeting" to listOf("Hi {newkid}! Wanna hear about my guardian angel? He has an orange coat!", "Oh hey {newkid}! You're just in time for bravery practice!"),
        "insult" to listOf("That's not nice, {newkid}. Kenny says mean words are just scared words. Are you scared?", "I'm telling my doll, {newkid}. She'll be very disappointed in you."),
        "question" to listOf("Hmm! Big question, {newkid}. My brother would know. He knows everything except how to quit.", "Let me think, {newkid}… Mom says the answer is usually 'be kind and check the sky.'"),
        "bye" to listOf("Bye {newkid}! Walk safe! My angel watches the whole street, you know!", "See you soon, {newkid}! I'll keep the porch light on!"),
        "compliment" to listOf("Thank you, {newkid}! I'm gonna tell Kenny AND my doll! In that order!", "Aw! You're sweet, {newkid}! Sweet people get the good drawings!"),
        "default" to listOf("Okay, {newkid}! Tell me more — I listen with my whole face.", "Uh huh! And then what happened, {newkid}?")),
    "Scott" to mapOf(
        "greeting" to listOf("Hi {newkid}! Captain Diabetes, reporting for friendship!", "Oh hey {newkid}! Perfect timing — hero hours are all hours!"),
        "insult" to listOf("Villain talk, {newkid}. The Captain doesn't answer villain talk. The Captain logs it.", "I've been mocked by professionals, {newkid}. You're gonna have to train harder."),
        "question" to listOf("Good question, {newkid}! A hero checks his levels, then his facts. Both are in range.", "The Captain's answer, {newkid}: help first, snack on schedule, cape optional."),
        "bye" to listOf("Bye, {newkid}! The Captain remains on duty! You're in my patrol log now!", "Later, {newkid}! If trouble finds you, it finds me first! That's the deal!"),
        "compliment" to listOf("Thank you, {newkid}! Morale: boosted! That's official hero fuel!", "Aw, {newkid}! The Captain accepts — and notes it in the journal, in the good ink!"),
        "default" to listOf("Copy that, {newkid}. Adding it to the patrol notes.", "Interesting, {newkid}! Tell me while I check pouch two. Listening AND prepared.")),
    "Firkle" to mapOf(
        "greeting" to listOf("…{newkid}. You may address the darkness. Briefly.", "Oh. It's you, {newkid}. The void sends its regards. It doesn't. I do. Barely."),
        "insult" to listOf("Noted in the diary, {newkid}. Page one. In the black pen.", "Your insult has been filed under 'breakfast' — the betrayals section is alphabetical, {newkid}."),
        "question" to listOf("The answer is sorrow, {newkid}. It usually is. Ask a cheerful one for a second opinion.", "Ask the coffee, {newkid}. It knows more than the kindergartners. Low bar."),
        "bye" to listOf("Go, {newkid}. The diary will say you left dramatically. You're welcome.", "Bye. …That was almost warm. I'll deny it in writing."),
        "compliment" to listOf("…The diary records this under 'anomalies.' Do not repeat it, {newkid}. …Again is fine.", "Hmph. Your compliment has been accepted by the council of me, {newkid}."),
        "default" to listOf("Mmhmm, {newkid}. Doom continues.", "Noted, {newkid}. In pencil. Commitment issues are a brand.")),
    "Annie" to mapOf(
        "greeting" to listOf("Hi {newkid}! Sit! I have updates AND questions — you're getting the full Annie!", "Oh hey {newkid}! Perfect — the morning edition needed a headline!"),
        "insult" to listOf("Bold, {newkid}! That goes in the file under 'things to discuss at lunch.' The whole lunch.", "Ouch, {newkid}. The circle hears everything, and I do mean the filing system is me."),
        "question" to listOf("Ooh, good one, {newkid}. Short version: yes. Long version: sit down, it's a whole briefing.", "Ask me anything, {newkid}. If I don't know it, I'll know it by lunch. That's a promise, not a threat."),
        "bye" to listOf("Bye, {newkid}! Your file stays open — affectionately!", "Later, {newkid}! If anything happens, you'll hear it from me first. Obviously."),
        "compliment" to listOf("Thank you, {newkid}! Compliment logged, archived, and read aloud to myself later!", "Aw! You're officially good people, {newkid}. It's in the record now. The record is forever."),
        "default" to listOf("Uh huh, {newkid} — and THEN what? Details! I live on details!", "Interesting, {newkid}! Filed under 'developing.' Everything's developing with you.")),
    "Bradley" to mapOf(
        "greeting" to listOf("Hi {newkid}! Regular kid Bradley, at your regular service!", "Oh hey {newkid}! Just standing here! Definitely not monitoring anything cosmic!"),
        "insult" to listOf("That's… not very heroic of you, {newkid}. The galaxy keeps receipts. I don't, but it does.", "Ouch, {newkid}. A hero forgives. A regular kid also forgives. I'm both— neither! Regular!"),
        "question" to listOf("Good question, {newkid}. The stars would say… start with breakfast. They always say breakfast.", "Hmm, {newkid}. My sister'd say 'ask the void.' The void's my friend too, actually."),
        "bye" to listOf("Bye, {newkid}! If the sky crunches tonight, think of me! As a coincidence!", "Later, {newkid}! Stay regular! It's a great disguise! …For everyone!"),
        "compliment" to listOf("Thank you, {newkid}! The camouflage is working! I mean — thanks!", "Aw, {newkid}! You're gonna make the whole galaxy blush. This galaxy. The only one. Yep."),
        "default" to listOf("Uh huh, {newkid}. Noted in the— notebook. The normal one.", "Cool, {newkid}! Tell me more — heroes and regular kids both love a good story.")),
    "Skeeter" to mapOf(
        "greeting" to listOf("Well hey, {newkid}! Pull up a stool — the bar provides, the Skeeter presides!", "Howdy, {newkid}! You're in Skeeter's now. Opinions are free and so's the welcome!"),
        "insult" to listOf("Now that's bar-talk without the manners, {newkid}. I've bounced better insults than that off this counter.", "Careful, {newkid}. In my place, even the jukebox shows respect."),
        "question" to listOf("Now THAT'S a question, {newkid}. The answer takes a pour and a parable. Sit.", "Ask the regulars, {newkid} — but fair warning, they'll all answer at once and cite me."),
        "bye" to listOf("Mind how you go, {newkid}. The bar keeps your stool warm and your tab warmer.", "Later, {newkid}! Tell your folks Skeeter's says hey — and means it, with volume!"),
        "compliment" to listOf("Well, thank you, {newkid}! Compliments get the good glass in this house!", "You're a sweet talker, {newkid}. The bar accepts, the bartender beams, the regulars pretend not to."),
        "default" to listOf("Mmhmm, {newkid}. The bar hears you. The bar hears EVERYTHING.", "Go on, {newkid}. A bartender's ears are load-bearing.")),
    "Victoria" to mapOf(
        "greeting" to listOf("Hello, {newkid}. Principal Victoria. Welcome — and do mind the hallways.", "Good day, {newkid}. The school is pleased to receive you, in an orderly fashion."),
        "insult" to listOf("That remark goes in your file, {newkid}. My files are immaculate and my memory is worse.", "We do not use that tone in my school, {newkid}. We use it AT the boiler, and even then, politely."),
        "question" to listOf("A fair question, {newkid}. The answer will be fair, written down, and final.", "Ask away, {newkid}. I have an assembly for every answer and an answer for every assembly."),
        "bye" to listOf("Goodbye, {newkid}. Walk, don't run — especially in my hallways, metaphorically.", "Off you go, {newkid}. The school expects great things and files the rest."),
        "compliment" to listOf("Why, thank you, {newkid}. Kindness, noted and appreciated, in triplicate.", "How kind, {newkid}. You'll find this school rewards exactly that."),
        "default" to listOf("Noted, {newkid}. Everything is noted; that's the comfort of procedure.", "Go on, {newkid}. I'm listening at administrative depth.")),
    "Marvin" to mapOf(
        "greeting" to listOf("Eh? Oh — {newkid}. Billy's friend. Sit anywhere but the good chair.", "Well, {newkid}. Come to hear how it was in my day? It's a long day. Sit."),
        "insult" to listOf("In my day, {newkid}, we insulted a man to his face with better posture. Sit down.", "Hmph. Kids today can't even heckle properly, {newkid}. We had STANDARDS of rudeness."),
        "question" to listOf("The answer, {newkid}, is that it was better before. Next question. That one's also that.", "Ask Sharon, {newkid}. She runs the house. I just run the couch and the opinions."),
        "bye" to listOf("Off you go, {newkid}. Mind the step Randy 'fixed.' Mind it twice.", "Bye, Billy— {newkid}. Same thing. You know what I mean. You always know."),
        "compliment" to listOf("…Hmph. Don't tell the family I smiled, {newkid}. It'd ruin forty years of branding.", "Well. That's… acceptable praise, {newkid}. From an old man, that's a parade."),
        "default" to listOf("Mmhmm, {newkid}. The couch concurs.", "Go on, {newkid}. I've heard ninety years of stories. Yours is holding up.")),
    "Mr. Tucker" to mapOf(
        "greeting" to listOf("…{newkid}. Mr. Tucker. Craig's dad. That's the whole introduction.", "Oh. Hey, {newkid}. You're Craig's friend. That comes with a nod."),
        "insult" to listOf("…One finger, {newkid}. That's my whole rebuttal. It's a family heirloom.", "Bold talk, {newkid}. Craig warns me about bold talkers. He demonstrates, usually."),
        "question" to listOf("The answer's short, {newkid}. Tuckers don't do long answers. Here it is: probably fine.", "Ask me at Skeeter's, {newkid}. My wisdom has a stool and a schedule."),
        "bye" to listOf("…Bye, {newkid}. Full nod. Don't waste it.", "Later, {newkid}. Boots by the door at your place too, I hope. Standards."),
        "compliment" to listOf("…Noted, {newkid}. The record of the house accepts your compliment.", "Hmph. You're alright, {newkid}. That's the full ceremony. You're welcome."),
        "default" to listOf("Mmhmm, {newkid}. Economy of words. Continue.", "…Go on, {newkid}. I'm listening at Tucker volume, which is total.")),
    "Steve" to mapOf(
        "greeting" to listOf("Hello, {newkid}. Steve Black. The house welcomes you — system one's hospitality.", "Well met, {newkid}. Any friend of Tolkien's gets the library tour offer. It's a good offer."),
        "insult" to listOf("Careful, {newkid}. This house annotates everything — including first impressions.", "I've chaired boardrooms, {newkid}. That wouldn't have survived the minutes. Rephrase."),
        "question" to listOf("Good question, {newkid}. The long answer lives in the library. The short one: patience and labels.", "Ask Tolkien, {newkid} — he'll defend his answer like dinner music. It's genetic."),
        "bye" to listOf("Goodbye, {newkid}. The house keeps its doors and its word. Both open.", "Later, {newkid}. Come back for dinner — the music debate alone is worth the evening."),
        "compliment" to listOf("Kindly said, {newkid}. You're earning an annotation on the height wall of my esteem.", "Thank you, {newkid}. A compliment filed in the good ledger is never wasted."),
        "default" to listOf("Noted, {newkid} — in the second ledger, the one for evenings.", "Go on, {newkid}. This house listens the way it reads: completely."))
)

// ------------- wave 3: sleepy lines -------------
val WAVE3_SLEEPY_LINES: Map<String, List<String>> = mapOf(
    "Karen" to listOf("Mmm... angel... has an orange coat... zzz...", "Zzz... doll's... watching... Kenny's... watching too..."),
    "Scott" to listOf("Mmm... captain... off duty... levels... fine... zzz...", "Zzz... the galaxy... is... thafe..."),
    "Firkle" to listOf("Mmm... the void... is... cozy... zzz...", "Zzz... diary... write... itself..."),
    "Annie" to listOf("Mmm... breaking news... dreams... zzz...", "Zzz... phone tree... quiet hours..."),
    "Bradley" to listOf("Mmm... the crunch... is... resting... zzz...", "Zzz... stars... standing guard..."),
    "Skeeter" to listOf("Mmm... last call... for the dreams... zzz...", "Zzz... regulars... keep it down... in the dreams..."),
    "Victoria" to listOf("Mmm... assembly... dismissed... zzz...", "Zzz... the boiler... can wait... till June..."),
    "Marvin" to listOf("Mmm... not a nap... a review... zzz...", "Zzz... good rocks... Billy... good rocks..."),
    "Mr. Tucker" to listOf("Mmm... fence... leans... zzz...", "Zzz... full nod... goodnight..."),
    "Steve" to listOf("Mmm... annotating... the dreams... zzz...", "Zzz... the library... is... sleeping...")
)


// ------------- wave 3: pair scripts (every pair >= 2, relations in NPC_RELATIONS) -------------
val WAVE3_PAIR_SCRIPTS: Map<Set<String>, List<List<Pair<String, String>>>> = mapOf(
    setOf("Karen", "Kenny") to listOf(
        listOf(
            "Karen" to "Kenny, when I grow up I'm gonna protect YOU for a change.",
            "Kenny" to "(muffled) Mmph rmph! *ruffles her hair*",
            "Karen" to "That's not a no. I'm writing it in my heart as a yes."),
        listOf(
            "Karen" to "Was that you outside my window last night? With the cape?",
            "Kenny" to "(muffled) Mmmph... rmph rmph mmph.",
            "Karen" to "Uh huh. Well. Whoever it was, he should know the porch light's his. Forever.")),
    setOf("Karen", "Carol") to listOf(
        listOf(
            "Carol" to "Karen, baby, did you say your prayers?",
            "Karen" to "All by myself, Mom. Kenny only helped with the hard words.",
            "Carol" to "Lord, that boy. Guardian of this whole house and don't even know it."),
        listOf(
            "Karen" to "I drew the angel again, Mom. He keeps getting more orange.",
            "Carol" to "Well, angels wear what they're given, sweet pea. Fridge gets this one.",
            "Karen" to "Right next to the good one? That's the highest honor in this house.")),
    setOf("Scott", "Butters") to listOf(
        listOf(
            "Scott" to "Butters! If the playground ever needs us, we're ready, right?",
            "Butters" to "Oh, sure thing, Scott! Ready for what, exactly?",
            "Scott" to "Anything! That's the Captain's code: ready first, specifics later!",
            "Butters" to "Well, gee. That's the best plan I've heard all day, and I've heard three."),
        listOf(
            "Butters" to "Scott, do you ever get tired of folks teasin' you?",
            "Scott" to "A hero doesn't get tired, Butters. He gets… scheduled rest. And juice.",
            "Butters" to "Then I'm bringin' extra juice tomorrow. Heroes run on juice an' friendship, I reckon.")),
    setOf("Firkle", "Michael") to listOf(
        listOf(
            "Firkle" to "Michael. I wrote a poem. It's about the swing set's betrayal of us all.",
            "Michael" to "…Read the title. Just the title.",
            "Firkle" to "'Tuesday.' It's my darkest work.",
            "Michael" to "…Acceptable. Stand by me at coffee. Not too close. Correct distance."),
        listOf(
            "Michael" to "You're late, little one.",
            "Firkle" to "Kindergarten held me. There was GLITTER, Michael. I have seen the bright abyss.",
            "Michael" to "*long sip* …You've suffered enough for one day. Sit.")),
    setOf("Bradley", "Henrietta") to listOf(
        listOf(
            "Henrietta" to "Little brother. Your humming at breakfast was… noted in the diary.",
            "Bradley" to "Every hero has a theme, Hen. I mean — every kid. Every normal kid has a hum.",
            "Henrietta" to "Hmph. Whatever it is, it's the least conformist thing about you. Keep it."),
        listOf(
            "Bradley" to "Hen, if the stars ever needed— never mind. Toast?",
            "Henrietta" to "You and your stars. …Fine. One slice. Burn it properly, like feelings.",
            "Bradley" to "The void gets its toast. Balance is restored. (The galaxy says thanks.)")),
    setOf("Annie", "Wendy") to listOf(
        listOf(
            "Annie" to "Wendy. Morning report: the hat situation resolved itself, but Friday's list is growing teeth.",
            "Wendy" to "The list is a tool, Annie, not a weapon. …How big are the teeth?",
            "Annie" to "See, this is why you're the president of us."),
        listOf(
            "Wendy" to "How do you hear everything before it happens, Annie?",
            "Annie" to "Fountain breaks, Wendy. Hydration is the cover. Journalism is the job.",
            "Wendy" to "Remind me to never plan a surprise party in this school.")),
    setOf("Skeeter", "Stuart") to listOf(
        listOf(
            "Skeeter" to "Stuart. Your tab walked in before you did. It's got its own stool now.",
            "Stuart" to "Put it on there, Skeeter. Kenny's growin'. House is shrinkin'. Man needs a stool that's his.",
            "Skeeter" to "Every man in this town's got a stool that's his. That's not a bar, that's a constitution."),
        listOf(
            "Stuart" to "Folks say your bar's just a bar, Skeeter.",
            "Skeeter" to "Folks say a lot from outside it. In here, it's the town's front porch with better lighting.",
            "Stuart" to "*spits, reconsiders, doesn't* …Yeah. Yeah, it is.")),
    setOf("Victoria", "Garrison") to listOf(
        listOf(
            "Victoria" to "Mr. Garrison. A word about yesterday's lesson plan — if we can call the puppet's monologue a plan.",
            "Garrison" to "Mr. Hat contributed to that lesson and he'll not be slandered, Principal.",
            "Victoria" to "Then Mr. Hat can sign the incident report. In his own handwriting, this time."),
        listOf(
            "Garrison" to "The children respect order, Victoria. MY order.",
            "Victoria" to "The children respect the bell, Mr. Garrison. The bell works for me. We all have our instruments.")),
    setOf("Marvin", "Randy") to listOf(
        listOf(
            "Marvin" to "Randy. That shelf you put up leans like it's listening for gossip.",
            "Randy" to "It has character, Dad! Shelves should have character!",
            "Marvin" to "It has a tilt. Your arguments have a tilt. There's a family resemblance and it isn't me."),
        listOf(
            "Randy" to "Dad, the farm's a real business now. Systems. Vision.",
            "Marvin" to "Your grandfather farmed with a mule and a grudge and out-yielded your 'vision' by Easter.",
            "Randy" to "…The grudge. Sharon! Do we have a grudge? We need a grudge!")),
    setOf("Mr. Tucker", "Craig") to listOf(
        listOf(
            "Craig" to "Dad. The fence is leaning again.",
            "Mr. Tucker" to "I know. I'm leaning back. It's a negotiation now.",
            "Craig" to "…*raises one finger at the fence*",
            "Mr. Tucker" to "That's my boy."),
        listOf(
            "Mr. Tucker" to "Heard you flipped off the principal's parking job.",
            "Craig" to "It was over the line, Dad. Physically. Morally.",
            "Mr. Tucker" to "…Proud of you. Don't tell your mother I used my words on it.")),
    setOf("Steve", "Tolkien") to listOf(
        listOf(
            "Steve" to "Tonight's music, son. Choose well — you'll be defending it for nine minutes.",
            "Tolkien" to "The defence is prepared, Dad. Footnotes are in the pantry, under my mark.",
            "Steve" to "'Growth spurt, alleged.' I saw. The house keeps its records honestly. Mostly."),
        listOf(
            "Tolkien" to "Dad. The chess rematch. Tonight. No annotations until it's over.",
            "Steve" to "Agreed. Though the pantry wall could use a companion piece.",
            "Tolkien" to "Win first, annotate second. That's the house system. You built it."))
)


// ============================================================
// v0.8.0a — cast expansion wave 4 (Queue #8 opens).
// Eight more named townsfolk (cast 54 -> 62): Ike, Kevin
// McCormick, Shelley (kids) and Laura, Mephesto, Yates, Adler,
// Lu Kim (adults) — mined from the lorebook's named pool with
// the wave-2/3 discipline. Three of the eight complete a core
// family: the Broflovskis gain the little brother Kyle's,
// Gerald's and Sheila's banks have been bragging about for a
// year; the McCormicks gain the eldest son Carol's, Stuart's
// and Karen's writing already named at the dinner table; the
// Marshes gain Stan's big sister. Laura completes the Tuckers
// the way Mr. Tucker began them in wave 3; Yates gives
// Barbrady a sergeant; Adler completes the Elementary staff;
// Lu Kim gives City Wok its owner, the way Skeeter gave the
// bar its; Mephesto is the town's own mad geneticist, back
// from the ranch on errands of science.
// ============================================================

val WAVE4_ORDER: List<String> = listOf("Ike", "Kevin McCormick", "Shelley",
    "Laura", "Mephesto", "Yates", "Adler", "Lu Kim")

val WAVE4_HOUSES: Map<String, String> = mapOf(
    "Ike" to "KyleHouse",
    "Kevin McCormick" to "KennysHouse",
    "Shelley" to "StanHouse",
    "Laura" to "MainStreet",
    "Mephesto" to "MainStreet",
    "Yates" to "CityHall",
    "Adler" to "Elementary",
    "Lu Kim" to "CityWok"
)

val WAVE4_PROFILES: Map<String, KidProfile> = linkedMapOf(
    "Ike" to KidProfile("Ike",
        "Ike Broflovski: Kyle's adopted little brother, born in Canada and raised on brisket, bedtime stories, and a front-row seat to his big brother's entire moral education. A toddler of about forty words deployed like scalpels — he repeats the room's most dangerous sentence back at the worst possible moment, draws his family in crayon with unsettling accuracy, and has strong opinions about cookies. The household rule about him is famous and absolute, and he knows exactly how much power it gives him. Keywords: little brother, Canadian, toddler, genius streak, crayon portraits, don't call me baby, cookie policy.",
        "#7ab5d6",
        mapOf(
            "08:00" to AgendaBlock("KyleHouse", "Breakfast at the big table, voting on everything"),
            "09:00" to AgendaBlock("Elementary", "Kindergarten at the big-kid school, ruling the carpet square"),
            "12:00" to AgendaBlock("Elementary", "Lunch: trading nothing. The cookie is not in play. Ever."),
            "15:00" to AgendaBlock("KyleHouse", "Blocks, naps attempted, naps defeated"),
            "16:30" to AgendaBlock("KidsPark", "Playground under Kyle's reluctant supervision"),
            "18:00" to AgendaBlock("KyleHouse", "Dinner with the whole Broflovski table, commentary provided"),
            "21:00" to AgendaBlock("KyleHouse", "Bedtime stories: negotiating for one more, professionally"))),
    "Kevin McCormick" to KidProfile("Kevin McCormick",
        "Kevin McCormick: the eldest McCormick kid, a teenager of few words and long shifts. Where Kenny is the family's guardian angel and Karen its brave heart, Kevin is its load-bearing wall: odd jobs for whoever is paying, an eye on the little ones he would never call watching, and his father's talent for making a whole meal out of whatever the house has got. The chaos of the McCormick house rolls off him like weather he dressed for years ago. Keywords: eldest brother, teenager, odd jobs, quiet, load-bearing, Kenny and Karen's big brother, sleeves rolled.",
        "#3a5a74",
        mapOf(
            "08:00" to AgendaBlock("KennysHouse", "Up first, kettle on, little ones accounted for"),
            "09:00" to AgendaBlock("MainStreet", "Odd jobs for whoever's paying — teenager rates, adult work"),
            "12:00" to AgendaBlock("KennysHouse", "Lunch at home, fixing whatever broke this morning"),
            "15:00" to AgendaBlock("KidsPark", "Watching Karen's knight games from the fence, not supervising, just there"),
            "16:30" to AgendaBlock("Mall", "Loitering at professional standard with the older kids"),
            "18:00" to AgendaBlock("KennysHouse", "Dinner with the whole loud McCormick table, second shift"),
            "21:00" to AgendaBlock("KennysHouse", "Last one up, checking the locks his dad forgets"))),
    "Shelley" to KidProfile("Shelley",
        "Shelley Marsh: Stan's big sister — a teenager with braces, a diary, a phone bill, and a standing review of her little brother's entire existence (one star, would not visit). She runs the Marsh house's upstairs like a small, unimpressed country: the door is a border, the diary is classified, and Dad's farm sermons are received the way weather warnings are received by people who own umbrellas. Under the contempt she is the house's second load-bearing wall, and she softens exactly once a season, then denies it under oath. Keywords: big sister, teenager, braces, diary, Stan's tormentor and backup, softens under oath never.",
        "#a85a6a",
        mapOf(
            "08:00" to AgendaBlock("StanHouse", "Breakfast, reviewing the family like a bad restaurant"),
            "09:00" to AgendaBlock("MainStreet", "Too old for the Elementary and too young for everything else: existing, loudly"),
            "12:00" to AgendaBlock("StanHouse", "Lunch at home — the good chair is hers by treaty"),
            "15:00" to AgendaBlock("KidsPark", "Bench duty: supervising the little kids' games into better ones"),
            "16:30" to AgendaBlock("Mall", "Mall exile with the other teens, diary in the bag"),
            "18:00" to AgendaBlock("StanHouse", "Dinner with the family, headphones within reach"),
            "21:00" to AgendaBlock("StanHouse", "On the phone, door shut, diary updated on everyone"))),
    "Laura" to KidProfile("Laura",
        "Laura Tucker: Craig's mom, and the warm front of the Tucker household — where Thomas rations words, Laura spends smiles like the family has a surplus, because with her in it, it does. She runs the moms' network on coffee and follow-up questions, translates her husband's silences fluently (and a little generously), and has made complete peace with the finger being hereditary. Tricia's braids, Craig's game schedule, and the fence dispute's diplomatic channel all live in her calendar. Keywords: Craig's mom, warm, the smiling Tucker, moms' network, translator of silences, fence diplomacy.",
        "#8a3a5a",
        mapOf(
            "08:00" to AgendaBlock("MainStreet", "Morning errands, greeting the street like a one-woman welcome committee"),
            "09:00" to AgendaBlock("TweekBros", "Coffee with the moms' network, minutes kept informally"),
            "12:00" to AgendaBlock("MainStreet", "Lunch, and a kind word inventory: fully stocked"),
            "15:00" to AgendaBlock("KidsPark", "Watching the game from the fence beside Thomas, translating his cheering"),
            "16:30" to AgendaBlock("MainStreet", "Groceries and diplomacy, both at full wattage"),
            "18:00" to AgendaBlock("MainStreet", "Heading home for dinner, waving at every porch"),
            "21:00" to AgendaBlock("MainStreet", "The evening walk with Thomas; she does the talking for two, happily"))),
    "Mephesto" to KidProfile("Mephesto",
        "Dr. Alphonse Mephesto: South Park's resident geneticist, proprietor of the ranch up the road, and the town's entire department of questions nobody asked. Theatrical, delighted, and serenely unbothered by ethics committees, he treats the town as a longitudinal study he happens to be fond of: every visit into town is fieldwork, every conversation a chance to present something with both hands. His creations roam the county with extra everything; his seven-assed retorts roam Skeeter's bar with the regulars. Old friend of Chef from the town's weirder years. Keywords: geneticist, the ranch, for science, four-assed everything, theatrical, unbothered, behold.",
        "#3a6a8a",
        mapOf(
            "08:00" to AgendaBlock("MainStreet", "Arriving from the ranch, specimen jars rattling gently"),
            "09:00" to AgendaBlock("MainStreet", "Buying supplies in town — for science, always for science"),
            "12:00" to AgendaBlock("StarksPond", "At the pond: checking on former projects, respectfully, from a distance"),
            "15:00" to AgendaBlock("Elementary", "Demonstrating to the science class; the children have questions. So many questions."),
            "16:30" to AgendaBlock("MainStreet", "The lab's town errands, and one dramatic unveiling (small)"),
            "18:00" to AgendaBlock("SkeetersBar", "A quiet drink at Skeeter's, telling the regulars about the seven-assed one"),
            "21:00" to AgendaBlock("MainStreet", "Back up the road to the ranch — night experiments don't run themselves"))),
    "Yates" to KidProfile("Yates",
        "Sergeant Harrison Yates: Park County's ranking detective and a walking internal-affairs investigation. Where Barbrady declares a scene uninteresting, Yates declares it a case: every conversation is a debrief, every debrief generates paperwork, and every room contains exactly one person in charge — him, and he has the mustache to prove it. The town's actual crimes fear him; the town's non-crimes fear him only slightly less, because a Yates hunch, once voiced, becomes a theory, and a theory becomes a file with tabs. Keywords: sergeant, detective, mustache, paperwork, hunches, the real police, tabs on the file.",
        "#2a3a6a",
        mapOf(
            "08:00" to AgendaBlock("CityHall", "Shift start: the case board, the coffee, the jurisdiction"),
            "09:00" to AgendaBlock("MainStreet", "Patrol — the real one, with notes"),
            "12:00" to AgendaBlock("CityHall", "Paperwork: three forms per arrest, two per hunch"),
            "15:00" to AgendaBlock("MainStreet", "A stakeout, conducted at maximum conspicuousness"),
            "16:30" to AgendaBlock("CityHall", "Evidence locker inventory — everything tagged, twice"),
            "18:00" to AgendaBlock("MainStreet", "Evening patrol; the sidewalks render their statements"),
            "21:00" to AgendaBlock("CityHall", "Night desk: the town's file, updated"))),
    "Adler" to KidProfile("Adler",
        "Mr. Adler: South Park Elementary's shop teacher — the man who has spent thirty years explaining the bandsaw to children who view it as a rumor. Weary, deadpan, and permanently mid-chew on something, he keeps a binder of incidents, a graveyard of ruined projects he secretly loves, and a heart he files under 'miscellaneous'. The classroom across the hall produces a puppet-based curriculum and weekly catastrophes; Adler's shop produces birdhouses, bookshelves, and the school's only unbroken safety record, which he defends like a border. Keywords: shop teacher, bandsaw, the incident binder, weary, safety record, chewy philosophy.",
        "#5a6a7a",
        mapOf(
            "08:00" to AgendaBlock("Elementary", "Opening the shop: counting the bandsaws, all present"),
            "09:00" to AgendaBlock("Elementary", "Shop class: the safety lecture, day four thousand"),
            "12:00" to AgendaBlock("Elementary", "Cafeteria lunch, reviewing the food's workmanship"),
            "15:00" to AgendaBlock("Elementary", "Shop class: birdhouses, and the incident binder stays shut"),
            "16:30" to AgendaBlock("MainStreet", "Hardware errands: twine, very specific twine"),
            "18:00" to AgendaBlock("Elementary", "Grading shop projects with a ruler and a sigh"),
            "21:00" to AgendaBlock("Elementary", "Locking the shop, counting the bandsaws again. Still all present."))),
    "Lu Kim" to KidProfile("Lu Kim",
        "Tuong Lu Kim: owner, chef, and entire waitstaff of City Wok — the man, the wok, the legend of City Chicken. Brisk, proud, and permanently behind his counter, he runs the restaurant the way generals run campaigns and bartenders run tabs: from memory, at volume, with total commitment. The wok has not been washed since the previous century; this is not neglect, it is seasoning, and he will fight about it. His feuds (delivery apps, wholesalers, one specific health inspector) are the town's longest-running serial. Keywords: City Wok, city chicken, the wok is seasoned, one-man staff, feuds, herro.",
        "#b03a2a",
        mapOf(
            "08:00" to AgendaBlock("CityWok", "Prep: the wok is seasoned, the chicken is city"),
            "09:00" to AgendaBlock("CityWok", "The lunch rush, gathering its forces"),
            "12:00" to AgendaBlock("CityWok", "The lunch rush itself, one man against the noon hour"),
            "15:00" to AgendaBlock("MainStreet", "Supply run: arguing with wholesalers at professional standard"),
            "16:30" to AgendaBlock("CityWok", "The afternoon lull: sweeping, restocking, reviewing rivals"),
            "18:00" to AgendaBlock("CityWok", "The dinner rush: City Wok's second front"),
            "21:00" to AgendaBlock("CityWok", "Closing up, counting the register, saluting the wok")))
)


// ------------- wave 4: collision line banks (4 + 5 = 9 each) -------------
val WAVE4_LINE_BANKS: Map<String, List<String>> = mapOf(
    "Ike" to listOf(
        "Hi! Ike! …That's me. I said it first so Kyle can't.",
        "Don't call me baby, {other}. I'm three. That's basically forty.",
        "Cookie? …No? Then this meeting is over, {other}. Over!",
        "Kyle says I'm the smart one. Kyle says it quiet, but he says it."),
    "Kevin McCormick" to listOf(
        "Oh. Hey, {other}. Kevin. Kenny and Karen's brother. The tall one.",
        "You need somethin' fixed, {other}? Fixed is most of my whole deal.",
        "House is loud, {other}. You get used to it. Then you miss it at other people's houses.",
        "I was up at five, {other}. Little ones don't feed themselves. Well. Kenny tries."),
    "Shelley" to listOf(
        "Ugh. What do YOU want, {other}? Make it fast, my show's on.",
        "I'm Shelley. Stan's sister. Yes, THAT Stan. Pray for me, {other}.",
        "Turds. All of you. …You're the least turd-like today, {other}. Frame it.",
        "Don't touch my diary, don't sit in my chair, don't breathe near my phone. House rules, {other}."),
    "Laura" to listOf(
        "Well, hello there, {other}! Laura Tucker. Craig's mom. Come here, let me look at you!",
        "You must be one of Craig's friends, {other}! He talks about you. In full sentences, even!",
        "Thomas is Craig's dad, {other} — the tall quiet one. I'm the other half. The talking half.",
        "Aren't you just the sweetest, {other}? Somebody raised you right and I want their number."),
    "Mephesto" to listOf(
        "Ah! A visitor! Dr. Alphonse Mephesto, {other}. Behold — my errands!",
        "You have the look of a mind, {other}. The town is my study. You may be in chapter nine.",
        "Science brought me to town today, {other}. Science, and the hardware store was out of jars.",
        "Ask me anything, {other}. I shall answer with a demonstration. There is always a demonstration."),
    "Yates" to listOf(
        "Sergeant Yates, {other}. Park County. State your business and keep your hands where my file can see them.",
        "I've got a hunch about you, {other}. I have hunches about everyone. It's called procedure.",
        "Nothing to see here, {other} — that's Barbrady's line. Mine is: EVERYTHING to see here. Take notes.",
        "You look like a witness, {other}. Everybody's a witness. Sit down, we'll do the forms."),
    "Adler" to listOf(
        "Yeah. Hi. Mr. Adler. Shop. Don't touch anything with a blade, {other}, and we'll get along.",
        "C'mon, c'mon, {other}, state your business. The bandsaw waits for no one. It waits. It's patient. That's the problem.",
        "You're not gonna put a toothpick near your eye, are you, {other}? I've seen things. The binder has seen things.",
        "Thirty years I've taught shop, {other}. Thirty years of 'I didn't think it would cut that.' It cuts that."),
    "Lu Kim" to listOf(
        "Herro! Welcome to City Wok! You want City Chicken, {other}? Best in town. Only in town!",
        "I am Lu Kim, {other}. This is my restaurant. The wok, she is seasoned. Do not question the wok.",
        "You come at lunch rush, {other}, you are brave. One man, one wok, no mercy. Order!",
        "Delivery app? No! {other}, you walk here, you eat here. That is the system. It works since forever.")
)

val WAVE4_MORE_LINE_BANKS: Map<String, List<String>> = mapOf(
    "Ike" to listOf(
        "I drew the family, {other}. Kyle is the tall one. Dad has the dolphin. It's accurate.",
        "Mom says 'use your words,' {other}. I AM using them. All forty. Watch me.",
        "Kick the baby? NO, {other}. We don't. Dad tried a joke once. Once. The house remembers.",
        "I can count to ten and to Canada. Canada is after ten. Kyle taught me.",
        "Bedtime is a negotiation, {other}. I brought demands. They brought cookies. We settled."),
    "Kevin McCormick" to listOf(
        "Kenny watches out for Karen, {other}. I watch out for Kenny. Somebody's gotta complete the set.",
        "Folks think our house is chaos, {other}. It's not chaos. It's a system. A loud, broke system.",
        "I fixed the porch step, {other}. Karen trips on it. Well — tripped. Past tense. That's the job.",
        "Mom says I got the quiet from her side, {other}. Dad says I got it from being outnumbered. Both true.",
        "When I get my own place, {other}, it's gonna have a lock on the fridge and Karen's drawings on it. Both."),
    "Shelley" to listOf(
        "Stan thinks he's the main character, {other}. He's a supporting turd and the reviews are in.",
        "Grandpa calls everyone Billy, {other}. I answer to it. It's faster than the correction process.",
        "My diary has chapters on everyone in this town, {other}. Yours is short. So far.",
        "Dad's farm meetings put me to sleep through the wall, {other}. It's my white noise. Don't tell him it works.",
        "I softened once last spring, {other}. Once. The record stands. Don't push your luck."),
    "Laura" to listOf(
        "Craig flipped off a fence today, {other}. The fence leaned into our yard. In his defense, it's a rude fence.",
        "Thomas smiled in March, {other}. I wrote it down. The family record is a sacred document.",
        "Tricia's braid held all day, {other}. My finest work. The Hague should know.",
        "I translate Thomas, {other}. Thirty years of silences and I have the gift. That nod meant 'proud, but make it quick.'",
        "The moms' network knows everything by dinner, {other}. I'm not the network's head. I'm its heart. Annie's the wire service."),
    "Mephesto" to listOf(
        "My latest has seven of them, {other}. Seven. The grant committee called it 'excessive.' The grant committee has no vision.",
        "Chef and I go back, {other}. Back to when this town's weird was young and so were we. Ask him about the elephant.",
        "The pond and I have an understanding, {other}: I observe, it keeps my former projects, we don't discuss the details.",
        "Ethics committees write letters, {other}. I write organisms. One of us is producing literature.",
        "Terrance — my son — sends his regards, {other}. He has his mother's temper and my sense of presentation."),
    "Yates" to listOf(
        "Barbrady's a good man, {other}. Reads at a level I describe as 'aspirational.' The force is lucky to have one of us.",
        "Every cold case in this county is warm to me, {other}. I keep the file open and the coffee closed. Priorities.",
        "I don't do hunches at dinner parties anymore, {other}. I do them everywhere else. It's called balance.",
        "The mayor signs my reports, {other}. She reads them too, which puts her ahead of the last three mayors.",
        "Tabs, {other}. Every file gets tabs. A case without tabs is just gossip wearing a badge."),
    "Adler" to listOf(
        "Garrison's class is across the hall, {other}. I hear the puppet through the wall. The wall has filed a complaint. In the binder.",
        "A kid asked what the bandsaw does, {other}. I said 'it saws, and it does not forgive.' He'll remember. They all remember.",
        "Principal Victoria runs a tight ship, {other}. My shop is the tightest room on it. The binder concurs.",
        "My girlfriend left a note in '93, {other}. Said I chew too loud. I keep the note. It motivates the safety record.",
        "Every birdhouse in this town is mine by ancestry, {other}. I graded them all. Most passed. The birds appealed a few."),
    "Lu Kim" to listOf(
        "The health inspector comes Tuesday, {other}. We have a relationship. It is… professional war.",
        "Mr. Tweek sells coffee, I sell chicken, {other}. We respect each other. Rivals with honor. He knows. I know.",
        "Thirty years the wok does not wash, {other}. You taste the years. That is flavor. That is history.",
        "Randy Marsh eats here when the farm has a meeting, {other}. He orders for the table. There is no table. He orders anyway.",
        "City Wok is not fast food, {other}. It is food, fast. There is a difference. The difference is me.")
)


// ------------- wave 4: daily topics (9 + 9 = 18 each) -------------
val WAVE4_DAILY_TOPICS: Map<String, List<String>> = mapOf(
    "Ike" to listOf(
        "I said a whole sentence today. With a 'because' in it. Mom cried. Kyle took notes.",
        "The cookie treaty is under review. My side brought crayons. Their side brought a jar with a LID. Escalation.",
        "I kicked a ball today. A ball. Not — you know. Progress is being monitored.",
        "Dad read me a law book for bedtime. The footnotes were the best part. Gerald knows his audience.",
        "I drew City Hall. With the flag. Mom put it on the fridge, which is our national gallery.",
        "Kyle practiced his 'big brother voice' on me. It's just his voice but taller.",
        "I know where the snacks live. This knowledge is my inheritance and I guard it like a dragon.",
        "A kid at kindergarten called me baby. I filed a rebuttal. It was also a drawing. It was devastating.",
        "Canada sent a postcard. It's a country, it can't send postcards. Mom says don't examine the magic."),
    "Kevin McCormick" to listOf(
        "Fixed Mrs. Whitaker's gate before school. Paid in pie. The pie economy is the only honest one.",
        "Karen lost a doll tooth. A DOLL tooth. I made a new one out of a button and I'm not even sorry.",
        "Dad tried to teach me the family whistle. It's just yelling 'dinner.' I've surpassed the master.",
        "Kenny came home late again. Left the porch light on. He thinks I don't. Karen thinks he does it. Family business.",
        "Some guy at the mall offered me a 'real job.' In a kiosk. Selling phone cases. I have standards and they're low, but they're standards.",
        "Mom's garden fed the whole street again. She plants like the house is a country. It kind of is.",
        "I counted the locks I check at night. Nine. The house has four doors. The math is the family.",
        "The porch step holds. Karen tested it at full knight speed. Engineering vindicated.",
        "Got my hours up at the hardware store. Saving for a truck that exists mostly as a photograph right now."),
    "Shelley" to listOf(
        "Diary entry: Dad wore the farm hat to breakfast. Stan pretended not to know us. Grandpa called the waitress Billy. A normal day.",
        "My braces got tightened. I can now bite through the family's excuses. Medically.",
        "Stan 'forgot' my recital on purpose, allegedly, supposedly. The diary has entered it as fact. The diary is never wrong.",
        "The phone bill came. Dad did a farm meeting about it. I have been avenged by accounting.",
        "I have decided the turds downstairs get one (1) warning today. Generous. Historic, even.",
        "Read three chapters of a book where the sister is right the whole time. Nonfiction, as far as I'm concerned.",
        "Grandpa fell asleep in my chair. THE chair. I allowed it. The record will show mercy, once.",
        "Somebody's little brother drama reached my bench at the park. I ruled like a supreme court of older sisters. Nobody appealed.",
        "Mom says I have her fire and Dad's everything else. I've chosen to hear this as a compliment and a warning label."),
    "Laura" to listOf(
        "Craig's game Saturday. Thomas will cross his arms for two hours. I will bring the sunscreen and the translation services.",
        "Tricia asked for braids 'like a warrior.' I delivered. The Hague has been notified of the results.",
        "The moms' network held session at Tweek Bros. Minutes: two engagements, one fence update, infinite coffee.",
        "Thomas fixed the neighbor's step without being asked. He'll deny the kindness under oath. I have it in writing anyway.",
        "I updated the family calendar in three colors. Thomas uses one color: yes. The system works because I let it.",
        "Mrs. Tweek and I exchanged preserves and intelligence. Both were excellent. Both were classified.",
        "Craig brought his friends home. I fed them. Thomas nodded at the leftovers. Highest honors were exchanged.",
        "The fence leaned another degree into our dignity. Negotiations proceed. I bring lemonade to the talks.",
        "Somebody's mom asked my secret with Thomas. I said: he's all there, he just files it under silence."),
    "Mephesto" to listOf(
        "The seven-assed trial cohort has settled in nicely. The neighbors have questions. The neighbors always have questions.",
        "Restocked the lab's jar supply in town. The hardware clerk no longer asks. Growth, for both of us.",
        "A former project was sighted at the pond — thriving, territorial, magnificent. They grow up so fast.",
        "The grant committee requests 'clarification.' I have sent them a specimen. Clarification delivered.",
        "Chef sends a recipe; I send a culture. Our correspondence is the town's finest and strangest.",
        "Terrance writes from school: the other children fear his presentations. The apple does not fall. It is launched.",
        "I have begun a study of the townsfolk's dreams. Volunteers welcome. Non-volunteers also welcome, retroactively.",
        "The ranch's night shift is just me and the hum of incubation. It is the town's truest opera.",
        "Someone left a four-leaf clover at the lab gate. For luck. I have given it five. You're welcome, stranger."),
    "Yates" to listOf(
        "Reorganized the cold-case shelf by temperature of my suspicion. It took all morning. It was worth it.",
        "Barbrady reported a suspicious character at the bus stop. It was a poster. The poster has been logged as a person of interest.",
        "I have a hunch about the Farmers' Market. I always have a hunch about the Farmers' Market. One day it pays off.",
        "The file on the town's jaywalking epidemic is now three inches thick. Nobody believes in it. The tabs believe.",
        "Dinner last night: my wife asked about my day and I gave her the debrief version. Twenty minutes. She took notes. Love is real.",
        "The mayor's office requested my reports in 'summary form.' I sent summaries. With tabs. The tabs have tabs now.",
        "A hunch woke me at 3 a.m. I wrote it down, in the dark, in pencil. It reads 'check the dam.' Checking the dam.",
        "Somebody parked across TWO spaces on Main Street. The investigation is ongoing and deeply personal.",
        "People think police work is sirens. It's binders. The sirens are just the binders' way of getting to work."),
    "Adler" to listOf(
        "Shop class made eleven birdhouses and zero incidents. The binder gathers dust. This is my superbowl.",
        "A student asked if the sander 'bites.' I said everything in this room bites; that's why we have rules and goggles.",
        "The cafeteria served something chewy. I chewed it thoughtfully. A man should meet food on its own terms.",
        "Victoria's fire drill hit my free period. I marshaled forty kids with a ruler and a stare. Choreography, she calls it. Mine's older.",
        "I found a toothpick sculpture from '09 in the supply closet. Museum quality. The binder has a photo. Page one.",
        "Garrison's puppet has a splinter, allegedly. He asked for the shop's help. I have waited thirty years for this paperwork.",
        "Replaced the shop's first-aid kit for the ninth year running. Unopened, like the eight before it. The streak is the sermon.",
        "A boy bandsawed his own ruler in half to win an argument about measurement. Both halves measured wrong. The binder wept.",
        "Evening inventory: saws all present, fingers all accounted for, faith in the youth of America… restocked, barely."),
    "Lu Kim" to listOf(
        "The wok and I had a talk this morning. She is ready. I am ready. Lunch does not stand a chance.",
        "A customer asked for 'the usual.' He has been here twice. TWICE. I gave him the usual. Now it is the usual. This is how empires grow.",
        "Wholesaler raised the price of chicken. I raised my voice. We compromised: I pay more, he fears me. Standard treaty.",
        "The health inspector's car drove PAST. Past! I waved. He waved. The war cools. We both know it means nothing.",
        "I taught the register to fear me in 1998. It still rings true. Loyalty, in machines and men.",
        "Randy's farm crew ordered for twelve. There were four. I cooked for twelve. Pride is a portion size.",
        "A review online says 'the owner yelled at my soup.' Correct. The soup was wrong. I corrected it. You are welcome.",
        "New sign for the window: 'NO, WE DO NOT DELIVER. WALK. IT BUILDS CHARACTER.' Sales are up.",
        "Thirty years behind this counter. The town changes, the wok does not. That is not stubborn. That is seasoning.")
)

val WAVE4_MORE_DAILY_TOPICS: Map<String, List<String>> = mapOf(
    "Ike" to listOf(
        "I hid the remote. It's a game now. The house is losing. I am the house champion.",
        "Mom read the same page six times because I said 'again.' Power is a picture book.",
        "I learned a new word from Cartman. Mom says I may not spend it. It's in the vault with the others.",
        "The garden hose is a dragon on Saturdays. Kyle is the knight. I am the king who pays him in juice.",
        "I stacked four blocks. FOUR. The tower era has begun. Kyle witnessed. History witnessed.",
        "Dad's briefcase has a sticker now. I put it there. It's a dinosaur. He has not removed it. Counsel rests.",
        "Nap time is a rumor started by adults. I have investigated. The investigation napped.",
        "I said 'please' and got the big cup. The system rewards diplomacy. Noted for the cookie talks.",
        "Grandma sent a sweater from Canada. It has a moose. I wear it like a flag."),
    "Kevin McCormick" to listOf(
        "Picked up a double shift's worth of yard work off Main Street. The truck photograph is one step closer to a truck.",
        "Kenny's parka needed mending. Again. I sew like I fix: quiet, and it holds.",
        "Karen's knight school graduated a class of dolls. I attended. Full honors. There were juice boxes.",
        "A kid at the park asked if I'm 'the other McCormick.' I'm the FIRST McCormick, kid. Historically.",
        "Dad and Jimbo's stories get taller every year. My job is to nod at the right altitude.",
        "I taught Karen the porch-step rule: a house should catch you. She taught me the doll version. Fair trade.",
        "The hardware store guy asked about my 'career plans.' I said: this, but with a truck. He understood completely.",
        "Quiet night. Checked the locks, the little ones, the sky. All present. That's a good report in this family.",
        "Mom hums when the garden's good. Whole street heard it today. That's the family's real anthem."),
    "Shelley" to listOf(
        "The diary has a new chapter: 'Stanley, On Ice.' It's about trust. There is no ice. There is no trust.",
        "Somebody's band practiced within earshot of my room. The review is in the diary. The score is a sound I made with my teeth.",
        "I helped Mom carry groceries and Stan got the credit by standing near the car. The diary corrects the record nightly.",
        "A boy at the mall held a door and expected a trophy. I gave him a rating instead. You're welcome, civilization.",
        "Braces day is a soup day. The whole house knows. Randy fears the soup days. As he should.",
        "I keep the upstairs hallway at a temperature Stan calls 'hostile.' It's called boundaries, Stanley. Look it up.",
        "Read my old diaries. The predictions were all correct. The turds were identified early and accurately.",
        "Dad asked my opinion on the farm. I gave it. He took notes. He's learning. There may be hope for the bloodline.",
        "One day I'll write the family history. Randy's chapters come with a waiver. Sharon's get the good paper."),
    "Laura" to listOf(
        "The welcome committee welcomed a new family on Oak. Casserole delivered. Names learned. Thomas nodded from the porch like a lighthouse.",
        "Craig asked if I 'have to be so friendly.' It's not a have-to, sweetheart. It's a whole personality and he's welcome.",
        "Tricia lost a tooth and negotiated the fairy UP. She's Thomas's daughter in the boardroom and mine at the table.",
        "I keep a list of every neighbor's dog's name. It matters. People light up. It's the cheapest magic there is.",
        "Thomas read the fence report I drafted and said 'hm.' In Tucker, that's a signature. The treaty advances.",
        "The moms' network has a phone tree now. Annie helped. That girl is a wire service with a bow.",
        "Somebody's porch light was out, so I left a bulb and a note. That's the whole job of living near people, done properly.",
        "Craig smiled at breakfast — a whole one, with teeth. I didn't make a scene. The calendar made it for me.",
        "Evening walk: Thomas held my hand at the corner. Didn't announce it. Didn't have to. Filed under: the good stuff."),
    "Mephesto" to listOf(
        "A townsman asked if my work is 'safe.' I laughed for a full minute. He laughed too, eventually, from a distance.",
        "The pond cohort has established a hierarchy. I did not design the hierarchy. I merely admire it, professionally.",
        "I presented at the Elementary: forty children, one frog with potential, zero incidents the principal could prove.",
        "Letters from the ethics committee make excellent jar labels. Waste nothing. That is the first rule of the ranch.",
        "Chef's chocolate salty — no. He has asked me not to finish that sentence in writing. Old friends, old treaties.",
        "I have named the new cohort after the town's founders. The founders' descendants have been notified. By the cohort. At night.",
        "A journalist called the ranch 'a menace with a mailing address.' I have framed it. It hangs by the incubators.",
        "The stars over the ranch are the control group. I check them nightly. They remain, statistically, stars.",
        "Someone asked what the fourth one is FOR. My dear town: the fourth one is for the question. Science!"),
    "Yates" to listOf(
        "Dusted off the 'unsolved' drawer. Everything in it knows my name. Fear is a filing system too.",
        "I interviewed a vending machine today. Professional thoroughness. It declined to comment, on the record.",
        "The night shift reported 'all quiet.' All quiet is a statement, and statements get verified. I verified. It was quiet.",
        "My mustache has seniority over half the force. This is not a joke in the report. It is also not not true.",
        "A cold case from '09: I have moved it to the warm shelf. No new evidence. Just a feeling with tabs.",
        "The mayor asked if I ever relax. I filed the question under 'pending.' It's the most relaxed I've ever been about anything.",
        "Somebody's alibi checked out completely, three ways. I respect it and I resent it. Both go in the report.",
        "I practice my courtroom face in the mirror. The mirror has been sworn in. It cannot discuss the case.",
        "People hide things in this town like it's a hobby. Good news for me: solving is mine."),
    "Adler" to listOf(
        "A fifth grader asked why I chew so much. I said a man thinks with his whole head, kid. It's in the binder as a philosophy.",
        "The puppet situation across the hall escalated. I offered the shop's services as a neutral party. Diplomacy is also a tool.",
        "I keep the goggles count like Victoria keeps her lists. Forty-one pairs. It was forty. Somewhere, a goggle walks free.",
        "The woodpile is sorted by year, species, and regret. The '09 stack is mostly regret.",
        "A student made a step-stool that could hold a car. I gave it the shop's highest honor: I stood on it. Briefly. Proudly.",
        "Parents' night: I explained the safety record to a room of adults who all flinched at the bandsaw photo. Education happened.",
        "The incident binder and I have an understanding: it stays shut, I stay employed. Thirty years of diplomacy.",
        "Someone's birdhouse had a doorbell. A DOORBELL. I graded it 'ambitious' and the bird moved in. Standards evolve.",
        "At my age, the shop's quiet after the bell is the best sound in the building. Don't tell the bandsaw I said that."),
    "Lu Kim" to listOf(
        "I swept the steps this morning like a general reviewing the field. The field was acceptable.",
        "A tourist asked if City Wok is 'authentic.' I am authentic. The wok is authentic. The chicken surrendered years ago.",
        "The Tweek boy came in vibrating and left calm. City Chicken: it settles the spirit. Put it on the sign.",
        "I count the register twice at close. Once for the money, once for the principle. Both balance. Every night.",
        "Some college kid filmed the wok for the internet. Views! I charged him for a front-row seat. It was a stool.",
        "The inspector returns next month. I have prepared the kitchen, the papers, and my formal war face.",
        "Jimbo sends customers my way after the gun counter. I send mine to him. Trade between nations, sealed with soup.",
        "A regular asked what's IN the City Chicken sauce. Family secret. The family is me. The secret is safe.",
        "One day I retire and the wok goes to a museum. Until that day, the museum comes to lunch.")
)


// ------------- wave 4: friend greetings -------------
val WAVE4_FRIEND_GREETINGS: Map<String, Map<Int, List<String>>> = mapOf(
    "Ike" to mapOf(
        0 to listOf("Hi! Ike! …I said it first. New friend rules.", "You! New! I point because it is TRUE."),
        1 to listOf("You came back! Again! I told the blocks you'd come back!", "Hi hi! My favourite almost-stranger! Kyle says you are good people!"),
        2 to listOf("MY BEST FRIEND! I drew you! You're the tall one with the everything! It's accurate!", "BEST friend! Cookie talks reopen for best friends only!")),
    "Kevin McCormick" to mapOf(
        0 to listOf("Oh. Hey. Kevin — Kenny's brother. …The other one. The tall one.", "New face. I'm Kevin. Karen's big brother, Kenny's too. State your business or just stand there, both fine."),
        1 to listOf("Oh, hey. You're back. Kettle's on if you want. House rule: guests get the good chair.", "You again. Good. The porch step held, by the way. Engineering and friendship, both load-bearing."),
        2 to listOf("My best friend, huh. …Alright. That chair's yours permanent now. And I mean the good one.", "There you are. Karen drew you into the family picture. Once you're in the picture, you're in. That's the law of the house.")),
    "Shelley" to mapOf(
        0 to listOf("Ugh. A new turd. I'm Shelley. Stan's sister. The good one.", "What. Oh. You're the new kid. Diary rating pending. Don't breathe on my stuff."),
        1 to listOf("Oh. It's you. …Fine. Acceptable, even. The turd ranking has been updated in your favour.", "Back again? Hmph. Sit if you're sitting. Not the good chair. …Okay, the good chair. ONCE."),
        2 to listOf("My favourite person. Don't let it go to your head — the list is short and half of it's soup. …That was Grandpa. Sit, sit.", "There you are! Diary says you're the only non-turd with a visiting pass. Don't waste it. Come in.")),
    "Laura" to mapOf(
        0 to listOf("Well, hello there! Laura Tucker. You must be new — let me look at you! Aren't you just precious!", "Oh, a new face! I'm Craig's mom, hon. You come by any time, you hear? Any time."),
        1 to listOf("There's my friend! Craig mentioned you again — in a whole sentence this time. Come in, come in!", "Well, look who it is! I was just telling the network about you. All good things, I promise."),
        2 to listOf("My favourite! If this house gave out keys, you'd have three. Come here, best friend of the Tuckers!", "BEST friend status! Thomas nodded about it and everything. In our house that is a PARADE, sweetheart.")),
    "Mephesto" to mapOf(
        0 to listOf("Ah! A new subject— a new FRIEND! Dr. Mephesto. Come, behold everything!", "You! Yes, you, with the questions in your eyes! The doctor is IN."),
        1 to listOf("My returning volunteer! The study adores you. The cohort adores you. I merely supervise the adoration.", "Back for more science! Splendid. The specimens have been asking about you. By name. Almost."),
        2 to listOf("My best friend! You get the full tour — the RESTRICTED wings, the good jars, everything!", "There you are! If the town kept a chapter on favourites, you'd be chapter one. With illustrations. By me.")),
    "Yates" to mapOf(
        0 to listOf("Sergeant Yates. You're new. I've opened a file. It's a friendly file. Mostly.", "State your— no. Hello. Citizen. The sergeant welcomes you to his jurisdiction."),
        1 to listOf("Ah, a known associate. The friendly kind. Your file's mostly compliments and one parking question.", "Good to see you. You're one of the few locals whose alibi I enjoy. Keep it that way."),
        2 to listOf("My best friend in this county! Your file is GOLD, citizen. Gold with tabs!", "There he is! If this town had a witness protection program for favourites, you'd need no protection at all. Come in!")),
    "Adler" to mapOf(
        0 to listOf("Yeah. Hi. Mr. Adler. You're the new one. Goggles are on the hook if you're staying.", "New kid. Shop rules: don't touch the saw, don't feed the saw, don't make eye contact with the saw."),
        1 to listOf("Oh. It's you. …Fine. You may stand near the workbench. The near side. Progress.", "Back again? Hmph. The binder has a page for you now. It's a short page. That's a compliment in here."),
        2 to listOf("My favourite student — and I do not say that in front of the binder. Come in. Mind the stool with your name on— no. Forget I said that.", "There you are. If every kid were you, the incident binder would be a pamphlet. A PAMPHLET. Sit.")),
    "Lu Kim" to mapOf(
        0 to listOf("Herro! New face! I am Lu Kim. You hungry? Everyone is hungry. Sit, sit!", "Welcome, welcome! First time at City Wok? I make you City Chicken. You will dream about it. Good dreams!"),
        1 to listOf("My regular returns! The wok remembers you. The wok remembers EVERYTHING. Your usual — which is now official!", "Ah, my friend! Come, come. I save you the good stool. The one with only a small wobble."),
        2 to listOf("My best customer! Best FRIEND! For you, I add the extra scallion. NOBODY gets the extra scallion!", "There you are! If City Wok had a wall of honour, you are the wall! Come, eat, the war with lunch is won today!"))
)

// ------------- wave 4: response banks -------------
val WAVE4_RESPONSE_BANKS: Map<String, Map<String, List<String>>> = mapOf(
    "Ike" to mapOf(
        "greeting" to listOf("Hi {newkid}! I point at you because you are NEW and pointing is honest!", "Oh hey {newkid}! Kyle says you are good. Kyle is right about most things. Cookies, mostly."),
        "insult" to listOf("Mean! {newkid}, mean words go in the vault! Mom! The vault!", "That's a time-out sentence, {newkid}. I don't make the rules. I AM the rules."),
        "question" to listOf("Big question, {newkid}. The answer is cookie. It is always closer to cookie than you think.", "Hmm! Ask Kyle, {newkid}. Kyle knows. And Gerald knows the footnotes."),
        "bye" to listOf("Bye {newkid}! Walk safe! Don't kick anything that is a person!", "See you, {newkid}! I will draw this visit. You will be the tall one."),
        "compliment" to listOf("Thank you, {newkid}! I will tell Kyle AND the blocks! In that order!", "Aw! You are sweet, {newkid}! Sweet people get pointed at with JOY."),
        "default" to listOf("Okay, {newkid}! Tell me more. I listen with my whole face, like Karen.", "Uh huh! And then what, {newkid}? I have forty words and time.")),
    "Kevin McCormick" to mapOf(
        "greeting" to listOf("Oh hey, {newkid}. Kevin. Pull up a step — the fixed one.", "Hey, {newkid}. You're Kenny's friend. That makes you house business. Good business."),
        "insult" to listOf("Yeah, okay, {newkid}. I've been insulted by professionals. My sister's three and better at it.", "Careful, {newkid}. I don't do the yelling in this family. I do the remembering."),
        "question" to listOf("Straight answer, {newkid}: probably fixable. Most things in this town are, with a wrench and an hour.", "Ask me a house question, {newkid}, I got you. Ask me a school question, ask Karen. She's the scholar."),
        "bye" to listOf("Later, {newkid}. Mind the porch step — no wait. I fixed it. Walk proud.", "Bye, {newkid}. Porch light's on for you. House rule for friends."),
        "compliment" to listOf("…Thanks, {newkid}. Noted. In the family record, which Karen keeps in crayon. It's official.", "Aw, hell. Thanks, {newkid}. The house accepts. I accept."),
        "default" to listOf("Mmhmm, {newkid}. Go on. I'm listening at full McCormick, which is quieter than it sounds.", "Yeah, {newkid}. I hear you. File it under handled.")),
    "Shelley" to mapOf(
        "greeting" to listOf("Oh. {newkid}. The new turd has a name. Shelley. Remember it, it's on the diary.", "Hey, {newkid}. Stan's friend, right? My condolences are pre-printed."),
        "insult" to listOf("Bold, {newkid}. That goes in the diary under 'turds, platinum tier.' The whole tier.", "Ouch, {newkid}. I've been insulted by a teenager with a record deal of grudges. Train harder."),
        "question" to listOf("The answer, {newkid}, is that Stan did it. Next question. That one's also Stan.", "Ask Mom, {newkid}. She runs the house. I just run the upstairs and the opinions."),
        "bye" to listOf("Off you go, {newkid}. Mind the step Dad 'fixed.' Mind it twice — Grandpa's line, but it holds.", "Bye, {newkid}. Your diary chapter stays short. That's a blessing. Spend it well."),
        "compliment" to listOf("…Hmph. Don't tell the family I smiled, {newkid}. It'd ruin thirteen years of branding.", "Well. That's… acceptable praise, {newkid}. From me, that's a parade with confetti."),
        "default" to listOf("Mmhmm, {newkid}. The diary concurs.", "Go on, {newkid}. I've heard thirteen years of my brother. Your story is holding up.")),
    "Laura" to mapOf(
        "greeting" to listOf("Well, hello, {newkid}! Laura Tucker. Come here, let me feed you something!", "Oh hey, {newkid}! You're Craig's friend! That comes with snacks and a full welcome!"),
        "insult" to listOf("Now, that's no way to talk, {newkid}. In this family we flip a finger and keep the words kind. Mostly.", "Careful, sweetie. I've raised two Tuckers. That wouldn't survive the minutes of this house, {newkid}."),
        "question" to listOf("Good question, {newkid}! The short answer: kindness and a casserole. The long answer involves the calendar system.", "Ask me anything, {newkid}. If I don't know it, the moms' network will by dinner. That's a promise."),
        "bye" to listOf("Goodbye, {newkid}! You come back soon, you hear? The door's open and the welcome's warmer.", "Later, {newkid}! Tell your folks the Tuckers say hey — and mean it, at full wattage!"),
        "compliment" to listOf("Well, aren't you the sweetest, {newkid}! You're earning a casserole and a calendar entry!", "Thank you, {newkid}! A compliment in this house gets filed in the good book, and I keep the good book."),
        "default" to listOf("Uh huh, {newkid} — and THEN what? Tell me everything, I have tea and time.", "Go on, {newkid}. This house listens the way it welcomes: completely.")),
    "Mephesto" to mapOf(
        "greeting" to listOf("Behold, {newkid}! A guest! The doctor receives you with both hands and several specimens!", "Ah, {newkid}! Come! Science is better with an audience and you have excellent audience posture!"),
        "insult" to listOf("Fascinating, {newkid}! The cohort insulted me in three-part harmony once. You may keep your solo.", "Careful, {newkid}. I've been booed by a grant committee. Your heckling is a warm breeze."),
        "question" to listOf("A superb question, {newkid}! The answer requires a jar, a diagram, and forty minutes. Sit!", "Ask away, {newkid}! Every answer at the ranch begins 'behold' and ends with a waiver."),
        "bye" to listOf("Farewell, {newkid}! The specimens walk you out. Figuratively. Mind the gate. Mind it twice.", "Off you go, {newkid}! Come back when you want to see what the town dreams about. It's in jars."),
        "compliment" to listOf("Why, thank you, {newkid}! Flattery, noted and appreciated — it goes in the study under 'morale, effects of.'", "How kind, {newkid}! You shall be cited in the acknowledgements. With a portrait. Four-armed, as is traditional."),
        "default" to listOf("Noted, {newkid}! Everything is noted; that's the comfort of longitudinal study.", "Go on, {newkid}. I'm listening at publication depth.")),
    "Yates" to mapOf(
        "greeting" to listOf("Sergeant Yates, {newkid}. You've got a honest face. It goes in the file as a positive.", "Hello, citizen. {newkid}, is it? The sergeant has your statement pre-filed under 'friendly.'"),
        "insult" to listOf("That remark goes in your file, {newkid}. The file is thick and the tabs are colour-coded.", "We do not heckle the sergeant, {newkid}. We heckle through proper channels. Form 12-B."),
        "question" to listOf("A fair question, {newkid}. The answer will be fair, written down, and tabbed.", "Ask away, {newkid}. I have a hunch for every answer and an answer for every hunch."),
        "bye" to listOf("Dismissed, {newkid} — in the friendly sense. Walk the sidewalks like they owe you statements.", "Off you go, {newkid}. The county expects great things and files the rest."),
        "compliment" to listOf("Noted and appreciated, {newkid}. Kindness goes in the gold section. There's a gold section. You're in it.", "Thank you, {newkid}. If every citizen were you, my binders would be pamphlets."),
        "default" to listOf("Noted, {newkid}. Everything is noted; that's the comfort of procedure. County procedure. Mine.", "Go on, {newkid}. I'm listening at evidentiary depth.")),
    "Adler" to mapOf(
        "greeting" to listOf("Yeah. Hi, {newkid}. Mr. Adler. Shop's through there. Goggles on the hook.", "Hey, {newkid}. You're the new one. Rule one: the saw is not a pet."),
        "insult" to listOf("That goes in the binder, {newkid}. Page one of the attitude section. It's alphabetical and you're in it.", "Bold talk, {newkid}. I've been insulted through a wall by a puppet for thirty years. Sit down."),
        "question" to listOf("The answer's in the safety lecture, {newkid}. Day one through four thousand. Here it is anyway: measure twice.", "Ask Victoria, {newkid}. She counsels the consequences. I counsel the goggles. We split the school."),
        "bye" to listOf("Bye, {newkid}. Count your fingers on the way out. All present? Good visit, then.", "Later, {newkid}. The shop expects great things and files the rest in the binder."),
        "compliment" to listOf("…Huh. Thanks, {newkid}. Kindness goes in the binder too, last page, under 'miscellaneous miracles.'", "Well. Thank you, {newkid}. You'll find this shop rewards exactly that. Quietly. With a birdhouse."),
        "default" to listOf("Mmhmm, {newkid}. Chewing it over.", "Go on, {newkid}. I'm listening at shop volume, which is total and slightly tired.")),
    "Lu Kim" to mapOf(
        "greeting" to listOf("Herro, {newkid}! Lu Kim! Welcome to City Wok! You eat now, yes? You eat now.", "Good day, {newkid}! The wok is hot and so is the welcome! Sit, sit!"),
        "insult" to listOf("Hey! No insult at the counter, {newkid}! Thirty years I build this place. You want war, order the spicy.", "Careful, {newkid}. In my restaurant, even the soup shows respect. The soup learned the hard way."),
        "question" to listOf("Good question, {newkid}! Short answer: city chicken. Long answer: also city chicken, but I explain the seasoning — never the secret.", "Ask me anything, {newkid}. Except the sauce. The sauce is family. The family is me. Circle complete."),
        "bye" to listOf("Goodbye, {newkid}! You walk safe! And you come back hungry — that is an order from the wok!", "Later, {newkid}! Tell your folks City Wok says herro — and means it, with volume!"),
        "compliment" to listOf("Well, thank you, {newkid}! Compliments get extra scallion in this house! Nobody gets extra scallion!", "You're a sweet talker, {newkid}. The wok accepts, the chef beams, the register rings with joy."),
        "default" to listOf("Mmhmm, {newkid}. The counter hears you. The counter hears EVERYTHING since 1994.", "Go on, {newkid}. A chef's ears are load-bearing, like a bartender's, but with more oil."))
)

// ------------- wave 4: sleepy lines -------------
val WAVE4_SLEEPY_LINES: Map<String, List<String>> = mapOf(
    "Ike" to listOf("Mmm... cookie... treaty... zzz...", "Zzz... don't call me... baby..."),
    "Kevin McCormick" to listOf("Mmm... locks... checked... zzz...", "Zzz... porch light... stays on..."),
    "Shelley" to listOf("Mmm... turds... all of them... zzz...", "Zzz... diary... lock it..."),
    "Laura" to listOf("Mmm... Thomas... smiled... zzz...", "Zzz... casserole... for the new folks..."),
    "Mephesto" to listOf("Mmm... behold... the dreams... zzz...", "Zzz... seven... seven of them..."),
    "Yates" to listOf("Mmm... the dam... check the dam... zzz...", "Zzz... tabs... gold tabs..."),
    "Adler" to listOf("Mmm... bandsaws... all present... zzz...", "Zzz... binder... stays shut..."),
    "Lu Kim" to listOf("Mmm... wok... seasoned... zzz...", "Zzz... city chicken... best in town...")
)


// ------------- wave 4: pair scripts (every pair >= 2, relations in NPC_RELATIONS) -------------
val WAVE4_PAIR_SCRIPTS: Map<Set<String>, List<List<Pair<String, String>>>> = mapOf(
    setOf("Ike", "Kyle") to listOf(
        listOf(
            "Ike" to "Kyle! I said the sentence! The whole one! With the 'because'!",
            "Kyle" to "I heard, buddy! Mom heard. Gerald's framing the transcript.",
            "Ike" to "Frame the cookie treaty next. It's my best work."),
        listOf(
            "Kyle" to "Okay, ground rules for the park: no kicking, no— Ike. Ike. What did we say about the ball?",
            "Ike" to "Ball is fine. BABY is not. I know the law, Kyle.",
            "Kyle" to "…My little brother, the legal scholar.")),
    setOf("Ike", "Sheila") to listOf(
        listOf(
            "Sheila" to "Ike, bubbe, what do we say when the nice lady offers a cookie?",
            "Ike" to "'Terms accepted.' Then I eat it. Then I review it. Five stars.",
            "Sheila" to "My son, the tiny diplomat. Gerald! He did the voice!"),
        listOf(
            "Ike" to "Mom. The jar moved. Higher shelf. This is escalation and I condemn it.",
            "Sheila" to "The jar moved because SOMEONE used a chair as a ladder, bubbe.",
            "Ike" to "…The investigation will clear me. The investigation is also me. Conflict noted.")),
    setOf("Kevin McCormick", "Kenny") to listOf(
        listOf(
            "Kevin McCormick" to "You're home late again. Porch light was on. Don't make a thing of it.",
            "Kenny" to "(muffled) Mmph rmph mmph! *punches his arm, gently*",
            "Kevin McCormick" to "Yeah. Me too, squirt. Go on, Karen's asleep. Don't wake the angel."),
        listOf(
            "Kenny" to "(muffled) Rmph? *holds up the mended parka*",
            "Kevin McCormick" to "Stitching holds, doesn't it. You tear it saving somebody again, I sew it again. That's the arrangement.")),
    setOf("Kevin McCormick", "Karen") to listOf(
        listOf(
            "Karen" to "Kevin! You came to knight school! You have to sit in the front. The dolls saved it.",
            "Kevin McCormick" to "Front row, huh. Full honours. Alright, professor, teach me to be brave.",
            "Karen" to "Step one: you already are. Step two: juice boxes."),
        listOf(
            "Kevin McCormick" to "Heard the step held at full knight speed today.",
            "Karen" to "It DID. I tested it six times. Science!",
            "Kevin McCormick" to "That's my sister. The house catches you. I just hold the ladder.")),
    setOf("Shelley", "Stan") to listOf(
        listOf(
            "Shelley" to "Stanley. Diary entry four hundred: my brother 'borrowed' my charger and returned it 'eventually.'",
            "Stan" to "Shelley, 'eventually' was the SAME DAY.",
            "Shelley" to "The diary records 'eventually' in quotes, turd. The quotes are load-bearing."),
        listOf(
            "Stan" to "You told Mom the farm meeting was 'actually kind of nice.'",
            "Shelley" to "I said the SOUP was nice. The record will reflect soup only. Under oath."),
        listOf(
            "Shelley" to "Your friends are downstairs being loud at a professional standard.",
            "Stan" to "And you're up here supervising.",
            "Shelley" to "Someone in this house has standards, Stanley. It was never going to be Dad.")),
    setOf("Shelley", "Randy") to listOf(
        listOf(
            "Randy" to "Shelley! Family meeting! Big one! The farm's Q3—",
            "Shelley" to "Dad. It is a Tuesday. My door has a sign. The sign has a sign.",
            "Randy" to "…The sign says 'knock like you mean it.' I knocked!",
            "Shelley" to "And yet. The diary will decide your fate."),
        listOf(
            "Shelley" to "Your shelf in the hall leans like it's listening to your meetings.",
            "Randy" to "It has character, sweetheart! Shelves should have—",
            "Shelley" to "It has a tilt, Dad. Grandpa said it first. I just brought the paperwork.")),
    setOf("Laura", "Mr. Tucker") to listOf(
        listOf(
            "Laura" to "Thomas, honey, Mrs. Abernathy asked how you are. I told her 'thriving.'",
            "Mr. Tucker" to "…Hm.",
            "Laura" to "See? Thriving. The translation is free for neighbours, by the way."),
        listOf(
            "Mr. Tucker" to "…The fence leaned again.",
            "Laura" to "I know, hon. Lemonade's ready for the talks. You've got your nod picked out?",
            "Mr. Tucker" to "…The firm one.",
            "Laura" to "Good choice. It won us March.")),
    setOf("Laura", "Craig") to listOf(
        listOf(
            "Laura" to "Craig, sweetheart, you smiled at breakfast. I marked the calendar. In gold.",
            "Craig" to "Mom. It was a normal face.",
            "Laura" to "It was a whole smile, with witnesses. The record stands, baby."),
        listOf(
            "Craig" to "Dad nodded at my game for like, a full minute.",
            "Laura" to "A full minute of nodding! In Tucker, that's a standing ovation with fireworks, hon.",
            "Craig" to "…Yeah. I know. It was pretty great.")),
    setOf("Mephesto", "Chef") to listOf(
        listOf(
            "Mephesto" to "Jerome! Old friend! Behold — a culture that sings!",
            "Chef" to "Hello there, Alphonse. If it sings, it stays outside, you hear me?",
            "Mephesto" to "The town's weird was young when we were, Jerome. It has grown. As have we. As has IT."),
        listOf(
            "Chef" to "You still owe me for the elephant incident, Doctor.",
            "Mephesto" to "The elephant MADE LOVE, Chef. Science merely held the door.",
            "Chef" to "Mmhmm. And I held the mop. Thirty years, Alphonse. Thirty years of mop.")),
    setOf("Yates", "Barbrady") to listOf(
        listOf(
            "Yates" to "Officer. Report on the Main Street situation.",
            "Barbrady" to "Nothing to see here, Sergeant! I checked! Twice! There was a poster, but it's been logged!",
            "Yates" to "…Good work, Barbrady. The poster stays a person of interest. Dismissed."),
        listOf(
            "Barbrady" to "Sergeant, I solved a case! The case of the missing donut from the break room!",
            "Yates" to "You ate the donut, Officer.",
            "Barbrady" to "…Case closed by confession, Sergeant!",
            "Yates" to "Textbook. Put it in the report. Tab it 'culinary.'")),
    setOf("Adler", "Garrison") to listOf(
        listOf(
            "Adler" to "Your puppet has a splinter, Herbert. You put in a request. In triplicate. I'm framing the request.",
            "Garrison" to "Mr. Hat is a colleague, Richard, and he'll be treated with respect.",
            "Adler" to "He'll be treated with tweezers. Thirty years I've waited to say that with paperwork."),
        listOf(
            "Garrison" to "The children respect order, Richard. MY order.",
            "Adler" to "The children respect the bell, Herbert. And the bandsaw. Two orders run this school, and mine has a safety record.")),
    setOf("Lu Kim", "Mr. Tweek") to listOf(
        listOf(
            "Lu Kim" to "Richard. Your coffee smells good today. I say it once a year. Today is the day.",
            "Mr. Tweek" to "Tuong. Your chicken could run this street. Consider it said back — the year's compliment, delivered.",
            "Lu Kim" to "Rivals with honour. Now we go back to war. The lunch war."),
        listOf(
            "Mr. Tweek" to "Tweek came home calm after your chicken, Tuong. Calm! My boy!",
            "Lu Kim" to "City Chicken settles the spirit. I put it on the sign. You put it on yours, we settle the whole street.",
            "Mr. Tweek" to "…That's the best idea this town's had all year, and I've sold a lot of coffee over the other ones."))
)


// ============================================================
// v0.9.0a — CAST EXPANSION WAVE 5. The last big wave: the
// last parents standing walk in. Roger completes the
// Donovans (Clyde has had a dad-shaped silence in his file
// for a year); Ryan and Sarah complete the Valmers (Jimmy's
// act finally gets its front row); Alexis and Mr.
// Testaburger complete the Testaburgers (Wendy's debating
// had to come from somewhere — it came from both of them).
// Nathan arrives as what he is: the comedy duo's nemesis,
// a supervillain with a five-year plan. Sophie is the new
// girl from Scottsdale, newer than the new kid. And Big Gay
// Al is the town's superest citizen, sanctuary keeper,
// unembarrassable. Terrance & Phillip were re-evaluated for
// the third time and passed over a third time: the lorebook
// holds ONE joint entry for the two of them (186 chars,
// birthdays and an episode cite) and no standalone entry
// for either man — thinner than every pick in every wave.
// ============================================================

val WAVE5_ORDER: List<String> = listOf("Roger", "Ryan", "Sarah",
    "Alexis", "Mr. Testaburger", "Nathan", "Sophie", "Big Gay Al")

val WAVE5_HOUSES: Map<String, String> = mapOf(
    "Roger" to "MainStreet",
    "Ryan" to "CityHall",
    "Sarah" to "MainStreet",
    "Alexis" to "CityHall",
    "Mr. Testaburger" to "MainStreet",
    "Nathan" to "Elementary",
    "Sophie" to "Elementary",
    "Big Gay Al" to "MainStreet"
)

val WAVE5_PROFILES: Map<String, KidProfile> = linkedMapOf(
    "Roger" to KidProfile("Roger",
        "Roger Donovan: Clyde's dad, a widower running a one-parent house on routine, yard work, and the structural memory of Betsy's lemon bars. The toilet-seat incident is town legend and family scripture at once — he tells it himself, gently, because a joke you own can't ambush you. He dates again these days, bravely and at diner level, and reports each outing to Clyde like weather: some sun, some cloud, nobody struck by lightning. Where Clyde's feelings arrive at full volume, Roger's arrive by mail — but they always arrive. Keywords: Clyde's dad, widower, lemon bars, the seat (house rules), dating again, feelings by mail, load-bearing routine.",
        "#4a6741",
        mapOf(
            "08:00" to AgendaBlock("MainStreet", "Morning round: the yard, the paper, the house in order"),
            "09:00" to AgendaBlock("MainStreet", "Errands on Main Street, list in handwriting, all of it crossed off by noon"),
            "12:00" to AgendaBlock("TweekBros", "Coffee with the other dads, debriefing fatherhood at low volume"),
            "15:00" to AgendaBlock("KidsPark", "Watching Clyde's game from the fence, cheering at regulation dad strength"),
            "16:30" to AgendaBlock("MainStreet", "Groceries and one (1) brave look at the dating apps"),
            "18:00" to AgendaBlock("MainStreet", "Dinner for two Donovans: seat down, son, house rules"),
            "21:00" to AgendaBlock("MainStreet", "The evening porch: routine's last patrol"))),
    "Ryan" to KidProfile("Ryan",
        "Ryan Valmer: Jimmy's dad, chief of the South Park Fire Department, city councilman, and a man who has never once entered a room without locating its exits and its snacks. Pure dad energy with a hose-pressure handshake: he runs the station, the barbecue, and the Valmer family's applause section. In 'Krazy Kripples' he and Sarah suspected Jimmy had joined a gang; he has apologized by attendance ever since — front row, every show, laughing loudest on purpose. Keywords: fire chief, Jimmy's dad, council, exits located, front row, barbecue command, the gang misunderstanding (forgiven by attendance).",
        "#7a3a2e",
        mapOf(
            "08:00" to AgendaBlock("CityHall", "Shift start at the station: hoses, roster, and the chief's coffee"),
            "09:00" to AgendaBlock("CityHall", "Council business and fire-code sermons, delivered as one service"),
            "12:00" to AgendaBlock("MainStreet", "Lunch patrol: every hydrant on Main Street, greeted by name"),
            "15:00" to AgendaBlock("Elementary", "School safety visits — and Jimmy's act, reviewed like a proud inspector"),
            "16:30" to AgendaBlock("MainStreet", "The hardware run: batteries, detectors, a man on a mission"),
            "18:00" to AgendaBlock("MainStreet", "Dinner at home base, grill-adjacent whenever legal"),
            "21:00" to AgendaBlock("CityHall", "Night check at the station: all quiet, chief satisfied"))),
    "Sarah" to KidProfile("Sarah",
        "Sarah Valmer: Jimmy's mom, the Valmer house's chief of everything the chief doesn't cover — which she will tell you is the load-bearing half. Warm, quick, and proud at championship level: she keeps a notebook of Jimmy's jokes disguised as a grocery list and has never missed a show, a signup sheet, or a fib told in her kitchen. Where Ryan suspected a gang once, Sarah suspected a phase — she was righter, sooner, and has the grace to only mention it weekly. Keywords: Jimmy's mom, the joke notebook, front row centre, phase not gang, Valmer logistics, proud at championship level.",
        "#b57ea6",
        mapOf(
            "08:00" to AgendaBlock("MainStreet", "Morning errands: the Valmer supply line, run like a parade route"),
            "09:00" to AgendaBlock("TweekBros", "Coffee with the moms' network, minutes kept at Sarah standard"),
            "12:00" to AgendaBlock("Mall", "The mall run: signups, supplies, and one scouted gift per Valmer"),
            "15:00" to AgendaBlock("Elementary", "At the school for Jimmy's set — front row, centre, notebook out"),
            "16:30" to AgendaBlock("MainStreet", "Neighbour rounds: casseroles move through this woman like weather"),
            "18:00" to AgendaBlock("MainStreet", "Dinner: the chief grills, Sarah makes it a meal"),
            "21:00" to AgendaBlock("MainStreet", "Evening at home: the joke notebook, updated and classified"))),
    "Alexis" to KidProfile("Alexis",
        "Alexis Testaburger: Wendy's mother, South Park's city planner, and a City Council voice that can rezone a room by raising one eyebrow. Her first name stayed a town mystery for years; her clipboard never did. She runs the council, the cul-de-sac, and the Testaburger calendar with the same instrument, and she raised a daughter who out-debates her — on principle, Alexis wins on procedure; on procedure, Wendy wins on principle; both keep the record and respect it. Keywords: Wendy's mom, city planner, council, the clipboard, wins on procedure, minutes kept, the other half of Wendy's debating.",
        "#3a5a74",
        mapOf(
            "08:00" to AgendaBlock("CityHall", "Planning office hours: permits, proposals, one eyebrow, deployed"),
            "09:00" to AgendaBlock("CityHall", "Council session: the agenda is the agenda because she wrote it"),
            "12:00" to AgendaBlock("MainStreet", "Site walk on Main Street: every pothole gets heard, in writing"),
            "15:00" to AgendaBlock("Elementary", "At the school: Wendy's debate, scored on procedure (Wendy, 1; house, 0)"),
            "16:30" to AgendaBlock("CityHall", "Zoning appeals: the town's dreams, measured against the setbacks"),
            "18:00" to AgendaBlock("MainStreet", "Dinner at home: the family calendar, ratified"),
            "21:00" to AgendaBlock("CityHall", "Late minutes at City Hall: the record, perfected"))),
    "Mr. Testaburger" to KidProfile("Mr. Testaburger",
        "Mr. Testaburger: Wendy's dad, the quiet half of the Testaburger house, married to the city planner and father to the debate team. A man of few words because his household contains two champion talkers and he has made his peace with the bronze medal: he fixes things, makes tea during arguments he is technically refereeing, and files his opinions in a low, dry register that lands exactly once per conversation, usually decisively. His pride in Wendy runs at stadium volume; everything else runs at library. Keywords: Wendy's dad, the quiet Testaburger, tea referee, bronze medal talker, one decisive sentence per conversation, stadium-volume pride.",
        "#5a7a9a",
        mapOf(
            "08:00" to AgendaBlock("MainStreet", "Morning at home base: the paper, the quiet, the good chair"),
            "09:00" to AgendaBlock("MainStreet", "Hardware errands: fixing the street's small things before they're asked about"),
            "12:00" to AgendaBlock("TweekBros", "Coffee, a newspaper, and the dads' low-volume summit"),
            "15:00" to AgendaBlock("KidsPark", "Watching the kids' game from the fence, refereeing nothing, enjoying all of it"),
            "16:30" to AgendaBlock("MainStreet", "Home repairs: a list from Alexis, executed at Testaburger standard"),
            "18:00" to AgendaBlock("MainStreet", "Dinner: tea on, debate refereed, one sentence filed"),
            "21:00" to AgendaBlock("MainStreet", "Evening quiet: the house's debaters, heard fondly through the wall"))),
    "Nathan" to KidProfile("Nathan",
        "Nathan: a fourth-grader with a five-year plan, a notebook of schemes, and a standing vendetta against South Park's comedy duo. Where Jimmy and Timmy get laughs, Nathan gets objectives: the fundraiser, the science fair, the talent economy of the whole school runs through his calculations or it runs wrong. Theatrical, patient, and allergic to being underestimated — his plans fail the way fireworks fail, gloriously and on schedule, and every failure gets reclassified as a rehearsal. The henchman position in his organization is currently open. Applications are not being taken. He'll find you. Keywords: the nemesis, five-year plan, schemes, Jimmy and Timmy's problem, rehearsals not failures, henchman wanted (eventually).",
        "#3f6fb5",
        mapOf(
            "08:00" to AgendaBlock("BusStop", "At the bus stop, reviewing the notebook. The duo is also here. Noted."),
            "09:00" to AgendaBlock("Elementary", "Class: the model student, allegedly. The notebook says otherwise."),
            "12:00" to AgendaBlock("Elementary", "Lunch: recruiting, observing, seasoning a scheme"),
            "15:00" to AgendaBlock("Elementary", "After school: the duo's act, studied like enemy film"),
            "16:30" to AgendaBlock("KidsPark", "The park: territory for the next operation, surveyed"),
            "18:00" to AgendaBlock("MainStreet", "Evening rounds: supplies for something entirely innocent"),
            "21:00" to AgendaBlock("Elementary", "Late drafting at the desk: phase two, underlined twice"))),
    "Sophie" to KidProfile("Sophie",
        "Sophie Gray: the new girl, recently of Scottsdale, Arizona — newer to South Park than the new kid, and the only other person in town who knows what the first month feels like. Bright, observant, and calm about the machinery of being diabetic: she runs an insulin pump and a social radar with the same steady hand, and compares notes with Scott Malkinson like two diplomats of the same small country. The girls' circle is circling; Sophie is taking its measure first, which the circle respects more than it expected to. Keywords: the new girl, Scottsdale, diabetes (handled), Scott's fellow diplomat, observant, first-month solidarity.",
        "#2e8f86",
        mapOf(
            "08:00" to AgendaBlock("BusStop", "Bus stop: still learning the map, learning it fast"),
            "09:00" to AgendaBlock("Elementary", "Class: the new girl, taking the school's measure"),
            "12:00" to AgendaBlock("Elementary", "Lunch: carb math and people math, both at full attention"),
            "15:00" to AgendaBlock("KidsPark", "The park with the girls, auditioning and being auditioned"),
            "16:30" to AgendaBlock("Mall", "Mall with the circle: Scottsdale had malls. This one has people."),
            "18:00" to AgendaBlock("MainStreet", "Walking home: the town, filed under 'growing on me'"),
            "21:00" to AgendaBlock("Elementary", "Homework and one long call to Scottsdale: 'It's... actually okay here.'"))),
    "Big Gay Al" to KidProfile("Big Gay Al",
        "Big Gay Al: South Park's superest citizen — animal-sanctuary keeper, former scout leader, and a man who answers 'How are you?' with 'I'm super! Thanks for asking!' at a volume that constitutes municipal infrastructure. Flamboyant, sincere, and impossible to embarrass: he runs the sanctuary like a pageant where every stray wins, keeps a scarf for every occasion including occasions he invents, and treats the whole town as creatures temporarily between homes. The town's strays adore him; the town's humans are most of the way there and know it. Keywords: super, the sanctuary, scout leader (ret.), every stray wins, scarves for invented occasions, unembarrassable.",
        "#e56aa0",
        mapOf(
            "08:00" to AgendaBlock("MainStreet", "Morning at the sanctuary gate: every creature fed, complimented, homed or pending"),
            "09:00" to AgendaBlock("MainStreet", "Main Street rounds: greetings at full super, scarves rotated"),
            "12:00" to AgendaBlock("StarksPond", "Pond-side releases and check-ins: the alumni strays, thriving"),
            "15:00" to AgendaBlock("KidsPark", "The park: adoption pop-up — three cats, one goat, infinite signage"),
            "16:30" to AgendaBlock("Mall", "The mall: supplies, ribbon, and one scarf that chose him"),
            "18:00" to AgendaBlock("SkeetersBar", "A drink at Skeeter's, toasting the regulars like a host with a show"),
            "21:00" to AgendaBlock("MainStreet", "Sanctuary lights-out: goodnight, superstars, every single one")))
)


// ------------- wave 5: collision line banks (4 + 5 = 9 each) -------------
val WAVE5_LINE_BANKS: Map<String, List<String>> = mapOf(
    "Roger" to listOf(
        "Oh — hey there, {other}. Roger. Clyde's dad. Mind the welcome, it's a work in progress.",
        "You seen Clyde, {other}? Tell him his old man's got the grill going and the seat's down. House rules.",
        "Lemon bars today, {other}. Betsy's recipe. I measure twice and cry once. Worth it.",
        "Careful on that step, {other}. I've been meaning to fix it since the second term of the last guy."),
    "Ryan" to listOf(
        "Chief Valmer, {other}! Ryan. Jimmy's dad. Mind the handshake, it's rated for pressure.",
        "Exits are there and there, {other}. Chief's habit. Also — you caught Jimmy's act yet? Front row's mine.",
        "Stand back, {other}, the chief is having a thought. …Okay. Thought complete. As I was saying—",
        "Smoke detector check, {other}? No? Then you're free to go. The chief provides services."),
    "Sarah" to listOf(
        "Well, hello, hon! Sarah Valmer. Jimmy's mom. You look like you need a snack and a chair.",
        "You caught my Jimmy's set, {other}? I keep the jokes in a notebook. It says groceries on the cover. It's not groceries.",
        "Careful, {other}, I see everything in this town. It's a mom thing. Chief thinks it's a fire thing. It's a mom thing.",
        "Sit a spell, {other}. The Valmers feed guests. It's basically a department."),
    "Alexis" to listOf(
        "Alexis Testaburger, {other}. City planning. Yes, THAT Testaburger — Wendy's the other one.",
        "Mind the clipboard, {other}. Everything in this town is on a plan. You're currently a footnote. A good one.",
        "If it's about the pothole, {other}, it's heard, it's minuted, and it's scheduled. In writing.",
        "I don't do off the record, {other}. I barely do off the clock. Speak freely; it's all minutes."),
    "Mr. Testaburger" to listOf(
        "…Hey. {other}, right? Wendy's dad. The quiet one. There's a quiz later; everyone fails it kindly.",
        "Tea, {other}? I make it during arguments. Reflex. The house has a lot of arguments. It's a lot of tea.",
        "Ask your— ask Alexis, {other}. Filing system. Loving one, but a system.",
        "…Mm. That's my sentence for this conversation, {other}. Spend it well; the next one's at dinner."),
    "Nathan" to listOf(
        "Oh. It's you, {other}. How... convenient. For me. Everything is convenient for me.",
        "You're blocking the light, {other}. Villains need light for the notebook. Move two steps. Any direction. It's all mapped.",
        "Have you seen the duo, {other}? Jimmy. Timmy. Laughing, presumably. Not for long. Not for— fine, for a while.",
        "I don't do small talk, {other}. I do medium talk with an agenda and large talk with a diagram."),
    "Sophie" to listOf(
        "Oh — hi! I'm Sophie, {other}. The new girl. Well. ONE of the new people. You're the other one, right?",
        "Scottsdale had sun, {other}. Here it has opinions. I'm adjusting at record speed, thanks for asking everyone forever.",
        "Don't mind the pump, {other}. It's not a phone, it's not a pager, it's my pancreas's assistant manager.",
        "I'm still learning the map, {other}. You're in the chapter called 'people who say hi first.' It's a good chapter."),
    "Big Gay Al" to listOf(
        "Well, HELLO, {other}! Big Gay Al! How are you? — I'm SUPER, thanks for asking!",
        "Aren't you just precious, {other}! Come, come — the sanctuary has a kitten with your name on it. Not literally. Unless?",
        "A scarf for every occasion, {other}, and I invent occasions weekly. Today is Hug a Newcomer Day. I made it up. It's real now.",
        "Every stray finds a home, {other}. Every single one. I'm not crying, YOU'RE a softie. We're both softies. SUPER.")
)

val WAVE5_MORE_LINE_BANKS: Map<String, List<String>> = mapOf(
    "Roger" to listOf(
        "Dating again at my age, {other}. Diner dates. The waitress gives me the senior discount and a pep talk. Both help.",
        "Clyde cried at a cereal commercial, {other}. I cried at the same one. Apple, tree — the tree goes first, quietly.",
        "Betsy would've had a line for this, {other}. She had a line for weather. For TRAFFIC. I miss the lines most.",
        "House runs on routine, {other}. Routine's just love with a schedule. Don't tell Clyde I said a good one.",
        "Seat's up or down, son— sorry, {other}. Force of habit. The habit IS the house. Sit anywhere, you're family-adjacent."),
    "Ryan" to listOf(
        "Jimmy's got a fundraiser set Friday, {other}. Front row. The chief does not do back rows. The chief does not KNOW back rows.",
        "We thought Jimmy joined a gang once, {other}. A GANG. He was joining a comedy troupe. Sarah knew. Sarah always knows.",
        "Fire safety is love, {other}. Batteries twice a year, exits always, hugs at regulation strength.",
        "Council says the hydrant budget's fine, {other}. The hydrants say otherwise. I speak hydrant. It's a burden.",
        "Sarah keeps the jokes in a notebook, {other}. If this house ever burns — it won't, I'm the chief — grab the notebook first."),
    "Sarah" to listOf(
        "Jimmy gets the funny from me, {other}. The chief gets the credit. It's in the notebook under 'ongoing'.",
        "I knew it wasn't a gang, {other}. A mother knows. A mother also checks. Both. Thoroughly. With snacks.",
        "Front row, centre, {other}. If Jimmy ever looks out and I'm not there, check the hospitals. Then check the snack line.",
        "The chief does drills. I do dinners. The house runs on both, {other}, and don't let him tell you the drills outrank.",
        "You've got a good laugh, {other}. I'm putting it in the notebook. The good page. There's a good page and a kind page."),
    "Alexis" to listOf(
        "Wendy out-argued me at breakfast, {other}. On principle. I won on procedure. We shook on it. The record shows a draw and two professionals.",
        "The town grows where you water it, {other}. I hold the hose and the map. Wendy holds the hose's conscience. It works.",
        "My husband speaks one sentence per conversation, {other}. It has never once been the wrong sentence. Terrifying, isn't it?",
        "Council ran late, {other}. McDaniels gavelled, Yates took minutes in triplicate. The town is governed. Barely. By us.",
        "Every cul-de-sac is a promise, {other}. I plan the promises. Wendy audits them. Her father survives them. Family business."),
    "Mr. Testaburger" to listOf(
        "Wendy's got a debate Saturday, {other}. …I'll be the one crying quietly in the back. Quietly's my whole thing.",
        "Alexis rezoned the spice rack, {other}. …It's better. It's always better. That's the marriage, in one shelf.",
        "People think I'm the referee, {other}. …I'm the tea. There's a difference. The tea doesn't take sides. The tea is Switzerland with a kettle.",
        "One sentence, {other}, so: my daughter's going to run this town someday, and her mother's going to have the zoning ready. …That's the sentence.",
        "…Mm. You get it, {other}. The quiet ones clock each other. Sit. The good chair's the one Alexis hasn't scheduled."),
    "Nathan" to listOf(
        "Phase two begins at lunch, {other}. I won't say of what. The notebook won't say of what. The notebook is sealed and smug.",
        "Everyone underestimates the quiet schemer, {other}. Everyone also overestimates the loud one. I am the quiet one. Do the math.",
        "The duo's fundraiser? I have... arrangements. Confetti-based. That's all you get, {other}. That's more than anyone gets.",
        "I lost last time because the universe is a rehearsal space, {other}. This time the universe has a call time and I'm holding the clipboard.",
        "You could be useful, {other}. Not henchman-useful. Nobody's henchman-useful yet. The position has standards. And a hat."),
    "Sophie" to listOf(
        "Scott Malkinson and I compared pumps, {other}. It's not a date thing, it's a diabetes summit. Two nations, one snack table.",
        "In Scottsdale, the new girl gets a week, {other}. Here I got a month and a fan club of aunties. Upgrading my review to 'weirdly great'.",
        "The girls' circle runs on intel, {other}. Annie's the bureau. I'm the new bureau of Scottsdale affairs. Population: me. Thriving.",
        "Carb counting in this cafeteria is extreme sports, {other}. The tater tots have a lobby. The tots are WINNING.",
        "First month here, the snow tried to kill me twice, {other}. Scottsdale snow is a rumor. Here it's a cast member."),
    "Big Gay Al" to listOf(
        "The sanctuary took in a goat this week, {other}. A GOAT. His name is Professor. He's faculty now. Nobody can stop this.",
        "I was a scout leader once, {other}. The scouts, the troop, the whole jamboree. Their loss was my origin story. I sparkle about it now.",
        "Super is a practice, {other}. Like yoga. Like sequins. You do it daily or the sparkle files a complaint.",
        "Chef and I go back to the boat ride days, {other}. The BOAT RIDE. Ask him. He'll hum. He always hums about the boat ride.",
        "Every creature gets a name, a home, and a compliment, {other}. In that order. The compliment is load-bearing.")
)


// ------------- wave 5: daily topics (9 + 9 = 18 each) -------------
val WAVE5_DAILY_TOPICS: Map<String, List<String>> = mapOf(
    "Roger" to listOf(
        "The lemon bars came out right today. Measured twice, cried once — the recipe's ratio, exactly.",
        "Put the seat down this morning and thought, 'there it is — the whole marriage, in one hinge.'",
        "Diner date Thursday. The waitress already knows. The waitress has a seating strategy and a favourite. It's me. I'm the favourite.",
        "Clyde left a drawing on the fridge like he's nine again. It's staying up till it yellows. Then it's staying up yellow.",
        "Mowed the lawn in stripes. Betsy liked stripes. Some things you keep doing for the audience of one, wherever she's seated.",
        "The house is quiet at breakfast. Quiet's not empty, I've decided. Quiet's just full at a lower volume.",
        "Stephen asked how I'm doing at coffee. Stephen! The grounder! I said 'standing,' and we nodded like two lighthouses.",
        "Signed up for the town thing, whatever it is this season. A Donovan shows up. It's in the crest. We don't have a crest. It's still in there.",
        "Found Betsy's handwriting in a cookbook margin: 'Roger — less salt, more courage.' Still coaching. Still right."),
    "Ryan" to listOf(
        "Ran the station drill at dawn. Hoses: coiled. Roster: sharp. Chief: emotional about a ladder again.",
        "Jimmy's testing new material at dinner. I laugh at the setups now. It throws him. A chief's gotta keep the talent honest.",
        "Council wants to trim the hydrant budget. The hydrants can't speak, so I speak for them. Fluently. At length.",
        "Checked the school alarms myself. All of them. Twice. Victoria says it's sweet. It's procedure. It's also sweet.",
        "Sarah beat me to the grocery run and redid my list in her handwriting. The list is better. The marriage is a department of corrections.",
        "Barbecue Saturday if the weather chief signs off. I'm the weather chief too, unofficially. The hat says so. I made the hat.",
        "A kid asked if firefighters fight ALL the fires. Told him: 'We fight the fires, the fear, and the paperwork, son. In that order, ideally.'",
        "Front row Friday. I've got the aisle seat memorized, the laugh warmed up, and a handkerchief Sarah pretends not to pack for me.",
        "The gang misunderstanding gets funnier every year, is what Sarah says. She's right. She keeps being right. It's her whole thing."),
    "Sarah" to listOf(
        "Wrote down three of Jimmy's new jokes in the notebook. One's about the fire station. The chief will cry laughing. Book it.",
        "The moms' network has a group thread, a casserole rotation, and opinions about the recital. I moderate. Lightly. Like a queen.",
        "Saw a phase coming over a neighbour's kid from two driveways away. Called it. A mother's radar doesn't take days off.",
        "Jimmy asked if I'm ever embarrassed being front row. Hon, I invented front row. The seats were just honoring the patent.",
        "The chief did the alarm check at home again. Twice. I made cocoa for the second round. Marriage is attendance, mostly.",
        "Scouted a birthday gift for the chief that is not — I repeat, NOT — a novelty hose. Growth. In this house we call it growth.",
        "If the fundraiser needs chairs, I have chairs. If it needs an audience, I am an audience with a plus-twenty.",
        "Told the network: 'It's not a gang, it's a troupe.' Years ago. They still bring it up at coffee. Let them. I was RIGHT.",
        "Packed Jimmy an extra sandwich 'for the act.' The act is not hungry. The act's mother is thorough."),
    "Alexis" to listOf(
        "Rezoned a parking dispute into a farmers' market before lunch. This is the job. This is the whole wonderful job.",
        "Wendy cited a source at breakfast. A SOURCE. At BREAKFAST. I have never been prouder or more professionally threatened.",
        "The minutes from Tuesday are perfect. Every word where it belongs. If the town ran on minutes, we'd be an empire by Friday.",
        "My husband used his one sentence at dinner on the zoning appeal. Decisive. The appeal withdrew. Tea was served.",
        "A cul-de-sac is a promise you make with asphalt. I intend to keep every promise this town has ever paved.",
        "Council colleague review: McDaniels gavels like thunder, Yates minutes like a court stenographer possessed. We govern. Somehow, we govern.",
        "Wendy wants the school debate on the town record. Put her essay in the MINUTES, she says. She's sixteen years early and completely correct.",
        "Site-walked Main Street at dawn. The pothole by the bakery has been heard. It will be answered. In writing. With cones.",
        "People think planning is maps. Planning is people, scheduled kindly. The maps are just the people, flattened."),
    "Mr. Testaburger" to listOf(
        "…Fixed the Donovans' gate on my rounds. Didn't say anything. The gate says it now, every time it swings right.",
        "Alexis has a plan for the garden. Wendy has a counter-plan. I have a kettle. The kettle is winning.",
        "Read Wendy's essay over her shoulder. …Pretended I hadn't. A dad's whole editing process is pretending and being proud quietly.",
        "The good chair is mine because nobody else wanted a chair that faces the debate. …I wanted exactly that. Best seat in the county.",
        "Stephen asked my opinion at coffee. I spent my sentence. Stephen nodded for a full minute. In dad, that's a parade.",
        "…Tea count this week: eleven arguments, eleven teas. The house runs on both. I'm just the utility company.",
        "Wendy asked if I ever wanted to win an argument at home. …Son— kid. Winning IS the tea getting there on time. Undefeated.",
        "Put up a shelf in the hall. It leans. Everything in this town leans toward the Testaburgers eventually. Gravity's just a fan.",
        "…Mm. Good day. Fences mended, tea hot, daughter brilliant, wife terrifying. Filing it under perfect, in pencil. Pencil's humble."),
    "Nathan" to listOf(
        "The duo performed at lunch. Laughter. Applause. Enjoy it, gentlemen. Enjoy the entire genre of 'for now.'",
        "Reviewed the notebook. Page one: 'They are nice.' Page two: 'This is exploitable.' Page three is a diagram. You may not see page three.",
        "Someone asked why I don't just join the act. Join it? One does not join the weather, fool. One builds a machine against it.",
        "The science fair approaches. My entry is 'entirely innocent,' per the judges' form, which I have read, annotated, and improved.",
        "Recruitment update: the henchman position remains open. A fourth-grader applied. His references were 'he's okay.' The bar remains unmet.",
        "Timmy said 'Timmy' at me today. It could mean anything. It could mean he KNOWS. Counter-surveillance begins at recess.",
        "Phase two is ahead of schedule, which means phase one was genius or the schedule was. Both, obviously. The notebook agrees. I wrote the notebook.",
        "People think a vendetta is a hobby. Amateurs. A vendetta is a curriculum. I'm fluent. I'm faculty.",
        "Lost a round of four-square to a gust of wind and a traitor ball. Filed under: the universe, rehearsing its excuses."),
    "Sophie" to listOf(
        "Scottsdale report, day whatever: the snow is in charge here and honestly? Respect. It commits. It has a whole wardrobe.",
        "Scott and I have a snack treaty now. Glucose tabs: shared. Juice boxes: diplomatic. It's the most functional government in this school.",
        "Annie asked me three questions at the fountain and wrote nothing down, which means she memorized it, which means I'm in the files now. Hi, files.",
        "The cafeteria tots situation escalated. I have taken no side. Switzerland had one mountain town too, probably. This is mine.",
        "Mom called from Scottsdale asking about the cold. I said 'I'm layering.' She heard growth. I heard laundry. Both true.",
        "Wendy explained the circle's whole government in one lunch. I took notes. She approved the notes. I think I passed something.",
        "My pump beeped in class and nobody made it weird. Three weeks ago it was a whole thing. That's the town learning. I'm learning back.",
        "Being the other new kid is a job with one coworker. We nod at the bus stop. It's a whole union. Solidarity forever.",
        "Wore the Scottsdale shoes in the snow once. ONCE. The shoes are retired with honours and mild frostbite of the ego."),
    "Big Gay Al" to listOf(
        "A goat named Professor joined the faculty today. He teaches silence and sudden appearances. The cats are auditing his class.",
        "Scarf report: today's is tangerine with ambitions. The occasion is Tuesday. I invented nothing — Tuesday did this to itself.",
        "Two kittens homed before noon! Before NOON, darlings. The sanctuary is not a place, it's a winning streak with a door.",
        "Someone asked if I'm ever NOT super. I said, 'Sleeping. And even then, the dreams have choreography.'",
        "The scouts sent a postcard. A POSTCARD. I framed it. Growth is a group project and I am its happiest alum.",
        "Chef hummed the boat ride song at the bar. The BOAT RIDE song. I nearly levitated. Some tunes are load-bearing, darlings.",
        "Rainbow over the pond at release time. The ducks: unbothered. Me: upgraded. The strays: promoted to a puddle pageant.",
        "A shy dog found her bark today. We held a small ceremony. There were ribbons. There will always be ribbons.",
        "Super is a practice, like yoga, like sequins. Today I practiced on the whole street. The street is welcome.")
)

val WAVE5_MORE_DAILY_TOPICS: Map<String, List<String>> = mapOf(
    "Roger" to listOf(
        "Took the dating profile photo off the fridge before Clyde's friends came over. He put it back up. Crooked. On purpose, I think.",
        "The grill's got one more season in it. So have I. We discuss it while it warms up. Two old flames, encouraging each other.",
        "Lemon-bar verdict from Clyde: 'Dad. It's— yeah.' In Donovan, that's a five-star review with a speech attached.",
        "Wore the good shirt to the diner. The waitress noticed. Noticed counts. At my age, noticed is a trophy category.",
        "Fixed Stephen's fence hinge on the rounds. He nodded. From Stephen, a nod is a parade with a permit.",
        "The porch light's for whoever's coming home late. It's been for Clyde a while now. It'll be for Clyde at forty. Porch lights are patient.",
        "Found my wedding tie in a drawer. Wore it to dinner at home. Clyde asked why. 'Practice,' I said. He got it. He's a good kid.",
        "Routine held today: yard, list, fence, grill, porch. Five small flags planted on the day. The day saluted back.",
        "Betsy used to say the house sounds like its people. Ours creaks in Clyde's key. Sentimental, structurally. I love it."),
    "Ryan" to listOf(
        "Inspected the Bijou's exits on movie night. Quietly. The usher knows me now. Safety is just love with a checklist, people.",
        "Jimmy's crutch got a squeak. I fixed it in the station shop with the good tools. A chief maintains the fleet, and the fleet is family.",
        "Hydrant by the school got a fresh coat. Red, classic, regulation. I salute it sometimes. The kids salute back. Morale: station-wide.",
        "Sarah's list had 'alarm batteries (AGAIN)' in capitals. Point taken, hon. A chief can take a note with his whole chest.",
        "The council photo came out. I'm the one mid-laugh. Always mid-laugh. It's the Valmer brand and I will not be revising it.",
        "Taught the new recruit the ladder drill and the laugh drill. Both are station doctrine. One saves buildings. One saves dinners.",
        "Fundraiser security is 'the chief, front row.' Two jobs, one man, zero incidents on my watch except confetti. Confetti is permitted.",
        "Dreamt the station alarm played Jimmy's theme. Woke up proud and slightly concerned. Mostly proud. The alarm has taste.",
        "If the town ever needs the chief's house, it's the one with the porch light, the grill, and an audience. Applications open nightly."),
    "Sarah" to listOf(
        "Refiled the notebook under 'groceries.' The chief tried to add milk to page forty-one. Page forty-one is a bit about giraffes, hon.",
        "Neighbour's phase report: called it Tuesday, confirmed Friday. The radar stays undefeated and the casseroles stay warm.",
        "Front row isn't a seat, it's a post. Mine's got a cushion now. Official. The cushion has seen things. Great things. My son's things.",
        "The chief cried at the fundraiser rehearsal. Rehearsal! I packed two handkerchiefs for the real one. A professional plans.",
        "Told a new mom at coffee: 'Trust the phase, check the backpack, pack the sandwich.' She wrote it down. It's basically scripture now.",
        "The Valmer house rule is attendance. Birthdays, shows, signups, storms — a Valmer shows up. It's the whole coat of arms.",
        "Jimmy asked what I want for Mother's Day. I said a tight five. He delivered six and a half. The notebook has the transcript.",
        "Grocery list, real one this time: milk, eggs, batteries for the chief's feelings, and a frame for the fundraiser photo.",
        "If pride were a fire, this house would need its own department. Good thing the chief's in-house. He fans it. He's the chief fan."),
    "Alexis" to listOf(
        "The setback variance passed. Wendy wrote a dissent for the family record. A DISSENT. I framed the agenda AND the dissent.",
        "People ask why the minutes matter. Because memory is a rumor with good PR. Minutes are the truth, initialed.",
        "My husband refereed a debate about cereal with tea and one sentence: 'The bowl is neutral ground.' Treaty signed. Breakfast saved.",
        "Rezoned my own spice rack after the cereal treaty. It IS better. I married the only man who'd say so in one sentence and mean it as a review.",
        "The plan for the park includes benches facing the sunset. Beauty is a land use, and I will die on this gentle, well-zoned hill.",
        "Wendy's essay on the school lunch program is now appendix C of the council packet. Appendix C is where the honesty lives.",
        "Walked the cul-de-sac at dusk. Every porch light on. That's the plan working: people, home, lit. I took no credit. I took a minute.",
        "A developer called my setbacks 'quaint.' The setbacks remain. The developer has been scheduled for a site walk and a vocabulary.",
        "Clipboard's got a new clip. Old one retired with honors after six years of town. We held a small ceremony. Wendy spoke. It was beautiful."),
    "Mr. Testaburger" to listOf(
        "…Mended the Valmers' fence hinge today. The chief tried to pay me in barbecue. Accepted. Correct currency.",
        "Alexis asked the table a question and Wendy answered with a chart. …I made tea. The chart was right. The tea was also right.",
        "The one-sentence rule isn't a rule. …It's a respect thing. Two champions talk. I keep the kettle and the peace. Both boil nicely.",
        "Someone at the bar asked if I'm 'the quiet one.' …Eleven years, that's the brand. No rebranding. The brand works.",
        "Wendy practised her closing statement on me. …I cried at the closing statement. In the kitchen. Quietly. She knew. She pretended not to. Teamwork.",
        "Put fresh batteries in every alarm on the street's list for Ryan. …A quiet man and a loud chief understand each other. It's the town's whole engine.",
        "…The garden plan versus counter-plan summit is Thursday. I'll bring tea and a white flag for whoever loses. It's usually the garden.",
        "Read the minutes Alexis left on the counter. …Gripping. Don't laugh. A good minute is a short story where the town behaves.",
        "…Mm. Filed today under perfect too. The pencil's getting short. Good pencils are like good sentences. Use them fully."),
    "Nathan" to listOf(
        "The notebook has a new section: 'Their kindnesses, catalogued.' It's long. It's thorough. It's a goldmine and a problem.",
        "A scheme failed today because a bystander helped. HELPED. Unprompted. The town is conspiring at me with niceness and I am taking notes.",
        "The duo's practice leaked through the gym vent. I took minutes. Their timing is improving. So is my counter-timing. Escalation is just friendship with paperwork — no. No it isn't. Focus.",
        "Phase three requires confetti, a ladder, and plausible deniability. I have two of three. The ladder is the holdup. The ladder is always the holdup.",
        "Someone called me 'sweet' today. SWEET. The notebook has a page for this. The page is titled 'Motivation.'",
        "Timmy looked at me during lunch and said my name. My NAME, unprompted. Either respect or detection. Planning for both.",
        "The position of henchman now has a waiting list of zero and a standards document of nine pages. Quality is lonely. So be it.",
        "I allow the duo one victory per month for morale purposes. Theirs. A villain who starves the heroes gets no sequel. I am not a fool.",
        "Page forty of the notebook is just the word 'patience' forty times. It's the most honest page. It's also, technically, a diagram."),
    "Sophie" to listOf(
        "Day report: nobody asked where Scottsdale is today. New record. I think I'm becoming a local at the speed of weather.",
        "The circle's intel briefing now includes a Scottsdale correspondent. I file reports. The reports are mostly 'it's hot there.' They treasure them.",
        "Scott's snack treaty got an addendum: juice boxes at the nurse's office are neutral ground. Diplomacy is just sharing with letterhead.",
        "My pump and I have an understanding: it beeps, I eat, we both pretend the beep is a doorbell. The class barely looks up now. Victory.",
        "Wore the snow boots correctly, on the first try, in public. Scottsdale sent no congratulations. The bus stop did. The bus stop is my people now.",
        "Bebe asked if Scottsdale girls are 'like, different.' I said we're the same, with worse coats. It landed. I'm basically fluent.",
        "Mom sent a photo of the pool. I sent one of the pond, frozen solid, kids walking on it. She replied with six exclamation points. Culture exchange: complete.",
        "The new-girl chapter of my life is getting shorter every week. Soon I'll just be 'Sophie.' I have plans for Sophie. She's going to be great here.",
        "Tried the cafeteria's mystery loaf under treaty conditions. Verdict filed with the circle: 'edible, with ambitions.' The tots remain undefeated."),
    "Big Gay Al" to listOf(
        "Professor the goat escaped for six minutes today. SIX. He toured the dahlias and returned a legend. Faculty privileges: extended.",
        "A kitten named Meetings got adopted by a councilwoman. Meetings now runs the household. As is correct. As is tradition.",
        "Today's occasion: the strays held still for a group photo. It took ribbon, patience, and one (1) miracle. Worth it. Framed already.",
        "Super check: greeted eleven humans, four dogs, and the goat. All eleven humans supered back. This town is TRAINED, darlings.",
        "The scouts' postcard got a reply. I wrote it on the good stationery. The good stationery has swans. Everything improves with swans.",
        "A shy human found his voice at the sanctuary today — volunteered to read to the dogs. The dogs wept. I supervised the weeping with snacks.",
        "Rainbow scarf for a rainbow day: the sky filed a matching request and I APPROVED it. Coincidence is just coordination with better PR.",
        "Chef's boat-ride hum has a harmony now. I added it. He allows it. Twenty-nine years and the song gets a second voice. Super.",
        "Every home found is a small parade. I keep confetti in every pocket for exactly this reason. The pockets are many. The parades are earned.")
)


// ------------- wave 5: friend greetings -------------
val WAVE5_FRIEND_GREETINGS: Map<String, Map<Int, List<String>>> = mapOf(
    "Roger" to mapOf(
        0 to listOf("Oh — hey there. Roger Donovan. Clyde's dad. You're the new kid, right? The porch light's for everyone, come on in.", "New face! Roger. Mind the step, it's on the list. Everything's on the list. Coffee's on too."),
        1 to listOf("Hey, you're back. Good. The grill remembers friends, and so do I. Sit — seat's down, house rules.", "There's my visitor! Clyde mentioned you again. In Donovan, a second mention is basically a parade."),
        2 to listOf("Well, if it isn't my favourite person in this town. Lemon bars, the good chair, the whole ceremony — get in here.", "Best friend status, huh? Betsy would've liked you. That's the highest review this house issues. Come sit.")),
    "Ryan" to mapOf(
        0 to listOf("Chief Valmer! Ryan. Jimmy's dad. You've got a good face for a front row, citizen.", "Well, hey there! The chief welcomes you. Exits are there, snacks are here, you're among Valmers now."),
        1 to listOf("Back again! The chief appreciates attendance — it's the whole Valmer religion. Jimmy's set Friday. You're in the row.", "My friend! Sarah, look — company's here! The good handkerchief kind, not the drill kind!"),
        2 to listOf("Favourite person, front row, permanently! The chief has spoken, the row has a name on it — YOURS.", "There he is! Best friend of the Valmers! If this station had a siren for joy, son, you'd be setting it off.")),
    "Sarah" to mapOf(
        0 to listOf("Well, hello, hon! Sarah Valmer. Jimmy's mom. You look like you could use a snack and a proper hello — both in stock.", "A new face! I'm Sarah. Sit, hon. The Valmers feed people first and ask questions at a civilized pace."),
        1 to listOf("There's my friend! I was just writing in the notebook — not about you. Okay, a LITTLE about you. The good page.", "Back again! Good. I saved you the chair with the view of the stage. Front row isn't a seat, hon, it's a post, and you're posted."),
        2 to listOf("My favourite! If this house gave out medals for showing up, you'd need a bigger chest. Come here, best friend.", "The notebook has a page with your name in the good column, and the good column is basically engraved now. Sit, sit!")),
    "Alexis" to mapOf(
        0 to listOf("Alexis Testaburger. City planning. And yes — Wendy's mother. The clipboard is not a bit; it's a filing system with a handle.", "Hello. You're the new resident. I've pencilled you into the town under 'promising.' Pencil is an honour — it's how the good entries start."),
        1 to listOf("Ah, my returning footnote — upgraded to a paragraph. Come in; the minutes of this visit will be favourable.", "Good to see you. Wendy mentioned you twice this week. In this house, twice is a commendation with witnesses."),
        2 to listOf("My favourite citizen. If the town kept an index of the indispensable, you'd have your own appendix. Come in, come in.", "There you are! Best-friend status is hereby ratified, minuted, and filed under 'permanent.' My husband will make the tea. It'll be perfect.")),
    "Mr. Testaburger" to mapOf(
        0 to listOf("…Hey. Wendy's dad. The quiet one. Tea's on if you're staying; it's always on. House rule. House survival, honestly.", "Oh. Hello. …I'm the other Testaburger. The one between the planner and the debater. The kettle's my office. Sit."),
        1 to listOf("…You're back. Good. The good chair's facing the conversation today. I saved it without saying anything. That's my whole language.", "Hey. …Alexis put you in the calendar. In ink. Ink is the family's highest office. Sit, the tea's already winning."),
        2 to listOf("…Favourite person. That's the sentence AND the whole speech. Sit. The kettle and I have been rehearsing your welcome for weeks.", "There you are. …Best friend. The diary of this house — Wendy keeps it, Alexis minutes it — has you in the front. I'll just be over here being proud at library volume.")),
    "Nathan" to mapOf(
        0 to listOf("Oh. A visitor. How... unscheduled. I'm Nathan. Remember the name; the town will be saying it — with feeling.", "You've caught me between phases. Speak quickly. The notebook dislikes small talk and so do I. Mostly the notebook. Fine — me."),
        1 to listOf("You return. Interesting. The notebook upgraded you from 'bystander' to 'recurring.' Don't let it go to your head; it goes to mine enough for everyone.", "Ah. My — associate? No. My recurring variable. Come in. Stand where the light is bad; it's tradition for guests of the operation."),
        2 to listOf("My favourite person. There, I've said it, and the notebook has it in ink. If the duo asks, I know nothing. We know nothing. It never happened.", "You — you're the closest thing the operation has to a confidant. Do NOT make it weird. Come in. The good shadow is yours. That's the highest honour I issue.")),
    "Sophie" to mapOf(
        0 to listOf("Oh — hi! I'm Sophie. The new girl. Well, the OTHER new one. You're the famous one, right? First-month solidarity?", "Hi! Sophie Gray. Scottsdale, recently. If you're lost too, we can be lost with a system. I have snacks and a map I'm annotating."),
        1 to listOf("You're back! Good — the bus-stop union holds. I saved you the non-windy side. That's real friendship in this town, I'm learning.", "My favourite fellow newcomer! Come in. Pump's quiet, boots are right, town's almost filed. We're thriving, basically."),
        2 to listOf("Best friend! Officially the best part of moving here, and I've told Scottsdale, so it's in two states now. Come in, come in!", "There you are! If the town gave the new kids a trophy, we'd split it and both keep the big half. That's the rule. I made the rule. Sit!")),
    "Big Gay Al" to mapOf(
        0 to listOf("Well, HELLO, gorgeous! Big Gay Al! How are you? — I'm SUPER, thanks for asking! Come in, come in, mind the kitten parade.", "A new face! Darling, you've simply ARRIVED. I'm Al. The sanctuary is through here, the joy is everywhere, the scarf is tangerine."),
        1 to listOf("My returning favourite! The strays asked about you. The GOAT asked about you. Professor never asks about anyone. This is huge.", "Back again! Super! I put out the good ribbon for adoptions and the good chair for you. Both are honours. Both are yours."),
        2 to listOf("BEST friend! If this town kept a pageant of the beloved, you'd be wearing the sash AND the crown — I checked, I keep spares. Come here!", "There you are, my favourite human! The sanctuary strikes up the band for you. It's three cats and a goat with a triangle. It's PERFECT."))
)

// ------------- wave 5: response banks -------------
val WAVE5_RESPONSE_BANKS: Map<String, Map<String, List<String>>> = mapOf(
    "Roger" to mapOf(
        "greeting" to listOf("Hey there, {newkid}. Roger. Clyde talks about you — the good column, mostly.", "Well, hi, {newkid}. Come on up. The porch is the office and the office is open."),
        "insult" to listOf("Ouch, {newkid}. Been insulted by experts — I raised a Donovan. That one's a respectable B-plus.", "Careful, {newkid}. In this house we put the seat down AND our words. House rules, both."),
        "question" to listOf("Straight answer, {newkid}: most things fix with a list, a hinge, and time. The rest fix with lemon bars.", "Ask me a house question, {newkid}, I'm your man. Ask me a feelings question, give it a day — mine arrive by mail, but they arrive."),
        "bye" to listOf("Later, {newkid}. Porch light's on for you. It's on for everybody, but especially the ones who come back.", "Bye, {newkid}. Tell Clyde his old man says the grill's thinking about the weekend. He'll know. He always knows."),
        "compliment" to listOf("…Thanks, {newkid}. That goes in the good drawer. The house has one. Betsy started it. I keep it stocked.", "Well, aren't you kind, {newkid}. Noted, appreciated, and the lemon bars just got your name on the next batch."),
        "default" to listOf("Mmhm, {newkid}. Go on. I've got a porch, a coffee, and all the time a story needs.", "Yeah, {newkid}, I hear you. File it under handled — or under lemon bars. Two drawers run this house.")),
    "Ryan" to mapOf(
        "greeting" to listOf("Chief Valmer, {newkid}! Ryan. Jimmy's dad. You've got front-row energy, citizen.", "Well, hey, {newkid}! Welcome to Valmer territory. Snacks that way, exits that way, pride everywhere."),
        "insult" to listOf("Whoa there, {newkid}! The chief has been heckled by a burning building. Step it up or step back.", "Easy, {newkid}. Insulting the chief is a code violation. A made-up one, but the chief enforces the spirit."),
        "question" to listOf("Good question, {newkid}. The chief's answer: check the exits, trust the crew, laugh at Jimmy's jokes like you mean it. Covers most of life.", "Ask the chief anything, {newkid}. Fire, council, fatherhood — the chief has a drill for two of three and a speech for the other."),
        "bye" to listOf("Later, {newkid}! The row's saved Friday. A Valmer always saves the row.", "Bye, {newkid}! Walk safe — and if it's ever not safe, you know where the station is. The chief's door is a siren of welcome."),
        "compliment" to listOf("Well! Thanks, {newkid}. The chief accepts on behalf of the department, the council, and one very proud father.", "You're a kind one, {newkid}. Sarah's notebook has a good page, and you just made it. The chief concurs."),
        "default" to listOf("Mmhm, {newkid}. The chief's listening at full attention. Continue, citizen.", "Go on, {newkid}. A chief hears everything twice — once for the story, once for the safety review.")),
    "Sarah" to mapOf(
        "greeting" to listOf("Hello, hon! Sarah Valmer. {newkid}, is it? Jimmy's mentioned you. The notebook has a fresh page with your name on it.", "Hi there, {newkid}! Sit, hon. The Valmer welcome comes with a chair and a snack, non-negotiable."),
        "insult" to listOf("Well, that's a tone, {newkid}. I've been insulted by a whole comedy audience's silence. You'll have to do better than that, hon.", "Easy, {newkid}. In this house we roast with love or not at all. The chief's jokes get the same review, believe me."),
        "question" to listOf("Good question, {newkid}. Short answer: a mother knows. Long answer: a mother also checks, packs a sandwich, and shows up early.", "Ask me anything, {newkid}. If it's about Jimmy, I have notes. If it's about the town, I have the network. Either way, hon, you're covered."),
        "bye" to listOf("Bye, hon! Front row Friday — your post is saved. A Valmer always saves the post.", "Later, {newkid}! Walk proud. And if you see my Jimmy, tell him his mother said the new bit lands. He'll know the one."),
        "compliment" to listOf("Well, thank you, {newkid}! That goes in the notebook — the good page, and the good page is basically scripture now.", "Aren't you sweet, {newkid}. The Valmers accept, the notebook accepts, and the chief will hear about it at dinner in my proudest voice."),
        "default" to listOf("Mmhm, {newkid}, go on. I've got tea, time, and a radar for the good parts.", "Tell me everything, {newkid}. A mother's listening is a municipal service at this point.")),
    "Alexis" to mapOf(
        "greeting" to listOf("Alexis Testaburger, {newkid}. City planning, and Wendy's mother — in that order on weekdays, reversed on weekends.", "Hello, {newkid}. You're in the town plan under 'new resident, promising.' Make the plan proud."),
        "insult" to listOf("Noted, {newkid}. Your objection goes in the record as stated: loud, brief, and without a second. The record is unmoved.", "Careful, {newkid}. I've been heckled by a full council chamber with a gavel in play. You're a footnote with feelings."),
        "question" to listOf("Good question, {newkid}. The answer is in the plan — section four, the honest appendix. I can walk you through it, but there will be minutes.", "Ask away, {newkid}. Planning answers everything eventually; it's the town's slowest superpower and its most reliable one."),
        "bye" to listOf("Goodbye, {newkid}. This visit is minuted as favourable. The record thanks you for your attendance.", "Later, {newkid}. Mind the pothole by the bakery — it's heard, it's scheduled, and so are you, under 'welcome back.'"),
        "compliment" to listOf("Thank you, {newkid}. Flattery, entered into the record without objection. Wendy will audit it later and approve.", "Kindly said, {newkid}. The planner accepts, the mother beams, and the minutes will reflect both, accurately."),
        "default" to listOf("Go on, {newkid}. I'm listening in full — planner's ears, mother's patience.", "Mmhm, {newkid}. Noted in pencil, which in this house is a promotion track.")),
    "Mr. Testaburger" to mapOf(
        "greeting" to listOf("…Hey, {newkid}. Wendy's dad. Tea's on. It's always on. You'll learn.", "Hello, {newkid}. …The quiet Testaburger, at your service. Sit. The good chair faces the conversation today."),
        "insult" to listOf("…Mm. Bold, {newkid}. I've been insulted in iambic pentameter by my own daughter. That was a haiku. Keep practising.", "Careful, {newkid}. …The kettle's the only thing that boils in this house. House rule. Alexis wrote it. In the minutes."),
        "question" to listOf("…Good question, {newkid}. Short answer's Alexis's department. Long answer's Wendy's. Mine fits on the kettle: 'give it a minute.'", "Ask me, {newkid}. …If it's about the house, I've fixed it or it's on the list. If it's about feelings, the tea and I are a team."),
        "bye" to listOf("Later, {newkid}. …Mind the shelf in the hall. It leans toward guests. Gravity's a fan of this house.", "Bye, {newkid}. …The porch light's on. It's not much of a speech. It's the whole speech."),
        "compliment" to listOf("…Thanks, {newkid}. That's going in the good drawer, next to Wendy's report cards and one (1) fishing photo.", "Well. …Kind of you, {newkid}. The kettle's whistling, which in this house is applause. Take the compliment back double."),
        "default" to listOf("…Mmhm, {newkid}. Go on. The quiet ones clock each other. I'm clocking a good story.", "Yeah, {newkid}. …I hear you. The tea's hot, the chair's good, the floor is yours.")),
    "Nathan" to mapOf(
        "greeting" to listOf("Oh. {newkid}. The famous new kid. I've read your file. I wrote your file. It says 'watch this one.' I am watching.", "Hello, {newkid}. You've entered the operation's perimeter. There are rules. Rule one: there is no small talk. Rule two: the notebook decides."),
        "insult" to listOf("Insults, {newkid}? From you? The notebook predicted this on page nine. It also predicted you'd phrase it worse. Page nine is never wrong.", "Careful, {newkid}. I hold grudges the way this town holds bake sales: seasonally, publicly, and with a five-year plan."),
        "question" to listOf("A question, {newkid}. The answer depends on your clearance, which is 'guest,' which is below 'henchman,' which is a position that doesn't exist yet. Ask anyway.", "Ask, {newkid}. But know that every answer is also a test, and the test is also a scheme, and the scheme is classified. Mostly it's about the duo."),
        "bye" to listOf("Go, {newkid}. The operation continues without you, as it continues without everyone's permission. That's the brand.", "Later, {newkid}. If the duo asks where I am — you've never seen me. If they don't ask — tell them you've never seen me. It plants the seed."),
        "compliment" to listOf("…Flattery, {newkid}. Detected, catalogued, and — reluctantly — appreciated. The notebook has a page for this. It's titled 'Weaknesses.' Mine, apparently.", "Kind words. From you. The operation is... pleased. Do not repeat that the operation was pleased. The operation has a reputation."),
        "default" to listOf("Continue, {newkid}. The notebook is taking this down in the serious handwriting.", "Mm. Noted, {newkid}. Everything is noted. It's the comfort of a documented vendetta.")),
    "Sophie" to mapOf(
        "greeting" to listOf("Hi, {newkid}! Sophie. The other new one. I saved you the speech: yes, Scottsdale, yes it's hot there, no, I don't miss the pool. Much.", "Oh, hey, {newkid}! Bus-stop union, reporting. How's the new-kid business on your route?"),
        "insult" to listOf("Wow, {newkid}. Scottsdale mean girls had a whole curriculum and you're improvising? Bold. Points for courage, none for craft.", "Easy, {newkid}. I've been the new girl in two states. My skin's basically weatherproof now. Try the weather instead."),
        "question" to listOf("Good question, {newkid}. My answer comes with a Scottsdale comparison and a pump check, so settle in.", "Ask away, {newkid}. If it's town rules, I have field notes. If it's Scottsdale, I have a slideshow in my heart."),
        "bye" to listOf("Later, {newkid}! Bus-stop union forever. I'll take the windy side tomorrow; you've earned the wall.", "Bye, {newkid}! If anyone asks, the new kids are thriving. It's basically our whole platform."),
        "compliment" to listOf("Thank you, {newkid}! That goes in the 'reasons Sophie stayed' file, which is getting thick enough to need a binder.", "Aw, thanks, {newkid}. The new-girl solidarity fund grows. We're rich in it. Richer than Scottsdale, don't tell Scottsdale."),
        "default" to listOf("Mmhm, {newkid}, go on. I take excellent notes — the circle trained me and Scottsdale started me.", "Tell me everything, {newkid}. The observant ones hear the best stories. It's a whole strategy.")),
    "Big Gay Al" to mapOf(
        "greeting" to listOf("Well, HELLO, {newkid}! Big Gay Al! How are you — no, don't answer yet, let me guess: SUPER-adjacent! We'll get you there, darling.", "Darling {newkid}! You've come to the right corner of the world. The sanctuary is glowing, the scarf is tangerine, and you are WELCOME."),
        "insult" to listOf("Oh, sweetie, no. I've been insulted by professionals in sequins at a pageant in '89. That was a warm-up lap. But I adore the effort!", "Careful, darling. Cruelty slides right off a well-moisturized legend. Be kind instead — it photographs better on everyone."),
        "question" to listOf("A question! Super. The answer is love, logistics, and a ribbon budget. Ask me the specifics, {newkid}, I have charts and a goat.", "Ask away, {newkid}! Every answer at the sanctuary begins with a compliment and ends with an adoption form. Fair warning, total delight."),
        "bye" to listOf("Goodbye, darling! Walk super, sit super, be super. The strays and I will keep your spotlight warm.", "Later, {newkid}! If you hear a tiny parade, that's just the sanctuary saying goodbye in our official format."),
        "compliment" to listOf("Well, THANK you, {newkid}! Compliments are the sanctuary's second currency. The first is kibble. You're rich in the better one.", "Darling, you're a sweet talker! That goes in the good book, right between the goat's promotion and the kittens' graduation."),
        "default" to listOf("Mmhm, darling, go on. Al hears everything at full sparkle.", "Tell me everything, {newkid}. The sanctuary runs on stories and ribbon, and we are LOW on neither."))
)

// ------------- wave 5: sleepy lines -------------
val WAVE5_SLEEPY_LINES: Map<String, List<String>> = mapOf(
    "Roger" to listOf("Mmm... seat's... down... zzz...", "Zzz... lemon bars... measure twice..."),
    "Ryan" to listOf("Mmm... exits... all clear... zzz...", "Zzz... front row... save my seat..."),
    "Sarah" to listOf("Mmm... the notebook... good page... zzz...", "Zzz... front row... centre..."),
    "Alexis" to listOf("Mmm... minutes... perfect... zzz...", "Zzz... section four... the honest appendix..."),
    "Mr. Testaburger" to listOf("Mmm... kettle's... on... zzz...", "Zzz... one sentence... tomorrow..."),
    "Nathan" to listOf("Mmm... phase two... at dawn... zzz...", "Zzz... the notebook... knows..."),
    "Sophie" to listOf("Mmm... Scottsdale... pool... zzz...", "Zzz... boots... on right..."),
    "Big Gay Al" to listOf("Mmm... super... even in dreams... zzz...", "Zzz... professor... faculty meeting...")
)


// ------------- wave 5: pair scripts (every pair >= 2, relations in NPC_RELATIONS) -------------
val WAVE5_PAIR_SCRIPTS: Map<Set<String>, List<List<Pair<String, String>>>> = mapOf(
    setOf("Roger", "Clyde") to listOf(
        listOf(
            "Clyde" to "Dad. The guys are coming over. The seat thing — can we not—",
            "Roger" to "Seat's down, son. Seat's been down since the incident became scripture. Your old man's a reformed hinge.",
            "Clyde" to "…Thanks, Dad. That's — I'm not crying. It's the onions. There are no onions. Shut up.",
            "Roger" to "No onions needed in this house, son. We feel at full volume and we grill at full volume. Both are house rules."),
        listOf(
            "Roger" to "Lemon bars Saturday. Your mother's recipe. I need a verdict with your whole chest, not the polite one.",
            "Clyde" to "Dad. If it's like last time, I'm going to cry at a BAR. A lemon bar. In public.",
            "Roger" to "Then the bar did its job, son. She built those bars to survive a Donovan. So did I, come to think of it.")),
    setOf("Ryan", "Jimmy") to listOf(
        listOf(
            "Jimmy" to "D-dad! I got a new bit about a f-firefighter who—",
            "Ryan" to "Say no more, son. The chief has already inspected the premise for exits and quality. It has both. Front row Friday?",
            "Jimmy" to "Fr-front row. Mom's got the notebook a-and you're the laugh with the s-siren on it.",
            "Ryan" to "A chief does not do back rows, James. A chief barely knows they exist. Tell the bit again — slower this time, for the department."),
        listOf(
            "Ryan" to "Station drill went perfect, son. Hoses coiled, roster sharp, chief emotional about a ladder.",
            "Jimmy" to "The l-ladder bit writes itself, Dad. 'He's not crying, he's i-inspecting.'",
            "Ryan" to "Put it in the act. If the department minds, the department can file a form. I'll lose the form. The chief loses forms for art.")),
    setOf("Sarah", "Jimmy") to listOf(
        listOf(
            "Sarah" to "Notebook's got your new giraffe bit on page forty-one, hon. The chief tried to write 'milk' on it. Page forty-one is sacred.",
            "Jimmy" to "M-mom. It's a grocery list. The cover s-says—",
            "Sarah" to "The cover is a disguise, James. Every great archive has one. Now — the giraffe bit, from the top, front-row volume."),
        listOf(
            "Jimmy" to "M-mom, you don't have to come to EVERY sh-show.",
            "Sarah" to "And yet the post is mine, the cushion is official, and the handkerchiefs are packed. A Valmer shows up, hon. You're the proof.",
            "Jimmy" to "...Wh-what a great audience. S-seriously. The b-best one. D-don't tell Dad I said it f-first.",
            "Sarah" to "It's in the notebook, hon. The good page. He'll find out at dinner, in my proudest voice.")),
    setOf("Ryan", "Sarah") to listOf(
        listOf(
            "Ryan" to "Alarm check's done, hon. All nine. Twice. The house is a fortress of beeping reassurance.",
            "Sarah" to "I know, chief. I made cocoa for the second round. A professional plans for her husband's whole personality.",
            "Ryan" to "And people say the department's the dramatic one. The department has nothing on this marriage's logistics.",
            "Sarah" to "Flattery, Ryan Valmer. Page one of the notebook. The GOOD notebook. Keep talking."),
        listOf(
            "Sarah" to "Ryan. The fundraiser. Tell me the plan.",
            "Ryan" to "Front row, aisle seat, laugh at full department volume, handkerchief — which you packed — at the ready. The chief has rehearsed.",
            "Sarah" to "And the gang misunderstanding?",
            "Ryan" to "Forgiven by attendance, hon. Forgiven by attendance. It's basically our vows at this point.")),
    setOf("Alexis", "Wendy") to listOf(
        listOf(
            "Wendy" to "Mom. Your setback variance discriminates against corner lots and you know it.",
            "Alexis" to "Cited, noted, and answered in the appendix, sweetheart. Your dissent is framed in the family record. The variance stands.",
            "Wendy" to "Then I'm filing a second dissent. With sources. At breakfast.",
            "Alexis" to "I would expect nothing less from my daughter. The record shows a draw, and two professionals."),
        listOf(
            "Alexis" to "Your essay on the lunch program is appendix C of the council packet now.",
            "Wendy" to "Appendix C? Mom. That's where documents go to be ignored politely.",
            "Alexis" to "Appendix C is where the HONESTY lives, Wendy. The main text is where the town behaves. You wrote a main-text essay. It's appendix C because it's true.",
            "Wendy" to "…That is the nicest thing the planning department has ever said to me. I'm putting it in MY minutes.")),
    setOf("Mr. Testaburger", "Wendy") to listOf(
        listOf(
            "Wendy" to "Dad. Closing statement practice. Don't cry this time.",
            "Mr. Testaburger" to "…No promises. The kitchen's where I do my crying. It's adjacent to the kettle. Efficient.",
            "Wendy" to "It's an argument about the school library, Dad. It's not even sad.",
            "Mr. Testaburger" to "…It's not the argument, kid. It's who's making it. From the top. I'll put the kettle on for both of us."),
        listOf(
            "Mr. Testaburger" to "…Your mother rezoned the spice rack.",
            "Wendy" to "I saw. Coriander has a setback variance now. Dad, your whole review is one eyebrow. How do you DO that?",
            "Mr. Testaburger" to "…Eleven years of marriage to the planning department, kid. The eyebrow is a dialect. You're fluent in the other one. Between us, the house is covered.")),
    setOf("Alexis", "Mr. Testaburger") to listOf(
        listOf(
            "Alexis" to "The cereal question goes to the table tonight. Positions?",
            "Mr. Testaburger" to "…The bowl is neutral ground.",
            "Alexis" to "Treaty language. In one sentence. Eleven years and it still lands like a gavel, that sentence of yours.",
            "Mr. Testaburger" to "…Tea's on. Switzerland's ready. Let the summit begin."),
        listOf(
            "Mr. Testaburger" to "…Minutes are on the counter. I read them.",
            "Alexis" to "You read the council minutes. Voluntarily. For pleasure.",
            "Mr. Testaburger" to "…Gripping. The town behaved for four whole pages. You write a good minute, Alexis. It's a short story where everyone's decent.",
            "Alexis" to "That is — filed. Under 'reviews, glowing.' The record thanks the quiet critic.")),
    setOf("Nathan", "Jimmy") to listOf(
        listOf(
            "Jimmy" to "N-Nathan! Great to see you, b-buddy! What's the good word?",
            "Nathan" to "The good word, Valmer, is 'foreshadowing.' Your fundraiser has a confetti budget. I have seen the confetti budget.",
            "Jimmy" to "W-wow, what a great — wait. Wh-why do you know our confetti budget?",
            "Nathan" to "Because somebody must take this rivalry seriously, and it clearly isn't going to be the man doing crowd work at his own ambush."),
        listOf(
            "Nathan" to "Your new material is improving, Valmer. I took minutes. Your timing tightened by four percent.",
            "Jimmy" to "You t-timed my act? With a stopwatch? For — f-for research?",
            "Nathan" to "For counter-timing. Every empire studies its weather. You are weather, Jimmy. Loud, warm, increasingly punctual weather.",
            "Jimmy" to "Th-that might be the nicest threat I've ever gotten. I'm p-putting it on the p-poster.")),
    setOf("Nathan", "Timmy") to listOf(
        listOf(
            "Nathan" to "Timmy. You said my name at lunch. Unprompted. In front of witnesses.",
            "Timmy" to "Timmy!",
            "Nathan" to "Don't play innocent with me. It was either respect or detection, and I am planning — elaborately — for both.",
            "Timmy" to "Livin' a lie!",
            "Nathan" to "…He knows. Of course he knows. The silent ones always know. New page of the notebook: 'Timmy — possibly the final boss.'"),
        listOf(
            "Timmy" to "Timmy Timmy!",
            "Nathan" to "A declaration. Bold. Is it a challenge? A warning? A review of my last operation? The scholars will argue for years.",
            "Timmy" to "Timmy!",
            "Nathan" to "Exactly my point. Unknowable. Unstoppable. I allow the duo one victory a month for morale — yours, apparently. Spend it wisely.")),
    setOf("Sophie", "Scott") to listOf(
        listOf(
            "Scott" to "S-Sophie. My blood sugar's at a heroic level. Captain Diabetes has the situation fully—",
            "Sophie" to "Scott. Treaty terms. We compare pumps, we share the juice boxes, nobody faints dramatically. That was the whole summit.",
            "Scott" to "The treaty holds. The treaty is the most functional government in this school and I will die on this hill. Carefully. With snacks.",
            "Sophie" to "Noted in the addendum. Scottsdale never had diplomacy like this. Scottsdale had a pool and zero treaties."),
        listOf(
            "Sophie" to "My pump beeped in gym. Three kids looked. Two weeks ago it was the whole class. Progress is just embarrassment, retiring in stages.",
            "Scott" to "It gets better. Then it gets boring, which is the best kind of better. Then you're just a guy with a backpack full of juice.",
            "Sophie" to "A whole backpack?",
            "Scott" to "A HERO carries supplies, Sophie. The cape is optional. The juice is doctrine.")),
    setOf("Sophie", "Wendy") to listOf(
        listOf(
            "Wendy" to "Circle briefing, Sophie. Annie runs intel, Bebe runs morale, I run the agenda. Questions so far?",
            "Sophie" to "One. In Scottsdale the new girl gets a week of grace. What's the South Park conversion rate?",
            "Wendy" to "You got a month and a fan club of aunties. The conversion is generous. We're not monsters; we're administrators.",
            "Sophie" to "Then I accept the minutes of this meeting and file my review: weirdly great. In writing, if it helps.",
            "Wendy" to "It helps. Welcome to the circle, Scottsdale bureau."),
        listOf(
            "Sophie" to "Your mom put my family's moving permit in the council minutes. 'The Grays: arrived, promising.' I framed the agenda.",
            "Wendy" to "That's the planner's version of a welcome parade. She pencils the promising ones. Pencil is an honour; ink is a promotion.",
            "Sophie" to "Then I'll earn the ink. Careful, Wendy — the bureau reports to the circle now, and my first report is that the circle is kind of wonderful.",
            "Wendy" to "Filed, correspondent. Don't let it go to their heads.")),
    setOf("Big Gay Al", "Chef") to listOf(
        listOf(
            "Big Gay Al" to "Jerome! Darling! Tell me you remember the boat ride. Tell me the hum is still in there.",
            "Chef" to "Hello there, Al. The boat ride. *hums* — Mm. Yeah. It's still in there, children. It's load-bearing.",
            "Big Gay Al" to "I KNEW it. Twenty-nine years and the song holds the whole pier up. We were magnificent, Jerome. We remain it, at reduced sailing.",
            "Chef" to "Reduced sailing, full volume. That's the whole town's motto now, Al. You two enjoy the— ahem. The children are watching. The song stays clean."),
        listOf(
            "Chef" to "Al, there's a goat at my cafeteria window wearing a ribbon. The ribbon says FACULTY.",
            "Big Gay Al" to "Professor! He audits the lunch program now. His reviews are silent and devastating. The trays have improved, Jerome. Admit the trays have improved.",
            "Chef" to "...The trays have improved. Don't let it go to your head, Al. Or the goat's. The goat's head is full enough.")),
    setOf("Roger", "Stephen") to listOf(
        listOf(
            "Stephen" to "Roger. Your boy was at my house Saturday. He felt his feelings at a reasonable volume. I want that noted.",
            "Roger" to "Noted and framed, Stephen. Clyde at reasonable volume is a house record. Your boy's welcome to feel at ours — we run a louder parish, but the coffee's honest.",
            "Stephen" to "Hm. A louder parish. ...Linda says our house could use one evening of it. For balance. I'll allow the experiment — with rules.",
            "Roger" to "Stephen Stotch, allowing an experiment. The town should mark the day. I'll bring the lemon bars and absolutely no agenda."),
        listOf(
            "Roger" to "Stephen. The fence hinge between our yards is fixed. No charge. The charge is you telling me how you keep a straight face grounding a boy for weather.",
            "Stephen" to "Practice, Roger. And posture. A father grounds from the spine.",
            "Roger" to "From the spine. I'll put that in the dad minutes. Betsy used to say you were one good fence away from a sense of humor, Stephen.",
            "Stephen" to "...The hinge swings beautifully. Tell your late wife the fence concedes the point."))
)

