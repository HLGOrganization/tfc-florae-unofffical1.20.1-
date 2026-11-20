# 注册 / 资源审计（A/B/C）

由 `tools/audit_register_assets.py` 生成。判定依据：展开源码里的 `register(...)` 调用（含 `mapOfKeys` / `for` 循环变量），再对照 `assets/tfcflorae/{blockstates,models/block,models/item}` 与 `lang/{zh_cn,en_us}.json`，并用 `reports/runtime-registries.json`（真实注册表 dump）交叉验证。

| 类别 | 数量 | 含义 | 处理 |
|---|---|---|---|
| A | 670 | 注册了但无 lang、无任何模型 | 删除注册 |
| B | 78 | 有模型、缺 lang | 补翻译 |
| C | 0 | 有 lang、缺模型 | 补模型 |

统计：方块 4039、物品 4046；未解析注册点 50；expander-only 337；dump-only 6310。

## A 类（删注册）

### clay/fluvisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `clay/fluvisol` | TFCFBlocks.java:1330 | 无 |

### clay/mollisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `clay/mollisol` | TFCFBlocks.java:1330 | 无 |

### clay/oxisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `clay/oxisol` | TFCFBlocks.java:1330 | 无 |

### clay/podzol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `clay/podzol` | TFCFBlocks.java:1330 | 无 |

### clay_grass/fluvisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `clay_grass/fluvisol` | TFCFBlocks.java:1330 | 无 |

### clay_grass/mollisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `clay_grass/mollisol` | TFCFBlocks.java:1330 | 无 |

### clay_grass/oxisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `clay_grass/oxisol` | TFCFBlocks.java:1330 | 无 |

### clay_grass/podzol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `clay_grass/podzol` | TFCFBlocks.java:1330 | 无 |

### coarse_dirt/fluvisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `coarse_dirt/fluvisol` | TFCFBlocks.java:1330 | 无 |

### coarse_dirt/mollisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `coarse_dirt/mollisol` | TFCFBlocks.java:1330 | 无 |

### coarse_dirt/oxisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `coarse_dirt/oxisol` | TFCFBlocks.java:1330 | 无 |

### coarse_dirt/podzol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `coarse_dirt/podzol` | TFCFBlocks.java:1330 | 无 |

### compact_dirt/fluvisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `compact_dirt/fluvisol` | TFCFBlocks.java:1330 | 无 |

### compact_dirt/mollisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `compact_dirt/mollisol` | TFCFBlocks.java:1330 | 无 |

### compact_dirt/oxisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `compact_dirt/oxisol` | TFCFBlocks.java:1330 | 无 |

### compact_dirt/podzol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `compact_dirt/podzol` | TFCFBlocks.java:1330 | 无 |

### dense_clay_grass/fluvisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `dense_clay_grass/fluvisol` | TFCFBlocks.java:1330 | 无 |

### dense_clay_grass/mollisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `dense_clay_grass/mollisol` | TFCFBlocks.java:1330 | 无 |

### dense_clay_grass/oxisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `dense_clay_grass/oxisol` | TFCFBlocks.java:1330 | 无 |

### dense_clay_grass/podzol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `dense_clay_grass/podzol` | TFCFBlocks.java:1330 | 无 |

### dense_earthenware_clay_grass/fluvisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `dense_earthenware_clay_grass/fluvisol` | TFCFBlocks.java:1330 | 无 |

### dense_earthenware_clay_grass/mollisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `dense_earthenware_clay_grass/mollisol` | TFCFBlocks.java:1330 | 无 |

### dense_earthenware_clay_grass/oxisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `dense_earthenware_clay_grass/oxisol` | TFCFBlocks.java:1330 | 无 |

### dense_earthenware_clay_grass/podzol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `dense_earthenware_clay_grass/podzol` | TFCFBlocks.java:1330 | 无 |

### dense_grass/fluvisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `dense_grass/fluvisol` | TFCFBlocks.java:1330 | 无 |

### dense_grass/mollisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `dense_grass/mollisol` | TFCFBlocks.java:1330 | 无 |

### dense_grass/oxisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `dense_grass/oxisol` | TFCFBlocks.java:1330 | 无 |

### dense_grass/podzol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `dense_grass/podzol` | TFCFBlocks.java:1330 | 无 |

### dense_grass_path/fluvisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `dense_grass_path/fluvisol` | TFCFBlocks.java:1330 | 无 |

### dense_grass_path/mollisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `dense_grass_path/mollisol` | TFCFBlocks.java:1330 | 无 |

### dense_grass_path/oxisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `dense_grass_path/oxisol` | TFCFBlocks.java:1330 | 无 |

### dense_grass_path/podzol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `dense_grass_path/podzol` | TFCFBlocks.java:1330 | 无 |

### dense_stoneware_clay_grass/fluvisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `dense_stoneware_clay_grass/fluvisol` | TFCFBlocks.java:1330 | 无 |

### dense_stoneware_clay_grass/mollisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `dense_stoneware_clay_grass/mollisol` | TFCFBlocks.java:1330 | 无 |

### dense_stoneware_clay_grass/oxisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `dense_stoneware_clay_grass/oxisol` | TFCFBlocks.java:1330 | 无 |

### dense_stoneware_clay_grass/podzol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `dense_stoneware_clay_grass/podzol` | TFCFBlocks.java:1330 | 无 |

### dirt/fluvisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `dirt/fluvisol` | TFCFBlocks.java:1330 | 无 |

### dirt/mollisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `dirt/mollisol` | TFCFBlocks.java:1330 | 无 |

### dirt/oxisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `dirt/oxisol` | TFCFBlocks.java:1330 | 无 |

### dirt/podzol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `dirt/podzol` | TFCFBlocks.java:1330 | 无 |

### dry_clay_grass/fluvisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `dry_clay_grass/fluvisol` | TFCFBlocks.java:1330 | 无 |

### dry_clay_grass/mollisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `dry_clay_grass/mollisol` | TFCFBlocks.java:1330 | 无 |

### dry_clay_grass/oxisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `dry_clay_grass/oxisol` | TFCFBlocks.java:1330 | 无 |

### dry_clay_grass/podzol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `dry_clay_grass/podzol` | TFCFBlocks.java:1330 | 无 |

### dry_earthenware_clay_grass/fluvisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `dry_earthenware_clay_grass/fluvisol` | TFCFBlocks.java:1330 | 无 |

### dry_earthenware_clay_grass/mollisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `dry_earthenware_clay_grass/mollisol` | TFCFBlocks.java:1330 | 无 |

### dry_earthenware_clay_grass/oxisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `dry_earthenware_clay_grass/oxisol` | TFCFBlocks.java:1330 | 无 |

### dry_earthenware_clay_grass/podzol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `dry_earthenware_clay_grass/podzol` | TFCFBlocks.java:1330 | 无 |

### dry_grass/fluvisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `dry_grass/fluvisol` | TFCFBlocks.java:1330 | 无 |

### dry_grass/mollisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `dry_grass/mollisol` | TFCFBlocks.java:1330 | 无 |

### dry_grass/oxisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `dry_grass/oxisol` | TFCFBlocks.java:1330 | 无 |

### dry_grass/podzol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `dry_grass/podzol` | TFCFBlocks.java:1330 | 无 |

### dry_grass_path/fluvisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `dry_grass_path/fluvisol` | TFCFBlocks.java:1330 | 无 |

### dry_grass_path/mollisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `dry_grass_path/mollisol` | TFCFBlocks.java:1330 | 无 |

### dry_grass_path/oxisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `dry_grass_path/oxisol` | TFCFBlocks.java:1330 | 无 |

### dry_grass_path/podzol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `dry_grass_path/podzol` | TFCFBlocks.java:1330 | 无 |

### dry_stoneware_clay_grass/fluvisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `dry_stoneware_clay_grass/fluvisol` | TFCFBlocks.java:1330 | 无 |

### dry_stoneware_clay_grass/mollisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `dry_stoneware_clay_grass/mollisol` | TFCFBlocks.java:1330 | 无 |

### dry_stoneware_clay_grass/oxisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `dry_stoneware_clay_grass/oxisol` | TFCFBlocks.java:1330 | 无 |

### dry_stoneware_clay_grass/podzol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `dry_stoneware_clay_grass/podzol` | TFCFBlocks.java:1330 | 无 |

### drying_bricks/fluvisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `drying_bricks/fluvisol` | TFCFBlocks.java:1330 | 无 |

### drying_bricks/mollisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `drying_bricks/mollisol` | TFCFBlocks.java:1330 | 无 |

### drying_bricks/oxisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `drying_bricks/oxisol` | TFCFBlocks.java:1330 | 无 |

### drying_bricks/podzol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `drying_bricks/podzol` | TFCFBlocks.java:1330 | 无 |

### farmland/fluvisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `farmland/fluvisol` | TFCFBlocks.java:1330 | 无 |

### farmland/mollisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `farmland/mollisol` | TFCFBlocks.java:1330 | 无 |

### farmland/oxisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `farmland/oxisol` | TFCFBlocks.java:1330 | 无 |

