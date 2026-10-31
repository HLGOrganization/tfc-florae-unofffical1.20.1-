"""Append our rocks to the boulder / baby_boulder / soil_disc features.

These three feature types list their rocks explicitly in `config.states`, so our rocks are
appended as new entries without touching TFC/TFE's own ones:

  tfc:boulder / tfc:baby_boulder
      {"rock": <raw>, "blocks": [<block>, <block>]}
      cobble_boulder*  -> [raw, cobble]
      mossy_boulder*   -> [cobble, mossy_cobble]

  tfc:soil_disc   (the ore panning deposits)
      {"replace": <gravel>, "with": <deposit>}

These features cover every rock, not a subset, so all nine of ours are appended.
Categories do not apply here.
"""
import json
import os
import zipfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "src", "main", "resources")
TFE_JAR = os.path.join(r"D:\线框的服务器\versions\TFC_Wild_Fire\mods",
                       "Wildfire_TerraFirmaEarth-1.20.1-forge-2.3.0.jar")

OUR_ROCKS = ["red_granite", "porphyry", "boninite", "carbonatite",
             "laterite", "mudstone", "jaspillite", "wackestone", "soapstone"]

tfe = zipfile.ZipFile(TFE_JAR)

BLOCK_PAIRS = {
    "cobble": ("rock/raw", "rock/cobble"),
    "mossy": ("rock/cobble", "rock/mossy_cobble"),
}

targets = []
for n in tfe.namelist():
    if "worldgen/configured_feature/" not in n or not n.endswith(".json"):
        continue
    try:
        d = json.loads(tfe.read(n).decode("utf-8"))
    except Exception:
        continue
    t = d.get("type", "")
    states = d.get("config", {}).get("states")
    if t in ("tfc:boulder", "tfc:baby_boulder") and isinstance(states, list):
        targets.append((n, d, "boulder"))
    elif t == "tfc:soil_disc" and isinstance(states, list):
        targets.append((n, d, "disc"))

print("candidate features:", len(targets))

touched = 0
appended = 0
for name, data, kind in targets:
    states = data["config"]["states"]
    existing = set()
    for s in states:
        if isinstance(s, dict):
            r = s.get("rock") or s.get("replace")
            if isinstance(r, str):
                existing.add(r.rsplit("/", 1)[-1])
    names = [os.path.basename(name)]
    mossy = "mossy" in name
    added = 0
    if kind == "boulder":
        a, b = BLOCK_PAIRS["mossy" if mossy else "cobble"]
        for rock in OUR_ROCKS:
            if rock in existing:
                continue
            states.append({
                "rock": "tfcflorae:%s/%s" % (a, rock),
                "blocks": ["tfcflorae:%s/%s" % (a, rock), "tfcflorae:%s/%s" % (b, rock)],
            })
            added += 1
    else:
        # soil_disc: derive the deposit name from an existing entry
        deposit = None
        for s in states:
            if isinstance(s, dict):
                w = s.get("with")
                if isinstance(w, str) and w.startswith("tfc:deposit/"):
                    deposit = w.split("/")[1]
                    break
        if not deposit:
            print("   skip (no deposit template):", names[0])
            continue
        for rock in OUR_ROCKS:
            if rock in existing:
                continue
            states.append({
                "replace": "tfcflorae:rock/gravel/%s" % rock,
                "with": "tfcflorae:deposit/%s/%s" % (deposit, rock),
            })
            added += 1
    if added:
        rel = name
        p = os.path.join(RES, rel.replace("/", os.sep))
        os.makedirs(os.path.dirname(p), exist_ok=True)
        with open(p, "w", encoding="utf-8", newline="\n") as fh:
            fh.write(json.dumps(data, indent=2) + "\n")
        touched += 1
        appended += added
        print("   %-42s +%d" % (names[0], added))

print()
print("features written: %d | rock entries appended: %d" % (touched, appended))
