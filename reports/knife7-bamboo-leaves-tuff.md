# knife7：竹苗、树叶掉苗、silty_loam 物品、tuff 重复项

承接前两轮的竹苗修复与凝灰岩换色，本轮解决 4 个回归/遗留问题。

## 1. 竹苗：粗竹与细竹分工修好

上一轮为了绕开「`tfcflorae:tree/grower/<plant>` 不存在」的崩溃，把 `wood/sapling/{blue,dragon,red}_bamboo`
改成了 `BambooSaplingPlantBlock`。副作用是这三株苗长成了**细竹**（`plant/<colour>`），
而它们本应是**粗竹**；同时 `plant/<colour>_bamboo_sapling` 因为没实现骨粉接口也无法催熟。

根因与修复：

| 方块 | 应有行为 | 之前 | 现在 |
|---|---|---|---|
| `wood/sapling/bamboo` | 粗竹（`wood/log/bamboo`） | 正常 | 保持（`TFCTreeGrower` → `tree/bamboo`） |
| `wood/sapling/{blue,dragon,red}_bamboo` | 粗竹（`wood/log/<colour>_bamboo`） | 长成细竹、不能催熟 | `TFCSaplingBlock` + `tree/<colour>_bamboo`（新建的 configured feature） |
| `plant/{blue,dragon,red}_bamboo_sapling` | 细竹（`plant/<colour>`） | 长成细竹但**不能催熟** | 保持 `BambooSaplingPlantBlock`，补 `implements BonemealableBlock` |

- 新增 `data/tfcflorae/worldgen/configured_feature/tree/{blue,dragon,red}_bamboo.json`：
  由 `tree/bamboo.json` 派生，树干换成 `wood/log/<colour>_bamboo`、树叶换成 `wood/leaves/<colour>_bamboo`。
  `bambooSaplingMapper` 的 grower 重新指向这三个 feature（`_large` 缺失时 `TFCTreeGrower` 会回落到普通 feature）。
- `BambooSaplingPlantBlock` 之前只「定义」了 `isValidBonemealTarget` / `performBonemeal`，
  但 `PlantBlock` 并不实现 `BonemealableBlock`，所以骨粉根本不会调用它们。现在显式实现接口，
  并把 `isValidBonemealTarget` 参数改为 1.20.1 的 `LevelReader`、补 `isBonemealSuccess`（每次骨粉必定生效）。
- `VanillaBambooSaplingBlock` 的 `isValidBonemealTarget` 同样修正签名（原来是个不覆盖任何东西的重载）。
- 竹苗方块重新挂到 `TFCFBlockEntities.TICK_COUNTER`（`BAMBOO_SAPLINGS` 已在该 BE 的合法方块表里），
  自然生长靠 `TFCSaplingBlock.randomTick`。

## 2. 所有树叶都不掉树苗

`data/tfcflorae/loot_tables/blocks/wood/leaves/` 下 70 张表：

- 树苗概率统一是 `0.0083`（0.83%），而 TFC 是 0.011~0.078（多数 0.03~0.04）。
  0.83% 实际体验就是「挖几百个叶子也不掉」，已统一改为 `0.04`。
- `bald_cypress`、`red_cypress` 两张表**根本没有树苗条目**：它们的第二个 alternative
  又掉了一次树叶本身（复制粘贴错误），已改为掉对应树苗。

修复后 70 张表全部包含各自树苗掉落。工具：`tools/fix_leaf_saplings.py`（幂等）。

## 3. `wood/roots/white_mangrove/silty_loam` 紫黑

方块本身资源齐全（blockstate + block model + side/top 贴图），缺的是 **`models/item/.../silty_loam.json`**，
所以拿在手上/物品栏里是紫黑。已补（`parent` 指向对应方块模型）。

审计工具新增 **item model 检查**：所有带 BlockItem 的方块若缺 `models/item/<path>.json` 会被列出，
这类「方块正常、物品紫黑」的问题以后能直接发现。

## 4. 凝灰岩重复物品删除

TFE 已提供 tuff 的整套岩石方块，TFCF 这边重复注册的物品会挤在创造栏里。按要求把下列 18 个 id
的**物品**注册去掉（方块本体保留，避免已有存档、配方与内部引用变成空气）：

`rock/{raw,hardened,smooth,cobble,bricks,gravel,spike,cracked_bricks,chiseled,loose,pressure_plate,button,aqueduct,anvil,mossy_bricks,mossy_cobble,mossy_loose}/tuff` 与 `brick/tuff`

实现：

- `TFCFBlocks.registerRockBlock(name, supplier, withItem)`：`withItem=false` 时只注册方块、不生成 BlockItem。
- `rockMapper` / `ROCK_ANVILS` 对 `TFCFRock.TUFF` 传 `false`（`Rock.BlockType` 正好 16 种，与清单一致）。
- `TFCFItems.BRICKS` 跳过 `TFCFRock.TUFF`。

未动：`rock/column`、`rock/polished_column`、`rock/smoke_block`、`rock/magma`、`rock/geyser`、
`rock/gravel_layer` 等 tuff 方块，以及 `RockTypeDecoTFCMapper` 生成的 `rock/<type>/{slab,stairs,wall}/tuff`。
如果这些也要一并去掉物品，告诉我即可。

## 5. 仍存在的问题（本轮未处理，需要你确认）

审计发现 **264 个方块物品没有 `models/item`**，而且它们的方块模型也全部缺失，
集中在四类：`wood/horizontal_support/*`、`wood/vertical_support/*`、`wood/wall_sign/*`、`wood/windmill/*`
（各 66 种木材）。这些属于未完成的移植内容（支撑柱、墙牌、风车），
在游戏里既是紫黑方块也是紫黑物品。要补齐需要设计/搬运对应模型与贴图，或直接从注册里移除，等你决定。

## 6. 验证

- `compileJava` EXIT=0（19s）
- 审计：**A=0 / B=0 / C=0**，missing model refs 0、missing textures 0；物品数 5676 → 5675（`brick/tuff` 移除）
- `assemble` EXIT=0
- jar 部署到 `D:\线框的服务器\versions\TFC_Wild_Fire\mods`（旧包备份、SHA256 见 `reports/deployment.json`）

> 附带修了一个环境问题：Gradle daemon 偶尔会卡在 `Starting a Gradle Daemon` 不动，
> 清掉 `gradle-home/daemon/8.8/registry.bin.lock` 与项目 `.gradle/**/*.lock` 后重新构建即恢复。