### farmland/podzol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `farmland/podzol` | TFCFBlocks.java:1330 | 无 |

### grass/fluvisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `grass/fluvisol` | TFCFBlocks.java:1330 | 无 |

### grass/mollisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `grass/mollisol` | TFCFBlocks.java:1330 | 无 |

### grass/oxisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `grass/oxisol` | TFCFBlocks.java:1330 | 无 |

### grass/podzol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `grass/podzol` | TFCFBlocks.java:1330 | 无 |

### grass_path/fluvisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `grass_path/fluvisol` | TFCFBlocks.java:1330 | 无 |

### grass_path/mollisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `grass_path/mollisol` | TFCFBlocks.java:1330 | 无 |

### grass_path/oxisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `grass_path/oxisol` | TFCFBlocks.java:1330 | 无 |

### grass_path/podzol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `grass_path/podzol` | TFCFBlocks.java:1330 | 无 |

### loose_mud/fluvisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `loose_mud/fluvisol` | TFCFBlocks.java:1330 | 无 |

### loose_mud/mollisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `loose_mud/mollisol` | TFCFBlocks.java:1330 | 无 |

### loose_mud/oxisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `loose_mud/oxisol` | TFCFBlocks.java:1330 | 无 |

### loose_mud/podzol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `loose_mud/podzol` | TFCFBlocks.java:1330 | 无 |

### mossy_packed_mud/fluvisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `mossy_packed_mud/fluvisol` | TFCFBlocks.java:1330 | 无 |

### mossy_packed_mud/mollisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `mossy_packed_mud/mollisol` | TFCFBlocks.java:1330 | 无 |

### mossy_packed_mud/oxisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `mossy_packed_mud/oxisol` | TFCFBlocks.java:1330 | 无 |

### mossy_packed_mud/podzol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `mossy_packed_mud/podzol` | TFCFBlocks.java:1330 | 无 |

### mud_bricks/fluvisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `mud_bricks/fluvisol` | TFCFBlocks.java:1330 | 无 |

### mud_bricks/fluvisol_slab（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `mud_bricks/fluvisol_slab` | TFCFBlocks.java:166 | 无 |

### mud_bricks/fluvisol_stairs（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `mud_bricks/fluvisol_stairs` | TFCFBlocks.java:167 | 无 |

### mud_bricks/fluvisol_wall（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `mud_bricks/fluvisol_wall` | TFCFBlocks.java:172 | 无 |

### mud_bricks/mollisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `mud_bricks/mollisol` | TFCFBlocks.java:1330 | 无 |

### mud_bricks/mollisol_slab（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `mud_bricks/mollisol_slab` | TFCFBlocks.java:166 | 无 |

### mud_bricks/mollisol_stairs（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `mud_bricks/mollisol_stairs` | TFCFBlocks.java:167 | 无 |

### mud_bricks/mollisol_wall（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `mud_bricks/mollisol_wall` | TFCFBlocks.java:172 | 无 |

### mud_bricks/oxisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `mud_bricks/oxisol` | TFCFBlocks.java:1330 | 无 |

### mud_bricks/oxisol_slab（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `mud_bricks/oxisol_slab` | TFCFBlocks.java:166 | 无 |

### mud_bricks/oxisol_stairs（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `mud_bricks/oxisol_stairs` | TFCFBlocks.java:167 | 无 |

### mud_bricks/oxisol_wall（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `mud_bricks/oxisol_wall` | TFCFBlocks.java:172 | 无 |

### mud_bricks/podzol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `mud_bricks/podzol` | TFCFBlocks.java:1330 | 无 |

### mud_bricks/podzol_slab（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `mud_bricks/podzol_slab` | TFCFBlocks.java:166 | 无 |

### mud_bricks/podzol_stairs（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `mud_bricks/podzol_stairs` | TFCFBlocks.java:167 | 无 |

### mud_bricks/podzol_wall（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `mud_bricks/podzol_wall` | TFCFBlocks.java:172 | 无 |

### mycelium_dirt/fluvisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `mycelium_dirt/fluvisol` | TFCFBlocks.java:1330 | 无 |

### mycelium_dirt/mollisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `mycelium_dirt/mollisol` | TFCFBlocks.java:1330 | 无 |

### mycelium_dirt/oxisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `mycelium_dirt/oxisol` | TFCFBlocks.java:1330 | 无 |

### mycelium_dirt/podzol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `mycelium_dirt/podzol` | TFCFBlocks.java:1330 | 无 |

### packed_mud/fluvisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `packed_mud/fluvisol` | TFCFBlocks.java:1330 | 无 |

### packed_mud/mollisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `packed_mud/mollisol` | TFCFBlocks.java:1330 | 无 |

### packed_mud/oxisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `packed_mud/oxisol` | TFCFBlocks.java:1330 | 无 |

### packed_mud/podzol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `packed_mud/podzol` | TFCFBlocks.java:1330 | 无 |

### podzol/fluvisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `podzol/fluvisol` | TFCFBlocks.java:1330 | 无 |

### podzol/mollisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `podzol/mollisol` | TFCFBlocks.java:1330 | 无 |

### podzol/oxisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `podzol/oxisol` | TFCFBlocks.java:1330 | 无 |

### podzol/podzol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `podzol/podzol` | TFCFBlocks.java:1330 | 无 |

### rock/aqueduct（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `rock/aqueduct/tuff` | TFCFBlocks.java:3192 | 无 |

### rock/bricks（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `rock/bricks/tuff` | TFCFBlocks.java:3192 | 无 |

### rock/button（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `rock/button/tuff` | TFCFBlocks.java:3192 | 无 |

### rock/chiseled（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `rock/chiseled/tuff` | TFCFBlocks.java:3192 | 无 |

### rock/cobble（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `rock/cobble/tuff` | TFCFBlocks.java:3192 | 无 |

### rock/cracked_bricks（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `rock/cracked_bricks/tuff` | TFCFBlocks.java:3192 | 无 |

### rock/gravel（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `rock/gravel/tuff` | TFCFBlocks.java:3192 | 无 |

### rock/hardened（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `rock/hardened/tuff` | TFCFBlocks.java:3192 | 无 |

### rock/loose（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `rock/loose/tuff` | TFCFBlocks.java:3192 | 无 |

### rock/mossy_bricks（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `rock/mossy_bricks/tuff` | TFCFBlocks.java:3192 | 无 |

### rock/mossy_cobble（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `rock/mossy_cobble/tuff` | TFCFBlocks.java:3192 | 无 |

### rock/mossy_loose（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `rock/mossy_loose/tuff` | TFCFBlocks.java:3192 | 无 |

### rock/pressure_plate（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `rock/pressure_plate/tuff` | TFCFBlocks.java:3192 | 无 |

### rock/raw（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `rock/raw/tuff` | TFCFBlocks.java:3192 | 无 |

### rock/smooth（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `rock/smooth/tuff` | TFCFBlocks.java:3192 | 无 |

### rock/spike（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `rock/spike/tuff` | TFCFBlocks.java:3192 | 无 |

### rooted_dirt/fluvisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `rooted_dirt/fluvisol` | TFCFBlocks.java:1330 | 无 |

### rooted_dirt/mollisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `rooted_dirt/mollisol` | TFCFBlocks.java:1330 | 无 |

### rooted_dirt/oxisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `rooted_dirt/oxisol` | TFCFBlocks.java:1330 | 无 |

### rooted_dirt/podzol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `rooted_dirt/podzol` | TFCFBlocks.java:1330 | 无 |

### sparse_clay_grass/fluvisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `sparse_clay_grass/fluvisol` | TFCFBlocks.java:1330 | 无 |

### sparse_clay_grass/mollisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `sparse_clay_grass/mollisol` | TFCFBlocks.java:1330 | 无 |

### sparse_clay_grass/oxisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `sparse_clay_grass/oxisol` | TFCFBlocks.java:1330 | 无 |

### sparse_clay_grass/podzol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `sparse_clay_grass/podzol` | TFCFBlocks.java:1330 | 无 |

### sparse_earthenware_clay_grass/fluvisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `sparse_earthenware_clay_grass/fluvisol` | TFCFBlocks.java:1330 | 无 |

### sparse_earthenware_clay_grass/mollisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `sparse_earthenware_clay_grass/mollisol` | TFCFBlocks.java:1330 | 无 |

### sparse_earthenware_clay_grass/oxisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `sparse_earthenware_clay_grass/oxisol` | TFCFBlocks.java:1330 | 无 |

### sparse_earthenware_clay_grass/podzol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `sparse_earthenware_clay_grass/podzol` | TFCFBlocks.java:1330 | 无 |

### sparse_grass/fluvisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `sparse_grass/fluvisol` | TFCFBlocks.java:1330 | 无 |

### sparse_grass/mollisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `sparse_grass/mollisol` | TFCFBlocks.java:1330 | 无 |

### sparse_grass/oxisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `sparse_grass/oxisol` | TFCFBlocks.java:1330 | 无 |

### sparse_grass/podzol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `sparse_grass/podzol` | TFCFBlocks.java:1330 | 无 |

### sparse_grass_path/fluvisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `sparse_grass_path/fluvisol` | TFCFBlocks.java:1330 | 无 |

