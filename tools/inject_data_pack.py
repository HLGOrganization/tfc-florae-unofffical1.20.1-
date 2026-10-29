"""Inject the working-tree tag files straight into the deployed mod jar.

Pure data-pack changes (tags / recipes / loot tables) do not need a gradle build:
a mod jar is just a zip, and Forge reads data files from it.  This rewrites the
jar's data-pack section from `src/main/resources/data`, so the change is live
after the next game restart - seconds instead of a 4 minute build.

Usage:
    python tools/inject_data_pack.py --jar <path-to-deployed.jar> --backup
"""
from __future__ import annotations

import argparse
import os
import shutil
import time
import zipfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DATA = os.path.join(ROOT, "src", "main", "resources", "data")

# everything under data/ is data pack content (tags, recipes, loot tables, worldgen,
# advancements, structures...).  The jar's data section is built from this same tree,
# so replacing it wholesale is safe and keeps recipes in sync with the tags.
PACK_DIRS = None  # None = replace the entire data/ section


def collect():
    """relative arcname -> absolute path, for every file we want in the jar"""
    out = {}
    for dirpath, _d, files in os.walk(DATA):
        for fn in files:
            full = os.path.join(dirpath, fn)
            rel = os.path.relpath(full, os.path.join(ROOT, "src", "main", "resources")).replace(os.sep, "/")
            parts = rel.split("/")
            if len(parts) >= 3 and parts[0] == "data":
                if PACK_DIRS is None or any(parts[2].startswith(p) for p in PACK_DIRS):
                    out[rel] = full
    return out


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--jar", required=True)
    ap.add_argument("--backup", action="store_true")
    args = ap.parse_args()

    jar = args.jar
    if not os.path.isfile(jar):
        raise SystemExit("jar not found: %s" % jar)

    files = collect()
    print("data-pack files to inject:", len(files))
    if args.backup:
        bak = "%s.bak-%s" % (jar, time.strftime("%Y%m%d-%H%M%S"))
        shutil.copy2(jar, bak)
        print("backup:", bak)

    tmp = jar + ".injecting"
    kept = 0
    dropped = 0
    with zipfile.ZipFile(jar) as zin, zipfile.ZipFile(tmp, "w", zipfile.ZIP_DEFLATED) as zout:
        for item in zin.infolist():
            name = item.filename
            parts = name.split("/")
            is_pack = len(parts) >= 2 and parts[0] == "data" and (
                PACK_DIRS is None or (len(parts) >= 3 and any(parts[2].startswith(p) for p in PACK_DIRS)))
            if is_pack:
                dropped += 1
                continue  # replaced below
            zout.writestr(item, zin.read(name))
            kept += 1
        for rel, full in sorted(files.items()):
            zout.write(full, rel)
    shutil.move(tmp, jar)
    print("kept %d entries, replaced %d pack files with %d" % (kept, dropped, len(files)))
    print("done:", jar, os.path.getsize(jar), "bytes")


if __name__ == "__main__":
    main()
