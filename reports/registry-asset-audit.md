# 注册 / 资源审计（A/B/C）

由 `tools/audit_register_assets.py` 生成。判定依据：展开源码里的 `register(...)` 调用（含 `mapOfKeys` / `for` 循环变量），再对照 `assets/tfcflorae/{blockstates,models/block,models/item}` 与 `lang/{zh_cn,en_us}.json`，并用 `reports/runtime-registries.json`（真实注册表 dump）交叉验证。

| 类别 | 数量 | 含义 | 处理 |
|---|---|---|---|
| A | 0 | 注册了但无 lang、无任何模型 | 删除注册 |
| B | 19 | 有模型、缺 lang | 补翻译 |
| C | 2 | 有 lang、缺模型 | 补模型 |

统计：方块 5667、物品 5676；未解析注册点 48；expander-only 76；dump-only 4421。

## A 类（删注册）


## B 类（补 lang）

| id | 代码位置 |
|---|---|
| `brick/tuff` | TFCFItems.java:83 |
| `rock/anvil/tuff` | TFCFBlocks.java:692 |
| `rock/aqueduct/tuff` | TFCFBlocks.java:3522 |
| `rock/bricks/tuff` | TFCFBlocks.java:3522 |
| `rock/button/tuff` | TFCFBlocks.java:3522 |
| `rock/chiseled/tuff` | TFCFBlocks.java:3522 |
| `rock/cobble/tuff` | TFCFBlocks.java:3522 |
| `rock/cracked_bricks/tuff` | TFCFBlocks.java:3522 |
| `rock/gravel/tuff` | TFCFBlocks.java:3522 |
| `rock/hardened/tuff` | TFCFBlocks.java:3522 |
| `rock/loose/tuff` | TFCFBlocks.java:3522 |
| `rock/magma/tuff` | TFCFBlocks.java:708 |
| `rock/mossy_bricks/tuff` | TFCFBlocks.java:3522 |
| `rock/mossy_cobble/tuff` | TFCFBlocks.java:3522 |
| `rock/mossy_loose/tuff` | TFCFBlocks.java:3522 |
| `rock/pressure_plate/tuff` | TFCFBlocks.java:3522 |
| `rock/raw/tuff` | TFCFBlocks.java:3522 |
| `rock/smooth/tuff` | TFCFBlocks.java:3522 |
| `rock/spike/tuff` | TFCFBlocks.java:3522 |

## C 类（补模型）

| id | 代码位置 |
|---|---|
| `soil_pile/silty_loam` | TFCFItems.java:97 |
| `wood/roots/white_mangrove/silty_loam` | TFCFBlocks.java:2349 |
