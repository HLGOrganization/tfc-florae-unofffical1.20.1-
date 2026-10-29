"""Find orphaned assets: files nothing in the mod points at any more.

Reference graph inside assets/tfcflorae:
  blockstate  -> model            ("model": "ns:path")
  model       -> parent model      ("parent": "ns:path")
  model       -> textures          ("textures": {"x": "ns:path"})
Items reference models/item/<id>, blocks reference blockstates/<id>.

Orphan rules (conservative - only deletes what nothing can reach):
  * blockstates/<id>.json   -> orphan when <id> is not a registered block
  * models/item/<id>.json   -> orphan when <id> is not a registered item and no
                               blockstate/model references it
  * models/block/<id>.json  -> orphan when no blockstate references it and no other
                               model uses it as parent
  * textures/<id>.png       -> orphan when no model references it

Run with --apply to delete.
"""
from __future__ import annotations

import argparse
import io
import json
import os
import re
import zipfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "src", "main", "resources")
ASSETS = os.path.join(RES, "assets", "tfcflorae")
FULL = os.path.join(ROOT, "reports", "registered-ids-full.json")
SUSPECTS = os.path.join(ROOT, "reports", "suspect-unregistered.json")

REF = re.compile(r'"(?:model|parent)"\s*:\s*"([^"]+)"')
TEX = re.compile(r'"[a-z_]+"\s*:\s*"([^"]+)"')


def rel(p):
    return os.path.relpath(p, ASSETS).replace(os.sep, "/")


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--apply", action="store_true")
    args = ap.parse_args()

    wl = json.load(io.open(FULL, encoding="utf-8-sig"))
    known_blocks = set(wl["blocks"])
    known_items = set(wl["items"])
    try:
        suspects = set(json.load(io.open(SUSPECTS, encoding="utf-8-sig")))
    except Exception:
        suspects = set()
    registered_blocks = known_blocks - suspects
    registered_items = known_items - suspects

    # ---- collect references -------------------------------------------------
    model_refs = set()      # every model path referenced by blockstates/models
    texture_refs = set()    # every texture path referenced by models

    def scan_model_values(path):
        txt = io.open(path, encoding="utf-8-sig").read()
        for m in REF.finditer(txt):
            target = m.group(1)
            if target.startswith("tfcflorae:"):
                model_refs.add(target.split(":", 1)[1])
        try:
            d = json.loads(txt)
        except Exception:
            return
        for v in (d.get("textures") or {}).values():
            if isinstance(v, str) and v.startswith("tfcflorae:"):
                texture_refs.add(v.split(":", 1)[1])

    blockstates = {}
    for dirpath, _d, files in os.walk(os.path.join(ASSETS, "blockstates")):
        for fn in files:
            if fn.endswith(".json"):
                p = os.path.join(dirpath, fn)
                blockstates[rel(p)[len("blockstates/"):-len(".json")]] = p
    models = {}
    for sub in ("block", "item"):
        for dirpath, _d, files in os.walk(os.path.join(ASSETS, "models", sub)):
            for fn in files:
                if fn.endswith(".json"):
                    p = os.path.join(dirpath, fn)
                    models[rel(p)[len("models/"):-len(".json")]] = p

    for p in blockstates.values():
        scan_model_values(p)
    for p in models.values():
        scan_model_values(p)

    def texture_path(tex):
        return "textures/%s.png" % tex

    textures = {}
    for dirpath, _d, files in os.walk(os.path.join(ASSETS, "textures")):
        for fn in files:
            if fn.endswith(".png"):
                p = os.path.join(dirpath, fn)
                textures[rel(p)[len("textures/"):-len(".png")]] = p

    # ---- decide orphans -----------------------------------------------------
    orphan_bstates = [k for k in blockstates if k not in registered_blocks]
    orphan_models = []
    for k, p in models.items():
        if k.startswith("item/"):
            ident = k[len("item/"):]
            if ident in registered_items:
                continue
            if k in model_refs:
                continue
            orphan_models.append(k)
        else:
            if k in model_refs:
                continue
            orphan_models.append(k)
    orphan_textures = [k for k in textures if k not in texture_refs]

    print("registered blocks:", len(registered_blocks), " items:", len(registered_items))
    print("blockstates:", len(blockstates), " orphan:", len(orphan_bstates))
    print("models:", len(models), " orphan:", len(orphan_models))
    print("textures:", len(textures), " orphan:", len(orphan_textures))
    for label, items in (("blockstate", orphan_bstates[:8]), ("model", orphan_models[:8]), ("texture", orphan_textures[:8])):
        for i in items:
            print("   %-10s %s" % (label, i))

    if not args.apply:
        print("\n(dry run - pass --apply to delete)")
        return

    for k in orphan_bstates:
        os.remove(blockstates[k])
    for k in orphan_models:
        os.remove(models[k])
    for k in orphan_textures:
        os.remove(textures[k])
    print("\ndeleted: %d blockstates, %d models, %d textures" % (
        len(orphan_bstates), len(orphan_models), len(orphan_textures)))

    # prune empty dirs
    n = 0
    for dirpath, _d, _f in os.walk(ASSETS, topdown=False):
        if not os.listdir(dirpath):
            os.rmdir(dirpath)
            n += 1
    print("empty dirs removed:", n)


if __name__ == "__main__":
    main()
