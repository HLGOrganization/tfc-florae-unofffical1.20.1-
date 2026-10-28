"""Before/after contact sheet for the tuff palette swap (column-aligned)."""
import io
import os
import subprocess

from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TEX = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae", "textures")
OUT = os.path.join(ROOT, "reports", "tuff-palette-preview.png")
ATT = r"C:\Users\hsy66\.dsh\attachments\v1\objects\c6\c6ca9c6ea6e8b9dec8a6b17c8ebbf4802494ba8f2816873ea795377e2eca3fc5"

SAMPLES = [
    ("raw", "block/rock/raw/tuff.png"),
    ("cobble", "block/rock/cobble/tuff.png"),
    ("bricks", "block/rock/bricks/tuff.png"),
    ("gravel", "block/rock/gravel/tuff.png"),
    ("magma", "block/rock/magma/tuff.png"),
    ("sand+rock", "block/sand/rocky/black/tuff.png"),
    ("dirt+rock", "block/rockier_compact_dirt/loam/tuff.png"),
    ("mossy", "block/rock/mossy_cobble/tuff.png"),
]

SCALE = 9
LABEL = 13
PAD = 4


def original(rel):
    data = subprocess.run(
        ["git", "show", "HEAD:src/main/resources/assets/tfcflorae/textures/" + rel],
        cwd=ROOT, capture_output=True,
    ).stdout
    if not data:
        return None
    return Image.open(io.BytesIO(data)).convert("RGBA").copy()


def first_frame(im):
    w, h = im.size
    if h > w and h % w == 0:
        return im.crop((0, 0, w, w))
    return im


def main():
    cols = [("user ref", Image.open(ATT).convert("RGBA"))]
    for label, rel in SAMPLES:
        cols.append((label, Image.open(os.path.join(TEX, rel)).convert("RGBA")))

    cell = 16 * SCALE
    width = len(cols) * (cell + PAD) + PAD
    height = 2 * (cell + LABEL + PAD) + PAD
    sheet = Image.new("RGBA", (width, height), (24, 24, 28, 255))
    d = ImageDraw.Draw(sheet)

    for row, (title, use_before) in enumerate((("AFTER (palette applied)", False), ("BEFORE (original)", True))):
        y = PAD + row * (cell + LABEL + PAD)
        d.text((PAD, y), title, fill=(240, 240, 240, 255))
        for i, (label, cur) in enumerate(cols):
            x = PAD + i * (cell + PAD)
            img = cur if (row == 0 or i == 0) else (original(SAMPLES[i - 1][1]) or cur)
            img = first_frame(img)
            d.text((x, y + LABEL), label[:12], fill=(200, 200, 200, 255))
            sheet.paste(img.resize((cell, cell), Image.NEAREST), (x, y + LABEL + 11))

    sheet.save(OUT)
    print("wrote", OUT, sheet.size)


if __name__ == "__main__":
    main()
