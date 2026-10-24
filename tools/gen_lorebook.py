#!/usr/bin/env python3
"""Generate the in-app lorebook asset + alias tables from Lucky's SillyTavern
lorebook JSON. All prose in the source is already paraphrased (never copied
from the wiki) -- we only repackage name/category/content/keywords.

Outputs:
  assets/lorebook.json       trimmed entries [{n,c,t,k}]
  src/.../LoreAliases.kt     verified game-name -> lorebook-name match tables
Stdout: match coverage report so misses can be fixed by hand.
"""
import json, re
from collections import Counter

SRC = "/home/hatch/workspace/southpark-lorebooks/south-park-lorebook.json"
APP = "/home/hatch/workspace/sptown-app"
DASH = "\u2014"

lb = json.load(open(SRC))
raw = lb["entries"]
if isinstance(raw, dict):
    raw = [raw[k] for k in sorted(raw, key=lambda x: int(x))]

def parse(e):
    comment = e.get("comment", "")
    content = (e.get("content") or "").strip()
    if "CHARACTER" in comment:
        cat, name = "char", comment.split(DASH, 1)[1].strip().strip('"')
    elif "LOCATION" in comment:
        cat, name = "loc", comment.split(DASH, 1)[1].strip().strip('"')
    elif "SPECIAL" in comment:
        cat, name = "spec", comment.split(DASH, 1)[1].strip()
    elif "GAME" in comment:
        cat, name = "game", comment.split(DASH, 1)[1].strip()
    elif "MOVIE" in comment:
        cat, name = "spec", comment.split(DASH, 1)[1].strip()
    elif re.match(r"^S\d+E\d+", comment):
        cat, name = "ep", comment.strip()
    else:
        return None
    keys = []
    for k in e.get("keys", []):
        k2 = k.strip().strip('"').lower()
        if k2 and k2 not in keys and len(k2) >= 3:
            keys.append(k2)
    return {"n": name, "c": cat, "t": content, "k": keys[:8]}

entries = [p for p in (parse(e) for e in raw) if p and p["t"]]
print("parsed entries:", len(entries))
print(Counter(e["c"] for e in entries))

def norm(s):
    return re.sub(r"[^a-z0-9 ]", "", s.lower()).strip()

by_cat = {}
for e in entries:
    by_cat.setdefault(e["c"], {})[norm(e["n"])] = e["n"]

def find(cat, candidates):
    table = by_cat.get(cat, {})
    for cand in candidates:
        if norm(cand) in table:
            return table[norm(cand)]
    return None

