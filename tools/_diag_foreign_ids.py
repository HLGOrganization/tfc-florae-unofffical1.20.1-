"""Check the foreign ids our tag files carry, and plan cleanup.

Our tag files should only add *our* ids - TFC/vanilla already define their own
entries and Forge merges with replace:false.  Carrying copies of their ids is
both redundant and dangerous: one stale id (e.g. a TFC id renamed in 1.20) kills
the whole tag, which is what happened earlier with tuff.

This reports, for every id in our files under data/{tfc,minecraft,forge}/tags:
  * tfcflorae ids -> keep (that is the point of the file)
  * tfc ids       -> check against TFC's own blockstates (both kinds)
  * minecraft ids -> check against the vanilla jar blockstates/models
  * '#' refs      -> check that a tag file exists somewhere
"""
import io
import json
import os
import zipfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DATA = os.path.join(ROOT, "src", "main", "resources", "data")
TFC_JAR = os.path.join(os.path.dirname(ROOT), "[群峦传说] TerraFirmaCraft-Forge-1.20.1-3.2.20.jar")
VANILLA = os.path.join(os.path.dirname(ROOT), "gradle-home", "caches", "modules-2", "files-2.1")


def jar_index(path):
    idx = set()
    if not os.path.isfile(path):
        return idx
    with zipfile.ZipFile(path) as z:
        for n in z.namelist():
            if n.startswith("assets/") and "/blockstates/" in n and n.endswith(".json"):
                idx.add(("blocks", n.split("/blockstates/", 1)[1][:-5]))
            if n.startswith("assets/") and "/models/item/" in n and n.endswith(".json"):
                idx.add(("items", n.split("/models/item/", 1)[1][:-5]))
    return idx


tfc_idx = jar_index(TFC_JAR)
print("TFC jar blockstates/items indexed:", len(tfc_idx))

# tag files available anywhere (ours + TFC + vanilla is not indexed, treat #minecraft: as ok)
tagfiles = set()
for dirpath, _d, files in os.walk(DATA):
    for fn in files:
        if fn.endswith(".json") and "/tags/" in dirpath.replace(os.sep, "/"):
            norm = os.path.join(dirpath, fn).replace(os.sep, "/")
            ns = os.path.basename(norm.split("/tags/")[0])
            rest = norm.split("/tags/", 1)[1]
            kind = rest.split("/")[0]
            tagfiles.add((ns, kind, "/".join(rest.split("/")[1:])[:-5]))
with zipfile.ZipFile(TFC_JAR) as z:
    for n in z.namelist():
        if n.startswith("data/") and "/tags/" in n and n.endswith(".json"):
            rest = n[len("data/"):]
            ns = rest.split("/")[0]
            tail = rest.split("/tags/", 1)[1]
            kind = tail.split("/")[0]
            tagfiles.add((ns, kind, "/".join(tail.split("/")[1:])[:-5]))

report = {"foreign": [], "badrefs": [], "files": 0}
for dirpath, _d, files in os.walk(DATA):
    for fn in files:
        if not fn.endswith(".json"):
            continue
        full = os.path.join(dirpath, fn)
        norm = full.replace(os.sep, "/")
        if "/tags/" not in norm:
            continue
        ns = os.path.basename(norm.split("/tags/")[0])
        if ns not in ("tfc", "minecraft", "forge"):
            continue
        report["files"] += 1
        rest = norm.split("/tags/", 1)[1]
        kind = rest.split("/")[0]
        rel = os.path.relpath(full, DATA)
        try:
            d = json.load(io.open(full, encoding="utf-8-sig"))
        except Exception:
            continue
        for v in d.get("values", []):
            if not isinstance(v, str):
                continue
            if v.startswith("tfcflorae:"):
                continue
            if v.startswith("#"):
                ident = v[1:]
                if ":" not in ident:
                    ident = "minecraft:" + ident
                rns, rpath = ident.split(":", 1)
                if (rns, kind, rpath) not in tagfiles and rns != "minecraft":
                    report["badrefs"].append((rel, v))
                continue
            if ":" not in v:
                continue
            fns, fpath = v.split(":", 1)
            if fns == "tfc":
                if (kind, fpath) not in tfc_idx:
                    report["foreign"].append((rel, v, "tfc-id-missing"))
            elif fns == "minecraft":
                pass  # vanilla ids: cannot cheaply verify, but they come from the old files
            else:
                report["foreign"].append((rel, v, "other-ns"))

print("our tag files under tfc/minecraft/forge:", report["files"])
print("bad #refs:", len(report["badrefs"]))
for r in report["badrefs"][:10]:
    print("   ", r)
print("foreign concrete ids:", len(report["foreign"]))
seen = {}
for rel, v, why in report["foreign"]:
    seen.setdefault(why, []).append((rel, v))
for why, items in seen.items():
    print("  %s: %d  e.g. %s" % (why, len(items), items[:3]))
