package com.hunt3r.southparktown

// ============================================================
// InteriorLayouts — v0.16.0a, Lucky's BIG UPDATE (items
// 3–7 + the interior half of 8/9/19/20).
//
// Every building gets an interior LAYOUT, as data:
//  - ROOMS: most interiors are several rooms (an entry /
//    living space, the working rooms, the private rooms),
//    laid side by side across the widened interior world;
//    a single room remains only where one room genuinely
//    is the building (the ice hut, the pavilion).
//  - The layout MATCHES the building: the school is a
//    school inside (hall, classroom, cafeteria, library,
//    office), houses are houses (entry, living room,
//    kitchen, bedroom, bath), the bus is a bus (driver
//    cab, seat rows, rear door).
//  - FURNITURE is placed per room, sized off the same
//    kid-unit ladder as the figures (WorldScale), and
//    USABLE: every piece carries the action it affords
//    (sit / sleep / use / serve / work / read / watch),
//    which GameView turns into tap targets and
//    GameActivity into the player's action animations.
//
// Pure Kotlin; InteriorArt/GameView paint from this.
// ============================================================

object InteriorLayouts {

    data class Room(
        val id: String, val label: String,
        val x0: Float, val x1: Float,   // world fractions
        val kind: String                // entry|living|kitchen|bed|bath|
                                        // class|hall|cafe|library|office|
                                        // sales|counter|stock|worship|stage|
                                        // cab|seats|work|generic
    )

    /** One furniture piece. Sizes are kid units off the
     *  WorldScale ladder so a chair is a chair next to a
     *  kid figure in EVERY room (item 6). [seatYU] is the
     *  seat height in units where the piece seats anyone;
     *  [action] is the use it affords (item 7). */
    data class Furniture(
        val id: String, val roomId: String, val kind: String,
        val label: String,
        val xFrac: Float,           // center, world fraction
        val back: Boolean,          // against the wall (true) or floor-front
        val widthU: Float, val heightU: Float,
        val seatYU: Float,
        val action: String,         // sit|sleep|use|serve|work|read|watch|ride
        val seats: Int
    )

    data class Layout(
        val key: String, val title: String,
        val rooms: List<Room>, val furniture: List<Furniture>
    )

    // ---------- furniture size ladder (kid units) ----------
    // A kid figure stands ~3.3 units tall in the interior
    // ladder's terms (WorldScale proportions); furniture
    // is pinned against that in the suite.
    const val CHAIR_W = 0.46f; const val CHAIR_H = 0.62f
    const val DESK_W = 1.15f; const val DESK_H = 0.66f
    const val TABLE_W = 1.35f; const val TABLE_H = 0.62f
    const val BED_W = 1.55f; const val BED_H = 0.72f
    const val COUNTER_W = 1.85f; const val COUNTER_H = 0.78f
    const val SOFA_W = 1.75f; const val SOFA_H = 0.78f
    const val PEW_W = 2.3f; const val PEW_H = 0.72f
    const val SHELF_W = 1.25f; const val SHELF_H = 1.55f
    const val BUS_SEAT_W = 0.58f; const val BUS_SEAT_H = 0.68f
    const val SEAT_Y = 0.30f        // chair/desk seat height
    const val DESK_TOP_Y = 0.52f    // desk/counter work height

    private fun rooms(vararg r: Triple<String, String, String>): List<Room> {
        val n = r.size
        return r.mapIndexed { i, (id, label, kind) ->
            Room(id, label, i.toFloat() / n, (i + 1).toFloat() / n, kind)
        }
    }

    private var fid = 0
    private fun furn(room: Room, kind: String, label: String,
                     fx: Float, back: Boolean, w: Float, h: Float,
                     seatY: Float, action: String, seats: Int): Furniture =
        Furniture("f${fid++}", room.id, kind, label,
            room.x0 + (room.x1 - room.x0) * fx, back, w, h, seatY,
            action, seats)

    private fun layout(key: String, title: String,
                       roomList: List<Room>,
                       build: (Room) -> List<Furniture>): Layout {
        fid = 0
        return Layout(key, title, roomList, roomList.flatMap(build))
    }