CAST = {
    "Cartman": ["Eric Cartman"],
    "Stan": ["Stan Marsh"],
    "Kyle": ["Kyle Broflovski"],
    "Kenny": ["Kenny McCormick"],
    "Butters": ["Butters Stotch", "Leopold Stotch"],
    "Wendy": ["Wendy Testaburger"],
    "Craig": ["Craig Tucker"],
    "Tweek": ["Tweek Tweak"],
    "Clyde": ["Clyde Donovan"],
    "Jimmy": ["Jimmy Valmer"],
    "Timmy": ["Timmy Burch"],
    "Tolkien": ["Tolkien Black", "Token Black"],
    "Bebe": ["Bebe Stevens"],
    "Michael": ["Michael (Goth)", "Michael"],
    "Pete": ["Pete Thelman", "Pete"],
    "Henrietta": ["Henrietta Biggle"],
    "Randy": ["Randy Marsh"],
    "Sharon": ["Sharon Marsh"],
    "Gerald": ["Gerald Broflovski"],
    "Sheila": ["Sheila Broflovski"],
    "Liane": ["Liane Cartman"],
    "Stuart": ["Stuart McCormick"],
    "Carol": ["Carol McCormick"],
    "Stephen": ["Stephen Stotch"],
    "Linda": ["Linda Stotch"],
    "Garrison": ["Herbert Garrison", "Mr. Garrison", "Mrs. Garrison"],
    "Mackey": ["Mr. Mackey", "Richard Mackey", "Mackey"],
    "Chef": ["Jerome McElroy", 'Jerome "Chef" McElroy', "Chef"],
    "Jesus": ["Jesus Christ", "Jesus"],
    "Jimbo": ["Jimbo Kern", "Jimbo"],
    "Father Maxi": ["Father Maxi", "Priest Maxi", "Maxi"],
    "Towelie": ["Towelie", "Towelie (Character)"],
    "Heidi": ["Heidi Turner"],
    "Nichole": ["Nichole Daniels"],
    "Red": ["Red McArthur"],
    "Kevin": ["Kevin Stoley"],
    "Francis": ["Francis"],
    "DogPoo": ["DogPoo Petuski"],
    "McDaniels": ["Mayor McDaniels"],
    "Barbrady": ["Officer Barbrady"],
    "Crabtree": ["Veronica Crabtree"],
    "Mr. Tweek": ["Richard Tweak"],
    "Mrs. Tweek": ["Mrs. Tweak"],
    "Ned": ["Ned Gerblanski"],
    "Karen": ["Karen McCormick"],
    "Scott": ["Scott Malkinson"],
    "Firkle": ["Firkle Smith"],
    "Annie": ["Annie Knitts"],
    "Bradley": ["Bradley Biggle"],
    "Skeeter": ["Skeeter"],
    "Victoria": ["Principal Victoria"],
    "Marvin": ["Marvin Marsh"],
    "Mr. Tucker": ["Thomas Tucker"],
    "Steve": ["Steve Black"],
    "Ike": ["Ike Broflovski"],
    "Kevin McCormick": ["Kevin McCormick"],
    "Shelley": ["Shelley Marsh"],
    "Laura": ["Laura Tucker"],
    "Mephesto": ["Dr. Alphonse Mephesto"],
    "Yates": ["Harrison Yates"],
    "Adler": ["Richard Adler"],
    "Lu Kim": ["Tuong Lu Kim"],
    # v0.9.0a wave 5: Jimmy's parents, Wendy's parents,
    # and the town's superest citizen join the cast proper;
    # Roger, Nathan and Sophie graduate from reference
    # rows to cast rows of their own.
    "Roger": ["Roger Donovan"],
    "Nathan": ["Nathan"],
    "Sophie": ["Sophie Gray"],
    "Ryan": ["Ryan Valmer"],
    "Sarah": ["Sarah Valmer"],
    "Alexis": ["Alexis Testaburger"],
    "Mr. Testaburger": ["Mr. Testaburger"],
    "Big Gay Al": ["Big Gay Al"],
    # ---- wave 6 (v0.11.0d): the evidence audit's admits ----
    "Pip": ["Pip Pirrip"],
    "Dougie": ["Dougie O'Connell"],
    "Jason": ["Jason White"],
    "Terrance Mephesto": ["Terrance Mephesto"],
    "Jenny": ["Jenny Simons"],
    "Flora": ["Flora Larsen"],
    "Filmore": ["Filmore Anderson"],
    "Quaid": ["Quaid"],
    "Mimsy": ["Mimsy"],
    "Tricia": ["Tricia Tucker"],
    "Mr. Slave": ["Mr. Slave"],
    "Linda Black": ["Linda Black"],
    "Harriet": ["Harriet Biggle"],
    "Mr. Biggle": ["Mr. Biggle"],
    "Mr. Turner": ["Thomas Turner"],
    "Nancy": ["Nancy Turner"],
    "Mr. Stevens": ["Mr. Stevens"],
    "Mrs. Stevens": ["Mrs. Stevens"],
    "Mr. Stoley": ["Mr. Stoley"],
    "Mrs. Stoley": ["Mrs. Stoley"],
    "Clark": ["Clark Malkinson"],
    "Maggie": ["Maggie Yates"],
    "Mitch": ["Mitch Murphy"],
    "Bob Johnson": ["Bob Johnson"],
    "Pusslicker": ["Tom Pusslicker"],
    "Dr. Doctor": ["Dr. Doctor"],
    "Choksondik": ["Diane Choksondik"],
    "Strong Woman": ["Strong Woman"],
}