### sparse_grass_path/mollisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `sparse_grass_path/mollisol` | TFCFBlocks.java:1330 | 无 |

### sparse_grass_path/oxisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `sparse_grass_path/oxisol` | TFCFBlocks.java:1330 | 无 |

### sparse_grass_path/podzol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `sparse_grass_path/podzol` | TFCFBlocks.java:1330 | 无 |

### sparse_stoneware_clay_grass/fluvisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `sparse_stoneware_clay_grass/fluvisol` | TFCFBlocks.java:1330 | 无 |

### sparse_stoneware_clay_grass/mollisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `sparse_stoneware_clay_grass/mollisol` | TFCFBlocks.java:1330 | 无 |

### sparse_stoneware_clay_grass/oxisol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `sparse_stoneware_clay_grass/oxisol` | TFCFBlocks.java:1330 | 无 |

### sparse_stoneware_clay_grass/podzol（1）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `sparse_stoneware_clay_grass/podzol` | TFCFBlocks.java:1330 | 无 |

### wood/axle（11）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `wood/axle/hawthorn` | TFCFBlocks.java:1700 | 有 |
| `wood/axle/jabuticabeira` | TFCFBlocks.java:1700 | 有 |
| `wood/axle/maclura` | TFCFBlocks.java:1700 | 有 |
| `wood/axle/medlar` | TFCFBlocks.java:1700 | 有 |
| `wood/axle/mulberry` | TFCFBlocks.java:1700 | 有 |
| `wood/axle/pear` | TFCFBlocks.java:1700 | 有 |
| `wood/axle/persimmon` | TFCFBlocks.java:1700 | 有 |
| `wood/axle/quince` | TFCFBlocks.java:1700 | 有 |
| `wood/axle/sorb` | TFCFBlocks.java:1700 | 有 |
| `wood/axle/syzygium` | TFCFBlocks.java:1700 | 有 |
| `wood/axle/yew` | TFCFBlocks.java:1700 | 有 |

### wood/barrel（11）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `wood/barrel/hawthorn` | TFCFBlocks.java:1700 | 有 |
| `wood/barrel/jabuticabeira` | TFCFBlocks.java:1700 | 有 |
| `wood/barrel/maclura` | TFCFBlocks.java:1700 | 有 |
| `wood/barrel/medlar` | TFCFBlocks.java:1700 | 有 |
| `wood/barrel/mulberry` | TFCFBlocks.java:1700 | 有 |
| `wood/barrel/pear` | TFCFBlocks.java:1700 | 有 |
| `wood/barrel/persimmon` | TFCFBlocks.java:1700 | 有 |
| `wood/barrel/quince` | TFCFBlocks.java:1700 | 有 |
| `wood/barrel/sorb` | TFCFBlocks.java:1700 | 有 |
| `wood/barrel/syzygium` | TFCFBlocks.java:1700 | 有 |
| `wood/barrel/yew` | TFCFBlocks.java:1700 | 有 |

### wood/bladed_axle（11）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `wood/bladed_axle/hawthorn` | TFCFBlocks.java:1700 | 有 |
| `wood/bladed_axle/jabuticabeira` | TFCFBlocks.java:1700 | 有 |
| `wood/bladed_axle/maclura` | TFCFBlocks.java:1700 | 有 |
| `wood/bladed_axle/medlar` | TFCFBlocks.java:1700 | 有 |
| `wood/bladed_axle/mulberry` | TFCFBlocks.java:1700 | 有 |
| `wood/bladed_axle/pear` | TFCFBlocks.java:1700 | 有 |
| `wood/bladed_axle/persimmon` | TFCFBlocks.java:1700 | 有 |
| `wood/bladed_axle/quince` | TFCFBlocks.java:1700 | 有 |
| `wood/bladed_axle/sorb` | TFCFBlocks.java:1700 | 有 |
| `wood/bladed_axle/syzygium` | TFCFBlocks.java:1700 | 有 |
| `wood/bladed_axle/yew` | TFCFBlocks.java:1700 | 有 |

### wood/bookshelf（34）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `wood/bookshelf/alder` | TFCFBlocks.java:1700 | 有 |
| `wood/bookshelf/angelim` | TFCFBlocks.java:1700 | 有 |
| `wood/bookshelf/argyle_eucalyptus` | TFCFBlocks.java:1700 | 有 |
| `wood/bookshelf/bamboo` | TFCFBlocks.java:1554 | 有 |
| `wood/bookshelf/cocobolo` | TFCFBlocks.java:1700 | 有 |
| `wood/bookshelf/ghaf` | TFCFBlocks.java:1700 | 有 |
| `wood/bookshelf/ginkgo` | TFCFBlocks.java:1700 | 有 |
| `wood/bookshelf/greenheart` | TFCFBlocks.java:1700 | 有 |
| `wood/bookshelf/hawthorn` | TFCFBlocks.java:1700 | 有 |
| `wood/bookshelf/iroko` | TFCFBlocks.java:1700 | 有 |
| `wood/bookshelf/jabuticabeira` | TFCFBlocks.java:1700 | 有 |
| `wood/bookshelf/juniper` | TFCFBlocks.java:1700 | 有 |
| `wood/bookshelf/logwood` | TFCFBlocks.java:1700 | 有 |
| `wood/bookshelf/maclura` | TFCFBlocks.java:1700 | 有 |
| `wood/bookshelf/mahoe` | TFCFBlocks.java:1700 | 有 |
| `wood/bookshelf/marblewood` | TFCFBlocks.java:1700 | 有 |
| `wood/bookshelf/medlar` | TFCFBlocks.java:1700 | 有 |
| `wood/bookshelf/messmate` | TFCFBlocks.java:1700 | 有 |
| `wood/bookshelf/mulberry` | TFCFBlocks.java:1700 | 有 |
| `wood/bookshelf/pear` | TFCFBlocks.java:1700 | 有 |
| `wood/bookshelf/persimmon` | TFCFBlocks.java:1700 | 有 |
| `wood/bookshelf/poplar` | TFCFBlocks.java:1700 | 有 |
| `wood/bookshelf/purple_jacaranda` | TFCFBlocks.java:1700 | 有 |
| `wood/bookshelf/purpleheart` | TFCFBlocks.java:1700 | 有 |
| `wood/bookshelf/quince` | TFCFBlocks.java:1700 | 有 |
| `wood/bookshelf/red_cedar` | TFCFBlocks.java:1700 | 有 |
| `wood/bookshelf/red_cypress` | TFCFBlocks.java:1700 | 有 |
| `wood/bookshelf/sorb` | TFCFBlocks.java:1700 | 有 |
| `wood/bookshelf/syzygium` | TFCFBlocks.java:1700 | 有 |
| `wood/bookshelf/wenge` | TFCFBlocks.java:1700 | 有 |
| `wood/bookshelf/white_jacaranda` | TFCFBlocks.java:1700 | 有 |
| `wood/bookshelf/yellow_meranti` | TFCFBlocks.java:1700 | 有 |
| `wood/bookshelf/yew` | TFCFBlocks.java:1700 | 有 |
| `wood/bookshelf/zebrawood` | TFCFBlocks.java:1700 | 有 |

### wood/button（11）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `wood/button/hawthorn` | TFCFBlocks.java:1700 | 有 |
| `wood/button/jabuticabeira` | TFCFBlocks.java:1700 | 有 |
| `wood/button/maclura` | TFCFBlocks.java:1700 | 有 |
| `wood/button/medlar` | TFCFBlocks.java:1700 | 有 |
| `wood/button/mulberry` | TFCFBlocks.java:1700 | 有 |
| `wood/button/pear` | TFCFBlocks.java:1700 | 有 |
| `wood/button/persimmon` | TFCFBlocks.java:1700 | 有 |
| `wood/button/quince` | TFCFBlocks.java:1700 | 有 |
| `wood/button/sorb` | TFCFBlocks.java:1700 | 有 |
| `wood/button/syzygium` | TFCFBlocks.java:1700 | 有 |
| `wood/button/yew` | TFCFBlocks.java:1700 | 有 |

### wood/chest（11）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `wood/chest/hawthorn` | TFCFBlocks.java:1700 | 有 |
| `wood/chest/jabuticabeira` | TFCFBlocks.java:1700 | 有 |
| `wood/chest/maclura` | TFCFBlocks.java:1700 | 有 |
| `wood/chest/medlar` | TFCFBlocks.java:1700 | 有 |
| `wood/chest/mulberry` | TFCFBlocks.java:1700 | 有 |
| `wood/chest/pear` | TFCFBlocks.java:1700 | 有 |
| `wood/chest/persimmon` | TFCFBlocks.java:1700 | 有 |
| `wood/chest/quince` | TFCFBlocks.java:1700 | 有 |
| `wood/chest/sorb` | TFCFBlocks.java:1700 | 有 |
| `wood/chest/syzygium` | TFCFBlocks.java:1700 | 有 |
| `wood/chest/yew` | TFCFBlocks.java:1700 | 有 |

