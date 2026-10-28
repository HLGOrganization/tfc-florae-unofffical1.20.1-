# knife8：tag 体系、滑坡/坍塌配方、岩质分类、敲制面板纹理

## 1. Tag 桥接：把 tfcflorae 接入 TFC 的 tag 体系

1.18 移植过来的 tag 全部放在 `tfcflorae:` 命名空间（如 `tfcflorae:dirt`、`tfcflorae:can_landslide`），
TFC 1.20 的机制只认 `tfc:` 命名空间的 tag，所以那些 tag 一直是“死”的。

现在新增 `tools/gen_tags.py`，按 id 规则生成 TFC / Forge / minecraft 命名空间的同名 tag
（`"replace": false`，与 TFC 自己的值合并，这是 addon 的标准做法），共 **118 个文件**：

| 类别 | 生成的 tag | 说明 |
|---|---|---|
| 岩石 | `tfc:rock/{raw,hardened,smooth,gravel,bricks,mossy_bricks,cracked_bricks,chiseled_bricks,aqueducts}`、`tfc:rock_anvils` | 13 种岩石的对应方块 |
| 坍塌 | `tfc:can_collapse`（spike）、`tfc:breaks_when_isolated`（raw） | 其余经 `#tfc:rock/raw`、`#tfc:rock/smooth`、`#tfc:rock/cracked_bricks`、`#tfc:rock/hardened` 级联 |
| 滑坡 | `tfc:can_landslide` | cobble / mossy_cobble / gravel / gravel_layer / compact_dirt / sand 变体 |
| 松散石 | `tfc:loose_rocks` | loose + mossy_loose |
| 矿石 | `tfc:rock/ores`、`tfc:ore_deposits` | 609 个矿石与 48 个矿床 |
| 木材 | `minecraft:logs/planks/leaves/saplings/...`、`forge:logs/stripped_logs/chests` | 67 种木材 |
| 木制设备 | `tfc:barrels/bookshelves/workbenches/looms/sluices/lecterns/...` | 对应方块 |
| 通用 | `forge:cobblestone/normal`、`forge:gravel`、`forge:stone_bricks`、`forge:sand`、`forge:sandstone`、`minecraft:fences/doors/buttons/slabs/stairs/signs/trapdoors/flower_pots` | |

## 2. 岩质 tag（与现实地质一致）

TFC 没有岩质 tag（它用 `RockCategory` 枚举），所以新建 `tfcflorae:rock/<category>` 的 block + item tag，
分类与代码里的 `RockCategory` 完全一致：

| 岩质 | 岩石 |
|---|---|
| `igneous_intrusive`（侵入火成岩） | 红花岗岩 red_granite |
| `igneous_extrusive`（喷出火成岩） | 斑岩 porphyry、玻安岩 boninite、碳酸岩 carbonatite、凝灰岩 tuff |
| `sedimentary`（沉积岩） | 红土岩 laterite、泥岩 mudstone、砂岩 sandstone、长石砂岩 arkose、碧玉铁质岩 jaspillite、粒泥灰岩 wackestone |
| `metamorphic`（变质岩） | 碎裂岩 cataclasite、皂石 soapstone |

岩石的**产物**（cobble、bricks、loose、anvil、aqueduct、slab/stairs/wall 等）也在同一 tag 里。

## 3. 滑坡 / 坍塌配方

TFC 的滑坡与坍塌是**配方**（`tfc:landslide` / `tfc:collapse`），tfcflorae 之前只有 `collapse/arkose_cobble.json` 一条。
新增 `tools/gen_collapse_landslide.py` 生成：

- `collapse/<rock>_cobble.json`：13 种岩石的 raw/hardened/smooth/chiseled/bricks/cracked_bricks/cobble
  加该岩石的全部矿石 → 坍塌为 cobble
- `landslide/rock/<rock>_{cobble,gravel,mossy_cobble}.json`：碎石类滑坡
- `landslide/soil/*.json`：`rockier/rocky/rockiest/pebble_compact_dirt` 全部变体
- `landslide/sand/*.json`：`sand/{rocky,sandy_tiles,sandier_tiles,sandiest_tiles}` 全部变体

## 4. 敲制面板纹理

**根因**：`KnappingScreen.getButtonLocation()` 把纹理路径硬编码在 `tfc` 命名空间：

```java
Helpers.identifier("textures/gui/knapping/" + item.getPath() + (disabled ? "_disabled" : "") + ".png")
```

而 `assets/tfc/textures/gui/knapping/` 下**一个文件都没有**，所以陶土、石胎瓷土、石子敲制时整块面板空白。

新增 `tools/gen_knapping_textures.py`，按输入物品（敲制类型的 input tag + `tfc:rock_knapping` 里的本模组石子）
从**该物品自己的纹理**生成 16×16 面板贴图，并按 TFC 的做法生成 `_disabled` 版本
（亮度 ×0.772，系数取自 TFC 的 `clay_ball` 与 `clay_ball_disabled` 实测）。

生成 17 张：

- `knapping/ceramic/earthenware/clay.png` + `_disabled.png`（陶土）
- `knapping/ceramic/stoneware/clay.png` + `_disabled.png`（石胎瓷土）
- `knapping/rock/loose/<rock>.png` × 13（长石砂岩、玻安岩、碳酸岩、碎裂岩、滴水石、碧玉铁质岩、红土岩、泥岩、斑岩、红花岗岩、砂岩、皂石、粒泥灰岩）

燧石（`tfcflorae:rock/loose/flint`）用的是 `minecraft:item/flint` 外部纹理，未生成。

## 5. 验证

- 审计：A=0 / B=0 / C=0，missing model refs 0、missing textures 0
- 新增 data 文件：118 个 tag、约 1100 个 collapse/landslide 配方
- 新增资源：17 张敲制面板贴图
- `assemble` EXIT=0，jar 已部署（SHA256 见 `reports/deployment.json`）
