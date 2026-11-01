# -*- coding: utf-8 -*-
"""为 TFC/TFE（可选 tfcflorae）的草方块与腐殖土生成「顶部随机纹理」资源包，并写入 Wild_Fire.zip。

思路（参照 Simple Grass Flowers v2.0.0）：
  - 只改 blockstate 里 {"snowy": false} 那一项的 apply，换成加权随机列表；
    每个变体都给 4 个 y 旋转，与 TFC 原本的随机旋转一致。
  - 合并型变体：模型 parent 指向原顶层父模型，仅覆盖 overlay 贴图（草=染色、腐殖土=不染色）。
  - 叠加型变体：自建模型 = 原顶面（tintindex 1）+ 上方 0.075 处透明装饰面（不染色）。

用法：
  python tools/build_grass_variant_pack.py                 # 只做 TFC/TFE + 腐殖土，预演
  python tools/build_grass_variant_pack.py --apply         # 写入 zip
  python tools/build_grass_variant_pack.py --ours=core --apply
        --ours 取值：none(默认) | core | dense | dry | all
"""
import io
import json
import os
import sys
import zipfile

REF = r"D:\新储存\Simple Grass Flowers v2.0.0\assets\minecraft\textures\block"
ZIP = r"D:\线框的服务器\versions\TFC_Wild_Fire\config\paxi\resourcepacks\Wild_Fire.zip"
OURS_JAR = r"D:\学习资料\附属模组工作区\tfc_florae_port\build\libs\TFCFlorae-1.20.1-2.2.0-mc1.20.1.jar"
TFC_JAR = r"D:\学习资料\附属模组工作区\[群峦传说] TerraFirmaCraft-Forge-1.20.1-3.2.20.jar"
TFE_JAR = r"D:\线框的服务器\versions\TFC_Wild_Fire\mods\Wildfire_TerraFirmaEarth-1.20.1-forge-2.3.0.jar"

NS = "wfvariant"          # 本资源包自己的命名空间
WEIGHT_PLAIN = 120
WEIGHT_CLOVER = 8
WEIGHT_CLOVER_SMALL = 12
WEIGHT_FLOWER_SMALL = 4
WEIGHT_FLOWER_BIG = 2
WEIGHT_ROCK = 1

JARS = {}
for key, path in (("tfcflorae", OURS_JAR), ("tfc", TFC_JAR), ("tfe", TFE_JAR)):
    z = zipfile.ZipFile(path)
    JARS[key] = (z, set(x for x in z.namelist() if not x.endswith("/")))


def read_src(srcjar, ns, rel):
    """srcjar 是资源所在 jar 的键；ns 是方块 id 的命名空间。
    注意两者可能不同：tfc:duff/* 的方块 id 属 tfc，但资源文件在 TFE 的 jar 里。"""
    z, names = JARS[srcjar]
    full = "assets/%s/blockstates/%s.json" % (ns, rel)
    if full not in names:
        raise SystemExit("源 blockstate 缺失: %s (jar=%s)" % (full, srcjar))
    return json.loads(z.read(full).decode("utf-8"))


def png_bytes(sub, name):
    p = os.path.join(REF, sub, name + ".png")
    if not os.path.exists(p):
        raise SystemExit("参考贴图缺失: " + p)
    with open(p, "rb") as f:
        return f.read()


# ---------------------------------------------------------------- 资源定义

GRASS_MERGED = [("clover", "grass_block_top_clover", WEIGHT_CLOVER),
                ("clover_small", "grass_block_top_clover_small", WEIGHT_CLOVER_SMALL)]
GRASS_OVERLAY = [("flower_small", "grass_block_top_flower_small", WEIGHT_FLOWER_SMALL),
                 ("flower_big", "grass_block_top_flower_big", WEIGHT_FLOWER_BIG),
                 ("rock", "grass_block_top_rock", WEIGHT_ROCK)]
DUFF_MERGED = [("clover", "podzol_top_clover", WEIGHT_CLOVER),
               ("clover_small", "podzol_top_clover_small", WEIGHT_CLOVER_SMALL),
               ("flower_small", "podzol_top_flower_small", WEIGHT_FLOWER_SMALL),
               ("flower_big", "podzol_top_flower_big", WEIGHT_FLOWER_BIG),
               ("rock", "podzol_top_rock", WEIGHT_ROCK)]


