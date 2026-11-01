# -*- coding: utf-8 -*-
"""把「修改纹理」目录里的贴图同步进源码树 + jar。

源目录是扁平暂存区，各子目录对应 jar 里不同位置（映射见 MAP）。
工具文件（.exe / .aseprite / .slices.txt / .lip-tmp）一律不进 jar。

用法：
  python tools/sync_sand_textures.py            # 只看报告，不改任何东西
  python tools/sync_sand_textures.py --apply    # 写入源码树 + 替换 jar 内条目
"""
import hashlib
import io
import os
import shutil
import struct
import sys
import zipfile

SRC = r"D:\学习资料\mod解包\TFCFlorae-1.20.1-2.2.0-mc1.20.1\assets\tfcflorae\textures\block\sand\修改纹理"
ROOT = r"D:\学习资料\附属模组工作区\tfc_florae_port"
JAR = os.path.join(ROOT, "build", "libs", "TFCFlorae-1.20.1-2.2.0-mc1.20.1.jar")
DEPLOY = r"D:\线框的服务器\versions\TFC_Wild_Fire\mods\TFCFlorae-1.20.1-2.2.0-mc1.20.1.jar"

# 源子目录 -> jar 内目录（前缀）
BASE = "assets/tfcflorae/textures/block/"
MAP = {
    "earthenware_clay":        BASE + "earthenware_clay",
    "stoneware_clay":          BASE + "stoneware_clay",
    "pebble":                  BASE + "sand/pebble",
    "rocky":                   BASE + "sand/rocky",
    "rockier":                 BASE + "sand/rockier",
    "rockiest":                BASE + "sand/rockiest",
    "pebble_compact_dirt":     BASE + "compact_dirt/pebble_compact_dirt",
    "rocky_compact_dirt":      BASE + "compact_dirt/rocky_compact_dirt",
    "rockier_compact_dirt":    BASE + "compact_dirt/rockier_compact_dirt",
    "rockiest_compact_dirt":   BASE + "compact_dirt/rockiest_compact_dirt",
}

SKIP_DIRS = {".lip-tmp"}
SKIP_EXT = {".exe", ".aseprite", ".txt", ".log", ".db", ".ini"}


def sha(b):
    return hashlib.sha256(b).hexdigest()


def png_info(data):
    """只用文件头解析：返回 (宽, 高, 位深, 颜色类型) 或 None。"""
    if data[:8] != b"\x89PNG\r\n\x1a\n":
        return None
    if data[12:16] != b"IHDR":
        return None
    w, h, depth, ctype = struct.unpack(">IIBB", data[16:26])
    return w, h, depth, ctype


CTYPE = {0: "灰度", 2: "RGB", 3: "调色板", 4: "灰度+A", 6: "RGBA"}


def collect():
    out = []
    for root, dirs, files in os.walk(SRC):
        dirs[:] = [d for d in dirs if d not in SKIP_DIRS]
        for f in files:
            full = os.path.join(root, f)
            rel = os.path.relpath(full, SRC).replace("\\", "/")
            ext = os.path.splitext(f)[1].lower()
            if ext != ".png":
                out.append(("SKIP", rel, full, None))
                continue
            sub = rel.rsplit("/", 1)[0] if "/" in rel else ""
            if sub not in MAP:
                out.append(("NOMAP", rel, full, None))
                continue
            dest = MAP[sub] + "/" + rel.rsplit("/", 1)[-1]
            out.append(("PNG", rel, full, dest))
    out.sort(key=lambda r: r[1])
    return out


