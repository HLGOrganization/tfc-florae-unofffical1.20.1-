import io
import json
import os
import re
import zipfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "src", "main", "resources")
DATA = os.path.join(RES, "data")
TFC_JAR = os.path.join(os.path.dirname(ROOT), "[群峦传说] TerraFirmaCraft-Forge-1.20.1-3.2.20.jar")

have = set()


def add(ns, kind, rel):
    have.add((ns, kind, rel))


for dirpath, _d, files in os.walk(DATA):
    for fn in files:
        if not fn.endswith(".json"):
            continue
        norm = os.path.join(dirpath, fn).replace(os.sep, "/")
        if "/tags/" not in norm:
            continue
        ns = os.path.basename(norm.split("/tags/")[0])
        rest = norm.split("/tags/", 1)[1]
        kind = rest.split("/")[0]
        add(ns, kind, "/".join(rest.split("/")[1:])[:-5])

if os.path.isfile(TFC_JAR):
    with zipfile.ZipFile(TFC_JAR) as z:
        for n in z.namelist():
            if n.startswith("data/") and "/tags/" in n and n.endswith(".json"):
                rest = n[len("data/"):]
                ns = rest.split("/")[0]
                tail = rest.split("/tags/", 1)[1]
                kind = tail.split("/")[0]
                add(ns, kind, "/".join(tail.split("/")[1:])[:-5])

print("known tag files:", len(have))

pat = re.compile(r'"tag"\s*:\s*"([^"]+)"')
unresolved = {}
for dirpath, _d, files in os.walk(DATA):
    for fn in files:
        if not fn.endswith(".json"):
            continue
        full = os.path.join(dirpath, fn)
        norm = full.replace(os.sep, "/")
        if "/tags/" in norm:
            continue
        try:
            txt = io.open(full, encoding="utf-8-sig").read()
        except Exception:
            continue
        for m in pat.finditer(txt):
            ident = m.group(1)
            if ":" not in ident:
                ns, path = "minecraft", ident
            else:
                ns, path = ident.split(":", 1)
            if (ns, "items", path) in have or (ns, "blocks", path) in have:
                continue
            unresolved.setdefault(ident, []).append(os.path.relpath(full, DATA))

print("unresolved \"tag\" references:", len(unresolved))
for ident, files in sorted(unresolved.items(), key=lambda kv: -len(kv[1]))[:30]:
    print("   %-44s %3d files   e.g. %s" % (ident, len(files), files[0]))
