# tag 补全：把审计漏掉的 id 也纳入 1:1 映射

## 问题

上一轮的 tag 生成只用「源码审计白名单」（5666 blocks / 5674 items），
但审计解析器漏了若干 mapper 写法，典型如：

- `ore/*`：`Helpers.mapOfKeys(rock, p1, ore -> mapOfKeys(Ore.class, p2, ...))` 这种带 predicate 的**嵌套** mapOfKeys；
- `ceramic/*`、`food/*`、`wood/lumber/*`、`sandstone/layered/*`。

结果这批 id 拿不到 TFC 同款 tag，而旧 tag 文件里其实一直在引用它们。

## 做法

`tools/build_whitelist.py` 构建**第二来源**并入白名单：

```
完整白名单 = 审计白名单 ∪ 旧 tag 文件（ef0e95394 版）引用过的 tfcflorae id − 已移除家族
```

「旧 tag 文件」取的是生成器第一次运行**之前**的提交，那里面的 id 是 1.18 移植时写入、
且游戏一直能加载的，可信度高于「资源存在」（`wood/wood/joshua_tree` 就是有资源但没注册，
当初正是它让 `minecraft:logs` 失效、所有树叶消失）。

排除项：`REMOVED_SOILS`（20 类土壤）、`rock/loose/flint`、`rock/mossy_loose/flint`。

## 结果

| | 审计白名单 | 完整白名单 |
|---|---:|---:|
| blocks | 5666 | **5756** |
| items | 5674 | **6237** |
| 拿到 TFC 同款 tag 的 blocks | 2480 | **2624** |
| 拿到 TFC 同款 tag 的 items | 2223 | **2632** |
| 岩质归属 | 2108 | **2396** |
| tag 文件 | 217 | **245** |

## 安全校验（这次全做了）

1. **无无效 id**：所有 tag 里 38,185 条 `tfcflorae:` 引用，逐条对照完整白名单 → **0 条无效**。
2. **无断链引用**：所有 `#tfcflorae:xxx` 引用都有对应 tag 文件 → **0 条缺失**
   （另有 73 条 `#tfc:xxx` 指向 TFC 自带 tag，属正常）。
3. `minecraft:logs` 仍为 351 条，不含 `joshua_tree`。

## 仍未覆盖的部分

- 3598 个 block / 3605 个 item 仍然没有 TFC 同款 tag。其中大部分是 TFC 自己也不打 tag 的类型
  （多数 `slab`/`stairs`/`wall`、`plant/*` 的细分变体等），按 1:1 原则保持不打。
- 少部分（如 `sand/rocky/*`、`rockier_compact_dirt/*`）TFC 没有对应体系，也就没有可复制的 tag。

## 复现

```
python tools/audit_register_assets.py --dump-ids reports/registered-ids.json
python tools/build_whitelist.py            # -> reports/registered-ids-full.json
python tools/gen_tags.py                   # 245 个 tag
python tools/prune_dead_tag_ids.py         # 可选：清死条目
```
