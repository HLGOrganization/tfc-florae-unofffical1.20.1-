"""Walk blockstate -> model -> texture for the restored blocks and report any break.

Only `tfcflorae:` references are resolved against our own resources; `tfc:` / `minecraft:`
ones are left to their own mod (they are checked for existence where the jar is available).
"""
import io
import json
import os
import zipfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
A = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae")

MODS = os.path.join(r"D:\线框的服务器\versions\TFC_Wild_Fire", "mods")
TFC_JARS = [os.path.join(MODS, f) for f in os.listdir(MODS)
            if f.lower().startswith("terrafirmacraft") and f.endswith(".jar")]
TFC = zipfile.ZipFile(TFC_JARS[0]) if TFC_JARS else None
VANILLA = zipfile.ZipFile(r"D:\线框的服务器\versions\1.20.1\1.20.1.jar")

IDS = ["rock/column/tuff", "rock/polished_column/tuff", "rock/smoke_block/tuff", "rock/magma/tuff"]
COLORS = ["black", "brown", "green", "pink", "red", "white", "yellow"]
for c in COLORS:
    IDS.append("sandstone/layered/" + c)
    for s in ("slab", "stairs", "wall"):
        IDS.append("sandstone/layered/%s/%s" % (s, c))


def model_path(ref):
    if ":" not in ref:
        ref = "minecraft:" + ref
    ns, path = ref.split(":", 1)
    return ns, "assets/%s/models/%s.json" % (ns, path)


def tex_path(ref):
    if ":" not in ref:
        ref = "minecraft:" + ref
    ns, path = ref.split(":", 1)
    return ns, "assets/%s/textures/%s.png" % (ns, path)


def exists(ns, entry):
    if ns == "tfcflorae":
        p = os.path.join(ROOT, "src", "main", "resources", entry)
        return os.path.isfile(p)
    if ns == "minecraft":
        return entry in VANILLA.namelist()
    if ns == "tfc" and TFC:
        return entry in TFC.namelist()
    return True                      # other mods: assume present


problems = []
for ident in IDS:
    bs = os.path.join(A, "blockstates", ident.replace("/", os.sep) + ".json")
    if not os.path.isfile(bs):
        problems.append((ident, "blockstate missing"))
        continue
    d = json.load(io.open(bs, encoding="utf-8-sig"))
    models = set()
    for v in d.get("variants", {}).values():
        for e in (v if isinstance(v, list) else [v]):
            if isinstance(e, dict) and "model" in e:
                models.add(e["model"])
    for m in models:
        ns, entry = model_path(m)
        if not exists(ns, entry):
            problems.append((ident, "model missing: " + m))
            continue
        if ns != "tfcflorae":
            continue
        md = json.load(io.open(os.path.join(ROOT, "src", "main", "resources", entry), encoding="utf-8-sig"))
        for t in md.get("textures", {}).values():
            if not isinstance(t, str) or ":" not in t:
                continue
            tns, tentry = tex_path(t)
            if not exists(tns, tentry):
                problems.append((ident, "texture missing: " + t))
        par = md.get("parent")
        depth = 0
        while par and depth < 6:
            pns, pentry = model_path(par)
            if pns == "tfcflorae":
                pp = os.path.join(ROOT, "src", "main", "resources", pentry)
                if not os.path.isfile(pp):
                    problems.append((ident, "parent missing: " + par))
                    break
                par = json.load(io.open(pp, encoding="utf-8-sig")).get("parent")
            elif pns == "minecraft":
                if not exists("minecraft", pentry):
                    problems.append((ident, "parent missing: " + par))
                break
            elif pns == "tfc":
                if not exists("tfc", pentry):
                    problems.append((ident, "parent missing: " + par))
                break
            else:
                break
            depth += 1
    # item model
    im = os.path.join(A, "models", "item", ident.replace("/", os.sep) + ".json")
    if not os.path.isfile(im):
        problems.append((ident, "item model missing"))
    else:
        idm = json.load(io.open(im, encoding="utf-8-sig"))
        par = idm.get("parent")
        if par:
            pns, pentry = model_path(par)
            if pns == "tfcflorae" and not os.path.isfile(os.path.join(ROOT, "src", "main", "resources", pentry)):
                problems.append((ident, "item parent missing: " + par))

print("checked ids:", len(IDS))
print("problems:", len(problems))
for p in problems[:30]:
    print("   %-42s %s" % p)
