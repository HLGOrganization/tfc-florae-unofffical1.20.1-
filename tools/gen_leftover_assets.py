"""Generate the leftover assets found by tools/audit_register_assets.py.

Two groups of missing resources:

1. `rock/magma/tuff`: the texture itself exists (16x48, three frames, identical
   to boninite's), but the `.png.mcmeta` animation descriptor is missing, so the
   client treats the 3-frame strip as a single static texture and squashes it
   onto the block face.  Every other magma texture ships an `.mcmeta`; copying
   boninite's descriptor restores the animation.

2. `silty_loam`: the silty-loam flavour of the soil pile item and of the
   white-mangrove muddy roots has lang entries and registrations but no model,
   blockstate or texture at all.  The textures are derived as the per-pixel
   average of the existing silt and loam textures (silty loam is exactly the
   silt/loam mix), and the model/blockstate files mirror the loam ones.

Run with --force to regenerate files that already exist.
"""
from __future__ import annotations

import argparse
import json
import os

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae")
TEX = os.path.join(ASSETS, "textures")
MODELS = os.path.join(ASSETS, "models")
BLOCKSTATES = os.path.join(ASSETS, "blockstates")


def write_text(path, text, force):
    if os.path.isfile(path) and not force:
        print("  skip (exists) %s" % os.path.relpath(path, ASSETS))
        return False
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as fh:
        fh.write(text)
    print("  write %s" % os.path.relpath(path, ASSETS))
    return True


