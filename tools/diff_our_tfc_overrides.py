"""Structural diff of every data/tfc file we ship, against TFC and TFE's own copies.

The goal is to find files where we did more than append entries - a dropped field, a changed
value, a different type - because the vegetation loss has no data-level explanation yet.

For each path present in our jar under data/tfc/, compare against the newest original
(TFE wins over TFC).  Report:
  * keys present in the original but missing in ours   (dropped)
  * keys present in ours but not the original          (added)
  * values that differ                                 (changed)
  * list length changes                                (appended/removed entries)
"""
import json
import os
import zipfile

MODS = r"D:\线框的服务器\versions\TFC_Wild_Fire\mods"
TFC = zipfile.ZipFile(os.path.join(MODS, "[群峦传说] TerraFirmaCraft-Forge-1.20.1-3.2.20.jar"))
TFE = zipfile.ZipFile(os.path.join(MODS, "Wildfire_TerraFirmaEarth-1.20.1-forge-2.3.0.jar"))
OURS = zipfile.ZipFile(os.path.join(MODS, "TFCFlorae-1.20.1-2.2.0-mc1.20.1.jar"))


def load(z, name):
    try:
        return json.loads(z.read(name).decode("utf-8-sig"))
    except Exception:
        return None


def flat(o, pre=""):
    out = {}
    if isinstance(o, dict):
        for k, v in o.items():
            out.update(flat(v, pre + "/" + str(k)))
    elif isinstance(o, list):
        for i, v in enumerate(o):
            out.update(flat(v, pre + "[%d]" % i))
        out[pre] = "len=%d" % len(o)
    else:
        out[pre] = o
    return out


rows = []
for n in sorted(OURS.namelist()):
    if not n.startswith("data/tfc/") or not n.endswith(".json"):
        continue
    base_name = n if n in TFE.namelist() else (n if n in TFC.namelist() else None)
    if base_name is None:
        rows.append((n, "no original", {}))
        continue
    mine = flat(load(OURS, n) or {})
    theirs = flat(load(TFE if n in TFE.namelist() else TFC, n) or {})
    dropped = [k for k in theirs if k not in mine]
    added = [k for k in mine if k not in theirs]
    changed = [k for k in mine if k in theirs and mine[k] != theirs[k]]
    if dropped or changed:
        rows.append((n, "diff", {"dropped": dropped, "changed": changed, "added": added}))

print("our data/tfc json files:", sum(1 for n in OURS.namelist() if n.startswith("data/tfc/") and n.endswith(".json")))
print("files with dropped keys or changed values:", len(rows))
print()
for n, kind, info in rows:
    print("=" * 88)
    print(n, "->", kind)
    if kind != "diff":
        continue
    for label in ("dropped", "changed"):
        items = info.get(label) or []
        if not items:
            continue
        print("  %s (%d):" % (label, len(items)))
        for k in items[:15]:
            print("     ", k[:110])
        if len(items) > 15:
            print("      ... and %d more" % (len(items) - 15))
