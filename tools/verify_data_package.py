"""Verify every packaged data file matches source and no audit harness is shipped."""
import hashlib
import json
import zipfile
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RESOURCES = ROOT / "src/main/resources"
JAR = ROOT / "build/libs/TFCFlorae-1.20.1-2.2.0-mc1.20.1.jar"
TFC_JAR = ROOT.parent / "[群峦传说] TerraFirmaCraft-Forge-1.20.1-3.2.20.jar"
IGNORED = {".bak", ".bbmodel", ".orig", ".tmp"}
source = {p.relative_to(RESOURCES).as_posix(): p for p in (RESOURCES / "data").rglob("*")
          if p.is_file() and p.suffix not in IGNORED}
errors = []
json_count = 0
with zipfile.ZipFile(JAR) as archive:
    names = archive.namelist()
    errors.extend("Duplicate ZIP entry: " + n for n, count in Counter(names).items() if count > 1)
    corrupt = archive.testzip()
    if corrupt:
        errors.append("Corrupt ZIP entry: " + corrupt)
    packaged = {n for n in names if n.startswith("data/") and not n.endswith("/")}
    errors.extend("Missing data file: " + n for n in sorted(source.keys() - packaged))
    # The build can legitimately contain upstream TFC datapack files from dependency
    # resources. Only stale files under our own namespace indicate removed Florae data.
    errors.extend("Stale Florae data file: " + n for n in sorted(packaged - source.keys())
                  if n.startswith("data/tfcflorae/"))
    for name in sorted(source.keys() & packaged):
        content = archive.read(name)
        if content != source[name].read_bytes():
            errors.append("Source differs from jar: " + name)
        if name.endswith(".json"):
            json_count += 1
            try:
                json.loads(content)
            except Exception as error:
                errors.append("Invalid JSON: " + name + ": " + str(error))
    for name in names:
        if name.startswith(("tfcflorae/audit/", "_audit/", "_backup_datapack_registry/")) or name == "tfcflorae.audit.mixins.json":
            errors.append("Temporary audit/backup artifact packaged: " + name)
    for category in ("configured_feature", "placed_feature"):
        name = f"data/tfcflorae/worldgen/{category}/vein/kaolinite.json"
        if name in packaged:
            errors.append("Retired 1.18 kaolinite feature packaged: " + name)
    tag = json.loads(archive.read("data/tfc/tags/worldgen/configured_feature/forest_trees.json"))
    forest_entries = [v for v in tag["values"] if isinstance(v, str)]
    with zipfile.ZipFile(TFC_JAR) as tfc:
        tfc_names = set(tfc.namelist())
        for ident in forest_entries:
            namespace, location = ident.split(":", 1)
            configured_name = f"data/{namespace}/worldgen/configured_feature/{location}.json"
            if namespace == "tfc" and configured_name in tfc_names:
                configured = json.loads(tfc.read(configured_name))
            else:
                configured = json.loads(archive.read(configured_name))
            if configured.get("type") != "tfc:forest_entry":
                errors.append("Unsafe forest tag member (must be tfc:forest_entry): " + ident)

with JAR.open("rb") as stream:
    digest = hashlib.file_digest(stream, "sha256").hexdigest().upper()
report = dict(passed=not errors, jar=str(JAR), bytes=JAR.stat().st_size, sha256=digest,
              source_data_files=len(source), packaged_data_files=len(packaged),
              json_files=json_count, forest_tag_members=len(forest_entries), errors=errors)
(ROOT / "reports/data-package.json").write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
print(json.dumps(report, ensure_ascii=True, indent=2))
raise SystemExit(bool(errors))
