# -*- coding: utf-8 -*-
"""在资源包上「只改需要改的」：把草方块方向分支的 top/snowy_top 换成 side/snowy_side。

关键原则：**以包内现有 JSON 为基准**，只重写方向分支里的 model 字段，
其余内容（含 wfvariant 随机花草顶纹理的加权引用）原样保留。
"""
import json
import os
import shutil
import sys
import zipfile

DIRS = ("north", "east", "south", "west")


def fix(j):
    changed = 0
    for part in j.get("multipart", []):
        when = part.get("when", {})
        if not any(d in when for d in DIRS):
            continue
        ap = part.get("apply")
        items = ap if isinstance(ap, list) else [ap]
        for a in items:
            m = a.get("model", "")
            if m.endswith("/snowy_top"):
                a["model"] = m[: -len("/snowy_top")] + "/snowy_side"
                changed += 1
            elif m.endswith("/top"):
                a["model"] = m[: -len("/top")] + "/side"
                changed += 1
    return changed


def run(path):
    tmp = path + ".side"
    changed_files = 0
    changed_items = 0
    with zipfile.ZipFile(path) as zin, zipfile.ZipFile(tmp, "w", zipfile.ZIP_DEFLATED) as zout:
        for info in zin.infolist():
            if info.is_dir():
                continue
            data = zin.read(info.filename)
            name = info.filename
            if "/blockstates/" in name and name.endswith(".json") and "grass" in name:
                try:
                    j = json.loads(data.decode("utf-8"))
                except Exception:
                    j = None
                if j and "multipart" in j:
                    n = fix(j)
                    if n:
                        changed_files += 1
                        changed_items += n
                        data = (json.dumps(j, ensure_ascii=False,
                                           separators=(",", ":")) + "\n").encode("utf-8")
            zout.writestr(name, data)
    os.replace(tmp, path)
    return changed_files, changed_items


if __name__ == "__main__":
    target = sys.argv[1] if len(sys.argv) > 1 else \
        r"D:\线框的服务器\versions\TFC_Wild_Fire\config\paxi\resourcepacks\Wild_Fire.zip"
    f, i = run(target)
    print("已处理: %s" % target)
    print("   改动 blockstate %d 个，替换模型引用 %d 处" % (f, i))
