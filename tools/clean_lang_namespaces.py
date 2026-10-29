"""Drop the last few foreign-namespace keys and push zh_cn into the jar."""
import io
import json
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
LANG = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae", "lang")
FOREIGN = ("generator.tfc.", "subtitles.item.", "tfc.screen.")

for name in ("en_us.json", "zh_cn.json"):
    p = os.path.join(LANG, name)
    d = json.load(io.open(p, encoding="utf-8-sig"))
    before = len(d)
    d = {k: v for k, v in d.items() if not k.startswith(FOREIGN)}
    if len(d) != before:
        io.open(p, "w", encoding="utf-8", newline="\n").write(
            json.dumps(d, indent=2, ensure_ascii=False) + "\n")
        print("%s: removed %d" % (name, before - len(d)))
    print(name, "keys:", len(d))
