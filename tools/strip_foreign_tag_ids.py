"""Strip foreign ids out of the tag files we ship.

Rationale: our files under data/{tfc,minecraft,forge}/tags are *additions*
(replace:false - Forge merges them with whatever TFC/vanilla/modpack already
define).  Carrying copies of TFC's own ids adds nothing and is a liability: if TFC
renames or removes one of them, the whole tag dies - exactly the failure mode that
once wiped every leaf block.

`minecraft:slabs` shipped 216 TFC slab ids this way.  This removes every concrete
id that is not ours, plus #references (the origin file already has them), and
drops files that end up empty.
"""
import io
import json
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DATA = os.path.join(ROOT, "src", "main", "resources", "data")

changed = []
removed_files = []
total_stripped = 0

for ns in ("tfc", "minecraft", "forge"):
    base = os.path.join(DATA, ns, "tags")
    if not os.path.isdir(base):
        continue
    for dirpath, _d, files in os.walk(base):
        for fn in files:
            if not fn.endswith(".json"):
                continue
            full = os.path.join(dirpath, fn)
            try:
                d = json.load(io.open(full, encoding="utf-8-sig"))
            except Exception:
                continue
            vals = d.get("values", [])
            keep, drop = [], []
            for v in vals:
                ident = v.get("id") if isinstance(v, dict) else v
                if not isinstance(ident, str):
                    keep.append(v)
                    continue
                if ident.startswith("tfcflorae:"):
                    keep.append(v)
                else:
                    drop.append(ident)
            if not drop:
                continue
            total_stripped += len(drop)
            if not keep:
                removed_files.append(os.path.relpath(full, DATA))
                os.remove(full)
                continue
            d["values"] = keep
            io.open(full, "w", encoding="utf-8", newline="\n").write(
                json.dumps(d, indent=2, ensure_ascii=False) + "\n")
            changed.append((os.path.relpath(full, DATA), len(drop)))

print("ids stripped:", total_stripped)
print("files rewritten:", len(changed))
for rel, n in changed[:15]:
    print("   %-70s -%d" % (rel, n))
print("files removed (nothing of ours left):", len(removed_files))
for rel in removed_files[:15]:
    print("   ", rel)
