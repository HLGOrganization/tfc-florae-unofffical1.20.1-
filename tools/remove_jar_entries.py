"""Remove entries from a jar that patch_jar.py cannot delete.

patch_jar.py only adds/overwrites.  The previous vein pass wrote overrides for dike and
gravel veins that are structurally invalid (`"weight": null`, and ore ids derived from rock
blocks).  Those files are still inside the jar and make the data pack fail to load, so they
have to be dropped.

Rewrites the whole archive the same safe way patch_jar does: sequential write, sizes and CRC
backfilled in the local header, flag bit 3 never set, so Java's ZipInputStream accepts it.

Usage:
    python remove_jar_entries.py --jar <jar> --prefix data/tfc/worldgen/configured_feature/vein/ \
        --keep-from src/main/resources
"""
import argparse
import os
import shutil
import sys
import time
import zipfile


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--jar", required=True)
    ap.add_argument("--prefix", required=True, help="only entries under this prefix are candidates")
    ap.add_argument("--keep-from", required=True, help="source tree; entries present here are kept")
    ap.add_argument("--dry-run", action="store_true")
    args = ap.parse_args()

    if not os.path.exists(args.jar):
        sys.exit("jar not found: " + args.jar)
    if not os.path.isdir(args.keep_from):
        sys.exit("source tree not found: " + args.keep_from)

    keep_rel = set()
    for dirpath, _d, files in os.walk(args.keep_from):
        for fn in files:
            rel = os.path.relpath(os.path.join(dirpath, fn), args.keep_from).replace("\\", "/")
            keep_rel.add(args.prefix + rel)

    zin = zipfile.ZipFile(args.jar)
    names = zin.namelist()
    drop = [n for n in names if n.startswith(args.prefix) and n not in keep_rel]

    print("jar entries :", len(names))
    print("candidates  :", sum(1 for n in names if n.startswith(args.prefix)))
    print("to remove   :", len(drop))
    for d in drop[:20]:
        print("   -", d)

    if args.dry_run or not drop:
        zin.close()
        return

    tmp = args.jar + ".tmp"
    t0 = time.time()
    dropped = set(drop)
    with zipfile.ZipFile(tmp, "w", zipfile.ZIP_DEFLATED, compresslevel=6) as zout:
        for item in zin.infolist():
            if item.filename in dropped:
                continue
            data = zin.read(item.filename)
            zout.writestr(item.filename, data)
    zin.close()

    # verify before replacing
    chk = zipfile.ZipFile(tmp)
    if chk.testzip() is not None:
        os.remove(tmp)
        sys.exit("new archive failed integrity check, jar left untouched")
    chk.close()

    shutil.move(tmp, args.jar)
    print("removed %d entries in %.1f s" % (len(drop), time.time() - t0))
    print("jar entries now:", len(zipfile.ZipFile(args.jar).namelist()))


if __name__ == "__main__":
    main()