    // ---------- per-theme room plans ----------
    private fun planFor(theme: String, locId: String): List<Room> =
        when (theme) {
            "home", "loft" -> rooms(
                Triple("entry", "Entry", "entry"),
                Triple("living", "Living Room", "living"),
                Triple("kitchen", "Kitchen", "kitchen"),
                Triple("bedroom", "Bedroom", "bed"),
                Triple("bath", "Bathroom", "bath"))
            "classroom" -> rooms(
                Triple("hall", "Hallway", "hall"),
                Triple("class", "Classroom", "class"),
                Triple("cafeteria", "Cafeteria", "cafe"),
                Triple("library", "Library", "library"),
                Triple("office", "Office", "office"))
            "bus" -> rooms(
                Triple("cab", "Driver's Cab", "cab"),
                Triple("frontseats", "Front Seats", "seats"),
                Triple("rearseats", "Rear Seats", "seats"))
            "cafe", "restaurant", "diner" -> rooms(
                Triple("entry", "Entry", "entry"),
                Triple("dining", "Dining Room", "living"),
                Triple("counter", "Counter & Kitchen", "counter"))
            "bar" -> rooms(
                Triple("entry", "Entry", "entry"),
                Triple("barroom", "Bar Room", "living"),
                Triple("counter", "The Bar", "counter"))
            "shop" -> rooms(
                Triple("sales", "Sales Floor", "sales"),
                Triple("counter", "Counter", "counter"),
                Triple("stock", "Stock Room", "stock"))
            "mall" -> rooms(
                Triple("atrium", "Atrium", "hall"),
                Triple("shops", "Shops", "sales"),
                Triple("food", "Food Court", "cafe"))
            "bank" -> rooms(
                Triple("lobby", "Lobby", "entry"),
                Triple("tellers", "Teller Line", "counter"),
                Triple("vault", "Vault Office", "office"))
            "office" -> rooms(
                Triple("lobby", "Lobby", "entry"),
                Triple("desks", "Public Desks", "office"),
                Triple("records", "Records", "stock"))
            "clinic" -> rooms(
                Triple("waiting", "Waiting Room", "entry"),
                Triple("exam", "Exam Room", "work"),
                Triple("office", "Office", "office"))
            "church" -> rooms(
                Triple("narthex", "Narthex", "entry"),
                Triple("nave", "Nave", "worship"),
                Triple("altar", "Altar & Choir", "stage"))
            "theater" -> rooms(
                Triple("lobby", "Lobby", "entry"),
                Triple("auditorium", "Auditorium", "worship"),
                Triple("stage", "Stage & Screen", "stage"))
            "barn" -> rooms(
                Triple("yard", "Barn Entry", "entry"),
                Triple("stalls", "Stalls", "work"),
                Triple("loft", "Hay Loft", "stock"))
            "pavilion" -> rooms(
                Triple("shade", "Under the Pavilion", "living"),
                Triple("grills", "Grill End", "work"))
            "hut" -> rooms(
                Triple("main", "The Hut", "living"),
                Triple("gear", "Gear Corner", "work"))
            else -> rooms(
                Triple("entry", "Entry", "entry"),
                Triple("main", "Main Room", "living"),
                Triple("back", "Back Room", "work"))
        }