def build_resources():
    """返回 {zip 内路径: bytes}"""
    out = {}

    # --- 贴图 ---
    for _tag, tex, _w in GRASS_MERGED + GRASS_OVERLAY:
        out["assets/%s/textures/block/%s.png" % (NS, tex)] = png_bytes("grass_block", tex)
    for _tag, tex, _w in DUFF_MERGED:
        out["assets/%s/textures/block/%s.png" % (NS, tex)] = png_bytes("podzol", tex)

    # --- 草：合并型模型（沿用 TFC 的 grass_top，只换 overlay 贴图，仍是 tintindex 1）---
    for tag, tex, _w in GRASS_MERGED:
        m = {"parent": "tfc:block/grass_top",
             "textures": {"overlay": "%s:block/%s" % (NS, tex)}}
        out["assets/%s/models/block/grass_%s.json" % (NS, tag)] = \
            json.dumps(m, ensure_ascii=False).encode("utf-8")

    # --- 草：叠加型模型（原顶面 + 上方装饰面，装饰不染色）---
    for tag, tex, _w in GRASS_OVERLAY:
        m = {
            "parent": "block/block",
            "textures": {
                "overlay": "tfc:block/grass_top",
                "decor": "%s:block/%s" % (NS, tex),
                "particle": "tfc:block/grass_top",
            },
            "elements": [
                {"from": [0, 0, 0], "to": [16, 16, 16],
                 "faces": {"up": {"uv": [0, 0, 16, 16], "texture": "#overlay",
                                  "cullface": "up", "tintindex": 1}}},
                {"from": [0, 16.075, 0], "to": [16, 16.075, 16],
                 "faces": {"up": {"uv": [0, 0, 16, 16], "texture": "#decor"},
                           "down": {"uv": [0, 16, 16, 0], "texture": "#decor"}}},
            ],
        }
        out["assets/%s/models/block/grass_%s.json" % (NS, tag)] = \
            json.dumps(m, ensure_ascii=False).encode("utf-8")

    # --- 腐殖土：全部合并型（podzol 变体本身就是整张顶面）---
    for tag, tex, _w in DUFF_MERGED:
        m = {"parent": "tfe:block/duff_top",
             "textures": {"overlay": "%s:block/%s" % (NS, tex)}}
        out["assets/%s/models/block/duff_%s.json" % (NS, tag)] = \
            json.dumps(m, ensure_ascii=False).encode("utf-8")
    return out


# ---------------------------------------------------------------- blockstate 改写

def top_selector_index(j):
    for i, sel in enumerate(j.get("multipart", [])):
        when = sel.get("when") or {}
        if when == {"snowy": False}:
            ap = sel.get("apply")
            # TFC/TFE 这里是 4 条旋转的数组；tfcflorae 是单个对象（原本不带随机旋转）
            if isinstance(ap, (list, dict)) and ap:
                return i
    raise SystemExit("找不到 {snowy:false} 的顶层 apply")


def rewrite(j, kind):
    """kind: 'grass' | 'duff'"""
    i = top_selector_index(j)
    ap = j["multipart"][i]["apply"]
    plain = ap if isinstance(ap, list) else [ap]
    plain_model = plain[0].get("model")
    rows = []
    for rot in (0, 90, 180, 270):
        e = {"model": plain_model, "x": 270, "weight": WEIGHT_PLAIN}
        if rot:
            e["y"] = rot
        rows.append(e)
    if kind == "grass":
        for tag, _tex, w in GRASS_MERGED:
            for rot in (0, 90, 180, 270):
                e = {"model": "%s:block/grass_%s" % (NS, tag), "x": 270, "weight": w}
                if rot:
                    e["y"] = rot
                rows.append(e)
        for tag, _tex, w in GRASS_OVERLAY:
            for rot in (0, 90, 180, 270):
                e = {"model": "%s:block/grass_%s" % (NS, tag), "weight": w}
                if rot:
                    e["y"] = rot
                rows.append(e)
    else:
        for tag, _tex, w in DUFF_MERGED:
            for rot in (0, 90, 180, 270):
                e = {"model": "%s:block/duff_%s" % (NS, tag), "x": 270, "weight": w}
                if rot:
                    e["y"] = rot
                rows.append(e)
    j["multipart"][i]["apply"] = rows
    return j


