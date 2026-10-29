# 注册 / 资源审计（A/B/C）

由 `tools/audit_register_assets.py` 生成。判定依据：展开源码里的 `register(...)` 调用（含 `mapOfKeys` / `for` 循环变量），再对照 `assets/tfcflorae/{blockstates,models/block,models/item}` 与 `lang/{zh_cn,en_us}.json`，并用 `reports/runtime-registries.json`（真实注册表 dump）交叉验证。

| 类别 | 数量 | 含义 | 处理 |
|---|---|---|---|
| A | 0 | 注册了但无 lang、无任何模型 | 删除注册 |
| B | 0 | 有模型、缺 lang | 补翻译 |
| C | 54 | 有 lang、缺模型 | 补模型 |

统计：方块 5597、物品 5603；未解析注册点 52；expander-only 42；dump-only 4457。

## A 类（删注册）


## B 类（补 lang）

| id | 代码位置 |
|---|---|

## C 类（补模型）

| id | 代码位置 |
|---|---|
| `dense_clay_grass/loam` | TFCFBlocks.java:1346 |
| `dense_clay_grass/sandy_loam` | TFCFBlocks.java:1346 |
| `dense_clay_grass/silt` | TFCFBlocks.java:1346 |
| `dense_clay_grass/silty_loam` | TFCFBlocks.java:1346 |
| `dense_earthenware_clay_grass/loam` | TFCFBlocks.java:1346 |
| `dense_earthenware_clay_grass/sandy_loam` | TFCFBlocks.java:1346 |
| `dense_earthenware_clay_grass/silt` | TFCFBlocks.java:1346 |
| `dense_earthenware_clay_grass/silty_loam` | TFCFBlocks.java:1346 |
| `dense_grass/bog_iron` | TFCFBlocks.java:204 |
| `dense_grass/loam` | TFCFBlocks.java:1346 |
| `dense_grass/sandy_loam` | TFCFBlocks.java:1346 |
| `dense_grass/silt` | TFCFBlocks.java:1346 |
| `dense_grass/silty_loam` | TFCFBlocks.java:1346 |
| `dense_stoneware_clay_grass/loam` | TFCFBlocks.java:1346 |
| `dense_stoneware_clay_grass/sandy_loam` | TFCFBlocks.java:1346 |
| `dense_stoneware_clay_grass/silt` | TFCFBlocks.java:1346 |
| `dense_stoneware_clay_grass/silty_loam` | TFCFBlocks.java:1346 |
| `dry_clay_grass/loam` | TFCFBlocks.java:1346 |
| `dry_clay_grass/sandy_loam` | TFCFBlocks.java:1346 |
| `dry_clay_grass/silt` | TFCFBlocks.java:1346 |
| `dry_clay_grass/silty_loam` | TFCFBlocks.java:1346 |
| `dry_earthenware_clay_grass/loam` | TFCFBlocks.java:1346 |
| `dry_earthenware_clay_grass/sandy_loam` | TFCFBlocks.java:1346 |
| `dry_earthenware_clay_grass/silt` | TFCFBlocks.java:1346 |
| `dry_earthenware_clay_grass/silty_loam` | TFCFBlocks.java:1346 |
| `dry_grass/loam` | TFCFBlocks.java:1346 |
| `dry_grass/sandy_loam` | TFCFBlocks.java:1346 |
| `dry_grass/silt` | TFCFBlocks.java:1346 |
| `dry_grass/silty_loam` | TFCFBlocks.java:1346 |
| `dry_stoneware_clay_grass/loam` | TFCFBlocks.java:1346 |
| `dry_stoneware_clay_grass/sandy_loam` | TFCFBlocks.java:1346 |
| `dry_stoneware_clay_grass/silt` | TFCFBlocks.java:1346 |
| `dry_stoneware_clay_grass/silty_loam` | TFCFBlocks.java:1346 |
| `mossy_packed_mud/loam` | TFCFBlocks.java:1346 |
| `mossy_packed_mud/sandy_loam` | TFCFBlocks.java:1346 |
| `mossy_packed_mud/silt` | TFCFBlocks.java:1346 |
| `mossy_packed_mud/silty_loam` | TFCFBlocks.java:1346 |
| `sparse_clay_grass/loam` | TFCFBlocks.java:1346 |
| `sparse_clay_grass/sandy_loam` | TFCFBlocks.java:1346 |
| `sparse_clay_grass/silt` | TFCFBlocks.java:1346 |
| `sparse_clay_grass/silty_loam` | TFCFBlocks.java:1346 |
| `sparse_earthenware_clay_grass/loam` | TFCFBlocks.java:1346 |
| `sparse_earthenware_clay_grass/sandy_loam` | TFCFBlocks.java:1346 |
| `sparse_earthenware_clay_grass/silt` | TFCFBlocks.java:1346 |
| `sparse_earthenware_clay_grass/silty_loam` | TFCFBlocks.java:1346 |
| `sparse_grass/bog_iron` | TFCFBlocks.java:191 |
| `sparse_grass/loam` | TFCFBlocks.java:1346 |
| `sparse_grass/sandy_loam` | TFCFBlocks.java:1346 |
| `sparse_grass/silt` | TFCFBlocks.java:1346 |
| `sparse_grass/silty_loam` | TFCFBlocks.java:1346 |
| `sparse_stoneware_clay_grass/loam` | TFCFBlocks.java:1346 |
| `sparse_stoneware_clay_grass/sandy_loam` | TFCFBlocks.java:1346 |
| `sparse_stoneware_clay_grass/silt` | TFCFBlocks.java:1346 |
| `sparse_stoneware_clay_grass/silty_loam` | TFCFBlocks.java:1346 |
