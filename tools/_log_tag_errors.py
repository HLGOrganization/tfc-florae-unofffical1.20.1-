import io
import re
import zipfile

z = zipfile.ZipFile(r"D:\学习资料\错误报告-2026-10-29_13.42.57.zip")
raw = z.read("latest.log").decode("utf-8", errors="replace")
lines = raw.splitlines()
print("log lines:", len(lines))

pat = re.compile(r"Couldn't load tag (\S+) as it is missing following references: (.*)")
rows = []
for l in lines:
    m = pat.search(l)
    if m:
        tag = m.group(1)
        refs = [r.strip().split(" (from")[0] for r in m.group(2).split(",")]
        rows.append((tag, refs))

print("failed tags:", len(rows))
mine = [r for r in rows if any("tfcflorae" in x for x in r[1])]
print("failed tags mentioning tfcflorae:", len(mine))
for tag, refs in mine[:25]:
    ours = [x for x in refs if "tfcflorae" in x]
    print("  %-52s missing: %s" % (tag, ours[:4]))

print()
print("== tags we care about ==")
for tag, refs in rows:
    if tag in ("tfcflorae:all_bamboo_logs", "minecraft:slabs") or tag.endswith("all_bamboo_logs"):
        print("  %-40s %s" % (tag, refs[:10]))

# also look for other tag problems
print()
for kw in ("all_bamboo_logs", "slabs"):
    hits = [l for l in lines if kw in l and ("ERROR" in l or "WARN" in l or "Couldn't" in l)]
    print("%s lines: %d" % (kw, len(hits)))
    for h in hits[:6]:
        print("   ", h[:220])
