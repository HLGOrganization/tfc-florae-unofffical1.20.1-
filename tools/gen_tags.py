"""Give tfcflorae blocks/items the same tags TFC gives its own equivalents.

How it works (data-driven, no Java source involved)
---------------------------------------------------
1. Parse every tag JSON shipped by TFC, minecraft and forge inside the TFC jar.
2. Resolve `#tag` references so each concrete id knows every tag it ends up in.
3. For each of our registered ids, pick the TFC id it is "the same kind of thing
   as" (a wood part of another species, a rock part of another rock, or simply
   `tfc:<same path>`), and copy that id's whole tag set onto ours.
4. Write `data/<ns>/tags/<kind>/<tag>.json` with `replace:false`, containing only
   our ids.

Hard safety rule: **every id written must be in the registered-id whitelist**
(`reports/registered-ids.json`, produced by `audit_register_assets.py --dump-ids`).
A single unknown id makes the whole tag fail to load, which is exactly what made
every TFC leaf block vanish last time (one bogus entry in `minecraft:logs`).

Usage
    python tools/audit_register_assets.py --dump-ids reports/registered-ids.json
    python tools/gen_tags.py [--check]
"""
from __future__ import annotations

import argparse
import collections
import json
import os
import zipfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "src", "main", "resources")
DATA = os.path.join(RES, "data")
ASSETS = os.path.join(RES, "assets", "tfcflorae")
TFC_JAR = r"D:\学习资料\附属模组工作区\[群峦传说] TerraFirmaCraft-Forge-1.20.1-3.2.20.jar"
WHITELIST = os.path.join(ROOT, "reports", "registered-ids-full.json")

# Soil families that are deliberately no longer registered (see
# TFCFBlocks.REMOVED_SOILS): duplicate grass paths, packed/loose mud, coarse
# dirt and the broken dry/dense/sparse grass blocks.  The source-level audit
# still expands their register() call (it does not model the REMOVED_SOILS
# filter), so they must be excluded here or the tags would reference blocks
# that do not exist - which is exactly what broke every leaf block once.
REMOVED_SOILS = {
    "grass_path", "dense_grass_path", "sparse_grass_path", "dry_grass_path",
    "packed_mud", "mossy_packed_mud", "loose_mud", "coarse_dirt",
    "dry_grass", "dense_grass", "sparse_grass",
    "dry_clay_grass", "dense_clay_grass", "sparse_clay_grass",
    "dry_earthenware_clay_grass", "dense_earthenware_clay_grass", "sparse_earthenware_clay_grass",
    "dry_stoneware_clay_grass", "dense_stoneware_clay_grass", "sparse_stoneware_clay_grass",
}

# TFCFRock -> TFC geology tag prefix
ROCK_CATEGORY = {
    "red_granite": "igneous_intrusive",
    "porphyry": "igneous_extrusive",
    "boninite": "igneous_extrusive",
    "carbonatite": "igneous_extrusive",
    "tuff": "igneous_extrusive",
    "laterite": "sedimentary",
    "mudstone": "sedimentary",
    "sandstone": "sedimentary",
    "arkose": "sedimentary",
    "jaspillite": "sedimentary",
    "wackestone": "sedimentary",
    "cataclasite": "metamorphic",
    "soapstone": "metamorphic",
}


def wood_names():
    base = os.path.join(ASSETS, "blockstates", "wood", "planks")
    return {f[:-5] for f in os.listdir(base) if f.endswith(".json")}


def rock_names():
    return set(ROCK_CATEGORY)


def load_whitelist():
    d = json.load(open(WHITELIST, encoding="utf-8"))
    blocks = {b for b in d["blocks"] if b.split("/")[0] not in REMOVED_SOILS}
    items = {i for i in d["items"] if i.split("/")[0] not in REMOVED_SOILS}
    return blocks, items


def load_tfc_tags():
    """{(kind, tag_id): [raw values]} for tfc / minecraft / forge namespaces."""
    z = zipfile.ZipFile(TFC_JAR)
    out = {}
    for n in z.namelist():
        if "/tags/" not in n or not n.endswith(".json") or not n.startswith("data/"):
            continue
        ns = n.split("/")[1]
        if ns not in ("tfc", "minecraft", "forge"):
            continue
        rest = n.split("/tags/", 1)[1][:-5]
        kind, path = rest.split("/", 1)
        if kind not in ("blocks", "items"):
            continue
        try:
            d = json.loads(z.read(n))
        except Exception:
            continue
        vals = [v if isinstance(v, str) else v.get("id", "") for v in d.get("values", [])]
        out[(kind, "%s:%s" % (ns, path))] = [v for v in vals if v]
    return out


def build_reverse(tags):
    """{(kind, concrete id): {tag ids it belongs to}} with #refs resolved."""
    rev = collections.defaultdict(set)
    seen_stack = set()

    def resolve(kind, tag, depth=0):
        if depth > 16 or (kind, tag) in seen_stack:
            return set()
        seen_stack.add((kind, tag))
        try:
            vals = tags.get((kind, tag), [])
            out = set()
            for v in vals:
                if v.startswith("#"):
                    out |= resolve(kind, v[1:], depth + 1)
                else:
                    out.add(v)
            return out
        finally:
            seen_stack.discard((kind, tag))

    for (kind, tag), _vals in tags.items():
        for ident in resolve(kind, tag):
            rev[(kind, ident)].add(tag)
    return rev


