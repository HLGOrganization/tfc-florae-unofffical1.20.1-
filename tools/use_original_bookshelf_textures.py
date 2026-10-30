"""Use the original mod's chiseled bookshelf textures instead of generating any.

The 1.18.2 pack ships a complete chiseled bookshelf set that was overlooked:

  textures/block/wood/chiseled_bookshelf/0..6/<wood>.png   7 book-count stages
  textures/block/wood/chiseled_bookshelf/back/<wood>.png   back plate
  textures/block/wood/chiseled_bookshelf/side/<wood>.png   side
  textures/block/wood/chiseled_bookshelf/top/<wood>.png    top

Mapping onto the 1.20 model set:

  <wood>_bookshelf_empty      <- chiseled_bookshelf/0   (zero books = empty slot)
  <wood>_bookshelf_occupied   <- chiseled_bookshelf/6   (full slot)
  <wood>_bookshelf_side       <- chiseled_bookshelf/side
  <wood>_bookshelf_top        <- chiseled_bookshelf/top
  <wood>_bookshelf_stage0..6  <- chiseled_bookshelf/0..6

Nothing is recoloured or synthesised here.
"""
import io
import os
import shutil

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TEX = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae",
                   "textures", "block", "wood", "planks")
SRC = os.path.join(r"D:\学习资料\mod解包\TFCFlorae-1.20.1-2.2.0-mc1.20.1",
                   "assets", "tfcflorae", "textures", "block", "wood", "chiseled_bookshelf")

if not os.path.isdir(SRC):
    raise SystemExit("source pack not found: " + SRC)

woods = sorted(f[:-len("_bookshelf_side.png")] for f in os.listdir(TEX)
               if f.endswith("_bookshelf_side.png"))
print("woods:", len(woods))

MAPPING = {
    "empty": "0",
    "occupied": "6",
    "side": "side",
    "top": "top",
}
for i in range(7):
    MAPPING["stage%d" % i] = str(i)

copied = {}
skipped = []
for w in woods:
    for dst_suffix, src_dir in MAPPING.items():
        src = os.path.join(SRC, src_dir, w + ".png")
        if not os.path.isfile(src):
            skipped.append((w, dst_suffix))
            continue
        dst = os.path.join(TEX, "%s_bookshelf_%s.png" % (w, dst_suffix))
        shutil.copy2(src, dst)
        copied[dst_suffix] = copied.get(dst_suffix, 0) + 1

print("copied:", copied)
print("skipped:", len(skipped), skipped[:8])

# sanity: every referenced suffix now comes from the original pack
need = ["empty", "occupied", "side", "top"] + ["stage%d" % i for i in range(7)]
missing = [(w, s) for w in woods for s in need
           if not os.path.isfile(os.path.join(TEX, "%s_bookshelf_%s.png" % (w, s)))]
print("still missing:", len(missing))
