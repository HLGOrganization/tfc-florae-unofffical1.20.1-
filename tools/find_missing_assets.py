"""Find textures/models that are referenced but missing (would render as the pink/
black checkerboard).

Walks every model JSON in the mod and checks:
  * "parent"  -> model file exists
  * "textures" values with tfcflorae: namespace -> png exists
Also checks blockstates' model references.
"""
import io
import json
import os
import re
from collections import defaultdict

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
A = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae")

def path_of(kind, ref):
    """ref like 'tfcflorae:block/x/y' -> filesystem path"""
    if ":" not in ref:
        return None
    ns, p = ref.split(":", 1)
    if ns != "tfcflorae":
        return None
    if kind == "model":
        return os.path.join(A, "models", p.replace("/", os.sep) + ".json")
    return os.path.join(A, "textures", p.replace("/", os.sep) + ".png")

missing_models = defaultdict(list)   # referenced model -> who references it
missing_textures = defaultdict(list)

TEX = re.compile(r'"(?:textures)"\s*:\s*\{([^}]*)\}')
VAL = re.compile(r'"([^"]+)"\s*:\s*"([^"]+)"')
PARENT = re.compile(r'"parent"\s*:\s*"([^"]+)"')

for dirpath, _d, files in os.walk(os.path.join(A, "models")):
    for fn in files:
        if not fn.endswith(".json"):
            continue
        full = os.path.join(dirpath, fn)
        rel = os.path.relpath(full, os.path.join(A, "models")).replace(os.sep, "/")[:-5]
        txt = io.open(full, encoding="utf-8-sig").read()
        for m in PARENT.finditer(txt):
            ref = m.group(1)
            p = path_of("model", ref)
            if p is not None and not os.path.isfile(p):
                missing_models[ref].append(rel)
        for block in TEX.finditer(txt):
            for vm in VAL.finditer(block.group(1)):
                ref = vm.group(2)
                p = path_of("tex", ref)
                if p is not None and not os.path.isfile(p):
                    missing_textures[ref].append(rel)

for dirpath, _d, files in os.walk(os.path.join(A, "blockstates")):
    for fn in files:
        if not fn.endswith(".json"):
            continue
        full = os.path.join(dirpath, fn)
        rel = os.path.relpath(full, os.path.join(A, "blockstates")).replace(os.sep, "/")
        txt = io.open(full, encoding="utf-8-sig").read()
        for m in re.finditer(r'"model"\s*:\s*"([^"]+)"', txt):
            ref = m.group(1)
            p = path_of("model", ref)
            if p is not None and not os.path.isfile(p):
                missing_models[ref].append("blockstate:" + rel)

print("missing model files:", len(missing_models))
for ref, users in sorted(missing_models.items(), key=lambda kv: -len(kv[1]))[:15]:
    print("   %-62s used by %d (%s)" % (ref, len(users), users[0]))

print("\nmissing textures:", len(missing_textures))
for ref, users in sorted(missing_textures.items(), key=lambda kv: -len(kv[1]))[:25]:
    print("   %-62s used by %d (%s)" % (ref, len(users), users[0]))