### wood/clutch（11）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `wood/clutch/hawthorn` | TFCFBlocks.java:1700 | 有 |
| `wood/clutch/jabuticabeira` | TFCFBlocks.java:1700 | 有 |
| `wood/clutch/maclura` | TFCFBlocks.java:1700 | 有 |
| `wood/clutch/medlar` | TFCFBlocks.java:1700 | 有 |
| `wood/clutch/mulberry` | TFCFBlocks.java:1700 | 有 |
| `wood/clutch/pear` | TFCFBlocks.java:1700 | 有 |
| `wood/clutch/persimmon` | TFCFBlocks.java:1700 | 有 |
| `wood/clutch/quince` | TFCFBlocks.java:1700 | 有 |
| `wood/clutch/sorb` | TFCFBlocks.java:1700 | 有 |
| `wood/clutch/syzygium` | TFCFBlocks.java:1700 | 有 |
| `wood/clutch/yew` | TFCFBlocks.java:1700 | 有 |

### wood/door（11）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `wood/door/hawthorn` | TFCFBlocks.java:1700 | 有 |
| `wood/door/jabuticabeira` | TFCFBlocks.java:1700 | 有 |
| `wood/door/maclura` | TFCFBlocks.java:1700 | 有 |
| `wood/door/medlar` | TFCFBlocks.java:1700 | 有 |
| `wood/door/mulberry` | TFCFBlocks.java:1700 | 有 |
| `wood/door/pear` | TFCFBlocks.java:1700 | 有 |
| `wood/door/persimmon` | TFCFBlocks.java:1700 | 有 |
| `wood/door/quince` | TFCFBlocks.java:1700 | 有 |
| `wood/door/sorb` | TFCFBlocks.java:1700 | 有 |
| `wood/door/syzygium` | TFCFBlocks.java:1700 | 有 |
| `wood/door/yew` | TFCFBlocks.java:1700 | 有 |

### wood/encased_axle（11）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `wood/encased_axle/hawthorn` | TFCFBlocks.java:1700 | 有 |
| `wood/encased_axle/jabuticabeira` | TFCFBlocks.java:1700 | 有 |
| `wood/encased_axle/maclura` | TFCFBlocks.java:1700 | 有 |
| `wood/encased_axle/medlar` | TFCFBlocks.java:1700 | 有 |
| `wood/encased_axle/mulberry` | TFCFBlocks.java:1700 | 有 |
| `wood/encased_axle/pear` | TFCFBlocks.java:1700 | 有 |
| `wood/encased_axle/persimmon` | TFCFBlocks.java:1700 | 有 |
| `wood/encased_axle/quince` | TFCFBlocks.java:1700 | 有 |
| `wood/encased_axle/sorb` | TFCFBlocks.java:1700 | 有 |
| `wood/encased_axle/syzygium` | TFCFBlocks.java:1700 | 有 |
| `wood/encased_axle/yew` | TFCFBlocks.java:1700 | 有 |

### wood/fallen_leaves（11）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `wood/fallen_leaves/hawthorn` | TFCFBlocks.java:1700 | 有 |
| `wood/fallen_leaves/jabuticabeira` | TFCFBlocks.java:1700 | 有 |
| `wood/fallen_leaves/maclura` | TFCFBlocks.java:1700 | 有 |
| `wood/fallen_leaves/medlar` | TFCFBlocks.java:1700 | 有 |
| `wood/fallen_leaves/mulberry` | TFCFBlocks.java:1700 | 有 |
| `wood/fallen_leaves/pear` | TFCFBlocks.java:1700 | 有 |
| `wood/fallen_leaves/persimmon` | TFCFBlocks.java:1700 | 有 |
| `wood/fallen_leaves/quince` | TFCFBlocks.java:1700 | 有 |
| `wood/fallen_leaves/sorb` | TFCFBlocks.java:1700 | 有 |
| `wood/fallen_leaves/syzygium` | TFCFBlocks.java:1700 | 有 |
| `wood/fallen_leaves/yew` | TFCFBlocks.java:1700 | 有 |

### wood/fence（11）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `wood/fence/hawthorn` | TFCFBlocks.java:1700 | 有 |
| `wood/fence/jabuticabeira` | TFCFBlocks.java:1700 | 有 |
| `wood/fence/maclura` | TFCFBlocks.java:1700 | 有 |
| `wood/fence/medlar` | TFCFBlocks.java:1700 | 有 |
| `wood/fence/mulberry` | TFCFBlocks.java:1700 | 有 |
| `wood/fence/pear` | TFCFBlocks.java:1700 | 有 |
| `wood/fence/persimmon` | TFCFBlocks.java:1700 | 有 |
| `wood/fence/quince` | TFCFBlocks.java:1700 | 有 |
| `wood/fence/sorb` | TFCFBlocks.java:1700 | 有 |
| `wood/fence/syzygium` | TFCFBlocks.java:1700 | 有 |
| `wood/fence/yew` | TFCFBlocks.java:1700 | 有 |

### wood/fence_gate（11）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `wood/fence_gate/hawthorn` | TFCFBlocks.java:1700 | 有 |
| `wood/fence_gate/jabuticabeira` | TFCFBlocks.java:1700 | 有 |
| `wood/fence_gate/maclura` | TFCFBlocks.java:1700 | 有 |
| `wood/fence_gate/medlar` | TFCFBlocks.java:1700 | 有 |
| `wood/fence_gate/mulberry` | TFCFBlocks.java:1700 | 有 |
| `wood/fence_gate/pear` | TFCFBlocks.java:1700 | 有 |
| `wood/fence_gate/persimmon` | TFCFBlocks.java:1700 | 有 |
| `wood/fence_gate/quince` | TFCFBlocks.java:1700 | 有 |
| `wood/fence_gate/sorb` | TFCFBlocks.java:1700 | 有 |
| `wood/fence_gate/syzygium` | TFCFBlocks.java:1700 | 有 |
| `wood/fence_gate/yew` | TFCFBlocks.java:1700 | 有 |

### wood/gear_box（11）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `wood/gear_box/hawthorn` | TFCFBlocks.java:1700 | 有 |
| `wood/gear_box/jabuticabeira` | TFCFBlocks.java:1700 | 有 |
| `wood/gear_box/maclura` | TFCFBlocks.java:1700 | 有 |
| `wood/gear_box/medlar` | TFCFBlocks.java:1700 | 有 |
| `wood/gear_box/mulberry` | TFCFBlocks.java:1700 | 有 |
| `wood/gear_box/pear` | TFCFBlocks.java:1700 | 有 |
| `wood/gear_box/persimmon` | TFCFBlocks.java:1700 | 有 |
| `wood/gear_box/quince` | TFCFBlocks.java:1700 | 有 |
| `wood/gear_box/sorb` | TFCFBlocks.java:1700 | 有 |
| `wood/gear_box/syzygium` | TFCFBlocks.java:1700 | 有 |
| `wood/gear_box/yew` | TFCFBlocks.java:1700 | 有 |

### wood/horizontal_support（11）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `wood/horizontal_support/hawthorn` | TFCFBlocks.java:1700 | 有 |
| `wood/horizontal_support/jabuticabeira` | TFCFBlocks.java:1700 | 有 |
| `wood/horizontal_support/maclura` | TFCFBlocks.java:1700 | 有 |
| `wood/horizontal_support/medlar` | TFCFBlocks.java:1700 | 有 |
| `wood/horizontal_support/mulberry` | TFCFBlocks.java:1700 | 有 |
| `wood/horizontal_support/pear` | TFCFBlocks.java:1700 | 有 |
| `wood/horizontal_support/persimmon` | TFCFBlocks.java:1700 | 有 |
| `wood/horizontal_support/quince` | TFCFBlocks.java:1700 | 有 |
| `wood/horizontal_support/sorb` | TFCFBlocks.java:1700 | 有 |
| `wood/horizontal_support/syzygium` | TFCFBlocks.java:1700 | 有 |
| `wood/horizontal_support/yew` | TFCFBlocks.java:1700 | 有 |

### wood/jar_shelf（11）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `wood/jar_shelf/hawthorn` | TFCFBlocks.java:1700 | 有 |
| `wood/jar_shelf/jabuticabeira` | TFCFBlocks.java:1700 | 有 |
| `wood/jar_shelf/maclura` | TFCFBlocks.java:1700 | 有 |
| `wood/jar_shelf/medlar` | TFCFBlocks.java:1700 | 有 |
| `wood/jar_shelf/mulberry` | TFCFBlocks.java:1700 | 有 |
| `wood/jar_shelf/pear` | TFCFBlocks.java:1700 | 有 |
| `wood/jar_shelf/persimmon` | TFCFBlocks.java:1700 | 有 |
| `wood/jar_shelf/quince` | TFCFBlocks.java:1700 | 有 |
| `wood/jar_shelf/sorb` | TFCFBlocks.java:1700 | 有 |
| `wood/jar_shelf/syzygium` | TFCFBlocks.java:1700 | 有 |
| `wood/jar_shelf/yew` | TFCFBlocks.java:1700 | 有 |