def average_texture(a, b, out, force):
    if os.path.isfile(out) and not force:
        print("  skip (exists) %s" % os.path.relpath(out, ASSETS))
        return False
    ia = Image.open(a).convert("RGBA")
    ib = Image.open(b).convert("RGBA")
    if ia.size != ib.size:
        raise SystemExit("texture size mismatch: %s %s vs %s %s" % (a, ia.size, b, ib.size))
    px = []
    for ya in range(ia.size[1]):
        for xa in range(ia.size[0]):
            ca = ia.getpixel((xa, ya))
            cb = ib.getpixel((xa, ya))
            px.append(tuple((ca[i] + cb[i]) // 2 for i in range(4)))
    dst = Image.new("RGBA", ia.size)
    dst.putdata(px)
    os.makedirs(os.path.dirname(out), exist_ok=True)
    dst.save(out)
    print("  write %s (avg of %s + %s)" % (
        os.path.relpath(out, ASSETS),
        os.path.basename(a), os.path.basename(b)))
    return True


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--force", action="store_true")
    args = ap.parse_args()
    force = args.force

    print("== 1. tuff magma animation metadata ==")
    src_mcmeta = os.path.join(TEX, "block", "rock", "magma", "boninite.png.mcmeta")
    dst_mcmeta = os.path.join(TEX, "block", "rock", "magma", "tuff.png.mcmeta")
    write_text(dst_mcmeta, open(src_mcmeta, encoding="utf-8").read(), force)

    print("== 2. silty_loam soil pile item ==")
    average_texture(
        os.path.join(TEX, "item", "soil_pile", "silt.png"),
        os.path.join(TEX, "item", "soil_pile", "loam.png"),
        os.path.join(TEX, "item", "soil_pile", "silty_loam.png"),
        force,
    )
    soil_pile_model = {
        "__comment__": "This file was automatically created by mcresources",
        "parent": "item/generated",
        "textures": {"layer0": "tfcflorae:item/soil_pile/silty_loam"},
    }
    write_text(
        os.path.join(MODELS, "item", "soil_pile", "silty_loam.json"),
        json.dumps(soil_pile_model, indent=2, ensure_ascii=False) + "\n",
        force,
    )

    print("== 3. white_mangrove muddy roots: silty_loam ==")
    average_texture(
        os.path.join(TEX, "block", "wood", "roots", "white_mangrove", "silt_side.png"),
        os.path.join(TEX, "block", "wood", "roots", "white_mangrove", "loam_side.png"),
        os.path.join(TEX, "block", "wood", "roots", "white_mangrove", "silty_loam_side.png"),
        force,
    )
    average_texture(
        os.path.join(TEX, "block", "wood", "roots", "white_mangrove", "silt_top.png"),
        os.path.join(TEX, "block", "wood", "roots", "white_mangrove", "loam_top.png"),
        os.path.join(TEX, "block", "wood", "roots", "white_mangrove", "silty_loam_top.png"),
        force,
    )

    roots_bs = {
        "__comment__": "This file was automatically created by mcresources",
        "variants": {"": {"model": "tfcflorae:block/wood/roots/white_mangrove/silty_loam"}},
    }
    write_text(
        os.path.join(BLOCKSTATES, "wood", "roots", "white_mangrove", "silty_loam.json"),
        json.dumps(roots_bs, indent=2, ensure_ascii=False) + "\n",
        force,
    )

    loam_model_path = os.path.join(MODELS, "block", "wood", "roots", "white_mangrove", "loam.json")
    loam_model = open(loam_model_path, encoding="utf-8").read()
    silty_model = loam_model.replace("/loam_side", "/silty_loam_side").replace("/loam_top", "/silty_loam_top")
    write_text(
        os.path.join(MODELS, "block", "wood", "roots", "white_mangrove", "silty_loam.json"),
        silty_model,
        force,
    )

    print("== 4. tuff item textures (loose rock + rock brick) ==")
    # The whole tuff rock texture set is currently a byte-for-byte copy of
    # boninite's (raw/cobble/bricks/...), so copying the two boninite item
    # sprites keeps tuff's items visually consistent with its blocks.
    for rel in (("item", "loose_rock"), ("item", "rock", "brick")):
        src = os.path.join(TEX, *rel, "boninite.png")
        dst = os.path.join(TEX, *rel, "tuff.png")
        if os.path.isfile(dst) and not force:
            print("  skip (exists) %s" % os.path.relpath(dst, ASSETS))
            continue
        if not os.path.isfile(src):
            raise SystemExit("missing reference texture: %s" % src)
        os.makedirs(os.path.dirname(dst), exist_ok=True)
        open(dst, "wb").write(open(src, "rb").read())
        print("  write %s (copy of boninite)" % os.path.relpath(dst, ASSETS))

    print("== 5. tuff groundcover models (boulder / pebble / rubble) ==")
    # These three shipped with the wrong texture key ("all") and pointed at
    # tfc:item/loose_rock/conglomerate, so tuff loose rocks rendered with the
    # conglomerate sprite.  Rebuilt from the boninite shape.
    for kind, parent in (("boulder", "boulder"), ("pebble", "pebble"), ("rubble", "rubble")):
        normal = {
            "__comment__": "This file was automatically created by mcresources",
            "parent": "tfcflorae:block/groundcover/%s" % parent,
            "textures": {
                "0": "tfcflorae:item/loose_rock/tuff",
                "particle": "tfcflorae:item/loose_rock/tuff",
            },
        }
        write_text(
            os.path.join(MODELS, "block", "rock", kind, "tuff.json"),
            json.dumps(normal, indent=2, ensure_ascii=False) + "\n",
            True,  # always overwrite: the existing file is known to be wrong
        )
        mossy = {
            "__comment__": "This file was automatically created by mcresources",
            "parent": "tfcflorae:block/groundcover/%s" % parent,
            "textures": {
                "0": "tfcflorae:block/rock/mossy_cobble/tuff",
                "particle": "tfcflorae:block/rock/mossy_cobble/tuff",
            },
        }
        write_text(
            os.path.join(MODELS, "block", "rock", kind, "tuff_mossy.json"),
            json.dumps(mossy, indent=2, ensure_ascii=False) + "\n",
            force,
        )

    print("== 6. tuff aqueduct models (base/east/north/south/west) ==")
    for part in ("base", "east", "north", "south", "west"):
        src = os.path.join(MODELS, "block", "rock", "aqueduct", "boninite", part + ".json")
        text = open(src, encoding="utf-8").read().replace("bricks/boninite", "bricks/tuff")
        write_text(
            os.path.join(MODELS, "block", "rock", "aqueduct", "tuff", part + ".json"),
            text,
            force,
        )


if __name__ == "__main__":
    main()
