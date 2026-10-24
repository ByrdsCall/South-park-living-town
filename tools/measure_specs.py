#!/usr/bin/env python3
"""
measure_specs.py — measure character proportions + palettes from Lucky's
supplied reference images, so the game's SheetSpecs carry IMAGE-DERIVED
values instead of hand approximations (Lucky, v0.1.7: "Actually use the
images to build the characters").

Sources (all supplied by Lucky; images are measured, never shipped):
  * five construction sheets (Kenny/Cartman/Craig/Stan/Kyle), user/files
  * Garrison character model photo, user/files/261061_11_ocqn.png
  * adolescence proportion chart, user/files/photostudio_..._3_fgew.png
  * blank New Kid reference, docs/reference-new-kid.jpg (style baseline)

Method per figure:
  - silhouette mask from alpha, or from corner-background color distance
  - a ground-shadow trim (rows much wider than the legs near the bottom)
  - the FACE is the longest skin-classified run in the center column;
    its end is the chin. Head share uses the FACE (hat height logged
    separately, since hats draw above the head circle in-game)
  - torso/leg split from center-column color runs below the chin
    (robe fallback: 45/55 split of the below-chin span, flagged)
  - palettes = quantized dominant colors per region (head / torso / legs
    / shoe band), mapped back to bucket-mean RGB
  - widths from the row-width profile: head width = max in the face
    band, torso width = median in the torso band

Outputs tools/measurements.json + a stdout summary.
"""
import json
import sys
from collections import Counter, deque
from PIL import Image

# ---------------------------------------------------------------- mask util

def load_rgba(path):
    im = Image.open(path).convert("RGBA")
    return im


def build_mask(im):
    """Boolean silhouette mask: alpha if the image has real transparency,
    else FLOOD-FILL background removal: candidate background colors are
    the dominant colors of the border ring (the New Kid photo has black
    corner wedges AND white paper — both are background); flood from the
    border through pixels close to any candidate color."""
    w, h = im.size
    px = im.load()
    alpha = im.split()[3]
    a_min = alpha.getextrema()[0]
    if a_min < 200:  # real transparency present
        return [[px[x, y][3] > 40 for x in range(w)] for y in range(h)]

    border = []
    for x in range(w):
        border.append(px[x, 0][:3])
        border.append(px[x, h - 1][:3])
    for y in range(h):
        border.append(px[0, y][:3])
        border.append(px[w - 1, y][:3])
    counts = Counter(qkey(c) for c in border)
    total = len(border)
    cands = []
    covered = 0
    for k, n in counts.most_common():
        if n / total < 0.06 and cands:
            break
        cands.append(k)
        covered += n
        if covered / total > 0.85 or len(cands) >= 3:
            break
    cand_rgb = [(k[0] * 16 + 8, k[1] * 16 + 8, k[2] * 16 + 8) for k in cands]

    def is_bg(x, y):
        c = px[x, y]
        return any(max(abs(c[0] - r), abs(c[1] - g), abs(c[2] - b)) <= 30
                   for (r, g, b) in cand_rgb)

    bgm = [[False] * w for _ in range(h)]
    dq = deque()
    for x in range(w):
        for y in (0, h - 1):
            if is_bg(x, y) and not bgm[y][x]:
                bgm[y][x] = True
                dq.append((x, y))
    for y in range(h):
        for x in (0, w - 1):
            if is_bg(x, y) and not bgm[y][x]:
                bgm[y][x] = True
                dq.append((x, y))
    while dq:
        cx, cy = dq.popleft()
        for nx, ny in ((cx + 1, cy), (cx - 1, cy), (cx, cy + 1), (cx, cy - 1)):
            if 0 <= nx < w and 0 <= ny < h and not bgm[ny][nx] and is_bg(nx, ny):
                bgm[ny][nx] = True
                dq.append((nx, ny))
    return [[not bgm[y][x] for x in range(w)] for y in range(h)]


def bbox_of(mask):
    h = len(mask)
    w = len(mask[0])
    rows = [y for y in range(h) if any(mask[y])]
    if not rows:
        return None
    cols = [x for x in range(w) if any(mask[y][x] for y in range(h))]
    return min(cols), min(rows), max(cols), max(rows)


