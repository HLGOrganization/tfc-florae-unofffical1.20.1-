"""Add zh_cn / en_us names and tag entries for the new TFE clay variants.

Language files are rewritten in place preserving key order (the existing files are only
roughly alphabetical, so a full re-sort would produce a huge diff).  Each new key is
inserted right after the last existing key of its own group.

Tags: the four new variants of each clay join the same tags the four existing variants
already sit in, so the blocks behave like clay / dirt everywhere the old ones do:

    tfc:clay          (blocks + items)  -> tfcflorae:<clay>/<family>
    minecraft:dirt    (blocks + items)  -> tfcflorae:<clay>_grass/<family>
"""
import io
import json
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "src", "main", "resources")

CLAWS = ("earthenware_clay", "stoneware_clay")
FAMILIES = ("fluvisol", "mollisol", "oxisol", "podzol")

# family -> (zh_cn soil name, zh_cn grass name, en_us soil name, en_us grass name)
NAMES = {
    "fluvisol": ("冲积", "Fluvisol"),
    "mollisol": ("黑", "Mollisol"),
    "oxisol": ("氧化", "Oxisol"),
    "podzol": ("灰化", "Podzol"),
}

ZH_CLAY = {"earthenware_clay": "陶土", "stoneware_clay": "石胎瓷土"}
EN_CLAY = {"earthenware_clay": "Earthenware Clay", "stoneware_clay": "Stoneware Clay"}


def build_lang(lang):
    """Return {key: value} for the 16 new keys, plus anchor keys to insert after."""
    out = []
    for clay in CLAWS:
        for fam in FAMILIES:
            zh_prefix, en_name = NAMES[fam]
            if lang == "zh_cn":
                soil = zh_prefix + ZH_CLAY[clay]
                grass = soil + "草方块"
            else:
                soil = "%s %s" % (EN_CLAY[clay], en_name)
                grass = "%s Grass %s" % (EN_CLAY[clay], en_name)
            out.append(("block.tfcflorae.%s.%s" % (clay, fam), soil,
                        "block.tfcflorae.%s.silty_loam" % clay))
            out.append(("block.tfcflorae.%s_grass.%s" % (clay, fam), grass,
                        "block.tfcflorae.%s_grass.silty_loam" % clay))
    return out


def patch_lang(lang):
    p = os.path.join(RES, "assets", "tfcflorae", "lang", "%s.json" % lang)
    with io.open(p, encoding="utf-8") as fh:
        data = json.load(fh)

    entries = build_lang(lang)
    # group by anchor so we can splice in one pass
    by_anchor = {}
    for key, value, anchor in entries:
        by_anchor.setdefault(anchor, []).append((key, value))

    new = {}
    added = 0
    for key, value in data.items():
        new[key] = value
        if key in by_anchor:
            for nk, nv in by_anchor[key]:
                if nk not in data:
                    new[nk] = nv
                    added += 1
    missing = [k for k, _, a in entries if k not in new]
    with io.open(p, "w", encoding="utf-8", newline="\n") as fh:
        fh.write(json.dumps(new, indent=2, sort_keys=False, ensure_ascii=False) + "\n")
    print("  %-6s +%d keys (missing after insert: %s)" % (lang, added, missing or "none"))


TAG_FILES = [
    ("data/tfc/tags/blocks/clay.json", "%s/%s", CLAWS),
    ("data/tfc/tags/items/clay.json", "%s/%s", CLAWS),
    ("data/minecraft/tags/blocks/dirt.json", "%s_grass/%s", CLAWS),
    ("data/minecraft/tags/items/dirt.json", "%s_grass/%s", CLAWS),
]


def patch_tag(rel, pattern, claws):
    p = os.path.join(RES, rel.replace("/", os.sep))
    with io.open(p, encoding="utf-8") as fh:
        data = json.load(fh)
    values = list(data.get("values", []))
    existing = set(values)

    group = set()
    for clay in claws:
        for fam in ("silt", "loam", "sandy_loam", "silty_loam") + FAMILIES:
            group.add("tfcflorae:" + pattern % (clay, fam))

    added = []
    for clay in claws:
        for fam in FAMILIES:
            bid = "tfcflorae:" + pattern % (clay, fam)
            if bid in existing:
                continue
            # insert alphabetically within our own group, else right after the group
            idx = None
            for i, v in enumerate(values):
                if v in group and v > bid:
                    idx = i
                    break
            if idx is None:
                last = max((i for i, v in enumerate(values) if v in group), default=len(values) - 1)
                idx = last + 1
            values.insert(idx, bid)
            existing.add(bid)
            added.append(bid)

    data["values"] = values
    with io.open(p, "w", encoding="utf-8", newline="\n") as fh:
        fh.write(json.dumps(data, indent=2, sort_keys=False, ensure_ascii=False) + "\n")
    print("  %-46s +%d -> %d values" % (rel, len(added), len(values)))


def main():
    print("=== lang ===")
    for lang in ("zh_cn", "en_us"):
        patch_lang(lang)
    print()
    print("=== tags ===")
    for rel, pattern, claws in TAG_FILES:
        patch_tag(rel, pattern, claws)


if __name__ == "__main__":
    main()