    // ---------- furniture per room kind ----------
    private fun furnitureFor(room: Room): List<Furniture> = when (room.kind) {
        "entry" -> listOf(
            furn(room, "bench", "Bench", 0.30f, false, 0.95f, 0.55f, SEAT_Y, "sit", 2),
            furn(room, "shelf", "Coat Shelf", 0.72f, true, SHELF_W, SHELF_H, 0f, "use", 0))
        "living" -> listOf(
            furn(room, "sofa", "Sofa", 0.32f, true, SOFA_W, SOFA_H, SEAT_Y, "sit", 3),
            furn(room, "chair", "Armchair", 0.62f, false, CHAIR_W, CHAIR_H, SEAT_Y, "sit", 1),
            furn(room, "table", "Coffee Table", 0.45f, false, TABLE_W, TABLE_H, 0f, "use", 0),
            furn(room, "shelf", "Bookshelf", 0.85f, true, SHELF_W, SHELF_H, 0f, "read", 0))
        "kitchen" -> listOf(
            furn(room, "counter", "Kitchen Counter", 0.30f, true, COUNTER_W, COUNTER_H, 0f, "serve", 0),
            furn(room, "table", "Kitchen Table", 0.62f, false, TABLE_W, TABLE_H, 0f, "use", 0),
            furn(room, "chair", "Kitchen Chair", 0.52f, false, CHAIR_W, CHAIR_H, SEAT_Y, "sit", 1),
            furn(room, "chair", "Kitchen Chair", 0.74f, false, CHAIR_W, CHAIR_H, SEAT_Y, "sit", 1))
        "bed" -> listOf(
            furn(room, "bed", "Bed", 0.35f, true, BED_W, BED_H, 0.38f, "sleep", 2),
            furn(room, "desk", "Desk", 0.72f, true, DESK_W, DESK_H, 0f, "work", 0),
            furn(room, "chair", "Desk Chair", 0.72f, false, CHAIR_W, CHAIR_H, SEAT_Y, "sit", 1))
        "bath" -> listOf(
            furn(room, "counter", "Sink Counter", 0.40f, true, 1.2f, COUNTER_H, 0f, "use", 0),
            furn(room, "bench", "Laundry Bench", 0.75f, false, 0.95f, 0.55f, SEAT_Y, "sit", 1))
        "hall" -> listOf(
            furn(room, "bench", "Hall Bench", 0.30f, false, 0.95f, 0.55f, SEAT_Y, "sit", 2),
            furn(room, "shelf", "Lockers", 0.70f, true, 1.6f, 1.5f, 0f, "use", 0))
        "class" -> listOf(
            furn(room, "desk", "Teacher's Desk", 0.22f, true, DESK_W, DESK_H, 0f, "work", 0),
            furn(room, "desk", "Student Desk", 0.48f, false, 0.85f, DESK_H, 0f, "work", 0),
            furn(room, "chair", "Student Chair", 0.48f, false, CHAIR_W, CHAIR_H, SEAT_Y, "sit", 1),
            furn(room, "desk", "Student Desk", 0.72f, false, 0.85f, DESK_H, 0f, "work", 0),
            furn(room, "chair", "Student Chair", 0.72f, false, CHAIR_W, CHAIR_H, SEAT_Y, "sit", 1),
            furn(room, "shelf", "Class Shelf", 0.90f, true, SHELF_W, SHELF_H, 0f, "read", 0))
        "cafe" -> listOf(
            furn(room, "counter", "Serving Counter", 0.28f, true, COUNTER_W, COUNTER_H, 0f, "serve", 0),
            furn(room, "table", "Cafeteria Table", 0.58f, false, TABLE_W, TABLE_H, 0f, "use", 0),
            furn(room, "chair", "Cafeteria Chair", 0.50f, false, CHAIR_W, CHAIR_H, SEAT_Y, "sit", 1),
            furn(room, "chair", "Cafeteria Chair", 0.68f, false, CHAIR_W, CHAIR_H, SEAT_Y, "sit", 1))
        "library" -> listOf(
            furn(room, "shelf", "Library Shelves", 0.30f, true, 1.6f, SHELF_H, 0f, "read", 0),
            furn(room, "table", "Reading Table", 0.62f, false, TABLE_W, TABLE_H, 0f, "read", 0),
            furn(room, "chair", "Reading Chair", 0.62f, false, CHAIR_W, CHAIR_H, SEAT_Y, "sit", 1))
        "office" -> listOf(
            furn(room, "desk", "Office Desk", 0.40f, true, DESK_W, DESK_H, 0f, "work", 0),
            furn(room, "chair", "Office Chair", 0.40f, false, CHAIR_W, CHAIR_H, SEAT_Y, "sit", 1),
            furn(room, "shelf", "File Shelf", 0.78f, true, SHELF_W, SHELF_H, 0f, "read", 0))
        "sales" -> listOf(
            furn(room, "shelf", "Display Shelf", 0.30f, true, SHELF_W, SHELF_H, 0f, "use", 0),
            furn(room, "table", "Display Table", 0.62f, false, TABLE_W, TABLE_H, 0f, "use", 0),
            furn(room, "bench", "Trying Bench", 0.85f, false, 0.95f, 0.55f, SEAT_Y, "sit", 2))
        "counter" -> listOf(
            furn(room, "counter", "Counter", 0.38f, true, COUNTER_W, COUNTER_H, 0f, "serve", 0),
            furn(room, "chair", "Counter Stool", 0.30f, false, CHAIR_W, CHAIR_H, SEAT_Y, "sit", 1),
            furn(room, "chair", "Counter Stool", 0.52f, false, CHAIR_W, CHAIR_H, SEAT_Y, "sit", 1))
        "stock" -> listOf(
            furn(room, "shelf", "Stock Shelves", 0.35f, true, 1.6f, SHELF_H, 0f, "use", 0),
            furn(room, "desk", "Work Desk", 0.72f, false, DESK_W, DESK_H, 0f, "work", 0))
        "worship" -> listOf(
            furn(room, "pew", "Pew", 0.32f, false, PEW_W, PEW_H, SEAT_Y, "sit", 5),
            furn(room, "pew", "Pew", 0.62f, false, PEW_W, PEW_H, SEAT_Y, "sit", 5),
            furn(room, "pew", "Pew", 0.88f, false, PEW_W, PEW_H, SEAT_Y, "sit", 5))
        "stage" -> listOf(
            furn(room, "desk", "Lectern", 0.30f, false, 0.7f, 0.9f, 0f, "work", 0),
            furn(room, "chair", "Stage Chair", 0.62f, false, CHAIR_W, CHAIR_H, SEAT_Y, "sit", 1),
            furn(room, "bench", "Stage Bench", 0.82f, false, 0.95f, 0.55f, SEAT_Y, "sit", 2))
        "cab" -> listOf(
            furn(room, "busseat", "Driver's Seat", 0.35f, true, BUS_SEAT_W, BUS_SEAT_H, SEAT_Y, "ride", 1),
            furn(room, "counter", "Fare Box & Wheel", 0.68f, true, 0.9f, 0.8f, 0f, "ride", 0))
        "seats" -> listOf(
            furn(room, "busseat", "Bus Seat", 0.25f, false, BUS_SEAT_W, BUS_SEAT_H, SEAT_Y, "ride", 2),
            furn(room, "busseat", "Bus Seat", 0.45f, false, BUS_SEAT_W, BUS_SEAT_H, SEAT_Y, "ride", 2),
            furn(room, "busseat", "Bus Seat", 0.65f, false, BUS_SEAT_W, BUS_SEAT_H, SEAT_Y, "ride", 2),
            furn(room, "busseat", "Bus Seat", 0.85f, false, BUS_SEAT_W, BUS_SEAT_H, SEAT_Y, "ride", 2))
        "work" -> listOf(
            furn(room, "desk", "Work Bench", 0.38f, true, DESK_W, DESK_H, 0f, "work", 0),
            furn(room, "chair", "Work Chair", 0.38f, false, CHAIR_W, CHAIR_H, SEAT_Y, "sit", 1),
            furn(room, "shelf", "Tool Shelf", 0.78f, true, SHELF_W, SHELF_H, 0f, "use", 0))
        else -> listOf(
            furn(room, "table", "Table", 0.45f, false, TABLE_W, TABLE_H, 0f, "use", 0),
            furn(room, "chair", "Chair", 0.68f, false, CHAIR_W, CHAIR_H, SEAT_Y, "sit", 1))
    }