### wood/leaves（11）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `wood/leaves/hawthorn` | TFCFBlocks.java:1612 | 有 |
| `wood/leaves/jabuticabeira` | TFCFBlocks.java:1612 | 有 |
| `wood/leaves/maclura` | TFCFBlocks.java:1612 | 有 |
| `wood/leaves/medlar` | TFCFBlocks.java:1612 | 有 |
| `wood/leaves/mulberry` | TFCFBlocks.java:1612 | 有 |
| `wood/leaves/pear` | TFCFBlocks.java:1612 | 有 |
| `wood/leaves/persimmon` | TFCFBlocks.java:1612 | 有 |
| `wood/leaves/quince` | TFCFBlocks.java:1612 | 有 |
| `wood/leaves/sorb` | TFCFBlocks.java:1612 | 有 |
| `wood/leaves/syzygium` | TFCFBlocks.java:1612 | 有 |
| `wood/leaves/yew` | TFCFBlocks.java:1612 | 有 |

### wood/lectern（11）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `wood/lectern/hawthorn` | TFCFBlocks.java:1665 | 有 |
| `wood/lectern/jabuticabeira` | TFCFBlocks.java:1665 | 有 |
| `wood/lectern/maclura` | TFCFBlocks.java:1665 | 有 |
| `wood/lectern/medlar` | TFCFBlocks.java:1665 | 有 |
| `wood/lectern/mulberry` | TFCFBlocks.java:1665 | 有 |
| `wood/lectern/pear` | TFCFBlocks.java:1665 | 有 |
| `wood/lectern/persimmon` | TFCFBlocks.java:1665 | 有 |
| `wood/lectern/quince` | TFCFBlocks.java:1665 | 有 |
| `wood/lectern/sorb` | TFCFBlocks.java:1665 | 有 |
| `wood/lectern/syzygium` | TFCFBlocks.java:1665 | 有 |
| `wood/lectern/yew` | TFCFBlocks.java:1665 | 有 |

### wood/log（11）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `wood/log/hawthorn` | TFCFBlocks.java:1700 | 有 |
| `wood/log/jabuticabeira` | TFCFBlocks.java:1700 | 有 |
| `wood/log/maclura` | TFCFBlocks.java:1700 | 有 |
| `wood/log/medlar` | TFCFBlocks.java:1700 | 有 |
| `wood/log/mulberry` | TFCFBlocks.java:1700 | 有 |
| `wood/log/pear` | TFCFBlocks.java:1700 | 有 |
| `wood/log/persimmon` | TFCFBlocks.java:1700 | 有 |
| `wood/log/quince` | TFCFBlocks.java:1700 | 有 |
| `wood/log/sorb` | TFCFBlocks.java:1700 | 有 |
| `wood/log/syzygium` | TFCFBlocks.java:1700 | 有 |
| `wood/log/yew` | TFCFBlocks.java:1700 | 有 |

### wood/log_fence（11）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `wood/log_fence/hawthorn` | TFCFBlocks.java:1700 | 有 |
| `wood/log_fence/jabuticabeira` | TFCFBlocks.java:1700 | 有 |
| `wood/log_fence/maclura` | TFCFBlocks.java:1700 | 有 |
| `wood/log_fence/medlar` | TFCFBlocks.java:1700 | 有 |
| `wood/log_fence/mulberry` | TFCFBlocks.java:1700 | 有 |
| `wood/log_fence/pear` | TFCFBlocks.java:1700 | 有 |
| `wood/log_fence/persimmon` | TFCFBlocks.java:1700 | 有 |
| `wood/log_fence/quince` | TFCFBlocks.java:1700 | 有 |
| `wood/log_fence/sorb` | TFCFBlocks.java:1700 | 有 |
| `wood/log_fence/syzygium` | TFCFBlocks.java:1700 | 有 |
| `wood/log_fence/yew` | TFCFBlocks.java:1700 | 有 |

### wood/log_wall（11）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `wood/log_wall/hawthorn` | TFCFBlocks.java:2026 | 有 |
| `wood/log_wall/jabuticabeira` | TFCFBlocks.java:2026 | 有 |
| `wood/log_wall/maclura` | TFCFBlocks.java:2026 | 有 |
| `wood/log_wall/medlar` | TFCFBlocks.java:2026 | 有 |
| `wood/log_wall/mulberry` | TFCFBlocks.java:2026 | 有 |
| `wood/log_wall/pear` | TFCFBlocks.java:2026 | 有 |
| `wood/log_wall/persimmon` | TFCFBlocks.java:2026 | 有 |
| `wood/log_wall/quince` | TFCFBlocks.java:2026 | 有 |
| `wood/log_wall/sorb` | TFCFBlocks.java:2026 | 有 |
| `wood/log_wall/syzygium` | TFCFBlocks.java:2026 | 有 |
| `wood/log_wall/yew` | TFCFBlocks.java:2026 | 有 |

### wood/loom（11）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `wood/loom/hawthorn` | TFCFBlocks.java:1700 | 有 |
| `wood/loom/jabuticabeira` | TFCFBlocks.java:1700 | 有 |
| `wood/loom/maclura` | TFCFBlocks.java:1700 | 有 |
| `wood/loom/medlar` | TFCFBlocks.java:1700 | 有 |
| `wood/loom/mulberry` | TFCFBlocks.java:1700 | 有 |
| `wood/loom/pear` | TFCFBlocks.java:1700 | 有 |
| `wood/loom/persimmon` | TFCFBlocks.java:1700 | 有 |
| `wood/loom/quince` | TFCFBlocks.java:1700 | 有 |
| `wood/loom/sorb` | TFCFBlocks.java:1700 | 有 |
| `wood/loom/syzygium` | TFCFBlocks.java:1700 | 有 |
| `wood/loom/yew` | TFCFBlocks.java:1700 | 有 |

### wood/planks（22）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `wood/planks/hawthorn` | TFCFBlocks.java:1700 | 有 |
| `wood/planks/hawthorn_bookshelf` | TFCFBlocks.java:1685 | 无 |
| `wood/planks/jabuticabeira` | TFCFBlocks.java:1700 | 有 |
| `wood/planks/jabuticabeira_bookshelf` | TFCFBlocks.java:1685 | 无 |
| `wood/planks/maclura` | TFCFBlocks.java:1700 | 有 |
| `wood/planks/maclura_bookshelf` | TFCFBlocks.java:1685 | 无 |
| `wood/planks/medlar` | TFCFBlocks.java:1700 | 有 |
| `wood/planks/medlar_bookshelf` | TFCFBlocks.java:1685 | 无 |
| `wood/planks/mulberry` | TFCFBlocks.java:1700 | 有 |
| `wood/planks/mulberry_bookshelf` | TFCFBlocks.java:1685 | 无 |
| `wood/planks/pear` | TFCFBlocks.java:1700 | 有 |
| `wood/planks/pear_bookshelf` | TFCFBlocks.java:1685 | 无 |
| `wood/planks/persimmon` | TFCFBlocks.java:1700 | 有 |
| `wood/planks/persimmon_bookshelf` | TFCFBlocks.java:1685 | 无 |
| `wood/planks/quince` | TFCFBlocks.java:1700 | 有 |
| `wood/planks/quince_bookshelf` | TFCFBlocks.java:1685 | 无 |
| `wood/planks/sorb` | TFCFBlocks.java:1700 | 有 |
| `wood/planks/sorb_bookshelf` | TFCFBlocks.java:1685 | 无 |
| `wood/planks/syzygium` | TFCFBlocks.java:1700 | 有 |
| `wood/planks/syzygium_bookshelf` | TFCFBlocks.java:1685 | 无 |
| `wood/planks/yew` | TFCFBlocks.java:1700 | 有 |
| `wood/planks/yew_bookshelf` | TFCFBlocks.java:1685 | 无 |

### wood/potted_sapling（11）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `wood/potted_sapling/hawthorn` | TFCFBlocks.java:1700 | 有 |
| `wood/potted_sapling/jabuticabeira` | TFCFBlocks.java:1700 | 有 |
| `wood/potted_sapling/maclura` | TFCFBlocks.java:1700 | 有 |
| `wood/potted_sapling/medlar` | TFCFBlocks.java:1700 | 有 |
| `wood/potted_sapling/mulberry` | TFCFBlocks.java:1700 | 有 |
| `wood/potted_sapling/pear` | TFCFBlocks.java:1700 | 有 |
| `wood/potted_sapling/persimmon` | TFCFBlocks.java:1700 | 有 |
| `wood/potted_sapling/quince` | TFCFBlocks.java:1700 | 有 |
| `wood/potted_sapling/sorb` | TFCFBlocks.java:1700 | 有 |
| `wood/potted_sapling/syzygium` | TFCFBlocks.java:1700 | 有 |
| `wood/potted_sapling/yew` | TFCFBlocks.java:1700 | 有 |