LOCS = {
    "KennysHouse": ["Kenny's House", "McCormick House", "McCormick Residence"],
    "ButtersHouse": ["Stotch Residence", "Stotch House", "Butters' House"],
    "CartmanHouse": ["Cartman Residence", "Cartman House"],
    "StanHouse": ["Marsh Residence", "Marsh House"],
    "KyleHouse": ["Broflovski Residence", "Broflovski House"],
    "YourHouse": ["The New Kid's Residence"],
    "BusStop": ["South Park Bus Stop", "Bus Stop"],
    "MainStreet": ["Main Street"],
    "TweekBros": ["Tweek Bros. Coffeehouse", "Tweek Bros. Coffee", "Tweek Bros Coffee"],
    "JimbosGuns": ["Jimbo's Guns"],
    "CityWok": ["City Wok"],
    "SkeetersBar": ["Skeeter's Bar", "Skeeter's Bar and Cocktails", "Skeeter's Wine Bar"],
    "Bijou": ["Bijou Cinema", "Bijou Theatre", "Bijou Theater"],
    "TomsRhino": ["Tom's Rhinoplasty", "Tom's Rhinoplasty (Location)"],
    "Bank": ["Bank of South Park", "First Bank of South Park", "South Park Bank"],
    "PostOffice": ["Post Office", "South Park Post Office"],
    "FreemansTacos": ["Freeman's Tacos"],
    "Church": ["South Park Church", "Catholic Church", "Church"],
    "CityHall": ["City Hall", "South Park City Hall"],
    "Elementary": ["South Park Elementary", "South Park Elementary School"],
    "KidsPark": ["South Park Playground", "Playground", "Basketball Court"],
    "StarksPond": ["Stark's Pond"],
    "Mall": ["South Park Mall"],
    "TolkienHouse": ["Tolkien's House", "Black Residence", "Token's House"],
    "SoDoSoPa": ["SoDoSoPa", "SodoSoPa"],
    "TegridyFarms": ["Tegridy Farms", "Tegridy Farms (Location)"],
    "Raisins": ["Raisins", "Raisins (Location)"],
}

char_alias, loc_alias, missed = {}, {}, []
for game_name, cands in CAST.items():
    hit = find("char", cands)
    if hit:
        char_alias[game_name] = hit
    else:
        missed.append(("char", game_name))
for loc_id, cands in LOCS.items():
    hit = find("loc", cands)
    if hit:
        loc_alias[loc_id] = hit
    else:
        missed.append(("loc", loc_id))

print(f"cast matched: {len(char_alias)}/{len(CAST)}")
print(f"locations matched: {len(loc_alias)}/{len(LOCS)}")
for kind, name in missed:
    print("  MISS:", kind, name)

def kt_escape(s):
    return s.replace("\\", "\\\\").replace('"', '\\"').replace("$", "\\$")

def kt_map(d):
    lines = ",\n    ".join(f'"{kt_escape(k)}" to "{kt_escape(v)}"' for k, v in d.items())
    return "    " + lines

kt = f"""package com.hunt3r.southparktown

// ============================================================
// LoreAliases -- GENERATED by tools/gen_lorebook.py from
// south-park-lorebook.json. Game name -> lorebook entry name,
// verified against the actual lorebook. Do not hand-edit;
// re-run the generator instead.
// ============================================================

/** Game character name -> lorebook character entry name. */
val LORE_CHAR_ALIASES: Map<String, String> = mapOf(
{kt_map(char_alias)}
)

/** Game location id -> lorebook location entry name. */
val LORE_LOC_ALIASES: Map<String, String> = mapOf(
{kt_map(loc_alias)}
)
"""
out_kt = APP + "/src/com/hunt3r/southparktown/LoreAliases.kt"
with open(out_kt, "w") as f:
    f.write(kt)
print("wrote", out_kt)

asset = {"entries": entries}
out_json = APP + "/assets/lorebook.json"
with open(out_json, "w") as f:
    json.dump(asset, f, ensure_ascii=True, separators=(",", ":"))
import os
print("wrote", out_json, os.path.getsize(out_json), "bytes")