def main():
    apply = "--apply" in sys.argv
    items = collect()

    z = zipfile.ZipFile(JAR)
    jarnames = set(x for x in z.namelist() if not x.endswith("/"))

    ok, bad, nomap, skipped = [], [], [], []
    for kind, rel, full, dest in items:
        if kind == "SKIP":
            skipped.append(rel)
            continue
        if kind == "NOMAP":
            nomap.append(rel)
            continue
        data = open(full, "rb").read()
        info = png_info(data)
        if info is None:
            bad.append((rel, "不是合法 PNG"))
            continue
        w, h, depth, ctype = info
        if (w, h) != (16, 16):
            bad.append((rel, "尺寸 %dx%d 不是 16x16" % (w, h)))
            continue
        if ctype not in CTYPE:
            bad.append((rel, "颜色类型 %d 无法识别" % ctype))
            continue
        if dest not in jarnames:
            bad.append((rel, "jar 里没有目标 %s" % dest))
            continue
        ok.append((rel, full, dest, data, info))

    print("=== 分类 ===")
    print("  可同步 PNG      : %d" % len(ok))
    print("  被跳过（非纹理）: %d" % len(skipped))
    print("  无映射子目录    : %d" % len(nomap))
    print("  异常            : %d" % len(bad))

    if skipped:
        print("\n  跳过的文件：")
        for s in skipped:
            print("     ", s)
    if nomap:
        print("\n  无映射（需确认）：")
        for s in nomap:
            print("     ", s)
    if bad:
        print("\n  异常（不会写入）：")
        for r, why in bad:
            print("     %-50s %s" % (r, why))

    print("\n=== 明细（源 -> jar 内路径，尺寸/格式，字节变化）===")
    by_dir = {}
    for rel, full, dest, data, info in ok:
        by_dir.setdefault(rel.rsplit("/", 1)[0], []).append((rel, full, dest, data, info))
    for sub in sorted(by_dir):
        print("\n  [%s]" % sub)
        for rel, full, dest, data, info in sorted(by_dir[sub]):
            old = z.read(dest)
            w, h, depth, ctype = info
            mark = "  同" if sha(old) == sha(data) else "改  "
            print("     %s %-42s %s %2dx%-2d  %6d -> %6d" % (
                mark, rel, CTYPE[ctype], w, h, len(old), len(data)))

    if not apply:
        print("\n（这是预演，未做任何修改。加 --apply 才写入）")
        return

    # ---------- 1. 写入源码树 ----------
    n_src = 0
    for rel, full, dest, data, info in ok:
        target = os.path.join(ROOT, "src", "main", "resources", dest.replace("/", os.sep))
        os.makedirs(os.path.dirname(target), exist_ok=True)
        shutil.copyfile(full, target)
        n_src += 1
    print("\n写入源码树: %d 个" % n_src)

    # ---------- 2. 重写 jar（替换同名条目） ----------
    # 必须先关掉开头的只读句柄：Windows 不允许替换仍被打开的文件
    # （否则 os.replace 报 WinError 5 拒绝访问）。
    z.close()
    replaced = {dest: data for rel, full, dest, data, info in ok}
    tmp = JAR + ".tmp"
    n_rep = 0
    with zipfile.ZipFile(JAR) as zin, \
         zipfile.ZipFile(tmp, "w", zipfile.ZIP_DEFLATED) as zout:
        for item in zin.infolist():
            if item.filename in replaced and not item.filename.endswith("/"):
                zout.writestr(item, replaced[item.filename])
                n_rep += 1
            else:
                zout.writestr(item, zin.read(item.filename))
    print("替换 jar 内条目: %d" % n_rep)

    # ---------- 3. 校验新 jar ----------
    with zipfile.ZipFile(tmp) as zc:
        if zc.testzip() is not None:
            raise SystemExit("新 jar 校验失败，未替换")
        n_entries = len(zc.namelist())
        for dest, data in replaced.items():
            if sha(zc.read(dest)) != sha(data):
                raise SystemExit("新 jar 内 %s 内容不符，未替换" % dest)
    with zipfile.ZipFile(JAR) as zo:
        n_old = len(zo.namelist())
    print("条目数: %d -> %d" % (n_old, n_entries))
    os.replace(tmp, JAR)
    print("jar 已更新: %s" % JAR)

    # ---------- 4. 部署 ----------
    shutil.copyfile(JAR, DEPLOY)
    h1 = sha(open(JAR, "rb").read())
    h2 = sha(open(DEPLOY, "rb").read())
    print("\n部署: %s" % DEPLOY)
    print("  大小  : %d 字节" % os.path.getsize(DEPLOY))
    print("  SHA256: %s" % h2)
    print("  一致  : %s" % (h1 == h2))


if __name__ == "__main__":
    main()