### wood/pressure_plate（11）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `wood/pressure_plate/hawthorn` | TFCFBlocks.java:1700 | 有 |
| `wood/pressure_plate/jabuticabeira` | TFCFBlocks.java:1700 | 有 |
| `wood/pressure_plate/maclura` | TFCFBlocks.java:1700 | 有 |
| `wood/pressure_plate/medlar` | TFCFBlocks.java:1700 | 有 |
| `wood/pressure_plate/mulberry` | TFCFBlocks.java:1700 | 有 |
| `wood/pressure_plate/pear` | TFCFBlocks.java:1700 | 有 |
| `wood/pressure_plate/persimmon` | TFCFBlocks.java:1700 | 有 |
| `wood/pressure_plate/quince` | TFCFBlocks.java:1700 | 有 |
| `wood/pressure_plate/sorb` | TFCFBlocks.java:1700 | 有 |
| `wood/pressure_plate/syzygium` | TFCFBlocks.java:1700 | 有 |
| `wood/pressure_plate/yew` | TFCFBlocks.java:1700 | 有 |

### wood/sapling（11）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `wood/sapling/hawthorn` | TFCFBlocks.java:1588 | 有 |
| `wood/sapling/jabuticabeira` | TFCFBlocks.java:1588 | 有 |
| `wood/sapling/maclura` | TFCFBlocks.java:1588 | 有 |
| `wood/sapling/medlar` | TFCFBlocks.java:1588 | 有 |
| `wood/sapling/mulberry` | TFCFBlocks.java:1588 | 有 |
| `wood/sapling/pear` | TFCFBlocks.java:1588 | 有 |
| `wood/sapling/persimmon` | TFCFBlocks.java:1588 | 有 |
| `wood/sapling/quince` | TFCFBlocks.java:1588 | 有 |
| `wood/sapling/sorb` | TFCFBlocks.java:1588 | 有 |
| `wood/sapling/syzygium` | TFCFBlocks.java:1588 | 有 |
| `wood/sapling/yew` | TFCFBlocks.java:1588 | 有 |

### wood/scribing_table（11）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `wood/scribing_table/hawthorn` | TFCFBlocks.java:1700 | 有 |
| `wood/scribing_table/jabuticabeira` | TFCFBlocks.java:1700 | 有 |
| `wood/scribing_table/maclura` | TFCFBlocks.java:1700 | 有 |
| `wood/scribing_table/medlar` | TFCFBlocks.java:1700 | 有 |
| `wood/scribing_table/mulberry` | TFCFBlocks.java:1700 | 有 |
| `wood/scribing_table/pear` | TFCFBlocks.java:1700 | 有 |
| `wood/scribing_table/persimmon` | TFCFBlocks.java:1700 | 有 |
| `wood/scribing_table/quince` | TFCFBlocks.java:1700 | 有 |
| `wood/scribing_table/sorb` | TFCFBlocks.java:1700 | 有 |
| `wood/scribing_table/syzygium` | TFCFBlocks.java:1700 | 有 |
| `wood/scribing_table/yew` | TFCFBlocks.java:1700 | 有 |

### wood/sewing_table（11）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `wood/sewing_table/hawthorn` | TFCFBlocks.java:1700 | 有 |
| `wood/sewing_table/jabuticabeira` | TFCFBlocks.java:1700 | 有 |
| `wood/sewing_table/maclura` | TFCFBlocks.java:1700 | 有 |
| `wood/sewing_table/medlar` | TFCFBlocks.java:1700 | 有 |
| `wood/sewing_table/mulberry` | TFCFBlocks.java:1700 | 有 |
| `wood/sewing_table/pear` | TFCFBlocks.java:1700 | 有 |
| `wood/sewing_table/persimmon` | TFCFBlocks.java:1700 | 有 |
| `wood/sewing_table/quince` | TFCFBlocks.java:1700 | 有 |
| `wood/sewing_table/sorb` | TFCFBlocks.java:1700 | 有 |
| `wood/sewing_table/syzygium` | TFCFBlocks.java:1700 | 有 |
| `wood/sewing_table/yew` | TFCFBlocks.java:1700 | 有 |

### wood/sign（11）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `wood/sign/hawthorn` | TFCFBlocks.java:1634 | 有 |
| `wood/sign/jabuticabeira` | TFCFBlocks.java:1634 | 有 |
| `wood/sign/maclura` | TFCFBlocks.java:1634 | 有 |
| `wood/sign/medlar` | TFCFBlocks.java:1634 | 有 |
| `wood/sign/mulberry` | TFCFBlocks.java:1634 | 有 |
| `wood/sign/pear` | TFCFBlocks.java:1634 | 有 |
| `wood/sign/persimmon` | TFCFBlocks.java:1634 | 有 |
| `wood/sign/quince` | TFCFBlocks.java:1634 | 有 |
| `wood/sign/sorb` | TFCFBlocks.java:1634 | 有 |
| `wood/sign/syzygium` | TFCFBlocks.java:1634 | 有 |
| `wood/sign/yew` | TFCFBlocks.java:1634 | 有 |

### wood/slab（11）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `wood/slab/hawthorn` | TFCFBlocks.java:1700 | 有 |
| `wood/slab/jabuticabeira` | TFCFBlocks.java:1700 | 有 |
| `wood/slab/maclura` | TFCFBlocks.java:1700 | 有 |
| `wood/slab/medlar` | TFCFBlocks.java:1700 | 有 |
| `wood/slab/mulberry` | TFCFBlocks.java:1700 | 有 |
| `wood/slab/pear` | TFCFBlocks.java:1700 | 有 |
| `wood/slab/persimmon` | TFCFBlocks.java:1700 | 有 |
| `wood/slab/quince` | TFCFBlocks.java:1700 | 有 |
| `wood/slab/sorb` | TFCFBlocks.java:1700 | 有 |
| `wood/slab/syzygium` | TFCFBlocks.java:1700 | 有 |
| `wood/slab/yew` | TFCFBlocks.java:1700 | 有 |

### wood/sluice（11）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `wood/sluice/hawthorn` | TFCFBlocks.java:1700 | 有 |
| `wood/sluice/jabuticabeira` | TFCFBlocks.java:1700 | 有 |
| `wood/sluice/maclura` | TFCFBlocks.java:1700 | 有 |
| `wood/sluice/medlar` | TFCFBlocks.java:1700 | 有 |
| `wood/sluice/mulberry` | TFCFBlocks.java:1700 | 有 |
| `wood/sluice/pear` | TFCFBlocks.java:1700 | 有 |
| `wood/sluice/persimmon` | TFCFBlocks.java:1700 | 有 |
| `wood/sluice/quince` | TFCFBlocks.java:1700 | 有 |
| `wood/sluice/sorb` | TFCFBlocks.java:1700 | 有 |
| `wood/sluice/syzygium` | TFCFBlocks.java:1700 | 有 |
| `wood/sluice/yew` | TFCFBlocks.java:1700 | 有 |

### wood/stairs（11）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `wood/stairs/hawthorn` | TFCFBlocks.java:1700 | 有 |
| `wood/stairs/jabuticabeira` | TFCFBlocks.java:1700 | 有 |
| `wood/stairs/maclura` | TFCFBlocks.java:1700 | 有 |
| `wood/stairs/medlar` | TFCFBlocks.java:1700 | 有 |
| `wood/stairs/mulberry` | TFCFBlocks.java:1700 | 有 |
| `wood/stairs/pear` | TFCFBlocks.java:1700 | 有 |
| `wood/stairs/persimmon` | TFCFBlocks.java:1700 | 有 |
| `wood/stairs/quince` | TFCFBlocks.java:1700 | 有 |
| `wood/stairs/sorb` | TFCFBlocks.java:1700 | 有 |
| `wood/stairs/syzygium` | TFCFBlocks.java:1700 | 有 |
| `wood/stairs/yew` | TFCFBlocks.java:1700 | 有 |

### wood/stripped_log（11）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `wood/stripped_log/hawthorn` | TFCFBlocks.java:1700 | 有 |
| `wood/stripped_log/jabuticabeira` | TFCFBlocks.java:1700 | 有 |
| `wood/stripped_log/maclura` | TFCFBlocks.java:1700 | 有 |
| `wood/stripped_log/medlar` | TFCFBlocks.java:1700 | 有 |
| `wood/stripped_log/mulberry` | TFCFBlocks.java:1700 | 有 |
| `wood/stripped_log/pear` | TFCFBlocks.java:1700 | 有 |
| `wood/stripped_log/persimmon` | TFCFBlocks.java:1700 | 有 |
| `wood/stripped_log/quince` | TFCFBlocks.java:1700 | 有 |
| `wood/stripped_log/sorb` | TFCFBlocks.java:1700 | 有 |
| `wood/stripped_log/syzygium` | TFCFBlocks.java:1700 | 有 |
| `wood/stripped_log/yew` | TFCFBlocks.java:1700 | 有 |

### wood/stripped_wood（11）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `wood/stripped_wood/hawthorn` | TFCFBlocks.java:1700 | 有 |
| `wood/stripped_wood/jabuticabeira` | TFCFBlocks.java:1700 | 有 |
| `wood/stripped_wood/maclura` | TFCFBlocks.java:1700 | 有 |
| `wood/stripped_wood/medlar` | TFCFBlocks.java:1700 | 有 |
| `wood/stripped_wood/mulberry` | TFCFBlocks.java:1700 | 有 |
| `wood/stripped_wood/pear` | TFCFBlocks.java:1700 | 有 |
| `wood/stripped_wood/persimmon` | TFCFBlocks.java:1700 | 有 |
| `wood/stripped_wood/quince` | TFCFBlocks.java:1700 | 有 |
| `wood/stripped_wood/sorb` | TFCFBlocks.java:1700 | 有 |
| `wood/stripped_wood/syzygium` | TFCFBlocks.java:1700 | 有 |
| `wood/stripped_wood/yew` | TFCFBlocks.java:1700 | 有 |

