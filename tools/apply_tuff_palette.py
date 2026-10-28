"""Recolour the tuff rock textures with the tuff palette supplied by the user.

The tuff texture set is not original art: almost every `*tuff*.png` is a copy of
another rock's sprite (boninite for raw/cobble/bricks/..., other rocks for the
sand/dirt composites).  Instead of redrawing anything, this tool keeps each
sprite's pixels/structure and only remaps the *rock* colours onto the five-colour
tuff palette below.

Why a mask instead of a global palette swap
-------------------------------------------
Many tuff blocks are composites: `sand/rocky/<colour>/tuff` is rock gravel over
coloured sand, `rockier_compact_dirt/<soil>/tuff` is rock over dirt, and
`rock/magma/tuff` is rock plus lava.  Recolouring the whole image would tint the
sand, dirt and lava too.  For a given texture, its sibling variants (the same
sprite for the other rocks) share the non-rock base pixel-for-pixel and differ
exactly on the rock pixels, so:

    rock_mask = { pixels where the sibling variants are not all identical }

Only pixels inside the mask are remapped; sand, dirt, lava, moss overlays keep
their original colours.

Mapping
-------
Rock pixel luminance is quantised into the five palette steps by *quantile*
(20/40/60/80 %), so every sprite uses the full palette and keeps its own contrast.

Usage
    python tools/apply_tuff_palette.py            # dry run, prints a summary
    python tools/apply_tuff_palette.py --apply    # rewrite the textures
"""
from __future__ import annotations

import argparse
import collections
import os

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TEX = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae", "textures")
ROCK_DIR = os.path.join(TEX, "block", "rock", "raw")
# The user-supplied 16x16 tuff rock texture.  It is the colour source for the
# whole swap and is written verbatim to block/rock/raw/tuff.png.
REFERENCE = os.path.join(ROOT, "tools", "reference", "tuff_raw.png")
RAW_TARGET = "block/rock/raw/tuff.png"

SKIP_TOP_DIRS = ("entity",)  # chest entity textures are not rock art


def luminance(c):
    return 0.299 * c[0] + 0.587 * c[1] + 0.114 * c[2]


def palette_and_quantiles():
    """Palette (dark -> light) and cumulative colour distribution of the reference."""
    im = Image.open(REFERENCE).convert("RGBA")
    counts = collections.Counter(p for p in im.getdata() if p[3] > 0)
    ordered = sorted(counts.items(), key=lambda kv: luminance(kv[0]))
    palette = [c for c, _n in ordered]
    total = sum(counts.values())
    quantiles = []
    run = 0
    for c, n in ordered[:-1]:
        run += n
        quantiles.append(run / total)
    return palette, tuple(quantiles)


PALETTE, QUANTILES = palette_and_quantiles()
PALETTE_SET = set(PALETTE)


def rock_names():
    names = [f[:-4] for f in os.listdir(ROCK_DIR) if f.endswith(".png")]
    return sorted(n for n in names if n != "tuff")


def find_siblings(rel, rocks):
    """Existing sibling textures for the other rocks (same folder / file name)."""
    d, fn = os.path.split(rel)
    out = []
    for r in rocks:
        cand = fn.replace("tuff", r)
        p = os.path.join(TEX, d, cand)
        if os.path.isfile(p):
            im = Image.open(p).convert("RGBA")
            out.append((r, im))
    return out


def build_mask(tuff_px, siblings):
    """True for pixels that are not shared by every sibling (= rock pixels)."""
    mask = [False] * len(tuff_px)
    for i in range(len(tuff_px)):
        if tuff_px[i][3] == 0:
            continue
        first = siblings[0][1][i]
        differs = any(s[1][i] != first for s in siblings[1:])
        if differs:
            mask[i] = True
    return mask


