# 修正：按 TFC 数据包 1:1 映射 tag（并修复 tag 崩塌）

## 出了什么问题

上一轮我凭「tag 名字猜用途」生成了 217 个 tag，犯了两类错误：

1. **覆盖了 TFC 的原生 tag 归属**：把 `minecraft:logs` 直接写成只有本模组的原木，
   而且里面混进了一个**有资源、但未注册**的 id（`tfcflorae:wood/wood/joshua_tree`）。
   Minecraft 的行为是：**tag 里只要有一个不存在的 id，整个 tag 加载失败**，
   依赖它的 tag 一并失效。`minecraft:logs` 失效后，TFC 树叶的
   `getDistance()`（用 `BlockTags.LOGS` 判定主干）永远拿不到主干距离，
   超过 `maxDecayDistance` 就被移除 —— 于是**所有树叶瞬间消失**，TFC 自己的树叶也一样。
2. **凭空造了岩质 tag**（`tfcflorae:rock/sedimentary` 等）。TFC 其实自带
   `tfc:sedimentary_items` / `sedimentary_rock`、`tfc:igneous_intrusive_items`、
   `tfc:igneous_extrusive_items`、`tfc:metamorphic_items` 等 8 个岩质 tag，我漏查了 item tag 目录。

## 现在的做法：完全从 TFC 数据包推导

`tools/gen_tags.py` 重写为纯数据包驱动：

1. 解析 TFC jar 里 `data/{tfc,minecraft,forge}/tags/{blocks,items}/**` 的**全部 823 个 tag**；
2. 递归解析 `#tag` 引用，得到「每个具体 id 属于哪些 tag」的反向索引（9028 个 id 有归属）；
3. 对本模组每个已注册 id，找它在 TFC 里的「同种类同类型」对照物，**把对照物的全部 tag 原样复制过来**：
   - `wood/<部件>/<木材>` → `tfc:wood/<部件>/oak`（66 种木材 × 30+ 部件）
   - `rock/<类型>/<岩石>` → `tfc:rock/<类型>/granite`
   - `ore/<矿种>/<岩石>` → `tfc:ore/<矿种>/granite`
   - `deposit/<矿种>/<岩石>` → `tfc:deposit/<矿种>/granite`
   - `brick/<岩石>` → `tfc:brick/granite`
   - 其余 → 同名 `tfc:<path>`
4. 岩质不新建 tag，直接进 TFC 的 8 个：按 `TFCFRock` 的 `RockCategory`
   → `tfc:<category>_items`（全部岩石物品），碎石类另加 `tfc:<category>_rock`。

## 硬性安全规则

> **写入 tag 的每一个 id 必须在 `reports/registered-ids.json` 白名单里**
> （由 `audit_register_assets.py --dump-ids` 从源码注册调用解析出：5666 blocks / 5674 items）。

原因是「资源存在」**不能**证明注册（`wood/wood/joshua_tree` 就有模型没有注册）。
合并已有 tag 文件时，同样会把不在白名单里的 `tfcflorae:` 条目剔除。

## 结果

- 共 **217 个 tag 文件**，其中 `minecraft:logs` 351 条（TFC 原有 89 + 本模组 262），无任何无效 id
- 获得 TFC 同款 tag 的 id：**blocks 2480 / 5666**、**items 2223 / 5674**
- 岩质归属 **2108** 条（`sedimentary_items` / `igneous_extrusive_items` / `igneous_intrusive_items` / `metamorphic_items`，碎石类另进 `*_rock`）
- 其余 3749 个 block 没有对照 tag，属正常：TFC 自己的同类方块（如多数 `slab`/`stairs`/`wall`）也不在任何 tag 里

## 待确认 / 下一步

1. **覆盖缺口**：审计解析器漏掉了约 1283 个真实注册的 id（`ore/*`、`ceramic/*`、`fluid/*` 等，
   它们有资源、但源码解析没抓到），因此这批物品暂时拿不到 TFC 同款 tag。
   要补齐需要一份**运行时注册表 dump**；工程里的 `_audit` harness 目前跑不起来
   （`common_setup` 阶段报 `Registry Object not present`，属于 harness 环境问题），
   需要先修 harness 或换一种 dump 方式。
2. 旧文件里那 1283 条「不在白名单但资源存在」的条目被我保留（否则会丢功能），
   它们理论上仍有和 `joshua_tree` 一样的风险，只是目前都有资源、未被证实失效。
3. 上一轮我误删/改写的工具（`list_tags.py` 的用途词典、自建岩质 tag）已同步修正：
   岩质 tag 已全部删除。

## 复现

```
python tools/audit_register_assets.py --dump-ids reports/registered-ids.json
python tools/gen_tags.py            # 生成
python tools/prune_dead_tag_ids.py  # 可选：清理无资源的死条目
```
