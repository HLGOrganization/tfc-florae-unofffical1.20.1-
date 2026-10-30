"""Delete the leftover tuff assets.

Tuff is provided by TerraFirmaEarth (tfe) now; TFCFBlocks/TFCFItems already stopped
registering it.  What remained is the asset tree: blockstates, models, loot tables,
recipes, lang keys and tag entries.  Mossy tuff bricks are part of the same set.
"""
import glob
import io
import json
import os
import shutil

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "src", "main", "resources")

removed_files = 0
removed_dirs = 0

# ---- delete any file whose path contains tuff ----------------------------
for base in ("assets/tfcflorae", "data/tfcflorae", "data/tfc", "data/minecraft", "data/forge"):
    root = os.path.join(RES, base.replace("/", os.sep))
    if not os.path.isdir(root):
        continue
    for path in glob.glob(os.path.join(root, "**", "*tuff*"), recursive=True):
        if os.path.isdir(path):
            n = sum(len(fs) for _, _, fs in os.walk(path))
            shutil.rmtree(path)
            removed_dirs += 1
            removed_files += n
        elif os.path.isfile(path):
            os.remove(path)
            removed_files += 1
print("tuff files removed:", removed_files, "| dirs:", removed_dirs)

# ---- purge tuff entries from remaining json (tags, recipes lists) --------
touched = 0
for path in glob.glob(os.path.join(RES, "data", "**", "*.json"), recursive=True):
    try:
        text = io.open(path, encoding="utf-8-sig").read()
    except Exception:
        continue
    if "tuff" not in text:
        continue
    try:
        data = json.loads(text)
    except Exception:
        continue

    def scrub(node):
        changed = False
        if isinstance(node, dict):
            for key in ("values",):
                if isinstance(node.get(key), list):
                    before = len(node[key])
                    node[key] = [v for v in node[key] if "tuff" not in json.dumps(v)]
                    changed |= len(node[key]) != before
            for v in node.values():
                changed |= scrub(v)
        elif isinstance(node, list):
            for v in node:
                changed |= scrub(v)
        return changed

    if scrub(data):
        io.open(path, "w", encoding="utf-8", newline="\n").write(json.dumps(data, indent=2) + "\n")
        touched += 1
print("json files scrubbed of tuff entries:", touched)

# ---- lang ---------------------------------------------------------------
for name in ("en_us.json", "zh_cn.json"):
    p = os.path.join(RES, "assets", "tfcflorae", "lang", name)
    d = json.load(io.open(p, encoding="utf-8-sig"))
    drop = [k for k in d if "tuff" in k.lower()]
    for k in drop:
        del d[k]
    if drop:
        io.open(p, "w", encoding="utf-8", newline="\n").write(
            json.dumps(d, indent=2, ensure_ascii=False) + "\n")
    print(name, "removed", len(drop), "keys")

# ---- remaining ----------------------------------------------------------
left = []
for base in ("assets/tfcflorae", "data"):
    root = os.path.join(RES, base.replace("/", os.sep))
    for path in glob.glob(os.path.join(root, "**", "*tuff*"), recursive=True):
        left.append(os.path.relpath(path, RES))
print("remaining tuff paths:", left[:10] if left else "none")
