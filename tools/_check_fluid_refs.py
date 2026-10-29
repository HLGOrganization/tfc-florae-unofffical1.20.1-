import io
import os

DATA = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "src", "main", "resources", "data")
NAMES = ("anthracite", "bituminous_coal", "charcoal", "coke", "lignite")

left = []
for dp, _d, fs in os.walk(DATA):
    for f in fs:
        if not f.endswith(".json"):
            continue
        p = os.path.join(dp, f)
        try:
            txt = io.open(p, encoding="utf-8-sig").read()
        except Exception:
            continue
        for n in NAMES:
            token = "tfcflorae:ore/" + n
            if token in txt and ("fluid" in txt or "result_fluid" in txt):
                left.append((os.path.relpath(p, DATA).replace(os.sep, "/"), n))
                break

print("files still mentioning removed fluid ids:", len(left))
for rel, n in left[:20]:
    print("   %-64s %s" % (rel, n))