### wood/tool_rack（11）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `wood/tool_rack/hawthorn` | TFCFBlocks.java:1700 | 有 |
| `wood/tool_rack/jabuticabeira` | TFCFBlocks.java:1700 | 有 |
| `wood/tool_rack/maclura` | TFCFBlocks.java:1700 | 有 |
| `wood/tool_rack/medlar` | TFCFBlocks.java:1700 | 有 |
| `wood/tool_rack/mulberry` | TFCFBlocks.java:1700 | 有 |
| `wood/tool_rack/pear` | TFCFBlocks.java:1700 | 有 |
| `wood/tool_rack/persimmon` | TFCFBlocks.java:1700 | 有 |
| `wood/tool_rack/quince` | TFCFBlocks.java:1700 | 有 |
| `wood/tool_rack/sorb` | TFCFBlocks.java:1700 | 有 |
| `wood/tool_rack/syzygium` | TFCFBlocks.java:1700 | 有 |
| `wood/tool_rack/yew` | TFCFBlocks.java:1700 | 有 |

### wood/trapdoor（11）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `wood/trapdoor/hawthorn` | TFCFBlocks.java:1700 | 有 |
| `wood/trapdoor/jabuticabeira` | TFCFBlocks.java:1700 | 有 |
| `wood/trapdoor/maclura` | TFCFBlocks.java:1700 | 有 |
| `wood/trapdoor/medlar` | TFCFBlocks.java:1700 | 有 |
| `wood/trapdoor/mulberry` | TFCFBlocks.java:1700 | 有 |
| `wood/trapdoor/pear` | TFCFBlocks.java:1700 | 有 |
| `wood/trapdoor/persimmon` | TFCFBlocks.java:1700 | 有 |
| `wood/trapdoor/quince` | TFCFBlocks.java:1700 | 有 |
| `wood/trapdoor/sorb` | TFCFBlocks.java:1700 | 有 |
| `wood/trapdoor/syzygium` | TFCFBlocks.java:1700 | 有 |
| `wood/trapdoor/yew` | TFCFBlocks.java:1700 | 有 |

### wood/trapped_chest（11）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `wood/trapped_chest/hawthorn` | TFCFBlocks.java:1700 | 有 |
| `wood/trapped_chest/jabuticabeira` | TFCFBlocks.java:1700 | 有 |
| `wood/trapped_chest/maclura` | TFCFBlocks.java:1700 | 有 |
| `wood/trapped_chest/medlar` | TFCFBlocks.java:1700 | 有 |
| `wood/trapped_chest/mulberry` | TFCFBlocks.java:1700 | 有 |
| `wood/trapped_chest/pear` | TFCFBlocks.java:1700 | 有 |
| `wood/trapped_chest/persimmon` | TFCFBlocks.java:1700 | 有 |
| `wood/trapped_chest/quince` | TFCFBlocks.java:1700 | 有 |
| `wood/trapped_chest/sorb` | TFCFBlocks.java:1700 | 有 |
| `wood/trapped_chest/syzygium` | TFCFBlocks.java:1700 | 有 |
| `wood/trapped_chest/yew` | TFCFBlocks.java:1700 | 有 |

### wood/twig（11）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `wood/twig/hawthorn` | TFCFBlocks.java:1700 | 有 |
| `wood/twig/jabuticabeira` | TFCFBlocks.java:1700 | 有 |
| `wood/twig/maclura` | TFCFBlocks.java:1700 | 有 |
| `wood/twig/medlar` | TFCFBlocks.java:1700 | 有 |
| `wood/twig/mulberry` | TFCFBlocks.java:1700 | 有 |
| `wood/twig/pear` | TFCFBlocks.java:1700 | 有 |
| `wood/twig/persimmon` | TFCFBlocks.java:1700 | 有 |
| `wood/twig/quince` | TFCFBlocks.java:1700 | 有 |
| `wood/twig/sorb` | TFCFBlocks.java:1700 | 有 |
| `wood/twig/syzygium` | TFCFBlocks.java:1700 | 有 |
| `wood/twig/yew` | TFCFBlocks.java:1700 | 有 |

### wood/vertical_support（11）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `wood/vertical_support/hawthorn` | TFCFBlocks.java:1700 | 有 |
| `wood/vertical_support/jabuticabeira` | TFCFBlocks.java:1700 | 有 |
| `wood/vertical_support/maclura` | TFCFBlocks.java:1700 | 有 |
| `wood/vertical_support/medlar` | TFCFBlocks.java:1700 | 有 |
| `wood/vertical_support/mulberry` | TFCFBlocks.java:1700 | 有 |
| `wood/vertical_support/pear` | TFCFBlocks.java:1700 | 有 |
| `wood/vertical_support/persimmon` | TFCFBlocks.java:1700 | 有 |
| `wood/vertical_support/quince` | TFCFBlocks.java:1700 | 有 |
| `wood/vertical_support/sorb` | TFCFBlocks.java:1700 | 有 |
| `wood/vertical_support/syzygium` | TFCFBlocks.java:1700 | 有 |
| `wood/vertical_support/yew` | TFCFBlocks.java:1700 | 有 |

### wood/wall_sign（11）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `wood/wall_sign/hawthorn` | TFCFBlocks.java:1649 | 有 |
| `wood/wall_sign/jabuticabeira` | TFCFBlocks.java:1649 | 有 |
| `wood/wall_sign/maclura` | TFCFBlocks.java:1649 | 有 |
| `wood/wall_sign/medlar` | TFCFBlocks.java:1649 | 有 |
| `wood/wall_sign/mulberry` | TFCFBlocks.java:1649 | 有 |
| `wood/wall_sign/pear` | TFCFBlocks.java:1649 | 有 |
| `wood/wall_sign/persimmon` | TFCFBlocks.java:1649 | 有 |
| `wood/wall_sign/quince` | TFCFBlocks.java:1649 | 有 |
| `wood/wall_sign/sorb` | TFCFBlocks.java:1649 | 有 |
| `wood/wall_sign/syzygium` | TFCFBlocks.java:1649 | 有 |
| `wood/wall_sign/yew` | TFCFBlocks.java:1649 | 有 |

### wood/water_wheel（11）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `wood/water_wheel/hawthorn` | TFCFBlocks.java:1700 | 有 |
| `wood/water_wheel/jabuticabeira` | TFCFBlocks.java:1700 | 有 |
| `wood/water_wheel/maclura` | TFCFBlocks.java:1700 | 有 |
| `wood/water_wheel/medlar` | TFCFBlocks.java:1700 | 有 |
| `wood/water_wheel/mulberry` | TFCFBlocks.java:1700 | 有 |
| `wood/water_wheel/pear` | TFCFBlocks.java:1700 | 有 |
| `wood/water_wheel/persimmon` | TFCFBlocks.java:1700 | 有 |
| `wood/water_wheel/quince` | TFCFBlocks.java:1700 | 有 |
| `wood/water_wheel/sorb` | TFCFBlocks.java:1700 | 有 |
| `wood/water_wheel/syzygium` | TFCFBlocks.java:1700 | 有 |
| `wood/water_wheel/yew` | TFCFBlocks.java:1700 | 有 |

### wood/windmill（11）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `wood/windmill/hawthorn` | TFCFBlocks.java:1700 | 有 |
| `wood/windmill/jabuticabeira` | TFCFBlocks.java:1700 | 有 |
| `wood/windmill/maclura` | TFCFBlocks.java:1700 | 有 |
| `wood/windmill/medlar` | TFCFBlocks.java:1700 | 有 |
| `wood/windmill/mulberry` | TFCFBlocks.java:1700 | 有 |
| `wood/windmill/pear` | TFCFBlocks.java:1700 | 有 |
| `wood/windmill/persimmon` | TFCFBlocks.java:1700 | 有 |
| `wood/windmill/quince` | TFCFBlocks.java:1700 | 有 |
| `wood/windmill/sorb` | TFCFBlocks.java:1700 | 有 |
| `wood/windmill/syzygium` | TFCFBlocks.java:1700 | 有 |
| `wood/windmill/yew` | TFCFBlocks.java:1700 | 有 |

### wood/wood（11）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `wood/wood/hawthorn` | TFCFBlocks.java:1700 | 有 |
| `wood/wood/jabuticabeira` | TFCFBlocks.java:1700 | 有 |
| `wood/wood/maclura` | TFCFBlocks.java:1700 | 有 |
| `wood/wood/medlar` | TFCFBlocks.java:1700 | 有 |
| `wood/wood/mulberry` | TFCFBlocks.java:1700 | 有 |
| `wood/wood/pear` | TFCFBlocks.java:1700 | 有 |
| `wood/wood/persimmon` | TFCFBlocks.java:1700 | 有 |
| `wood/wood/quince` | TFCFBlocks.java:1700 | 有 |
| `wood/wood/sorb` | TFCFBlocks.java:1700 | 有 |
| `wood/wood/syzygium` | TFCFBlocks.java:1700 | 有 |
| `wood/wood/yew` | TFCFBlocks.java:1700 | 有 |