def targets(ours_mode):
    """返回 [(方块命名空间, 相对路径, 类型, 资源所在 jar)]"""
    t = []
    # TFC 草方块（资源在 TFC jar）
    for v in ("loam", "sandy_loam", "silt", "silty_loam"):
        t.append(("tfc", "grass/" + v, "grass", "tfc"))
        t.append(("tfc", "clay_grass/" + v, "grass", "tfc"))
    # tfc:duff / tfc:clay_duff 是 TFE 注册、资源也在 TFE jar
    for v in ("loam", "sandy_loam", "silt", "silty_loam"):
        t.append(("tfc", "duff/" + v, "duff", "tfe"))
        t.append(("tfc", "clay_duff/" + v, "duff", "tfe"))
    # TFE 草方块
    for v in ("fluvisol", "mollisol", "oxisol", "podzol"):
        t.append(("tfe", "grass/" + v, "grass", "tfe"))
        t.append(("tfe", "clay_grass/" + v, "grass", "tfe"))
    for v in ("fluvisol", "loam", "mollisol", "oxisol", "podzol", "sandy_loam", "silt", "silty_loam"):
        t.append(("tfe", "duff/" + v, "duff", "tfe"))
        t.append(("tfe", "clay_duff/" + v, "duff", "tfe"))
    # tfcflorae 自己的常规草方块：单层染色，顶层同为 tfc:block/grass_top，可共用同一套变体
    if ours_mode in ("core", "dense", "dry", "all"):
        t.append(("tfcflorae", "grass/bog_iron", "grass", "tfcflorae"))
        t.append(("tfcflorae", "clay_grass/humus", "grass", "tfcflorae"))
        for v in ("fluvisol", "loam", "mollisol", "oxisol", "podzol",
                  "sandy_loam", "silt", "silty_loam"):
            t.append(("tfcflorae", "earthenware_clay_grass/" + v, "grass", "tfcflorae"))
            t.append(("tfcflorae", "stoneware_clay_grass/" + v, "grass", "tfcflorae"))
    return t


# ---------------------------------------------------------------- 写入 zip

def main():
    apply = "--apply" in sys.argv
    ours = "none"
    for a in sys.argv:
        if a.startswith("--ours="):
            ours = a.split("=", 1)[1]

    res = build_resources()
    tg = targets(ours)

    bss = {}
    for ns, rel, kind, srcjar in tg:
        j = read_src(srcjar, ns, rel)
        bss["assets/%s/blockstates/%s.json" % (ns, rel)] = \
            json.dumps(rewrite(j, kind), ensure_ascii=False).encode("utf-8")

    print("=== 资源清单 ===")
    print("  贴图: %d  模型: %d  blockstate: %d" % (
        sum(1 for k in res if k.endswith(".png")),
        sum(1 for k in res if "/models/" in k),
        len(bss)))
    print("  合计新增条目: %d" % (len(res) + len(bss)))
    print()
    print("=== 方块（%d）===" % len(tg))
    fam = {}
    for ns, rel, kind, _sj in tg:
        fam.setdefault("%s %s" % (kind, rel.split("/")[0]), []).append(rel)
    for k in sorted(fam):
        print("   %-34s %d 个" % (k, len(fam[k])))

    payload = dict(res)
    payload.update(bss)

    if not apply:
        print()
        print("（预演，未写入。加 --apply 才写进 zip）")
        return

    print()
    print("=== 写入 %s ===" % ZIP)
    zin = zipfile.ZipFile(ZIP)
    old = [i for i in zin.infolist() if not i.filename.endswith("/")]
    oldnames = set(i.filename for i in old)
    dup = sorted(set(payload) & oldnames)
    print("  原条目: %d" % len(old))
    print("  覆盖同名条目: %d %s" % (len(dup), "" if not dup else "-> 将整包重写"))
    if dup:
        for d in dup[:10]:
            print("     ~ " + d)

    tmp = ZIP + ".tmp"
    n_add = n_rep = 0
    with zipfile.ZipFile(tmp, "w", zipfile.ZIP_DEFLATED) as zout:
        for it in old:
            data = zin.read(it.filename)
            if it.filename in payload:
                data = payload[it.filename]
                n_rep += 1
            zout.writestr(it, data)
        for k, v in payload.items():
            if k not in oldnames:
                zout.writestr(k, v)
                n_add += 1
    zin.close()

    with zipfile.ZipFile(tmp) as zc:
        if zc.testzip() is not None:
            raise SystemExit("新 zip 校验失败，未替换")
        n_new = len([x for x in zc.namelist() if not x.endswith("/")])
        for k, v in payload.items():
            if zc.read(k) != v:
                raise SystemExit("条目内容不符: " + k)
        if "pack.mcmeta" not in zc.namelist():
            raise SystemExit("pack.mcmeta 丢了")
    print("  替换 %d，新增 %d，条目 %d -> %d" % (n_rep, n_add, len(old), n_new))
    os.replace(tmp, ZIP)
    print("  已写入: %s  (%d 字节)" % (ZIP, os.path.getsize(ZIP)))


if __name__ == "__main__":
    main()
