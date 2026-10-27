# knife5：A 类（无 lang、无模型）清理记录

- 提交：`abec259b0 knife5: drop registrations that had neither lang nor model (audit A=0)`
- 审计工具：`tools/audit_register_assets.py`
- 实时报告（清理后）：`reports/registry-asset-audit.{json,md}`

## 清理前的 A 类清单（123 项）

判定标准：源码确实注册、`assets/tfcflorae/blockstates`、`models/block`、`models/item` 三处均无文件，
且 `lang/zh_cn.json`、`lang/en_us.json` 均无对应键 —— 即游戏里必然显现的紫黑实物。
每一项都用 `reports/runtime-registries.json`（2026-10-24 真实注册表 dump）交叉核对过。

| 分组 | 数量 | 注册点 | id 形式 | dump 中是否存在 |
|---|---|---|---|---|
| TFC 宝石类矿石 | 96 | `TFCFBlocks.ORES`（`Ore.class, ore -> !ore.isGraded()`） | `ore/<gem>/<rock>`，gem ∈ {diamond, emerald, lapis_lazuli, opal, pyrite, saltpeter, sapphire, topaz}，rock 为 12 种本模组岩石 | 是（10-24 起就是紫黑） |
| TFC 岩石间歇泉 | 20 | `TFCFBlocks.GEYSER_TFC`（`Rock.class`） | `rock/geyser/<rock>`，rock 为 TFC 20 种原生岩石 | 是 |
| 凝灰岩间歇泉 | 1 | `TFCFBlocks.GEYSER_TFCF`（`TFCFRock.class`） | `rock/geyser/tuff` | 否（TUFF 为 10-26 后新增岩石） |
| 红树木书柜 / 木墙 | 3 | `normalBookshelfMapperTFC`、`woodWallMapperTFC`（`Wood.class`） | `wood/bookshelf/mangrove`、`wood/log_wall/mangrove`、`wood/wood_wall/mangrove` | 是 |
| 竹子的 wood / stripped_wood | 2 | `woodMapper`（`TFCFWood.class`）的 BAMBOO 分支 | `wood/wood/bamboo`、`wood/stripped_wood/bamboo` | 否（bamboo 木材为后续恢复） |
| （解析器死代码误报，未删） | 1 | `chiseledBookshelfMapperTFC` 从未被调用 | `wood/chiseled_bookshelf/mangrove` | 否 |

合计 123 = 96 + 21 + 3 + 2 + 1（其中最后 1 项经核实是「未被调用的 mapper」，实际未注册，因此未做删除）。

## 删除方式（都复用了已有的空值/谓词守卫，未用正则删枚举常量）

1. `TFCFBlocks.ORES`：新增 `ORE_WITHOUT_BLOCK`（`EnumSet`，8 种宝石矿），
   谓词改为 `ore -> !ore.isGraded() && !ORE_WITHOUT_BLOCK.contains(ore)`。
2. `TFCFBlocks.GEYSER_TFC`：整表改为 `Map.of()`（不再注册 TFC 岩石间歇泉）；
   `GEYSER_TFCF` 外层 `mapOfKeys` 增加 `rock -> rock != TFCFRock.TUFF`。
3. `normalBookshelfMapperTFC`、`woodWallMapperTFC`：for 循环首行
   `if (wood == Wood.MANGROVE) { continue; }`。
4. `woodMapper` 的 BAMBOO 分支：`if (type == BlockType.WOOD || type == BlockType.STRIPPED_WOOD) { continue; }`；
   同时 `bambooBundledLogMapper` 里原本引用 `WOODS.get(BAMBOO).get(STRIPPED_WOOD)` 的 supplier
   回退到仍注册的 `STRIPPED_LOG`，避免空值。
5. 顺带删除源码树里的 `TFCFBlocks.java.damaged`（3027 行旧备份，会被打进 jar）。

## 删除后验证

- `gradlew --offline compileJava` → EXIT=0
- `gradlew --offline assemble` → EXIT=0（1m49s，`build/libs/TFCFlorae-1.20.1-2.2.0-mc1.20.1.jar`，34,798,586 字节）
- jar 内无 `tfcflorae/audit/`、`_audit/`、`*.damaged`、`*.bak` 残留（65,495 entries）
- 重跑审计：**A = 0**、B = 19、C = 2
- 部署：`D:\线框的服务器\versions\TFC_Wild_Fire\mods\TFCFlorae-1.20.1-2.2.0-mc1.20.1.jar`
  SHA256 `285BC5DD73F7EDCD0AF6A15232CB38FC2B9572FB4FC96B5A62D5125524160984`，
  旧包备份 `...jar.bak-20261027-231107-080`，SHA256 一致（`reports/deployment.json`）

## 仍然遗留（未在本次处理）

- **B 类 19 项**：全部是 `tuff`（凝灰岩）系列方块缺 `block.tfcflorae.*` 中文/英文 lang，
  有模型、无翻译，游戏内显示原始键名。清单见 `reports/registry-asset-audit.md`。
- **C 类 2 项**：`soil_pile/silty_loam`、`wood/roots/white_mangrove/silty_loam` 有 lang 无模型。
