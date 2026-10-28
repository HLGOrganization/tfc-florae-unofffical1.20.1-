# 凝灰岩色板替换（不重绘）

按用户提供的 16×16 凝灰岩参考图（`tools/reference/tuff_raw.png`）给整套凝灰岩贴图换色，
**不重绘任何图案**，只把「岩石部分」的像素颜色映射到这套 5 色板。

## 参考图与色板

参考图（`tools/reference/tuff_raw.png`，sha256 `c6ca9c6e…`）是 5 色像素画，按亮度从暗到亮：

| 档位 | 颜色 | 参考图内像素数 |
|---|---|---|
| 0 | `#4D5046` (77, 80, 70) | 31 |
| 1 | `#5D5D52` (93, 93, 82) | 71 |
| 2 | `#6A6E6F` (106, 110, 111) | 76 |
| 3 | `#85837B` (133, 131, 123) | 69 |
| 4 | `#A0A297` (160, 162, 151) | 9 |

## 为什么需要「限定色盘」

很多凝灰岩方块不是纯岩石贴图：

| 贴图 | 组成 |
|---|---|
| `sand/rocky/<color>/tuff`、`sand/sandy_tiles/...` | 岩石碎砾 + 彩色沙子 |
| `rockier/rocky/rockiest/pebble_compact_dirt/<soil>/tuff` | 岩石碎块 + 泥土 |
| `dirty/dirtier/dirtiest_stone_tiles/<soil>/tuff` | 石砖 + 泥土 |
| `rock/magma/tuff` | 岩石底 + 岩浆（3 帧动画） |
| `rock/mossy_*/tuff` | 岩石 + 苔藓 |

整图换色会把沙子、泥土、岩浆、苔藓一起染色，所以脚本先用**兄弟贴图求岩石掩码**：

> 同一张贴图的其他岩石版本（`tuff` 换成 `arkose`/`boninite`/… 共 12~16 个）在
> 非岩石部分（沙、土、苔藓、岩浆）像素完全一致，只在岩石像素上不同。
>
> `rock_mask = { 像素 | 并非所有兄弟版本在该点完全相同 }`

只对掩码内像素换色，掩码外的像素原样保留。

## 映射方式

对每张贴图的岩石像素按亮度**分位映射**到 5 档，分位点直接取自参考图的颜色分布
（累积 12.1% / 39.8% / 69.5% / 96.5%），因此结果的颜色配比与参考图一致，
而不是平均分成 20% 一档。每张图独立归一化，保证各自的明暗层次都在。

`block/rock/raw/tuff.png` 直接写入参考图本身（用户提供的就是岩石本体贴图），
其余 102 张贴图由它的色板派生。

## 结果

- 处理 103 张：`raw` 替换为参考图，其余 102 张完成岩石部分换色
- 验证（数值）：
  - `raw` 5 色，分布 76/70/70/32/8（参考图 76/71/69/31/9）
  - `magma` 11 色 = 5 色板 + 6 种岩浆色（岩浆完整保留）
  - `sand/rocky/black` = 黑沙 3 色 + 5 色板（沙色保留）
  - `mossy_cobble` = 苔藓 4 色 + 5 色板（苔藓保留）
- 对比图：`reports/tuff-palette-preview.png`（上排为替换后，下排为原始 boninite 版本）
- `entity/chest/**` 6 张箱子实体贴图**未处理**（不是岩石贴图）

## 复现

```
python tools/apply_tuff_palette.py           # 预演，打印每张图的像素统计
python tools/apply_tuff_palette.py --apply   # 写入
python tools/preview_tuff_palette.py         # 重新生成对比图
```

脚本幂等：岩石像素已经是色板颜色时自动跳过；`raw` 与参考图相同也跳过。
换色板只需替换 `tools/reference/tuff_raw.png` 后重跑（需先 `git checkout` 贴图到原始状态）。