def row_widths(mask, x0, x1, y0, y1):
    return [sum(1 for x in range(x0, x1 + 1) if mask[y][x])
            for y in range(y0, y1 + 1)]


def is_skin(r, g, b):
    """Light skin tones only. Tuned for the supplied measurement set
    (all its figures are light-skinned): Stan's BROWN JACKET (107,74,47)
    passes looser warm-color tests and must NOT classify as skin."""
    return r > 150 and g > 105 and 25 < (r - b) < 100 and (r - g) < 85


def qkey(c):
    return (c[0] // 16, c[1] // 16, c[2] // 16)


def dominant(im, mask, x0, x1, y0, y1, skip_skin=False, only_skin=False):
    """Dominant color in a region as bucket-mean hex + coverage share."""
    px = im.load()
    buckets = {}
    total = 0
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            if not mask[y][x]:
                continue
            c = px[x, y][:3]
            sk = is_skin(*c)
            if skip_skin and sk:
                continue
            if only_skin and not sk:
                continue
            total += 1
            k = qkey(c)
            if k not in buckets:
                buckets[k] = [0, 0, 0, 0]
            b = buckets[k]
            b[0] += 1
            b[1] += c[0]
            b[2] += c[1]
            b[3] += c[2]
    if not buckets or total == 0:
        return None, 0.0
    k, b = max(buckets.items(), key=lambda kv: kv[1][0])
    hexv = "#%02x%02x%02x" % (b[1] // b[0], b[2] // b[0], b[3] // b[0])
    return hexv, b[0] / total


def dominant_where(im, mask, x0, x1, y0, y1, pred):
    """Dominant non-skin color in a region among pixels passing pred."""
    px = im.load()
    buckets = {}
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            if not mask[y][x]:
                continue
            c = px[x, y][:3]
            if is_skin(*c) or not pred(c):
                continue
            k = qkey(c)
            if k not in buckets:
                buckets[k] = [0, 0, 0, 0]
            b = buckets[k]
            b[0] += 1
            b[1] += c[0]
            b[2] += c[1]
            b[3] += c[2]
    if not buckets:
        return None
    _k, b = max(buckets.items(), key=lambda kv: kv[1][0])
    return "#%02x%02x%02x" % (b[1] // b[0], b[2] // b[0], b[3] // b[0])


def center_runs(im, mask, cx, y0, y1):
    """Runs of quantized color down a narrow center column (mask only).
    Returns [(ystart, yend, hexmean)] with short runs (<2% of span) merged
    into their neighbors."""
    px = im.load()
    span = y1 - y0 + 1
    col = []
    for y in range(y0, y1 + 1):
        vals = []
        for x in range(cx - 2, cx + 3):
            if 0 <= x < len(mask[0]) and mask[y][x]:
                vals.append(px[x, y][:3])
        if not vals:
            col.append(None)
        else:
            n = len(vals)
            col.append(tuple(sum(v[i] for v in vals) // n for i in range(3)))
    runs = []
    for i, c in enumerate(col):
        if c is None:
            continue
        k = qkey(c)
        if runs and runs[-1][2] == k:
            runs[-1][1] = y0 + i
            runs[-1][3].append(c)
        else:
            runs.append([y0 + i, y0 + i, k, [c]])

    def mean_of(cs):
        n = len(cs)
        return tuple(sum(v[i] for v in cs) // n for i in range(3))

    def close(c1, c2, tol):
        return max(abs(c1[0] - c2[0]), abs(c1[1] - c2[1]),
                   abs(c1[2] - c2[2])) <= tol

    # Semantic merging, iterated to a fixed point:
    #  - adjacent runs merge when both are skin-classified or their mean
    #    colors are close (flat-color gradients split on bucket edges)
    #  - a SHORT run (a mouth line, glasses frame, belt) bridges its two
    #    neighbors when they are close to each other or both skin
    changed = True
    while changed:
        changed = False
        out = []
        i = 0
        while i < len(runs):
            r = runs[i]
            m = mean_of(r[3])
            if out:
                pm = mean_of(out[-1][3])
                if (is_skin(*pm) and is_skin(*m)) or close(pm, m, 22):
                    out[-1][1] = r[1]
                    out[-1][3].extend(r[3])
                    changed = True
                    i += 1
                    continue
            if (0 < i < len(runs) - 1 and (r[1] - r[0] + 1) < span * 0.03
                    and out):
                nm = mean_of(runs[i + 1][3])
                pm = mean_of(out[-1][3])
                if close(pm, nm, 30) or (is_skin(*pm) and is_skin(*nm)):
                    out[-1][1] = runs[i + 1][1]
                    out[-1][3].extend(r[3])
                    out[-1][3].extend(runs[i + 1][3])
                    changed = True
                    i += 2
                    continue
            out.append(r)
            i += 1
        runs = out
    return [(a, b, "#%02x%02x%02x" % mean_of(cs), is_skin(*mean_of(cs)))
            for a, b, _k, cs in runs]


# ---------------------------------------------------------------- figures

def measure_figure(im, mask, label):
    """Measure one figure (mask already isolated to that figure)."""
    bb = bbox_of(mask)
    if bb is None:
        return {"label": label, "error": "empty mask"}
    x0, y0, x1, y1 = bb
    H = y1 - y0 + 1
    widths = row_widths(mask, x0, x1, y0, y1)

    # ground-shadow trim: bottom rows far wider than the leg band above
    leg_probe = widths[int(H * 0.80)] if H > 10 else widths[-1]
    cut = H
    for i in range(H - 1, int(H * 0.82), -1):
        if leg_probe > 0 and widths[i] > leg_probe * 1.6:
            cut = i
        else:
            break
    if cut < H:
        y1 = y0 + cut - 1
        H = cut
        widths = widths[:H]

    # center column x = mask centroid of the upper half (stable vs stance)
    tot = 0
    sx = 0
    for yy in range(y0, y0 + H // 2):
        for xx in range(x0, x1 + 1):
            if mask[yy][xx]:
                tot += 1
                sx += xx
    cx = sx // max(tot, 1)

    runs = center_runs(im, mask, cx, y0, y1)
    notes = []

    # skin coverage of the whole figure: mannequins (the adolescence
    # chart) are skin all over — their chin comes from the width profile
    skin_px = 0
    tot_px = 0
    pxa = im.load()
    for yy in range(y0, y1 + 1):
        for xx in range(x0, x1 + 1):
            if mask[yy][xx]:
                tot_px += 1
                if is_skin(*pxa[xx, yy][:3]):
                    skin_px += 1
    all_skin = tot_px > 0 and skin_px / tot_px > 0.55

    # width-profile chin: first sustained drop below 90% of the head's
    # max width (defined only when the head is clearly wider than the body)
    head_max_w = max(widths[: int(H * 0.55)]) if H > 10 else 0
    hm_row = widths.index(head_max_w) if head_max_w else 0
    torso_probe = sorted(widths[int(H * 0.62): int(H * 0.8)])[max(0, int(H * 0.09))] \
        if H > 20 else head_max_w
    chin_w = None
    if head_max_w > torso_probe * 1.12:
        for i in range(hm_row, int(H * 0.7)):
            if all(widths[j] < head_max_w * 0.90
                   for j in range(i, min(i + 3, H))):
                chin_w = i
                break

    if all_skin:
        # bare all-skin figure (New Kid): the head is an ellipse whose
        # top half is unobstructed — fit ry from the 70%-width crossing
        hw45 = max(widths[: int(H * 0.45)])
        y70 = next((i for i, wd in enumerate(widths) if wd >= 0.7 * hw45), 0)
        head_fit = 2 * (y70 / 0.286) if y70 > 0 else hw45
        face_top = y0
        chin = y0 + int(round(head_fit)) - 1
        notes.append("all-skin: chin via head ellipse fit")
    else:
        # face = skin runs in the top 75%, GROUPED across short non-skin
        # gaps (glasses, eye whites): the group with the most skin wins;
        # its end is the chin
        skin_runs = [(a, b) for (a, b, _hx, sk) in runs
                     if sk and b - y0 < H * 0.75]
        groups = []
        for (a, b) in skin_runs:
            if groups and a - groups[-1][1] < H * 0.12:
                groups[-1][1] = b
                groups[-1][2] += b - a + 1
            else:
                groups.append([a, b, b - a + 1])
        if groups:
            g = max(groups, key=lambda t: t[2])
            face_top, chin = g[0], g[1]
        else:
            face_top, chin = y0, y0
            notes.append("no skin face found")
        # (a bare chest would make the skin group run past the real
        # chin, but that case is the all_skin branch above — the width
        # profile must NOT override a skin chin here: Stan's jaw
        # narrowing sits above his chin and fooled it)

    hat_share = (face_top - y0) / H

    # skin patch width inside the head zone: the longest CONTIGUOUS skin
    # run in any head row. (A plain bbox is useless here: anti-aliased
    # fringe pixels where a saturated color meets white — Kenny's orange
    # hood edge — classify as "skin" and span the whole silhouette.)
    patch_w = 0
    for yy in range(y0, chin + 1):
        run = 0
        for xx in range(x0, x1 + 1):
            if mask[yy][xx] and is_skin(*pxa[xx, yy][:3]):
                run += 1
                patch_w = max(patch_w, run)
            else:
                run = 0
    head_sil_w = max(widths[face_top - y0: chin - y0 + 1]) if chin >= face_top else 0
    # hooded figures (Kenny): the face patch is much narrower than the
    # head silhouette — the split must anchor at the HOOD's bottom,
    # which is the width profile's local minimum under the head's
    # widest row (hood narrows to the neck, then the body widens again)
    hooded = patch_w > 0 and head_sil_w > 0 and patch_w < 0.52 * head_sil_w
    if hooded:
        seg = widths[hm_row: int(H * 0.75)]
        if seg and min(seg) < head_max_w * 0.92:
            chin = y0 + hm_row + seg.index(min(seg))
            notes.append("hooded: split anchored at hood bottom")

    # torso / legs split: the torso is the first long non-skin run below
    # the chin; the hip is where a later long run in a clearly different
    # color (the pants) begins. Same-color torso+pants (Kenny's orange
    # parka + orange pants) split by the width profile instead; a single
    # run to the ground (a robe) uses the 45/55 fallback.
    def hexrgb(hx):
        return tuple(int(hx[i:i + 2], 16) for i in (1, 3, 5)) \
            if hx and len(hx) == 7 else (0, 0, 0)

    def cdist(h1, h2):
        a, b = hexrgb(h1), hexrgb(h2)
        return max(abs(a[0] - b[0]), abs(a[1] - b[1]), abs(a[2] - b[2]))

    below = [r for r in runs if r[0] > chin and not r[3]]
    hip = None
    if below and not hooded:
        torso_run = next((r for r in below if r[1] - r[0] + 1 >= H * 0.04),
                         below[0])
        hip = torso_run[1]
        color_split = False
        absorbed = 0
        for r in below[below.index(torso_run) + 1:]:
            if r[1] - r[0] + 1 < H * 0.03 and absorbed < H * 0.025:
                absorbed += r[1] - r[0] + 1
                hip = r[1]  # belt / detail line stays with the torso
                continue
            if cdist(r[2], torso_run[2]) > 45:
                hip = r[0] - 1
                color_split = True
            break
        if hip >= y1 - H * 0.02:
            hip = None
        elif not color_split and absorbed == 0 and hip == torso_run[1]:
            pass  # torso run end IS the hip (tiny pants strip below)
        elif not color_split:
            hip = None  # ambiguous: try the width profile
    if hip is None:
        leg_rows = widths[int(H * 0.90):]
        leg_w = sorted(leg_rows)[len(leg_rows) // 2] if leg_rows else 0
        if leg_w > 0:
            i = H - 1
            while i > int(H * 0.55):
                if widths[i] > leg_w * 1.25 and all(
                        widths[j] > leg_w * 1.25
                        for j in range(i, min(i + 3, H))):
                    hip = y0 + i
                    notes.append("torso/leg split via width profile")
                    break
                i -= 1
    if hip is None or hip >= y1 - H * 0.02:
        hip = chin + int((y1 - chin) * 0.45)
        notes.append("torso/leg split via robe fallback 45/55")

    torso_share = (hip - chin) / H
    leg_share = (y1 - hip) / H

    head_w = max(widths[face_top - y0: chin - y0 + 1])
    torso_rows = widths[chin - y0: hip - y0 + 1]
    torso_w = sorted(torso_rows)[len(torso_rows) // 2] if torso_rows else head_w

    # ---- head circle geometry -------------------------------------
    # The renderer draws the head as an ellipse and the hat AROUND it,
    # so the encoded geometry is: circle height share + circle aspect.
    face_h = chin - face_top + 1
    unit_h = chin - y0 + 1
    if all_skin or hat_share < 0.03:
        # bare head fully visible (Garrison, New Kid): read directly
        head_h = face_h if hat_share >= 0.03 else unit_h
        head_aspect = head_w / max(head_h, 1)
    elif hooded:
        # the hood IS the head unit; use its silhouette
        head_h = unit_h
        head_aspect = head_w / max(unit_h, 1)
        notes.append("hooded: head unit = silhouette")
    else:
        # hatted: the circle fills the head unit minus the cap the hat
        # draws above it (~8% of the unit); the circle's width is the
        # visible skin patch corrected for side coverage (x1.08), since
        # earflaps/brims hide the patch's edges, not its middle
        head_h = unit_h * 0.92
        oval_w = patch_w * 1.08
        head_aspect = min(1.35, max(0.9, oval_w / max(head_h, 1)))
        head_h = oval_w / head_aspect
    head_share = head_h / H

    skin, _ = dominant(im, mask, x0, x1, face_top, chin, only_skin=True)
    head_extra, _ = dominant(im, mask, x0, x1, y0, chin, skip_skin=True)
    top, top_cov = dominant(im, mask, x0, x1, chin, hip)
    shoe_band_y = y1 - max(2, int(H * 0.06))
    bottom, _ = dominant(im, mask, x0, x1, hip, shoe_band_y)
    shoe, _ = dominant(im, mask, x0, x1, shoe_band_y, y1)
    # the very bottom rows = the shoe line itself (thin dark strip on
    # the sheets) and the center-column run at the pants row
    shoe_line, _ = dominant(im, mask, x0, x1, y1 - max(1, int(H * 0.025)), y1)
    pants_run = None
    for (a, b, hx2, _sk) in runs:
        if a <= hip + 2 <= b:
            pants_run = hx2
            break

    return {
        "label": label,
        "height_px": H,
        "headShare": round(head_share, 3),
        "torsoShare": round(torso_share, 3),
        "legShare": round(leg_share, 3),
        "hatShare": round(hat_share, 3),
        "headAspect": round(head_aspect, 3),
        "headBodyWidth": round(head_w / max(torso_w, 1), 3),
        "skin": skin,
        "headExtra": head_extra,
        "top": top,
        "topCoverage": round(top_cov, 2),
        "bottom": bottom,
        "shoe": shoe,
        "shoeLine": shoe_line,
        "pantsRun": pants_run,
        "notes": notes,
    }


def components(mask, min_area=200):
    """Connected components (4-conn) of a boolean mask. Returns list of
    (area, bbox, [(x,y)...]) — coords in mask space."""
    h = len(mask)
    w = len(mask[0])
    seen = [[False] * w for _ in range(h)]
    comps = []
    for y in range(h):
        for x in range(w):
            if not mask[y][x] or seen[y][x]:
                continue
            q = deque([(x, y)])
            seen[y][x] = True
            pts = []
            minx = maxx = x
            miny = maxy = y
            while q:
                cx, cy = q.popleft()
                pts.append((cx, cy))
                minx = min(minx, cx)
                maxx = max(maxx, cx)
                miny = min(miny, cy)
                maxy = max(maxy, cy)
                for nx, ny in ((cx + 1, cy), (cx - 1, cy), (cx, cy + 1), (cx, cy - 1)):
                    if 0 <= nx < w and 0 <= ny < h and mask[ny][nx] and not seen[ny][nx]:
                        seen[ny][nx] = True
                        q.append((nx, ny))
            if len(pts) >= min_area:
                comps.append((len(pts), (minx, miny, maxx, maxy), pts))
    comps.sort(key=lambda c: -c[0])
    return comps


def mask_from_pts(w, h, pts):
    m = [[False] * w for _ in range(h)]
    for (x, y) in pts:
        m[y][x] = True
    return m


def downscaled(im, max_dim=640):
    w, h = im.size
    if max(w, h) <= max_dim:
        return im
    f = max_dim / max(w, h)
    return im.resize((int(w * f), int(h * f)), Image.BILINEAR)


def measure_sheet(path):
    """Construction sheet: measure the assembled figure (largest component
    in the bottom-right) + HEAD/TORSO part palettes (left column).
    Runs on a downscaled copy — all outputs are ratios / dominant colors,
    so scale does not change them, and component labeling stays fast."""
    im = downscaled(load_rgba(path))
    w, h = im.size
    full = build_mask(im)
    # assembled figure zone: right 42%, bottom 58%
    zx0, zy0 = int(w * 0.58), int(h * 0.42)
    zone = [[full[y][x] if x >= zx0 and y >= zy0 else False
             for x in range(w)] for y in range(h)]
    comps = components(zone, min_area=120)
    if not comps:
        return {"label": path, "error": "no assembled figure found"}
    # The assembled figure can split into pieces at thin joins (Cartman's
    # hat separates from his face). Merge every sizable component that
    # overlaps the largest one's x-range and sits near it vertically;
    # neighboring sheet parts (mouths, eyelids, name text) fail the
    # x-overlap or area test and stay out.
    _area, (lx0, ly0, lx1, ly1), pts = comps[0]
    merged = list(pts)
    for _a, (cx0, cy0, cx1, cy1), cpts in comps[1:]:
        if _a < 150:
            continue
        x_overlap = min(lx1, cx1) - max(lx0, cx0)
        if cx1 - cx0 + 1 > 1.6 * (lx1 - lx0 + 1):
            continue  # the sheet's frame spans everything; never merge it
        if x_overlap < 0.4 * (lx1 - lx0 + 1) or x_overlap < 0.5 * (cx1 - cx0 + 1):
            continue
        if cy1 < ly0 - 0.55 * (ly1 - ly0 + 1) or cy0 > ly1 + 0.4 * (ly1 - ly0 + 1):
            continue
        merged.extend(cpts)
    fig_mask = mask_from_pts(w, h, merged)
    res = measure_figure(im, fig_mask, path)
    # part palettes: HEAD part = largest component in top-left quadrant,
    # TORSO part = largest in mid-left band
    head_zone = [[full[y][x] if x < w * 0.36 and y < h * 0.42 else False
                  for x in range(w)] for y in range(h)]
    hc = components(head_zone, min_area=300)
    if hc:
        _a, (hx0, hy0, hx1, hy1), hpts = hc[0]
        hm = mask_from_pts(w, h, hpts)
        res["headPartSkin"], _ = dominant(im, hm, hx0, hx1, hy0, hy1, only_skin=True)
        res["headPartExtra"], _ = dominant(im, hm, hx0, hx1, hy0, hy1, skip_skin=True)
        # second accent: re-run with the dominant bucket's color excluded
        ex = res["headPartExtra"]
        if ex:
            ergb = tuple(int(ex[i:i + 2], 16) for i in (1, 3, 5))

            def not_first(c, _e=ergb):
                return max(abs(c[0] - _e[0]), abs(c[1] - _e[1]),
                           abs(c[2] - _e[2])) > 40
            res["headPartExtra2"] = dominant_where(
                im, hm, hx0, hx1, hy0, hy1, not_first)
    torso_zone = [[full[y][x] if x < w * 0.36 and h * 0.40 <= y < h * 0.72 else False
                   for x in range(w)] for y in range(h)]
    tc = components(torso_zone, min_area=300)
    if tc:
        _a, (tx0, ty0, tx1, ty1), tpts = tc[0]
        tm = mask_from_pts(w, h, tpts)
        res["torsoPartTop"], _ = dominant(im, tm, tx0, tx1, ty0, ty1)
    return res


def classify_sheet(res):
    """Name a construction sheet from its HEAD PART palette (the part is
    an unambiguous drawing of the head; the assembled figure's head
    region is dominated by the big white eyes) + torso color."""
    extra = (res.get("headPartExtra") or res.get("headExtra") or "").lower()
    top = (res.get("top") or "").lower()

    def rgb(hexv):
        if not hexv or len(hexv) != 7:
            return (0, 0, 0)
        return tuple(int(hexv[i:i + 2], 16) for i in (1, 3, 5))

    er, eg, eb = rgb(extra)
    tr, tg, tb = rgb(top)
    if er > 200 and 55 < eg < 185 and eb < 100:
        return "Kenny"        # orange hood
    if eg > 120 and er < 120 and eb < 110 and tr > 200:
        return "Kyle"         # green hat (low blue) + orange jacket
    if eg > 140 and abs(eg - eb) < 45 and er < 120:
        return "Cartman"      # teal hat (green ~= blue)
    if eb > 130 and 90 < tr < 175 and 55 < tg < 115 and tb < 92:
        return "Stan"         # blue hat + brown jacket
    if eb > 120:
        return "Craig"        # blue chullo + blue jacket
    return "unknown"


def measure_chart(path):
    """Adolescence chart: 5 mannequin figures left→right = age bands
    10-12 / 13-14 / 15-16 / 17-18 / 19-20. The mannequins are uniform
    warm skin on warm off-white paper, so distance-to-skin fails; the
    mask uses the warm channel gap (r-b) instead, figures are split by
    x-projection valleys, and each head is measured by an ellipse fit
    over its unobstructed top half."""
    im = load_rgba(path).convert("RGB")
    w, h = im.size
    px = im.load()

    def warm(c):
        r, g, b = c
        return r > 190 and (r - b) > 38 and (r - g) > 12

    mask = [[warm(px[x, y]) for x in range(w)] for y in range(h)]
    colsum = [sum(1 for y in range(h) if mask[y][x]) for x in range(w)]
    segs = []
    inseg = False
    start = 0
    for x in range(w):
        if colsum[x] > 2 and not inseg:
            start = x
            inseg = True
        elif colsum[x] <= 2 and inseg:
            segs.append((start, x - 1))
            inseg = False
    if inseg:
        segs.append((start, w - 1))
    bands = ["10-12", "13-14", "15-16", "17-18", "19-20"]
    out = []
    for (x0, x1) in [s for s in segs if s[1] - s[0] > 40][:5]:
        rows = [y for y in range(h) if any(mask[y][x] for x in range(x0, x1 + 1))]
        fy0, fy1 = min(rows), max(rows)
        H = fy1 - fy0 + 1
        widths = [sum(1 for x in range(x0, x1 + 1) if mask[y][x])
                  for y in range(fy0, fy1 + 1)]
        hw = max(widths[: int(H * 0.40)])
        y70 = next((i for i, wd in enumerate(widths) if wd >= 0.7 * hw), 0)
        head_h = 2 * (y70 / 0.286) if y70 > 0 else hw
        out.append({"band": bands[len(out)], "heightPx": H,
                    "headShare": round(head_h / H, 3),
                    "headAspect": round(hw / head_h, 3)})
    if out:
        base = out[0]["heightPx"]
        for o in out:
            o["heightRel"] = round(o["heightPx"] / base, 3)
    return out


def main():
    import os
    uf = os.path.expanduser("~/workspace/user/files")
    root = os.path.expanduser("~/workspace/sptown-app")
    results = {}

    sheets = ["261058_5_5zar.png", "261057_6_d0ik.png", "261056_7_vvvi.png",
              "261054_8_x9tu.png", "261053_9_arfb.png"]
    for s in sheets:
        r = measure_sheet(os.path.join(uf, s))
        r["file"] = s
        r["classifiedAs"] = classify_sheet(r) if "error" not in r else "error"
        results["sheet:" + s] = r
        if r["classifiedAs"] not in ("unknown", "error"):
            results[r["classifiedAs"]] = r

    def largest_only(im):
        m = build_mask(im)
        comps = components(m, min_area=200)
        if not comps:
            return m
        return mask_from_pts(im.size[0], im.size[1], comps[0][2])

    gm = load_rgba(os.path.join(uf, "261061_11_ocqn.png"))
    results["Garrison"] = measure_figure(gm, largest_only(gm), "garrison-model")

    nk = load_rgba(os.path.join(root, "docs/reference-new-kid.jpg"))
    results["_newkid"] = measure_figure(nk, largest_only(nk), "new-kid")

    chart = measure_chart(os.path.join(uf, "photostudio_1791830680276_3_fgew.png"))
    results["_chart"] = chart

    out_path = os.path.join(root, "tools/measurements.json")
    with open(out_path, "w") as f:
        json.dump(results, f, indent=1)
    for k, v in results.items():
        print("==", k, json.dumps(v)[:600])
    print("wrote", out_path)


if __name__ == "__main__":
    main()