def ref_id(our_id, woods, rocks):
    """TFC id that is 'the same kind of thing' as ours."""
    parts = our_id.split("/")
    last = parts[-1]
    if parts[0] == "wood" and len(parts) >= 3 and last in woods:
        return "tfc:wood/%s/oak" % "/".join(parts[1:-1])
    if parts[0] in ("rock", "ore", "deposit") and len(parts) >= 3 and last in rocks:
        return "tfc:%s/%s/granite" % (parts[0], "/".join(parts[1:-1]))
    if parts[0] == "brick" and len(parts) == 2 and last in rocks:
        return "tfc:brick/granite"
    return "tfc:" + our_id


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--check", action="store_true")
    args = ap.parse_args()

    blocks, items = load_whitelist()
    woods, rocks = wood_names(), rock_names()
    print("whitelist: %d blocks, %d items | woods %d | rocks %d" % (len(blocks), len(items), len(woods), len(rocks)))

    tags = load_tfc_tags()
    print("TFC data pack tags parsed: %d" % len(tags))
    rev = build_reverse(tags)
    print("concrete ids with tag knowledge: %d" % len(rev))

    out = collections.defaultdict(set)   # (kind, tag) -> {our ids}
    covered = collections.Counter()
    unmatched = []

    for kind, ids in (("blocks", blocks), ("items", items)):
        for our_id in ids:
            ref = ref_id(our_id, woods, rocks)
            tagset = rev.get((kind, ref))
            if tagset:
                for t in tagset:
                    out[(kind, t)].add("tfcflorae:" + our_id)
                covered[kind] += 1
            elif our_id.split("/")[0] in ("wood", "rock", "ore", "deposit", "brick"):
                unmatched.append((kind, our_id, ref))

    # geology tags, driven directly by TFCFRock's category
    geo = 0
    for kind, ids in (("blocks", blocks), ("items", items)):
        for our_id in ids:
            parts = our_id.split("/")
            rock = parts[-1]
            if rock not in ROCK_CATEGORY:
                continue
            if parts[0] not in ("rock", "ore", "deposit", "brick"):
                continue
            cat = ROCK_CATEGORY[rock]
            # `<cat>_items` = every item of that rock class; `<cat>_rock` = loose samples
            out[(kind, "tfc:%s_items" % cat)].add("tfcflorae:" + our_id)
            if parts[0] == "rock" and parts[1] in ("loose", "mossy_loose"):
                out[(kind, "tfc:%s_rock" % cat)].add("tfcflorae:" + our_id)
            geo += 1

    # never write anything that isn't actually registered
    whitelist = {"blocks": {"tfcflorae:" + i for i in blocks}, "items": {"tfcflorae:" + i for i in items}}
    bad = []
    clean = {}
    for (kind, tag), ids in out.items():
        allowed = whitelist[kind]
        keep = {i for i in ids if i in allowed}
        if len(keep) != len(ids):
            bad.append((kind, tag, len(ids) - len(keep)))
        if keep:
            clean[(kind, tag)] = keep

    written = []
    for (kind, tag), ids in sorted(clean.items()):
        ns, path = tag.split(":", 1)
        target = os.path.join(DATA, ns, "tags", kind, path + ".json")
        existing = set()
        if os.path.isfile(target):
            try:
                existing = {v for v in json.load(open(target, encoding="utf-8-sig")).get("values", []) if isinstance(v, str)}
            except Exception:
                existing = set()
        # anything of ours that is no longer registered would break the whole tag
        existing = {v for v in existing
                    if not v.startswith("tfcflorae:") or v in whitelist[kind]}
        merged = sorted(existing | ids)
        written.append((os.path.relpath(target, ROOT), len(merged), len(ids)))
        if not args.check:
            os.makedirs(os.path.dirname(target), exist_ok=True)
            with open(target, "w", encoding="utf-8", newline="\n") as fh:
                json.dump({"replace": False, "values": merged}, fh, indent=2, ensure_ascii=False)
                fh.write("\n")

    print()
    print("tag files written: %d   (check=%s)" % (len(written), args.check))
    print("ids with a TFC-twin tag set: blocks %d / %d, items %d / %d" % (
        covered["blocks"], len(blocks), covered["items"], len(items)))
    print("geology assignments: %d" % geo)
    if bad:
        print("DROPPED unregistered ids (safety):", bad[:5])
    if unmatched:
        print("no TFC twin (%d), needs a manual rule:" % len(unmatched))
        for k, i, r in unmatched[:20]:
            print("   %-6s %-46s -> %s" % (k, i, r))
    for path, total, ours in written[:15]:
        print("   %-72s %d values (+%d ours)" % (path, total, ours))


if __name__ == "__main__":
    main()
