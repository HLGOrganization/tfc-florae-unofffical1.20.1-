"""Verify the packaged tag fixes against source before deploying this build."""
import hashlib
import json
import zipfile
from collections import Counter
from pathlib import Path

root = Path(__file__).resolve().parents[1]
jar = root / "build/libs/TFCFlorae-1.20.1-2.2.0-mc1.20.1.jar"
resources = root / "src/main/resources"
source = {path.relative_to(resources).as_posix(): path
          for path in (resources / "data").rglob("*.json")
          if "/tags/" in path.relative_to(resources).as_posix()}
errors = []
with zipfile.ZipFile(jar) as archive:
    names = archive.namelist()
    errors.extend("Duplicate ZIP entry: " + name for name, count in Counter(names).items() if count > 1)
    corrupt = archive.testzip()
    if corrupt:
        errors.append("Corrupt ZIP entry: " + corrupt)
    packaged = {name for name in names if name.startswith("data/") and "/tags/" in name
                and name.endswith(".json")}
    for name in sorted(source.keys() - packaged):
        errors.append("Missing tag: " + name)
    for name in sorted(packaged - source.keys()):
        errors.append("Stale tag: " + name)
    for name in sorted(packaged & source.keys()):
        if archive.read(name) != source[name].read_bytes():
            errors.append("Source differs from jar: " + name)
    for leaf in ("leaves", "logs", "fences"):
        name = f"data/tfcflorae/tags/blocks/{leaf}.json"
        if name in names:
            errors.append("Previously deleted tag still packaged: " + name)

with jar.open("rb") as jar_stream:
    digest = hashlib.file_digest(jar_stream, "sha256").hexdigest().upper()
report = {"passed": not errors, "jar": str(jar), "bytes": jar.stat().st_size,
          "sha256": digest,
          "source_tags": len(source), "packaged_tags": len(packaged), "errors": errors}
output = root / "reports/tag-package.json"
output.parent.mkdir(parents=True, exist_ok=True)
output.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
print(json.dumps(report, ensure_ascii=True, indent=2))
raise SystemExit(int(bool(errors)))