### wood/wood_wall（11）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `wood/wood_wall/hawthorn` | TFCFBlocks.java:2044 | 有 |
| `wood/wood_wall/jabuticabeira` | TFCFBlocks.java:2044 | 有 |
| `wood/wood_wall/maclura` | TFCFBlocks.java:2044 | 有 |
| `wood/wood_wall/medlar` | TFCFBlocks.java:2044 | 有 |
| `wood/wood_wall/mulberry` | TFCFBlocks.java:2044 | 有 |
| `wood/wood_wall/pear` | TFCFBlocks.java:2044 | 有 |
| `wood/wood_wall/persimmon` | TFCFBlocks.java:2044 | 有 |
| `wood/wood_wall/quince` | TFCFBlocks.java:2044 | 有 |
| `wood/wood_wall/sorb` | TFCFBlocks.java:2044 | 有 |
| `wood/wood_wall/syzygium` | TFCFBlocks.java:2044 | 有 |
| `wood/wood_wall/yew` | TFCFBlocks.java:2044 | 有 |

### wood/workbench（11）

| id | 代码位置 | 注册表 dump |
|---|---|---|
| `wood/workbench/hawthorn` | TFCFBlocks.java:1700 | 有 |
| `wood/workbench/jabuticabeira` | TFCFBlocks.java:1700 | 有 |
| `wood/workbench/maclura` | TFCFBlocks.java:1700 | 有 |
| `wood/workbench/medlar` | TFCFBlocks.java:1700 | 有 |
| `wood/workbench/mulberry` | TFCFBlocks.java:1700 | 有 |
| `wood/workbench/pear` | TFCFBlocks.java:1700 | 有 |
| `wood/workbench/persimmon` | TFCFBlocks.java:1700 | 有 |
| `wood/workbench/quince` | TFCFBlocks.java:1700 | 有 |
| `wood/workbench/sorb` | TFCFBlocks.java:1700 | 有 |
| `wood/workbench/syzygium` | TFCFBlocks.java:1700 | 有 |
| `wood/workbench/yew` | TFCFBlocks.java:1700 | 有 |


## B 类（补 lang）

| id | 代码位置 |
|---|---|
| `coarse_dirt/loam` | TFCFBlocks.java:1349 |
| `coarse_dirt/sandy_loam` | TFCFBlocks.java:1349 |
| `coarse_dirt/silt` | TFCFBlocks.java:1349 |
| `coarse_dirt/silty_loam` | TFCFBlocks.java:1349 |
| `dense_clay_grass/loam` | TFCFBlocks.java:1349 |
| `dense_clay_grass/sandy_loam` | TFCFBlocks.java:1349 |
| `dense_clay_grass/silt` | TFCFBlocks.java:1349 |
| `dense_clay_grass/silty_loam` | TFCFBlocks.java:1349 |
| `dense_earthenware_clay_grass/loam` | TFCFBlocks.java:1349 |
| `dense_earthenware_clay_grass/sandy_loam` | TFCFBlocks.java:1349 |
| `dense_earthenware_clay_grass/silt` | TFCFBlocks.java:1349 |
| `dense_earthenware_clay_grass/silty_loam` | TFCFBlocks.java:1349 |
| `dense_grass/bog_iron` | TFCFBlocks.java:197 |
| `dense_grass/loam` | TFCFBlocks.java:1349 |
| `dense_grass/sandy_loam` | TFCFBlocks.java:1349 |
| `dense_grass/silt` | TFCFBlocks.java:1349 |
| `dense_grass/silty_loam` | TFCFBlocks.java:1349 |
| `dense_grass_path/loam` | TFCFBlocks.java:1349 |
| `dense_grass_path/sandy_loam` | TFCFBlocks.java:1349 |
| `dense_grass_path/silt` | TFCFBlocks.java:1349 |
| `dense_grass_path/silty_loam` | TFCFBlocks.java:1349 |
| `dense_stoneware_clay_grass/loam` | TFCFBlocks.java:1349 |
| `dense_stoneware_clay_grass/sandy_loam` | TFCFBlocks.java:1349 |
| `dense_stoneware_clay_grass/silt` | TFCFBlocks.java:1349 |
| `dense_stoneware_clay_grass/silty_loam` | TFCFBlocks.java:1349 |
| `dry_clay_grass/loam` | TFCFBlocks.java:1349 |
| `dry_clay_grass/sandy_loam` | TFCFBlocks.java:1349 |
| `dry_clay_grass/silt` | TFCFBlocks.java:1349 |
| `dry_clay_grass/silty_loam` | TFCFBlocks.java:1349 |
| `dry_earthenware_clay_grass/loam` | TFCFBlocks.java:1349 |
| `dry_earthenware_clay_grass/sandy_loam` | TFCFBlocks.java:1349 |
| `dry_earthenware_clay_grass/silt` | TFCFBlocks.java:1349 |
| `dry_earthenware_clay_grass/silty_loam` | TFCFBlocks.java:1349 |
| `dry_grass/loam` | TFCFBlocks.java:1349 |
| `dry_grass/sandy_loam` | TFCFBlocks.java:1349 |
| `dry_grass/silt` | TFCFBlocks.java:1349 |
| `dry_grass/silty_loam` | TFCFBlocks.java:1349 |
| `dry_grass_path/loam` | TFCFBlocks.java:1349 |
| `dry_grass_path/sandy_loam` | TFCFBlocks.java:1349 |
| `dry_grass_path/silt` | TFCFBlocks.java:1349 |
| `dry_grass_path/silty_loam` | TFCFBlocks.java:1349 |
| `dry_stoneware_clay_grass/loam` | TFCFBlocks.java:1349 |
| `dry_stoneware_clay_grass/sandy_loam` | TFCFBlocks.java:1349 |
| `dry_stoneware_clay_grass/silt` | TFCFBlocks.java:1349 |
| `dry_stoneware_clay_grass/silty_loam` | TFCFBlocks.java:1349 |
| `loose_mud/loam` | TFCFBlocks.java:1349 |
| `loose_mud/sandy_loam` | TFCFBlocks.java:1349 |
| `loose_mud/silt` | TFCFBlocks.java:1349 |
| `loose_mud/silty_loam` | TFCFBlocks.java:1349 |
| `mossy_packed_mud/loam` | TFCFBlocks.java:1349 |
| `mossy_packed_mud/sandy_loam` | TFCFBlocks.java:1349 |
| `mossy_packed_mud/silt` | TFCFBlocks.java:1349 |
| `mossy_packed_mud/silty_loam` | TFCFBlocks.java:1349 |
| `packed_mud/loam` | TFCFBlocks.java:1349 |
| `packed_mud/sandy_loam` | TFCFBlocks.java:1349 |
| `packed_mud/silt` | TFCFBlocks.java:1349 |
| `packed_mud/silty_loam` | TFCFBlocks.java:1349 |
| `sparse_clay_grass/loam` | TFCFBlocks.java:1349 |
| `sparse_clay_grass/sandy_loam` | TFCFBlocks.java:1349 |
| `sparse_clay_grass/silt` | TFCFBlocks.java:1349 |
| `sparse_clay_grass/silty_loam` | TFCFBlocks.java:1349 |
| `sparse_earthenware_clay_grass/loam` | TFCFBlocks.java:1349 |
| `sparse_earthenware_clay_grass/sandy_loam` | TFCFBlocks.java:1349 |
| `sparse_earthenware_clay_grass/silt` | TFCFBlocks.java:1349 |
| `sparse_earthenware_clay_grass/silty_loam` | TFCFBlocks.java:1349 |
| `sparse_grass/bog_iron` | TFCFBlocks.java:184 |
| `sparse_grass/loam` | TFCFBlocks.java:1349 |
| `sparse_grass/sandy_loam` | TFCFBlocks.java:1349 |
| `sparse_grass/silt` | TFCFBlocks.java:1349 |
| `sparse_grass/silty_loam` | TFCFBlocks.java:1349 |
| `sparse_grass_path/loam` | TFCFBlocks.java:1349 |
| `sparse_grass_path/sandy_loam` | TFCFBlocks.java:1349 |
| `sparse_grass_path/silt` | TFCFBlocks.java:1349 |
| `sparse_grass_path/silty_loam` | TFCFBlocks.java:1349 |
| `sparse_stoneware_clay_grass/loam` | TFCFBlocks.java:1349 |
| `sparse_stoneware_clay_grass/sandy_loam` | TFCFBlocks.java:1349 |
| `sparse_stoneware_clay_grass/silt` | TFCFBlocks.java:1349 |
| `sparse_stoneware_clay_grass/silty_loam` | TFCFBlocks.java:1349 |

## C 类（补模型）

| id | 代码位置 |
|---|---|
