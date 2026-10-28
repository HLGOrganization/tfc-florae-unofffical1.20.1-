# 土壤整理：删掉重复与坏掉的土壤方块

## 移除的土壤类型（20 个，78 个方块）

在 `TFCFBlocks` 新增 `REMOVED_SOILS`，注册时给这些类型放**空表**（枚举常量保留，
其它地方仍有 `transform()`/switch 引用；空表让 `TFCFSOIL.get(type).values()` 是空集合而不是 NPE）。

| 分类 | 类型 | 理由 |
|---|---|---|
| 土径 | `grass_path`、`dense_grass_path`、`sparse_grass_path`、`dry_grass_path` | TFC 1.20 已有 `tfc:grass_path/<variant>`，重复 |
| 泥土 | `packed_mud`、`mossy_packed_mud`、`loose_mud`、`coarse_dirt` | 与 TFC 的泥巴/泥土体系重复或不需要 |
| 干燥草 | `dry_grass`、`dense_grass`、`sparse_grass` | 放置后变成**可透视的树叶方块**（坏的实现），不要 |
| 干燥黏土草 | `dry_clay_grass`、`dense_clay_grass`、`sparse_clay_grass` | 同上 |
| 干燥陶土草 | `dry_earthenware_clay_grass`、`dense_`、`sparse_` | 同上 |
| 干燥瓷土草 | `dry_stoneware_clay_grass`、`dense_`、`sparse_` | 同上 |

保留：`dirt`、`grass`、`farmland`、`rooted_dirt`、`clay`、`clay_grass`、`podzol`、
`mycelium_dirt`、`mud_bricks`、`drying_bricks`、`earthenware_clay`、`earthenware_clay_grass`、
`stoneware_clay`、`stoneware_clay_grass`、`compact_dirt`。

顺带修掉：`TFCFForgeEventHandler` 里「泥巴用铲子压平变 packed_mud」的分支（方块已不存在）。

## 关键：tag 必须同步剔除，否则会重演树叶全灭

审计工具从源码展开 `register()` 调用，**不会**模拟 `REMOVED_SOILS` 过滤，
所以 `--dump-ids` 出来的白名单里仍有这 78 个方块。如果 tag 继续引用它们，
就会再次出现「一个未知 id 让整个 tag 失效」——上次就是这样把所有树叶清空的。

因此：

- `tools/gen_tags.py` 增加 `REMOVED_SOILS` 过滤（生成时就不会写这些 id）；
- `tools/prune_dead_tag_ids.py` 增加同一份前缀清单，并对**已有 tag 文件**执行清理：
  18 个文件、298 个条目被移除（如 `minecraft:dirt` 里残留的
  `tfcflorae:coarse_dirt/loam`、`tfcflorae:dense_grass/loam` 等）。

验证：jar 内所有 tag 中，指向已移除土壤的条目 **0** 条。

## 资源与数据

方块已不注册，但 `assets/`（blockstate/model/texture）、`data/`（loot/recipe/tag）里的对应文件仍留着，
属于孤儿资源，不影响运行，也已被 tag 清理排除。需要的话可以再单独做一轮资源清理。

## 验证

- `compileJava` EXIT=0（23s）、`assemble` EXIT=0（4m24s）
- 审计：A=0 / B=0 / C=0
- jar：`minecraft:logs` 351 条、无已移除土壤引用；66,822 entries
- 已部署，旧包备份 `.bak-20261028-220231-270`