    /** The layout of one location's interior. */
    fun forLocation(locId: String): Layout {
        val def = INTERIORS[locId]
        val theme = def?.theme ?: "home"
        val title = def?.name ?: locId
        return layout(locId, title, planFor(theme, locId)) { room ->
            furnitureFor(room)
        }
    }

    /** The layout of a street-house SECTION (item 20):
     *  every distinct house on a residential street leads
     *  to its own section — same house grammar, its own
     *  arrangement: the furniture variant and the room
     *  labels are keyed to the section id, so no two
     *  sections on a street share a layout whole. */
    fun forSection(sectionId: String, title: String): Layout {
        // v0.16.0e pass 2: a FABRICATED fiction set — an
        // interior that exists only while the kids' game
        // does (the Keep's dungeon under City Hall, the
        // lair in your garage). Same section machinery,
        // a set's rooms: torch hall, the deep room, and
        // the throne nook, dressed from the ordinary
        // furniture grammar — it is, after all, made of
        // the building's own furniture wearing costumes.
        if (sectionId.startsWith("fiction:")) {
            val lair = sectionId.endsWith("lair")
            val setRooms = rooms(
                Triple("entry", "Torch Hall", "hall"),
                Triple("deep", if (lair) "The Lair" else "The Dungeon",
                    "generic"),
                Triple("throne", if (lair) "War Room" else "Throne Nook",
                    "office"))
            return layout(sectionId,
                if (lair) "The Garage Lair — a set"
                else "Beneath the Keep — a set",
                setRooms) { room -> furnitureFor(room) }
        }
        val rooms = planFor("home", sectionId)
        val variant = (sectionId.hashCode() and 0x7fffffff) % 3
        return layout(sectionId, title, rooms) { room ->
            val base = furnitureFor(room)
            if (variant == 0) base
            else base.map { f ->
                val shift = if (variant == 1) 0.05f else -0.05f
                f.copy(xFrac = (f.xFrac + shift * (f.roomSpan(rooms)))
                    .coerceIn(0.03f, 0.97f))
            }
        }
    }

    private fun Furniture.roomSpan(rooms: List<Room>): Float {
        val r = rooms.firstOrNull { it.id == roomId } ?: return 0.2f
        return r.x1 - r.x0
    }

    /** The bus interior layout (items 8+9) — the school
     *  bus and the public bus share the shell; the route
     *  board and livery differ, the seats are real. */
    fun busLayout(school: Boolean): Layout =
        forLocation("BusStop").copy(
            key = if (school) "bus:school" else "bus:public",
            title = if (school) "Inside the School Bus"
                else "Inside the Bus")

    /** Every location's layout, for coverage pins. */
    val ALL: Map<String, Layout> by lazy {
        INTERIORS.keys.associateWith { forLocation(it) }
    }

    fun roomAt(layout: Layout, xFrac: Float): Room =
        layout.rooms.firstOrNull { xFrac >= it.x0 && xFrac < it.x1 }
            ?: layout.rooms.last()

    /** Figures use furniture too (item 7): the seat a
     *  figure at [xFrac] in [layout] would take — the
     *  nearest seatable piece in their room. */
    fun seatNear(layout: Layout, xFrac: Float): Furniture? =
        layout.furniture
            .filter { it.seats > 0 }
            .minByOrNull { kotlin.math.abs(it.xFrac - xFrac) }
}