def quantize(rock_pixels):
    """Map rock pixel colours onto the palette using luminance quantiles."""
    lums = sorted(luminance(p) for p in rock_pixels)
    if not lums:
        return {}
    if lums[0] == lums[-1]:
        return {p: PALETTE[2] for p in rock_pixels}
    cuts = [lums[min(len(lums) - 1, int(len(lums) * q))] for q in QUANTILES]
    out = {}
    for p in rock_pixels:
        lum = luminance(p)
        idx = 4
        for i, cut in enumerate(cuts):
            if lum <= cut:
                idx = i
                break
        out[p] = PALETTE[idx]
    return out


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--apply", action="store_true")
    ap.add_argument("--limit", type=int, default=0)
    args = ap.parse_args()

    rocks = rock_names()
    print("palette:", PALETTE)
    print("sibling rock sprites:", len(rocks), "->", rocks[:6], "...")

    targets = []
    for dirpath, _d, files in os.walk(TEX):
        for fn in files:
            if not fn.endswith(".png") or "tuff" not in fn:
                continue
            rel = os.path.relpath(os.path.join(dirpath, fn), TEX).replace(os.sep, "/")
            if rel.split("/")[0] in SKIP_TOP_DIRS:
                continue
            targets.append(rel)
    targets.sort()
    print("tuff textures to process:", len(targets))

    summary = collections.Counter()
    skipped = []
    rows = []

    # The reference texture *is* the tuff raw rock sprite: write it verbatim.
    raw_path = os.path.join(TEX, RAW_TARGET)
    ref_bytes = open(REFERENCE, "rb").read()
    cur_bytes = open(raw_path, "rb").read() if os.path.isfile(raw_path) else b""
    if ref_bytes != cur_bytes:
        if args.apply:
            open(raw_path, "wb").write(ref_bytes)
        summary["raw replaced with reference"] += 1
    else:
        summary["raw already reference"] += 1

    for rel in targets[: args.limit or None]:
        if rel == RAW_TARGET:
            continue
        path = os.path.join(TEX, rel)
        im = Image.open(path).convert("RGBA")
        px = list(im.getdata())
        sib = find_siblings(rel, rocks)
        sib = [(r, list(sim.getdata())) for r, sim in sib if sim.size == im.size]
        if not sib:
            summary["no sibling"] += 1
            skipped.append((rel, "no sibling"))
            continue
        mask = build_mask(px, sib)
        rock_px = [px[i] for i in range(len(px)) if mask[i]]
        if not rock_px:
            summary["no rock pixels"] += 1
            skipped.append((rel, "no rock pixels"))
            continue
        if set(rock_px) <= PALETTE_SET:
            summary["already tuff palette"] += 1
            skipped.append((rel, "already palette"))
            continue

        mapping = quantize(rock_px)
        new_px = list(px)
        changed = 0
        for i in range(len(px)):
            if mask[i]:
                repl = mapping.get(px[i])
                if repl is not None and repl != px[i]:
                    new_px[i] = repl
                    changed += 1
        summary["recoloured"] += 1
        rows.append((rel, len(rock_px), changed, len(set(rock_px)), len(set(mapping.values()))))
        if args.apply:
            out = Image.new("RGBA", im.size)
            out.putdata(new_px)
            out.save(path)

    print()
    for k, v in summary.most_common():
        print("  %-24s %d" % (k, v))
    if skipped:
        print()
        print("  skipped:")
        for rel, why in skipped[:20]:
            print("    %-56s %s" % (rel, why))
        if len(skipped) > 20:
            print("    ... %d more" % (len(skipped) - 20))

    print()
    print("== per texture (rock pixels / changed / distinct source colours / palette steps used) ==")
    for rel, nrock, changed, ncol, nsteps in rows:
        print("  %-58s rock=%-4d changed=%-4d src_colours=%-4d steps=%d" % (rel, nrock, changed, ncol, nsteps))
    print()
    print(("APPLIED" if args.apply else "DRY RUN") + ": %d textures recoloured" % summary["recoloured"])


if __name__ == "__main__":
    main()
