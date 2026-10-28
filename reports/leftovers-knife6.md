# knife6：遗留项（B/C 类 + 凝灰岩纹理）修复记录

承接 `knife5` 的审计结论：A=0、B=19、C=2。本轮把 B/C 清零，并修掉凝灰岩（tuff）的纹理问题。

## 1. B 类：19 项凝灰岩缺翻译 → 0

`zh_cn.json` / `en_us.json` 各新增 37 个键（18 个岩石类型的 block + item 键，加 `item.tfcflorae.brick.tuff`）。
命名严格沿用其它岩石的既有模式（对照 boninite/arkose 的键值），例如：

| 键 | zh_cn | en_us |
|---|---|---|
| `block.tfcflorae.rock.anvil.tuff` | 凝灰岩石砧 | Rock Anvil Tuff |
| `block.tfcflorae.rock.magma.tuff` | 凝灰岩岩浆块 | Rock Magma Tuff |
| `block.tfcflorae.rock.mossy_loose.tuff` | 长满苔藓的凝灰岩 | Mossy Loose Tuff Rock |
| `item.tfcflorae.brick.tuff` | 凝灰岩砖 | Tuff Brick |

工具：`tools/add_lang_keys.py`（按字母序插入，保留 BOM，其余字节不动，可重复执行）。
diff 只有 `+37` 行/文件，没有删改任何既有条目。

## 2. C 类：2 项 silty_loam 缺模型 → 0

`silty_loam`（粉砂壤土）此前的注册与 lang 都存在，但模型/贴图整条链缺失：

| 新增文件 | 说明 |
|---|---|
| `models/item/soil_pile/silty_loam.json` | 仿 `soil_pile/loam.json` |
| `textures/item/soil_pile/silty_loam.png` | silt 与 loam 逐像素平均 |
| `blockstates/wood/roots/white_mangrove/silty_loam.json` | 仿 loam 的 blockstate |
| `models/block/wood/roots/white_mangrove/silty_loam.json` | 仿 loam 的模型，纹理改 `silty_loam_side/_top` |
| `textures/block/wood/roots/white_mangrove/silty_loam_{side,top}.png` | silt/loam 对应贴图的平均值 |

工具：`tools/gen_leftover_assets.py`（幂等，`--force` 可重生成）。

## 3. 凝灰岩岩浆块纹理 → 修好，并顺带发现同类问题

**根因**：`textures/block/rock/magma/tuff.png` 是 16×48 的三帧动画图（与 boninite 同一张），
但缺少配套的 `tuff.png.mcmeta`。客户端于是把整张 3 帧竖条当成**单帧静态贴图**压到 16×16 的方块面上，
显示为被纵向压缩的错乱纹理。其它所有岩浆块都带 `.mcmeta`（frametime 8 / interpolate / 3 帧）。

修复：复制同款 `magma/tuff.png.mcmeta`，岩浆动画恢复正常。

顺着「纹理链」继续检查（本轮给审计工具加了这条检查），又发现并修掉：

| 问题 | 数量 | 修复 |
|---|---|---|
| `rock/boulder|pebble|rubble/tuff.json` 贴图键写成 `all`、且指向 `tfc:item/loose_rock/conglomerate`（砾岩贴图） | 3 | 按 boninite 的结构重写为 `"0"`+`particle` → `tfcflorae:item/loose_rock/tuff` |
| `boulder|pebble|rubble/tuff_mossy.json` 缺失（生苔碎石紫黑） | 3 | 新增，贴图 `tfcflorae:block/rock/mossy_cobble/tuff` |
| `aqueduct/tuff/{base,east,north,south,west}.json` 缺失（凝灰岩引水桥紫黑） | 5 | 由 boninite 的对应模型派生，贴图换 `bricks/tuff` |
| `textures/item/loose_rock/tuff.png`、`item/rock/brick/tuff.png` 缺失（碎石/砖物品紫黑） | 2 | 由 boninite 对应物品贴图复制（tuff 岩石贴图本就是 boninite 的副本，保持一致） |

## 4. 审计工具增强

`tools/audit_register_assets.py` 新增 **纹理链检查**：

- blockstate → `variants`/`multipart` 引用的模型是否存在；
- 模型 → `parent` 链（只追 `tfcflorae:` 与能命中本地模型的隐式引用，`minecraft:`/`tfc:` 交给上游）；
- `textures` 里 `tfcflorae:` 贴图是否存在（含 `#var` 间接引用解析）。

结果进入报告（`missing_model_refs` / `missing_textures`），控制台也会打印。
正是这条检查发现了上面 13 个模型/贴图问题。

## 5. 当前状态

- 审计：**A=0 / B=0 / C=0**，`missing model refs = 0`，`missing textures = 0`
- `gradlew --offline assemble` EXIT=0
- jar 部署到 `D:\线框的服务器\versions\TFC_Wild_Fire\mods`（旧包备份，SHA256 见 `reports/deployment.json`）

## 6. 仍待确认的一点（未改动）

凝灰岩的**整套岩石贴图**（`raw`/`cobble`/`bricks`/`smooth`/`chiseled`/`gravel`/`cracked_bricks`/
`mossy_bricks`/`mossy_cobble`/`magma`）目前都是 boninite 贴图的字节级副本 —— 也就是说凝灰岩和玻安岩
外观完全相同。这是移植时就带来的现状，不属于「紫黑/错乱」类错误，本轮没有重绘。
如果希望凝灰岩有独立外观，需要单独重绘这 ~10 张 16×16 贴图（可选，另开一轮）。
