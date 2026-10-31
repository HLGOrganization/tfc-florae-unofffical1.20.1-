"""Structural diff focused on worldgen, with set-based comparison for tag `values`.

Only data/tfc/worldgen/** and data/tfc/tags/worldgen/** matter for vegetation.
Tag `values` are compared as sets so that adding entries does not show up as a change.
Scalar fields are compared by value.
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


def scalars(o, pre="", out=None):
    """Collect every scalar leaf except list elements (lists compared as sets elsewhere)."""
    if out is None:
        out = {}
    if isinstance(o, dict):
        for k, v in o.items():
            if k == "values" and isinstance(v, list):
                out[pre + "/values"] = frozenset(json.dumps(x, sort_keys=True) for x in v)
            else:
                scalars(v, pre + "/" + str(k), out)
    elif isinstance(o, list):
        for i, v in enumerate(o):
            scalars(v, pre + "[%d]" % i, out)
    else:
        out[pre] = o
    return out


targets = [n for n in OURS.namelist()
           if n.startswith("data/tfc/") and n.endswith(".json")
           and ("/worldgen/" in n or "/tags/worldgen/" in n)]
print("our worldgen-scope files under data/tfc:", len(targets))

report = []
for n in sorted(targets):
    if n not in TFE.namelist() and n not in TFC.namelist():
        report.append((n, "NO ORIGINAL", [], [], []))
        continue
    src = TFE if n in TFE.namelist() else TFC
    mine = scalars(load(OURS, n) or {})
    theirs = scalars(load(src, n) or {})

    dropped = [k for k in theirs if k not in mine]
    added = [k for k in mine if k not in theirs]
    changed = []
    removed_from_set = []
    for k in mine:
        if k not in theirs:
            continue
        if isinstance(mine[k], frozenset) and isinstance(theirs[k], frozenset):
            lost = theirs[k] - mine[k]
            if lost:
                removed_from_set.append((k, sorted(lost)[:6], len(lost)))
        elif mine[k] != theirs[k]:
            changed.append((k, theirs[k], mine[k]))

    if dropped or changed or removed_from_set:
        report.append((n, "DIFF", dropped, changed, removed_from_set))

print("files where we LOSE or CHANGE something relative to the original:", len(report))
print()
for n, kind, dropped, changed, removed in report:
    print("=" * 88)
    print(n)
    if kind == "NO ORIGINAL":
        print("   (we ship it, neither TFC nor TFE has it)")
        continue
    if dropped:
        print("   dropped keys (%d): %s" % (len(dropped), [k[:70] for k in dropped[:6]]))
    if changed:
        print("   changed values (%d):" % len(changed))
        for k, a, b in changed[:6]:
            print("      %-52s original=%s  ours=%s" % (k[:52], str(a)[:40], str(b)[:40]))
    if removed:
        print("   tag entries we dropped (%d lists):" % len(removed))
        for k, sample, cnt in removed[:4]:
            print("      %-40s lost %d e.g. %s" % (k[:40], cnt, sample))
